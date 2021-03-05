package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.bc.Signatures.JAVA_SYSTEM;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.StrategyUpdate;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;

/**
 * Meta-level implementation of {@link java.lang.System#setIn0(java.io.InputStream)}, 
 * {@link java.lang.System#setOut0(java.io.PrintStream)} and 
 * {@link java.lang.System#setErr0(java.io.PrintStream)}.
 * 
 * @author Pietro Braione
 */
abstract class Algo_JAVA_SYSTEM_SETX0 extends Algo_INVOKEMETA_Nonbranching {
	private final String methodName;         //set by constructor
	private final Signature streamSignature; //set by constructor
    private Klass k;                         //set by cookMore
    
    Algo_JAVA_SYSTEM_SETX0(String methodName, Signature streamSignature) {
    	this.methodName = methodName;
    	this.streamSignature = streamSignature;
    }
    
    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 1;
    }
    
    
    @Override
    protected final void cookMore(State state) throws UndefinedResultException, InvalidInputException, RenameUnsupportedException {
        try {
            final ClassFile cf_JAVA_SYSTEM = state.getClassHierarchy().loadCreateClass(JAVA_SYSTEM);
            this.k = state.getKlass(cf_JAVA_SYSTEM);
            if (this.k == null || !this.k.initializationCompleted()) {
                throw new UndefinedResultException("Invoked java.lang.System." + this.methodName + " before initialization of class java.lang.System.");
            }
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                 WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            this.k.setFieldValue(this.streamSignature, this.data.operand(0));
        };
    }
}
