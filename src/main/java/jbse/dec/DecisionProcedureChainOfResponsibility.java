package jbse.dec;

import static jbse.val.Rewriter.applyRewriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
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
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Rewriter;
import jbse.val.Simplex;
import jbse.val.exc.NoResultException;

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
	/** The {@link Calculator}. */
	protected final Calculator calc;
	
    /** The next {@link DecisionProcedure} in the Chain Of Responsibility */
    protected final DecisionProcedure next;

    /** 
     * The {@link Rewriter}s for the (possible) simplification of expressions. 
     * Not final to allow to set it later than construction (may be necessary 
     * if the rewriters are created by the decision procedure itself). 
     */
    protected Rewriter[] rewriters;

    /**
     * Constructor.
     * 
     * @param next the next {@link DecisionProcedure} in the 
     *        Chain Of Responsibility. It must not be {@code null}.
     * @param rewriters a vararg of {@link Rewriter}s for the 
     *        (possible) simplification of expressions.
     * @throws InvalidInputException if {@code next == null || rewriters == null}. 
     */
    protected DecisionProcedureChainOfResponsibility(DecisionProcedure next, Rewriter... rewriters) 
    throws InvalidInputException {
    	if (next == null || rewriters == null) {
    		throw new InvalidInputException("Attempted to construct a " + getClass().getName() + " with null DecisionProcedure next or Rewriter... rewriters.");
    	}
    	this.calc = next.getCalculator();
        this.next = next;
        this.rewriters = rewriters;
    }

    /**
     * Constructor (no next procedure in the Chain Of Responsibility).
     * 
     * @param a {@link Calculator}. It must not be {@code null}.
     * @param rewriters a vararg of {@link Rewriter}s for the 
     *        (possible) simplification of expressions.
     * @throws InvalidInputException if {@code calc == null || rewriters == null}.
     */
    protected DecisionProcedureChainOfResponsibility(Calculator calc, Rewriter... rewriters) 
    throws InvalidInputException {
    	if (calc == null || rewriters == null) {
    		throw new InvalidInputException("Attempted to construct a " + getClass().getName() + " with null Calculator calc or Rewriter... rewriters.");
    	}
    	this.calc = calc;
    	this.next = null;
    	this.rewriters = rewriters;
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
    public final Calculator getCalculator() {
    	return this.calc;
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
    public final List<Clause> getAssumptions() throws DecisionException {
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
    protected List<Clause> getAssumptionsLocal() 
    throws DecisionException {
        throw new DecisionException(NO_ASSUMPTION_ERROR); //TODO throw a better exception
    }

    private final String NO_ASSUMPTION_ERROR = "Class " + getClass().getName() + " does not store assumptions and is the last in the Chain of Responsibility.";

    @Override
    public final boolean isSat(Expression expression) 
    throws InvalidInputException, DecisionException {
        if (expression == null) {
            throw new InvalidInputException("isSat invoked with a null parameter.");
        }
        if (expression.getType() != Type.BOOLEAN) {
            throw new DecisionException("isSat expression has type " + expression.getType());
        }
        final Primitive expSimpl = simplifyLocal(expression);
        if (expSimpl instanceof Simplex) {
            return ((Simplex) expSimpl).surelyTrue();
        } else { // (expSimpl instanceof Expression)
            final boolean localDecidesSat = isSatLocal(expression, (Expression) expSimpl);
            if (localDecidesSat && hasNext()) {
                //tries the delegate, that could have a more restrictive answer
                return delegateIsSat(expression);  //TODO shouldn't we pass expSimpl instead? do we really need to pass the original exp to the next in chain?
            }
            return localDecidesSat;
        }
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSat(Expression)}. 
     * The default implementation 
     * answers {@code true} (no local decision) so the
     * answer is generated by the next decision procedure in 
     * the chain. 
     *  
     * @param exp see {@link #isSat(Expression) isSat}. 
     *        Note that it is <em>not</em> locally simplified, and that 
     *        it remains an {@link Expression} after local simplification.
     * @param expSimpl {@code exp} after local simplification.
     * @return see {@link #isSat(Expression) isSat}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatLocal(Expression exp, Expression expSimpl) throws DecisionException {
        return true;
    }

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of an {@link Expression}.
     *  
     * @param exp see {@link #isSat(Expression) isSat}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSat(Expression) isSat}{@code (exp)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private boolean delegateIsSat(Expression exp) 
    throws DecisionException {
        try {
            return this.next.isSat(exp);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public final boolean isSatNull(ReferenceSymbolic r) 
    throws InvalidInputException, DecisionException {
        if (r == null) {
            throw new InvalidInputException("isSatNull invoked with a null parameter.");
        }
        final boolean localDecidesSat = isSatNullLocal(r);
        if (localDecidesSat && hasNext()) {
            return delegateIsSatNull(r);
        }
        return localDecidesSat;
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatNull(ReferenceSymbolic)}. 
     * The default implementation 
     * answers {@code true} (no local decision) so the
     * answer is generated by the next decision procedure in 
     * the chain.
     * 
     * @param r see {@link #isSatNull(ReferenceSymbolic) isSatNull}.
     * @return see {@link #isSatNull(ReferenceSymbolic) isSatNull}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatNullLocal(ReferenceSymbolic r) throws DecisionException {
        return true;
    }

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by null.
     *  
     * @param r see {@link #isSatNull(ReferenceSymbolic) isSatNull}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatNull(ReferenceSymbolic) isSatNull}{@code (r)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private boolean delegateIsSatNull(ReferenceSymbolic r) 
    throws DecisionException {
        try {
            return this.next.isSatNull(r);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public final boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws InvalidInputException, DecisionException {
        if (r == null || o == null) {
            throw new InvalidInputException("isSatAliases invoked with a null parameter.");
        }
        final boolean localDecidesSat = isSatAliasesLocal(r, heapPos, o);
        if (localDecidesSat && hasNext()) {
            return delegateIsSatAliases(r, heapPos, o);
        }
        return localDecidesSat;
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatAliases(ReferenceSymbolic, long, Objekt)}. 
     * The default implementation answers {@code true} 
     * (no local decision) so the answer is generated by 
     * the next decision procedure in the chain.
     *  
     * @param r see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @param heapPos see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @param o see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @return see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatAliasesLocal(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
        return true;
    }

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by aliasing.
     *  
     * @param hier see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @param r see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @param heapPos see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @param o see {@link #isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatAliases(ReferenceSymbolic, long, Objekt) isSatAliases}{@code (r, heapPos, o)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private boolean delegateIsSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
    throws DecisionException {
        try {
            return this.next.isSatAliases(r, heapPos, o);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public final boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        if (r == null || classFile == null) {
            throw new InvalidInputException("isSatExpands invoked with a null parameter.");
        }
        final boolean localDecidesSat = isSatExpandsLocal(r, classFile);
        if (localDecidesSat && hasNext()) {
            return delegateIsSatExpands(r, classFile);
        }
        return localDecidesSat;
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatExpands(ReferenceSymbolic, String)}. 
     * The default implementation answers {@code true} 
     * (no local decision) so the answer is generated by 
     * the next decision procedure in the chain. 
     *  
     * @param r see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @param classFile see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @return see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatExpandsLocal(ReferenceSymbolic r, ClassFile classFile) 
    throws DecisionException {
        return true;
    }

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of a resolution by expansion.
     *  
     * @param hier see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @param r see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @param classFile see {@link #isSatExpands(ReferenceSymbolic, ClassFile) isSatExpands}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatExpands(ReferenceSymbolic, String) isSatExpands}{@code (r, classFile)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private final boolean delegateIsSatExpands(ReferenceSymbolic r, ClassFile classFile) 
    throws DecisionException {
        try {
            return this.next.isSatExpands(r, classFile);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public final boolean isSatInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        if (classFile == null) {
            throw new InvalidInputException("isSatInitialized invoked with a null parameter.");
        }
        final boolean localDecidesSat = isSatInitializedLocal(classFile);
        if (localDecidesSat && hasNext()) {
            return delegateIsSatInitialized(classFile);
        }
        return localDecidesSat;
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatInitialized(ClassFile)}. 
     * The default implementation answers {@code true} 
     * (no local decision) so the answer is generated 
     * by the next decision procedure in 
     * the chain.
     *  
     * @param hier see {@link #isSatInitialized(ClassFile) isSatInitialized}.
     * @param classFile see {@link #isSatInitialized(ClassFile) isSatInitialized}.
     * @return see {@link #isSatInitialized(ClassFile) isSatInitialized}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatInitializedLocal(ClassFile classFile) 
    throws DecisionException {
        return true;
    }


    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of the assumption that a class is
     * initialized when symbolic execution starts.
     *  
     * @param hier see {@link #isSatInitialized(ClassFile) isSatInitialized}.
     * @param classFile see {@link #isSatInitialized(ClassFile) isSatInitialized}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatInitialized(ClassFile) isSatInitialized}{@code (classFile)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private final boolean delegateIsSatInitialized(ClassFile classFile) 
    throws DecisionException {
        try {
            return this.next.isSatInitialized(classFile);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public final boolean isSatNotInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
        if (classFile == null) {
            throw new InvalidInputException("isSatNotInitialized invoked with a null parameter.");
        }
        final boolean localDecidesSat = isSatNotInitializedLocal(classFile);
        if (localDecidesSat && hasNext()) {
            return delegateIsSatNotInitialized(classFile);
        }
        return localDecidesSat;
    }

    /**
     * Must be overridden by subclasses to implement 
     * {@link #isSatNotInitialized(ClassFile)}. 
     * The default implementation answers {@code true} 
     * (no local decision) so the answer is generated by 
     * the next decision procedure in the chain. 
     *  
     * @param hier see {@link #isSatNotInitialized(ClassFile) isSatNotInitialized}.
     * @param classFile see {@link #isSatNotInitialized(ClassFile) isSatNotInitialized}.
     * @return see {@link #isSatNotInitialized(ClassFile) isSatNotInitialized}.
     * @throws DecisionException upon failure.
     */
    protected boolean isSatNotInitializedLocal(ClassFile classFile) 
    throws DecisionException {
        return true;
    }

    /**
     * Queries the next decision procedure in the chain for 
     * satisfiability of the assumption that a class is
     * not initialized when symbolic execution starts.
     *  
     * @param hier see {@link #isSatNotInitialized(ClassFile) isSatNotInitialized}.
     * @param classFile see {@link #isSatNotInitialized(ClassFile) isSatNotInitialized}.
     * @return the result of invoking 
     *         {@link DecisionProcedure#isSatNotInitialized(ClassFile) isSatNotInitialized}{@code (classFile)}
     *         on the next decision procedure in the chain.
     * @throws DecisionException if the successor
     *         throws it.
     */
    private final boolean delegateIsSatNotInitialized(ClassFile classFile) 
    throws DecisionException {
        try {
            return this.next.isSatNotInitialized(classFile);
        } catch (InvalidInputException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public Map<PrimitiveSymbolic, Simplex> getModel() 
    throws DecisionException {
        try {
            return getModelLocal();
        } catch (NoModelException e) {
            if (hasNext()) {
                return delegateGetModel();
            } else {
                throw e;
            }
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
     * @throws DecisionException if the successor
     *         throws it.
     */
    private final Map<PrimitiveSymbolic, Simplex> delegateGetModel() 
    throws DecisionException {
        return this.next.getModel();
    }

    @Override
    public final Primitive simplify(Primitive p) throws DecisionException {
        final Primitive pSimpl = simplifyLocal(p);
        if (hasNext()) {
            return this.next.simplify(pSimpl);
        }
        return pSimpl;
    }

    /**
     * Simplifies a {@link Clause} under the current assumption.
     * 
     * @param c a {@link Clause}.
     * @return a {@link Clause} equivalent to {@code c}
     *         under the current assumption (possibly {@code c} itself).
     * @throws DecisionException  if simplification returned {@code null}
     *         or a {@link Primitive} that is neither a {@link Simplex} 
     *         nor an {@link Expression}.
     */
    private final Clause simplifyLocal(Clause c) throws DecisionException {
        if (c instanceof ClauseAssume) {
            final Primitive p = ((ClauseAssume) c).getCondition();
            final Primitive pSimpl = simplifyLocal(p);
            try {
                return new ClauseAssume(pSimpl);
            } catch (InvalidInputException e) {
                throw new UnexpectedInternalException(e);
            }
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
     *         (possibly {@code p} itself).
     * @throws DecisionException if simplification returned {@code null}
     *         or a {@link Primitive} that is neither a {@link Simplex} 
     *         nor an {@link Expression}.
     */
    protected final Primitive simplifyLocal(Primitive p) throws DecisionException {
        try {
            final Primitive retVal = applyRewriters(p, this.rewriters);
            if (retVal == null || retVal.getType() != Type.BOOLEAN || 
            !(retVal instanceof Simplex || retVal instanceof Expression)) {
                //TODO throw a better exception
                throw new DecisionException("The simplification of " + p + " returned " + retVal + " that is neither a boolean Simplex nor a boolean Expression.");
            }
            return retVal;
        } catch (NoResultException e) {
            throw new DecisionException(e);
        }
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
