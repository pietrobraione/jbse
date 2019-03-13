package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.ZipFile;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#freeEntry(long, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_FREEENTRY extends Algo_INVOKEMETA_Nonbranching {
    private long jzentry; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException {
        try {
            //gets the first (long jzfile) parameter
            final Primitive _jzfile = (Primitive) this.data.operand(0);
            if (_jzfile.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzfile parameter to invocation of method java.util.zip.ZipFile.freeEntry cannot be a symbolic value.");
            }
            final long jzfile = ((Long) ((Simplex) _jzfile).getActualValue()).longValue();
            if (!state.hasZipFile(jzfile)) {
                //hotspot crashes
                throw new UndefinedResultException("Invoked method java.util.zip.ZipFile.freeEntry with a jzfile parameter not corresponding to an open zipfile.");
            }
            
            //gets the second (long jzentry) parameter
            final Primitive _jzentry = (Primitive) this.data.operand(1);
            if (_jzentry.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzentry parameter to invocation of method java.util.zip.ZipFile.freeEntry cannot be a symbolic value.");
            }
            this.jzentry = ((Long) ((Simplex) _jzentry).getActualValue()).longValue();
            //TODO what if jzentry is not open?
            
            //invokes metacircularly the freeEntry method
            final Method method = ZipFile.class.getDeclaredMethod("freeEntry", long.class, long.class);
            method.setAccessible(true);
            method.invoke(null, state.getZipFileJz(jzfile), state.getZipFileEntryJz(this.jzentry));
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, this.ctx.getCalculator(), cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.removeZipFileEntry(this.jzentry);
        };
    }
}
