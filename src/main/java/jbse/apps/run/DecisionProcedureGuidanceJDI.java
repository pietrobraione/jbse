package jbse.apps.run;

import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.isPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;

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
import com.sun.jdi.InvocationException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortValue;
import com.sun.jdi.StackFrame;
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
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import jbse.bc.Signature;
import jbse.dec.DecisionProcedure;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.val.Calculator;
import jbse.val.KlassPseudoReference;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.ReferenceSymbolic;
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
            final Map<String, Connector.Argument> arguments =
                connectorArguments(connector, mainArgs);
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

        private void setEventRequests() {
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
        }

        private void run() {
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
                } else {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
            } catch (IncompatibleThreadStateException | AbsentInformationException | 
            com.sun.jdi.InvalidTypeException | ClassNotLoadedException | 
            InvocationException e) {
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
        protected void close() {
            this.vm.exit(0);

            //obviates to inferior process leak
            this.vm.process().destroyForcibly();
        }
    }
}

