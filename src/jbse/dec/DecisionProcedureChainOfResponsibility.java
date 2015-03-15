package jbse.dec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
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
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

/**
 * A {@link DecisionProcedure} that delegates the operations it 
 * is unable to do to another one, as a Chain Of Responsibility.
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
	protected final boolean hasNext() {
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
	 * Must be implemented by subclasses that 
	 * offer the "fast and imprecise" mode feature
	 * described in {@link #goFastAndImprecise()}.
	 * In the default implementation it does nothing.
	 */
	protected void goFastAndImpreciseLocal() {
		//default implementation
	}

	@Override
	public void stopFastAndImprecise() {
		stopFastAndImpreciseLocal();
		if (hasNext()) {
			this.next.stopFastAndImprecise();
		}
	}
	
	/**
	 * Must be implemented by subclasses that 
	 * offer the "fast and imprecise" mode feature
	 * described in {@link #goFastAndImprecise()}.
	 * In the default implementation it does nothing.
	 */
	protected void stopFastAndImpreciseLocal() {
		//default implementation
	}
	
	@Override
	public final void pushAssumption(Clause c) throws DecisionException {
        final Clause cSimpl = simplifyLocal(c);
		pushAssumptionLocal(cSimpl);
		if (hasNext()) {
			this.next.pushAssumption(cSimpl);
		}
	}

	/**
	 * Must be invoked by subclasses if they need to locally add an 
	 * assumption. The default implementation redispatches on the
	 * overloaded methods for the specific subclasses of {@link Clause}. 
	 * 
	 * @param cSimpl the {@link Clause} to be added after local simplification, 
	 *        see also {@link #pushAssumption(Clause)}. 
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
	 * Must be implemented by subclasses to locally add a {@link ClauseAssume}.
	 * The default implementation does nothing.
	 * 
	 * @param c a {@link ClauseAssume}, see {@link #pushAssumption(Clause)} 
	 *        (after local simplification).
	 * @throws DecisionException see {@link #pushAssumption(Clause)}
	 */
	protected void pushAssumptionLocal(ClauseAssume c) throws DecisionException {
		//default implementation
	}

	/**
	 * Must be implemented by subclasses to locally add a {@link ClauseAssumeAliases}.
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
	public final void clearAssumptions() 
	throws DecisionException {
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
    throws DecisionException {
		final Collection<Clause> currentAssumptions = getAssumptions();
		final int common = numCommonAssumptions(currentAssumptions, newAssumptions);
		final int toPop = currentAssumptions.size() - common;
		final int toPush = newAssumptions.size() - common;
		final Collection<Clause> newAssumptionsSimpl;
    	if (canPopAssumptions() && toPop < common) { //toPop < common is heuristic!
    	    newAssumptionsSimpl = setAssumptionsLocalConservatively(newAssumptions, toPop, toPush);
    	} else {
    	    newAssumptionsSimpl = setAssumptionsLocalDestructively(newAssumptions);
    	}
    	if (hasNext()) {
            this.next.setAssumptions(newAssumptionsSimpl);
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
	 * @return a {@link Collection}{@code <}{@link Clause}{@code >}
	 *         whose members are obtained by locally 
	 *         simplifying the members of {@code newAssumptions}, 
	 *         in the same order.
	 * @throws DecisionException upon failure.
	 */
    private Collection<Clause> 
    setAssumptionsLocalConservatively(Collection<Clause> newAssumptions, int toPop, int toPush)
    throws DecisionException {
    	//pops
    	for (int i = 1; i <= toPop; ++i) {
    		popAssumptionLocal();
    	}

    	//pushes
    	final ArrayList<Clause> retVal = new ArrayList<>();
    	final int common = newAssumptions.size() - toPush;
		int i = 1;
    	for (Clause c : newAssumptions) {
    		if (i > common) {
    		    final Clause cSimpl = simplifyLocal(c);
    			pushAssumptionLocal(cSimpl);
    			retVal.add(cSimpl);
    		}
    		++i;
    	}
    	
    	return retVal;
    }
    
	/**
	 * Clears the current local assumption, and locally pushes 
	 * the whole new assumption.
	 * 
	 * @param newAssumptions see {@link #setAssumptions}.
     * @return a {@link Collection}{@code <}{@link Clause}{@code >}
     *         whose members are obtained by locally 
     *         simplifying the members of {@code newAssumptions}, 
     *         in the same order.
	 * @throws DecisionException upon failure.
	 */
    private Collection<Clause> 
    setAssumptionsLocalDestructively(Collection<Clause> newAssumptions) 
    throws DecisionException {
        final ArrayList<Clause> retVal = new ArrayList<>();
    	clearAssumptionsLocal();
    	for (Clause c : newAssumptions) {
            final Clause cSimpl = simplifyLocal(c);
    		pushAssumptionLocal(cSimpl);
    		retVal.add(cSimpl);
    	}
    	return retVal;
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
    	return getAssumptionsImpl();
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #getAssumptions()}. The default implementation
     * throws a {@link DecisionException}.
     * 
     * @return see {@link #getAssumptions()}.
     * @throws DecisionException upon failure.
     */
    protected Collection<Clause> getAssumptionsImpl() 
    throws DecisionException {
    	throw new DecisionException("bottom of the chain does not store assumptions"); //TODO throw a better exception
    }

    @Override
    public final boolean isSat(Expression exp) throws DecisionException {
    	if (exp.getType() != Type.BOOLEAN) {
    		throw new DecisionException("cannot decide satisfiability of " + exp + ", it is not boolean"); //TODO throw a better exception
    	}
    	final Primitive expSimpl = simplifyLocal(exp);
		if (expSimpl instanceof Simplex) {
			return ((Simplex) expSimpl).surelyTrue();
		} else if (expSimpl instanceof Expression) {
			return isSatImpl(exp, (Expression) expSimpl);
		}
		throw new DecisionException("simplified " + expSimpl + " is neither Simplex nor Expression"); //TODO throw a better exception
    }
    
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSat(Expression)}. The default implementation 
     * invokes {@link #delegateIsSat}{@code (exp)}.
     *  
     * @param exp see {@link #isSat(Expression)}. It is <em>not</em> 
     *        locally simplified, but after local simplification it must 
     *        remain an {@link Expression}.
     * @param expSimpl {@code exp} after local simplification.
     * @return see {@link #isSat(Expression)}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatImpl(Expression exp, Expression expSimpl) throws DecisionException {
    	return delegateIsSat(exp);
    }
    

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of an {@link Expression}.
     *  
     * @param exp the {@link Expression}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSat(Expression) isSat}{@code (exp)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if this decision procedure has
     *         not a successor in the chain.
     */
    protected final boolean delegateIsSat(Expression exp) throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSat(exp);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " is trying to delegate isSat but has no next decision procedure in Chain Of Responsibility");
    }

    @Override
    public final boolean isSatNull(ReferenceSymbolic r) throws DecisionException {
    	//TODO check input parameters; simplify?
		return isSatNullImpl(r);
    }
    
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatNull(ReferenceSymbolic)}. The default implementation 
     * invokes {@link #delegateIsSatNull}{@code (r)}.
     *  
     * @param r see {@link #isSatNull(ReferenceSymbolic)}.
     * @return see {@link #isSatNull(ReferenceSymbolic)}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatNullImpl(ReferenceSymbolic r) throws DecisionException {
    	return delegateIsSatNull(r);
    }
    
    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by null.
     *  
     * @param r a {@link ReferenceSymbolic}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatNull(ReferenceSymbolic) isSatNull}{@code (r)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if this decision procedure has
     *         not a successor in the chain.
     */
   protected final boolean delegateIsSatNull(ReferenceSymbolic r) 
    throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSatNull(r);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " has no implementation for isSatNull and no next decision procedure in Chain Of Responsibility.");
    }

    @Override
    public final boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
    	//TODO check input parameters; simplify?
		return isSatAliasesImpl(r, heapPos, o);
    }
	
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}. 
     * The default implementation invokes 
     * {@link #delegateIsSatAliases}{@code (r, heapPos, o)}.
     *  
     * @param r see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @param heapPos see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @param o see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @return see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatAliasesImpl(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
    	return delegateIsSatAliases(r, heapPos, o);
    }
    
    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by aliasing.
     *  
     * @param r see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @param heapPos see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @param o see {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatAliases isSatAliases}{@code (r, heapPos, o)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if this decision procedure has
     *         not a successor in the chain.
     */
    protected final boolean delegateIsSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSatAliases(r, heapPos, o);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " has no implementation for isSatAliases and no next decision procedure in Chain Of Responsibility.");
    }

    @Override
    public final boolean isSatExpands(ReferenceSymbolic r, String className) 
    throws DecisionException {
    	//TODO check input parameters; simplify?
		return isSatExpandsImpl(r, className);
    }
    
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatExpands(ReferenceSymbolic, String)}. 
     * The default implementation invokes 
     * {@link #delegateIsSatExpands}{@code (r, className)}.
     *  
     * @param r see {@link #isSatExpands(ReferenceSymbolic, String)}.
     * @param className see {@link #isSatExpands(ReferenceSymbolic, String)}.
     * @return see {@link #isSatExpands(ReferenceSymbolic, String) isSatExpands}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatExpandsImpl(ReferenceSymbolic r, String className) 
    throws DecisionException {
    	return delegateIsSatExpands(r, className);
    }
    
    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by expansion.
     *  
     * @param r see {@link #isSatExpands(ReferenceSymbolic, String)}.
     * @param className see {@link #isSatExpands(ReferenceSymbolic, String)}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatExpands isSatExpands}{@code (r, className)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if this decision procedure has
     *         not a successor in the chain.
     */
    protected final boolean delegateIsSatExpands(ReferenceSymbolic r, String className) 
    throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSatExpands(r, className);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " has no implementation for isSatExpands and no next decision procedure in Chain Of Responsibility.");
    }

    @Override
    public final boolean isSatInitialized(String className) 
    throws DecisionException {
    	//TODO check input parameters; simplify?
		return isSatInitializedImpl(className);
    }
    
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatInitialized(String)}. 
     * The default implementation queries the next decision procedure, 
     * if there is one, otherwise throws a {@link DecisionException}.
     *  
     * @param className see {@link #isSatInitialized(String)}.
     * @return see {@link #isSatInitialized(String)}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatInitializedImpl(String className) 
    throws DecisionException {
    	return delegateIsSatInitialized(className);
    }
    
    protected final boolean delegateIsSatInitialized(String className) 
    throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSatInitialized(className);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " has no implementation for isSatInitialized and no next decision procedure in Chain Of Responsibility.");
    }

    @Override
    public final boolean isSatNotInitialized(String className) 
    throws DecisionException {
    	//TODO check input parameters; simplify?
		return isSatNotInitializedImpl(className);
    }
    
    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatNotInitialized(String)}. 
     * The default implementation queries the next decision procedure, 
     * if there is one, otherwise throws a {@link DecisionException}.
     *  
     * @param className see {@link #isSatNotInitialized(String)}.
     * @return see {@link #isSatNotInitialized(String)}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatNotInitializedImpl(String className) 
    throws DecisionException {
    	return delegateIsSatNotInitialized(className);
    }
    
    protected final boolean delegateIsSatNotInitialized(String className) 
    throws DecisionException {
    	if (hasNext()) {
    		return this.next.isSatNotInitialized(className);
    	}
    	throw new DecisionException("Class " + this.getClass().getName() + " has no implementation for isSatNotInitialized and no next decision procedure in Chain Of Responsibility.");
    }
	
    /**
     * Simplifies a {@link Primitive} under the current assumption.
     * 
     * @param p a boolean-valued {@link Primitive}.
     * @return a {@link Primitive} equivalent to {@code p}
     *         under the current assumption (possibly {@code p} itself).
     */
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
	public void close() throws DecisionException {
		closeLocal();
		if (hasNext()) {
			this.next.close();
		}
	}

	/**
	 * May be implemented by subclasses to locally quit a decision 
	 * procedure. By default it does nothing.
	 */
	protected void closeLocal() {
		//default implementation
	}
}
