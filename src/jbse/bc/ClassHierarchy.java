package jbse.bc;

import static jbse.bc.Util.JAVA_OBJECT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jbse.Type;
import jbse.exc.algo.JavaReifyException;
import jbse.exc.bc.ClassFileNotAccessibleException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.FieldNotAccessibleException;
import jbse.exc.bc.FieldNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.MethodAbstractException;
import jbse.exc.bc.MethodNotAccessibleException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.common.UnexpectedInternalException;

/**
 * Class handling a hierarchy of Java classes as specified 
 * by some {@link ClassFileInterface}. Wraps a {@link ClassFileInterface} 
 * and offers its methods.
 *  
 * @author Pietro Braione
 *
 */
public class ClassHierarchy {
	private final Classpath cp;
	private final ClassFileInterface cfi;
	private final Map<String, Set<String>> expansionBackdoor;

	/**
	 * Constructor.
	 * 
	 * @param cp a {@link Classpath}.
	 * @param fClass the {@link Class} of some subclass of {@link ClassFileFactory}.
	 *        The class must have an accessible constructor with two parameters, the first a 
	 *        {@link ClassFileInterface}, the second a {@link Classpath}.
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
		this.cfi = new ClassFileInterface(cp, fClass);
		this.expansionBackdoor = expansionBackdoor;
	}
	
	/**
	 * Returns the {@link Classpath} of this hierarchy.
	 * 
	 * @return a {@link Classpath} (clone of that used
	 *         to create this hierarchy).
	 */
	public Classpath getClassPath() {
		return this.cp.clone();
	}

    /**
     * Given a class name returns the correspondent {@link ClassFile}.
     * To avoid name clashes it does not manage primitive classes.
     * 
     * @param className the searched class.
     * @return the classFile structure of the correspondent class.
     * @throws ClassFileNotFoundException when the class file cannot be 
     * found in the classpath.
     */
    public ClassFile getClassFile(String className) 
    throws ClassFileNotFoundException {
    	return cfi.getClassFile(className);
    }
    
    /**
     * Given the name of a primitive type returns the correspondent 
     * {@link ClassFile}.
     * 
     * @param typeName the name of a primitive type (see the class {@link Type}).
     * @return the classFile structure of the correspondent class.
     */
    public ClassFile getClassFilePrimitive(String typeName) {
    	return cfi.getClassFilePrimitive(typeName);
    }

	/**
	 * Lists the concrete subclasses of a class. <br />
	 * <em>Note:</em> An exact implementation of this method, 
	 * searching the classpath for all the concrete subclasses 
	 * of an arbitrary class, would be too demanding. Thus this
	 * implementation returns {@code className}, if it is not 
	 * an interface or an abstract class, and all the classes 
	 * associated to {@code className} in the 
	 * {@code expansionBackdoor} provided at construction time..
	 * 
	 * @param className a {@link String}, the name of a class.
	 * @return A {@link Set}{@code <}{@link String}{@code >} of class
	 *         names.
	 * @throws ClassFileNotFoundException if {@code className} does
	 *         not correspond to a valid class in the classpath.
	 */
	public Set<String> getAllConcreteSubclasses(String className) 
	throws ClassFileNotFoundException {
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
		return new ListSuperclasses(startClassName);
	}

