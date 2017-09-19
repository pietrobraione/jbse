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
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class DecisionProcedureEqualityTest {
	final CalculatorRewriting calc;
    final ClassHierarchy hier;
	DecisionProcedureEquality dec;
	
	public DecisionProcedureEqualityTest() throws InvalidClassFileFactoryClassException {
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
		this.dec = new DecisionProcedureEquality(new DecisionProcedureNoDecision(), this.calc);
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A == B |- B == A
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		this.dec.isSat(this.hier, (Expression) B.eq(A));
	}	
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest2() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A == B |- f(A) == f(B)
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.INT, "f", A).eq(this.calc.applyFunction(Type.INT, "f", B)));
	}
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest3() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |- f((A - B) / (C - D)) == f((E - F) / (G - H))
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		Term D = this.calc.valTerm(Type.INT, "D");
		Term E = this.calc.valTerm(Type.INT, "E");
		Term F = this.calc.valTerm(Type.INT, "F");
		Term G = this.calc.valTerm(Type.INT, "G");
		Term H = this.calc.valTerm(Type.INT, "H");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).eq(this.calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H)))));
	}	
	
	@Test
	public void simpleTest4() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |-/- f((A - B) / (C - D)) != f((E - F) / (G - H))
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		Term D = this.calc.valTerm(Type.INT, "D");
		Term E = this.calc.valTerm(Type.INT, "E");
		Term F = this.calc.valTerm(Type.INT, "F");
		Term G = this.calc.valTerm(Type.INT, "G");
		Term H = this.calc.valTerm(Type.INT, "H");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		assertFalse(dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).ne(this.calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H))))));
	}	
	
	@Test(expected=NoDecisionException.class)
	public void simpleTest5() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |- !(f((A - B) / (C - D)) != f((E - F) / (G - H)))
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		Term D = this.calc.valTerm(Type.INT, "D");
		Term E = this.calc.valTerm(Type.INT, "E");
		Term F = this.calc.valTerm(Type.INT, "F");
		Term G = this.calc.valTerm(Type.INT, "G");
		Term H = this.calc.valTerm(Type.INT, "H");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		this.dec.isSat(this.hier, (Expression) this.calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).ne(this.calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H)))).not());
	}	
	
	@Test(expected=NoDecisionException.class)
	public void pushExpTest1() 
	throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
		//f(A) == g(B) |- A + g(f(A)) == A + g(g(B))
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.applyFunction(Type.INT, "f", A).eq(this.calc.applyFunction(Type.INT, "g", B))));
		this.dec.isSat(this.hier, (Expression) A.add(this.calc.applyFunction(Type.INT, "g", this.calc.applyFunction(Type.INT, "f", A))).eq(A.add(this.calc.applyFunction(Type.INT, "g", this.calc.applyFunction(Type.INT, "g", B)))));
	}	
	
	@Test(expected=NoDecisionException.class)
	public void transitiveTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A == B, B == C |- A == C
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(C)));
		this.dec.isSat(this.hier, (Expression) A.eq(C));
	}	
	
	@Test
	public void complexExpressionTest1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//A + -1 * B == 0 |-/- A + -1 * B != 0
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		this.dec.pushAssumption(new ClauseAssume((Expression) A.add(this.calc.valInt(-1).mul(B)).eq(this.calc.valInt(0))));
		assertFalse(this.dec.isSat(this.hier, (Expression) A.add(this.calc.valInt(-1).mul(B)).ne(this.calc.valInt(0))));
	}	
}
