package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.CASTINSTANCEOF_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Objekt;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;

/**
 * Abstract {@link Algorithm} implementing the 
 * checkcast and the instanceof bytecodes.
 * 
 * @author Pietro Braione
 *
 */
abstract class Algo_CASTINSTANCEOF extends Algorithm<
BytecodeData_1CL,
DecisionAlternative_NONE,
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    protected boolean isNull; //result of the check, for the subclasses of this algorithm
    protected boolean isSubclass; //result of the check, for the subclasses of this algorithm

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected final Supplier<BytecodeData_1CL> bytecodeData() {
        return BytecodeData_1CL::get;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            try {
                //gets the operand
                final Reference tmpValue = (Reference) this.data.operand(0);

                //checks whether the object's class is a subclass 
                //of the class name from the constant pool
                if (state.isNull(tmpValue)) {
                    this.isNull = true;
                } else {
                    this.isNull = false;
                    //performs resolution of the class name
                    final ClassFile currentClass = state.getCurrentClass();    
                    final ClassFile classSuper = state.getClassHierarchy().resolveClass(currentClass, this.data.className(), state.bypassStandardLoading());
                    
                    //gets the object's class
                    final Objekt obj = state.getObject(tmpValue);
                    final ClassFile classSub = obj.getType();
                    this.isSubclass = classSub.isSubclass(classSuper);
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
            } catch (ClassCastException | ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            }
        };
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> { 
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> CASTINSTANCEOF_OFFSET;
    }
}
