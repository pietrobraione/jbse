package jbse.bc;

import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.common.Type.toPrimitiveInternalName;
import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isPrimitiveCanonicalName;
import static jbse.common.Type.isReference;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.Type;
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
    private final HashMap<String, ArrayList<Signature>> allFieldsOf;
    private ClassFileStore cfs; //not final because of clone

    /**
     * Constructor.
     * 
     * @param cp a {@link Classpath}.
     * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
     *        The class must have an accessible constructor with two parameters, the first a 
     *        {@link ClassFileStore}, the second a {@link Classpath}.
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
        this.cfs = new ClassFileStore(cp, fClass);
        this.expansionBackdoor = expansionBackdoor;
        this.allFieldsOf = new HashMap<>();
    }

    /**
     * Returns the {@link Classpath} of this hierarchy.
     * 
     * @return a {@link Classpath} (clone of that used
     *         to create this hierarchy).
     */
    public Classpath getClasspath() {
        return this.cp.clone();
    }

    /**
     * Given a class name returns the correspondent {@link ClassFile}.
     * To avoid name clashes it does not manage primitive classes.
     * 
     * @param className a {@link String}, the name of the searched class.
     * @return the {@link ClassFile} of the correspondent class.
     * @throws BadClassFileException when the class file does not 
     *         exist or is ill-formed.
     */
    public ClassFile getClassFile(String className) 
    throws BadClassFileException {
        final ClassFile retval = this.cfs.getClassFile(className);
        if (retval instanceof ClassFileBad) {
            throw ((ClassFileBad) retval).getException();
        }
        return retval;
    }
    
    /**
     * Wraps a {@link ClassFile} to (temporarily) add
     * some constants to its constant pool.
     * 
     * @param classToWrap a {@link String}, 
     *        the name of the class to wrap.
     * @param constants a {@link Map}{@code <}{@link Integer}{@code , }{@link ConstantPoolValue}{@code >}, 
     *        mapping indices to corresponding constant 
     *        values in the expanded constant pool.
     * @param signatures a {@link Map}{@code <}{@link Integer}{@code , }{@link Signature}{@code >}, 
     *        mapping indices to corresponding {@link Signature}s 
     *        in the expanded constant pool.
     * @param classes a {@link Map}{@code <}{@link Integer}{@code , }{@link String}{@code >}, 
     *        mapping indices to corresponding class names 
     *        in the expanded constant pool.
     */
    public void wrapClassFile(String classToWrap,
                     Map<Integer, ConstantPoolValue> constants, 
                     Map<Integer, Signature> signatures,
                     Map<Integer, String> classes) {
        this.cfs.wrapClassFile(classToWrap, constants, signatures, classes);
    }
    
    /**
     * Unwraps a previously wrapped {@link ClassFile}.
     * 
     * @param classToUnwrap classToWrap a {@link String}, 
     *        the name of the class to unwrap.
     */
    public void unwrapClassFile(String classToUnwrap) {
        this.cfs.unwrapClassFile(classToUnwrap);
    }

    /**
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the canonical name of a primitive type 
     *        (see JLS v8, section 6.7).
     * @return the {@link ClassFile} of the correspondent class.
     * @throws BadClassFileException when the class file does not 
     *         exist or is ill-formed (happens when {@code typeName}
     *         is not the name of a primitive type).
     */
    public ClassFile getClassFilePrimitive(String typeName)
    throws BadClassFileException {
        final ClassFile retval = 
            this.cfs.getClassFilePrimitive(toPrimitiveInternalName(typeName));
        if (retval instanceof ClassFileBad) {
            throw ((ClassFileBad) retval).getException();
        }
        return retval;
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
     * Lists the concrete subclasses of a class. <br />
     * <em>Note:</em> An exact implementation of this method, 
     * searching the classpath for all the concrete subclasses 
     * of an arbitrary class, would be too demanding. Thus this
     * implementation returns {@code className}, if it is not 
     * an interface or an abstract class, and all the classes 
     * associated to {@code className} in the 
     * {@code expansionBackdoor} provided at construction time.
     * 
     * @param className a {@link String}, the name of a class.
     * @return A {@link Set}{@code <}{@link String}{@code >} of class
     *         names.
     * @throws BadClassFileException when the class file does not 
     *         exist or is ill-formed.
     */
    public Set<String> getAllConcreteSubclasses(String className) 
    throws BadClassFileException {
        final HashSet<String> retVal = new HashSet<>();
        final ClassFile cf = this.getClassFile(className);
        if (!cf.isAbstract()) {
            retVal.add(className);
        }
        final Set<String> moreSubclasses = this.expansionBackdoor.get(className);
        if (moreSubclasses != null) {
            retVal.addAll(moreSubclasses);
        }
        return retVal;
    }


    /**
     * Produces all the superclasses of a given class.
     * 
     * @param startClassName the name of the class whose superclasses 
     *        are returned. Nonprimitive classes should be indicated by their
     *        internal names and primitive classes should be indicated by their
     *        canonical names (see JLS v8, section 6.7).
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superclasses of {@code startClassName} (included). If
     *         {@code startClassName == null} an empty {@link Iterable} is
     *         returned.
     */
    public Iterable<ClassFile> superclasses(String startClassName) {
        return new IterableSuperclasses(startClassName);
    }

    /**
     * Produces all the superinterfaces of a given class.
     * 
     * @param startClassName the name of the class whose superinterfaces 
     *        are returned. Nonprimitive classes should be indicated by their
     *        internal names and primitive classes should be indicated by their
     *        canonical names (see JLS v8, section 6.7).
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superinterfaces of {@code startClassName} (included if
     *         it is an interface). If {@code startClassName == null} an empty 
     *         {@link Iterable} is returned. A same superinterface is not iterated
     *         more than once even if the class inherits it more than once. 
     */
    public Iterable<ClassFile> superinterfaces(String startClassName) {
        return new IterableSuperinterfaces(startClassName);
    }

    /**
     * Checks whether a class/interface is a subclass of/implements another one.
     * 
     * @param sub a {@link String}, a class/interface name
     * @param sup a {@link String}, another class/interface name
     * @return {@code true} if {@code sub.equals(sup)}, or {@code sub} 
     *         extends {@code sup}, or {@code sub} implements {@code sup}, 
     *         {@code false} otherwise.
     */
    public boolean isSubclass(String sub, String sup) {
        if (Type.isArray(sub) && Type.isArray(sup)) {
            final String subMember = Type.getArrayMemberType(sub); 
            final String supMember = Type.getArrayMemberType(sup);
            if (Type.isPrimitive(subMember) && Type.isPrimitive(supMember)) {
                return subMember.equals(supMember);
            } else if (Type.isReference(subMember) && Type.isReference(supMember)) {
                final String subMemberClass = Type.className(subMember);
                final String supMemberClass = Type.className(supMember);
                return isSubclass(subMemberClass, supMemberClass);
            } else if (Type.isArray(subMember) && Type.isArray(supMember)) {
                return isSubclass(subMember, supMember);
            } else {
                return false;
            }
        } else {
            for (ClassFile f : superclasses(sub)) { 
                if (f.getClassName().equals(sup)) {
                    return true;
                } 
            }
            for (ClassFile f : superinterfaces(sub)) {
                if (f.getClassName().equals(sup)) {
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
        private String startClassName;

        /**
         * Constructor.
         * 
         * @param startClassName 
         *        The name of the class from where the iteration is started. 
         */
        public IterableSuperclasses(String startClassName) {
            this.startClassName = startClassName;
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
            private ClassFile nextClassFile;

            public MyIterator(String startClassName) {
                if (startClassName == null) {
                    this.nextClassFile = null;
                } else if (isPrimitiveCanonicalName(startClassName)) {
                    this.nextClassFile = 
                        ClassHierarchy.this.cfs.getClassFilePrimitive(toPrimitiveInternalName(startClassName));
                } else {
                    this.nextClassFile = ClassHierarchy.this.cfs.getClassFile(startClassName);
                }
            }

            public boolean hasNext() {
                return (this.nextClassFile != null);
            }

            public ClassFile next() {
                //ensures the method precondition
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                //stores the return value
                final ClassFile retval = this.nextClassFile;

                //gets the classfile of the superclass
                final String superclassName = retval.getSuperclassName();
                if (superclassName == null) {
                    //no superclass
                    this.nextClassFile = null;
                } else {
                    this.nextClassFile = ClassHierarchy.this.cfs.getClassFile(superclassName);
                } 

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
        private String startClassName;

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
        public IterableSuperinterfaces(String startClassName) {
            this.startClassName = startClassName;
        }

        public Iterator<ClassFile> iterator() {
            return new MyIterator(this.startClassName);
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

            public MyIterator(String startClassName) {
                this.visitedClassFiles = new HashSet<>();
                this.nextClassFiles = new LinkedList<>();
                if (startClassName == null) {
                    return; //keeps the iterator empty
                }
                final ClassFile cf;
                if (isPrimitiveCanonicalName(startClassName)) {
                    cf = ClassHierarchy.this.cfs.getClassFilePrimitive(toPrimitiveInternalName(startClassName));
                } else {
                    cf = ClassHierarchy.this.cfs.getClassFile(startClassName);
                }
                if (cf instanceof ClassFileBad || cf.isInterface()) {
                    this.nextClassFiles.add(cf);
                } else { //is not interface and is not ClassFileBad
                    for (ClassFile cfSuper : superclasses(startClassName)) {
                        this.nextClassFiles.addAll(superinterfacesImmediateFiltered(cfSuper));
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
                if (retVal instanceof ClassFileBad) {
                    this.nextClassFiles.clear(); //stops iteration
                } else { //retVal.isInterface()
                    this.visitedClassFiles.add(retVal);
                    this.nextClassFiles.addAll(superinterfacesImmediateFiltered(retVal));
                }

                //returns the result
                return retVal;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private List<ClassFile> superinterfacesImmediateFiltered(ClassFile base) {
                return base.getSuperInterfaceNames().stream()
                       .map(s -> ClassHierarchy.this.cfs.getClassFile(s))
                       .filter(cf -> !this.visitedClassFiles.contains(cf))
                       .collect(Collectors.toList());
            }
        }
    }

    private static final Signature[] SIGNATURE_ARRAY = new Signature[0];

    /**
     * Returns all the fields known to an object of a given class.
     * 
     * @param className a {@link String}, the name of the class.
     * @return a {@link Signature}{@code []}. It will contain all the 
     *         {@link Signature}s of the class' static fields, followed
     *         by all the {@link Signature}s of the class' object (nonstatic) 
     *         fields, followed by all the {@link Signature}s of the object 
     *         fields of all the superclasses of the class.
     * @throws BadClassFileException if the classfile for the class 
     *         {@code className}, or for one of its superclasses, 
     *         does not exist in the classpath or is ill-formed.
     */	
    public Signature[] getAllFields(String className) throws BadClassFileException {
        ArrayList<Signature> signatures = this.allFieldsOf.get(className);
        if (signatures == null) {
            signatures = new ArrayList<Signature>(0);
            this.allFieldsOf.put(className, signatures);
            boolean isStartClass = true;
            for (ClassFile c : superclasses(className)) {
                if (c instanceof ClassFileBad) {
                    throw ((ClassFileBad) c).getException();
                }
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
     * @param className a {@link String}, the name of the class.
     * @return an {@code int}.
     * @throws BadClassFileException if the classfile for the class 
     *         {@code className} does not exist in the classpath 
     *         or is ill-formed.
     */
    public int numOfStaticFields(String className) throws BadClassFileException {
        final ClassFile c = getClassFile(className);
        return c.getDeclaredFieldsStatic().length;
    }

    /**
     * Performs class (including array class) and interface resolution 
     * (see JVMS v8, section 5.4.3.1).
     * 
     * @param accessor a {@link String}, the signature of the accessor's class.
     * @param classSignature a {@link String}, the signature of the class to be resolved.
     * @throws BadClassFileException if the classfile for the class 
     *         to be resolved does not exist in the classpath or
     *         is ill-formed.
     * @throws ClassFileNotAccessibleException if the resolved class is not accessible
     *         from {@code accessor}.
     */
    public void resolveClass(String accessor, String classSignature) 
    throws BadClassFileException, ClassFileNotAccessibleException {
        //TODO implement complete class creation as in JVMS 2nd ed, sec. 5.3

        final ClassFile cf = getClassFile(classSignature);
        if (cf instanceof ClassFileBad) {
            throw ((ClassFileBad) cf).getException();
        }

        //checks accessibility and either returns or raises an exception 
        if (isClassAccessible(accessor, classSignature)) {
            return;
        } else {
            throw new ClassFileNotAccessibleException(classSignature);
        }
    }

    /**
     * Performs field resolution (see JVMS v8. section 5.4.3.2).
     * 
     * @param accessor a {@link String}, the signature of the accessor's class.
     * @param fieldSignature the {@link Signature} of the field to be resolved.
     * @return the {@link Signature} of the declaration of the resolved field.
     * @throws BadClassFileException if the classfile for {@code fieldSignature}'s 
     *         class and its superclasses does not exist in the classpath.
     * @throws ClassFileNotAccessibleException if the resolved class is not accessible
     *         from {@code accessor}.
     * @throws FieldNotAccessibleException if the resolved field cannot 
     *         be accessed by {@code accessor}.
     * @throws FieldNotFoundException if resolution of the field fails.
     */
    public Signature resolveField(String accessor, Signature fieldSignature) 
    throws BadClassFileException, ClassFileNotAccessibleException, 
    FieldNotAccessibleException, FieldNotFoundException {
        //first resolves the class
        resolveClass(accessor, fieldSignature.getClassName());

        //then performs field lookup
        final Signature fieldSignatureResolved = resolveFieldLookup(fieldSignature);

        //if nothing has been found, raises an exception
        if (fieldSignatureResolved == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }

        //if a declaration has been found, then it checks accessibility 
        //and either returns its signature or raises an exception 
        try {
            if (isFieldAccessible(accessor, fieldSignatureResolved)) {
                //everything went ok
                return fieldSignatureResolved;
            } else {
                throw new FieldNotAccessibleException(fieldSignatureResolved.toString());
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
     * @param fieldSignature a field {@link Signature}.
     * @return the {@link Signature} for the declaration for {@code fieldSignature}, 
     *         or {@code null} if such declaration does not exist. 
     */
    private Signature resolveFieldLookup(Signature fieldSignature) throws BadClassFileException {
        final ClassFile classFile = getClassFile(fieldSignature.getClassName());
        
        //if the field is declared in its signature's class,
        //just return the input signature
        if (classFile.hasFieldDeclaration(fieldSignature)) {
            return fieldSignature;
        }

        //otherwise, lookup recursively in all the immediate superinterfaces
        for (String superinterfaceName : classFile.getSuperInterfaceNames()) {
            final ClassFile classFileSuperinterface = getClassFile(superinterfaceName);
            final Signature fieldSignatureSuperinterface = new Signature(classFileSuperinterface.getClassName(), fieldSignature.getDescriptor(), fieldSignature.getName());
            final Signature fieldSignatureResolved = resolveFieldLookup(fieldSignatureSuperinterface);
            if (fieldSignatureResolved != null) {
                return fieldSignatureResolved;
            }
        }

        //otherwise, lookup recursively in the superclass (if any)
        final String superclassName = classFile.getSuperclassName();
        if (superclassName == null) {
            //no superclass: lookup failed
            return null;
        } else {
            final ClassFile classFileSuperclass = getClassFile(superclassName);
            final Signature fieldSignatureSuperclass = new Signature(classFileSuperclass.getClassName(), fieldSignature.getDescriptor(), fieldSignature.getName());
            final Signature fieldSignatureResolved = resolveFieldLookup(fieldSignatureSuperclass);
            return fieldSignatureResolved;
        }
    }
    
    /**
     * Performs both method and interface method resolution 
     * (see JVMS v8, section 5.4.3.3 and 5.4.3.4).
     * 
     * @param accessor a {@link String}, the signature of the accessor's class.
     * @param methodSignature the {@link Signature} of the method to be resolved.
     * @param isInterface {@code true} iff the method to be resolved is required to be 
     *        an interface method (i.e., if the bytecode which triggered the resolution
     *        is invokeinterface).
     * @return the {@link Signature} of the declaration of the resolved method.
     * @throws BadClassFileException if the classfile for any class involved in the
     *         resolution does not exist on the classpath or is ill-formed.
     * @throws IncompatibleClassFileException if the symbolic reference in 
     *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         to the method disagrees with {@code isInterface}.
     * @throws MethodNotFoundException if resolution fails.
     * @throws MethodNotAccessibleException if the resolved method is not accessible 
     *         by {@code accessor}.
     */
    public Signature resolveMethod(String accessor, Signature methodSignature, boolean isInterface) 
    throws BadClassFileException, IncompatibleClassFileException,  
    MethodNotFoundException, MethodNotAccessibleException {
        //gets the classfile for class mentioned in the method's *invocation*
        //TODO implement class resolution and loading!
        final ClassFile classFile = getClassFile(methodSignature.getClassName());

        //checks if the symbolic reference to the method class 
        //is an interface (JVMS v8, section 5.4.3.3 step 1 and section 5.4.3.4 step 1)
        if (isInterface != classFile.isInterface()) {
            throw new IncompatibleClassFileException(methodSignature.getClassName());
        }

        //attempts to find a superclass or superinterface containing 
        //a declaration for the method
        Signature methodSignatureResolved = null;

        //searches for the method declaration in the superclasses; for
        //interfaces this means searching only in the interface
        //(JVMS v8, section 5.4.3.3 step 2 and section 5.4.3.4 step 2)
        for (ClassFile cf : superclasses(methodSignature.getClassName())) {
            if (cf instanceof ClassFileBad) {
                throw ((ClassFileBad) cf).getException();
            } else if (!isInterface && cf.hasOneSignaturePolymorphicMethodDeclaration(methodSignature.getName())) {
                methodSignatureResolved = 
                    new Signature(cf.getClassName(), SIGNATURE_POLYMORPHIC_DESCRIPTOR, methodSignature.getName());
                //TODO resolve all the class names in methodSignature.getDescriptor()
                break;
            } else if (cf.hasMethodDeclaration(methodSignature)) {
                methodSignatureResolved = 
                    new Signature(cf.getClassName(), methodSignature.getDescriptor(), methodSignature.getName());
                break;
            }
        }
        
        //searches for the method declaration in java.lang.Object, thing that
        //the previous code does not do in the case of interfaces
        //(JVMS v8, section 5.4.3.4 step 3)
        if (methodSignatureResolved == null && isInterface) {
            final ClassFile cfJAVA_OBJECT = getClassFile(JAVA_OBJECT);
            if (cfJAVA_OBJECT.hasMethodDeclaration(methodSignature)) {
                methodSignatureResolved = 
                    new Signature(JAVA_OBJECT, methodSignature.getDescriptor(), methodSignature.getName());
            }
        }

        //searches for a single, non-abstract, maximally specific superinterface method 
        //(JVMS v8, section 5.4.3.3 step 3a and section 5.4.3.4 step 4)
        if (methodSignatureResolved == null) {
            final Set<Signature> nonabstractMaxSpecMethods = maximallySpecificSuperinterfaceMethods(methodSignature, true);
            if (nonabstractMaxSpecMethods.size() == 1) {
                methodSignatureResolved = nonabstractMaxSpecMethods.iterator().next();
            }
        }

        //searches in the superinterfaces
        //(JVMS v8, section 5.4.3.3 step 3b and 5.4.3.4 step 5)
        if (methodSignatureResolved == null) {
            for (ClassFile cf : superinterfaces(methodSignature.getClassName())) {
                if (cf instanceof ClassFileBad) {
                    throw ((ClassFileBad) cf).getException();
                } else if (cf.hasMethodDeclaration(methodSignature) && 
                          !cf.isMethodPrivate(methodSignature) && 
                          !cf.isMethodStatic(methodSignature)) {
                    methodSignatureResolved = 
                        new Signature(cf.getClassName(), methodSignature.getDescriptor(), methodSignature.getName());
                    break;
                }
            }
        }

        //exits if lookup failed
        if (methodSignatureResolved == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }

        //if a declaration has found, then it checks accessibility and, in case, 
        //raises IllegalAccessError; otherwise, returns the resolved method signature
        try {
            if (isMethodAccessible(accessor, methodSignatureResolved)) {
                //everything went ok
                return methodSignatureResolved;
            } else {
                throw new MethodNotAccessibleException(methodSignatureResolved.toString());
            }
        } catch (ClassFileNotFoundException | MethodNotFoundException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    /**
     * Returns the maximally specific superinterface methods
     * of a given method signature.
     * 
     * @param methodSignature a method {@link Signature}.
     * @param nonAbstract a {@code boolean}
     * @return a {@link Set}{@code <}{@link Signature}{@code >} containing maximally-specific 
     *         superinterface methods of {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         for {@code methodSignature.}{@link Signature#getDescriptor() getDescriptor()}
     *         and {@code methodSignature.}{@link Signature#getName() getName()}, 
     *         as for JVMS v8, section 5.4.3.3. If {@code nonAbstract == true}, such set
     *         contains all and only the maximally-specific superinterface methods that are 
     *         not abstract. If {@code nonAbstract == false}, it contains exactly all the 
     *         maximally-specific superinterface methods.
     * @throws BadClassFileException if the classfile for {@code methodSignature.}{@link Signature#getClassName() getClassName()}
     *         or any of its superinterfaces does not exist on the classpath or is ill-formed.
     */
    private Set<Signature> maximallySpecificSuperinterfaceMethods(Signature methodSignature, boolean nonAbstract) 
    throws BadClassFileException {
        final HashSet<String> maximalSet = new HashSet<>();
        final HashSet<String> nextSet = new HashSet<>();
        final HashSet<String> dominatedSet = new HashSet<>();
        
        //initializes next with all the superinterfaces of methodSignature's class
        nextSet.addAll(getClassFile(methodSignature.getClassName()).getSuperInterfaceNames());
        
        while (!nextSet.isEmpty()) {
            //picks a superinterface from the next set
            final String superinterface = nextSet.iterator().next();
            final ClassFile cfSuperinterface = getClassFile(superinterface);
            nextSet.remove(superinterface);

            //determine all the (strict) superinterfaces of the superinterface
            final Iterable<ClassFile> cfSuperinterfaceSuperinterfaces = superinterfaces(cfSuperinterface.getClassName());
            final Set<String> superinterfaceSuperinterfaces = 
                stream(cfSuperinterfaceSuperinterfaces)
                .map(ClassFile::getClassName)
                .collect(Collectors.toSet());
            superinterfaceSuperinterfaces.remove(superinterface);            
            
            //look for a method declaration of methodSignature in the superinterface 
            try {
                if (cfSuperinterface.hasMethodDeclaration(methodSignature) &&  !cfSuperinterface.isMethodPrivate(methodSignature) && !cfSuperinterface.isMethodStatic(methodSignature)) {
                    //method declaration found: add the superinterface 
                    //to maximalSet...
                    maximalSet.add(cfSuperinterface.getClassName());
                    
                    //...remove the superinterface's strict superinterfaces
                    //from maximalSet, and add them to dominatedSet
                    maximalSet.removeAll(superinterfaceSuperinterfaces);
                    dominatedSet.addAll(superinterfaceSuperinterfaces);
                } else if (!dominatedSet.contains(superinterface)) {
                    //no method declaration: add to nextSet all the direct 
                    //superinterfaces of the superinterface that are not 
                    //dominated; skips this step if the superinterface is 
                    //itself dominated
                    nextSet.addAll(cfSuperinterface.getSuperInterfaceNames());
                    nextSet.removeAll(dominatedSet);
                }
            } catch (MethodNotFoundException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        return maximalSet.stream()
            .map(s -> new Signature(s, methodSignature.getDescriptor(), methodSignature.getName()))
            .filter(s -> {
                try {
                    return (nonAbstract ? !(getClassFile(s.getClassName()).isMethodAbstract(s)) : true);
                } catch (MethodNotFoundException | BadClassFileException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            })
            .collect(Collectors.toSet());
    }
    
    /**
     * Converts an iterable to a stream.
     * See <a href="https://stackoverflow.com/a/23177907/450589">https://stackoverflow.com/a/23177907/450589</a>.
     * @param it
     * @return
     */
    private static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
    
    /**
     * Performs method type resolution (JVMS v8, section 5.4.3.5) without 
     * creating the {@link java.lang.invoke.MethodType} instance.
     * 
     * @param accessor a {@link String}, the signature of the accessor's class.
     * @param descriptor a {@link String}, the descriptor of a method. 
     * @throws BadClassFileException if the classfile for one of the classes 
     *         in {@code methodSignature}'s descriptor does not exist in the 
     *         classpath or is ill-formed.
     * @throws ClassFileNotAccessibleException if one of the classes 
     *         in {@code methodSignature}'s descriptor is not accessible
     *         from {@code accessor}.
     */
    public void resolveMethodType(String accessor, String descriptor) 
    throws BadClassFileException, ClassFileNotAccessibleException {
        final String[] paramsTypes = splitParametersDescriptors(descriptor);
        for (String paramType: paramsTypes) {
            if (isArray(paramType) || isReference(paramType)) {
                resolveClass(accessor, className(paramType));
            }
        }
        final String returnType = splitReturnValueDescriptor(descriptor);
        if (isArray(returnType) || isReference(returnType)) {
            resolveClass(accessor, className(returnType));
        }
    }

    /**
     * Checks whether a class/interface is accessible to another class/interface
     * according to JVMS v8, section 5.4.4.
     * 
     * @param accessor a {@link String}, the signature of a class or interface.
     * @param accessed a {@link String}, the signature of a class or interface.
     * @return {@code true} iff {@code accessed} is accessible to 
     *         {@code accessor}.
     * @throws BadClassFileException if the classfiles for {@code accessor}
     *         or {@code accessed} are not found in the classpath or are
     *         ill-formed.
     */
    private boolean isClassAccessible(String accessor, String accessed) 
    throws BadClassFileException {
        //TODO this implementation is incomplete: some kinds of nested (member) classes may have all the visibility accessors. Also, the treatment of arrays is wrong.
        final ClassFile cfAccessed = getClassFile(accessed);
        if (cfAccessed.isPublic()) {
            return true;
        } else { //cfAccessed.isPackage()
            final ClassFile cfAccessor = getClassFile(accessor);
            return cfAccessed.getPackageName().equals(cfAccessor.getPackageName());
        }
    }

    /**
     * Checks whether a field is accessible to a class/interface
     * according to JVMS v8, section. 5.4.4.
     * 
     * @param accessor a {@link String}, the name of a class or interface.
     * @param accessed a {@link Signature}, the signature of a field declaration.
     * @return {@code true} iff {@code accessed} is accessible to 
     *         {@code accessor}.
     * @throws BadClassFileException if the classfiles for {@code accessor}
     *         or {@code accessed.}{@link Signature#getClassName() getClassName()} 
     *         are not found in the classpath or are ill-formed.
     * @throws FieldNotFoundException if the {@code accessed} field is not 
     *         found in its classfile. 
     */
    private boolean isFieldAccessible(String accessor, Signature accessed) 
    throws BadClassFileException, FieldNotFoundException {
        final ClassFile cfAccessed = getClassFile(accessed.getClassName());
        final ClassFile cfAccessor = getClassFile(accessor);
        final boolean samePackage = cfAccessed.getPackageName().equals(cfAccessor.getPackageName());
        if (cfAccessed.isFieldPublic(accessed)) {
            return true;
        } else if (cfAccessed.isFieldProtected(accessed)) {
            if (samePackage) {
                return true;
            } else if (!isSubclass(accessor, accessed.getClassName())) {
                return false;
            } else if (cfAccessed.isFieldStatic(accessed)) {
                return true;
            } else {
                //gets the fields declarations in the accessed classfile
                final Signature[] declaredFields = cfAccessed.getDeclaredFieldsNonStatic();
                
                //looks for the class of the declared field and checks it
                for (Signature decl : declaredFields) {
                    if (decl.getDescriptor().equals(accessed.getDescriptor()) && decl.getName().equals(accessed.getName())) {
                        return isSubclass(accessor, decl.getClassName()) || isSubclass(decl.getClassName(), accessor);
                    }
                }
                
                //if we reach here, the previous for loop did not find
                //the field in the declarations of cfAccessed, which 
                //should never happen
                throw new UnexpectedInternalException("did not find in class " + accessed.getClassName() + " a declaration for field " + accessed.getDescriptor() + ":" + accessed.getName());
            }
        } else if (cfAccessed.isFieldPackage(accessed)) {
            return samePackage; 
        } else { //cfAccessed.isFieldPrivate(fld)
            return accessed.getClassName().equals(accessor); 
            //TODO there was a || cfAccessor.isInner(cfAccessed) clause but it is *wrong*!
        }
    }

    /**
     * Checks whether a method is accessible to a class/interface
     * according to JVMS v8, section 5.4.4.
     * 
     * @param accessor a {@link String}, the signature of a class or interface.
     * @param accessed a {@link Signature}, the signature of a method.
     * @return {@code true} iff {@code accessed} is accessible to 
     *         {@code accessor}.
     * @throws BadClassFileException if the classfiles for {@code accessor}
     *         or {@code accessed.}{@link Signature#getClassName() getClassName()} 
     *         are not found in the classpath or are ill-formed.
     * @throws MethodNotFoundException if the {@code accessed} method is not 
     *         found in its classfile. 
     */
    private boolean isMethodAccessible(String accessor, Signature accessed) 
    throws BadClassFileException, MethodNotFoundException {
        final ClassFile cfAccessed = getClassFile(accessed.getClassName());
        final ClassFile cfAccessor = getClassFile(accessor);
        boolean samePackage = cfAccessed.getPackageName().equals(cfAccessor.getPackageName());
        if (cfAccessed.isMethodPublic(accessed)) {
            return true;
        } else if (cfAccessed.isMethodProtected(accessed)) {
            if (samePackage) {
                return true;
            } else if (!isSubclass(accessor, accessed.getClassName())) {
                return false;
            } else if (cfAccessed.isMethodStatic(accessed)) {
                return true;
            } else {
                //gets the method declarations in the accessed classfile
                final Signature[] declaredMethods = 
                    Stream.concat(Arrays.stream(cfAccessed.getDeclaredMethods()), 
                                  Arrays.stream(cfAccessed.getDeclaredConstructors()))
                    .toArray(Signature[]::new);
                
                //looks for the class of the declared method and checks it
                for (Signature decl : declaredMethods) {
                    if (decl.getDescriptor().equals(accessed.getDescriptor()) && decl.getName().equals(accessed.getName())) {
                        return isSubclass(accessor, decl.getClassName()) || isSubclass(decl.getClassName(), accessor);
                    }
                }
                
                //if we reach here, the previous for loop did not find
                //the field in the declarations of cfAccessed, which 
                //should never happen
                throw new UnexpectedInternalException("did not find in class " + accessed.getClassName() + " a declaration for method " + accessed.getDescriptor() + ":" + accessed.getName());
            }
        } else if (cfAccessed.isMethodPackage(accessed)) {
            return samePackage;
        } else { //cfAccessed.isMethodPrivate(accessed)
            return accessed.getClassName().equals(accessor);
            //TODO there was a || cfAccessor.isInner(cfAccessed) clause but it is *wrong*!
        }
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKEINTERFACE bytecode.
     * 
     * @param receiverClassName the name of the class of the 
     *        method invocation's receiver.
     * @param methodSignatureResolved the {@link Signature} of the resolved method 
     *        which must be looked up.
     * @return the {@link ClassFile} which contains the method implementation of 
     *         {@code methodSignatureResolved}.
     * @throws BadClassFileException when the class file 
     *         with name {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()} 
     *         or that of one of its superclasses does not exist or is ill-formed.
     * @throws MethodNotAccessibleException  if lookup fails and {@link java.lang.IllegalAccessError} should be thrown.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplInterface(String receiverClassName, Signature methodSignatureResolved) 
    throws BadClassFileException, MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException {
        ClassFile retVal = null;
        
        try {
            //step 1 and 2
            for (ClassFile f : superclasses(receiverClassName)) {
                if (f instanceof ClassFileBad) {
                    throw ((ClassFileBad) f).getException();
                } else if (f.hasMethodDeclaration(methodSignatureResolved) && !f.isMethodStatic(methodSignatureResolved)) {
                    retVal = f;
                    
                    //third run-time exception
                    if (!retVal.isMethodPublic(methodSignatureResolved)) {
                        throw new MethodNotAccessibleException(methodSignatureResolved.toString());
                    }

                    //fourth run-time exception
                    if (retVal.isMethodAbstract(methodSignatureResolved)) {
                        throw new MethodAbstractException(methodSignatureResolved.toString());
                    }
                    
                    break;
                }
            }

            //step 3
            if (retVal == null) {
                final Set<Signature> nonabstractMaxSpecMethods = 
                maximallySpecificSuperinterfaceMethods(methodSignatureResolved, true);
                if (nonabstractMaxSpecMethods.size() == 0) {
                    //sixth run-time exception
                    throw new MethodAbstractException(methodSignatureResolved.toString());
                } else if (nonabstractMaxSpecMethods.size() == 1) {
                    retVal = getClassFile(nonabstractMaxSpecMethods.iterator().next().getClassName());
                } else { //nonabstractMaxSpecMethods.size() > 1
                    //fifth run-time exception
                    throw new IncompatibleClassFileException(methodSignatureResolved.toString());
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
     * @param currentClassName the name of the class of the invoker.
     * @param methodSignatureResolved the signature of the resolved method 
     *        which must be looked up.
     * @return the {@link ClassFile} of the class which 
     *         contains the method implementation of 
     *         {@code methodSignatureResolved}.
     * @throws BadClassFileException when the class file 
     *         with name {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplSpecial(String currentClassName, Signature methodSignatureResolved) 
    throws BadClassFileException, MethodAbstractException, IncompatibleClassFileException {
        final String resolutionClassName = methodSignatureResolved.getClassName();
        final ClassFile currentClass = getClassFile(currentClassName);
        final ClassFile resolutionClass = getClassFile(resolutionClassName);

        //determines whether should start looking for the implementation in 
        //the superclass of the current class (virtual semantics, for super 
        //calls) or in the class of the resolved method (nonvirtual semantics, 
        //for <init> and private methods)
        final boolean useVirtualSemantics = 
            (!"<init>".equals(methodSignatureResolved.getName()) &&
             (resolutionClass.isInterface() || isSubclass(currentClass.getSuperclassName(), resolutionClassName)) && 
             currentClass.isSuperInvoke());
        final ClassFile c = (useVirtualSemantics ? 
                             getClassFile(currentClass.getSuperclassName()) : 
                             getClassFile(resolutionClassName));
        
        //applies lookup
        ClassFile retVal = null;
        try {
            //step 1
            if (c.hasMethodDeclaration(methodSignatureResolved) && 
                !c.isMethodStatic(methodSignatureResolved)) {
                retVal = c;
                //third run-time exception
                if (retVal.isMethodAbstract(methodSignatureResolved)) {
                    throw new MethodAbstractException(methodSignatureResolved.toString());
                }
            } 

            //step 2
            if (retVal == null && !c.isInterface() && c.getSuperclassName() != null) {
                for (ClassFile f : superclasses(c.getSuperclassName())) {
                    if (f instanceof ClassFileBad) {
                        throw ((ClassFileBad) f).getException();
                    } else if (f.hasMethodDeclaration(methodSignatureResolved)) {
                        retVal = f;
                        //third run-time exception
                        if (retVal.isMethodAbstract(methodSignatureResolved)) {
                            throw new MethodAbstractException(methodSignatureResolved.toString());
                        }
                        break;
                    }
                }
            }

            //step 3
            if (retVal == null && c.isInterface()) {
                final ClassFile cfJAVA_OBJECT = getClassFile(JAVA_OBJECT);
                if (c.hasMethodDeclaration(methodSignatureResolved) && 
                    !c.isMethodStatic(methodSignatureResolved) && 
                    c.isMethodPublic(methodSignatureResolved)) {
                    retVal = cfJAVA_OBJECT;
                    //third run-time exception
                    if (retVal.isMethodAbstract(methodSignatureResolved)) {
                        throw new MethodAbstractException(methodSignatureResolved.toString());
                    }
                }
            }

            //step 4
            if (retVal == null) {
                final Set<Signature> nonabstractMaxSpecMethods = 
                    maximallySpecificSuperinterfaceMethods(methodSignatureResolved, true);
                if (nonabstractMaxSpecMethods.size() == 0) {
                    //sixth run-time exception
                    throw new MethodAbstractException(methodSignatureResolved.toString());
                } else if (nonabstractMaxSpecMethods.size() == 1) {
                    retVal = getClassFile(nonabstractMaxSpecMethods.iterator().next().getClassName());
                } else { //nonabstractMaxSpecMethods.size() > 1
                    //fifth run-time exception
                    throw new IncompatibleClassFileException(methodSignatureResolved.toString());
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
     * @param methodSignatureResolved the signature of the resolved method 
     *        which must be looked up.
     * @return the {@link ClassFile} of the class which contains the method 
     *         implementation of {@code methodSignatureResolved}. 
     *         Trivially, this is the {@link ClassFile} of
     *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}.
     * @throws BadClassFileException when the classfile 
     *         for {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
     */
    public ClassFile lookupMethodImplStatic(Signature methodSignatureResolved) 
    throws BadClassFileException {
        final ClassFile retVal = getClassFile(methodSignatureResolved.getClassName());
        return retVal;
    }

    /**
     * Performs method implementation lookup according to the semantics of the 
     * INVOKEVIRTUAL bytecode.
     *   
     * @param receiverClassName the name of the class of the 
     *        method invocation's receiver.
     * @param methodSignatureResolved the signature of the resolved method 
     *        which must be looked up.
     * @return the {@link ClassFile} of the class which contains the method 
     *         implementation of {@code methodSignatureResolved}; In the case
     *         {@code methodSignatureResolved} is signature polymorphic returns
     *         the classfile for {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}.
     * @throws BadClassFileException when the classfile 
     *         for {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
     * @throws MethodNotFoundException if no declaration of {@code methodSignatureResolved} is found in 
     *         {@code methodSignatureResolved.}{@link Signature#getClassName() getClassName()}. 
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     */
    public ClassFile lookupMethodImplVirtual(String receiverClassName, Signature methodSignatureResolved) 
    throws BadClassFileException, MethodNotFoundException, MethodAbstractException, IncompatibleClassFileException {
        final ClassFile cfMethod = getClassFile(methodSignatureResolved.getClassName());
        if (cfMethod.isMethodSignaturePolymorphic(methodSignatureResolved)) {
            return cfMethod;
        } else {
            ClassFile retVal = null;
            
            //step 1 and 2
            for (ClassFile f : superclasses(receiverClassName)) {
                if (f instanceof ClassFileBad) {
                    throw ((ClassFileBad) f).getException();
                } else if (f.hasMethodDeclaration(methodSignatureResolved) && !f.isMethodStatic(methodSignatureResolved)) {
                    final Signature fMethodSignature = new Signature(f.getClassName(), methodSignatureResolved.getDescriptor(), methodSignatureResolved.getName());
                    if (overrides(fMethodSignature, methodSignatureResolved)) {
                        retVal = f;

                        //third run-time exception
                        if (retVal.isMethodAbstract(methodSignatureResolved)) {
                            throw new MethodAbstractException(methodSignatureResolved.toString());
                        }

                        break;
                    }
                }
            }

            //step 3
            if (retVal == null) {
                final Set<Signature> nonabstractMaxSpecMethods = 
                    maximallySpecificSuperinterfaceMethods(methodSignatureResolved, true);
                if (nonabstractMaxSpecMethods.size() == 0) {
                    //sixth run-time exception
                    throw new MethodAbstractException(methodSignatureResolved.toString());
                } else if (nonabstractMaxSpecMethods.size() == 1) {
                    retVal = getClassFile(nonabstractMaxSpecMethods.iterator().next().getClassName());
                } else { //nonabstractMaxSpecMethods.size() > 1
                    //fifth run-time exception
                    throw new IncompatibleClassFileException(methodSignatureResolved.toString());
                }
            }
            
            return retVal;
        }
    }
    
    /**
     * Checks assignment compatibility for references 
     * (see JVMS v8 4.9.2 and JLS v8 5.2).
     * 
     * @param source the name of the class of the source of the 
     *        assignment.
     * @param target the name of the class of the target of the 
     *        assignment.
     * @return {@code true} iff {@code source} is assignment
     *         compatible with {@code target}.
     * @throws BadClassFileException
     */
    public boolean isAssignmentCompatible(String source, String target) 
    throws BadClassFileException {        
        final ClassFile sourceCF = getClassFile(source);
        final ClassFile targetCF = getClassFile(target);

        if (sourceCF.isInterface()) {
            if (targetCF.isInterface()) {
                return isSubclass(source, target);
            } else if (targetCF.isArray()) {
                return false; //should not happen (verify error)
            } else {
                return target.equals(JAVA_OBJECT);
            }
        } else if (sourceCF.isArray()) {
            if (targetCF.isInterface()) {
                return (target.equals(JAVA_CLONEABLE) || target.equals(JAVA_SERIALIZABLE));
            } else if (targetCF.isArray()) {
                final String sourceComponent = Type.getArrayMemberType(source);
                final String targetComponent = Type.getArrayMemberType(target);
                if (Type.isPrimitive(sourceComponent) && Type.isPrimitive(targetComponent)) {
                    return sourceComponent.equals(targetComponent);
                } else if ((Type.isReference(sourceComponent) && Type.isReference(targetComponent)) ||
                (Type.isArray(sourceComponent) && Type.isArray(targetComponent))) {
                    return isAssignmentCompatible(className(sourceComponent), className(targetComponent));
                } else {
                    return false;
                }
            } else {
                return target.equals(JAVA_OBJECT);
            }
        } else {
            if (targetCF.isArray()) {
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
     * @param subMethod a method {@link Signature}.
     * @param supMethod a method {@link Signature}.
     * @return {@code true} iff {@code subMethod} overrides 
     *         {@code supMethod}.
     * @throws BadClassFileException when the classfile 
     *         for {@code subMethod.}{@link Signature#getClassName() getClassName()} 
     *         or {@code supMethod.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
     * @throws MethodNotFoundException if {@code subMethod} or {@code supMethod}
     *         do not exist in their respective classfiles (note that 
     *         the exception is not always raised in this case).
     */
    public boolean overrides(Signature subMethod, Signature supMethod) 
    throws BadClassFileException, MethodNotFoundException {
        //first case: same method
        if (subMethod.equals(supMethod)) {
            return true;
        }
        
        //second case: all of the following must be true
        //1- subMethod's class is a subclass of supMethod's class 
        final ClassFile cfSub = getClassFile(subMethod.getClassName());
        if (!isSubclass(cfSub.getSuperclassName(), supMethod.getClassName())) {
            return false;
        }
        
        //2- subMethod has same name and descriptor of supMethod
        if (!subMethod.getName().equals(supMethod.getName())) {
            return false;
        }
        if (!subMethod.getDescriptor().equals(supMethod.getDescriptor())) {
            return false;
        }
        
        //3- subMethod is not private
        if (cfSub.isMethodPrivate(subMethod)) {
            return false;
        }
        
        //4- one of the following is true:
        //4a- supMethod is public, or protected, or (package in the same runtime package of subMethod)
        final ClassFile cfSup = getClassFile(supMethod.getClassName());
        if (cfSup.isMethodPublic(supMethod)) {
            return true;
        }
        if (cfSup.isMethodProtected(supMethod)) {
            return true;
        }
        if (cfSup.isMethodPackage(supMethod) && cfSup.getPackageName().equals(cfSub.getPackageName())) {
            return true;
        }
        
        //4b- there is another method m such that subMethod overrides 
        //m and m overrides supMethod; we look for such m in subMethod's 
        //superclasses up to supMethods
        for (ClassFile cf : superclasses(cfSub.getSuperclassName())) {
            if (cf.getClassName().equals(supMethod.getClassName())) {
                break;
            }
            if (cf.hasMethodDeclaration(subMethod)) {
                final Signature m = new Signature(cf.getClassName(), subMethod.getDescriptor(), subMethod.getName());
                if (overrides(subMethod, m) && overrides (m, supMethod)) {
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
