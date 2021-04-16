package jbse.dec;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.Objekt;
import jbse.mem.exc.ContradictionException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Rewriter;
import jbse.val.Simplex;

/**
 * A {@link DecisionProcedureExternal} is a {@link DecisionProcedureChainOfResponsibility} 
 * implemented as a Mediator to a {@link DecisionProcedureExternalInterface} which effectively 
 * does the work. Concrete subclasses must inject the dependency to a 
 * {@link DecisionProcedureExternalInterface}, usually by implementing a constructor which sets it.
 * It assumes that the external decision procedure is <emph>partial</emph> but <emph>safe</emph>, 
 * i.e., that when it answer that a predicate is unsat it is surely unsat, but when it answer that 
 * a predicate is sat it may mean that it actually is sat, but it might as well mean that the 
 * decision procedure was unable to draw a conclusion. Correspondingly, whenever the external 
 * interface returns unsat as an answer, this decision procedure returns unsat, but when the
 * external interface returns sat, this decision procedures delegates the query to the next 
 * in the chain.
 * 
 * @author Pietro Braione
 */
public abstract class DecisionProcedureExternal extends DecisionProcedureChainOfResponsibility {
    private final String NOT_WORKING = "Method invoked after the failure of the external decision procedure " + this.getClass().getName() + ".";

    /** The interface to the external decision procedure; it is set by subclasses. */
    protected DecisionProcedureExternalInterface extIf;

    /** Caches the current assumptions sent (or to be sent) to the external decision procedure. */
    protected final ArrayDeque<Clause> clauses;

    /** 
     * true iff we want to go fast by exploiting unchecked assumption pushing
     * of the decision procedure of choice. 
     */
    private boolean fast = false;

    /** true iff the external decision procedure has not yet received the current assumption. */
    private boolean notInSynch = false;

    protected DecisionProcedureExternal(DecisionProcedure next, Rewriter... rewriters) 
    throws InvalidInputException {
        super(next, rewriters);
        this.clauses = new ArrayDeque<>();
    }

    protected DecisionProcedureExternal(Calculator calc, Rewriter... rewriters) 
    throws InvalidInputException {
        super(calc, rewriters);
        this.clauses = new ArrayDeque<>();
    }

