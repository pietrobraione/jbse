package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.meta.Util.INVALID_FILE_ID;
import static jbse.bc.Signatures.IO_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_FD;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_HANDLE;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM_FD;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.FileInputStream#close0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_FILEINPUTSTREAM_CLOSE0 extends Algo_INVOKEMETA_Nonbranching {
    private Instance fileDescriptor; //set by cookMore
    private boolean onWindows; //set by cookMore
    private long fileId; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
        try {
            //gets the FileInputStream 'this' parameter and its file descriptor
            final Reference thisReference = (Reference) this.data.operand(0);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.io.FileInputStream.close0 method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisReference);
            final Reference fileDescriptorReference = (Reference) thisObject.getFieldValue(JAVA_FILEINPUTSTREAM_FD);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.io.FileInputStream.close0 method apparently has not a FileDescriptor fd field.");
            }
            this.fileDescriptor = (Instance) state.getObject(fileDescriptorReference);
            
            //determines if we are on Windows
            try {
            	FileDescriptor.class.getDeclaredField("handle");
            	//no exception: we are on windows
            	this.onWindows = true;
            } catch (NoSuchFieldException e) {
            	//we are not on Windows
            	this.onWindows = false;
            }
            
            //gets the file descriptor/handle
            if (this.onWindows) {
            	final Simplex _handle = (Simplex) this.fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_HANDLE);
            	this.fileId = ((Long) _handle.getActualValue()).longValue();
            } else {
            	final Simplex _fd = (Simplex) this.fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_FD);
            	this.fileId = ((Integer) _fd.getActualValue()).longValue();
            }
            //TODO more checks

            //checks if the file is open
            if (this.fileId == INVALID_FILE_ID) {
                //nothing to do
                exitFromAlgorithm();
            }            
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //sets the descriptor's fd field to -1
            this.fileDescriptor.setFieldValue(JAVA_FILEDESCRIPTOR_FD, this.ctx.getCalculator().valInt((int) INVALID_FILE_ID));
            
            //if we are on Windows, also sets the descriptor's handle field to -1
            if (this.onWindows) {
            	this.fileDescriptor.setFieldValue(JAVA_FILEDESCRIPTOR_HANDLE, this.ctx.getCalculator().valLong(INVALID_FILE_ID));
            }
            
            //removes the association fd/FileInputStream from the state
            //and closes the FileInputStream
            final FileInputStream fis = (FileInputStream) state.getFile(this.fileId);
            state.removeFile(this.fileId);
            try {
                fis.close();
            } catch (IOException e) {
                //exception while closing
                throwNew(state, this.ctx.getCalculator(), IO_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }
}
