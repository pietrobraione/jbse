package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.meta.Util.INVALID_FILE_ID;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.IO_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_FD;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_HANDLE;
import static jbse.bc.Signatures.JAVA_FILEOUTPUTSTREAM_FD;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.io.FileOutputStream#writeBytes(byte[], int, int, boolean)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_FILEOUTPUTSTREAM_WRITEBYTES extends Algo_INVOKEMETA_Nonbranching {
    private byte[] writeBytes; //set by cookMore
    private int len; //set by cookMore
    private FileOutputStream fos; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();    	
        try {
            //gets the FileOutputStream 'this' parameter and its file descriptor
            final Reference thisReference = (Reference) this.data.operand(0);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.io.FileOutputStream.writeBytes method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisReference);
            final Reference fileDescriptorReference = (Reference) thisObject.getFieldValue(JAVA_FILEOUTPUTSTREAM_FD);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.io.FileOutputStream.writeBytes method apparently has not a FileDescriptor fd field.");
            }
            final Instance fileDescriptor = (Instance) state.getObject(fileDescriptorReference);
            
            //determines if we are on Windows
            boolean onWindows;
            try {
            	FileDescriptor.class.getDeclaredField("handle");
            	//no exception: we are on windows
            	onWindows = true;
            } catch (NoSuchFieldException e) {
            	//we are not on Windows
            	onWindows = false;
            }
            
            //gets the file descriptor/handle
            final long fileId;
            if (onWindows) {
            	final Simplex _handle = (Simplex) fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_HANDLE);
            	fileId = ((Long) _handle.getActualValue()).longValue();
            } else {
            	final Simplex _fd = (Simplex) fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_FD);
            	fileId = ((Integer) _fd.getActualValue()).longValue();
            }

            //gets the buffer
            final Reference bufReference = (Reference) this.data.operand(1);
            if (state.isNull(bufReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Array buf = (Array) state.getObject(bufReference);
            if (!buf.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.io.FileOutputStream.writeBytes has not a simple representation.");
            }
            
            //gets offset
            final Primitive _ofst = (Primitive) this.data.operand(2);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int off parameter to invocation of method java.io.FileOutputStream.writeBytes cannot be a symbolic value.");
            }
            final int ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            
            //gets length
            final Primitive _len = (Primitive) this.data.operand(3);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.io.FileOutputStream.writeBytes cannot be a symbolic value.");
            }
            this.len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            
            //checks offset and length
            final int bufLength = ((Integer) ((Simplex) buf.getLength()).getActualValue()).intValue();
            if (ofst < 0 || this.len < 0 || bufLength - ofst < this.len) {
                throwNew(state, calc, INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //checks if len == 0
            if (this.len == 0) {
                //nothing to do
                exitFromAlgorithm();
            }
            
            //gets append
            final Primitive _append = (Primitive) this.data.operand(4);
            if (_append.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The boolean append parameter to invocation of method java.io.FileOutputStream.writeBytes cannot be a symbolic value.");
            }
            final boolean append = (((Integer) ((Simplex) _append).getActualValue()).intValue() != 0);
            
            //checks if the file is open
            if (fileId == INVALID_FILE_ID) {
                throwNew(state, calc, IO_EXCEPTION);
                exitFromAlgorithm();
            }

            //gets the (meta-level) FileOutputStream associated to fd
            this.fos = (FileOutputStream) state.getFile(fileId);
            //TODO more checks
            
            //sets the append field reflectively
            try {
                final Field fldAppend = FileOutputStream.class.getDeclaredField("append");
                fldAppend.setAccessible(true);
                fldAppend.set(this.fos, append);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                //this should never happen
                failExecution(e);
            }

            //puts the bytes to write into this.writeBytes
            this.writeBytes = new byte[this.len];
            try {
                for (int i = ofst; i < ofst + this.len; ++i) {
                    final Simplex val = (Simplex) ((AccessOutcomeInValue) buf.getFast(calc, calc.valInt(i))).getValue();
                    this.writeBytes[i - ofst] = ((Byte) val.getActualValue()).byteValue();
                }
            } catch (FastArrayAccessNotAllowedException | InvalidTypeException | 
                     InvalidInputException | ClassCastException e) {
                //this should never happen
                failExecution(e);
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            try {
                this.fos.write(this.writeBytes, 0, this.len);
            } catch (IOException e) {
                //read error
                throwNew(state, this.ctx.getCalculator(), IO_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }
}
