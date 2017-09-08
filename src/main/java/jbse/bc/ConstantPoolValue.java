package jbse.bc;

import jbse.val.Value;

/**
 * A {@link Value} representing a Java object indicated by some constant 
 * pool entry and used by ldc bytecodes. JBSE currently supports primitives, 
 * string literals and symbolic references to classes, but in future we might 
 * as well support method types and method handles. 
 * 
 * @author Pietro Braione
 *
 */
public abstract class ConstantPoolValue {
    public abstract Object getValue();
}
