package jbse.algo.meta;

import static jbse.algo.Util.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.exc.BadClassFileException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

public final class Algo_SUN_REFLECTION_GETCLASSACCESSFLAGS extends Algo_INVOKEMETA_Nonbranching {
    private int flags; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
            final Reference refParam = (Reference) this.data.operand(0);
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(refParam);
            final String className = clazz.representedClass();
            this.flags = state.getClassHierarchy().getClassFile(className).getAccessFlags();
        } catch (ClassCastException | BadClassFileException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(state.getCalculator().valInt(this.flags));
    }
}
