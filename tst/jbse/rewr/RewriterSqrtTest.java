package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterSqrtTest {
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		calc.addRewriter(new RewriterPolynomials()); //necessary to normalize results, but it does not seem necessary to RewriterSqrt by itself
		calc.addRewriter(new RewriterSqrt());
	}
	
	@Test
	public void testBasic() throws InvalidOperandException, InvalidTypeException {
		//sqrt(A * A) -> abs(A)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.mul(A)); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A), p_post);
	}
	
	@Test
	public void testDoubleMult() throws InvalidOperandException, InvalidTypeException {
		//sqrt((A * B) * (B * A)) -> abs(A * B)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Term B = calc.valTerm(Type.DOUBLE, "B");
		final Primitive dmul = A.mul(B).mul(B.mul(A));
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, dmul); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A.mul(B)), p_post);
	}
	
	@Test
	public void testNested() throws InvalidOperandException, InvalidTypeException {
		//sqrt(A * sqrt(A * A)) -> sqrt(A * abs(A))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.mul(calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.mul(A)))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.mul(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A))), p_post);
	}	
	
	@Test
	public void testMult() throws InvalidOperandException, InvalidTypeException {
		//sqrt(A * 2 * A) -> abs(A) * sqrt(2)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.mul(calc.valDouble(2.0d).mul(A))); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A).mul(calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, calc.valDouble(2.0d))), p_post); 
		//TODO the assertion check is quite fragile, make it more robust
	}	
	
	@Test
	public void testBinomial1() throws InvalidOperandException, InvalidTypeException {
		//sqrt(A * A + 2 * A * B + B * B) -> abs(A + B)
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Term B = calc.valTerm(Type.DOUBLE, "B");
		final Primitive binomial = A.mul(A).add(calc.valDouble(2.0d).mul(A).mul(B)).add(B.mul(B)); 
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, binomial); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A.add(B)), p_post); 
		//this check works thanks to polynomial rewriter normalization of results
	}	
	
	@Test
	public void testBinomial2() throws InvalidOperandException, InvalidTypeException {
		//sqrt(- 2 * A * B + B * B + A * A) -> abs(A - B) (OR abs(B - A))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Term B = calc.valTerm(Type.DOUBLE, "B");
		final Primitive binomial = calc.valDouble(-2.0d).mul(A).mul(B).add(B.mul(B)).add(A.mul(A));
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, binomial); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, A.sub(B)), p_post); 
		//TODO this check is fragile, as it does not verifies the OR part; it also depends on polynomial rewriter to normalize results
	}	
	
	@Test
	public void testBinomial3() throws InvalidOperandException, InvalidTypeException {
		//sqrt(2 * f(A) * g(A) + f(A) * f(A) + g(A) * g(A)) -> abs(f(A) + g(A)) (OR abs(g(A) + f(A)))
		final Term A = calc.valTerm(Type.DOUBLE, "A");
		final Primitive binomial = calc.valDouble(2.0d).mul(calc.applyFunction(Type.DOUBLE, "f", A)).mul(calc.applyFunction(Type.DOUBLE, "g", A)).add(calc.applyFunction(Type.DOUBLE, "f", A).mul(calc.applyFunction(Type.DOUBLE, "f", A))).add(calc.applyFunction(Type.DOUBLE, "g", A).mul(calc.applyFunction(Type.DOUBLE, "g", A)));
		final Primitive p_post = calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, binomial); 
		assertEquals(calc.applyFunction(Type.DOUBLE, FunctionApplication.ABS, calc.applyFunction(Type.DOUBLE, "f", A).add(calc.applyFunction(Type.DOUBLE, "g", A))), p_post); 
		//TODO this assertion check is fragile, as it does not verifies the OR part; it also depends on polynomial rewriter to normalize results
	}	
}
