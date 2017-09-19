package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterSinCosTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterSinCos());

	}
	
	@Test
	public void testBasic() throws Exception {
		//sin(A) * sin(A) + cos(A) * cos(A) -> 1
		final Term A = calc.valTerm(Type.INT, "A");
		final Primitive sinA = calc.applyFunction(Type.FLOAT, FunctionApplication.SIN, A); 
		final Primitive cosA = calc.applyFunction(Type.FLOAT, FunctionApplication.COS, A); 
		final Primitive p_post = calc.add(sinA.mul(sinA), cosA.mul(cosA)); 
		assertEquals(calc.valFloat(1.0f), p_post);
	}
	
	@Test
	public void testAlternate() throws Exception {
		//A * sin(A) * B * sin(A) + cos(A) * B * A * cos(A) -> A * B
		final Term A = calc.valTerm(Type.FLOAT, "A");
		final Term B = calc.valTerm(Type.FLOAT, "B");
		final Primitive sinA = calc.applyFunction(Type.FLOAT, FunctionApplication.SIN, A); 
		final Primitive cosA = calc.applyFunction(Type.FLOAT, FunctionApplication.COS, A); 
		final Primitive first = A.mul(sinA).mul(B).mul(sinA);
		final Primitive second = cosA.mul(B.mul(A)).mul(cosA);
		final Primitive p_post = calc.add(first, second); 
		assertEquals(A.mul(B), p_post);
	}
	
	@Test
	public void testMultiplier1() throws Exception {
		//3.0 * sin(A) * 2.0 * sin(A) + cos(A) * 6.0 * cos(A) -> 6.0
		final Term A = calc.valTerm(Type.INT, "A");
		final Primitive sinA = calc.applyFunction(Type.FLOAT, FunctionApplication.SIN, A); 
		final Primitive cosA = calc.applyFunction(Type.FLOAT, FunctionApplication.COS, A); 
		final Primitive first = calc.valFloat(3.0f).mul(sinA).mul(calc.valFloat(2.0f)).mul(sinA);
		final Primitive second = cosA.mul(cosA).mul(calc.valFloat(6.0f));
		final Primitive p_post = calc.add(first, second); 
		assertEquals(calc.valFloat(6.0f), p_post);
	}

	@Test
	public void testMultiplier2() throws Exception {
		//5.0 * sin(A) sin(A) + cos(A) * 5.0 * cos(A) -> 5.0
		final Term A = calc.valTerm(Type.INT, "A");
		final Primitive sinA = calc.applyFunction(Type.FLOAT, FunctionApplication.SIN, A); 
		final Primitive cosA = calc.applyFunction(Type.FLOAT, FunctionApplication.COS, A); 
		final Primitive first = calc.valFloat(5.0f).mul(sinA).mul(sinA);
		final Primitive second = cosA.mul(calc.valFloat(5.0f)).mul(cosA);
		final Primitive p_post = first.add(second); 
		assertEquals(calc.valFloat(5.0f), p_post);
	}

	@Test
	public void testComplex() throws Exception {
		//-cos(A) * 2 * cos(A) - B + (C - 2 * sin(A) sin(A)) +  -> -2 - B + C
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Primitive sinA = calc.applyFunction(Type.INT, FunctionApplication.SIN, A); 
		final Primitive cosA = calc.applyFunction(Type.INT, FunctionApplication.COS, A); 
		final Primitive first = cosA.neg().mul(calc.valInt(2)).mul(cosA).sub(B);
		final Primitive second = C.sub(calc.valInt(2).mul(sinA).mul(sinA));
		final Primitive p_post = first.add(second); 
		assertEquals(calc.valInt(2).neg().sub(B).add(C), p_post);
	}
}
