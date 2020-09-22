package jbse.bc;

import java.util.List;

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * A {@link ClassFile} for snippets that wraps another {@link ClassFile}, 
 * extending its constant pool.
 * 
 * @author Pietro Braione
 */
public class ClassFileSnippetWrap extends ClassFile {
	final Snippet snippet;
    final ClassFile component;
    
    /**
     * Constructor.
     * 
     * @param snippet a {@link Snippet}.
     * @param component a {@link ClassFile}. It must not be the classfile
     *        for a primitive type, an array, or another wrapping snippet
     * @throws InvalidInputException if {@code component} is the classfile
     *        for a primitive type, an array, or another snippet classfile.
     */
    public ClassFileSnippetWrap(Snippet snippet, ClassFile component) throws InvalidInputException {
    	if (component.isPrimitiveOrVoid() || component.isArray() || component.getClass() == ClassFileSnippetWrap.class || component.getClass() == ClassFileSnippetNoWrap.class) {
    		throw new InvalidInputException("Tried to create a snippet classfile wrapping another snippet classfile with class "  + component.getClassName() + ".");
    	}
        this.snippet = snippet;
        this.component = component;
    }
    
    /**
     * {@inheritDoc}
     * 
     * Beware! The method returns the binary content of the
     * component classfile, <em>without</em> the constant pool
     * elements added by the snippet.
     */
    @Override
    public byte[] getBinaryFileContent() {
        return this.component.getBinaryFileContent();
    }

    @Override
    public String getSourceFile() {
        return this.component.getSourceFile();
    }

    @Override
    public int getMajorVersion() {
        return this.component.getMajorVersion();
    }
    
    @Override
    public int getMinorVersion() {
        return this.component.getMinorVersion();
    }
    
    @Override
    public String getPackageName() {
    	return this.component.getPackageName();
    }
    
    @Override
    public String getClassName() {
        return this.component.getClassName();
    }
    
    @Override
    public void rename(String classNameNew) throws RenameUnsupportedException {
    	this.component.rename(classNameNew);
    }
    
    @Override
    public String getInternalTypeName() {
        return this.component.getInternalTypeName();
    }
    
    @Override
    public int getDefiningClassLoader() {
        return this.component.getDefiningClassLoader();
    }

    @Override
    public String getGenericSignatureType() {
    	return null;
    }
    
    @Override
    public int getModifiers() {
        return this.component.getModifiers();
    }

    @Override
    public int getAccessFlags() {
        return this.component.getAccessFlags();
    }
    
    @Override
    public boolean isDummy() {
        return this.component.isDummy();
    }

    @Override
    public boolean isArray() {
        return this.component.isArray();
    }

    @Override
    public boolean isEnum() {
        return this.component.isEnum();
    }

    @Override
    public boolean isPrimitiveOrVoid() {
        return this.component.isPrimitiveOrVoid();
    }

    @Override
    public boolean isInterface() {
        return this.component.isInterface();
    }

    @Override
    public boolean isAbstract() {
        return this.component.isAbstract();
    }
    
    @Override
    public boolean isFinal() {
    	return this.component.isFinal();
    }

    @Override
    public boolean isPublic() {
        return this.component.isPublic();
    }

    @Override
    public boolean isProtected() {
        return this.component.isProtected();
    }

    @Override
    public boolean isPackage() {
        return this.component.isPackage();
    }

    @Override
    public boolean isPrivate() {
        return this.component.isPrivate();
    }

    @Override
    public boolean isSuperInvoke() {
        return this.component.isSuperInvoke();
    }

    @Override
    public boolean isLocal() {
        return this.component.isLocal();
    }

    @Override
    public boolean isAnonymous() {
        return this.component.isAnonymous();
    }
    
    @Override
    public byte[] getClassAnnotationsRaw() {
        return new byte[0];
    }

    @Override
    public ClassFile getMemberClass() {
        return this.component.getMemberClass();
    }
    
    @Override
    public boolean isAnonymousUnregistered() {
        return this.component.isAnonymousUnregistered();
    }
    
    @Override
    public ClassFile getHostClass() {
        return this.component.getHostClass();
    }

    @Override
    public String classContainer() {
        return this.component.classContainer();
    }

    @Override
    public Signature getEnclosingMethodOrConstructor() {
        return this.component.getEnclosingMethodOrConstructor();
    }

    @Override
    public boolean isStatic() {
        return this.component.isStatic();
    }
    
