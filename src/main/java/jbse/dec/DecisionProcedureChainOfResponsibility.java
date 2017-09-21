package jbse.dec;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.dec.exc.NoModelException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.ClauseVisitor;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

/**
 * A {@link DecisionProcedure} that delegates the decision of 
 * the formulas outside its theory to its successor in a 
 * Chain Of Responsibility of decision procedures. The default 
 * implementation of its methods yield a decision procedure 
 * over the empty theory, that delegates all decisions to its 
 * successor.
 */
//TODO possibly push up stuff that is not related with the chain of responsibility coordination scheme (rewriting, setting path condition)
public abstract class DecisionProcedureChainOfResponsibility implements DecisionProcedure {
	/** The next {@link DecisionProcedure} in the Chain Of Responsibility */
	protected final DecisionProcedure next;

	/** The {@link CalculatorRewriting}; it is set by the constructor. */
	protected final CalculatorRewriting calc;

	/** 
	 * The {@link Rewriter}s for the (possible) simplification of expressions. 
	 * Not final to allow to set it later than construction (may be necessary 
	 * if the rewriters are created by the decision procedure itself). 
	 */
	protected Rewriter[] rewriters;

	/**
	 * Constructor.
	 * 
	 * @param next The next {@link DecisionProcedure} in the 
	 *        Chain Of Responsibility.
	 * @param calc a {@link CalculatorRewriting}. It must not be {@code null}.
	 * @param rewriters a vararg of {@link Rewriter}s for the 
	 *        (possible) simplification of expressions.
	 */
	protected DecisionProcedureChainOfResponsibility(DecisionProcedure next, CalculatorRewriting calc, Rewriter... rewriters) {
		this.next = next;
		this.calc = calc;
		this.rewriters = rewriters;
	}

	/**
	 * Constructor (no next procedure in the Chain Of Responsibility).
	 * 
	 * @param calc a {@link CalculatorRewriting}. It must not be {@code null}.
	 * @param rewriters a vararg of {@link Rewriter}s for the 
	 *        (possible) simplification of expressions.
	 */
	protected DecisionProcedureChainOfResponsibility(CalculatorRewriting calc, Rewriter... rewriters) {
		this(null, calc, rewriters);
	}

	/**
	 * Checks whether there is a next {@code DecisionProcedure}
	 * in the Chain Of Responsibility.
	 * 
	 * @return a {@code boolean}.
	 */
	private final boolean hasNext() {
		return (this.next != null);
	}

	@Override
	public final void goFastAndImprecise() { 
		goFastAndImpreciseLocal();
		if (hasNext()) {
			this.next.goFastAndImprecise();
		}
	}

	/**
	 * Must be overridden by subclasses that 
	 * offer the "fast and imprecise" mode feature
	 * described in {@link #goFastAndImprecise()}.
	 * The default implementation does nothing.
	 */
	protected void goFastAndImpreciseLocal() {
		//default implementation
	}

	@Override
	public final void stopFastAndImprecise() {
		stopFastAndImpreciseLocal();
		if (hasNext()) {
			this.next.stopFastAndImprecise();
		}
	}

	/**
	 * Must be overridden by subclasses that 
	 * offer the "fast and imprecise" mode feature
	 * described in {@link #goFastAndImprecise()}.
	 * The default implementation does nothing.
	 */
	protected void stopFastAndImpreciseLocal() {
		//default implementation
	}

	@Override
	public final void pushAssumption(Clause c) 
	throws InvalidInputException, DecisionException {
		if (c == null) {
			throw new InvalidInputException("pushAssumption invoked with a null parameter.");
		}
		final Clause cSimpl = simplifyLocal(c);
		pushAssumptionLocal(cSimpl);
		if (hasNext()) {
			this.next.pushAssumption(cSimpl);
		}
	}

