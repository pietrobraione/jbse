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

public final class RewriterArcTanTest {
	private HistoryPoint hist;
	private CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.calc.addRewriter(new RewriterPolynomials());
		this.calc.addRewriter(new RewriterArcTan());
	}
	
	@Test
	public void testSimple1() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//tan(atan(A)) -> A
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.TAN, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A)); 
		assertEquals(A, p_post);
	}
	
	@Test
	public void testSimple2() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//tan(tan(atan(A))) -> tan(A)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.TAN, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.TAN, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A))); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.TAN, A), p_post);
	}
	
	@Test
	public void testSimple3() throws InvalidOperandException, InvalidTypeException, InvalidInputException {
		//atan(tan(atan(A))) -> atan(A)
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Primitive p_post = this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.TAN, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A))); 
		assertEquals(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A), p_post);
	}
}
