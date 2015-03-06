package jbse.tree;

import jbse.val.Value;

/**
 * {@link DecisionAlternative} for all the bytecodes that may 
 * load a value to the operand stack: load from local variable 
 * (*load*), load from field (get*), and load from array (*aload). 
 * This {@link DecisionAlternative} is only for the cases where 
 * the execution  of the bytecode actually loads the value to the 
 * operand stack.
 * 
 * @author Pietro Braione
 */
public interface DecisionAlternative_XYLOAD_GETX_Loads extends DecisionAlternative {
	Value getValueToLoad();
}
