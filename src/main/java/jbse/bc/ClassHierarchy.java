package jbse.bc;

import static jbse.bc.ClassFile.JAVA_8;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.ClassLoaders.CLASSLOADER_EXT;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.common.Type.className;
import static jbse.common.Type.classNameContained;
import static jbse.common.Type.classNameContainer;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;
import static jbse.common.Type.isVoid;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;
import static jbse.common.Type.TYPEEND;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jbse.bc.exc.AlreadyDefinedClassException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Util;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * Class handling a hierarchy of Java classes as specified 
 * by some {@link ClassFileFactory}.
 *  
 * @author Pietro Braione
 */
public final class ClassHierarchy implements Cloneable {
	/**
	 * The {@link Classpath} of symbolic execution, where 
	 * all the classfiles are picked.
	 */
    private final Classpath cp;
    
    /**
     * The expansion backdoor, associating class names to sets of 
     * names of their subclasses. It is used in place of the class 
     * hierarchy to perform expansion of symbolic references.
     */
    private final Map<String, Set<String>> expansionBackdoor;
    
    /**
     * Associates class names to the class names of the corresponding 
     * model classes that replace them. It is not mutated.
     */
    private final HashMap<String, String> modelClassSubstitutions;
    
    /** 
     * Private classpath where all the model classes are picked;
     * It is nothing more nothing less than the classpath of the 
     * JBSE execution. It is not mutated. 
     */
    private final ArrayList<Path> implementationClassPath;
    
    /** The {@link ClassFileFactory} used to create {@link ClassFile}s. */
    private final ClassFileFactory f;
    
    /**
     * The {@link ClassFileStore} used to store the {@link ClassFile}s
     * after they have been loaded. Not final because of clone.
     */
    private ClassFileStore cfs;
    
    /**
     * Associates the names of the system packages to the 
     * jar file or directory from which the classes in the 
     * package were loaded from. Not final because of clone.
     */
    private HashMap<String, Path> systemPackages;
    
    private static class FindBytecodeResult {
        final byte[] bytecode;
        final Path loadedFrom;
        
        public FindBytecodeResult(byte[] bytecode, Path loadedFrom) {
            this.bytecode = bytecode;
            this.loadedFrom = loadedFrom;
        }
   }

