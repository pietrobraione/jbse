package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterTanTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterTan());
	}
	
	@Test
	public void testSimple1() throws InvalidOperandException, InvalidTypeException {
		//sin(A) / cos(A) -> tan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunctionPrimitive(Type.DOUBLE, null, PrimitiveSymbolicApply.SIN, A).div(calc.applyFunctionPrimitive(Type.DOUBLE, null, PrimitiveSymbolicApply.COS, A)); 
		assertEquals(calc.applyFunctionPrimitive(Type.DOUBLE, null, PrimitiveSymbolicApply.TAN, A), p_post);
	}
}
