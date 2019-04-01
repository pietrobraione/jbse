package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.algo.meta.Util.INVALID_FILE_ID;
import static jbse.bc.Signatures.FILE_NOT_FOUND_EXCEPTION;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_FD;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_HANDLE;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM_FD;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
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

/**
 * Meta-level implementation of {@link java.io.FileInputStream#open0(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_FILEINPUTSTREAM_OPEN0 extends Algo_INVOKEMETA_Nonbranching {
    private Instance fileDescriptor; //set by cookMore
    private int fd; //set by cookMore
    private boolean onWindows; //set by cookMore
    private long handle; //set by cookMore
    private FileInputStream fis; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
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
                failExecution("The 'this' parameter to java.io.FileInputStream.open0 method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisReference);
            final Reference fileDescriptorReference = (Reference) thisObject.getFieldValue(JAVA_FILEINPUTSTREAM_FD);
            if (state.isNull(fileDescriptorReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.io.FileInputStream.open0 method apparently has not a FileDescriptor fd field.");
            }
            this.fileDescriptor = (Instance) state.getObject(fileDescriptorReference);
            
            //gets the String parameter
            final Reference pathReference = (Reference) this.data.operand(1);
            if (state.isNull(pathReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final String path = valueString(state, pathReference);
            if (path == null) {
                throw new SymbolicValueNotAllowedException("The String parameter to invocation of method java.io.FileInputStream.open0 has a symbolic String in its path field.");
            }
            
            //opens a FileInputStream; this invokes metacircularly 
            //the open0 method that implants a file descriptor in this.fis 
            try {
                this.fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                throwNew(state, calc, FILE_NOT_FOUND_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //gets the file descriptor from fis
            final FileDescriptor fileDescriptor = this.fis.getFD();
            final Field fileDescriptorFD = FileDescriptor.class.getDeclaredField("fd");
            fileDescriptorFD.setAccessible(true);
            this.fd = ((Integer) fileDescriptorFD.get(fileDescriptor)).intValue();
            try {
            	final Field fileDescriptorHandle = FileDescriptor.class.getDeclaredField("handle");
            	//no exception: we are on windows
            	this.onWindows = true;
            	fileDescriptorHandle.setAccessible(true);
                this.handle = ((Long) fileDescriptorHandle.get(fileDescriptor)).longValue();
            } catch (NoSuchFieldException e) {
            	//we are not on Windows
            	this.onWindows = false;
            	this.handle = INVALID_FILE_ID;
            }
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IOException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //implants this.fd in this.fileDescriptor
            this.fileDescriptor.setFieldValue(JAVA_FILEDESCRIPTOR_FD, this.ctx.getCalculator().valInt(this.fd));
            
            //implants this.handle in this.fileDescriptor, if it has such a field
            if (this.onWindows) {
            	this.fileDescriptor.setFieldValue(JAVA_FILEDESCRIPTOR_HANDLE, this.ctx.getCalculator().valLong(this.handle));
            }
            
            //associates in state the file descriptor to the FileInputStream
            //created to access the file at the meta-level
            if (this.onWindows) {
            	state.setFile(this.handle, this.fis);
            } else {
            	state.setFile(this.fd, this.fis);
            }
        };
    }
}
