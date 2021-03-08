package jbse.apps.disasm;

import jbse.bc.Dispatcher;
import jbse.bc.Dispatcher.DispatchStrategy;

/**
 * A {@link DispatchStrategy} for {@link BytecodeDisassembler}s. 
 * This interface exists only to simplify naming. 
 * 
 * @author Pietro Braione
 */
interface DispatchStrategyFormat 
extends Dispatcher.DispatchStrategy<BytecodeDisassembler> {
	String UNRECOGNIZED_BYTECODE = "<???>";
}