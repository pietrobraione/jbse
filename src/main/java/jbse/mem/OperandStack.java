package jbse.mem;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.val.Value;

/**
 * Class that represent the JVM's operand stack.
 */
//TODO manage stack maps and possibly raise unexpected internal error
final class OperandStack implements Cloneable {
    /** Not final because of clone(). */
    private ArrayDeque<Value> valueStack;
    
    /**
     * Constructor of empty operand stack.
     * 
     */
    OperandStack() {
        this.valueStack = new ArrayDeque<Value>();
    }
    
    /**
     * Pushes a {@link Value} on the top of the operand stack.
     * @param item the {@link Value} to be pushed on the top 
     *             of the operand stack.
     */
    void push(Value item) {
        this.valueStack.push(item);
    }
    
    /**
     * Extracts and returns the element on the top of the operand stack. 
     * 
     * @return the {@link Value} on the top of the operand stack.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    Value pop() throws InvalidNumberOfOperandsException {
    	if (this.valueStack.isEmpty()) {
    		throw new InvalidNumberOfOperandsException();
    	}
    	return this.valueStack.pop();
    }

    /**
     * Removes the topmost {@code num} elements in the operand stack.
     * 
     * @param num a nonnegative {@code int}.
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, or
     *         if {@code num} is negative.
     */
    void pop(int num) throws InvalidNumberOfOperandsException {
        if (num < 0 || this.valueStack.size() < num) {
            throw new InvalidNumberOfOperandsException();
        }
        for (int i = 1; i <= num; ++i) {
            this.valueStack.pop();
        }
    }
    
    /**
     * Returns the topmost element without removing it.
     * 
     * @return the topmost {@link Value}.
     * @throws InvalidNumberOfOperandsException if the operand stack is empty.
     */
    Value top() throws InvalidNumberOfOperandsException {
    	if (this.valueStack.isEmpty()) {
    		throw new InvalidNumberOfOperandsException();
    	}
        return(this.valueStack.peek());
    }

    /**
     * Returns the topmost {@code num} elements without removing them.
     * 
     * @param num a nonnegative {@code int}.
     * @return a {@link Value}{@code []} containing the first {@code num}
     *         elements of the operand stack in reverse depth order 
     *         (the topmost value in the operand stack will be the last
     *         value in the return value).
     * @throws InvalidNumberOfOperandsException if the operand stack 
     *         does not contain at least {@code num} elements, 
     *         or if {@code num} is negative. 
     */
    Value[] operands(int num) throws InvalidNumberOfOperandsException {
        if (num < 0 || this.valueStack.size() < num) {
            throw new InvalidNumberOfOperandsException();
        }
        final Value[] retVal = new Value[num];
        int i = num - 1;
        for (Value v : this.valueStack) {
            if (i < 0) {
                break;
            }
            retVal[i] = v;
            --i;
        }
        return retVal;
    }
    
    void clear() {
    	this.valueStack.clear();
    }
    
    Collection<Value> values() {
    	return Collections.unmodifiableCollection(this.valueStack);
    }
    
    /**
     * Returns a string representation for the operand stack
     */
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("{");
        boolean isFirst = true;
        for (Value v : this.valueStack) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            buf.append(v.toString());
        }
        buf.append("}");
        return buf.toString();
    }
    
    @Override
    public OperandStack clone() {
        final OperandStack o;
        try {
            o = (OperandStack) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }        
        o.valueStack = new ArrayDeque<Value>(this.valueStack);
        return o;
    }
}


