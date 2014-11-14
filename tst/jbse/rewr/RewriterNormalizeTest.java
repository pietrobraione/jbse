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

public class RewriterNormalizeTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterNormalize());
	}
	
	@Test
	public void test1() throws InvalidOperandException, InvalidTypeException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Term E = calc.valTerm(Type.INT, "E");
		final Term F = calc.valTerm(Type.INT, "F");
		final Primitive p_post = D.mul(F.mul(A)).mul(B.mul(E.mul(C)));
		assertEquals(F.mul(E).mul(D).mul(C).mul(B).mul(A), p_post);
	}

	@Test
	public void test2() throws InvalidOperandException, InvalidTypeException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Primitive p_post = new FunctionApplication(Type.INT, calc, "f", A.mul(B)).mul(A);
		assertEquals(A.mul(new FunctionApplication(Type.INT, calc, "f", B.mul(A))), p_post);
	}


	@Test
	public void test3() throws InvalidOperandException, InvalidTypeException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p_post = new FunctionApplication(Type.INT, calc, "f", A.mul(B), C.mul(D)).mul(A);
		assertEquals(A.mul(new FunctionApplication(Type.INT, calc, "f", B.mul(A), D.mul(C))), p_post);
	}


	@Test
	public void test4() throws InvalidOperandException, InvalidTypeException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p_post = A.mul(new FunctionApplication(Type.INT, calc, "f", A.add(calc.valInt(-1).mul(B))).div(new FunctionApplication(Type.INT, calc, "g", C.add(calc.valInt(-1).mul(D)))));
		assertEquals(new FunctionApplication(Type.INT, calc, "f", calc.valInt(-1).mul(B).add(A)).div(new FunctionApplication(Type.INT, calc, "g", calc.valInt(-1).mul(D).add(C))).mul(A), p_post);
	}
}
