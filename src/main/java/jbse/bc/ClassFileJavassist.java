package jbse.bc;

import static javassist.bytecode.AccessFlag.clear;
import static javassist.bytecode.AccessFlag.setPackage;
import static javassist.bytecode.AccessFlag.setPrivate;
import static javassist.bytecode.AccessFlag.setProtected;
import static javassist.bytecode.AccessFlag.setPublic;
import static javassist.bytecode.AccessFlag.STATIC;
import static javassist.bytecode.AccessFlag.SUPER;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
//also uses javassist.bytecode.ExceptionTable, not imported to avoid name clash

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

/**
 * A {@link ClassFile} produced by a {@link ClassFileFactoryJavassist}.
 * 
 * @author Pietro Braione
 */
public class ClassFileJavassist extends ClassFile {
    private CtClass cls;
    private ConstPool cp;
    private ArrayList<Signature> fieldsStatic;
    private ArrayList<Signature> fieldsObject;
    private ArrayList<Signature> constructors;

    ClassFileJavassist(String className, ClassPool cpool) throws BadClassFileException {
        try {
            this.cls = cpool.get(binaryClassName(className));
            this.cp = this.cls.getClassFile().getConstPool();
            this.fieldsStatic = this.fieldsObject = this.constructors = null;
        } catch (NotFoundException e) {
            throw new ClassFileNotFoundException(className);
        } catch (RuntimeException e) {
            //ugly, but it seems to be the only way to detect
            //an ill-formed classfile
            if (e.getMessage().equals("java.io.IOException: non class file")) {
                throw new ClassFileIllFormedException(className);
            } else {
                throw e;
            }
        }
    }

    @Override
    public String getSourceFile() {
        return this.cls.getClassFile().getSourceFile();
    }

    @Override
    public String getClassName() {
        return internalClassName(this.cls.getName());
    }

    @Override
    public String getClassSignature(int classIndex) throws InvalidIndexException {
        if (classIndex < 1 || classIndex > this.cp.getSize()) {
            throw new InvalidIndexException(indexOutOfRangeMessage(classIndex));
        }
        if (this.cp.getTag(classIndex) != ConstPool.CONST_Class) {
            throw new InvalidIndexException(entryInvalidMessage(classIndex));
        }
        return internalClassName(this.cp.getClassInfo(classIndex));
    }
    
    @Override
    public int getModifiers() {
        //this code reimplements CtClassType.getModifiers() to circumvent a bug
        javassist.bytecode.ClassFile cf = this.cls.getClassFile2();
        int acc = cf.getAccessFlags();
        acc = clear(acc, SUPER);
        int inner = cf.getInnerAccessFlags();
        if (inner != -1) {
            if ((inner & STATIC) != 0) {
                acc |= STATIC;
            }
            if (AccessFlag.isPublic(inner)) {
                //seems that public nested classes already have the PUBLIC modifier set
                //but we are paranoid and we set it again
                acc = setPublic(acc);
            } else if (AccessFlag.isProtected(inner)) {
                acc = setProtected(acc);
            } else if (AccessFlag.isPrivate(inner)) {
                acc = setPrivate(acc);
            } else { //package visibility
                acc = setPackage(acc); //clear the PUBLIC modifier in case it is set
            }
        }
        return AccessFlag.toModifier(acc);
    }

    @Override
    public int getAccessFlags() {
        return this.cls.getClassFile().getAccessFlags();
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }
    
