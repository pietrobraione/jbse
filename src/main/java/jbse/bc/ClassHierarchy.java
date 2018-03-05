package jbse.bc;

import static jbse.bc.ClassFile.JAVA_6;
import static jbse.bc.ClassFile.JAVA_8;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.ClassLoaders.CLASSLOADER_EXT;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.common.Type.className;
import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isReference;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.toPrimitiveCanonicalName;
import static jbse.common.Type.toPrimitiveInternalName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * Class handling a hierarchy of Java classes as specified 
 * by some {@link ClassFileFactory}.
 *  
 * @author Pietro Braione
 */
public final class ClassHierarchy implements Cloneable {
    private final Classpath cp;
    private final Map<String, Set<String>> expansionBackdoor;
    private final HashMap<ClassFile, ArrayList<Signature>> allFieldsOf;
    private final ClassFileFactory f;
    private ClassFileStore cfs; //not final because of clone
    private HashMap<String, String> systemPackages; //not final because of clone
    
    private static class FindBytecodeResult {
        final byte[] bytecode;
        final String loadedFrom;
        
        public FindBytecodeResult(byte[] bytecode, String loadedFrom) {
            this.bytecode = bytecode;
            this.loadedFrom = loadedFrom;
        }
    }

    /**
     * Constructor.
     * 
     * @param cp a {@link Classpath}.
     * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
     *        The class must have an accessible parameterless constructor.
     * @param expansionBackdoor a 
     *        {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>}
     *        associating class names to sets of names of their subclasses. It 
     *        is used in place of the class hierarchy to perform expansion.
     * @throws InvalidClassFileFactoryClassException in the case {@link fClass}
     *         has not the expected features (missing constructor, unaccessible 
     *         constructor...).
     */
    public ClassHierarchy(Classpath cp, Class<? extends ClassFileFactory> fClass, Map<String, Set<String>> expansionBackdoor)
    throws InvalidClassFileFactoryClassException {
        this.cp = cp.clone(); //safety copy
        this.cfs = new ClassFileStore();
        this.expansionBackdoor = expansionBackdoor;
        this.allFieldsOf = new HashMap<>();
        try {
            this.f = fClass.newInstance();
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
        final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT);
        if (cf_JAVA_OBJECT == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Object.");
        }
        final ClassFile cf_JAVA_CLONEABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLONEABLE);
        if (cf_JAVA_CLONEABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Cloneable.");
        } 
        final ClassFile cf_JAVA_SERIALIZABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_SERIALIZABLE);
        if (cf_JAVA_SERIALIZABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArrays was unable to find standard class java.lang.Cloneable.");
        }

        final ClassFile retval =
            this.f.newClassFileArray(className, memberClass, cf_JAVA_OBJECT, cf_JAVA_CLONEABLE, cf_JAVA_SERIALIZABLE);
        return retval;
    }
    
    /**
     * Adds a {@link ClassFile} for to this hierarchy.
     * 
     * @param initiatingLoader an {@code int}, the identifier of 
     *        a classloader.
     * @param classFile a {@link ClassFile} for the class to be added.
     * @throws InvalidInputException if {@code initiatingLoader} is invalid (negative),
     *         or {@code classFile == null}, or {@code classFile.}{@link ClassFile#isPrimitive()}, or 
     *         {@code classFile.}{@link ClassFile#isAnonymousUnregistered()}, or there is already a different
     *         {@link ClassFile} in the loaded class cache for the pair
     *         {@code (initiatingLoader, classFile.}{@link ClassFile#getClassName() getClassName}{@code ())}.
     */
    public void addClassFileClassArray(int initiatingLoader, ClassFile classFile) 
    throws InvalidInputException {
        this.cfs.putLoadedClassCache(initiatingLoader, classFile);
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
        return this.cfs.getLoadedClassCache(initiatingLoader, className);
    }
    
    /**
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the canonical name of a primitive type 
     *        (see JLS v8, section 6.7).
     * @return the {@link ClassFile} of the correspondent class.
     * @throws InvalidInputException when {@code typeName}
     *         is not the canonical name of a primitive type.
     */
    public ClassFile getClassFilePrimitive(String typeName)
    throws InvalidInputException {
        final ClassFile retval = 
            this.cfs.getClassFilePrimitive(toPrimitiveInternalName(typeName));
        return retval;
    }
    
    /**
     * Creates a dummy {@link ClassFile} for an anonymous (in the sense of
     * {@link sun.misc.Unsafe#defineAnonymousClass}) class.
     * 
     * @param bytecode a {@code byte[]}, the bytecode for the anonymous class.
     * @return a dummy {@link ClassFile} for the anonymous class.
     * @throws ClassFileIllFormedException if {@code bytecode} is ill-formed.
     * @throws InvalidInputException if {@code bytecode == null}.
     */
    public ClassFile createClassFileAnonymousDummy(byte[] bytecode) 
    throws ClassFileIllFormedException, InvalidInputException {
        final ClassFile retval =
            this.f.newClassFileAnonymous(bytecode, null, null);
        return retval;
    }

    /**
     * Adds a {@link ClassFile} for an anonymous class to this hierarchy.
     *
     * @param classFile a dummy {@link ClassFile} created with a previous invocation 
     *        of {@link #createClassFileAnonymousDummy(byte[])}.
     * @param cpPatches a {@link ConstantPoolValue}{@code []}; The i-th element of this
     *        array patches the i-th element in the constant pool defined
     *        by the {@code bytecode} (the two must agree). Note that 
     *        {@code cpPatches[0]} and all the {@code cpPatches[i]} with {@code i} equal 
     *        or greater than the size of the constant pool in {@code classFile} are ignored.
     *        It can be {@code null} to signify no patches.
     * @param hostClass a {@link ClassFile}, the host class for the
     *        anonymous class.
     * @return the {@link ClassFile} for the anonymous class; It will be different
     *         from {@code classFile} because it will have the host class set and
     *         the classpath patches applied.
     * @throws InvalidInputException  if any of the parameters has an invalid
     *         value.
     */
    public ClassFile addClassFileAnonymous(ClassFile classFile, ClassFile hostClass, ConstantPoolValue[] cpPatches) 
    throws InvalidInputException {
        if (classFile == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".addClassFileAnonymous() with a classFile parameter that has value null.");
        }
        if (!classFile.isAnonymous()) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".addClassFileAnonymous() with a classFile parameter that is not anonymous.");
        }
        if (hostClass == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".addClassFileAnonymous() with a hostClass parameter that has value null.");
        }
        final ClassFile retVal;
        try {
            retVal = this.f.newClassFileAnonymous(classFile.getBinaryFileContent(), cpPatches, hostClass);
        } catch (ClassFileIllFormedException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.cfs.putAnonymousClassCache(retVal);
        return retVal;
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
     * @return a {@link String}, the jar file or directory from which 
     *         the system classes in {@code packageName} were loaded from, 
     *         or {@code null} if no system class from {@code packageName}
     *         was loaded. 
     */
    public String getSystemPackageLoadedFrom(String packageName) {
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
     * @throws WrongClassNameException  when the bytecode for one of 
     *         the subclass names in the expansion backdoor has a 
     *         class name different from that used for resolving it.
     */
    public Set<ClassFile> getAllConcreteSubclasses(ClassFile classFile)
    throws InvalidInputException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
    BadClassFileVersionException, WrongClassNameException {
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
                    if (isSubclass(subclass, classFile)) {
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
     * Produces all the superclasses of a given class.
     * 
     * @param startClass the {@link ClassFile} of the class whose superclasses 
     *        are returned.
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superclasses of {@code startClass} (included). If
     *         {@code startClass == null} an empty {@link Iterable} is
     *         returned.
     */
    public Iterable<ClassFile> superclasses(ClassFile startClass) {
        return new IterableSuperclasses(startClass);
    }

    /**
     * Produces all the superinterfaces of a given class.
     * 
     * @param startClass the {@link ClassFile} of the class whose superinterfaces 
     *        are returned.
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superinterfaces of {@code startClassName} (included if
     *         it is an interface). If {@code startClass == null} an empty 
     *         {@link Iterable} is returned. A same superinterface is not iterated
     *         more than once even if the class inherits it more than once. 
     */
    public Iterable<ClassFile> superinterfaces(ClassFile startClass) {
        return new IterableSuperinterfaces(startClass);
    }

    /**
     * Checks whether a class/interface is a subclass of/implements another one.
     * 
     * @param sub a {@link ClassFile}.
     * @param sup anothe {@link ClassFile}.
     * @return {@code true} if {@code sub == sup}, or {@code sub} 
     *         extends {@code sup}, or {@code sub} implements {@code sup}, 
     *         {@code false} otherwise.
     */
    public boolean isSubclass(ClassFile sub, ClassFile sup) {
        if (sub.isArray() && sup.isArray()) {
            final ClassFile subMember = sub.getMemberClass(); 
            final ClassFile supMember = sup.getMemberClass();
            if (subMember.isPrimitive() && supMember.isPrimitive()) {
                return (subMember == supMember);
            } else if (subMember.isReference() && supMember.isReference()) {
                return isSubclass(subMember, supMember);
            } else if (subMember.isArray() && supMember.isArray()) {
                return isSubclass(subMember, supMember);
            } else {
                return false;
            }
        } else {
            for (ClassFile f : superclasses(sub)) { 
                if (f == sup) {
                    return true;
                } 
            }
            for (ClassFile f : superinterfaces(sub)) {
                if (f == sup) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * {@link Iterable}{@code <}{@link ClassFile}{@code >} for upwardly 
     * scanning a class hierarchy.
     *  
     * @author Pietro Braione
     */
    private class IterableSuperclasses implements Iterable<ClassFile> {
        private ClassFile startClassName;

        /**
         * Constructor.
         * 
         * @param startClass The {@link ClassFile} of the 
         *        class from where the iteration is started. 
         */
        public IterableSuperclasses(ClassFile startClass) {
            this.startClassName = startClass;
        }

        public Iterator<ClassFile> iterator() {
            return new MyIterator(this.startClassName);
        }        

        /**
         * {@link Iterator}{@code <}{@link ClassFile}{@code >} for
         * upwardly scanning a class hierarchy.
         * 
         * @author Pietro Braione
         */
        private class MyIterator implements Iterator<ClassFile> {
            private ClassFile nextClass;

            public MyIterator(ClassFile startClass) {
                this.nextClass = startClass;
            }

            public boolean hasNext() {
                return (this.nextClass != null);
            }

            public ClassFile next() {
                //ensures the method precondition
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                //stores the return value
                final ClassFile retval = this.nextClass;

                //gets the classfile of the superclass
                this.nextClass = retval.getSuperclass();

                //returns
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * {@link Iterable}{@code <}{@link ClassFile}{@code >} 
     * for upwardly scanning an interface hierarchy.
     *  
     * @author Pietro Braione
     */
    private class IterableSuperinterfaces implements Iterable<ClassFile> {
        private ClassFile startClass;

        /**
         * Constructor.
         * 
         * @param startClassName 
         *        The name of the class from where the iteration is started. 
         *        Note that the first call to {@code hasNext()} 
         *        will return {@code true} iff {@code startClassName != null} and 
         *        {@code startClassName} exists in the environment 
         *        defined by {@link Classpath}{@code .this.env}, and it is an 
         *        interface.
         */
        public IterableSuperinterfaces(ClassFile startClass) {
            this.startClass = startClass;
        }

        public Iterator<ClassFile> iterator() {
            return new MyIterator(this.startClass);
        }        

        /**
         * {@link Iterator}{@code <}{@link ClassFile}{@code >} for
         * upwardly scanning the superinterfaces of a class/interface. 
         * For the sake of simplicity it scans in breadth-first 
         * order. It does not visit a same interface twice. 
         * 
         * @author Pietro Braione
         */
        private class MyIterator implements Iterator<ClassFile> {
            private final LinkedList<ClassFile> nextClassFiles;
            private final HashSet<ClassFile> visitedClassFiles;

            public MyIterator(ClassFile startClass) {
                this.visitedClassFiles = new HashSet<>();
                this.nextClassFiles = new LinkedList<>();
                if (startClass == null) {
                    return; //keeps the iterator empty
                }
                if (startClass.isInterface()) {
                    this.nextClassFiles.add(startClass);
                } else { //is not interface and is not ClassFileBad
                    for (ClassFile cfSuper : superclasses(startClass)) {
                        this.nextClassFiles.addAll(nonVisitedImmediateSuperinterfaces(cfSuper));
                    }
                }
            }

            public boolean hasNext() {
                return !(this.nextClassFiles.isEmpty());
            }

            public ClassFile next() {
                //ensures the method precondition
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                //gets the next interface into the return value
                //and updates the iteration state
                final ClassFile retVal = this.nextClassFiles.removeFirst(); 
                this.visitedClassFiles.add(retVal);
                this.nextClassFiles.addAll(nonVisitedImmediateSuperinterfaces(retVal));

                //returns the result
                return retVal;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private List<ClassFile> nonVisitedImmediateSuperinterfaces(ClassFile base) {
                return base.getSuperInterfaces().stream()
                       .filter(cf -> !this.visitedClassFiles.contains(cf))
                       .collect(Collectors.toList());
            }
        }
    }

    private static final Signature[] SIGNATURE_ARRAY = new Signature[0];

    /**
     * Returns all the fields known to an object of a given class.
     * 
     * @param classFile a {@link ClassFile}.
     * @return a {@link Signature}{@code []}. It will contain all the 
     *         {@link Signature}s of the class' static fields, followed
     *         by all the {@link Signature}s of the class' object (nonstatic) 
     *         fields, followed by all the {@link Signature}s of the object 
     *         fields of all the superclasses of the class.
     */	
    public Signature[] getAllFields(ClassFile classFile) {
        ArrayList<Signature> signatures = this.allFieldsOf.get(classFile);
        if (signatures == null) {
            signatures = new ArrayList<Signature>(0);
            this.allFieldsOf.put(classFile, signatures);
            boolean isStartClass = true;
            for (ClassFile c : superclasses(classFile)) {
                if (isStartClass) {
                    signatures.addAll(Arrays.asList(c.getDeclaredFieldsStatic()));
                    isStartClass = false;
                }
                final Signature[] fields = c.getDeclaredFieldsNonStatic();
                signatures.addAll(Arrays.asList(fields));
            }
        }
        final Signature[] retVal = signatures.toArray(SIGNATURE_ARRAY);
        return retVal;
    }

    /**
     * Returns the number of static fields of a given class.
     * 
     * @param classFile a {@link ClassFile}.
     * @return an {@code int}, the number of static fields
     *         declared by {@code classFile}.
     */
    public int numOfStaticFields(ClassFile classFile) {
        return classFile.getDeclaredFieldsStatic().length;
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
     * @throws WrongClassNameException if the class name specified in {@code bytecode} is different
     *         from {@code classSignature}. 
     */
    public ClassFile loadCreateClass(String classSignature)
    throws InvalidInputException, ClassFileNotFoundException, 
    ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, 
    WrongClassNameException  {
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
     * @throws WrongClassNameException if the class name specified in {@code bytecode} is different
     *         from {@code classSignature}. 
     */
    public ClassFile loadCreateClass(int initiatingLoader, String classSignature, boolean bypassStandardLoading) 
    throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, 
    ClassFileNotAccessibleException, IncompatibleClassFileException, PleaseLoadClassException, 
    BadClassFileVersionException, WrongClassNameException  {
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
                        memberClass = getClassFilePrimitive(toPrimitiveCanonicalName(memberType));
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
                        accessed = defineClass(definingClassLoader, classSignature, findBytecodeResult.bytecode, bypassStandardLoading);
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
     * @param classSignature a {@link String}, the name of the class to be defined. If it is
     *        {@code null} then the method does not check that the class name in {@code byte[]}
     *        {@link Object#equals(Object) equals} this parameter.
     * @param bytecode a {@code byte[]} containing the content of the classfile for the class.
     * @param bypassStandardLoading  a {@code boolean}. If it is {@code true} and the {@code definingClassLoader}
     *        is either {@link ClassLoaders#CLASSLOADER_EXT} or {@link ClassLoaders#CLASSLOADER_APP}, 
     *        bypasses the standard loading procedure and loads itself the superclass/superinterfaces, 
     *        instead of raising {@link PleaseLoadClassException}.
     * @return the defined {@code classFile}.
     * @throws InvalidInputException if  
     *         {@code definingClassLoader < }{@link ClassLoaders#CLASSLOADER_BOOT CLASSLOADER_BOOT}, or if
     *         {@code bytecode} is invalid ({@code null}).
     * @throws AlreadyDefinedClassException if {@code classSignature} was already defined for 
     *         {@code definingClassLoader}.
     * @throws BadClassFileVersionException if {@code bytecode}'s classfile version number is unsupported
     *         by this version of JBSE.
     * @throws WrongClassNameException if the class name specified in {@code bytecode} is different
     *         from {@code classSignature}. 
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
    public ClassFile defineClass(int definingClassLoader, String classSignature, byte[] bytecode, boolean bypassStandardLoading) 
    throws InvalidInputException, AlreadyDefinedClassException, BadClassFileVersionException, 
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
        if (classDummy.getMajorVersion() < JAVA_6 || classDummy.getMajorVersion() > JAVA_8) {
            throw new BadClassFileVersionException("The classfile for " + classDummy.getClassName() + " has unsupported version " + classDummy.getMajorVersion() + "." + classDummy.getMinorVersion() +".");
        }
        
        //checks the name
        if (classSignature != null && !classDummy.getClassName().equals(classSignature)) {
            throw new WrongClassNameException("The classfile for class " + classDummy.getClassName() + " has different class name " + classDummy.getClassName());
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

        //creates a complete ClassFile for the class and registers it
        final ClassFile retVal = createClassFileClass(classDummy, superClass, superInterfaces);
        addClassFileClassArray(definingClassLoader, retVal);
        return retVal;
    }
    
    /**
     * Registers a system package.
     * 
     * @param classSignature a {@link String}, a signature of a loaded
     *        system class.
     * @param loadedFrom a {@link String}, the name of the jar file
     *        or directory from where {@code classSignature} was loaded.
     */
    private void registerSystemPackage(String classSignature, String loadedFrom) {
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
        final Iterable<String> paths = (initiatingLoader == CLASSLOADER_BOOT ? this.cp.bootClassPath() :
                                        initiatingLoader == CLASSLOADER_EXT ? this.cp.extClassPath() :
                                        this.cp.userClassPath());
        for (String _path : paths) {
            try {
                final Path path = Paths.get(_path); 
                if (Files.isDirectory(path)) {
                    final Path pathOfClass = path.resolve(className + ".class");
                    return new FindBytecodeResult(Files.readAllBytes(pathOfClass), _path);
                } else if (Files.isRegularFile(path) && _path.endsWith(".jar")) {
                    try (final JarFile f = new JarFile(_path)) {
                        final JarEntry e = f.getJarEntry(className + ".class");
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
                        return new FindBytecodeResult(outStr.toByteArray(), _path);
                    }
                }
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
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException {
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
     * Performs field resolution (see JVMS v8. section 5.4.3.2).
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
    WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, FieldNotAccessibleException, 
    FieldNotFoundException, PleaseLoadClassException {
        //checks the parameters
        if (fieldSignature.getName() == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveField with an invalid signature (null name field).");
        }

        //resolves the class of the field signature
        final ClassFile fieldSignatureClass = resolveClass(accessor, fieldSignature.getClassName(), bypassStandardLoading);

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
     * @param startClass a {@link ClassFile} for the class where lookup starts.
     * @param fieldSignature a field {@link Signature}. Only the name and the descriptor
     *        will be considered.
     * @return the {@link ClassFile} for the class where a field with the type 
     *         and name of {@code fieldSignature} is declared, 
     *         or {@code null} if such declaration does not exist in the 
     *         hierarchy. 
     */
    private ClassFile resolveFieldLookup(ClassFile startClass, Signature fieldSignature) {
        //if the field is declared in startClass,
        //lookup finishes
        if (startClass.hasFieldDeclaration(fieldSignature)) {
            return startClass;
        }

        //otherwise, lookup recursively in all the immediate superinterfaces
        for (ClassFile classFileSuperinterface : startClass.getSuperInterfaces()) {
            final ClassFile classFileLookup = resolveFieldLookup(classFileSuperinterface, fieldSignature);
            if (classFileLookup != null) {
                return classFileLookup;
            }
        }

        //otherwise, lookup recursively in the superclass (if any)
        final ClassFile classFileSuperclass = startClass.getSuperclass();
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
    WrongClassNameException, ClassFileNotAccessibleException, IncompatibleClassFileException, MethodNotFoundException, 
    MethodNotAccessibleException, PleaseLoadClassException {
        //checks the parameters
        if (methodSignature.getName() == null) {
            throw new InvalidInputException("Invoked " + this.getClass().getName() + ".resolveMethod with an invalid signature (null name field).");
        }

        //resolves the class of the method's signature
        final ClassFile methodSignatureClass = resolveClass(accessor, methodSignature.getClassName(), bypassStandardLoading);

        //checks if the symbolic reference to the method class 
        //is an interface (JVMS v8, section 5.4.3.3 step 1 and section 5.4.3.4 step 1)
        if (isInterface != methodSignatureClass.isInterface()) {
            throw new IncompatibleClassFileException(methodSignature.getClassName());
        }

        //attempts to find a superclass or superinterface containing 
        //a declaration for the method
        ClassFile accessed = null;

        //searches for the method declaration in the superclasses; for
        //interfaces this means searching only in the interface
        //(JVMS v8, section 5.4.3.3 step 2 and section 5.4.3.4 step 2)
        for (ClassFile cf : superclasses(methodSignatureClass)) {
            if (!isInterface && cf.hasOneSignaturePolymorphicMethodDeclaration(methodSignature.getName())) {
                accessed = cf; //note that the method has methodSignature.getName() as name and SIGNATURE_POLYMORPHIC_DESCRIPTOR as descriptor 
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
            final ClassFile cfJAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT);
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
            for (ClassFile cf : superinterfaces(methodSignatureClass)) {
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
            if (isMethodAccessible(accessor, accessed, methodSignatureClass, methodSignature)) {
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
                stream(superinterfaces(superinterface))
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
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException {
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
            } else { //isPrimitive(paramType)
                retVal[i] = getClassFilePrimitive(toPrimitiveCanonicalName(paramType));
            }
            ++i;
        }
        final String returnType = splitReturnValueDescriptor(descriptor);
        if (isArray(returnType) || isReference(returnType)) {
            retVal[retVal.length - 1] = resolveClass(accessor, className(returnType), bypassStandardLoading);
        } else { //isPrimitive(returnType)
            retVal[retVal.length - 1] = getClassFilePrimitive(toPrimitiveCanonicalName(returnType));
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
        //TODO this implementation is incomplete: some kinds of nested (member) classes may have all the visibility accessors. Also, the treatment of arrays might be wrong.
        if (accessed.isPublic()) {
            return true;
        } else { //cfAccessed.isPackage()
            return (accessed.getDefiningClassLoader() == accessor.getDefiningClassLoader() && accessed.getPackageName().equals(accessor.getPackageName()));
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
        final boolean sameRuntimePackage = (accessor.getDefiningClassLoader() == accessed.getDefiningClassLoader() && accessed.getPackageName().equals(accessor.getPackageName()));
        if (accessed.isFieldPublic(fieldSignature)) {
            return true;
        } else if (accessed.isFieldProtected(fieldSignature)) {
            if (sameRuntimePackage) {
                return true;
            } else if (!isSubclass(accessor, accessed)) {
                return false;
            } else if (accessed.isFieldStatic(fieldSignature)) {
                return true;
            } else {
                return isSubclass(accessor, fieldSignatureClass) || isSubclass(fieldSignatureClass, accessor);
            }
        } else if (accessed.isFieldPackage(fieldSignature)) {
            return sameRuntimePackage; 
        } else { //accessed.isFieldPrivate(fieldSignature)
            return (accessed == accessor); 
            //TODO there was a || accessor.isInner(accessed) clause but it is *wrong*!
        }
    }

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
        final boolean sameRuntimePackage = (accessor.getDefiningClassLoader() == accessed.getDefiningClassLoader() && accessed.getPackageName().equals(accessor.getPackageName()));
        if (accessed.isMethodPublic(methodSignature)) {
            return true;
        } else if (accessed.isMethodProtected(methodSignature)) {
            if (sameRuntimePackage) {
                return true;
            } else if (!isSubclass(accessor, accessed)) {
                return false;
            } else if (accessed.isMethodStatic(methodSignature)) {
                return true;
            } else {
                return isSubclass(accessor, methodSignatureClass) || isSubclass(methodSignatureClass, accessor);
            }
        } else if (accessed.isMethodPackage(methodSignature)) {
            return sameRuntimePackage;
        } else { //accessed.isMethodPrivate(methodSignature)
            return (accessed == accessor);
            //TODO there was a || accessor.isInner(accessed) clause but it is *wrong*!
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
     * @throws MethodNotAccessibleException  if lookup fails and {@link java.lang.IllegalAccessError} should be thrown.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplInterface(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException {
        ClassFile retVal = null;
        
        try {
            //step 1 and 2
            for (ClassFile f : superclasses(receiverClass)) {
                if (f.hasMethodDeclaration(methodSignature) && !f.isMethodStatic(methodSignature)) {
                    retVal = f;
                    
                    //third run-time exception
                    if (!retVal.isMethodPublic(methodSignature)) {
                        throw new MethodNotAccessibleException(methodSignature.toString());
                    }

                    //fourth run-time exception
                    if (retVal.isMethodAbstract(methodSignature)) {
                        throw new MethodAbstractException(methodSignature.toString());
                    }
                    
                    break;
                }
            }

            //step 3
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
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplSpecial(ClassFile currentClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodAbstractException, IncompatibleClassFileException {
        //determines whether should start looking for the implementation in 
        //the superclass of the current class (virtual semantics, for super 
        //calls) or in the class of the resolved method (nonvirtual semantics, 
        //for <init> and private methods)
        final boolean useVirtualSemantics = 
            (!"<init>".equals(methodSignature.getName()) &&
             (resolutionClass.isInterface() || isSubclass(currentClass.getSuperclass(), resolutionClass)) && 
             currentClass.isSuperInvoke());
        final ClassFile c = (useVirtualSemantics ? currentClass.getSuperclass() : resolutionClass);
        
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
            if (retVal == null && !c.isInterface() && c.getSuperclassName() != null) {
                for (ClassFile f : superclasses(c.getSuperclass())) {
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
                final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT);
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
     * @throws MethodNotFoundException if no declaration of {@code methodSignature} is found in 
     *         {@code resolutionClass}. 
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplVirtual(ClassFile receiverClass, ClassFile resolutionClass, Signature methodSignature) 
    throws MethodNotFoundException, MethodAbstractException, IncompatibleClassFileException {
        if (resolutionClass.isMethodSignaturePolymorphic(methodSignature)) {
            return resolutionClass;
        } else {
            ClassFile retVal = null;
            
            //step 1 and 2
            for (ClassFile f : superclasses(receiverClass)) {
                if (f.hasMethodDeclaration(methodSignature) && !f.isMethodStatic(methodSignature)) {
                    if (overrides(f, resolutionClass, methodSignature, methodSignature)) {
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
            
            return retVal;
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
        final ClassFile cf_JAVA_OBJECT = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT);
        if (cf_JAVA_OBJECT == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Object.");
        }
        final ClassFile cf_JAVA_CLONEABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLONEABLE);
        if (cf_JAVA_CLONEABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArray was unable to find standard class java.lang.Cloneable.");
        } 
        final ClassFile cf_JAVA_SERIALIZABLE = getClassFileClassArray(CLASSLOADER_BOOT, JAVA_SERIALIZABLE);
        if (cf_JAVA_SERIALIZABLE == null) {
            throw new UnexpectedInternalException("Method " + this.getClass().getName() + ".createClassFileArrays was unable to find standard class java.lang.Cloneable.");
        }
        
        if (source.isInterface()) {
            if (target.isInterface()) {
                return isSubclass(source, target);
            } else if (target.isArray()) {
                return false; //should not happen (verify error)
            } else {
                return (target == cf_JAVA_OBJECT);
            }
        } else if (source.isArray()) {
            if (target.isInterface()) {
                return (target == cf_JAVA_CLONEABLE || target == cf_JAVA_SERIALIZABLE);
            } else if (target.isArray()) {
                final ClassFile sourceComponent = source.getMemberClass();
                final ClassFile targetComponent = target.getMemberClass();
                if (sourceComponent.isPrimitive() && targetComponent.isPrimitive()) {
                    return (sourceComponent == targetComponent);
                } else if ((sourceComponent.isReference() && targetComponent.isReference()) ||
                           (sourceComponent.isArray() && targetComponent.isArray())) {
                    return isAssignmentCompatible(sourceComponent, targetComponent);
                } else {
                    return false;
                }
            } else {
                return (target == cf_JAVA_OBJECT);
            }
        } else {
            if (target.isArray()) {
                return false; //should not happen (verify error)
            } else {
                return isSubclass(source, target);
            }
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
     */
    public boolean overrides(ClassFile sub, ClassFile sup, Signature subMethodSignature, Signature supMethodSignature) 
    throws MethodNotFoundException {
        //first case: same method
        if (sub == sup && 
            subMethodSignature.getDescriptor().equals(supMethodSignature.getDescriptor()) &&
            subMethodSignature.getName().equals(supMethodSignature.getName()) ) {
            return true;
        }
        
        //second case: all of the following must be true
        //1- subMethod's class is a subclass of supMethod's class 
        if (!isSubclass(sub.getSuperclass(), sup)) {
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
        for (ClassFile cf : superclasses(sub.getSuperclass())) {
            if (cf == sup) {
                break;
            }
            if (cf.hasMethodDeclaration(subMethodSignature)) {
                if (overrides(sub, cf, subMethodSignature, supMethodSignature) && overrides (cf, sup, subMethodSignature, supMethodSignature)) {
                    return true;
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
