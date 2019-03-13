package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.CRC32;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.util.zip.CRC32#updateBytes(int, byte[], int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CRC32_UPDATEBYTES extends Algo_INVOKEMETA_Nonbranching {
    private Simplex toPush;
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException, InvalidTypeException {
        final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first (int crc) parameter
            final Primitive _crc = (Primitive) this.data.operand(0);
            if (_crc.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long addr parameter to invocation of method java.util.zip.CRC32.updateBytes cannot be a symbolic value.");
            }
            final int crc = ((Integer) ((Simplex) _crc).getActualValue()).intValue();
            
            //gets the second (byte[] b) parameter
            final Reference bufReference = (Reference) this.data.operand(1);
            if (state.isNull(bufReference)) {
                throw new UndefinedResultException("Invoked method java.util.zip.CRC32.updateBytes with a null b parameter.");
            }
            final Array _buf = (Array) state.getObject(bufReference);
            if (!_buf.isSimple()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.util.zip.CRC32.updateBytes is not simple.");
            }
            final int bufLength = ((Integer) ((Simplex) _buf.getLength()).getActualValue()).intValue();
            final byte[] buf = new byte[bufLength];
            for (int i = 0; i < bufLength; ++i) {
                final Simplex _buf_i = (Simplex) ((Array.AccessOutcomeInValue) _buf.get(calc, calc.valInt(i)).iterator().next()).getValue(); 
                buf[i] = ((Byte) _buf_i.getActualValue()).byteValue();
            }
            
            //gets the third (int off) parameter
            final Primitive _ofst = (Primitive) this.data.operand(2);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int off parameter to invocation of method java.util.zip.CRC32.updateBytes cannot be a symbolic value.");
            }
            final int ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            //TODO what if ofst is out of range?
            
            //gets the fourth (int len) parameter
            final Primitive _len = (Primitive) this.data.operand(3);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.util.zip.CRC32.updateBytes cannot be a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            //TODO what if len is out of range?
                        
            //invokes metacircularly the updateBytes method
            final Method method = CRC32.class.getDeclaredMethod("updateBytes", int.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            final int crcNew = (int) method.invoke(null, crc, buf, ofst, len);
            this.toPush = calc.valInt(crcNew);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
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
            state.pushOperand(this.toPush);
        };
    }
}
