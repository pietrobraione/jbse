package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.nio.file.Path;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * Meta-level implementation of {@link java.lang.Package#getSystemPackage0(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_PACKAGE_GETSYSTEMPACKAGE0 extends Algo_INVOKEMETA_Nonbranching {
    private ReferenceConcrete refPathNameOfPackage; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first (String name) parameter
            final Reference nameReference = (Reference) this.data.operand(0);
            if (state.isNull(nameReference)) {
                throw new UndefinedResultException("The String name parameter to invocation of method java.lang.Package.getSystemPackage0 was null.");
            }
            final String name = valueString(state, nameReference);
            if (name == null) {
                throw new SymbolicValueNotAllowedException("The String name parameter to invocation of method java.lang.Package.getSystemPackage0 cannot be a symbolic String.");
            }
            final Path packagePath = state.getClassHierarchy().getSystemPackageLoadedFrom(name);
            if (packagePath == null) {
                this.refPathNameOfPackage = Null.getInstance();
            } else {
            	final String packagePathName = packagePath.toAbsolutePath().toString();
                state.ensureStringLiteral(calc, packagePathName);
                this.refPathNameOfPackage = state.referenceToStringLiteral(packagePathName);
            }
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.refPathNameOfPackage);
        };
    }
}