    @Override
    public int constantPoolSize() {
        return this.component.constantPoolSize() + this.snippet.size();
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        return this.component.hasMethodImplementation(methodSignature);
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return this.component.hasMethodDeclaration(methodSignature);
    }

    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        return this.component.hasOneSignaturePolymorphicMethodDeclaration(methodName);
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodAbstract(methodSignature);
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodStatic(methodSignature);
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodPublic(methodSignature);
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodProtected(methodSignature);
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodPackage(methodSignature);
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodPrivate(methodSignature);
    }

    @Override
    public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodNative(methodSignature);
    }

    @Override
    public boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodVarargs(methodSignature);
    }
    
    @Override
    public boolean isMethodFinal(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodFinal(methodSignature);
    }

    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.isMethodCallerSensitive(methodSignature);
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.getMethodGenericSignatureType(methodSignature);
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.getMethodModifiers(methodSignature);
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.getMethodAnnotationsRaw(methodSignature);
    }

    @Override
    public String[] getMethodAvailableAnnotations(Signature methodSignature) throws MethodNotFoundException {
    	return this.component.getMethodAvailableAnnotations(methodSignature);
    }
    
    @Override
    public String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation, String parameter) 
    throws MethodNotFoundException {
    	return this.component.getMethodAnnotationParameterValueString(methodSignature, annotation, parameter);
    }
    
    @Override
    public ParameterInfo[] getMethodParameters(Signature methodSignature) 
    throws MethodNotFoundException {
    	return this.component.getMethodParameters(methodSignature);
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
    	return this.component.getMethodThrownExceptions(methodSignature);
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getExceptionTable(methodSignature);
    }

    @Override
    public LocalVariableTable getLocalVariableTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getLocalVariableTable(methodSignature);
    }

    @Override
    public LocalVariableTable getLocalVariableTypeTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getLocalVariableTypeTable(methodSignature);
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getLineNumberTable(methodSignature);
    }

    @Override
    public ConstantPoolValue getValueFromConstantPool(int index) 
    throws InvalidIndexException, ClassFileIllFormedException {
    	if (this.snippet.containsValueFromConstantPool(index)) {
    		try {
    			return this.snippet.getValueFromConstantPool(index);
    		} catch (InvalidIndexException e) {
    			//this should never happen
    			throw new UnexpectedInternalException(e);
    		}
    	} else {
    		return this.component.getValueFromConstantPool(index);
    	}
    }

    @Override
    public byte[] getMethodCodeBySignature(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getMethodCodeBySignature(methodSignature);
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
    	return this.component.hasFieldDeclaration(fieldSignature);
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldFinal(fieldSignature);
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldPublic(fieldSignature);
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldProtected(fieldSignature);
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldPackage(fieldSignature);
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldPrivate(fieldSignature);
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.isFieldStatic(fieldSignature);
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.hasFieldConstantValue(fieldSignature);
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.getFieldGenericSignatureType(fieldSignature);
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.getFieldModifiers(fieldSignature);
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) throws FieldNotFoundException {
    	return this.component.getFieldAnnotationsRaw(fieldSignature);
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature)
    throws FieldNotFoundException, AttributeNotFoundException {
    	return this.component.fieldConstantValueIndex(fieldSignature);
    }
    
    @Override
    public Signature[] getDeclaredFieldsNonStatic() {
    	return this.component.getDeclaredFieldsNonStatic();
    }

    @Override
    public Signature[] getDeclaredFieldsStatic() {
    	return this.component.getDeclaredFieldsStatic();
    }

    @Override
    public Signature[] getDeclaredFields() {
    	return this.component.getDeclaredFields();
    }

    @Override
    public Signature getFieldSignature(int index) throws InvalidIndexException {
    	if (this.snippet.getSignatures().containsKey(index)) {
    		return this.snippet.getSignatures().get(index);
    	} else {
    		return this.component.getFieldSignature(index);
    	}
    }

    @Override
    public Signature[] getDeclaredConstructors() {
    	return this.component.getDeclaredConstructors();
    }

    @Override
    public Signature[] getDeclaredMethods() {
    	return this.component.getDeclaredMethods();
    }

    @Override
    public Signature getMethodSignature(int index) throws InvalidIndexException {
        if (this.snippet.getSignatures().containsKey(index)) {
            return this.snippet.getSignatures().get(index);
        } else {
        	return this.component.getMethodSignature(index);
        }
    }

    @Override
    public Signature getInterfaceMethodSignature(int index) throws InvalidIndexException {
        if (this.snippet.getSignatures().containsKey(index)) {
            return this.snippet.getSignatures().get(index);
        } else {
        	return this.component.getInterfaceMethodSignature(index);
        }
    }

    @Override
    public String getClassSignature(int index) throws InvalidIndexException {
        if (this.snippet.getClasses().containsKey(index)) {
            return this.snippet.getClasses().get(index);
        } else {
        	return this.component.getClassSignature(index);
        }
    }
    
    @Override
    public CallSiteSpecifier getCallSiteSpecifier(int index)
    throws InvalidIndexException, ClassFileIllFormedException {
    	return this.component.getCallSiteSpecifier(index);
    }
    
    @Override
    public ClassFile getSuperclass() {
    	return this.component.getSuperclass();
    }

    @Override
    public String getSuperclassName() {
    	return this.component.getSuperclassName();
    }
    
    @Override
    public List<ClassFile> getSuperInterfaces() {
    	return this.component.getSuperInterfaces();
    }
    
    @Override
    public List<String> getSuperInterfaceNames() {
    	return this.component.getSuperInterfaceNames();
    }

    @Override
    public int getLocalVariableTableLength(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getLocalVariableTableLength(methodSignature);
    }

    @Override
    public int getCodeLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
    	return this.component.getCodeLength(methodSignature);
    }
}
