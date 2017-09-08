package jbse.rewr;

import static org.junit.Assert.assertTrue;

import jbse.common.Type;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class MonomialTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterNormalize());
	}
	
	@Test
	public void test1() throws InvalidTypeException, InvalidOperandException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p1 = calc.applyFunction(Type.INT, "f", A.div(B).div(C.div(D)));
		final Primitive p2 = calc.applyFunction(Type.INT, "f", A.mul(D).div(B.mul(C)));
		Monomial m1 = Monomial.of(calc, p1);
		Monomial m2 = Monomial.of(calc, p2);
		assertTrue(m1.equals(m2));
	}
}
