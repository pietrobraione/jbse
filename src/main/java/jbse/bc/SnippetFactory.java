package jbse.bc;

import static jbse.bc.Opcodes.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SnippetFactory {
    private final HashMap<Integer, ConstantPoolValue> constants = new HashMap<>();
    private final HashMap<Integer, Signature> signatures = new HashMap<>();
    private final HashMap<Integer, String> classes = new HashMap<>();
    private ArrayList<Byte> bytecode = new ArrayList<>();
    private int nextIndex;
    
    public SnippetFactory() {
        this.nextIndex = 1; 
    }
    
    private void addIndex() {
        this.bytecode.add((byte) (this.nextIndex >>> 8));
        this.bytecode.add((byte) (this.nextIndex & 0x0000_0000_0000_00FF));
        ++this.nextIndex;
    }
    
    private void addSignature(Signature sig) {
        this.signatures.put(this.nextIndex, sig);
        addIndex();
    }
    
    public SnippetFactory op_dup() {
        this.bytecode.add(OP_DUP);
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
    
    public SnippetFactory op_pop() {
        this.bytecode.add(OP_POP);
        return this;
    }
    
    public SnippetFactory op_return() {
        this.bytecode.add(OP_RETURN);
        return this;
    }
    
    public Snippet mk() {
        //no way to do it with streams or other conversion functions
        final byte[] code = new byte[this.bytecode.size()];
        for (int i = 0; i < code.length; ++i) {
            code[i] = this.bytecode.get(i).byteValue();
        }
        return new Snippet(this.constants, this.signatures, this.classes, code);
    }
}
