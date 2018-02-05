package jbse.apps.run;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Engine;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.Runner.Actions;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.jvm.RunnerParameters;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcome;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.Frame;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Util;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Access;
import jbse.val.AccessArrayLength;
import jbse.val.AccessArrayMember;
import jbse.val.AccessField;
import jbse.val.AccessHashCode;
import jbse.val.AccessLocalVariable;
import jbse.val.AccessStatic;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureGuidance} that uses JBSE to perform concrete execution. 
 */
public final class DecisionProcedureGuidanceJBSE extends DecisionProcedureGuidance {
    /**
     * Builds the {@link DecisionProcedureGuidanceJBSE}.
     *
     * @param component the component {@link DecisionProcedure} it decorates.
     * @param calc a {@link Calculator}.
     * @param runnerParameters the {@link RunnerParameters} of the symbolic execution.
     *        The constructor modifies this object by adding the {@link Runner.Actions}s
     *        necessary to the execution.
     * @param stopSignature the {@link Signature} of a method. The guiding concrete execution 
     *        will stop at the entry of the first invocation of the method whose 
     *        signature is {@code stopSignature}, and the reached state will be used 
     *        to answer queries.
     * @throws GuidanceException if something fails during creation (and the caller
     *         is to blame).
     */
    public DecisionProcedureGuidanceJBSE(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature) 
    throws GuidanceException {
        this(component, calc, runnerParameters, stopSignature, 1);
    }

