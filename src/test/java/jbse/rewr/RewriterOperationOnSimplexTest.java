package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterOperationOnSimplexTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
	}
	
	@Test
	public void test1() throws InvalidOperandException, InvalidTypeException {
		//(A + 0) * 1 -> A
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Primitive p_post = A.add(calc.valInt(0)).mul(calc.valInt(1));
		assertEquals(A, p_post);
	}
	
	@Test
	public void test2() throws InvalidOperandException, InvalidTypeException {
		//3 == 3L -> true
		final Primitive threeEqThreeL = this.calc.valInt(3).eq(this.calc.valLong(3));
		final Primitive p_post = this.calc.valBoolean(true);
		assertEquals(threeEqThreeL, p_post);
	}
	
	
}
