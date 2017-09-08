package jbse.rewr;

import static org.junit.Assert.*;

import jbse.common.Type;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterDivisionEquationTest {
	CalculatorRewriting calcPoly, calcNoPoly;
	
	@Before
	public void before() {
		calcPoly = new CalculatorRewriting();
		calcPoly.addRewriter(new RewriterOperationOnSimplex());
		calcPoly.addRewriter(new RewriterPolynomials());
		calcPoly.addRewriter(new RewriterDivisionEqualsZero());
		
		calcNoPoly = new CalculatorRewriting();
		calcNoPoly.addRewriter(new RewriterOperationOnSimplex());
		calcNoPoly.addRewriter(new RewriterDivisionEqualsZero());
	}
	
	@Test
	public void testBasic() throws Exception {
		//A / B == 0 -> A == 0 && B != 0
		final Simplex ZERO = calcPoly.valInt(0);
		final Term A = calcPoly.valTerm(Type.INT, "A");
		final Term B = calcPoly.valTerm(Type.INT, "B");
		final Primitive p_post = A.div(B).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedNumeratorPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B * C != 0
		final Simplex ZERO = calcPoly.valInt(0);
		final Term A = calcPoly.valTerm(Type.INT, "A");
		final Term B = calcPoly.valTerm(Type.INT, "B");
		final Term C = calcPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B).div(C).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.mul(C).ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedNumeratorNoPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B != 0 && C != 0
		final Simplex ZERO = calcNoPoly.valInt(0);
		final Term A = calcNoPoly.valTerm(Type.INT, "A");
		final Term B = calcNoPoly.valTerm(Type.INT, "B");
		final Term C = calcNoPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B).div(C).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.ne(ZERO)).and(C.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedDenominatorPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A * C / B == 0] -> A * C == 0 && B != 0
		//(note this is NOT correct!)
		final Simplex ZERO = calcPoly.valInt(0);
		final Term A = calcPoly.valTerm(Type.INT, "A");
		final Term B = calcPoly.valTerm(Type.INT, "B");
		final Term C = calcPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B.div(C)).eq(ZERO);
		assertEquals(A.mul(C).eq(ZERO).and(B.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedDenominatorNoPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A == 0 && B / C != 0 -> [B / C != 0 -> ! (B / C == 0)]] -> A == 0 && ! (B == 0 && C != 0)
		//(note this is equivalent to the poly rewriting variant, so it is also not correct)
		final Simplex ZERO = calcNoPoly.valInt(0);
		final Term A = calcNoPoly.valTerm(Type.INT, "A");
		final Term B = calcNoPoly.valTerm(Type.INT, "B");
		final Term C = calcNoPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B.div(C)).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.eq(ZERO).and(C.ne(ZERO)).not()), p_post);
	}
	
	@Test
	public void testUnderFunctionApplication() throws Exception {
		//f((A / B == 0), (C / D == 0))  -> f((A == 0 && B != 0), (C == 0 && D != 0))
		final Simplex ZERO = calcPoly.valInt(0);
		final Term A = calcPoly.valTerm(Type.INT, "A");
		final Term B = calcPoly.valTerm(Type.INT, "B");
		final Term C = calcPoly.valTerm(Type.INT, "C");
		final Term D = calcPoly.valTerm(Type.INT, "D");
		final Primitive p_post = calcPoly.applyFunction(Type.BOOLEAN, "f", A.div(B).eq(ZERO), C.div(D).eq(ZERO));
		assertEquals(calcPoly.applyFunction(Type.BOOLEAN, "f", A.eq(ZERO).and(B.ne(ZERO)), C.eq(ZERO).and(D.ne(ZERO))), p_post);
	}
}
