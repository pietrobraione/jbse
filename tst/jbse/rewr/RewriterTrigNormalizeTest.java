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

public class RewriterTrigNormalizeTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterTrigNormalize());
	}
	
	@Test
	public void testSimple1() throws InvalidOperandException, InvalidTypeException {
		//tan(A + PI) -> tan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.add(calc.valDouble(Math.PI))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A), p_post);
	}
	
	@Test
	public void testSimple2() throws InvalidOperandException, InvalidTypeException {
		//2 / tan(A + PI) -> 2 / tan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.valDouble(2.0d).div(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.add(calc.valDouble(Math.PI)))); 
		assertEquals(calc.valDouble(2.0d).div(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A)), p_post);
	}
	
	@Test
	public void testSimple3() throws InvalidOperandException, InvalidTypeException {
		//cos(A * A + 7) -> cos(A * A + (7 - 2 * PI))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, A.mul(A).add(calc.valDouble(7.0d))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, A.mul(A).add(calc.valDouble(7 - 2 * Math.PI))), p_post);
	}
	
	@Test
	public void testSimple4() throws InvalidOperandException, InvalidTypeException {
		//sin(A + PI) -> -sin(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, A.add(calc.valDouble(Math.PI))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, A).neg(), p_post);
	}
	
	@Test
	public void testNested1() throws InvalidOperandException, InvalidTypeException {
		//sin(4 + tan(A * A + 4)) -> sin(4 + tan(A * A + (4 - PI)))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, calc.valDouble(4.0d).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.mul(A).add(calc.valDouble(4.0d))))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, calc.valDouble(4.0d).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.mul(A).add(calc.valDouble(4 - Math.PI))))), p_post);
	}

	@Test
	public void testNested2() throws InvalidOperandException, InvalidTypeException {
		//cos(9 + tan(A * A - 1)) -> cos((9 - 2 * PI) + tan(A * A + (PI - 1)))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.valDouble(9.0d).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.mul(A).sub(calc.valDouble(1.0d))))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.valDouble(9 - 2 * Math.PI).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A.mul(A).add(calc.valDouble(Math.PI - 1))))), p_post);
	}

	@Test
	public void testNested3() throws InvalidOperandException, InvalidTypeException {
		//cos(4 + sin(A * A + 5)) -> -cos((4 - PI) - sin(A * A + (5 - PI)))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.valDouble(4.0d).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, A.mul(A).add(calc.valDouble(5.0d)))));
		final Primitive expected = calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, 
									calc.valDouble(4 - Math.PI).sub(
											calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, A.mul(A).add(calc.valDouble(5 - Math.PI))))).neg(); 
		assertEquals(expected, p_post);
	}

	@Test
	public void testDoNothing() throws InvalidOperandException, InvalidTypeException {
		//cos(A) -> cos(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, A); 
		assertEquals(new FunctionApplication(Type.DOUBLE, calc, FunctionApplication.COS, A), p_post);
	}
}
