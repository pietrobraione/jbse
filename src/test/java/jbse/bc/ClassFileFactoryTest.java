package jbse.bc;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.junit.BeforeClass;
import org.junit.Test;

import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.InvalidInputException;

public class ClassFileFactoryTest {
    private static ClassFileFactory f;

    @BeforeClass
    public static void setUpClass() {
        //Javassist
        f = new ClassFileFactoryJavassist(); 
    }
    
    private static byte[] getFromJar(String className) throws IOException {
        try (final JarFile jarFile = new JarFile("src/test/resources/jbse/bc/testdata/lib/rt.jar")) {
            final JarEntry jarEntry = jarFile.getJarEntry(className + ".class");
            final InputStream inStr = jarFile.getInputStream(jarEntry);
            final ByteArrayOutputStream outStr = new ByteArrayOutputStream();
            final byte[] buf = new byte[2048];
            int nbytes;
            while ((nbytes = inStr.read(buf)) != -1) {
                outStr.write(buf, 0, nbytes);
            }
            return outStr.toByteArray();
        }
    }
    
    private static byte[] getFromFile(String className) throws IOException {
        final Path path = Paths.get("src/test/resources/jbse/bc/testdata", className + ".class"); 
        return Files.readAllBytes(path);
    }

    @Test
    public void testNewClassFile1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/util/LinkedList$ListItr";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        assertNotNull(c);
    }

    @Test
    public void testGetClassName1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);		
        assertEquals(className, c.getClassName());
    }

    @Test
    public void testGetClassName2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "tsafe/main/SimpleCalculator";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals(className, c.getClassName());
    }

    @Test
    public void testGetClassSignature1() throws InvalidIndexException, IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "tsafe/main/SimpleCalculator";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        int i = 1; //entry 1 in constant pool should be tsafe/main/SimpleCalculator
        assertEquals(className, c.getClassSignature(i));
    }		

    @Test(expected=InvalidIndexException.class)
    public void testGetClassSignature2() throws InvalidIndexException, IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "tsafe/engine/TsafeEngine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getClassSignature(10);
    }		

    @Test
    public void testGetSuperClassName1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "tsafe/main/SimpleCalculator";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals("tsafe/engine/EngineCalculator", c.getSuperclassName());
    }

    @Test
    public void testGetSuperClassName2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals("java/lang/Object", c.getSuperclassName());
    }

    @Test
    public void testGetSuperClassName3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertNull(c.getSuperclassName());
    }

    @Test
    public void testGetSuperInterfaceNames1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         

        HashSet<String> ifnsExp = new HashSet<String>();
        ifnsExp.add("java/util/List");
        ifnsExp.add("java/util/Deque");
        ifnsExp.add("java/lang/Cloneable");
        ifnsExp.add("java/io/Serializable");

        HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());

        assertEquals(ifnsExp, ifnsAct);
    }

    @Test
    public void testGetSuperInterfaceNames2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/io/Serializable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         

        HashSet<String> ifnsExp = new HashSet<String>();
        HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());
        assertEquals(ifnsExp, ifnsAct);
    }

    @Test
    public void testGetSuperInterfaceNames3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         

        HashSet<String> ifnsExp = new HashSet<String>();
        HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());
        assertEquals(ifnsExp, ifnsAct);
    }

    /**
     * Method not declared in the class, but declared in some superclass
     */
    @Test(expected=MethodNotFoundException.class)
    public void testGetExceptionTable1() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/String;", "toString");
        c.getExceptionTable(sig);
    }

    /**
     * Method declared, but without code
     */
    @Test(expected=MethodCodeNotFoundException.class)
    public void testGetExceptionTable2() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "run");
        c.getExceptionTable(sig);
    }

    /**
     * Method with bytecode.
     */
    @Test
    public void testGetExceptionTable3() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljsymba/tree/StateTree$BranchPoint;", "step");

        //we use strings because ExceptionTableEntry does not override equals()

        HashSet<String> etExp = new HashSet<String>();
        etExp.add("15,240,241,jsymba/dec/DecisionException");

        ExceptionTable et = c.getExceptionTable(sig);
        HashSet<String> etAct = new HashSet<String>();
        for (int k = 0; k < et.getLength(); k++) {
            ArrayList<String> ls = new ArrayList<String>();
            ls.add("jsymba/dec/DecisionException");
            ExceptionTableEntry e = et.getEntry(ls, 15);
            etAct.add(e.getStartPC() + "," + e.getEndPC() + "," + e.getPCHandle() + "," + e.getType());
        }
        assertEquals(etExp, etAct);
    }

    /**
     * Native method.
     */
    @Test(expected=MethodCodeNotFoundException.class)
    public void testGetExceptionTable4() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
        c.getExceptionTable(sig);
    }

    @Test
    public void testGetValueFromConstantPool1() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals(new ConstantPoolPrimitive(999999), c.getValueFromConstantPool(13));
    }

    @Test
    public void testGetValueFromConstantPool2() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Math";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals(new ConstantPoolPrimitive(180.0d), c.getValueFromConstantPool(8));
    }

    @Test
    public void testGetValueFromConstantPool3() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Math";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals(new ConstantPoolPrimitive(Math.E), c.getValueFromConstantPool(119));
    }

    @Test
    public void testGetValueFromConstantPool4() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Long";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertEquals(new ConstantPoolPrimitive(Long.MAX_VALUE), c.getValueFromConstantPool(125));
    }

    /**
     * Constant pool index out of bounds.
     */
    @Test(expected=InvalidIndexException.class)
    public void testGetValueFromConstantPool5() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Long";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getValueFromConstantPool(300);
    }

    /**
     * Method not declared in the class, but declared in some superclass.
     */
    @Test
    public void testHasMethodDeclaration1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/String;", "toString");
        assertFalse(c.hasMethodDeclaration(sig));
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test
    public void testHasMethodDeclaration2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "foo");
        assertFalse(c.hasMethodDeclaration(sig));
    }

    /**
     * Method declared in abstract class, but not implemented in it.
     */
    @Test
    public void testHasMethodDeclaration3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "isInterface");
        assertTrue(c.hasMethodDeclaration(sig));
    }

    /**
     * Method declared and implemented in abstract class.
     */
    @Test
    public void testHasMethodDeclaration4() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");
        assertTrue(c.hasMethodDeclaration(sig));
    }

    /**
     * Method declared and implemented in concrete class.
     */
    @Test
    public void testHasMethodDeclaration5() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");
        assertTrue(c.hasMethodDeclaration(sig));
    }

    /**
     * Method declared in interface.
     */
    @Test
    public void testHasMethodDeclaration6() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "run");
        assertTrue(c.hasMethodDeclaration(sig));
    }

    /**
     * Native method.
     */
    @Test
    public void testHasMethodDeclaration7() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "notifyAll");
        assertTrue(c.hasMethodDeclaration(sig));
    }

    /**
     * Method not declared in the class, but declared in some superclass.
     */
    @Test
    public void testHasMethodImplementation1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/String;", "toString");

        assertFalse(c.hasMethodImplementation(sig));
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test
    public void testHasMethodImplementation2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "foo");

        assertFalse(c.hasMethodImplementation(sig));
    }

    /**
     * Method declared in abstract class, but not implemented in it.
     */
    @Test
    public void testHasMethodImplementation3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "isInterface");

        assertFalse(c.hasMethodImplementation(sig));
    }

    /**
     * Method declared and implemented in abstract class.
     */
    @Test
    public void testHasMethodImplementation4() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");

        assertTrue(c.hasMethodImplementation(sig));
    }

    /**
     * Method declared and implemented in concrete class.
     */
    @Test
    public void testHasMethodImplementation5() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");

        assertTrue(c.hasMethodImplementation(sig));
    }

    /**
     * Method declared in interface.
     */
    @Test
    public void testHasMethodImplementation6() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "run");

        assertFalse(c.hasMethodImplementation(sig));
    }

    /**
     * Native method.
     */
    @Test
    public void testHasMethodImplementation7() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "notifyAll");

        assertTrue(c.hasMethodImplementation(sig));
    }

    @Test
    public void testIsInterface1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertFalse(c.isInterface());
    }

    @Test
    public void testIsInterface2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertTrue(c.isInterface());
    }

    @Test
    public void testIsAbstract1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertFalse(c.isAbstract());
    }

    @Test
    public void testIsAbstract2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertTrue(c.isAbstract());
    }

    @Test
    public void testIsAbstract3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertTrue(c.isAbstract());
    }

    /** 
     * public class
     */
    @Test
    public void testIsPublic1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        assertTrue(c.isPublic());
        assertFalse(c.isProtected());
        assertFalse(c.isPackage());
        assertFalse(c.isPrivate());
    }

    /**
     * private nested class
     */
    @Test
    public void testIsPublic2() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/util/LinkedList$ListItr";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        assertFalse(c.isPublic());
        assertFalse(c.isProtected());
        assertTrue(c.isPackage());
        assertFalse(c.isPrivate());
    }

    /**
     * public nested class
     */
    @Test
    public void testIsPublic3() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/util/concurrent/ThreadPoolExecutor$AbortPolicy";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        assertTrue(c.isPublic());
        assertFalse(c.isProtected());
        assertFalse(c.isPackage());
        assertFalse(c.isPrivate());
    }

    /**
     * (package) class
     */
    @Test
    public void testIsPublic4() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/util/regex/ASCII";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        assertFalse(c.isPublic());
        assertFalse(c.isProtected());
        assertTrue(c.isPackage());
        assertFalse(c.isPrivate());
    }

    @Test
    public void testIsSuperInvoke1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Class";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        assertTrue(c.isSuperInvoke());
    }

    /**
     * Method not declared in the class, but declared in some superclass.
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodAbstract1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/String;", "toString");

        c.isMethodAbstract(sig);
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodAbstract2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "foo");

        c.isMethodAbstract(sig);
    }

    /**
     * Method declared in abstract class, but not implemented in it.
     */
    @Test
    public void testIsMethodAbstract3() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "isInterface");

        assertTrue(c.isMethodAbstract(sig));
    }

    /**
     * Method declared and implemented in abstract class.
     */
    @Test
    public void testIsMethodAbstract4() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");

        assertFalse(c.isMethodAbstract(sig));
    }

    /**
     * Method declared and implemented in concrete class.
     */
    @Test
    public void testIsMethodAbstract5() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");

        assertFalse(c.isMethodAbstract(sig));
    }

    /**
     * Method declared in interface.
     */
    @Test
    public void testIsMethodAbstract6() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "run");

        assertTrue(c.isMethodAbstract(sig));
    }

    /**
     * Method not declared in the class, but declared in some superclass 
     * (not native in the superclass).
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodNative1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/String;", "toString");

        c.isMethodNative(sig);
    }

    /**
     * Method not declared in the class, but declared in some superclass 
     * (native in the superclass).
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodNative2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/Class;", "getClass");

        c.isMethodNative(sig);
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodNative3() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "baz");

        c.isMethodNative(sig);
    }

    /**
     * Method declared in abstract class, but not implemented in it.
     */
    @Test
    public void testIsMethodNative4() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "isInterface");

        assertFalse(c.isMethodNative(sig));
    }

    /**
     * Method declared and implemented in abstract class, non native.
     */
    @Test
    public void testIsMethodNative5() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/bc/ClassFile";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "equals");

        assertFalse(c.isMethodNative(sig));
    }

    /**
     * Method declared and implemented in concrete class, native.
     */
    @Test
    public void testIsMethodNative6() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Shutdown";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "runAllFinalizers");

        assertTrue(c.isMethodNative(sig));
    }

    /**
     * Method declared and implemented in concrete class, non native.
     */
    @Test
    public void testIsMethodNative7() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Class";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);
        Signature sig = new Signature(className, "([Ljava/lang/Class;)Ljava/lang/String;", "argumentTypesToString");

        assertFalse(c.isMethodNative(sig));
    }

    /**
     * Method declared in interface.
     */
    @Test
    public void testIsMethodNative8() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Runnable";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "run");
        assertFalse(c.isMethodNative(sig));
    }

    /**
     * Static method declared in class.
     */
    @Test
    public void testIsMethodNative9() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "registerNatives");
        assertTrue(c.isMethodNative(sig));
    }

    /**
     * Method not declared in the class, but declared in some superclass
     * (not static in the superclass).
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodStatic1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/Class;", "getClass");
        c.isMethodStatic(sig);
    }

    /**
     * Method not declared in the class, but declared in some superclass
     * (static in the superclass).
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodStatic2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "registerNatives");
        c.isMethodStatic(sig);
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test(expected=MethodNotFoundException.class)
    public void testIsMethodStatic3() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "jsymba/jvm/Engine";
        byte[] b = getFromFile(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Z", "baz");
        c.isMethodStatic(sig);
    }

    /**
     * Method declared in the class, not static.
     */
    @Test
    public void testIsMethodStatic4() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()Ljava/lang/Class;", "getClass");
        assertFalse(c.isMethodStatic(sig));
    }

    /**
     * Method declared in the class, static.
     */
    @Test
    public void testIsMethodStatic5() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException {
        String className = "java/lang/Class";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "registerNatives");
        assertTrue(c.isMethodStatic(sig));
    }

    @Test
    public void testGetFieldSignature1() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Byte";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "B", "value");
        assertEquals(sig, c.getFieldSignature(22));
    }

    @Test(expected=InvalidIndexException.class)
    public void testGetFieldSignature2() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Byte";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getFieldSignature(1);
    }

    @Test(expected=InvalidIndexException.class)
    public void testGetFieldSignature3() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/lang/Byte";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getFieldSignature(200);
    }

    @Test
    public void testGetMethodSignature1() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature("java/io/ObjectOutputStream", "()V", "defaultWriteObject");
        assertEquals(sig, c.getMethodSignature(61));
    }

    @Test(expected=InvalidIndexException.class)
    public void testGetMethodSignature2() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getMethodSignature(0);
    }

    @Test
    public void testGetInterfaceMethodSignature1() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature("java/util/Collection", "()[Ljava/lang/Object;", "toArray");
        assertEquals(sig, c.getInterfaceMethodSignature(24));
    }

    @Test(expected=InvalidIndexException.class)
    public void testGetInterfaceMethodSignature2() throws IOException, ClassFileIllFormedException, InvalidInputException, InvalidIndexException  {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        c.getInterfaceMethodSignature(227);
    }

    @Test
    public void testGetFieldsNonStatic1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Boolean";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         

        //oddly, with HashSet<Signature> it does not work even with 
        //Signature.equals defined, so we use HashSet<String> instead

        HashSet<String> fldsExp = new HashSet<String>();
        fldsExp.add("java/lang/Boolean:Z:value");

        Signature[] flds = c.getDeclaredFieldsNonStatic();
        HashSet<String> fldsAct = new HashSet<String>();
        for (Signature sig: flds) fldsAct.add(sig.toString());

        assertEquals(fldsExp, fldsAct);
    }

    @Test
    public void testGetFieldsStatic1() throws IOException, ClassFileIllFormedException, InvalidInputException {
        String className = "java/lang/Boolean";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         

        //oddly, with HashSet<Signature> it does not work even with 
        //Signature.equals defined, so we use HashSet<String> instead

        HashSet<String> fldsExp = new HashSet<String>();
        fldsExp.add("java/lang/Boolean:Ljava/lang/Boolean;:TRUE");
        fldsExp.add("java/lang/Boolean:Ljava/lang/Boolean;:FALSE");
        fldsExp.add("java/lang/Boolean:Ljava/lang/Class;:TYPE");
        fldsExp.add("java/lang/Boolean:J:serialVersionUID");

        Signature[] flds = c.getDeclaredFieldsStatic();
        HashSet<String> fldsAct = new HashSet<String>();
        for (Signature sig: flds) fldsAct.add(sig.toString());

        assertEquals(fldsExp, fldsAct);
    }

    @Test
    public void testGetLocalVariableLength1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)V", "addFirst");
        assertEquals(2, c.getLocalVariableTableLength(sig));
    }

    @Test(expected=MethodNotFoundException.class)
    public void testGetLocalVariableLength2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "foo");
        c.getLocalVariableTableLength(sig);
    }

    @Test(expected=MethodCodeNotFoundException.class)
    public void testGetLocalVariableLength3() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/util/Collection";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(Ljava/lang/Object;)Z", "remove");
        c.getLocalVariableTableLength(sig);
    }

    @Test
    public void testGetCodeLength1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/util/LinkedList";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "<init>");
        assertEquals(10, c.getCodeLength(sig));
    }

    @Test
    public void testGetCodeLength2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "<clinit>");
        assertEquals(4, c.getCodeLength(sig));
    }

    /**
     * Native method.
     * @throws MethodCodeNotFoundException 
     * @throws MethodNotFoundException 
     */
    @Test(expected=MethodCodeNotFoundException.class)
    public void testGetCodeLength3() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "notifyAll");
        c.getCodeLength(sig);
    }

    /**
     * Method not declared in the class neither in some superclass.
     */
    @Test(expected=MethodNotFoundException.class)
    public void testGetMethodCodeBySignature1() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "(I)Z", "foo");
        c.getMethodCodeBySignature(sig);
    }

    /**
     * Native method.
     */
    @Test(expected=MethodCodeNotFoundException.class)
    public void testGetMethodCodeBySignature2() throws IOException, ClassFileIllFormedException, InvalidInputException, MethodNotFoundException, MethodCodeNotFoundException {
        String className = "java/lang/Object";
        byte[] b = getFromJar(className);
        ClassFile c = f.newClassFileClass(0, className, b, null, null);         
        Signature sig = new Signature(className, "()V", "notifyAll");
        c.getMethodCodeBySignature(sig);
    }

    //TODO hasField/getLocalVariableTable/getMethodCodeBySignature (to complete), array classes
}