    /**
     * Resynchs the external decision procedure with this.bs.
     * 
     * @throws DecisionException
     * @throws ContradictionException 
     */
    private void resynch() throws DecisionException, ContradictionException {
        try {
            this.extIf.clear();
            final Iterable<Clause> i = () -> this.clauses.descendingIterator();
            for (Clause c : i) {
                super.pushAssumptionLocal(c); //redispatches
            }
            this.notInSynch = false;
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void goFastAndImpreciseLocal() {
        this.fast = true;
    }

    @Override
    protected final void stopFastAndImpreciseLocal() {
        this.fast = false;
    }

    @Override
    protected final void pushAssumptionLocal(Clause cSimpl) 
    throws DecisionException, ContradictionException {
        this.clauses.push(cSimpl);
        if (this.fast) {
            this.notInSynch = true;
        } else if (this.extIf.isWorking()) {
        	if (this.notInSynch) {
        		resynch();
        	}
        	super.pushAssumptionLocal(cSimpl); //redispatches
        } else {
        	throw new DecisionException(NOT_WORKING);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssume cSimpl)
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssume(cSimpl.getCondition());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssumeAliases cSimpl)
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssumeAliases(cSimpl.getReference(), cSimpl.getHeapPosition(), cSimpl.getObjekt());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssumeExpands cSimpl)
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssumeExpands(cSimpl.getReference(), cSimpl.getObjekt().getType().getClassName());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssumeNull cSimpl) 
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssumeNull(cSimpl.getReference());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssumeClassInitialized cSimpl)
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssumeClassInitialized(cSimpl.getClassFile().getClassName());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void pushAssumptionLocal(ClauseAssumeClassNotInitialized cSimpl) 
    throws DecisionException { 
        try {
            this.extIf.sendClauseAssumeClassNotInitialized(cSimpl.getClassFile().getClassName());
            this.extIf.pushAssumption(true);
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void clearAssumptionsLocal() 
    throws DecisionException {
        this.clauses.clear();
        if (this.fast) {
            this.notInSynch = true;
        } else {
            try {
                if (this.extIf.isWorking()) {
                    //"lightweight" resynch
                    this.extIf.clear();
                    this.notInSynch = false; 
                } else {
                    throw new DecisionException(NOT_WORKING);
                }
            } catch (ExternalProtocolInterfaceException | IOException e) {
                throw new DecisionException(e);
            }
        }
    }

    @Override
    protected final void popAssumptionLocal() 
    throws DecisionException {
        this.clauses.pop();
        if (this.fast) {
            this.notInSynch = true;
        } else {
            try {
                if (this.extIf.isWorking()) {
                    this.extIf.popAssumption();
                } else {
                    throw new DecisionException(NOT_WORKING);					
                }
            } catch (ExternalProtocolInterfaceException | IOException e) {
                throw new DecisionException(e);
            }
        }
    }

    @Override
    protected final boolean isSatLocal(Expression exp, Expression expSimpl) 
    throws DecisionException {
        try {
            if (this.extIf.isWorking()) {
                if (this.notInSynch) {
                    resynch();
                }
                this.extIf.sendClauseAssume(expSimpl);
                final boolean retVal = this.extIf.checkSat(true); 
                this.extIf.retractClause();
                return retVal;
            } else {
                throw new DecisionException(NOT_WORKING);
            }
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        } catch (ContradictionException e) {
        	return false;
        }
    }

    @Override
    protected final boolean isSatAliasesLocal(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
        try {
            if (this.extIf.isWorking()) {
                if (this.notInSynch) {
                    resynch();
                }
                this.extIf.sendClauseAssumeAliases(r, heapPos, o);
                final boolean retVal = this.extIf.checkSat(true); 
                this.extIf.retractClause();
                return retVal;
            } else {
                throw new DecisionException(NOT_WORKING);
            }
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        } catch (ContradictionException e) {
        	return false;
        }
    }

    @Override
    protected final boolean isSatExpandsLocal(ReferenceSymbolic r, ClassFile classFile)
    throws DecisionException {
        try {
            if (this.extIf.isWorking()) {
                if (this.notInSynch) {
                    resynch();
                }
                this.extIf.sendClauseAssumeExpands(r, classFile.getClassName());
                final boolean retVal = this.extIf.checkSat(true); 
                this.extIf.retractClause();
                return retVal;
            } else {
                throw new DecisionException(NOT_WORKING);
            }
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        } catch (ContradictionException e) {
        	return false;
        }
    }

    @Override
    protected final boolean isSatNullLocal(ReferenceSymbolic r)
    throws DecisionException {
        try {
            if (this.extIf.isWorking()) {
                if (this.notInSynch) {
                    resynch();
                }
                this.extIf.sendClauseAssumeNull(r);
                final boolean retVal = this.extIf.checkSat(true); 
                this.extIf.retractClause();
                return retVal;
            } else {
                throw new DecisionException(NOT_WORKING);
            }
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        } catch (ContradictionException e) {
        	return false;
        }
    }

    @Override
    protected Map<PrimitiveSymbolic, Simplex> getModelLocal()
    throws DecisionException {
        try {
            return this.extIf.getModel();
        } catch (ExternalProtocolInterfaceException | IOException e) {
            throw new DecisionException(e);
        }
    }

    @Override
    protected final void closeLocal() throws DecisionException {
        if (this.extIf.isWorking()) {
            try {
                this.extIf.quit();
            } catch (ExternalProtocolInterfaceException | IOException e) {
                this.extIf.fail();
                throw new DecisionException(e);
            }
        } else {
            throw new DecisionException(NOT_WORKING);
        }
    }
}