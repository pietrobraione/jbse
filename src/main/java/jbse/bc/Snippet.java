package jbse.bc;

import java.util.ArrayList;
import java.util.HashMap;

import jbse.bc.exc.InvalidIndexException;
import jbse.val.Value;

/**
 * A bytecode snippet that may be injected
 * during the regular execution.  
 * 
 * @author Pietro Braione
 * @see SnippetFactory
 */
public final class Snippet {
	/** Constant pool: {@link Signature}s. Immutable. */
    private final HashMap<Integer, Signature> signatures;
    
	/** Constant pool: integer values. Immutable. */
    private final HashMap<Integer, Integer> integers;
    
	/** Constant pool: float values. Immutable. */
    private final HashMap<Integer, Float> floats;
    
	/** Constant pool: long values. Immutable. */
    private final HashMap<Integer, Long> longs;
    
	/** Constant pool: double values. Immutable. */
    private final HashMap<Integer, Double> doubles;
    
	/** Constant pool: {@link String}s. Immutable. */
    private final HashMap<Integer, String> strings;
    
	/** Constant pool: class names.Immutable.  */
    private final HashMap<Integer, String> classes;
    
    /** The arguments to the snippet. Immutable. */
    private final ArrayList<Value> args;
    
    /** The bytecode snippet to be executed. Immutable. */
    private final byte[] bytecode;
    
    /**
     * Constructor.
     * 
     * @param signatures a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link Signature} constant value. The caller must give up 
     *        ownership of this argument.
     * @param integers a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link Integer} constant value. The caller must give up 
     *        ownership of this argument.
     * @param floats a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link Float} constant value. The caller must give up 
     *        ownership of this argument.
     * @param longs a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link Long} constant value. The caller must give up 
     *        ownership of this argument.
     * @param doubles a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link Double} constant value. The caller must give up 
     *        ownership of this argument.
     * @param strings a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link String} constant value. The caller must give up 
     *        ownership of this argument.
     * @param classes a {@link HashMap} mapping an integer, interpreted
     *        as a constant pool index referred by {@code bytecode}, to a
     *        {@link String} constant value to be interpreted as a class
     *        name. The caller must give up ownership of this argument.
     * @param args an {@link ArrayList}{@code<}{@link Value}{@code >}
     *        that is interpreted as the list of arguments, stored in the
     *        local variables accessed by {@code bytecode}.  The caller 
     *        must give up ownership of this argument.
     * @param bytecode a {@code byte[]} containing the bytecode of the 
     *        snippet. The caller must give up ownership of this argument. 
     */
    Snippet(HashMap<Integer, Signature> signatures, HashMap<Integer, Integer> integers, HashMap<Integer, Float> floats, 
            HashMap<Integer, Long> longs, HashMap<Integer, Double> doubles, HashMap<Integer, String> strings, 
            HashMap<Integer, String> classes, ArrayList<Value> args, byte[] bytecode) {
        this.signatures = signatures;
        this.integers = integers;
        this.floats = floats;
        this.longs = longs;
        this.doubles = doubles;
        this.strings = strings;
        this.classes = classes;
        this.args = args;
        this.bytecode = bytecode;
    }
    
    /**
     * Returns the {@link Signature}s.
     * 
     * @return the {@link Signature} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, Signature> getSignatures() {
        return this.signatures;
    }
    
    /**
     * Returns the {@link Integer}s.
     * 
     * @return the {@link Integer} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, Integer> getIntegers() {
        return this.integers;
    }
    
    /**
     * Returns the {@link Float}s.
     * 
     * @return the {@link Float} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, Float> getFloats() {
        return this.floats;
    }
    
    /**
     * Returns the {@link String}s.
     * 
     * @return the {@link String} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, String> getStrings() {
        return this.strings;
    }
    
    /**
     * Returns the class names.
     * 
     * @return the class names constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, String> getClasses() {
        return this.classes;
    }
    
    /**
     * Returns the {@link Long}s.
     * 
     * @return the {@link Long} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, Long> getLongs() {
        return this.longs;
    }
    
    /**
     * Returns the {@link Double}s.
     * 
     * @return the {@link Double} constants 
     *         stored in this object. The returned
     *         value shall not be modified by the
     *         caller.
     */
    HashMap<Integer, Double> getDoubles() {
        return this.doubles;
    }
    
    /**
     * Returns the arguments.
     * 
     * @return the arguments stored in this object. 
     *         The returned value shall not be 
     *         modified by the caller.
     */
    public ArrayList<Value> getArgs() {
    	return this.args;
    }
    
    /**
     * Returns the bytecode.
     * 
     * @return the bytecode stored in this object.
     *         The returned value shall not be 
     *         modified by the caller.
     */
    public byte[] getBytecode() {
        return this.bytecode;
    }
    
    /**
     * Returns the total number of constants
     * stored in this snippet.
     * 
     * @return a nonnegative {@code int}.
     */
    int size() {
    	return this.signatures.size() +
               this.integers.size() +
               this.floats.size() +
               this.longs.size() +
               this.doubles.size() +
               this.strings.size() +
               this.classes.size();
    }
    
    /**
     * Checks whether this snippet has a constant value of 
     * primitive, String or Class type.
     *  
     * @param index an {@code int}.
     * @return {@code true} iff this snippet has an integer, 
     *         float, long, double, String or Class constant corresponding
     *         to {@code index}. 
     */
    boolean containsValueFromConstantPool(int index) {
    	return this.integers.containsKey(index) ||
               this.floats.containsKey(index) ||
               this.longs.containsKey(index) ||
               this.doubles.containsKey(index) ||
               this.strings.containsKey(index) ||
               this.classes.containsKey(index);
    }
    
    /**
     * Returns a constant value corresponding to an index (only for primitive, 
     * String, or Class constants).
     * 
     * @param index an {@code int}.
     * @return a {@link ConstantPoolValue} if {@code index} refers to a primitive, 
     *         string or class constant stored in this snippet.
     * @throws InvalidIndexException iff the snippet has no integer, 
     *         float, long, double, String or Class constant corresponding
     *         to {@code index}.
     */
    ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException {
    	if (this.integers.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.integers.get(index));
    	} else if (this.floats.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.floats.get(index));
    	} else if (this.longs.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.longs.get(index));
    	} else if (this.doubles.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.doubles.get(index));
    	} else if (this.strings.containsKey(index)) {
    		return new ConstantPoolString(this.strings.get(index));
    	} else if (this.classes.containsKey(index)) {
    		return new ConstantPoolClass(this.classes.get(index));
    	} else {
    		throw new InvalidIndexException("Constant pool index " + index + " does not correspond to a primitive, string, or class constant in snippet.");
    	}
    }
}
