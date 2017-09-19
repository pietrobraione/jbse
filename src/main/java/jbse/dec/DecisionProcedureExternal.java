package jbse.dec;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;

import jbse.bc.ClassHierarchy;
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
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
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
	
	protected DecisionProcedureExternal(DecisionProcedure next, CalculatorRewriting calc, Rewriter... rewriters) {
		super(next, calc, rewriters);
		this.clauses = new ArrayDeque<>();
	}
	
	/**
	 * Resynchs the external decision procedure with this.bs.
	 * 
	 * @throws DecisionException
	 */
	private void resynch() throws DecisionException {
	    try {
	        this.extIf.clear();
	        final Iterable<Clause> i = () -> clauses.descendingIterator();
	        for (Clause c : i) {
	            super.pushAssumptionLocal(c);
	            this.extIf.pushAssumption(true);
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
	throws DecisionException {
		this.clauses.push(cSimpl);
		if (this.fast) {
			this.notInSynch = true;
		} else {
			try {
				if (this.extIf.isWorking()) {
					if (this.notInSynch) {
						resynch();
					}
					super.pushAssumptionLocal(cSimpl); //redispatches
					this.extIf.pushAssumption(true);
				} else {
					throw new DecisionException(NOT_WORKING);
				}
			} catch (ExternalProtocolInterfaceException | IOException e) {
				throw new DecisionException(e);
			}
		}
	}
    
	@Override
	protected final void pushAssumptionLocal(ClauseAssume cSimpl)
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssume(cSimpl.getCondition());
	    } catch (ExternalProtocolInterfaceException | IOException e) {
	        throw new DecisionException(e);
	    }
	}

	@Override
	protected final void pushAssumptionLocal(ClauseAssumeAliases cSimpl)
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssumeAliases(cSimpl.getReference(), cSimpl.getHeapPosition(), cSimpl.getObjekt());
	    } catch (ExternalProtocolInterfaceException | IOException e) {
	        throw new DecisionException(e);
	    }
	}

	@Override
	protected final void pushAssumptionLocal(ClauseAssumeExpands cSimpl)
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssumeExpands(cSimpl.getReference(), cSimpl.getObjekt().getType());
	    } catch (ExternalProtocolInterfaceException | IOException e) {
	        throw new DecisionException(e);
	    }
	}

	@Override
	protected final void pushAssumptionLocal(ClauseAssumeNull cSimpl) 
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssumeNull(cSimpl.getReference());
	    } catch (ExternalProtocolInterfaceException | IOException e) {
	        throw new DecisionException(e);
	    }
	}

	@Override
	protected final void pushAssumptionLocal(ClauseAssumeClassInitialized cSimpl)
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssumeClassInitialized(cSimpl.getClassName());
	    } catch (ExternalProtocolInterfaceException | IOException e) {
	        throw new DecisionException(e);
	    }
	}

	@Override
	protected final void pushAssumptionLocal(ClauseAssumeClassNotInitialized cSimpl) 
	throws DecisionException { 
	    try {
	        this.extIf.sendClauseAssumeClassNotInitialized(cSimpl.getClassName());
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
	protected final boolean isSatLocal(ClassHierarchy hier, Expression exp, Expression expSimpl) 
	throws DecisionException {
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssume(expSimpl);
	        	final boolean retVal = this.extIf.checkSat(hier, true); 
	        	this.extIf.retractClause();
	            return retVal;
	        } else {
	        	throw new DecisionException(NOT_WORKING);
	        }
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected final boolean isSatAliasesLocal(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o) 
	throws DecisionException {
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeAliases(r, heapPos, o);
	        	final boolean retVal = this.extIf.checkSat(hier, true); 
	        	this.extIf.retractClause();
	            return retVal;
	        } else {
	        	throw new DecisionException(NOT_WORKING);
	        }
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected final boolean isSatExpandsLocal(ClassHierarchy hier, ReferenceSymbolic r, String className)
	throws DecisionException {
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeExpands(r, className);
	        	final boolean retVal = this.extIf.checkSat(hier, true); 
	        	this.extIf.retractClause();
	            return retVal;
	        } else {
	        	throw new DecisionException(NOT_WORKING);
	        }
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected final boolean isSatNullLocal(ClassHierarchy hier, ReferenceSymbolic r)
	throws DecisionException {
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeNull(r);
	        	final boolean retVal = this.extIf.checkSat(hier, true); 
	        	this.extIf.retractClause();
	            return retVal;
	        } else {
	        	throw new DecisionException(NOT_WORKING);
	        }
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
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