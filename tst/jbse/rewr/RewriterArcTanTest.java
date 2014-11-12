package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.Type;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.FunctionApplication;
import jbse.mem.Primitive;
import jbse.mem.Term;

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
