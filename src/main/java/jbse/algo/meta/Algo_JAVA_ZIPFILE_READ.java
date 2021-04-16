package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
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
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#read(long, long, long, byte[], int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_READ extends Algo_INVOKEMETA_Nonbranching {
    private Array buf; //set by cookMore
    private int ofst; //set by cookMore
    private byte[] readBytes; //set by cookMore
    private int nread; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 6;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, UndefinedResultException, FrozenStateException {
        try {
            //gets the first (long jzfile) parameter
            final Primitive _jzfile = (Primitive) this.data.operand(0);
            if (_jzfile.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzfile parameter to invocation of method java.util.zip.ZipFile.read cannot be a symbolic value.");
            }
            final long jzfile = ((Long) ((Simplex) _jzfile).getActualValue()).longValue();
            if (!state.hasZipFile(jzfile)) {
                //hotspot crashes
                throw new UndefinedResultException("Invoked method java.util.zip.ZipFile.read with a jzfile parameter not corresponding to an open zipfile.");
            }
            
            //gets the second (long jzentry) parameter
            final Primitive _jzentry = (Primitive) this.data.operand(1);
            if (_jzentry.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzentry parameter to invocation of method java.util.zip.ZipFile.read cannot be a symbolic value.");
            }
            final long jzentry = ((Long) ((Simplex) _jzentry).getActualValue()).longValue();
            //if jzentry is not open the metacircular method invocation will raise InvocationTargetException
            
            //gets the third (long pos) parameter
            final Primitive _pos = (Primitive) this.data.operand(2);
            if (_pos.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long pos parameter to invocation of method java.util.zip.ZipFile.read cannot be a symbolic value.");
            }
            final long pos = ((Long) ((Simplex) _pos).getActualValue()).longValue();
            //if pos is out of range the metacircular method invocation will raise InvocationTargetException
            
            //gets the (byte[] b) parameter
            final Reference bufReference = (Reference) this.data.operand(3);
            if (state.isNull(bufReference)) {
                //hotspot crashes
                throw new UndefinedResultException("Invoked method java.util.zip.ZipFile.read with a null buf parameter.");
            }
            this.buf = (Array) state.getObject(bufReference);
            if (!this.buf.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.util.zip.ZipFile.read has not a simple representation.");
            }
            
            //gets the (int off) parameter
            final Primitive _ofst = (Primitive) this.data.operand(4);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int off parameter to invocation of method java.util.zip.ZipFile.read cannot be a symbolic value.");
            }
            this.ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            //if ofst is out of range the metacircular method invocation will raise InvocationTargetException
            
            //gets the (int len) parameter
            final Primitive _len = (Primitive) this.data.operand(5);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.util.zip.ZipFile.read cannot be a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            //if len is out of range the metacircular method invocation will raise InvocationTargetException
                        
            //invokes metacircularly the read method
            this.readBytes = new byte[len];
            final Method method = ZipFile.class.getDeclaredMethod("read", long.class, long.class, long.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            this.nread = (int) method.invoke(null, jzfile, jzentry, pos, this.readBytes, 0, len);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, this.ctx.getCalculator(), cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | 
                 IllegalArgumentException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Calculator calc = this.ctx.getCalculator();
            state.pushOperand(calc.valInt(this.nread));
            
            try {
                for (int i = this.ofst; i < this.ofst + this.nread; ++i) {
                    this.buf.setFast(calc.valInt(i), calc.valByte(this.readBytes[i - this.ofst]));
                }
            } catch (FastArrayAccessNotAllowedException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
