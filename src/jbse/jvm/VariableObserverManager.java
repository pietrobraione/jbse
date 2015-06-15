package jbse.jvm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jbse.bc.Signature;
import jbse.common.Type;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.StateTree.BranchPoint;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * A {@link VariableObserverManager} is the manager (see the classical GoF Observer pattern)
 * of the subscriptions and notifications of the {@link ExecutionObserver}s that are 
 * interested in variable value changes.
 * 
 * @author Pietro Braione
 *
 */
class VariableObserverManager {
	/** The name of the (root) class containing the variables to observe. */ 
	private final String rootClassName;
	
	/** The reference to the root object (does not change during symbolic execution). */
	private Reference rootObjectReference = null;

	/** The {@link Signature}s of the variables under observation. */
	private LinkedList<Signature> varSigs = new LinkedList<>();

	/** The {@link ExecutionObserver}s of the variables {@link varSigs}. */
	private LinkedList<ExecutionObserver> obs = new LinkedList<>();

	/** Cache for the current {@link Value}s of the variables under observation. */
	private LinkedList<Value> values = new LinkedList<>();

	/** Cache for the {@link Value}s of the variables under observation (for backtrack). */
	private Map<BranchPoint, LinkedList<Value>> savedValues;
	
	/** Backlink to the {@link Engine}; will be initialized later. */
	private Engine engine;
	
	VariableObserverManager(String rootClassName) {
		this.rootClassName = rootClassName;
	}
	
	void addObserver(Signature varSignature, ExecutionObserver obs) {
		if (varSignature == null || obs == null) {
			throw new NullPointerException(); 
		}
		if (varSignature.getClassName().equals(rootClassName) && 
		    varSignature.getDescriptor().equals("" + Type.BOOLEAN)) {
			this.varSigs.add(varSignature);
			this.obs.add(obs);
		} //TODO else? Maybe throw some exception
	}

	void init(Engine engine) 
	throws ThreadStackEmptyException, NonexistingObservedVariablesException {
		this.engine = engine;
		this.rootObjectReference = this.engine.getCurrentState().getRootObjectReference();
		
		//saves the values of the observed variables
		final List<Integer> nonexistingVariables = new LinkedList<Integer>();
		if (this.hasObservers()) {
			for (int i = 0; i < this.numObservers(); ++i) {
				this.values.add(this.getObservedVariableValue(i));
				if (this.values.get(i) == null) {
					nonexistingVariables.add(i);
					this.obs.add(null);
				}
			}
			this.savedValues = new HashMap<BranchPoint, LinkedList<Value>>();
		}
		
		//if some of the observed variables does not exist, throws 
		//the related exception
		if (nonexistingVariables.size() > 0) {
			throw new NonexistingObservedVariablesException(nonexistingVariables);
		}		
	}
	
	void notifyObservers(BranchPoint branch) {
		if (this.hasObservers()) {
			//if may backtrack, saves last observed values
			final boolean mayBacktrack = (branch != null);
			if (mayBacktrack) {
				final LinkedList<Value> toSave = new LinkedList<Value>();
				for (Value v : this.values) { 
					if (v != null) {
						toSave.add(v);
					}
				}
				this.savedValues.put(branch, toSave);
			}
			
			//updates values of observed variables and notifies 
			//observers
			for (int i = 0; i < this.obs.size(); ++i) {
				final ExecutionObserver o = this.obs.get(i);
				final Value vOld = this.values.get(i);
				final Value vNew = this.getObservedVariableValue(i);
				if (o == null || vNew == null || vOld.equals(vNew)) {
					; //does nothing
				} else {
					this.values.set(i, vNew);
					o.update(this.engine);
				}
			}
		}
	}
	
	void saveObservedVariablesValues(BranchPoint bp) {
		if (this.hasObservers()) {
			final LinkedList<Value> toSave = new LinkedList<Value>();
			for (int i = 0; i < this.values.size(); ++i) {
				toSave.add(this.values.get(i));
			}
			this.savedValues.put(bp, toSave);
		}
	}
	
	void restoreObservedVariablesValues(BranchPoint bp, boolean delete) {
		if (this.hasObservers()) {
			this.values = this.savedValues.get(bp);
			if (delete) {
				this.savedValues.remove(bp);
			}
		}
	}

	/**
	 * Tests whether there are some variables under observation.
	 * 
	 * @return {@code true} iff some variable with their observers have been registered.
	 */
	private boolean hasObservers() {
		return (this.varSigs.size() > 0);
	}

	/**
	 * Gets the number of registered observers.
	 * 
	 * @return the number of specified observers variables, independently on the
	 *         fact that they do indeed exist.
	 */
	private int numObservers() {
		return (this.varSigs.size());
	}

	/**
	 * Gets the current value of a variable under observation.
	 * 
	 * @param i the ordinal number of the observed variable as 
	 *        set by initialization.
	 * @return the current value of {@code this.varSigs[i]}, or 
	 *         {@code null} if the variable does not exist
	 *         neither in the root object nor in its class.
	 */
	private Value getObservedVariableValue(int i) {
		final Signature obsVarSignature = this.varSigs.get(i);
		Value retVal = null;
		if (this.engine.getCurrentState().getStackSize() > 0) {
			//if the state is not stuck because of a return 
			//from the root method, looks in the root object
			final Instance rootObject = (Instance) this.engine.getCurrentState().getObject(this.rootObjectReference);
			retVal = rootObject.getFieldValue(obsVarSignature);
		}
		if (retVal == null) {
			//not in the root object? Let's see if it is a static variable 
			//in the root class 
			final Klass rootKlass = this.engine.getCurrentState().getKlass(rootClassName);
			retVal = rootKlass.getFieldValue(obsVarSignature);
		}
		return retVal;
	}
}
