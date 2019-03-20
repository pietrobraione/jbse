package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterPolynomialsTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials());
	}
	
	@Test
	public void testDivision1() throws InvalidOperandException, InvalidTypeException {
		//(A * B) / (B * C) -> A / C
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Primitive p_post = this.calc.push(A).mul(B).div(this.calc.push(B).mul(C).pop()).pop();
		assertEquals(this.calc.push(A).div(C).pop(), p_post);
	}
	
	@Test
	public void testDivision2() throws InvalidOperandException, InvalidTypeException {
		//(2 * A * A * B + 4 * A * B) / (A * B) -> 2 * A + 4
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.pushInt(2).mul(A).mul(A).mul(B).add(this.calc.pushInt(4).mul(A).mul(B).pop()).div(this.calc.push(A).mul(B).pop()).pop();
		assertEquals(this.calc.pushInt(2).mul(A).add(this.calc.pushInt(4).pop()).pop(), p_post);
	}
	
	@Test
	public void testDivision3() throws InvalidOperandException, InvalidOperatorException, InvalidTypeException, InvalidInputException {
		//(A - B) / (C - D) preserves division
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Primitive p1 = this.calc.push(A).sub(B).pop();
		final Primitive p2 = this.calc.push(C).sub(D).pop();
		final Primitive p_actual = this.calc.push(p1).div(p2).pop();
		assertEquals(Expression.makeExpressionBinary(p1, Operator.DIV, p2), p_actual);
	}
	
	@Test
	public void testDivision4() throws InvalidOperandException, InvalidTypeException {
		//(-2 * A * B) / (3 * B * C) -> (-2 * A) / (3 * C)
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Primitive p_post = this.calc.pushInt(-2).mul(A).mul(B).div(this.calc.pushInt(3).mul(B).mul(C).pop()).pop();
		assertEquals(this.calc.pushInt(-2).mul(A).div(this.calc.pushInt(3).mul(C).pop()).pop(), p_post);
	}
	
	@Test
	public void testDivision5() throws InvalidOperandException, InvalidTypeException {
		//A / (-2) -> - (A / 2)
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Simplex two = this.calc.valInt(2);
		final Simplex minusTwo = this.calc.valInt(-2);
		final Primitive p_post = this.calc.push(A).div(minusTwo).pop();
		assertEquals(this.calc.push(A).div(two).neg().pop(), p_post);
	}
	
	@Test
	public void testSum1() throws InvalidOperandException, InvalidTypeException {
		//(A + B) - A  -> B
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.push(A).add(B).sub(A).pop();
		assertEquals(B, p_post);
	}
	
	@Test
	public void testSum2() throws InvalidOperandException, InvalidTypeException {
		//A / (A + B) + B / (A + B) -> 1
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive p_post = this.calc.push(A).div(this.calc.push(A).add(B).pop()).add(this.calc.push(B).div(this.calc.push(A).add(B).pop()).pop()).pop();
		assertEquals(this.calc.valDouble(1.0d), p_post);
	}
	
	@Test
	public void testSum3() throws InvalidOperandException, InvalidTypeException {
		//A / (A + B) - B / (A + B) -> (A * A - B * B) / (A * A + 2 * A * B + B * B)
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Primitive p_post = this.calc.push(A).div(this.calc.push(A).add(B).pop()).sub(this.calc.push(B).div(this.calc.push(A).add(B).pop()).pop()).pop();
		assertEquals(this.calc.push(A).mul(A).sub(this.calc.push(B).mul(B).pop()).div(this.calc.push(A).mul(A).add(this.calc.pushFloat(2.0f).mul(B).mul(A).pop()).add(this.calc.push(B).mul(B).pop()).pop()).pop(), p_post);
	}
	
	@Test
	public void testMultiplication1() throws InvalidOperandException, InvalidTypeException {
		//-1 * (A / B) -> (-1 * A) / B
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive p_post = this.calc.pushDouble(-1.0d).mul(this.calc.push(A).div(B).pop()).pop();
		assertEquals(this.calc.pushDouble(-1.0d).mul(A).div(B).pop(), p_post);
	}
	
	@Test
	public void testMultiplication2() throws InvalidOperandException, InvalidTypeException {
		//(A + B) * (A + B) -> A * A + 2 * A * B + B * B
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.push(A).add(B).mul(this.calc.push(A).add(B).pop()).pop();
		assertEquals(this.calc.push(A).mul(A).add(this.calc.push(B).mul(B).pop()).add(this.calc.pushInt(2).mul(A).mul(B).pop()).pop(), p_post);
	}
	
	@Test
	public void testMultiplication3() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//(A + C) * D -> (A * D) + (C * D) with polynomial normalization
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Term D = this.calc.valTerm(Type.INT, "D");
		final Primitive p1 = this.calc.applyFunctionPrimitiveAndPop(Type.INT, this.hist, "foo", A, B);
		final Primitive p_actual = this.calc.push(p1).add(C).mul(D).pop();
		assertEquals(this.calc.push(p1).mul(D).add(this.calc.push(C).mul(D).pop()).pop(), p_actual); 
	}   
	
	@Test
	public void testMultiplication4() throws InvalidOperandException, InvalidTypeException {
		//-1 * (-1 * A) -> A
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive p_post = this.calc.pushInt(-1).mul(this.calc.pushInt(-1).mul(A).pop()).pop();
		assertEquals(A, p_post);
	}
	
	@Test
	public void testNegation1() throws InvalidOperandException, InvalidTypeException {
		//- (- A) -> A
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive p_post = this.calc.push(A).neg().neg().pop();
		assertEquals(A, p_post);
	}
	
	@Test
	public void testNegation2() throws InvalidOperandException, InvalidTypeException {
		// A * (- B + C) -> -1 * A * B + A * C
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Primitive p_post = this.calc.push(A).mul(this.calc.push(B).neg().add(C).pop()).pop();
		assertEquals(this.calc.pushInt(-1).mul(A).mul(B).add(this.calc.push(A).mul(C).pop()).pop(), p_post);
	}
	
	@Test
	public void testNegation3() throws InvalidOperandException, InvalidTypeException {
		// - (- (A + B)) -> A + B
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.push(A).add(B).neg().neg().pop();
		assertEquals(this.calc.push(A).add(B).pop(), p_post);
	}
	
	@Test
	public void testConstant1() throws InvalidOperandException, InvalidTypeException {
		// -A / -B -> A / B
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Primitive p_post = this.calc.push(A).neg().div(this.calc.push(B).neg().pop()).pop();
		assertEquals(this.calc.push(A).div(B).pop(), p_post);
	}
	
	@Test
	public void testSimplification1() throws InvalidOperandException, InvalidTypeException {
		// 131072 * A / 2048 -> 64 * A
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive p_post = this.calc.pushInt(131072).mul(A).div(this.calc.valInt(2048)).pop();
		assertEquals(this.calc.pushInt(64).mul(A).pop(), p_post);
	}
	
	@Test
	public void testSimplification2() throws InvalidOperandException, InvalidTypeException {
		// 2048 * A / 131072  -> A / 64
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive p_post = this.calc.pushInt(2048).mul(A).div(this.calc.valInt(131072)).pop();
		assertEquals(this.calc.push(A).div(this.calc.valInt(64)).pop(), p_post);
	}
	
	@Test
	public void testSimplification3() throws InvalidOperandException, InvalidTypeException {
		// (2.5f * A + 3.5f B) / 0.2f  -> (2.5f / 0.2f) * A + (3.5f / 0.2f) * B
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Primitive p_post = this.calc.pushFloat(2.5f).mul(A).add(this.calc.pushFloat(3.5f).mul(B).pop()).div(this.calc.valFloat(0.2f)).pop();
		assertEquals(this.calc.pushFloat(2.5f / 0.2f).mul(A).add(this.calc.pushFloat(3.5f / 0.2f).mul(B).pop()).pop(), p_post);
	}
}
