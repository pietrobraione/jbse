package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterAbsSumTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials());
		calc.addRewriter(new RewriterAbsSum());

	}
	
	@Test
	public void testBasic1() throws Exception {
		//abs(A) + A > 0 -> A > 0
		final Term A = calc.valTerm(Type.FLOAT, "A");
		final Primitive absA = calc.applyFunction(Type.FLOAT, FunctionApplication.ABS, A); 
		final Primitive p_post = absA.add(A).gt(calc.valFloat(0.0f)); 
		assertEquals(A.gt(calc.valFloat(0.0f)), p_post);
	}
	
	@Test
	public void testBasic2() throws Exception {
		//-abs(A) + A > 0 -> false
		final Term A = calc.valTerm(Type.FLOAT, "A");
		final Primitive absA = calc.applyFunction(Type.FLOAT, FunctionApplication.ABS, A); 
		final Primitive p_post = absA.mul(calc.valFloat(-1.0f)).add(A).gt(calc.valFloat(0.0f)); 
		assertEquals(calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic3() throws Exception {
		//A - abs(A) > 0 -> false
		final Term A = calc.valTerm(Type.INT, "A");
		final Primitive absA = calc.applyFunction(Type.INT, FunctionApplication.ABS, A); 
		final Primitive p_post = A.sub(absA).gt(calc.valInt(0)); 
		assertEquals(calc.valBoolean(false), p_post);
	}
	
	@Test
	public void testBasic4() throws Exception {
		//A - abs(A) < 0 -> A < 0
		final Term A = calc.valTerm(Type.LONG, "A");
		final Primitive absA = calc.applyFunction(Type.LONG, FunctionApplication.ABS, A); 
		final Primitive p_post = A.sub(absA).lt(calc.valLong(0L)); 
		assertEquals(A.lt(calc.valLong(0L)), p_post);
	}
	
	@Test
	public void testComposite1() throws Exception {
		//A - abs(A + B) + B < 0 -> A + B < 0
		final Term A = calc.valTerm(Type.FLOAT, "A");
		final Term B = calc.valTerm(Type.FLOAT, "B");
		final Primitive absAplusB = calc.applyFunction(Type.FLOAT, FunctionApplication.ABS, A.add(B)); 
		final Primitive p_post = A.sub(absAplusB).add(B).lt(calc.valFloat(0)); 
		assertEquals(A.add(B).lt(calc.valFloat(0)), p_post);
	}
	
	@Test
	public void testComposite2() throws Exception {
		//A - abs(B - A) - B < 0 -> B - A > 0
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Term B = calc.valTerm(Type.DOUBLE, "B");
		final Primitive absBminusA = calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, B.sub(A)); 
		final Primitive p_post = A.sub(absBminusA).sub(B).lt(calc.valDouble(0.0d)); 
		assertEquals(B.sub(A).gt(calc.valDouble(0.0d)), p_post);
	}
}
