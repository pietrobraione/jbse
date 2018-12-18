package jbse.bc;

import java.util.List;
import java.util.Map;

import jbse.bc.exc.InvalidIndexException;
import jbse.val.Value;

public final class Snippet {
    private final Map<Integer, Signature> signatures;
    private final Map<Integer, Integer> integers;
    private final Map<Integer, Long> longs;
    private final Map<Integer, Float> floats;
    private final Map<Integer, Double> doubles;
    private final Map<Integer, String> utf8s;
    private final Map<Integer, String> strings;    
    private final Map<Integer, String> classes;
    private final List<Value> args;
    private final byte[] bytecode;
    
    Snippet(Map<Integer, Signature> signatures, Map<Integer, Integer> integers, Map<Integer, Long> longs, Map<Integer, Float> floats, 
    		Map<Integer, Double> doubles, Map<Integer, String> utf8s, Map<Integer, String> strings, Map<Integer, String> classes, 
    		List<Value> args, byte[] bytecode) {
        this.signatures = signatures;
        this.integers = integers;
        this.longs = longs;
        this.floats = floats;
        this.doubles = doubles;
        this.utf8s = utf8s;
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
    
    public Map<Integer, Long> getLongs() {
        return this.longs;
    }
    
    public Map<Integer, Float> getFloats() {
        return this.floats;
    }
    
    public Map<Integer, Double> getDoubles() {
        return this.doubles;
    }
    
    public Map<Integer, String> getUtf8s() {
        return this.utf8s;
    }
    
    public Map<Integer, String> getStrings() {
        return this.strings;
    }
    
    public Map<Integer, String> getClasses() {
        return this.classes;
    }
    
    public List<Value> getArgs() {
    	return this.args;
    }
    
    public byte[] getBytecode() {
        return this.bytecode;
    }
    
    public int size() {
    	return this.signatures.size() +
               this.integers.size() +
               this.longs.size() +
               this.floats.size() +
               this.doubles.size() +
               this.utf8s.size() +
               this.strings.size() +
               this.classes.size();
    }
    
    public boolean containsValueFromConstantPool(int index) {
    	return this.integers.containsKey(index) ||
               this.longs.containsKey(index) ||
               this.floats.containsKey(index) ||
               this.doubles.containsKey(index) ||
               this.utf8s.containsKey(index) ||
               this.strings.containsKey(index) ||
               this.classes.containsKey(index);
    }
    
    public ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException {
    	if (this.integers.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.integers.get(index));
    	} else if (this.longs.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.longs.get(index));
    	} else if (this.floats.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.floats.get(index));
    	} else if (this.doubles.containsKey(index)) {
    		return new ConstantPoolPrimitive(this.doubles.get(index));
    	} else if (this.utf8s.containsKey(index)) {
    		return new ConstantPoolUtf8(this.utf8s.get(index));
    	} else if (this.strings.containsKey(index)) {
    		return new ConstantPoolString(this.strings.get(index));
    	} else if (this.classes.containsKey(index)) {
    		return new ConstantPoolClass(this.classes.get(index));
    	} else {
    		throw new InvalidIndexException("Constant pool index " + index + " does not exist in snippet.");
    	}
    }
}
