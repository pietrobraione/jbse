package jbse.bc;

import java.util.LinkedList;
import java.util.List;





import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
//also uses javassist.bytecode.ExceptionTable, not imported to avoid name clash



import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

public class ClassFileJavassist extends ClassFile {
	private CtClass cls;
	private ConstPool cp;
	
	ClassFileJavassist(String className, ClassPool cpool) throws ClassFileNotFoundException {
		//TODO understand how in Javassist "file not found" is differentiated from "file found and invalid"
		try {
			this.cls = cpool.get(className.replace("/", "."));
			this.cp = this.cls.getClassFile().getConstPool();
		} catch (NotFoundException e) {
			throw new ClassFileNotFoundException(className);
		}
	}

	@Override
	public String getClassName() {
		return this.cls.getName().replace(".", "/");
	}

	@Override
	public String getClassSignature(int classIndex) throws InvalidIndexException {
		if (classIndex < 1 || classIndex > this.cp.getSize()) {
			throw new InvalidIndexException(indexOutOfRangeMessage(classIndex));
		}
		if (this.cp.getTag(classIndex) != ConstPool.CONST_Class) {
			throw new InvalidIndexException(entryInvalidMessage(classIndex));
		}
		return this.cp.getClassInfo(classIndex).replace(".", "/");
	}

	@Override
	public boolean isPublic() {
		return Modifier.isPublic(this.cls.getModifiers());
	}

	@Override
	public boolean isPackage() {
		return Modifier.isPackage(this.cls.getModifiers());
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isSuperInvoke() {
		//note that we use getClassFile().getAccessFlag() because 
		//getModifiers() does not provide the ACC_SUPER flag
		return ((this.cls.getClassFile().getAccessFlags() & AccessFlag.SUPER) != 0);
	}

	@Override
	public Signature getFieldSignature(int fieldIndex) throws InvalidIndexException {
		if (fieldIndex < 1 || fieldIndex > this.cp.getSize()) {
			throw new InvalidIndexException(indexOutOfRangeMessage(fieldIndex));
		}
		if (this.cp.getTag(fieldIndex) != ConstPool.CONST_Fieldref) {
			throw new InvalidIndexException(entryInvalidMessage(fieldIndex));
		}
        final String containerClass = this.cp.getFieldrefClassName(fieldIndex).replace('.', '/');
        final String descriptor = this.cp.getFieldrefType(fieldIndex);
        final String name = this.cp.getFieldrefName(fieldIndex);
        return new Signature(containerClass, descriptor, name);
	}

	private List<Signature> getFields(boolean areStatic) {
    	List<Signature> fields = new LinkedList<Signature>();
    	CtField[] fieldsJA = this.cls.getDeclaredFields();
    	for (CtField fld : fieldsJA) {
    		if (Modifier.isStatic(fld.getModifiers()) == areStatic) {
    			Signature sig = new Signature(this.getClassName(), fld.getSignature(), fld.getName());
    			fields.add(sig);
    		}
    	}
    	return fields;
	}
	
	@Override
	public Signature[] getFieldsNonStatic() {
    	List<Signature> fieldsList = this.getFields(false);
    	Signature[] retval = new Signature[fieldsList.size()];
    	fieldsList.<Signature>toArray(retval);
    	return retval;
	}

	@Override
	public Signature[] getFieldsStatic() {
    	List<Signature> fieldsList = this.getFields(true);
    	Signature[] retval = new Signature[fieldsList.size()];
    	fieldsList.<Signature>toArray(retval);
    	return retval;
	}

	@Override
	public Signature getInterfaceMethodSignature(int methodIndex) throws InvalidIndexException {
		if (methodIndex < 1 || methodIndex > this.cp.getSize()) {
			throw new InvalidIndexException(indexOutOfRangeMessage(methodIndex));
		}
		if (this.cp.getTag(methodIndex) != ConstPool.CONST_InterfaceMethodref) {
			throw new InvalidIndexException(entryInvalidMessage(methodIndex));
		}
        final String containerClass = this.cp.getInterfaceMethodrefClassName(methodIndex).replace('.', '/');
        final String descriptor = this.cp.getInterfaceMethodrefType(methodIndex);
        final String name = this.cp.getInterfaceMethodrefName(methodIndex);
		return new Signature(containerClass, descriptor, name); 
	}


	/**
	 * Finds a method declaration in the classfile.
	 * 
	 * @param methodSignature a {@link Signature}.
	 * @return <code>null</code> if no method with <code>methodSignature</code> 
	 *         signature is declared in <code>this</code>, otherwise the 
	 *         <code>CtMethod</code> for it; the class name in <code>methodSignature</code>
	 *         is ignored.
	 */
	private CtBehavior findMethod(Signature methodSignature) {
		CtConstructor cc = this.cls.getClassInitializer();
		if (methodSignature.getName().equals("<clinit>"))
			return cc;
		
		CtBehavior[] bs = this.cls.getDeclaredBehaviors();
		for (CtBehavior b : bs) {
			String internalName = 
				(((b instanceof CtConstructor) && (!((CtConstructor) b).isClassInitializer())) ? 
						"<init>" : 
						b.getName());
			if (internalName.equals(methodSignature.getName()) &&
				b.getSignature().equals(methodSignature.getDescriptor())) {
				return b;
			}
		}
		return null;
	}
	
	private CodeAttribute getMethodCodeAttribute(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) { 
			throw new MethodNotFoundException(methodSignature.toString());
		}
		CodeAttribute ca = b.getMethodInfo().getCodeAttribute();
		if (ca == null) {
			throw new MethodCodeNotFoundException(methodSignature.toString()); 
		}
		return ca;
	}

