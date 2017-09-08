package jbse.dec;

import java.io.IOException;
import java.util.Map;

import jbse.bc.ClassHierarchy;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.dec.exc.NoModelException;
import jbse.mem.Objekt;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

public abstract class DecisionProcedureExternalInterface {
    /**
     * Checks whether the external decision procedure works.
     *  
     * @return {@code true} iff the external decision procedure
     *         is working, and therefore can be used.
     */
	public abstract boolean isWorking();

	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends numeric clauses.
	 * 
	 * @param predicate the clause to put. It is a {@code Primitive}.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails.
	 */
	public abstract void sendClauseAssume(Primitive predicate) 
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends an aliasing clause for a symbolic 
	 * reference.
	 * 
     * @param r a {@link ReferenceSymbolic}.
     * @param heapPos a {@code long} value, the position of {@code o} in the heap.
     * @param o an {@link Objekt}, the object to which {@code r} refers.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void sendClauseAssumeAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends an expansion clause for a symbolic 
	 * reference.
	 * 
     * @param r a {@link ReferenceSymbolic}.
     * @param className a {@link String}, the name of the class
     *        to which {@code r} is expanded.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void sendClauseAssumeExpands(ReferenceSymbolic r, String className) 
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends a null clause for a symbolic 
	 * reference.
	 * 
     * @param r a {@link ReferenceSymbolic}, the reference that
     *        is assumed to be null.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void sendClauseAssumeNull(ReferenceSymbolic r) 
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends a class initialization clause.
	 * 
     * @param className a {@link String}, the name of the class
     *        that is initialized at the start of symbolic execution.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void sendClauseAssumeClassInitialized(String className) 
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Sends a clause to the external decision procedure; 
	 * the clause is just transmitted, without checking or 
	 * assuming it, and becomes the current clause to work on.
	 * This method sends a class not-initialization clause.
	 * 
     * @param className a {@link String}, the name of the class
     *        that is not initialized at the start of symbolic execution.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when a current predicate already exists.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void sendClauseAssumeClassNotInitialized(String className) 
	throws ExternalProtocolInterfaceException, IOException;

	/**
	 * Retracts the current predicate, setting the decision procedure 
	 * to a "no current predicate" state.
	 * 
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when there is no current predicate.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void retractClause() 
	throws ExternalProtocolInterfaceException, IOException;

	/**
	 * Verifies whether the current assumption is satisfiable 
	 * when put in logical and with the (possibly negated) 
	 * current predicate.
	 * 
	 * @param hier a {@link ClassHierarchy}.
	 * @param positive if {@code false} the current predicate must 
	 *        be negated before checking satisfiability, otherwise not.
	 * @return {@code false} if the decision procedure proves that the 
	 *         current assumption and the (possibly negate) current 
	 *         predicate are not satisfiable, {@code true} otherwise. 
	 * @throws ExternalProtocolInterfaceException if this method is 
	 *         invoked when there is no current predicate.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract boolean checkSat(ClassHierarchy hier, boolean positive)
	throws ExternalProtocolInterfaceException, IOException;
	
    /**
     * Returns a model of the last sent clause whose satisfiability
     * was checked with {@link #checkSat(ClassHierarchy, boolean) checkSat}.
     * The model is for the numeric symbols only.
     * 
     * @return a {@link Map}{@code <}{@link PrimitiveSymbolic}{@code ,}
     *         {@link Simplex}{@code >} associating a concrete 
     *         numeric value to all the symbols with numeric type
     *         in the last checked clause. 
     * @throws NoModelException if the external decision
     *         procedure cannot produce a model, either because
     *         the method is unimplemented or for any reason.
     * @throws ExternalProtocolInterfaceException if this method is 
     *         invoked when there is no current predicate.
     * @throws IOException if communication with the external 
     *         decision procedure fails. 
     */
	public Map<PrimitiveSymbolic, Simplex> getModel() 
    throws NoModelException, ExternalProtocolInterfaceException, IOException {
        throw new NoModelException("Model extraction is not implemented for external decision procedure interface of class " + this.getClass().getName());
	}

	/**
	 * Pushes the (possibly negated) current clauses to the current
	 * assumptions. 
	 * 
	 * @param positive if {@code false} the current predicate is negated
	 *        before pushing, otherwise not.
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when there is no current predicate.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails. 
	 */
	public abstract void pushAssumption(boolean positive)
	throws ExternalProtocolInterfaceException, IOException;
	
	/**
	 * Pops the last clause added to the current assumption by 
	 * a call to {@link #pushAssumption(boolean)}. It can be unimplemented.
	 * 
	 * @throws ExternalProtocolInterfaceException if this method
	 *         is invoked when there is a current predicate, 
	 *         or if the method is unimplemented.
	 * @throws IOException if communication with the external 
	 *         decision procedure fails.
	 */
	public void popAssumption()
	throws ExternalProtocolInterfaceException, IOException {
		throw new ExternalProtocolInterfaceException("Popping assumptions is not implemented for external decision procedure interface of class " + this.getClass().getName());
	}

	/**
	 * Deletes the whole assumption set.
	 * 
	 * @throws ExternalProtocolInterfaceException if it is
	 *         invoked after {@link #quit()}.
	 * @throws IOException 
	 */
	public abstract void clear() 
	throws ExternalProtocolInterfaceException, IOException;

	/**
	 * Quits the decision procedure.
	 * 
	 * @throws ExternalProtocolInterfaceException
	 * @throws IOException 
	 */
	public abstract void quit() 
	throws ExternalProtocolInterfaceException, IOException;

	/**
	 * To be invoked when the decision procedure fails.
	 */
	public abstract void fail();
}