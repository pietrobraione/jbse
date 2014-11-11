package jbse.mem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbse.exc.mem.ThreadStackEmptyException;

/**
 * Class representing JVM thread stacks.
 */
class ThreadStack implements Cloneable {
	/** The stack position of the root frame. */
	private static final int ROOT_FRAME = 0;

    private ArrayList<Frame> frameStack;

    /**
     * Constructor of stack.
     */
    ThreadStack() {
        this.frameStack = new ArrayList<Frame>();
    }
    
    /**
     * Puts a frame on the top of the stack.
     * 
     * @param item {@link Frame} that must be put on the top of the stack, 
     *        and that will become the current frame.
     */
    void push(Frame item){
    	this.frameStack.add(item);
    }
    
    /**
     * Return the current frame and deletes it
     * from the stack.
     * 
     * @return the current (topmost) {@link Frame} 
     * before the call.
     * @throws ThreadStackEmptyException 
     */
    Frame pop() throws ThreadStackEmptyException {
        if (this.isEmpty()) {
            throw new ThreadStackEmptyException();
        }
        return this.frameStack.remove(this.frameStack.size() - 1);
    }
    
    /**
     * Tests if the stack is empty.
     * 
     * @return {@code true} iff the stack is empty.
     */
    boolean isEmpty() {
        return this.frameStack.isEmpty();
    }
    
    /**
     * Remove all the elements from the stack.
     */
    void clear() {
        this.frameStack.clear();
    }
    
    /**
     * Returns the current frame.
     * 
     * @return the current {@link Frame}, i.e., the 
     *         {@link Frame} on the top of the stack.
     * @throws ThreadStackEmptyException 
     */
    Frame currentFrame() throws ThreadStackEmptyException {
        if (this.isEmpty()) {
            throw new ThreadStackEmptyException();
        }
        return frameStack.get(frameStack.size() - 1);
    }
    
    /**
     * Returns the current frame.
     * 
     * @return the current {@link Frame}, i.e., the 
     *         {@link Frame} on the top of the stack.
     * @throws ThreadStackEmptyException 
     */
    Frame rootFrame() throws ThreadStackEmptyException {
        if (this.isEmpty()) {
            throw new ThreadStackEmptyException();
        }
        return frameStack.get(ROOT_FRAME);
    }
    
    /**
     * Returns an unmodifiable list of all the frames
     * in the stack.
     * 
     * @return a {@link List}{@code <}{@link Frame}{@code >} 
     *         of the frames in the stack, in their push order.
     */
    List<Frame> frames() {
    	return Collections.unmodifiableList(this.frameStack);
    }
    
    @Override
    public String toString() {
    	String tmpRet = "[";
        int j = 0;
        for (Frame f : this.frameStack) {
            tmpRet +=  j + ":" + f.toString(); 
            if (j < this.frameStack.size() - 1) 
            	tmpRet += ", ";
            j++;
        }
        tmpRet += "]";
        return(tmpRet);
    }
    
    @Override
    public ThreadStack clone() {
        final ThreadStack o;
        try {
            o = (ThreadStack) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        
        final ArrayList<Frame> frameStackClone = new ArrayList<Frame>();
        for (int i = 0; i < this.frameStack.size(); ++i) {
            frameStackClone.add(this.frameStack.get(i).clone());
        }
        o.frameStack = frameStackClone;
        //TODO is it just the same as o.frameStack = (ArrayList) o.frameStack.clone()?
        return o;
    }   
}