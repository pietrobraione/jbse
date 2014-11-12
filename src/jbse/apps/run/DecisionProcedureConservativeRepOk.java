package jbse.apps.run;

import static jbse.jvm.Util.doRunRepOk;

import java.util.HashMap;
import java.util.function.Supplier;

import jbse.Type;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureChainOfResponsibility;
import jbse.exc.algo.CannotManageStateException;
import jbse.exc.algo.PleaseDoNativeException;
import jbse.exc.bc.ClassFileNotFoundException;
import jbse.exc.bc.IncompatibleClassFileException;
import jbse.exc.bc.InvalidClassFileFactoryClassException;
import jbse.exc.bc.MethodNotFoundException;
import jbse.exc.bc.NoMethodReceiverException;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.jvm.CannotBacktrackException;
import jbse.exc.jvm.CannotBuildEngineException;
import jbse.exc.jvm.EngineStuckException;
import jbse.exc.jvm.FailureException;
import jbse.exc.jvm.InitializationException;
import jbse.exc.jvm.NonexistingObservedVariablesException;
import jbse.exc.mem.CannotRefineException;
import jbse.exc.mem.ContradictionException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidSlotException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Objekt;
import jbse.mem.Reference;
import jbse.mem.ReferenceConcrete;
import jbse.mem.ReferenceSymbolic;
import jbse.mem.State;
import jbse.meta.annotations.ConservativeRepOk;
import jbse.rewr.CalculatorRewriting;

/**
 * A decision procedure based on the execution of conservative repOk methods;
 * it validates a path condition by constructing the corresponding initial 
 * state, and running the methods annotated with the {@link ConservativeRepOk}
 * annotation on all the object of the corresponding classes that are present
 * in the refined initial state's heap.  
 * 
 * @author Pietro Braione
 *
 */
public class DecisionProcedureConservativeRepOk extends DecisionProcedureChainOfResponsibility {
	private final RunParameters runParameters;
	private final DecisionProcedureAlgorithms dec;
	private Supplier<State> initialStateSupplier = null;
	private Supplier<State> currentStateSupplier = null;

	/** 
	 * A cache map associating class names with the method that must
	 * be triggered when the heap is expanded by an object of
	 * that class. It's just redundand information extrapolated 
	 * from the class hierarchy.
	 */
	private final HashMap<String, Signature> conservativeRepOk = new HashMap<String, Signature>();
	
	public DecisionProcedureConservativeRepOk(DecisionProcedure next, CalculatorRewriting calc, 
	RunParameters runParameters, DecisionProcedureAlgorithms dec) {
		super(next, calc);
		this.runParameters = runParameters;
		this.dec = dec;
	}
	
	public void setInitialStateSupplier(Supplier<State> initialState) {
		this.initialStateSupplier = initialState;
	}
	
	public void setCurrentStateSupplier(Supplier<State> currentState) {
		this.currentStateSupplier = currentState;
	}
	
	@Override
	protected boolean isSatExpandsImpl(ReferenceSymbolic r, String className)
	throws DecisionException {
		final State sIni = makeInitialState();
		try {
			sIni.assumeExpands(r, className);
		} catch (InvalidTypeException | ContradictionException e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
		return runConservativeRepOks(sIni);
	}
	
	@Override
	protected boolean isSatAliasesImpl(ReferenceSymbolic r, long heapPosition, Objekt o) 
	throws DecisionException {
		final State sIni = makeInitialState();
		try {
			sIni.assumeAliases(r, heapPosition, o);
		} catch (ContradictionException e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
		return runConservativeRepOks(sIni);
	}
	
	@Override
	protected boolean isSatNullImpl(ReferenceSymbolic r)
	throws DecisionException {
		final State sIni = makeInitialState();
		try {
			sIni.assumeNull(r);
		} catch (ContradictionException e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
		return runConservativeRepOks(sIni);
	}
	
	private State makeInitialState() {
		//takes a copy of the initial state and refines it
		final State sIni =  this.initialStateSupplier.get();
		sIni.clearStack();
		final State s = this.currentStateSupplier.get();
		try {
			sIni.refine(s);
		} catch (CannotRefineException e) {
			//this should not happen
			throw new UnexpectedInternalException(e);
		}
		return sIni;
	}

	/**
	 * Runs {@link ConservativeRepOk} annotated methods 
	 * on the initial symbolic state refined based on the
	 * path condition of the current {@link State}, 
	 * and decides whether all of them are satisfied.
	 * 
	 * @return {@code true} iff for all the object in the 
	 *         heap with a {@link ConservativeRepOk} 
	 *         annotated method, the execution of the method
	 *         on the initial state returns {@code true} on 
	 *         at least one trace.
	 * @throws ClassFileNotFoundException 
	 */
	private boolean runConservativeRepOks(State sIni) {
		//runs the conservative repOk methods on all the instances in the heap 
		for (long heapPos : sIni.getHeap().keySet()) {
			final Reference r = new ReferenceConcrete(heapPos);
			final Objekt obj = sIni.getObject(r);
			if (obj.isSymbolic()) {
				final Signature sigConservativeRepOk;
				try {
					sigConservativeRepOk = findConservativeRepOk(obj.getType(), sIni);
				} catch (ClassFileNotFoundException e) {
					//this should not happen
					throw new UnexpectedInternalException(e);
				}
				if (sigConservativeRepOk == null) {
					; //wrong annotation; do nothing
					//TODO raise some exception?
				} else {
					final State sRun = sIni.clone();
					try {
						final boolean repOk = doRunRepOk(sRun, r, sigConservativeRepOk, runParameters.getConservativeRepOkDriverParameters(this.dec), true);
						if (!repOk) {
							return false; 
						}
					} catch (CannotBacktrackException |
							DecisionException | EngineStuckException | CannotManageStateException | 
							ContradictionException | FailureException | PleaseDoNativeException |
							InitializationException | NonexistingObservedVariablesException | 
							CannotBuildEngineException | InvalidClassFileFactoryClassException | 
							ClassFileNotFoundException | MethodNotFoundException | 
							IncompatibleClassFileException | ThreadStackEmptyException | 
							InvalidProgramCounterException | NoMethodReceiverException | 
							InvalidSlotException | OperandStackEmptyException exc) {
						throw new UnexpectedInternalException(exc);
					}
				}
			}
		}
		return true;
	}

	private Signature findConservativeRepOk(String className, State s) 
	throws ClassFileNotFoundException {
		Signature sigConservativeRepOk = conservativeRepOk.get(className);
		if (sigConservativeRepOk == null) {
			final ClassFile cf = s.getClassHierarchy().getClassFile(className);
			for (Signature sig : cf.getMethodSignatures()) {
				final Object[] annotations;
				try {
					annotations = cf.getMethodAvailableAnnotations(sig);
				} catch (MethodNotFoundException e) {
					//this cannot happen
					throw new UnexpectedInternalException(e);
				}
				for (Object o : annotations) {
					if (o instanceof ConservativeRepOk) {
						sigConservativeRepOk = sig;
						conservativeRepOk.put(className, sigConservativeRepOk);
						break;
					}
				}
			}
		}
		if (sigConservativeRepOk == null) {
			return null;
		} else if (Type.splitReturnValueDescriptor(sigConservativeRepOk.getDescriptor()).equals("" + Type.BOOLEAN) &&
				Type.splitParametersDescriptors(sigConservativeRepOk.getDescriptor()).length == 0) {
			return sigConservativeRepOk;
		} else {
			return null;
		}
	}
}
