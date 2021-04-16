package jbse.apps.disasm;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * A disassembler.
 * 
 * @author Pietro Braione
 *
 */
public class Disassembler {
    private DispatcherBytecodeDisassembler dbf = new DispatcherBytecodeDisassembler();

    /**
     * Disassemble the current bytecode in a frame.
     * 
     * @param f a {@link Frame}.
     * @param hier a {@link ClassHierarchy}.
     * @return a {@link String}, the disassembly of the current bytecode
     *         of {@code f}.
     */
    public String format(Frame f, ClassHierarchy hier) {
        return this.dbf.select(f.getInstruction()).apply(f, hier);
    }

    /**
     * Disassemble the current bytecode in a state.
     * 
     * @param s a {@link State}.
     * @return a {@link String}, the disassembly of the current bytecode
     *         of {@code s}.
     * @throws ThreadStackEmptyException when {@code s} has not a
     *         current frame (i.e., is stuck). 
     * @throws FrozenStateException if {@code s} is frozen.

     */
    public String format(State s) 
    throws ThreadStackEmptyException, FrozenStateException {
        return format(s.getCurrentFrame(), s.getClassHierarchy());
    }
}
