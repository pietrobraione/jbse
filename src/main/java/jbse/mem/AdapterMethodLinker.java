package jbse.mem;

import static jbse.bc.Opcodes.OP_INVOKEDYNAMIC;

import java.util.HashMap;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.ReferenceConcrete;

/**
 * Class that stores the adapter method and 
 * appendices linked to signature polymorphic
 * methods or call sites.
 * 
 * @author Pietro Braione
 *
 */
final class AdapterMethodLinker implements Cloneable {
    /**
     * Class used as key for the call sites link maps
     * .
     * @author Pietro Braione
     *
     */
    private static final class CSKey {
    	final ClassFile containerClass;
    	final String descriptor;
    	final String name;
    	final int programCounter;
    	
    	final int hashCode;
    	
    	CSKey(ClassFile containerClass, String descriptor, String name, int programCounter) throws InvalidInputException {
    		if (containerClass == null || descriptor == null || name == null || programCounter < 0) {
    			throw new InvalidInputException("Tried to create a call site key with null containerClass, descriptor or name, or with negative program counter.");
    		}
    		final Signature methodSignature = new Signature(containerClass.getClassName(), descriptor, name);
    		if (!containerClass.hasMethodImplementation(methodSignature)) {
    			throw new InvalidInputException("Tried to create a call site key for nonexisting method.");
    		}
    		final byte[] methodCode;
			try {
				methodCode = containerClass.getMethodCodeBySignature(methodSignature);
			} catch (MethodNotFoundException | MethodCodeNotFoundException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
    		if (programCounter >= methodCode.length) {
    			throw new InvalidInputException("Tried to create a call site key for a programCounter exceeding the code length.");
    		}
    		if (methodCode[programCounter] != OP_INVOKEDYNAMIC) {
    			throw new InvalidInputException("Tried to create a call site key for a programCounter not pointing to a dynamic call site.");
    		}
    		this.containerClass = containerClass;
    		this.descriptor = descriptor;
    		this.name = name;
    		this.programCounter = programCounter;
			final int prime = 31;
			int result = 1;
			result = prime * result + this.containerClass.hashCode();
			result = prime * result + this.descriptor.hashCode();
			result = prime * result + this.name.hashCode();
			result = prime * result + this.programCounter;
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
			final CSKey other = (CSKey) obj;
			if (!this.containerClass.equals(other.containerClass)) {
				return false;
			}
			if (!this.descriptor.equals(other.descriptor)) {
				return false;
			}
			if (!this.name.equals(other.name)) {
				return false;
			}
			if (this.programCounter != other.programCounter) {
				return false;
			}
			return true;
		}
    }

    /** 
     * Links signature polymorphic methods that have nonintrinsic
     * (type checking) semantics to their adapter methods, indicated
     * as {@link ReferenceConcrete}s to their respective {@code java.lang.invoke.MemberName}s.
     */
    private HashMap<Signature, ReferenceConcrete> methodAdapters = new HashMap<>();
    
    /** 
     * Links signature polymorphic methods that have nonintrinsic
     * (type checking) semantics to their invocation appendices, indicated
     * as {@link ReferenceConcrete}s to {@code Object[]}s.
     */
    private HashMap<Signature, ReferenceConcrete> methodAppendices = new HashMap<>();
    
    /** 
     * Links dynamic call sites to their adapter methods, indicated
     * as {@link ReferenceConcrete}s to their respective {@code java.lang.invoke.MemberName}s.
     */
    private HashMap<CSKey, ReferenceConcrete> callSiteAdapters = new HashMap<>();
    
    /** 
     * Links dynamic call sites to their invocation appendices, indicated
     * as {@link ReferenceConcrete}s to {@code Object[]}s.
     */
    private HashMap<CSKey, ReferenceConcrete> callSiteAppendices = new HashMap<>();
    
    
    /**
     * Checks whether a  a signature polymorphic nonintrinsic 
     * method is linked to an adapter/appendix. 
     * 
     * @param signature a {@link Signature}.
     * @return {@code true} iff {@code signature} is the
     *         signature of a method that has been previously
     *         linked to an adapter method.
     */
    boolean isMethodLinked(Signature signature) {
        return this.methodAdapters.containsKey(signature);
    }

    /**
     * Links a signature polymorphic nonintrinsic method
     * to an adapter method, represented as a reference to
     * a {@link java.lang.invoke.MemberName}, and an appendix.
     * 
     * @param signature a {@link Signature}. It should be 
     *        the signature of a signature polymorphic
     *        nonintrinsic method, but this is not checked.
     * @param adapter a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.invoke.MemberName},
     *        but this is not checked.
     * @param appendix a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.Object[]},
     *        but this is not checked.
     * @throws InvalidInputException if any of the 
     *         parameters is {@code null}.
     */
    void linkMethod(Signature signature, ReferenceConcrete adapter, ReferenceConcrete appendix) 
    throws InvalidInputException {
        if (signature == null || adapter == null || appendix == null) {
            throw new InvalidInputException("Tried to link a method with null signature, or adapter, or appendix");
        }
        this.methodAdapters.put(signature, adapter);
        this.methodAppendices.put(signature, appendix);
    }
    
    /**
     * Returns the adapter method for a linked signature 
     * polymorphic nonintrinsic method.
     * 
     * @param signature a {@link Signature}.
     * @return a {@link ReferenceConcrete} to a {@code java.lang.invoke.MemberName}
     *         set with a previous call to {@link #linkMethod(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     */
    ReferenceConcrete getMethodAdapter(Signature signature) {
        return this.methodAdapters.get(signature);
    }
    
