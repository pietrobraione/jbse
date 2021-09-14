package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.algo.Util.valueString;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.Symbolizer;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

public final class Algo_JBSE_BASE_MAKEKLASSSYMBOLIC_DO extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile classFile; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws FrozenStateException, SymbolicValueNotAllowedException, ClasspathException, InterruptException {
        try {
            //gets the first (int classLoader) parameter
            final Primitive definingClassLoaderPrimitive = (Primitive) this.data.operand(0);
            if (definingClassLoaderPrimitive.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int definingClassLoader parameter to invocation of method jbse.base.Base.makeKlassSymbolic_do cannot be a symbolic int.");
            }
            final int definingClassLoader = ((Integer) ((Simplex) definingClassLoaderPrimitive).getActualValue()).intValue();

            //gets the second (String className) parameter
            final Reference classNameRef = (Reference) this.data.operand(1);
            if (state.isNull(classNameRef)) {
                //this should never happen
                failExecution("The 'this' parameter to jbse.base.Base.makeKlassSymbolic_do method is null.");
            }
            final String className = valueString(state, classNameRef);
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The String className parameter to invocation of method jbse.base.Base.makeKlassSymbolic_do cannot be a symbolic String.");
            }
            this.classFile = state.getClassHierarchy().getClassFileClassArray(definingClassLoader, className);
            if (this.classFile == null) {
                //this should never happen
                failExecution("Invoked method jbse.base.Base.makeKlassSymbolic_do for a class that has not been initialized (no classfile).");
            }
            final Klass theKlass = state.getKlass(this.classFile);
            if (theKlass == null) {
                //this should never happen
                failExecution("Invoked method jbse.base.Base.makeKlassSymbolic_do for a class that has not been initialized (no Klass object).");
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.TF;
        };
    }

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { }; //nothing to do
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> { 
        	final Symbolizer symbolizer = new Symbolizer(this.ctx.getCalculator());
        	try {
				symbolizer.makeKlassSymbolic(state, this.classFile);
			} catch (HeapMemoryExhaustedException e) {
                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
			}
        };
    }
}
