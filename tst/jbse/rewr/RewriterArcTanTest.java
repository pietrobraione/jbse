package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterArcTanTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterArcTan());
	}
	
	@Test
	public void testSimple1() throws InvalidOperandException, InvalidTypeException {
		//tan(atan(A)) -> A
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A)); 
		assertEquals(A, p_post);
	}
	
	@Test
	public void testSimple2() throws InvalidOperandException, InvalidTypeException {
		//tan(tan(atan(A))) -> tan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A), p_post);
	}
	
	@Test
	public void testSimple3() throws InvalidOperandException, InvalidTypeException {
		//atan(tan(atan(A))) -> atan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A), p_post);
	}
}
