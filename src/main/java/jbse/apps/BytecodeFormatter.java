package jbse.apps;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;

/**
 * A higher-level disassembler.
 * 
 * @author Pietro Braione
 *
 */
public class BytecodeFormatter {
	private DispatcherBytecodeFormatter bdf = new DispatcherBytecodeFormatter();
	
	/**
	 * Disassemble the current bytecode in a frame.
	 * 
	 * @param f a {@link Frame}.
	 * @param cfi a {@link ClassHierarchy}.
	 * @return a {@link String}, the disassembly of the current bytecode
	 *         of {@code f}.
	 */
	public String format(Frame f, ClassHierarchy cfi) {
		return bdf.select(f.getInstruction()).apply(f, cfi);
	}

	/**
	 * Disassemble the current bytecode in a state.
	 * 
	 * @param s a {@link State}.
	 * @return a {@link String}, the disassembly of the current bytecode
	 *         of {@code s}.
	 * @throws ThreadStackEmptyException when {@code s} has not a
	 *         current frame (i.e., is stuck). 

	 */
	public String format(State s) throws ThreadStackEmptyException {
		return format(s.getCurrentFrame(), s.getClassHierarchy());
	}
}
