package jbse.apps.run;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.Engine;
import jbse.jvm.Runner;
import jbse.jvm.RunnerBuilder;
import jbse.jvm.RunnerParameters;
import jbse.jvm.exc.CannotBacktrackException;
import jbse.jvm.exc.CannotBuildEngineException;
import jbse.jvm.exc.EngineStuckException;
import jbse.jvm.exc.FailureException;
import jbse.jvm.exc.InitializationException;
import jbse.jvm.exc.NonexistingObservedVariablesException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.CannotRefineException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Value;

public final class InitialHeapChecker {
    private final RunnerParameters runnerParameters;
    private final CheckMethodTable checkMethodTable;
    private Supplier<State> initialStateSupplier = null;
    private Supplier<State> currentStateSupplier = null;
    
    public InitialHeapChecker(RunnerParameters runnerParameters, 
                              Class<? extends Annotation> methodAnnotationClass,
                              Map<String, String> checkMethods) {
        this.runnerParameters = runnerParameters;
        this.checkMethodTable = new CheckMethodTable(methodAnnotationClass, checkMethods);
    }
    
    public void setInitialStateSupplier(Supplier<State> initialStateSupplier) {
        this.initialStateSupplier = initialStateSupplier;
    }
    
    public void setCurrentStateSupplier(Supplier<State> currentStateSupplier) {
        this.currentStateSupplier = currentStateSupplier;
    }   
    
    public boolean checkHeap(boolean scopeExhaustionMeansSuccess) {
        final State sIni = makeInitialState();
        return checkHeap(sIni, scopeExhaustionMeansSuccess);
    }
        
    public boolean checkHeap(State sIni, boolean scopeExhaustionMeansSuccess) {
        //runs the check methods on all the instances in the heap 
        for (long heapPos : sIni.getHeap().keySet()) {
            final Reference objectRef = new ReferenceConcrete(heapPos);
            final Objekt obj = sIni.getObject(objectRef);
            if (obj.isSymbolic()) {
                try {
                    final Signature methodSignature = this.checkMethodTable.findCheckMethod(obj.getType(), sIni);
                    if (methodSignature == null) {
                        //nothing to check
                    } else {
                        final State sRun = sIni.clone();
                        final boolean repOk = 
                            runCheckMethod(sRun, objectRef, methodSignature, this.runnerParameters, scopeExhaustionMeansSuccess);
                        if (!repOk) {
                            return false; 
                        }
                    }
                } catch (DecisionException | 
                InitializationException | InvalidClassFileFactoryClassException | 
                NonexistingObservedVariablesException |  
                CannotBacktrackException | EngineStuckException | CannotManageStateException | 
                ClasspathException | ContradictionException | FailureException | 
                UnexpectedInternalException | CannotBuildEngineException | 
                BadClassFileException | MethodNotFoundException | MethodCodeNotFoundException |
                ThreadStackEmptyException | InvalidProgramCounterException | 
                NullMethodReceiverException | InvalidSlotException exc) {
                    //TODO check and filter exceptions and blame caller when necessary
                    throw new UnexpectedInternalException(exc);
                }
            }
        }
        return true;
    }
    
    public State makeInitialState() {
        //takes a copy of the initial state and refines it
        final State sIni =  this.initialStateSupplier.get();
        sIni.clearStack();
        final State s = this.currentStateSupplier.get();
        try {
            sIni.refine(s);
        } catch (CannotRefineException e) {
            //this should not happen
            throw new UnexpectedInternalException(e);
        }
        return sIni;
    }

    /**
     * This class stores bindings from class names to signatures
     * of methods in the class that must be used to check the 
     * instances of that class.
     * 
     * @author Pietro Braione
     */
    private static class CheckMethodTable {
        /** 
         * A map associating class names with the method in it that must
         * be invoked to check the objects of that class.
         */
        private final HashMap<String, Signature> checkMethods = new HashMap<String, Signature>();
        
        /**
         * The annotation that is attached to the check methods, 
         * possibly {@code null}.
         */
        private final Class<? extends Annotation> methodAnnotationClass;
        
