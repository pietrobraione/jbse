package jbse.bc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

/**
 * A {@link ClassFile} that wraps another one at the purpose of 
 * extending its constant pool.
 * 
 * @author Pietro Braione
 *
 */
public class ClassFileWrapper extends ClassFile {
    private final ClassFile toWrap; 
    final HashMap<Integer, ConstantPoolValue> constants;
    final HashMap<Integer, Signature> signatures;
    final HashMap<Integer, String> classes;
    
    ClassFileWrapper(ClassFile toWrap,
                     Map<Integer, ConstantPoolValue> constants, 
                     Map<Integer, Signature> signatures,
                     Map<Integer, String> classes) {
        this.toWrap = toWrap;
        this.constants = new HashMap<>(constants);
        this.signatures = new HashMap<>(signatures);
        this.classes = new HashMap<>(classes);
    }
    
    public ClassFile getWrapped() {
        return this.toWrap;
    }
    
    @Override
    public String getSourceFile() {
        return this.toWrap.getSourceFile();
    }

    @Override
    public int getModifiers() {
        return this.toWrap.getModifiers();
    }

    @Override
    public int getAccessFlags() {
        return this.toWrap.getAccessFlags();
    }

    @Override
    public boolean isArray() {
        return this.toWrap.isArray();
    }

    @Override
    public boolean isEnum() {
        return this.toWrap.isEnum();
    }

    @Override
    public boolean isPrimitive() {
        return this.toWrap.isPrimitive();
    }

    @Override
    public boolean isInterface() {
        return this.toWrap.isInterface();
    }

    @Override
    public boolean isAbstract() {
        return this.toWrap.isAbstract();
    }

    @Override
    public boolean isPublic() {
        return this.toWrap.isPublic();
    }

    @Override
    public boolean isProtected() {
        return this.toWrap.isProtected();
    }

    @Override
    public boolean isPackage() {
        return this.toWrap.isPackage();
    }

    @Override
    public boolean isPrivate() {
        return this.toWrap.isPrivate();
    }

    @Override
    public boolean isSuperInvoke() {
        return this.toWrap.isSuperInvoke();
    }

    @Override
    public boolean isLocal() {
        return this.toWrap.isLocal();
    }

    @Override
    public boolean isAnonymous() {
        return this.toWrap.isAnonymous();
    }

    @Override
    public String classContainer() throws ClassFileNotFoundException {
        return this.toWrap.classContainer();
    }

    @Override
    public Signature getEnclosingMethodOrConstructor() throws ClassFileNotFoundException {
        return this.toWrap.getEnclosingMethodOrConstructor();
    }

    @Override
    public boolean isStatic() {
        return this.toWrap.isStatic();
    }
    
