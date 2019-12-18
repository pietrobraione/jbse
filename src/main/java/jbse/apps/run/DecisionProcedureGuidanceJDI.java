package jbse.apps.run;

import static jbse.bc.Offsets.offset;
import static jbse.bc.Offsets.ANEWARRAY_OFFSET;
import static jbse.bc.Offsets.INVOKESPECIALSTATICVIRTUAL_OFFSET;
import static jbse.bc.Offsets.MULTIANEWARRAY_OFFSET;
import static jbse.bc.Offsets.NEWARRAY_OFFSET;
import static jbse.bc.Offsets.XALOADSTORE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_WIDE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMMEDIATE_OFFSET;
import static jbse.bc.Offsets.XLOADSTORE_IMPLICIT_OFFSET;
import static jbse.bc.Opcodes.OP_ANEWARRAY;
import static jbse.bc.Opcodes.OP_IALOAD;
import static jbse.bc.Opcodes.OP_IASTORE;
import static jbse.bc.Opcodes.OP_IF_ACMPNE;
import static jbse.bc.Opcodes.OP_IFEQ;
import static jbse.bc.Opcodes.OP_ILOAD;
import static jbse.bc.Opcodes.OP_ILOAD_0;
import static jbse.bc.Opcodes.OP_ILOAD_1;
import static jbse.bc.Opcodes.OP_ILOAD_2;
import static jbse.bc.Opcodes.OP_ILOAD_3;
import static jbse.bc.Opcodes.OP_INVOKEHANDLE;
import static jbse.bc.Opcodes.OP_INVOKEDYNAMIC;
import static jbse.bc.Opcodes.OP_INVOKESTATIC;
import static jbse.bc.Opcodes.OP_INVOKEVIRTUAL;
import static jbse.bc.Opcodes.OP_IRETURN;
import static jbse.bc.Opcodes.OP_LOOKUPSWITCH;
import static jbse.bc.Opcodes.OP_MULTIANEWARRAY;
import static jbse.bc.Opcodes.OP_TABLESWITCH;
import static jbse.bc.Opcodes.OP_NEWARRAY;
import static jbse.bc.Opcodes.OP_RETURN;
import static jbse.bc.Opcodes.OP_SALOAD;
import static jbse.bc.Opcodes.OP_SASTORE;
import static jbse.bc.Opcodes.OP_WIDE;
import static jbse.bc.Opcodes.opcodeName;
import static jbse.bc.Signatures.JAVA_STRING_EQUALS;
import static jbse.bc.Signatures.JAVA_STRING_HASHCODE;
import static jbse.common.Type.INT;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.isPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.toPrimitiveOrVoidInternalName;
import static jbse.common.Util.asUnsignedByte;
import static jbse.common.Util.byteCat;
import static jbse.common.Util.byteCatShort;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
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

