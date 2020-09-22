package jbse.mem;

import java.util.Collections;
import java.util.List;

import jbse.bc.CallSiteSpecifier;
import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolValue;
import jbse.bc.ExceptionTable;
import jbse.bc.LineNumberTable;
import jbse.bc.LocalVariableTable;
import jbse.bc.Signature;
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.exc.InvalidTypeException;

/**
 * Class that implements {@link Instance_METALEVELBOX}. 
 */
public final class InstanceImpl_METALEVELBOX extends InstanceImpl implements Instance_METALEVELBOX {
	/** The encapsulated object. */
	private final Object it;

	protected InstanceImpl_METALEVELBOX(Calculator calc, HistoryPoint epoch, Object it) throws InvalidTypeException {
		super(calc, false, new ClassFileFoo(), null, epoch, 0);
		this.it = it;
	}

	@Override
	InstanceWrapper<? extends InstanceImpl> makeWrapper(Heap destinationHeap, long destinationPosition) {
		return new InstanceWrapper_METALEVELBOX(destinationHeap, destinationPosition, this);
	}
	
	@Override
	public Object get() {
		return this.it;
	}

	@Override
	public InstanceImpl_METALEVELBOX clone() {
		return (InstanceImpl_METALEVELBOX) super.clone();
	}

	private static class ClassFileFoo extends ClassFile {
		@Override
		public byte[] getBinaryFileContent() {
			return null;
		}

		@Override
		public String getSourceFile() {
			return null;
		}

		@Override
		public int getMajorVersion() {
			return JAVA_1;
		}

		@Override
		public int getMinorVersion() {
			return 0;
		}

		@Override
		public String getClassName() {
			return null;
		}

		@Override
		public void rename(String classNameNew) throws RenameUnsupportedException {
			throw new RenameUnsupportedException();
		}

		@Override
		public String getInternalTypeName() {
			return null;
		}

		@Override
		public int getDefiningClassLoader() {
			return 0;
		}

		@Override
		public String getPackageName() {
			return null;
		}

		@Override
		public String getGenericSignatureType() {
			return null;
		}

		@Override
		public int getModifiers() {
			return 0;
		}

		@Override
		public int getAccessFlags() {
			return 0;
		}

		@Override
		public boolean isDummy() {
			return false;
		}

		@Override
		public boolean isArray() {
			return false;
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
		public boolean isInterface() {
			return false;
		}

		@Override
		public boolean isAbstract() {
			return false;
		}

		@Override
		public boolean isFinal() {
			return false;
		}

		@Override
		public boolean isPublic() {
			return false;
		}

		@Override
		public boolean isProtected() {
			return false;
		}

		@Override
		public boolean isPackage() {
			return false;
		}

		@Override
		public boolean isPrivate() {
			return false;
		}

		@Override
		public boolean isStatic() {
			return false;
		}

		@Override
		public boolean isSuperInvoke() {
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
			return null;
		}

		@Override
		public ClassFile getMemberClass() {
			return null;
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
		public String classContainer() {
			return null;
		}

		@Override
		public Signature getEnclosingMethodOrConstructor() {
			return null;
		}

		@Override
		public int constantPoolSize() {
			return 0;
		}

		@Override
		public boolean hasMethodDeclaration(Signature methodSignature) {
			return false;
		}

		@Override
		public boolean hasMethodImplementation(Signature methodSignature) {
			return false;
		}

		@Override
		public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
			return false;
		}

		@Override
		public boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
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
		public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public boolean isMethodFinal(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public boolean isMethodCallerSensitive(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public int getMethodModifiers(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public byte[] getMethodAnnotationsRaw(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public String[] getMethodAvailableAnnotations(Signature methodSignature) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation,
		String parameter) throws MethodNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
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
		public ExceptionTable getExceptionTable(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException, InvalidIndexException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public LocalVariableTable getLocalVariableTable(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public LocalVariableTable getLocalVariableTypeTable(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public LineNumberTable getLineNumberTable(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public int getLocalVariableTableLength(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public int getCodeLength(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public byte[] getMethodCodeBySignature(Signature methodSignature)
		throws MethodNotFoundException, MethodCodeNotFoundException {
			throw new MethodNotFoundException(methodSignature.toString());
		}

		@Override
		public ConstantPoolValue getValueFromConstantPool(int index)
		throws InvalidIndexException, ClassFileIllFormedException {
			throw new InvalidIndexException("" + index);
		}

		@Override
		public boolean hasFieldDeclaration(Signature fieldSignature) {
			return false;
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
		public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
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
		public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
			throw new FieldNotFoundException(fieldSignature.toString());
		}

		@Override
		public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
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
		public int fieldConstantValueIndex(Signature fieldSignature)
		throws FieldNotFoundException, AttributeNotFoundException {
			throw new FieldNotFoundException(fieldSignature.toString());
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
		public Signature getFieldSignature(int fieldRef) throws InvalidIndexException {
			throw new InvalidIndexException("" + fieldRef);
		}

		@Override
		public Signature[] getDeclaredConstructors() {
			return new Signature[0];
		}

		@Override
		public Signature[] getDeclaredMethods() {
			return new Signature[0];
		}

		@Override
		public Signature getMethodSignature(int methodRef) throws InvalidIndexException {
			throw new InvalidIndexException("" + methodRef);
		}

		@Override
		public Signature getInterfaceMethodSignature(int methodRef) throws InvalidIndexException {
			throw new InvalidIndexException("" + methodRef);
		}

		@Override
		public String getClassSignature(int classRef) throws InvalidIndexException {
			throw new InvalidIndexException("" + classRef);
		}
		
		@Override
		public CallSiteSpecifier getCallSiteSpecifier(int callSiteSpecifierIndex)
		throws InvalidIndexException {
			throw new InvalidIndexException("" + callSiteSpecifierIndex);
		}

		@Override
		public ClassFile getSuperclass() {
			return null;
		}

		@Override
		public String getSuperclassName() {
			return null;
		}

		@Override
		public List<ClassFile> getSuperInterfaces() {
			return Collections.emptyList();
		}

		@Override
		public List<String> getSuperInterfaceNames() {
			return Collections.emptyList();
		}
	}
}
