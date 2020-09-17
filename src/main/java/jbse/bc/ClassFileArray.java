package jbse.bc;

import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
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
    private static final String NO_CONSTANT_POOL = "No member of this kind is in the constant pool of an array class.";
    
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
    public CallSiteSpecifier getCallSiteSpecifier(int callSiteSpecifierIndex) throws InvalidIndexException {
        throw new InvalidIndexException(NO_CONSTANT_POOL);
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
    public ParameterInfo[] getMethodParameters(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getCodeLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public LocalVariableTable getLocalVariableTypeTable(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getLocalVariableTableLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Signature getMethodSignature(int methodRef)
    throws InvalidIndexException {
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
        return false;
    }
    
    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        return false;
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return false;
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
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodVarargs(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodFinal(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public Signature[] getDeclaredMethods() {
        final Signature[] retVal = new Signature[0];
        return retVal;
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) 
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public String[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }
    
    @Override
    public String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation, String parameter) {
        return null;
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        throw new MethodNotFoundException(methodSignature.toString());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
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
