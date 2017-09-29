package jbse.mem;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.mem.Objekt.Epoch;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Value;

public class InstanceTest {
	private ClassHierarchy hier;
	private CalculatorRewriting calc = new CalculatorRewriting();

	@Before
	public void setUp() throws InvalidClassFileFactoryClassException {
		//environment
		final Classpath env = new Classpath("src/test/resources/jbse/bc/testdata/rt.jar", "src/test/resources/jbse/bc/testdata");

		//Javassist
		this.hier = new ClassHierarchy(env, ClassFileFactoryJavassist.class, new HashMap<>());

	}
	
	@Test
	public void testInstanceGetFieldValue1() throws BadClassFileException {
		final String className = "tsafe/main/SimpleCalculator";
		final Signature[] fieldsSignatures = this.hier.getAllFieldsInstance(className);
		final Instance i = new Instance(this.calc, className, null, Epoch.EPOCH_AFTER_START, fieldsSignatures);
		final Signature sigMinLat = new Signature(className, "D", "minLat");
		final Value valMinLat = i.getFieldValue(sigMinLat);
		assertEquals(valMinLat, this.calc.valDouble(0));
	}
	
	@Test
	public void testInstanceGetFieldValue2() throws BadClassFileException {
		final String className = "tsafe/main/SimpleCalculator";
		final Signature[] fieldsSignatures = this.hier.getAllFieldsInstance(className);
		final Instance i = new Instance(this.calc, className, null, Epoch.EPOCH_AFTER_START, fieldsSignatures);
		final Signature sigMinLat = new Signature(className, "D", "minLat");
		final Value valMinLat = i.getFieldValue(sigMinLat);
		final Value valMinLat2 = i.getFieldValue("minLat");
		assertEquals(valMinLat, valMinLat2);
	}
	
	
	@Test
	public void testInstanceSetFieldValue() throws BadClassFileException {
		final String className = "tsafe/main/SimpleCalculator";
		final Signature[] fieldsSignatures = this.hier.getAllFieldsInstance(className);
		final Instance i = new Instance(this.calc, className, null, Epoch.EPOCH_AFTER_START, fieldsSignatures);
		final Signature sigMinLat = new Signature(className, "D", "minLat");
		i.setFieldValue(sigMinLat, this.calc.valDouble(1.0d));
		final Value valMinLat = i.getFieldValue("minLat");
		assertEquals(valMinLat, this.calc.valDouble(1.0d));
	}
	
	@Test
	public void testInstanceClone() throws BadClassFileException {
		final String className = "tsafe/main/SimpleCalculator";
		final Signature[] fieldsSignatures = this.hier.getAllFieldsInstance(className);
		final Instance i = new Instance(this.calc, className, null, Epoch.EPOCH_AFTER_START, fieldsSignatures);
		final Instance iClone = i.clone();
		final Signature sigMinLat = new Signature(className, "D", "minLat");
		i.setFieldValue(sigMinLat, this.calc.valDouble(1.0d));
		final Value valMinLatClone = iClone.getFieldValue("minLat");
		assertEquals(valMinLatClone, this.calc.valDouble(0));
	}
}
