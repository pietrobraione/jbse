package jbse.apps.run;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.apps.run.JAVA_MAP_Utils.classImplementsJavaUtilMap;
import static jbse.apps.run.JAVA_MAP_Utils.isInitialMapField;
import static jbse.apps.run.JAVA_MAP_Utils.isSymbolicApplyOnInitialMap;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSKEY;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.isPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

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
import com.sun.jdi.Location;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodExitRequest;

import jbse.bc.ClassFile;
import jbse.bc.Offsets;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.SymbolicApply;
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
	 * @throws InvalidInputException if {@code component == null}.
	 */
	public DecisionProcedureGuidanceJDI(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature) 
	throws GuidanceException, InvalidInputException {
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
	 *        will stop at the entry of the {@code numberOfHits}-th nonrecursive invocation of 
	 *        the method whose signature is {@code stopSignature}, and the reached state will 
	 *        be used to answer queries.
	 * @param numberOfHits an {@code int} greater or equal to one.
	 * @throws GuidanceException if something fails during creation (and the caller
	 *         is to blame).
	 * @throws InvalidInputException if {@code component == null}.
	 */
	public DecisionProcedureGuidanceJDI(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
	throws GuidanceException, InvalidInputException {
		super(component, new JVMJDI(calc, runnerParameters, stopSignature, numberOfHits));
	}
	
	@Override
	public void setCurrentStateSupplier(Supplier<State> currentStateSupplier) {
		super.setCurrentStateSupplier(currentStateSupplier);
		((JVMJDI) this.jvm).setCurrentStateSupplier(this.currentStateSupplier);
	}

	/**
	 * Calculates the number of nonrecursive hits of a method.
	 *  
	 * @param runnerParameters the {@link RunnerParameters} of a concrete execution.
	 * @param stopSignature the {@link Signature} of a method.
	 * @return an {@code int} that amounts to the total number of nonrecursive 
	 *         invocations of the method whose signature is {@code stopSignature} 
	 *         from the concrete execution started by {@code runnerParameters}.
	 * @throws GuidanceException if something fails during creation (and the caller
	 *         is to blame).
	 */
	public static int countNonRecursiveHits(RunnerParameters runnerParameters, Signature stopSignature) 
	throws GuidanceException {
		final JVMJDI jdiCompleteExecution = new JVMJDI(runnerParameters, stopSignature);
		return jdiCompleteExecution.hitCounter;
	}
    
	private static interface IJVMJDIContext {
		public ThreadReference getCurrentThread() throws GuidanceException;
	}

	private static class JVMJDI extends JVM implements IJVMJDIContext {
		private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path: ";

		StreamRedirectThread outThread = null; 
		StreamRedirectThread errThread = null; 

		protected VirtualMachine vm;
		private BreakpointRequest breakpoint;
		protected int hitCounter;
		private int numOfFramesAtMethodEntry;
		protected Event currentExecutionPointEvent;        
		private Map<String, ReferenceType> alreadyLoadedClasses = new HashMap<>();

		private final RunnerParameters runnerParameters;
		private final Signature stopSignature;
		private final int numberOfHits;
		
		//Handling of uninterpreted functions
		protected LinkedHashMap<SymbolicApply, ISymbolicApplyJVMJDIContext> symbolicApplyCache = new LinkedHashMap<>(); // LinkedHashMap to maintain the entries in order of insertion
		private Map<String, List<String>> symbolicApplyOperatorOccurrences = new HashMap<>();
		private String currentHashMapModelMethod;
		
		private Supplier<State> currentStateSupplier = null;
		
		public JVMJDI(RunnerParameters runnerParameters, Signature stopSignature) 
		throws GuidanceException {
			super(null, runnerParameters, stopSignature, Integer.MAX_VALUE);
			this.runnerParameters = runnerParameters;
			this.stopSignature = stopSignature;
			this.numberOfHits = Integer.MAX_VALUE;
			this.vm = createVM();
			try {
				goToBreakpoint(stopSignature, 0, Integer.MAX_VALUE);			
			} catch (GuidanceException e) {
                                //obviates to inferior process leak
                                this.vm.process().destroyForcibly();
                                this.vm = null;
				return;
			}
			throw new GuidanceException("This constructor continues the execution up to termination, thus JDI will throw an exception eventually upon disconnecting.");
		}
		
		void setCurrentStateSupplier(Supplier<State> currentStateSupplier) {
			this.currentStateSupplier = currentStateSupplier;
		}

		public JVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
		throws GuidanceException {
			super(calc, runnerParameters, stopSignature, numberOfHits);
			this.runnerParameters = runnerParameters;
			this.stopSignature = stopSignature;
			this.numberOfHits = numberOfHits;
			this.vm = createVM();
			goToBreakpoint(stopSignature, 0, numberOfHits);
			try {
				this.numOfFramesAtMethodEntry = getCurrentThread().frameCount();
			} catch (IncompatibleThreadStateException e) {
				throw new UnexpectedInternalException(e); 
			}
			this.outThread = redirect("Subproc stdout", this.vm.process().getInputStream(), System.out);
			this.errThread = redirect("Subproc stderr", this.vm.process().getErrorStream(), System.err);
		}
		
		private StreamRedirectThread redirect(String name, InputStream in, OutputStream out) {
			StreamRedirectThread t = new StreamRedirectThread(name, in, out);
			t.setDaemon(true);
			t.start();
			return t;
		}
		
		/**
		 * StreamRedirectThread is a thread which copies its input to
		 * its output and terminates when it completes.
		 *
		 * @author Robert Field
		 */
		private static class StreamRedirectThread extends Thread {

			private final Reader in;
			private final Writer out;

			private static final int BUFFER_SIZE = 2048;

			/**
			 * Set up for copy.
			 * @param name  Name of the thread
			 * @param in    Stream to copy from
			 * @param out   Stream to copy to
			 */
			StreamRedirectThread(String name, InputStream in, OutputStream out) {
				super(name);
				this.in = new InputStreamReader(in);
				this.out = new OutputStreamWriter(out);
				setPriority(Thread.MAX_PRIORITY - 1);
			}

			/**
			 * Copy.
			 */
			@Override
			public void run() {
				try {
					char[] cbuf = new char[BUFFER_SIZE];
					int count;
					while ((count = this.in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
					    this.out.write(cbuf, 0, count);
					}
					this.out.flush();
				} catch (IOException exc) {
					//do nothing
				}
			}

			public void flush() {
				try {
				    this.out.flush();
				} catch (IOException exc) {
					//do nothing
				}
			}
		}


		private VirtualMachine createVM() 
		throws GuidanceException {
			try {
				final Iterable<Path> classPath = this.runnerParameters.getClasspath().userClassPath();
				final ArrayList<String> listClassPath = new ArrayList<>();
				classPath.forEach(p -> listClassPath.add(p.toString()));
				final String stringClassPath = String.join(File.pathSeparator, listClassPath.toArray(new String[0]));
				final String mainClass = DecisionProcedureGuidanceJDILauncher.class.getName();
				final String targetClass = binaryClassName(this.runnerParameters.getMethodSignature().getClassName());
				final String startMethodName = this.runnerParameters.getMethodSignature().getName();
				return launchTarget("-classpath \"" + stringClassPath + File.pathSeparator + this.runnerParameters.getClasspath().jbseLibPath() + "\" " + mainClass + " " + targetClass + " " + startMethodName);
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

		protected void goToBreakpoint(Signature sig, int offset, int numberOfHits) throws GuidanceException {
			//System.out.println("*** moveJdiToCurrentExecutionPointOfJbse: " + jbseLocationAhead.sig + "::" + jbseLocationAhead.pc + " (occurrence " + numberOfHits + ")");

			//sets event requests
			final EventRequestManager mgr = this.vm.eventRequestManager();
			final ClassPrepareRequest cprr = mgr.createClassPrepareRequest();
			cprr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			cprr.enable();

			for (ReferenceType classType: this.vm.allClasses()) {
			    this.alreadyLoadedClasses.put(classType.name().replace('.', '/'), classType);
				//System.out.println("ClassLOADED: " + classType.name());
			}

			trySetBreakPoint(sig, offset);

			//executes
			this.vm.resume();

			final EventQueue queue = this.vm.eventQueue();
			boolean stopPointFound = false;
			while (!stopPointFound) {
				try {
					final EventSet eventSet = queue.remove();
					final EventIterator it = eventSet.eventIterator();
					while (!stopPointFound && it.hasNext()) {
						final Event event = it.nextEvent();
						handleClassPrepareEvents(event);
						if (this.breakpoint == null) {
							trySetBreakPoint(sig, offset);
						} else {
							stopPointFound = handleBreakpointEvents(event, numberOfHits);
						}
					}
					if (!stopPointFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					throw new GuidanceException(e);
				} catch (VMDisconnectedException e) {
					if (this.errThread != null) {
					    this.errThread.flush();
					}
					if (this.outThread != null) {
					    this.outThread.flush();
					}
					if (stopPointFound) {
						return; //must not try to disable event requests
					} else {
						throw new GuidanceException("while looking for " + sig + "::" + offset + ", number of hits: " + numberOfHits + " : " + e);
					}
				}
			}
			//disables event requests
			cprr.disable();
			if (this.breakpoint != null) {
				this.breakpoint.disable();
				this.breakpoint = null;
			}
		}

		private void handleClassPrepareEvents(Event event) {
			if (event instanceof ClassPrepareEvent) {
				final ClassPrepareEvent evt = (ClassPrepareEvent) event;
				final ReferenceType classType = evt.referenceType();
				this.alreadyLoadedClasses.put(classType.name().replace('.', '/'), classType);
				for (ReferenceType innerType: classType.nestedTypes()) {
				    this.alreadyLoadedClasses.put(innerType.name().replace('.', '/'), innerType);					
					//System.out.println("ClassPrepareEvent: Inner-class: " + innerType.name());
				}
				//System.out.println("ClassPrepareEvent: " + classType.name());
			}
		}

		private void trySetBreakPoint(Signature sig, int offset) throws GuidanceException {
			final String stopClassName = sig.getClassName();
			final String stopMethodName = sig.getName();
			final String stopMethodDescr = sig.getDescriptor();
			if (this.alreadyLoadedClasses.containsKey(stopClassName)) {
				final ReferenceType classType = this.alreadyLoadedClasses.get(stopClassName);
				final List<Method> methods = classType.methodsByName(stopMethodName);
				for (Method m: methods) {
					if (stopMethodDescr.equals(m.signature())) {
						//System.out.println("** Set breakpoint at: " + m.locationOfCodeIndex(offset));
						final EventRequestManager mgr = this.vm.eventRequestManager();
						this.breakpoint = mgr.createBreakpointRequest(m.locationOfCodeIndex(offset));
						this.breakpoint.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
						this.breakpoint.enable();
						this.hitCounter = 0;
						return;
					}
				}
				throw new GuidanceException("Cannot set breakpoint because there is no method " + stopClassName + "." + stopMethodName + stopMethodDescr);
			}
		}

		protected boolean handleBreakpointEvents(Event event, int numberOfHits) throws GuidanceException {
			if (this.breakpoint.equals(event.request())) {
				//System.out.println("Breakpoint: stopped at: " + event);
				this.currentExecutionPointEvent = event;
				++this.hitCounter;
				
				// We check if the breakpoint is at a recursive method call, and do not count it if so
				try {
					final HashSet<Method> seenMethods = new HashSet<>();
					for (int i = 0; i < numFramesFromRootFrameConcrete(); ++i) {
						final Method m = getCurrentThread().frame(i).location().method();
						if (seenMethods.contains(m)) {
							--this.hitCounter;
							break;
						}
					}
				} catch (IncompatibleThreadStateException e) {
					throw new GuidanceException("JDI failed to check the call stack at breakpoint while searching for the target method: " + e);
				}
				
				if (this.hitCounter == numberOfHits) {
					return true;
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

		private StackFrame rootFrameConcrete() throws IncompatibleThreadStateException, GuidanceException {
			final int numFramesFromRoot = numFramesFromRootFrameConcrete();
			final List<StackFrame> frameStack = getCurrentThread().frames();
			return frameStack.get(numFramesFromRoot);
		}

		protected int numFramesFromRootFrameConcrete() throws IncompatibleThreadStateException, GuidanceException {
			final int numFrames = getCurrentThread().frameCount() - this.numOfFramesAtMethodEntry;
			return numFrames;
		}

		@Override
		public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException, ImpureMethodException {
			final ObjectReference object;
			try {
				object = (ObjectReference) getValue(origin);
			} catch (IndexOutOfBoundsException e) {
				if (!origin.asOriginString().equals(e.getMessage())) {
					System.out.println("[JDI] WARNING: In DecisionProcedureGuidanceJDI.typeOfObject: " + origin.asOriginString() + " leads to invalid throw reference: " + e + 
							"\n ** Normally this happens when JBSE wants to extract concrete types for fresh-expands, but the reference is null in the concrete state, thus we can safely assume that no Fresh object shall be considered"
							+ "\n ** However it seems that the considered references do not to match with this assumtion in this case.");
				}
				return null; // Origin depends on out-of-bound array access: Fresh expansion is neither possible, nor needed
			}
			if (object == null) {
				return null;
			}
			final StringBuilder buf = new StringBuilder();
			String name = object.referenceType().name();
			boolean isArray = false;
			while (name.endsWith("[]")) {
				isArray = true;
				buf.append("[");
				name = name.substring(0, name.length() - 2);
			}
			buf.append(isPrimitiveOrVoidCanonicalName(name) ? toPrimitiveOrVoidInternalName(name) : (isArray ? REFERENCE : "") + internalClassName(name) + (isArray ? TYPEEND : ""));
			return buf.toString();
		}

		@Override
		public boolean isNull(ReferenceSymbolic origin) throws GuidanceException, ImpureMethodException {
			final ObjectReference object = (ObjectReference) getValue(origin);
			return (object == null);
		}

		@Override
		public boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException, ImpureMethodException {
			final ObjectReference objectFirst = (ObjectReference) getValue(first);
			final ObjectReference objectSecond = (ObjectReference) getValue(second);
			return ((objectFirst == null && objectSecond == null) || 
					(objectFirst != null && objectSecond != null && objectFirst.equals(objectSecond)));
		}

		/**
		 * The {@link SymbolicApplyJVMJDI} where the value returned by 
		 * {@link #getJDIValue(Symbolic)} lives.
		 */
		protected IJVMJDIContext getJDIValueJVMJDIContext;
		
		@Override
		public Object getValue(Symbolic origin) throws GuidanceException, ImpureMethodException {
			this.getJDIValueJVMJDIContext = this;
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
			} else if (val == null) {
				return null;
			} else {  //val instanceof VoidValue
				throw new GuidanceException("Unexpected JDI VoidValue returned from the concrete evaluation of symbolic value " + origin.asOriginString());
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
		 * @throws ImpureMethodException 
		 */
		protected Object getJDIValue(Symbolic origin) throws GuidanceException, ImpureMethodException {
			try {
				if (origin instanceof SymbolicLocalVariable) {
					return getJDIValueLocalVariable(((SymbolicLocalVariable) origin).getVariableName());
				} else if (origin instanceof KlassPseudoReference) {
					return getJDIObjectStatic(((KlassPseudoReference) origin).getClassFile().getClassName());
				} else if (origin instanceof SymbolicMemberField) {
					final Object o = getJDIValue(((SymbolicMemberField) origin).getContainer());
					if (!(o instanceof com.sun.jdi.ReferenceType) && !(o instanceof com.sun.jdi.ObjectReference)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					return getJDIValueField(((SymbolicMemberField) origin), o);
				} else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
					final Object o = getJDIValue(((PrimitiveSymbolicMemberArrayLength) origin).getContainer());
					if (!(o instanceof ArrayReference)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					return this.vm.mirrorOf(((ArrayReference) o).length());
				} else if (origin instanceof SymbolicMemberArray) {
					final Object o = getJDIValue(((SymbolicMemberArray) origin).getContainer());
					if (!(o instanceof ArrayReference)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					try {
						final Simplex index = (Simplex) eval(((SymbolicMemberArray) origin).getIndex());
						return ((ArrayReference) o).getValue(((Integer) index.getActualValue()).intValue());
					} catch (ClassCastException e) {
						throw new GuidanceException(e);
					} catch (IndexOutOfBoundsException e) {
						throw new IndexOutOfBoundsException(((SymbolicMemberArray) origin).asOriginString());
					}
				//TODO else if (origin instanceof ReferenceSymbolicMemberMapKey)
				} else if (origin instanceof ReferenceSymbolicMemberMapValue) {
					final ReferenceSymbolicMemberMapValue refSymbolicMemberMapValue = (ReferenceSymbolicMemberMapValue) origin;
					final SymbolicApply javaMapContainsKeySymbolicApply;
					try {
						javaMapContainsKeySymbolicApply = (SymbolicApply) this.calc.applyFunctionPrimitive(BOOLEAN, refSymbolicMemberMapValue.keyHistoryPoint(), 
						JAVA_MAP_CONTAINSKEY.toString(), refSymbolicMemberMapValue.getContainer(), refSymbolicMemberMapValue.getAssociatedKey()).pop();
					} catch (NoSuchElementException | jbse.val.exc.InvalidTypeException | InvalidInputException e) {
						throw new UnexpectedInternalException(e);
					}
					if (!this.symbolicApplyCache.containsKey(javaMapContainsKeySymbolicApply)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containsKey was not evaluated before evaluating this GET symbol");
					} 
					final ISymbolicApplyJVMJDIContext symbolicApplyVm = this.symbolicApplyCache.get(javaMapContainsKeySymbolicApply); 
					if (!(symbolicApplyVm instanceof IInitialMapSymbolicApplyJVMJDIContext)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containsKey was evaluated as an ordinary abstract-interpreted call, rather than as a JAVA_MAP function");
					} 
					final IInitialMapSymbolicApplyJVMJDIContext initialMapSymbolicApplyVm = (IInitialMapSymbolicApplyJVMJDIContext) symbolicApplyVm;
					final Value val = initialMapSymbolicApplyVm.getValueAtKey();
					if (val != null) {
						this.getJDIValueJVMJDIContext = initialMapSymbolicApplyVm;
					}
					return val;
				} else if (origin instanceof PrimitiveSymbolicHashCode) {
					if (this.getJDIValueJVMJDIContext != this) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + 
								" : Fails because the curret implementation of JDI-guidance does not reliably support"
								+ " decisions that deopend on hashCodes of SymbolicApply symbols or their fields");						
					}
					final Object o = getJDIValue(((PrimitiveSymbolicHashCode) origin).getContainer());
					if (!(o instanceof ObjectReference)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					final ObjectReference oRef = (ObjectReference) o;
					final Value retVal = oRef.invokeMethod(this.getJDIValueJVMJDIContext.getCurrentThread(), oRef.referenceType().methodsByName("hashCode").get(0), Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
					return retVal;
				} else if (origin instanceof SymbolicApply) {
					//Implicit invariant: when we see a ReferenceSymbolicApply for the first time, JBSE is at the call point of the corresponding function.
					// TODO: it seems like this assumption may not hold, and this invalidates our computation of the context of the invocation of this uninterpreted function. 
					//       As a solution, we should modify JBSE such that each SymbolicApply has the context information as an immutable attribute.
					final SymbolicApply symbolicApply = (SymbolicApply) origin;
					if (!this.symbolicApplyCache.containsKey(symbolicApply)) {
						startSymbolicApplyVm(symbolicApply);
					} 
					final ISymbolicApplyJVMJDIContext symbolicApplyJVMJDI = this.symbolicApplyCache.get(symbolicApply);
					this.getJDIValueJVMJDIContext = symbolicApplyJVMJDI;
					return symbolicApplyJVMJDI.getRetValue();
				} else {
					throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
				}
			} catch (IncompatibleThreadStateException | AbsentInformationException | InvocationException | com.sun.jdi.InvalidTypeException | ClassNotLoadedException e) {
				throw new GuidanceException(e);
			}
		}

		private void startSymbolicApplyVm(SymbolicApply symbolicApply) throws GuidanceException, ImpureMethodException {
			/* TODO: Add a strategy to limit the maximum number of SymbolicApplyJVMJDI that we might allocate 
			 * to execute uninterpreted functions.
			 * At the moment, we start a new SymbolicApplyJVMJDI for each symbolicApply, and we keep alive all 
			 * SymbolicApplyJVMJDIs that handle any symbolicApply of type ReferenceSymbolicApply, because 
			 * these might be re-queried at future states for the values of fields within the return object. 
			 * However, this can become expensive if there are many invocations of ReferenceSymbolicApply 
			 * uninterpreted functions. 			  
			 */
			SymbolicApplyJVMJDI symbolicApplyVm;
			final List<String> hitCallCtxs;
			if (isSymbolicApplyOnInitialMap(this.currentStateSupplier.get().getClassHierarchy(), (jbse.val.Value) symbolicApply)) {
				final String op = this.currentHashMapModelMethod; //the operator is containsKey, but we need to move into the jbse.base.JAVA_MAP method where containsKey is being evaluated to obtain the proper value of the key
				hitCallCtxs = this.symbolicApplyOperatorOccurrences.get(op);
				final SymbolicMemberField initialMap = (SymbolicMemberField) symbolicApply.getArgs()[0];
				symbolicApplyVm = new InitialMapSymbolicApplyJVMJDI(this.calc, this.runnerParameters, this.stopSignature, this.numberOfHits, op, hitCallCtxs, initialMap, this.symbolicApplyCache, this.currentStateSupplier);
				symbolicApplyVm.eval_INVOKEX();
				if (((InitialMapSymbolicApplyJVMJDI) symbolicApplyVm).getValueAtKey() == null) {
					// the return value of containsKey is a boolean and there is no Object associated with this key,
					// thus we do not need this vm any further
					symbolicApplyVm.close(); 
				}
			} else {
				final String op = symbolicApply.getOperator();
				String opWithContext = SymbolicApplyJVMJDI.formatContextualSymbolicApplyOperatorOccurrence(op, this.currentStateSupplier.get());
				if (opWithContext == null) {
					throw new UninterpretedNoContextException();
				}
				storeNewSymbolicApplyOperatorContextualOccurrence(op, opWithContext);
				hitCallCtxs = this.symbolicApplyOperatorOccurrences.get(op);
				symbolicApplyVm = new SymbolicApplyJVMJDI(this.calc, this.runnerParameters, this.stopSignature, this.numberOfHits, op, hitCallCtxs);
				symbolicApplyVm.eval_INVOKEX();
				
				//If the return value is a primitive, we do not need this vm any further
				if (symbolicApplyVm.getRetValue() instanceof PrimitiveValue ||
				symbolicApplyVm.getRetValue() instanceof StringReference) {
					symbolicApplyVm.close(); 
				}
			}
			this.symbolicApplyCache.put(symbolicApply, symbolicApplyVm);

		}
		
		private void storeNewSymbolicApplyOperatorContextualOccurrence(String symbolicApplyOperator, String symbolicApplyOperatorCallWithContext) {
			if (!this.symbolicApplyOperatorOccurrences.containsKey(symbolicApplyOperator)) {
				this.symbolicApplyOperatorOccurrences.put(symbolicApplyOperator, new ArrayList<>());
			}
			List<String> occurrences = symbolicApplyOperatorOccurrences.get(symbolicApplyOperator);
			occurrences.add(symbolicApplyOperatorCallWithContext);
		}
		
		private com.sun.jdi.Value getJDIValueLocalVariable(String var) 
		throws GuidanceException, IncompatibleThreadStateException, AbsentInformationException {
			final com.sun.jdi.Value val;
			if ("this".equals(var)) {
				val = rootFrameConcrete().thisObject();
			} else {
				final LocalVariable variable = rootFrameConcrete().visibleVariableByName(var); 
				if (variable == null) {
					throw new GuidanceException(ERROR_BAD_PATH + "{ROOT}:" + var + ".");
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
				throw new GuidanceException(ERROR_BAD_PATH + "[" + className + "].");
			}
		}
		
		private static com.sun.jdi.Value cloneObject(ObjectReference ref, IJVMJDIContext oJVMJDIContext) throws GuidanceException {
			try {
				final com.sun.jdi.Value valClone = ref.invokeMethod(oJVMJDIContext.getCurrentThread(), ref.referenceType().methodsByName("clone").get(0), Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
				return valClone;
			} catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException e) {
				throw new UnexpectedInternalException(e);
			}
		}

		private com.sun.jdi.Value getJDIValueField(SymbolicMemberField origin, Object o) 
		throws GuidanceException {
			if (isInitialMapField(this.currentStateSupplier.get().getClassHierarchy(), (jbse.val.Value) origin)) {
				return cloneObject((ObjectReference) o, this.getJDIValueJVMJDIContext);
			}
			final String fieldName = origin.getFieldName();
			if (o instanceof com.sun.jdi.ReferenceType) {
				//the field is static
				final com.sun.jdi.ReferenceType oReferenceType = ((com.sun.jdi.ReferenceType) o);
				final Field fld = oReferenceType.fieldByName(fieldName);
				if (fld == null) {
					throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " (missing field " + fieldName + ").");
				}
				try {
					return oReferenceType.getValue(fld);
				} catch (IllegalArgumentException e) {
					throw new GuidanceException(e);
				}
			} else {
				//the field is not static (note that it can be declared in the superclass)
				final com.sun.jdi.ObjectReference oReference = ((com.sun.jdi.ObjectReference) o);
				final List<Field> fields = oReference.referenceType().allFields();
				Field fld = null;
				for (Field _fld : fields) {
					if (_fld.name().equals(fieldName)) {
						fld = _fld;
						break;
					}
				}
				if (fld == null) {
					throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " (missing field " + fieldName + ").");
				}
				try {
					return oReference.getValue(fld);
				} catch (IllegalArgumentException e) {
					throw new GuidanceException(e);
				}
			}
		}
		
		public int getCurrentCodeIndex() throws GuidanceException {
			return (int) getCurrentLocation().codeIndex();
		}

		public ThreadReference getCurrentThread() throws GuidanceException {
			if (this.currentExecutionPointEvent != null) {
				if (this.currentExecutionPointEvent instanceof BreakpointEvent) {
					return ((BreakpointEvent) this.currentExecutionPointEvent).thread();
				} else if (this.currentExecutionPointEvent instanceof StepEvent) {
					return ((StepEvent) this.currentExecutionPointEvent).thread();
				} else if (this.currentExecutionPointEvent instanceof MethodExitEvent) {
					return ((MethodExitEvent) this.currentExecutionPointEvent).thread();
				} else {
					throw new GuidanceException("Unexpected JDI failure: current execution point is neither BreakpointEvent nor StepEvent");
				}
			} else {
				throw new GuidanceException("Unexpected JDI failure: current method entry not known ");
			}
		}

		public Location getCurrentLocation() throws GuidanceException {
			if (this.currentExecutionPointEvent != null) {
				if (this.currentExecutionPointEvent instanceof BreakpointEvent) {
					return ((BreakpointEvent) this.currentExecutionPointEvent).location();
				} else if (this.currentExecutionPointEvent instanceof StepEvent) {
					return ((StepEvent) this.currentExecutionPointEvent).location();
				} else if (this.currentExecutionPointEvent instanceof MethodExitEvent) {
					return ((MethodExitEvent) this.currentExecutionPointEvent).location();
				} else {
					throw new GuidanceException("Unexpected JDI failure: current execution point is neither BreakpointEvent nor StepEvent");
				}
			} else {
				throw new GuidanceException("Unexpected JDI failure: current execution point entry not known ");
			}
		}

		@Override
		protected void preStep(State preSymbolicState) throws GuidanceException {
			// Nothing to do: This version of JVMJDI remains stuck at the initial state of the method under analysis
		}
		
		@Override
		protected void postStep(State postSymbolicState) throws GuidanceException {
			// Nothing to do: This version of JVMJDI remains stuck at the initial state of the method under analysis
		}

		private static String jdiMethodClassName(Method jdiMeth) {
			return jdiMeth.toString().substring(0, jdiMeth.toString().indexOf(jdiMeth.name() + '(') - 1).replace('.', '/');
		}

		@Override
		public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
			try {
				final Method jdiMeth = getCurrentLocation().method();
				final String jdiMethClassName = jdiMethodClassName(jdiMeth);
				final String jdiMethDescr = jdiMeth.signature();
				final String jdiMethName = jdiMeth.name();
				return new Signature(jdiMethClassName, jdiMethDescr, jdiMethName);
			} catch (GuidanceException e) {
				throw new UnexpectedInternalException(e);
			}
		}

		@Override
		public int getCurrentProgramCounter() throws ThreadStackEmptyException {
			try {
				return getCurrentCodeIndex();
			} catch (GuidanceException e) {
				throw new UnexpectedInternalException(e);
			}
		}

		@Override
		protected void close() {
			if (this.vm != null) {
				this.vm.exit(0);

				//obviates to inferior process leak
				this.vm.process().destroyForcibly();
				this.vm = null;
			}
			for (ISymbolicApplyJVMJDIContext symbolicApplyVm: this.symbolicApplyCache.values()) {
				symbolicApplyVm.closeVM();
			}
		}

	}
	
	private static interface ISymbolicApplyJVMJDIContext extends IJVMJDIContext {
		public String getSymbolicApplyOperator();
		public List<String> getHitCallCtxs();
		public Value getRetValue();
		void closeVM();
	}
	
	private static class SymbolicApplyJVMJDI extends JVMJDI implements ISymbolicApplyJVMJDIContext {
		protected String symbolicApplyOperator;
		protected List<String> hitCallCtxs;
		public static final String CALL_CONTEXT_SEPARATOR = "&&";
		private final BreakpointRequest targetMethodExitedBreakpoint;
		protected Value symbolicApplyRetValue;
		private boolean postInitial = false;

		public SymbolicApplyJVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int stopSignatureNumberOfHits, String symbolicApplyOperator, List<String> hitCallCtxs) 
		throws GuidanceException {
			super(calc, runnerParameters, stopSignature, stopSignatureNumberOfHits);
			this.postInitial = true;
			this.symbolicApplyOperator = symbolicApplyOperator;
			if (hitCallCtxs == null || hitCallCtxs.isEmpty()) {
				throw new UnexpectedInternalException("This should never happen: the considered symbolic apply operator (" + symbolicApplyOperator + ") must occurr at least once");
			}
			this.hitCallCtxs = hitCallCtxs;
			
			//Sets up a control breakpoint to check if, at any next step, JDI erroneously returns 
			//from the method under analysis
			try { 
				final EventRequestManager mgr = this.vm.eventRequestManager();
				final Location callPoint = getCurrentThread().frames().get(1).location(); //the current location in caller frame
				this.targetMethodExitedBreakpoint = mgr.createBreakpointRequest(callPoint.method().locationOfCodeIndex(callPoint.codeIndex() + Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET));
			} catch (IncompatibleThreadStateException e) {
				throw new UnexpectedInternalException(e);
			}
		}
		
		// NB: This method return null if the call stack indicate a call nested within hash map models,
		// because these calls do not match actual call in the concrete execution observed with JDI.
		public static String formatContextualSymbolicApplyOperatorOccurrence(String symbolicApplyOperator, State state) {
            String callCtxString = "";
            try {
            	List<Frame> stack = state.getStack();
				for (int i = 0; i < stack.size(); ++i) {
					final ClassFile methodClass = stack.get(i).getMethodClass();
					if (i != stack.size() - 1 && classImplementsJavaUtilMap(methodClass)) {
						return null; // refuse calls nested within hash map models
					} else {
						callCtxString += (i > 0 ? CALL_CONTEXT_SEPARATOR : "") + stack.get(i).getMethodSignature();						
					}

				}
			} catch (FrozenStateException e) {
	            //this should never happen
	            failExecution(e);
			}
            if (symbolicApplyOperator != null) {
            	callCtxString += CALL_CONTEXT_SEPARATOR + symbolicApplyOperator;
            }
            return callCtxString;
		}
		
		@Override
		protected void close() {
			if (this.symbolicApplyRetValue != null && this.symbolicApplyRetValue instanceof ObjectReference) {
				//populates the caching referenceType field before shutdown
				//of the JDI JVM, so it can be used later
				((ObjectReference) this.symbolicApplyRetValue).referenceType();
			}
			super.close();
		}

		@Override
		public Value getRetValue() {
			return this.symbolicApplyRetValue;
		}

		@Override
		protected boolean handleBreakpointEvents(Event event, int numberOfHits) throws GuidanceException {
			if (this.postInitial && this.targetMethodExitedBreakpoint.equals(event.request())) {
				try { //Did we exit from target method? Should not happen 
					if (numFramesFromRootFrameConcrete() < 0) {
						throw new UnexpectedInternalException("Exited from target method, while looking for method " + this.symbolicApplyOperator + " - " + this.hitCallCtxs);
					}
				} catch (IncompatibleThreadStateException e) {
					throw new UnexpectedInternalException(e);
				}
			}
			final int hitCounterBefore = this.hitCounter;
			final boolean atBreakpoint = super.handleBreakpointEvents(event, numberOfHits);
			if (this.postInitial && this.hitCounter > hitCounterBefore) {
				// We skip (do not count) breakpoints that do not correspond to call contexts in our list of hits
				if (this.hitCallCtxs.size() < this.hitCounter) {
					throw new UnexpectedInternalException("This should never happen: the target number of hits cannot be larger than the size of the list of the hits' call contexts");
				}
				final String[] expectedCallCtx = this.hitCallCtxs.get(this.hitCounter - 1).split(CALL_CONTEXT_SEPARATOR);
				try {
					final int numFrames = numFramesFromRootFrameConcrete() + 1;
					final boolean callStackLenOk = expectedCallCtx.length == numFrames; //check if the call stack corresponds, otherwise roll-back the decision
					if (!callStackLenOk) {
						--this.hitCounter;
						return false;
					}
					for (int i = 0; i < numFramesFromRootFrameConcrete(); ++i) {
						final String method = getCurrentThread().frame(i).location().method().name();
						final String callCtxItem = expectedCallCtx[expectedCallCtx.length - 1 - i];
						final String name = callCtxItem.substring(callCtxItem.lastIndexOf(':') + 1);
						final boolean callStackOk = method.equals(name);
						if (!callStackOk) {
							--this.hitCounter;
							return false;
						}					
					}
				} catch (IncompatibleThreadStateException e) {
					throw new GuidanceException("JDI failed to check the call stack at breakpoint on HashMap method related to symbolic HashMap: " + e);
				}
			}
			return atBreakpoint;
		}
		
		protected void eval_INVOKEX() throws GuidanceException, ImpureMethodException {
			//steps and decides
			stepIntoSymbolicApplyMethod();
			this.symbolicApplyRetValue = stepUpToMethodExit();
		}

		protected void stepIntoSymbolicApplyMethod() throws GuidanceException {
			// Make JDI execute the uninterpreted function that corresponds to the symboliApply
			this.targetMethodExitedBreakpoint.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			this.targetMethodExitedBreakpoint.enable();
			goToBreakpoint(signatureOf(this.symbolicApplyOperator), 0, this.hitCallCtxs.size());
			this.targetMethodExitedBreakpoint.disable();			
		}
		
		private static Signature signatureOf(String unintFuncOperator) {
			final String[] parts = unintFuncOperator.split(":");
			return new Signature(parts[0], parts[1], parts[2]);
		}

		protected Value stepUpToMethodExit() throws GuidanceException, ImpureMethodException {
			final int currFrames;
			final Method currMethod;
			try {
				currFrames = getCurrentThread().frameCount();
				currMethod = getCurrentThread().frame(0).location().method();
			} catch (IncompatibleThreadStateException e) {
				throw new GuidanceException(e);
			}

			final EventRequestManager mgr = this.vm.eventRequestManager();
			final MethodExitRequest mexr = mgr.createMethodExitRequest();
			mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			mexr.enable();
			final ReferenceType classType = this.vm.classesByName("jbse.apps.run.DecisionProcedureGuidanceJDILauncher").get(0);
			final Method method = classType.methodsByName("main").get(0);
			final ArrayList<BreakpointRequest> bkprs = new ArrayList<>();
			try {
				for (Location l : method.allLineLocations()) {
					final BreakpointRequest bkpr = mgr.createBreakpointRequest(l);
					bkpr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
					bkpr.enable();
					bkprs.add(bkpr);
				}
			} catch (AbsentInformationException e) {
				throw new GuidanceException(e);
			}

			this.vm.resume();
			final EventQueue queue = this.vm.eventQueue();

			MethodExitEvent mthdExitEvent = null;
			boolean exitFound = false;
			eventLoop:
			while (!exitFound) {
				try {
					final EventSet eventSet = queue.remove();
					final EventIterator it = eventSet.eventIterator();
					while (!exitFound && it.hasNext()) {
						final Event event = it.nextEvent();

						if (event instanceof MethodExitEvent) {
							mthdExitEvent = (MethodExitEvent) event;
							final int methodExitFrameCount = mthdExitEvent.thread().frameCount();
							if (methodExitFrameCount < currFrames) {
								//somehow we exited from the method to a caller frame;
								//this may be caused by an exception that is catched,
								//and for which JDI was not able to detect a catch point
								break eventLoop; 
							} else if (methodExitFrameCount == currFrames && mthdExitEvent.location().method().equals(currMethod)) { 
								exitFound = true;
								this.currentExecutionPointEvent = event;
							}
						} else if (event instanceof BreakpointRequest) {
							//we hit the catch block of the InvocationTargetException
							//of the JDI launcher: the method threw an uncaught exception
							break eventLoop;
						}
					}
					if (!exitFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					throw new GuidanceException(e);
				} catch (VMDisconnectedException | IncompatibleThreadStateException e) {
					throw new GuidanceException(e);
				}
			}

			for (BreakpointRequest bkpr : bkprs) {
				bkpr.disable();
			}
			mexr.disable();
			if (!exitFound) {
				throw new ImpureMethodException();
			}
			return mthdExitEvent.returnValue();
		}

		@Override
		public String getSymbolicApplyOperator() {
			return symbolicApplyOperator;
		}

		@Override
		public List<String> getHitCallCtxs() {
			return hitCallCtxs;
		}

		@Override
		public void closeVM() {
			this.close();
		}
	}
	
	private static interface IInitialMapSymbolicApplyJVMJDIContext extends ISymbolicApplyJVMJDIContext {
		public Value getValueAtKey();
	}
	
	private static class InitialMapSymbolicApplyJVMJDI extends SymbolicApplyJVMJDI implements IInitialMapSymbolicApplyJVMJDIContext {
		private SymbolicMemberField initialMapOrigin;
		private Value valueAtKey;

		public InitialMapSymbolicApplyJVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits, String symbolicApplyOperator, List<String> hitCallCtxs, SymbolicMemberField initialMapOrigin, LinkedHashMap<SymbolicApply, ISymbolicApplyJVMJDIContext> symbolicApplyCache, Supplier<State> currentStateSupplier) 
		throws GuidanceException, ImpureMethodException {
			super(calc, runnerParameters, stopSignature, numberOfHits, symbolicApplyOperator, hitCallCtxs);
			setCurrentStateSupplier(currentStateSupplier);
			populateLocalSymbolicApplyCache(symbolicApplyCache); //needed to call getJDIValue below
			this.initialMapOrigin = initialMapOrigin;
		}
		
		private void populateLocalSymbolicApplyCache(LinkedHashMap<SymbolicApply, ISymbolicApplyJVMJDIContext> symbolicApplyCache) throws GuidanceException, ImpureMethodException {
			// TODO: Add a strategy to recompute only those SymbolicApplyJVMJDI needed for serving getJDIValue(initialMapOrigin)

			// backup state info that will be temporarily replaced, to be restored at the end
			String savedSymbolicApplyOperator = this.symbolicApplyOperator;
			List<String> savedHitCallCtxs = this.hitCallCtxs;

			for (SymbolicApply symbolicApply: symbolicApplyCache.keySet()) {
				//recall the entries are in order of insertion, which corresponds to the order in which the symbolicApplys were met in the symbolic trace
				ISymbolicApplyJVMJDIContext symbolicApplyVm = symbolicApplyCache.get(symbolicApply);
				this.symbolicApplyOperator = symbolicApplyVm.getSymbolicApplyOperator();
				this.hitCallCtxs = symbolicApplyVm.getHitCallCtxs();
				final ThreadReference currentThread = this.getCurrentThread();
				if (symbolicApplyVm instanceof InitialMapSymbolicApplyJVMJDI) {
					// Advance this JDIJVM to recompute the result of containsKey and GET for a Map object 
					this.initialMapOrigin = ((InitialMapSymbolicApplyJVMJDI) symbolicApplyVm).initialMapOrigin;
					eval_INVOKEX();
					final Value currentRetValue = this.getRetValue(); //it is a primitive (a boolean), no need to clone it
					final Value currentValueAtKey = this.getValueAtKey(); 
					// We clone currentValueAtKey since the state of the object may change lately, while we advance this JVMJDI further on
					final Value currentValueAtKeyClone = JVMJDI.cloneObject((ObjectReference) currentValueAtKey, this);
					this.symbolicApplyCache.put(symbolicApply, new IInitialMapSymbolicApplyJVMJDIContext() {
						@Override
						public String getSymbolicApplyOperator() {
							return symbolicApplyOperator;
						}
						@Override
						public List<String> getHitCallCtxs() {
							return hitCallCtxs;
						}
						@Override
						public Value getRetValue() {
							return currentRetValue;
						}
						@Override
						public Value getValueAtKey() {
							return currentValueAtKeyClone;
						}
						@Override
						public ThreadReference getCurrentThread() throws GuidanceException {
							return currentThread;
						}
						@Override
						public void closeVM() {
							// nothing to do, because this JVMJDIContext does not include an independent VM
						}
					});
				} else if (symbolicApplyVm.getRetValue() instanceof PrimitiveValue ||
				symbolicApplyVm.getRetValue() instanceof StringReference) {
					// clearly we cannot retrieve the initalMap out of any non-Map primitive return value,
					// thus here we do not need to recompute this SymbolicApply
					continue; 
				} else {
					// Advance this JDIJVM to recompute the result of an unintepreted function 
					stepIntoSymbolicApplyMethod();
					this.symbolicApplyRetValue = stepUpToMethodExit();
					final Value currentRetValue = this.getRetValue();
					// We clone currentRetValue since the state of the object may change lately, while we advance this JVMJDI further on
					final Value currentRetValueClone = JVMJDI.cloneObject((ObjectReference) currentRetValue, this);
					this.symbolicApplyCache.put(symbolicApply, new ISymbolicApplyJVMJDIContext() {
						@Override
						public String getSymbolicApplyOperator() {
							return symbolicApplyOperator;
						}
						@Override
						public List<String> getHitCallCtxs() {
							return hitCallCtxs;
						}
						@Override
						public Value getRetValue() {
							return currentRetValueClone;
						}
						@Override
						public ThreadReference getCurrentThread() throws GuidanceException {
							return currentThread;
						}
						@Override
						public void closeVM() {
							// nothing to do, because this JVMJDIContext does not include an independent VM
						}
					});
				} 
			}
			// restore the original state info
			this.symbolicApplyOperator = savedSymbolicApplyOperator;
			this.hitCallCtxs = savedHitCallCtxs;
		}

		@Override
		protected void eval_INVOKEX() throws GuidanceException, ImpureMethodException {
			this.getJDIValueJVMJDIContext = this; //needed to call getJDIValue below
			final ObjectReference initialMapRef = (ObjectReference) getJDIValue(this.initialMapOrigin);
			stepIntoSymbolicApplyMethod();
			try {
				final ThreadReference currentThread = getCurrentThread();
				final ObjectReference keyRef = (ObjectReference) currentThread.frame(0).getArgumentValues().get(0);
				this.symbolicApplyRetValue = initialMapRef.invokeMethod(currentThread, initialMapRef.referenceType().methodsByName("containsKey").get(0), Collections.singletonList(keyRef), ObjectReference.INVOKE_SINGLE_THREADED);
				this.valueAtKey = initialMapRef.invokeMethod(currentThread, initialMapRef.referenceType().methodsByName("get").get(0), Collections.singletonList(keyRef), ObjectReference.INVOKE_SINGLE_THREADED);
			} catch (InvalidTypeException | ClassNotLoadedException | IncompatibleThreadStateException | InvocationException e) {
				throw new GuidanceException("Failed to call method on the concrete HashMap that corresponds to a symbolic HashMap:" + e);
			}
		}
		
		@Override
		public Value getValueAtKey() {
			return this.valueAtKey;
		}
	}

	public void notifyExecutionOfMapModelMethod(Signature currentMethodSignature, State state) {
		final String jbseMethodWithCtx = SymbolicApplyJVMJDI.formatContextualSymbolicApplyOperatorOccurrence(null, state);
		if (jbseMethodWithCtx != null) {
			((JVMJDI) this.jvm).storeNewSymbolicApplyOperatorContextualOccurrence(currentMethodSignature.toString(), jbseMethodWithCtx);
			((JVMJDI) this.jvm).currentHashMapModelMethod = currentMethodSignature.toString();
			//consistency check
			final String lastCall = jbseMethodWithCtx.substring(jbseMethodWithCtx.lastIndexOf(SymbolicApplyJVMJDI.CALL_CONTEXT_SEPARATOR) + SymbolicApplyJVMJDI.CALL_CONTEXT_SEPARATOR.length());
			if (!currentMethodSignature.toString().equals(lastCall)) {
				throw new UnexpectedInternalException("We expect that the notified method is the last that jbse is executing, but CURRENT=" + currentMethodSignature + " while LAST=" + lastCall + " withing the CONTEXT=" + jbseMethodWithCtx);
			}
		} //else: it is an invocation nested in a chain of JAVA_MAP methods. We ignore it.
	}
}

