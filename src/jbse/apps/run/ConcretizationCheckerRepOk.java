package jbse.apps.run;

import static jbse.jvm.Util.doRunRepOk;

import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.PleaseDoNativeException;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NoMethodReceiverException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.CannotRefineException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.OperandStackEmptyException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;

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
	 * @throws CannotRefineException 
	 */
	public static boolean check(State state, State sIni, RunParameters params, 
	String repOkMethodName, Reference repOkTargetObjectReference, 
	DecisionProcedureAlgorithms dec) throws CannotRefineException {
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
				ClasspathException | ContradictionException | FailureException | 
				UnexpectedInternalException | CannotBuildEngineException | 
				BadClassFileException | MethodNotFoundException | 
				IncompatibleClassFileException | ThreadStackEmptyException | InvalidProgramCounterException | 
				NoMethodReceiverException | InvalidSlotException | OperandStackEmptyException exc) {
			throw new UnexpectedInternalException(exc); //TODO blame caller when necessary
		}
		return repOk;
	}
	
	//do not instantiate it!
	private ConcretizationCheckerRepOk() { }
}
