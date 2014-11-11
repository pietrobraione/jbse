package jbse.dec;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Expression;
import jbse.mem.FunctionApplication;
import jbse.mem.Objekt;
import jbse.mem.Primitive;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.Term;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.rewr.RewriterPolynomials;
import jbse.rewr.RewriterTrigNormalize;

public class DecisionProcedureSignAnalysisTest {
	CalculatorRewriting calc;
	DecisionProcedureSignAnalysis dec;

	static class NoDecisionException extends DecisionException {
		private static final long serialVersionUID = 1L;		
	}
	
	static class DecisionProcedureNoDecision implements DecisionProcedure {
		protected DecisionProcedureNoDecision() { }

		@Override
		public void pushAssumption(Clause c) { }

		@Override
		public void clearAssumptions() { }

		@Override
		public Collection<Clause> getAssumptions() 
		throws DecisionException { return null; }

		@Override
		public boolean isSat(Expression exp) 
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatNull(ReferenceSymbolic r) 
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatExpands(ReferenceSymbolic r, String className)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatInitialized(String className)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatNotInitialized(String className)
		throws DecisionException { throw new NoDecisionException(); }
	}
	
	@Before
	public void setUp() throws UnexpectedInternalException {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		dec = new DecisionProcedureSignAnalysis(new DecisionProcedureNoDecision(), calc);
	}
	
