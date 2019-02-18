package jbse.rewr;

import static org.junit.Assert.assertTrue;

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
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Primitive p1 = this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A.div(B).div(C.div(D)));
		final Primitive p2 = this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A.mul(D).div(B.mul(C)));
		final Monomial m1 = Monomial.of(this.calc, p1);
		final Monomial m2 = Monomial.of(this.calc, p2);
		assertTrue(m1.equals(m2));
	}
}
