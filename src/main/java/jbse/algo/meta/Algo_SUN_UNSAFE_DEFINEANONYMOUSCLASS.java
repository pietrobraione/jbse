package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.CLASS_FORMAT_ERROR;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_UNSAFE_DEFINEANONYMOUSCLASS extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile hostClass; //set by cookMore
    private byte[] bytecode; //set by cookMore
    private Object[] cpPatches;  //set by cookMore
    private ClassFile cfAnonymous; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    @Override
    protected void cookMore(State state) 
    throws SymbolicValueNotAllowedException, InterruptException, ClasspathException,
    InvalidInputException, InvalidTypeException, ThreadStackEmptyException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the name of the host class
            final Reference refClassHost = (Reference) this.data.operand(1);
            if (state.isNull(refClassHost)) {
                throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION); //this is the behaviour of Hotspot
                exitFromAlgorithm();
            }
            final Instance_JAVA_CLASS instanceClassHost = (Instance_JAVA_CLASS) state.getObject(refClassHost);
            if (instanceClassHost == null) {
                throw new UnexpectedInternalException("Unexpected symbolic unresolved reference Class hostClass parameter of sun.misc.Unsafe.defineAnonymousClass.");
            }
            this.hostClass = instanceClassHost.representedClass();
            
            //gets the bytecode
            final Reference refBytecode = (Reference) this.data.operand(2);
            if (state.isNull(refBytecode)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION); //this is the behaviour of Hotspot
                exitFromAlgorithm();
            }
            final Array arrayBytecode = (Array) state.getObject(refBytecode);
            if (!arrayBytecode.isSimple()) {
                throw new SymbolicValueNotAllowedException("The byte[] data parameter to sun.misc.Unsafe.defineAnonymousClass must be a simple array.");
            }
            this.bytecode = new byte[((Integer) ((Simplex) arrayBytecode.getLength()).getActualValue()).intValue()];
            for (int i = 0; i < this.bytecode.length; ++i) {
                final Simplex _i = calc.valInt(i);
                final Simplex _b = (Simplex) ((AccessOutcomeInValue) arrayBytecode.getFast(calc, _i)).getValue();
                this.bytecode[i] = ((Byte) _b.getActualValue()).byteValue();
            }
            
            //gets the constant pool patches
            final Reference refCpPatches = (Reference) this.data.operand(3);
            this.cpPatches = patches(state, refCpPatches);
            
            //defines the anonymous class
            this.cfAnonymous = state.getClassHierarchy().defineClassAnonymous(this.bytecode, state.bypassStandardLoading(), this.hostClass, this.cpPatches);
            state.ensureInstance_JAVA_CLASS(this.ctx.getCalculator(), this.cfAnonymous);
            state.assumeClassNotInitialized(this.cfAnonymous);
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
            exitFromAlgorithm();
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            throwNew(state, this.ctx.getCalculator(), CLASS_NOT_FOUND_EXCEPTION);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //TODO is it right?
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            throwNew(state, calc, CLASS_FORMAT_ERROR); //this is the behaviour of Hotspot
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (FastArrayAccessNotAllowedException | RenameUnsupportedException e) {
            //this should never happen
            failExecution(e);
		}
    }
    
    private Object[] patches(State state, Reference refCpPatches) 
    throws SymbolicValueNotAllowedException, InvalidInputException, InvalidTypeException, 
    FastArrayAccessNotAllowedException, ClassFileIllFormedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();
        if (state.isNull(refCpPatches)) {
            return null;
        }
        final Array arrayCpPatches = (Array) state.getObject(refCpPatches);
        if (!arrayCpPatches.isSimple()) {
            throw new SymbolicValueNotAllowedException("The Object[] cpPatches parameter to sun.misc.Unsafe.defineAnonymousClass must be a simple array.");
        }
        final Object[] retVal = new Object[((Integer) ((Simplex) arrayCpPatches.getLength()).getActualValue()).intValue()];
        try {
            for (int i = 0; i < retVal.length; ++i) {
                final Simplex _i = calc.valInt(i);
                final Reference _r = (Reference) ((AccessOutcomeInValue) arrayCpPatches.getFast(calc, _i)).getValue();
                if (state.isNull(_r)) {
                    retVal[i] = null;
                } else {
                    final HeapObjekt _o = (HeapObjekt) state.getObject(_r);
                    if (JAVA_DOUBLE.equals(_o.getType().getClassName())) {
                    	retVal[i] = (Double) ((Simplex) (_o.getFieldValue(JAVA_DOUBLE_VALUE))).getActualValue();
                    } else if (JAVA_FLOAT.equals(_o.getType().getClassName())) {
                    	retVal[i] = (Float) ((Simplex) (_o.getFieldValue(JAVA_FLOAT_VALUE))).getActualValue();
                    } else if (JAVA_INTEGER.equals(_o.getType().getClassName())) {
                    	retVal[i] = (Integer) ((Simplex) (_o.getFieldValue(JAVA_INTEGER_VALUE))).getActualValue();
                    } else if (JAVA_LONG.equals(_o.getType().getClassName())) {
                    	retVal[i] = (Long) ((Simplex) (_o.getFieldValue(JAVA_LONG_VALUE))).getActualValue();
                    } else if (JAVA_STRING.equals(_o.getType().getClassName())) {
                    	retVal[i] = valueString(state, _r);
                    } else if (JAVA_CLASS.equals(_o.getType().getClassName())) {
                        final Instance_JAVA_CLASS _oJavaClass = (Instance_JAVA_CLASS) _o;
                        retVal[i] = _oJavaClass.representedClass();
                    } else {
                        retVal[i] = _r;
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new ClassFileIllFormedException("patches");
        }
        return retVal;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(state.referenceToInstance_JAVA_CLASS(this.cfAnonymous));
        };
    }
}