    /**
     * Builds the {@link DecisionProcedureGuidanceJBSE}.
     *
     * @param component the component {@link DecisionProcedure} it decorates.
     * @param calc a {@link Calculator}.
     * @param runnerParameters the {@link RunnerParameters} of the symbolic execution.
     *        The constructor modifies this object by adding the {@link Runner.Actions}s
     *        necessary to the execution.
     * @param stopSignature the {@link Signature} of a method. The guiding concrete execution 
     *        will stop at the entry of the {@code numberOfHits}-th invocation of the 
     *        method whose signature is {@code stopSignature}, and the reached state will be used 
     *        to answer queries.
     * @param numberOfHits an {@code int} greater or equal to one.
     * @throws GuidanceException if something fails during creation (and the caller
     *         is to blame).
     */
    public DecisionProcedureGuidanceJBSE(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
    throws GuidanceException {
        super(component, calc, new JVMJBSE(calc, runnerParameters, stopSignature, numberOfHits));
    }
    
    private static class JVMJBSE extends JVM {
        private static final String ERROR_NONCONCRETE_GUIDANCE = "Guided execution fell outside the concrete domain.";
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path.";
        
        private final Frame rootFrameConcrete;
        private final State initialStateConcrete;
        
        private final Engine engine; //used only by constructor
        private Exception catastrophicFailure; //used only by constructor (to allow Actions to report errors)
        private boolean failedConcrete; //used only by constructor (to allow Actions to report errors)

        public JVMJBSE(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            super(calc, runnerParameters, stopSignature, numberOfHits);
            this.catastrophicFailure = null;
            this.failedConcrete = false;
            
            //builds the runner actions
            final Actions a = new Actions() {
                private int hitCounter = 0;

                @Override
                public boolean atMethodPre() {
                    try {
                        final State currentState = getEngine().getCurrentState();
                        if (currentState.getCurrentMethodSignature().equals(stopSignature) && currentState.getPC() == 0) {
                            ++this.hitCounter;
                        }
                        return (this.hitCounter == numberOfHits);
                    } catch (ThreadStackEmptyException e) {
                        //this should never happen
                        JVMJBSE.this.catastrophicFailure = e;
                        return true;
                    }
                }

                @Override
                public boolean atStepPost() {
                    JVMJBSE.this.failedConcrete = JVMJBSE.this.engine.canBacktrack();
                    return JVMJBSE.this.failedConcrete;
                }

                @Override
                public boolean atTraceEnd() {
                    //trace ended before meeting the stop method
                    JVMJBSE.this.failedConcrete = true;
                    return true;
                }
                
            };
            runnerParameters.setActions(a);

            //builds the private runner
            final Runner runner;
            try {
                final RunnerBuilder b = new RunnerBuilder();
                runner = b.build(runnerParameters);
                this.engine = b.getEngine();
            } catch (CannotBuildEngineException | InitializationException | ClasspathException e) {
                //CannotBuildEngineException may happen if something goes wrong in the construction 
                //of the decision procedure
                //InitializationException happens when the method does not exist or is native
                //ClasspathException happens when the classpath does not point to a valid JRE
                throw new GuidanceException(e);
            } catch (NonexistingObservedVariablesException | DecisionException | InvalidClassFileFactoryClassException e) {
                //NonexistingObservedVariablesException should not happen since this decision procedure does not register any variable observer
                //DecisionException should not happen since it happens only when the initial path condition is contradictory
                //InvalidClassFileFactoryClassException should not happen since we use the default class file factory (javassist)
                throw new UnexpectedInternalException(e);
            }


            //runs the private runner until it hits stopSignature the right number of times
            try {
                runner.run();
            } catch (ClasspathException e) {
                throw new GuidanceException(e);
            } catch (CannotBacktrackException | EngineStuckException | CannotManageStateException | 
                     ContradictionException | FailureException | DecisionException | 
                     ThreadStackEmptyException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }

            //fails catastrophically if the case
            if (this.catastrophicFailure != null) {
                throw new UnexpectedInternalException(this.catastrophicFailure);
            }

            //fails if by some reason it fell into symbolic execution
            if (this.failedConcrete) {
                throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
            }

            //saves the current state and its current frame as the 
            //concrete initial state/frame
            this.initialStateConcrete = this.engine.getCurrentState().clone();
            try {
                this.rootFrameConcrete = this.initialStateConcrete.getCurrentFrame();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }

            //we don't need the guiding engine anymore
            try {
                this.engine.close();
            } catch (DecisionException e) {
                throw new UnexpectedInternalException(e);
            }
        }
        
        @Override
        public boolean isCurrentMethodNonStatic() throws GuidanceException {
            try {
                final ClassHierarchy hier = this.initialStateConcrete.getClassHierarchy();
                final Signature currentMethod = this.initialStateConcrete.getCurrentMethodSignature();
                return !hier.getClassFile(currentMethod.getClassName()).isMethodStatic(currentMethod);
            } catch (ThreadStackEmptyException | 
                     MethodNotFoundException | BadClassFileException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        @Override
        public String typeOfObject(MemoryPath origin) throws GuidanceException {
            final ReferenceConcrete refInConcreteState = (ReferenceConcrete) getValue(origin);
            if (this.initialStateConcrete.isNull(refInConcreteState)) {
                return null;
            }
            final Objekt objInConcreteState = this.initialStateConcrete.getObject(refInConcreteState);
            return objInConcreteState.getType();
        }

        @Override
        public boolean isNull(MemoryPath origin) throws GuidanceException {
            final ReferenceConcrete refInConcreteState = (ReferenceConcrete) getValue(origin);
            return (this.initialStateConcrete.isNull(refInConcreteState));
        }

        @Override
        public boolean areAlias(MemoryPath first, MemoryPath second) throws GuidanceException {
            final ReferenceConcrete firstInConcreteState = (ReferenceConcrete) getValue(first);
            final ReferenceConcrete secondInConcreteState = (ReferenceConcrete) getValue(second);
            return Util.areAlias(this.initialStateConcrete, firstInConcreteState, secondInConcreteState);
        }

        @Override
        public Object getValue(MemoryPath origin) throws GuidanceException {
            Value value = null;
            Objekt o = null;
            for (Access a : origin) {
                if (a instanceof AccessLocalVariable) {
                    final AccessLocalVariable al = (AccessLocalVariable) a;
                    value = this.rootFrameConcrete.getLocalVariableValue(al.variableName());
                    if (value == null) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                } else if (a instanceof AccessStatic) {
                    final AccessStatic as = (AccessStatic) a;
                    value = null;
                    o = this.initialStateConcrete.getKlass(as.className());
                    if (o == null) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                } else if (a instanceof AccessField) {
                    final AccessField af = (AccessField) a;
                    value = o.getFieldValue(af.fieldName());
                } else if (a instanceof AccessArrayLength) {
                    if (! (o instanceof Array)) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    value = ((Array) o).getLength();
                } else if (a instanceof AccessArrayMember) {
                    if (! (o instanceof Array)) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    final AccessArrayMember aa = (AccessArrayMember) a;
                    try {
                        for (AccessOutcome ao : ((Array) o).get(eval(aa.index()))) {
                            if (ao instanceof AccessOutcomeIn) {
                                final AccessOutcomeIn aoi = (AccessOutcomeIn) ao;
                                value = aoi.getValue();
                                break;
                            }
                        }
                    } catch (InvalidOperandException | InvalidTypeException e) {
                        throw new GuidanceException(e);
                    }
                } else if (a instanceof AccessHashCode) {
                    if (o == null) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    value = o.getObjektHashCode();
                }
                if (value instanceof Reference) {
                    o = this.initialStateConcrete.getObject((Reference) value);
                } else if (value != null) {
                    o = null;
                }
            }
            if (value == null) {
                throw new GuidanceException(ERROR_BAD_PATH);
            }
            return value;
        }
    }
}