	@Override
	public ExceptionTable getExceptionTable(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException, InvalidIndexException {
		javassist.bytecode.ExceptionTable et = getMethodCodeAttribute(methodSignature).getExceptionTable();

		final ExceptionTable retVal = new ExceptionTable(et.size());
		for (int i = 0; i < et.size(); ++i) {
		    final int exType = et.catchType(i);
		    final String catchType = (exType == 0 ? Signatures.JAVA_THROWABLE : getClassSignature(exType));
	        final ExceptionTableEntry exEntry = new ExceptionTableEntry(et.startPc(i), et.endPc(i), et.handlerPc(i), catchType);
            retVal.addEntry(exEntry);
		}
		return retVal;
	}
	
	@Override
	public int getLocalVariableLength(Signature methodSignature)
	throws MethodNotFoundException, MethodCodeNotFoundException {
		return getMethodCodeAttribute(methodSignature).getMaxLocals();
	}

	@Override
	public int getCodeLength(Signature methodSignature) throws MethodNotFoundException, MethodCodeNotFoundException {
		return getMethodCodeAttribute(methodSignature).getCodeLength();
	}

	@Override
	public LocalVariableTable getLocalVariableTable(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException  {
		CodeAttribute ca = getMethodCodeAttribute(methodSignature);
		LocalVariableAttribute lvtJA = (LocalVariableAttribute) ca.getAttribute("LocalVariableTable");
		
        if (lvtJA == null) {
        	return this.defaultLocalVariableTable(methodSignature);
        }
        	
        //builds the local variable table from the LocalVariableTable attribute 
    	//information; this has always success
        final LocalVariableTable lvt = new LocalVariableTable(ca.getMaxLocals());
        for (int i = 0; i < lvtJA.tableLength(); ++i) {
        	lvt.setEntry(lvtJA.index(i), lvtJA.descriptor(i), 
        			     lvtJA.variableName(i), lvtJA.startPc(i),  lvtJA.codeLength(i));
        }
        return lvt;
	}

	@Override
	public byte[] getMethodCodeBySignature(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException {
		return getMethodCodeAttribute(methodSignature).getCode();
	}

	@Override
	public Signature getMethodSignature(int methodIndex) throws InvalidIndexException {
		if (methodIndex < 1 || methodIndex > this.cp.getSize()) {
			throw new InvalidIndexException(indexOutOfRangeMessage(methodIndex));
		}
		if (this.cp.getTag(methodIndex) != ConstPool.CONST_Methodref) {
			throw new InvalidIndexException(entryInvalidMessage(methodIndex));
		}
        final String containerClass = this.cp.getMethodrefClassName(methodIndex).replace('.', '/');
        final String descriptor = this.cp.getMethodrefType(methodIndex);
        final String name = this.cp.getMethodrefName(methodIndex);
		return new Signature(containerClass, descriptor, name); 
	}

	@Override
	public String getSuperClassName() {
		String name = this.cls.getClassFile().getSuperclass();
		if (name != null) {
			name = name.replace(".", "/");
		}
		return name;
	}

	@Override
	public List<String> getSuperInterfaceNames() {
		LinkedList<String> retVal = new LinkedList<String>();
		String[] ifs = this.cls.getClassFile().getInterfaces();
		
		for (String s : ifs) {
			retVal.add(s.replace(".", "/"));
		}
		return retVal;
	}

	@Override
	public ConstantPoolValue getValueFromConstantPool(int index) throws InvalidIndexException {
		if (index < 1 || index > this.cp.getSize()) {
	        throw new InvalidIndexException(indexOutOfRangeMessage(index));
		}
		final int tag = this.cp.getTag(index);
		switch (tag) {
        case ConstPool.CONST_Integer:
            return new ConstantPoolPrimitive(this.cp.getIntegerInfo(index));
        case ConstPool.CONST_Float:
            return new ConstantPoolPrimitive(this.cp.getFloatInfo(index));
        case ConstPool.CONST_Long:
            return new ConstantPoolPrimitive(this.cp.getLongInfo(index));
        case ConstPool.CONST_Double:
            return new ConstantPoolPrimitive(this.cp.getDoubleInfo(index));
        case ConstPool.CONST_String:
            return new ConstantPoolString(this.cp.getStringInfo(index));
        case ConstPool.CONST_Class:
            return new ConstantPoolClass(this.cp.getClassInfo(index).replace(".", "/"));
		}
        throw new InvalidIndexException(entryInvalidMessage(index));
	}
	
	@Override
	public boolean hasMethodDeclaration(Signature methodSignature) {
		return (findMethod(methodSignature) != null);
	}

	@Override
	public boolean hasMethodImplementation(Signature methodSignature) {
		CtBehavior b = findMethod(methodSignature);
		return (b != null && (b.getMethodInfo().getCodeAttribute() != null || Modifier.isNative(b.getModifiers())));
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(this.cls.getModifiers());
	}

	@Override
	public boolean isInterface() {
		return this.cls.isInterface();
	}

	@Override
	public boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) throw new MethodNotFoundException(methodSignature.toString());
		return Modifier.isAbstract(b.getModifiers());
	}

	@Override
	public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isNative(b.getModifiers());
	}

