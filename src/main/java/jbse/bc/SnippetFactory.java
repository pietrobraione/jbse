package jbse.bc;

import static jbse.bc.Opcodes.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jbse.val.Value;

public class SnippetFactory {
    private final HashMap<Integer, Signature> signatures = new HashMap<>();
    private final HashMap<Integer, Integer> integers = new HashMap<>();
    private final HashMap<Integer, Float> floats = new HashMap<>();
    private final HashMap<Integer, Long> longs = new HashMap<>();
    private final HashMap<Integer, Double> doubles = new HashMap<>();
    private final HashMap<Integer, String> strings = new HashMap<>();    
    private final HashMap<Integer, String> classes = new HashMap<>();
    private final HashMap<Signature, Integer> signaturesInverse = new HashMap<>();
    private final HashMap<Integer, Integer> integersInverse = new HashMap<>();
    private final HashMap<Float, Integer> floatsInverse = new HashMap<>();
    private final HashMap<Long, Integer> longsInverse = new HashMap<>();
    private final HashMap<Double, Integer> doublesInverse = new HashMap<>();
    private final HashMap<String, Integer> stringsInverse = new HashMap<>();    
    private final HashMap<String, Integer> classesInverse = new HashMap<>();
    private final ArrayList<Byte> bytecode = new ArrayList<>();
    private final ArrayList<Value> args = new ArrayList<>();
    private int nextIndex;
    
    public SnippetFactory() {
        this.nextIndex = 1; 
    }
    
    public SnippetFactory(ClassFile cf) {
        this.nextIndex = cf.constantPoolSize() + 1; 
    }
    
    private void addIndex(int index) {
        this.bytecode.add((byte) (index >>> 8));
        this.bytecode.add((byte) (index & 0x0000_0000_0000_00FF));
    }
    
    private <V> void addConstantPoolItem(Map<Integer, V> map, Map<V, Integer> mapInverse, V value) {
    	if (mapInverse.containsKey(value)) {
    		final int index = mapInverse.get(value);
    		addIndex(index);
    	} else {
    		map.put(this.nextIndex, value);
    		mapInverse.put(value, this.nextIndex);
    		addIndex(this.nextIndex);
    		++this.nextIndex;
    	}
    }
    
    private void addSignature(Signature sig) {
    	addConstantPoolItem(this.signatures, this.signaturesInverse, sig);
    }
    
    private void addInteger(int value) {
    	addConstantPoolItem(this.integers, this.integersInverse, value);
    }
    
    private void addFloat(float value) {
    	addConstantPoolItem(this.floats, this.floatsInverse, value);
    }
    
    private void addLong(long value) {
    	addConstantPoolItem(this.longs, this.longsInverse, value);
    }
    
    private void addDouble(double value) {
    	addConstantPoolItem(this.doubles, this.doublesInverse, value);
    }
    
    private void addString(String value) {
    	addConstantPoolItem(this.strings, this.stringsInverse, value);
    }
    
    private void addClass(String value) {
    	addConstantPoolItem(this.classes, this.classesInverse, value);
    }
    
    public SnippetFactory addArg(Value arg) {
    	this.args.add(arg);
    	return this;
    }
    
    public SnippetFactory op_aload(byte index) {
        this.bytecode.add(OP_ALOAD);
        this.bytecode.add(index);
        return this;
    }
    
    public SnippetFactory op_dload(byte index) {
        this.bytecode.add(OP_DLOAD);
        this.bytecode.add(index);
        return this;
    }
    
    public SnippetFactory op_fload(byte index) {
        this.bytecode.add(OP_FLOAD);
        this.bytecode.add(index);
        return this;
    }
    
    public SnippetFactory op_iload(byte index) {
        this.bytecode.add(OP_ILOAD);
        this.bytecode.add(index);
        return this;
    }
    
    public SnippetFactory op_lload(byte index) {
        this.bytecode.add(OP_LLOAD);
        this.bytecode.add(index);
        return this;
    }
    
    public SnippetFactory op_ldc(int intConst) {
        this.bytecode.add(OP_LDC_W);
        addInteger(intConst);
        return this;
    }
    
    public SnippetFactory op_ldc(float floatConst) {
        this.bytecode.add(OP_LDC_W);
        addFloat(floatConst);
        return this;
    }
    
    public SnippetFactory op_ldc(long longConst) {
        this.bytecode.add(OP_LDC2_W);
        addLong(longConst);
        return this;
    }
    
    public SnippetFactory op_ldc(double doubleConst) {
        this.bytecode.add(OP_LDC2_W);
        addDouble(doubleConst);
        return this;
    }
    
    public SnippetFactory op_ldc_String(String stringConst) {
        this.bytecode.add(OP_LDC_W);
        addString(stringConst);
        return this;
    }
    
    public SnippetFactory op_ldc_Class(String classConst) {
        this.bytecode.add(OP_LDC_W);
        addClass(classConst);
        return this;
    }
    
    public SnippetFactory op_dup() {
        this.bytecode.add(OP_DUP);
        return this;
    }
    
    public SnippetFactory op_pop() {
        this.bytecode.add(OP_POP);
        return this;
    }
    
    private void op_invoke(byte bytecode, Signature methodSignature) {
        this.bytecode.add(bytecode);
        addSignature(methodSignature);
    }
    
    public SnippetFactory op_invokehandle(Signature methodSignature) {
        op_invoke(OP_INVOKEHANDLE, methodSignature);
        return this;
    }
    
    public SnippetFactory op_invokeinterface(Signature methodSignature) {
        op_invoke(OP_INVOKEINTERFACE, methodSignature);
        this.bytecode.add((byte) 1);
        this.bytecode.add((byte) 0);
        return this;
    }
    
    public SnippetFactory op_invokespecial(Signature methodSignature) {
        op_invoke(OP_INVOKESPECIAL, methodSignature);
        return this;
    }
    
    public SnippetFactory op_invokestatic(Signature methodSignature) {
        op_invoke(OP_INVOKESTATIC, methodSignature);
        return this;
    }
    
    public SnippetFactory op_invokevirtual(Signature methodSignature) {
        op_invoke(OP_INVOKEVIRTUAL, methodSignature);
        return this;
    }
    
    public SnippetFactory op_return() {
        this.bytecode.add(OP_RETURN);
        return this;
    }
    
    public SnippetFactory op_areturn() {
        this.bytecode.add(OP_ARETURN);
        return this;
    }
    
    public SnippetFactory op_dreturn() {
        this.bytecode.add(OP_DRETURN);
        return this;
    }
    
    public SnippetFactory op_freturn() {
        this.bytecode.add(OP_FRETURN);
        return this;
    }
    
    public SnippetFactory op_ireturn() {
        this.bytecode.add(OP_IRETURN);
        return this;
    }
    
    public SnippetFactory op_lreturn() {
        this.bytecode.add(OP_LRETURN);
        return this;
    }
    
    public Snippet mk() {
        //no way to do it with streams or other conversion functions
        final byte[] bytecode = new byte[this.bytecode.size()];
        for (int i = 0; i < bytecode.length; ++i) {
            bytecode[i] = this.bytecode.get(i).byteValue();
        }
        return new Snippet(this.signatures, this.integers, this.floats, this.longs, 
        		this.doubles, this.strings, this.classes, this.args, bytecode);
    }
}
