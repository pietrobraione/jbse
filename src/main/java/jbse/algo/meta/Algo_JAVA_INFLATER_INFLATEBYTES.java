package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.JAVA_INFLATER_BUF;
import static jbse.bc.Signatures.JAVA_INFLATER_BYTESREAD;
import static jbse.bc.Signatures.JAVA_INFLATER_BYTESWRITTEN;
import static jbse.bc.Signatures.JAVA_INFLATER_FINISHED;
import static jbse.bc.Signatures.JAVA_INFLATER_LEN;
import static jbse.bc.Signatures.JAVA_INFLATER_NEEDDICT;
import static jbse.bc.Signatures.JAVA_INFLATER_OFF;
import static jbse.bc.Signatures.JAVA_INFLATER_ZSREF;
import static jbse.common.Type.internalClassName;
import static jbse.common.Util.unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;
import sun.misc.Unsafe;

/**
 * Meta-level implementation of {@link java.util.zip.Inflater#inflateBytes(long, byte[], int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_INFLATER_INFLATEBYTES extends Algo_INVOKEMETA_Nonbranching {
    private Instance _inflater; //set by cookMore
    private Array outBuf; //set by cookMore
    private int ofst; //set by cookMore
    private Inflater inflater; //set by cookMore
    private byte[] inflatedBytes; //set by cookMore
    private int nread; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, 
    UndefinedResultException, InvalidInputException {
        try {
            //gets the first ('this') parameter
            final Reference thisReference = (Reference) this.data.operand(0);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.util.zip.Inflater.inflateBytes method is null.");
            }
            this._inflater = (Instance) state.getObject(thisReference);
            
            //gets the second (long addr) parameter
            final Primitive _addr = (Primitive) this.data.operand(1);
            if (_addr.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long addr parameter to invocation of method java.util.zip.Inflater.inflateBytes cannot be a symbolic value.");
            }
            final long addr = Long.valueOf(state.getInflater(((Long) ((Simplex) _addr).getActualValue()).longValue()));
            //TODO what if addr is wrong?
            
            //gets the third (byte[] b) parameter
            final Reference bufReference = (Reference) this.data.operand(2);
            if (state.isNull(bufReference)) {
                throw new UndefinedResultException("Invoked method java.util.zip.Inflater.inflateBytes with a null b parameter.");
            }
            this.outBuf = (Array) state.getObject(bufReference);
            if (!this.outBuf.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.util.zip.Inflater.inflateBytes has not a simple representation.");
            }
            
            //gets the fourth (int off) parameter
            final Primitive _ofst = (Primitive) this.data.operand(3);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int off parameter to invocation of method java.util.zip.Inflater.inflateBytes cannot be a symbolic value.");
            }
            this.ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            //TODO what if ofst is out of range?
            
            //gets the fifth (int len) parameter
            final Primitive _len = (Primitive) this.data.operand(4);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.util.zip.Inflater.inflateBytes cannot be a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            //TODO what if len is out of range?
                        
            //invokes metacircularly the inflateBytes method
            makeInflater(state, addr);
            this.inflatedBytes = new byte[len];
            final Method method = Inflater.class.getDeclaredMethod("inflateBytes", long.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            this.nread = (int) method.invoke(this.inflater, addr, this.inflatedBytes, 0, len);
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
    
    private void makeInflater(State state, long zsrefAddress) throws SymbolicValueNotAllowedException, UndefinedResultException, InvalidInputException {
        try {
            final Calculator calc = this.ctx.getCalculator();
            
            //builds a zsRef
            final Class<?> clZStreamRef = Class.forName("java.util.zip.ZStreamRef");
            final Constructor<?> consZStreamRef = clZStreamRef.getDeclaredConstructor(long.class);
            consZStreamRef.setAccessible(true);
            final Object zsRef = consZStreamRef.newInstance(zsrefAddress);
            
            //gets this.inflater.buf
            final Reference bufReference = (Reference) this._inflater.getFieldValue(JAVA_INFLATER_BUF);
            if (state.isNull(bufReference)) {
                //method invoked on a closed inflater
                throw new UndefinedResultException("The byte[] this.buf field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is null.");
            }
            final Array _inBuf = (Array) state.getObject(bufReference);
            if (!_inBuf.isSimple()) {
                throw new SymbolicValueNotAllowedException("The byte[] this.buf field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is not simple.");
            }
            
            //builds a buffer
            final int inBufLength = ((Integer) ((Simplex) _inBuf.getLength()).getActualValue()).intValue();
            final byte[] inBuf = new byte[inBufLength];
            for (int i = 0; i < inBufLength; ++i) {
                final Simplex inBuf_i = (Simplex) ((Array.AccessOutcomeInValue) _inBuf.get(calc, calc.valInt(i)).iterator().next()).getValue(); 
                inBuf[i] = ((Byte) inBuf_i.getActualValue()).byteValue();
            }
            
            //gets this.inflater.off
            final Primitive _off = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_OFF);
            if (_off.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.off field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final int off = ((Integer) ((Simplex) _off).getActualValue()).intValue();
            
            //gets this.inflater.len
            final Primitive _len = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_LEN);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.len field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            
            //gets this.inflater.finished
            final Primitive _finished = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_FINISHED);
            if (_finished.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.finished field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final boolean finished = ((Boolean) ((Simplex) _finished).getActualValue()).booleanValue();
            
            //gets this.inflater.needDict
            final Primitive _needDict = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_NEEDDICT);
            if (_needDict.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.needDict field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final boolean needDict = ((Boolean) ((Simplex) _needDict).getActualValue()).booleanValue();
            
            //gets this.inflater.bytesRead
            final Primitive _bytesRead = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_BYTESREAD);
            if (_bytesRead.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.bytesRead field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final long bytesRead = ((Long) ((Simplex) _bytesRead).getActualValue()).longValue();
            
            //gets this.inflater.bytesWritten
            final Primitive _bytesWritten = (Primitive) this._inflater.getFieldValue(JAVA_INFLATER_BYTESWRITTEN);
            if (_bytesWritten.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int this.zsRef.bytesWritten field of 'this' parameter to invocation of method java.util.zip.Inflater.inflateBytes is a symbolic value.");
            }
            final long bytesWritten = ((Long) ((Simplex) _bytesWritten).getActualValue()).longValue();
            
            //makes the inflater
            final Unsafe unsafe = unsafe();
            this.inflater = (Inflater) unsafe.allocateInstance(Inflater.class);
            final Field zsRefFld = Inflater.class.getDeclaredField(JAVA_INFLATER_ZSREF.getName());
            zsRefFld.setAccessible(true);
            zsRefFld.set(this.inflater, zsRef);
            final Field bufFld = Inflater.class.getDeclaredField(JAVA_INFLATER_BUF.getName());
            bufFld.setAccessible(true);
            bufFld.set(this.inflater, inBuf);
            final Field offFld = Inflater.class.getDeclaredField(JAVA_INFLATER_OFF.getName());
            offFld.setAccessible(true);
            offFld.set(this.inflater, off);
            final Field lenFld = Inflater.class.getDeclaredField(JAVA_INFLATER_LEN.getName());
            lenFld.setAccessible(true);
            lenFld.set(this.inflater, len);
            final Field finishedFld = Inflater.class.getDeclaredField(JAVA_INFLATER_FINISHED.getName());
            finishedFld.setAccessible(true);
            finishedFld.set(this.inflater, finished);
            final Field needDictFld = Inflater.class.getDeclaredField(JAVA_INFLATER_NEEDDICT.getName());
            needDictFld.setAccessible(true);
            needDictFld.set(this.inflater, needDict);
            final Field bytesReadFld = Inflater.class.getDeclaredField(JAVA_INFLATER_BYTESREAD.getName());
            bytesReadFld.setAccessible(true);
            bytesReadFld.set(this.inflater, bytesRead);
            final Field bytesWrittenFld = Inflater.class.getDeclaredField(JAVA_INFLATER_BYTESWRITTEN.getName());
            bytesWrittenFld.setAccessible(true);
            bytesWrittenFld.set(this.inflater, bytesWritten);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
                 InstantiationException | IllegalAccessException | IllegalArgumentException | 
                 InvocationTargetException | InvalidTypeException | 
                 NoSuchFieldException | ClassCastException e) {
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
                    this.outBuf.setFast(calc.valInt(i), calc.valByte(this.inflatedBytes[i - this.ofst]));
                }
            } catch (FastArrayAccessNotAllowedException e) {
                //this should never happen
                failExecution(e);
            }
            updateInflater(state);
        };
    }
    
    private void updateInflater(State state) {
        try {
            //reads the possibly modified fields of this.inflaterMeta
            final Field offFld = Inflater.class.getDeclaredField(JAVA_INFLATER_OFF.getName());
            offFld.setAccessible(true);
            final int off = ((Integer) offFld.get(this.inflater)).intValue();
            final Field lenFld = Inflater.class.getDeclaredField(JAVA_INFLATER_LEN.getName());
            lenFld.setAccessible(true);
            final int len = ((Integer) lenFld.get(this.inflater)).intValue();
            final Field finishedFld = Inflater.class.getDeclaredField(JAVA_INFLATER_FINISHED.getName());
            finishedFld.setAccessible(true);
            final boolean finished = ((Boolean) finishedFld.get(this.inflater)).booleanValue();
            final Field needDictFld = Inflater.class.getDeclaredField(JAVA_INFLATER_NEEDDICT.getName());
            needDictFld.setAccessible(true);
            final boolean needDict = ((Boolean) needDictFld.get(this.inflater)).booleanValue();
            
            //updates this.inflaterBase
            final Calculator calc = this.ctx.getCalculator();
            this._inflater.setFieldValue(JAVA_INFLATER_OFF, calc.valInt(off));
            this._inflater.setFieldValue(JAVA_INFLATER_LEN, calc.valInt(len));
            this._inflater.setFieldValue(JAVA_INFLATER_FINISHED, calc.valBoolean(finished));
            this._inflater.setFieldValue(JAVA_INFLATER_NEEDDICT, calc.valBoolean(needDict));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            //this should never happen
            failExecution(e);
        }
    }
}
