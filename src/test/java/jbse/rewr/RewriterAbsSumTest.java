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
		final Primitive absA = this.calc.applyFunctionPrimitive(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, A); 
		final Primitive p_post = absA.add(A).gt(this.calc.valFloat(0.0f)); 
		assertEquals(A.gt(this.calc.valFloat(0.0f)), p_post);
	}
	
	@Test
	public void testBasic2() throws Exception {
		//-abs(A) + A > 0 -> false
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Primitive absA = this.calc.applyFunctionPrimitive(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, A); 
		final Primitive p_post = absA.mul(this.calc.valFloat(-1.0f)).add(A).gt(this.calc.valFloat(0.0f)); 
		assertEquals(this.calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic3() throws Exception {
		//A - abs(A) > 0 -> false
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive absA = this.calc.applyFunctionPrimitive(Type.INT, this.hist, PrimitiveSymbolicApply.ABS_INT, A); 
		final Primitive p_post = A.sub(absA).gt(this.calc.valInt(0)); 
		assertEquals(this.calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic4() throws Exception {
		//A - abs(A) < 0 -> A < 0
		final Term A = this.calc.valTerm(Type.LONG, "A");
		final Primitive absA = this.calc.applyFunctionPrimitive(Type.LONG, this.hist, PrimitiveSymbolicApply.ABS_LONG, A); 
		final Primitive p_post = A.sub(absA).lt(this.calc.valLong(0L)); 
		assertEquals(A.lt(this.calc.valLong(0L)), p_post);
	}
	
	@Test
	public void testComposite1() throws Exception {
		//A - abs(A + B) + B < 0 -> A + B < 0
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		final Term B = this.calc.valTerm(Type.FLOAT, "B");
		final Primitive absAplusB = this.calc.applyFunctionPrimitive(Type.FLOAT, this.hist, PrimitiveSymbolicApply.ABS_FLOAT, A.add(B)); 
		final Primitive p_post = A.sub(absAplusB).add(B).lt(this.calc.valFloat(0)); 
		assertEquals(A.add(B).lt(this.calc.valFloat(0)), p_post);
	}
	
	@Test
	public void testComposite2() throws Exception {
		//A - abs(B - A) - B < 0 -> B - A > 0
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive absBminusA = this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, B.sub(A)); 
		final Primitive p_post = A.sub(absBminusA).sub(B).lt(this.calc.valDouble(0.0d)); 
		assertEquals(B.sub(A).gt(this.calc.valDouble(0.0d)), p_post);
	}
}