	/**
	 * Must be overridden by subclasses if they need to locally add an 
	 * assumption. The default implementation redispatches on the
	 * overloaded methods based on the runtime class of {@code cSimpl},
	 * so if you do not need to locally add all types of {@link Clause} 
	 * you may override the relevant overloaded methods instead of this
	 * one. 
	 * 
	 * @param cSimpl the {@link Clause} to be added, 
	 *        see also {@link #pushAssumption(Clause)}.
	 *        It will be locally simplified.
	 * @throws DecisionException see {@link #pushAssumption(Clause)}.
	 */
	protected void pushAssumptionLocal(Clause cSimpl) throws DecisionException {
		try {
			cSimpl.accept(this.redispatcher);
		} catch (DecisionException e) {
			throw e;
		} catch (Exception e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
	}

	/**
	 * Must be overridden by subclasses that need to 
	 * locally add a {@link ClauseAssume}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssume}, 
	 *        see {@link #pushAssumption(Clause)} 
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssume c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be overridden by subclasses to locally add a {@link ClauseAssumeAliases}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssumeAliases}, see {@link #pushAssumption(Clause)}
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssumeAliases c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be implemented by subclasses to locally add a {@link ClauseAssumeExpands}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssumeExpands}, see {@link #pushAssumption(Clause)}
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssumeExpands c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be implemented by subclasses to locally add a {@link ClauseAssumeNull}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssumeNull}, see {@link #pushAssumption(Clause)}
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssumeNull c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be implemented by subclasses to locally add a {@link ClauseAssumeClassInitialized}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssumeClassInitialized}, see {@link #pushAssumption(Clause)}
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssumeClassInitialized c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be implemented by subclasses to locally add a {@link ClauseAssumeClassNotInitialized}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssumeClassNotInitialized}, see {@link #pushAssumption(Clause)}
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssumeClassNotInitialized c) throws DecisionException {
		//default implementation
	}

	/**
	 * Redispatches a call to {@link #pushAssumptionLocal(Clause)} to the
	 * right overloaded methods with same name. It is stateless, so it is
	 * thread-safe to share one instance between all the calls of 
	 * {@link #pushAssumptionLocal(Clause)}.
	 */
	private final ClauseVisitor redispatcher = new ClauseVisitor() {
		@Override
		public void visitClauseAssume(ClauseAssume c) throws DecisionException {
			pushAssumptionLocal(c);
		}

		@Override
		public void visitClauseAssumeAliases(ClauseAssumeAliases c) throws DecisionException { 
			pushAssumptionLocal(c); 
		}

		@Override
		public void visitClauseAssumeExpands(ClauseAssumeExpands c) throws DecisionException { 
			pushAssumptionLocal(c); 
		}

		@Override
		public void visitClauseAssumeNull(ClauseAssumeNull c) throws DecisionException { 
			pushAssumptionLocal(c); 
		}

		@Override
		public void visitClauseAssumeClassInitialized(ClauseAssumeClassInitialized c) throws DecisionException { 
			pushAssumptionLocal(c); 
		}

		@Override
		public void visitClauseAssumeClassNotInitialized(ClauseAssumeClassNotInitialized c) throws DecisionException { 
			pushAssumptionLocal(c); 
		}
	};

	@Override
	public final void clearAssumptions() throws DecisionException {
		clearAssumptionsLocal();
		if (hasNext()) {
			this.next.clearAssumptions();
		}
	}

	/**
	 * Must be implemented by subclasses to locally drop
	 * the current assumptions.
	 * The default implementation does nothing.
	 * 
	 * @throws DecisionException upon failure.
	 */
	protected void clearAssumptionsLocal() throws DecisionException {
		//default implementation
	}

	@Override
	public final void setAssumptions(Collection<Clause> newAssumptions) 
	throws InvalidInputException, DecisionException {
		if (newAssumptions == null) {
			throw new InvalidInputException("setAssumptions invoked with a null parameter.");
		}
		Collection<Clause> currentAssumptions;
		try {
			currentAssumptions = getAssumptionsLocal();
		} catch (DecisionException e) {
			//sorry, no locally stored assumptions
			currentAssumptions = getAssumptions(); //queries the successor (best effort)
		}
		final int common = numCommonAssumptions(currentAssumptions, newAssumptions);
		final int toPop = currentAssumptions.size() - common;
		final int toPush = newAssumptions.size() - common;
		if (canPopAssumptions() && toPop < common) { //TODO toPop < common is a guess! Implement better heuristics
			setAssumptionsLocalConservatively(newAssumptions, toPop, toPush);
		} else {
			setAssumptionsLocalDestructively(newAssumptions);
		}
		if (hasNext()) {
			this.next.setAssumptions(newAssumptions);
		}
	}

	private static int numCommonAssumptions(Collection<Clause> oldAssumptions, Collection<Clause> newAssumptions) {
		final Iterator<Clause> iterOld = oldAssumptions.iterator();
		final Iterator<Clause> iterNew = newAssumptions.iterator();
		int retVal = 0;
		while (iterOld.hasNext() && iterNew.hasNext()) {
			final Clause oldAssumption = iterOld.next();
			final Clause newAssumption = iterNew.next();
			if (!oldAssumption.equals(newAssumption)) {
				break;
			}
			++retVal;
		}
		return retVal;
	}

	/**
	 * Locally pops/pushes just the clauses that differ.
	 * 
	 * @param newAssumptions see {@link #setAssumptions}.
	 * @param toPop see {@link #setAssumptions}.
	 * @param toPush see {@link #setAssumptions}.
	 * @throws DecisionException upon failure.
	 */
	private void 
	setAssumptionsLocalConservatively(Collection<Clause> newAssumptions, int toPop, int toPush)
	throws DecisionException {
		//pops
		for (int i = 1; i <= toPop; ++i) {
			popAssumptionLocal();
		}

		//pushes
		final int common = newAssumptions.size() - toPush;
		int i = 1;
		for (Clause c : newAssumptions) {
			if (i > common) {
				final Clause cSimpl = simplifyLocal(c);
				pushAssumptionLocal(cSimpl);
			}
			++i;
		}
	}

	/**
	 * Clears the current local assumption, and locally pushes 
	 * the whole new assumption.
	 * 
	 * @param newAssumptions see {@link #setAssumptions}.
	 * @throws DecisionException upon failure.
	 */
	private void 
	setAssumptionsLocalDestructively(Collection<Clause> newAssumptions) 
	throws DecisionException {
		clearAssumptionsLocal();
		for (Clause c : newAssumptions) {
			final Clause cSimpl = simplifyLocal(c);
			pushAssumptionLocal(cSimpl);
		}
	}

	/**
	 * Must be overridden by subclasses that implement {@link #popAssumptionLocal()}
	 * to return {@code true}.
	 * 
	 * @return {@code false} in the default implementation.
	 */
	protected boolean canPopAssumptions() {
		//default implementation
		return false;
	}

	/**
	 * Must be overridden by subclasses that want to offer the
	 * ability of locally popping the last clause added to the 
	 * current assumptions. In some cases this feature might increase the
	 * speed of the decision procedure. The default implementation 
	 * doesn't offer this feature and throws {@link DecisionException}.
	 * 
	 * @throws DecisionException if the subclass does not offer
	 *         this feature.
	 */
	protected void popAssumptionLocal() throws DecisionException {
		//default implementation
		throw new DecisionException();
	}

	@Override
	public final Collection<Clause> getAssumptions() throws DecisionException {
		//the farthest element in the chain has
		//the most simplified assumptions
		if (hasNext()) {
			return this.next.getAssumptions();
		}
		return getAssumptionsLocal();
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #getAssumptions()} in the case they store
	 * locally. It will only be invoked when this decision
	 * procedure is the last in the chain of responsibility. 
	 * The default implementation throws a {@link DecisionException}.
	 * 
	 * @return see {@link #getAssumptions()}.
	 * @throws DecisionException upon failure.
	 */
	protected Collection<Clause> getAssumptionsLocal() 
	throws DecisionException {
		throw new DecisionException(NO_ASSUMPTION_ERROR); //TODO throw a better exception
	}

	private final String NO_ASSUMPTION_ERROR, NO_DELEGATE_ERROR; 
	{
		NO_ASSUMPTION_ERROR = "Class " + getClass().getName() + " does not store assumptions and is the last in the Chain of Responsibility.";
		NO_DELEGATE_ERROR = "Class " + getClass().getName() + " cannot delegate to successor in Chain Of Responsibility because it is the last one.";
	}

	@Override
	public final boolean isSat(ClassHierarchy hier, Expression expression) 
	throws InvalidInputException, DecisionException {
		if (hier == null || expression == null) {
			throw new InvalidInputException("isSat invoked with a null parameter.");
		}
		if (expression.getType() != Type.BOOLEAN) {
			throw new DecisionException("isSat expression has type " + expression.getType());
		}
		final Primitive expSimpl = simplifyLocal(expression);
		if (expSimpl instanceof Simplex) {
			return ((Simplex) expSimpl).surelyTrue();
		} else if (expSimpl instanceof Expression) {
			final boolean localDecidesSat = isSatLocal(hier, expression, (Expression) expSimpl);
			if (localDecidesSat) {
				return delegateIsSat(hier, expression);  //TODO shouldn't we pass expSimpl instead? do we really need to pass the original exp to the next in chain?
			}
			return false; //surely unsat
		}
		throw new DecisionException("the simplified " + expSimpl + " is neither a Simplex nor an Expression"); //TODO throw a better exception
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSat(ClassHierarchy, Expression)}. 
	 * The default implementation 
	 * answers {@code true} (no local decision) so the
	 * answer is generated by the next decision procedure in 
	 * the chain. 
	 *  
	 * @param hier see {@link #isSat(ClassHierarchy, Expression) isSat}.
	 * @param exp see {@link #isSat(ClassHierarchy, Expression) isSat}. 
	 *        Note that it is <em>not</em> locally simplified, and that 
	 *        it remains an {@link Expression} after local simplification.
	 * @param expSimpl {@code exp} after local simplification.
	 * @return see {@link #isSat(ClassHierarchy, Expression) isSat}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatLocal(ClassHierarchy hier, Expression exp, Expression expSimpl) throws DecisionException {
		return true;
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of an {@link Expression}.
	 *  
	 * @param hier see {@link #isSat(ClassHierarchy, Expression) isSat}.
	 * @param exp see {@link #isSat(ClassHierarchy, Expression) isSat}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSat(ClassHierarchy, Expression) isSat}{@code (hier, exp)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSat(ClassHierarchy hier, Expression exp) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSat(hier, exp);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws InvalidInputException, DecisionException {
		if (hier == null || r == null) {
			throw new InvalidInputException("isSatNull invoked with a null parameter.");
		}
		final boolean localDecidesSat = isSatNullLocal(hier, r);
		if (localDecidesSat) {
			return delegateIsSatNull(hier, r);
		}
		return false; //surely unsat
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSatNull(ClassHierarchy, ReferenceSymbolic)}. 
	 * The default implementation 
	 * answers {@code true} (no local decision) so the
	 * answer is generated by the next decision procedure in 
	 * the chain.
	 * 
	 * @param hier see {@link #isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}.
	 * @param r see {@link #isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}.
	 * @return see {@link #isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatNullLocal(ClassHierarchy hier, ReferenceSymbolic r) throws DecisionException {
		return true;
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of a resolution by null.
	 *  
	 * @param hier see {@link #isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}.
	 * @param r see {@link #isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSatNull(ClassHierarchy, ReferenceSymbolic) isSatNull}{@code (hier, r)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSatNull(ClassHierarchy hier, ReferenceSymbolic r) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSatNull(hier, r);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o) 
	throws InvalidInputException, DecisionException {
		if (hier == null || r == null || o == null) {
			throw new InvalidInputException("isSatAliases invoked with a null parameter.");
		}
		final boolean localDecidesSat = isSatAliasesLocal(hier, r, heapPos, o);
		if (localDecidesSat) {
			return delegateIsSatAliases(hier, r, heapPos, o);
		}
		return false; //surely unsat
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt)}. 
	 * The default implementation answers {@code true} 
	 * (no local decision) so the answer is generated by 
	 * the next decision procedure in the chain.
	 *  
	 * @param hier see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param r see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param heapPos see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param o see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @return see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatAliasesLocal(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o) 
	throws DecisionException {
		return true;
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of a resolution by aliasing.
	 *  
	 * @param hier see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param r see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param heapPos see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @param o see {@link #isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSatAliases(ClassHierarchy, ReferenceSymbolic, long, Objekt) isSatAliases}{@code (hier, r, heapPos, o)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSatAliases(hier, r, heapPos, o);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className) 
	throws InvalidInputException, DecisionException {
		if (hier == null || r == null || className == null) {
			throw new InvalidInputException("isSatExpands invoked with a null parameter.");
		}
		final boolean localDecidesSat = isSatExpandsLocal(hier, r, className);
		if (localDecidesSat) {
			return delegateIsSatExpands(hier, r, className);
		}
		return false; //surely unsat
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String)}. 
	 * The default implementation answers {@code true} 
	 * (no local decision) so the answer is generated by 
	 * the next decision procedure in the chain. 
	 *  
	 * @param hier see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @param r see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @param className see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @return see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatExpandsLocal(ClassHierarchy hier, ReferenceSymbolic r, String className) 
	throws DecisionException {
		return true;
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of a resolution by expansion.
	 *  
	 * @param hier see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @param r see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @param className see {@link #isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSatExpands(ClassHierarchy, ReferenceSymbolic, String) isSatExpands}{@code (hier, r, className)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSatExpands(hier, r, className);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final boolean isSatInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		if (hier == null || className == null) {
			throw new InvalidInputException("isSatInitialized invoked with a null parameter.");
		}
		final boolean localDecidesSat = isSatInitializedLocal(hier, className);
		if (localDecidesSat) {
			return delegateIsSatInitialized(hier, className);
		}
		return false; //surely unsat
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSatInitialized(ClassHierarchy, String)}. 
	 * The default implementation answers {@code true} 
	 * (no local decision) so the answer is generated 
	 * by the next decision procedure in 
	 * the chain.
	 *  
	 * @param hier see {@link #isSatInitialized(ClassHierarchy, String) isSatInitialized}.
	 * @param className see {@link #isSatInitialized(ClassHierarchy, String) isSatInitialized}.
	 * @return see {@link #isSatInitialized(ClassHierarchy, String) isSatInitialized}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatInitializedLocal(ClassHierarchy hier, String className) 
	throws DecisionException {
		return true;
	}


	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of the assumption that a class is
	 * initialized when symbolic execution starts.
	 *  
	 * @param hier see {@link #isSatInitialized(ClassHierarchy, String) isSatInitialized}.
	 * @param className see {@link #isSatInitialized(ClassHierarchy, String) isSatInitialized}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSatInitialized(ClassHierarchy, String) isSatInitialized}{@code (hier, className)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSatInitialized(ClassHierarchy hier, String className) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSatInitialized(hier, className);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final boolean isSatNotInitialized(ClassHierarchy hier, String className) 
	throws InvalidInputException, DecisionException {
		if (hier == null || className == null) {
			throw new InvalidInputException("isSatNotInitialized invoked with a null parameter.");
		}
		final boolean localDecidesSat = isSatNotInitializedLocal(hier, className);
		if (localDecidesSat) {
			return delegateIsSatNotInitialized(hier, className);
		}
		return false; //surely unsat
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #isSatNotInitialized(ClassHierarchy, String)}. 
	 * The default implementation answers {@code true} 
	 * (no local decision) so the answer is generated by 
	 * the next decision procedure in the chain. 
	 *  
	 * @param hier see {@link #isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}.
	 * @param className see {@link #isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}.
	 * @return see {@link #isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}.
	 * @throws DecisionException upon failure.
	 */
	protected boolean isSatNotInitializedLocal(ClassHierarchy hier, String className) 
	throws DecisionException {
		return true;
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * satisfiability of the assumption that a class is
	 * not initialized when symbolic execution starts.
	 *  
	 * @param hier see {@link #isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}.
	 * @param className see {@link #isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}.
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#isSatNotInitialized(ClassHierarchy, String) isSatNotInitialized}{@code (hier, className)}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final boolean delegateIsSatNotInitialized(ClassHierarchy hier, String className) 
	throws DecisionException {
		if (hasNext()) {
			try {
				return this.next.isSatNotInitialized(hier, className);
			} catch (InvalidInputException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public Map<PrimitiveSymbolic, Simplex> getModel() 
	throws DecisionException {
		try {
			return getModelLocal();
		} catch (NoModelException e) {
			return delegateGetModel();
		}
	}

	/**
	 * Must be overridden by subclasses to implement 
	 * {@link #getModel()}. 
	 * The default implementation throws {@link NoModelException} 
	 * (no local decision) so the answer is generated by 
	 * the next decision procedure in the chain. 
	 * 
	 * @return see {@link #getModel() getModel}.
	 * @throws DecisionException upon failure.
	 */
	protected Map<PrimitiveSymbolic, Simplex> getModelLocal() 
	throws DecisionException {
		throw new NoModelException();
	}

	/**
	 * Queries the next decision procedure in the chain for 
	 * a model.
	 * 
	 * @return the result of invoking 
	 *         {@link DecisionProcedure#getModel()}
	 *         on the next decision procedure in the chain.
	 * @throws DecisionException if this decision procedure has
	 *         not a successor in the chain.
	 */
	private final Map<PrimitiveSymbolic, Simplex> delegateGetModel() 
	throws DecisionException {
		if (hasNext()) {
			return this.next.getModel();
		}
		throw new DecisionException(NO_DELEGATE_ERROR);
	}

	@Override
	public final Primitive simplify(Primitive p) {
		final Primitive pSimpl = simplifyLocal(p);
		if (this.next == null) {
			return pSimpl;
		} else {
			return this.next.simplify(pSimpl);
		}
	}

	/**
	 * Simplifies a {@link Clause} under the current assumption.
	 * 
	 * @param c a {@link Clause}.
	 * @return a {@link Clause} equivalent to {@code c}
	 *         under the current assumption (possibly {@code c} itself).
	 */
	private final Clause simplifyLocal(Clause c) {
		if (c instanceof ClauseAssume) {
			final Primitive p = ((ClauseAssume) c).getCondition();
			final Primitive pSimpl = simplifyLocal(p);
			return new ClauseAssume(pSimpl);
		} else {
			return c;
		}
	}

	/**
	 * Applies all the {@link Rewriter}s of this decision procedure
	 * to a {@link Primitive}.
	 * 
	 * @param p The {@link Primitive}.
	 * @return Another {@link Primitive} equivalent to {@code p} 
	 * (possibly {@code p} itself).
	 */
	protected final Primitive simplifyLocal(Primitive p) {
		return this.calc.applyRewriters(p, this.rewriters);
	}

	@Override
	public final void close() throws DecisionException {
		closeLocal();
		if (hasNext()) {
			this.next.close();
		}
	}

	/**
	 * May be implemented by subclasses to locally quit a decision 
	 * procedure. By default it does nothing.
	 */
	protected void closeLocal() throws DecisionException {
		//default implementation
	}
}