	@Test
	public void simpleTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A > 0 |-/- A <= 0
		Term A = calc.valTerm(Type.INT, "A");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.le(calc.valInt(0))));
	}
	
	@Test
	public void simpleTest2() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//0 < A |-/- A <= 0 
		Term A = calc.valTerm(Type.INT, "A");
		dec.pushAssumption(new ClauseAssume((Expression) calc.valInt(0).lt(A)));
		assertFalse(dec.isSat((Expression) A.le(calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest3() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A > 0, B <= 0 |-?- A * B >= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		dec.isSat((Expression) A.mul(B).ge(calc.valInt(0)));
	}
	
	@Test
	public void simpleTest4() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A > 0, B <= 0 |-/- A * B > 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.mul(B).gt(calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest5() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A > B |-?- A > 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(B)));
		dec.isSat((Expression) A.gt(calc.valInt(0)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest6() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A > B |-?- A <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(B)));
		dec.isSat((Expression) A.le(calc.valInt(0)));
	}
	
	@Test
	public void simpleTest7() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : INT, A > 0 |-/- A < 1
		Term A = calc.valTerm(Type.INT, "A");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.lt(calc.valInt(1))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest8() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : FLOAT, A > 0 |-?- A < 1
		Term A = calc.valTerm(Type.FLOAT, "A");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valFloat(0.0f))));
		assertTrue(dec.isSat((Expression) A.lt(calc.valInt(1))));
	}
	
	@Test
	public void simpleTest9() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A > 0, B > 0 |- A * B >= 0
		Term A = calc.valTerm(Type.DOUBLE, "A");
		Term B = calc.valTerm(Type.DOUBLE, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0).to(Type.DOUBLE))));
		dec.pushAssumption(new ClauseAssume((Expression) B.gt(calc.valDouble(0.0d))));
		assertTrue(dec.isSat((Expression) A.mul(B).ge(calc.valDouble(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest10() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A >= 0, B <= 0 |-?- A * B == 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.ge(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		dec.isSat((Expression) A.mul(B).eq(calc.valInt(0)));
	}
	
	@Test
	public void mulTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : INT, B : INT, A > 0, B <= 0 |-/- A * B > 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.mul(B).gt(calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest2() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : INT, B : INT, A > 0, B <= 0 |-?- A * B > -1
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		dec.isSat((Expression) A.mul(B).gt(calc.valInt(-1)));
	}
	
	@Test
	public void mulTest3() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : INT, B : INT, A > 0, B < 0 |-/- A * B > -1
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.lt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.mul(B).gt(calc.valInt(-1))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest4() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A : INT, B : DOUBLE, A > 0, B < 0 |-?- A * B > -1
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.DOUBLE, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.lt(calc.valInt(0))));
		dec.isSat((Expression) A.to(Type.DOUBLE).mul(B).gt(calc.valInt(-1)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A >= 0, B >= 0 |-?- A + B <= 0
		Term A = calc.valTerm(Type.DOUBLE, "A");
		Term B = calc.valTerm(Type.DOUBLE, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.ge(calc.valDouble(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.ge(calc.valDouble(0))));
		dec.isSat((Expression) A.add(B).le(calc.valDouble(0)));
	}
	
	@Test
	public void addTest2() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A > 0, B >= 0 |-/- A + B <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.ge(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.add(B).le(calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest3() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A >= 0, B >= 0 |-?- A - B <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.ge(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.ge(calc.valInt(0))));
		dec.isSat((Expression) A.sub(B).le(calc.valInt(0)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest4() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A >= 0, B >= 0 |-?- A - B > 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.ge(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.ge(calc.valInt(0))));
		dec.isSat((Expression) A.sub(B).le(calc.valInt(0)));
	}
	
	@Test
	public void addTest5() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A > 0, B <= 0 |-/- A - B <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.sub(B).le(calc.valInt(0))));
	}
	
	@Test
	public void negTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A > 0 |-/- -A >= 0
		Term A = calc.valTerm(Type.INT, "A");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.neg().ge(calc.valInt(0))));
	}
	
	@Test
	public void negTest2() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A > 0, B < 0 |-/- -A * B <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.lt(calc.valDouble(0.0d))));
		assertFalse(dec.isSat((Expression) A.neg().mul(B).le(calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A * B > 0 |-/- A * B <= 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.mul(B).gt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.mul(B).le(calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest2() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		// A * B >= 0, A >= 0, B <= 0 |-/- A * B != 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.mul(B).ge(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) A.ge(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.le(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.mul(B).ne(calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest3() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		// A > 0, B > 0, A * B + -1 * sqrt(C * C)) > 0 |-/- (A * B + -1 * sqrt(C * C)) / (-1 * A) > 0
		Term A = calc.valTerm(Type.DOUBLE, "A");
		Term B = calc.valTerm(Type.DOUBLE, "B");
		Term C = calc.valTerm(Type.DOUBLE, "C");
		dec.pushAssumption(new ClauseAssume((Expression) A.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) B.gt(calc.valInt(0))));
		Primitive p = A.mul(B).add(calc.valDouble(-1.0d).mul(calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, C.mul(C))));
		dec.pushAssumption(new ClauseAssume((Expression) p.gt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) p.div(calc.valDouble(-1.0d).mul(A)).gt(calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void divAssumptionTest1() throws DecisionException, InvalidOperandException, InvalidTypeException, UnexpectedInternalException {
		//A / B < C |-?- A / B = C (it can only check exp rel_op number)
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		dec.pushAssumption(new ClauseAssume((Expression) A.div(B).lt(C)));
		assertFalse(dec.isSat((Expression) A.div(B).eq(C)));
	}
	
	@Test
	public void trigTest1() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		//true |-/- asin(A) + 10 < 0
		Term A = calc.valTerm(Type.INT, "A");
		assertFalse(dec.isSat((Expression) calc.applyFunction(Type.DOUBLE, FunctionApplication.ASIN, A).add(calc.valDouble(10.0d)).lt(calc.valInt(0))));
	}

	@Test
	public void trigTest2() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		//true |-/- cos(atan(A)) <= 0
		Term A = calc.valTerm(Type.INT, "A");
		assertFalse(dec.isSat((Expression) calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A)).le(calc.valInt(0))));
	}

	@Test
	public void trigTest3() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		//true |- cos(atan(A)) > 0 (decided by simplification to true)
		Term A = calc.valTerm(Type.INT, "A");
		assertTrue(dec.isSat((Expression) calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A)).gt(calc.valInt(0))));
	}

	@Test
	public void trigTest4() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		//true |-/- cos(PI + atan(A)) >= 0
		calc.addRewriter(new RewriterTrigNormalize());
		Term A = calc.valTerm(Type.DOUBLE, "A");
		assertFalse(dec.isSat((Expression) calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, calc.valDouble(Math.PI).add(calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A))).ge(calc.valInt(0))));
	}

	@Test
	public void trigTest5() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		//A < 0 |-/- atan(A) >= 0
		calc.addRewriter(new RewriterTrigNormalize());
		Term A = calc.valTerm(Type.DOUBLE, "A");
		dec.pushAssumption(new ClauseAssume((Expression) A.lt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A).ge(calc.valInt(0))));
	}
	
	@Test
	public void funTest1() throws DecisionException, InvalidTypeException, InvalidOperandException, UnexpectedInternalException {
		calc.addRewriter(new RewriterPolynomials()); //necessary to normalize ~x to -1.0 * x
		Term A = calc.valTerm(Type.DOUBLE, "A");
		Term B = calc.valTerm(Type.DOUBLE, "B");
		Term C = calc.valTerm(Type.DOUBLE, "C");
		Term D = calc.valTerm(Type.DOUBLE, "D");
		Term E = calc.valTerm(Type.DOUBLE, "E");
		Term F = calc.valTerm(Type.DOUBLE, "F");
		FunctionApplication f = (FunctionApplication) calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.sub(B).mul(A.sub(B)).add(C.sub(D).mul(C.sub(D))));
		dec.pushAssumption(new ClauseAssume((Expression) E.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) F.gt(calc.valInt(0))));
		dec.pushAssumption(new ClauseAssume((Expression) calc.valDouble(-1.0d).mul(f).add(E.mul(F)).div(calc.valDouble(-1.0d).mul(E)).lt(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) f.sub(E.mul(F)).ge(calc.valInt(0))));
	}
}