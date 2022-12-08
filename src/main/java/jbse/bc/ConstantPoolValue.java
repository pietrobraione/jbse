package jbse.bc;

/**
 * Class representing a value indicated by some constant 
 * pool entry and used by ldc bytecodes.
 * 
 * @author Pietro Braione
 */
public abstract class ConstantPoolValue {
    public abstract Object getValue();
}
