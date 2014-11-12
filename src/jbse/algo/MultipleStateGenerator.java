package jbse.algo;

import java.util.SortedSet;

import jbse.dec.DecisionProcedureAlgorithms.Outcome;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.dec.DecisionEmptyException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.InvalidInputException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.State;
import jbse.tree.DecisionAlternative;


/**
 * Class for completing the semantics of all the bytecodes that require 
 * decision over symbolic values, thus the possible generation of many 
 * successor symbolic states.
 * 
 * @author Pietro Braione
 *
 * @param <R> the {@link DecisionAlternative}s used to encode the results 
 *            of deciding over symbolic values.
 */
public abstract class MultipleStateGenerator<R extends DecisionAlternative> {
	/** Reifies {@code R}. */
	private final Class<R> superclassDecisionAlternatives;

	/** 
	 * Constructor.
	 * 
	 * @param superclassDecisionAlternatives the {@link Class}{@code <R>}
	 *        that reifies {@code R}.
	 */
	protected MultipleStateGenerator(Class<R> superclassDecisionAlternatives) {
		this.superclassDecisionAlternatives = superclassDecisionAlternatives;
	}
	
	//Must be set by subclasses (inputs to generateStates())
	
	/** The {@link DecisionStrategy} used to decide which states must be generated. */ 
	protected DecisionStrategy<R> ds; 
	
	/** The {@link StateRefinementStrategy} used to refine each generated state. */
	protected StateRefinementStrategy<R> srs;
	
	/** 
	 * The {@link StateUpdateStrategy} used to complete the bytecode semantics 
	 * of the generated states.
	 */
	protected StateUpdateStrategy<R> sus;
	
	/** Must be set by subclasses to the current {@link State}. */
	protected State state;
	
	/** Must be set by subclasses to the current {@link ExecutionContext}. */
	protected ExecutionContext ctx;
	
	/**
	 * Completes the bytecode semantics of a state by performing the following 
	 * actions:
	 * 
	 * <ul>
	 * <li>Invokes a decision procedures and gathers its satisfiable results;</li>
	 * <li>Creates one state for each result;</li>
	 * <li>Performs on each state a refinement action, if it is the case;</li>
	 * <li>Completes the bytecode semantics on each state;</li>
	 * <li>Finally, adds all the obtained states to the state tree, creating 
	 * a backtrack point if it is the case.</li>
	 * </ul>
	 * @throws ClassFileNotFoundException
	 * @throws DecisionException
	 * @throws ContradictionException 
	 * @throws InvalidInputException
	 * @throws InvalidTypeException
	 * @throws ThreadStackEmptyException  
	 */
	protected void generateStates() 
	throws ClassFileNotFoundException, 
	DecisionException, ContradictionException, 
	InvalidInputException, InvalidTypeException,
	ThreadStackEmptyException {
		//decides the satisfiability of the different alternatives
		final SortedSet<R> decisionResults = this.ctx.mkDecisionResultSet(this.superclassDecisionAlternatives);		
		final Outcome outcome = this.ds.decide(decisionResults);
		final boolean shouldRefine = outcome.shouldRefine();
		final boolean branchingDecision = outcome.branchingDecision();

		//checks if at least one alternative is satisfiable
		final int tot = decisionResults.size();
		if (tot == 0) {
			throw new DecisionEmptyException();
		}

		//generates the next states
		final boolean mustAddBranch = this.ctx.stateTree.possiblyAddBranch(decisionResults);
		int cur = 1;
		for (R r : decisionResults) {
			final State s = (cur < tot ? this.state.clone() : this.state);

			//possibly refines the state
			if (shouldRefine) {
				this.srs.refine(s, r);
			}
			
			//completes the bytecode semantics
			this.sus.update(s, r);

			//has the state been produced by a branching decision?
			s.setBranchingDecision(branchingDecision);
			
	        //adds the created state to the tree, if on a new branch
			if (mustAddBranch) {
				this.ctx.stateTree.addState(s, r.getBranchNumber(), r.getIdentifier());
			}
				
			++cur;
		}
	}
}
