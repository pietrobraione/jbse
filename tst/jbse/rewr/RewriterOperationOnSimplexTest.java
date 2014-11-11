package jbse.rewr;

import static org.junit.Assert.assertEquals;
import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.Primitive;
import jbse.mem.Term;

import org.junit.Before;
import org.junit.Test;

public class RewriterOperationOnSimplexTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() throws UnexpectedInternalException {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
	}
	
	@Test
	public void test1() throws InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//(A + 0) * 1 -> A
		final Term A = calc.valTerm(Type.INT, "A");
		final Primitive p_post = A.add(calc.valInt(0)).mul(calc.valInt(1));
		assertEquals(A, p_post);
	}
}
