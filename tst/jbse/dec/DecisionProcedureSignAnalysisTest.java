package jbse.dec;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.Type;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.rewr.RewriterPolynomials;
import jbse.rewr.RewriterTrigNormalize;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class DecisionProcedureSignAnalysisTest {
	final CalculatorRewriting calc;
    final ClassHierarchy hier;
	DecisionProcedureSignAnalysis dec;
	
	public DecisionProcedureSignAnalysisTest() throws InvalidClassFileFactoryClassException {
        this.calc = new CalculatorRewriting();
        this.calc.addRewriter(new RewriterOperationOnSimplex());
        this.hier = new ClassHierarchy(new Classpath(), ClassFileFactoryJavassist.class, new HashMap<>());
	}

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
		public boolean isSat(ClassHierarchy hier, Expression exp) 
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatInitialized(ClassHierarchy hier, String className)
		throws DecisionException { throw new NoDecisionException(); }

		@Override
		public boolean isSatNotInitialized(ClassHierarchy hier, String className)
		throws DecisionException { throw new NoDecisionException(); }
	}
	
	@Before
	public void setUp() {
		this.dec = new DecisionProcedureSignAnalysis(new DecisionProcedureNoDecision(), this.calc);
	}
	
	@Test
	public void simpleTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0 |-/- A <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.le(this.calc.valInt(0))));
	}
	
	@Test
	public void simpleTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//0 < A |-/- A <= 0 
		Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.valInt(0).lt(A)));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.le(this.calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0, B <= 0 |-?- A * B >= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.mul(B).ge(this.calc.valInt(0)));
	}
	
	@Test
	public void simpleTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > 0, B <= 0 |-/- A * B > 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.mul(B).gt(this.calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest5() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > B |-?- A > 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(B)));
		this.dec.isSat(this.hier, (Expression) A.gt(this.calc.valInt(0)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest6() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A > B |-?- A <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(B)));
		this.dec.isSat(this.hier, (Expression) A.le(this.calc.valInt(0)));
	}
	
	@Test
	public void simpleTest7() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, A > 0 |-/- A < 1
		Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.lt(this.calc.valInt(1))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest8() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : FLOAT, A > 0 |-?- A < 1
		Term A = this.calc.valTerm(Type.FLOAT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valFloat(0.0f))));
		assertTrue(this.dec.isSat(this.hier, (Expression) A.lt(this.calc.valInt(1))));
	}
	
	@Test
	public void simpleTest9() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B > 0 |- A * B >= 0
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0).to(Type.DOUBLE))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.gt(this.calc.valDouble(0.0d))));
		assertTrue(this.dec.isSat(this.hier, (Expression) A.mul(B).ge(this.calc.valDouble(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest10() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B <= 0 |-?- A * B == 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.ge(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.mul(B).eq(this.calc.valInt(0)));
	}
	
	@Test
	public void mulTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B <= 0 |-/- A * B > 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.mul(B).gt(this.calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B <= 0 |-?- A * B > -1
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.mul(B).gt(this.calc.valInt(-1)));
	}
	
	@Test
	public void mulTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : INT, A > 0, B < 0 |-/- A * B > -1
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.lt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.mul(B).gt(this.calc.valInt(-1))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void mulTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A : INT, B : DOUBLE, A > 0, B < 0 |-?- A * B > -1
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.lt(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.to(Type.DOUBLE).mul(B).gt(this.calc.valInt(-1)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A + B <= 0
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		Term B = this.calc.valTerm(Type.DOUBLE, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.ge(this.calc.valDouble(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.ge(this.calc.valDouble(0))));
		this.dec.isSat(this.hier, (Expression) A.add(B).le(this.calc.valDouble(0)));
	}
	
	@Test
	public void addTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B >= 0 |-/- A + B <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.ge(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.add(B).le(this.calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A - B <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.ge(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.ge(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.sub(B).le(this.calc.valInt(0)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void addTest4() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0, B >= 0 |-?- A - B > 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.ge(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.ge(this.calc.valInt(0))));
		this.dec.isSat(this.hier, (Expression) A.sub(B).le(this.calc.valInt(0)));
	}
	
	@Test
	public void addTest5() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B <= 0 |-/- A - B <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.sub(B).le(this.calc.valInt(0))));
	}
	
	@Test
	public void negTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0 |-/- -A >= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.neg().ge(this.calc.valInt(0))));
	}
	
	@Test
	public void negTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A > 0, B < 0 |-/- -A * B <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.lt(this.calc.valDouble(0.0d))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.neg().mul(B).le(this.calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A * B > 0 |-/- A * B <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.mul(B).gt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.mul(B).le(this.calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A * B >= 0, A >= 0, B <= 0 |-/- A * B != 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.mul(B).ge(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) A.ge(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.le(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.mul(B).ne(this.calc.valInt(0))));
	}
	
	@Test
	public void complexAssumptionTest3() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		// A > 0, B > 0, A * B + -1 * sqrt(C * C)) > 0 |-/- (A * B + -1 * sqrt(C * C)) / (-1 * A) > 0
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		Term B = this.calc.valTerm(Type.DOUBLE, "B");
		Term C = this.calc.valTerm(Type.DOUBLE, "C");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.gt(this.calc.valInt(0))));
		Primitive p = A.mul(B).add(this.calc.valDouble(-1.0d).mul(this.calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, C.mul(C))));
		this.dec.pushAssumption(new ClauseAssume((Expression) p.gt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) p.div(this.calc.valDouble(-1.0d).mul(A)).gt(this.calc.valInt(0))));
	}
	
	@Test(expected=NoDecisionException.class)
	public void divAssumptionTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A / B < C |-?- A / B = C (it can only check exp rel_op number)
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.div(B).lt(C)));
		this.dec.isSat(this.hier, (Expression) A.div(B).eq(C));
	}
	
	@Test
	public void trigTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- asin(A) + 10 < 0
		Term A = this.calc.valTerm(Type.INT, "A");
		assertFalse(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.ASIN, A).add(this.calc.valDouble(10.0d)).lt(this.calc.valInt(0))));
	}

	@Test
	public void trigTest2() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- cos(atan(A)) <= 0
		Term A = this.calc.valTerm(Type.INT, "A");
		assertFalse(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, this.calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A)).le(this.calc.valInt(0))));
	}

	@Test
	public void trigTest3() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |- cos(atan(A)) > 0 (decided by simplification to true)
		Term A = this.calc.valTerm(Type.INT, "A");
		assertTrue(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, this.calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A)).gt(this.calc.valInt(0))));
	}

	@Test
	public void trigTest4() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//true |-/- cos(PI + atan(A)) >= 0
		this.calc.addRewriter(new RewriterTrigNormalize());
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		assertFalse(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.COS, this.calc.valDouble(Math.PI).add(this.calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A))).ge(this.calc.valInt(0))));
	}

	@Test
	public void trigTest5() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A < 0 |-/- atan(A) >= 0
		this.calc.addRewriter(new RewriterTrigNormalize());
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.lt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.ATAN, A).ge(this.calc.valInt(0))));
	}

    @Test
    public void powTest1() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |- pow(A, 0.0) > 0
        Term A = this.calc.valTerm(Type.DOUBLE, "A");
        assertTrue(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.POW, A, this.calc.valDouble(0.0)).gt(this.calc.valInt(0))));
    }
	
    @Test
    public void powTest2() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |-/- pow(A, 2.0) < 0
        Term A = this.calc.valTerm(Type.DOUBLE, "A");
        assertFalse(this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.POW, A, this.calc.valDouble(2.0)).lt(this.calc.valInt(0))));
    }
    
    @Test(expected=NoDecisionException.class)
    public void powTest3() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //true |-?- pow(A, 2.0) >= 0 (can refute but cannot prove)
        Term A = this.calc.valTerm(Type.DOUBLE, "A");
        this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.POW, A, this.calc.valDouble(2.0)).ge(this.calc.valInt(0)));
    }
    
	@Test
	public void funTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		this.calc.addRewriter(new RewriterPolynomials()); //necessary to normalize ~x to -1.0 * x
		Term A = this.calc.valTerm(Type.DOUBLE, "A");
		Term B = this.calc.valTerm(Type.DOUBLE, "B");
		Term C = this.calc.valTerm(Type.DOUBLE, "C");
		Term D = this.calc.valTerm(Type.DOUBLE, "D");
		Term E = this.calc.valTerm(Type.DOUBLE, "E");
		Term F = this.calc.valTerm(Type.DOUBLE, "F");
		FunctionApplication f = (FunctionApplication) this.calc.applyFunction(Type.DOUBLE, FunctionApplication.SQRT, A.sub(B).mul(A.sub(B)).add(C.sub(D).mul(C.sub(D))));
		this.dec.pushAssumption(new ClauseAssume((Expression) E.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) F.gt(this.calc.valInt(0))));
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.valDouble(-1.0d).mul(f).add(E.mul(F)).div(this.calc.valDouble(-1.0d).mul(E)).lt(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) f.sub(E.mul(F)).ge(this.calc.valInt(0))));
	}
}