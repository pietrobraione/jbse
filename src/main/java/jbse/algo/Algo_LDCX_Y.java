package jbse.algo;

import static jbse.algo.Util.ensureInstance_JAVA_METHODHANDLE;
import static jbse.algo.Util.ensureInstance_JAVA_METHODTYPE;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.LDC_OFFSET;
import static jbse.bc.Offsets.LDC_W_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.isCat_1;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolMethodHandle;
import jbse.bc.ConstantPoolMethodType;
import jbse.bc.ConstantPoolObject;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Value;

/**
 * {@link Algorithm} for all the "push constant from 
 * constant pool" bytecodes ldc*_* (ldc, ldc_w, ldc2_w).
 * 
 * @author Pietro Braione
 *
 */
final class Algo_LDCX_Y extends Algorithm<
BytecodeData_1ZUX,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    private final boolean wide; //set by constructor
    private final boolean cat1; //set by constructor

    /**
     * Constructor.
     * 
     * @param wide {@code true} for ldc*_w, {@code false} for ldc.
     * @param cat1 {@code true} for ldc_w, {@code false} for ldc2_w.
     */
    public Algo_LDCX_Y(boolean wide, boolean cat1) {
        this.wide = wide;
        this.cat1 = cat1;
    }

    private Value val; //set by cooker


    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected Supplier<BytecodeData_1ZUX> bytecodeData() {
        return () -> BytecodeData_1ZUX.withWide(this.wide).get();
    }

    @Override
    protected BytecodeCooker bytecodeCooker() {
        return (state) -> {
            try {
            	final Calculator calc = this.ctx.getCalculator();
                final ClassFile currentClass = state.getCurrentClass();
                final int index = (this.wide ? this.data.immediateUnsignedWord() : this.data.immediateUnsignedByte());
                final ConstantPoolValue cpv = currentClass.getValueFromConstantPool(index);
                if (cpv instanceof ConstantPoolPrimitive) {
                    this.val = calc.val_(cpv.getValue());
                    if (this.cat1 != isCat_1(val.getType())) {
                        throwVerifyError(state, this.ctx.getCalculator());
                        exitFromAlgorithm();
                    }
                } else if (cpv instanceof ConstantPoolString) {
                    final String stringLit = ((ConstantPoolString) cpv).getValue();
                    state.ensureStringLiteral(calc, stringLit);
                    this.val = state.referenceToStringLiteral(stringLit);
                } else if (cpv instanceof ConstantPoolClass) {
                    final String classSignature = ((ConstantPoolClass) cpv).getValue();
                    final ClassFile resolvedClass = state.getClassHierarchy().resolveClass(currentClass, classSignature, state.bypassStandardLoading());
                    state.ensureInstance_JAVA_CLASS(calc, resolvedClass);
                    this.val = state.referenceToInstance_JAVA_CLASS(resolvedClass);
                } else if (cpv instanceof ConstantPoolMethodType) {
                	final String descriptor = ((ConstantPoolMethodType) cpv).getValue();
                	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(currentClass, descriptor, state.bypassStandardLoading());
                	ensureInstance_JAVA_METHODTYPE(state, calc, descriptorResolved);
                	this.val = state.referenceToInstance_JAVA_METHODTYPE(descriptorResolved);
                } else if (cpv instanceof ConstantPoolMethodHandle) {
                	final ConstantPoolMethodHandle cpvMH = (ConstantPoolMethodHandle) cpv;
                	final int refKind = cpvMH.getKind();
                	final Signature sig = cpvMH.getValue();
                	final ClassFile callee = state.getClassHierarchy().resolveClass(currentClass, sig.getClassName(), state.bypassStandardLoading());
                	final String descriptor = sig.getDescriptor();
                	final ClassFile[] descriptorResolved = state.getClassHierarchy().resolveMethodType(currentClass, descriptor, state.bypassStandardLoading());
                	ensureInstance_JAVA_METHODHANDLE(state, calc, currentClass, refKind, callee, descriptorResolved, sig.getName());
                	this.val = state.referenceToInstance_JAVA_METHODHANDLE(refKind, callee, descriptorResolved, sig.getName());
                } else if (cpv instanceof ConstantPoolObject) {
                    this.val = ((ConstantPoolObject) cpv).getValue();
                } else {
                    //this should never happen
                    failExecution("Unexpected value from the constant pool.");
                }
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException e) {
                throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException e) {
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileIllFormedException e) {
                //TODO throw LinkageError instead
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (InvalidIndexException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        };
    }

    @Override
    protected Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.val);            
        };
    }

    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> (this.wide ? LDC_W_OFFSET : LDC_OFFSET);
    }
}
