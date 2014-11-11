package jbse.dec;

import java.io.IOException;
import java.util.ArrayDeque;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.ExternalProtocolInterfaceException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.Expression;
import jbse.mem.Objekt;
import jbse.mem.Primitive;
import jbse.mem.ReferenceSymbolic;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;

/**
 * A {@link DecisionProcedureExternal} is a {@link DecisionProcedureChainOfResponsibility} 
 * implemented as a Mediator to a {@link DecisionProcedureExternalInterface} which effectively 
 * does the work. Concrete subclasses must inject the dependency to a 
 * {@link DecisionProcedureExternalInterface}, usually by implementing a constructor which sets it.
 * 
 * @author Pietro Braione
 */
//TODO now this class only manages numeric clauses; extend to all kinds of clauses
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
	protected boolean notInSynch = false;
	
	protected DecisionProcedureExternal(DecisionProcedure next, CalculatorRewriting calc, Rewriter... rewriters) {
		super(next, calc, rewriters);
		this.clauses = new ArrayDeque<Clause>();
	}
	
	/**
	 * Resynchs the external decision procedure with this.bs.
	 * 
	 * @throws ExternalProtocolInterfaceException
	 * @throws IOException
	 * @throws UnexpectedInternalException 
	 */
	private void resynch() 
	throws ExternalProtocolInterfaceException, IOException, UnexpectedInternalException {
		this.extIf.clear();
		final Iterable<Clause> i = () -> clauses.descendingIterator();
		for (Clause c : i) {
			sendClause(c);
			this.extIf.pushAssumption(true);
		}
		this.notInSynch = false;
	}
	
	protected final void sendClause(Clause c) 
	throws ExternalProtocolInterfaceException, IOException, UnexpectedInternalException {
		//TODO use dispatcher
		if (c instanceof ClauseAssume) {
			this.extIf.sendClauseAssume(((ClauseAssume) c).getCondition());
		} else if (c instanceof ClauseAssumeAliases) {
			final ClauseAssumeAliases cAliases = (ClauseAssumeAliases) c;
			this.extIf.sendClauseAssumeAliases(cAliases.getReference(), cAliases.getHeapPosition(), cAliases.getObjekt());
		} else if (c instanceof ClauseAssumeExpands) {
			final ClauseAssumeExpands cExpands = (ClauseAssumeExpands) c;
			this.extIf.sendClauseAssumeExpands(cExpands.getReference(), cExpands.getObjekt().getType());
		} else if (c instanceof ClauseAssumeNull) {
			final ClauseAssumeNull cNull = (ClauseAssumeNull) c;
			this.extIf.sendClauseAssumeNull(cNull.getReference());
		} else if (c instanceof ClauseAssumeClassInitialized) {
			final ClauseAssumeClassInitialized cIni = (ClauseAssumeClassInitialized) c;
			this.extIf.sendClauseAssumeClassInitialized(cIni.getClassName());
		} else { //c instanceof ClauseAssumeClassNotInitialized
			final ClauseAssumeClassNotInitialized cNIni = (ClauseAssumeClassNotInitialized) c;
			this.extIf.sendClauseAssumeClassNotInitialized(cNIni.getClassName());
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
	protected final void pushAssumptionLocal(Clause c) 
	throws DecisionException, UnexpectedInternalException {
		this.clauses.push(c);
		if (this.fast) {
			this.notInSynch = true;
		} else {
			try {
				if (this.extIf.isWorking()) {
					if (this.notInSynch) {
						resynch();
					}
					sendClause(c);
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
	protected final void clearAssumptionsLocal() 
	throws DecisionException, UnexpectedInternalException {
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
	protected void popAssumptionLocal() 
	throws DecisionException, UnexpectedInternalException {
		final Clause c = this.clauses.pop();
		if (this.fast) {
			this.notInSynch = true;
		} else {
			try {
				if (this.extIf.isWorking()) {
					if (c instanceof ClauseAssume) {
						this.extIf.popAssumption();
					}
				} else {
					throw new DecisionException(NOT_WORKING);					
				}
			} catch (ExternalProtocolInterfaceException | IOException e) {
				throw new DecisionException(e);
			}
		}
	}

	@Override
	protected boolean isSatImpl(Expression exp) 
	throws DecisionException, UnexpectedInternalException {
		final Primitive p = this.calc.applyRewriters(exp, this.rewriters);
		boolean retVal;
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssume(p);
	        	retVal = this.extIf.checkSat(true); 
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
	protected boolean isSatAliasesImpl(ReferenceSymbolic r, long heapPos, Objekt o) 
	throws DecisionException, UnexpectedInternalException {
		boolean retVal;
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeAliases(r, heapPos, o);
	        	retVal = this.extIf.checkSat(true); 
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
	protected boolean isSatExpandsImpl(ReferenceSymbolic r, String className)
	throws DecisionException, UnexpectedInternalException {
		boolean retVal;
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeExpands(r, className);
	        	retVal = this.extIf.checkSat(true); 
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
	protected boolean isSatNullImpl(ReferenceSymbolic r)
	throws DecisionException, UnexpectedInternalException {
		boolean retVal;
	    try {
	        if (this.extIf.isWorking()) {
	        	if (this.notInSynch) {
	        		resynch();
	        	}
	        	this.extIf.sendClauseAssumeNull(r);
	        	retVal = this.extIf.checkSat(true); 
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
	public void close() 
	throws DecisionException, UnexpectedInternalException {
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