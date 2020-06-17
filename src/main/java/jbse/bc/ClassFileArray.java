package jbse.bc;

import static jbse.bc.Opcodes.OP_ACONST_NULL;
import static jbse.bc.Opcodes.OP_ALOAD_0;
import static jbse.bc.Opcodes.OP_ARETURN;
import static jbse.bc.Opcodes.OP_CHECKCAST;
import static jbse.bc.Opcodes.OP_INVOKESPECIAL;
import static jbse.bc.Opcodes.OP_POP;
import static jbse.bc.Signatures.CLONE_NOT_SUPPORTED_EXCEPTION;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_OBJECT_CLONE;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;

/**
 * {@link ClassFile} for array classes.
 * 
 * @author Pietro Braione
 */
public final class ClassFileArray extends ClassFile {
    /* TODO it is unclear how to treat the method clone; since it has greater
     * visibility than java.lang.Object.clone, we consider the clone method of
     * array classes as an overridden implementation.
     */
    private static final byte[] METHOD_CLONE_BYTECODE = {
      OP_ALOAD_0,
      OP_INVOKESPECIAL, (byte) 0, (byte) 1,
      OP_CHECKCAST,     (byte) 0, (byte) 2,
      OP_ARETURN,
      OP_POP,
      OP_ACONST_NULL,
      OP_ARETURN
    };
    private static final ExceptionTable METHOD_CLONE_EXCEPTIONTABLE = new ExceptionTable(1);
    
    static {
        METHOD_CLONE_EXCEPTIONTABLE.addEntry(new ExceptionTableEntry(0, 7, 8, CLONE_NOT_SUPPORTED_EXCEPTION));
    }
    
    private static final String NO_CONSTANT_POOL = "no member of this kind is in the constant pool of an array class";
    
    private static enum Accessibility {
        PUBLIC(Modifier.PUBLIC), 
        PROTECTED(Modifier.PROTECTED), 
        PACKAGE(0), 
        PRIVATE(Modifier.PRIVATE);
        
        private final int modifier;
        private Accessibility(int modifier) { this.modifier = modifier; }
    };

    private final String className;
    private final ClassFile memberClass;
    private final ClassFile cf_JAVA_OBJECT;
    private final ClassFile cf_JAVA_CLONEABLE;
    private final ClassFile cf_JAVA_SERIALIZABLE;
    private final int definingClassLoader;
    private final String packageName;
    private final Accessibility accessibility;
    private final Signature signatureCloneMethod;

    ClassFileArray(String className, ClassFile memberClass, ClassFile cf_JAVA_OBJECT, ClassFile cf_JAVA_CLONEABLE, ClassFile cf_JAVA_SERIALIZABLE) { 
        this.className = className; 
        this.memberClass = memberClass;
        this.cf_JAVA_OBJECT = cf_JAVA_OBJECT;
        this.cf_JAVA_CLONEABLE = cf_JAVA_CLONEABLE;
        this.cf_JAVA_SERIALIZABLE = cf_JAVA_SERIALIZABLE;
        
        //calculates accessibility (JVMS v8, section 5.3.3 last line, this
        //implementation also works with primitive classfile members
        //because they all have public visibility when queried)
        if (memberClass.isPublic()) {
            this.accessibility = Accessibility.PUBLIC;
        } else if (memberClass.isProtected()) {
            this.accessibility = Accessibility.PROTECTED;
        } else if (memberClass.isPackage()) {
            this.accessibility = Accessibility.PACKAGE;
        } else { //private
            this.accessibility = Accessibility.PRIVATE;
        }
        
        //calculates dynamic package (defining classloader and package name);
        //note that this complements the value of this.accessibility in 
        //determining what this class may access and which classes may access
        //this class.
        //TODO the JVMS v8 is not clear about that. Is it ok? Does it work for nested classes?
        this.definingClassLoader = memberClass.getDefiningClassLoader();
        this.packageName = memberClass.getPackageName();

        this.signatureCloneMethod = new Signature(this.className, JAVA_OBJECT_CLONE.getDescriptor(), JAVA_OBJECT_CLONE.getName());
    }

    private boolean isMethodClone(Signature methodSignature) {
        return (JAVA_OBJECT_CLONE.getName().equals(methodSignature.getName()) &&
                JAVA_OBJECT_CLONE.getDescriptor().equals(methodSignature.getDescriptor()));
    }
    
    @Override
    public byte[] getBinaryFileContent() {
        return null;
    }

    @Override
    public String getSourceFile() {
        return "";
    }
    
    @Override
    public int getMajorVersion() {
        return JAVA_8;
    }
    
    @Override
    public int getMinorVersion() {
        return 0;
    }
    
    @Override
    public int getDefiningClassLoader() {
        return this.definingClassLoader;
    }

    @Override
    public String getPackageName() {
        return this.packageName;
    }

    @Override
    public String getGenericSignatureType() {
    	return null;
    }
    
