package jbse.dec;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFile;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.rewr.RewriterPolynomials;
import jbse.rewr.RewriterTrigNormalize;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class DecisionProcedureSignAnalysisTest {
	HistoryPoint hist;
	CalculatorRewriting calc;
	DecisionProcedureSignAnalysis dec;
	
	static class NoDecisionException extends DecisionException {
		private static final long serialVersionUID = 1L;		
	}
	
	static class DecisionProcedureNoDecision implements DecisionProcedure {
        private final CalculatorRewriting calc;
        protected DecisionProcedureNoDecision(CalculatorRewriting calc) { this.calc = calc; }

		@Override
		public Calculator getCalculator() { return this.calc; }

		@Override
		public void pushAssumption(Clause c) { }

		@Override
		public void clearAssumptions() { }

		@Override
		public List<Clause> getAssumptions() 
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
		public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatInitialized(ClassFile classFile)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatNotInitialized(ClassFile classFile)
		throws DecisionException { throw new NoDecisionException(); }
	}
	
	@Before
	public void setUp() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		this.hist = HistoryPoint.unknown();
        this.calc = new CalculatorRewriting();
        this.calc.addRewriter(new RewriterOperationOnSimplex());
		this.dec = new DecisionProcedureSignAnalysis(new DecisionProcedureNoDecision(this.calc));
	}
	
	@Test
	public void simpleTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0 |-/- A <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).le(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void simpleTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//0 < A |-/- A <= 0 
		final Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.pushInt(0).lt(A).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).le(this.calc.valInt(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0, B <= 0 |-?- A * B >= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).mul(B).ge(this.calc.valInt(0)).pop());
	}
	
	@Test
	public void simpleTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0, B <= 0 |-/- A * B > 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).mul(B).gt(this.calc.valInt(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest5() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > B |-?- A > 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(B).pop()));
		this.dec.isSat((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop());
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest6() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > B |-?- A <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(B).pop()));
		this.dec.isSat((Expression) this.calc.push(A).le(this.calc.valInt(0)).pop());
	}
	
	@Test
	public void simpleTest7() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, A > 0 |-/- A < 1
		final Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).lt(this.calc.valInt(1)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest8() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : FLOAT, A > 0 |-?- A < 1
		final Term A = this.calc.valTerm(Type.FLOAT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valFloat(0.0f)).pop()));
		assertTrue(this.dec.isSat((Expression) this.calc.push(A).lt(this.calc.valInt(1)).pop()));
	}
	
	@Test
	public void simpleTest9() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B > 0 |- A * B >= 0
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.pushInt(0).to(Type.DOUBLE).pop()).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).gt(this.calc.valDouble(0.0d)).pop()));
		assertTrue(this.dec.isSat((Expression) this.calc.push(A).mul(B).ge(this.calc.valDouble(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest10() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B <= 0 |-?- A * B == 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).mul(B).eq(this.calc.valInt(0)).pop());
	}
	
	@Test
	public void mulTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B <= 0 |-/- A * B > 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).mul(B).gt(this.calc.valInt(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B <= 0 |-?- A * B > -1
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).mul(B).gt(this.calc.valInt(-1)).pop());
	}
	
	@Test
	public void mulTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B < 0 |-/- A * B > -1
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).lt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).mul(B).gt(this.calc.valInt(-1)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : DOUBLE, A > 0, B < 0 |-?- A * B > -1
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).lt(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).to(Type.DOUBLE).mul(B).gt(this.calc.valInt(-1)).pop());
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A + B <= 0
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).ge(this.calc.valDouble(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).ge(this.calc.valDouble(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).add(B).le(this.calc.valDouble(0)).pop());
	}
	
	@Test
	public void addTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B >= 0 |-/- A + B <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).ge(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).add(B).le(this.calc.valInt(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A - B <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).ge(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).sub(B).le(this.calc.valInt(0)).pop());
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A - B > 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).ge(this.calc.valInt(0)).pop()));
		this.dec.isSat((Expression) this.calc.push(A).sub(B).le(this.calc.valInt(0)).pop());
	}
	
	@Test
	public void addTest5() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B <= 0 |-/- A - B <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).sub(B).le(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void negTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0 |-/- -A >= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).neg().ge(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void negTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B < 0 |-/- -A * B <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).lt(this.calc.valDouble(0.0d)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).neg().mul(B).le(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void complexAssumptionTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A * B > 0 |-/- A * B <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).mul(B).gt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).mul(B).le(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void complexAssumptionTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A * B >= 0, A >= 0, B <= 0 |-/- A * B != 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).mul(B).ge(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).le(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(A).mul(B).ne(this.calc.valInt(0)).pop()));
	}
	
	@Test
	public void complexAssumptionTest3() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		// A > 0, B > 0, A * B + -1 * sqrt(C * C)) > 0 |-/- (A * B + -1 * sqrt(C * C)) / (-1 * A) > 0
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Term C = this.calc.valTerm(Type.DOUBLE, "C");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(B).gt(this.calc.valInt(0)).pop()));
		final Primitive p = this.calc.push(A).mul(B).add(this.calc.pushDouble(-1.0d).mul(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(C).mul(C).pop())).pop()).pop();
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(p).gt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(p).div(this.calc.pushDouble(-1.0d).mul(A).pop()).gt(this.calc.valInt(0)).pop()));
	}
	
	@Test(expected=NoDecisionException.class)
	public void divAssumptionTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A / B < C |-?- A / B = C (it can only check exp rel_op number)
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Term C = this.calc.valTerm(Type.INT, "C");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).div(B).lt(C).pop()));
		this.dec.isSat((Expression) this.calc.push(A).div(B).eq(C).pop());
	}
	
	@Test
	public void trigTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- asin(A) + 10 < 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		assertFalse(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ASIN, A).add(this.calc.valDouble(10.0d)).lt(this.calc.valInt(0)).pop()));
	}

	@Test
	public void trigTest2() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- cos(atan(A)) <= 0
		final Term A = this.calc.valTerm(Type.INT, "A");
		assertFalse(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.COS, this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A).pop()).le(this.calc.valInt(0)).pop()));
	}

	@Test
	public void trigTest3() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |- cos(atan(A)) > 0 (decided by simplification to true)
		final Term A = this.calc.valTerm(Type.INT, "A");
		assertTrue(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.COS, this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A)).gt(this.calc.valInt(0)).pop()));
	}

	@Test
	public void trigTest4() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- cos(PI + atan(A)) >= 0
		this.calc.addRewriter(new RewriterTrigNormalize());
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		assertFalse(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.COS, this.calc.pushDouble(Math.PI).add(this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A)).pop()).ge(this.calc.valInt(0)).pop()));
	}

	@Test
	public void trigTest5() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A < 0 |-/- atan(A) >= 0
		this.calc.addRewriter(new RewriterTrigNormalize());
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(A).lt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.ATAN, A).ge(this.calc.valInt(0)).pop()));
	}

    @Test
    public void powTest1() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |- pow(A, 0.0) > 0
    	final Term A = this.calc.valTerm(Type.DOUBLE, "A");
        assertTrue(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.POW, A, this.calc.valDouble(0.0)).gt(this.calc.valInt(0)).pop()));
    }
	
    @Test
    public void powTest2() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |-/- pow(A, 2.0) < 0
    	final Term A = this.calc.valTerm(Type.DOUBLE, "A");
        assertFalse(this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.POW, A, this.calc.valDouble(2.0)).lt(this.calc.valInt(0)).pop()));
    }
    
    @Test(expected=NoDecisionException.class)
    public void powTest3() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |-?- pow(A, 2.0) >= 0 (can refute but cannot prove)
    	final Term A = this.calc.valTerm(Type.DOUBLE, "A");
        this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.POW, A, this.calc.valDouble(2.0)).ge(this.calc.valInt(0)).pop());
    }
    
	@Test
	public void funTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		this.calc.addRewriter(new RewriterPolynomials()); //necessary to normalize ~x to -1.0 * x
		final Term A = this.calc.valTerm(Type.DOUBLE, "A");
		final Term B = this.calc.valTerm(Type.DOUBLE, "B");
		final Term C = this.calc.valTerm(Type.DOUBLE, "C");
		final Term D = this.calc.valTerm(Type.DOUBLE, "D");
		final Term E = this.calc.valTerm(Type.DOUBLE, "E");
		final Term F = this.calc.valTerm(Type.DOUBLE, "F");
		final PrimitiveSymbolicApply f = (PrimitiveSymbolicApply) this.calc.applyFunctionPrimitiveAndPop(Type.DOUBLE, this.hist, PrimitiveSymbolicApply.SQRT, this.calc.push(A).sub(B).mul(this.calc.push(A).sub(B).pop()).add(this.calc.push(C).sub(D).mul(this.calc.push(C).sub(D).pop()).pop()).pop());
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(E).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.push(F).gt(this.calc.valInt(0)).pop()));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.pushDouble(-1.0d).mul(f).add(this.calc.push(E).mul(F).pop()).div(this.calc.pushDouble(-1.0d).mul(E).pop()).lt(this.calc.valInt(0)).pop()));
		assertFalse(this.dec.isSat((Expression) this.calc.push(f).sub(this.calc.push(E).mul(F).pop()).ge(this.calc.valInt(0)).pop()));
	}
}