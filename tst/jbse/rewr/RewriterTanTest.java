package jbse.rewr;

import static org.junit.Assert.assertEquals;
import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.FunctionApplication;
import jbse.mem.Primitive;
import jbse.mem.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterTanTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() throws UnexpectedInternalException {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterTan());
	}
	
	@Test
	public void testSimple1() throws InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//sin(A) / cos(A) -> tan(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SIN, A).div(calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, A)); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.TAN, A), p_post);
	}
}
