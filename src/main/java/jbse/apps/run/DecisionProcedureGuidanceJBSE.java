package jbse.apps.run;

import static jbse.bc.Signatures.JAVA_HASHMAP;
import static jbse.bc.Signatures.JAVA_MAP_GET;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import jbse.algo.ExecutionContext;
import jbse.algo.exc.CannotManageStateException;
import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.Snippet;
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
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicMemberMapKey;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.SymbolicApply;
import jbse.val.SymbolicLocalVariable;
import jbse.val.SymbolicMemberArray;
import jbse.val.SymbolicMemberField;
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
        
        private final Engine engine;
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
        
        private Exception catastrophicFailure = null; //used only by runners actions to report errors
        private boolean failedConcrete = false;       //used only by runners actions to report errors
        
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
                    	JVMJBSE.this.catastrophicFailure = e;
                        return true;
                    }
                }

                @Override
                public boolean atStepPost() {
                	JVMJBSE.this.failedConcrete = getEngine().canBacktrack();
                    return JVMJBSE.this.failedConcrete;
                }

                @Override
                public boolean atPathEnd() {
                    //path ended before meeting the stop method
                	JVMJBSE.this.failedConcrete = true;
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
            this.engine = runner.getEngine();
            final State initialState = this.engine.getCurrentState().clone();
            this.statesConcrete.put(initialState.getHistoryPoint().startingInitial(), initialState);

			final ClassHierarchy hier = initialState.getClassHierarchy();
			try {
				this.cfJAVA_HASHMAP = hier.loadCreateClass(JAVA_HASHMAP);
			} catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException |
			         ClassFileNotAccessibleException | IncompatibleClassFileException | BadClassFileVersionException |
			         RenameUnsupportedException | WrongClassNameException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
			}

            /*try {
                engine.close();
            } catch (DecisionException e) {
                throw new UnexpectedInternalException(e);
            }*/
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
        	if (this.symbolValueConversionCache.containsKey(origin)) {
        		return this.symbolValueConversionCache.get(origin);
        	}
        	
			final State stateConcrete = this.statesConcrete.get(origin.historyPoint());
			if (stateConcrete == null) {
				throw new GuidanceException(ERROR_BAD_HISTORY_POINT + origin.historyPoint().toString());
			}
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
        			final State stateSymbolic = this.statesSymbolic.get(origin.historyPoint());
            		final ClassFile cfStateConcrete = stateConcrete.getClassHierarchy().getClassFileClassArray(stateSymbolic.getClassHierarchy().getInitiatingLoader(cfStateSymbolic), cfStateSymbolic.getClassName());
        			final ReferenceSymbolic k = stateConcrete.getKlass(cfStateConcrete).getOrigin();
        			if (k == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			}
        			this.symbolValueConversionCache.put(origin, k); //caches
        			return k;
        		} else if (origin instanceof SymbolicMemberField) {
        			final SymbolicMemberField af = (SymbolicMemberField) origin;
        			final Object o = getValue(af.getContainer());
        			if (o == null) {
        				throw new GuidanceException(ERROR_BAD_PATH);
        			} else if (o instanceof Reference) {
        				//we need to treat specially the field initialMap of 
        				//all the map model
        				final Objekt object = stateConcrete.getObject((Reference) o);
        				final Value fieldValue = object.getFieldValue(af.getFieldName(), af.getFieldClass());
        				if (object.getType().isSubclass(this.cfJAVA_HASHMAP) &&
        				af.getFieldName().equals("initialMap") && stateConcrete.isNull((Reference) fieldValue)) {
        					//all concrete maps have the initialMap field set
        					//to null, but all the symbolic maps must initialize
        					//initialMap to a fresh object (this is done in the 
        					//initSymbolic methods). Therefore, we must
        					//set the initialMap field of the concrete map to 
        					//a fresh map, otherwise this decision procedure 
        					//will return that initialMap is resolved to null.
        					//It doesn't care very much what is contained in this
        					//fresh map, because initSymbolic will overwrite all
        					//its content.
        					final ReferenceConcrete refInitialMap = stateConcrete.createInstance(this.calc, object.getType());
        					object.setFieldValue(af.getFieldName(), af.getFieldClass(), refInitialMap);
                			this.symbolValueConversionCache.put(origin, refInitialMap); //caches
        					return refInitialMap;
        				} else {
                			this.symbolValueConversionCache.put(origin, fieldValue); //caches
        					return fieldValue;
        				}
        			} else if (o instanceof Klass) {
        				final Value retVal = ((Klass) o).getFieldValue(af.getFieldName(), af.getFieldClass());
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

        			//gets the symbolic state
        			final State stateSymbolic = this.statesSymbolic.get(mk.valueHistoryPoint()); //TODO are we sure that the symbolic states with valueHistoryPoints are saved???
        			if (stateSymbolic == null) {
        				throw new GuidanceException(ERROR_BAD_HISTORY_POINT + mk.valueHistoryPoint().toString());
        			}
        			
        			//gets a reference to the map
        			final ReferenceSymbolic originMap = mk.getContainer();
        			final ReferenceConcrete refMapStateConcrete = (ReferenceConcrete) getValue(originMap);
        			
        			//analyzes the value
        			final Reference value = mk.getAssociatedValue(); 
        			if (value instanceof ReferenceSymbolicMemberMapValue) {
        				//TODO break circularity by making an hypothesis on the purely symbolic <key,value> pair
        				return null;
        			} else {
    					//builds a clone of the concrete state
    					final State stateConcreteClone = stateConcrete.clone();
    					
            			//gets a reference to the value, possibly building it, in the cloned concrete state
        				final ReferenceConcrete refValueStateConcrete = portToStateConcrete(value, stateSymbolic, stateConcreteClone);
        				
        				//gets the set of all the keys by running a snippet of the Map.get() method on the clone 
        				//of the concrete state
        				//TODO
        				return null;
        			}
        			
				} else if (origin instanceof ReferenceSymbolicMemberMapValue) {
        			final ReferenceSymbolicMemberMapValue mv = (ReferenceSymbolicMemberMapValue) origin;

        			//gets the symbolic state
        			final State stateSymbolic = this.statesSymbolic.get(mv.keyHistoryPoint()); //TODO are we sure that the symbolic states with keyHistoryPoints are saved???
        			if (stateSymbolic == null) {
        				throw new GuidanceException(ERROR_BAD_HISTORY_POINT + mv.keyHistoryPoint().toString());
        			}
        			
        			//gets a reference to the map in the concrete state
        			final ReferenceSymbolic originMap = mv.getContainer();
        			final ReferenceConcrete refMapStateConcrete = (ReferenceConcrete) getValue(originMap);
        			
        			//analyzes the key
        			final Reference key = mv.getAssociatedKey(); 
        			if (key instanceof ReferenceSymbolicMemberMapKey) {
        				//TODO break circularity by making an hypothesis on the purely symbolic <key,value> pair
        				return null;
        			} else {
    					//builds a clone of the concrete state
    					final State stateConcreteClone = stateConcrete.clone();
    					
            			//gets a reference to the key, possibly building it, in the cloned concrete state
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
                	//the value is on top of the stack 
        			//TODO is it true? I am in doubt for the case where the returned value is a ReferenceSymbolicApply
        			final Value retVal = stateConcrete.topOperand();
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
        private HashMap<ReferenceConcrete, ReferenceConcrete> mapObjectStateSymbolicToStateConcrete; //used only in the context of a call to portToStateConcrete

        /**
         * Ports an object from a symbolic state to a concrete state. This
         * might mean either finding a corresponding object in the concrete
         * state (e.g., if {@code refStateSymbolic} is a symbolic reference,
         * if it refers an object with class {@code java.lang.Class}...), or 
         * by building a lazy clone of the object in the concrete state. 
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
        	this.mapObjectStateSymbolicToStateConcrete = new HashMap<>();
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
        		if (this.mapObjectStateSymbolicToStateConcrete.containsKey(refStateSymbolic)) {
        			return this.mapObjectStateSymbolicToStateConcrete.get(refStateSymbolic);
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
        			this.mapObjectStateSymbolicToStateConcrete.put(refcStateSymbolic, retVal);

        			return retVal;
        		} else if (objectStateSymbolic instanceof Instance_JAVA_CLASSLOADER) {
        			//finds the corresponding object in the concrete state
        			final int classLoaderIdentifier = ((Instance_JAVA_CLASSLOADER) objectStateSymbolic).classLoaderIdentifier();
        			final ReferenceConcrete retVal = stateConcrete.referenceToInstance_JAVA_CLASSLOADER(classLoaderIdentifier);
        			if (retVal == null) {
        				throw new GuidanceException(ERROR_CANNOT_FIND_OBJECT_STATE_CONCRETE);
        			}

        			//caches
        			this.mapObjectStateSymbolicToStateConcrete.put(refcStateSymbolic, retVal);

        			return retVal;
        		} else if (objectStateSymbolic instanceof Instance_JAVA_THREAD) {
        			//currently there is only one thread in JBSE
        			final ReferenceConcrete retVal = stateConcrete.getMainThread();

        			//caches
        			this.mapObjectStateSymbolicToStateConcrete.put(refcStateSymbolic, retVal);

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
        			this.mapObjectStateSymbolicToStateConcrete.put(refcStateSymbolic, retVal);

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
        			//finds the classfile of the instance in the concrete state
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
        			this.mapObjectStateSymbolicToStateConcrete.put(refcStateSymbolic, retVal);

        			//sets all the members in the concrete instance
        			final Array arrayStateConcrete = (Array) stateConcrete.getObject(retVal);
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
        							arrayStateConcrete.set(this.calc, i, valueStateConcrete);
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
        										arrayStateConcrete.set(this.calc, i, valueStateConcrete);
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

        			return retVal;
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
                	JVMJBSE.this.failedConcrete = getEngine().canBacktrack();
                	if (JVMJBSE.this.failedConcrete) {
                		return true;
                	}
                	final int stackSizeCurrent = getEngine().getCurrentState().getStackSize();
                	return (stackSizeCurrent == stackSizeInitial - 1);
                }

                @Override
                public boolean atPathEnd() {
                    //path ended before meeting the stop method
                	JVMJBSE.this.failedConcrete = true;
                    return true;
                }
                
            };
            
            final RunnerParameters rp = this.runnerParameters.clone();
            rp.setActions(a);
            rp.setStartingState(stateInitial);

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
            if (this.failedConcrete) {
                throw new GuidanceException(ERROR_NONCONCRETE_GUIDANCE);
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
        
        //member variables used only by the step method

        private int stackSizeOffset = -1;
        private boolean aligned = true;
        
        @Override
        protected void step(State postSymbolicState) throws GuidanceException {
        	//invariant: after a step the engine's current state (postConcreteState) has same 
        	//stack size (minus offset) and same program counter as postSymbolicState 
        	//(alignment), or postSymbolicState has bigger stack size minus offset 
        	//(the symbolic execution is executing a trigger)
        	try {
        		final int postSymbolicStateStackSize = postSymbolicState.getStackSize();
            	final int postSymbolicStateCurrentProgramCounter = postSymbolicState.getCurrentProgramCounter();
        		final State preConcreteState = this.engine.getCurrentState();
            	final int preConcreteStateStackSize = preConcreteState.getStackSize();
            	final int preConcreteStateCurrentProgramCounter = preConcreteState.getCurrentProgramCounter();
            	
            	//the guiding (concrete) execution might start with a 
            	//deeper stack, because it executes the driver method(s)
            	if (this.stackSizeOffset == -1) {
            		this.stackSizeOffset = preConcreteStateStackSize - 1;
            	}
            	
            	if (this.aligned) {
            		//step
    				this.engine.step();
            		final State postConcreteState = this.engine.getCurrentState();
                	final int postConcreteStateStackSize = postConcreteState.getStackSize();
                	final int postConcreteStateCurrentProgramCounter = postConcreteState.getCurrentProgramCounter();
                	
                	//checks whether they are still aligned
                	if (postConcreteStateStackSize == (postSymbolicStateStackSize + this.stackSizeOffset) &&
                	postConcreteStateCurrentProgramCounter == postSymbolicStateCurrentProgramCounter) {
                		//aligned: nothing to do
                	} else if (postConcreteStateStackSize > (postSymbolicStateStackSize + this.stackSizeOffset) &&
                	postConcreteStateCurrentProgramCounter == 0) {
                		//misaligned: the concrete execution entered a set of frames, but the symbolic
                		//did not. Possible causes: triggers of the concrete execution not active in
                		//the symbolic execution (unlikely with snippets), or the symbolic execution 
                		//skipped a method call execution and returned a SymbolicApply instead.
                		//In both cases, we must repeatedly step this.engine until the concrete
                		//execution realigns with the symbolic one. In the second case,
                		//we must also save the final engine.currentState() in this.concreteStates
                		//so we can retrieve the concrete value of the SymbolicApply later.
                		final Signature postConcreteStateCurrentMethodSignature = postConcreteState.getCurrentMethodSignature();
                		Value postSymbolicStateTOS;
                		try { 
                			postSymbolicStateTOS = postSymbolicState.topOperand();
                		} catch (InvalidNumberOfOperandsException e) {
                			postSymbolicStateTOS = null;
                		}
                		
            			//if the guided execution skipped a method call execution and
            			//returned a SymbolicApply instead, remembers to save the
            			//concrete (guiding) state with the return value
                		final boolean saveRealignedConcreteState = 
                		(postConcreteStateStackSize == (postSymbolicStateStackSize + this.stackSizeOffset) + 1 &&
                		postSymbolicStateTOS != null && postSymbolicStateTOS instanceof SymbolicApply &&
                		postConcreteStateCurrentMethodSignature.toString().equals(((SymbolicApply) postSymbolicStateTOS).getOperator()));
                		
                		//realigns
                		while (this.engine.getCurrentState().getStackSize() != (postSymbolicStateStackSize + this.stackSizeOffset)) {
                			this.engine.step();
                		}
                		
                		//saves the concrete (guiding) state
                		if (saveRealignedConcreteState) {
                			this.statesConcrete.put(postSymbolicState.getHistoryPoint(), this.engine.getCurrentState().clone());
                			this.statesSymbolic.put(postSymbolicState.getHistoryPoint(), postSymbolicState.clone());
                		}
                	} else if (postConcreteStateStackSize < (postSymbolicStateStackSize + this.stackSizeOffset) &&
                	postSymbolicStateCurrentProgramCounter == 0) {
                		//misaligned: the guided execution entered a set of frames, but the guiding
                		//did not. Possible causes: triggers of the guided execution not active in
                		//the guiding execution (unlikely with snippets). We must suspend stepping
                		//this.engine() until the guided execution realigns with the guiding one.
                		this.aligned = false;
                	}
            	} else {
            		//misaligned: we need to check whether the 
            		//last step of the guided procedure realigned 
            		//with the guiding one
            		if (preConcreteStateStackSize == (postSymbolicStateStackSize + this.stackSizeOffset) &&
                	preConcreteStateCurrentProgramCounter == postSymbolicStateCurrentProgramCounter) {
            			this.aligned = true;
            		}
            	}            	
			} catch (EngineStuckException | ThreadStackEmptyException e) {
				throw new GuidanceException(ERROR_GUIDANCE_MISALIGNMENT);
			} catch (CannotManageStateException | NonexistingObservedVariablesException |
			         ClasspathException | ContradictionException | DecisionException |
			         FailureException e) {
				throw new GuidanceException(e);
			} catch (FrozenStateException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
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
    }
}