	@Override
	public Signature[] getMethodSignatures() {
		CtBehavior[] methods = cls.getDeclaredMethods();
		Signature[] retVal = new Signature[methods.length];
		for (int i = 0; i < methods.length; ++i) {
			retVal[i] = new Signature(this.getClassName(), methods[i].getSignature(), methods[i].getName());
		}
		return retVal;
	}

	@Override
	public Object[] getMethodAvailableAnnotations(Signature methodSignature)
	throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return b.getAvailableAnnotations();
	}

	@Override
	public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isStatic(b.getModifiers());
	}

	@Override
	public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isPublic(b.getModifiers());
	}

	@Override
	public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isProtected(b.getModifiers());
	}

	@Override
	public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isPackage(b.getModifiers());
	}

	@Override
	public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
		CtBehavior b = this.findMethod(methodSignature);
		if (b == null) {
			throw new MethodNotFoundException(methodSignature.toString());
		}
		return Modifier.isPrivate(b.getModifiers());
	}

	@Override
	public boolean hasFieldDeclaration(Signature fieldSignature) {
		return (this.findField(fieldSignature) != null);
	}

	@Override
	public LineNumberTable getLineNumberTable(Signature methodSignature) 
	throws MethodNotFoundException, MethodCodeNotFoundException {
		CodeAttribute ca = this.getMethodCodeAttribute(methodSignature);
		LineNumberAttribute lnJA = (LineNumberAttribute) ca.getAttribute("LineNumberTable");
		
		if (lnJA == null)
			return this.defaultLineNumberTable();
		LineNumberTable LN = new LineNumberTable(lnJA.tableLength());
		for (int i = 0; i < lnJA.tableLength(); ++i) {
			LN.addRow(lnJA.startPc(i), lnJA.lineNumber(i));
        }
        return LN;
	}

	@Override
	public int fieldConstantValueIndex(Signature fieldSignature) throws FieldNotFoundException, AttributeNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		final int cpVal = fld.getFieldInfo().getConstantValue();
		if (cpVal == 0) {
			throw new AttributeNotFoundException();
		}
		return cpVal;
	}

	@Override
	public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return (fld.getConstantValue() != null);
	}

	@Override
	public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isFinal(fld.getModifiers());
	}

	@Override
	public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isPublic(fld.getModifiers());
	}

	@Override
	public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isProtected(fld.getModifiers());
	}

	@Override
	public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isPackage(fld.getModifiers());
	}

	@Override
	public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isPrivate(fld.getModifiers());
	}
	
	@Override
	public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
		final CtField fld = this.findField(fieldSignature);
		if (fld == null) {
			throw new FieldNotFoundException(fieldSignature.toString());
		}
		return Modifier.isStatic(fld.getModifiers());
	}

	private CtField findField(Signature fieldSignature) {
		final CtField[] fieldsJA = this.cls.getDeclaredFields();
    	for (CtField fld : fieldsJA) {
    		if (fld.getSignature().equals(fieldSignature.getDescriptor()) && 
    				fld.getName().equals(fieldSignature.getName())) {
    			return fld;
    		}
    	}
    	return null;
	}

	@Override
	public String classContainer() {
		return this.cls.getName().substring(0, this.cls.getName().lastIndexOf('$'));
	}

	@Override
	public boolean isNested() {
		return this.cls.getName().contains("$");
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(cls.getModifiers());
	}
}