        CheckMethodTable(Class<? extends Annotation> methodAnnotationClass, 
                         Map<String, String> checkMethods) {
            this.methodAnnotationClass = isMethodAnnotationClass(methodAnnotationClass) ? methodAnnotationClass : null;
            for (Map.Entry<String, String> e : checkMethods.entrySet()) {
                final String methodClass = e.getKey();
                final Signature methodSignature =  new Signature(methodClass, "()" + Type.BOOLEAN, e.getValue());
                this.checkMethods.put(methodClass, methodSignature);
            }
        }
        
        private static final boolean isMethodAnnotationClass(Class<? extends Annotation> annotationClass) {
            return (annotationClass.isAnnotation() &&
            Arrays.asList(annotationClass.getAnnotation(Target.class).value()).contains(METHOD));
        }

        /**
         * Finds a check method.
         * 
         * @param className The name of a class.
         * @param s a {@link State}.
         * @return the {@link Signature} associated to {@code className} 
         *         at construction time, or that of an instance (nonstatic), 
         *         concrete (nonabstract) and nonnative method in 
         *         {@code className} that is annotated with 
         *         an instance of {@code methodAnnotationClass}, 
         *         has no parameters and returns a boolean, or 
         *         {@code null} if no such signatures exist.
         * @throws BadClassFileException if {@code className} does not
         *         refer to a valid classfile in the classpath of {@link s}.
         */
        Signature findCheckMethod(String className, State s) 
        throws BadClassFileException {
            Signature methodSignature = this.checkMethods.get(className);
            if (methodSignature == null && this.methodAnnotationClass != null) {
                final ClassFile cf = s.getClassHierarchy().getClassFile(className);
                for (Signature sig : cf.getMethodSignatures()) {
                    try {
                        if (isMethodCheck(cf, sig) && isMethodAnnotated(cf, sig)) {
                            methodSignature = sig;
                            break;
                        }
                    } catch (MethodNotFoundException e) {
                        //this cannot happen
                        throw new UnexpectedInternalException(e);
                    }
                }
                this.checkMethods.put(className, methodSignature);
            }
            return methodSignature;
        }
        
        private static boolean isMethodCheck(ClassFile cf, Signature sig)
        throws MethodNotFoundException {
            final String methodDescriptor = sig.getDescriptor();
            if (!Type.splitReturnValueDescriptor(methodDescriptor).equals("" + Type.BOOLEAN) ||
                Type.splitParametersDescriptors(methodDescriptor).length > 0) {
                return false;
            }
            if (cf.isMethodAbstract(sig) || cf.isMethodNative(sig) || cf.isMethodStatic(sig)) {
                return false;
            }
            return true;
        }

