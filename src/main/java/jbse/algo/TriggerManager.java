package jbse.algo;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;
import static jbse.common.Type.VOID;
import static jbse.rules.Util.getTriggerMethodParameterObject;

import java.util.ArrayList;

import jbse.algo.exc.MissingTriggerParameterException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.exc.FrozenStateException;
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
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

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
     * @param state a {@link State}. It must not be {@code null} and must be initial.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param rootExpansion a {@link DecisionAlternative_XLOAD_GETX_Expands}
     *        for the initial expansion of the {ROOT}:this reference.
     * @throws InvalidInputException if {@code state == null || calc == null || rootExpansion == null},
     *         or if {@code state} is not in its initial phase.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty.
     * @throws MissingTriggerParameterException  if the parameter of a trigger cannot be find
     *         in {@code State}.
     * @throws NotYetImplementedException if the class where the trigger method resides
     *         is not loaded.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    public void loadTriggerFramesRoot(State state, Calculator calc, DecisionAlternative_XLOAD_GETX_Expands rootExpansion) 
    throws InvalidInputException, ThreadStackEmptyException, MissingTriggerParameterException, 
    NotYetImplementedException, FrozenStateException {
    	if (state == null || calc == null || rootExpansion == null) {
    		throw new InvalidInputException("Attempted to invoke " + getClass().getName() + ".loadTriggerFramesRoot with a null state, or calc, or rootExpansion parameter.");
    	}
    	if (state.phase() != Phase.INITIAL) {
    		throw new InvalidInputException("Attempted to invoke " + getClass().getName() + ".loadTriggerFramesRoot with a state that is not in the initial phase.");
    	}
        try {
            loadTriggerFrames(state, calc, rootExpansion, 0);
        } catch (InvalidInputException | InvalidProgramCounterException e) {
        	//this should never happen
            throw new UnexpectedInternalException(e);
        }
    }


    /**
     * Possibly loads frames on a state for triggers execution. 
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param da a {@link DecisionAlternative_XYLOAD_GETX_Loads}.  It must not be {@code null}.
     *        If it is a {@link DecisionAlternative_XYLOAD_GETX_Unresolved}
     *        and has a trigger method, a frame for it will be pushed on {@code state}. 
     *        Otherwise, {@code state} remains unchanged.
     * @param pcOffset an {@code int}, an offset for the program counter of {@code state}. Used
     *        as return offset after the execution of the trigger method.
     * @return {@code true} iff the method loads at least one trigger frame on {@code state}.
     * @throws InvalidInputException if {@code state == null || calc == null || da == null}.
     * @throws InvalidProgramCounterException when {@code pcOffset} is not a valid
     *         return offset.
     * @throws ThreadStackEmptyException if {@code state}'s thread stack is empty.
     * @throws MissingTriggerParameterException if the parameter of a trigger cannot be find
     *         in {@code State}.
     * @throws NotYetImplementedException if the class where the trigger method resides
     *         is not loaded.
     * @throws FrozenStateException if {@code state} is frozen.
     */
    public boolean loadTriggerFrames(State state, Calculator calc, DecisionAlternative_XYLOAD_GETX_Loads da, int pcOffset) 
    throws InvalidInputException, InvalidProgramCounterException, ThreadStackEmptyException, 
    MissingTriggerParameterException, NotYetImplementedException, FrozenStateException {
    	if (state == null || calc == null || da == null) {
    		throw new InvalidInputException("Attempted to invoke " + getClass().getName() + ".loadTriggerFrames with a null state, or calc, or da parameter.");
    	}
        if (!(da instanceof DecisionAlternative_XYLOAD_GETX_Unresolved)) {
            return false;
        }

        //handles triggers by creating a frame for the fresh object;
        //first, gets data
        final ArrayList<TriggerRule> rules = satisfiedTriggerRules(state, da, this.triggerRulesRepo);

        //then, pushes all the frames
        boolean retVal = false;
        for (TriggerRule rule : rules) {
            final Signature triggerSig = rule.getTriggerMethodSignature();
            if (splitReturnValueDescriptor(triggerSig.getDescriptor()).equals("" + VOID) &&
                splitParametersDescriptors(triggerSig.getDescriptor()).length <= 1) {
            	final ReferenceSymbolic originTarget;
            	if (rule instanceof TriggerRuleNull) {
            		originTarget = ((DecisionAlternative_XYLOAD_GETX_Null) da).getValueToLoad();
            	} else if (rule instanceof TriggerRuleAliases) {
            		final ReferenceConcrete refObject = new ReferenceConcrete(((DecisionAlternative_XYLOAD_GETX_Aliases) da).getObjectPosition());
            		originTarget = state.getObject(refObject).getOrigin();
            	} else { //(rule instanceof TriggerRuleExpandsTo) 
            		originTarget = ((DecisionAlternative_XYLOAD_GETX_Expands) da).getValueToLoad();
            	}
            	final Reference triggerArg = getTriggerMethodParameterObject(rule, originTarget, state);
            	if (triggerArg == null) {
            		throw new MissingTriggerParameterException("No heap object matches the parameter part in the trigger rule " + rule);
            	}
                try {
                    final ClassFile cf = state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, triggerSig.getClassName(), true);
                    state.pushFrame(calc, cf, triggerSig, false, pcOffset, triggerArg);
                    retVal = true;
                    pcOffset = 0; //the offset of the second, third... frames
                } catch (ClassFileNotFoundException | IncompatibleClassFileException | ClassFileIllFormedException | 
                         BadClassFileVersionException | RenameUnsupportedException | WrongClassNameException | 
                         ClassFileNotAccessibleException | MethodNotFoundException | MethodCodeNotFoundException | 
                         InvalidSlotException | InvalidTypeException | InvalidInputException e) {
                    //does nothing, falls through to skip the unfriendly method
                    //TODO very ugly! should we throw an exception? are we sure that they are all not internal exceptions?
                } catch (PleaseLoadClassException e) {
                    throw new NotYetImplementedException("Currently JBSE is unable to automatically load the class where a trigger implementation resides. Please load manually the class before trigger execution.");
                } catch (NullMethodReceiverException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
            } //else, just skip 
            //TODO should we manage the else case, possibly throwing an exception?
        }
        return retVal;
    }

    private ArrayList<TriggerRule> 
    satisfiedTriggerRules(State s, DecisionAlternative_XYLOAD_GETX_Loads da, TriggerRulesRepo rulesRepo) 
    throws FrozenStateException {
        //TODO replace with double dispatching
        if (da instanceof DecisionAlternative_XYLOAD_GETX_Aliases) {
            final DecisionAlternative_XYLOAD_GETX_Aliases daa = (DecisionAlternative_XYLOAD_GETX_Aliases) da;
            final ReferenceSymbolic ref = daa.getValueToLoad();
            final Objekt o = s.getObject(new ReferenceConcrete(daa.getObjectPosition()));
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
                            if (o.getOrigin().asOriginString().length() < oOther.getOrigin().asOriginString().length() && 
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
            final ClassFile classFile = dae.getClassFileOfTargetObject();
            final ArrayList<TriggerRuleExpandsTo> rules = rulesRepo.matchingTriggerRulesExpandsTo(ref);
            final ArrayList<TriggerRule> retVal = new ArrayList<>();
            for (TriggerRuleExpandsTo rule : rules) {
                if (rule.satisfies(classFile.getClassName())) {
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