    /**
     * Constructor.
     * 
     * @param classPath a {@link Classpath}. It must not be {@code null}.
     * @param factoryClass a {@link Class}{@code <? extends }{@link ClassFileFactory}{@code >}.
     *        It must not be {@code null} and have an accessible parameterless constructor.
     * @param expansionBackdoor a 
     *        {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>}
     *        associating class names to sets of names of their subclasses. It 
     *        is used in place of the class hierarchy to perform expansion of 
     *        symbolic references. It must not be {@code null}.
     * @param modelClassSubstitutions a 
     *        {@link Map}{@code <}{@link String}{@code , }{@link String}{@code >}
     *        associating class names to the class names of the corresponding 
     *        model classes that replace them. 
     * @throws InvalidClassFileFactoryClassException in the case {@link fClass}
     *         has not the expected features (missing constructor, unaccessible 
     *         constructor...).
     * @throws InvalidInputException if 
     *         {@code classPath == null || factoryClass == null || expansionBackdoor == null || modelClassSubstitutions == null}.
     */
    public ClassHierarchy(Classpath classPath, Class<? extends ClassFileFactory> factoryClass, Map<String, Set<String>> expansionBackdoor, Map<String, String> modelClassSubstitutions)
    throws InvalidClassFileFactoryClassException, InvalidInputException {
    	if (classPath == null || factoryClass == null || expansionBackdoor == null || modelClassSubstitutions == null) {
    		throw new InvalidInputException("Attempted creation of a " + this.getClass().getName() + " with a null classPath, or factoryClass, or expansionBackdoor, or modelClassSubstitutions.");
    	}
        this.cp = classPath.clone(); //safety copy
        this.cfs = new ClassFileStore();
        this.expansionBackdoor = new HashMap<>(expansionBackdoor); //safety copy
        this.modelClassSubstitutions = new HashMap<>(modelClassSubstitutions); //safety copy
        this.implementationClassPath = new ArrayList<>();
        final ClassLoader cl = ClassLoader.getSystemClassLoader();
        final URL[] urls = ((URLClassLoader) cl).getURLs();
        for (URL url : urls) {
        	try {
				this.implementationClassPath.add(Paths.get(url.toURI()));
			} catch (URISyntaxException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
        }
        try {
            this.f = factoryClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidClassFileFactoryClassException(e);
        }
        this.systemPackages = new HashMap<>();
    }

    /**
     * Returns the {@link Classpath} of this hierarchy.
     * 
     * @return a {@link Classpath} (safety copy).
     */
    public Classpath getClasspath() {
        return this.cp.clone();
    }
    
    /**
     * Creates a dummy {@link ClassFile} for an ordinary (instance) class without
     * adding it to the hierarchy.
     * 
     * @param definingClassLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param className a {@link String}, the name of the class.
     * @param bytecode a {@code byte[]}, the bytecode for the class.
     * @return a dummy {@link ClassFile} for the class.
     * @throws InvalidInputException if any of the parameters has an invalid
     *         value.
     * @throws ClassFileIllFormedException if {@code bytecode} is ill-formed.
     */
    public ClassFile createClassFileClassDummy(int definingClassLoader, String className, byte[] bytecode) 
    throws InvalidInputException, ClassFileIllFormedException {
        final ClassFile retval =
            this.f.newClassFileClass(definingClassLoader, className, bytecode, null, null);
        return retval;
    }
    
    /**
     * Creates a {@link ClassFile} for an ordinary (instance) class without
     * adding it to the hierarchy.
     * 
     * @param classFile a dummy {@link ClassFile} for the class to be added. It must have 
     *        been created with {@link #createClassFileClassDummy(int, String, byte[]) createClassFileClass}.
     * @param superClass a {@link ClassFile} for {@code classFile}'s superclass. It must agree with 
     *        {@code classFile.}{@link ClassFile#getSuperclassName() getSuperclassName()}.
     * @param superInterfaces a {@link ClassFile}{@code []} for {@code classFile}'s superinterfaces. It must agree with 
     *        {@code classFile.}{@link ClassFile#getSuperInterfaceNames() getSuperInterfaceNames()}.
     * @throws InvalidInputException if any of the parameters has an invalid
     *         value.
     */
    public ClassFile createClassFileClass(ClassFile classFile, ClassFile superClass, ClassFile[] superInterfaces) 
    throws InvalidInputException {
        if (classFile == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".createClassFileClass() with a classFile parameter that has value null.");
        }
        if (!classFile.isDummy()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".createClassFileClass() with a classFile parameter that is not dummy.");
        }
        if (!classFile.isReference()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".createClassFileClass() with a classFile parameter that is not an object classfile but a classfile for class " + classFile.getClassName() + ".");
        }
        final ClassFile retVal;
        try {
            retVal =
                this.f.newClassFileClass(classFile.getDefiningClassLoader(), classFile.getClassName(), classFile.getBinaryFileContent(), superClass, superInterfaces);
        } catch (ClassFileIllFormedException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        return retVal;
    }
    
    /**
     * Creates a {@link ClassFile} for an array class without
     * adding it to the hierarchy.
     * 
     * @param className a {@link String}, the name of the class.
     * @param memberClass a {@link ClassFile} for the array member.
     *        It is not a dummy classfile.
     * @return a {@link ClassFile}.
     * @throws InvalidInputException
     */
    public ClassFile createClassFileArray(String className, ClassFile memberClass) 
    throws InvalidInputException {
        //Note that initialization put all the following 
        //standard classes in the loaded class cache
        final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
        if (cf_JAVA_OBJECT == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Object.");
        }
        final ClassFile cf_JAVA_CLONEABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLONEABLE); //surely loaded
        if (cf_JAVA_CLONEABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Cloneable.");
        } 
        final ClassFile cf_JAVA_SERIALIZABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_SERIALIZABLE); //surely loaded
        if (cf_JAVA_SERIALIZABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArrays was unable to find standard class java.lang.Cloneable.");
        }

        final ClassFile retval =
            this.f.newClassFileArray(className, memberClass, cf_JAVA_OBJECT, cf_JAVA_CLONEABLE, cf_JAVA_SERIALIZABLE);
        return retval;
    }
    
    /**
     * Adds a {@link ClassFile} to this hierarchy.
     * 
     * @param initiatingLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param classFile a {@link ClassFile} for the class to be added.
     * @throws InvalidInputException if {@code initiatingLoader} is invalid (negative),
     *         or {@code classFile == null}, or {@code classFile.}{@link ClassFile#isPrimitiveOrVoid() isPrimitiveOrVoid()}, or 
     *         {@code classFile.}{@link ClassFile#isAnonymousUnregistered() isAnonymousUnregistered()}, 
     *         or {@code classFile.}{@link ClassFile#isDummy() isDummy()}, 
     *         or there is already a different {@link ClassFile} in the loaded class cache for the pair
     *         {@code (initiatingLoader, classFile.}{@link ClassFile#getClassName() getClassName}{@code ())}.
     */
    public void addClassFileClassArray(int initiatingLoader, ClassFile classFile) 
    throws InvalidInputException {
        this.cfs.putClassFile(initiatingLoader, classFile);
    }
    
    /**
     * Given a class name and the identifier of an initiating class loader 
     * returns the corresponding {@link ClassFile} stored in the loaded 
     * class cache, if present. It does not manage primitive classes.
     * 
     * @param initiatingLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param className a {@link String}, the name of a class.
     * @return the {@link ClassFile} corresponding to the pair 
     *         {@code (initiatingLoader, className)} in the
     *         loaded class cache, if there is one, {@code null}
     *         otherwise. 
     */
    public ClassFile getClassFileClassArray(int initiatingLoader, String className) {
        return this.cfs.getClassFile(initiatingLoader, className);
    }
    
    /**
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the canonical name of a primitive type 
     *        or of void (see JLS v8, section 6.7).
     * @return the {@link ClassFile} of the correspondent class.
     * @throws InvalidInputException when {@code typeName}
     *         is not the canonical name of a primitive type.
     */
    public ClassFile getClassFilePrimitiveOrVoid(String typeName)
    throws InvalidInputException {
        final ClassFile retval = 
            this.cfs.getClassFilePrimitiveOrVoid(toPrimitiveOrVoidInternalName(typeName));
        return retval;
    }
    
    /**
     * Creates a dummy {@link ClassFile} for an anonymous (in the sense of
     * {@link sun.misc.Unsafe#defineAnonymousClass}) class.
     * 
     * @param hostClass a {@link ClassFile}, the host class for the
     *        anonymous class.
     * @param bytecode a {@code byte[]}, the bytecode for the anonymous class.
     * @return a dummy {@link ClassFile} for the anonymous class.
     * @throws ClassFileIllFormedException if {@code bytecode} is ill-formed.
     * @throws InvalidInputException if {@code bytecode == null}.
     */
    public ClassFile createClassFileAnonymousDummy(ClassFile hostClass, byte[] bytecode) 
    throws ClassFileIllFormedException, InvalidInputException {
        final ClassFile retval =
            this.f.newClassFileAnonymous(hostClass, bytecode, null, null, null);
        return retval;
    }

    /**
     * Creates a {@link ClassFile} for an anonymous (in the sense of
     * {@link sun.misc.Unsafe#defineAnonymousClass}) class.
     *
     * @param classFile a dummy {@link ClassFile} created with a previous invocation 
     *        of {@link #createClassFileAnonymousDummy(byte[])}.
     * @param superClass a {@link ClassFile} for {@code classFile}'s superclass. It must agree with 
     *        {@code classFile.}{@link ClassFile#getSuperclassName() getSuperclassName()}.
     * @param superInterfaces a {@link ClassFile}{@code []} for {@code classFile}'s superinterfaces. It must agree with 
     *        {@code classFile.}{@link ClassFile#getSuperInterfaceNames() getSuperInterfaceNames()}.
     * @param cpPatches a {@link Object}{@code []}; The i-th element of this
     *        array patches the i-th element in the constant pool defined
     *        by the {@code bytecode} (the two must agree). Note that 
     *        {@code cpPatches[0]} and all the {@code cpPatches[i]} with {@code i} equal 
     *        or greater than the size of the constant pool in {@code classFile} are ignored.
     *        It can be {@code null} to signify no patches.
     * @return the {@link ClassFile} for the anonymous class; It will be different
     *         from {@code classFile} because it will have the host class set and
     *         the classpath patches applied.
     * @throws InvalidInputException  if any of the parameters has an invalid
     *         value.
     */
    public ClassFile createClassFileAnonymous(ClassFile classFile, ClassFile superClass, ClassFile[] superInterfaces, Object[] cpPatches) 
    throws InvalidInputException {
        if (classFile == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".addClassFileAnonymous() with a classFile parameter that has value null.");
        }
        if (!classFile.isAnonymousUnregistered()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".addClassFileAnonymous() with a classFile parameter that is not anonymous.");
        }
        final ClassFile retVal;
        try {
            retVal = this.f.newClassFileAnonymous(classFile.getHostClass(), classFile.getBinaryFileContent(), superClass, superInterfaces, cpPatches);
        } catch (ClassFileIllFormedException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        return retVal;
    }
    
    @Deprecated
    public void addClassFileAnonymous(ClassFile classFile) throws InvalidInputException {
        this.cfs.putClassFileAnonymous(classFile);
    }
    
    /**
     * Adds a pair (supertype, subtype) to the expansion backdoor
     * provided at construction time.
     * 
     * @param supertype a {@link String}, the supertype.
     * @param subtype a {@link String}, the subtype.
     */
    public void addToExpansionBackdoor(String supertype, String subtype) {
        Set<String> expansions = this.expansionBackdoor.get(supertype);
        if (expansions == null) {
            expansions = new HashSet<>();
            this.expansionBackdoor.put(supertype, expansions);
        }
        if (!expansions.contains(subtype)) {
            expansions.add(subtype);
        }
    }
    
    /**
     * Returns the jar file or directory from which the classes
     * in a system package were loaded from.
     * 
     * @param packageName a {@link String}, the name of a package
     * @return the {@link Path} of the jar file or directory from which 
     *         the system classes in {@code packageName} were loaded from, 
     *         or {@code null} if no system class from {@code packageName}
     *         was loaded. 
     */
    public Path getSystemPackageLoadedFrom(String packageName) {
        return this.systemPackages.get(packageName);
    }
    
    /**
     * Gets the system packages.
     * 
     * @return a {@link Set}{@code <}{@link String}{@code >} of all
     *         the system package names.
     */
    public Set<String> getSystemPackages() {
        return new HashSet<>(this.systemPackages.keySet());
    }

    /**
     * Lists the concrete subclasses of a class. <br />
     * <em>Note:</em> An exact implementation of this method, 
     * searching the classpath for all the concrete subclasses 
     * of an arbitrary class, would be too demanding. Thus this
     * implementation returns {@code classFile.}{@link ClassFile#getClassName() getClassName()}, 
     * if it is not an interface or an abstract class, and all the classes 
     * associated to {@code classFile.}{@link ClassFile#getClassName() getClassName()}, 
     * in the expansion backdoor provided at construction time.
     * 
     * @param classFile a {@link ClassFile}.
     * @return A {@link Set}{@code <}{@link ClassFile}{@code >} of 
     *         subclasses of {@code classFile}.
     * @throws InvalidInputException if {@code classFile == null} or 
     *         one of the subclass names in the expansion backdoor 
     *         is ill-formed.
     * @throws ClassFileNotFoundException when the class file 
     *         for one of the subclass names 
     *         in the expansion backdoor does not exist neither 
     *         in the bootstrap, nor in the 
     *         extension, nor in the application classpath.
     * @throws ClassFileIllFormedException when the classfile 
     *         for one of the subclass names 
     *         in the expansion backdoor is invalid.
     * @throws ClassFileIllFormedException when a subclass 
     *         in the expansion backdoor cannot access one
     *         of its superclasses/superinterfaces.
     * @throws IncompatibleClassFileException if any subclass 
     *         in the expansion backdoor has as superclass 
     *         an interface type, or as superinterface an object type.
     * @throws BadClassFileVersionException  when the bytecode 
     *         for one of the subclass names in the expansion backdoor
     *         has a version number that is unsupported by this 
     *         version of JBSE.
     * @throws RenameUnsupportedException if the classfile for one of 
     *         the subclass names in the expansion backdoor derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException  when the bytecode for one of 
     *         the subclass names in the expansion backdoor has a 
     *         class name different from that used for resolving it.
     */
    public Set<ClassFile> getAllConcreteSubclasses(ClassFile classFile)
    throws InvalidInputException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        if (classFile == null) {
            throw new InvalidInputException("Tried to get the concrete subclasses of a null classfile.");
        }
        final HashSet<ClassFile> retVal = new HashSet<>();
        if (!classFile.isAbstract()) {
            retVal.add(classFile);
        }
        final Set<String> moreSubclasses = this.expansionBackdoor.get(classFile.getClassName());
        if (moreSubclasses != null) {
            for (String subclassName : moreSubclasses) {
                try {
                    final ClassFile subclass = loadCreateClass(CLASSLOADER_APP, subclassName, true);
                    if (subclass.isSubclass(classFile)) {
                        retVal.add(subclass);
                    }
                } catch (PleaseLoadClassException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                } 
            }
        }
        return retVal;
    }
    
    /**
     * Performs class creation and loading (see JVMS v8 section 5.3) with the 
     * bootstrap classloader. The created
     * {@link ClassFile} will be registered in this hierarchy's loaded class cache.
     * Also creates recursively the superclasses and superinterfaces up in the
     * hierarchy. Before attempting to create/load a class it first tries to 
     * fetch it from the loaded class cache, so no class is created twice.
     * It is equivalent to 
     * {@link #loadCreateClass(int, String, boolean) loadCreateClass}{@code (}{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}{@code , classSignature, false)}.
     * 
     * @param classSignature a {@link String}, the symbolic name of a class (array or object) 
     *        or interface.
     * @return the created {@link ClassFile}.
     * @throws InvalidInputException if {@code classSignature} is invalid.
     * @throws ClassFileNotFoundException if a class file for {@code classSignature} is
     *         not found; This happens when the initiating loader is the bootstrap
     *         classloader and the classfile is not found in the bootstrap classpath.
     * @throws ClassFileIllFormedException if the class file for {@code classSignature} is
     *         ill-formed; This happens when the initiating loader is the bootstrap
     *         classloader and the classfile for {@code classSignature} or for any of its 
     *         superclasses/superinterface is not found in the bootstrap classpath.
     * @throws ClassFileNotAccessibleException if during creation the recursive resolution 
     *         of a superclass/superinterface fails because the superclass/superinterface
     *         is not accessible by the subclass.
     * @throws IncompatibleClassFileException if the superclass for {@code classSignature} is 
     *         resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws BadClassFileVersionException if the classfile for {@code classSignature}  
     *         or of one of its superclasses/superinterfaces has a version number that is 
     *         unsupported by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code classSignature} derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the class name specified in {@code bytecode} is different
     *         from {@code classSignature}. 
     */
    public ClassFile loadCreateClass(String classSignature)
    throws InvalidInputException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException  {
        try {
            return loadCreateClass(CLASSLOADER_BOOT, classSignature, false);
        } catch (PleaseLoadClassException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Performs class creation and loading (see JVMS v8 section 5.3), 
     * playing the role of the bootstrap classloader if necessary. The created
     * {@link ClassFile} will be registered in this hierarchy's loaded class cache.
     * Also creates recursively the superclasses and superinterfaces up in the
     * hierarchy. Before attempting to create/load a class it first tries to 
     * fetch it from the loaded class cache, so no class is created twice.
     * This method can also bypass the standard classloading procedure
     * based on invoking the {@link ClassLoader#loadClass(String) ClassLoader.loadClass(String)}
     * method in the case the initiating loader is one of the standard loaders 
     * (extension or application classloader).
     * 
     * @param initiatingLoader an {@code int}, the identifier of the initiating loader. If it is
     *        {@code initiatingLoader == }{@link ClassLoaders#CLASSLOADER_BOOT}, loads itself the class,
     *        otherwise, if the class is not already loaded, raises {@link PleaseLoadClassException}
     *        (but see also the description of {@code bypassStandardLoading}).
     * @param classSignature a {@link String}, the symbolic name of a class (array or object) 
     *        or interface.
     * @param bypassStandardLoading a {@code boolean}. If it is {@code true} and the {@code initiatingLoader}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the class, instead of raising 
     *        {@link PleaseLoadClassException}. In this case the defining loader is the one that
     *        would load the class starting from {@code initiatingLoader} in the delegation sequence
     *        of the standard classloaders. 
     * @return the created {@link ClassFile}.
     * @throws InvalidInputException if {@code classSignature} is invalid ({@code null}), or if 
     *         {@code initiatingLoader < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, or if 
     *         {@code bypassStandardLoading == true} and {@code initiatingLoader > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}.
     * @throws ClassFileNotFoundException if a class file for {@code classSignature} is
     *         not found; This happens when the initiating loader is the bootstrap
     *         classloader and the classfile is not found in the bootstrap classpath
     *         (or when {@code bypassStandardLoading == true}, the initiating loader
     *         is the extensions or the application classloader, and the classfile
     *         is not found in their respective classpaths).
     * @throws ClassFileIllFormedException if the class file for {@code classSignature} 
     *         or for its superclass/superinterfaces is ill-formed.
     * @throws ClassFileNotAccessibleException if during creation the recursive resolution 
     *         of a superclass/superinterface fails because the superclass/superinterface
     *         is not accessible by the subclass.
     * @throws IncompatibleClassFileException if the superclass for {@code classSignature} is 
     *         resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws PleaseLoadClassException if creation cannot be performed because
     *         a superclass/superinterface must be loaded via a user-defined classloader before. 
     * @throws BadClassFileVersionException if the classfile for {@code classSignature}  
     *         or of one of its superclasses/superinterfaces has a version number that is 
     *         unsupported by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code classSignature} derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the class name specified in {@code bytecode} is different
     *         from {@code classSignature}. 
     */
    public ClassFile loadCreateClass(int initiatingLoader, String classSignature, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, PleaseLoadClassException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException  {
        //checks parameters
        if (initiatingLoader < CLASSLOADER_BOOT) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".loadCreateClass with invalid initiating loader " + initiatingLoader + ".");
        }
        if (bypassStandardLoading && initiatingLoader > CLASSLOADER_APP) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".loadCreateClass with bypassStandardLoading == true " + 
                                            "but the accessor has defining classloader with id " + initiatingLoader + ".");
        }
        if (classSignature == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".loadCreateClass with classSignature == null.");
        }

        
        //starts
        boolean rethrowInvalidInputException = false;
        try {
            //first looks in the loaded class cache
            ClassFile accessed = getClassFileClassArray(initiatingLoader, classSignature);
            if (accessed == null) {
                //nothing in the loaded class cache: must create/load
                if (isArray(classSignature)) {
                    //JVMS v8, section 5.3.3: the JVM creates a ClassFile for the array

                    //first, determines the member class and the defining class loader
                    final String memberType = getArrayMemberType(classSignature);
                    final ClassFile memberClass;
                    final int definingClassLoader;
                    if (isReference(memberType) || isArray(memberType)) {
                        memberClass = loadCreateClass(initiatingLoader, className(memberType), bypassStandardLoading);
                        definingClassLoader = memberClass.getDefiningClassLoader();
                    } else if (isPrimitive(memberType)) {
                        memberClass = getClassFilePrimitiveOrVoid(toPrimitiveOrVoidCanonicalName(memberType));
                        definingClassLoader = CLASSLOADER_BOOT;
                    } else {
                        rethrowInvalidInputException = true;
                        throw new InvalidInputException("Tried to create an array class with invalid array member type " + memberType + ".");
                    }

                    //now that the defining class loader is known, looks again 
                    //in the loaded class cache for the class (otherwise, we 
                    //might create a duplicate of an already existing class)
                    accessed = getClassFileClassArray(definingClassLoader, classSignature);
                    if (accessed == null) {
                        //nothing in the loaded class cache: creates the class
                        //and adds it to the loaded class cache, associating it 
                        //to *both* the initiating *and* the defining classloader
                        accessed = createClassFileArray(classSignature, memberClass);
                        addClassFileClassArray(initiatingLoader, accessed);
                        addClassFileClassArray(definingClassLoader, accessed);
                    }
                } else if (initiatingLoader == CLASSLOADER_BOOT || bypassStandardLoading) {
                    //JVMS v8, section 5.3.1: the JVM loads a ClassFile from the classpath

                    //first, looks for the bytecode in the filesystem and determines
                    //the defining classloader based on where it finds the bytecode
                    FindBytecodeResult findBytecodeResult = null;
                    int definingClassLoader;
                    for (definingClassLoader = CLASSLOADER_BOOT; definingClassLoader <= initiatingLoader; ++definingClassLoader) {
                        findBytecodeResult = findBytecode(classSignature, definingClassLoader);
                        if (findBytecodeResult != null) {
                            break;
                        }
                    }
                    if (findBytecodeResult == null) {
                        throw new ClassFileNotFoundException("Did not find any class " + classSignature + " in the classpath.");
                    }
                    
                    //now that the defining class loader is known, looks again 
                    //in the loaded class cache for the class (otherwise, we 
                    //might create a duplicate of an already existing class)
                    accessed = getClassFileClassArray(definingClassLoader, classSignature);
                    if (accessed == null) {
                        //creates a ClassFile for the class and puts it in the 
                        //loaded class cache, registering it with all the compatible 
                        //initiating loaders through the delegation chain
                        accessed = defineClass(definingClassLoader, classSignature, findBytecodeResult.bytecode, bypassStandardLoading, true);
                        for (int i = definingClassLoader; i <= initiatingLoader; ++i) {
                            addClassFileClassArray(i, accessed);
                        }
                        
                        //finally, if the loader is the bootstrap one, registers
                        //the system package
                        if (definingClassLoader == CLASSLOADER_BOOT) {
                            registerSystemPackage(classSignature, findBytecodeResult.loadedFrom);
                        }
                    } //TODO else throw LinkageError???
                } else { //the initiating loader is a user-defined classloader and we do not bypass standard loading
                    //JVMS v8, section 5.3.1: the JVM invokes the loadClass method of the classloader.
                    //This cannot be done here, so we interrupt the invoker with an exception.
                    //The invoker must load the class by invoking ClassFile.loadClass,  
                    //put the produced class in the loaded class cache, and finally
                    //reinvoke this method.
                    throw new PleaseLoadClassException(initiatingLoader, classSignature);
                }
            }
            return accessed;
        } catch (InvalidInputException e) {
            if (rethrowInvalidInputException) {
                throw e;
            }
            //this should never happen
            throw new UnexpectedInternalException(e);
        } catch (AlreadyDefinedClassException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Defines a class from a bytecode array according to the JVMS v8 section 5.3.5.
     * 
     * @param definingClassLoader an {@code int}, the identifier of the defining class loader.
     * @param classSignature a {@link String}, the name of the class to be defined. If it is not
     *        {@code null} then the method checks that the class name in {@code bytecode}
     *        {@link Object#equals(Object) equals} this parameter, or sets the class name in 
     *        {@code bytecode} to this parameter, depending on the value of {@code rename}.
     * @param bytecode a {@code byte[]} containing the content of the classfile for the class.
     * @param bypassStandardLoading  a {@code boolean}. If it is {@code true} and the {@code definingClassLoader}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the superclass/superinterfaces, 
     *        instead of raising {@link PleaseLoadClassException}.
     * @param rename a {@code boolean}. If it is {@code true}, {@code classSignature != null} and
     *        the class name specified in {@code bytecode} is different from {@code classSignature}, 
     *        then modifies the name in {@code bytecode} to align it with {@code classSignature}.
     * @return the defined {@code classFile}.
     * @throws InvalidInputException if  
     *         {@code definingClassLoader < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, or if
     *         {@code bytecode} is invalid ({@code null}).
     * @throws AlreadyDefinedClassException if {@code classSignature} was already defined for 
     *         {@code definingClassLoader}.
     * @throws BadClassFileVersionException if {@code bytecode}'s classfile version number is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if {@code rename == true} but the generated classfile
     *         does not support renaming.
     * @throws WrongClassNameException if {@code classSignature != null}, {@code rename == false}, and 
     *         the class name specified in {@code bytecode} is different from {@code classSignature}. 
     * @throws ClassFileIllFormedException if {@code bytecode} or the bytecode for  
     *         its superclass/superinterfaces is ill-formed.
     * @throws ClassFileNotFoundException if a class file for {@code bytecode}'s superclass/superinterfaces 
     *         is not found; This happens when the initiating loader is the bootstrap
     *         classloader and the classfile is not found in the bootstrap classpath
     *         (or when {@code bypassStandardLoading == true}, the initiating loader
     *         is the extensions or the application classloader, and the classfile
     *         is not found in their respective classpaths).
     * @throws ClassFileNotAccessibleException if the recursive resolution 
     *         of a superclass/superinterface fails because the superclass/superinterface
     *         is not accessible by the subclass.
     * @throws IncompatibleClassFileException if the superclass for {@code bytecode} is resolved 
     *         to an interface type, or any superinterface is resolved to an object type.
     * @throws PleaseLoadClassException if creation cannot be performed because
     *         a superclass/superinterface must be loaded via a user-defined classloader before. 
     */
    public ClassFile defineClass(int definingClassLoader, String classSignature, byte[] bytecode, boolean bypassStandardLoading, boolean rename) 
    throws InvalidInputException, AlreadyDefinedClassException, BadClassFileVersionException, RenameUnsupportedException, 
    WrongClassNameException, ClassFileIllFormedException, ClassFileNotFoundException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, PleaseLoadClassException {
        //checks parameters
        if (definingClassLoader < CLASSLOADER_BOOT) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".defineClass with invalid defining class loader " + definingClassLoader + ".");
        }
        if (bytecode == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".defineClass with null bytecode.");
        }
        
        //checks if a ClassFile exists for classSignature
        if (classSignature != null && getClassFileClassArray(definingClassLoader, classSignature) != null) {
            throw new AlreadyDefinedClassException("Tried to redefine (" + definingClassLoader + ", " + classSignature + ").");
        }
        
        //makes a dummy ClassFile
        final ClassFile classDummy = createClassFileClassDummy(definingClassLoader, classSignature, bytecode);
        
        //checks (again) if a ClassFile exists, now for the dummy ClassFile's name
        if (getClassFileClassArray(definingClassLoader, classDummy.getClassName()) != null) {
            throw new AlreadyDefinedClassException("Tried to redefine (" + definingClassLoader + ", " + classDummy.getClassName() + ").");
        }
        
        //checks the version
        if (classDummy.getMajorVersion() > JAVA_8) {
            throw new BadClassFileVersionException("The classfile for " + classDummy.getClassName() + " has unsupported version " + classDummy.getMajorVersion() + "." + classDummy.getMinorVersion() + ".");
        }
        
        //checks the name
        if (classSignature != null && !classDummy.getClassName().equals(classSignature)) {
        	if (rename) {
        		classDummy.rename(classSignature);
        	} else {
        		throw new WrongClassNameException("The classfile for class " + classDummy.getClassName() + " has different class name " + classDummy.getClassName());
        	}
        }

        //uses the dummy ClassFile to recursively resolve all the immediate 
        //ancestor classes
        //TODO check circularity
        final ClassFile superClass = (classDummy.getSuperclassName() == null ? null : resolveClass(classDummy, classDummy.getSuperclassName(), bypassStandardLoading));
        if (superClass != null && superClass.isInterface()) {
            throw new IncompatibleClassFileException("Superclass " + classDummy.getSuperclassName() + " of class " + classDummy.getClassName() + " actually is an interface.");
        }
        final List<String> superInterfaceNames = classDummy.getSuperInterfaceNames();
        final ClassFile[] superInterfaces = new ClassFile[superInterfaceNames.size()];
        for (int i = 0; i < superInterfaces.length; ++i) {
            superInterfaces[i] = resolveClass(classDummy, superInterfaceNames.get(i), bypassStandardLoading);
            if (!superInterfaces[i].isInterface()) {
                throw new IncompatibleClassFileException("Superinterface " + superInterfaceNames.get(i) + " of class " + classDummy.getClassName() + " actually is not an interface.");
            }
        }

        //creates a complete ClassFile for the class, registers it and returns it
        final ClassFile retVal = createClassFileClass(classDummy, superClass, superInterfaces);
        addClassFileClassArray(definingClassLoader, retVal);
        return retVal;
    }
    
    public ClassFile defineClassAnonymous(byte[] bytecode, boolean bypassStandardLoading, ClassFile hostClass, Object[] cpPatches) 
    throws InvalidInputException, ClassFileIllFormedException, BadClassFileVersionException, ClassFileNotFoundException, RenameUnsupportedException, 
    WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, PleaseLoadClassException {
        if (bytecode == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".defineClassAnonymous with null bytecode.");
        }
        
        //makes a dummy ClassFile
        final ClassFile classDummy = createClassFileAnonymousDummy(hostClass, bytecode);
        
        //checks the version
        if (classDummy.getMajorVersion() > JAVA_8) {
            throw new BadClassFileVersionException("The classfile for " + classDummy.getClassName() + " has unsupported version " + classDummy.getMajorVersion() + "." + classDummy.getMinorVersion() + ".");
        }

        //uses the dummy ClassFile to recursively resolve all the immediate 
        //ancestor classes
        //TODO check circularity
        final ClassFile superClass = (classDummy.getSuperclassName() == null ? null : resolveClass(classDummy, classDummy.getSuperclassName(), bypassStandardLoading));
        if (superClass != null && superClass.isInterface()) {
            throw new IncompatibleClassFileException("Superclass " + classDummy.getSuperclassName() + " of class " + classDummy.getClassName() + " actually is an interface.");
        }
        final List<String> superInterfaceNames = classDummy.getSuperInterfaceNames();
        final ClassFile[] superInterfaces = new ClassFile[superInterfaceNames.size()];
        for (int i = 0; i < superInterfaces.length; ++i) {
            superInterfaces[i] = resolveClass(classDummy, superInterfaceNames.get(i), bypassStandardLoading);
            if (!superInterfaces[i].isInterface()) {
                throw new IncompatibleClassFileException("Superinterface " + superInterfaceNames.get(i) + " of class " + classDummy.getClassName() + " actually is not an interface.");
            }
        }
        
        //creates a complete ClassFile for the class, registers it and returns it
        final ClassFile retVal = createClassFileAnonymous(classDummy, superClass, superInterfaces, cpPatches);
        addClassFileAnonymous(retVal);
        return retVal;
    }
    
    /**
     * Registers a system package.
     * 
     * @param classSignature a {@link String}, a signature of a loaded
     *        system class.
     * @param loadedFrom the {@link Path} of the jar file
     *        or directory from where {@code classSignature} was loaded.
     */
    private void registerSystemPackage(String classSignature, Path loadedFrom) {
        final String packageName = classSignature.substring(0, classSignature.lastIndexOf('/') + 1);
        this.systemPackages.put(packageName, loadedFrom);  
        //note that replacing the origin of an already registered package
        //upon loading of multiple classes from the package is a behavior
        //compatible with what Hotspot does, see hotspot:src/share/vm/classfile/classLoader.cpp:1013
        //(function ClassLoader::add_package).
    }
    
    /**
     * Returns the bytecode of a class file by searching the 
     * class file on the filesystem.
     * 
     * @param className a {@link String}, the name of the class.
     * @param initatingLoader an {@code int}; It must be either {@link ClassLoaders#CLASSLOADER_BOOT}, 
     *        or {@link ClassLoaders#CLASSLOADER_EXT}, or {@link ClassLoaders#CLASSLOADER_APP}.
     * @return a {@link FindBytecodeResult} or {@code null} if there is no class for {@code classSignature}
     *         in {@code paths}.
     */
    private FindBytecodeResult findBytecode(String className, int initiatingLoader) {
    	final String sourceContainer = classNameContainer(className);
    	final boolean toSubstitute = this.modelClassSubstitutions.containsKey(sourceContainer);
    	final String targetClassName;
    	if (toSubstitute) {
    		final String targetContainer = this.modelClassSubstitutions.get(sourceContainer);
    		targetClassName = targetContainer + classNameContained(className);
    	} else {
    		targetClassName = className;
    	}
        final Iterable<Path> paths = (toSubstitute ? this.implementationClassPath :
                                      initiatingLoader == CLASSLOADER_BOOT ? this.cp.bootClassPath() :
                                      initiatingLoader == CLASSLOADER_EXT ? this.cp.extClassPath() :
                                      this.cp.userClassPath());
        for (Path path : paths) {
            try {
                if (Files.isDirectory(path)) {
                    final Path pathOfClass = path.resolve(targetClassName + ".class");
                    return new FindBytecodeResult(Files.readAllBytes(pathOfClass), path);
                } else if (Util.isJarFile(path)) {
                    try (final JarFile f = new JarFile(path.toFile())) {
                        final JarEntry e = f.getJarEntry(targetClassName + ".class");
                        if (e == null) {
                            continue;
                        }
                        final InputStream inStr = f.getInputStream(e);
                        final ByteArrayOutputStream outStr = new ByteArrayOutputStream();
                        final byte[] buf = new byte[2048];
                        int nbytes;
                        while ((nbytes = inStr.read(buf)) != -1) {
                            outStr.write(buf, 0, nbytes);
                        }
                        return new FindBytecodeResult(outStr.toByteArray(), path);
                    }
                } //else do nothing
            } catch (IOException e) {
                continue;
            }
        }
        return null;
    }
    
    /**
     * Performs class (including array class) and interface resolution 
     * (see JVMS v8, section 5.4.3.1).
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param classSignature a {@link String}, the signature of the class to be resolved.
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @return the {@link ClassFile} of the resolved class/interface.
     * @throws InvalidInputException if {@code classSignature} is invalid ({@code null}), or if 
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, 
     *         or if {@code bypassStandardLoading == true} and
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}.
     * @throws ClassFileNotFoundException if there is no class file for {@code classSignature}
     *         and its superclasses/superinterfaces in the classpath.
     * @throws ClassFileIllFormedException if the bytecode for {@code classSignature}
     *         or its superclasses/superinterfaces is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for {@code classSignature}
     *         or its superclasses/superinterfaces has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code classSignature} derives from a 
     *         model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the bytecode for {@code classSignature}
     *         or its superclasses/superinterfaces contains a class name that is different
     *         from the expected one ({@code classSignature} or the corresponding 
     *         superclass/superinterface name).
     * @throws IncompatibleClassFileException if the superclass for {@code classSignature} is 
     *         resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the resolved class is not accessible
     *         from {@code accessor}.
     * @throws PleaseLoadClassException if resolution cannot be performed because
     *         a class must be loaded via a user-defined classloader before. 
     */
    public ClassFile resolveClass(ClassFile accessor, String classSignature, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, PleaseLoadClassException {
    	//this handle the special case of an anonymous class that invokes itself
    	if (accessor.isAnonymousUnregistered() && accessor.getClassName().equals(classSignature)) {
    		return accessor;
    	}
    	
        //loads/creates the class for classSignature
        final int initiatingLoader = accessor.getDefiningClassLoader();
        final ClassFile accessed = loadCreateClass(initiatingLoader, classSignature, bypassStandardLoading);
        
        //checks accessibility and either returns or raises an exception 
        if (isClassAccessible(accessor, accessed)) {
            return accessed;
        } else {
            throw new ClassFileNotAccessibleException("Cannot access " + classSignature + " from " + accessor.getClassName() + ".");
        }
    }

    /**
     * Performs field resolution (see JVMS v8. section 5.4.3.2). Equivalent to
     * {@link #resolveField(ClassFile, Signature, boolean, ClassFile) resolveField}{@code (accessor, fieldSignature, bypassStandardLoading, null)}.
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param fieldSignature the {@link Signature} of the field to be resolved.
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @return the {@link ClassFile} of the class of the resolved field.
     * @throws InvalidInputException if {@code fieldSignature} is invalid ({@code null} name or class name), or if 
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, 
     *         or if {@code bypassStandardLoading == true} and
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}.
     * @throws ClassFileNotFoundException if there is no class file for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         and its superclasses/superinterfaces in the classpath.
     * @throws ClassFileIllFormedException if the class file for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces derives from a model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the bytecode for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces contains a class name that is different
     *         from the expected one ({@code fieldSignature.}{@link Signature#getClassName() getClassName()} or the corresponding 
     *         superclass/superinterface name).
     * @throws IncompatibleClassFileException if the superclass for {@code fieldSignature.}{@link Signature#getClassName() getClassName()} 
     *         is resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the resolved class 
     *         {@code fieldSignature.}{@link Signature#getClassName() getClassName}{@code ()}
     *         is not accessible from {@code accessor}.
     * @throws FieldNotAccessibleException if the resolved field is not 
     *         accessible from {@code accessor}.
     * @throws FieldNotFoundException if resolution of the field fails.
     * @throws PleaseLoadClassException if creation cannot be performed because
     *         a class must be loaded via a user-defined classloader before.
     */
    public ClassFile resolveField(ClassFile accessor, Signature fieldSignature, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    FieldNotAccessibleException, FieldNotFoundException, PleaseLoadClassException {
    	return resolveField(accessor, fieldSignature, bypassStandardLoading, null);
    }
    
    /**
     * Performs field resolution (see JVMS v8. section 5.4.3.2).
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param fieldSignature the {@link Signature} of the field to be resolved.
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @param classStart a {@link ClassFile}, the class from which
     *        the resolution will start. It can be {@code null}, 
     *        in which case the resolution will start from
     *        {@code fieldSignature.}{@link Signature#getClassName() getClassName()}.
     * @return the {@link ClassFile} of the class of the resolved field.
     * @throws InvalidInputException if {@code fieldSignature} is invalid ({@code null} name or class name), or if 
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, 
     *         or if {@code bypassStandardLoading == true} and
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}.
     * @throws ClassFileNotFoundException if there is no class file for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         and its superclasses/superinterfaces in the classpath.
     * @throws ClassFileIllFormedException if the class file for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces derives from a model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the bytecode for {@code fieldSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces contains a class name that is different
     *         from the expected one ({@code fieldSignature.}{@link Signature#getClassName() getClassName()} or the corresponding 
     *         superclass/superinterface name).
     * @throws IncompatibleClassFileException if the superclass for {@code fieldSignature.}{@link Signature#getClassName() getClassName()} 
     *         is resolved to an interface type, or any superinterface is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the resolved class 
     *         {@code fieldSignature.}{@link Signature#getClassName() getClassName}{@code ()}
     *         is not accessible from {@code accessor}.
     * @throws FieldNotAccessibleException if the resolved field is not 
     *         accessible from {@code accessor}.
     * @throws FieldNotFoundException if resolution of the field fails.
     * @throws PleaseLoadClassException if creation cannot be performed because
     *         a class must be loaded via a user-defined classloader before.
     */
    public ClassFile resolveField(ClassFile accessor, Signature fieldSignature, boolean bypassStandardLoading, ClassFile classStart) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    FieldNotAccessibleException, FieldNotFoundException, PleaseLoadClassException {
        //checks the parameters
        if (fieldSignature.getName() == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveField with an invalid signature (null name field).");
        }

        //resolves the class of the field signature
        final ClassFile fieldSignatureClass = (classStart == null ? resolveClass(accessor, fieldSignature.getClassName(), bypassStandardLoading) : classStart);

        //performs field lookup starting from it
        final ClassFile accessed = resolveFieldLookup(fieldSignatureClass, fieldSignature);

        //if nothing was found, raises an exception
        if (accessed == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }

        //if a declaration was found, then it checks accessibility 
        try {
            if (isFieldAccessible(accessor, accessed, fieldSignatureClass, fieldSignature)) {
                //everything went ok
                return accessed;
            } else {
                throw new FieldNotAccessibleException(accessed.toString());
            }
        } catch (FieldNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Searches a field declaration in the class or superclasses/superinterfaces
     * of the field signature. The lookup procedure is the recursive procedure
     * described in the JVMS v8, section 5.4.3.2.
     * 
     * @param classStart a {@link ClassFile} for the class where lookup starts.
     * @param fieldSignature a field {@link Signature}. Only the name and the descriptor
     *        will be considered.
     * @return the {@link ClassFile} for the class where a field with the type 
     *         and name of {@code fieldSignature} is declared, 
     *         or {@code null} if such declaration does not exist in the 
     *         hierarchy. 
     */
    private ClassFile resolveFieldLookup(ClassFile classStart, Signature fieldSignature) {
        //if the field is declared in startClass,
        //lookup finishes
        if (classStart.hasFieldDeclaration(fieldSignature)) {
            return classStart;
        }

        //otherwise, lookup recursively in all the immediate superinterfaces
        for (ClassFile classFileSuperinterface : classStart.getSuperInterfaces()) {
            final ClassFile classFileLookup = resolveFieldLookup(classFileSuperinterface, fieldSignature);
            if (classFileLookup != null) {
                return classFileLookup;
            }
        }

        //otherwise, lookup recursively in the superclass (if any)
        final ClassFile classFileSuperclass = classStart.getSuperclass();
        if (classFileSuperclass == null) {
            //no superclass: lookup failed
            return null;
        } else {
            final ClassFile classFileLookup = resolveFieldLookup(classFileSuperclass, fieldSignature);
            return classFileLookup;
        }
    }
    
    /**
     * Performs both method and interface method resolution 
     * (see JVMS v8, section 5.4.3.3 and 5.4.3.4). Equivalent 
     * to {@link #resolveMethod(ClassFile, Signature, boolean, boolean, ClassFile) resolveMethod}{@code (accessor, methodSignature, isInterface, bypassStandardLoading, null)}.
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param methodSignature the {@link Signature} of the method to be resolved.  
     * @param isInterface {@code true} iff the method to be resolved is  
     *        an interface method (our interpretation is: if the reference from
     *        the constant pool is a CONSTANT_InterfaceMethodref rather than
     *        a CONSTANT_Methodref).
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @return the {@link ClassFile} for the resolved method.
     * @throws InvalidInputException if {@code methodSignature} is invalid ({@code null} name or class name), or if 
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, 
     *         or if {@code bypassStandardLoading == true} and
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *         or if the class for {@code java.lang.Object} was not yet loaded by the bootstrap classloading mechanism.
     * @throws ClassFileNotFoundException if there is no class for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         and its superclasses/superinterfaces in the classpath.
     * @throws ClassFileIllFormedException if the class file for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces derives from a model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the bytecode for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces contains a class name that is different
     *         from the expected one ({@code methodSignature.}{@link Signature#getClassName() getClassName()} or the corresponding 
     *         superclass/superinterface name).
     * @throws ClassFileNotAccessibleException if the resolved class for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         is not accessible from {@code accessor}.
     * @throws IncompatibleClassFileException if the symbolic reference in 
     *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         to the method disagrees with {@code isInterface}.
     * @throws MethodNotFoundException if resolution fails.
     * @throws MethodNotAccessibleException if the resolved method is not accessible 
     *         by {@code accessor}.
     * @throws PleaseLoadClassException if resolution cannot be performed because
     *         a class must be loaded via a user-defined classloader before. 
     */
    public ClassFile resolveMethod(ClassFile accessor, Signature methodSignature, boolean isInterface, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    MethodNotFoundException, MethodNotAccessibleException, PleaseLoadClassException {
    	return resolveMethod(accessor, methodSignature, isInterface, bypassStandardLoading, null);
    }
    
    /**
     * Performs both method and interface method resolution 
     * (see JVMS v8, section 5.4.3.3 and 5.4.3.4).
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param methodSignature the {@link Signature} of the method to be resolved.  
     * @param isInterface {@code true} iff the method to be resolved is required to be 
     *        an interface method (i.e., if the bytecode which triggered the resolution
     *        is invokeinterface).
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @param classStart a {@link ClassFile}, the class from which
     *        the resolution will start. It can be {@code null}, 
     *        in which case the resolution will start from
     *        {@code methodSignature.}{@link Signature#getClassName() getClassName()}.
     * @return the {@link ClassFile} for the resolved method.
     * @throws InvalidInputException if {@code methodSignature} is invalid ({@code null} name or class name), or if 
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, 
     *         or if {@code bypassStandardLoading == true} and
     *         {@code accessor.}{@link ClassFile#getDefiningClassLoader() getDefiningClassLoader}{@code () > }{@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *         or if the class for {@code java.lang.Object} was not yet loaded by the bootstrap classloading mechanism.
     * @throws ClassFileNotFoundException if there is no class for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         and its superclasses/superinterfaces in the classpath.
     * @throws ClassFileIllFormedException if the class file for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces derives from a model class but the classfile does not support renaming.
     * @throws WrongClassNameException if the bytecode for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or its superclasses/superinterfaces contains a class name that is different
     *         from the expected one ({@code methodSignature.}{@link Signature#getClassName() getClassName()} or the corresponding 
     *         superclass/superinterface name).
     * @throws ClassFileNotAccessibleException if the resolved class for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         is not accessible from {@code accessor}.
     * @throws IncompatibleClassFileException if the symbolic reference in 
     *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         to the method disagrees with {@code isInterface}.
     * @throws MethodNotFoundException if resolution fails.
     * @throws MethodNotAccessibleException if the resolved method is not accessible 
     *         by {@code accessor}.
     * @throws PleaseLoadClassException if resolution cannot be performed because
     *         a class must be loaded via a user-defined classloader before. 
     */
    public ClassFile resolveMethod(ClassFile accessor, Signature methodSignature, boolean isInterface, boolean bypassStandardLoading, ClassFile classStart) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, BadClassFileVersionException, 
    RenameUnsupportedException, WrongClassNameException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    MethodNotFoundException, MethodNotAccessibleException, PleaseLoadClassException {
        //checks the parameters
        if (methodSignature.getName() == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveMethod with an invalid signature (null name field).");
        }

        //resolves the class of the method's signature
        final ClassFile methodSignatureClass = (classStart == null ? resolveClass(accessor, methodSignature.getClassName(), bypassStandardLoading) : classStart);

        //checks if the symbolic reference to the method class 
        //is an interface (JVMS v8, section 5.4.3.3 step 1 and section 5.4.3.4 step 1)
        if (isInterface != methodSignatureClass.isInterface()) {
            throw new IncompatibleClassFileException(methodSignature.getClassName());
        }

        //attempts to find a superclass or superinterface containing 
        //a declaration for the method
        ClassFile accessed = null;
        Signature methodSignaturePolymorphic = methodSignature;

        //searches for the method declaration in the superclasses; for
        //interfaces this means searching only in the interface
        //(JVMS v8, section 5.4.3.3 step 2 and section 5.4.3.4 step 2)
        for (ClassFile cf : methodSignatureClass.superclasses()) {
            if (!isInterface && cf.hasOneSignaturePolymorphicMethodDeclaration(methodSignature.getName())) {
                accessed = cf;
                methodSignaturePolymorphic = new Signature(methodSignature.getClassName(), SIGNATURE_POLYMORPHIC_DESCRIPTOR, methodSignature.getName());
                //TODO resolve all the class names in methodSignature.getDescriptor() (it is unclear how the resolved names should be used)
                break;
            } else if (cf.hasMethodDeclaration(methodSignature)) {
                accessed = cf; 
                break;
            }
        }
        
        //searches for the method declaration in java.lang.Object, thing that
        //the previous code does not do in the case of interfaces
        //(JVMS v8, section 5.4.3.4 step 3)
        if (accessed == null && isInterface) {
            final ClassFile cfJAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
            if (cfJAVA_OBJECT == null) {
                throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveMethod before the class java.lang.Object were loaded.");
            }
            if (cfJAVA_OBJECT.hasMethodDeclaration(methodSignature)) {
                accessed = cfJAVA_OBJECT;
            }
        }

        //searches for a single, non-abstract, maximally specific superinterface method 
        //(JVMS v8, section 5.4.3.3 step 3a and section 5.4.3.4 step 4)
        if (accessed == null) {
            final Set<ClassFile> nonabstractMaxSpecMethods = maximallySpecificSuperinterfaceMethods(methodSignatureClass, methodSignature, true);
            if (nonabstractMaxSpecMethods.size() == 1) {
                accessed = nonabstractMaxSpecMethods.iterator().next();
            }
        }

        //searches in the superinterfaces
        //(JVMS v8, section 5.4.3.3 step 3b and 5.4.3.4 step 5)
        if (accessed == null) {
            for (ClassFile cf : methodSignatureClass.superinterfaces()) {
                if (cf.hasMethodDeclaration(methodSignature) && 
                    !cf.isMethodPrivate(methodSignature) && 
                    !cf.isMethodStatic(methodSignature)) {
                    accessed = cf;
                    break;
                }
            }
        }

        //exits if lookup failed
        if (accessed == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }

        //if a declaration has found, then it checks accessibility and, in case, 
        //raises IllegalAccessError; otherwise, returns the resolved method signature
        try {
            if (isMethodAccessible(accessor, accessed, methodSignatureClass, methodSignaturePolymorphic)) {
                //everything went ok
                return accessed;
            } else {
                throw new MethodNotAccessibleException(methodSignature.toString());
            }
        } catch (MethodNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Returns the maximally specific superinterface methods
     * of a given method signature.
     * 
     * @param classFile a {@link ClassFile}.
     * @param methodSignature the {@link Signature} of a method declared in {@code classFile}.
     *        Only the name and the descriptor are considered.
     * @param nonAbstract a {@code boolean}.
     * @return a {@link Set}{@code <}{@link ClassFile}{@code >} of classes containing maximally-specific 
     *         superinterface methods of {@code classFile}
     *         for {@code methodSignature.}{@link Signature#getDescriptor() getDescriptor()}
     *         and {@code methodSignature.}{@link Signature#getName() getName()}, 
     *         as for JVMS v8, section 5.4.3.3. If {@code nonAbstract == true}, such set
     *         contains all and only the maximally-specific superinterface methods that are 
     *         not abstract. If {@code nonAbstract == false}, it contains exactly all the 
     *         maximally-specific superinterface methods.
     */
    private Set<ClassFile> maximallySpecificSuperinterfaceMethods(ClassFile classFile, Signature methodSignature, boolean nonAbstract) {
        final HashSet<ClassFile> maximalSet = new HashSet<>();
        final HashSet<ClassFile> nextSet = new HashSet<>();
        final HashSet<ClassFile> dominatedSet = new HashSet<>();
        
        //initializes next with all the superinterfaces of methodSignature's class
        nextSet.addAll(classFile.getSuperInterfaces());
        
        while (!nextSet.isEmpty()) {
            //picks a superinterface from the next set
            final ClassFile superinterface = nextSet.iterator().next();
            nextSet.remove(superinterface);

            //determine all the (strict) superinterfaces of the superinterface
            final Set<ClassFile> superinterfaceSuperinterfaces = 
                stream(superinterface.superinterfaces())
                .collect(Collectors.toSet());
            superinterfaceSuperinterfaces.remove(superinterface);            
            
            //look for a method declaration of methodSignature in the superinterface 
            try {
                if (superinterface.hasMethodDeclaration(methodSignature) &&  !superinterface.isMethodPrivate(methodSignature) && !superinterface.isMethodStatic(methodSignature)) {
                    //method declaration found: add the superinterface 
                    //to maximalSet...
                    maximalSet.add(superinterface);
                    
                    //...remove the superinterface's strict superinterfaces
                    //from maximalSet, and add them to dominatedSet
                    maximalSet.removeAll(superinterfaceSuperinterfaces);
                    dominatedSet.addAll(superinterfaceSuperinterfaces);
                } else if (!dominatedSet.contains(superinterface)) {
                    //no method declaration: add to nextSet all the direct 
                    //superinterfaces of the superinterface that are not 
                    //dominated; skips this step if the superinterface is 
                    //itself dominated
                    nextSet.addAll(superinterface.getSuperInterfaces());
                    nextSet.removeAll(dominatedSet);
                }
            } catch (MethodNotFoundException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        return maximalSet;
    }
    
    /**
     * Converts an iterable to a stream.
     * See <a href="https://stackoverflow.com/a/23177907/450589">https://stackoverflow.com/a/23177907/450589</a>.
     * @param it an {@link Iterable}{@code <T>}.
     * @return a {@link Stream}{@code <T>} for {@code it}.
     */
    private static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
    
    /**
     * Performs method type resolution (JVMS v8, section 5.4.3.5) without 
     * creating the {@link java.lang.invoke.MethodType} instance.
     * 
     * @param accessor a {@link ClassFile}, the accessor's class.
     * @param descriptor a {@link String}, the descriptor of a method. 
     * @param bypassStandardLoading a {@code boolean}; if it is {@code true} and the defining classloader
     *        of {@code accessor}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the classes, instead of raising 
     *        {@link PleaseLoadClassException}.
     * @return a {@link ClassFile}{@code []} containing the resolved
     *         classes for all the types in the method descriptor; 
     *         the {@link ClassFile} for the return value type is the
     *         last in the returned array. 
     * @throws InvalidInputException if {@code descriptor} is not a correct 
     *         method descriptor.
     * @throws ClassFileNotFoundException if the bytecode for one of the classes 
     *         in {@code descriptor} does not exist in the 
     *         classpath.
     * @throws ClassFileIllFormedException if the bytecode for one of the classes 
     *         in {@code descriptor} is ill-formed.
     * @throws BadClassFileVersionException if the bytecode for one of the classes 
     *         in {@code descriptor} has a version number that is unsupported
     *         by this version of JBSE.
     * @throws RenameUnsupportedException if the classfile for one of the classes 
     *         in {@code descriptor} derives from a model class but the classfile 
     *         does not support renaming.
     * @throws WrongClassNameException if the bytecode for one of the classes 
     *         in {@code descriptor} contains a class name that is different
     *         from the expected one (that is, the one contained in the descriptor).
     * @throws IncompatibleClassFileException if the superclass for  for one of the classes 
     *         in {@code descriptor} is resolved to an interface type, or any superinterface 
     *         is resolved to an object type.
     * @throws ClassFileNotAccessibleException if the classfile for one of the classes 
     *         in {@code descriptor} is not accessible from {@code accessor}.
     * @throws PleaseLoadClassException if resolution cannot be performed because
     *         a class must be loaded via a user-defined classloader before. 
     */
    public ClassFile[] resolveMethodType(ClassFile accessor, String descriptor, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException, 
    IncompatibleClassFileException, ClassFileNotAccessibleException, PleaseLoadClassException {
        //checks the parameters
        if (descriptor == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveMethodType with descriptor == null.");
        }
        
        final String[] paramsTypes = splitParametersDescriptors(descriptor);
        final ClassFile[] retVal = new ClassFile[paramsTypes.length + 1];
        int i = 0;
        for (String paramType: paramsTypes) {
            if (isArray(paramType) || isReference(paramType)) {
                retVal[i] = resolveClass(accessor, className(paramType), bypassStandardLoading);
            } else if (isPrimitive(paramType)) {
                retVal[i] = getClassFilePrimitiveOrVoid(toPrimitiveOrVoidCanonicalName(paramType));
            } else {
                throw new InvalidInputException("Invoked" + this.getClass().getName() + ".resolveMethodType with invalid parameter type in descriptor.");
            }
            ++i;
        }
        final String returnType = splitReturnValueDescriptor(descriptor);
        if (isArray(returnType) || isReference(returnType)) {
            retVal[retVal.length - 1] = resolveClass(accessor, className(returnType), bypassStandardLoading);
        } else if (isPrimitive(returnType) || isVoid(returnType)) {
            retVal[retVal.length - 1] = getClassFilePrimitiveOrVoid(toPrimitiveOrVoidCanonicalName(returnType));
        } else {
            throw new InvalidInputException("Invoked" + this.getClass().getName() + ".resolveMethodType with invalid return type in descriptor.");
        }
        
        return retVal;
    }

    /**
     * Checks whether a class/interface is accessible to another class/interface
     * according to JVMS v8, section 5.4.4.
     * 
     * @param accessor a {@link ClassFile}.
     * @param accessed a {@link ClassFile}.
     * @return {@code true} iff {@code accessed} is accessible to 
     *         {@code accessor}.
     */
    public boolean isClassAccessible(ClassFile accessor, ClassFile accessed) {
        final boolean sameRuntimePackage = (accessor.getDefiningClassLoader() == accessed.getDefiningClassLoader() && accessed.getPackageName().equals(accessor.getPackageName()));
        if (accessor.equals(accessed) || accessed.isPublic()) {
            return true;
        } else {
            return sameRuntimePackage;
        }
    }

    /**
     * Checks whether a field is accessible to a class/interface
     * according to JVMS v8, section. 5.4.4.
     * 
     * @param accessor a {@link ClassFile}.
     * @param accessed a {@link ClassFile}.
     * @param fieldSignatureClass the {@link ClassFile} obtained by the 
     *        resolution of {@code fieldSignature.}{@link Signature#getClassName() getClassName()}.
     * @param fieldSignature a {@link Signature}; Its name and descriptor 
     *        must detect a field declared in {@code accessed}.
     * @return {@code true} iff the {@code fieldSignature} field in {@code accessed} 
     *         is accessible to {@code accessor}.
     * @throws FieldNotFoundException if the {@code fieldSignature} field is not 
     *         found in {@code accessed}. 
     */
    private boolean isFieldAccessible(ClassFile accessor, ClassFile accessed, ClassFile fieldSignatureClass, Signature fieldSignature) 
    throws FieldNotFoundException {
    	try {
    		ClassFile accessorHost = accessor;
    		while (accessorHost.isAnonymousUnregistered()) {
    			accessorHost = accessorHost.getHostClass();
    		}
	        final boolean sameRuntimePackage = (accessorHost.getDefiningClassLoader() == accessed.getDefiningClassLoader() && accessed.getPackageName().equals(accessorHost.getPackageName()));
	        if (accessor.equals(accessed) || accessed.isFieldPublic(fieldSignature)) {
	            return true;
	        } else if (accessed.isFieldProtected(fieldSignature)) {
	            if (sameRuntimePackage) {
	                return true;
	            } else if (!accessorHost.isSubclass(accessed)) {
	                return false;
	            } else if (accessed.isFieldStatic(fieldSignature)) {
	                return true;
	            } else {
	                return accessorHost.isSubclass(fieldSignatureClass) || fieldSignatureClass.isSubclass(accessorHost);
	            }
	        } else if (accessed.isFieldPackage(fieldSignature)) {
	            return sameRuntimePackage; 
	        } else { //accessed.isFieldPrivate(fieldSignature)
	            return (accessed.equals(accessorHost)); 
	            //TODO there was a || accessorHost.isInner(accessed) clause but it is *wrong*!
	        }
    	} catch (InvalidInputException e) {
    		//this should never happen, NullPointerException shall be thrown before
    		throw new UnexpectedInternalException(e);
    	}
    }
    
    private static final String JAVA_OBJECT_CLONE_DESCRIPTOR = "()" + REFERENCE + JAVA_OBJECT + TYPEEND;
    private static final String JAVA_OBJECT_CLONE_NAME = "clone";

    /**
     * Checks whether a method is accessible to a class/interface
     * according to JVMS v8, section 5.4.4.
     * 
     * @param accessor a {@link ClassFile}.
     * @param accessed a {@link ClassFile}.
     * @param methodSignatureClass the {@link ClassFile} obtained by the 
     *        resolution of {@code methodSignature.}{@link Signature#getClassName() getClassName()}.
     * @param methodSignature a {@link Signature}; Its name and descriptor 
     *        must detect a method declared in {@code accessed}.
     * @return {@code true} iff the {@code methodSignature} method in {@code accessed} 
     *         is accessible to {@code accessor}.
     * @throws MethodNotFoundException if the {@code methodSignature} method is not 
     *         found in {@code accessed}. 
     */
    private boolean isMethodAccessible(ClassFile accessor, ClassFile accessed, ClassFile methodSignatureClass, Signature methodSignature) 
    throws MethodNotFoundException {
    	try {
    	        //special case: the resolution class is an array class and the method is
    	        //clone; in this case, treat it as a public method
    	        if (JAVA_OBJECT_CLONE_DESCRIPTOR.equals(methodSignature.getDescriptor()) && JAVA_OBJECT_CLONE_NAME.equals(methodSignature.getName()) && methodSignatureClass.isArray()) {
    	            return true;
    	        }
    		ClassFile accessorHost = accessor;
    		while (accessorHost.isAnonymousUnregistered()) {
    			accessorHost = accessorHost.getHostClass();
    		}
    		final boolean sameRuntimePackage = (accessorHost.getDefiningClassLoader() == accessed.getDefiningClassLoader() && accessed.getPackageName().equals(accessorHost.getPackageName()));
    		if (accessor.equals(accessed) || accessed.isMethodPublic(methodSignature)) {
    			return true;
    		} else if (accessed.isMethodProtected(methodSignature)) {
    			if (sameRuntimePackage) {
    				return true;
    			} else if (!accessorHost.isSubclass(accessed)) {
    				return false;
    			} else if (accessed.isMethodStatic(methodSignature)) {
    				return true;
    			} else {
    				return accessorHost.isSubclass(methodSignatureClass) || methodSignatureClass.isSubclass(accessorHost);
    			}
    		} else if (accessed.isMethodPackage(methodSignature)) {
    			return sameRuntimePackage;
    		} else { //accessed.isMethodPrivate(methodSignature)
    			return (accessed.equals(accessorHost));
    			//TODO there was a || accessorHost.isInner(accessed) clause but it is *wrong*!
    		}
    	} catch (InvalidInputException e) {
    		//this should never happen, NullPointerException shall be thrown before
    		throw new UnexpectedInternalException(e);
    	}
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKEINTERFACE bytecode.
     * 
     * @param receiverClass the {@link ClassFile} of the class of the 
     *        method invocation's receiver.
     * @param resolutionClass the {@link ClassFile} of the resolved method.
     * @param methodSignature the {@link Signature} of the method whose implementation 
     *        must be looked up.
     * @return the {@link ClassFile} which contains the implementation of 
     *         {@code methodSignature}.
     * @throws InvalidInputException if any parameter is {@code null}.
     * @throws MethodNotAccessibleException  if lookup fails and {@link java.lang.IllegalAccessError} should be thrown.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplInterface(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws InvalidInputException, MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException {
    	if (receiverClass == null || resolutionClass == null || methodSignature == null) {
    		throw new InvalidInputException("Invoked ClassHierarchy.lookupMethodImplInterface with a null parameter.");
    	}
        final ClassFile retVal = lookupMethodImplInterface_recurse(receiverClass, resolutionClass, methodSignature);
        if (retVal == null) {
            //sixth run-time exception
        	throw new MethodAbstractException(methodSignature.toString());
        }
        return retVal;
    }
        
    public ClassFile lookupMethodImplInterface_recurse(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException {
    	ClassFile retVal = null;
        try {
            //step 1
        	if (receiverClass.hasMethodDeclaration(methodSignature) && !receiverClass.isMethodStatic(methodSignature)) {
        		retVal = receiverClass;
        		
        		//third run-time exception
        		if (!retVal.isMethodPublic(methodSignature)) {
        			throw new MethodNotAccessibleException(methodSignature.toString());
        		}

        		//fourth run-time exception
        		if (retVal.isMethodAbstract(methodSignature)) {
        			throw new MethodAbstractException(methodSignature.toString());
        		}
        	}
        	
        	//step 2
        	if (retVal == null) {
        		final ClassFile receiverClassSuperclass = receiverClass.getSuperclass();
        		if (receiverClassSuperclass != null) {
        			retVal = lookupMethodImplInterface_recurse(receiverClassSuperclass, resolutionClass, methodSignature);
        		}
        	}

            //step 3
            if (retVal == null) {
                final Set<ClassFile> nonabstractMaxSpecMethods = 
                    maximallySpecificSuperinterfaceMethods(receiverClass, methodSignature, true);
                if (nonabstractMaxSpecMethods.size() == 0) {
                    //defer sixth run-time exception
                    retVal = null;
                } else if (nonabstractMaxSpecMethods.size() == 1) {
                    retVal = nonabstractMaxSpecMethods.iterator().next();
                } else { //nonabstractMaxSpecMethods.size() > 1
                    //fifth run-time exception
                    throw new IncompatibleClassFileException(methodSignature.toString());
                }
            }
            
            return retVal;
        } catch (MethodNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKESPECIAL bytecode.
     * 
     * @param currentClass the {@link ClassFile} of the class of the invoker.
     * @param resolutionClass the {@link ClassFile} of the resolved method.
     * @param methodSignature the {@link Signature} of the method whose implementation 
     *        must be looked up.
     * @return the {@link ClassFile} which contains the implementation of 
     *         {@code methodSignature}.
     * @throws MethodNotFoundException if no declaration of {@code methodSignature} is found in 
     *         {@code resolutionClass}. 
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     * @throws InvalidInputException if {@code resolutionClass == null}, or if a virtual ("super")
     *         call semantics is required and {@code currentClass} has not a superclass.
     */
    public ClassFile lookupMethodImplSpecial(ClassFile currentClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodNotFoundException, MethodAbstractException, IncompatibleClassFileException, InvalidInputException {
    	if (resolutionClass == null) {
    		throw new InvalidInputException("Invoked " + this.getClass().getName() + ".lookupMethodImplSpecial with a null resolutionClass.");
    	}
        if (resolutionClass.isMethodSignaturePolymorphic(methodSignature)) {
            return resolutionClass;
        } else {
        	//determines whether should start looking for the implementation in 
        	//the superclass of the current class (virtual semantics, for super 
        	//calls) or in the class of the resolved method (nonvirtual semantics, 
        	//for <init> and private methods).
        	//N.B. here we use the specification as described in the JVMS v.12 and
        	//following ("The symbolic reference names a class (not an interface),
        	//and that class is a superclass of the current class") rather than 
        	//that of the JVMS v.8 to v.11 ("If the symbolic reference names a class 
        	//(not an interface), then that class is a superclass of the current class")
        	//because the former seems to be the correct one and the latter to be
        	//a mistake.
        	final boolean useVirtualSemantics = 
        			(!"<init>".equals(methodSignature.getName()) &&
        			(!resolutionClass.isInterface() && currentClass.getSuperclass() != null && currentClass.getSuperclass().isSubclass(resolutionClass)) && 
        			currentClass.isSuperInvoke());
        	final ClassFile c = (useVirtualSemantics ? currentClass.getSuperclass() : resolutionClass);
        	if (c == null) {
        		throw new InvalidInputException("Invoked " + this.getClass().getName() + ".lookupMethodImplSpecial with a virtual invocation semantics (\"super\") but currentClass has not a superclass.");
        	}

        	//applies lookup
        	ClassFile retVal = null;
        	try {
        		//step 1
        		if (c.hasMethodDeclaration(methodSignature) && 
        				!c.isMethodStatic(methodSignature)) {
        			retVal = c;
        			//third run-time exception
        			if (retVal.isMethodAbstract(methodSignature)) {
        				throw new MethodAbstractException(methodSignature.toString());
        			}
        		} 

        		//step 2
        		if (retVal == null && !c.isInterface() && c.getSuperclass() != null) {
        			for (ClassFile f : c.getSuperclass().superclasses()) {
        				if (f.hasMethodDeclaration(methodSignature)) {
        					retVal = f;
        					//third run-time exception
        					if (retVal.isMethodAbstract(methodSignature)) {
        						throw new MethodAbstractException(methodSignature.toString());
        					}
        					break;
        				}
        			}
        		}

        		//step 3
        		if (retVal == null && c.isInterface()) {
        			final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
        			if (cf_JAVA_OBJECT == null) {
        				throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".lookupMethodImplSpecial was unable to find standard class java.lang.Object.");
        			}
        			if (c.hasMethodDeclaration(methodSignature) && 
        					!c.isMethodStatic(methodSignature) && 
        					c.isMethodPublic(methodSignature)) {
        				retVal = cf_JAVA_OBJECT;
        				//third run-time exception
        				if (retVal.isMethodAbstract(methodSignature)) {
        					throw new MethodAbstractException(methodSignature.toString());
        				}
        			}
        		}

        		//step 4
        		if (retVal == null) {
        			final Set<ClassFile> nonabstractMaxSpecMethods = 
        					maximallySpecificSuperinterfaceMethods(resolutionClass, methodSignature, true);
        			if (nonabstractMaxSpecMethods.size() == 0) {
        				//sixth run-time exception
        				throw new MethodAbstractException(methodSignature.toString());
        			} else if (nonabstractMaxSpecMethods.size() == 1) {
        				retVal = nonabstractMaxSpecMethods.iterator().next();
        			} else { //nonabstractMaxSpecMethods.size() > 1
        				//fifth run-time exception
        				throw new IncompatibleClassFileException(methodSignature.toString());
        			}
        		}
        	} catch (MethodNotFoundException e) {
        		//this should never happen
        		throw new UnexpectedInternalException(e);
        	}

        	return retVal;
        }
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKESTATIC bytecode.
     *   
     * @param resolutionClass the {@link ClassFile} of the resolved method.
     * @param methodSignature the {@link Signature} of the method whose implementation 
     *        must be looked up.
     * @return the {@link ClassFile} which contains the implementation of 
     *         {@code methodSignature}. Trivially, this is {@code resolutionClass}.
     */
    public ClassFile lookupMethodImplStatic(ClassFile resolutionClass, Signature methodSignature) {
        return resolutionClass;
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKEVIRTUAL bytecode.
     *   
     * @param receiverClass the {@link ClassFile} of the class of the 
     *        method invocation's receiver.
     * @param resolutionClass the {@link ClassFile} of the resolved method.
     * @param methodSignature the {@link Signature} of the method whose implementation 
     *        must be looked up.
     * @return the {@link ClassFile} which contains the implementation of 
     *         {@code methodSignature}. In the case the method is signature polymorphic returns
     *         {@code resolutionClass}.
     * @throws InvalidInputException if any parameter is {@code null}.
     * @throws MethodNotFoundException if no declaration of {@code methodSignature} is found in 
     *         {@code resolutionClass}. 
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplVirtual(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws InvalidInputException, MethodNotFoundException, MethodAbstractException, IncompatibleClassFileException {
    	if (receiverClass == null || resolutionClass == null || methodSignature == null) {
    		throw new InvalidInputException("Invoked ClassHierarchy.lookupMethodImplVirtual with a null parameter.");
    	}
        if (resolutionClass.isMethodSignaturePolymorphic(methodSignature)) {
            return resolutionClass;
        } else {
        	final ClassFile retVal = lookupMethodImplVirtual_recurse(receiverClass, resolutionClass, methodSignature);
        	if (retVal == null) {
                //sixth run-time exception
            	throw new MethodAbstractException(methodSignature.toString());
            }
            return retVal;
        }
    }
    
    
    private ClassFile lookupMethodImplVirtual_recurse(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodAbstractException, IncompatibleClassFileException {
    	ClassFile retVal = null;
    	try {
	    	//step 1
	    	if (receiverClass.hasMethodDeclaration(methodSignature) && !receiverClass.isMethodStatic(methodSignature)) {
	    		if (overrides(receiverClass, resolutionClass, methodSignature, methodSignature)) {
	    			retVal = receiverClass;
	
	    			//third run-time exception
	    			if (retVal.isMethodAbstract(methodSignature)) {
	    				throw new MethodAbstractException(methodSignature.toString());
	    			}
	    		}
	    	}
	
	    	//step 2
	    	if (retVal == null) {
	    		final ClassFile receiverClassSuperclass = receiverClass.getSuperclass();
	    		if (receiverClassSuperclass != null) {
	    			retVal = lookupMethodImplVirtual_recurse(receiverClassSuperclass, resolutionClass, methodSignature);
	    		}
	    	}
	
	
	    	//step 3
	    	if (retVal == null) {
	    		final Set<ClassFile> nonabstractMaxSpecMethods = 
	    		    maximallySpecificSuperinterfaceMethods(receiverClass, methodSignature, true);
	    		if (nonabstractMaxSpecMethods.size() == 0) {
	    			//defer sixth run-time exception
	    			retVal = null;
	    		} else if (nonabstractMaxSpecMethods.size() == 1) {
	    			retVal = nonabstractMaxSpecMethods.iterator().next();
	    		} else { //nonabstractMaxSpecMethods.size() > 1
	    			//fifth run-time exception
	    			throw new IncompatibleClassFileException(methodSignature.toString());
	    		}
	    	}
	
	    	return retVal;
    	} catch (MethodNotFoundException | InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Checks assignment compatibility for references 
     * (see JVMS v8 4.9.2 and JLS v8 5.2).
     * 
     * @param source the {@link ClassFile} of the source of the 
     *        assignment.
     * @param target the {@link ClassFile} of the target of the 
     *        assignment.
     * @return {@code true} iff {@code source} is assignment
     *         compatible with {@code target}.
     */
    public boolean isAssignmentCompatible(ClassFile source, ClassFile target) {       
        final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
        if (cf_JAVA_OBJECT == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Object.");
        }
        final ClassFile cf_JAVA_CLONEABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLONEABLE); //surely loaded
        if (cf_JAVA_CLONEABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Cloneable.");
        } 
        final ClassFile cf_JAVA_SERIALIZABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_SERIALIZABLE); //surely loaded
        if (cf_JAVA_SERIALIZABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArrays was unable to find standard class java.lang.Cloneable.");
        }
        
        try {
        	if (source.isInterface()) {
        		if (target.isInterface()) {
        			return source.isSubclass(target);
        		} else if (target.isArray()) {
        			return false; //should not happen (verify error)
        		} else {
        			return (cf_JAVA_OBJECT.equals(target));
        		}
        	} else if (source.isArray()) {
        		if (target.isInterface()) {
        			return (cf_JAVA_CLONEABLE.equals(target) || cf_JAVA_SERIALIZABLE.equals(target));
        		} else if (target.isArray()) {
        			final ClassFile sourceComponent = source.getMemberClass();
        			final ClassFile targetComponent = target.getMemberClass();
        			if (sourceComponent.isPrimitiveOrVoid() && targetComponent.isPrimitiveOrVoid()) {
        				return (sourceComponent.equals(targetComponent));
        			} else if ((sourceComponent.isReference() && targetComponent.isReference()) ||
        					(sourceComponent.isArray() && targetComponent.isArray())) {
        				return isAssignmentCompatible(sourceComponent, targetComponent);
        			} else {
        				return false;
        			}
        		} else {
        			return (cf_JAVA_OBJECT.equals(target));
        		}
        	} else {
        		if (target.isArray()) {
        			return false; //should not happen (verify error)
        		} else {
        			return source.isSubclass(target);
        		}
        	}
		} catch (InvalidInputException e) {
			//this should never happen (NullPointerException shall be raised before)
			throw new UnexpectedInternalException(e);
		}
    }
    
    /**
     * Checks if a method overrides another one, according to
     * JVMS v8, section 5.4.5. 
     * 
     * @param sub a {@link ClassFile}.
     * @param sup a {@link ClassFile}.
     * @param subMethodSignature a method {@link Signature}. The method
     *        must be declared in {@code sub}.
     * @param supMethodSignature a method {@link Signature}. The method
     *        must be declared in {@code sup}.
     * @return {@code true} iff {@code subMethodSignature} in {@code sub} 
     *         overrides {@code supMethodSignature} in {@code sup}.
     * @throws MethodNotFoundException if {@code subMethodSignature} or 
     *         {@code supMethodSignature} do not exist in their respective 
     *         classfiles (note that the exception is not always raised 
     *         in this case).
     * @throws InvalidInputException if any parameter is {@code null}.
     */
    public boolean overrides(ClassFile sub, ClassFile sup, Signature subMethodSignature, Signature supMethodSignature) 
    throws MethodNotFoundException, InvalidInputException {
    	if (sub == null || sup == null || subMethodSignature == null || supMethodSignature == null) {
    		throw new InvalidInputException("Invoked ClassHierarchy.overrides with a null parameter.");
    	}
        //first case: same method
        if (sub.equals(sup) && 
            subMethodSignature.getDescriptor().equals(supMethodSignature.getDescriptor()) &&
            subMethodSignature.getName().equals(supMethodSignature.getName()) ) {
            return true;
        }
        
        //second case: all of the following must be true
        //1- subMethod's class is a (proper) subclass of supMethod's class 
        if (sub.getSuperclass() == null || !sub.getSuperclass().isSubclass(sup)) {
            return false;
        }
        
        //2- subMethod has same name and descriptor of supMethod
        if (!subMethodSignature.getName().equals(supMethodSignature.getName())) {
            return false;
        }
        if (!subMethodSignature.getDescriptor().equals(supMethodSignature.getDescriptor())) {
            return false;
        }
        
        //3- subMethod is not private
        if (sub.isMethodPrivate(subMethodSignature)) {
            return false;
        }
        
        //4- one of the following is true:
        //4a- supMethod is public, or protected, or (package in the same runtime package of subMethod)
        if (sup.isMethodPublic(supMethodSignature)) {
            return true;
        }
        if (sup.isMethodProtected(supMethodSignature)) {
            return true;
        }
        final boolean sameRuntimePackage = (sub.getDefiningClassLoader() == sup.getDefiningClassLoader() && sub.getPackageName().equals(sup.getPackageName()));
        if (sup.isMethodPackage(supMethodSignature) && sameRuntimePackage) {
            return true;
        }
        
        //4b- there is another method m such that subMethod overrides 
        //m and m overrides supMethod; we look for such m in subMethod's 
        //superclasses up to supMethods
        if (sub.getSuperclass() != null) {
        	for (ClassFile cf : sub.getSuperclass().superclasses()) {
        		if (sup.equals(cf)) {
        			break;
        		}
        		if (cf.hasMethodDeclaration(subMethodSignature)) {
        			if (overrides(sub, cf, subMethodSignature, supMethodSignature) && overrides (cf, sup, subMethodSignature, supMethodSignature)) {
        				return true;
        			}
        		}
        	}
        }
        
        return false; //no such m was found
    }
    
    @Override
    public ClassHierarchy clone() {
        final ClassHierarchy o;
        try {
            o = (ClassHierarchy) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        //cp, expansionBackdoor and allFieldsOf may be shared;
        //in a future, expansionBackdoor may possibly be cloned
        
        o.cfs = o.cfs.clone();
        
        return o;
    }
}