        private boolean isMethodAnnotated(ClassFile cf, Signature sig)
        throws MethodNotFoundException {
            final Object[] annotations = cf.getMethodAvailableAnnotations(sig);
            for (Object annotation : annotations) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) annotation.getClass();
                if (this.methodAnnotationClass.isAssignableFrom(annotationClass)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Runs a check method in a new {@link Engine}.
     * 
     * @param s the initial {@link State} to run the
     *        method. It will be modified.
     * @param r a {@link Reference} to the target
     *        of the method invocation ("this").
     * @param methodSignatureImpl the {@link Signature} of the method to run.
     *        It must be the signature of the looked up method (i.e., it must
     *        have its implementation in the declared class) and it must be
     *        nonnative.
     * @param p the {@link RunnerParameters} object that will be used to build 
     *        the runner; It must be coherent with the parameters of the engine
     *        that created {@code s}. It will be modified.
     * @param scopeExhaustionMeansSuccess {@code true} iff a trace that exhausts
     *        the execution scope must be interpreted as a successful 
     *        execution of the method that returns {@code true}. 
     * @return {@code true} iff there is at least one successful execution
     *         of the method that returns {@code true}. 
     * @throws PleaseDoNativeException
     * @throws CannotBuildEngineException
     * @throws DecisionException
     * @throws InitializationException
     * @throws InvalidClassFileFactoryClassException
     * @throws NonexistingObservedVariablesException
     * @throws CannotBacktrackException
     * @throws CannotManageStateException
     * @throws ClasspathException
     * @throws ContradictionException
     * @throws EngineStuckException
     * @throws FailureException
     * @throws InvalidSlotException 
     * @throws NullMethodReceiverException 
     * @throws InvalidProgramCounterException 
     * @throws ThreadStackEmptyException 
     * @throws MethodNotFoundException 
     * @throws MethodCodeNotFoundException 
     */
    //TODO handle and convert all these exceptions and raise the abstraction level of the operation
    private static boolean 
    runCheckMethod(State s, Reference r, Signature methodSignatureImpl, RunnerParameters p, boolean scopeExhaustionMeansSuccess) 
    throws CannotBuildEngineException, InitializationException, 
    InvalidClassFileFactoryClassException, InvalidProgramCounterException, 
    NullMethodReceiverException, InvalidSlotException, NonexistingObservedVariablesException, 
    DecisionException, CannotBacktrackException, CannotManageStateException, 
    ClasspathException, ContradictionException, EngineStuckException, FailureException, 
    BadClassFileException, MethodNotFoundException, MethodCodeNotFoundException, ThreadStackEmptyException {
        try {
            s.pushFrame(methodSignatureImpl, true, 0, r);
        } catch (MethodNotFoundException | MethodCodeNotFoundException e) {
            return true; //TODO ugly way to cope with nonexistent methods; possibly handle the situation in the constructor of CheckMethodTable
        }
        p.setInitialState(s);
        final RepOkRunnerActions actions = new RepOkRunnerActions(scopeExhaustionMeansSuccess);
        p.setActions(actions);

        //runs
        final RunnerBuilder builder = new RunnerBuilder();
        final Runner runner = builder.build(p);
        runner.run();
        return actions.repOk;
    }

    private static class RepOkRunnerActions extends Runner.Actions {
        final boolean scopeExhaustionMeansSuccess;
        boolean repOk = false;

        public RepOkRunnerActions(boolean scopeExhaustionMeansSuccess) { 
            this.scopeExhaustionMeansSuccess = scopeExhaustionMeansSuccess;
        }

        //TODO log differently!
/*
        @Override
        public boolean atStepPost() {
            final StateFormatterTrace f = new StateFormatterTrace();
            f.formatState(getEngine().getCurrentState());
            System.out.println("==> " + f.emit());
            return super.atStepPost();
        }

        @Override
        public boolean atBacktrackPost(BranchPoint bp) {
            final StateFormatterTrace f = new StateFormatterTrace();
            f.formatState(getEngine().getCurrentState());
            System.out.println("==> " + f.emit());
            return super.atBacktrackPost(bp);
        }
*/
        @Override
        public boolean atTraceEnd() {
            final Value retVal = this.getEngine().getCurrentState().getStuckReturn();
            if (retVal != null) {
                final Simplex retValSimplex = (Simplex) retVal;
                this.repOk = (((Integer) retValSimplex.getActualValue()) == 1);
            }
            return repOk; //interrupts symbolic execution if exists a successful trace that returns true
        }

        @Override
        public boolean atContradictionException(ContradictionException e)
        throws ContradictionException {
            return false; //assumption violated: move to next trace
        }

        @Override
        public boolean atScopeExhaustionHeap() {
            if (scopeExhaustionMeansSuccess) {
                this.repOk = true;
                return true;
            }
            return super.atScopeExhaustionHeap();
            //was: throw new ...whateverException("A conservative repOk must not expand the heap");
        }

        @Override
        public boolean atScopeExhaustionCount() {
            if (scopeExhaustionMeansSuccess) {
                this.repOk = true;
                return true;
            }
            return super.atScopeExhaustionCount();
        }

        @Override
        public boolean atScopeExhaustionDepth() {
            if (scopeExhaustionMeansSuccess) {
                this.repOk = true;
                return true;
            }
            return super.atScopeExhaustionDepth();
        }
    }
}
