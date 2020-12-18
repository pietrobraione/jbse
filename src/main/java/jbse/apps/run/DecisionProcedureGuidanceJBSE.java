package jbse.apps.run;

import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.Signature;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
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
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Frame;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.Util;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Symbolic;
import jbse.val.SymbolicLocalVariable;
import jbse.val.SymbolicMemberArray;
import jbse.val.SymbolicMemberField;
import jbse.val.Value;
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
     * @param ctx an {@link ExecutionContext}.
     * @throws GuidanceException if something fails during creation (and the caller
     *         is to blame).
     * @throws InvalidInputException if {@code component == null}.
     */
    public DecisionProcedureGuidanceJBSE(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature) 
    throws GuidanceException, InvalidInputException {
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
     * @throws InvalidInputException if {@code component == null}.
     */
    public DecisionProcedureGuidanceJBSE(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
    throws GuidanceException, InvalidInputException {
        super(component, new JVMJBSE(calc, runnerParameters, stopSignature, numberOfHits));
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
                        if (currentState.phase() != Phase.PRE_INITIAL && currentState.getCurrentMethodSignature().equals(stopSignature) && currentState.getCurrentProgramCounter() == 0) {
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
                public boolean atPathEnd() {
                    //path ended before meeting the stop method
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
            } catch (CannotBuildEngineException | InitializationException | 
                     ClasspathException | NotYetImplementedException e) {
                //CannotBuildEngineException may happen if something goes wrong in the construction 
                //of the decision procedure
                //InitializationException happens when the method does not exist or is native
                //ClasspathException happens when the classpath does not point to a valid JRE
                //NotYetImplementedException happens when JBSE does not implement some feature that is necessary to run
                throw new GuidanceException(e);
            } catch (NonexistingObservedVariablesException | DecisionException | 
                     InvalidClassFileFactoryClassException | ContradictionException e) {
                //NonexistingObservedVariablesException should not happen since this decision procedure does not register any variable observer
                //DecisionException should not happen since it happens only when the initial path condition is contradictory
                //InvalidClassFileFactoryClassException should not happen since we use the default class file factory (javassist)
                //ContradictionException should not happen since it is only raised if we cannot assume the root class to be preloaded, which should never be the case (or not?)
                throw new UnexpectedInternalException(e);
            }


            //runs the private runner until it hits stopSignature the right number of times
            try {
                runner.run();
            } catch (ClasspathException e) {
                throw new GuidanceException(e);
            } catch (CannotBacktrackException | EngineStuckException | CannotManageStateException | 
                     ContradictionException | FailureException | DecisionException | 
                     ThreadStackEmptyException | NonexistingObservedVariablesException e) {
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
            } catch (ThreadStackEmptyException | FrozenStateException e) {
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
        public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException {
            final ReferenceConcrete refInConcreteState = (ReferenceConcrete) getValue(origin);
            if (this.initialStateConcrete.isNull(refInConcreteState)) {
                return null;
            }
            final Objekt objInConcreteState;
			try {
				objInConcreteState = this.initialStateConcrete.getObject(refInConcreteState);
			} catch (FrozenStateException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
            return objInConcreteState.getType().getClassName();
        }

        @Override
        public boolean isNull(ReferenceSymbolic origin) throws GuidanceException {
            final ReferenceConcrete refInConcreteState = (ReferenceConcrete) getValue(origin);
            return (this.initialStateConcrete.isNull(refInConcreteState));
        }

        @Override
        public boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException {
            final ReferenceConcrete firstInConcreteState = (ReferenceConcrete) getValue(first);
            final ReferenceConcrete secondInConcreteState = (ReferenceConcrete) getValue(second);
            return Util.areAlias(this.initialStateConcrete, firstInConcreteState, secondInConcreteState);
        }

        @Override
        public Object getValue(Symbolic origin) throws GuidanceException {
        	try {
        		if (origin instanceof SymbolicLocalVariable) {
        			final SymbolicLocalVariable al = (SymbolicLocalVariable) origin;
        			final Value value = this.rootFrameConcrete.getLocalVariableValue(al.getVariableName());
        			if (value == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			return value;
        		} else if (origin instanceof KlassPseudoReference) {            
        			final KlassPseudoReference as = (KlassPseudoReference) origin;
        			final Klass k = this.initialStateConcrete.getKlass(as.getClassFile());
        			if (k == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			return k;
        		} else if (origin instanceof SymbolicMemberField) {
        			final SymbolicMemberField af = (SymbolicMemberField) origin;
        			final Object o = getValue(af.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			if (o instanceof Reference) {
        				return this.initialStateConcrete.getObject((Reference) o).getFieldValue(af.getFieldName(), af.getFieldClass());
        			} else if (o instanceof Klass) {
        				return ((Klass) o).getFieldValue(af.getFieldName(), af.getFieldClass());
        			} else { //(o is a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        		} else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
        			final PrimitiveSymbolicMemberArrayLength al = (PrimitiveSymbolicMemberArrayLength) origin;
        			final Object o = getValue(al.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			if (o instanceof Reference) {
        				return ((Array) this.initialStateConcrete.getObject((Reference) o)).getLength();
        			} else { //(o is a Klass or a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        		} else if (origin instanceof SymbolicMemberArray) {
        			final SymbolicMemberArray aa = (SymbolicMemberArray) origin;
        			final Object o = getValue(aa.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			final Array a;
        			if (o instanceof Reference) {
        				a = ((Array) this.initialStateConcrete.getObject((Reference) o));
        			} else { //(o is a Klass or a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			try {
        				for (AccessOutcome ao : a.get(this.calc, eval(aa.getIndex()))) {
        					if (ao instanceof AccessOutcomeInValue) {
        						final AccessOutcomeInValue aoi = (AccessOutcomeInValue) ao;
        						return aoi.getValue();
        					}
        				}
        				throw new GuidanceException(ERROR_BAD_PATH);
        			} catch (InvalidInputException | InvalidTypeException e) {
        				throw new GuidanceException(e);
        			}
        		} else if (origin instanceof PrimitiveSymbolicHashCode) {
        			final PrimitiveSymbolicHashCode ah = (PrimitiveSymbolicHashCode) origin;
        			final Object o = getValue(ah.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			if (o instanceof Reference) {
        				return this.initialStateConcrete.getObject((Reference) o).getIdentityHashCode();
        			} else { //(o is a Klass or a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        		/*} else if (origin instanceof PrimitiveSymbolicApply) {
                	//TODO assuming the symbol has shape f(arg1, ..., argn)@historyPoint, do guided execution up to historyPoint, then concretely exec f, and get the return value 
                } else if (origin instanceof ReferenceSymbolicApply) {
                	//TODO assuming the symbol has shape f(arg1, ..., argn)@historyPoint, do guided execution up to historyPoint, then concretely exec f, and get the return value*/
        		} else {
        			throw new GuidanceException(ERROR_BAD_PATH);
        		}
        	} catch (FrozenStateException e) {
        		//this should never happen
        		throw new UnexpectedInternalException(e);
        	}
        }
        
        @Override
        protected void step(State state) throws GuidanceException {
        	//do nothing - sorry, not yet supported
        	//TODO update
        }

        @Override
        protected Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
            return this.initialStateConcrete.getCurrentMethodSignature(); //TODO update
        }

        @Override
        protected int getCurrentProgramCounter() throws ThreadStackEmptyException {
            return this.initialStateConcrete.getCurrentProgramCounter(); //TODO update
        }
    }
}
