package jbse.bc;

import static org.junit.Assert.*;

import org.junit.*;

import java.util.ArrayList;
import java.util.HashSet;

import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;

public class ClassFileFactoryTest {
	ClassFileFactory f;

	@Before
	public void setUp() {
		//environment
		Classpath env = new Classpath("src/test/resources/jbse/bc/testdata/rt.jar", "src/test/resources/jbse/bc/testdata");

		//Javassist
		this.f = new ClassFileFactoryJavassist(null, env); 
		//stubbed ClassFileInterface with null because no array classes are involved
	}

	@Test
	public void testNewClassFile1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/util/LinkedList$ListItr");
		assertNotNull(c);
	}

	@Test
	public void testGetClassName1() throws BadClassFileException {
		String className = "java/lang/Object";
		ClassFile c = f.newClassFile(className);		
		assertEquals(className, c.getClassName());
	}

	@Test
	public void testGetClassName2() throws BadClassFileException {
		String className = "tsafe/main/SimpleCalculator";
		ClassFile c = f.newClassFile(className);
		assertEquals(className, c.getClassName());
	}

	@Test
	public void testGetClassSignature1() throws BadClassFileException, InvalidIndexException {
		String className = "tsafe/main/SimpleCalculator";
		ClassFile c = f.newClassFile(className);
		int i = 1; //entry 1 in constant pool should be tsafe/main/SimpleCalculator
		assertEquals(className, c.getClassSignature(i));
	}		

	@Test(expected=InvalidIndexException.class)
	public void testGetClassSignature2() throws BadClassFileException, InvalidIndexException {
		String className = "tsafe/engine/TsafeEngine";
		ClassFile c = f.newClassFile(className);
		c.getClassSignature(10);
	}		

	@Test
	public void testGetSuperClassName1() throws BadClassFileException {
		String className = "tsafe/main/SimpleCalculator";
		ClassFile c = f.newClassFile(className);
		assertEquals("tsafe/engine/EngineCalculator", c.getSuperClassName());
	}

	@Test
	public void testGetSuperClassName2() throws BadClassFileException {
		String className = "jsymba/jvm/Engine";
		ClassFile c = f.newClassFile(className);
		assertEquals("java/lang/Object", c.getSuperClassName());
	}

	@Test
	public void testGetSuperClassName3() throws BadClassFileException {
		String className = "java/lang/Object";
		ClassFile c = f.newClassFile(className);
		assertNull(c.getSuperClassName());
	}

	@Test
	public void testGetSuperInterfaceNames1() throws BadClassFileException {
		String className = "java/util/LinkedList";
		ClassFile c = f.newClassFile(className);

		HashSet<String> ifnsExp = new HashSet<String>();
		ifnsExp.add("java/util/List");
		ifnsExp.add("java/util/Queue");
		ifnsExp.add("java/lang/Cloneable");
		ifnsExp.add("java/io/Serializable");

		HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());

		assertEquals(ifnsExp, ifnsAct);
	}

	@Test
	public void testGetSuperInterfaceNames2() throws BadClassFileException {
		String className = "java/io/Serializable";
		ClassFile c = f.newClassFile(className);

		HashSet<String> ifnsExp = new HashSet<String>();
		HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());
		assertEquals(ifnsExp, ifnsAct);
	}

	@Test
	public void testGetSuperInterfaceNames3() throws BadClassFileException {
		String className = "java/lang/Object";
		ClassFile c = f.newClassFile(className);

		HashSet<String> ifnsExp = new HashSet<String>();
		HashSet<String> ifnsAct = new HashSet<String>(c.getSuperInterfaceNames());
		assertEquals(ifnsExp, ifnsAct);
	}

	/**
	 * Method not declared in the class, but declared in some superclass
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testGetExceptionTable1() throws BadClassFileException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/String;", "toString");
		c.getExceptionTable(sig);
	}

	/**
	 * Method declared, but without code
	 */
	@Test(expected=MethodCodeNotFoundException.class)
	public void testGetExceptionTable2() throws BadClassFileException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Runnable");
		Signature sig = new Signature("java/lang/Runnable", "()V", "run");
		c.getExceptionTable(sig);
	}

	/**
	 * Method with bytecode.
	 */
	@Test
	public void testGetExceptionTable3() throws BadClassFileException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljsymba/tree/StateTree$BranchPoint;", "step");

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
	public void testGetExceptionTable4() throws BadClassFileException, InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
		c.getExceptionTable(sig);
	}

	@Test
	public void testGetValueFromConstantPool1() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Object");
		assertEquals(new ConstantPoolPrimitive(500000), c.getValueFromConstantPool(1));
	}

	@Test
	public void testGetValueFromConstantPool2() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Math");
		assertEquals(new ConstantPoolPrimitive(0.5f), c.getValueFromConstantPool(2));
	}

	@Test
	public void testGetValueFromConstantPool3() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Math");
		assertEquals(new ConstantPoolPrimitive(Math.E), c.getValueFromConstantPool(73));
	}

	@Test
	public void testGetValueFromConstantPool4() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Long");
		assertEquals(new ConstantPoolPrimitive(Long.MAX_VALUE), c.getValueFromConstantPool(150));
	}

	/**
	 * Constant pool index out of bounds.
	 */
	@Test(expected=InvalidIndexException.class)
	public void testGetValueFromConstantPool5() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Long");
		c.getValueFromConstantPool(300);
	}

	/**
	 * Method not declared in the class, but declared in some superclass.
	 */
	@Test
	public void testHasMethodDeclaration1() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/String;", "toString");
		assertFalse(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test
	public void testHasMethodDeclaration2() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "foo");
		assertFalse(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method declared in abstract class, but not implemented in it.
	 */
	@Test
	public void testHasMethodDeclaration3() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");
		Signature sig = new Signature("jsymba/bc/ClassFile", "()Z", "isInterface");
		assertTrue(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method declared and implemented in abstract class.
	 */
	@Test
	public void testHasMethodDeclaration4() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");
		Signature sig = new Signature("jsymba/bc/ClassFile", "(Ljava/lang/Object;)Z", "equals");
		assertTrue(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method declared and implemented in concrete class.
	 */
	@Test
	public void testHasMethodDeclaration5() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "(Ljava/lang/Object;)Z", "equals");
		assertTrue(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method declared in interface.
	 */
	@Test
	public void testHasMethodDeclaration6() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Runnable");
		Signature sig = new Signature("java/lang/Runnable", "()V", "run");
		assertTrue(c.hasMethodDeclaration(sig));
	}

	/**
	 * Native method.
	 */
	@Test
	public void testHasMethodDeclaration7() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
		assertTrue(c.hasMethodDeclaration(sig));
	}

	/**
	 * Method not declared in the class, but declared in some superclass.
	 */
	@Test
	public void testHasMethodImplementation1() throws BadClassFileException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/String;", "toString");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		assertFalse(c.hasMethodImplementation(sig));
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test
	public void testHasMethodImplementation2() throws BadClassFileException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "foo");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		assertFalse(c.hasMethodImplementation(sig));
	}

	/**
	 * Method declared in abstract class, but not implemented in it.
	 */
	@Test
	public void testHasMethodImplementation3() throws BadClassFileException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "()Z", "isInterface");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertFalse(c.hasMethodImplementation(sig));
	}

	/**
	 * Method declared and implemented in abstract class.
	 */
	@Test
	public void testHasMethodImplementation4() throws BadClassFileException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "(Ljava/lang/Object;)Z", "equals");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertTrue(c.hasMethodImplementation(sig));
	}

	/**
	 * Method declared and implemented in concrete class.
	 */
	@Test
	public void testHasMethodImplementation5() throws BadClassFileException {
		Signature sig = new Signature("java/lang/Object", "(Ljava/lang/Object;)Z", "equals");
		ClassFile c = f.newClassFile("java/lang/Object");

		assertTrue(c.hasMethodImplementation(sig));
	}

	/**
	 * Method declared in interface.
	 */
	@Test
	public void testHasMethodImplementation6() throws BadClassFileException {
		Signature sig = new Signature("java/lang/Runnable", "()V", "run");
		ClassFile c = f.newClassFile("java/lang/Runnable");

		assertFalse(c.hasMethodImplementation(sig));
	}

	/**
	 * Native method.
	 */
	@Test
	public void testHasMethodImplementation7() throws BadClassFileException {
		Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
		ClassFile c = f.newClassFile("java/lang/Object");

		assertTrue(c.hasMethodImplementation(sig));
	}

	@Test
	public void testIsInterface1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Object");
		assertFalse(c.isInterface());
	}

	@Test
	public void testIsInterface2() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Runnable");
		assertTrue(c.isInterface());
	}

	@Test
	public void testIsAbstract1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Object");
		assertFalse(c.isAbstract());
	}

	@Test
	public void testIsAbstract2() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Runnable");
		assertTrue(c.isAbstract());
	}

	@Test
	public void testIsAbstract3() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");
		assertTrue(c.isAbstract());
	}

	/** 
	 * public class
	 */
	@Test
	public void testIsPublic1() throws BadClassFileException {
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");
		assertTrue(c.isPublic());
	}

	/**
	 * private nested class
	 */
	@Test
	public void testIsPublic2() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/util/LinkedList$ListItr");
		assertFalse(c.isPublic());
	}

	/**
	 * public nested class
	 */
	@Test
	public void testIsPublic3() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/util/concurrent/ThreadPoolExecutor$AbortPolicy");
		assertTrue(c.isPublic());
	}

	/**
	 * (package) class
	 */
	@Test
	public void testIsPublic4() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/util/regex/ASCII");
		assertFalse(c.isPublic());
	}
	
	@Test
	public void testIsSuperInvoke1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Class");
		assertTrue(c.isSuperInvoke());
	}

	/**
	 * Method not declared in the class, but declared in some superclass.
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodAbstract1() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/String;", "toString");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		c.isMethodAbstract(sig);
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodAbstract2() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "foo");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		c.isMethodAbstract(sig);
	}

	/**
	 * Method declared in abstract class, but not implemented in it.
	 */
	@Test
	public void testIsMethodAbstract3() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "()Z", "isInterface");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertTrue(c.isMethodAbstract(sig));
	}

	/**
	 * Method declared and implemented in abstract class.
	 */
	@Test
	public void testIsMethodAbstract4() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "(Ljava/lang/Object;)Z", "equals");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertFalse(c.isMethodAbstract(sig));
	}

	/**
	 * Method declared and implemented in concrete class.
	 */
	@Test
	public void testIsMethodAbstract5() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("java/lang/Object", "(Ljava/lang/Object;)Z", "equals");
		ClassFile c = f.newClassFile("java/lang/Object");

		assertFalse(c.isMethodAbstract(sig));
	}

	/**
	 * Method declared in interface.
	 */
	@Test
	public void testIsMethodAbstract6() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("java/lang/Runnable", "()V", "run");
		ClassFile c = f.newClassFile("java/lang/Runnable");

		assertTrue(c.isMethodAbstract(sig));
	}

	/**
	 * Method not declared in the class, but declared in some superclass 
	 * (not native in the superclass).
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodNative1() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/String;", "toString");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		c.isMethodNative(sig);
	}

	/**
	 * Method not declared in the class, but declared in some superclass 
	 * (native in the superclass).
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodNative2() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/Class;", "getClass");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		c.isMethodNative(sig);
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodNative3() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "baz");
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");

		c.isMethodNative(sig);
	}

	/**
	 * Method declared in abstract class, but not implemented in it.
	 */
	@Test
	public void testIsMethodNative4() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "()Z", "isInterface");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertFalse(c.isMethodNative(sig));
	}

	/**
	 * Method declared and implemented in abstract class, non native.
	 */
	@Test
	public void testIsMethodNative5() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("jsymba/bc/ClassFile", "(Ljava/lang/Object;)Z", "equals");
		ClassFile c = f.newClassFile("jsymba/bc/ClassFile");

		assertFalse(c.isMethodNative(sig));
	}

	/**
	 * Method declared and implemented in concrete class, native.
	 */
	@Test
	public void testIsMethodNative6() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("java/lang/Shutdown", "()V", "runAllFinalizers");
		ClassFile c = f.newClassFile("java/lang/Shutdown");

		assertTrue(c.isMethodNative(sig));
	}

	/**
	 * Method declared and implemented in concrete class, non native.
	 */
	@Test
	public void testIsMethodNative7() throws BadClassFileException, MethodNotFoundException {
		Signature sig = new Signature("java/lang/Class", "([Ljava/lang/Class;)Ljava/lang/String;", "argumentTypesToString");
		ClassFile c = f.newClassFile("java/lang/Class");

		assertFalse(c.isMethodNative(sig));
	}
	
	/**
	 * Method declared in interface.
	 */
	@Test
	public void testIsMethodNative8() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Runnable");
		Signature sig = new Signature("java/lang/Runnable", "()V", "run");
		assertFalse(c.isMethodNative(sig));
	}

	/**
	 * Static method declared in class.
	 */
	@Test
	public void testIsMethodNative9() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "registerNatives");
		assertTrue(c.isMethodNative(sig));
	}
	
	/**
	 * Method not declared in the class, but declared in some superclass
	 * (not static in the superclass).
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodStatic1() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Ljava/lang/Class;", "getClass");
		c.isMethodStatic(sig);
	}

	/**
	 * Method not declared in the class, but declared in some superclass
	 * (static in the superclass).
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodStatic2() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()V", "registerNatives");
		c.isMethodStatic(sig);
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testIsMethodStatic3() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("jsymba/jvm/Engine");
		Signature sig = new Signature("jsymba/jvm/Engine", "()Z", "baz");
		c.isMethodStatic(sig);
	}
	
	/**
	 * Method declared in the class, not static.
	 */
	@Test
	public void testIsMethodStatic4() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()Ljava/lang/Class;", "getClass");
		assertFalse(c.isMethodStatic(sig));
	}
	
	/**
	 * Method declared in the class, static.
	 */
	@Test
	public void testIsMethodStatic5() throws BadClassFileException, MethodNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Class");
		Signature sig = new Signature("java/lang/Class", "()V", "registerNatives");
		assertTrue(c.isMethodStatic(sig));
	}
	
	@Test
	public void testGetFieldSignature1() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Byte");
		Signature sig = new Signature("java/lang/Byte", "B", "value");
		assertEquals(sig, c.getFieldSignature(130));
	}
	
	@Test(expected=InvalidIndexException.class)
	public void testGetFieldSignature2() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Byte");
		c.getFieldSignature(1);
	}
	
	@Test(expected=InvalidIndexException.class)
	public void testGetFieldSignature3() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/lang/Byte");
		c.getFieldSignature(200);
	}

	@Test
	public void testGetMethodSignature1() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		Signature sig = new Signature("java/io/ObjectOutputStream", "()V", "defaultWriteObject");
		assertEquals(sig, c.getMethodSignature(200));
	}
	
	@Test(expected=InvalidIndexException.class)
	public void testGetMethodSignature2() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		c.getMethodSignature(0);
	}
	
	@Test
	public void testGetInterfaceMethodSignature1() throws BadClassFileException, InvalidIndexException {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		Signature sig = new Signature("java/util/Collection", "()[Ljava/lang/Object;", "toArray");
		assertEquals(sig, c.getInterfaceMethodSignature(228));
	}
	
	@Test(expected=InvalidIndexException.class)
	public void testGetInterfaceMethodSignature2() throws BadClassFileException, InvalidIndexException  {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		c.getInterfaceMethodSignature(227);
	}
	
	@Test
	public void testGetFieldsNonStatic1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Boolean");

		//oddly, with HashSet<Signature> it does not work even with 
		//Signature.equals defined, so we use HashSet<String> instead
		
		HashSet<String> fldsExp = new HashSet<String>();
		fldsExp.add("java/lang/Boolean:Z:value");

		Signature[] flds = c.getFieldsNonStatic();
		HashSet<String> fldsAct = new HashSet<String>();
		for (Signature sig: flds) fldsAct.add(sig.toString());

		assertEquals(fldsExp, fldsAct);
	}
	
	@Test
	public void testGetFieldsStatic1() throws BadClassFileException {
		ClassFile c = f.newClassFile("java/lang/Boolean");

		//oddly, with HashSet<Signature> it does not work even with 
		//Signature.equals defined, so we use HashSet<String> instead
		
		HashSet<String> fldsExp = new HashSet<String>();
		fldsExp.add("java/lang/Boolean:Ljava/lang/Boolean;:TRUE");
		fldsExp.add("java/lang/Boolean:Ljava/lang/Boolean;:FALSE");
		fldsExp.add("java/lang/Boolean:Ljava/lang/Class;:TYPE");
		fldsExp.add("java/lang/Boolean:J:serialVersionUID");

		Signature[] flds = c.getFieldsStatic();
		HashSet<String> fldsAct = new HashSet<String>();
		for (Signature sig: flds) fldsAct.add(sig.toString());

		assertEquals(fldsExp, fldsAct);
	}
	
	@Test
	public void testGetLocalVariableLength1() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		Signature sig = new Signature("java/util/LinkedList", "(Ljava/lang/Object;)V", "addFirst");
		assertEquals(2, c.getLocalVariableLength(sig));
	}
	
	@Test(expected=MethodNotFoundException.class)
	public void testGetLocalVariableLength2() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "foo");
		c.getLocalVariableLength(sig);
	}
	
	@Test(expected=MethodCodeNotFoundException.class)
	public void testGetLocalVariableLength3() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/util/Collection");
		Signature sig = new Signature("java/util/Collection", "(Ljava/lang/Object;)Z", "remove");
		c.getLocalVariableLength(sig);
	}
	
	@Test
	public void testGetCodeLength1() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/util/LinkedList");
		Signature sig = new Signature("java/util/LinkedList", "()V", "<init>");
		assertEquals(43, c.getCodeLength(sig));
	}
	
	@Test
	public void testGetCodeLength2() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "<clinit>");
		assertEquals(4, c.getCodeLength(sig));
	}

	/**
	 * Native method.
	 * @throws MethodCodeNotFoundException 
	 * @throws MethodNotFoundException 
	 */
	@Test(expected=MethodCodeNotFoundException.class)
	public void testGetCodeLength3() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		ClassFile c = f.newClassFile("java/lang/Object");
		Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
		c.getCodeLength(sig);
	}

	/**
	 * Method not declared in the class neither in some superclass.
	 */
	@Test(expected=MethodNotFoundException.class)
	public void testGetMethodCodeBySignature1() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		Signature sig = new Signature("java/lang/Object", "(I)Z", "foo");
		ClassFile c = f.newClassFile("java/lang/Object");
		c.getMethodCodeBySignature(sig);
	}

	/**
	 * Native method.
	 */
	@Test(expected=MethodCodeNotFoundException.class)
	public void testGetMethodCodeBySignature2() throws BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException {
		Signature sig = new Signature("java/lang/Object", "()V", "notifyAll");
		ClassFile c = f.newClassFile("java/lang/Object");
		c.getMethodCodeBySignature(sig);
	}

	//TODO hasField/getLocalVariableTable/getMethodCodeBySignature (to complete), array classes
}
