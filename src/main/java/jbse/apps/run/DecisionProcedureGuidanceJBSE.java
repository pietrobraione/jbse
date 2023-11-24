package jbse.apps.run;

import static jbse.bc.Opcodes.OP_INVOKEDYNAMIC;
import static jbse.bc.Opcodes.OP_INVOKEHANDLE;
import static jbse.bc.Opcodes.OP_INVOKEINTERFACE;
import static jbse.bc.Opcodes.OP_INVOKESPECIAL;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;
import static jbse.bc.Opcodes.OP_INVOKEVIRTUAL;
import static jbse.bc.Signatures.JAVA_HASHMAP;
import static jbse.bc.Signatures.JAVA_LINKEDHASHMAP_CONTAINSVALUE;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSKEY;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSVALUE;
import static jbse.bc.Signatures.JAVA_MAP_GET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;

import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.SnippetFactory;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
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
import jbse.mem.Array.AccessOutcomeInInitialArray;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Instance_JAVA_CLASSLOADER;
import jbse.mem.Instance_JAVA_THREAD;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.State.Phase;
import jbse.mem.Variable;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.DefaultValue;
import jbse.val.Expression;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;
import jbse.val.NarrowingConversion;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicLocalVariable;
import jbse.val.PrimitiveSymbolicMemberArray;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.PrimitiveSymbolicMemberField;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.ReferenceSymbolicLocalVariable;
import jbse.val.ReferenceSymbolicMemberArray;
import jbse.val.ReferenceSymbolicMemberField;
import jbse.val.ReferenceSymbolicMemberMapKey;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.SymbolicApply;
import jbse.val.SymbolicLocalVariable;
import jbse.val.SymbolicMemberArray;
import jbse.val.SymbolicMemberField;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.ValueVisitor;
import jbse.val.WideningConversion;
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
        private static final String ERROR_NONCONCRETE_GUIDANCE = "Guided execution fell outside the concrete domain";
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path";
        private static final String ERROR_BAD_HISTORY_POINT = "No (concrete or symbolic) state was previously stored with following history point: ";
        private static final String ERROR_GUIDANCE_MISALIGNMENT = "The concrete and the symbolic execution are no longer aligned";
        private static final String ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE = "An object in the symbolic state has no corresponding object in the concrete state, and it is not possible to replicate it";
        private static final String ERROR_UNEXPECTED_ARRAY_OUTCOME = "Met an unexpected ArrayOutcome when reading the content of an array";
        private static final String ERROR_UNEXPECTED_OBJECT = "Met an unexpected object from the heap (neither an instance nor an array)";
        private static final String ERROR_DEFAULT_VALUE_DETECTED = "Met an unexpected value of class DefaultValue";
        private static final String ERROR_CLONE_OBJECT_NOT_SIMPLE = "Tried to clone an object in a concrete state that is not simple";
        private static final String ERROR_UIF_DURING_MISALIGNMENT = "An uninterpreted method was met when symbolic execution and concrete execution were not aligned (e.g., possible invocation of uninterpreted methods by map model code)";
        private static final String ERROR_BAD_ORIGIN_AT_CURRENT_HISTORY_POINT = "A query was produced over a symbolic value that has same history point as that of the current bytecode execution, but this symbolic value is not one of the allowed ones.";
        
        /** The {@link Engine} used to perform concrete (guiding) execution. */
        private final Engine engine;
        
        /** 
         * The {@link ClassFile} for {@code java.util.HashMap}, in the
         * concrete {@link Engine}.
         */
        private final ClassFile cfJAVA_HASHMAP;
        
        /**
         * Maps a history point of a <em>symbolic</em> computation with the corresponding
         * concrete state.
         */
        private final HashMap<HistoryPoint, State> statesConcrete = new HashMap<>();

        /**
         * Maps a history point of a <em>symbolic</em> computation with the corresponding
         * symbolic state.
         */
        private final HashMap<HistoryPoint, State> statesSymbolic = new HashMap<>();
        
        /**
         * Caches all the symbols to values conversions.
         */
        private final HashMap<Symbolic, Value> symbolValueConversionCache = new HashMap<>();
        
        /** 
         * The symbolic state before the current execution step. It is not
         * a clone, so it is updated and becomes the post execution step
         * after the symbolic engine is stepped. So do not use it in 
         * #postStep(State)!
         */
        private State preSymbolicState = null;
        
        /** 
         * Stores if the symbolic state before the current execution step
         * is at an invoke* bytecode.
         */
        private boolean preSymbolicStateAtInvoke;
        
        /** 
         * Stores the stack size of the symbolic state before the current 
         * execution step.
         */
        private long preSymbolicStateStackSize;
        
        private UnexpectedInternalException excUnexpected = null; //used only by runners actions to report errors
        private GuidanceException excGuidance = null;             //used only by runners actions to report errors
        
        public JVMJBSE(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            super(calc, runnerParameters, stopSignature, numberOfHits);
            
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
                    	JVMJBSE.this.excUnexpected = new UnexpectedInternalException(e);
                        return true;
                    }
                }

                @Override
                public boolean atStepPost() {
                	if (getEngine().canBacktrack()) {
                		JVMJBSE.this.excGuidance = new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
                		return true;
                	}
                    return super.atStepPost();
                }

                @Override
                public boolean atPathEnd() {
                    //path ended before meeting the stop method
                	JVMJBSE.this.excGuidance = new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
                    return true;
                }
            };
            
            final RunnerParameters rp = this.runnerParameters.clone();
            rp.setActions(a);

            //builds the private runner
            final Runner runner;
            try {
                final RunnerBuilder b = new RunnerBuilder();
                runner = b.build(rp);
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

            //fails if the case
            if (this.excUnexpected != null) {
                throw this.excUnexpected;
            } else if (this.excGuidance != null) {
                throw this.excGuidance;
            }

            //saves the current state and its current frame as the 
            //concrete initial state/frame
            this.engine = runner.getEngine();
            final State initialState = this.engine.getCurrentState().clone();
            this.statesConcrete.put(initialState.getHistoryPoint().startingInitial(), initialState);
            //the corresponding symbolic state is saved at the first invocation
            //of preStep

			final ClassHierarchy hier = initialState.getClassHierarchy();
			try {
				this.cfJAVA_HASHMAP = hier.loadCreateClass(JAVA_HASHMAP);
			} catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException |
			         ClassFileNotAccessibleException | IncompatibleClassFileException | BadClassFileVersionException |
			         RenameUnsupportedException | WrongClassNameException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
			}
        }
        
        @Override
        public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException, ImpureMethodException {
        	if (isNull(origin)) {
        		return null;
        	}
            final ReferenceConcrete referenceStateConcrete = (ReferenceConcrete) getValue(origin);
            final HeapObjekt objectStateConcrete = getObject(referenceStateConcrete, origin.historyPoint());
            return objectStateConcrete.getType().getClassName();
        }

        @Override
        public boolean isNull(ReferenceSymbolic origin) throws GuidanceException, ImpureMethodException {
            final ReferenceConcrete referenceStateConcrete = (ReferenceConcrete) getValue(origin);
            return referenceStateConcrete.isNull();
        }

        @Override
        public boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException, ImpureMethodException {
            final ReferenceConcrete referenceFirstStateConcrete = (ReferenceConcrete) getValue(first);
            final ReferenceConcrete referenceSecondStateConcrete = (ReferenceConcrete) getValue(second);
            return (referenceFirstStateConcrete.equals(referenceSecondStateConcrete));
        }
        
        @Override
        public Value getValue(Symbolic origin) throws GuidanceException, ImpureMethodException {
        	//if the value is cached, return it
        	if (this.symbolValueConversionCache.containsKey(origin)) {
        		return this.symbolValueConversionCache.get(origin);
        	}
        	
        	//gets the concrete state - additionally, if the origin was created at the current
        	//execution step, registers the current concrete/symbolic state associating them 
        	//to the origin's history point
        	final State stateConcrete;
        	if (this.statesConcrete.containsKey(origin.historyPoint())) {
        		//Note that this must be the first check due to a crazy initialization race
        		//of symbolic states during symbolic execution.
        		stateConcrete = this.statesConcrete.get(origin.historyPoint());
        	} else if (origin.historyPoint().equals(this.preSymbolicState.getHistoryPoint())) {
        		//the origin was created during the execution of the current bytecode,
        		//a thing that may happen in two different cases that are illustrated below;
        		//In both cases we need to register the current concrete/symbolic state and
        		//associate them to the history point of the origin
        		if (origin instanceof ReferenceSymbolicApply) {
        			//An invoke* bytecode is not executing the method, and instead is 
            		//returning a ReferenceSymbolicApply: This symbolic reference is then 
            		//pushed on top of the stack, and it must be therefore resolved immediately.
                	//In this case, if the concrete execution was aligned before the current step, 
            		//then the current concrete state is inside the invoke* method frame: We therefore 
            		//need to realign the concrete execution by stepping out the invoked method, 
            		//and register the obtained concrete state that has the returned value on the
            		//top of the stack so that the subsequent resolution query on the ReferenceSymbolicApply
            		//can be answered. If the concrete execution was not aligned before the current step,
            		//we are in a very dangerous case, e.g., we are combining symbolic maps and 
            		//uninterpreted methods: when a map method is executed this causes misalignment, 
            		//and if during the execution of the map method an uninterpreted method is met
            		//that returns a ReferenceSymbolicApply, we fall in this unpleasant situation.
        			if (this.aligned) {
	        			returnEngineFromMethod(this.preSymbolicState.getStackSize());
	        			this.statesConcrete.put(origin.historyPoint(), this.engine.getCurrentState());
	        			this.statesSymbolic.put(origin.historyPoint(), this.preSymbolicState);
	        			stateConcrete = this.statesConcrete.get(origin.historyPoint());
        			} else {
        				throw new GuidanceException(ERROR_UIF_DURING_MISALIGNMENT);
        			}
        		} else if (origin instanceof PrimitiveSymbolicApply) {
            		//The meta-level invocation of a map model method refineOnKeyAndBranch,
            		//refineOnKeyCombinationsAndBranch, or refineOnValueAndBranch creates a
            		//PrimitiveSymbolicApply for Map.containsKey, Map.containsValue, or
            		//LinkedHashMap.containsValue, and this is immediately used to decide
            		//refinement of an initial map.
            		//In this case, we need to query the concrete map by executing
            		//(with a suitable snippet) the Map.containsKey, Map.containsValue, or
            		//LinkedHashMap.containsValue methods to put the answer on top of the
            		//operand stack, a thing that will be done later. What we need to do now
        			//is to register the current concrete state as it contains the current
        			//concrete key/value by which refinement is decided.
        			final PrimitiveSymbolicApply originApply = (PrimitiveSymbolicApply) origin;
        			final String operator = originApply.getOperator();
        			if ((operator.equals(JAVA_LINKEDHASHMAP_CONTAINSVALUE.toString()) ||
        			operator.equals(JAVA_MAP_CONTAINSKEY.toString()) ||
        			operator.equals(JAVA_MAP_CONTAINSVALUE.toString()))) {
	        			this.statesConcrete.put(origin.historyPoint(), this.engine.getCurrentState());
	        			this.statesSymbolic.put(origin.historyPoint(), this.preSymbolicState);
	        			stateConcrete = this.statesConcrete.get(origin.historyPoint());
        			} else {
        				//no other kind of origin should be produced at the same history point
        				//of the bytecode execution
        				throw new UnexpectedInternalException(ERROR_BAD_ORIGIN_AT_CURRENT_HISTORY_POINT);
        			}
    			} else {
    				//no other kind of origin should be produced at the same history point
    				//of the bytecode execution
    				throw new UnexpectedInternalException(ERROR_BAD_ORIGIN_AT_CURRENT_HISTORY_POINT);
        		}
        	} else {
				throw new UnexpectedInternalException(ERROR_BAD_HISTORY_POINT + origin.historyPoint().toString());
			}
        	
        	//analyzes origin and returns the corresponding Value
        	try {
        		if (origin instanceof SymbolicLocalVariable) {
        			final SymbolicLocalVariable al = (SymbolicLocalVariable) origin;
        			final Value value = stateConcrete.getLocalVariableValue(al.getVariableName());
        			if (value == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			this.symbolValueConversionCache.put(origin, value); //caches
        			return value;
        		} else if (origin instanceof KlassPseudoReference) {            
        			final KlassPseudoReference as = (KlassPseudoReference) origin;
        			final ClassFile cfStateSymbolic = as.getClassFile();
            		final ClassFile cfStateConcrete = stateConcrete.getClassHierarchy().getClassFileClassArray(this.preSymbolicState.getClassHierarchy().getInitiatingLoader(cfStateSymbolic), cfStateSymbolic.getClassName());
        			final ReferenceSymbolic k = stateConcrete.getKlass(cfStateConcrete).getOrigin();
        			if (k == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			this.symbolValueConversionCache.put(origin, k); //caches
        			return k;
        		} else if (origin instanceof SymbolicMemberField) {
        			final SymbolicMemberField originField = (SymbolicMemberField) origin;
        			final ReferenceSymbolic containerStateSymbolic = originField.getContainer();
        			final Value valueContainerStateConcrete = getValue(containerStateSymbolic);
        			if (valueContainerStateConcrete == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			} else if (valueContainerStateConcrete instanceof Reference) {
        				//we need to treat specially the field initialMap of 
        				//all the map model
        				final Reference containerStateConcrete = (Reference) valueContainerStateConcrete;
        				final Objekt objContainerStateConcrete = stateConcrete.getObject(containerStateConcrete);
        				final Value fieldValueStateConcrete = objContainerStateConcrete.getFieldValue(originField.getFieldName(), originField.getFieldClass());
        				if (objContainerStateConcrete.getType().isSubclass(this.cfJAVA_HASHMAP) &&
        				originField.getFieldName().equals("initialMap")) {
        					//All concrete maps have the initialMap field set
        					//to null, but all the symbolic maps must initialize
        					//initialMap to a fresh object (this is done in the 
        					//initSymbolic methods). Therefore, we must
        					//set the initialMap field of the concrete map to 
        					//a fresh map, otherwise this decision procedure 
        					//will return that initialMap is resolved to null.
        					//The initialMap must be a clone of the container map.
        					//This is not enough: Since it must be possible to use
        					//the produced reference with *all* the concrete states, 
        					//we need to implant, at exactly the same heap positions, 
        					//the created initialMap in *all* the stored concrete states, 
        					//i.e., in all this.statesConcrete, and in this.engine.currentState.
        					//We must start with the most recent (with the biggest heap)
        					//state, that happens to be this.engine.currentState.
        					
        					//gets the concrete state of the initial map; this should be
        					//this.statesConcrete.get(containerStateSymbolic.historyPoint())
        					//but since a ReferenceSymbolic with origin container.initialMap
        					//has same history point as the ReferenceSymbolic with origin 
        					//container, this is the same as stateConcrete 
        					final State stateConcreteInitialMap = stateConcrete;
        					
        					//sets this.engine.currentState
        					final ReferenceConcrete refInitialMap = (ReferenceConcrete) cloneDeep(containerStateConcrete, stateConcreteInitialMap, this.engine.getCurrentState());
                			this.engine.getCurrentState().getObject(containerStateConcrete).setFieldValue(originField.getFieldName(), originField.getFieldClass(), refInitialMap);
                			
                			//sets the states not yet processed in this.statesConcrete
                			for (State currentStateConcrete : this.statesConcrete.values()) {
                				final Instance theMap = (Instance) currentStateConcrete.getObject(containerStateConcrete);
                				if (currentStateConcrete.isNull((Reference) theMap.getFieldValue(originField.getFieldName(), originField.getFieldClass()))) {
                					implantDeep(refInitialMap, this.engine.getCurrentState(), currentStateConcrete);
                					theMap.setFieldValue(originField.getFieldName(), originField.getFieldClass(), refInitialMap);
                				}
                			}
        					
                			this.symbolValueConversionCache.put(origin, refInitialMap); //caches
        					return refInitialMap;
        				} else {
                			this.symbolValueConversionCache.put(origin, fieldValueStateConcrete); //caches
        					return fieldValueStateConcrete;
        				}
        			} else if (valueContainerStateConcrete instanceof Klass) {
        				final Value retVal = ((Klass) valueContainerStateConcrete).getFieldValue(originField.getFieldName(), originField.getFieldClass());
            			this.symbolValueConversionCache.put(origin, retVal); //caches
    					return retVal;
        			} else { //(o is a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        		} else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
        			final PrimitiveSymbolicMemberArrayLength al = (PrimitiveSymbolicMemberArrayLength) origin;
        			final Object o = getValue(al.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			} else if (o instanceof Reference) {
        				final Primitive retVal = ((Array) stateConcrete.getObject((Reference) o)).getLength();
            			this.symbolValueConversionCache.put(origin, retVal); //caches
    					return retVal;
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
        				a = ((Array) stateConcrete.getObject((Reference) o));
        			} else { //(o is a Klass or a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			try {
        				for (AccessOutcome ao : a.get(this.calc, eval(aa.getIndex()))) {
        					if (ao instanceof AccessOutcomeInValue) {
        						final AccessOutcomeInValue aoi = (AccessOutcomeInValue) ao;
        						final Value retVal = aoi.getValue();
                    			this.symbolValueConversionCache.put(origin, retVal); //caches
            					return retVal;
        					}
        				}
        				throw new GuidanceException(ERROR_BAD_PATH);
        			} catch (InvalidInputException | InvalidTypeException e) {
        				throw new GuidanceException(e);
        			}
				} else if (origin instanceof ReferenceSymbolicMemberMapKey) {
        			final ReferenceSymbolicMemberMapKey mk = (ReferenceSymbolicMemberMapKey) origin;

        			//gets a reference to the map
        			final ReferenceSymbolic originMap = mk.getContainer();
        			final ReferenceConcrete refMapStateConcrete = (ReferenceConcrete) getValue(originMap);
        			
        			//gets the symbolic state where the key is stored
        			final State stateSymbolic = this.statesSymbolic.get(mk.valueHistoryPoint()); //TODO are we sure that the symbolic states with valueHistoryPoints are saved???
        			if (stateSymbolic == null) {
        				throw new GuidanceException(ERROR_BAD_HISTORY_POINT + mk.valueHistoryPoint().toString());
        			}
        			
        			//analyzes the value
        			final Reference value = mk.getAssociatedValue(); 
        			if (value instanceof ReferenceSymbolicMemberMapValue) {
        				//hard situation: we have a symbolic key/value pair, any could match:
        				//since this situation emerges when an iterator is opened on the symbolic 
        				//map, we open a corresponding iterator on the concrete map, we position
        				//it at the same position of the iterator on the symbolic map, and we 
        				//associate the pairs at the same positions
        				return null; //TODO
        			} else {
    					//builds a clone of the concrete state
    					final State stateConcreteClone = stateConcrete.clone();
    					
            			//ports the value in the cloned concrete state
        				final ReferenceConcrete refValueStateConcrete = portToStateConcrete(value, stateSymbolic, stateConcreteClone);
        				
        				//TODO manage the situation with the same iterator trick as for the symbolic key/value pairs
        				return null;
        			}
				} else if (origin instanceof ReferenceSymbolicMemberMapValue) {
        			final ReferenceSymbolicMemberMapValue mv = (ReferenceSymbolicMemberMapValue) origin;

        			//gets a reference to the map in the concrete state
        			final ReferenceSymbolic originMap = mv.getContainer();
        			final ReferenceConcrete refMapStateConcrete = (ReferenceConcrete) getValue(originMap);
        			
        			//gets the symbolic state where the value is stored
        			final State stateSymbolic = this.statesSymbolic.get(mv.keyHistoryPoint()); //TODO are we sure that the symbolic states with keyHistoryPoints are saved???
        			if (stateSymbolic == null) {
        				throw new GuidanceException(ERROR_BAD_HISTORY_POINT + mv.keyHistoryPoint().toString());
        			}
        			
        			//analyzes the key
        			final Reference key = mv.getAssociatedKey(); 
        			if (key instanceof ReferenceSymbolicMemberMapKey) {
        				//hard situation: we have a symbolic key/value pair, any could match:
        				//since this situation emerges when an iterator is opened on the symbolic 
        				//map, we open a corresponding iterator on the concrete map, we position
        				//it at the same position of the iterator on the symbolic map, and we 
        				//associate the pairs at the same positions
        				return null; //TODO
        			} else {
    					//builds a clone of the concrete state
    					final State stateConcreteClone = stateConcrete.clone();
    					
            			//ports the key in the cloned concrete state
        				final ReferenceConcrete refKeyStateConcrete = portToStateConcrete(key, stateSymbolic, stateConcreteClone);

        				//gets the value by running a snippet of the Map.get() method on the clone 
        				//of the concrete state
        				final Snippet snippet = stateConcreteClone.snippetFactoryNoWrap()
        					//parameters
        					.addArg(refMapStateConcrete)
        					.addArg(refKeyStateConcrete)
        					//pushes everything
        					.op_aload((byte) 0)
        					.op_aload((byte) 1)
        					//invokes
        					.op_invokeinterface(JAVA_MAP_GET)
        					//returns the result
        					.op_areturn()
        					.mk();
        				stateConcreteClone.pushSnippetFrameNoWrap(snippet, 0);
        				final State stateConcretePostGet = runSnippet(stateConcreteClone);
        				final Value retVal = stateConcretePostGet.topOperand();
        				this.symbolValueConversionCache.put(origin, retVal); //caches
        				return retVal;
        			}
        		} else if (origin instanceof PrimitiveSymbolicHashCode) {
        			final PrimitiveSymbolicHashCode ah = (PrimitiveSymbolicHashCode) origin;
        			final Object o = getValue(ah.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			if (o instanceof Reference) {
        				final Primitive retVal = stateConcrete.getObject((Reference) o).getIdentityHashCode();
            			this.symbolValueConversionCache.put(origin, retVal); //caches
    					return retVal;
        			} else { //(o is a Klass or a primitive)
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        		} else if (origin instanceof SymbolicApply) {
        			final State stateConcreteLocal;
        			if (origin instanceof PrimitiveSymbolicApply) {
        				final PrimitiveSymbolicApply originApply = (PrimitiveSymbolicApply) origin;
        				final String operator = originApply.getOperator();
        				if (operator.equals(JAVA_LINKEDHASHMAP_CONTAINSVALUE.toString()) ||
        				operator.equals(JAVA_MAP_CONTAINSKEY.toString()) ||
        				operator.equals(JAVA_MAP_CONTAINSVALUE.toString())) {
                			//gets the symbolic state where map/arg are stored
                			final State stateSymbolic = this.statesSymbolic.get(origin.historyPoint());                			
							
        					//gets the map reference in the concrete state (the map has always an origin)
            				final Value mapStateSymbolic = originApply.getArgs()[0];
							final HeapObjekt mapObjectStateSymbolic = stateSymbolic.getObject((Reference) mapStateSymbolic);
							final ReferenceConcrete mapStateConcrete = (ReferenceConcrete) getValue(mapObjectStateSymbolic.getOrigin());
							
        					//clones the concrete state
        					final State stateConcreteClone = stateConcrete.clone();
        					
                			//ports the argument of contains* invocation in the concrete state clone (could be a concrete/simple object)
        					final Reference argStateSymbolic = (Reference) originApply.getArgs()[1];
							final ReferenceConcrete argStateConcrete = portToStateConcrete(argStateSymbolic, stateSymbolic, stateConcreteClone);
        					
        					//puts the value on top of the operand stack by running a snippet of the 
        					//method indicated by origin on stackConcrete
        					final SnippetFactory snippetFactory = stateConcreteClone.snippetFactoryNoWrap()
        						//parameters
        						.addArg(mapStateConcrete)
        						.addArg(argStateConcrete)
        						//pushes everything
        						.op_aload((byte) 0)
        						.op_aload((byte) 1);
        					//invokes
            				if ((operator.equals(JAVA_LINKEDHASHMAP_CONTAINSVALUE.toString()))) {
            					snippetFactory.op_invokevirtual(JAVA_LINKEDHASHMAP_CONTAINSVALUE);
            				} else if (operator.equals(JAVA_MAP_CONTAINSKEY.toString())) {
            					snippetFactory.op_invokeinterface(JAVA_MAP_CONTAINSKEY);
            				} else if (operator.equals(JAVA_MAP_CONTAINSVALUE.toString())) {
            					snippetFactory.op_invokeinterface(JAVA_MAP_CONTAINSVALUE);
            				}
        					//returns the result
            				snippetFactory.op_ireturn();
        					final Snippet snippet = snippetFactory.mk();
        					
        					//runs the snippet and saves the final state
        					stateConcreteClone.pushSnippetFrameNoWrap(snippet, 0);
        					stateConcreteLocal = runSnippet(stateConcreteClone);
        				} else {
        					stateConcreteLocal = stateConcrete;
        				}
        			} else {
        				stateConcreteLocal = stateConcrete;
        			}
        			
                	//the value is on top of the stack
        			final Value retVal = stateConcreteLocal.topOperand();
        			this.symbolValueConversionCache.put(origin, retVal); //caches
					return retVal;
        		} else {
        			throw new GuidanceException(ERROR_BAD_PATH);
        		}
        	} catch (HeapMemoryExhaustedException e) {
        		throw new GuidanceException(e);
        	} catch (ThreadStackEmptyException | InvalidInputException | InvalidOperandException | InvalidTypeException | 
        	InvalidNumberOfOperandsException | InvalidProgramCounterException e) {
        		//this should never happen
        		throw new UnexpectedInternalException(e);
        	}
        }
        
        /**
         * Maps a concrete reference of a symbolic state with a corresponding 
         * concrete reference in the concrete state.
         */
        private HashMap<ReferenceConcrete, ReferenceConcrete> mapRefStateSourceToStateDest; //used only in the context of a call to portToStateConcrete

        /**
         * Ports an object from a symbolic state to a concrete state. This
         * might mean either finding a corresponding object in the concrete
         * state (e.g., if {@code refStateSymbolic} is a symbolic reference,
         * if it refers an object with class {@code java.lang.Class}...), or 
         * by building a clone (deep copy) of the object in the concrete state. 
         * 
         * @param refStateSymbolic a {@link Reference} to the object
         *        in {@code stateSymbolic}.
         * @param stateSymbolic a {@link State}, the one containing the object.
         * @param stateConcrete a {@link State}, the one where the object must
         *        be ported.
         * @return a {@link ReferenceConcrete} to the ported object in 
         *         {@code stateConcrete}.
         * @throws GuidanceException
         * @throws InvalidInputException
         * @throws HeapMemoryExhaustedException
         * @throws ImpureMethodException
         * @throws NoSuchElementException
         * @throws InvalidOperandException
         * @throws InvalidTypeException
         */
        private ReferenceConcrete portToStateConcrete(Reference refStateSymbolic, State stateSymbolic, State stateConcrete) 
        throws GuidanceException, ImpureMethodException, NoSuchElementException, InvalidInputException, HeapMemoryExhaustedException, 
        InvalidOperandException, InvalidTypeException {
        	this.mapRefStateSourceToStateDest = new HashMap<>();
        	return doPortToStateConcrete(refStateSymbolic, stateSymbolic, stateConcrete);
        }
        
        private ReferenceConcrete doPortToStateConcrete(Reference refStateSymbolic, State stateSymbolic, State stateConcrete) 
        throws GuidanceException, InvalidInputException, HeapMemoryExhaustedException, ImpureMethodException, NoSuchElementException, InvalidOperandException, InvalidTypeException {
    		if (stateSymbolic.isNull(refStateSymbolic)) {
        		//if refStateSymbolic is null, return null
    			return Null.getInstance();
    		} else if (refStateSymbolic instanceof ReferenceSymbolic) {
        		//easy part: we exploit getValue
        		return (ReferenceConcrete) getValue((ReferenceSymbolic) refStateSymbolic);
        	} else { //refStateSymbolic instanceof ReferenceConcrete
        		//hard part: key is a concrete reference, so we must port the key in
        		//the concrete state and return a reference to it
        		
        		final ReferenceConcrete refcStateSymbolic = (ReferenceConcrete) refStateSymbolic;

        		//if the object is already available, return it
        		if (this.mapRefStateSourceToStateDest.containsKey(refStateSymbolic)) {
        			return this.mapRefStateSourceToStateDest.get(refStateSymbolic);
        		}

        		//gets the object in the state symbolic and analyzes its class
        		final HeapObjekt objectStateSymbolic = stateSymbolic.getObject(refStateSymbolic);
        		if (objectStateSymbolic instanceof Instance_JAVA_CLASS) {
        			//finds the classfile of the represented class in the concrete state
        			final ClassFile cfObjectStateSymbolic = ((Instance_JAVA_CLASS) objectStateSymbolic).representedClass();
        			final ClassFile cfObjectStateConcrete = stateConcrete.getClassHierarchy().getClassFileClassArray(stateSymbolic.getClassHierarchy().getInitiatingLoader(cfObjectStateSymbolic), cfObjectStateSymbolic.getClassName());
        			if (cfObjectStateConcrete == null) {
        				throw new GuidanceException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//gets the java.lang.Class instance in the concrete state
        			stateConcrete.ensureInstance_JAVA_CLASS(this.calc, cfObjectStateConcrete);
        			final ReferenceConcrete retVal = stateConcrete.referenceToInstance_JAVA_CLASS(cfObjectStateConcrete);
        			if (retVal == null) {
        				throw new UnexpectedInternalException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//caches
        			this.mapRefStateSourceToStateDest.put(refcStateSymbolic, retVal);

        			return retVal;
        		} else if (objectStateSymbolic instanceof Instance_JAVA_CLASSLOADER) {
        			//finds the corresponding object in the concrete state
        			final int classLoaderIdentifier = ((Instance_JAVA_CLASSLOADER) objectStateSymbolic).classLoaderIdentifier();
        			final ReferenceConcrete retVal = stateConcrete.referenceToInstance_JAVA_CLASSLOADER(classLoaderIdentifier);
        			if (retVal == null) {
        				throw new GuidanceException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//caches
        			this.mapRefStateSourceToStateDest.put(refcStateSymbolic, retVal);

        			return retVal;
        		} else if (objectStateSymbolic instanceof Instance_JAVA_THREAD) {
        			//currently there is only one thread in JBSE
        			final ReferenceConcrete retVal = stateConcrete.getMainThread();

        			//caches
        			this.mapRefStateSourceToStateDest.put(refcStateSymbolic, retVal);

        			return retVal;
        		} else if (objectStateSymbolic instanceof Instance) {
        			//finds the classfile of the instance in the concrete state
        			final ClassFile cfObjectStateSymbolic = objectStateSymbolic.getType();
        			final ClassFile cfObjectStateConcrete = stateConcrete.getClassHierarchy().getClassFileClassArray(stateSymbolic.getClassHierarchy().getInitiatingLoader(cfObjectStateSymbolic), cfObjectStateSymbolic.getClassName());
        			if (cfObjectStateConcrete == null) {
        				throw new GuidanceException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//creates the object
        			final ReferenceConcrete retVal = stateConcrete.createInstance(this.calc, cfObjectStateConcrete);

        			//caches
        			this.mapRefStateSourceToStateDest.put(refcStateSymbolic, retVal);

        			//sets all the fields in the concrete instance
        			final Instance instanceStateSymbolic = (Instance) objectStateSymbolic;
        			final Instance instanceStateConcrete = (Instance) stateConcrete.getObject(retVal);
        			for (Map.Entry<Signature, Variable> entry : instanceStateConcrete.fields().entrySet()) {
        				final Value value = instanceStateSymbolic.getFieldValue(entry.getKey());
        				final Value valueStateConcrete;
        				if (value instanceof Reference) {
        					valueStateConcrete = doPortToStateConcrete((Reference) value, stateSymbolic, stateConcrete);
        				} else if (value instanceof Primitive) {
        					valueStateConcrete = eval((Primitive) value);
        				} else { //value instanceof DefaultValue
        					//this should never happen
        					throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
        				}
        				entry.getValue().setValue(valueStateConcrete);
        			}

        			return retVal;
        		} else if (objectStateSymbolic instanceof Array) {
        			//finds the classfile of the array in the concrete state
        			final ClassFile cfObjectStateSymbolic = objectStateSymbolic.getType();
        			final ClassFile cfObjectStateConcrete = stateConcrete.getClassHierarchy().getClassFileClassArray(stateSymbolic.getClassHierarchy().getInitiatingLoader(cfObjectStateSymbolic), cfObjectStateSymbolic.getClassName());
        			if (cfObjectStateConcrete == null) {
        				throw new GuidanceException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//gets the value of the length
        			final Array arrayStateSymbolic = (Array) objectStateSymbolic;
        			final Simplex arrayLengthConcrete = (Simplex) eval(arrayStateSymbolic.getLength());

        			//creates the object
        			final ReferenceConcrete retVal = stateConcrete.createArray(this.calc, null, arrayLengthConcrete, cfObjectStateConcrete);

        			//caches
        			this.mapRefStateSourceToStateDest.put(refcStateSymbolic, retVal);

        			//sets all the members in the concrete instance
        			final Array arrayStateConcrete = (Array) stateConcrete.getObject(retVal);
        			try {
        				for (Simplex i = this.calc.valInt(0); ((Boolean) ((Simplex) this.calc.push(i).lt(arrayLengthConcrete).pop()).getActualValue()).booleanValue(); i = (Simplex) this.calc.push(i).add(this.calc.valInt(1)).pop()) {
        					final Collection<AccessOutcome> outcomes = arrayStateSymbolic.get(this.calc, i);
        					for (AccessOutcome outcome : outcomes) {
        						final boolean hasAccess = ((Boolean) ((Simplex) eval(outcome.inRange(this.calc, i))).getActualValue()).booleanValue();
        						if (hasAccess) {
        							if (outcome instanceof AccessOutcomeInValue) {
        								final Value value = ((AccessOutcomeInValue) outcome).getValue();
        								final Value valueStateConcrete;
        								if (value instanceof Reference) {
        									valueStateConcrete = doPortToStateConcrete((Reference) value, stateSymbolic, stateConcrete);
        								} else if (value instanceof Primitive) {
        									valueStateConcrete = eval((Primitive) value);
        								} else { //value instanceof DefaultValue
        									//this should never happen
        									throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
        								}
        								arrayStateConcrete.setFast(i, valueStateConcrete);
        								break;
        							} else if (outcome instanceof AccessOutcomeInInitialArray) {
        								final Reference refInitialArray = ((AccessOutcomeInInitialArray) outcome).getInitialArray();
        								final Array initialArrayStateSymbolic = (Array) stateSymbolic.getObject(refInitialArray);
        								final Collection<AccessOutcome> initialOutcomes = initialArrayStateSymbolic.get(this.calc, i);
        								for (AccessOutcome initialOutcome : initialOutcomes) {
        									final boolean hasAccessInitial = ((Boolean) ((Simplex) eval(initialOutcome.inRange(this.calc, i))).getActualValue()).booleanValue();
        									if (hasAccessInitial) {
        										if (outcome instanceof AccessOutcomeInValue) {
        											final Value value = ((AccessOutcomeInValue) outcome).getValue();
        											final Value valueStateConcrete;
        											if (value instanceof Reference) {
        												valueStateConcrete = doPortToStateConcrete((Reference) value, stateSymbolic, stateConcrete);
        											} else if (value instanceof Primitive) {
        												valueStateConcrete = eval((Primitive) value);
        											} else { //value instanceof DefaultValue
        												//this should never happen
        												throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
        											}
        											arrayStateConcrete.setFast(i, valueStateConcrete);
        											break;
        										} else {
        											//this should never happen
        											throw new UnexpectedInternalException(ERROR_UNEXPECTED_ARRAY_OUTCOME);
        										}
        									} //else continue
        								}
        								break;
        							} else {
        								//this should never happen
        								throw new UnexpectedInternalException(ERROR_UNEXPECTED_ARRAY_OUTCOME);
        							}
        						} //else continue
        					}
        				}
    				} catch (ClassCastException | FastArrayAccessNotAllowedException e) {
    					//this should never happen
    					throw new UnexpectedInternalException(e);
    				}

        			return retVal;
        		} else {
        			//possibly a metalevel box pilfered here?
        			throw new UnexpectedInternalException(ERROR_UNEXPECTED_OBJECT);
        		}
        	}
        }
        
        /**
         * Maps a reference in a source concrete state with a corresponding 
         * reference in a destination concrete state.
         */
        private HashMap<Reference, Reference> mapRefClonedToClone; //used only in the context of a call to cloneDeep

        /**
         * Clones an object from a concrete state to another one 
         * (deep copy). 
         * 
         * @param refSource a {@link Reference} to the object
         *        to be cloned. The object must be simple
         *        (i.e., it must be a simple array, or be composed by
         *        fields with concrete values or with references to
         *        other simple objects).
         * @param stateConcreteSource a {@link State}, the one containing
         *        the object referred by {@code refSource}.
         * @param stateConcreteDestination a {@link State}, the one 
         *        where the clone must be created. We assume that it has 
         *        greater {@link HistoryPoint} than (i.e., it comes from the 
         *        same concrete execution and chronologically
         *        after) {@code stateConcreteSource}.
         * @return a {@link ReferenceConcrete} to the cloned object in 
         *         {@code stateConcreteDestination}.
         * @throws InvalidInputException
         * @throws HeapMemoryExhaustedException
         */
        private Reference cloneDeep(Reference refSource, State stateConcreteSource, State stateConcreteDestination) 
        throws InvalidInputException, HeapMemoryExhaustedException {
        	this.mapRefClonedToClone = new HashMap<>();
        	return doCloneDeep(refSource, stateConcreteSource, stateConcreteDestination);
        }
        
        private Reference doCloneDeep(Reference refSource, State stateConcreteSource, State stateConcreteDestination)
        throws InvalidInputException, HeapMemoryExhaustedException {
    		if (stateConcreteSource.isNull(refSource)) {
        		//if refStateConcrete is null, return null
    			return Null.getInstance();
        	} else if (refSource instanceof ReferenceSymbolic && !stateConcreteSource.resolved((ReferenceSymbolic) refSource)) {
        		throw new InvalidInputException(ERROR_CLONE_OBJECT_NOT_SIMPLE);
        	} else {
        		//if the object is already available, return it
        		if (this.mapRefClonedToClone.containsKey(refSource)) {
        			return this.mapRefClonedToClone.get(refSource);
        		}

        		//gets the object in the state and analyzes its class
        		final HeapObjekt object = stateConcreteSource.getObject(refSource);
        		if (object instanceof Instance_JAVA_CLASS ||
        		object instanceof Instance_JAVA_CLASSLOADER ||
        		object instanceof Instance_JAVA_THREAD) {
        			//these cannot be cloned; since we assume that
        			//stateConcreteDestination comes chronologically
        			//after stateConcreteSource, the references are
        			//the same
        			
        			//caches
        			this.mapRefClonedToClone.put(refSource, refSource);
        			
        			return refSource;
        		} else if (object instanceof Instance) {
        			//gets the classfile of the object
        			final ClassFile cfObject = object.getType();
        			
        			//creates the object
        			final ReferenceConcrete retVal = stateConcreteDestination.createInstance(this.calc, cfObject);

        			//caches
        			this.mapRefClonedToClone.put(refSource, retVal);

        			//sets all the fields in the instance
        			final Instance instance = (Instance) object;
        			final Instance instanceClone = (Instance) stateConcreteDestination.getObject(retVal);
        			for (Map.Entry<Signature, Variable> entry : instanceClone.fields().entrySet()) {
        				final Value value = instance.getFieldValue(entry.getKey());
        				final Value valueClone;
        				if (value instanceof Reference) {
        					valueClone = doCloneDeep((Reference) value, stateConcreteSource, stateConcreteDestination);
        				} else if (value instanceof Primitive) {
        					valueClone = value;
        				} else { //value instanceof DefaultValue
        					//this should never happen
        					throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
        				}
        				entry.getValue().setValue(valueClone);
        			}

        			return retVal;
        		} else if (object instanceof Array) {
        			//gets the classfile of the object
        			final ClassFile cfObject = object.getType();
        			
        			//gets the value of the length
        			final Array array = (Array) object;
        			final Simplex arrayLength = (Simplex) array.getLength();

        			//creates the object
        			final ReferenceConcrete retVal = stateConcreteDestination.createArray(this.calc, null, arrayLength, cfObject);

        			//caches
        			this.mapRefClonedToClone.put(refSource, retVal);
        			
        			//sets all the members in the clone
        			final Array arrayClone = (Array) stateConcreteDestination.getObject(retVal);
    				try {
    					for (Simplex i = this.calc.valInt(0); ((Boolean) ((Simplex) this.calc.push(i).lt(arrayLength).pop()).getActualValue()).booleanValue(); i = (Simplex) this.calc.push(i).add(this.calc.valInt(1)).pop()) {
        					final AccessOutcomeInValue outcome = (AccessOutcomeInValue) array.getFast(this.calc, i);
        					final Value value = ((AccessOutcomeInValue) outcome).getValue();
        					final Value valueClone;
            				if (value instanceof Reference) {
            					valueClone = doCloneDeep((Reference) value, stateConcreteSource, stateConcreteDestination);
            				} else if (value instanceof Primitive) {
            					valueClone = value;
            				} else { //value instanceof DefaultValue
            					//this should never happen
            					throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
            				}
        					arrayClone.setFast(i, valueClone);
    					}
    				} catch (ClassCastException | FastArrayAccessNotAllowedException | InvalidTypeException | InvalidOperandException e) {
    					//this should never happen
    					throw new UnexpectedInternalException(e);
    				}

        			return retVal;
        		} else {
        			//possibly a metalevel box pilfered here?
        			throw new UnexpectedInternalException(ERROR_UNEXPECTED_OBJECT);
        		}
        	}
        }
        
        /**
         * Stores the references to objects that have been 
         * already created during implantation.
         */
        private HashSet<Reference> created; //used only in the context of a call to implantDeep

        /**
         * Implants an object from a concrete state to another one 
         * (deep copy). 
         * 
         * @param refSource a {@link Reference} to the object
         *        to be implanted. The object must be simple
         *        (i.e., it must be a simple array, or be composed by
         *        fields with concrete values or with references to
         *        other simple objects).
         * @param stateConcreteSource a {@link State}, the one containing
         *        the object referred by {@code refSource}.
         * @param stateConcreteDestination a {@link State}, the one 
         *        where the object must be implanted. A deep clone of the 
         *        object will be created in {@code stateConcreteDestination} 
         *        at the same heap position as indicated by {@code refSource}. 
         *        We assume that {@code stateConcreteDestination} has 
         *        less {@link HistoryPoint} than (i.e., it comes from the 
         *        same concrete execution and chronologically
         *        before) {@code stateConcreteSource}. We also assume that
         *        there are enough free slots in {@code stateConcreteDestination}
         *        to host the new object.
         * @return a {@link ReferenceConcrete} to the implanted object in 
         *         {@code stateConcreteDestination}.
         * @throws HeapMemoryExhaustedException 
         * @throws InvalidInputException 
         */
        private void implantDeep(Reference refSource, State stateConcreteSource, State stateConcreteDestination) 
        throws InvalidInputException, HeapMemoryExhaustedException {
        	this.created = new HashSet<>();
        	doImplantDeep(refSource, stateConcreteSource, stateConcreteDestination);
        }
        
        private void doImplantDeep(Reference refSource, State stateConcreteSource, State stateConcreteDestination)
        throws InvalidInputException, HeapMemoryExhaustedException {
    		if (stateConcreteSource.isNull(refSource)) {
        		//if refStateConcrete is null, do nothing
    			return;
        	} else if (refSource instanceof ReferenceSymbolic && !stateConcreteSource.resolved((ReferenceSymbolic) refSource)) {
        		throw new InvalidInputException(ERROR_CLONE_OBJECT_NOT_SIMPLE);
        	} else {
        		//if the object is already available, return it
        		if (this.created.contains(refSource)) {
        			return;
        		}

        		//gets the object in the state and analyzes its class
        		final HeapObjekt object = stateConcreteSource.getObject(refSource);
        		if (object instanceof Instance_JAVA_CLASS ||
        		object instanceof Instance_JAVA_CLASSLOADER ||
        		object instanceof Instance_JAVA_THREAD) {
        			//these could in principle be implanted, but
        			//since they are usually created early it is 
        			//unlikely that we will find their slots free,
        			//and moreover we will need to register them,
        			//so we just look if they are already present 
        			//and in the negative case we fail
        			if (stateConcreteDestination.getObject(refSource) == null) {
        				throw new UnexpectedInternalException(); //TODO better exception
        			} else {
        				return;
        			}
        		} else if (object instanceof Instance) {
        			//gets the classfile of the object
        			final ClassFile cfObject = object.getType();
        			
        			//creates the object
        			final long heapPosition = (refSource instanceof ReferenceConcrete ? ((ReferenceConcrete) refSource).getHeapPosition() : stateConcreteSource.getResolution(((ReferenceSymbolic) refSource)));
        			stateConcreteDestination.createInstanceAt(this.calc, cfObject, heapPosition);

        			//caches
        			this.created.add(refSource);

        			//sets all the fields in the instance
        			final Instance instance = (Instance) object;
        			final Instance instanceClone = (Instance) stateConcreteDestination.getObject(refSource);
        			for (Map.Entry<Signature, Variable> entry : instanceClone.fields().entrySet()) {
        				final Value value = instance.getFieldValue(entry.getKey());
        				final Value valueClone;
        				if (value instanceof Reference) {
        					doImplantDeep((Reference) value, stateConcreteSource, stateConcreteDestination);
        					valueClone = (value instanceof ReferenceConcrete) ? value : new ReferenceConcrete(stateConcreteSource.getResolution(((ReferenceSymbolic) value)));
        				} else if (value instanceof Primitive) {
        					//nothing to implant
        					valueClone = value;
        				} else { //value instanceof DefaultValue
        					//this should never happen
        					throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
        				}
        				entry.getValue().setValue(valueClone);
        			}

        			return;
        		} else if (object instanceof Array) {
        			//gets the classfile of the object
        			final ClassFile cfObject = object.getType();
        			
        			//gets the value of the length
        			final Array array = (Array) object;
        			final Simplex arrayLength = (Simplex) array.getLength();

        			//creates the object
        			final long heapPosition = (refSource instanceof ReferenceConcrete ? ((ReferenceConcrete) refSource).getHeapPosition() : stateConcreteSource.getResolution(((ReferenceSymbolic) refSource)));
        			stateConcreteDestination.createArrayAt(this.calc, null, arrayLength, cfObject, heapPosition);

        			//caches
        			this.created.add(refSource);
        			
        			//sets all the members in the clone
        			final Array arrayClone = (Array) stateConcreteDestination.getObject(refSource);
    				try {
    					for (Simplex i = this.calc.valInt(0); ((Boolean) ((Simplex) this.calc.push(i).lt(arrayLength).pop()).getActualValue()).booleanValue(); i = (Simplex) this.calc.push(i).add(this.calc.valInt(1)).pop()) {
        					final AccessOutcomeInValue outcome = (AccessOutcomeInValue) array.getFast(this.calc, i);
        					final Value value = ((AccessOutcomeInValue) outcome).getValue();
        					final Value valueClone;
            				if (value instanceof Reference) {
            					doImplantDeep((Reference) value, stateConcreteSource, stateConcreteDestination);
            					valueClone = (value instanceof ReferenceConcrete) ? value : new ReferenceConcrete(stateConcreteSource.getResolution(((ReferenceSymbolic) value)));
            				} else if (value instanceof Primitive) {
            					//nothing to implant
            					valueClone = value;
            				} else { //value instanceof DefaultValue
            					//this should never happen
            					throw new UnexpectedInternalException(ERROR_DEFAULT_VALUE_DETECTED);
            				}
        					arrayClone.setFast(i, valueClone);
    					}
    				} catch (ClassCastException | FastArrayAccessNotAllowedException | InvalidTypeException | InvalidOperandException e) {
    					//this should never happen
    					throw new UnexpectedInternalException(e);
    				}

        			return;
        		} else {
        			//possibly a metalevel box pilfered here?
        			throw new UnexpectedInternalException(ERROR_UNEXPECTED_OBJECT);
        		}
        	}
        }
        
        private State runSnippet(State stateInitial) throws GuidanceException {
        	final int stackSizeInitial = stateInitial.getStackSize();
            
            //builds the runner actions
            final Actions a = new Actions() {
                @Override
                public boolean atStepPost() {
                	if (getEngine().canBacktrack()) {
                    	JVMJBSE.this.excGuidance = new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
                		return true;
                	}
                	final int stackSizeCurrent = getEngine().getCurrentState().getStackSize();
                	return (stackSizeCurrent == stackSizeInitial - 1);
                }

                @Override
                public boolean atPathEnd() {
                    //path ended before meeting the stop method
                	JVMJBSE.this.excGuidance = new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
                    return true;
                }
                
            };
            
            final RunnerParameters rp = this.runnerParameters.clone();
            rp.setActions(a);
            rp.setStartingState(stateInitial);
            rp.clearUninterpreted();
            rp.clearUninterpretedPattern();

            //builds the private runner
            final Runner runner;
            try {
                final RunnerBuilder b = new RunnerBuilder();
                runner = b.build(rp);
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

            //runs the private runner until it returns from the snippet frame
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

            //fails if by some reason it fell into symbolic execution
            if (this.excGuidance != null) {
                throw this.excGuidance;
            }

            //returns the final state
            return runner.getEngine().getCurrentState();
        }
        
        /**
         * Returns an object in a concrete state.
         * 
         * @param reference a {@link ReferenceConcrete}.
         * @param historyPoint the {@link HistoryPoint} of the state
         * @return a {@link HeapObjekt}, or {@code null} if {@code reference}
         *         does not refer to an object in the state at {@code historyPoint}.
         * @throws GuidanceException if no concrete state at {@code historyPoint}
         *         was previously saved.
         */
        private HeapObjekt getObject(ReferenceConcrete reference, HistoryPoint historyPoint) 
        throws GuidanceException {
        	try {
				final State state = this.statesConcrete.get(historyPoint);
				if (state == null) {
					throw new GuidanceException(ERROR_BAD_HISTORY_POINT + historyPoint.toString());
				}
				return state.getObject(reference);
			} catch (FrozenStateException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
        }
        
        private void returnEngineFromMethod(int symbolicStateStackSizeTarget) throws GuidanceException {
        	try {
        		while (this.engine.getCurrentState().getStackSize() != (symbolicStateStackSizeTarget + this.stackSizeOffset)) {
        			this.engine.step();
        		}
			} catch (EngineStuckException | ThreadStackEmptyException e) {
				throw new GuidanceException(ERROR_GUIDANCE_MISALIGNMENT);
			} catch (CannotManageStateException | NonexistingObservedVariablesException |
			         ClasspathException | ContradictionException | DecisionException |
			         FailureException e) {
				throw new GuidanceException(e);
        	}
        }

        private int preConcreteStateStackSize;             //used only by stepping and alignment
        private int stackSizeOffset = -1;                  //used only by stepping and alignment
        private boolean aligned = true;                    //used only by stepping and alignment
        
        @Override
        protected void preStep(State preSymbolicState) throws GuidanceException {
        	//invariant: the engine's current state (preConcreteState) has same 
        	//stack size (minus offset) and same program counter as preSymbolicState 
        	//(this.aligned == true), or preSymbolicState has bigger stack size minus offset 
        	//(this.aligned == false)
        	try {
        		this.preSymbolicState = preSymbolicState;
        		this.preSymbolicStateAtInvoke = isInvoke(this.preSymbolicState.getCurrentFrame().getInstruction());
        		this.preSymbolicStateStackSize = this.preSymbolicState.getStackSize();
        		final State preConcreteState = this.engine.getCurrentState();
        		this.preConcreteStateStackSize = preConcreteState.getStackSize();
            	
            	//the guiding (concrete) execution might start with a 
            	//deeper stack than the guided (symbolic), because it 
            	//also executes the driver method(s): saves the offset
            	if (this.stackSizeOffset == -1) {
            		this.stackSizeOffset = this.preConcreteStateStackSize - 1;
            		//note that here we are at the start of symbolic execution;
            		//we save the initial symbolic state, a thing that we could not
            		//do in the constructor
            		this.statesSymbolic.put(preSymbolicState.getHistoryPoint().startingInitial(), preSymbolicState);
            	}
            	
        		//steps if aligned
            	if (this.aligned) {
    				this.engine.step();
            	} //else does nothing, it must wait for the symbolic execution to realign
            	
            	//saves the history point of the current symbolic state
			} catch (ThreadStackEmptyException | EngineStuckException e) {
				throw new GuidanceException(ERROR_GUIDANCE_MISALIGNMENT);
			} catch (CannotManageStateException | NonexistingObservedVariablesException |
			ClasspathException | ContradictionException | DecisionException | FrozenStateException |
			FailureException e) {
				throw new GuidanceException(e);
			}
        }
        
        private boolean symbolicAndConcreteAreAligned(State postSymbolicState) throws ThreadStackEmptyException {
    		final int postSymbolicStateStackSize = postSymbolicState.getStackSize(); //can be zero upon uncaught exception or end of execution
        	final int postSymbolicStateCurrentProgramCounter = (postSymbolicStateStackSize == 0 ? -1 : postSymbolicState.getCurrentProgramCounter());
    		final State postConcreteState = this.engine.getCurrentState(); //can be zero upon uncaught exception
        	final int postConcreteStateStackSize = postConcreteState.getStackSize();
        	final int postConcreteStateCurrentProgramCounter = (postConcreteStateStackSize == 0 ? -1 : postConcreteState.getCurrentProgramCounter());
        	
        	return (postConcreteStateStackSize == (postSymbolicStateStackSize + this.stackSizeOffset) &&
        	(postSymbolicStateStackSize == 0 || postConcreteStateCurrentProgramCounter == postSymbolicStateCurrentProgramCounter));
        }
        
        @Override
        protected void postStep(State postSymbolicState) throws GuidanceException {
        	//invariant: after a step the engine's current state (postConcreteState) has same 
        	//stack size (minus offset) and same program counter as postSymbolicState 
        	//(alignment), or postSymbolicState has bigger stack size minus offset 
        	//(the symbolic execution is executing a trigger or a map model method 
        	//"fast forwarded" by concrete execution). In the latter case postConcreteState 
        	//is not earlier, possibly later, in the execution history than postSymbolicState.
        	try {
        		final int postSymbolicStateStackSize = postSymbolicState.getStackSize(); //can be zero upon uncaught exception or end of execution
            	
            	if (this.aligned) {
                	//we were aligned before the symbolic execution step: checks whether 
            		//the two executions are still aligned
            		final State postConcreteState = this.engine.getCurrentState(); //can be zero upon uncaught exception
                	final int postConcreteStateStackSize = postConcreteState.getStackSize();
                	
                	if (symbolicAndConcreteAreAligned(postSymbolicState)) {
                		//yes, they are still aligned: nothing to do
                	} else {
                		//no, we are no longer aligned
                		if (postConcreteStateStackSize > (postSymbolicStateStackSize + this.stackSizeOffset)) {
                			//the concrete execution entered more method frames than the symbolic.
                			//Possible causes: triggers of the concrete execution not active in
                			//the symbolic execution (unlikely with snippets), <clinit> methods
                			//not executed in the symbolic execution but executed in the concrete
                			//execution (may be a consequence of the activation of other frames),
                			//or most interestingly the symbolic execution skipped a method call 
                			//execution and returned a SymbolicApply instead.
                			//In all cases we must repeatedly step this.engine until the concrete
                			//execution realigns with the symbolic one. In the last case,
                			//we must also save the final aligned concrete and symbolic states
                			//so we can retrieve the concrete value of the SymbolicApply later.
                			//We detect this case in the saveRealignedStates variable.
                			Value postSymbolicStateTOS;
                			try { 
                				postSymbolicStateTOS = postSymbolicState.topOperand();
                			} catch (InvalidNumberOfOperandsException e) {
                				postSymbolicStateTOS = null;
                			} catch (ThreadStackEmptyException e) {
                				//it is possible that postSymbolicState is stuck
                				//with a return value
                				postSymbolicStateTOS = postSymbolicState.getStuckReturn();
                			}
                			final Signature postConcreteStateCurrentMethodSignature = postConcreteState.getCurrentMethodSignature();
                			final boolean saveRealignedStates = 
                			(postConcreteStateStackSize == (postSymbolicStateStackSize + this.stackSizeOffset) + 1 &&
                			postSymbolicStateTOS != null && containsSymbolicApply(postSymbolicStateTOS, postConcreteStateCurrentMethodSignature));

                			//realigns
                			returnEngineFromMethod(postSymbolicStateStackSize);
                			
                			//checks realignment
                			if (symbolicAndConcreteAreAligned(postSymbolicState)) {
                    			//possibly saves the realigned states
                    			if (saveRealignedStates) {
                    				final HistoryPoint historyPoint = getHistoryPointSymbolicApply(postSymbolicStateTOS, postConcreteStateCurrentMethodSignature);
                    				this.statesConcrete.put(historyPoint, this.engine.getCurrentState().clone());
                    				this.statesSymbolic.put(historyPoint, postSymbolicState.clone());
                    			}
                			} else {
                				//realignment failure: nothing to do
                				throw new GuidanceException(ERROR_GUIDANCE_MISALIGNMENT);
                			}
                		} else if (postConcreteStateStackSize < (postSymbolicStateStackSize + this.stackSizeOffset)) {
                			//the symbolic execution entered more method frames than the concrete.
                			//Possible causes: triggers of the guided execution not active in
                			//the guiding execution (unlikely with snippets), or <clinit> methods
                			//not executed in the symbolic execution but executed in the concrete
                			//execution (may be a consequence of the activation of other frames). 
                			//We must suspend stepping the concrete execution until the symbolic 
                			//execution realigns with the concrete one.
                			this.aligned = false;
                		} else {
                			//the symbolic and concrete execution have the same stack depth: this
                			//means that they stayed on the current frame and jumped to different 
                			//destinations, a thing that happens with map models when a map method 
                			//is executed, but this situation should never happen because in this case
                			//alignment is managed by the notifyExecutionOfMapModelMethod method. In 
                			//general, the situation may happen with methods that introspect the state of
                			//symbolic execution itself like the jbse.meta.Analysis.isSymbolic methods.
                			//Realignment in this case is hopeless.
            				throw new GuidanceException(ERROR_GUIDANCE_MISALIGNMENT);
                		}
                	}
            	} else {
                	//we were not aligned before the symbolic execution step:             		
            		//Also in this situation symbolic execution might have skipped 
            		//a method call execution and returned a SymbolicApply: We must
        			//detect the situation and save the pair concrete+symbolic states
        			//so we can retrieve the concrete value of the SymbolicApply later.
            		//Since postConcreteState is later in history than postSymbolicState
            		//it is ok to use postConcreteState as the concrete state corresponding
            		//to the history point.
            		if (this.preSymbolicStateAtInvoke && this.preSymbolicStateStackSize == postSymbolicStateStackSize) {
	        			Value postSymbolicStateTOS;
	        			try { 
	        				postSymbolicStateTOS = postSymbolicState.topOperand();
	        			} catch (InvalidNumberOfOperandsException e) {
	        				postSymbolicStateTOS = null;
	        			} catch (ThreadStackEmptyException e) {
	        				//it is possible that postSymbolicState is stuck
	        				//with a return value
	        				postSymbolicStateTOS = postSymbolicState.getStuckReturn();
	        			}
	            		if (postSymbolicStateTOS != null && containsSymbolicApply(postSymbolicStateTOS)) {
            				final HistoryPoint historyPoint = getHistoryPointSymbolicApply(postSymbolicStateTOS);
            				this.statesConcrete.put(historyPoint, this.engine.getCurrentState().clone());
            				this.statesSymbolic.put(historyPoint, postSymbolicState.clone());
	            		}
            		}
            		
            		//finally checks whether now the two executions became aligned
            		this.aligned = symbolicAndConcreteAreAligned(postSymbolicState);
            	}
			} catch (ThreadStackEmptyException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
        }
        
        private static boolean isInvoke(byte code) {
        	return (code == OP_INVOKEDYNAMIC ||
        	code == OP_INVOKEHANDLE ||
        	code == OP_INVOKEINTERFACE ||
        	code == OP_INVOKESPECIAL ||
        	code == OP_INVOKESTATIC ||
        	code == OP_INVOKEVIRTUAL);
        }

        private boolean containsSymbolicApply(Value value) {
        	if (value instanceof SymbolicApply) {
        		return true;
        	} else if (value instanceof WideningConversion && ((WideningConversion) value).getArg() instanceof SymbolicApply) {
        		return true;
        	} else {
        		return false;
        	}
        }
        
        private HistoryPoint getHistoryPointSymbolicApply(Value value) {
        	final HistoryPoint retVal;
        	if (value instanceof SymbolicApply) {
        		retVal = ((SymbolicApply) value).historyPoint();
        	} else if (value instanceof WideningConversion && ((WideningConversion) value).getArg() instanceof SymbolicApply) {
        		retVal = ((SymbolicApply) ((WideningConversion) value).getArg()).historyPoint();
        	} else {
        		retVal = null;
        	}
        	return retVal;
        }
        
        private boolean containsSymbolicApply(Value value, Signature methodSignature) {
        	final String operator = methodSignature.toString();
        	final ArrayList<Boolean> retVal = new ArrayList<>();
        	final ValueVisitor v = new ValueVisitor() {
				
				@Override
				public void visitReferenceSymbolicMemberMapValue(ReferenceSymbolicMemberMapValue x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicMemberMapKey(ReferenceSymbolicMemberMapKey x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicMemberField(ReferenceSymbolicMemberField x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicMemberArray(ReferenceSymbolicMemberArray x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicLocalVariable(ReferenceSymbolicLocalVariable x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitReferenceSymbolicApply(ReferenceSymbolicApply x) throws Exception {
					if (x.getOperator().equals(operator)) {
						retVal.add(true);
					} else {
						for (Value arg : x.getArgs()) {
							arg.accept(this);
						}
					}
				}
				
				@Override
				public void visitReferenceConcrete(ReferenceConcrete x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitReferenceArrayImmaterial(ReferenceArrayImmaterial x) throws Exception {
					x.getLength().accept(this);
				}
				
				@Override
				public void visitKlassPseudoReference(KlassPseudoReference x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitWideningConversion(WideningConversion x) throws Exception {
					x.getArg().accept(this);
				}
				
				@Override
				public void visitTerm(Term x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitSimplex(Simplex x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
					if (x.getOperator().equals(operator)) {
						retVal.add(true);
					} else {
						for (Value arg : x.getArgs()) {
							arg.accept(this);
						}
					}
				}
				
				@Override
				public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
					x.getArg().accept(this);
				}
				
				@Override
				public void visitExpression(Expression e) throws Exception {
					if (e.isUnary()) {
						e.getOperand().accept(this);
					} else {
						e.getFirstOperand().accept(this);
						e.getSecondOperand().accept(this);
					}
				}
				
				@Override
				public void visitAny(Any x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitDefaultValue(DefaultValue x) {
					//nothing
				}

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
					x.getContainer().accept(this);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
					//nothing
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
					x.getContainer().accept(this);
					
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x)
				throws Exception {
					x.getContainer().accept(this);
					
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
					x.getContainer().accept(this);
				}
			};
			
			try {
				value.accept(v);
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			return retVal.size() > 0;
        }
        
        
        private HistoryPoint getHistoryPointSymbolicApply(Value value, Signature methodSignature) {
        	final String operator = methodSignature.toString();
        	final ArrayList<HistoryPoint> historyPoints = new ArrayList<>();
        	final ValueVisitor v = new ValueVisitor() {
				
				@Override
				public void visitReferenceSymbolicMemberMapValue(ReferenceSymbolicMemberMapValue x) throws Exception {
					x.getContainer().accept(this);
				}
				
				@Override
				public void visitReferenceSymbolicMemberMapKey(ReferenceSymbolicMemberMapKey x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicMemberField(ReferenceSymbolicMemberField x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicMemberArray(ReferenceSymbolicMemberArray x) throws Exception {
					x.getContainer().accept(this);
					
				}
				
				@Override
				public void visitReferenceSymbolicLocalVariable(ReferenceSymbolicLocalVariable x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitReferenceSymbolicApply(ReferenceSymbolicApply x) throws Exception {
					if (x.getOperator().equals(operator)) {
						historyPoints.add(x.historyPoint());
					} else {
						for (Value arg : x.getArgs()) {
							arg.accept(this);
						}
					}
				}
				
				@Override
				public void visitReferenceConcrete(ReferenceConcrete x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitReferenceArrayImmaterial(ReferenceArrayImmaterial x) throws Exception {
					x.getLength().accept(this);
				}
				
				@Override
				public void visitKlassPseudoReference(KlassPseudoReference x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitWideningConversion(WideningConversion x) throws Exception {
					x.getArg().accept(this);
				}
				
				@Override
				public void visitTerm(Term x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitSimplex(Simplex x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
					if (x.getOperator().equals(operator)) {
						historyPoints.add(x.historyPoint());
					} else {
						for (Value arg : x.getArgs()) {
							arg.accept(this);
						}
					}
				}
				
				@Override
				public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
					x.getArg().accept(this);
				}
				
				@Override
				public void visitExpression(Expression e) throws Exception {
					if (e.isUnary()) {
						e.getOperand().accept(this);
					} else {
						e.getFirstOperand().accept(this);
						e.getSecondOperand().accept(this);
					}
				}
				
				@Override
				public void visitAny(Any x) throws Exception {
					//nothing
				}
				
				@Override
				public void visitDefaultValue(DefaultValue x) {
					//nothing
				}

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
					x.getContainer().accept(this);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
					//nothing
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
					x.getContainer().accept(this);
					
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x)
				throws Exception {
					x.getContainer().accept(this);
					
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
					x.getContainer().accept(this);
				}
			};
			
			try {
				value.accept(v);
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			
			//finds the most recent and returns it
			HistoryPoint retVal = null;
			for (HistoryPoint cur : historyPoints) {
				if (retVal == null || retVal.weaklyBefore(cur)) {
					retVal = cur;
				}
			}
			return retVal;
        }
        
        public void notifyExecutionOfMapModelMethod() {
        	//Concrete and symbolic execution of map model methods
        	//follow different control flow paths, therefore we cannot
        	//execute them in locksteps and keep then aligned. 
        	//Note that this method is invoked between the pre-step
        	//and the post-step, when the symbolic execution has not
        	//yet ended stepping (but concrete execution has).
        	if (this.aligned) {
            	//If we are starting from aligned states, then the 
        		//current concrete state and the current symbolic state
            	//are both inside (right after the beginning of) a 
            	//map model method. What we do is to "fast-forward"
            	//the concrete execution to the end of the
            	//map model method, and wait for the symbolic execution
            	//to realign.
    			try {
					returnEngineFromMethod(this.preSymbolicState.getStackSize() - 1);
				} catch (GuidanceException e) {
					// TODO how do we report that??? We cannot throw because we are in the context of an Algorithm execution, and moreover the method has no exception declared in its signature
				}
    			this.aligned = false;
        	} else {
        		//If we are starting from misaligned states, this may 
        		//happen for one of two reasons. The first is that symbolic 
        		//execution previously stepped in a map model method, 
        		//misaligned, and then the map model method invoked another
        		//map model method (nested calls) that triggered this invocation
        		//of notifyExecutionOfMapModelMethod(). In this case we 
        		//need to do nothing, because concrete execution must keep
        		//on waiting for the outermost call of symbolic execution
        		//to realign and ignore this last notification. The second 
        		//reason is, the executions misaligned for some other reason 
        		//(e.g., symbolic execution is executing a trigger or a <clinit>
        		//that concrete execution is not executing) and during 
        		//this misalignment, symbolic execution invoked a map model 
        		//method. Actually, this is not a much different situation, 
        		//and also in this case the right thing to do is wait 
        		//for symbolic execution to realign, so we still have nothing 
        		//to do in this else-branch.
        	}
        }

        @Override
        protected Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
            return this.engine.getCurrentState().getCurrentMethodSignature();
        }

        @Override
        protected int getCurrentProgramCounter() throws ThreadStackEmptyException {
            return this.engine.getCurrentState().getCurrentProgramCounter();
        }

        @Override
        public void close() {
        	try {
        		this.engine.close();
        	} catch (DecisionException e) {
        		throw new UnexpectedInternalException(e);
        	}
        }        
    }
    
    public void notifyExecutionOfMapModelMethod() {
    	((JVMJBSE) this.jvm).notifyExecutionOfMapModelMethod();
    }
}
