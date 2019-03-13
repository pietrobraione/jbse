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

public final class RewriterNormalizeTest {
	private HistoryPoint hist;
	private CalculatorRewriting calc;
	
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
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Term E = this.calc.valTerm(Type.INT, "E");
		final Term F = this.calc.valTerm(Type.INT, "F");
		final Primitive p_post = this.calc.push(D).mul(this.calc.push(F).mul(A).pop()).mul(this.calc.push(B).mul(this.calc.push(E).mul(C).pop()).pop()).pop();
		assertEquals(this.calc.push(F).mul(E).mul(D).mul(C).mul(B).mul(A).pop(), p_post);
	}

	@Test
	public void test2() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.push(new PrimitiveSymbolicApply(Type.INT, this.hist, "f", this.calc.push(A).mul(B).pop())).mul(A).pop();
		assertEquals(this.calc.push(A).mul(new PrimitiveSymbolicApply(Type.INT, this.hist, "f", this.calc.push(B).mul(A).pop())).pop(), p_post);
	}


	@Test
	public void test3() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Primitive p_post = this.calc.push(new PrimitiveSymbolicApply(Type.INT, this.hist, "f", this.calc.push(A).mul(B).pop(), this.calc.push(C).mul(D).pop())).mul(A).pop();
		assertEquals(this.calc.push(A).mul(new PrimitiveSymbolicApply(Type.INT, this.hist, "f", this.calc.push(B).mul(A).pop(), this.calc.push(D).mul(C).pop())).pop(), p_post);
	}


	@Test
	public void test4() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Primitive p_post = 
				this.calc.push(A).mul(
						this.calc.push(
								new PrimitiveSymbolicApply(Type.INT, this.hist, "f", 
										this.calc.push(A).add(
												this.calc.pushInt(-1).mul(B).pop()
										).pop()
								)
						).div(
							new PrimitiveSymbolicApply(Type.INT, this.hist, "g", 
									this.calc.push(C).add(
											this.calc.pushInt(-1).mul(D).pop()
									).pop()
							)
						).pop()
				).pop();
		assertEquals(this.calc.push(new PrimitiveSymbolicApply(Type.INT, this.hist, "f", this.calc.pushInt(-1).mul(B).add(A).pop())).div(new PrimitiveSymbolicApply(Type.INT, this.hist, "g", this.calc.pushInt(-1).mul(D).add(C).pop())).mul(A).pop(), p_post);
	}
}
