package jbse.algo;

import static jbse.algo.Util.ensureClassInitialized;
import static jbse.algo.Util.failExecution;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.ClassLoaders.CLASSLOADER_EXT;
import static jbse.bc.Signatures.ARITHMETIC_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;
import static jbse.bc.Signatures.ERROR;
import static jbse.bc.Signatures.EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_MONITOR_STATE_EXCEPTION;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT;
import static jbse.bc.Signatures.JAVA_ANNOTATEDELEMENT;
import static jbse.bc.Signatures.JAVA_CHARSEQUENCE;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_GETSYSTEMCLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_COMPARABLE;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_EXECUTABLE;
import static jbse.bc.Signatures.JAVA_FINALIZER;
import static jbse.bc.Signatures.JAVA_FINALREFERENCE;
import static jbse.bc.Signatures.JAVA_GENERICDECLARATION;
import static jbse.bc.Signatures.JAVA_MEMBER;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_PACKAGE;
import static jbse.bc.Signatures.JAVA_REFERENCE;
import static jbse.bc.Signatures.JAVA_RUNNABLE;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.JAVA_STACKTRACEELEMENT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRINGCODING;
import static jbse.bc.Signatures.JAVA_SYSTEM;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.JAVA_THREAD_INIT;
import static jbse.bc.Signatures.JAVA_THREAD_PRIORITY;
import static jbse.bc.Signatures.JAVA_THREAD_UNCAUGHTEXCEPTIONHANDLER;
import static jbse.bc.Signatures.JAVA_THREADGROUP;
import static jbse.bc.Signatures.JAVA_THREADGROUP_INIT_1;
import static jbse.bc.Signatures.JAVA_THREADGROUP_INIT_2;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.bc.Signatures.JAVA_TYPE;
import static jbse.bc.Signatures.JAVA_SYSTEM_INITIALIZESYSTEMCLASS;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.LINKAGE_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.RUNTIME_EXCEPTION;
import static jbse.bc.Signatures.STACK_OVERFLOW_ERROR;
import static jbse.bc.Signatures.SUN_EXTENSIONDEPENDENCY;
import static jbse.bc.Signatures.VERIFY_ERROR;
import static jbse.bc.Signatures.VIRTUAL_MACHINE_ERROR;
import static jbse.bc.Signatures.noclass_SETSTANDARDCLASSLOADERSREADY;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isReference;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.TYPEEND;

import java.util.ArrayList;
import java.util.HashSet;

import static java.lang.Thread.NORM_PRIORITY;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.jvm.exc.InitializationException;
import jbse.mem.State;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.ReferenceConcrete;
import jbse.val.exc.InvalidTypeException;

/**
 * Action (does not implement {@link Action}) for the preparation 
 * of the starting state.
 * 
 * @author Pietro Braione
 *
 */
public final class Action_START {
    /**
     * Inhibit set for class initialization.
     */
    private final HashSet<String> doNotInitialize = new HashSet<>();
    
    /**
     * List of the classes in the root method signature.
     */
    private final ArrayList<String> rootMethodClasses = new ArrayList<>();

    /**
     * Constructor.
     */
    public Action_START() { }

    public void exec(ExecutionContext ctx) 
    throws DecisionException, InitializationException, 
    InvalidClassFileFactoryClassException, 
    ClasspathException, ContradictionException, InvalidInputException {
        //TODO do checks and possibly raise exceptions

        //gets or creates the starting state
        State state = ctx.getStateStart();
        boolean userProvidedStartState = true;
        if (state == null) {
            state = createStateStart(ctx);
            userProvidedStartState = false;
        }
        state.setStutters(false);

        //adds the state to the state tree
        ctx.stateTree.addStateStart(state, userProvidedStartState);
    }

