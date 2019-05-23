package jbse.dec;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class DecisionProcedureExternalTest {
    private CalculatorRewriting calc;
    private DecisionProcedureExternal dec;
    private DecisionProcedureExternalInterfaceStub extIf;
    
    private final class DecisionProcedureExternalInterfaceStub extends DecisionProcedureExternalInterface {
        private boolean working = true;
        private boolean hasClause = false;
        private Primitive currentPredicate = null;
        private final ArrayDeque<Clause> clauses = new ArrayDeque<>();

        @Override
        public boolean isWorking() {
            return this.working;
        }

        @Override
        public void sendClauseAssume(Primitive predicate) throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssume invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssume invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = predicate;
        }

        @Override
        public void sendClauseAssumeAliases(ReferenceSymbolic r, long heapPos, Objekt o)
        throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeAliases invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeAliases invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = null; //sorry, only numeric predicates in this stub
        }

        @Override
        public void sendClauseAssumeExpands(ReferenceSymbolic r, String className)
        throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeExpands invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeExpands invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = null; //sorry, only numeric predicates in this stub
        }

        @Override
        public void sendClauseAssumeNull(ReferenceSymbolic r) throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeNull invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeNull invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = null; //sorry, only numeric predicates in this stub
       }

        @Override
        public void sendClauseAssumeClassInitialized(String className)
        throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeClassInitialized invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeClassInitialized invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = null; //sorry, only numeric predicates in this stub
        }

        @Override
        public void sendClauseAssumeClassNotInitialized(String className)
        throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeClassNotInitialized invoked after quit or failure.");
            }
            if (this.hasClause) {
                throw new ExternalProtocolInterfaceException("sendClauseAssumeClassNotInitialized invoked with a current clause already existing.");
            }
            this.hasClause = true;
            this.currentPredicate = null; //sorry, only numeric predicates in this stub
        }

        @Override
        public void retractClause() throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("retractClause invoked after quit or failure.");
            }
            if (!this.hasClause) {
                throw new ExternalProtocolInterfaceException("retractClause invoked with a current clause not existing.");
            }
            this.hasClause = false;
        }

        @Override
        public boolean checkSat(boolean positive) throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("checkSat invoked after quit or failure.");
            }
            return true;
        }

        @Override
        public void pushAssumption(boolean positive) throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("pushAssumption invoked after quit or failure.");
            }
            if (!this.hasClause) {
                throw new ExternalProtocolInterfaceException("pushAssumption invoked with a current clause not existing.");
            }
            try {
                this.clauses.push(new ClauseAssume(positive ? this.currentPredicate : DecisionProcedureExternalTest.this.calc.push(this.currentPredicate).not().pop()));
            } catch (NoSuchElementException | InvalidInputException | InvalidTypeException |
                     InvalidOperandException e) {
                throw new RuntimeException(e);
            }
            this.hasClause = false;
        }

        @Override
        public void clear() throws ExternalProtocolInterfaceException, IOException {
            if (!isWorking()) {
                throw new ExternalProtocolInterfaceException("clear invoked after quit or failure.");
            }
            this.clauses.clear();
            this.hasClause = false;
        }

        @Override
        public void quit() throws ExternalProtocolInterfaceException, IOException {
            this.working = false;
            
        }

        @Override
        public void fail() {
            this.working = false;
        }
        
        
    }

    @Before
    public void setUp() throws InvalidInputException {
        this.calc = new CalculatorRewriting();
        this.calc.addRewriter(new RewriterOperationOnSimplex());
        this.extIf = new DecisionProcedureExternalInterfaceStub();
        this.dec = new DecisionProcedureExternal(this.calc) {
            {
                this.extIf = DecisionProcedureExternalTest.this.extIf;
            }
        };
    }
    
    @Test
    public void test0() throws NoSuchElementException, InvalidInputException, InvalidOperandException, InvalidTypeException, DecisionException {
        final ClauseAssume c0 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(0)).pop());
        final ClauseAssume c1 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(1)).pop());
        final ClauseAssume c2 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(2)).pop());
        this.dec.pushAssumption(c0);
        this.dec.pushAssumption(c1);
        this.dec.pushAssumption(c2);
        
        assertEquals(3, this.extIf.clauses.size());
        assertEquals(c2, this.extIf.clauses.pop());
        assertEquals(c1, this.extIf.clauses.pop());
        assertEquals(c0, this.extIf.clauses.pop());
    }
    
    @Test
    public void test1() throws NoSuchElementException, InvalidInputException, InvalidOperandException, InvalidTypeException, DecisionException {
        final ClauseAssume c0 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(0)).pop());
        final ClauseAssume c1 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(1)).pop());
        final ClauseAssume c2 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(2)).pop());
        final ClauseAssume c3 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(3)).pop());
        final ClauseAssume c4 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(4)).pop());
        this.dec.pushAssumption(c0);
        this.dec.pushAssumption(c1);
        this.dec.pushAssumption(c2);
        this.dec.clearAssumptions();
        this.dec.pushAssumption(c3);
        this.dec.pushAssumption(c4);
        
        assertEquals(2, this.extIf.clauses.size());
        assertEquals(c4, this.extIf.clauses.pop());
        assertEquals(c3, this.extIf.clauses.pop());
    }
    
    @Test
    public void test2() throws NoSuchElementException, InvalidInputException, InvalidOperandException, InvalidTypeException, DecisionException {
        final ClauseAssume c0 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(0)).pop());
        final ClauseAssume c1 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(1)).pop());
        final ClauseAssume c2 = new ClauseAssume(this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(2)).pop());
        this.dec.goFastAndImprecise();
        this.dec.pushAssumption(c0);
        this.dec.pushAssumption(c1);
        this.dec.pushAssumption(c2);
        this.dec.stopFastAndImprecise();
        this.dec.isSat((Expression) c2.getCondition()); //just to force resynch, we don't care of the result
        
        assertEquals(3, this.extIf.clauses.size());
        assertEquals(c2, this.extIf.clauses.pop());
        assertEquals(c1, this.extIf.clauses.pop());
        assertEquals(c0, this.extIf.clauses.pop());
    }
}