    @Override
    public int getModifiers() {
        return Modifier.ABSTRACT | Modifier.FINAL | this.accessibility.modifier;
    }

    @Override
    public int getAccessFlags() {
        return 0; //no access flags set, checked against implementation
    }
    
    @Override
    public boolean isDummy() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public boolean isPrimitiveOrVoid() {
        return false;
    }
    
    @Override
    public int constantPoolSize() {
        return 1;  //empty constant pool
    }

    @Override
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public void rename(String classNameNew) throws RenameUnsupportedException {
    	throw new RenameUnsupportedException();
    }
    
    @Override
    public String getInternalTypeName() {
        return getClassName();
    }

    @Override
    public String getClassSignature(int classRef) throws InvalidIndexException {
        if (classRef == 2) {
            return this.className;
        }
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public int getCodeLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return METHOD_CLONE_BYTECODE.length;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return METHOD_CLONE_EXCEPTIONTABLE;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Signature getFieldSignature(int fieldRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public Signature[] getDeclaredFieldsNonStatic() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredFieldsStatic() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredFields() {
        return new Signature[0];
    }

    @Override
    public Signature[] getDeclaredConstructors() {
        return new Signature[0];
    }

    @Override
    public Signature getInterfaceMethodSignature(int methodRef)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public int getLocalVariableLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return 2;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return defaultLocalVariableTable(this.signatureCloneMethod);
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public LocalVariableTable getLocalVariableTypeTable(Signature methodSignature)
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return new LocalVariableTable(0);
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return METHOD_CLONE_BYTECODE;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Signature getMethodSignature(int methodRef)
    throws InvalidIndexException {
        if (methodRef == 1) {
            return JAVA_OBJECT_CLONE;
        }
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }
    
    @Override
    public ClassFile getSuperclass() {
        return this.cf_JAVA_OBJECT;
    }

    @Override
    public String getSuperclassName() {
        return JAVA_OBJECT;
    }
    
    @Override
    public List<ClassFile> getSuperInterfaces() {
        final List<ClassFile> superinterfaces = new ArrayList<>(2);
        superinterfaces.add(this.cf_JAVA_CLONEABLE);
        superinterfaces.add(this.cf_JAVA_SERIALIZABLE);
        return Collections.unmodifiableList(superinterfaces);
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        final List<String> superinterfaces = new ArrayList<>(2);
        superinterfaces.add(JAVA_CLONEABLE);
        superinterfaces.add(JAVA_SERIALIZABLE);
        return Collections.unmodifiableList(superinterfaces);
    }

    @Override
    public ConstantPoolValue getValueFromConstantPool(int index)
    throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return isMethodClone(methodSignature);
    }
    
    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        return false;
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return isMethodClone(methodSignature);
    }

    @Override
    public boolean isAbstract() {
        return true;
    }
    
    @Override
    public boolean isFinal() {
    	return true;
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature)
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature)
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodVarargs(Signature methodSignature)
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodFinal(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return true;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodSignaturePolymorphic(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Signature[] getDeclaredMethods() {
        final Signature[] retVal = new Signature[1];
        retVal[0] = this.signatureCloneMethod;
        return retVal;
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return null;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return Modifier.PUBLIC;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return new byte[0];
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public String[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return new String[0];
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation, String parameter) {
        return null;
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return new String[0];
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
    }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return true;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
        if (isMethodClone(methodSignature)) {
            return false;
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isPublic() {
        return this.accessibility == Accessibility.PUBLIC;
    }
    
    @Override
    public boolean isProtected() {
        return this.accessibility == Accessibility.PROTECTED;
    }

    @Override
    public boolean isPackage() {
        return this.accessibility == Accessibility.PACKAGE;
    }
    
    @Override
    public boolean isPrivate() {
        return this.accessibility == Accessibility.PRIVATE;
    }

    @Override
    public boolean isSuperInvoke() {
        //TODO check this!
        return false;
    }
    
    @Override
    public boolean isLocal() {
        return false;
    }
    
    @Override
    public boolean isAnonymous() {
        return false;
    }
    
    @Override
    public byte[] getClassAnnotationsRaw() {
        return new byte[0];
    }
    
    @Override
    public ClassFile getMemberClass() {
        return this.memberClass;
    }
    
    @Override
    public boolean isAnonymousUnregistered() {
        return false;
    }
    
    @Override
    public ClassFile getHostClass() {
        return null;
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
        return false;
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        if (isMethodClone(methodSignature)) {
            return defaultLineNumberTable();
        }
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) throws FieldNotFoundException {
        throw new FieldNotFoundException(fieldSignature.toString());
    }

    @Override
    public String classContainer() {
        return null;
    }
    
    @Override
    public Signature getEnclosingMethodOrConstructor() {
        return null;
    }

    @Override
    public boolean isStatic() {
        return true;
    }
}
