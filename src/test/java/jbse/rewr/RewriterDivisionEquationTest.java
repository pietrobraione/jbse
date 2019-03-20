package jbse.rewr;

import static org.junit.Assert.*;

import jbse.common.Type;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public final class RewriterDivisionEquationTest {
	private HistoryPoint hist;
	private CalculatorRewriting calcPoly, calcNoPoly;
	
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
		final Simplex ZERO = this.calcPoly.valDouble(0);
		final Term A = this.calcPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcPoly.valTerm(Type.DOUBLE, "B");
		final Primitive p_post = this.calcPoly.push(A).div(B).eq(ZERO).pop();
		assertEquals(this.calcPoly.push(A).eq(ZERO).and(this.calcPoly.push(B).ne(ZERO).pop()).pop(), p_post);
	}
	
	@Test
	public void testNestedNumeratorPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B * C != 0
		final Simplex ZERO = this.calcPoly.valDouble(0);
		final Term A = this.calcPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcPoly.valTerm(Type.DOUBLE, "B");
		final Term C = this.calcPoly.valTerm(Type.DOUBLE, "C");
		final Primitive p_post = this.calcPoly.push(A).div(B).div(C).eq(ZERO).pop();
		assertEquals(this.calcPoly.push(A).eq(ZERO).and(this.calcPoly.push(B).mul(C).ne(ZERO).pop()).pop(), p_post);
	}
	
	@Test
	public void testNestedNumeratorNoPolyRewriting() throws Exception {
		//(A / B) / C == 0 -> [A / (B * C) == 0] -> A == 0 && B != 0 && C != 0
		final Simplex ZERO = this.calcNoPoly.valDouble(0);
		final Term A = this.calcNoPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcNoPoly.valTerm(Type.DOUBLE, "B");
		final Term C = this.calcNoPoly.valTerm(Type.DOUBLE, "C");
		final Primitive p_post = this.calcNoPoly.push(A).div(B).div(C).eq(ZERO).pop();
		assertEquals(this.calcNoPoly.push(A).eq(ZERO).and(this.calcNoPoly.push(B).ne(ZERO).pop()).and(this.calcNoPoly.push(C).ne(ZERO).pop()).pop(), p_post);
	}
	
	@Test
	public void testNestedDenominatorPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A * C / B == 0] -> A * C == 0 && B != 0
		//(note this is NOT correct!)
		final Simplex ZERO = this.calcPoly.valDouble(0);
		final Term A = this.calcPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcPoly.valTerm(Type.DOUBLE, "B");
		final Term C = this.calcPoly.valTerm(Type.DOUBLE, "C");
		final Primitive p_post = this.calcPoly.push(A).div(this.calcPoly.push(B).div(C).pop()).eq(ZERO).pop();
		assertEquals(this.calcPoly.push(A).mul(C).eq(ZERO).and(this.calcPoly.push(B).ne(ZERO).pop()).pop(), p_post);
	}
	
	@Test
	public void testNestedDenominatorNoPolyRewriting() throws Exception {
		//A / (B / C) == 0 -> [A == 0 && B / C != 0 -> [B / C != 0 -> ! (B / C == 0)]] -> A == 0 && ! (B == 0 && C != 0)
		//(note this is equivalent to the poly rewriting variant, so it is also not correct)
		final Simplex ZERO = this.calcNoPoly.valDouble(0);
		final Term A = this.calcNoPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcNoPoly.valTerm(Type.DOUBLE, "B");
		final Term C = this.calcNoPoly.valTerm(Type.DOUBLE, "C");
		final Primitive p_post = this.calcNoPoly.push(A).div(this.calcNoPoly.push(B).div(C).pop()).eq(ZERO).pop();
		assertEquals(this.calcNoPoly.push(A).eq(ZERO).and(this.calcNoPoly.push(B).eq(ZERO).and(this.calcNoPoly.push(C).ne(ZERO).pop()).not().pop()).pop(), p_post);
	}
	
	@Test
	public void testUnderFunctionApplication() throws Exception {
		//f((A / B == 0), (C / D == 0))  -> f((A == 0 && B != 0), (C == 0 && D != 0))
		final Simplex ZERO = this.calcPoly.valInt(0);
		final Term A = this.calcPoly.valTerm(Type.DOUBLE, "A");
		final Term B = this.calcPoly.valTerm(Type.DOUBLE, "B");
		final Term C = this.calcPoly.valTerm(Type.DOUBLE, "C");
		final Term D = this.calcPoly.valTerm(Type.DOUBLE, "D");
		final Primitive p_post = this.calcPoly.applyFunctionPrimitiveAndPop(Type.BOOLEAN, this.hist, "f", this.calcPoly.push(A).div(B).eq(ZERO).pop(), this.calcPoly.push(C).div(D).eq(ZERO).pop());
		assertEquals(this.calcPoly.applyFunctionPrimitiveAndPop(Type.BOOLEAN, this.hist, "f", this.calcPoly.push(A).eq(ZERO).and(this.calcPoly.push(B).ne(ZERO).pop()).pop(), this.calcPoly.push(C).eq(ZERO).and(this.calcPoly.push(D).ne(ZERO).pop()).pop()), p_post);
	}
}
