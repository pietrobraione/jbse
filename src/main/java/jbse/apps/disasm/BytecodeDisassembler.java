package jbse.apps.disasm;

import java.util.function.BiFunction;

import jbse.bc.ClassHierarchy;
import jbse.mem.Frame;

/**
 * A {@link BiFunction} that, when fed by a 
 * {@link Frame} and a {@link ClassHierarchy}, 
 * disassembles the current bytecode in the
 * {@link Frame} and returns the disassembled
 * {@link String}. A {@link BytecodeDisassembler}
 * can disassemble only one kind of bytecode, 
 * that must be the current bytecode of the
 * {@link Frame} that is passed as argument to it.
 * 
 * @author Pietro Braione
 *
 */
interface BytecodeDisassembler
extends BiFunction<Frame, ClassHierarchy, String> {
    //empty
}