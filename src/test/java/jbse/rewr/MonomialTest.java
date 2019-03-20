package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class MonomialTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials());
		this.calc.addRewriter(new RewriterNormalize());
	}
	
	@Test
	public void test1() throws InvalidTypeException, InvalidOperandException, InvalidInputException {
		//f((A / B) / (C / D)) == f ((A * D) / (B * C))
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Term C = this.calc.valTerm(Type.FLOAT, "C");
		final Term D = this.calc.valTerm(Type.FLOAT, "D");
		final Primitive p1 = this.calc.applyFunctionPrimitive(Type.FLOAT, this.hist, "f", this.calc.push(A).div(B).div(this.calc.push(C).div(D).pop()).pop()).pop();
		final Primitive p2 = this.calc.applyFunctionPrimitive(Type.FLOAT, this.hist, "f", this.calc.push(A).mul(D).div(this.calc.push(B).mul(C).pop()).pop()).pop();
		final Monomial m1 = Monomial.of(this.calc, p1);
		final Monomial m2 = Monomial.of(this.calc, p2);
		assertEquals(m2, m1);
	}
}