    @Override
    public int constantPoolSize() {
        return this.toWrap.constantPoolSize() + this.constants.size() + this.signatures.size() + this.classes.size();
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return this.toWrap.hasMethodImplementation(methodSignature);
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return this.toWrap.hasMethodDeclaration(methodSignature);
    }

    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        return this.toWrap.hasOneSignaturePolymorphicMethodDeclaration(methodName);
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodAbstract(methodSignature);
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodStatic(methodSignature);
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodPublic(methodSignature);
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodProtected(methodSignature);
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodPackage(methodSignature);
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodPrivate(methodSignature);
    }

    @Override
    public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodNative(methodSignature);
    }

    @Override
    public boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodVarargs(methodSignature);
    }

    @Override
    public boolean isMethodSignaturePolymorphic(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.isMethodSignaturePolymorphic(methodSignature);
    }
    
    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature) throws ClassFileNotFoundException, MethodNotFoundException {
        return this.toWrap.isMethodCallerSensitive(methodSignature);
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.getMethodGenericSignatureType(methodSignature);
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.getMethodModifiers(methodSignature);
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.getMethodAnnotationsRaw(methodSignature);
    }

    @Override
    public Object[] getMethodAvailableAnnotations(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.getMethodAvailableAnnotations(methodSignature);
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) throws MethodNotFoundException {
        return this.toWrap.getMethodThrownExceptions(methodSignature);
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getExceptionTable(methodSignature);
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getLocalVariableTable(methodSignature);
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getLineNumberTable(methodSignature);
    }

    @Override
    public ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException {
        if (index < this.toWrap.constantPoolSize()) {
            return this.toWrap.getValueFromConstantPool(index);
        } else if (this.constants.containsKey(index)) {
            return this.constants.get(index);
        } else {
            throw new InvalidIndexException("index " + index + " not set");
        }
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getMethodCodeBySignature(methodSignature);
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
        return this.toWrap.hasFieldDeclaration(fieldSignature);
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldFinal(fieldSignature);
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldPublic(fieldSignature);
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldProtected(fieldSignature);
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldPackage(fieldSignature);
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldPrivate(fieldSignature);
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.isFieldStatic(fieldSignature);
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.hasFieldConstantValue(fieldSignature);
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.getFieldGenericSignatureType(fieldSignature);
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.getFieldModifiers(fieldSignature);
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) throws FieldNotFoundException {
        return this.toWrap.getFieldAnnotationsRaw(fieldSignature);
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature)
    throws FieldNotFoundException, AttributeNotFoundException {
        return this.toWrap.fieldConstantValueIndex(fieldSignature);
    }
    
    @Override
    public Signature[] getDeclaredFieldsNonStatic() {
        return this.toWrap.getDeclaredFieldsNonStatic();
    }

    @Override
    public Signature[] getDeclaredFieldsStatic() {
        return this.toWrap.getDeclaredFieldsStatic();
    }

    @Override
    public Signature[] getDeclaredFields() {
        return this.toWrap.getDeclaredFields();
    }

    @Override
    public Signature getFieldSignature(int fieldRef) throws InvalidIndexException {
        if (fieldRef < this.toWrap.constantPoolSize()) {
            return this.toWrap.getFieldSignature(fieldRef);
        } else if (this.signatures.containsKey(fieldRef)) {
            return this.signatures.get(fieldRef);
        } else {
            throw new InvalidIndexException("index " + fieldRef + " not set");
        }
    }

    @Override
    public Signature[] getDeclaredConstructors() {
        return this.toWrap.getDeclaredConstructors();
    }

    @Override
    public Signature[] getDeclaredMethods() {
        return this.toWrap.getDeclaredMethods();
    }

    @Override
    public Signature getMethodSignature(int methodRef) throws InvalidIndexException {
        if (methodRef < this.toWrap.constantPoolSize()) {
            return this.toWrap.getMethodSignature(methodRef);
        } else if (this.signatures.containsKey(methodRef)) {
            return this.signatures.get(methodRef);
        } else {
            throw new InvalidIndexException("index " + methodRef + " not set");
        }
    }

    @Override
    public Signature getInterfaceMethodSignature(int methodRef) throws InvalidIndexException {
        if (methodRef < this.toWrap.constantPoolSize()) {
            return this.toWrap.getInterfaceMethodSignature(methodRef);
        } else if (this.signatures.containsKey(methodRef)) {
            return this.signatures.get(methodRef);
        } else {
            throw new InvalidIndexException("index " + methodRef + " not set");
        }
    }

    @Override
    public String getClassSignature(int classRef) throws InvalidIndexException {
        if (classRef < this.toWrap.constantPoolSize()) {
            return this.toWrap.getClassSignature(classRef);
        } else if (this.classes.containsKey(classRef)) {
            return this.classes.get(classRef);
        } else {
            throw new InvalidIndexException("index " + classRef + " not set");
        }
    }

    @Override
    public String getSuperclassName() {
        return this.toWrap.getSuperclassName();
    }
    
    @Override
    public List<String> getSuperInterfaceNames() {
        return this.toWrap.getSuperInterfaceNames();
    }

    @Override
    public String getClassName() {
        return this.toWrap.getClassName();
    }

    @Override
    public int getLocalVariableLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getLocalVariableLength(methodSignature);
    }

    @Override
    public int getCodeLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        return this.toWrap.getCodeLength(methodSignature);
    }
}
