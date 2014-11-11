package jbse.rewr;

import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.Primitive;
import jbse.mem.Term;

import org.junit.Before;
import org.junit.Test;

public class MonomialTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() throws UnexpectedInternalException {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterNormalize());
	}
	
	@Test
	public void test1() throws InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p1 = calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D)));
		final Primitive p2 = calc.applyFunction(Type.INT, "f", B.sub(A).div(D.sub(C)));
		for (int i = 1 ; i <= 10000 ; ++i) {
			Monomial m1 = Monomial.of(calc, p1);
			Monomial m2 = Monomial.of(calc, p2);
			m1.equals(m2);
		}
	}
}
