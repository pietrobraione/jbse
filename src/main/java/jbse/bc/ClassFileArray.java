package jbse.bc;

import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

public class ClassFileArray extends ClassFile {
	//TODO by now clone is treated as a native method; implement it.
	private static final int METHOD_CLONE_INDEX = 99;
	private static final String METHOD_CLONE_NAME = "clone";
	private static final String METHOD_CLONE_DESCRIPTOR = "()Ljava/lang/Object;";
	private static final String NO_CONSTANT_POOL = "Array classes have no constant pool.";	
	
	public static enum Visibility {PUBLIC, PACKAGE};
	
	private final String className;
	private final String packageName;
	private final Visibility visibility;
	private final Signature signatureCloneMethod;
	
	ClassFileArray(String className, String packageName, Visibility visibility) { 
		this.className = className; 
		this.packageName = packageName;
		this.visibility = visibility;
		this.signatureCloneMethod = new Signature(this.className, METHOD_CLONE_DESCRIPTOR, METHOD_CLONE_NAME);
	}
	
	@Override
	public String getSourceFile() {
	    return "";
	}

	@Override
	public String getPackageName() {
		return this.packageName;
	}
	
	@Override
	public boolean isArray() {
	    return true;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public String getClassName() {
		return this.className;
	}

	@Override
	public String getClassSignature(int classRef) throws InvalidIndexException {
		throw new InvalidIndexException(NO_CONSTANT_POOL);
	}

	@Override
	public int getCodeLength(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}

	@Override
	public ExceptionTable getExceptionTable(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
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
	public Signature getInterfaceMethodSignature(int methodRef)
	throws InvalidIndexException {
		throw new InvalidIndexException(NO_CONSTANT_POOL);
	}

	@Override
	public int getLocalVariableLength(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}

	@Override
	public LocalVariableTable getLocalVariableTable(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}

	@Override
	public byte[] getMethodCodeBySignature(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}

	@Override
	public Signature getMethodSignature(int methodRef)
	throws InvalidIndexException {
		if (methodRef == METHOD_CLONE_INDEX) {
			return this.signatureCloneMethod;
		}
		throw new InvalidIndexException(NO_CONSTANT_POOL);
	}

	@Override
	public String getSuperClassName() {
		return JAVA_OBJECT;
	}

	@Override
	public List<String> getSuperInterfaceNames() {
		final List<String> superinterfaces = new ArrayList<String>(2);
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
	public boolean hasMethodImplementation(Signature methodSignature) {
		return isMethodClone(methodSignature);
	}

	@Override
	public boolean isAbstract() {
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
			return true;
		}
		throw new MethodNotFoundException(methodSignature.toString());
	}

	@Override
	public Signature[] getMethodSignatures() {
		Signature[] retVal = new Signature[1];
		retVal[0] = this.signatureCloneMethod;
		return retVal;
	}

	@Override
	public Annotation[] getMethodAvailableAnnotations(Signature methodSignature)
	throws MethodNotFoundException {
		if (isMethodClone(methodSignature)) {
			return new Annotation[0];
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
			return this.isPublic();
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
			return this.isPackage();
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
		return this.visibility == Visibility.PUBLIC;
	}

	@Override
	public boolean isPackage() {
		return this.visibility == Visibility.PACKAGE;
	}

	@Override
	public boolean isSuperInvoke() {
		//TODO check this!
		return false;
	}

	@Override
	public boolean hasFieldDeclaration(Signature fieldSignature) {
		return false;
	}
	
	private boolean isMethodClone(Signature methodSignature) {
		return (methodSignature.getName().equals(METHOD_CLONE_NAME) &&
				methodSignature.getDescriptor().equals(METHOD_CLONE_DESCRIPTOR));
	}

	@Override
	public LineNumberTable getLineNumberTable(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException {
		if (isMethodClone(methodSignature)) {
			throw new MethodCodeNotFoundException(methodSignature.toString());
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
	public boolean isNested() {
		return false;
	}

	@Override
	public String classContainer() {
		return null;
	}

	@Override
	public boolean isStatic() {
		return true;
	}
}
