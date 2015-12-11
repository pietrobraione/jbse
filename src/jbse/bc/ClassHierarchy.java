package jbse.bc;

import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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
 * by some {@link ClassFileStore}. Wraps a {@link ClassFileStore} 
 * and offers its methods.
 *  
 * @author Pietro Braione
 *
 */
public class ClassHierarchy {
	private final Classpath cp;
	private final ClassFileStore cfs;
	private final Map<String, Set<String>> expansionBackdoor;

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
     * @param className the searched class.
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
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the name of a primitive type (see the class {@link Type}).
     * @return the {@link ClassFile} of the correspondent class.
     * @throws BadClassFileException when the class file does not 
     *         exist or is ill-formed (happens when {@code typeName}
     *         is not the name of a primitive type).
     */
    public ClassFile getClassFilePrimitive(String typeName)
    throws BadClassFileException {
        final ClassFile retval = this.cfs.getClassFilePrimitive(typeName);
        if (retval instanceof ClassFileBad) {
            throw ((ClassFileBad) retval).getException();
        }
        return retval;
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
	 *                       are returned.
	 * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
	 *         all the superclasses of {@code startClassName} (included).
	 */
	public Iterable<ClassFile> superclasses(String startClassName) {
		return new IterableSuperclasses(startClassName);
	}

	/**
	 * Produces all the superinterfaces of a given class.
	 * 
	 * @param startClassName the name of the class whose superinterfaces 
	 *                       are returned.
	 * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
	 *         all the superinterfaces of {@code startClassName} (included if
	 *         it is an interface).
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
				final String subMemberClass = Type.getReferenceClassName(subMember);
				final String supMemberClass = Type.getReferenceClassName(supMember);
				return isSubclass(subMemberClass, supMemberClass);
			} else if (Type.isArray(subMember) && Type.isArray(supMember)) {
				return isSubclass(subMember, supMember);
			} else {
				return false;
			}
		} else if (!Type.isArray(sub) && !Type.isArray(sup)) {
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
		} else {
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
				this.nextClassFile = ClassHierarchy.this.cfs.getClassFile(startClassName);
			}

			public boolean hasNext() {
				return (this.nextClassFile != null);
			}

			public ClassFile next() {
				//ensures the method precondition
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				//stores the return value
				final ClassFile retval = this.nextClassFile;

				//gets the classfile of the superclass
				final String superClassName = retval.getSuperClassName();
				if (superClassName == null) {
				    //no superclass
				    this.nextClassFile = null;
				} else {
				    this.nextClassFile = ClassHierarchy.this.cfs.getClassFile(superClassName);
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
				final ClassFile cf = ClassHierarchy.this.cfs.getClassFile(startClassName);
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
				if (!this.hasNext()) {
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
                .map((s) -> ClassHierarchy.this.cfs.getClassFile(s))
                .filter((cf) -> !this.visitedClassFiles.contains(cf))
                .collect(Collectors.toList());
            }
		}
	}
	
	private static final Signature[] SIGNATURE_ARRAY = new Signature[0];
	
	/**
	 * Returns all the nonstatic fields of a class by
	 * iterating over its superclass hierarchy.
	 * 
	 * @param className a {@link String}, the name of the class.
	 * @return a {@link Signature}{@code []}.
	 */	
	public Signature[] getAllFieldsInstance(String className) {
        final ArrayList<Signature> signatures = new ArrayList<Signature>(0);
        for (ClassFile c : superclasses(className)) {
            Signature[] fields = c.getFieldsNonStatic();
            signatures.addAll(Arrays.asList(fields));
        }
        
        //flattens myVector into mySgnArray
        final Signature[] retVal = signatures.toArray(SIGNATURE_ARRAY);
        return retVal;
	}
	
	/**
	 * Performs class (including array class) and interface resolution 
	 * (see JVM Specification, sec. 5.4.3.1).
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
        //TODO implement complete class creation as in JVM Specification, sec. 5.3
	    
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
	 * Performs field resolution (see JVM Specification, sec. 5.4.3.2).
	 * 
	 * @param accessor a {@link String}, the signature of the accessor's class.
	 * @param fieldSignature the {@link Signature} of the field to be resolved.
	 * @return the {@link Signature} of the declaration of the resolved field.
	 * @throws BadClassFileException if the classfile for {@code fieldSignature}'s 
	 *         class and its superclasses does not exist in the classpath.
	 * @throws FieldNotAccessibleException if the resolved field cannot 
	 *         be accessed by {@code accessor}.
	 * @throws FieldNotFoundException if resolution of the field fails.
	 */
	public Signature resolveField(String accessor, Signature fieldSignature) 
	throws BadClassFileException, FieldNotAccessibleException, FieldNotFoundException {
		//searches a declaration for the field in the field's
		//signature class (lookup starts from there)
		Signature fieldSignatureResolved = null;
		final ClassFile classFile = getClassFile(fieldSignature.getClassName());
		if (classFile.hasFieldDeclaration(fieldSignature)) {
			fieldSignatureResolved = fieldSignature;
		}

		//if nothing has been found, searches in the superinterfaces
        /* TODO this procedure differs from JVM Specification 5.4.3.2 because
         * the specification requires to search in the *immediate* superinterfaces, 
         * then recursively in the superclass, immediate superinterfaces of the
         * superclass, etc. 
         */
		if (fieldSignatureResolved == null) {
			for (ClassFile cf : superinterfaces(fieldSignature.getClassName())) {
        		if (cf.hasFieldDeclaration(fieldSignature)) {
        			fieldSignatureResolved = 
        				new Signature(cf.getClassName(), fieldSignature.getDescriptor(), fieldSignature.getName());
        			break;
        		}
			}
		}
        
		//if still nothing has been found, searches in the superclasses
		if (fieldSignatureResolved == null) {
	       	for (ClassFile cf : superclasses(fieldSignature.getClassName())) {
	       	    if (cf instanceof ClassFileBad) {
	       	        throw ((ClassFileBad) cf).getException();
	       	    } else if (cf.hasFieldDeclaration(fieldSignature)) {
	       			fieldSignatureResolved = 
	       				new Signature(cf.getClassName(), fieldSignature.getDescriptor(), fieldSignature.getName());
	       			break;
	       		}
	       	}
		}
		
        //if still nothing has been found, raises an exception
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
	 * Performs both method and interface method resolution (see JVM Specification, sec. 5.4.3.3 and 5.4.3.4).
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
	 * @throws MethodAbstractException if resolution yields an abstract method.
	 * @throws MethodNotAccessibleException if the resolved method is not accessible 
	 *         by {@code accessor}.
	 */
	public Signature resolveMethod(String accessor, Signature methodSignature, boolean isInterface) 
	throws BadClassFileException, IncompatibleClassFileException, MethodAbstractException, 
	MethodNotFoundException, MethodNotAccessibleException {
		//gets the classfile for class mentioned in the method's *invocation*
		//TODO implement class resolution and loading!
		final ClassFile classFile = getClassFile(methodSignature.getClassName());

		//checks if the symbolic reference to the method class 
		//is an interface (JVM spec 5.4.3.3(1) and 5.4.3.4(1))
		if (isInterface != classFile.isInterface()) {
			throw new IncompatibleClassFileException(methodSignature.getClassName());
		}

		//attempts to find a superclass or superinterface containing 
		//a declaration for the method
		Signature methodSignatureResolved = null;
		ClassFile classFileResolve = null;

		//searches for the method declaration in the superclasses 
		//(only method resolution, JVM spec 2, 5.4.3.3(2))
        if (!isInterface) {
        	for (ClassFile cf : superclasses(methodSignature.getClassName())) {
        	    if (cf instanceof ClassFileBad) {
        	        throw ((ClassFileBad) cf).getException();
        	    } else if (cf.hasMethodDeclaration(methodSignature)) {
        			classFileResolve = cf;
        			methodSignatureResolved = 
        				new Signature(cf.getClassName(), methodSignature.getDescriptor(), methodSignature.getName());
        			break;
        		}
        	}
        }
				
        //searches in the superinterfaces
		//(JVM spec 2, 5.4.3.3(3) and 5.4.3.4(2))
		if (methodSignatureResolved == null) {
			for (ClassFile cf : superinterfaces(methodSignature.getClassName())) {
                if (cf instanceof ClassFileBad) {
                    throw ((ClassFileBad) cf).getException();
                } else if (cf.hasMethodDeclaration(methodSignature)) {
        			classFileResolve = cf;
        			methodSignatureResolved = 
        				new Signature(cf.getClassName(), methodSignature.getDescriptor(), methodSignature.getName());
        			break;
        		}
			}
		}
        
        //searches in the java.lang.Object class 
        //(only interface method resolution, JVM spec 2, 5.4.3.4(2))
		if (methodSignatureResolved == null && isInterface) {
			final ClassFile cf = getClassFile(JAVA_OBJECT);
    		if (cf.hasMethodDeclaration(methodSignature)) {
    			classFileResolve = cf;
    			methodSignatureResolved = 
    				new Signature(cf.getClassName(), methodSignature.getDescriptor(), methodSignature.getName());
    		}
		}
        
        //exits if no method has been found
		if (methodSignatureResolved == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		
		//additional checks for noninterface methods
		if (!isInterface) {
			//checks the resolved method is not abstract in the case 
			//the class where we started is not an abstract class
			try {
				if (!classFile.isAbstract() && classFileResolve.isMethodAbstract(methodSignatureResolved)) {
					throw new MethodAbstractException(methodSignature.toString());
				}
			} catch (MethodNotFoundException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
			
		//if a declaration has found, then it checks accessibility and, in case, 
		//raises IllegalAccessError; otherwise, returns the resolved method signature;
		//note that this check is not required by the JVM spec 2, but later specs do 
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
	 * Checks whether a class/interface is accessible to another class/interface
	 * according to JVM specification sec. 5.4.4.
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
	 * according to JVM specification sec. 5.4.4.
	 * 
	 * @param accessor a {@link String}, the name of a class or interface.
	 * @param accessed a {@link Signature}, the signature of a field.
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
		boolean samePackage = cfAccessed.getPackageName().equals(cfAccessor.getPackageName());
		if (cfAccessed.isFieldPublic(accessed)) {
			return true;
		} else if (cfAccessed.isFieldProtected(accessed)) {
			if (samePackage) {
				return true;
			} else {
				return isSubclass(accessor, accessed.getClassName());
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
	 * according to JVM specification sec. 5.4.4.
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
			} else {
				return isSubclass(accessor, accessed.getClassName());
			}
		} else if (cfAccessed.isMethodPackage(accessed)) {
			return samePackage;
		} else { //cfAccessed.isMethodPrivate(accessed)
			return accessed.getClassName().equals(accessor);
            //TODO there was a || cfAccessor.isInner(cfAccessed) clause but it is *wrong*!
		}
	}
	
	/**
	 * Performs method lookup according to the semantics of the 
	 * INVOKESTATIC bytecode.
	 * 	 
	 * @param methodSignature the signature of the resolved method 
	 *        which must be looked up.
	 * @return the {@link ClassFile} of the class which 
	 *         contains the method implementation.
     * @throws BadClassFileException when the class file 
     *         with name {@code methodSignature.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when a method with signature 
	 *         {@code methodSignature} exists but it is not static.
	 */
	public ClassFile lookupMethodImplStatic(Signature methodSignature) 
	throws BadClassFileException, MethodNotFoundException, 
	IncompatibleClassFileException {
		//the method must be in the class of the method signature
		final ClassFile retVal = getClassFile(methodSignature.getClassName());
		if (!retVal.isMethodStatic(methodSignature)) {
			throw new IncompatibleClassFileException(retVal.getClassName());
		}
		return retVal;
	}

	/**
	 * Performs method lookup according to the semantics of the 
	 * INVOKEVIRTUAL bytecode.
	 * 
	 * @param receiverClassName the name of the class of the 
	 *        method invocation's receiver.
	 * @param methodSignature the {@link Signature} of the resolved method 
	 *        which must be looked up.
	 * @return the {@link ClassFile} of the superclass of
	 *         {@code methodSignature.getClassName()} which 
	 *         contains the method implementation.
     * @throws BadClassFileException when the class file 
     *         with name {@code methodSignature.}{@link Signature#getClassName() getClassName()} 
     *         or that of one of its superclasses does not exist or is ill-formed.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when method lookup succeeds, but the
	 *         found method is static.
	 */
	public ClassFile lookupMethodImplVirtualInterface(String receiverClassName, Signature methodSignature) 
	throws BadClassFileException, MethodNotFoundException, IncompatibleClassFileException {
		for (ClassFile f : superclasses(receiverClassName)) {
		    if (f instanceof ClassFileBad) {
		        throw ((ClassFileBad) f).getException();
		    } else if (f.hasMethodImplementation(methodSignature)) {
				if (f.isMethodStatic(methodSignature)) {
					throw new IncompatibleClassFileException(f.getClassName());
				}
				return f;
			}
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}
	
	/**
	 * Performs method lookup according to the semantics of the 
	 * INVOKESPECIAL bytecode.
	 * 
	 * @param currentClassName the name of the class of the invoker.
	 * @param methodSignature the signature of the resolved method 
	 *        which must be looked up.
	 * @return the {@link ClassFile} of the class which 
	 *         contains the method implementation.
     * @throws BadClassFileException when the class file 
     *         with name {@code methodSignature.}{@link Signature#getClassName() getClassName()} 
     *         does not exist or is ill-formed.
	 * @throws MethodNotFoundException when method lookup fails, or it is a {@code <init>}
	 *         method but it is not declared in 
	 *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}.
	 * @throws IncompatibleClassFileException when method lookup succeeds, but the
	 *         found method is static.
	 */
	public ClassFile lookupMethodImplSpecial(String currentClassName, Signature methodSignature) 
	throws BadClassFileException, MethodNotFoundException, IncompatibleClassFileException {
		ClassFile retVal = null;
		boolean useNonVirtual = true;

		//determines whether should look for the implementation in 
		//the current class superclasses (super() virtual semantics) or in 
		//the method's signature class (nonvirtual semantics) and 
		//at the same time calculates the superclass for virtual semantics
        final ClassFile currentClass = getClassFile(currentClassName);
		if (currentClass.isSuperInvoke() &&
		    isSubclass(currentClass.getClassName(), methodSignature.getClassName()) &&
		    !methodSignature.getName().equals("<init>")) {
			boolean skippedFirst = false;
			
			for (ClassFile f : superclasses(currentClassName)) {
			    if (f instanceof ClassFileBad) {
			        throw ((ClassFileBad) f).getException();
			    } else if (skippedFirst) { 
					if (f.hasMethodImplementation(methodSignature))
						retVal = f;
					if (f.getClassName().equals(methodSignature.getClassName())) {
						useNonVirtual = false;
						break;
					}
				}
				skippedFirst = true;
			}
		}
		
		//nonvirtual case: gets the method implementation
		//from the resolved method's class
		if (useNonVirtual) {
			final ClassFile resolvedClass = getClassFile(methodSignature.getClassName());
			if (resolvedClass.hasMethodImplementation(methodSignature)) {
				retVal = resolvedClass;
			} else {
				retVal = null;
			}
		}
		
		if (retVal == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		
		if (retVal.isMethodStatic(methodSignature)) {
			throw new IncompatibleClassFileException(retVal.getClassName());
		}

		return retVal;
	}
	
	/**
	 * Checks assignment compatibility for references 
	 * (see JVM spec v2 2.6.7 and chapter 6, aaload bytecode).
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
                    return isAssignmentCompatible(sourceComponent, targetComponent);
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
}
