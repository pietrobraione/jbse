package jbse.mem;

import jbse.common.exc.InvalidInputException;

/**
 * A Java object which may reside in the heap, 
 * i.e., either an instance of a class, or an array.
 */
public interface HeapObjekt extends Objekt {
    /**
     * Checks whether this {@link HeapObjekt} is initial.
     * 
     * @return {@code true} iff this object is initial, i.e., iff it is 
     *        not an object of the current state, but an (immutable) copy of the
     *        same symbolic object as it was in the initial state. Note that if
     *        {@link #isInitial() isInitial}{@code () == true}, then also 
     *        {@link #isSymbolic() isSymbolic}{@code () == true}
     *        and exists another, noninitial {@link Objekt}{@code  o} in the heap of the current state
     *        for which {@code this.}{@link #getOrigin() getOrigin}{@code () == o.}{@link #getOrigin() getOrigin}{@code ()}.
     *        The symbolic reference {@link #getOrigin() getOrigin}{@code ()} will
     *        be resolved to {@code o}, not to {@code this}.
     */
    boolean isInitial();

    /**
     * Makes this {@link HeapObjekt} initial if it is symbolic.
     * 
     * @throws InvalidInputException if {@link #isSymbolic() isSymbolic}{@code () == false}.
     */
    //TODO this is really ugly! Possibly delete and do as with arrays, i.e., all objects are created symbolic/initial upon construction and never need to change from concrete to symbolic and to noninitial to initial.
    void makeInitial() throws InvalidInputException;
    
	HeapObjekt clone();
}