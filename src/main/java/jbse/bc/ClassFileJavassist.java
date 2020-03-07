package jbse.bc;

import static javassist.bytecode.AccessFlag.clear;
import static javassist.bytecode.AccessFlag.setPackage;
import static javassist.bytecode.AccessFlag.setPrivate;
import static javassist.bytecode.AccessFlag.setProtected;
import static javassist.bytecode.AccessFlag.setPublic;
import static javassist.bytecode.AccessFlag.STATIC;
import static javassist.bytecode.AccessFlag.SUPER;
import static jbse.bc.ClassLoaders.CLASSLOADER_NONE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.SIGNATURE_POLYMORPHIC_DESCRIPTOR;
import static jbse.bc.Signatures.SUN_CALLERSENSITIVE;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.classNameContained;
import static jbse.common.Type.classNameContainer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import javassist.Modifier;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
//also uses javassist.bytecode.ClassFile, not imported to avoid name clash
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.EnclosingMethodAttribute;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.InnerClassesAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.LocalVariableTypeAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;
//also uses javassist.bytecode.ExceptionTable, not imported to avoid name clash
import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * A {@link ClassFile} produced by a {@link ClassFileFactoryJavassist}.
 * 
 * @author Pietro Braione
 */
public class ClassFileJavassist extends ClassFile {
    private final boolean isAnonymousUnregistered;
    private final int definingClassLoader;
    private final javassist.bytecode.ClassFile cf;
    private final ConstPool cp;
    private final ClassFile superClass;
    private final ClassFile[] superInterfaces;
    private final ConstantPoolValue[] cpPatches;
    private final ClassFile hostClass;
    private String className; //nonfinal because of classfile renaming
    private byte[] bytecode; //only for dummy classes, nonfinal because of classfile renaming
    private ArrayList<Signature> fieldsStatic; //lazily initialized, but actually final
    private ArrayList<Signature> fieldsObject; //lazily initialized, but actually final
    private ArrayList<Signature> methods; //lazily initialized, but actually final
    private ArrayList<Signature> constructors; //lazily initialized, but actually final

