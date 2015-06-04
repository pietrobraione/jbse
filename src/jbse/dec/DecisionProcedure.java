package jbse.dec;

import java.util.Collection;

import jbse.bc.ClassHierarchy;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;

/**
 * A {@code DecisionProcedure} accumulates a satisfiable assumption as a 
 * list of {@link Clause}s, and checks whether another clause is satisfiable under 
 * the assumption. It is organized as a Chain Of Responsibility, where 
 * if a {@code DecisionProcedure} is unable to decide the satisfiability 
 * of a {@link Clause} within an acceptable time interval, it may invoke 
 * the next {@code DecisionProcedure} in the chain to perform another try.
 * A {@code DecisionProcedure} can also simplify {@link Clause}s based 
 * on the current assumption.
 */
public interface DecisionProcedure extends AutoCloseable {
	/**
	 * Possibly delays checking that the pushed clauses 
	 * are inconsistent with the current cumulated 
	 * assumption ("fast and imprecise" mode). 
	 */
	default void goFastAndImprecise() { }
	
	/**
	 * Checks that all the pushed assumption clauses are 
	 * consistent with the current cumulated assumptions. 
	 * This is the mode of the decision procedure after
	 * creation. 
	 */
	default void stopFastAndImprecise() { }
	
    /**
     * Adds a {@link Clause} to the cumulated assumptions. This method 
     * <emph>may</emph> be faster (it needs not to be) when invoked 
     * after a {@link #goFastAndImprecise()} call than when invoked 
     * after a {@link #stopFastAndImprecise()} call or after creation.
     * 
     * @param c the {@link Clause} to be added. In the case {@code c} 
     *        is pushed after a call to {@link #goFastAndImprecise()}, the 
     *        {@link DecisionProcedure} <emph>might not</emph> check that 
     *        {@code c} does not contradict the current assumption.
     * @throws DecisionException upon failure, and when {@code c}
     *         contradicts the current assumption (after a call to 
     *         {@link #goFastAndImprecise()} the latter check 
     *         <emph>might not</emph> be performed).
     */
	void pushAssumption(Clause c) throws DecisionException;
    
    /**
     * Drops the current assumptions.
     * 
     * @throws DecisionException upon failure.
     */
	void clearAssumptions() throws DecisionException;
	
	
    /**
     * Adds to the current assumptions more assumptions.  
     * 
     * @param assumptionsToAdd a {@link Iterable}{@code <}{@link Clause}{@code >}, the
     *        new assumptions that must be added to the current ones, iterable in FIFO order 
     *        w.r.t. pushes. It may not be {@code null}.
     * @throws DecisionException upon failure.
     */	
	default void addAssumptions(Iterable<Clause> assumptionsToAdd) throws DecisionException {
		for (Clause c : assumptionsToAdd) {
			pushAssumption(c);
		}
	}

    /**
     * Changes the current assumptions.  
     * 
     * @param newAssumptions a {@link Collection}{@code <}{@link Clause}{@code >}, the
     *        new assumptions that must replace the current ones, iterable in FIFO order 
     *        w.r.t. pushes. It may not be {@code null}.
     * @throws DecisionException upon failure.
     */
	default void setAssumptions(Collection<Clause> newAssumptions) throws DecisionException {
		clearAssumptions();
		addAssumptions(newAssumptions);
	}
	
    /**
     * Gets the current assumptions.
     * 
     * @return an immutable {@link Collection}{@code <}{@link Clause}{@code >}
     *         with all the pushed clauses, possibly simplified.
     * @throws DecisionException upon failure.
     */
	Collection<Clause> getAssumptions() throws DecisionException;
    
    /**
     * Determines the satisfiability of an {@link Expression} under the
     * current assumption.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param exp a boolean {@link Expression}.
     * @return {@code true} iff {@code exp} is satisfiable under
     *         the current assumptions.
     * @throws DecisionException upon failure.
     */
    boolean isSat(ClassHierarchy hier, Expression exp) throws DecisionException;
    
    /**
     * Determines the satisfiability of a resolution by null under the
     * current assumptions.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param r a {@link ReferenceSymbolic}.
     * @return {@code true} iff {@code r} can be resolved by null under
     *         the current assumption.
     * @throws DecisionException upon failure.
     */
    boolean isSatNull(ClassHierarchy hier, ReferenceSymbolic r) throws DecisionException;
	
    /**
     * Determines the satisfiability of a resolution by aliasing under the
     * current assumptions.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param r a {@link ReferenceSymbolic}.
     * @param heapPos a {@code long} value, the position of {@code o} in the heap.
     * @param o an {@link Objekt}, the object to which {@code r} refers.
     * @return {@code true} iff {@code r} can be resolved by aliasing to {@code o}
     *         under the current assumption.
     * @throws DecisionException upon failure.
     */
    boolean isSatAliases(ClassHierarchy hier, ReferenceSymbolic r, long heapPos, Objekt o) throws DecisionException;
	
    /**
     * Determines the satisfiability of a resolution by expansion under the
     * current assumptions.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param r a {@link ReferenceSymbolic}.
     * @param className a {@link String}, the name of a class.
     * @return {@code true} iff {@code r} can be resolved by aliasing to 
     *         a fresh object of class {@code className} under
     *         the current assumption.
     * @throws DecisionException upon failure.
     */
    boolean isSatExpands(ClassHierarchy hier, ReferenceSymbolic r, String className) throws DecisionException;
	
    /**
     * Determines the satisfiability of the assumption that a class is
     * initialized when symbolic execution starts, under the current
     * assumptions.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param className a {@link String}, the name of a class.
     * @return {@code true} iff the assumption that {@code className} is
     *         initialized at the start of the symbolic execution is 
     *         satisfiable under the current assumption.
     * @throws DecisionException upon failure.
     */
    boolean isSatInitialized(ClassHierarchy hier, String className) throws DecisionException;
	
    /**
     * Determines the satisfiability of the assumption that a class is
     * not initialized when symbolic execution starts, under the current
     * assumptions.
     * 
     * @param hier a {@link ClassHierarchy}.
     * @param className a {@link String}, the name of a class.
     * @return {@code true} iff the assumption that {@code className} is
     *         not initialized at the start of the symbolic execution is 
     *         satisfiable under the current assumption.
     * @throws DecisionException upon failure. 
     */
    boolean isSatNotInitialized(ClassHierarchy hier, String className) throws DecisionException;
    
    /**
     * Simplifies a {@link Primitive} under the current assumptions.
     * 
     * @param p a boolean {@link Primitive}.
     * @return a {@link Primitive} equivalent to {@code p}
     *         under the current assumption (possibly {@code p} itself).
     */
    default Primitive simplify(Primitive p) {
    	return p;
    }
    
    /**
     * Releases the resources of the {@link DecisionProcedure}. After 
     * invocation of this method the {@link DecisionProcedure} cannot be
     * used anymore.
     * 
     * @throws DecisionException upon failure.
     */
    @Override
    default void close() throws DecisionException {
		//does nothing
	}
}