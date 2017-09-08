package jbse.algo;

import static jbse.rules.Util.getTriggerMethodParameterObject;

import java.util.ArrayList;

import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
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
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Unresolved;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Loads;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
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
     * Possibly loads frames on an initial state for 
     * execution of triggers fired by the expansion 
     * of {ROOT}:this. 
     * 
     * @param state a {@link State}. Must be initial.
     * @param rootExpansion a {@link DecisionAlternative_XLOAD_GETX_Expands}
     *        for the initial expansion of the {ROOT}:this reference.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty.
     */
    public void loadTriggerFramesRoot(State state, DecisionAlternative_XLOAD_GETX_Expands rootExpansion) 
    throws ThreadStackEmptyException {
        try {
            loadTriggerFrames(state, rootExpansion, 0);
        } catch (InvalidProgramCounterException e) {
            throw new UnexpectedInternalException(e); //should never happen
        }
    }


	/**
	 * Possibly loads frames on a state for triggers execution. 
	 * 
	 * @param state a {@link State}.
	 * @param da a {@link DecisionAlternative_XYLOAD_GETX_Loads}. If it is a 
	 *        {@link DecisionAlternative_XYLOAD_GETX_Unresolved}
	 *        and has a trigger method, a frame for it will be pushed on {@code state}. 
	 *        Otherwise, {@code state} remains unchanged.
	 * @param pcOffset an {@code int}, an offset for the program counter of {@code state}. Used
	 *        as return offset after the execution of the trigger method.
	 * @return {@code true} iff the method loads at least one trigger frame on {@code state}.
	 * @throws InvalidProgramCounterException when {@code pcOffset} is not a valid
	 *         return offset.
	 * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty.
	 */
	public boolean loadTriggerFrames(State state, DecisionAlternative_XYLOAD_GETX_Loads da, int pcOffset) 
	throws InvalidProgramCounterException, ThreadStackEmptyException {
		if (!(da instanceof DecisionAlternative_XYLOAD_GETX_Unresolved)) {
			return false;
		}

		//handles triggers by creating a frame for the fresh object;
		//first, gets data
		final ReferenceSymbolic ref = ((DecisionAlternative_XYLOAD_GETX_Unresolved) da).getValueToLoad();
		final ArrayList<TriggerRule> rules = satisfiedTriggerRules(state, da, this.triggerRulesRepo);

		//then, pushes all the frames
		boolean retVal = false;
		for (TriggerRule rule : rules) {
			final Signature triggerSig = rule.getTriggerSignature();
			if (Type.splitReturnValueDescriptor(triggerSig.getDescriptor()).equals("" + Type.VOID) &&
				Type.splitParametersDescriptors(triggerSig.getDescriptor()).length <= 1) {
				final ReferenceConcrete triggerArg = getTriggerMethodParameterObject(rule, ref, state);
				try {
				    //TODO resolution? lookup of implementation?
					state.pushFrame(triggerSig, false, pcOffset, triggerArg);
					retVal = true;
					pcOffset = 0; //the offset of the second, third... frames
				} catch (MethodNotFoundException | MethodCodeNotFoundException | 
				         InvalidSlotException e) {
					//does nothing, falls through to skip 
					//the nonexistent/nonstatic/native method
					//TODO should we throw an exception? are we sure that they are all not internal exceptions?
				} catch (BadClassFileException | NullMethodReceiverException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
			} //TODO should we throw an exception if the signature is not ok?
		}
		return retVal;
	}
	
	private ArrayList<TriggerRule> 
	satisfiedTriggerRules(State s, DecisionAlternative_XYLOAD_GETX_Loads da, TriggerRulesRepo rulesRepo) {
		//TODO replace with double dispatching
		if (da instanceof DecisionAlternative_XYLOAD_GETX_Aliases) {
			final DecisionAlternative_XYLOAD_GETX_Aliases daa = (DecisionAlternative_XYLOAD_GETX_Aliases) da;
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
						if (o.getOrigin().toString().length() < oOther.getOrigin().toString().length() && 
							rule.satisfies(ref, oOther)) {
							continue nextRule;
						}
					}
					retVal.add(rule);
				}
			}
			return retVal;
		} else if (da instanceof DecisionAlternative_XYLOAD_GETX_Expands) {
			final DecisionAlternative_XYLOAD_GETX_Expands dae = (DecisionAlternative_XYLOAD_GETX_Expands) da;
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
		} else if (da instanceof DecisionAlternative_XYLOAD_GETX_Null) {
			final DecisionAlternative_XYLOAD_GETX_Null dan = (DecisionAlternative_XYLOAD_GETX_Null) da;
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
