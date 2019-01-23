package jbse.dec;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Collection;

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
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.ReferenceSymbolic;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class DecisionProcedureEqualityTest {
	HistoryPoint hist;
    CalculatorRewriting calc;
    DecisionProcedureEquality dec;

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
    public void setUp() throws InvalidClassFileFactoryClassException, IOException {
		this.hist = HistoryPoint.unknown();
        this.calc = new CalculatorRewriting();
        this.calc.addRewriter(new RewriterOperationOnSimplex());
        this.dec = new DecisionProcedureEquality(new DecisionProcedureNoDecision(), this.calc);
    }

    @Test(expected=NoDecisionException.class)
    public void simpleTest1() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //A == B |- B == A
        final Term A = this.calc.valTerm(Type.INT, "A");
        final Term B = this.calc.valTerm(Type.INT, "B");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
        this.dec.isSat((Expression) B.eq(A));
    }	

    @Test(expected=NoDecisionException.class)
    public void simpleTest2() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //A == B |- f(A) == f(B)
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
        this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A).eq(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", B)));
    }

    @Test(expected=NoDecisionException.class)
    public void simpleTest3() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //A == E, B == F, C == G, D == H |- f((A - B) / (C - D)) == f((E - F) / (G - H))
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
    	final Term C = this.calc.valTerm(Type.INT, "C");
    	final Term D = this.calc.valTerm(Type.INT, "D");
    	final Term E = this.calc.valTerm(Type.INT, "E");
    	final Term F = this.calc.valTerm(Type.INT, "F");
    	final Term G = this.calc.valTerm(Type.INT, "G");
    	final Term H = this.calc.valTerm(Type.INT, "H");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
        this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
        this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
        this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
        this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A.sub(B).div(C.sub(D))).eq(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", E.sub(F).div(G.sub(H)))));
    }	

    @Test
    public void simpleTest4() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //A == E, B == F, C == G, D == H |-/- f((A - B) / (C - D)) != f((E - F) / (G - H))
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
    	final Term C = this.calc.valTerm(Type.INT, "C");
    	final Term D = this.calc.valTerm(Type.INT, "D");
    	final Term E = this.calc.valTerm(Type.INT, "E");
    	final Term F = this.calc.valTerm(Type.INT, "F");
    	final Term G = this.calc.valTerm(Type.INT, "G");
    	final Term H = this.calc.valTerm(Type.INT, "H");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
        this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
        this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
        this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
        assertFalse(dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A.sub(B).div(C.sub(D))).ne(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", E.sub(F).div(G.sub(H))))));
    }	

    @Test(expected=NoDecisionException.class)
    public void simpleTest5() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //A == E, B == F, C == G, D == H |- !(f((A - B) / (C - D)) != f((E - F) / (G - H)))
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
    	final Term C = this.calc.valTerm(Type.INT, "C");
    	final Term D = this.calc.valTerm(Type.INT, "D");
    	final Term E = this.calc.valTerm(Type.INT, "E");
    	final Term F = this.calc.valTerm(Type.INT, "F");
    	final Term G = this.calc.valTerm(Type.INT, "G");
    	final Term H = this.calc.valTerm(Type.INT, "H");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(E)));
        this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(F)));
        this.dec.pushAssumption(new ClauseAssume((Expression) C.eq(G)));
        this.dec.pushAssumption(new ClauseAssume((Expression) D.eq(H)));
        this.dec.isSat((Expression) this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A.sub(B).div(C.sub(D))).ne(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", E.sub(F).div(G.sub(H)))).not());
    }	

    @Test(expected=NoDecisionException.class)
    public void pushExpTest1() 
    throws InvalidInputException, DecisionException, InvalidTypeException, InvalidOperandException {
        //f(A) == g(B) |- A + g(f(A)) == A + g(g(B))
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
        this.dec.pushAssumption(new ClauseAssume((Expression) this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A).eq(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "g", B))));
        this.dec.isSat((Expression) A.add(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "g", this.calc.applyFunctionPrimitive(Type.INT, this.hist, "f", A))).eq(A.add(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "g", this.calc.applyFunctionPrimitive(Type.INT, this.hist, "g", B)))));
    }

    @Test(expected=NoDecisionException.class)
    public void transitiveTest1() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
        //A == B, B == C |- A == C
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
    	final Term C = this.calc.valTerm(Type.INT, "C");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.eq(B)));
        this.dec.pushAssumption(new ClauseAssume((Expression) B.eq(C)));
        this.dec.isSat((Expression) A.eq(C));
    }	

    @Test
    public void complexExpressionTest1() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
        //A + -1 * B == 0 |-/- A + -1 * B != 0
    	final Term A = this.calc.valTerm(Type.INT, "A");
    	final Term B = this.calc.valTerm(Type.INT, "B");
        this.dec.pushAssumption(new ClauseAssume((Expression) A.add(this.calc.valInt(-1).mul(B)).eq(this.calc.valInt(0))));
        assertFalse(this.dec.isSat((Expression) A.add(this.calc.valInt(-1).mul(B)).ne(this.calc.valInt(0))));
    }	
}
