package jbse.bc;

import static javassist.bytecode.AccessFlag.clear;
import static javassist.bytecode.AccessFlag.setPackage;
import static javassist.bytecode.AccessFlag.setPrivate;
import static javassist.bytecode.AccessFlag.setProtected;
import static javassist.bytecode.AccessFlag.setPublic;
import static javassist.bytecode.AccessFlag.STATIC;
import static javassist.bytecode.AccessFlag.SUPER;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
//also uses javassist.bytecode.ClassFile, not imported to avoid name clash
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.EnclosingMethodAttribute;
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
import jbse.common.exc.UnexpectedInternalException;

/**
 * A {@link ClassFile} produced by a {@link ClassFileFactoryJavassist}.
 * 
 * @author Pietro Braione
 */
public class ClassFileJavassist extends ClassFile {
    private final CtClass cls;
    private final ConstPool cp;
    private final byte[] bytecode; //only for anonymous classes
    private final ConstantPoolValue[] cpPatches;
    private String hostClass;
    private ArrayList<Signature> fieldsStatic; //lazily initialized
    private ArrayList<Signature> fieldsObject; //lazily initialized
    private ArrayList<Signature> constructors; //lazily initialized

    /**
     * Constructor for nonanonymous classes.
     * 
     * @param cpool a {@link ClassPool} where the class must be looked for.
     * @param className a {@code String}, the name of the class.
     * @throws BadClassFileException if the classfile for {@code className} 
     *         does not exist in {@code cpool} or is ill-formed.
     */
    ClassFileJavassist(ClassPool cpool, String className) throws BadClassFileException {
        try {
            this.cls = cpool.get(binaryClassName(className));
            this.cp = this.cls.getClassFile().getConstPool();
            this.bytecode = null;
            this.cpPatches = null;
            this.hostClass = null;
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
    
    ClassFileJavassist(ClassPool cpool, String hostClass, byte[] bytecode, ConstantPoolValue[] cpPatches) 
    throws ClassFileIllFormedException {
        try {
            final javassist.bytecode.ClassFile cf = new javassist.bytecode.ClassFile(new DataInputStream(new ByteArrayInputStream(bytecode)));
            checkCpPatches(cf.getConstPool(), cpPatches);
            patch(cf.getConstPool(), cpPatches);
            
            //modifies the class name by adding the hash
            final String defaultName = cf.getName(); //the (possibly patched) name in the bytecode
            final String name = defaultName + '$' + Arrays.hashCode(bytecode);
            cf.setName(name);
            
            //makes the CtClass
            this.cls = cpool.makeClass(cf);
            this.cp = this.cls.getClassFile().getConstPool();
            this.bytecode = (hostClass == null ? bytecode : null); //only dummy anonymous classfiles without a host class cache their bytecode
            this.cpPatches = (cpPatches == null ? null : cpPatches.clone());
            this.hostClass = hostClass;
            this.fieldsStatic = this.fieldsObject = this.constructors = null;
        } catch (IOException e) {
            throw new ClassFileIllFormedException("anonymous");
        }
    }
    
    private void checkCpPatches(javassist.bytecode.ConstPool cp, ConstantPoolValue[] cpPatches) 
    throws ClassFileIllFormedException {
        if (cpPatches == null) {
            return;
        }
        for (int i = 1; i < Math.min(cp.getSize(), cpPatches.length); ++i) {
            if (cpPatches[i] == null) {
                continue;
            }
            final int tag = cp.getTag(i);
            if (tag == ConstPool.CONST_String) {
                continue; //any will fit
            }
            if (tag == ConstPool.CONST_Integer && 
                cpPatches[i] instanceof ConstantPoolPrimitive && 
                ((ConstantPoolPrimitive) cpPatches[i]).getValue() instanceof Integer) {
                continue;
            }
            if (tag == ConstPool.CONST_Long && 
                cpPatches[i] instanceof ConstantPoolPrimitive && 
                ((ConstantPoolPrimitive) cpPatches[i]).getValue() instanceof Long) {
                continue;
            }
            if (tag == ConstPool.CONST_Float && 
                cpPatches[i] instanceof ConstantPoolPrimitive && 
                ((ConstantPoolPrimitive) cpPatches[i]).getValue() instanceof Float) {
                continue;
            }
            if (tag == ConstPool.CONST_Double && 
                cpPatches[i] instanceof ConstantPoolPrimitive && 
                ((ConstantPoolPrimitive) cpPatches[i]).getValue() instanceof Double) {
                continue;
            }
            if (tag == ConstPool.CONST_Utf8 && cpPatches[i] instanceof ConstantPoolString) {
                continue;
            }
            if (tag == ConstPool.CONST_Class && cpPatches[i] instanceof ConstantPoolClass) {
                continue;
            }
            throw new ClassFileIllFormedException("anonymous, patches");
        }
    }
    
    private void patch(javassist.bytecode.ConstPool cp, ConstantPoolValue[] cpPatches) {
        if (cpPatches == null) {
            return;
        }
        //must use reflection
        try {
            final Field cpItemsField = javassist.bytecode.ConstPool.class.getDeclaredField("items");
            cpItemsField.setAccessible(true);
            final Object cpItems = cpItemsField.get(cp);
            final Class<?> longVectorClass = Class.forName("javassist.bytecode.LongVector");
            final Method longVectorElementAt = longVectorClass.getDeclaredMethod("elementAt", int.class);
            longVectorElementAt.setAccessible(true);
            for (int i = 1; i < Math.min(cp.getSize(), cpPatches.length); ++i) {
                if (cpPatches[i] == null) {
                    continue;
                }
                final int tag = cp.getTag(i);
                if (tag == ConstPool.CONST_String) {
                    continue; //cannot set!
                }
                final Object cpItem = longVectorElementAt.invoke(cpItems, Integer.valueOf(i));
                if (tag == ConstPool.CONST_Integer) {
                    final Integer value = (Integer) ((ConstantPoolPrimitive) cpPatches[i]).getValue();
                    final Class<?> integerInfoClass = Class.forName("javassist.bytecode.IntegerInfo");
                    final Field integerInfoValueField = integerInfoClass.getDeclaredField("value");
                    integerInfoValueField.setAccessible(true);
                    integerInfoValueField.set(cpItem, value);
                    continue;
                }
                if (tag == ConstPool.CONST_Long) {
                    final Long value = (Long) ((ConstantPoolPrimitive) cpPatches[i]).getValue();
                    final Class<?> longInfoClass = Class.forName("javassist.bytecode.LongInfo");
                    final Field longInfoValueField = longInfoClass.getDeclaredField("value");
                    longInfoValueField.setAccessible(true);
                    longInfoValueField.set(cpItem, value);
                    continue;
                }
                if (tag == ConstPool.CONST_Float) {
                    final Float value = (Float) ((ConstantPoolPrimitive) cpPatches[i]).getValue();
                    final Class<?> floatInfoClass = Class.forName("javassist.bytecode.FloatInfo");
                    final Field floatInfoValueField = floatInfoClass.getDeclaredField("value");
                    floatInfoValueField.setAccessible(true);
                    floatInfoValueField.set(cpItem, value);
                    continue;
                }
                if (tag == ConstPool.CONST_Double) {
                    final Double value = (Double) ((ConstantPoolPrimitive) cpPatches[i]).getValue();
                    final Class<?> doubleInfoClass = Class.forName("javassist.bytecode.DoubleInfo");
                    final Field doubleInfoValueField = doubleInfoClass.getDeclaredField("value");
                    doubleInfoValueField.setAccessible(true);
                    doubleInfoValueField.set(cpItem, value);
                    continue;
                }
                if (tag == ConstPool.CONST_Utf8) {
                    final String value = ((ConstantPoolString) cpPatches[i]).getValue();
                    final Class<?> utf8InfoClass = Class.forName("javassist.bytecode.Utf8Info");
                    final Field utf8InfoStringField = utf8InfoClass.getDeclaredField("string");
                    utf8InfoStringField.setAccessible(true);
                    utf8InfoStringField.set(cpItem, value);
                    continue;
                }
                if (tag == ConstPool.CONST_Class) {
                    final int value = cp.addUtf8Info(((ConstantPoolClass) cpPatches[i]).getValue());
                    final Class<?> classInfoClass = Class.forName("javassist.bytecode.ClassInfo");
                    final Field classInfoNameField = classInfoClass.getDeclaredField("name");
                    classInfoNameField.setAccessible(true);
                    classInfoNameField.set(cpItem, Integer.valueOf(value));
                    continue;
                }
            }
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | 
                 IllegalAccessException | ClassNotFoundException | NoSuchMethodException | 
                 InvocationTargetException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
    @Override
    byte[] getBinaryFileContent() {
        return this.bytecode;
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
    public boolean isLocal() {
        final String className = getClassName();
        final int lastDollarSignIndex = className.lastIndexOf('$');
        if (lastDollarSignIndex == -1) {
            return false; //not a nested class
        }
        return isAsciiDigit(className.charAt(lastDollarSignIndex + 1));
    }
    
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }
    
    @Override
    public boolean isAnonymous() {
        final String className = getClassName();
        final int lastDollarSignIndex = className.lastIndexOf('$');
        if (lastDollarSignIndex == -1) {
            return false; //not a nested class
        }
        for (int i = lastDollarSignIndex + 1; i < className.length(); ++i) {
            if (!isAsciiDigit(className.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String getHostClass() {
        return this.hostClass;
    }
    
    @Override
    public int constantPoolSize() {
        return this.cp.getSize();
    }

    @Override
    public Signature getFieldSignature(int fieldIndex) throws InvalidIndexException {
        if (fieldIndex < 1 || fieldIndex > this.cp.getSize()) {
            throw new InvalidIndexException(indexOutOfRangeMessage(fieldIndex));
        }
        if (this.cp.getTag(fieldIndex) != ConstPool.CONST_Fieldref) {
            throw new InvalidIndexException(entryInvalidMessage(fieldIndex));
        }
        final String containerClass = internalClassName(this.cp.getFieldrefClassName(fieldIndex));
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
        final String containerClass = internalClassName(this.cp.getInterfaceMethodrefClassName(methodIndex));
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
    private CtBehavior findMethodDeclaration(Signature methodSignature) {
        if ("<clinit>".equals(methodSignature.getName())) {
            return this.cls.getClassInitializer();
        }

        final CtBehavior[] bs = this.cls.getDeclaredBehaviors();
        for (CtBehavior b : bs) {
            final String internalName = ctBehaviorInternalName(b);
            if (internalName.equals(methodSignature.getName()) &&
                b.getSignature().equals(methodSignature.getDescriptor())) {
                return b;
            }
        }
        return null;
    }

    private CodeAttribute getMethodCodeAttribute(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) { 
            throw new MethodNotFoundException(methodSignature.toString());
        }
        final CodeAttribute ca = b.getMethodInfo().getCodeAttribute();
        if (ca == null) {
            throw new MethodCodeNotFoundException(methodSignature.toString()); 
        }
        return ca;
    }

    @Override
    public ExceptionTable getExceptionTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException, InvalidIndexException {
        final javassist.bytecode.ExceptionTable et = getMethodCodeAttribute(methodSignature).getExceptionTable();

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
        final CodeAttribute ca = getMethodCodeAttribute(methodSignature);
        final LocalVariableAttribute lvtJA = (LocalVariableAttribute) ca.getAttribute("LocalVariableTable");

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
        final String containerClass = internalClassName(this.cp.getMethodrefClassName(methodIndex));
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
                name = internalClassName(name);
            }
            return name;
        }
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        final ArrayList<String> superinterfaces = new ArrayList<>();
        final String[] ifs = this.cls.getClassFile().getInterfaces();

        for (String s : ifs) {
            superinterfaces.add(internalClassName(s));
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
            if (this.cpPatches != null && index < this.cpPatches.length && this.cpPatches[index] != null) {
                return this.cpPatches[index];
            }
            return new ConstantPoolString(this.cp.getStringInfo(index));
        case ConstPool.CONST_Class:
            return new ConstantPoolClass(internalClassName(this.cp.getClassInfo(index)));
        case ConstPool.CONST_Utf8:
            return new ConstantPoolUtf8(this.cp.getUtf8Info(index));
        }
        throw new InvalidIndexException(entryInvalidMessage(index));
    }

    @Override
    public boolean hasMethodDeclaration(Signature methodSignature) {
        return (findMethodDeclaration(methodSignature) != null);
    }
    
    private final String ctBehaviorInternalName(CtBehavior b) {
        if (b instanceof CtConstructor) {
            final CtConstructor bc = (CtConstructor) b;
            return (bc.isClassInitializer() ? "<clinit>" : "<init>"); 
        } else {
            return b.getName();
        }
    }
    
    private CtBehavior findUniqueMethodDeclarationWithName(String methodName) {
        final CtBehavior[] bs = this.cls.getDeclaredBehaviors();
        CtBehavior retVal = null;
        for (CtBehavior b : bs) {
            final String internalName = ctBehaviorInternalName(b);
            if (internalName.equals(methodName)) {
                if (retVal == null) {
                    retVal = b;
                } else {
                    //two methods with same name - not unique
                    return null;
                }
            }
        }
        return retVal;
    }

    @Override
    public boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName) {
        //cannot be signature polymorphic if it is not in JAVA_METHODHANDLE
        if (!JAVA_METHODHANDLE.equals(getClassName())) {
            return false;
        }
        
        //the method declaration must be unique
        final CtBehavior uniqueMethod = findUniqueMethodDeclarationWithName(methodName);
        if (uniqueMethod == null) {
            return false;
        }
        
        //cannot be signature polymorphic if it has wrong descriptor
        if (!SIGNATURE_POLYMORPHIC_DESCRIPTOR.equals(uniqueMethod.getSignature())) {
            return false;
        }
        
        //cannot be signature polymorphic if it not native or if it is not varargs
        if (!Modifier.isNative(uniqueMethod.getModifiers()) || (uniqueMethod.getModifiers() & Modifier.VARARGS) == 0) {
            return false;
        }

        //all checks passed
        return true;
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        final CtBehavior b = findMethodDeclaration(methodSignature);
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
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) throw new MethodNotFoundException(methodSignature.toString());
        return Modifier.isAbstract(b.getModifiers());
    }

    @Override
    public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isNative(b.getModifiers());
    }
    
    @Override
    public boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return (b.getModifiers() & Modifier.VARARGS) != 0;
    }
    
    @Override
    public boolean isMethodSignaturePolymorphic(Signature methodSignature) throws MethodNotFoundException {
        //cannot be signature polymorphic if it is not in JAVA_METHODHANDLE
        if (!JAVA_METHODHANDLE.equals(getClassName())) {
            return false;
        }
        
        //cannot be signature polymorphic if it has wrong descriptor
        if (!SIGNATURE_POLYMORPHIC_DESCRIPTOR.equals(methodSignature.getDescriptor())) {
            return false;
        }
        
        //cannot be signature polymorphic if is not (native | varargs)
        if (!isMethodNative(methodSignature) || !isMethodVarargs(methodSignature)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isMethodCallerSensitive(Signature methodSignature) 
    throws ClassFileNotFoundException, MethodNotFoundException {
        final Object[] annotations = getMethodAvailableAnnotations(methodSignature);
        for (Object annotation : annotations) {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) annotation.getClass();
            final Class<?> callerSensitiveClass;
            try {
                callerSensitiveClass = Class.forName("sun.reflect.CallerSensitive");
            } catch (ClassNotFoundException e) {
                throw new ClassFileNotFoundException("sun.reflect.CallerSensitive");
            }
            if (callerSensitiveClass.isAssignableFrom(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Signature[] getDeclaredMethods() {
        final CtBehavior[] methods = cls.getDeclaredMethods();
        final Signature[] retVal = new Signature[methods.length];
        for (int i = 0; i < methods.length; ++i) {
            retVal[i] = new Signature(getClassName(), methods[i].getSignature(), ctBehaviorInternalName(methods[i]));
        }
        return retVal;
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return b.getGenericSignature();
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) 
    throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
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
        final CtBehavior b = findMethodDeclaration(methodSignature);
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
        //this circumvents a bug in Javassist 3.22.0-GA
        if (isMethodSignaturePolymorphic(methodSignature)) {
            Class<?> ann;
            try {
                ann = Class.forName("java.lang.invoke.MethodHandle$PolymorphicSignature");
            } catch (ClassNotFoundException e) {
                return new Object[0];
            }
            return new Object[]{ ann };
        } else if (isAnonymous()) {
            return new Object[0];
        }
        
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return b.getAvailableAnnotations();
    }

    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
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
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isStatic(b.getModifiers());
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPublic(b.getModifiers());
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isProtected(b.getModifiers());
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
        if (b == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPackage(b.getModifiers());
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
        final CtBehavior b = findMethodDeclaration(methodSignature);
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
            return defaultLineNumberTable();
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
                final Signature sig = new Signature(getClassName(), constr.getSignature(), ctBehaviorInternalName(constr));
                constructors.add(sig);
            }
            this.constructors = constructors;
        }
        final Signature[] retVal = new Signature[this.constructors.size()];
        this.constructors.toArray(retVal);
        return retVal;
    }

    @Override
    public String classContainer() throws ClassFileNotFoundException {
        try {
            if (this.cls.getDeclaringClass() == null) {
                return null;
            }
            return (this.cls.getDeclaringClass().getName());
        } catch (NotFoundException e) {
            throw new ClassFileNotFoundException(e.getMessage());
        }
    }
    
    @Override
    public Signature getEnclosingMethodOrConstructor() {
        //we do not use this.cls.getEnclosingBehavior because this
        //method does not handle the case of local and anonymous classes
        //that are not immediately enclosed by a method or constructor.
        //This code is copied from CtClassType#getEnclosingBehavior.
        final javassist.bytecode.ClassFile cf = this.cls.getClassFile2();
        final EnclosingMethodAttribute ema = 
            (EnclosingMethodAttribute) cf.getAttribute(EnclosingMethodAttribute.tag);
        if (ema == null) {
            return null;
        }
        return new Signature(internalClassName(ema.className()), ema.methodDescriptor(), ema.methodName());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(cls.getModifiers());
    }
}