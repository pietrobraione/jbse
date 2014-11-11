package jbse.apps.run;

import static jbse.jvm.Util.doRunRepOk;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedureAlgorithms;
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
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Objekt;
import jbse.mem.Reference;
import jbse.mem.State;

//TODO extend the check method to all the instances in the State's heap, making this class identical to DecisionProcedureConservativeRepOk, and eliminate this class
public final class ConcretizationCheckerRepOk {
	/**
	 * Checks that a state's heap can be 
	 * concretized. The approach is to run 
	 * repOk methods starting from the
	 * refined initial state, and then verify 
	 * that at least one execution returns 
	 * {@code true}. The current implementation
	 * checks just one object in the heap.
	 * 
	 * @param state The {@code State} to be checked.
	 *        It will not be modified.
	 * @param sIni The initial {@code State} to run the
	 *        repOk methods. It will be modified.
	 * @param params the {@link RunParameters} object used
	 *        to build the engine that created {@code state}.
	 * @param repOkMethodName a {@link String}, the name of the
	 *        method to run.
	 * @param repOkTargetObjectReference a {@link Reference} to
	 *        the target of the method invocation ("this").
	 * @param dec the {@link DecisionProcedureAlgorithms} to 
	 *        be used to run the repOk method.
	 * @return {@code true} iff executing the repOk 
	 *         method on the initial state yields {@code true}
	 *         on at least one path.
	 * @throws UnexpectedInternalException 
	 * @throws CannotRefineException 
	 */
	public static boolean check(State state, State sIni, RunParameters params, 
	String repOkMethodName, Reference repOkTargetObjectReference, 
	DecisionProcedureAlgorithms dec) throws UnexpectedInternalException, CannotRefineException {
		if (repOkMethodName == null || repOkTargetObjectReference == null) {
			return true; //does nothing
		}

		//refines the initial state
		sIni.clearStack();
		sIni.refine(state);

		//builds the signature of the repOk method
		final Objekt obj = sIni.getObject(repOkTargetObjectReference);
		if (obj == null) {
			return true; //does nothing
		}
		final String className = obj.getType();
		if (className == null) {
			return true; //does nothing
		}
		final Signature sigRepOk = new Signature(className, "()Z", repOkMethodName);

		//runs the repOk method on the object referred by repOkTargetObjectReference 
		boolean repOk = true;
		try {
			repOk = doRunRepOk(sIni, repOkTargetObjectReference, sigRepOk, params.getConcretizationDriverParameters(dec), false);
		} catch (PleaseDoNativeException | DecisionException | 
				InitializationException | InvalidClassFileFactoryClassException | 
				NonexistingObservedVariablesException |  
				CannotBacktrackException | EngineStuckException | CannotManageStateException | 
				ContradictionException | FailureException | 
				UnexpectedInternalException | CannotBuildEngineException | 
				ClassFileNotFoundException | MethodNotFoundException | 
				IncompatibleClassFileException | ThreadStackEmptyException | InvalidProgramCounterException | 
				NoMethodReceiverException | InvalidSlotException | OperandStackEmptyException exc) {
			throw new UnexpectedInternalException(exc);
		}
		return repOk;
	}
	
	//do not instantiate it!
	private ConcretizationCheckerRepOk() { }
}
