package jbse.algo;

import static jbse.algo.Util.continueWith;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.lookupClassfileMethodImpl;
import static jbse.algo.Util.throwNew;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_SUCH_METHOD_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.exc.BaseUnsupportedException;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Abstract algorithm for the invoke* bytecodes
 * (invoke[interface/special/static/virtual]).
 * 
 * @author Pietro Braione
 *
 */
//TODO this class was born when (JVMS 2nd edition) the four invoke bytecodes were not much different, and sharing the implementation made sense; now it should be split in four subclasses. 
public abstract class Algo_INVOKEX_Abstract<D extends BytecodeData> extends Algorithm<D,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    protected final boolean isInterface; //set by the constructor
    protected final boolean isSpecial; //set by the constructor
    protected final boolean isStatic; //set by the constructor

    public Algo_INVOKEX_Abstract(boolean isInterface, boolean isSpecial, boolean isStatic) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
    }

    protected Signature methodSignatureResolved; //set by cooking methods (resolveMethod)
    protected ClassFile classFileMethodImpl; //set by cooking methods (findImpl / findOverridingImpl)
    protected Signature methodSignatureImpl; //set by cooking methods
    protected boolean isNative; //set by cooking methods
    protected boolean isSignaturePolymorphic; //set by cooking methods
    
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> {
            final String[] paramsDescriptors = splitParametersDescriptors(this.data.signature().getDescriptor());
            return (this.isStatic ? paramsDescriptors.length : paramsDescriptors.length + 1);
        };
    }

    protected final void resolveMethod(State state) 
    throws BadClassFileException, IncompatibleClassFileException,  
    MethodNotFoundException, MethodNotAccessibleException {
        try {
            //performs method resolution
            final ClassHierarchy hier = state.getClassHierarchy();
            final String currentClassName = state.getCurrentMethodSignature().getClassName();
            this.methodSignatureResolved = hier.resolveMethod(currentClassName, this.data.signature(), this.isInterface);
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    protected final void check(State state) throws InterruptException, CannotManageStateException {
        try {
            final ClassHierarchy hier = state.getClassHierarchy();
            final ClassFile classFileResolved = hier.getClassFile(this.methodSignatureResolved.getClassName());
            
            if (this.isInterface) {
                //checks for invokeinterface
                
                //TODO the resolved method must not be an instance initialization method, or the class or interface initialization method: what should we do if it is???
                
                //the first linking exception pertains to method resolution
                
                //second linking exception
                if (classFileResolved.isMethodStatic(this.methodSignatureResolved) || classFileResolved.isMethodPrivate(this.methodSignatureResolved)) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
                
                //first run-time exception
                final Reference receiver = state.peekReceiverArg(this.methodSignatureResolved);
                if (state.isNull(receiver)) {
                    throwNew(state, NULL_POINTER_EXCEPTION);
                    exitFromAlgorithm();
                }
                
                //second run-time exception
                if (!hier.isSubclass(state.getObject(receiver).getType(), this.methodSignatureResolved.getClassName())) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
                
                //the third, fourth, fifth, sixth and seventh run-time exception pertain to lookup of method implementation
            } else if (this.isSpecial) {
                //checks for invokespecial
                
                //the first linking exception pertains to method resolution
                
                //second linking exception
                if ("<init>".equals(this.methodSignatureResolved.getName()) &&
                    !this.methodSignatureResolved.getClassName().equals(this.data.signature().getClassName())) {
                    throwNew(state, NO_SUCH_METHOD_ERROR);
                    exitFromAlgorithm();
                }
                
                //third linking exception
                if (classFileResolved.isMethodStatic(this.methodSignatureResolved)) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
                
                //first run-time exception
                final Reference receiver = state.peekReceiverArg(this.methodSignatureResolved);
                if (state.isNull(receiver)) {
                    throwNew(state, NULL_POINTER_EXCEPTION);
                    exitFromAlgorithm();
                }
                
                //second run-time exception (identical to second run-time exception of invokevirtual case)
                final String currentClass = state.getCurrentMethodSignature().getClassName();
                if (classFileResolved.isMethodProtected(this.methodSignatureResolved) &&
                    hier.isSubclass(currentClass, this.methodSignatureResolved.getClassName())) {
                    final ClassFile classFileCurrent = hier.getClassFile(currentClass);
                    final boolean samePackage = classFileCurrent.getPackageName().equals(classFileResolved.getPackageName());
                    final String receiverClass = state.getObject(receiver).getType();                    
                    if (!samePackage && !hier.isSubclass(receiverClass, currentClass)) {
                        throwNew(state, ILLEGAL_ACCESS_ERROR);
                        exitFromAlgorithm();
                    }
                }
                
                //the third, fifth and sixth run-time exceptions pertain to lookup of method implementation                
                //the fourth run-time exception is not raised by JBSE (natives)
            } else if (this.isStatic) {
                //checks for invokestatic
                
                //TODO the resolved method must not be an instance initialization method, or the class or interface initialization method: what should we do if it is???
                
                //the first linking exception pertains to method resolution
                
                //second linking exception
                if (!classFileResolved.isMethodStatic(this.methodSignatureResolved)) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
                
                //the first run-time exception pertains to class/interface initialization
                //the second run-time exception is not raised by JBSE (natives)
            } else {            
                //checks for invokevirtual
                
                //TODO the resolved method must not be an instance initialization method, or the class or interface initialization method: what should we do if it is???
                
                //the first linking exception pertains to method resolution
                
                //second linking exception
                if (classFileResolved.isMethodStatic(this.methodSignatureResolved)) {
                    throwNew(state, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
                }
                
                //the third linking exception pertains to method type resolution
                
                //first run-time exception
                final Reference receiver = state.peekReceiverArg(this.methodSignatureResolved);
                if (state.isNull(receiver)) {
                    throwNew(state, NULL_POINTER_EXCEPTION);
                    exitFromAlgorithm();
                }
                
                //second run-time exception (identical to second run-time exception of invokespecial case)
                final String currentClass = state.getCurrentMethodSignature().getClassName();
                if (classFileResolved.isMethodProtected(this.methodSignatureResolved) &&
                    hier.isSubclass(currentClass, this.methodSignatureResolved.getClassName())) {
                    final ClassFile classFileCurrent = hier.getClassFile(currentClass);
                    final boolean samePackage = classFileCurrent.getPackageName().equals(classFileResolved.getPackageName());
                    final String receiverClass = state.getObject(receiver).getType();                    
                    if (!samePackage && !hier.isSubclass(receiverClass, currentClass)) {
                        throwNew(state, ILLEGAL_ACCESS_ERROR);
                        exitFromAlgorithm();
                    }
                }
                
                //the third, fourth, fifth and sixth run-time exception pertain to lookup of method implementation
                
                //the seventh and eighth run-time exceptions pertain to the code after method type resolution
            }            
        } catch (BadClassFileException | MethodNotFoundException | ThreadStackEmptyException e) {
            //this should never happen after resolution
            failExecution(e);
        }
    }

    protected final void findImpl(State state) 
    throws BadClassFileException, IncompatibleClassFileException, 
    MethodNotAccessibleException, MethodAbstractException, InterruptException {
        try {
            final boolean isVirtualInterface = !this.isStatic && !this.isSpecial;
            final String receiverClassName;
            if (isVirtualInterface) {
                final Reference thisRef = state.peekReceiverArg(this.methodSignatureResolved);
                receiverClassName = state.getObject(thisRef).getType();
            } else {
                receiverClassName = null;
            }
            this.classFileMethodImpl = lookupClassfileMethodImpl(state, 
                                                                 this.methodSignatureResolved, 
                                                                 this.isInterface, 
                                                                 this.isSpecial, 
                                                                 this.isStatic,
                                                                 receiverClassName);
            this.methodSignatureImpl = new Signature(this.classFileMethodImpl.getClassName(), 
                                                     this.methodSignatureResolved.getDescriptor(), 
                                                     this.methodSignatureResolved.getName());
            this.isNative = this.classFileMethodImpl.isMethodNative(this.methodSignatureImpl);
            this.isSignaturePolymorphic = this.classFileMethodImpl.isMethodSignaturePolymorphic(this.methodSignatureImpl);
        } catch (MethodNotFoundException e) {
            this.classFileMethodImpl = null;
            this.methodSignatureImpl = null;
            this.isNative = false;
            this.isSignaturePolymorphic = false;
        } catch (ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }
    }

    protected final void findOverridingImpl(State state)
    throws BaseUnsupportedException, MetaUnsupportedException, InterruptException {
        if (this.methodSignatureImpl == null || this.isSignaturePolymorphic) {
            return; //no implementation to override, or method is signature polymorphic (cannot be overridden!)
        }
        if (this.ctx.isMethodBaseLevelOverridden(this.methodSignatureImpl)) {
            final Signature methodSignatureOverriding = this.ctx.getBaseOverride(this.methodSignatureImpl);
            try {
                final ClassFile classFileMethodOverriding = state.getClassHierarchy().getClassFile(methodSignatureOverriding.getClassName());
                checkOverridingMethodFits(state, methodSignatureOverriding, classFileMethodOverriding);
                this.classFileMethodImpl = classFileMethodOverriding;
                this.methodSignatureImpl = methodSignatureOverriding;
                this.isNative = this.classFileMethodImpl.isMethodNative(this.methodSignatureImpl);
                this.isSignaturePolymorphic = this.classFileMethodImpl.isMethodSignaturePolymorphic(this.methodSignatureImpl);
            } catch (MethodNotFoundException e) {
                throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " does not exist (but the class seems to exist)");
            } catch (BadClassFileException e) {
                throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " does not exist because its class does not exist");
            }
        } else {
            try {
                if (this.ctx.dispatcherMeta.isMeta(state.getClassHierarchy(), this.methodSignatureImpl)) {
                    final Algo_INVOKEMETA<?, ?, ?, ?> algo = this.ctx.dispatcherMeta.select(this.methodSignatureImpl);
                    algo.setFeatures(this.isInterface, this.isSpecial, this.isStatic);
                    continueWith(algo);
                }
            } catch (BadClassFileException | MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
            }
        }
    }

    private void checkOverridingMethodFits(State state, Signature methodSignatureOverriding, ClassFile classFileMethodOverriding) 
    throws BaseUnsupportedException, BadClassFileException, MethodNotFoundException {
        final ClassFile classFileMethodResolved = state.getClassHierarchy().getClassFile(this.methodSignatureResolved.getClassName());
        if (!classFileMethodOverriding.hasMethodImplementation(methodSignatureOverriding)) {
            throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " is abstract.");
        }
        final boolean resolvedStatic = classFileMethodResolved.isMethodStatic(this.methodSignatureResolved);
        final boolean overridingStatic = classFileMethodOverriding.isMethodStatic(methodSignatureOverriding);
        if (resolvedStatic == overridingStatic) {
            if (this.methodSignatureResolved.getDescriptor().equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else if (!resolvedStatic && overridingStatic) {
            if (descriptorAsStatic(this.methodSignatureResolved).equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else { //(resolvedStatic && !overridingStatic)
            if (this.methodSignatureResolved.getDescriptor().equals(descriptorAsStatic(methodSignatureOverriding))) {
                return;
            }
        }
        throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " has signature incompatible with overridden " + this.methodSignatureImpl);
    }

    private static String descriptorAsStatic(Signature sig) {
        return "(" + REFERENCE + sig.getClassName() + TYPEEND + sig.getDescriptor().substring(1);
    }
}
