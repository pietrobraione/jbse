package jbse.bc;

import java.util.Collections;
import java.util.List;

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

public final class ClassFileBad extends ClassFile {
    private final String className;
    private final BadClassFileException e;

    ClassFileBad(String className, BadClassFileException e) {
        this.className = className;
        this.e = e;
    }
    
    BadClassFileException getException() {
        return this.e;
    }
    
    @Override
    public String getSourceFile() {
        return "";
    }

    @Override
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isPackage() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isSuperInvoke() {
        return false;
    }

    @Override
    public boolean isNested() {
        return false;
    }

    @Override
    public String classContainer() {
        return null;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return false;
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return false;
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Object[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException,
    InvalidIndexException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ConstantPoolValue getValueFromConstantPool(int index)
    throws InvalidIndexException {
        throw new InvalidIndexException(this.className + ":" + index);
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getLocalVariableLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getCodeLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
        return false;
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature)
    throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature)
    throws FieldNotFoundException, AttributeNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public Signature[] getFieldsNonStatic() {
        return new Signature[0];
    }

    @Override
    public Signature[] getFieldsStatic() {
        return new Signature[0];
    }

    @Override
    public Signature getFieldSignature(int fieldRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(this.className + ":" + fieldRef);
    }

    @Override
    public Signature getMethodSignature(int methodRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(this.className + ":" + methodRef);
    }

    @Override
    public Signature[] getMethodSignatures() {
        return new Signature[0];
    }

    @Override
    public Signature getInterfaceMethodSignature(int methodRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(this.className + ":" + methodRef);
    }

    @Override
    public String getClassSignature(int classRef) throws InvalidIndexException {
        throw new InvalidIndexException(this.className + ":" + classRef);
    }

    @Override
    public String getSuperClassName() {
        return null;
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        return Collections.emptyList();
    }
}
