package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.IO_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_FD;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM_FD;

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
    private int fd; //set by cookMore
    
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
            final Simplex _fd = (Simplex) this.fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_FD);
            this.fd = ((Integer) _fd.getActualValue()).intValue();
            //TODO more checks

            //checks if the file is open
            if (this.fd == -1) {
                //nothing to do
                exitFromAlgorithm();
            }            
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //sets the descriptor's fd field to -1
            this.fileDescriptor.setFieldValue(JAVA_FILEDESCRIPTOR_FD, state.getCalculator().valInt(-1));
            
            
            //removes the association fd/FileInputStream from the state
            //and closes the FileInputStream
            final FileInputStream fis = (FileInputStream) state.getFile(this.fd);
            state.removeFile(this.fd);
            try {
                fis.close();
            } catch (IOException e) {
                //exception while closing
                throwNew(state, IO_EXCEPTION);
                exitFromAlgorithm();
            }
        };
    }
}
