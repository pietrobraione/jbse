package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.Value;

//TODO merge with Algo_GETFIELD
/**
 * {@link Algorithm} managing the "set field in object" 
 * (putfield) bytecode.
 * 
 * @author Pietro Braione
 *
 */
final class Algo_PUTFIELD extends Algo_PUTX {
    public Algo_PUTFIELD() {
        super(false);
    }

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void checkMore(State state)
    throws FieldNotFoundException, InterruptException, ClasspathException, ThreadStackEmptyException, FrozenStateException {
        //checks that the field is not static
        if (this.fieldClassResolved.isFieldStatic(this.data.signature())) {
            throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        }
        
        //checks that, if the field is protected and is member of a superclass
        //of the current class, and is not declared in the same runtime package
        //as the current class, then the class of the destination object
        //must be either the current class or a subclass of the current class
        final ClassFile currentClass = state.getCurrentClass();
        final boolean sameRuntimePackage = 
            currentClass.getDefiningClassLoader() == this.fieldClassResolved.getDefiningClassLoader() && currentClass.getPackageName().equals(this.fieldClassResolved.getPackageName());
        boolean isMemberOfASuperclassOfCurrentClass = false;
        if (currentClass.getSuperclass() != null) {
        	for (ClassFile cf : currentClass.getSuperclass().superclasses()) {
        		if (this.fieldClassResolved.equals(cf)) {
        			isMemberOfASuperclassOfCurrentClass = true;
        			break;
        		}
        	}
        }
        if (this.fieldClassResolved.isFieldProtected(this.data.signature()) &&
            isMemberOfASuperclassOfCurrentClass && !sameRuntimePackage) {
            boolean destinationClassIsCurrentClassOrSubclass = false;
            for (ClassFile cf: destination(state).getType().superclasses()) {
                if (currentClass.equals(cf)) {
                    destinationClassIsCurrentClassOrSubclass = true;
                    break;
                }
            }
            if (!destinationClassIsCurrentClassOrSubclass) {
                //TODO the JVMS v8, putfield instruction, does not say what to do in this case; we assume it is an invariant ensured by verification and so throw VerifyError
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            }
        }            
    }
    
    @Override
    protected Value valueToPut() {
        return this.data.operand(1);
    }

    @Override
    protected Objekt destination(State state) throws InterruptException, ClasspathException, FrozenStateException {
        try {
            final Reference myObjectRef = (Reference) this.data.operand(0);
            if (state.isNull(myObjectRef)) {
                throwNew(state, this.ctx.getCalculator(), NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            return state.getObject(myObjectRef);
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (ClasspathException e) {
            //this should never happen
            failExecution(e);
        }
        return null; //to keep the compiler happy
    }
}