    private State createStateStart(ExecutionContext ctx) 
    throws InvalidClassFileFactoryClassException, InitializationException, 
    DecisionException, ClasspathException, ContradictionException {
        final State state = ctx.createStateVirginPreInitial();
        
        //some initial bookkeeping
        enumerateExplicitlyInitializedClasses();
        enumerateRootMethodClasses(ctx);

        //(loads and) creates the essential classes that
        //will be initialized afterwards
        loadCreateEssentialClasses(state, ctx);

        //now starts pushing frames (in inverse order of execution)
        //first the root method classes
        initializeRootMethodClasses(state, ctx);

        //this part of the initialization mirrors stuff in sun.launcher.LauncherHelper
        //pushes frames to initialize classes for dynamic classloading
        initializeClass(state, JAVA_PACKAGE, ctx);
        initializeClass(state, JAVA_STRINGCODING, ctx);
        initializeClass(state, SUN_EXTENSIONDEPENDENCY, ctx);
        //TODO possibly more initialization assumption from sun.launcher.LauncherHelper

        //the rest of the initialization mirrors hotspot source code from openjdk v8, 
        //see hotspot:src/share/vm/runtime/thread.cpp method Threads::create_vm, 
        //create_initial_thread_group, and create_initial_thread,
        //and jdk:src/share/bin/java.c function JavaMain and invoked function 
        //LoadMainClass

        //pushes frames to initialize classes for handle invocation
        initializeClass(state, JAVA_METHODHANDLENATIVES, ctx);
        initializeClass(state, JAVA_MEMBERNAME, ctx);
        initializeClass(state, JAVA_METHODHANDLE, ctx);

        //pushes a frame for java.lang.ClassLoader.getSystemClassLoader
        invokeGetSystemClassLoader(state, ctx);

        //pushes frames to initialize some error/exception classes
        //(actually they do not have any static initializer, but
        //they might in the future)
        //TODO these currently statically initialize java.lang.Throwable, but not in Hotspot! This contradicts the fact that to statically initialize a class its superclass must be statically initialized first 
        initializeClass(state, ILLEGAL_ARGUMENT_EXCEPTION, ctx);
        initializeClass(state, ILLEGAL_MONITOR_STATE_EXCEPTION, ctx);
        initializeClass(state, STACK_OVERFLOW_ERROR, ctx);
        initializeClass(state, ARITHMETIC_EXCEPTION, ctx);
        initializeClass(state, ARRAY_STORE_EXCEPTION, ctx);
        initializeClass(state, CLASS_CAST_EXCEPTION, ctx);
        initializeClass(state, NULL_POINTER_EXCEPTION, ctx);
        initializeClass(state, OUT_OF_MEMORY_ERROR, ctx);

        //pushes a frame for java.lang.System.initializeSystemClass
        invokeInitializeSystemClass(state, ctx);

        //pushes a frame to initialize jbse.base.Base
        initializeClass(state, JBSE_BASE, ctx);

        //pushes frames to initialize more standard classes
        initializeClass(state, JAVA_FINALIZER, ctx);
        initializeClass(state, JAVA_METHOD, ctx);
        initializeClass(state, JAVA_CLASSLOADER, ctx);
        initializeClass(state, JAVA_CLASS, ctx);

        //creates the initial thread and thread group
        //and pushes frames to initialize them 
        createInitialThreadAndThreadGroups(state, ctx);

        //pushes frames to initialize more standard classes
        initializeClass(state, JAVA_THREAD, ctx);
        initializeClass(state, JAVA_THREADGROUP, ctx);
        initializeClass(state, JAVA_SYSTEM, ctx);
        initializeClass(state, JAVA_STRING, ctx);

        //done
        return state;
    }

    /**
     * Sets this.doNotInitialize with all the system classes
     * that will be initialized by explicitly pushing the frame
     * of their static initialized on the stack, and that therefore
     * must not be initialized by cascading invocations of superclass
     * initializers.
     */
    private void enumerateExplicitlyInitializedClasses() {
        this.doNotInitialize.add(JAVA_PACKAGE);
        this.doNotInitialize.add(JAVA_STRINGCODING);
        this.doNotInitialize.add(SUN_EXTENSIONDEPENDENCY);
        this.doNotInitialize.add(JAVA_METHODHANDLENATIVES);
        this.doNotInitialize.add(JAVA_MEMBERNAME);
        this.doNotInitialize.add(JAVA_METHODHANDLE);
        this.doNotInitialize.add(ILLEGAL_ARGUMENT_EXCEPTION);
        this.doNotInitialize.add(ILLEGAL_MONITOR_STATE_EXCEPTION);
        this.doNotInitialize.add(STACK_OVERFLOW_ERROR);
        this.doNotInitialize.add(ARITHMETIC_EXCEPTION);
        this.doNotInitialize.add(ARRAY_STORE_EXCEPTION);
        this.doNotInitialize.add(CLASS_CAST_EXCEPTION);
        this.doNotInitialize.add(NULL_POINTER_EXCEPTION);
        this.doNotInitialize.add(OUT_OF_MEMORY_ERROR);
        this.doNotInitialize.add(JAVA_FINALIZER);
        this.doNotInitialize.add(JAVA_METHOD);
        this.doNotInitialize.add(JAVA_CLASS);
        this.doNotInitialize.add(JAVA_THREAD);
        this.doNotInitialize.add(JAVA_THREADGROUP);
        this.doNotInitialize.add(JAVA_SYSTEM);
        this.doNotInitialize.add(JAVA_STRING);
    }
    
