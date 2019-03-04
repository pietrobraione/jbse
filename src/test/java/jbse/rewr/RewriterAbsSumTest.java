package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterAbsSumTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials());
		this.calc.addRewriter(new RewriterAbsSum());
	}
	
	@Test
	public void testBasic1() throws Exception {
		//abs(A) + A > 0 -> A > 0
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Primitive absA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, A); 
		final Primitive p_post = this.calc.push(absA).add(A).gt(this.calc.valFloat(0.0f)).pop(); 
		assertEquals(this.calc.push(A).gt(this.calc.valFloat(0.0f)).pop(), p_post);
	}
	
	@Test
	public void testBasic2() throws Exception {
		//-1.0 * abs(A) + A > 0 -> false
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Primitive absA = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, A); 
		final Primitive p_post = this.calc.push(absA).mul(this.calc.valFloat(-1.0f)).add(A).gt(this.calc.valFloat(0.0f)).pop(); 
		assertEquals(this.calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic3() throws Exception {
		//A - abs(A) > 0 -> false
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive absA = this.calc.applyFunctionPrimitiveAndPop(Type.INT, this.hist, PrimitiveSymbolicApply.ABS_INT, A); 
		final Primitive p_post = this.calc.push(A).sub(absA).gt(this.calc.valInt(0)).pop(); 
		assertEquals(this.calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic4() throws Exception {
		//A - abs(A) < 0 -> A < 0
		final Term A = this.calc.valTerm(Type.LONG, "A");
		final Primitive absA = this.calc.applyFunctionPrimitiveAndPop(Type.LONG, this.hist, PrimitiveSymbolicApply.ABS_LONG, A); 
		final Primitive p_post = this.calc.push(A).sub(absA).lt(this.calc.valLong(0L)).pop(); 
		assertEquals(this.calc.push(A).lt(this.calc.valLong(0L)).pop(), p_post);
	}
	
	@Test
	public void testComposite1() throws Exception {
		//A - abs(A + B) + B < 0 -> A + B < 0
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Primitive absAplusB = this.calc.applyFunctionPrimitiveAndPop(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, this.calc.push(A).add(B).pop()); 
		final Primitive p_post = this.calc.push(A).sub(absAplusB).add(B).lt(this.calc.valFloat(0)).pop(); 
		assertEquals(this.calc.push(A).add(B).lt(this.calc.valFloat(0)).pop(), p_post);
	}
	
	@Test
	public void testComposite2() throws Exception {
		//A - abs(B - A) - B < 0 -> B - A > 0
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive absBminusA = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, this.calc.push(B).sub(A).pop()); 
		final Primitive p_post = this.calc.push(A).sub(absBminusA).sub(B).lt(this.calc.valDouble(0.0d)).pop(); 
		assertEquals(this.calc.push(B).sub(A).gt(this.calc.valDouble(0.0d)).pop(), p_post);
	}
}
