package jbse.apps;

import jbse.bc.ClassHierarchy;
import jbse.exc.common.UnexpectedInternalException;
import jbse.mem.Frame;
import jbse.mem.State;

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
	 * @throws UnexpectedInternalException 
	 */
	public String format(Frame f, ClassHierarchy cfi) 
	throws UnexpectedInternalException {
		return bdf.select(f.getInstruction()).format(f, cfi);
	}

	/**
	 * Disassemble the current bytecode in a state.
	 * 
	 * @param s a {@link State}.
	 * @return a {@link String}, the disassembly of the current bytecode
	 *         of {@code s}.
	 * @throws UnexpectedInternalException 
	 */
	public String format(State s) 
	throws UnexpectedInternalException {
		return format(s.getCurrentFrame(), s.getClassHierarchy());
	}
}