import jbse.bc.Signature;
import jbse.common.exc.InvalidInputException;
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
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

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

    private static final class JVMJDI extends JVM {
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path: ";
        private static final String[] EXCLUDES = {"java.*", "javax.*", "sun.*", "com.sun.*"};

        private final String startMethodClassName;
        private final String startMethodDescriptor;
        private final String startMethodName;
        private final String stopMethodClassName;
        private final String stopMethodDescriptor;
        private final String stopMethodName;  
        private final int numberOfHits;
        private final VirtualMachine vm;
        private boolean intoMethodRunnPar = false;
        private int hitCounter = 0;
        private MethodEntryEvent methodEntryEvent;
        private int numOfFramesAtMethodEntry;
        private StepEvent currentStepEvent;        
        private boolean jdiIsWaitingForJBSE = false;
        private boolean jbseIsDoingClinit = false; //HACK

        private Map<ReferenceSymbolicApply, Object> unintFuncsNonPrimitiveRetValues = new HashMap<>();
        private boolean lookAheadDone = false;
        private boolean lookAheadDecisionBoolean;
        private boolean lookAheadDecisionIsDefaultCase;
        private int lookAheadDecisionCaseValue;
        private ObjectReference lookAheadUnintFuncNonPrimitiveRetValue;
        private int previousCodeIndex = -1;
        private Primitive xaloadIndex = null;

        public JVMJDI(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            super(calc, runnerParameters, stopSignature, numberOfHits);
            this.startMethodClassName = runnerParameters.getMethodSignature().getClassName();
            this.startMethodDescriptor = runnerParameters.getMethodSignature().getDescriptor();
            this.startMethodName = runnerParameters.getMethodSignature().getName();
            this.stopMethodClassName = stopSignature.getClassName();
            this.stopMethodDescriptor = stopSignature.getDescriptor();
            this.stopMethodName = stopSignature.getName();
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
                return launchTarget("-classpath \"" + stringClassPath + "\" " + mainClass + " " + targetClass + " " + this.startMethodName);
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

        private void run() throws GuidanceException {
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
                    throw new GuidanceException(e);
                    //TODO is it ok?
                } catch (VMDisconnectedException e) {
                    if (testMethodEntryFound) {
                        return; //must not try to disable event requests
                    } else {
                        throw new GuidanceException(e);
                    }
                }
            }

            //disables event requests
            mexr.disable();
            menr.disable();
        }

        private boolean checkIfMethodEntry(Event event) {
            if (event instanceof MethodExitEvent) {
                final Method jdiMeth = ((MethodExitEvent) event).method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                if (this.startMethodClassName.equals(jdiMethClassName) &&
                	this.startMethodDescriptor.equals(jdiMethDescr) &&
                	this.startMethodName.equals(jdiMethName)) {
                	this.intoMethodRunnPar = false;
                }
            }
            if (event instanceof MethodEntryEvent) {
                final Method jdiMeth = ((MethodEntryEvent) event).method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                if (this.startMethodClassName.equals(jdiMethClassName) &&
                this.startMethodDescriptor.equals(jdiMethDescr) &&
                this.startMethodName.equals(jdiMethName)) {
                    this.hitCounter = 0;
                    this.intoMethodRunnPar = true;
                }
                if (this.stopMethodClassName.equals(jdiMethClassName) &&
                    this.stopMethodDescriptor.equals(jdiMethDescr) &&
                    this.stopMethodName.equals(jdiMethName) && 
                    this.intoMethodRunnPar) {
                    ++this.hitCounter;
                    if (this.hitCounter == this.numberOfHits) {
                        this.methodEntryEvent = (MethodEntryEvent) event;
                        try {
                            this.numOfFramesAtMethodEntry = this.methodEntryEvent.thread().frameCount();
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
        public String typeOfObject(ReferenceSymbolic origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
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
                        throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
                    }
                    return getJDIValueField(((SymbolicMemberField) origin), o);
                } else if (origin instanceof PrimitiveSymbolicMemberArrayLength) {
                    final Object o = getJDIValue(((PrimitiveSymbolicMemberArrayLength) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
                    }
                    return this.vm.mirrorOf(((ArrayReference) o).length());
                } else if (origin instanceof SymbolicMemberArray) {
                    final Object o = getJDIValue(((SymbolicMemberArray) origin).getContainer());
                    if (!(o instanceof ArrayReference)) {
                        throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
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
                        throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
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
                    throw new GuidanceException(ERROR_BAD_PATH + origin.asOriginString());
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


        @Override
        public Primitive eval_IFX(DecisionAlternative_IFX da, Primitive condToEval) throws GuidanceException {			
            if (this.jdiIsWaitingForJBSE) {
                return super.eval_IFX(da, condToEval);
            }

            exec_IFX_lookAhead();

            return calc.valBoolean(da.value() == this.lookAheadDecisionBoolean);
        }

        @Override
        public Primitive eval_XCMPY(DecisionAlternative_XCMPY da, Primitive val1, Primitive val2) throws GuidanceException {
            if (this.jdiIsWaitingForJBSE) {
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
            if (this.jdiIsWaitingForJBSE) {
                return super.eval_XSWITCH(da, selector, tab);
            }
            exec_XSWITCH_lookAhead();	
            return this.calc.valBoolean((da.isDefault() && this.lookAheadDecisionIsDefaultCase) || da.value() == this.lookAheadDecisionCaseValue);
        }

        @Override
        public Primitive eval_XNEWARRAY(DecisionAlternative_XNEWARRAY da, Primitive countsNonNegative) throws GuidanceException {
            if (this.jdiIsWaitingForJBSE) {
                return super.eval_XNEWARRAY(da, countsNonNegative);
            }
            exec_XNEWARRAY_lookAhead();	
            return this.calc.valBoolean(da.ok() == this.lookAheadDecisionBoolean);
        }

        @Override
        public Primitive eval_XASTORE(DecisionAlternative_XASTORE da, Primitive inRange) throws GuidanceException {
            if (this.jdiIsWaitingForJBSE) {
                return super.eval_XASTORE(da, inRange);
            }
            exec_XASTORE_lookAhead();
            return this.calc.valBoolean(da.isInRange() == this.lookAheadDecisionBoolean);
        }

        @Override
        public Primitive eval_XALOAD(DecisionAlternative_XALOAD da) throws GuidanceException {
            //Note that handling the identification of the actual array range that is being loaded is
            //hard because it cannot be inferred by performing lookahead (XALOAD bytecodes jump only
            //on out-of-range) and it is not possible to spy via JDI the top of the operand stack to
            //get the index and determine whether the access is ok.
            //Our partial solution is to perform lookahead nevertheless to distinguish the out-of-range
            //case, and if we are not in such case, to read the bytecode before the current one, in hope
            //that it reveals that the index was taken from a local variable.
            if (this.jdiIsWaitingForJBSE) {
                return super.eval_XALOAD(da);
            }
            exec_XALOAD_lookAhead();
            if (this.lookAheadDecisionBoolean) {
                //in range: looks whether an index is available and 
                //uses it to decide the array access expression of da
                if (this.xaloadIndex == null) {
                    //no index: falls back on accepting all the DecisionAlternative_XALOAD_In
                    return this.calc.valBoolean(da instanceof DecisionAlternative_XALOAD_In);
                } else {
                    final Primitive accessExpressionOnConcreteIndex;
					try {
						accessExpressionOnConcreteIndex = this.calc.push(da.getArrayAccessExpression()).replace(da.getIndexFormal(), this.xaloadIndex).pop();
					} catch (InvalidOperandException | InvalidInputException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
                    if (accessExpressionOnConcreteIndex instanceof Simplex) {
                        return accessExpressionOnConcreteIndex;
                    } else {
                        //the access condition depends on more symbols than just the index: 
                        //falls back on accepting all the DecisionAlternative_XALOAD_In
                        return this.calc.valBoolean(da instanceof DecisionAlternative_XALOAD_In);
                    }
                }
            } else {
                return this.calc.valBoolean(da instanceof DecisionAlternative_XALOAD_Out);
            }
        }

        private static boolean isInvoke(byte currentOpcode) {
            return ((OP_INVOKEVIRTUAL <= currentOpcode && currentOpcode <= OP_INVOKEDYNAMIC) ||
                    currentOpcode == OP_INVOKEHANDLE);
        }

        private static boolean isReturn(byte currentOpcode) {
            return (OP_IRETURN <= currentOpcode && currentOpcode <= OP_RETURN);
        }

        /**
         * Does a single execution step of the concrete state.
         * 
         * @param doStepInto if {@code true} and the current bytecode is an INVOKEX bytecode,
         *        steps into the invoked method; if {@code false} and the current bytecode is 
         *        an INVOKEX bytecode, steps over.
         * @throws GuidanceException 
         */
        private void doStep(boolean doStepInto) throws GuidanceException {
            final int stepDepth = doStepInto ? StepRequest.STEP_INTO : StepRequest.STEP_OVER;

            final ThreadReference thread = this.methodEntryEvent.thread();
            final EventRequestManager mgr = this.vm.eventRequestManager();
            final StepRequest sr = mgr.createStepRequest(thread, StepRequest.STEP_MIN, stepDepth);
            sr.enable();

            //if we are at an ILOAD bytecode followed by an XALOAD, 
            //we store the value from the variable because it is used
            //as XALOAD index
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            final byte currentOpcode = bc[currentCodeIndex];
            if (currentOpcode == OP_ILOAD || 
                (OP_ILOAD_0 <= currentOpcode && currentOpcode <= OP_ILOAD_3)) {
                final boolean wide = (this.previousCodeIndex >= 0 && bc[this.previousCodeIndex] == OP_WIDE);
                final int nextCodeIndex; 
                if (currentOpcode == OP_ILOAD) {
                    nextCodeIndex = currentCodeIndex + (wide ? XLOADSTORE_IMMEDIATE_WIDE_OFFSET : XLOADSTORE_IMMEDIATE_OFFSET);
                } else {
                    nextCodeIndex = currentCodeIndex + XLOADSTORE_IMPLICIT_OFFSET;
                }
                final byte opcodeNext = bc[nextCodeIndex];
                if (OP_IALOAD <= opcodeNext && opcodeNext <= OP_SALOAD) {
                    //determines the index of the local variable
                    final int localVariableIndex;
                    if (currentOpcode == OP_ILOAD_0) {
                        localVariableIndex = 0;
                    } else if (currentOpcode == OP_ILOAD_1) {
                        localVariableIndex = 1;
                    } else if (currentOpcode == OP_ILOAD_2) {
                        localVariableIndex = 2;
                    } else if (currentOpcode == OP_ILOAD_3) {
                        localVariableIndex = 3;
                    } else {
                        localVariableIndex = (wide ? byteCat(bc[currentCodeIndex + 1], bc[currentCodeIndex + 2]) : asUnsignedByte(bc[currentCodeIndex + 1]));
                    }
                    this.xaloadIndex = readLocalVariable(localVariableIndex);
                } else {
                    this.xaloadIndex = null;
                }
            } else if (OP_IALOAD <= currentOpcode && currentOpcode <= OP_SALOAD) {
                //does nothing
            } else {
                this.xaloadIndex = null;
            }
            if (isInvoke(currentOpcode) || isReturn(currentOpcode)) {
                //no valid previous code index
                this.previousCodeIndex = -1;
            } else {
                this.previousCodeIndex = currentCodeIndex;
            }
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
                } catch (InterruptedException | VMDisconnectedException e) {
                    throw new GuidanceException(e);
                }
            }

            sr.disable();
        }        

        private void exec_INVOKEX_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //checks
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            if (bc[currentCodeIndex] < OP_INVOKEVIRTUAL || bc[currentCodeIndex] > OP_INVOKEDYNAMIC) {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during INVOKEX lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps and decides
            try {
                final int intialFrames = this.currentStepEvent.thread().frameCount();
                doStep(true); //true -> StepInto  
                final int currFramesStepPre = this.currentStepEvent.thread().frameCount();
                if (currFramesStepPre <= intialFrames) {
                    throw new GuidanceException("Error during INVOKEX lookahead: I expected to step into a new frame");
                }

                this.lookAheadUnintFuncNonPrimitiveRetValue = (ObjectReference) stepUpToMethodExit();

                doStep(false); //false -> StepOver  
                final int currFramesStepPost = this.currentStepEvent.thread().frameCount();
                if (currFramesStepPost != intialFrames) {
                    throw new GuidanceException("Error during INVOKEX lookahead: I expected to step into a new frame");
                }

            } catch (IncompatibleThreadStateException e) {
                throw new GuidanceException(e);
            }
            this.lookAheadDone = true;
        }


        private Value stepUpToMethodExit() throws IncompatibleThreadStateException, GuidanceException {
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
                    throw new GuidanceException(e);
                    //TODO is it ok?
                } catch (VMDisconnectedException e) {
                    throw new GuidanceException(e);
                }
            }

            mexr.disable();
            return mthdExitEvent.returnValue();
        }

        private void exec_IFX_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //checks
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            if (bc[currentCodeIndex] < OP_IFEQ || bc[currentCodeIndex] > OP_IF_ACMPNE) {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during IFX lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps
            lookAhead();

            //takes the decision
            final int newOffset = getCurrentCodeIndex();
            final int jumpOffset = currentCodeIndex + byteCatShort(bc[currentCodeIndex + 1], bc[currentCodeIndex + 2]);
            this.lookAheadDecisionBoolean = (newOffset == jumpOffset); 
        }

        private void exec_XSWITCH_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //checks
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            if (bc[currentCodeIndex] != OP_LOOKUPSWITCH && bc[currentCodeIndex] != OP_TABLESWITCH) {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during XSWITCH lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps
            lookAhead();

            //takes the decision
            final int newOffset = getCurrentCodeIndex();
            final int padding = 3 - (currentCodeIndex % 4);
            int nextParamStartIndex = currentCodeIndex + padding + 1;
            final int defaultCaseOffset = currentCodeIndex + byteCat(bc[nextParamStartIndex], bc[nextParamStartIndex + 1], bc[nextParamStartIndex + 2], bc[nextParamStartIndex + 3]);
            nextParamStartIndex += 4;
            if (newOffset == defaultCaseOffset) {
                this.lookAheadDecisionIsDefaultCase = true; 
                return;
            } 
            this.lookAheadDecisionIsDefaultCase = false; 
            if (bc[currentCodeIndex] == OP_LOOKUPSWITCH) {
                int npairs = byteCat(bc[nextParamStartIndex], bc[nextParamStartIndex + 1], bc[nextParamStartIndex + 2], bc[nextParamStartIndex + 3]); 
                nextParamStartIndex += 4;		

                for (int i = 0; i < npairs; i++, nextParamStartIndex += 8) {
                    final int caseValue = byteCat(bc[nextParamStartIndex], bc[nextParamStartIndex + 1], bc[nextParamStartIndex + 2], bc[nextParamStartIndex + 3]); 
                    final int caseOffset = currentCodeIndex + byteCat(bc[nextParamStartIndex + 4], bc[nextParamStartIndex + 5], bc[nextParamStartIndex + 6], bc[nextParamStartIndex + 7]); 

                    if (newOffset == caseOffset) {
                        this.lookAheadDecisionCaseValue = caseValue;
                        return;
                    }
                }
            } else { //(bc[currentCodeIndex] == Opcodes.OP_TABLESWITCH)
                final int low = byteCat(bc[nextParamStartIndex], bc[nextParamStartIndex + 1], bc[nextParamStartIndex + 2], bc[nextParamStartIndex + 3]); 
                final int high = byteCat(bc[nextParamStartIndex + 4], bc[nextParamStartIndex + 5], bc[nextParamStartIndex + 6], bc[nextParamStartIndex + 7]); 
                final int entries = high - low; 
                nextParamStartIndex += 8;

                for (int i = 0; i < entries; i++, nextParamStartIndex += 4) {
                    final int caseValue = low + i; 
                    final int caseOffset = currentCodeIndex + byteCat(bc[nextParamStartIndex], bc[nextParamStartIndex + 1], bc[nextParamStartIndex + 2], bc[nextParamStartIndex + 3]); 
                    if (newOffset == caseOffset) {
                        this.lookAheadDecisionCaseValue = caseValue;
                        return;
                    }
                }
            }

            throw new GuidanceException("Wrong step at SWITCH: offset " + newOffset + " does not correspond to any case/default jump");
        }

        private void exec_XALOAD_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //checks
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            final int nextCodeIndex;
            if (bc[currentCodeIndex] >= OP_IALOAD && bc[currentCodeIndex] <= OP_SALOAD) {
                nextCodeIndex = currentCodeIndex + XALOADSTORE_OFFSET;
            } else if (bc[currentCodeIndex] == OP_INVOKEVIRTUAL) { //invokevirtual sun.misc.Unsafe.getIntVolatile or invokevirtual sun.misc.Unsafe.getObjectVolatile
                nextCodeIndex = currentCodeIndex + INVOKESPECIALSTATICVIRTUAL_OFFSET;
            } else {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during XALOAD (or INVOKEVIRTUAL sun/misc/Unsafe:getIntVolatile, or INVOKEVIRTUAL sun/misc/Unsafe:getObjectVolatile) lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps
            lookAhead();

            //takes the decision
            calcLookaheadDecision(nextCodeIndex);
        }

        private final static short JDWP_INVALID_SLOT = (short) 35;

        /*
         * Code taken from JetBrains IntelliJ source code, 
         * https://github.com/JetBrains/intellij-community/blob/master/java/debugger/impl/src/com/intellij/debugger/jdi/LocalVariablesUtil.java
         */
        private Primitive readLocalVariable(int localVariableIndex) throws GuidanceException {
            //Uses JDWP to take the value of a local variable (works even if debug info is missing)
            try {
                final String getValuesClassName = "com.sun.tools.jdi.JDWP$StackFrame$GetValues";
                final Class<?> ourSlotInfoClass = Class.forName(getValuesClassName + "$SlotInfo");
                final Constructor<?> slotInfoConstructor = ourSlotInfoClass.getDeclaredConstructor(int.class, byte.class);
                slotInfoConstructor.setAccessible(true);

                final Class<?> ourGetValuesClass = Class.forName(getValuesClassName);
                final java.lang.reflect.Method ourEnqueueMethod = getDeclaredMethodByName(ourGetValuesClass, "enqueueCommand");
                final java.lang.reflect.Method ourWaitForReplyMethod = getDeclaredMethodByName(ourGetValuesClass, "waitForReply");

                final StackFrame frame = this.currentStepEvent.thread().frame(0);

                final java.lang.reflect.Field frameIdField = frame.getClass().getDeclaredField("id");
                frameIdField.setAccessible(true);
                final Long frameId = frameIdField.getLong(frame);
                final VirtualMachine vm = frame.virtualMachine();
                final java.lang.reflect.Method stateMethod = vm.getClass().getDeclaredMethod("state");
                stateMethod.setAccessible(true);

                final Object slotInfoArray = Array.newInstance(ourSlotInfoClass, 1);
                final Object info = slotInfoConstructor.newInstance(localVariableIndex, (byte) INT);
                Array.set(slotInfoArray, 0, info);
                Object ps;
                final Object vmState = stateMethod.invoke(vm);
                synchronized(vmState) {
                    ps = ourEnqueueMethod.invoke(null, vm, frame.thread(), frameId, slotInfoArray);
                }

                final Object reply;
                try {
                    reply = ourWaitForReplyMethod.invoke(null, vm, ps);
                } catch (InvocationTargetException e) {
                    final String jdwpExceptionClassName = "com.sun.tools.jdi.JDWPException";
                    if (jdwpExceptionClassName.equals(e.getTargetException().getClass().getName())) {
                        final Class<?> jdwpExceptionClass = Class.forName(jdwpExceptionClassName);
                        final java.lang.reflect.Field errorCodeField = jdwpExceptionClass.getDeclaredField("errorCode");
                        errorCodeField.setAccessible(true);
                        final short errorCode = errorCodeField.getShort(e.getTargetException());
                        if (errorCode == JDWP_INVALID_SLOT) {
                            return null; //give up
                        }
                    }
                    throw new GuidanceException(e);
                }
                final java.lang.reflect.Field replyValuesField = reply.getClass().getDeclaredField("values");
                replyValuesField.setAccessible(true);
                final com.sun.jdi.Value[] values = (com.sun.jdi.Value[]) replyValuesField.get(reply);
                if (values.length != 1) {
                    throw new GuidanceException("Wrong number of values returned from target VM");
                }
                com.sun.jdi.Value jdiIndex = values[0];

                return this.calc.valInt(((IntegerValue) jdiIndex).intValue());
            } catch (IncompatibleThreadStateException | IndexOutOfBoundsException | ClassCastException e) {
                throw new GuidanceException(e);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException | 
                     IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }

        /*
         * Code taken from JetBrains IntelliJ source code, 
         * https://github.com/JetBrains/intellij-community/blob/master/platform/util/src/com/intellij/util/ReflectionUtil.java
         */
        private static java.lang.reflect.Method getDeclaredMethodByName(Class<?> aClass, String methodName) throws NoSuchMethodException {
            for (java.lang.reflect.Method method : aClass.getDeclaredMethods()) {
                if (methodName.equals(method.getName())) {
                    method.setAccessible(true);
                    return method;
                }
            }
            throw new NoSuchMethodException(aClass.getName() + "." + methodName);
        }

        private void exec_XNEWARRAY_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //check
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            final int nextCodeIndex;
            if (bc[currentCodeIndex] == OP_NEWARRAY) {
                nextCodeIndex = currentCodeIndex + NEWARRAY_OFFSET;
            } else if (bc[currentCodeIndex] == OP_ANEWARRAY) {
                nextCodeIndex = currentCodeIndex + ANEWARRAY_OFFSET;
            } else if (bc[currentCodeIndex] == OP_MULTIANEWARRAY) {
                nextCodeIndex = currentCodeIndex + MULTIANEWARRAY_OFFSET;
            } else {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during XNEWARRAY lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps
            lookAhead();

            //takes the decision
            calcLookaheadDecision(nextCodeIndex);
        }

        private void exec_XASTORE_lookAhead() throws GuidanceException {
            if (this.lookAheadDone) {
                return;
            }

            //check
            final int currentCodeIndex = getCurrentCodeIndex();
            final byte[] bc = getCurrentBytecode();
            final int nextCodeIndex;
            if (bc[currentCodeIndex] >= OP_IASTORE && bc[currentCodeIndex] <= OP_SASTORE) {
                nextCodeIndex = currentCodeIndex + XALOADSTORE_OFFSET;
            } else if (bc[currentCodeIndex] == OP_INVOKESTATIC || bc[currentCodeIndex] == OP_INVOKEVIRTUAL) { //invokestatic java.lang.System.arrayCopy or invokevirtual sun.misc.Unsafe.putObjectVolatile
                nextCodeIndex = currentCodeIndex + INVOKESPECIALSTATICVIRTUAL_OFFSET;
            } else {
                final Method jdiMeth = this.currentStepEvent.location().method();
                final String jdiMethClassName = jdiMethodClassName(jdiMeth);
                final String jdiMethDescr = jdiMeth.signature();
                final String jdiMethName = jdiMeth.name();
                throw new GuidanceException("Wrong step alignment during XASTORE (or INVOKESTATIC java/lang/System:arrayCopy, or INVOKEVIRTUAL sun/misc/Unsafe:putObjectVolatile) lookahead (JDI at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + currentCodeIndex + ", bytecode = " + opcodeName(bc[currentCodeIndex]) + ")");
            }

            //steps
            lookAhead();

            //takes the decision
            calcLookaheadDecision(nextCodeIndex);
        }

        private void calcLookaheadDecision(long successorOffset) throws GuidanceException {
            final int newOffset = getCurrentCodeIndex();
            this.lookAheadDecisionBoolean = (newOffset == successorOffset); 
        }

        private void lookAhead() throws GuidanceException {
            doStep(false); //false -> StepOver
            this.lookAheadDone = true;
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

        @Override
        public void step(State jbseState) throws GuidanceException {
            try {
                final int jbseStackSize = jbseState.getStackSize();
                final int jdiStackSizeBeforeStep = numFramesFromRootFrameConcrete();
                if (this.jdiIsWaitingForJBSE) {
                    if (jbseStackSize == jdiStackSizeBeforeStep) {
                        //JBSE exited from the trigger/snippet: check alignment
                        this.jdiIsWaitingForJBSE = false; 
                        checkAlignmentWithJbseOrThrowException(jbseState); 
                    } //else, JBSE is not yet aligned: do not step and wait again
                } else if (this.jbseIsDoingClinit) {
                    if (jbseStackSize == jdiStackSizeBeforeStep - 1) {
                        //returned from clinit
                        this.jbseIsDoingClinit = false;
                        this.lookAheadDone = true; //the next bytecode is an invoke
                    }
                } else if (this.lookAheadDone) {
                    //finished lookahead: check alignment
                    this.lookAheadDone = false; 
                    checkAlignmentWithJbseOrThrowException(jbseState);
                } else {
                    if (jbseStackSize == 0) {
                        return; //avoid stepping JDI since JBSE terminated
                    }

                    //do step into or step over according to JBSE stack size;
                    //if the method is String.hashCode() JDI steps over and 
                    //waits for JBSE
                    final boolean doStepInto = jbseStackSize > jdiStackSizeBeforeStep && notSkip(jbseState);
                    doStep(doStepInto);

                    final int jdiStackSizeAfterStep = numFramesFromRootFrameConcrete();
                    if (jbseStackSize == jdiStackSizeAfterStep) {
                        //JDI should be aligned with JBSE
                        /* TODO actually JDI and JBSE could also be misaligned: JBSE 
                         * could be in the <clinit> of a class and JDI could be in 
                         * the classloader code loading that class or in another 
                         * invoked method.
                         * In this case JBSE should wait for JDI to return from 
                         * the classloader and load the <clinit>, given that 
                         * between the two something else does not happen.
                         */ 
                        checkAlignmentWithJbseOrThrowException(jbseState);
                    } else {
                        //JDI lost alignment with JBSE: this happens when JBSE is running a
                        //trigger or a snippet. JDI must wait until JBSE terminates the execution
                        this.jdiIsWaitingForJBSE = true;
                    }
                }
            } catch (ThreadStackEmptyException | IncompatibleThreadStateException | FrozenStateException e) {
                throw new GuidanceException(e); //TODO better exception!
            }
        }
        
        private boolean notSkip(State jbseState) throws ThreadStackEmptyException {
            //some java.lang.String method should be skipped because
            //concrete and symbolic execution might differ on interning;
            //in this case alignment must be checked at the exit of the method
            final Signature jbseMeth = jbseState.getCurrentMethodSignature();
            if (jbseMeth.equals(JAVA_STRING_EQUALS)) {
                return false;
            }
            if (jbseMeth.equals(JAVA_STRING_HASHCODE)) {
                return false;
            }
            return true;
        }

        private static String jdiMethodClassName(Method jdiMeth) {
            return jdiMeth.toString().substring(0, jdiMeth.toString().indexOf(jdiMeth.name() + '(') - 1).replace('.', '/');
        }

        private void checkAlignmentWithJbseOrThrowException(State jbseState) 
        throws FrozenStateException, GuidanceException, ThreadStackEmptyException, IncompatibleThreadStateException {
            //HACK: if JDI is loading a set of classes just wait for it to end (JBSE does not invoke the classloader in the current configuration)  
            while (jdiMethodClassName(this.currentStepEvent.location().method()).equals("java/lang/ClassLoader")) {
                stepUpToMethodExit();
                doStep(true); //true -> StepInto
            }
            //TODO pedantically walk the whole stack and check all frames; by now we are less paranoid and just check the stack depth and the topmost frame

            //gets JDI stack data
            final int jdiStackSize = numFramesFromRootFrameConcrete();
            final Method jdiMeth = this.currentStepEvent.location().method();
            final String jdiMethClassName = jdiMethodClassName(jdiMeth);
            final String jdiMethDescr = jdiMeth.signature();
            final String jdiMethName = jdiMeth.name();
            final int jdiProgramCounter = getCurrentCodeIndex();

            //gets JBSE stack data
            final int jbseStackSize = jbseState.getStackSize();
            final Signature jbseMeth = jbseState.getCurrentMethodSignature();
            final String jbseMethClassname = jbseMeth.getClassName() == null ? null : jbseMeth.getClassName();
            final String jbseMethDescr = jbseMeth.getDescriptor() == null ? null : jbseMeth.getDescriptor();
            final String jbseMethName = jbseMeth.getName() == null ? null : jbseMeth.getName();
            final int jbseProgramCounter = jbseState.getCurrentProgramCounter();

            if (jdiStackSize == jbseStackSize && jdiMethName.equals(jbseMethName) && 
                jdiMethDescr.equals(jbseMethDescr) && jdiMethClassName.equals(jbseMethClassname) &&
                jdiProgramCounter == jbseProgramCounter) {
                return;
            //HACK: if JBSE is just one bytecode behind JDI, then step and realign: This compensates
            //the fact that, in some situations (e.g., after class initialization), JBSE repeats 
            //a previously abandoned execution of a bytecode, while JDI does not  
            } else if (jdiStackSize == jbseStackSize && jdiMethName.equals(jbseMethName) && 
                       jdiMethDescr.equals(jbseMethDescr) && jdiMethClassName.equals(jbseMethClassname) &&
                       jdiProgramCounter == jbseProgramCounter + offset(jbseState.getCurrentFrame().getCode(), jbseProgramCounter, -1)) {
                this.lookAheadDone = true;
                return;
            //HACK: if JBSE is in a <clinit> method and JDI is not, then wait for JBSE: This compensates
            //the fact that, in some situations, JBSE runs a class initializer that JDI did run before 
            //(note howewer that, if the stack size is the same, JDI is inside a method invocation)  
            } else if (jdiStackSize == jbseStackSize && "<clinit>".equals(jbseMethName)) {
                this.jbseIsDoingClinit = true;
                return;
            } else {
                throw new GuidanceException("JDI alignment with JBSE failed unexpectedly: JBSE is at " + jbseState.getCurrentMethodSignature() + ":" + jbseProgramCounter + ", while JDI is at " + jdiMethClassName + ":" + jdiMethDescr + ":" + jdiMethName + ":" + jdiProgramCounter);
            }	
        }
        
        @Override
        protected Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
            if (this.currentStepEvent == null && this.methodEntryEvent == null) {
                throw new ThreadStackEmptyException();
            }
            final Method jdiMeth = (this.currentStepEvent == null ? this.methodEntryEvent.location().method() : this.currentStepEvent.location().method());
            final String jdiMethClassName = jdiMethodClassName(jdiMeth);
            final String jdiMethDescr = jdiMeth.signature();
            final String jdiMethName = jdiMeth.name();
            return new Signature(jdiMethClassName, jdiMethDescr, jdiMethName);
        }

        @Override
        protected int getCurrentProgramCounter() throws ThreadStackEmptyException {
            if (this.currentStepEvent == null && this.methodEntryEvent == null) {
                throw new ThreadStackEmptyException();
            }
            return getCurrentCodeIndex();
        }
        
        @Override
        protected void close() {
            this.vm.exit(0);

            //obviates to inferior process leak
            this.vm.process().destroyForcibly();
        }
    }
}

