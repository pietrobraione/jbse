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
            final Simplex _fd = (Simplex) fileDescriptor.getFieldValue(JAVA_FILEDESCRIPTOR_FD);
            final int fd = ((Integer) _fd.getActualValue()).intValue();
            //TODO more checks

            //checks if the file is open
            if (fd == -1) {
                throwNew(state, IO_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //gets the (meta-level) FileInputStream associated to fd
            //and reads from it
            final FileInputStream fis = (FileInputStream) state.getFile(fd);
            //TODO more checks
            int _retVal = 0; //to keep the compiler happy
            try {
                _retVal = fis.available();
            } catch (IOException e) {
                //read error
                throwNew(state, IO_EXCEPTION);
                exitFromAlgorithm();
            }
            this.retVal = state.getCalculator().valInt(_retVal);
        } catch (ClassCastException e) {
            throwVerifyError(state);
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
