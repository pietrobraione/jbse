package jbse.algo;

import static jbse.rules.Util.getTriggerMethodParameterObject;

import java.util.ArrayList;

import jbse.algo.exc.PleaseDoNativeException;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NoMethodReceiverException;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.rules.TriggerRule;
import jbse.rules.TriggerRuleAliases;
import jbse.rules.TriggerRuleExpandsTo;
import jbse.rules.TriggerRuleNull;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternativeLoadRef;
import jbse.tree.DecisionAlternativeLoad;
import jbse.tree.DecisionAlternativeLoadRefAliases;
import jbse.tree.DecisionAlternativeLoadRefExpands;
import jbse.tree.DecisionAlternativeLoadRefNull;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

/**
 * A {@link TriggerManager} detects whether a reference resolution
 * event fires some triggers, and performs their execution.
 * 
 * @author Pietro Braione
 *
 */
public class TriggerManager {
	/** The {@link ExecutionContext}. */
	private TriggerRulesRepo triggerRulesRepo;

	public TriggerManager(TriggerRulesRepo triggerRulesRepo) {
		this.triggerRulesRepo = triggerRulesRepo;
	}

	/**
	 * (Possibly) loads frames on a state for triggers execution. 
	 * 
	 * @param s a {@link State}.
	 * @param da a {@link DecisionAlternativeLoad}. If it is a {@link DecisionAlternativeLoadRef}
	 *        and has a trigger method, a frame for it will be pushed on {@code s}. 
	 *        Otherwise, {@code s} remains unchanged.
	 * @param pcOffset an {@code int}, an offset for the program counter of {@code s}. Used
	 *        as return offset after the execution of the trigger method.
	 * @return {@code true} ifF the method leaves unchanged the state.
	 * @throws InvalidProgramCounterException when {@code pcOffset} is not a valid
	 *         return offset.
	 * @throws ThreadStackEmptyException
	 */
	public boolean runTriggers(State s, DecisionAlternativeLoad da, int pcOffset) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		if (!(da instanceof DecisionAlternativeLoadRef)) {
			return true;
		}
/* TODO handle guidance; the following code tries to manage the case of guided 
 * execution without success (was thought for a different engine architecture). 
 */
/*		else if (da instanceof DecisionAlternativeResolved && ctx.guided() && !ctx.tracking()) {
			//this translation code is dual to DecisionProcedureGuidance.resolveLoadFromLocalVariable 
			//(that was done by a tracking engine, this is done by a tracked engine)
			DecisionAlternativeResolved dar = (DecisionAlternativeResolved) da;
			Value valToPush = dar.getValueToPush();
			if (Type.isReference(valToPush)) {
				Integer posResolved = ctx.tracker.getLast();
				if (posResolved == null) {
					refUnknown = true;
					Reference refTrackedObject = ctx.tracker.getLastTracked(); //equivalently, ... = (Reference) valToPush
					Objekt o = s.getHeap().getObject(refTrackedObject);
					className = o.getType();
				} 
			} 
		} 
*/		
		//handles triggers by creating a frame for the fresh object;
		//first, gets data
		final ReferenceSymbolic ref = ((DecisionAlternativeLoadRef) da).getValueToLoad();
		final ArrayList<TriggerRule> rules = satisfiedTriggerRules(s, da, this.triggerRulesRepo);

		//then, pushes all the frames
		boolean retVal = true;
		for (TriggerRule rule : rules) {
			final Signature triggerSig = rule.getTriggerSignature();
			if (Type.splitReturnValueDescriptor(triggerSig.getDescriptor()).equals("" + Type.VOID) &&
				Type.splitParametersDescriptors(triggerSig.getDescriptor()).length <= 1) {
				final ReferenceConcrete triggerArg = getTriggerMethodParameterObject(rule, ref, s);
				try {
					s.pushFrame(triggerSig, false, true, false, pcOffset, triggerArg);
					retVal = false;
					pcOffset = 0; //the offset of the second, third... frames
				} catch (MethodNotFoundException | IncompatibleClassFileException | 
						InvalidSlotException | PleaseDoNativeException e) {
					//does nothing, falls through to skip 
					//the nonexistent/nonstatic/native method
					//TODO should we throw an exception? are we sure that they are all not internal exceptions?
				} catch (ClassFileNotFoundException | NoMethodReceiverException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			} //TODO should we throw an exception if the signature is not ok?
		}
		return retVal;
	}
	
	private ArrayList<TriggerRule> 
	satisfiedTriggerRules(State s, DecisionAlternativeLoad da, TriggerRulesRepo rulesRepo) {
		//TODO replace with double dispatching
		if (da instanceof DecisionAlternativeLoadRefAliases) {
			final DecisionAlternativeLoadRefAliases daa = (DecisionAlternativeLoadRefAliases) da;
			final ReferenceSymbolic ref = daa.getValueToLoad();
			final Objekt o = s.getObject(new ReferenceConcrete(daa.getAliasPosition()));
			final ArrayList<TriggerRuleAliases> rulesNonMax = rulesRepo.matchingTriggerRulesAliasesNonMax(ref);
			final ArrayList<TriggerRuleAliases> rulesMax = rulesRepo.matchingTriggerRulesAliasesMax(ref);
			final ArrayList<TriggerRule> retVal = new ArrayList<>();
			for (TriggerRuleAliases rule : rulesNonMax) {
				if (rule.satisfies(ref, o)) {
					retVal.add(rule);
				}
			}
nextRule:
			for (TriggerRuleAliases rule : rulesMax) {
				if (rule.satisfies(ref, o)) {
					for (Objekt oOther : s.objectsSymbolic()) {
						if (o.getOrigin().length() < oOther.getOrigin().length() && 
							rule.satisfies(ref, oOther)) {
							continue nextRule;
						}
					}
					retVal.add(rule);
				}
			}
			return retVal;
		} else if (da instanceof DecisionAlternativeLoadRefExpands) {
			final DecisionAlternativeLoadRefExpands dae = (DecisionAlternativeLoadRefExpands) da;
			final ReferenceSymbolic ref = dae.getValueToLoad();
			final String className = dae.getClassNameOfTargetObject();
			final ArrayList<TriggerRuleExpandsTo> rules = rulesRepo.matchingTriggerRulesExpandsTo(ref);
			final ArrayList<TriggerRule> retVal = new ArrayList<>();
			for (TriggerRuleExpandsTo rule : rules) {
				if (rule.satisfies(className)) {
					retVal.add(rule);
				}
			}
			return retVal;
		} else if (da instanceof DecisionAlternativeLoadRefNull) {
			final DecisionAlternativeLoadRefNull dan = (DecisionAlternativeLoadRefNull) da;
			final ReferenceSymbolic ref = dan.getValueToLoad();
			final ArrayList<TriggerRuleNull> rules = rulesRepo.matchingTriggerRulesNull(ref);
			final ArrayList<TriggerRule> retVal = new ArrayList<>();
			for (TriggerRuleNull rule : rules) {
				retVal.add(rule);
			}
			return retVal;
		} else { //da instanceof DecisionAlternativeLoadResolved
			return new ArrayList<>();
		}
	}
}
