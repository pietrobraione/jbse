package jbse.apps.run;

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
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
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

import jbse.bc.Offsets;
import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.ReferenceSymbolic;
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
	 *        will stop at the entry of the {@code numberOfHits}-th invocation of the 
	 *        method whose signature is {@code stopSignature}, and the reached state will be 
	 *        used as the initial one.
	 * @param numberOfHits an {@code int} greater or equal to one.
	 * @throws GuidanceException if something fails during creation (and the caller
	 *         is to blame).
	 * @throws InvalidInputException if {@code component == null}.
	 */
	public DecisionProcedureGuidanceJDI(DecisionProcedure component, Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
			throws GuidanceException, InvalidInputException {
		super(component, new JVMJDI(calc, runnerParameters, stopSignature, numberOfHits));
	}

	private static class JVMJDI extends JVM {
		private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path: ";

		StreamRedirectThread outThread = null; 
		StreamRedirectThread errThread = null; 

		protected VirtualMachine vm;
		private BreakpointRequest breakpoint;
		private int hitCounter;
		private  boolean valueDependsOnSymbolicApply;
		private int numOfFramesAtMethodEntry;
		protected Event currentExecutionPointEvent;        
		private Map<String, ReferenceType> alreadyLoadedClasses = new HashMap<>();

		private final RunnerParameters runnerParameters;
		private final Signature stopSignature;
		private final int stopSignatureNumberOfHits;
		
		// Handling of uninterpreted functions
		private Map<SymbolicApply, SymbolicApplyJVMJDI> symbolicApplyCache = new HashMap<>();
		private Map<String, Integer> symbolicApplyOperatorOccurrences = new HashMap<>();		
		
		public JVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
		throws GuidanceException {
			super(calc, runnerParameters, stopSignature, numberOfHits);
			this.runnerParameters = runnerParameters;
			this.stopSignature = stopSignature;
			this.stopSignatureNumberOfHits = numberOfHits;
			this.vm = createVM();
			this.goToBreakpoint(stopSignature, 0, numberOfHits);
			try 	{
				this.numOfFramesAtMethodEntry = getCurrentThread().frameCount();
			} catch (IncompatibleThreadStateException e) {
				throw new UnexpectedInternalException(e); 
			}
			outThread = redirect("Subproc stdout", vm.process().getInputStream(), System.out);
			errThread = redirect("Subproc stderr", vm.process().getErrorStream(), System.err);
		}
		
		private StreamRedirectThread redirect(String name, InputStream in, OutputStream out) {
			StreamRedirectThread t = new StreamRedirectThread(name, in, out);
			t.setDaemon(true);
			t.start();
			return t;
		}

		/**
		 * StreamRedirectThread is a thread which copies it's input to
		 * it's output and terminates when it completes.
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
					while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
						out.write(cbuf, 0, count);
					}
					out.flush();
				} catch(IOException exc) {
					System.err.println("Child I/O Transfer - " + exc);
				}
			}

			public void flush() {
				try {
					out.flush();
				} catch (IOException exc) {
					System.err.println("Child I/O Transfer - " + exc);
				}
			}
		}


		private VirtualMachine createVM() 
				throws GuidanceException {
			try {
				final Iterable<Path> classPath = this.runnerParameters.getClasspath().classPath();
				final ArrayList<String> listClassPath = new ArrayList<>();
				classPath.forEach(p -> listClassPath.add(p.toString()));
				final String stringClassPath = String.join(File.pathSeparator, listClassPath.toArray(new String[0]));
				final String mainClass = DecisionProcedureGuidanceJDILauncher.class.getName();
				final String targetClass = binaryClassName(this.runnerParameters.getMethodSignature().getClassName());
				final String startMethodName = this.runnerParameters.getMethodSignature().getName();
				return launchTarget("-classpath \"" + stringClassPath + "\" " + mainClass + " " + targetClass + " " + startMethodName);
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
			final ClassPrepareRequest  cprr = mgr.createClassPrepareRequest();
			cprr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			cprr.enable();

			for (ReferenceType classType: vm.allClasses()) {
				alreadyLoadedClasses.put(classType.name().replace('.', '/'), classType);
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
						Event event = it.nextEvent();
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
					//TODO is it ok?
				} catch (VMDisconnectedException e) {
					if (errThread != null) {
						errThread.flush();
					}
					if (outThread != null) {
						outThread.flush();
					}
					if (stopPointFound) {
						return; //must not try to disable event requests
					} else {
						throw new GuidanceException("while looking for " + sig + "::" + offset + " : " + e);
					}
				}
			}
			//disables event requests
			cprr.disable();
			if (this.breakpoint != null) {
				this.breakpoint.disable();
			}
		}

		private void handleClassPrepareEvents(Event event) {
			if (event instanceof ClassPrepareEvent) {
				ClassPrepareEvent evt = (ClassPrepareEvent) event;
				ReferenceType classType = evt.referenceType();
				alreadyLoadedClasses.put(classType.name().replace('.', '/'), classType);
				for (ReferenceType innerType: classType.nestedTypes()) {
					alreadyLoadedClasses.put(innerType.name().replace('.', '/'), innerType);					
					//System.out.println("ClassPrepareEvent: Inner-class: " + innerType.name());
				}
				//System.out.println("ClassPrepareEvent: " + classType.name());
			}
		}

		private void trySetBreakPoint(Signature sig, int offset) throws GuidanceException {
			String stopClassName = sig.getClassName();
			String stopMethodName = sig.getName();
			String stopMethodDescr = sig.getDescriptor();
			if (alreadyLoadedClasses.containsKey(stopClassName)) {
				ReferenceType classType = alreadyLoadedClasses.get(stopClassName);
				List<Method> methods = classType.methodsByName(stopMethodName);
				for (Method m: methods) {
					if(stopMethodDescr.equals(m.signature())) {
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
			if (event.request().equals(breakpoint)) {
				//System.out.println("Breakpoint: stopped at: " + event);
				this.currentExecutionPointEvent = event;
				++this.hitCounter;
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
			List<StackFrame> frameStack = getCurrentThread().frames();
			return  frameStack.get(numFramesFromRoot);
		}

		protected int numFramesFromRootFrameConcrete() throws IncompatibleThreadStateException, GuidanceException {
			final int numFrames = this.getCurrentThread().frameCount() - this.numOfFramesAtMethodEntry;
			return numFrames;
		}

		@Override
		public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException {
			final ObjectReference object;
			try {
				object = (ObjectReference) this.getValue(origin);
			} catch (IndexOutOfBoundsException e) {
				if (!origin.asOriginString().equals(e.getMessage())) {
					System.out.println("[JDI] WARNING: In DecisionProcedureGuidanceJDI.typeOfObject: " + origin.asOriginString() + " leads to invalid throw reference: " + e + 
							"\n ** Normally this happens when JBSE wants to extract concrete types for Fresh-expands, but the reference is null in the concrete state, thus we can safely assume that no Fresh object shall be considered"
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
		public boolean isNull(ReferenceSymbolic origin) throws GuidanceException {
			final ObjectReference object = (ObjectReference) this.getValue(origin);
			return (object == null);
		}

		@Override
		public boolean areAlias(ReferenceSymbolic first, ReferenceSymbolic second) throws GuidanceException {
			final ObjectReference objectFirst = (ObjectReference) this.getValue(first);
			final ObjectReference objectSecond = (ObjectReference) this.getValue(second);
			return ((objectFirst == null && objectSecond == null) || 
					(objectFirst != null && objectSecond != null && objectFirst.equals(objectSecond)));
		}

		@Override
		public Object getValue(Symbolic origin) throws GuidanceException {
			this.valueDependsOnSymbolicApply = false;
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
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					return getJDIValueField(((SymbolicMemberField) origin), o);
				} else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
					final Object o = getJDIValue(((PrimitiveSymbolicMemberArrayLength) origin).getContainer() );
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
				} else if (origin instanceof PrimitiveSymbolicHashCode) {
					if (	this.valueDependsOnSymbolicApply) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + 
								" : Fails because the curret implementation of JDI-guidance does not reliably support"
								+ " decisions that deopend on hashCodes of SymbolicApply symbols or their fields");						
					}
					final Object o = getJDIValue(((PrimitiveSymbolicHashCode) origin).getContainer());
					if (!(o instanceof ObjectReference)) {
						throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString() + " : Fails because containing object is " + o);
					}
					final ObjectReference oRef = (ObjectReference) o;
					final Value retVal = oRef.invokeMethod(getCurrentThread(), oRef.referenceType().methodsByName("hashCode").get(0), Collections.emptyList(), ObjectReference.INVOKE_SINGLE_THREADED);
					return retVal;
				} else if (origin instanceof SymbolicApply) {
					//Implicit invariant: when we see a ReferenceSymbolicApply for the first time, JDI is at the call point of the corresponding function
					SymbolicApply symbolicApply = (SymbolicApply) origin;
					if (!this.symbolicApplyCache.containsKey(symbolicApply)) {
						SymbolicApplyJVMJDI symbolicApplyVm = startSymbolicApplyVm(symbolicApply);
						this.symbolicApplyCache.put(symbolicApply, symbolicApplyVm);
					} 
					SymbolicApplyJVMJDI symbolicApplyVm = this.symbolicApplyCache.get(symbolicApply); 
					this.valueDependsOnSymbolicApply = true;
					return symbolicApplyVm.getRetValue();
				} else {
					throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
				}
			} catch (IncompatibleThreadStateException | AbsentInformationException | InvocationException | com.sun.jdi.InvalidTypeException | ClassNotLoadedException e) {
				throw new GuidanceException(e);
			}
		}

		private SymbolicApplyJVMJDI startSymbolicApplyVm(SymbolicApply symbolicApply) throws GuidanceException {
			/* TODO: Add a strategy to limit the maximum number of SymbolicApplyJVMJDI that we might allocate 
			 * to execute uninterpreted functions.
			 * At the moment, we start a new SymbolicApplyJVMJDI for each symbolicApply, and we keep alive all 
			 * SymbolicApplyJVMJDIs that handle any symbolicApply of type ReferenceSymbolicApply, because 
			 * these might be re-queried at future states for the values of fields within the return object. 
			 * However, this can become expensive if there are many invocations of ReferenceSymbolicApply 
			 * uninterpreted functions. 			  
			 */
			final String op = symbolicApply.getOperator();
			final int numberOfHits;
			if (this.symbolicApplyOperatorOccurrences.containsKey(op)) {
                            numberOfHits = this.symbolicApplyOperatorOccurrences.get(op) + 1;
			} else {
                            numberOfHits = 1;
			}
			this.symbolicApplyOperatorOccurrences.put(op, numberOfHits);
			final SymbolicApplyJVMJDI symbolicApplyVm = new SymbolicApplyJVMJDI(this.calc, this.runnerParameters, this.stopSignature, this.stopSignatureNumberOfHits, symbolicApply, numberOfHits);
			return symbolicApplyVm;
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

		private com.sun.jdi.Value getJDIValueField(SymbolicMemberField origin, Object o) 
		throws GuidanceException {
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
				final String fieldDeclaringClass = binaryClassName(origin.getFieldClass());
				final List<Field> fields = oReference.referenceType().allFields();
				Field fld = null;
				for (Field _fld : fields) {
					if (_fld.declaringType().name().equals(fieldDeclaringClass) && _fld.name().equals(fieldName)) {
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
		public void step(State jbseState) throws GuidanceException {
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
			for (SymbolicApplyJVMJDI symbolicApplyVm: symbolicApplyCache.values()) {
				symbolicApplyVm.close();
			}
		}
	}
	
	private static final class SymbolicApplyJVMJDI extends JVMJDI {
		private Value symbolicApplyRetValue;
		private final BreakpointRequest targetMethodExitedBreakpoint;

		public SymbolicApplyJVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits, SymbolicApply symbolicApply, int symbolicApplyNumberOfHits) 
		throws GuidanceException {
			super(calc, runnerParameters, stopSignature, numberOfHits);			

			/* We set up a control breakpoint to check if, at any next step, JDI erroneously returns from the method under analysis */
			try { 
				final EventRequestManager mgr = this.vm.eventRequestManager();
				Location callPoint = getCurrentThread().frames().get(1).location(); //the current location in caller frame
				this.targetMethodExitedBreakpoint = mgr.createBreakpointRequest(callPoint.method().locationOfCodeIndex(callPoint.codeIndex() + Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET));
			} catch (IncompatibleThreadStateException e) {
				throw new UnexpectedInternalException(e);
			}
			this.targetMethodExitedBreakpoint.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
			this.targetMethodExitedBreakpoint.enable();
			
			// Make JDI execute the uninterpreted function that corresponds to the symboliApply
			this.eval_INVOKEX(symbolicApply, symbolicApplyNumberOfHits);
			this.targetMethodExitedBreakpoint.disable();
			
			//If the return value is a primitive, we do not need this vm any further
			if (symbolicApply instanceof PrimitiveSymbolicApply) {
				this.close(); 
			}
		}
		
		public Value getRetValue() {
			return this.symbolicApplyRetValue;
		}

		@Override
		protected boolean handleBreakpointEvents(Event event, int numberOfHits) throws GuidanceException {
			if (event.request().equals(this.targetMethodExitedBreakpoint)) {
				try { //Did we exited from target method? Should not happen 
					if (numFramesFromRootFrameConcrete() < 0) {
						throw new UnexpectedInternalException("Exited from target method, while looking for breakpoint");
					}
				} catch (IncompatibleThreadStateException e) {
					throw new UnexpectedInternalException(e);
				}
			}
			return super.handleBreakpointEvents(event, numberOfHits);
		}
		
		private void eval_INVOKEX(SymbolicApply symbolicApply, int numberOfHits) throws GuidanceException {
			final String operator = symbolicApply.getOperator();
			goToBreakpoint(signatureOf(operator), 0, numberOfHits);
			
			//steps and decides
			this.symbolicApplyRetValue = stepUpToMethodExit();
		}

		private static Signature signatureOf(String unintFuncOperator) {
			final String[] parts = unintFuncOperator.split(":");
			return new Signature(parts[0], parts[1], parts[2]);
		}

		private Value stepUpToMethodExit() throws GuidanceException {
			final int currFrames;
			try {
				currFrames = getCurrentThread().frameCount();
			} catch (IncompatibleThreadStateException e) {
				throw new GuidanceException(e);
			}

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
						final Event event = it.nextEvent();

						if (event instanceof MethodExitEvent) {
							mthdExitEvent = (MethodExitEvent) event;
							if (mthdExitEvent.thread().frameCount() == currFrames) { 
								exitFound = true;
								this.currentExecutionPointEvent = event;
							}
						}
					}
					if (!exitFound) {
						eventSet.resume();
					}
				} catch (InterruptedException e) {
					throw new GuidanceException(e);
					//TODO is it ok?
				} catch (VMDisconnectedException | IncompatibleThreadStateException e) {
					throw new GuidanceException(e);
				}
			}

			mexr.disable();
			return mthdExitEvent.returnValue();
		}
		
	}	
	
}

