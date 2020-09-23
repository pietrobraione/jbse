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
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getNextEntry(long, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETNEXTENTRY extends Algo_INVOKEMETA_Nonbranching {
    private long jzfile; //set by cookMore
    private int i; //set by cookMore
    private long jzentry; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws SymbolicValueNotAllowedException, IllegalArgumentException, 
    InvalidInputException, ClasspathException, InterruptException {
        final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first (long jzfile) parameter
            final Primitive _jzfile = (Primitive) this.data.operand(0);
            if (_jzfile.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzfile parameter to invocation of method java.util.zip.ZipFile.getNextEntry cannot be a symbolic value.");
            }
            this.jzfile = ((Long) ((Simplex) _jzfile).getActualValue()).longValue();
            //TODO check that jzfile is open, and throw UndefinedResultException in the negative case
            
            //gets the second (int i) parameter
            final Primitive _i = (Primitive) this.data.operand(1);
            if (_i.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The boolean addSlash parameter to invocation of method java.util.zip.ZipFile.getEntry method cannot be a symbolic value.");
            }
            this.i = ((Integer) ((Simplex) _i).getActualValue()).intValue();
            
            //invokes metacircularly the getNextEntry method
            final Method method = ZipFile.class.getDeclaredMethod("getNextEntry", long.class, int.class);
            method.setAccessible(true);
            this.jzentry = (long) method.invoke(null, state.getZipFileJz(this.jzfile), this.i);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                final long _toPush;
                if (this.jzentry == 0L) {
                    //not found
                    _toPush = 0L;
                } else if (state.hasZipFileEntryJzInverse(this.jzentry)) {
                    _toPush = state.getZipFileEntryJzInverse(this.jzentry);
                } else {
                    //gets the name of the entry
                    final Method method = ZipFile.class.getDeclaredMethod("getEntryBytes", long.class, int.class);
                    method.setAccessible(true);
                    final byte[] bytesName = (byte[]) method.invoke(null, this.jzentry, 0);
                    state.addZipFileEntry(this.jzentry, this.jzfile, bytesName);
                    _toPush = this.jzentry;
                }
                final Simplex toPush = this.ctx.getCalculator().valLong(_toPush);
                state.pushOperand(toPush);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
                     IllegalArgumentException | InvocationTargetException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
