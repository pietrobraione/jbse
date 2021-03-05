package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.Inflater;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.util.zip.Inflater#setDictionary(long, byte[], int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_INFLATER_SETDICTIONARY extends Algo_INVOKEMETA_Nonbranching {
    private long addr; //set by cookMore
    private byte[] b; //set by cookMore
    private int ofst; //set by cookMore
    private int len; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException {
        final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first (long addr) parameter
            final Primitive _addr = (Primitive) this.data.operand(0);
            if (_addr.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long addr parameter to invocation of method java.util.zip.Inflater.setDictionary cannot be a symbolic value.");
            }
            this.addr = ((Long) ((Simplex) _addr).getActualValue()).longValue();
            //TODO check that addr is valid, and react appropriately in the negative case
            
            //gets the second (byte[] b) parameter
            final Reference bReference = (Reference) this.data.operand(1);
            if (state.isNull(bReference)) {
                throw new UndefinedResultException("Invoked method java.util.zip.Inflater.inflateBytes with a null b parameter.");
            }
            final Array _b = (Array) state.getObject(bReference);
            if (!_b.isSimple()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.util.zip.Inflater.setDictionary is not simple.");
            }
            final int bLength = ((Integer) ((Simplex) _b.getLength()).getActualValue()).intValue();
            this.b = new byte[bLength];
            for (int i = 0; i < bLength; ++i) {
                final Simplex _b_i = (Simplex) ((Array.AccessOutcomeInValue) _b.getFast(calc, calc.valInt(i))).getValue();
                this.b[i] = ((Byte) _b_i.getActualValue()).byteValue();
            }
            
            //gets the third (int off) parameter
            final Primitive _ofst = (Primitive) this.data.operand(2);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int off parameter to invocation of method java.util.zip.Inflater.setDictionary cannot be a symbolic value.");
            }
            this.ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            //TODO what if ofst is out of range?
            
            //gets the fourth (int len) parameter
            final Primitive _len = (Primitive) this.data.operand(3);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.util.zip.Inflater.setDictionary cannot be a symbolic value.");
            }
            this.len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            //TODO what if len is out of range?
                        
            //invokes metacircularly the setDictionary method
            final Method method = Inflater.class.getDeclaredMethod("setDictionary", long.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(null, state.getInflater(this.addr), this.b, this.ofst, this.len);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, calc, cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | 
                 FastArrayAccessNotAllowedException | InvalidTypeException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.setInflaterDictionary(this.addr, this.b, this.ofst, this.len);
        };
    }
}