    /**
     * Constructor for nonanonymous classes.
     * 
     * @param definingClassLoader a {@code int}, the defining classloader of
     *        the class.
     * @param className a {@code String}, the name of the class (used only for
     *        error reporting).
     * @param bytecode a {@code byte[]}, the bytecode of the class.
     * @param superClass a {@link ClassFile}, the superclass. It must be {@code null} for
     *        <em>dummy</em>, i.e., incomplete, classfiles that are created to access
     *        the bytecode conveniently.
     * @param superInterfaces a {@link ClassFile}{@code []}, the superinterfaces 
     *        (empty array when no superinterfaces). 
     *        It must be {@code null} for <em>dummy</em>, i.e., incomplete, classfiles 
     *        that are created to access the bytecode conveniently.
     * @throws ClassFileIllFormedException if the {@code bytecode} 
     *         is ill-formed.
     * @throws InvalidInputException if {@code className}, {@code superClass} or
     *         {@code superInterfaces} do not agree with {@code bytecode},
     *         or {@code bytecode == null}.
     */
    ClassFileJavassist(int definingClassLoader, String className, byte[] bytecode, ClassFile superClass, ClassFile[] superInterfaces) 
    throws ClassFileIllFormedException, InvalidInputException {
        try {
            //checks
            if (bytecode == null) {
                throw new InvalidInputException("ClassFile constructor invoked with bytecode parameters whose value is null.");
            }
            
            //reads the bytecode
            this.cf = new javassist.bytecode.ClassFile(new DataInputStream(new ByteArrayInputStream(bytecode)));
            
            //checks
            if (superClass != null && !superClass.getClassName().equals(getSuperclassName())) {
                throw new InvalidInputException("ClassFile constructor invoked with superClass and bytecode parameters that do not agree: superClass is for class " + superClass.getClassName() + " but bytecode requires " + this.cf.getSuperclass() + ".");
            }
            if (superInterfaces != null) {
                final String[] superInterfaceNames = Arrays.stream(superInterfaces).map(ClassFile::getClassName).toArray(String[]::new);
                final String[] bytecodeSuperInterfaceNames = Arrays.stream(this.cf.getInterfaces()).map(Type::internalClassName).toArray(String[]::new);
                Arrays.sort(superInterfaceNames);
                Arrays.sort(bytecodeSuperInterfaceNames);
                if (superInterfaceNames.length != bytecodeSuperInterfaceNames.length) {
                    throw new InvalidInputException("ClassFile constructor invoked with superInterfaces and bytecode parameters that do not agree: superInterfaces counts " + superInterfaceNames.length + " superinterfaces but bytecode requires " + bytecodeSuperInterfaceNames.length + " superinterfaces." );
                }
                for (int i = 0; i < superInterfaceNames.length; ++i) {
                    if (!superInterfaceNames[i].equals(bytecodeSuperInterfaceNames[i])) {
                        throw new InvalidInputException("ClassFile constructor invoked with superInterfaces and bytecode parameters that do not agree: superInterfaces has superinterface " + superInterfaceNames[i] + " that does not match with bytecode superinterface " + bytecodeSuperInterfaceNames[i] + "." );
                    }
                }
            }
            
            //inits
            this.isAnonymousUnregistered = false;
            this.definingClassLoader = definingClassLoader;
            this.className = internalClassName(this.cf.getName());
            this.cp = this.cf.getConstPool();
            this.bytecode = (superInterfaces == null ? bytecode : null); //only dummy classfiles (without a superInterfaces array) cache their bytecode
            this.superClass = superClass;
            this.superInterfaces = superInterfaces;
            this.cpPatches = null;
            this.hostClass = null;
            this.fieldsStatic = this.fieldsObject = this.constructors = null;
        } catch (IOException e) {
            throw new ClassFileIllFormedException(className);
        }
    }
    
    /**
     * Constructor for anonymous (unregistered) classes.
     * 
     * @param bytecode a {@code byte[]}, the bytecode of the class.
     * @param cfJAVA_OBJECT a {@link ClassFile} for {@code java.lang.Object}.
     *        It can be {@code null} for <em>dummy</em>, i.e., incomplete 
     *        classfiles that are created to access the bytecode conveniently.
     * @param cpPatches a {@link ConstantPoolValue}{@code []}; The i-th element of this
     *        array patches the i-th element in the constant pool defined
     *        by the {@code bytecode}. Note that {@code cpPatches[0]} and all the
     *        {@code cpPatches[i]} with {@code i} equal or greater than the size
     *        of the constant pool in {@code classFile} are ignored. It can be 
     *        {@code null} for <em>dummy</em>, i.e., incomplete classfiles 
     *        that are created to access the bytecode conveniently.
     * @param hostClass a {@link ClassFile}, the host class for the anonymous class. 
     *        It must be {@code null} for <em>dummy</em>, i.e., incomplete classfiles 
     *        that are created to access the bytecode conveniently.
     * @throws ClassFileIllFormedException if the {@code bytecode} 
     *         is ill-formed.
     * @throws InvalidInputException if {@code cpPatches} does not agree with {@code bytecode},
     *         or {@code bytecode == null} or {@code cf_JAVA_OBJECT != null} and {@code cf_JAVA_OBJECT}
     *         is not a classfile for {@code java.lang.Object}.
     */
    ClassFileJavassist(byte[] bytecode, ClassFile cfJAVA_OBJECT, ConstantPoolValue[] cpPatches, ClassFile hostClass) 
    throws ClassFileIllFormedException, InvalidInputException {
        try {
            //checks
            if (bytecode == null) {
                throw new InvalidInputException("ClassFile constructor for anonymous classes invoked with bytecode parameter whose value is null.");
            }
            if (cfJAVA_OBJECT != null && !JAVA_OBJECT.equals(cfJAVA_OBJECT.getClassName())) {
                throw new InvalidInputException("ClassFile constructor for anonymous classes invoked with cf_JAVA_OBJECT parameter whose value is a classfile for class " + cfJAVA_OBJECT.getClassName() + ".");
            }
            
            //determines if it is dummy
            final boolean isDummy = (hostClass == null);
            
            //reads and patches the bytecode
            this.cf = new javassist.bytecode.ClassFile(new DataInputStream(new ByteArrayInputStream(bytecode)));
            checkCpPatches(this.cf.getConstPool(), cpPatches);
            patch(this.cf.getConstPool(), cpPatches);
            
            //modifies the class name by adding the hash
            final String defaultName = this.cf.getName(); //the (possibly patched) name in the bytecode
            final String name = defaultName + '/' + Arrays.hashCode(bytecode);
            this.cf.setName(name);
            
            //inits
            this.isAnonymousUnregistered = true;
            this.definingClassLoader = (isDummy ? CLASSLOADER_NONE : hostClass.getDefiningClassLoader());
            this.className = internalClassName(this.cf.getName());
            this.cp = this.cf.getConstPool();
            this.bytecode = (isDummy ? bytecode : null); //only dummy anonymous classfiles (without a host class) cache their bytecode
            this.superClass = cfJAVA_OBJECT;
            this.superInterfaces = new ClassFile[0];
            this.cpPatches = (cpPatches == null ? null : cpPatches.clone());
            this.hostClass = hostClass;
            this.fieldsStatic = this.fieldsObject = this.constructors = null;
        } catch (IOException e) {
            throw new ClassFileIllFormedException("anonymous");
        }
    }
    
