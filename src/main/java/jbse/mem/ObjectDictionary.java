package jbse.mem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceConcrete;

final class ObjectDictionary implements Cloneable {
    /**
     * Class used as key for the method handles cache.
     * 
     * @author Pietro Braione
     */
    private static final class MHKey {
    	private final int refKind; 
    	private final ClassFile container;
    	private final List<ClassFile> descriptorResolved;
    	private final String name;
    	
    	private final int hashCode;
    	
    	private MHKey(int refKind, ClassFile container, ClassFile[] descriptorResolved, String name) throws InvalidInputException {
    		if (container == null || descriptorResolved == null || name == null) {
    			throw new InvalidInputException("Tried to create a method handle key with null container class, or descriptor, or name of the field/method.");
    		}
    		this.refKind = refKind;
    		this.container = container;
    		this.descriptorResolved = Collections.unmodifiableList(Arrays.asList(descriptorResolved));
    		this.name = name;
			final int prime = 31;
			int result = 1;
			result = prime * result + this.refKind;
			result = prime * result + this.container.hashCode();
			result = prime * result + this.descriptorResolved.hashCode();
			result = prime * result + this.name.hashCode();
			this.hashCode = result;
    	}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final MHKey other = (MHKey) obj;
			if (this.refKind != other.refKind) {
				return false;
			}
			if (!this.container.equals(other.container)) {
				return false;
			}
			if (!this.descriptorResolved.equals(other.descriptorResolved)) {
				return false;
			}
			if (!this.name.equals(other.name)) { 
				return false;
			}
			return true;
		}
    }
    
    /** 
     * Maps {@link String}s to {@link ReferenceConcrete}s to {@link Instance}s
     * of class {@code java.lang.String} having same text (string literals). 
     */
    private HashMap<String, ReferenceConcrete> stringLiterals = new HashMap<>();

    /** 
     * Maps {@link ClassFile}s to {@link ReferenceConcrete}s to 
     * {@link Instance_JAVA_CLASS}es for the same class. This
     * holds only for nonprimitive types. 
     */
    private HashMap<ClassFile, ReferenceConcrete> classesNonprimitive = new HashMap<>();

    /** 
     * Maps the names of primitive types to The {@link ReferenceConcrete}s to 
     * {@link Instance_JAVA_CLASS}es for the same primitive type. 
     */
    private HashMap<String, ReferenceConcrete> classesPrimitive = new HashMap<>();
    
    /** 
     * Maps classloader identifiers (the position in the list) to 
     * {@link ReferenceConcrete}s to {@link Instance_JAVA_CLASSLOADER}s 
     * for the same identifier. 
     */
    private ArrayList<ReferenceConcrete> classLoaders = new ArrayList<>();
    
    /** 
     * Maps a method descriptor, represented as lists of {@link ClassFile}s,
     * to {@link ReferenceConcrete}s to {@link Instance}s of {@code java.lang.invoke.MethodType}s
     * for the same descriptor. 
     */
    private HashMap<List<ClassFile>, ReferenceConcrete> methodTypes = new HashMap<>();
    
    /** 
     * Maps a key identifying a method handle (a tuple of the kind of reference, 
     * the container class of the method, the method descriptor and the method name) 
     * to {@link ReferenceConcrete}s to {@link Instance}s of {@code java.lang.invoke.MethodHandle}s
     * for the key. 
     */
    private HashMap<MHKey, ReferenceConcrete> methodHandles = new HashMap<>();
    
    void putStringLiteral(String stringLiteral, ReferenceConcrete referenceStringLiteral) {
    	this.stringLiterals.put(stringLiteral, referenceStringLiteral);
    }
    
    boolean hasStringLiteral(String stringLiteral) {
    	return this.stringLiterals.containsKey(stringLiteral);
    }
    
    ReferenceConcrete getStringLiteral(String stringLiteral) {
    	return this.stringLiterals.get(stringLiteral);
    }
    
    void putClassNonprimitive(ClassFile classFile, ReferenceConcrete referenceClass) {
    	this.classesNonprimitive.put(classFile, referenceClass);
    }
    
    boolean hasClassNonprimitive(ClassFile classFile) {
    	return this.classesNonprimitive.containsKey(classFile);
    }
    
    ReferenceConcrete getClassNonprimitive(ClassFile classFile) {
    	return this.classesNonprimitive.get(classFile);
    }
    
    void putClassPrimitive(String typeName, ReferenceConcrete referenceClass) {
    	this.classesPrimitive.put(typeName, referenceClass);
    }
    
    boolean hasClassPrimitive(String typeName) {
    	return this.classesPrimitive.containsKey(typeName);
    }
    
    ReferenceConcrete getClassPrimitive(String typeName) {
    	return this.classesPrimitive.get(typeName);
    }
    
    void addClassLoader(ReferenceConcrete referenceClassLoader) {
    	this.classLoaders.add(referenceClassLoader);
    }
    
    ReferenceConcrete getClassLoader(int classLoader) {
    	return this.classLoaders.get(classLoader);
    }
    
    int maxClassLoaders() {
    	return this.classLoaders.size();
    }
    
    void putMethodType(ClassFile[] descriptor, ReferenceConcrete referenceMethodType) {
    	this.methodTypes.put(Collections.unmodifiableList(Arrays.asList(descriptor)), referenceMethodType);
    }
    
    boolean hasMethodType(ClassFile... descriptor) {
    	return this.methodTypes.containsKey(Arrays.asList(descriptor));
    }
    
    ReferenceConcrete getMethodType(ClassFile... descriptor) {
    	return this.methodTypes.get(Arrays.asList(descriptor));
    }
    
    void putMethodHandle(int refKind, ClassFile container, ClassFile[] descriptorResolved, String name, ReferenceConcrete referenceMethodHandle) 
    throws InvalidInputException {
    	this.methodHandles.put(new MHKey(refKind, container, descriptorResolved, name), referenceMethodHandle);
    }
    
    boolean hasMethodHandle(int refKind, ClassFile container, ClassFile[] descriptorResolved, String name) 
    throws InvalidInputException {
    	return this.methodHandles.containsKey(new MHKey(refKind, container, descriptorResolved, name));
    }
    
    ReferenceConcrete getMethodHandle(int refKind, ClassFile container, ClassFile[] descriptorResolved, String name) 
    throws InvalidInputException {
    	return this.methodHandles.get(new MHKey(refKind, container, descriptorResolved, name));
    }
    
    Collection<ReferenceConcrete> getReferences() {
    	final HashSet<ReferenceConcrete> retVal = new HashSet<>();
    	retVal.addAll(this.stringLiterals.values());
    	retVal.addAll(this.classesNonprimitive.values());
    	retVal.addAll(this.classesPrimitive.values());
    	retVal.addAll(this.classLoaders);
    	retVal.addAll(this.methodTypes.values());
    	retVal.addAll(this.methodHandles.values());
    	return retVal;
    }
    
    @Override
    protected ObjectDictionary clone() {
        final ObjectDictionary o;
        try {
            o = (ObjectDictionary) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        o.stringLiterals = new HashMap<>(o.stringLiterals);
        o.classesNonprimitive = new HashMap<>(o.classesNonprimitive);
        o.classesPrimitive = new HashMap<>(o.classesPrimitive);
        o.classLoaders = new ArrayList<>(o.classLoaders);
        o.methodTypes = new HashMap<>(o.methodTypes);
        o.methodHandles = new HashMap<>(o.methodHandles);
        
        return o;
    }
}