    @Override
    public boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }
    
    @Override
    public boolean isPackage() {
        return Modifier.isPackage(getModifiers());
    }
    
    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    @Override
    public boolean isArray() {
        return false;
    }
    
    @Override
    public boolean isEnum() {
        return Modifier.isEnum(getModifiers());
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

    private ArrayList<Signature> getDeclaredFields(boolean areStatic) {
        if ((areStatic ? this.fieldsStatic : this.fieldsObject) == null) {
            final ArrayList<Signature> fields = new ArrayList<Signature>();
            final CtField[] fieldsJA = this.cls.getDeclaredFields();
            for (CtField fld : fieldsJA) {
                if (Modifier.isStatic(fld.getModifiers()) == areStatic) {
                    final Signature sig = new Signature(getClassName(), fld.getSignature(), fld.getName());
                    fields.add(sig);
                }
            }
            if (areStatic) {
                this.fieldsStatic = fields;
            } else {
                this.fieldsObject = fields;
            }
        }
        return (areStatic ? this.fieldsStatic : this.fieldsObject);
    }

    @Override
    public Signature[] getDeclaredFieldsNonStatic() {
        final ArrayList<Signature> fieldsList = getDeclaredFields(false);
        final Signature[] retVal = new Signature[fieldsList.size()];
        fieldsList.toArray(retVal);
        return retVal;
    }

    @Override
    public Signature[] getDeclaredFieldsStatic() {
        final ArrayList<Signature> fieldsList = getDeclaredFields(true);
        final Signature[] retVal = new Signature[fieldsList.size()];
        fieldsList.toArray(retVal);
        return retVal;
    }

    @Override
    public Signature[] getDeclaredFields() {
        return Stream
        .concat(Arrays.stream(getDeclaredFieldsStatic()), Arrays.stream(getDeclaredFieldsNonStatic()))
        .toArray(Signature[]::new);
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
     * @return {@code null} if no method with {@code methodSignature} 
     *         signature is declared in this classfile, otherwise the 
     *         {@link CtBehavior} for it; the class name in {@code methodSignature}
     *         is ignored.
     */
    private CtBehavior findMethod(Signature methodSignature) {
        CtConstructor cc = this.cls.getClassInitializer();
        if (methodSignature.getName().equals("<clinit>")) {
            return cc;
        }

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
        CtBehavior b = findMethod(methodSignature);
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
    public String getSuperclassName() {
        if (isInterface()) {
            return null;
        } else {
            String name = this.cls.getClassFile().getSuperclass();
            if (name != null) {
                name = name.replace(".", "/");
            }
            return name;
        }
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        final ArrayList<String> superinterfaces = new ArrayList<>();
        final String[] ifs = this.cls.getClassFile().getInterfaces();

        for (String s : ifs) {
            superinterfaces.add(s.replace(".", "/"));
        }
        return Collections.unmodifiableList(superinterfaces);
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
        final CtBehavior b = findMethod(methodSignature);
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
        final CtBehavior b = this.findMethod(methodSignature);
        if (b == null) throw new MethodNotFoundException(methodSignature.toString());
        return Modifier.isAbstract(b.getModifiers());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = this.findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isNative(b.getModifiers());
    }

    @Override
    public Signature[] getMethodSignatures() {
        final CtBehavior[] methods = cls.getDeclaredMethods();
        final Signature[] retVal = new Signature[methods.length];
        for (int i = 0; i < methods.length; ++i) {
            retVal[i] = new Signature(this.getClassName(), methods[i].getSignature(), methods[i].getName());
        }
        return retVal;
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return b.getGenericSignature();
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) 
    throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return b.getModifiers();
    }

    private byte[] mergeVisibleAndInvisibleAttributes(AttributeInfo attrVisible, AttributeInfo attrInvisible) {
        final byte[] visible = (attrVisible == null ? new byte[0] : attrVisible.get());
        final byte[] invisible = (attrInvisible == null ? new byte[0] : attrInvisible.get());
        final byte[] retVal = new byte[visible.length + invisible.length];
        System.arraycopy(visible, 0, retVal, 0, visible.length);
        System.arraycopy(invisible, 0, retVal, visible.length, invisible.length);
        return retVal;
    }

    @Override
    public byte[] getMethodAnnotationsRaw(Signature methodSignature) 
    throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        final AttributeInfo attrVisible = b.getMethodInfo().getAttribute(AnnotationsAttribute.visibleTag);
        final AttributeInfo attrInvisible = b.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag);
        return mergeVisibleAndInvisibleAttributes(attrVisible, attrInvisible);
    }

    @Override
    public Object[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return b.getAvailableAnnotations();
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }

        CtClass[] exc;
        try {
            exc = b.getExceptionTypes();
        } catch (NotFoundException e) {
            //it is unclear when this exception is thrown;
            //so we just catch it and set exc to an empty array
            exc = new CtClass[0];
        }
        return Arrays.stream(exc).map(cls -> internalClassName(cls.getName())).toArray(String[]::new);
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isStatic(b.getModifiers());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPublic(b.getModifiers());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isProtected(b.getModifiers());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPackage(b.getModifiers());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethod(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPrivate(b.getModifiers());
    }

    @Override
    public boolean hasFieldDeclaration(Signature fieldSignature) {
        return (findField(fieldSignature) != null);
    }

    @Override
    public LineNumberTable getLineNumberTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        final CodeAttribute ca = this.getMethodCodeAttribute(methodSignature);
        final LineNumberAttribute lnJA = (LineNumberAttribute) ca.getAttribute("LineNumberTable");

        if (lnJA == null)
            return this.defaultLineNumberTable();
        final LineNumberTable LN = new LineNumberTable(lnJA.tableLength());
        for (int i = 0; i < lnJA.tableLength(); ++i) {
            LN.addRow(lnJA.startPc(i), lnJA.lineNumber(i));
        }
        return LN;
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature) throws FieldNotFoundException, AttributeNotFoundException {
        final CtField fld = findField(fieldSignature);
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
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return (fld.getConstantValue() != null);
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isFinal(fld.getModifiers());
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPublic(fld.getModifiers());
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isProtected(fld.getModifiers());
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPackage(fld.getModifiers());
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPrivate(fld.getModifiers());
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isStatic(fld.getModifiers());
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) 
    throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return fld.getGenericSignature();
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) 
    throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return fld.getModifiers();
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) 
    throws FieldNotFoundException {
        final CtField fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        final AttributeInfo attrVisible = fld.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        final AttributeInfo attrInvisible = fld.getFieldInfo().getAttribute(AnnotationsAttribute.invisibleTag);
        return mergeVisibleAndInvisibleAttributes(attrVisible, attrInvisible);
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
    public Signature[] getDeclaredConstructors() {
        if (this.constructors == null) {
            final ArrayList<Signature> constructors = new ArrayList<Signature>();
            final CtConstructor[] constrJA = this.cls.getDeclaredConstructors();
            for (CtConstructor constr : constrJA) {
                final Signature sig = new Signature(getClassName(), constr.getSignature(), constr.getMethodInfo().getName());
                constructors.add(sig);
            }
            this.constructors = constructors;
        }
        final Signature[] retVal = new Signature[this.constructors.size()];
        this.constructors.toArray(retVal);
        return retVal;
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