    private void checkCpPatches(javassist.bytecode.ConstPool cp, ConstantPoolValue[] cpPatches) 
    throws InvalidInputException {
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
            throw new InvalidInputException("ClassFile constructor for anonymous classfile invoked with cpPatches parameter not matching bytecode's constant pool.");
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
    public byte[] getBinaryFileContent() {
        return this.bytecode;
    }
    
    @Override
    public String getSourceFile() {
        final String javassistSourceFile = this.cf.getSourceFile();
        return (javassistSourceFile == null ? "" : javassistSourceFile);
    }
    
    @Override
    public int getMajorVersion() {
        return this.cf.getMajorVersion();
    }
    
    @Override
    public int getMinorVersion() {
        return this.cf.getMinorVersion();
    }
    
    @Override
    public int getDefiningClassLoader() {
        return this.definingClassLoader;
    }

    @Override
    public String getPackageName() {
        final String className = getClassName();
        final int lastDollar = className.lastIndexOf('$');
        final String prefix = (lastDollar == -1 ? className : className.substring(0, lastDollar));
        final int lastSlash = prefix.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        } else {
            return prefix.substring(0, lastSlash);
        }
    }
    
    @Override
    public String getClassName() {
        return this.className;
    }
    
    @Override
    public void rename(String classNameNew) throws RenameUnsupportedException {
    	final HashMap<String, String> renames = new HashMap<>();
    	renames.put(this.className, classNameNew);
        final InnerClassesAttribute ica = 
                (InnerClassesAttribute) this.cf.getAttribute(InnerClassesAttribute.tag);
        if (ica != null) {
        	final String fromContainer = classNameContainer(this.className);
        	final String toContainer = classNameContainer(classNameNew);
            final int n = ica.tableLength();
            for (int i = 0; i < n; ++i) {
            	final String innerClassName = internalClassName(ica.innerClass(i));
            	if (fromContainer.equals(classNameContainer(innerClassName)) &&
            		!renames.containsKey(innerClassName)) {
            		renames.put(innerClassName, toContainer + classNameContained(innerClassName));
            	}
                final String outerClassName = internalClassName(ica.outerClass(i));
                if (outerClassName != null && fromContainer.equals(classNameContainer(outerClassName)) &&
                	!renames.containsKey(outerClassName)) {
                	renames.put(outerClassName, toContainer + classNameContained(outerClassName));
                }
            }
        }
        this.cf.renameClass(renames);
        this.cf.compact();
        this.className = internalClassName(this.cf.getName());
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
			this.cf.write(new DataOutputStream(baos));
		} catch (IOException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
        this.bytecode = baos.toByteArray();
    }
    
