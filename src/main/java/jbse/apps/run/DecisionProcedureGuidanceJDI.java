package jbse.apps.run;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.apache.commons.lang3.ArrayUtils; 

import com.sun.jdi.AbsentInformationException;
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
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Runner;
import jbse.jvm.RunnerParameters;
import jbse.mem.State;
import jbse.mem.SwitchTable;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XALOAD;
import jbse.tree.DecisionAlternative_XALOAD_Unresolved;
import jbse.tree.DecisionAlternative_XASTORE;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XLOAD_GETX;
import jbse.tree.DecisionAlternative_XYLOAD_GETX_Unresolved;
import jbse.tree.DecisionAlternative_XNEWARRAY;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.AccessLocalVariable;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureAlgorithms} for guided symbolic execution based on JDI. 
 * It keeps a guiding JVM with which interacts via JDI and filters 
 * all the decisions taken by a component decision procedure it decorates 
 * according to the state reached by the guiding JVM.
 */
public final class DecisionProcedureGuidanceJDI extends DecisionProcedureAlgorithms {
    private boolean ended;
    private String path;   
    private String test;   
    private String methodTarget;  
    private VirtualMachine vm;
    private static MethodEntryEvent e;
    private String methodRunnPar;
    private boolean intoMethodRunnPar=false;
    RunnerParameters runnerParameters;
    Signature stopSignature;
    int numberOfHits;
    int numOfHits;
    
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
        this.runnerParameters=runnerParameters;
        this.stopSignature=stopSignature;
        this.numberOfHits=numberOfHits;
        if (numberOfHits < 1) {
            throw new GuidanceException("Invalid number of hits " + numberOfHits + ".");
        }
        methodRunnPar = runnerParameters.getMethodSignature().getName();
        methodTarget=stopSignature.getName();
        Iterable<String> classPath = runnerParameters.getClasspath().classPath();
        List<String> listClassPath = new ArrayList<>();
        classPath.forEach(listClassPath::add);
        //the variable 'path' is the last one defined in the list 'listClassPath'
        path = listClassPath.get(listClassPath.size()-1);
        path = path.substring(0, path.length()-1);
        test=stopSignature.getClassName();
        JVM VirtMach = new JVM();
		VirtMach.startVm();					
	}
    
    private void run(){
        EventQueue queue = vm.eventQueue();
        boolean testMethodEntryFound = false;
        while (!testMethodEntryFound) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (!testMethodEntryFound && it.hasNext()) {
                	testMethodEntryFound = checkIfMethodEntry(it.nextEvent());
                }
                if (!testMethodEntryFound) 
                	eventSet.resume();
            } catch (InterruptedException exc) {
                //System.out.println(exc.toString());
            } catch (VMDisconnectedException discExc) {
                //System.out.println(discExc.toString());
                break;
            }
        }
    }
    
    private boolean checkIfMethodEntry(Event event){
		if (event instanceof MethodExitEvent && 
				(((MethodExitEvent) event).method().name().equals(methodRunnPar))) {
			intoMethodRunnPar = false;
		}
    	if (event instanceof MethodEntryEvent){
    		if (((MethodEntryEvent) event).method().name().equals(methodRunnPar)) {
    			numOfHits=0;
    			intoMethodRunnPar = true;
    		}
			if (((MethodEntryEvent) event).method().name().equals(methodTarget) && (intoMethodRunnPar)) {
				numOfHits++;
				if (numOfHits==numberOfHits){
					e=(MethodEntryEvent) event;
					return true;
				}
				return false;
			}
		}
		return false;
	}
    
    public static Object getValueParam(String var) throws GuidanceException {
    	Object output = null;
    	try {
    		boolean findParam = false;
    		com.sun.jdi.Value val = null;
    		String delims = "[.]";
    		String[] tokens = var.split(delims);
    		ThreadReference thread = e.thread();
    		List<StackFrame> frames = thread.frames();
    		if (frames.size() > 0) { 
    			StackFrame frame = frames.get(0); 
    			List<LocalVariable> variables = frame.visibleVariables();  
    			if (variables != null) { 
    				for (LocalVariable variable: variables) { 
    					if (variable.name().equals(tokens[0])){
    						findParam = true;
    						val = frame.getValue(variable);
    						if (tokens.length>1){
    							val = innestate(val,tokens);
    						}
    					}
    				} 
    			}
    		}
    		if (!findParam)
    			val = getValueInstanceField(var);
    		switch(val.type().toString()){
    		case "int":
    			IntegerValue intVal = (IntegerValue) val;
    			output = intVal.intValue();
    			return output;
    		case "boolean":
    			BooleanValue boolVal = (BooleanValue)val;
    			output = boolVal.booleanValue();
    			return output;
    		case "char":
    			CharValue charVal = (CharValue)val;
    			output = charVal.charValue();
    			return output;
    		case "byte":
    			ByteValue byteVal = (ByteValue)val;
    			output = byteVal.byteValue();
    			return output;
    		case "double":
    			DoubleValue doubleVal = (DoubleValue)val;
    			output = doubleVal.doubleValue();
    			return output;
    		case "float":
    			FloatValue floatVal = (FloatValue)val;
    			output = floatVal.floatValue();
    			return output;
    		case "long":
    			LongValue longVal = (LongValue)val;
    			output = longVal.longValue();
    			return output;
    		case "short":
    			ShortValue shortVal = (ShortValue)val;
    			output = shortVal.shortValue();
    			return output;
    		default:
    			return output;
    		}
    	} catch (IncompatibleThreadStateException | AbsentInformationException e1) {
    		throw new GuidanceException(e1.toString());
    	}
	}

	public static com.sun.jdi.Value getValueInstanceField(String var) throws IncompatibleThreadStateException {
		com.sun.jdi.Value val=null;
		String delims = "[.]";
		String[] tokens = var.split(delims);
		ThreadReference thread = e.thread();
      	List<StackFrame> frames = thread.frames(); 
        if (frames.size() > 0) {
        	StackFrame frame = frames.get(0);
			ObjectReference objRef = frame.thisObject();
	   		ReferenceType refType = objRef.referenceType();
	    	List<Field> objFields = refType.allFields();
	    	for (int i=0; i<objFields.size(); i++){
	    		Field nextField = objFields.get(i);
	    		if (nextField.name().equals(tokens[0])){
	    			val = (com.sun.jdi.Value) objRef.getValue(nextField);
	    			if (tokens.length>1){
	    				val = innestate(val,tokens);
	    			}
	    		}
	    	}
        }
		return val;
	}
	
	private static com.sun.jdi.Value innestate (com.sun.jdi.Value val, String[] tokens){
		tokens=ArrayUtils.remove(tokens,0);
		ObjectReference obj = (ObjectReference)val;
		ReferenceType refType = obj.referenceType();
    	List<Field> objF = refType.allFields();
    	for (int i=0; i<objF.size(); i++){
    		Field nextField = objF.get(i);
    		if (nextField.name().equals(tokens[0])){
    			val = (com.sun.jdi.Value) obj.getValue(nextField);
	    		if (tokens.length>1){
		    		try{
		    			val = innestate(val, tokens);
		    		}
		    		catch (Exception exc){
		    			System.out.println(exc.toString());
		    		}
	    		}
    		}
    	}
    	return val;
    }

    /**
     * Returns the {@link Signature} of the  
     * guiding engine's current method.
     * 
     * @return a {@link Signature}.
     * @throws ThreadStackEmptyException if the stack is empty.
     */
    public Signature getCurrentMethodSignature() throws ThreadStackEmptyException {
        //TODO
        return null;
    }

    /**
     * Ends guidance decision, and falls back on the 
     * component decision procedure.
     */
    public void endGuidance() {
        this.ended = true;
        stopFastAndImprecise();
    }
    
    private static String nameVar (Primitive var){
		String nameVar = ((AccessLocalVariable) (((PrimitiveSymbolic)var).getOrigin().getAccess())[0]).variableName();
		return nameVar;
    }
    
    private Primitive eval (Primitive condition){
    	final Evaluator evaluator = new Evaluator(this.calc);
    	try {
			condition.accept((PrimitiveVisitor) evaluator);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return evaluator.value;
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
					final Primitive valueInConcreteState = eval(conditionToCheck);
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
    				final Primitive valueInConcreteState = eval(conditionToCheck);
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
                    final Primitive valueInConcreteState = eval(conditionToCheck);
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
                    final Primitive valueInConcreteState = eval(conditionToCheck);
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
                    final Primitive valueInConcreteState = eval(conditionToCheck);
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
                final Object valueInConcreteState = eval(conditionToCheck);
                if (!(boolean)valueInConcreteState) {
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
                final Object valueInConcreteState = eval(conditionToCheck);
                if (!(boolean)valueInConcreteState) {
                    it.remove();
                } else {
                    filter(state, refToLoad, dar, it);
                }
            }
        }
        return retVal;
    }

    private void updateExpansionBackdoor(State state, ReferenceSymbolic refToLoad) throws GuidanceException {
        //TODO
    }

    private void filter(State state, ReferenceSymbolic refToLoad, DecisionAlternative_XYLOAD_GETX_Unresolved dar, Iterator<?> it) 
    throws GuidanceException {
        //TODO
    }
    
    public class JVM {
		
	    private String[] excludes = {"java.*", "javax.*", "sun.*",
		                                 "com.sun.*", "org.*"};

		public void startVm (){
	        vm = launchTarget("-classpath \"" + path + test);
	        generateTrace();
	    }
	    
	    void generateTrace(){
	        setEventRequests();
	        run();
	    }
	    
	    void setEventRequests() {
	        EventRequestManager mgr = vm.eventRequestManager();
	        MethodEntryRequest menr = mgr.createMethodEntryRequest();
	        for (int i=0; i<excludes.length; ++i) {
	            menr.addClassExclusionFilter(excludes[i]);
	        }
	        menr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
	        menr.enable();
	        MethodExitRequest mexr = mgr.createMethodExitRequest();
	        for (int i=0; i<excludes.length; ++i) {
	            mexr.addClassExclusionFilter(excludes[i]);
	        }
	        mexr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
	        mexr.enable();
	    }
	    
	    VirtualMachine launchTarget(String mainArgs) {
	        LaunchingConnector connector = findLaunchingConnector();
	        Map<String, Connector.Argument> arguments =
	           connectorArguments(connector, mainArgs);
	        try {
	            return connector.launch(arguments);
	        } catch (IOException exc) {
	            throw new Error("Unable to launch target VM: " + exc);
	        } catch (IllegalConnectorArgumentsException exc) {
	            throw new Error("Internal error: " + exc);
	        } catch (VMStartException exc) {
	            throw new Error("Target VM failed to initialize: " +
	                            exc.getMessage());
	        }
	    }
		
		LaunchingConnector findLaunchingConnector() {
	        List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
	        for (Connector connector : connectors) {
	            if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
	                return (LaunchingConnector)connector;
	            }
	        }
	        throw new Error("No launching connector");
	    }

	    Map<String, Connector.Argument> connectorArguments(LaunchingConnector connector, String mainArgs) {
	        Map<String, Connector.Argument> arguments = connector.defaultArguments();
	        Connector.Argument mainArg =
	                           (Connector.Argument)arguments.get("main");
	        if (mainArg == null) {
	            throw new Error("Bad launching connector");
	        }
	        mainArg.setValue(mainArgs);
	        return arguments;
	    }
	}
    
    private static class Evaluator implements PrimitiveVisitor {
		private final Calculator calc;
		Primitive value; //the result
		
		public Evaluator(Calculator calc) {
			this.calc = calc;
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
    		String nameVar = nameVar(x);
			Object fieldValue = getValueParam(nameVar);
			try {
				final Simplex v = new Simplex('I',calc,fieldValue);
				if (v instanceof Primitive) {
					this.value = (Primitive) v;
				} else {
					this.value = null;
				}
			} catch (InvalidOperandException | InvalidTypeException e) {
				e.printStackTrace();
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
