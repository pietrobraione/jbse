package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT_OVERRIDE;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.bc.Signatures.JAVA_THROWABLE_BACKTRACE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaredFields0(boolean)}, 
 * {@link java.lang.Class#getDeclaredMethods0(boolean)} and 
 * {@link java.lang.Class#getDeclaredConstructors0(boolean)}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_JAVA_CLASS_GETDECLAREDX0 extends Algo_INVOKEMETA_Nonbranching {
	private final String methodName;              //set by constructor
	private final Signature signatureClazz;       //set by constructor
	private final Signature signatureSlot;        //set by constructor
	private final Signature signatureName;        //set by constructor
	private final Signature signatureModifiers;   //set by constructor
	private final Signature signatureSignature;   //set by constructor
	private final Signature signatureAnnotations; //set by constructor
    protected ClassFile thisClass;                //set by cookMore
    
    protected abstract Signature[] getDeclared();
    
    protected abstract boolean isPublic(Signature signature);
    
    protected abstract ReferenceConcrete createArray(State state, Calculator calc, int numDeclaredSignatures) throws ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException;
    
    protected abstract ReferenceConcrete createInstance(State state, Calculator calc) throws ClassFileNotFoundException, ClassFileIllFormedException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    RenameUnsupportedException, HeapMemoryExhaustedException, InvalidInputException;
    
    protected abstract int getModifiers(Signature signature);
    
    protected abstract String getGenericSignatureType(Signature signature);
    
    protected abstract byte[] getAnnotationsRaw(Signature signature);
    
    protected abstract void setRemainingFields(State state, Calculator calc, Signature signature, Instance object)
    throws FrozenStateException, InvalidInputException, InvalidTypeException, ClasspathException, 
    ThreadStackEmptyException, InterruptException;
    
    public Algo_JAVA_CLASS_GETDECLAREDX0(String methodName, Signature signatureClazz, Signature signatureSlot, 
                                         Signature signatureName, Signature signatureModifiers, 
                                         Signature signatureSignature, Signature signatureAnnotations) {
    	this.methodName = methodName;
    	this.signatureClazz = signatureClazz;
    	this.signatureSlot = signatureSlot;
    	this.signatureName = signatureName;
    	this.signatureModifiers = signatureModifiers;
    	this.signatureSignature = signatureSignature;
    	this.signatureAnnotations = signatureAnnotations;
    }

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected final void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
        try {           
            //gets the classfile represented by the 'this' parameter
            final Reference thisClassRef = (Reference) this.data.operand(0);
            if (state.isNull(thisClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class." + this.methodName + " method is null.");
            }
            final Instance_JAVA_CLASS thisClassObject = (Instance_JAVA_CLASS) state.getObject(thisClassRef);
            this.thisClass = thisClassObject.representedClass();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
        
        //TODO check that operands are concrete and kill path if they are not        
        //TODO resolve all field types, parameter/exception types of constructors, parameter/return/exception types of all methods!!!
    }
    
    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final ClassHierarchy hier = state.getClassHierarchy();
            final Calculator calc = this.ctx.getCalculator();
            
            //gets the signatures of the fields/methods/constructors to emit; 
            //the position of the signature in signatures indicates its slot
            final boolean onlyPublic = ((Simplex) this.data.operand(1)).surelyTrue();
            final List<Signature> signatures;
            final boolean skipBacktrace; //useful only for fields
            try {
                final boolean[] _skipBacktrace = new boolean[1]; //boxed boolean so the closure can access it
                signatures = Arrays.stream(getDeclared())
                .map(sig -> {
                	if (onlyPublic && !isPublic(sig)) {
                		return null;
                	} else if (JAVA_THROWABLE.equals(this.thisClass.getClassName()) && JAVA_THROWABLE_BACKTRACE.equals(sig)) {
                		//we need to exclude the backtrace field of java.lang.Throwable
                		_skipBacktrace[0] = true;
                		return null;
                	} else {
                		return sig;
                	}
                })
                .collect(Collectors.toList());
                skipBacktrace = _skipBacktrace[0]; //unboxes
            } catch (RuntimeException e) {
                if (e.getCause() instanceof FieldNotFoundException ||
                e.getCause() instanceof MethodNotFoundException) {
                    //this should never happen
                    failExecution((Exception) e.getCause());
                }
                throw e;
            }

            final int numDeclaredSignatures = signatures.stream()
            .map(s -> (s == null ? 0 : 1))
            .reduce(0, (a, b) -> a + b);
            
            //builds the array to return
            ReferenceConcrete result = null; //to keep the compiler happy
            try {
                result = createArray(state, calc, numDeclaredSignatures);
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
            WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
            	throw new ClasspathException(e);
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            } catch (HeapMemoryExhaustedException e) {
            	throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            	exitFromAlgorithm();
            }

            //constructs the reflective objects and fills the array
            final Reference thisClassRef = (Reference) this.data.operand(0);
            final Array resultArray = (Array) state.getObject(result);
            int index = 0;
            int slot = 0;
            for (Signature signature : signatures) {
                if (signature != null && !(skipBacktrace && JAVA_THROWABLE_BACKTRACE.equals(signature))) {
                    //creates an instance of the reflective object and 
                    //puts it in the return array
                    ReferenceConcrete referenceObject = null; //to keep the compiler happy
                    try {
                        referenceObject = createInstance(state, calc);
                        resultArray.setFast(calc.valInt(index), referenceObject);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                             WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
                        throw new ClasspathException(e);
                    } catch (RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
                        //this should never happen
                        failExecution(e);
                    }

                    //from here initializes the reflective object
                    final Instance object = (Instance) state.getObject(referenceObject);

                    //sets clazz
                    object.setFieldValue(this.signatureClazz, thisClassRef);

                    //sets slot
                    object.setFieldValue(this.signatureSlot, calc.valInt(slot));

                    //sets name
                    if (this.signatureName != null) {
	                    try {
	                        state.ensureStringLiteral(calc, signature.getName());
	                    } catch (HeapMemoryExhaustedException e) {
	                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
	                        exitFromAlgorithm();
	                    }
	                    final ReferenceConcrete refSigName = state.referenceToStringLiteral(signature.getName());
	                    object.setFieldValue(this.signatureName, refSigName);
                    }

                    //sets modifiers
                    object.setFieldValue(this.signatureModifiers, calc.valInt(getModifiers(signature)));

                    //sets signature
                    try {
                        final String sigType = getGenericSignatureType(signature);
                        final ReferenceConcrete refSigType;
                        if (sigType == null) {
                            refSigType = Null.getInstance();
                        } else {
                            state.ensureStringLiteral(calc, sigType);
                            refSigType = state.referenceToStringLiteral(sigType);
                        }
                        object.setFieldValue(this.signatureSignature, refSigType);
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    }

                    //sets override
                    object.setFieldValue(JAVA_ACCESSIBLEOBJECT_OVERRIDE, calc.valBoolean(false));

                    //sets annotations
                    try {
                        final byte[] annotations = getAnnotationsRaw(signature);
                        final ClassFile cf_arrayOfBYTE = hier.loadCreateClass("" + ARRAYOF + BYTE);
                        final ReferenceConcrete referenceAnnotations = (annotations.length == 0 ? Null.getInstance() : state.createArray(calc, null, calc.valInt(annotations.length), cf_arrayOfBYTE));
                        object.setFieldValue(this.signatureAnnotations, referenceAnnotations);
                        
                        //populates annotations
                        if (annotations.length > 0) {
                        	final Array annotationsArray = (Array) state.getObject(referenceAnnotations);
                        	for (int i = 0; i < annotations.length; ++i) {
                        		annotationsArray.setFast(calc.valInt(i), calc.valByte(annotations[i]));
                        	}
                        } 
                    } catch (HeapMemoryExhaustedException e) {
                        throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                        exitFromAlgorithm();
                    } catch (ClassFileNotFoundException | 
                             ClassFileIllFormedException | BadClassFileVersionException | 
                             RenameUnsupportedException | WrongClassNameException | 
                             IncompatibleClassFileException | ClassFileNotAccessibleException | 
                             FastArrayAccessNotAllowedException e) {
                        //this should never happen
                        failExecution(e);
                    }
                    
                    //sets the remaining fields
                    setRemainingFields(state, calc, signature, object);

                    ++index;
                }
                ++slot;
            }


            //returns the array
            state.pushOperand(result);
        };
    }
}
