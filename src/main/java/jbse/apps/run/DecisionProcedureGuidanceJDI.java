package jbse.apps.run;

import static jbse.common.Type.binaryClassName;
import static jbse.common.Type.internalClassName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

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
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Unresolved;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Aliases;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Expands;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Null;
import jbse.val.Access;
import jbse.val.AccessArrayLength;
import jbse.val.AccessArrayMember;
import jbse.val.AccessField;
import jbse.val.AccessHashCode;
import jbse.val.AccessLocalVariable;
import jbse.val.AccessStatic;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.MemoryPath;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureAlgorithms} for guided symbolic execution based on JDI. 
 * It keeps a private JVM that runs a guiding concrete execution up to the
 * concrete counterpart of the initial state, and filters all the decisions taken by 
 * the component decision procedure it decorates according to the state reached by the 
 * private engine.
 */
public final class DecisionProcedureGuidanceJDI extends DecisionProcedureAlgorithms {
    private final JVM jvm;
    private final HashSet<MemoryPath> seen = new HashSet<>();
    private boolean ended;    
    
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
        super(component, calc);
        if (numberOfHits < 1) {
            throw new GuidanceException("Invalid number of hits " + numberOfHits + ".");
        }
        goFastAndImprecise(); //disables theorem proving of component until guidance ends
        this.jvm = new JVM(calc, runnerParameters, stopSignature, numberOfHits);
        initSeen();
        this.ended = false;
    }

    /**
     * Ends guidance decision, and falls back on the 
     * component decision procedure.
     */
    public void endGuidance() {
        this.ended = true;
        stopFastAndImprecise();
    }

    @Override
    protected Outcome decide_IFX_Nonconcrete(ClassHierarchy hier, Primitive condition, SortedSet<DecisionAlternative_IFX> result) 
    throws DecisionException {
        final Outcome retVal = super.decide_IFX_Nonconcrete(hier, condition, result);
        if (!this.ended) {
            try {
                final Iterator<DecisionAlternative_IFX> it = result.iterator();
                while (it.hasNext()) {
                    final DecisionAlternative_IFX da = it.next();
                    final Primitive conditionNot = condition.not();
                    final Primitive conditionToCheck  = (da.value() ? condition : conditionNot);
                    final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                    if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                        it.remove();
                    }
                }
            } catch (InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome decide_XCMPY_Nonconcrete(ClassHierarchy hier, Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XCMPY_Nonconcrete(hier, val1, val2, result);
        if (!this.ended) {
            try {
                final Primitive comparisonGT = val1.gt(val2);
                final Primitive comparisonEQ = val1.eq(val2);
                final Primitive comparisonLT = val1.lt(val2);
                final Iterator<DecisionAlternative_XCMPY> it = result.iterator();
                while (it.hasNext()) {
                    final DecisionAlternative_XCMPY da = it.next();
                    final Primitive conditionToCheck  = 
                        (da.operator() == Operator.GT ? comparisonGT :
                         da.operator() == Operator.EQ ? comparisonEQ :
                         comparisonLT);
                    final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                    if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                        it.remove();
                    }
                }
            } catch (InvalidTypeException | InvalidOperandException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome decide_XSWITCH_Nonconcrete(ClassHierarchy hier, Primitive selector, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XSWITCH_Nonconcrete(hier, selector, tab, result);
        if (!this.ended) {
            try {
                final Iterator<DecisionAlternative_XSWITCH> it = result.iterator();
                while (it.hasNext()) {
                    final DecisionAlternative_XSWITCH da = it.next();
                    //Value valueSelector = getValueParam(nameVar(selector));
                    final Primitive conditionToCheck =
                        (da.isDefault() ?
                         tab.getDefaultClause(selector) :
                         selector.eq(this.calc.valInt(da.value())));
                    final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                    if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                        it.remove();
                    }
                }
            } catch (InvalidOperandException | InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome decide_XNEWARRAY_Nonconcrete(ClassHierarchy hier, Primitive countsNonNegative, SortedSet<DecisionAlternative_XNEWARRAY> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XNEWARRAY_Nonconcrete(hier, countsNonNegative, result);
        if (!this.ended) {
            try {
                final Iterator<DecisionAlternative_XNEWARRAY> it = result.iterator();
                while (it.hasNext()) {
                    final DecisionAlternative_XNEWARRAY da = it.next();
                    final Primitive conditionToCheck = (da.ok() ? countsNonNegative : countsNonNegative.not());
                    final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                    if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                        it.remove();
                    }
                }
            } catch (InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome decide_XASTORE_Nonconcrete(ClassHierarchy hier, Primitive inRange, SortedSet<DecisionAlternative_XASTORE> result)
    throws DecisionException {
        final Outcome retVal = super.decide_XASTORE_Nonconcrete(hier, inRange, result);
        if (!this.ended) {
            try {
                final Iterator<DecisionAlternative_XASTORE> it = result.iterator();
                while (it.hasNext()) {
                    final DecisionAlternative_XASTORE da = it.next();
                    final Primitive conditionToCheck = (da.isInRange() ? inRange : inRange.not());
                    final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                    if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                        it.remove();
                    }
                }
            } catch (InvalidTypeException e) {
                //this should never happen as arguments have been checked by the caller
                throw new UnexpectedInternalException(e);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome resolve_XLOAD_GETX_Unresolved(State state, ReferenceSymbolic refToLoad, SortedSet<DecisionAlternative_XLOAD_GETX> result)
    throws DecisionException, BadClassFileException {
        updateExpansionBackdoor(state, refToLoad);
        final Outcome retVal = super.resolve_XLOAD_GETX_Unresolved(state, refToLoad, result);
        if (!this.ended) {
            final Iterator<DecisionAlternative_XLOAD_GETX> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XYLOAD_GETX_Unresolved dar = (DecisionAlternative_XYLOAD_GETX_Unresolved) it.next();
                filter(state, refToLoad, dar, it);
            }
        }
        return retVal;
    }

    @Override
    protected Outcome resolve_XALOAD_ResolvedNonconcrete(ClassHierarchy hier, Expression accessExpression, Value valueToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException {
        final Outcome retVal = super.resolve_XALOAD_ResolvedNonconcrete(hier, accessExpression, valueToLoad, fresh, result);
        if (!this.ended) {
            final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XALOAD da = it.next();
                final Primitive conditionToCheck = da.getArrayAccessExpression();
                final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                }
            }
        }
        return retVal;
    }

    @Override
    protected Outcome resolve_XALOAD_Unresolved(State state, Expression accessExpression, ReferenceSymbolic refToLoad, boolean fresh, SortedSet<DecisionAlternative_XALOAD> result)
    throws DecisionException, BadClassFileException {
        updateExpansionBackdoor(state, refToLoad);
        final Outcome retVal = super.resolve_XALOAD_Unresolved(state, accessExpression, refToLoad, fresh, result);
        if (!this.ended) {
            final Iterator<DecisionAlternative_XALOAD> it = result.iterator();
            while (it.hasNext()) {
                final DecisionAlternative_XALOAD_Unresolved dar = (DecisionAlternative_XALOAD_Unresolved) it.next();
                final Primitive conditionToCheck = dar.getArrayAccessExpression();
                final Primitive valueInConcreteState = this.jvm.eval(conditionToCheck);
                if (valueInConcreteState != null && valueInConcreteState.surelyFalse()) {
                    it.remove();
                } else {
                    filter(state, refToLoad, dar, it);
                }
            }
        }
        return retVal;
    }

    private void updateExpansionBackdoor(State state, ReferenceSymbolic refToLoad) throws GuidanceException {
        final String refType = Type.getReferenceClassName(refToLoad.getStaticType());
        final String objType = this.jvm.typeOfObject(refToLoad.getOrigin());
        if (!refType.equals(objType)) {
            state.getClassHierarchy().addToExpansionBackdoor(refType, objType);
        }
    }

    private void filter(State state, ReferenceSymbolic refToLoad, DecisionAlternative_XYLOAD_GETX_Unresolved dar, Iterator<?> it) 
    throws GuidanceException {
        final MemoryPath refToLoadOrigin = refToLoad.getOrigin();
        if (dar instanceof DecisionAlternative_XYLOAD_GETX_Null && !this.jvm.isNull(refToLoadOrigin)) {
            it.remove();
        } else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Aliases) {
            final DecisionAlternative_XYLOAD_GETX_Aliases dara = (DecisionAlternative_XYLOAD_GETX_Aliases) dar;
            final MemoryPath aliasOrigin = state.getObject(new ReferenceConcrete(dara.getAliasPosition())).getOrigin();
            if (!this.jvm.areAlias(refToLoadOrigin, aliasOrigin)) {
                it.remove();
            }
        } else if (dar instanceof DecisionAlternative_XYLOAD_GETX_Expands) {
            final DecisionAlternative_XYLOAD_GETX_Expands dare = (DecisionAlternative_XYLOAD_GETX_Expands) dar;
            if (this.jvm.isNull(refToLoadOrigin) || alreadySeen(refToLoadOrigin) ||
               !dare.getClassNameOfTargetObject().equals(this.jvm.typeOfObject(refToLoadOrigin))) {
                it.remove();
            } else {
                markAsSeen(refToLoadOrigin);
            }
        }
    }
    
    private void initSeen() throws GuidanceException {
        if (this.jvm.isCurrentMethodNonStatic()) {
            final MemoryPath thisOrigin = MemoryPath.mkLocalVariable("this");
            this.seen.add(thisOrigin);
        }
    }
    
    private boolean alreadySeen(MemoryPath m) {
        return this.seen.contains(m);
    }
    
    private void markAsSeen(MemoryPath m) {
        this.seen.add(m);
    }
    
    private static class JVM {
        private static final String ERROR_BAD_PATH = "Failed accessing through a memory access path.";
        private static final String[] EXCLUDES = {"java.*", "javax.*", "sun.*", "com.sun.*", "org.*"};
        
        private final Calculator calc;
        private final String methodStart;
        private final String methodStop;  
        private final int numberOfHits;
        private final VirtualMachine vm;
        private boolean intoMethodRunnPar = false;
        private int hitCounter = 0;
        private MethodEntryEvent methodEntryEvent;
        private StackFrame rootFrameConcrete;

        public JVM(Calculator calc, RunnerParameters runnerParameters, Signature stopSignature, int numberOfHits) 
        throws GuidanceException {
            this.calc = calc;
            this.methodStart = runnerParameters.getMethodSignature().getName();
            this.methodStop = stopSignature.getName();
            this.numberOfHits = numberOfHits;
            this.vm = createVM(runnerParameters, stopSignature);
            setEventRequests();
            run();
            
            //sets rootFrameConcrete
            try {
                final ThreadReference thread = this.methodEntryEvent.thread();
                final List<StackFrame> frames = thread.frames();
                this.rootFrameConcrete = frames.get(0);
            } catch (IncompatibleThreadStateException | IndexOutOfBoundsException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        private VirtualMachine createVM(RunnerParameters runnerParameters, Signature stopSignature) 
        throws GuidanceException {
            //the variable 'path' is the last one defined in the list 'listClassPath'
            //TODO this block of code seems fragile
            final Iterable<String> classPath = runnerParameters.getClasspath().classPath();
            final ArrayList<String> listClassPath = new ArrayList<>();
            classPath.forEach(listClassPath::add);
            /*final String last = listClassPath.get(listClassPath.size() - 1);
            final String path = last.substring(0, last.length() - 1);*/
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
        
        public boolean isCurrentMethodNonStatic() throws GuidanceException {
            return !this.rootFrameConcrete.location().method().declaringType().isStatic();
        }
        
        public String typeOfObject(MemoryPath origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
            if (object == null) {
                return null;
            }
            return internalClassName(object.referenceType().name());
        }
        
        public boolean isNull(MemoryPath origin) throws GuidanceException {
            final ObjectReference object = (ObjectReference) getJDIValue(origin);
            return (object == null);
        }
        
        public boolean areAlias(MemoryPath first, MemoryPath second) throws GuidanceException {
            final ObjectReference objectFirst = (ObjectReference) getJDIValue(first);
            final ObjectReference objectSecond = (ObjectReference) getJDIValue(second);
            return ((objectFirst == null && objectSecond == null) || objectFirst.equals(objectSecond));
        }

        private Object getValue(MemoryPath origin) throws GuidanceException {
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
                com.sun.jdi.Value value = null;
                com.sun.jdi.ReferenceType t = null;
                com.sun.jdi.ObjectReference o = null;
                for (Access a : origin) {
                    if (a instanceof AccessLocalVariable) {
                        value = getJDIValueLocalVariable(((AccessLocalVariable) a).variableName());
                        if (value == null) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                    } else if (a instanceof AccessStatic) {
                        value = null;
                        t = getJDIObjectStatic(((AccessStatic) a).className());
                        if (t == null) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        o = null;
                    } else if (a instanceof AccessField) {
                        final String fieldName = ((AccessField) a).fieldName();
                        final Field fld = (t == null ? o.referenceType().fieldByName(fieldName) : t.fieldByName(fieldName));
                        if (fld == null) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        try {
                            value = (t == null ? o.getValue(fld) : t.getValue(fld));
                        } catch (IllegalArgumentException e) {
                            throw new GuidanceException(e);
                        }
                        o = null;
                    } else if (a instanceof AccessArrayLength) {
                        if (!(o instanceof ArrayReference)) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
                        value = this.vm.mirrorOf(((ArrayReference) o).length());
                    } else if (a instanceof AccessArrayMember) {
                        if (!(o instanceof ArrayReference)) {
                            throw new GuidanceException(ERROR_BAD_PATH);
                        }
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
                        final Method hashCode = o.referenceType().methodsByName("hashCode", "()I").get(0);
                        value = o.invokeMethod(this.methodEntryEvent.thread(), hashCode, Collections.emptyList(), 0);
                    }
                    if (value instanceof ObjectReference) {
                        t = null;
                        o = (ObjectReference) value;
                    } else if (value != null) {
                        t = null;
                        o = null; 
                    }
                }
                if (value == null) {
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
        throws IncompatibleThreadStateException, AbsentInformationException {
            final com.sun.jdi.Value val;
            if ("this".equals(var)) {
                val = this.rootFrameConcrete.thisObject();
            } else {
                final LocalVariable variable = this.rootFrameConcrete.visibleVariableByName(var); 
                val = (variable == null ? null : this.rootFrameConcrete.getValue(variable));
            }
            return val;
        }
        
        private com.sun.jdi.ReferenceType getJDIObjectStatic(String className) 
        throws IncompatibleThreadStateException, AbsentInformationException {
            com.sun.jdi.ReferenceType o = null;
            final List<ReferenceType> classes = this.vm.classesByName(className);
            if (classes.size() == 1) {
                o = classes.get(0);
            }
            return o;
        }

        public Primitive eval(Primitive toEval) throws GuidanceException {
            final Evaluator evaluator = new Evaluator(this.calc, this);
            try {
                toEval.accept(evaluator);
            } catch (RuntimeException | GuidanceException e) {
                //do not stop them
                throw e;
            } catch (Exception e) {
                //should not happen
                throw new UnexpectedInternalException(e);
            }
            return evaluator.value;
        }

        private static class Evaluator implements PrimitiveVisitor {
            private final Calculator calc;
            private final JVM jvm;
            Primitive value; //the result

            public Evaluator(Calculator calc, JVM jvm) {
                this.calc = calc;
                this.jvm = jvm;
            }

            @Override
            public void visitAny(Any x) {
                this.value = x;
            }

            @Override
            public void visitExpression(Expression e) throws Exception {
                if (e.isUnary()) {
                    e.getOperand().accept(this);
                    final Primitive operandValue = this.value;
                    if (operandValue == null) {
                        this.value = null;
                        return;
                    }
                    this.value = this.calc.applyUnary(e.getOperator(), operandValue);
                } else {
                    e.getFirstOperand().accept(this);
                    final Primitive firstOperandValue = this.value;
                    if (firstOperandValue == null) {
                        this.value = null;
                        return;
                    }
                    e.getSecondOperand().accept(this);
                    final Primitive secondOperandValue = this.value;
                    if (secondOperandValue == null) {
                        this.value = null;
                        return;
                    }
                    this.value = this.calc.applyBinary(firstOperandValue, e.getOperator(), secondOperandValue);
                }
            }

            @Override
            public void visitFunctionApplication(FunctionApplication x) throws Exception {
                final Primitive[] args = x.getArgs();
                final Primitive[] argValues = new Primitive[args.length];
                for (int i = 0; i < args.length; ++i) {
                    args[i].accept(this);
                    argValues[i] = this.value;
                    if (argValues[i] == null) {
                        this.value = null;
                        return;
                    }
                }
                this.value = this.calc.applyFunction(x.getType(), x.getOperator(), argValues);
            }

            @Override
            public void visitPrimitiveSymbolic(PrimitiveSymbolic x) throws GuidanceException {
                final Object fieldValue = this.jvm.getValue(x.getOrigin());
                if (fieldValue instanceof Primitive) {
                    this.value = (Primitive) fieldValue;
                } else {
                    this.value = null;
                }
            }

            @Override
            public void visitSimplex(Simplex x) {
                this.value = x;
            }

            @Override
            public void visitTerm(Term x) {
                this.value = x;
            }

            @Override
            public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                x.getArg().accept(this);
                this.value = this.calc.narrow(x.getType(), this.value);
            }

            @Override
            public void visitWideningConversion(WideningConversion x) throws Exception {
                x.getArg().accept(this);
                this.value = (x.getType() == this.value.getType() ? this.value : this.calc.widen(x.getType(), this.value));
                //note that the concrete this.value could already be widened
                //because of conversion of actual types to computational types
                //through operand stack, see JVMSpec 2.11.1, tab. 2.3
            }
        }
    }
}

