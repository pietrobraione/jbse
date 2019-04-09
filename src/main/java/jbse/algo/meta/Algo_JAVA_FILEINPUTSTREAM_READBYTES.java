package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.meta.Util.INVALID_FILE_ID;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.IO_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_FD;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_HANDLE;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM_FD;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.FileInputStream#readBytes(byte[], int, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_FILEINPUTSTREAM_READBYTES extends Algo_INVOKEMETA_Nonbranching {
    private Array buf; //set by cookMore
    private int ofst; //set by cookMore
    private byte[] readBytes; //set by cookMore
    private int nread; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 4;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
    	final Calculator calc = this.ctx.getCalculator();    	
        try {
            //gets the FileInputStream 'this' parameter and its file descriptor
            final Reference thisReference = (Reference) this.data.operand(0);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.io.FileInputStream.readBytes method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisReference);
            final Reference fileDescriptorReference = (Reference) thisObject.getFieldValue(JAVA_FILEINPUTSTREAM_FD);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.io.FileInputStream.readBytes method apparently has not a FileDescriptor fd field.");
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
            this.buf = (Array) state.getObject(bufReference);
            if (!this.buf.hasSimpleRep()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.io.FileInputStream.readBytes has not a simple representation.");
            }
            
            //gets offset
            final Primitive _ofst = (Primitive) this.data.operand(2);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int ofst parameter to invocation of method java.io.FileInputStream.readBytes cannot be a symbolic value.");
            }
            this.ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            
            //gets length
            final Primitive _len = (Primitive) this.data.operand(3);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.io.FileInputStream.readBytes cannot be a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            
            //checks offset and length
            final int bufLength = ((Integer) ((Simplex) this.buf.getLength()).getActualValue()).intValue();
            if (this.ofst < 0 || len < 0 || bufLength - this.ofst < len) {
                throwNew(state, calc, INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //checks if the file is open
            if (fileId == INVALID_FILE_ID) {
                throwNew(state, calc, IO_EXCEPTION);
                exitFromAlgorithm();
            }

            //TODO more checks

            //gets the (meta-level) FileInputStream associated to fd
            //and reads from it
            final FileInputStream fis = (FileInputStream) state.getFile(fileId);
            //TODO more checks
            this.readBytes = new byte[len];
            try {
                this.nread = fis.read(this.readBytes, 0, len);
            } catch (IOException e) {
                //read error
                throwNew(state, calc, IO_EXCEPTION);
                exitFromAlgorithm();
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
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
