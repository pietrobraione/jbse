package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.failExecution;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_FINALIZER;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_SYSTEM;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.JAVA_THREAD_INIT;
import static jbse.bc.Signatures.JAVA_THREAD_PRIORITY;
import static jbse.bc.Signatures.JAVA_THREADGROUP;
import static jbse.bc.Signatures.JAVA_THREADGROUP_INIT_1;
import static jbse.bc.Signatures.JAVA_THREADGROUP_INIT_2;
import static jbse.bc.Signatures.JAVA_SYSTEM_INITIALIZESYSTEMCLASS;

import static java.lang.Thread.NORM_PRIORITY;

import jbse.algo.exc.MissingTriggerParameterException;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.jvm.exc.InitializationException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link Algorithm} for the first execution step.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_INIT {
    public void exec(ExecutionContext ctx) 
    throws DecisionException, InitializationException, 
    InvalidClassFileFactoryClassException, ClasspathException {
        //TODO do checks and possibly raise exceptions
        State state = ctx.getInitialState();
        if (state == null) {
            //builds the initial state
            state = createInitialState(ctx);
        }

        //adds the initial state to the state tree
        ctx.stateTree.addInitialState(state);
    }

    private State createInitialState(ExecutionContext ctx) 
    throws InvalidClassFileFactoryClassException, InitializationException, 
    DecisionException, ClasspathException {
        final State state = new State(ctx.maxSimpleArrayLength, ctx.maxHeapSize, ctx.classpath, ctx.classFileFactoryClass, ctx.expansionBackdoor, ctx.calc);

        //adds a method frame for the initial method invocation (and possibly triggers)
        try {
            //TODO resolve rootMethodSignature and lookup implementation
            //TODO instead of assuming that {ROOT}:this exists and create the frame, use lazy initialization also on {ROOT}:this, for homogeneity and to explore a wider range of alternatives  
            final ReferenceSymbolic rootThis = state.pushFrameSymbolic(ctx.rootMethodSignature);
            if (rootThis != null) {
                final String className = state.getObject(rootThis).getType();
                final DecisionAlternative_XLOAD_GETX_Expands rootExpansion = ctx.decisionProcedure.getRootDecisionAlternative(rootThis, className);
                ctx.triggerManager.loadTriggerFramesRoot(state, rootExpansion);
            }
        } catch (BadClassFileException | MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new ClasspathException(e);
        } catch (MissingTriggerParameterException | HeapMemoryExhaustedException e) {
            throw new InitializationException(e);
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }

        //creates and initializes the root class
        initializeClass(state, ctx.rootMethodSignature.getClassName(), ctx);

        //the rest of the initialization is taken from hotspot source code from openjdk v8, 
        //see hotspot:src/share/vm/runtime/thread.cpp method Threads::create_vm, 
        //create_initial_thread_group, and create_initial_thread,
        //and jdk:src/share/bin/java.c function JavaMain and invoked function 
        //LoadMainClass
        
        //TODO possibly initialize sun.launcher.LauncherHelper
        
        //creates and initializes classes for handle invocation
        initializeClass(state, JAVA_METHODHANDLENATIVES, ctx);
        initializeClass(state, JAVA_MEMBERNAME, ctx);
        initializeClass(state, JAVA_METHODHANDLE, ctx);
        
        //TODO invoke java.lang.ClassLoader.getSystemClassLoader

        //pushes a frame for java.lang.System.initializeSystemClass
        try {
            state.pushFrame(JAVA_SYSTEM_INITIALIZESYSTEMCLASS, false, 0);
        } catch (NullMethodReceiverException | BadClassFileException | MethodNotFoundException | 
                 MethodCodeNotFoundException | InvalidSlotException | InvalidProgramCounterException | 
                 InvalidTypeException | ThreadStackEmptyException e) {
            //this should not happen now
            failExecution(e);
        }
        
        //creates and initializes more standard classes
        initializeClass(state, JAVA_FINALIZER, ctx);
        initializeClass(state, JAVA_METHOD, ctx);
        initializeClass(state, JAVA_CLASS, ctx);
        initializeClass(state, JAVA_THREAD, ctx);
        initializeClass(state, JAVA_THREADGROUP, ctx);
        initializeClass(state, JAVA_SYSTEM, ctx);
        initializeClass(state, JAVA_STRING, ctx);

        //TODO put all this code in a snippet and invoke it at the right moment
        try {
            //creates the initial thread and thread group
            final ReferenceConcrete systemThreadGroup = state.createInstance(JAVA_THREADGROUP);
            final ReferenceConcrete mainThreadGroup = state.createInstance(JAVA_THREADGROUP);
            final ReferenceConcrete mainThread = state.createInstance(JAVA_THREAD);
            state.getObject(mainThread).setFieldValue(JAVA_THREAD_PRIORITY, ctx.calc.valInt(NORM_PRIORITY)); //necessary to avoid circularity issues
            state.ensureStringLiteral("main");
            final ReferenceConcrete mainString = state.referenceToStringLiteral("main");
            state.pushFrame(JAVA_THREAD_INIT, false, 0, mainThread, mainThreadGroup, mainString);
            state.pushFrame(JAVA_THREADGROUP_INIT_2, false, 0, mainThreadGroup, systemThreadGroup, mainString);
            state.pushFrame(JAVA_THREADGROUP_INIT_1, false, 0, systemThreadGroup);

            //saves a copy of the created state, thread and thread group
            ctx.setMainThreadGroup(mainThreadGroup);
            ctx.setMainThread(mainThread);
            ctx.setInitialState(state);
        } catch (HeapMemoryExhaustedException e) {
            throw new InitializationException(e);
        } catch (BadClassFileException | MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new ClasspathException(e);
        } catch (NullMethodReceiverException | InvalidSlotException | InvalidProgramCounterException |
                 InvalidTypeException | ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }

        return state;
    }
    
    private void initializeClass(State state, String className, ExecutionContext ctx) 
    throws DecisionException, ClasspathException, InitializationException {
        try {
            ensureClassCreatedAndInitialized(state, className, ctx);
        } catch (InterruptException e) {
            //nothing to do: fall through
        } catch (HeapMemoryExhaustedException e) {
            throw new InitializationException(e);
        } catch (BadClassFileException e) {
            throw new ClasspathException(e);
        } catch (InvalidInputException e) {
            //this should not happen at this point
            failExecution(e);
        }
    }
}
