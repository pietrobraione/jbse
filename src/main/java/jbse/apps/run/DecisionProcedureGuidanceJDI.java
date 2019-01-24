package jbse.apps.run;

import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.binaryPrimitiveClassNameToInternal;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.isPrimitiveBinaryClassName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ByteValue;
import com.sun.jdi.CharValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.DoubleValue;
import com.sun.jdi.Field;
import com.sun.jdi.FloatValue;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
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
import com.sun.tools.jdi.StringReferenceImpl;

import jbse.algo.exc.CannotManageStateException;
//import jbse.apps.run.Run.ActionsRun;
import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.mem.Frame;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.val.Calculator;
import jbse.val.HistoryPoint;
import jbse.val.KlassPseudoReference;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.SymbolicLocalVariable;
import jbse.val.SymbolicMemberArray;
import jbse.val.SymbolicMemberField;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * {@link DecisionProcedureGuidance} that uses the installed JVM accessed via
 * JDI to perform concrete execution.
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

	/*
	 * MAME: ho modificato la visibilità della classe (da private a public):
	 * questo per avere a disposizione gli stati.
	 */
     
    public static class JVMJDI extends JVM {
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path.";
        private static final String[] EXCLUDES = {"java.*", "javax.*", "sun.*", "com.sun.*", "org.*"};
        
        private final String methodStart;
        private final String methodStop;  
        private final int numberOfHits;
        private final VirtualMachine vm;
        private boolean intoMethodRunnPar = false;
        private int hitCounter = 0;
        private MethodEntryEvent methodEntryEvent;
		private boolean oneStepAhead = false;
		private boolean lastConcreteDecision;
		
		
		//LUCA: varibile di classe per memorizzare info sullo stato corrente dello step
		private StepEvent currentStepEvent;
		
		//LUCA: ridefinito metodo eval per fare la valutazione in base al nextBytecode nel concreto
		@Override
		public Primitive eval(boolean branchToEval, Primitive toEval) throws GuidanceException {
			if (!oneStepAhead) {
				try {
					lastConcreteDecision = ConcreteDecision();
					oneStepAhead = true;
				} catch (CannotManageStateException | ThreadStackEmptyException e) {
					throw new GuidanceException(e);
				}
			}

			try {
				return Simplex.make(calc, new Boolean(branchToEval == lastConcreteDecision));
			} catch (jbse.val.exc.InvalidTypeException | InvalidOperandException e) {
				throw new GuidanceException (e);
			}

		}
		
		// LUCA: metodo ConcreteDecision esegue la valutazione sul next bytecode
		private boolean ConcreteDecision() throws CannotManageStateException, ThreadStackEmptyException, GuidanceException {
        	
			 byte[] bc = currentStepEvent.location().method().bytecodes();
			 int currentCodeIndex = (int) currentStepEvent.location().codeIndex();
        	 byte b1 = bc[currentCodeIndex + 1];
             byte b2=bc[currentCodeIndex+2];
             short offset=(short) ((b1 << 8) + b2);
             
             long codeIndexJump=  currentCodeIndex + offset;
             
             step();
             if(codeIndexJump==currentStepEvent.location().codeIndex()) {
            	 return true;
             }else{
            	 return false;
             }
 		
		 }

		/*
		 *MAME: metodo saveState che permette di salvare gli stati attraversati
		 * durante l'esecuzione.
		 */
		private static ArrayList<State> allStates = new ArrayList<State>();

		public static void saveState(State state) {
			State cloneState = state.clone();
			allStates.add(cloneState);
		}

		/* CODICE ORIGINALE: */
        
        public JVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            super(calc, runnerParameters, stopSignature, numberOfHits);
            this.methodStart = runnerParameters.getMethodSignature().getName();
            this.methodStop = stopSignature.getName();
            this.numberOfHits = numberOfHits;
            this.vm = createVM(runnerParameters, stopSignature);
            setEventRequests();
            run();
        }
        
		//LUCA : definito le seguenti variabili d'istanza
				private  ThreadReference thread;
				private  int stepSize;
				private  int stepDepth;
		       
		//LUCA: metodo step() per gestire le richieste di step per JDI
				
		@Override
		public void step() 
				throws GuidanceException, CannotManageStateException, ThreadStackEmptyException {
			
			if (oneStepAhead) {
				oneStepAhead = false;
				return;
			}
			
            
            this.thread = this.methodEntryEvent.thread();
			this.stepSize = StepRequest.STEP_MIN;
			this.stepDepth = StepRequest.STEP_INTO;
					
			StepRequest sr = this.vm.eventRequestManager().createStepRequest(this.thread, this.stepSize,
					this.stepDepth);
		
		    sr.enable();
			this.vm.resume();
			EventQueue queue1 = this.vm.eventQueue();
			boolean stepFound = false;
			while (!stepFound) {
				try {
					final EventSet eventSet = queue1.remove();
					final EventIterator it = eventSet.eventIterator();
					while (!stepFound && it.hasNext()) {
						Event event = it.nextEvent();
						
						if (event instanceof StepEvent) {
								currentStepEvent = (StepEvent) event;
								stepFound= true;
						}

					}
					if (!stepFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					// TODO
				} catch (VMDisconnectedException e) {
					break;
				}
			}
			sr.disable();
            
		}
		
		//CODICE ORIGINALE
		
        private StackFrame rootFrameConcrete() throws IncompatibleThreadStateException {
        	return this.methodEntryEvent.thread().frames().get(0);
        }
        
        private VirtualMachine createVM(RunnerParameters runnerParameters, Signature stopSignature) 
        throws GuidanceException {
            final Iterable<String> classPath = runnerParameters.getClasspath().classPath();
            final ArrayList<String> listClassPath = new ArrayList<>();
            classPath.forEach(listClassPath::add);
            final String stringClassPath = String.join(File.pathSeparator, listClassPath.toArray(new String[0]));
            final String mainClass = DecisionProcedureGuidanceJDILauncher.class.getName();
            final String targetClass = binaryClassName(runnerParameters.getMethodSignature().getClassName());
            return launchTarget("-classpath \"" + stringClassPath + "\" " + mainClass + " " + targetClass + " " + this.methodStart);
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
		
		private Map<String, Connector.Argument> connectorArguments(
				LaunchingConnector connector, String mainArgs) {
			final Map<String, Connector.Argument> arguments = connector
					.defaultArguments();
			final Connector.Argument mainArg = arguments.get("main");
			if (mainArg == null) {
				throw new Error("Bad launching connector");
			}
			mainArg.setValue(mainArgs);
			return arguments;
		}

		private void setEventRequests() {
			final EventRequestManager mgr = this.vm.eventRequestManager();
			
			//CODICE ORIGINALE
			
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
			
		}

		private void run() {
			final EventQueue queue = this.vm.eventQueue();
				boolean testMethodEntryFound = false;
			while (!testMethodEntryFound) {
				try {
					final EventSet eventSet = queue.remove();
					final EventIterator it = eventSet.eventIterator();
					while (!testMethodEntryFound && it.hasNext()) {
						testMethodEntryFound = checkIfMethodEntry(it
								.nextEvent());
					}
					if (!testMethodEntryFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					// TODO
				} catch (VMDisconnectedException e) {
					break;
				}
			}
		   
		 		}
                    
		private boolean checkIfMethodEntry(Event event) {

			if (event instanceof MethodExitEvent
					&& (((MethodExitEvent) event).method().name()
							.equals(this.methodStart))) {
					this.intoMethodRunnPar = false;
			}
			if (event instanceof MethodEntryEvent) {
				if (((MethodEntryEvent) event).method().name()
						.equals(this.methodStart)) {
					this.hitCounter = 0;
					this.intoMethodRunnPar = true;
				}
				if (((MethodEntryEvent) event).method().name()
						.equals(this.methodStop)
						&& (this.intoMethodRunnPar)) {
					++this.hitCounter;
					if (this.hitCounter == this.numberOfHits) {
						this.methodEntryEvent = (MethodEntryEvent) event;
						return true;
					}
					return false;
				}
			}
			return false;
		}

		private LaunchingConnector findLaunchingConnector() {
			final List<Connector> connectors = Bootstrap
					.virtualMachineManager().allConnectors();
			for (Connector connector : connectors) {
				if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
					return (LaunchingConnector) connector;
				}
			}
			throw new Error("No launching connector");
		}

		@Override
		public boolean isCurrentMethodNonStatic() throws GuidanceException {
			try {
				return !rootFrameConcrete().location().method().declaringType()
						.isStatic();
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
            buf.append(isPrimitiveBinaryClassName(name) ? binaryPrimitiveClassNameToInternal(name) : internalClassName(name));
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
            } 
			/* MAME: per metodo toString() */
			else if (origin instanceof ReferenceSymbolicApply) {
				ReferenceSymbolicApply rsa = (ReferenceSymbolicApply) origin;
				if (rsa.getOperator().equals("TO_STRING")) {
					if (val instanceof ObjectReference) {
						return new Object();
					} else {
						return null;
					}
				} else {
					return null;
				}
			}
			/* MAME FINE */
			else if (val instanceof ObjectReference) {
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
                    return getJDIObjectStatic(((KlassPseudoReference) origin).getClassName());
                } else if (origin instanceof SymbolicMemberField) {
                    final Object o = getJDIValue(((SymbolicMemberField) origin).getContainer());
                    if (!(o instanceof com.sun.jdi.ReferenceType) && !(o instanceof com.sun.jdi.ObjectReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    return getJDIValueField(((SymbolicMemberField) origin).getFieldName(), o);
                } else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
                    final Object o = getJDIValue(((PrimitiveSymbolicMemberArrayLength) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    return this.vm.mirrorOf(((ArrayReference) o).length());
                } else if (origin instanceof SymbolicMemberArray) {
                    final Object o = getJDIValue(((SymbolicMemberArray) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH);
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
                        throw new GuidanceException(ERROR_BAD_PATH);
                    }
                    final ObjectReference oRef = (ObjectReference) o;
                    final Method hashCode = oRef.referenceType().methodsByName("hashCode", "()I").get(0);
                    return oRef.invokeMethod(this.methodEntryEvent.thread(), hashCode, Collections.emptyList(), 0);
                /*} else if (origin instanceof PrimitiveSymbolicApply) {
                    * 1- let's assume the symbol is f(arg1, ..., argn)@historyPoint
                    * 2- see if we have a JDI VirtualMachine that is stopped at historyPoint
                    * 3- if not, create a new JDI VirtualMachine and a new JBSE, make them start at the initial state, then step both until we arrive at history point
                    * 4- now concretely execute f(arg1, ..., argn) and get the return value
                    * 5- finally, return it
                    *
                } else if (origin instanceof ReferenceSymbolicApply) {
                    TODO same as above */
 				/*
				 * MAME: gestione del metodo equals (per stringhe): si recupera
				 * historyPoint e i valori (che nel caso di equals saranno
				 * sempre due). Si valuta quanto valgono le due stringhe e si
				 * effettua equals, restituendo 1 o 0 a seconda del risultato di
				 * equals.
				 */

				/*
				 * da risolvere: gestione dei null nel secondo elemento (se
				 * l'elemento è concreto). da fare: gestione equals con oggetti.
				 */

				} else if (origin instanceof PrimitiveSymbolicApply) {
					String operator = ((PrimitiveSymbolicApply) origin)
							.getOperator();
					HistoryPoint hp = origin.historyPoint();
					PrimitiveSymbolicApply value = ((PrimitiveSymbolicApply) origin);

					Object obj = this.vm.mirrorOf(0);

					jbse.val.Value[] v = value.getArgs();
					if (operator.equals("EQUALS")) {

						jbse.val.Value first_v = v[0];
						Object first = getValueArgs(hp, first_v);

						jbse.val.Value second_v = v[1];
						Object second = getValueArgs(hp, second_v);

						
						if (second != null) {
							String new_first = first.toString().replace("\"",
									"");
							String new_second = second.toString().replace("\"",
									"");

							boolean checkEquals = new_first.equals(new_second);
							if (checkEquals) {
								obj = this.vm.mirrorOf(1);
							}
						}
					}

					return obj;
				}

				/*
				 * MAME: gestione del metodo toString: se si lavora con un
				 * simbolo, si riesce ad eseguire il metodo toString() per ogni
				 * oggetto. Se si ha un riferimento concreto, si può ottenere
				 * manualmente il toString() soltanto per le stringhe (comunque,
				 * non per tutte le classi: potrebbe in ogni caso essere una
				 * situazione non così ricorrente nei programmi che si possono
				 * testare).
				 */

				else if (origin instanceof ReferenceSymbolicApply) {

					String operator = ((ReferenceSymbolicApply) origin)
							.getOperator();
					HistoryPoint hp = origin.historyPoint();
					ReferenceSymbolicApply value = ((ReferenceSymbolicApply) origin);
					jbse.val.Value[] v = value.getArgs();
					Object obj = null;
					ObjectReference oRef;
					jbse.val.Value val = v[0];

					if (operator.equals("TO_STRING")) {
						if (val.isSymbolic()) {
							Symbolic s = (Symbolic) val;
							obj = getJDIValue(s);
							oRef = (ObjectReference) obj;
							Method toString = oRef
									.referenceType()
									.methodsByName("toString",
											"()Ljava/lang/String;").get(0);
							obj = oRef.invokeMethod(
									this.methodEntryEvent.thread(), toString,
									Collections.emptyList(), 0);
						}

						else {
							String obj2 = (String) getValueReferenceConcrete(
									hp, val);
							if (obj2 != null)
								obj = this.vm.mirrorOf(obj2);
						}
					}
					return obj;
				}

				/* CODICE ORIGINALE */
                else {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
            } catch (IncompatibleThreadStateException | AbsentInformationException | 
            com.sun.jdi.InvalidTypeException | ClassNotLoadedException | 
            InvocationException e) {
                throw new GuidanceException(e);
            }
        }
		/*
		 * MAME: metodo getValueArgs che restituisce il valore concreto di
		 * value: se è un simbolo, si chiama getJDIValue. Se è un reference
		 * concreto, si preleva lo stato a cui si è giunti (con history point),
		 * si prende l'heap di quello stato, si prende la posizione dell'oggetto
		 * (Object[4] avrà posizione 4) e si recupera l'objekt nello heap alla
		 * posizione individuata (meno uno; con 4, la posizione è 3). Se si
		 * tratta di stringhe, trasformo l'objekt in array, mi recupero i suoi
		 * valori e mi costruisco la stringa che compone l'array, restituendo
		 * quella stringa.
		 */

		/*
		 * DA FARE: capire come restituire una copia dell'oggetto se questo non
		 * è una stringa (ad esempio, se ho un reference concreto di tipo
		 * java/lang/Object, avendo una copia si può restituire e si ha un
		 * risultato finale).
		 */

		private Object getValueArgs(HistoryPoint hp, jbse.val.Value value)
				throws GuidanceException {
			if (value.isSymbolic()) {

				Symbolic s1 = (Symbolic) value;
				Object obj_first = getJDIValue(s1);

				return obj_first;

			} else {
				return getValueReferenceConcrete(hp, value);
			}
		}

		private Object getValueReferenceConcrete(HistoryPoint hp,
				jbse.val.Value value) {
			ReferenceConcrete rc = (ReferenceConcrete) value;
			if (!(rc.isNull())) {
				for (State st : allStates) {
					if (st.getHistoryPoint().equals(hp)) {
					long heapPosition = rc.getHeapPosition();
						Map<Long, Objekt> heap = st.getHeap();
						Objekt objekt = heap.get(heapPosition - 1);
						if (objekt.getType().equals("[C")) {
							Array a = (Array) objekt;
							List<AccessOutcomeIn> values = a.values();
							String obj_second = buildString(values);
							return obj_second;
						}
					}
				}
			}
			return null;
		}

		/*
		 * MAME: metodo buildString che permette di costruire la stringa di un
		 * elemento Object[2] (solo se questo è una stringa).
		 */

		private String buildString(List<AccessOutcomeIn> values) {

			String p1 = "";
			for (AccessOutcomeIn accOut : values) {
				p1 = p1.concat(accOut.getValue().toString());
			}
			return p1;
		}

		/* CODICE ORIGINALE */

        
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
        protected void finalize() {
            //obviates to inferior process leak
            this.vm.process().destroyForcibly();
        }
    }
}