    @Override
    public String getInternalTypeName() {
        return "" + REFERENCE + getClassName() + TYPEEND;
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
    public String getGenericSignatureType() {
    	final SignatureAttribute sa = (SignatureAttribute) this.cf.getAttribute(SignatureAttribute.tag);
    	return sa == null ? null : sa.getSignature();    
    }
    
    @Override
    public int getModifiers() {
        //this code reimplements CtClassType.getModifiers() to circumvent a bug
        int acc = this.cf.getAccessFlags();
        acc = clear(acc, SUPER);
        int inner = this.cf.getInnerAccessFlags();
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
        return this.cf.getAccessFlags();
    }
    
    @Override
    public boolean isDummy() {
        return (this.isAnonymousUnregistered ? this.hostClass == null : this.superInterfaces == null);
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
    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
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
    public boolean isPrimitiveOrVoid() {
        return false;
    }

    @Override
    public boolean isSuperInvoke() {
        //note that we use getClassFile().getAccessFlag() because 
        //getModifiers() does not provide the ACC_SUPER flag
        return ((this.cf.getAccessFlags() & AccessFlag.SUPER) != 0);
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
        boolean hasNumericCode; 
        try {
        	Integer.parseInt(className.substring(lastDollarSignIndex + 1));
        	hasNumericCode = true;
        } catch (NumberFormatException e) {
        	hasNumericCode = false;
        }
        return hasNumericCode;
    }
    
    @Override
    public byte[] getClassAnnotationsRaw() {
        final AttributeInfo attrVisible = this.cf.getAttribute(AnnotationsAttribute.visibleTag);
        final AttributeInfo attrInvisible = this.cf.getAttribute(AnnotationsAttribute.invisibleTag);
        return mergeVisibleAndInvisibleAttributes(attrVisible, attrInvisible);
    }

    @Override
    public ClassFile getMemberClass() {
        return null;
    }
    
    @Override
    public boolean isAnonymousUnregistered() {
        return this.isAnonymousUnregistered;
    }
 
    @Override
    public ClassFile getHostClass() {
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
            final List<FieldInfo> fieldsJA = this.cf.getFields();
            for (FieldInfo fld : fieldsJA) {
                if (Modifier.isStatic(AccessFlag.toModifier(fld.getAccessFlags())) == areStatic) {
                    final Signature sig = new Signature(getClassName(), fld.getDescriptor(), fld.getName());
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
    private MethodInfo findMethodDeclaration(Signature methodSignature) {
        if ("<clinit>".equals(methodSignature.getName())) {
            return this.cf.getStaticInitializer();
        }

        final List<MethodInfo> ms = this.cf.getMethods();
        for (MethodInfo m : ms) {
            final String internalName = m.getName();
            if (internalName.equals(methodSignature.getName()) &&
                m.getDescriptor().equals(methodSignature.getDescriptor())) {
                return m;
            }
        }
        return null;
    }

    private CodeAttribute getMethodCodeAttribute(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) { 
            throw new MethodNotFoundException(methodSignature.toString());
        }
        final CodeAttribute ca = m.getCodeAttribute();
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
        final LocalVariableAttribute lvtJA = (LocalVariableAttribute) ca.getAttribute(LocalVariableAttribute.tag);

        if (lvtJA == null) {
            return defaultLocalVariableTable(methodSignature);
        }

        //builds the local variable table from the LocalVariableTable attribute 
        //information; this has always success
        final LocalVariableTable lvt = new LocalVariableTable(ca.getMaxLocals());
        for (int i = 0; i < lvtJA.tableLength(); ++i) {
            lvt.addRow(lvtJA.index(i), lvtJA.descriptor(i), 
                         lvtJA.variableName(i), lvtJA.startPc(i),  lvtJA.codeLength(i));
        }
        return lvt;
    }
    
    @Override
    public LocalVariableTable getLocalVariableTypeTable(Signature methodSignature)
    throws MethodNotFoundException, MethodCodeNotFoundException {
        final CodeAttribute ca = getMethodCodeAttribute(methodSignature);
        final LocalVariableTypeAttribute lvttJA = (LocalVariableTypeAttribute) ca.getAttribute(LocalVariableTypeAttribute.tag);

        if (lvttJA == null) {
            return new LocalVariableTable(0);
        }

        //builds the local variable type table from the LocalVariableTypeTable attribute 
        //information; this has always success
        final LocalVariableTable lvt = new LocalVariableTable(ca.getMaxLocals());
        for (int i = 0; i < lvttJA.tableLength(); ++i) {
            lvt.addRow(lvttJA.index(i), lvttJA.signature(i), 
                         lvttJA.variableName(i), lvttJA.startPc(i),  lvttJA.codeLength(i));
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
    public ClassFile getSuperclass() {
        return this.superClass;
    }
    
    @Override
    public String getSuperclassName() {
        if (isInterface()) {
            return null;
        } else {
            String name = this.cf.getSuperclass();
            if (name != null) {
                name = internalClassName(name);
            }
            return name;
        }
    }
    
    @Override
    public List<ClassFile> getSuperInterfaces() {
        final List<ClassFile> superinterfaces = Arrays.asList(this.superInterfaces);
        return Collections.unmodifiableList(superinterfaces);
    }

    @Override
    public List<String> getSuperInterfaceNames() {
        final ArrayList<String> superinterfaces = new ArrayList<>();
        final String[] ifs = this.cf.getInterfaces();

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
    
    private MethodInfo findUniqueMethodDeclarationWithName(String methodName) {
        final List<MethodInfo> ms = this.cf.getMethods();
        MethodInfo retVal = null;
        for (MethodInfo m : ms) {
            final String internalName = m.getName();
            if (internalName.equals(methodName)) {
                if (retVal == null) {
                    retVal = m;
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
        final MethodInfo uniqueMethod = findUniqueMethodDeclarationWithName(methodName);
        if (uniqueMethod == null) {
            return false;
        }
        
        //cannot be signature polymorphic if it has wrong descriptor
        if (!SIGNATURE_POLYMORPHIC_DESCRIPTOR.equals(uniqueMethod.getDescriptor())) {
            return false;
        }
        
        //cannot be signature polymorphic if it not native or if it is not varargs
        if (!Modifier.isNative(AccessFlag.toModifier(uniqueMethod.getAccessFlags())) || (AccessFlag.toModifier(uniqueMethod.getAccessFlags()) & Modifier.VARARGS) == 0) {
            return false;
        }

        //all checks passed
        return true;
    }

    @Override
    public boolean hasMethodImplementation(Signature methodSignature) {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        return (m != null && (m.getCodeAttribute() != null || Modifier.isNative(AccessFlag.toModifier(m.getAccessFlags()))));
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(AccessFlag.toModifier(this.cf.getAccessFlags()));
    }
    
    @Override
    public boolean isFinal() {
        return Modifier.isFinal(AccessFlag.toModifier(this.cf.getAccessFlags()));
    }

    @Override
    public boolean isInterface() {
        return this.cf.isInterface();
    }

    @Override
    public boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isAbstract(AccessFlag.toModifier(m.getAccessFlags()));
    }

    @Override
    public boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isNative(AccessFlag.toModifier(m.getAccessFlags()));
    }
    
    @Override
    public boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return (AccessFlag.toModifier(m.getAccessFlags()) & Modifier.VARARGS) != 0;
    }
    
    @Override
    public boolean isMethodFinal(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isFinal(AccessFlag.toModifier(m.getAccessFlags()));
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
    throws MethodNotFoundException {
        final String[] annotations = getMethodAvailableAnnotations(methodSignature);
        for (String annotation : annotations) {
            if (SUN_CALLERSENSITIVE.equals(annotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Signature[] getDeclaredMethods() {
        if (this.methods == null) {
            fillMethodsAndConstructors();
        }
        final Signature[] retVal = new Signature[this.methods.size()];
        this.methods.toArray(retVal);
        return retVal;
    }

    @Override
    public String getMethodGenericSignatureType(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        final SignatureAttribute sa
            = (SignatureAttribute) m.getAttribute(SignatureAttribute.tag);
        return sa == null ? null : sa.getSignature();
    }

    @Override
    public int getMethodModifiers(Signature methodSignature) 
    throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return AccessFlag.toModifier(m.getAccessFlags());
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
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        final AttributeInfo attrVisible = m.getAttribute(AnnotationsAttribute.visibleTag);
        final AttributeInfo attrInvisible = m.getAttribute(AnnotationsAttribute.invisibleTag);
        return mergeVisibleAndInvisibleAttributes(attrVisible, attrInvisible);
    }

    @Override
    public String[] getMethodAvailableAnnotations(Signature methodSignature)
    throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        AnnotationsAttribute ainfo = 
            (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.invisibleTag);  
        AnnotationsAttribute ainfo2 = 
            (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag);
        final ArrayList<String> anno = new ArrayList<>();
        if (ainfo != null) {
            for (Annotation a : ainfo.getAnnotations()) {
                anno.add(internalClassName(a.getTypeName()));
            }
        }
        if (ainfo2 != null) {
            for (Annotation a : ainfo2.getAnnotations()) {
                anno.add(internalClassName(a.getTypeName()));
            }
        }
        return anno.toArray(new String[0]);
    }

    @Override
    public String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation, String parameter) 
    throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        AnnotationsAttribute ainfo = 
            (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.invisibleTag);  
        AnnotationsAttribute ainfo2 = 
            (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag);
        if (ainfo != null) {
            for (Annotation a : ainfo.getAnnotations()) {
                final MemberValue mv = a.getMemberValue(parameter);
                if (mv != null && mv instanceof StringMemberValue) {
                    return ((StringMemberValue) mv).getValue();
                }
            }
        }
        if (ainfo2 != null) {
            for (Annotation a : ainfo2.getAnnotations()) {
                final MemberValue mv = a.getMemberValue(parameter);
                if (mv != null && mv instanceof StringMemberValue) {
                    return ((StringMemberValue) mv).getValue();
                }
            }
        }
        return null;
    }
    
    @Override
    public String[] getMethodThrownExceptions(Signature methodSignature) 
    throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }

        final ExceptionsAttribute exc = m.getExceptionsAttribute();
        if (exc == null) {
            return new String[0];
        }
        return Arrays.stream(exc.getExceptions()).map(Type::internalClassName).toArray(String[]::new);
    }

    @Override
    public boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isStatic(AccessFlag.toModifier(m.getAccessFlags()));
    }

    @Override
    public boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPublic(AccessFlag.toModifier(m.getAccessFlags()));
    }

    @Override
    public boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isProtected(AccessFlag.toModifier(m.getAccessFlags()));
    }

    @Override
    public boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPackage(AccessFlag.toModifier(m.getAccessFlags()));
    }

    @Override
    public boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException {
        final MethodInfo m = findMethodDeclaration(methodSignature);
        if (m == null) {
            throw new MethodNotFoundException(methodSignature.toString());
        }
        return Modifier.isPrivate(AccessFlag.toModifier(m.getAccessFlags()));
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

        if (lnJA == null) {
            return defaultLineNumberTable();
        }
        final LineNumberTable LN = new LineNumberTable(lnJA.tableLength());
        for (int i = 0; i < lnJA.tableLength(); ++i) {
            LN.addRow(lnJA.startPc(i), lnJA.lineNumber(i));
        }
        return LN;
    }

    @Override
    public int fieldConstantValueIndex(Signature fieldSignature) throws FieldNotFoundException, AttributeNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        final int cpVal = fld.getConstantValue();
        if (cpVal == 0) {
            throw new AttributeNotFoundException();
        }
        return cpVal;
    }

    @Override
    public boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return (fld.getConstantValue() != 0);
    }

