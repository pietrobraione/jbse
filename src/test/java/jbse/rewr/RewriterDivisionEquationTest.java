package jbse.rewr;

import static org.junit.Assert.*;

import jbse.common.Type;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterDivisionEquationTest {
	HistoryPoint hist;
	CalculatorRewriting calcPoly, calcNoPoly;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calcPoly = new CalculatorRewriting();
		this.calcPoly.addRewriter(new RewriterOperationOnSimplex());
		this.calcPoly.addRewriter(new RewriterPolynomials());
		this.calcPoly.addRewriter(new RewriterDivisionEqualsZero());
		this.calcNoPoly = new CalculatorRewriting();
		this.calcNoPoly.addRewriter(new RewriterOperationOnSimplex());
		this.calcNoPoly.addRewriter(new RewriterDivisionEqualsZero());
	}
	
	@Test
	public void testBasic() throws Exception {
		//A / B == 0 -> A == 0 && B != 0
		final Simplex ZERO = this.calcPoly.valInt(0);
		final Term A = this.calcPoly.valTerm(Type.INT, "A");
		final Term B = this.calcPoly.valTerm(Type.INT, "B");
		final Primitive p_post = A.div(B).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedNumeratorPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B * C != 0
		final Simplex ZERO = this.calcPoly.valInt(0);
		final Term A = this.calcPoly.valTerm(Type.INT, "A");
		final Term B = this.calcPoly.valTerm(Type.INT, "B");
		final Term C = this.calcPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B).div(C).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.mul(C).ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedNumeratorNoPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B != 0 && C != 0
		final Simplex ZERO = this.calcNoPoly.valInt(0);
		final Term A = this.calcNoPoly.valTerm(Type.INT, "A");
		final Term B = this.calcNoPoly.valTerm(Type.INT, "B");
		final Term C = this.calcNoPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B).div(C).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.ne(ZERO)).and(C.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedDenominatorPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A * C / B == 0] -> A * C == 0 && B != 0
		//(note this is NOT correct!)
		final Simplex ZERO = this.calcPoly.valInt(0);
		final Term A = this.calcPoly.valTerm(Type.INT, "A");
		final Term B = this.calcPoly.valTerm(Type.INT, "B");
		final Term C = this.calcPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B.div(C)).eq(ZERO);
		assertEquals(A.mul(C).eq(ZERO).and(B.ne(ZERO)), p_post);
	}
	
	@Test
	public void testNestedDenominatorNoPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A == 0 && B / C != 0 -> [B / C != 0 -> ! (B / C == 0)]] -> A == 0 && ! (B == 0 && C != 0)
		//(note this is equivalent to the poly rewriting variant, so it is also not correct)
		final Simplex ZERO = this.calcNoPoly.valInt(0);
		final Term A = this.calcNoPoly.valTerm(Type.INT, "A");
		final Term B = this.calcNoPoly.valTerm(Type.INT, "B");
		final Term C = this.calcNoPoly.valTerm(Type.INT, "C");
		final Primitive p_post = A.div(B.div(C)).eq(ZERO);
		assertEquals(A.eq(ZERO).and(B.eq(ZERO).and(C.ne(ZERO)).not()), p_post);
	}
	
	@Test
	public void testUnderFunctionApplication() throws Exception {
		//f((A / B == 0), (C / D == 0))  -> f((A == 0 && B != 0), (C == 0 && D != 0))
		final Simplex ZERO = this.calcPoly.valInt(0);
		final Term A = this.calcPoly.valTerm(Type.INT, "A");
		final Term B = this.calcPoly.valTerm(Type.INT, "B");
		final Term C = this.calcPoly.valTerm(Type.INT, "C");
		final Term D = this.calcPoly.valTerm(Type.INT, "D");
		final Primitive p_post = this.calcPoly.applyFunctionPrimitive(Type.BOOLEAN, this.hist, "f", A.div(B).eq(ZERO), C.div(D).eq(ZERO));
		assertEquals(this.calcPoly.applyFunctionPrimitive(Type.BOOLEAN, this.hist, "f", A.eq(ZERO).and(B.ne(ZERO)), C.eq(ZERO).and(D.ne(ZERO))), p_post);
	}
}
