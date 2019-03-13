package jbse.rewr;

import static org.junit.Assert.assertEquals;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.Before;
import org.junit.Test;

public class RewriterSqrtTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials()); //necessary to normalize results, but it does not seem necessary to RewriterSqrt by itself
		this.calc.addRewriter(new RewriterSqrt());
	}
	
	@Test
	public void testBasic() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(A * A) -> abs(A)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).mul(A).pop()); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, A), p_post);
	}
	
	@Test
	public void testDoubleMult() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt((A * B) * (B * A)) -> abs(A * B)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive dmul = this.calc.push(A).mul(B).mul(this.calc.push(B).mul(A).pop()).pop();
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, dmul); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, this.calc.push(A).mul(B).pop()), p_post);
	}
	
	@Test
	public void testNested() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(A * sqrt(A * A)) -> sqrt(A * abs(A))
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).mul(A).pop())).pop()); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, A)).pop()), p_post);
	}	
	
	@Test
	public void testMult() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(A * 2 * A) -> abs(A) * sqrt(2)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).mul(this.calc.pushDouble(2.0d).mul(A).pop()).pop()); 
		assertEquals(this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, A).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.valDouble(2.0d))).pop(), p_post); 
		//TODO the assertion check is quite fragile, make it more robust
	}	
	
	@Test
	public void testBinomial1() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(A * A + 2 * A * B + B * B) -> abs(A + B)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive binomial = this.calc.push(A).mul(A).add(this.calc.pushDouble(2.0d).mul(A).mul(B).pop()).add(this.calc.push(B).mul(B).pop()).pop(); 
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, binomial); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, this.calc.push(A).add(B).pop()), p_post); 
		//this check works thanks to polynomial rewriter normalization of results
	}	
	
	@Test
	public void testBinomial2() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(- 2 * A * B + B * B + A * A) -> abs(A - B) (OR abs(B - A))
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Primitive binomial = this.calc.pushDouble(-2.0d).mul(A).mul(B).add(this.calc.push(B).mul(B).pop()).add(this.calc.push(A).mul(A).pop()).pop();
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, binomial); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, this.calc.push(A).sub(B).pop()), p_post); 
		//TODO this check is fragile, as it does not verifies the OR part; it also depends on polynomial rewriter to normalize results
	}	
	
	@Test
	public void testBinomial3() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//sqrt(2 * f(A) * g(A) + f(A) * f(A) + g(A) * g(A)) -> abs(f(A) + g(A)) (OR abs(g(A) + f(A)))
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive binomial = this.calc.pushDouble(2.0d).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, "f", A)).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, "g", A)).add(this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, "f", A).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, "f", A)).pop()).add(this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, "g", A).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, "g", A)).pop()).pop();
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, binomial); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ABS_DOUBLE, this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, "f", A).add(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, "g", A)).pop()), p_post); 
		//TODO this assertion check is fragile, as it does not verifies the OR part; it also depends on polynomial rewriter to normalize results
	}	
}
