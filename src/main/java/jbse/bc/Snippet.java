package jbse.bc;

import java.util.Map;

public final class Snippet {
    private final Map<Integer, ConstantPoolValue> constants;
    private final Map<Integer, Signature> signatures;
    private final Map<Integer, String> classes;
    private byte[] bytecode;
    
    Snippet(Map<Integer, ConstantPoolValue> constants, Map<Integer, Signature> signatures, Map<Integer, String> classes, byte[] bytecode) {
        this.constants = constants;
        this.signatures = signatures;
        this.classes = classes;
        this.bytecode = bytecode;
    }
    
    public Map<Integer, ConstantPoolValue> getConstants() {
        return this.constants;
    }
    
    public Map<Integer, Signature> getSignatures() {
        return this.signatures;
    }
    
    public Map<Integer, String> getClasses() {
        return this.classes;
    }
    
    public byte[] getBytecode() {
        return this.bytecode;
    }
}
