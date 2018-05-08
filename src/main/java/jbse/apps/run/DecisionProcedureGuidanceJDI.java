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
import jbse.val.Access;
import jbse.val.AccessArrayLength;
import jbse.val.AccessArrayMember;
import jbse.val.AccessField;
import jbse.val.AccessHashCode;
import jbse.val.AccessLocalVariable;
import jbse.val.AccessStatic;
import jbse.val.Calculator;
import jbse.val.MemoryPath;
import jbse.val.Simplex;

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

    private static class JVMJDI extends JVM {
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
        public String typeOfObject(MemoryPath origin) throws GuidanceException {
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
        public boolean isNull(MemoryPath origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
            return (object == null);
        }
        
        @Override
        public boolean areAlias(MemoryPath first, MemoryPath second) throws GuidanceException {
            final ObjectReference objectFirst = (ObjectReference) getJDIValue(first);
            final ObjectReference objectSecond = (ObjectReference) getJDIValue(second);
            return ((objectFirst == null && objectSecond == null) || 
                    (objectFirst != null && objectSecond != null && objectFirst.equals(objectSecond)));
        }

        @Override
        public Object getValue(MemoryPath origin) throws GuidanceException {
            final com.sun.jdi.Value val = getJDIValue(origin);
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
        
        private com.sun.jdi.Value getJDIValue(MemoryPath origin) throws GuidanceException {
            try {
                boolean someValue = false;
                com.sun.jdi.Value value = null;
                com.sun.jdi.ReferenceType t = null;
                com.sun.jdi.ObjectReference o = null;
                for (Access a : origin) {
                    if (a instanceof AccessLocalVariable) {
                        someValue = true;
                        value = getJDIValueLocalVariable(((AccessLocalVariable) a).variableName());
                    } else if (a instanceof AccessStatic) {
                        someValue = false;
                        t = getJDIObjectStatic(((AccessStatic) a).className());
                        o = null;
                    } else if (a instanceof AccessField) {
                        someValue = true;
                        value = getJDIValueField(((AccessField) a).fieldName(), t, o);
                    } else if (a instanceof AccessArrayLength) {
                        if (!(o instanceof ArrayReference)) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        someValue = true;
                        value = this.vm.mirrorOf(((ArrayReference) o).length());
                    } else if (a instanceof AccessArrayMember) {
                        if (!(o instanceof ArrayReference)) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        someValue = true;
                        try {
                            final Simplex index = (Simplex) eval(((AccessArrayMember) a).index());
                            value = ((ArrayReference) o).getValue(((Integer) index.getActualValue()).intValue());
                        } catch (ClassCastException e) {
                            throw new GuidanceException(e);
                        }
                    } else if (a instanceof AccessHashCode) {
                        if (o == null) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        someValue = true;
                        final Method hashCode = o.referenceType().methodsByName("hashCode", "()I").get(0);
                        value = o.invokeMethod(this.methodEntryEvent.thread(), hashCode, Collections.emptyList(), 0);
                    }
                    if (value instanceof ObjectReference) {
                        t = null;
                        o = (ObjectReference) value;
                    } else if (someValue) {
                        t = null;
                        o = null; 
                    }
                }
                if (!someValue) {
                    throw new GuidanceException(ERROR_BAD_PATH);
                }
                return value;
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
        
        private com.sun.jdi.Value getJDIValueField(String fieldName, com.sun.jdi.ReferenceType t, com.sun.jdi.ObjectReference o) 
        throws GuidanceException {
            final Field fld = (t == null ? o.referenceType().fieldByName(fieldName) : t.fieldByName(fieldName));
            if (fld == null) {
                throw new GuidanceException(ERROR_BAD_PATH);
            }
            try {
                return (t == null ? o.getValue(fld) : t.getValue(fld));
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

