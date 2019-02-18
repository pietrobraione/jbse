package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterNormalizeTest {
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
	public void test2() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Primitive p_post = new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", A.mul(B)).mul(A);
		assertEquals(A.mul(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", B.mul(A))), p_post);
	}


	@Test
	public void test3() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p_post = new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", A.mul(B), C.mul(D)).mul(A);
		assertEquals(A.mul(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", B.mul(A), D.mul(C))), p_post);
	}


	@Test
	public void test4() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = calc.valTerm(Type.INT, "A");
		final Term B = calc.valTerm(Type.INT, "B");
		final Term C = calc.valTerm(Type.INT, "C");
		final Term D = calc.valTerm(Type.INT, "D");
		final Primitive p_post = A.mul(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", A.add(this.calc.valInt(-1).mul(B))).div(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "g", C.add(this.calc.valInt(-1).mul(D)))));
		assertEquals(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "f", this.calc.valInt(-1).mul(B).add(A)).div(new PrimitiveSymbolicApply(Type.INT, this.hist, this.calc, "g", this.calc.valInt(-1).mul(D).add(C))).mul(A), p_post);
	}
}
