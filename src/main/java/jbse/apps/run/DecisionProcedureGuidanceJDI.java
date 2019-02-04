package jbse.apps.run;

import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.isPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.StepRequest;

import jbse.bc.Opcodes;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_In;
import jbse.tree.DecisionAlternative_XALOAD_Out;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.SymbolicLocalVariable;
import jbse.val.SymbolicMemberArray;
import jbse.val.SymbolicMemberField;

/**
 * {@link DecisionProcedureGuidance} that uses the installed JVM accessed via JDI to 
 * perform concrete execution. 
 */
public final class DecisionProcedureGuidanceJDI extends DecisionProcedureGuidance {
    /**
     * Builds the {@link DecisionProcedureGuidanceJDI}.
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
    public DecisionProcedureGuidanceJDI(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature) 
    throws GuidanceException {
        this(component, calc, runnerParameters, stopSignature, 1);
    }

    /**
     * Builds the {@link DecisionProcedureGuidanceJDI}.
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
    public DecisionProcedureGuidanceJDI(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
    throws GuidanceException {
        super(component, calc, new JVMJDI(calc, runnerParameters, stopSignature, numberOfHits));
    }

    private static final class JVMJDI extends JVM {
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path.";
        private static final String[] EXCLUDES = {"java.*", "javax.*", "sun.*", "com.sun.*", "org.*"};
        
        private final String methodStart;
        private final String methodStop;  
        private final int numberOfHits;
        private final VirtualMachine vm;
        private boolean intoMethodRunnPar = false;
        private int hitCounter = 0;
        private MethodEntryEvent methodEntryEvent;
        private int numOfFramesAtMethodEntry;
        private StepEvent currentStepEvent;        
		private boolean jdiIsWaitingForJbseTerminateCustomExecution = false;
		
		private Map<ReferenceSymbolicApply, Object> unintFuncsNonPrimitiveRetValues = new HashMap<>();
        private boolean lookAheadAlreadyDone = false;
		private boolean lookAheadDecisionBoolean;
		private boolean lookAheadDecisionIsDefaultCase;
		private int lookAheadDecisionCaseValue;
		private ObjectReference lookAheadUnintFuncNonPrimitiveRetValue;

        public JVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            super(calc, runnerParameters, stopSignature, numberOfHits);
            this.methodStart = runnerParameters.getMethodSignature().getName();
            this.methodStop = stopSignature.getName();
            this.numberOfHits = numberOfHits;
            this.vm = createVM(runnerParameters, stopSignature);
            run();
        }
        
        private VirtualMachine createVM(RunnerParameters runnerParameters, Signature stopSignature) 
        throws GuidanceException {
        	try {
        		final Iterable<Path> classPath = runnerParameters.getClasspath().classPath();
        		final ArrayList<String> listClassPath = new ArrayList<>();
        		classPath.forEach(p -> listClassPath.add(p.toString()));
        		final String stringClassPath = String.join(File.pathSeparator, listClassPath.toArray(new String[0]));
        		final String mainClass = DecisionProcedureGuidanceJDILauncher.class.getName();
        		final String targetClass = binaryClassName(runnerParameters.getMethodSignature().getClassName());
        		return launchTarget("-classpath \"" + stringClassPath + "\" " + mainClass + " " + targetClass + " " + this.methodStart);
        	} catch (IOException e) {
        		throw new GuidanceException(e);
        	}
        }
        
        private VirtualMachine launchTarget(String mainArgs) throws GuidanceException {
            final LaunchingConnector connector = findLaunchingConnector();
            final Map<String, Connector.Argument> arguments = connectorArguments(connector, mainArgs);
            try {
                return connector.launch(arguments);
            } catch (IOException | IllegalConnectorArgumentsException | VMStartException exc) {
                throw new GuidanceException(exc);
            }
        }
        
        private Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String mainArgs) {
            final Map<String, Connector.Argument> arguments = connector.defaultArguments();
            final Connector.Argument mainArg = arguments.get("main");
            if (mainArg == null) {
                throw new Error("Bad launching connector");
            }
            mainArg.setValue(mainArgs);
            return arguments;
        }

        private void run() {
        	//sets event requests
            final EventRequestManager mgr = this.vm.eventRequestManager();
            final MethodEntryRequest menr = mgr.createMethodEntryRequest();
            for (String exclude : EXCLUDES) {
                menr.addClassExclusionFilter(exclude);
            }
            menr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            menr.enable();
            final MethodExitRequest mexr = mgr.createMethodExitRequest();
            for (String exclude : EXCLUDES) {
                mexr.addClassExclusionFilter(exclude);
            }
            mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            mexr.enable();
            
            //executes
            final EventQueue queue = this.vm.eventQueue();
            boolean testMethodEntryFound = false;
            while (!testMethodEntryFound) {
                try {
                    final EventSet eventSet = queue.remove();
                    final EventIterator it = eventSet.eventIterator();
                    while (!testMethodEntryFound && it.hasNext()) {
                        testMethodEntryFound = checkIfMethodEntry(it.nextEvent());
                    }
                    if (!testMethodEntryFound) {
                        eventSet.resume();
                    }
                } catch (InterruptedException e) {
                    //TODO
                } catch (VMDisconnectedException e) {
                    break;
                }
            }
            
            //disables event requests
            menr.disable();
            mexr.disable();
        }

        private boolean checkIfMethodEntry(Event event) {
            if (event instanceof MethodExitEvent && 
                (((MethodExitEvent) event).method().name().equals(this.methodStart))) {
                this.intoMethodRunnPar = false;
            }
            if (event instanceof MethodEntryEvent) {
                if (((MethodEntryEvent) event).method().name().equals(this.methodStart)) {
                    this.hitCounter = 0;
                    this.intoMethodRunnPar = true;
                }
                if (((MethodEntryEvent) event).method().name().equals(this.methodStop) && (this.intoMethodRunnPar)) {
                    ++this.hitCounter;
                    if (this.hitCounter == this.numberOfHits){
                        this.methodEntryEvent = (MethodEntryEvent) event;
                        try {
                        	this.numOfFramesAtMethodEntry = methodEntryEvent.thread().frameCount();
						} catch (IncompatibleThreadStateException e) {
							throw new UnexpectedInternalException(e); 
						}
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        private LaunchingConnector findLaunchingConnector() {
            final List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
            for (Connector connector : connectors) {
                if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
                    return (LaunchingConnector) connector;
                }
            }
            throw new Error("No launching connector");
        }
                
        private StackFrame rootFrameConcrete() throws IncompatibleThreadStateException {
        	final int numFramesFromRoot = numFramesFromRootFrameConcrete();
            return this.methodEntryEvent.thread().frames().get(numFramesFromRoot - 1);
        }
        
        private int numFramesFromRootFrameConcrete() throws IncompatibleThreadStateException {
        	final int numFrames = 1 + this.methodEntryEvent.thread().frameCount() - this.numOfFramesAtMethodEntry;
        	return numFrames;
        }
        
        @Override
        public boolean isCurrentMethodNonStatic() throws GuidanceException {
            try {
                return !rootFrameConcrete().location().method().declaringType().isStatic();
            } catch (IncompatibleThreadStateException e) {
                throw new GuidanceException(e);
            }
        }
        
        @Override
        public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
            if (object == null) {
                return null;
            }
            final StringBuilder buf = new StringBuilder();
            String name = object.referenceType().name();
            while (name.endsWith("[]")) {
                buf.append("[");
                name = name.substring(0, name.length() - 2);
            }
            buf.append(isPrimitiveOrVoidCanonicalName(name) ? toPrimitiveOrVoidInternalName(name) : internalClassName(name));
            return buf.toString();
        }
        
        @Override
        public boolean isNull(ReferenceSymbolic origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
            return (object == null);
        }
        
        @Override
        public boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException {
            final ObjectReference objectFirst = (ObjectReference) getJDIValue(first);
            final ObjectReference objectSecond = (ObjectReference) getJDIValue(second);
            return ((objectFirst == null && objectSecond == null) || 
                    (objectFirst != null && objectSecond != null && objectFirst.equals(objectSecond)));
        }

        @Override
        public Object getValue(Symbolic origin) throws GuidanceException {
            final com.sun.jdi.Value val = (com.sun.jdi.Value) getJDIValue(origin);
            if (val instanceof IntegerValue) {
                return this.calc.valInt(((IntegerValue) val).intValue());
            } else if (val instanceof BooleanValue) {
                return this.calc.valBoolean(((BooleanValue) val).booleanValue());
            } else if (val instanceof CharValue) {
                return this.calc.valChar(((CharValue) val).charValue());
            } else if (val instanceof ByteValue) {
                return this.calc.valByte(((ByteValue) val).byteValue());
            } else if (val instanceof DoubleValue) {
                return this.calc.valDouble(((DoubleValue) val).doubleValue());
            } else if (val instanceof FloatValue) {
                return this.calc.valFloat(((FloatValue) val).floatValue());
            } else if (val instanceof LongValue) {
                return this.calc.valLong(((LongValue) val).longValue());
            } else if (val instanceof ShortValue) {
                return this.calc.valShort(((ShortValue) val).shortValue());
            } else if (val instanceof ObjectReference) {
                return val;
            } else { //val instanceof VoidValue || val == null
                return null;
            }
        }
        
        /**
         * Returns a JDI object from the concrete state standing 
         * for a {@link Symbolic}.
         * 
         * @param origin a {@link Symbolic}.
         * @return either a {@link com.sun.jdi.Value}, or a {@link com.sun.jdi.ReferenceType}, or
         *         a {@link com.sun.jdi.ObjectReference}.
         * @throws GuidanceException
         */
        private Object getJDIValue(Symbolic origin) throws GuidanceException {
            try {
                if (origin instanceof SymbolicLocalVariable) {
                    return getJDIValueLocalVariable(((SymbolicLocalVariable) origin).getVariableName());
                } else if (origin instanceof KlassPseudoReference) {
                    return getJDIObjectStatic(((KlassPseudoReference) origin).getClassFile().getClassName());
                } else if (origin instanceof SymbolicMemberField) {
                    final Object o = getJDIValue(((SymbolicMemberField) origin).getContainer());
                    if (!(o instanceof com.sun.jdi.ReferenceType) && !(o instanceof com.sun.jdi.ObjectReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + ": " + origin.asOriginString());
                    }
                    return getJDIValueField(((SymbolicMemberField) origin).getFieldName(), o);
                } else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
                    final Object o = getJDIValue(((PrimitiveSymbolicMemberArrayLength) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + ": " + origin.asOriginString());
                    }
                    return this.vm.mirrorOf(((ArrayReference) o).length());
                } else if (origin instanceof SymbolicMemberArray) {
                    final Object o = getJDIValue(((SymbolicMemberArray) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + ": " + origin.asOriginString());
                    }
                    try {
                        final Simplex index = (Simplex) eval(((SymbolicMemberArray) origin).getIndex());
                        return ((ArrayReference) o).getValue(((Integer) index.getActualValue()).intValue());
                    } catch (ClassCastException e) {
                        throw new GuidanceException(e);
                    }
                } else if (origin instanceof PrimitiveSymbolicHashCode) {
                    final Object o = getJDIValue(((PrimitiveSymbolicHashCode) origin).getContainer());
                    if (!(o instanceof ObjectReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + ": " + origin.asOriginString());
                    }
                    final ObjectReference oRef = (ObjectReference) o;
                    return this.vm.mirrorOf(oRef.hashCode()); //TODO is this the identity (default) hash code?
                } else if (origin instanceof ReferenceSymbolicApply) {
                    if (this.unintFuncsNonPrimitiveRetValues.containsKey((ReferenceSymbolicApply) origin)) {
                    	return this.unintFuncsNonPrimitiveRetValues.get((ReferenceSymbolicApply) origin);
                    } else {
                    	//Implicit invariant: when we see a ReferenceSymbolicApply for the first time, JDI is at the call point of the corresponding function
            			exec_INVOKEX_lookAhead();
                        this.unintFuncsNonPrimitiveRetValues.put((ReferenceSymbolicApply) origin, this.lookAheadUnintFuncNonPrimitiveRetValue);
                        return this.lookAheadUnintFuncNonPrimitiveRetValue;
            		}	
                //else if (origin instanceof PrimitiveSymbolicApply): should never happen (primitive values are decided by stepping forward)
                } else {
                    throw new GuidanceException(ERROR_BAD_PATH + ": " + origin.asOriginString());
                }
            } catch (IncompatibleThreadStateException | AbsentInformationException e) {
                throw new GuidanceException(e);
            }
        }
        
        private com.sun.jdi.Value getJDIValueLocalVariable(String var) 
        throws GuidanceException, IncompatibleThreadStateException, AbsentInformationException {
            final com.sun.jdi.Value val;
            if ("this".equals(var)) {
                val = rootFrameConcrete().thisObject();
            } else {
                final LocalVariable variable = rootFrameConcrete().visibleVariableByName(var); 
                if (variable == null) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                val = rootFrameConcrete().getValue(variable);
            }
            return val;
        }
        
        private com.sun.jdi.ReferenceType getJDIObjectStatic(String className) 
        throws GuidanceException, IncompatibleThreadStateException, AbsentInformationException {
            final List<ReferenceType> classes = this.vm.classesByName(className);
            if (classes.size() == 1) {
                return classes.get(0);
            } else {
                throw new GuidanceException(ERROR_BAD_PATH);
            }
        }
        
        private com.sun.jdi.Value getJDIValueField(String fieldName, Object o) 
        throws GuidanceException {
            final Field fld = (o instanceof com.sun.jdi.ObjectReference ? ((com.sun.jdi.ObjectReference) o).referenceType().fieldByName(fieldName) : ((com.sun.jdi.ReferenceType) o).fieldByName(fieldName));
            if (fld == null) {
                throw new GuidanceException(ERROR_BAD_PATH);
            }
            try {
                return (o instanceof com.sun.jdi.ObjectReference ? ((com.sun.jdi.ObjectReference) o).getValue(fld) : ((com.sun.jdi.ReferenceType) o).getValue(fld));
            } catch (IllegalArgumentException e) {
                throw new GuidanceException(e);
            }
        }
        
        
		@Override
		public Primitive eval_IFX(DecisionAlternative_IFX da, Primitive condToEval) throws GuidanceException {			
			if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_IFX(da, condToEval);
			}

			exec_IFX_lookAhead();
				
			return calc.valBoolean(da.value() == this.lookAheadDecisionBoolean);
		}

        @Override
		public Primitive eval_XCMPY(DecisionAlternative_XCMPY da, Primitive val1, Primitive val2) throws GuidanceException {
			if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_XCMPY(da, val1, val2);
			}

        	throw new GuidanceException("Guidance JDI with stepping behavior does not yet support XCMPY comparisons except for those related to a subsequent IF-style decision");

        	/*TODO: currently we do not handle these queries. Yet, note that JBSE does not issue this query when the XCMPY operation is followed by a IF evaluation.
        	 * How should be managed:
			 * byte[] bc = getCurrentBytecode();
			 * int currentCodeIndex = getCurrentCodeIndex();
			 *
			 * if (!aStepAhead && bc[currentCodeIndex] < Opcodes.OP_LCMP &&  bc[currentCodeIndex] > Opcodes.OP_DCMPG) {
			 *   throw new GuidanceException("Wrong step alignment: JBSE is at CMP statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
			 * }
			 * ...
        	 */
        }
        
        
        @Override
        public Primitive eval_XSWITCH(DecisionAlternative_XSWITCH da, Primitive selector, SwitchTable tab) throws GuidanceException {
			if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_XSWITCH(da, selector, tab);
			}

			if (!this.lookAheadAlreadyDone) {
				exec_XSWITCH_lookAhead();
			}
				
        	return this.calc.valBoolean((da.isDefault() && this.lookAheadDecisionIsDefaultCase) || da.value() == this.lookAheadDecisionCaseValue);
        }
        
        @Override
        public Primitive eval_XNEWARRAY(DecisionAlternative_XNEWARRAY da, Primitive countsNonNegative) throws GuidanceException {
        	if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_XNEWARRAY(da, countsNonNegative);
			}

			if (!this.lookAheadAlreadyDone) {
				exec_XNEWARRAY_lookAhead();
			}
				
			return this.calc.valBoolean(da.ok() == this.lookAheadDecisionBoolean);
        }

        @Override
        public Primitive eval_XASTORE(DecisionAlternative_XASTORE da, Primitive inRange) throws GuidanceException {
        	if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_XASTORE(da, inRange);
			}

			if (!this.lookAheadAlreadyDone) {
				exec_XASTORE_lookAhead();
			}
				
			return this.calc.valBoolean(da.isInRange() == this.lookAheadDecisionBoolean);
        }

        @Override
        public Primitive eval_XALOAD(DecisionAlternative_XALOAD da) throws GuidanceException {
        	if (this.jdiIsWaitingForJbseTerminateCustomExecution) {
				return super.eval_XALOAD(da);
			}

			if (!this.lookAheadAlreadyDone) {
				exec_XALOAD_lookAhead();
			}
				
			return this.calc.valBoolean(
					(da instanceof DecisionAlternative_XALOAD_In && this.lookAheadDecisionBoolean) ||
					(da instanceof DecisionAlternative_XALOAD_Out && !this.lookAheadDecisionBoolean));
        
			//TODO: currently we do not handle the identification of the actual array range that is being loaded, thus the decision procedure may produce more than one alternative
        }

        /**
         * Does a single execution step of the concrete state.
         * 
         * @param doStepInto if {@code true} and the current bytecode is an INVOKEX bytecode,
         *        steps into the invoked method; if {@code false} and the current bytecode is 
         *        an INVOKEX bytecode, steps over.
         */
		private void doStep(boolean doStepInto) {
			final int stepDepth = doStepInto ? StepRequest.STEP_INTO : StepRequest.STEP_OVER;

			final ThreadReference thread = this.methodEntryEvent.thread();
			final EventRequestManager mgr = this.vm.eventRequestManager();
			final StepRequest sr = mgr.createStepRequest(thread, StepRequest.STEP_MIN, stepDepth);
			sr.enable();

			this.vm.resume();
			final EventQueue queue = this.vm.eventQueue();

			boolean stepFound = false;
			while (!stepFound) {
				try {
					final EventSet eventSet = queue.remove();
					final EventIterator it = eventSet.eventIterator();
					while (!stepFound && it.hasNext()) {
						final Event event = it.nextEvent();

						if (event instanceof StepEvent) {
							this.currentStepEvent = (StepEvent) event;
							stepFound = true;
						}
					}
					if (!stepFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					throw new UnexpectedInternalException(e);
				} catch (VMDisconnectedException e) {
					break;
				}
			}

			sr.disable();
		}        
        
        private void exec_INVOKEX_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] < Opcodes.OP_INVOKEVIRTUAL && bc[currentCodeIndex] > Opcodes.OP_INVOKEDYNAMIC) {
					throw new GuidanceException("Wrong step alignment: JBSE is at INVOKE statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				try {
					final int intialFrames = this.currentStepEvent.thread().frameCount();

					doStep(true); //true -> StepInto  
					final int currFrames = this.currentStepEvent.thread().frameCount();
					if (currFrames <= intialFrames) {
						throw new GuidanceException("Problem with INVOKE: I expected to step into a new frame");
					}

					this.lookAheadUnintFuncNonPrimitiveRetValue = (ObjectReference) stepUpToMethodExit();

					doStep(false); //false -> StepOver  
					final int currFrames2 = this.currentStepEvent.thread().frameCount();
					if (currFrames2 != intialFrames) {
						throw new GuidanceException("Problem with INVOKE: I expected to step into a new frame");
					}

				} catch (IncompatibleThreadStateException e) {
					throw new GuidanceException(e);
				}

				this.lookAheadAlreadyDone = true;
			}
        }


		private Value stepUpToMethodExit() throws IncompatibleThreadStateException {
			final int currFrames = this.currentStepEvent.thread().frameCount();
			
        	final EventRequestManager mgr = this.vm.eventRequestManager();
            final MethodExitRequest mexr = mgr.createMethodExitRequest();
            mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            mexr.enable();
            
        	this.vm.resume();
        	final EventQueue queue = this.vm.eventQueue();

            MethodExitEvent mthdExitEvent = null;
        	boolean exitFound = false;
        	while (!exitFound) {
        		try {
        			final EventSet eventSet = queue.remove();
        			final EventIterator it = eventSet.eventIterator();
        			while (!exitFound && it.hasNext()) {
        				Event event = it.nextEvent();

        				if (event instanceof MethodExitEvent) {
        					mthdExitEvent = (MethodExitEvent) event;
        					if (mthdExitEvent.thread().frameCount() == currFrames) { 
        						exitFound = true;
        					}
        				}
        			}
        			if (!exitFound) {
        				eventSet.resume();
        			}
        		} catch (InterruptedException e) {
        			// TODO
        		} catch (VMDisconnectedException e) {
        			break;
        		}
        	}

        	mexr.disable();
			return mthdExitEvent.returnValue();
		}
        
        private void exec_IFX_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] < Opcodes.OP_IFEQ && bc[currentCodeIndex] > Opcodes.OP_IF_ACMPNE) {
					throw new GuidanceException("Wrong step alignment: JBSE is at IF statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				final int jumpOffset = currentCodeIndex + bytecodesToInt(bc, currentCodeIndex + 1, 2);
				final int newOffset = lookAhead();
				this.lookAheadDecisionBoolean = (newOffset == jumpOffset); 
			}
        }
                
        private void exec_XSWITCH_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] != Opcodes.OP_LOOKUPSWITCH && bc[currentCodeIndex] != Opcodes.OP_TABLESWITCH) {
					throw new GuidanceException("Wrong step alignment: JBSE is at SWITCH statament, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				final int newOffset = lookAhead();
				final int padding = 3 - (currentCodeIndex % 4);
				int nextParamStartIndex = currentCodeIndex + padding + 1;

				final int defaultCaseOffset = currentCodeIndex + bytecodesToInt(bc, nextParamStartIndex, 4);
				nextParamStartIndex += 4;
				if (newOffset == defaultCaseOffset) {
					this.lookAheadDecisionIsDefaultCase = true; 
					return;
				} 
				this.lookAheadDecisionIsDefaultCase = false; 

				if (bc[currentCodeIndex] == Opcodes.OP_LOOKUPSWITCH) {
					int npairs = bytecodesToInt(bc, nextParamStartIndex, 4); 
					nextParamStartIndex += 4;		

					for (int i = 0; i < npairs; i++, nextParamStartIndex += 8) {
						final int caseValue = bytecodesToInt(bc, nextParamStartIndex, 4); 
						final int caseOffset = currentCodeIndex + bytecodesToInt(bc, nextParamStartIndex + 4, 4); 

						if (newOffset == caseOffset) {
							this.lookAheadDecisionCaseValue = caseValue;
							return;
						}
					}
				} else { //Opcodes.TABLESWITCH
					final int low = bytecodesToInt(bc, nextParamStartIndex, 4); 
					final int high = bytecodesToInt(bc, nextParamStartIndex + 4, 4); 
					final int entries = high - low; 
					nextParamStartIndex += 8;

					for (int i = 0; i < entries; i++, nextParamStartIndex += 4) {
						final int caseValue = low + i; 
						final int caseOffset = currentCodeIndex + bytecodesToInt(bc, nextParamStartIndex, 4); 
						if (newOffset == caseOffset) {
							this.lookAheadDecisionCaseValue = caseValue;
							return;
						}
					}
				}

				throw new GuidanceException("Wrong step at SWITCH: offset " + newOffset + " does not correspond to any case/default jump");
			}
        }

        private void exec_XALOAD_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] < Opcodes.OP_IALOAD && bc[currentCodeIndex] > Opcodes.OP_SALOAD) {
					throw new GuidanceException("Wrong step alignment: JBSE is at XALOAD statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				checkAdvanceToImmediateSuccessor(currentCodeIndex + 1);
			}
        }

        private void exec_XNEWARRAY_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] != Opcodes.OP_NEWARRAY && bc[currentCodeIndex] != Opcodes.OP_ANEWARRAY) {
					throw new GuidanceException("Wrong step alignment: JBSE is at XNEWARRAY statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				checkAdvanceToImmediateSuccessor(currentCodeIndex + (bc[currentCodeIndex] == Opcodes.OP_NEWARRAY ? 2 : 3));
			}
        }

        private void exec_XASTORE_lookAhead() throws GuidanceException {
			if (!this.lookAheadAlreadyDone) {
				final byte[] bc = getCurrentBytecode();
				final int currentCodeIndex = getCurrentCodeIndex();

				if (bc[currentCodeIndex] < Opcodes.OP_IASTORE && bc[currentCodeIndex] > Opcodes.OP_SASTORE) {
					throw new GuidanceException("Wrong step alignment: JBSE is at XASTORE statement, while JDI's OPCODE is " + bc[currentCodeIndex]);
				}

				checkAdvanceToImmediateSuccessor(currentCodeIndex + 1);
			}
        }
        
        private void checkAdvanceToImmediateSuccessor(long successorOffset) throws GuidanceException {
        	final int newOffset = lookAhead();
        	this.lookAheadDecisionBoolean = (newOffset == successorOffset); 
        }

        private int lookAhead() {
        	doStep(false); //false -> StepOver
        	this.lookAheadAlreadyDone = true;
        	return getCurrentCodeIndex();
        } 
                
		private int bytecodesToInt(byte[] bytecode, int firstByte, int numOfBytes) { //TODO numOfBytes can be 2 or 4, use Util functions instead
        	final byte[] bytes = Arrays.copyOfRange(bytecode, firstByte, firstByte + numOfBytes);
        	final BigInteger num = new BigInteger(bytes);
        	return num.intValue();
        }

        private byte[] getCurrentBytecode() throws GuidanceException {
        	if (this.currentStepEvent != null) {
        		return this.currentStepEvent.location().method().bytecodes();
        	} else if (this.methodEntryEvent != null) {
        		return this.methodEntryEvent.location().method().bytecodes();        		
        	} else {
				throw new GuidanceException("Unexpected JDI failure: current method entry not known ");
        	}
		}

        private int getCurrentCodeIndex() {
        	if (this.currentStepEvent != null) {
        		return (int) this.currentStepEvent.location().codeIndex();
        	} else if (this.methodEntryEvent != null) {
        		return (int) this.methodEntryEvent.location().codeIndex();        		
        	} else {
				return -1;
        	}
		}

        private boolean jdiSameMethodAsJBSE(State jbseState) throws FrozenStateException, ThreadStackEmptyException, IncompatibleThreadStateException {
			final int jbseStackSize = jbseState.getStackSize();
			final Signature jbseMeth = jbseState.getCurrentMethodSignature();
        	final String jbseMethClassname = jbseMeth.getClassName() == null ? null : jbseMeth.getClassName().replace('/', '.');
        	final String jbseMethName = jbseMeth.getName() == null ? null : jbseMeth.getName();
        	final String jbseMethDescr = jbseMeth.getDescriptor() == null ? null : jbseMeth.getDescriptor();
        	
        	final int jdiStackSize = numFramesFromRootFrameConcrete();
        	final Method jdiMeth = currentStepEvent.location().method();
        	final String jdiMethName = jdiMeth.name();
        	final String jdiMethDescr = jdiMeth.signature();
        	final String jdiMethClassname = jdiMeth.toString().substring(0, jdiMeth.toString().indexOf(jdiMethName) - 1);

        	return jdiStackSize == jbseStackSize && jdiMethName.equals(jbseMethName) && 
        		   jdiMethDescr.equals(jbseMethDescr) && jdiMethClassname.equals(jbseMethClassname);
        }
        
        @Override
		public void step(State jbseState) throws GuidanceException {
			try {
				if (!this.jdiIsWaitingForJbseTerminateCustomExecution) {
					if (this.lookAheadAlreadyDone) { 
						this.lookAheadAlreadyDone = false; // re-alignment after lookahead
						checkAlignmentWithJbseOrThrowException(jbseState);
					} else {

						final int jbseStackSize = jbseState.getStackSize();
						if (jbseStackSize == 0) {
							return; //avoid stepping JDI since JBSE terminated
						}
						final int jdiStackSizeBeforeStep = numFramesFromRootFrameConcrete();
						final boolean doStepInto = jbseStackSize > jdiStackSizeBeforeStep;
						doStep(doStepInto);  //true -> StepInto, false -> StepOver

						if (!jdiSameMethodAsJBSE(jbseState)) {
							// PANIC: optimistically, we may assume that JBSE is doing some operations related
							//	  with a customized handling of what is happening in the current method,
							//	  and thus we try to wait until it terminates with executing this method, and
							//	  returns back to the preceding stack frame. If JBSE aligns with
							//	  JDI at that point (see else case below), we believe it is again safe to proceed.
							stepUpToMethodExit();
							doStep(false); //false -> StepOver
							this.jdiIsWaitingForJbseTerminateCustomExecution = true;
						} else {
							checkAlignmentWithJbseOrThrowException(jbseState);						}
						}
				} else  {
					// This is the optimistic handling in case of PANIC
					if (jdiSameMethodAsJBSE(jbseState)) {
						checkAlignmentWithJbseOrThrowException(jbseState); // Exception -> Yes, PANIC! 
						this.jdiIsWaitingForJbseTerminateCustomExecution = false; // No more PANIC 
					}
				}
        	} catch (ThreadStackEmptyException | IncompatibleThreadStateException | FrozenStateException e) {
				throw new GuidanceException(e); //TODO better exception!
        	}
		}

        private void checkAlignmentWithJbseOrThrowException(State jbseState) throws FrozenStateException, GuidanceException, ThreadStackEmptyException {
        	if (getCurrentCodeIndex() != jbseState.getPC()) {
				throw new GuidanceException("JDI alignment with JBSE failed: JBSE is at " + jbseState.getCurrentMethodSignature() + ":" + jbseState.getPC() + ", while JDI is at " + this.currentStepEvent.location().method() + ":" + getCurrentCodeIndex());
			}	
        }

        @Override
        protected void close() {
            this.vm.exit(0);

            //obviates to inferior process leak
            this.vm.process().destroyForcibly();
        }
    }
}

