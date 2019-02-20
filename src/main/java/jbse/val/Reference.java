package jbse.val;

import jbse.common.Type;

/**
 * Class for references to heap objects (instances and arrays).
 */
public abstract class Reference extends Value {
    protected Reference(char type) {
        super(type);
    }

    protected Reference() {
        this(Type.REFERENCE);
    }
}