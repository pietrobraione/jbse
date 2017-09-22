package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.LDC_OFFSET;
import static jbse.bc.Offsets.LDC_W_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.common.Type.isCat_1;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
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
                final String currentClassName = state.getCurrentMethodSignature().getClassName();
                ClassFile cf = null; //to keep the compiler happy
                try {
                    cf = state.getClassHierarchy().getClassFile(currentClassName);
                } catch (BadClassFileException e) {
                    //this should never happen
                    failExecution(e);
                }
                final int index = (this.wide ? this.data.immediateUnsignedWord() : this.data.immediateUnsignedByte());
                final ConstantPoolValue cpv = cf.getValueFromConstantPool(index);
                if (cpv instanceof ConstantPoolPrimitive) {
                    this.val = state.getCalculator().val_(cpv.getValue());
                    if (this.cat1 != isCat_1(val.getType())) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                } else if (cpv instanceof ConstantPoolString) {
                    final String stringLit = ((ConstantPoolString) cpv).getValue();
                    ensureStringLiteral(state, stringLit, this.ctx);
                    this.val = state.referenceToStringLiteral(stringLit);
                } else { // cpv instanceof ConstantPoolClass
                    final String classSignature = ((ConstantPoolClass) cpv).getValue();
                    ensureInstance_JAVA_CLASS(state, currentClassName, classSignature, this.ctx);
                    this.val = state.referenceToInstance_JAVA_CLASS(classSignature);
                }
            } catch (ClasspathException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR); //TODO is it right? This is when java.lang.Class is missing, what if ((ConstantPoolClass) cpv).getValue() is missing?
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (InvalidIndexException | BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
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