    /**
     * Sets this.rootMethodClasses with all the classes in the 
     * signature of the root method, excluded the one in the 
     * return value type.
     */
    private void enumerateRootMethodClasses(ExecutionContext ctx) {
        this.rootMethodClasses.add(ctx.rootMethodSignature.getClassName());
        final String[] parametersTypes = splitParametersDescriptors(ctx.rootMethodSignature.getDescriptor());
        for (String parameterType : parametersTypes) {
            if (isArray(parameterType) || isReference(parameterType)) {
                this.rootMethodClasses.add(className(parameterType));
            }
        }
    }

    private void loadCreateEssentialClasses(State state, ExecutionContext ctx) throws ClasspathException {
        try {
            final ClassHierarchy classHierarchy = state.getClassHierarchy();

            //loads standard library classes
            classHierarchy.loadCreateClass(JAVA_OBJECT);
            classHierarchy.loadCreateClass(JAVA_CLONEABLE);
            classHierarchy.loadCreateClass(JAVA_SERIALIZABLE);
            classHierarchy.loadCreateClass("" + ARRAYOF + CHAR);
            classHierarchy.loadCreateClass(JAVA_STACKTRACEELEMENT);
            classHierarchy.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_STACKTRACEELEMENT + TYPEEND);
            classHierarchy.loadCreateClass(JAVA_COMPARABLE);
            classHierarchy.loadCreateClass(JAVA_ENUM);
            classHierarchy.loadCreateClass(JAVA_CHARSEQUENCE);
            classHierarchy.loadCreateClass(JAVA_STRING);
            classHierarchy.loadCreateClass(JAVA_SYSTEM);
            classHierarchy.loadCreateClass(JAVA_RUNNABLE);
            classHierarchy.loadCreateClass(JAVA_THREAD);
            classHierarchy.loadCreateClass(JAVA_THREAD_UNCAUGHTEXCEPTIONHANDLER);
            classHierarchy.loadCreateClass(JAVA_THREADGROUP);
            classHierarchy.loadCreateClass(JAVA_ANNOTATEDELEMENT);
            classHierarchy.loadCreateClass(JAVA_GENERICDECLARATION);
            classHierarchy.loadCreateClass(JAVA_TYPE);
            classHierarchy.loadCreateClass(JAVA_CLASS);
            classHierarchy.loadCreateClass(JAVA_CLASSLOADER);
            classHierarchy.loadCreateClass(JAVA_ACCESSIBLEOBJECT);
            classHierarchy.loadCreateClass(JAVA_EXECUTABLE);
            classHierarchy.loadCreateClass(JAVA_METHOD);
            classHierarchy.loadCreateClass(JAVA_REFERENCE);
            classHierarchy.loadCreateClass(JAVA_FINALREFERENCE);
            classHierarchy.loadCreateClass(JAVA_FINALIZER);
            classHierarchy.loadCreateClass(JAVA_THROWABLE);
            classHierarchy.loadCreateClass(ERROR);
            classHierarchy.loadCreateClass(VIRTUAL_MACHINE_ERROR);
            classHierarchy.loadCreateClass(OUT_OF_MEMORY_ERROR);
            classHierarchy.loadCreateClass(LINKAGE_ERROR);
            classHierarchy.loadCreateClass(VERIFY_ERROR);
            classHierarchy.loadCreateClass(EXCEPTION);
            classHierarchy.loadCreateClass(RUNTIME_EXCEPTION);
            classHierarchy.loadCreateClass(NULL_POINTER_EXCEPTION);
            classHierarchy.loadCreateClass(CLASS_CAST_EXCEPTION);
            classHierarchy.loadCreateClass(ARRAY_STORE_EXCEPTION);
            classHierarchy.loadCreateClass(ARITHMETIC_EXCEPTION);
            classHierarchy.loadCreateClass(STACK_OVERFLOW_ERROR);
            classHierarchy.loadCreateClass(ILLEGAL_MONITOR_STATE_EXCEPTION);
            classHierarchy.loadCreateClass(ILLEGAL_ARGUMENT_EXCEPTION);
            classHierarchy.loadCreateClass(JAVA_METHODHANDLE);
            classHierarchy.loadCreateClass(JAVA_MEMBER);
            classHierarchy.loadCreateClass(JAVA_MEMBERNAME);
            classHierarchy.loadCreateClass(JAVA_METHODHANDLENATIVES);
            classHierarchy.loadCreateClass(SUN_EXTENSIONDEPENDENCY);
            classHierarchy.loadCreateClass(JAVA_STRINGCODING);
            classHierarchy.loadCreateClass(JAVA_PACKAGE);

            //loads application classes
            classHierarchy.loadCreateClass(CLASSLOADER_APP, JBSE_BASE, true);
            for (String className : this.rootMethodClasses) {
                classHierarchy.loadCreateClass(CLASSLOADER_APP, className, true);
            }
        } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                 WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException e) {
            throw new ClasspathException(e);
        } catch (RenameUnsupportedException | InvalidInputException | PleaseLoadClassException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    private void initializeRootMethodClasses(State state, ExecutionContext ctx) 
    throws DecisionException, ClasspathException, InitializationException, ContradictionException {
        try {
            for (String className : this.rootMethodClasses) {
                try {
                    ClassFile classFile = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_APP, className);
                    if (classFile == null) {
                        classFile = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_EXT, className);
                    }
                    if (classFile == null) {
                        classFile = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, className);
                    }
                    if (classFile == null) {
                        throw new ClasspathException("Root method invocation class " + className + " not found in the classpath.");
                    }
                    ensureClassInitialized(state, ctx, this.doNotInitialize, classFile);
                } catch (InterruptException e) {
                    //nothing to do: fall through
                }
            }
        } catch (HeapMemoryExhaustedException | RenameUnsupportedException e) {
            throw new InitializationException(e);
        } catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException |
                 BadClassFileVersionException | WrongClassNameException | IncompatibleClassFileException | 
                 ClassFileNotAccessibleException e) {
            //this should not happen at this point
            failExecution(e);
        }
    }

    private void initializeClass(State state, String className, ExecutionContext ctx) 
    throws DecisionException, ClasspathException, InitializationException, ContradictionException {
        try {
            this.doNotInitialize.remove(className);
            final int classLoader = (JBSE_BASE.equals(className) ? CLASSLOADER_APP : CLASSLOADER_BOOT);
            final ClassFile classFile = state.getClassHierarchy().getClassFileClassArray(classLoader, className); 
            ensureClassInitialized(state, ctx, this.doNotInitialize, classFile);
        } catch (InterruptException e) {
            //nothing to do: fall through
        } catch (HeapMemoryExhaustedException | RenameUnsupportedException e) {
            throw new InitializationException(e);
        } catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException | 
                 BadClassFileVersionException | WrongClassNameException | IncompatibleClassFileException | 
                 ClassFileNotAccessibleException e) {
            //this should not happen at this point
            failExecution(e);
        }
    }

    private void invokeGetSystemClassLoader(State state, ExecutionContext ctx) {
        try {
        	final ClassFile cf_JAVA_OBJECT = state.getClassHierarchy().loadCreateClass(JAVA_OBJECT);
            final Snippet snippet = state.snippetFactoryNoWrap()
            .op_invokestatic(JAVA_CLASSLOADER_GETSYSTEMCLASSLOADER)
            .op_pop() //discards the return value
            .op_invokestatic(noclass_SETSTANDARDCLASSLOADERSREADY)
            .op_return()
            .mk();
            state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_OBJECT);
        } catch (ThreadStackEmptyException | InvalidProgramCounterException |  
        		InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException | 
        		ClassFileNotAccessibleException | IncompatibleClassFileException | BadClassFileVersionException | 
        		RenameUnsupportedException | WrongClassNameException e) {
            //this should not happen now
            failExecution(e);
        }
    }

    private void invokeInitializeSystemClass(State state, ExecutionContext ctx) {
        try {
            final ClassFile cf_JAVA_SYSTEM = state.getClassHierarchy().loadCreateClass(JAVA_SYSTEM); 
            state.pushFrame(ctx.getCalculator(), cf_JAVA_SYSTEM, JAVA_SYSTEM_INITIALIZESYSTEMCLASS, false, 0);
        } catch (NullMethodReceiverException | MethodNotFoundException | MethodCodeNotFoundException | 
                 InvalidSlotException | InvalidProgramCounterException | InvalidTypeException | 
                 ThreadStackEmptyException | ClassFileNotFoundException | BadClassFileVersionException |
                 RenameUnsupportedException | WrongClassNameException | IncompatibleClassFileException | 
                 ClassFileIllFormedException | ClassFileNotAccessibleException | InvalidInputException e) {
            //this should not happen now
            failExecution(e);
        }
    }

    private void createInitialThreadAndThreadGroups(State state, ExecutionContext ctx) 
    throws InitializationException, ClasspathException {
        try {
            //creates the initial thread and thread group
            final ClassFile cf_JAVA_THREAD = state.getClassHierarchy().loadCreateClass(JAVA_THREAD);
            if (cf_JAVA_THREAD == null) {
                throw new UnexpectedInternalException("Could not get the classfile for java.lang.Thread.");
            }
            final ClassFile cf_JAVA_THREADGROUP = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_THREADGROUP); 
            if (cf_JAVA_THREADGROUP == null) {
                throw new UnexpectedInternalException("Could not get the classfile for java.lang.ThreadGroup.");
            }

            final ReferenceConcrete systemThreadGroup = state.createInstance(ctx.getCalculator(), cf_JAVA_THREADGROUP);
            final ReferenceConcrete mainThreadGroup = state.createInstance(ctx.getCalculator(), cf_JAVA_THREADGROUP);
            final ReferenceConcrete mainThread = state.createInstance(ctx.getCalculator(), cf_JAVA_THREAD);
            state.getObject(mainThread).setFieldValue(JAVA_THREAD_PRIORITY, ctx.getCalculator().valInt(NORM_PRIORITY)); //necessary to avoid circularity issues
            state.ensureStringLiteral(ctx.getCalculator(), "main");
            final ReferenceConcrete mainString = state.referenceToStringLiteral("main");
            state.pushFrame(ctx.getCalculator(), cf_JAVA_THREAD, JAVA_THREAD_INIT, false, 0, mainThread, mainThreadGroup, mainString);
            state.pushFrame(ctx.getCalculator(), cf_JAVA_THREADGROUP, JAVA_THREADGROUP_INIT_2, false, 0, mainThreadGroup, systemThreadGroup, mainString);
            state.pushFrame(ctx.getCalculator(), cf_JAVA_THREADGROUP, JAVA_THREADGROUP_INIT_1, false, 0, systemThreadGroup);

            //saves the created thread and thread group in the state
            state.setMainThreadGroup(mainThreadGroup);
            state.setMainThread(mainThread);
        } catch (HeapMemoryExhaustedException e) {
            throw new InitializationException(e);
        } catch (MethodNotFoundException | MethodCodeNotFoundException e) {
            throw new ClasspathException(e);
        } catch (ClassFileNotFoundException | BadClassFileVersionException | RenameUnsupportedException | 
        		 WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException | 
        		 ClassFileIllFormedException | NullMethodReceiverException | InvalidSlotException | 
        		 InvalidProgramCounterException | InvalidInputException | InvalidTypeException | 
        		 ThreadStackEmptyException e) {
            //this should never happen
            failExecution(e);
        }
    }
}