    @Override
    public boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isFinal(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPublic(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isProtected(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPackage(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isPrivate(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return Modifier.isStatic(AccessFlag.toModifier(fld.getAccessFlags()));
    }

    @Override
    public String getFieldGenericSignatureType(Signature fieldSignature) 
    throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        SignatureAttribute sa = (SignatureAttribute) fld.getAttribute(SignatureAttribute.tag);
        return (sa == null ? null : sa.getSignature());
    }

    @Override
    public int getFieldModifiers(Signature fieldSignature) 
    throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        return AccessFlag.toModifier(fld.getAccessFlags());
    }

    @Override
    public byte[] getFieldAnnotationsRaw(Signature fieldSignature) 
    throws FieldNotFoundException {
        final FieldInfo fld = findField(fieldSignature);
        if (fld == null) {
            throw new FieldNotFoundException(fieldSignature.toString());
        }
        final AttributeInfo attrVisible = fld.getAttribute(AnnotationsAttribute.visibleTag);
        final AttributeInfo attrInvisible = fld.getAttribute(AnnotationsAttribute.invisibleTag);
        return mergeVisibleAndInvisibleAttributes(attrVisible, attrInvisible);
    }

    private FieldInfo findField(Signature fieldSignature) {
        final List<FieldInfo> fieldsJA = this.cf.getFields();
        for (FieldInfo fld : fieldsJA) {
            if (fld.getDescriptor().equals(fieldSignature.getDescriptor()) && 
                fld.getName().equals(fieldSignature.getName())) {
                return fld;
            }
        }
        return null;
    }
    
    private void fillMethodsAndConstructors() {
        this.methods = new ArrayList<>();
        this.constructors = new ArrayList<>();
        final List<MethodInfo> ms = this.cf.getMethods();
        for (MethodInfo m : ms) {
            final Signature sig = new Signature(getClassName(), m.getDescriptor(), m.getName());
            this.methods.add(sig);
            if (m.isConstructor()) {
                this.constructors.add(sig);
            }
        }
    }

    @Override
    public Signature[] getDeclaredConstructors() {
        if (this.constructors == null) {
            fillMethodsAndConstructors();
        }
        final Signature[] retVal = new Signature[this.constructors.size()];
        this.constructors.toArray(retVal);
        return retVal;
    }

    @Override
    public String classContainer() {
        //taken from Javassist, method javassist.CtClassType.getDeclaringClass()
        final InnerClassesAttribute ica = 
            (InnerClassesAttribute) this.cf.getAttribute(InnerClassesAttribute.tag);
        if (ica == null) {
            return null;
        }

        final String name = getClassName();
        final int n = ica.tableLength();
        for (int i = 0; i < n; ++i)
            if (name.equals(internalClassName(ica.innerClass(i)))) {
                final String outName = ica.outerClass(i);
                if (outName != null) {
                    return internalClassName(outName);                    
                } else {
                    // maybe anonymous or local class.
                    final EnclosingMethodAttribute ema =
                        (EnclosingMethodAttribute) this.cf.getAttribute(EnclosingMethodAttribute.tag);
                    if (ema != null) {
                        return internalClassName(ema.className()); //filtering through internalClassName is for safety (it is unclear what Javassist returns)
                    }
                }
            }

        return null;
    }
    
    @Override
    public Signature getEnclosingMethodOrConstructor() {
        //taken from Javassist, method javassist.CtClassType.getEnclosingBehavior().
        final EnclosingMethodAttribute ema = 
            (EnclosingMethodAttribute) this.cf.getAttribute(EnclosingMethodAttribute.tag);
        if (ema == null) {
            return null;
        }
        return new Signature(internalClassName(ema.className()), ema.methodDescriptor(), ema.methodName());
    }
}