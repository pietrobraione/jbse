package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
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
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.io.FileInputStream#available()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_FILEINPUTSTREAM_AVAILABLE extends Algo_INVOKEMETA_Nonbranching {
    private Simplex retVal; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
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
                failExecution("The 'this' parameter to java.io.FileInputStream.available method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisReference);
            final Reference fileDescriptorReference = (Reference) thisObject.getFieldValue(JAVA_FILEINPUTSTREAM_FD);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.io.FileInputStream.available method apparently has not a FileDescriptor fd field.");
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
            int _retVal = 0; //to keep the compiler happy
            try {
                _retVal = fis.available();
            } catch (IOException e) {
                //read error
                throwNew(state, calc, IO_EXCEPTION);
                exitFromAlgorithm();
            }
            this.retVal = calc.valInt(_retVal);
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(this.retVal);
        };
    }
}
