package jbse.dec;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import jbse.Type;
import jbse.exc.dec.DecisionException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidTypeException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Expression;
import jbse.mem.Objekt;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.Term;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;

public class DecisionProcedureEqualityTest {
	final CalculatorRewriting calc;
	DecisionProcedureEquality dec;
	
	public DecisionProcedureEqualityTest() {
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
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
	public void setUp() {
		dec = new DecisionProcedureEquality(new DecisionProcedureNoDecision(), calc);
	}
	
	@Test
	public void simpleTest1() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//A == B |- B == A
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		assertTrue(dec.isSat((Expression) B.eq(A)));
	}	
	
	@Test
	public void simpleTest2() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//A == B |- f(A) == f(B)
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		assertTrue(dec.isSat((Expression) calc.applyFunction(Type.INT, "f", A).eq(calc.applyFunction(Type.INT, "f", B))));
	}
	
	@Test
	public void simpleTest3() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |- f((A - B) / (C - D)) == f((E - F) / (G - H))
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		Term D = calc.valTerm(Type.INT, "D");
		Term E = calc.valTerm(Type.INT, "E");
		Term F = calc.valTerm(Type.INT, "F");
		Term G = calc.valTerm(Type.INT, "G");
		Term H = calc.valTerm(Type.INT, "H");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		assertTrue(dec.isSat((Expression) calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).eq(calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H))))));
	}	
	
	@Test
	public void simpleTest4() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |-/- f((A - B) / (C - D)) != f((E - F) / (G - H))
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		Term D = calc.valTerm(Type.INT, "D");
		Term E = calc.valTerm(Type.INT, "E");
		Term F = calc.valTerm(Type.INT, "F");
		Term G = calc.valTerm(Type.INT, "G");
		Term H = calc.valTerm(Type.INT, "H");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		assertFalse(dec.isSat((Expression) calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).ne(calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H))))));
	}	
	
	@Test
	public void simpleTest5() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//A == E, B == F, C == G, D == H |- !(f((A - B) / (C - D)) != f((E - F) / (G - H)))
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		Term D = calc.valTerm(Type.INT, "D");
		Term E = calc.valTerm(Type.INT, "E");
		Term F = calc.valTerm(Type.INT, "F");
		Term G = calc.valTerm(Type.INT, "G");
		Term H = calc.valTerm(Type.INT, "H");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
		dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
		dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
		dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
		assertTrue(dec.isSat((Expression) calc.applyFunction(Type.INT, "f", A.sub(B).div(C.sub(D))).ne(calc.applyFunction(Type.INT, "f", E.sub(F).div(G.sub(H)))).not()));
	}	
	
	@Test
	public void pushExpTest1() throws DecisionException, InvalidTypeException, InvalidOperandException {
		//f(A) == g(B) |- A + g(f(A)) == A + g(g(B))
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) calc.applyFunction(Type.INT, "f", A).eq(calc.applyFunction(Type.INT, "g", B))));
		assertTrue(dec.isSat((Expression) A.add(calc.applyFunction(Type.INT, "g", calc.applyFunction(Type.INT, "f", A))).eq(A.add(calc.applyFunction(Type.INT, "g", calc.applyFunction(Type.INT, "g", B))))));
	}	
	
	@Test
	public void transitiveTest1() throws DecisionException, InvalidOperandException, InvalidTypeException {
		//A == B, B == C |- A == C
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
		dec.pushAssumption(new ClauseAssume((Expression) B.eq(C)));
		assertTrue(dec.isSat((Expression) A.eq(C)));
	}	
	
	@Test
	public void complexExpressionTest1() throws DecisionException, InvalidOperandException, InvalidTypeException {
		//A + -1 * B == 0 |-/- A + -1 * B != 0
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		dec.pushAssumption(new ClauseAssume((Expression) A.add(calc.valInt(-1).mul(B)).eq(calc.valInt(0))));
		assertFalse(dec.isSat((Expression) A.add(calc.valInt(-1).mul(B)).ne(calc.valInt(0))));
	}	
}
