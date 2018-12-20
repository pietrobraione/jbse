package jbse.bc;

import java.util.List;
import java.util.Map;

import jbse.bc.exc.InvalidIndexException;
import jbse.val.Value;

public final class Snippet {
    private final Map<Integer, Signature> signatures;
    private final Map<Integer, Integer> integers;
    private final Map<Integer, Float> floats;
    private final Map<Integer, Long> longs;
    private final Map<Integer, Double> doubles;
    private final Map<Integer, String> strings;
    private final Map<Integer, String> classes;
    private final List<Value> args;
    private final byte[] bytecode;
    
    Snippet(Map<Integer, Signature> signatures, Map<Integer, Integer> integers, Map<Integer, Float> floats, 
            Map<Integer, Long> longs, Map<Integer, Double> doubles, Map<Integer, String> strings, 
            Map<Integer, String> classes, List<Value> args, byte[] bytecode) {
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
    
    public Map<Integer, Signature> getSignatures() {
        return this.signatures;
    }
    
    public Map<Integer, Integer> getIntegers() {
        return this.integers;
    }
    
    public Map<Integer, Float> getFloats() {
        return this.floats;
    }
    
    public Map<Integer, String> getStrings() {
        return this.strings;
    }
    
    public Map<Integer, String> getClasses() {
        return this.classes;
    }
    
    public Map<Integer, Long> getLongs() {
        return this.longs;
    }
    
    public Map<Integer, Double> getDoubles() {
        return this.doubles;
    }
    
    public List<Value> getArgs() {
    	return this.args;
    }
    
    public byte[] getBytecode() {
        return this.bytecode;
    }
    
    /**
     * Returns the total number of constants
     * stored in this snippet.
     * 
     * @return a nonnegative {@code int}.
     */
    public int size() {
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
    public boolean containsValueFromConstantPool(int index) {
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
    public ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException {
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
