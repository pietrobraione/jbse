package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.CLASS_NOT_FOUND_EXCEPTION;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getPrimitiveClass(String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETPRIMITIVECLASS extends Algo_INVOKEMETA_Nonbranching {
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void update(State state) 
    throws SymbolicValueNotAllowedException, ThreadStackEmptyException, InterruptException {
        try {           
            //gets the binary name of the primitive type and converts it to a string
            final Reference typeNameRef = (Reference) this.data.operand(0);
            final String typeName = valueString(state, typeNameRef);
            if (typeName == null) {
                throw new SymbolicValueNotAllowedException("the String parameter to java.lang.Class.getPrimitiveClass method cannot be a symbolic String");
            }

            //gets the instance of the class
            state.ensureInstance_JAVA_CLASS_primitive(typeName);
            final Reference classRef = state.referenceToInstance_JAVA_CLASS_primitive(typeName);
            state.pushOperand(classRef);
        } catch (ClassFileNotFoundException e) {
            throwNew(state, CLASS_NOT_FOUND_EXCEPTION);  //this is how Hotspot behaves
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        }
    }
}
