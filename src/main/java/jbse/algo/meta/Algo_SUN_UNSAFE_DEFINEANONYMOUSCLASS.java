package jbse.algo.meta;

import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.CLASS_FORMAT_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_VALUE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_VALUE;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_VALUE;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_VALUE;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.ConstantPoolObject;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolUtf8;
import jbse.bc.ConstantPoolValue;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
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
    private ClassFile cfAnonymousDummy; //set by cookMore
    private ConstantPoolValue[] cpPatches;  //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }
    
    @Override
    protected void cookMore(State state) 
    throws SymbolicValueNotAllowedException, InterruptException, ClasspathException,
    InvalidInputException, InvalidTypeException {
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
            
            //creates the dummy anonymous classfile
            this.cfAnonymousDummy = state.getClassHierarchy().createClassFileAnonymousDummy(this.bytecode);
            
            //gets the constant pool patches
            final Reference refCpPatches = (Reference) this.data.operand(3);
            this.cpPatches = patches(state, refCpPatches);
            
            //let's push the right assumption
            state.assumeClassNotInitialized(this.cfAnonymousDummy);
        } catch (ClassFileIllFormedException e) {
            throwNew(state, calc, CLASS_FORMAT_ERROR); //this is the behaviour of Hotspot
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private ConstantPoolValue[] patches(State state, Reference refCpPatches) 
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
        final ConstantPoolValue[] retVal = new ConstantPoolValue[((Integer) ((Simplex) arrayCpPatches.getLength()).getActualValue()).intValue()];
        try {
            for (int i = 0; i < retVal.length; ++i) {
                final Simplex _i = calc.valInt(i);
                final Reference _r = (Reference) ((AccessOutcomeInValue) arrayCpPatches.getFast(calc, _i)).getValue();
                if (state.isNull(_r)) {
                    retVal[i] = null;
                } else {
                    final Instance _o = (Instance) state.getObject(_r);
                    final ConstantPoolValue cpEntry = this.cfAnonymousDummy.getValueFromConstantPool(i);
                    if (cpEntry instanceof ConstantPoolPrimitive) {
                        if (JAVA_DOUBLE.equals(_o.getType().getClassName()) && cpEntry.getValue() instanceof Double) {
                            final double value =  ((Double) ((Simplex) (_o.getFieldValue(JAVA_DOUBLE_VALUE))).getActualValue()).doubleValue();
                            retVal[i] = new ConstantPoolPrimitive(value);
                        } else if (JAVA_FLOAT.equals(_o.getType().getClassName()) && cpEntry.getValue() instanceof Float) {
                            final float value =  ((Float) ((Simplex) (_o.getFieldValue(JAVA_FLOAT_VALUE))).getActualValue()).floatValue();
                            retVal[i] = new ConstantPoolPrimitive(value);
                        } else if (JAVA_INTEGER.equals(_o.getType().getClassName()) && cpEntry.getValue() instanceof Integer) {
                            final int value =  ((Integer) ((Simplex) (_o.getFieldValue(JAVA_INTEGER_VALUE))).getActualValue()).intValue();
                            retVal[i] = new ConstantPoolPrimitive(value);
                        } else if (JAVA_LONG.equals(_o.getType().getClassName()) && cpEntry.getValue() instanceof Long) {
                            final long value =  ((Long) ((Simplex) (_o.getFieldValue(JAVA_LONG_VALUE))).getActualValue()).longValue();
                            retVal[i] = new ConstantPoolPrimitive(value);
                        } else {
                            throw new ClassFileIllFormedException("patches");
                        }
                    } else if (cpEntry instanceof ConstantPoolUtf8) {
                        final String value = valueString(state, _o);
                        retVal[i] = new ConstantPoolUtf8(value);
                    } else if (cpEntry instanceof ConstantPoolClass) {
                        final Instance_JAVA_CLASS _oJavaClass = (Instance_JAVA_CLASS) _o;
                        final String value = _oJavaClass.representedClass().getClassName();
                        retVal[i] = new ConstantPoolClass(value);
                    } else if (cpEntry instanceof ConstantPoolString) {
                        retVal[i] = new ConstantPoolObject(_r);
                    } else {
                        throw new UnexpectedInternalException("Unexpected constant pool value.");
                    }
                }
            }
        } catch (ClassCastException | InvalidIndexException e) {
            throw new ClassFileIllFormedException("patches");
        }
        return retVal;
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final ClassFile cfAnonymous = state.getClassHierarchy().addClassFileAnonymous(this.cfAnonymousDummy, this.hostClass, this.cpPatches);
                state.ensureInstance_JAVA_CLASS(this.ctx.getCalculator(), cfAnonymous);
                state.pushOperand(state.referenceToInstance_JAVA_CLASS(cfAnonymous));
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
        };
    }
}
