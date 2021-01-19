package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Null;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Resolved;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

/**
 * Abstract {@link Algorithm} for completing the semantics of the 
 * *load*, *load and get* bytecodes ([a/d/f/i/l]load[_0/1/2/3], 
 * [a/d/f/i/l]load, get[field/static]). 
 * It decides over the value loaded to the operand stack in the cases 
 * (aload[_0/1/2/3], aload, get[field/static]) 
 * it is an uninitialized symbolic reference ("lazy initialization").
 * 
 * @author Pietro Braione
 */
abstract class Algo_XLOAD_GETX<D extends BytecodeData> extends Algo_XYLOAD_GETX<
D, 
DecisionAlternative_XLOAD_GETX,
StrategyDecide<DecisionAlternative_XLOAD_GETX>, 
StrategyRefine_XLOAD_GETX,
StrategyUpdate<DecisionAlternative_XLOAD_GETX>> {

    //set by subclasses
    protected Value valToLoad;

    @Override
    protected final Class<DecisionAlternative_XLOAD_GETX> classDecisionAlternative() {
        return DecisionAlternative_XLOAD_GETX.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_XLOAD_GETX> decider() {
        return (state, result) -> {
            Outcome o = null; //to keep the compiler happy
            try {
                o = this.ctx.decisionProcedure.resolve_XLOAD_GETX(this.valToLoad, result);
            //TODO the next catch blocks should disappear, see comments on removing exceptions in jbse.dec.DecisionProcedureAlgorithms.doResolveReference
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
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
			}
            this.someReferencePartiallyResolved = o.partialReferenceResolution();
            if (this.someReferencePartiallyResolved) {
                try {
                    final ReferenceSymbolic refToLoad = (ReferenceSymbolic) this.valToLoad;
                    this.partiallyResolvedReferences.add(refToLoad);
                } catch (ClassCastException e) {
                    //this should never happen
                    failExecution(e);
                }
            }
            return o;
        };
    }

    @Override
    protected final StrategyRefine_XLOAD_GETX refiner() {
        return new StrategyRefine_XLOAD_GETX() {
            @Override
            public void refineRefExpands(State s, DecisionAlternative_XLOAD_GETX_Expands alt) 
            throws ContradictionException, InvalidTypeException, InterruptException, 
            SymbolicValueNotAllowedException, ClasspathException, InvalidInputException {
                Algo_XLOAD_GETX.this.refineRefExpands(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineRefAliases(State s, DecisionAlternative_XLOAD_GETX_Aliases alt)
            throws ContradictionException, InvalidInputException, ClasspathException, InterruptException {
                Algo_XLOAD_GETX.this.refineRefAliases(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineRefNull(State s, DecisionAlternative_XLOAD_GETX_Null alt)
            throws ContradictionException, InvalidInputException {
                Algo_XLOAD_GETX.this.refineRefNull(s, alt); //implemented in Algo_XYLOAD_GETX
            }

            @Override
            public void refineResolved(State s, DecisionAlternative_XLOAD_GETX_Resolved alt) {
                //nothing to do, the value is concrete or has been already refined
            }
        };
    }

    @Override
    protected final Value possiblyMaterialize(State s, Value val) {
        //nothing to do, val does not come from an array
        return val;
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_XLOAD_GETX> updater() {
        return this::update;
    }

    @Override
    protected final void onInvalidInputException(State state, InvalidInputException e) throws ClasspathException {
        //bad value to load (triggered by call to resolve_XLOAD_GETX done by decider())
        throwVerifyError(state, this.ctx.getCalculator());
    }

}
