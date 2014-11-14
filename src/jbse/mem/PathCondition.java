package jbse.mem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;

/**
 * A path condition. It retains all the clauses gathered at the 
 * different branch points traversed during execution as a 
 * suitable {@link Collection}{@code <}{@link Clause}{@code >}. 
 */
final class PathCondition implements Cloneable {
	/** {@link ArrayList} of all the {@link Clause}s forming the path condition. */
	private ArrayList<Clause> clauses;
	
	/** 
	 * Maps symbolic reference identifiers to their respective heap positions.
	 * It is just a cache of information already contained in {@code clauses}.
	 */
	private HashMap<Integer, Long> referenceResolutionMap;
	
	/**
	 * Maps each class with the number of assumed objects in it. 
	 * It is just a cache of information already contained in {@code clauses}.
	 */
	private HashMap<String, Integer> objectCounters;

    /**
     * Constructor.
     */
    PathCondition() {
    	this.clauses = new ArrayList<>();
    	this.referenceResolutionMap = new HashMap<>();
    	this.objectCounters = new HashMap<>();
    }
    
    /**
     * Adds a clause to the path condition. The clause is a condition 
     * over primitive values.
     * 
     * @param condition the additional condition as a {@link Primitive}.
     */
    void addClauseAssume(Primitive condition) {
		this.clauses.add(new ClauseAssume(condition));
    }

    /**
     * Adds a clause to the path condition. The clause is the resolution 
     * of a symbolic reference by expansion. 
     * 
     * @param r the {@link ReferenceSymbolic} which is resolved. It 
     *          must be {@code r != null} or the method has no effect.
     * @param heapPosition the position in the heap of the object to 
     *        which {@code r} is expanded.
     * @param o the {@link Objekt} to which {@code r} is expanded.
     */
    void addClauseAssumeExpands(ReferenceSymbolic r, long heapPosition, Objekt o) {
    	this.clauses.add(new ClauseAssumeExpands(r, heapPosition, o));
    	this.referenceResolutionMap.put(r.getId(), heapPosition);
    	
    	//increments objectCounters
    	if (!this.objectCounters.containsKey(o.getType())) {
    		this.objectCounters.put(o.getType(), 0);
    	}
    	final int nobjects = this.objectCounters.get(o.getType());
    	this.objectCounters.put(o.getType(), nobjects + 1);
    }

    /**
     * Adds a clause to the path condition. The clause is the resolution 
     * of a symbolic reference by aliasing. 
     * 
     * @param r the {@link ReferenceSymbolic} which is resolved. 
     * @param heapPosition the position in the heap of the object to 
     *        which {@code r} is resolved.
	 * @param aliasOrigin the {@link Objekt} at position {@code heapPosition}
	 *        as it was at the beginning of symbolic execution, or equivalently 
	 *        at the time of its assumption.
     */
    void addClauseAssumeAliases(ReferenceSymbolic r, long heapPosition, Objekt object) {
    	this.clauses.add(new ClauseAssumeAliases(r, heapPosition, object));
    	this.referenceResolutionMap.put(r.getId(), heapPosition);
    }

    /**
     * Adds a clause to the path condition. The clause is the resolution 
     * of a symbolic reference by assuming it null. 
     * 
     * @param r the {@link ReferenceSymbolic} which is resolved. 
     */
    void addClauseAssumeNull(ReferenceSymbolic r) {
		this.clauses.add(new ClauseAssumeNull(r));
		this.referenceResolutionMap.put(r.getId(), Util.POS_NULL);
    }

    /**
     * Adds a clause to the path condition. The clause is the resolution of a 
     * class by assuming it preloaded.
     *   
     * @param className the class name as a {@link String}.
     * @param k the symbolic {@link Klass} object to which {@code className}
     * is resolved.
     */
    void addClauseAssumeClassInitialized(String className, Klass k) {
   		this.clauses.add(new ClauseAssumeClassInitialized(className, k));
    }

    /**
     * Adds a clause to the path condition. The clause is the resolution of a 
     * class by assuming it non-preloaded.
     *   
     * @param className the concrete class name as a {@link String}.
     */
    void addClauseAssumeClassNotInitialized(String className) {
   		this.clauses.add(new ClauseAssumeClassNotInitialized(className));
    }

	/**
	 * Tests whether a symbolic reference is resolved.
	 * 
	 * @param ref a {@link ReferenceSymbolic}.
	 * @return {@code true} iff {@code ref} is resolved.
	 */
    boolean resolved(ReferenceSymbolic ref) {
    	return this.referenceResolutionMap.containsKey(ref.getId());
    }
        
	/**
	 * Returns the heap position associated to a resolved 
	 * symbolic reference.
	 * 
	 * @param ref a {@link ReferenceSymbolic}. It must be 
	 * {@link #resolved}{@code (ref) == true}.
	 * @return a {@code long}, the heap position to which
	 * {@code ref has been resolved.
	 * @throws NullPointerException if 
	 * {@link #resolved}{@code (ref) == false}.
	 */
    long getResolution(ReferenceSymbolic ref) {
    	return this.referenceResolutionMap.get(ref.getId());
    }
    
    /**
     * Tests whether this path condition refines, i.e., 
     * if it has more clauses than, another one.
     * 
     * @param pc the {@link PathCondition} to be compared against.
     * @return an {@link Iterator}{@code <}{@link Clause}{@code >} 
     *         if {@code this} refines {@code pc}, pointing to the
     *         first clause in {@code this} that does not appear in 
     *         {@code pc}. If {@code this} does not refine {@code pc}
     *         returns {@code null}.
     */
    Iterator<Clause> refines(PathCondition pc) {
    	final Iterator<Clause> i = this.clauses.iterator();
    	for (Clause c : pc.clauses) {
    		if (!i.hasNext()) {
    			return null;
    		}
    		final Clause cc = i.next();
    		if (!cc.equals(c)) {
    			return null;
    		}
    	}
    	return i;
    }
    
    /**
     * Returns the number of assumed object of a given class.
     * 
     * @param className a {@link String}.
     * @return the number of objects with class {@code className}
     * assumed by this path condition.
     */
    int getNumAssumed(String className) {
    	if (this.objectCounters.containsKey(className)) {
    		return this.objectCounters.get(className);
    	}
    	return 0;
    }
    
    /**
     * Returns all the {@link Clause}s of the path condition.
     *  
     * @return a read-only {@link List}{@code <}{@link Clause}{@code >} 
     * representing all the {@link Clause}s cumulated in {@code this}. 
     * It is valid until {@code this} is modified.
     */
    List<Clause> getClauses() {
    	return Collections.unmodifiableList(this.clauses);
    }
    
    @Override
    public String toString() {
    	final StringBuilder buf = new StringBuilder();
    	boolean isFirst = true;
    	for (Clause c : this.clauses) {
    		if (isFirst) {
    		    isFirst = false;
    		} else {
    		    buf.append(" && ");
    		}
    		buf.append(c.toString());
    	}
    	
    	final String bufString = buf.toString();
    	if (bufString.isEmpty()) {
    		return "true";
    	} else {
    	    return bufString;
    	}
    }
    
    @Override
    public PathCondition clone() {
        final PathCondition o;
        try {
            o = (PathCondition) super.clone();
        } catch (CloneNotSupportedException e) {
        	throw new InternalError(e);
        }
        
        //does a deep copy
        o.clauses = new ArrayList<Clause>(this.clauses);
        o.referenceResolutionMap = new HashMap<>(this.referenceResolutionMap);
        o.objectCounters = new HashMap<>(this.objectCounters);
        
        return o;
    }
}
