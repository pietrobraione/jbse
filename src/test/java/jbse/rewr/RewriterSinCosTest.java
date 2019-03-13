package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterSinCosTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials());
		this.calc.addRewriter(new RewriterSinCos());
	}
	
	@Test
	public void testBasic() throws Exception {
		//sin(A) * sin(A) + cos(A) * cos(A) -> 1
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive sinA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.SIN, A); 
		final Primitive cosA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.COS, A); 
		final Primitive p_post = this.calc.push(sinA).mul(sinA).add(this.calc.push(cosA).mul(cosA).pop()).pop(); 
		assertEquals(this.calc.valFloat(1.0f), p_post);
	}
	
	@Test
	public void testAlternate() throws Exception {
		//A * sin(A) * B * sin(A) + cos(A) * (B * A) * cos(A) -> A * B
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Primitive sinA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.SIN, A); 
		final Primitive cosA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.COS, A); 
		final Primitive first = this.calc.push(A).mul(sinA).mul(B).mul(sinA).pop();
		final Primitive second = this.calc.push(cosA).mul(this.calc.push(B).mul(A).pop()).mul(cosA).pop();
		final Primitive p_post = this.calc.push(first).add(second).pop(); 
		assertEquals(this.calc.push(A).mul(B).pop(), p_post);
	}
	
	@Test
	public void testMultiplier1() throws Exception {
		//(3.0 * sin(A)) * (2.0 * sin(A)) + cos(A) * cos(A) * 6.0 -> 6.0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive sinA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.SIN, A); 
		final Primitive cosA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.COS, A); 
		final Primitive first = this.calc.pushFloat(3.0f).mul(sinA).mul(this.calc.valFloat(2.0f)).mul(sinA).pop();
		final Primitive second = this.calc.push(cosA).mul(cosA).mul(this.calc.valFloat(6.0f)).pop();
		final Primitive p_post = this.calc.push(first).add(second).pop(); 
		assertEquals(this.calc.valFloat(6.0f), p_post);
	}

	@Test
	public void testMultiplier2() throws Exception {
		//5.0 * sin(A) sin(A) + cos(A) * 5.0 * cos(A) -> 5.0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive sinA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.SIN, A); 
		final Primitive cosA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.COS, A); 
		final Primitive first = this.calc.pushFloat(5.0f).mul(sinA).mul(sinA).pop();
		final Primitive second = this.calc.push(cosA).mul(this.calc.valFloat(5.0f)).mul(cosA).pop();
		final Primitive p_post = this.calc.push(first).add(second).pop(); 
		assertEquals(this.calc.valFloat(5.0f), p_post);
	}

	@Test
	public void testComplex() throws Exception {
		//-cos(A) * 2 * cos(A) - B + (C - 2 * sin(A) sin(A)) +  -> -2 - B + C
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		final Primitive sinA = this.calc.applyFunctionPrimitiveAndPop(Type.INT, this.hist, PrimitiveSymbolicApply.SIN, A); 
		final Primitive cosA = this.calc.applyFunctionPrimitiveAndPop(Type.INT, this.hist, PrimitiveSymbolicApply.COS, A); 
		final Primitive first = this.calc.push(cosA).neg().mul(this.calc.valInt(2)).mul(cosA).sub(B).pop();
		final Primitive second = this.calc.push(C).sub(this.calc.pushInt(2).mul(sinA).mul(sinA).pop()).pop();
		final Primitive p_post = this.calc.push(first).add(second).pop(); 
		assertEquals(this.calc.pushInt(2).neg().sub(B).add(C).pop(), p_post);
	}
}
