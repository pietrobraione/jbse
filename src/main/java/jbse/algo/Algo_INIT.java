package jbse.algo;

import static jbse.algo.Util.ensureClassCreatedAndInitialized;
import static jbse.algo.Util.ensureStringLiteral;
import static jbse.bc.Signatures.JAVA_CLASS;
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
import jbse.bc.exc.ClassFileIllFormedException;
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
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_XLOAD_GETX_Expands;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;

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
        final State state = new State(ctx.classpath, ctx.classFileFactoryClass, ctx.expansionBackdoor, ctx.calc);

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
        } catch (BadClassFileException | MethodNotFoundException | 
                 MethodCodeNotFoundException | MissingTriggerParameterException e) {
            throw new InitializationException(e);
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
        
        //the rest of the initialization is taken from hotspot source code from openjdk v8, src/share/vm/runtime/thread.cpp, 
        //functions create_vm, create_initial_thread_group, create_initial_thread 

        //pushes a frame for java.lang.System.initializeSystemClass
        try {
            state.pushFrame(JAVA_SYSTEM_INITIALIZESYSTEMCLASS, false, 0);
        } catch (NullMethodReceiverException | BadClassFileException | MethodNotFoundException | 
                 MethodCodeNotFoundException | InvalidSlotException | InvalidProgramCounterException | 
                 ThreadStackEmptyException e) {
            //this should not happen now
            throw new UnexpectedInternalException(e);
        }
        
        //creates and initializes a bunch of classes, including the root class
        try {
            try {
                ensureClassCreatedAndInitialized(state, ctx.rootMethodSignature.getClassName(), ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
            try {
                ensureClassCreatedAndInitialized(state, JAVA_CLASS, ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
            try {
                ensureClassCreatedAndInitialized(state, JAVA_THREAD, ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
            try {
                ensureClassCreatedAndInitialized(state, JAVA_THREADGROUP, ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
            try {
                ensureClassCreatedAndInitialized(state, JAVA_SYSTEM, ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
            try {
                ensureClassCreatedAndInitialized(state, JAVA_STRING, ctx);
            } catch (InterruptException e) {
                //nothing to do: fall through
            }
        } catch (InvalidInputException | BadClassFileException e) {
            //this should not happen at this point
            throw new UnexpectedInternalException(e);
        }

        //creates the initial thread and thread group
        final ReferenceConcrete systemThreadGroup = state.createInstance(JAVA_THREADGROUP);
        final ReferenceConcrete mainThreadGroup = state.createInstance(JAVA_THREADGROUP);
        final ReferenceConcrete mainThread = state.createInstance(JAVA_THREAD);
        state.getObject(mainThread).setFieldValue(JAVA_THREAD_PRIORITY, ctx.calc.valInt(NORM_PRIORITY)); //necessary to avoid circularity issues
        try {
            ensureStringLiteral(state, ctx, "main");
        } catch (ClassFileIllFormedException e) {
            throw new ClasspathException(e);
        } catch (InterruptException e) {
            //should not be raised now, anyways fall through is ok
        }
        final ReferenceConcrete mainString = state.referenceToStringLiteral("main");
        try {
            state.pushFrame(JAVA_THREAD_INIT, false, 0, mainThread, mainThreadGroup, mainString);
            state.pushFrame(JAVA_THREADGROUP_INIT_2, false, 0, mainThreadGroup, systemThreadGroup, mainString);
            state.pushFrame(JAVA_THREADGROUP_INIT_1, false, 0, systemThreadGroup);
        } catch (BadClassFileException | MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new ClasspathException(e);
        } catch (NullMethodReceiverException | InvalidSlotException | InvalidProgramCounterException |
                 ThreadStackEmptyException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }

        //saves a copy of the created state, thread and thread group
        ctx.setMainThreadGroup(mainThreadGroup);
        ctx.setMainThread(mainThread);
        ctx.setInitialState(state);

        return state;
    }
}