	/**
	 * Produces all the superclasses of a given class.
	 * 
	 * @param startClassName the name of the class whose superclasses 
	 *                       are returned.
	 * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
	 *         all the superinterfaces of {@code startClassName} (included).
	 */
	public Iterable<ClassFile> superinterfaces(String startClassName) {
		return new ListSuperinterfaces(startClassName);
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
				final String subMemberClass = Type.getClassFromReferenceType(subMember);
				final String supMemberClass = Type.getClassFromReferenceType(supMember);
				return this.isSubclass(subMemberClass, supMemberClass);
			} else if (Type.isArray(subMember) && Type.isArray(supMember)) {
				return this.isSubclass(subMember, supMember);
			} else {
				return false;
			}
		} else {
			for (ClassFile f : superclasses(sub)) { 
				final String s = f.getClassName();
				if (s.equals(sup)) {
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
	private class ListSuperclasses implements Iterable<ClassFile> {
		private String startClassName;

		/**
		 * Constructor.
		 * 
		 * @param startClassName 
		 *        The name of the class from where the iteration is started. 
		 *        Note that the iterator firstly visits this class: thus, 
		 *        the first call to {@code hasNext()} will return true 
		 *        iff {@code startClassName != null} and such class 
		 *        exists in this class hierarchy.
		 */
		public ListSuperclasses(String startClassName) {
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
			private String nextClassName;

			public MyIterator(String startClassName) {
				this.nextClassName = null;

				final ClassFile cf;
				try {
					cf = ClassHierarchy.this.cfi.getClassFile(startClassName);
				} catch (ClassFileNotFoundException e) {
					return;
				} catch (UnexpectedInternalException e) {
					throw new RuntimeException("Unexpected error while creating class hierarchy iterator", e);
				}
				this.nextClassName = cf.getClassName();
			}

			public boolean hasNext() {
				return (this.nextClassName != null);
			}

			public ClassFile next() {
				//ensures the method precondition
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				//gets the next class' class file (should always be successful)
				final ClassFile retval;
				try {
					retval = ClassHierarchy.this.cfi.getClassFile(this.nextClassName);
				} catch (ClassFileNotFoundException | UnexpectedInternalException e) {
					throw new RuntimeException("Unexpected error while iterating class hierarchy", e);
				} 

				//prepares for the next invocation
				this.nextClassName = retval.getSuperClassName();

				//returns the result
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
	private class ListSuperinterfaces implements Iterable<ClassFile> {
		private String startClassName;

		/**
		 * Constructor.
		 * 
		 * @param startClassName 
		 *        The name of the class from where the iteration is started. 
		 *        Note that the iterator firstly visits this class iff it is 
		 *        an interface: thus, the first call to {@code hasNext()} 
		 *        will return true iff {@code startClassName != null} and 
		 *        {@code startClassName} exists in the environment 
		 *        defined by {@link Classpath}{@code .this.env}, and it is an 
		 *        interface.
		 */
		public ListSuperinterfaces(String startClassName) {
			this.startClassName = startClassName;
		}

		public Iterator<ClassFile> iterator() {
			return new MyIterator(this.startClassName);
		}        

		/**
		 * {@link Iterator}{@code <}{@link ClassFile}{@code >} for
		 * upwardly scanning an interface hierarchy. For 
		 * the sake of simplicity it scans in breadth-first 
		 * order. It does not visit a same interface twice. 
		 * 
		 * @author Pietro Braione
		 */
		private class MyIterator implements Iterator<ClassFile> {
			private LinkedList<String> nextClassNames;
			private HashSet<String> visited;

			public MyIterator(String startClassName) {
				this.nextClassNames = new LinkedList<String>();
				this.visited = new HashSet<String>();

				final ClassFile cf;
				try {
					cf = ClassHierarchy.this.cfi.getClassFile(startClassName);
				} catch (ClassFileNotFoundException e) {
					return;
				} catch (UnexpectedInternalException e) {
					throw new RuntimeException("Unexpected error while iterating class hierarchy", e);
				}
				if (cf.isInterface()) {
					this.nextClassNames.add(cf.getClassName());
				} else {
					this.nextClassNames.addAll(cf.getSuperInterfaceNames());
				}
			}

			public boolean hasNext() {
				return !(this.nextClassNames.isEmpty());
			}

			public ClassFile next() {
				//ensures the method precondition
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				//gets the next unvisited interface names
				String nextClassName = this.nextClassNames.getFirst(); 
				this.visited.add(nextClassName);

				//gets its class file (should always be successful)
				final ClassFile retval;
				try {
					retval = ClassHierarchy.this.cfi.getClassFile(nextClassName);
				} catch (ClassFileNotFoundException | UnexpectedInternalException e) {
					throw new RuntimeException("Unexpected error while iterating class hierarchy", e);
				} 

				//prepares for the next invocation:
				//1 - adds all the superinterfaces of the visited one to the 
				//    yet-to-visit interfaces
				this.nextClassNames.addAll(retval.getSuperInterfaceNames());
				//2 - removes all the already-visited superinterfaces from 
				//    the yet-to-visit ones
				this.nextClassNames.removeAll(this.visited);

				//returns the result
				return retval;
			}

			public void remove() {
				throw new UnsupportedOperationException();
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
	 * @return a {@link String}, the signature of the declaration of the resolved field.
	 *         It is {@code resolveClass(accessor, classSignature) == 
	 *         classSignature}, the return value is kept only to keep 
	 *         consistency with {@link #resolveField} and {@link #resolveMethod}.
	 * @throws ClassFileNotFoundException
	 * @throws ClassFileNotAccessibleException 
	 */
	public String resolveClass(String accessor, String classSignature) 
	throws ClassFileNotFoundException, ClassFileNotAccessibleException {
		//TODO implement complete class creation as in JVM Specification, sec. 5.3
		cfi.getClassFile(classSignature);
		
		//if a declaration has found, then it checks accessibility and, in case, 
		//raises IllegalAccessError; otherwise, returns the resolved field signature
		if (isClassAccessible(accessor, classSignature)) {
			//everything went ok
			return classSignature;
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
	 * @throws JavaReifyException when method resolution does not succeed; the exception 
	 *         stores the name of the Java exception that the virtual machine must raise.
	 * @throws ClassFileNotFoundException if {@code fieldSignature}'s class does not
	 *         exist in the classpath.
	 */
	public Signature resolveField(String accessor, Signature fieldSignature) 
	throws ClassFileNotFoundException, FieldNotAccessibleException, FieldNotFoundException {
		//searches a declaration for the field in the field's
		//signature class (lookup starts from there)
		Signature fieldSignatureResolved = null;
		final ClassFile classFile = cfi.getClassFile(fieldSignature.getClassName());
		if (classFile.hasFieldDeclaration(fieldSignature)) {
			fieldSignatureResolved = fieldSignature;
		}

		//if nothing has been found, searches in the superinterfaces
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
	       		if (cf.hasFieldDeclaration(fieldSignature)) {
	       			fieldSignatureResolved = 
	       				new Signature(cf.getClassName(), fieldSignature.getDescriptor(), fieldSignature.getName());
	       			break;
	       		}
	       	}
		}
		
        //if still nothing has been found, raises a NoSuchFieldError
		if (fieldSignatureResolved == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}

		//if a declaration has been found, then it checks accessibility and, in case, 
		//raises IllegalAccessError; otherwise, returns the resolved field signature
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
	 * @throws ClassFileNotFoundException if 
	 * @throws IncompatibleClassFileException 
	 * @throws MethodAbstractException 
	 * @throws MethodNotFoundException 
	 * @throws MethodNotAccessibleException 
	 */
	public Signature resolveMethod(String accessor, Signature methodSignature, boolean isInterface) 
	throws ClassFileNotFoundException, IncompatibleClassFileException, MethodAbstractException, 
	MethodNotFoundException, MethodNotAccessibleException {
		//gets the classfile for class mentioned in the method's *invocation*
		//TODO implement class resolution and loading!
		final ClassFile classFile = cfi.getClassFile(methodSignature.getClassName());

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
        		if (cf.hasMethodDeclaration(methodSignature)) {
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
        		if (cf.hasMethodDeclaration(methodSignature)) {
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
			final ClassFile cf = cfi.getClassFile(JAVA_OBJECT);
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
	 * @throws ClassFileNotFoundException iff either {@code accessor}
	 *         or {@code accessed} are not found in this class hierarchy.
	 */
	private boolean isClassAccessible(String accessor, String accessed) 
	throws ClassFileNotFoundException {
		//TODO this implementation is incomplete: some kinds of nested (member) classes may have all the visibility accessors. Also, the treatment of arrays is wrong.
		final ClassFile cfAccessed = cfi.getClassFile(accessed);
		if (cfAccessed.isPublic()) {
			return true;
		} else { //cfAccessed.isPackage()
			final ClassFile cfAccessor = cfi.getClassFile(accessor);
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
	 * @throws ClassFileNotFoundException iff either {@code accessor}
	 *         or the class of {@code accessed} are not found in the class hierarchy.
	 * @throws FieldNotFoundException iff the {@code accessed} field is not 
	 *         found in its classfile. 
	 */
	private boolean isFieldAccessible(String accessor, Signature accessed) 
	throws ClassFileNotFoundException, FieldNotFoundException {
		final ClassFile cfAccessed = cfi.getClassFile(accessed.getClassName());
		final ClassFile cfAccessor = cfi.getClassFile(accessor);
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
			return accessed.getClassName().equals(accessor) || 
					cfAccessor.isInner(cfAccessed);
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
	 * @throws ClassFileNotFoundException iff either {@code accessor}
	 *         or the class of {@code accessed} are not found by {@code cfi}.
	 * @throws MethodNotFoundException iff the {@code accessed} method is not 
	 *         found in its classfile. 
	 */
	private boolean isMethodAccessible(String accessor, Signature accessed) 
	throws ClassFileNotFoundException, MethodNotFoundException {
		final ClassFile cfAccessed = cfi.getClassFile(accessed.getClassName());
		final ClassFile cfAccessor = cfi.getClassFile(accessor);
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
			return (accessed.getClassName().equals(accessor) ||
					cfAccessor.isInner(cfAccessed));
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
	 * @throws ClassFileNotFoundException when the class with name 
	 *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}
	 *         does not exist.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when a method with signature 
	 *         {@code methodSignature} exists but it is not static.
	 */
	public ClassFile lookupMethodImplStatic(Signature methodSignature) 
	throws ClassFileNotFoundException, MethodNotFoundException, 
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
	 * @param receiverClass the {@link ClassFile} of the 
	 *        method invocation's receiver.
	 * @param methodSignature the {@link Signature} of the resolved method 
	 *        which must be looked up.
	 * @return the {@link ClassFile} of the superclass of
	 *         {@code methodSignature.getClassName()} which 
	 *         contains the method implementation.
	 * @throws MethodNotFoundException when method lookup fails.
	 * @throws IncompatibleClassFileException when method lookup succeeds, but the
	 *         found method is static.
	 */
	public ClassFile lookupMethodImplVirtualInterface(ClassFile receiverClass, Signature methodSignature) 
	throws MethodNotFoundException, IncompatibleClassFileException {
		for (ClassFile f : superclasses(receiverClass.getClassName())) {
			if (f.hasMethodImplementation(methodSignature)) {
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
	 * @param currentClass the {@link ClassFile} for the class of the invoker.
	 * @param methodSignature the signature of the resolved method 
	 *        which must be looked up.
	 * @return the {@link ClassFile} of the class which 
	 *         contains the method implementation.
	 * @throws ClassFileNotFoundException when the class with name 
	 *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}
	 *         does not exist.
	 * @throws MethodNotFoundException when method lookup fails, or it is a <init>
	 *         method but it is not declared in 
	 *         {@code methodSignature.}{@link Signature#getClassName() getClassName()}.
	 * @throws IncompatibleClassFileException when method lookup succeeds, but the
	 *         found method is static.
	 */
	public ClassFile lookupMethodImplSpecial(ClassFile currentClass, Signature methodSignature) 
	throws ClassFileNotFoundException, MethodNotFoundException, 
	IncompatibleClassFileException {
		ClassFile retVal = null;
		boolean useNonVirtual = true;

		//determines whether should look for the implementation in 
		//the current class superclasses (virtual semantics) or in 
		//the method's signature class (nonvirtual semantics) and 
		//at the same time calculates the superclass for virtual semantics
		if (!methodSignature.getName().equals("<init>") && 
			currentClass.isSuperInvoke()) {
			boolean skippedFirst = false;
			
			for (ClassFile f : superclasses(currentClass.getClassName())) {
				if (skippedFirst) { 
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
}