    /**
     * Returns the appendix for a linked signature 
     * polymorphic nonintrinsic method.
     * 
     * @param signature a {@link Signature}.
     * @return a {@link ReferenceConcrete} to an {@code Object[]}
     *         set with a previous call to {@link #linkMethod(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     */
    ReferenceConcrete getMethodAppendix(Signature signature) {
        return this.methodAppendices.get(signature);
    }
        
    /**
     * Checks whether a dynamic call site is linked to an 
     * adapter/appendix. 
     * 
     * @param containerClass the {@link ClassFile} of the 
     *        dynamic call site method.
     * @param descriptor a {@link String}, the descriptor 
     *        of the dynamic call site method.
     * @param name a {@link String}, the name 
     *        of the dynamic call site method.
     * @param programCounter an {@code int}, the displacement
     *        in the method's bytecode of the dynamic call site.
     * @return {@code true} iff the dynamic call site has been 
     *         previously linked to an adapter method.
     * @throws InvalidInputException if any of the parameters
     *         is {@code null}, or if the parameters do not 
     *         indicate a method's dynamic call site.
     */
    boolean isCallSiteLinked(ClassFile containerClass, String descriptor, String name, int programCounter) 
    throws InvalidInputException {
        return this.callSiteAdapters.containsKey(new CSKey(containerClass, descriptor, name, programCounter));
    }

    /**
     * Links a dynamic call site to an adapter method, represented 
     * as a reference to a {@link java.lang.invoke.MemberName}, 
     * and an appendix.
     * 
     * @param containerClass the {@link ClassFile} of the 
     *        dynamic call site method.
     * @param descriptor a {@link String}, the descriptor 
     *        of the dynamic call site method.
     * @param name a {@link String}, the name 
     *        of the dynamic call site method.
     * @param programCounter an {@code int}, the displacement
     *        in the method's bytecode of the dynamic call site.
     * @param adapter a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.invoke.MemberName},
     *        but this is not checked.
     * @param appendix a {@link ReferenceConcrete}. It should
     *        refer an {@link Instance} of a {@link java.lang.Object[]},
     *        but this is not checked.
     * @throws InvalidInputException if any of the 
     *         parameters is {@code null}, or if the {@code containerClass}, 
     *         {@code descriptor}, {@code name}, {@code programCounter} parameters 
     *         do not indicate a method's dynamic call site
     */
    void linkCallSite(ClassFile containerClass, String descriptor, String name, int programCounter, ReferenceConcrete adapter, ReferenceConcrete appendix) 
    throws InvalidInputException {
        this.callSiteAdapters.put(new CSKey(containerClass, descriptor, name, programCounter), adapter);
        this.callSiteAppendices.put(new CSKey(containerClass, descriptor, name, programCounter), appendix);
    }
    
    /**
     * Returns the adapter method for a dynamic call site.
     * 
     * @param containerClass the {@link ClassFile} of the 
     *        dynamic call site method.
     * @param descriptor a {@link String}, the descriptor 
     *        of the dynamic call site method.
     * @param name a {@link String}, the name 
     *        of the dynamic call site method.
     * @param programCounter an {@code int}, the displacement
     *        in the method's bytecode of the dynamic call site.
     * @return a {@link ReferenceConcrete} to a {@code java.lang.invoke.MemberName}
     *         set with a previous call to {@link #linkMethod(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     * @throws InvalidInputException if any of the parameters
     *         is {@code null}, or if the parameters do not 
     *         indicate a method's dynamic call site.
     */
    ReferenceConcrete getCallSiteAdapter(ClassFile containerClass, String descriptor, String name, int programCounter) 
    throws InvalidInputException {
        return this.callSiteAdapters.get(new CSKey(containerClass, descriptor, name, programCounter));
    }
    
    /**
     * Returns the appendix for a linked signature 
     * polymorphic nonintrinsic method.
     * 
     * @param containerClass the {@link ClassFile} of the 
     *        dynamic call site method.
     * @param descriptor a {@link String}, the descriptor 
     *        of the dynamic call site method.
     * @param name a {@link String}, the name 
     *        of the dynamic call site method.
     * @param programCounter an {@code int}, the displacement
     *        in the method's bytecode of the dynamic call site.
     * @return a {@link ReferenceConcrete} to an {@code Object[]}
     *         set with a previous call to {@link #linkMethod(Signature, ReferenceConcrete, ReferenceConcrete) link}, 
     *         or {@code null} if {@code signature} was not previously linked.
     * @throws InvalidInputException if any of the parameters
     *         is {@code null}, or if the parameters do not 
     *         indicate a method's dynamic call site.
     */
    ReferenceConcrete getCallSiteAppendix(ClassFile containerClass, String descriptor, String name, int programCounter) 
    throws InvalidInputException {
        return this.callSiteAppendices.get(new CSKey(containerClass, descriptor, name, programCounter));
    }
    
    @Override
    protected AdapterMethodLinker clone() {
    	final AdapterMethodLinker o;
    	try {
    		o = (AdapterMethodLinker) super.clone();
    	} catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        o.methodAdapters = new HashMap<>(o.methodAdapters);
        o.methodAppendices = new HashMap<>(o.methodAppendices);
        o.callSiteAdapters = new HashMap<>(o.callSiteAdapters);
        o.callSiteAppendices = new HashMap<>(o.callSiteAppendices);
    	return o;
    }
}
