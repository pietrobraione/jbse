package jbse.algo;

import static jbse.algo.UtilControlFlow.continueWith;
import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_LOADCLASS;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_INVOKEBASIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOINTERFACE;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSPECIAL;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOSTATIC;
import static jbse.bc.Signatures.JAVA_METHODHANDLE_LINKTOVIRTUAL;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.noclass_REGISTERLOADEDCLASS;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.binaryClassName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jbse.algo.exc.BaseUnsupportedException;
import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.Snippet;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodAbstractException;
import jbse.bc.exc.MethodNotAccessibleException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.Type;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlgorithms.ArrayAccessInfo;
import jbse.dec.exc.DecisionException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidNumberOfOperandsException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public final class Util {
    /**
     * Finds a classfile corresponding to a class name from the loaded
     * classfiles with an initiating loader suitable to reference resolution.
     * To be used to find the classfile of a resolved reference from its
     * class name.
     * 
     * @param state a {@link State}.
     * @param className a {@link String}.
     * @return the {@link ClassFile} with name {@code className}, if one 
     *         was loaded in {@code state} with either the boot, or the 
     *         extension, or the app classloader as intiating loader. 
     */
    public static ClassFile findClassFile(State state, String className) {
        ClassFile retVal = null;
        for (int classLoader = CLASSLOADER_APP; classLoader >= CLASSLOADER_BOOT; --classLoader) {
            retVal = state.getClassHierarchy().getClassFileClassArray(classLoader, className);
            if (retVal != null) {
                return retVal;
            }
        }
        throw new UnexpectedInternalException("Unable to find the classfile for a reference resolution.");
    }
    
    /**
     * Performs lookup of a method implementation (bytecode or native).
     * See JVMS v8, invokeinterface, invokespecial, invokestatic and invokevirtual
     * bytecodes specification.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param resolutionClass the {@link ClassFile} of the resolved method. It must not be {@code null}.
     * @param methodSignature the {@link Signature} of the method
     *        whose implementation must be looked up. The class name encapsulated 
     *        in it will be ignored and {@code resolutionClass} will be considered
     *        instead. It must not be {@code null}.
     * @param isInterface {@code true} iff the method is declared interface.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param isStatic {@code true} iff the method is declared static.
     * @param receiverClass a {@link ClassFile}, the class of the receiver
     *        of the method invocation. It can be {@code null} when 
     *        {@code isStatic == true || isSpecial == true}.
     * @return the {@link ClassFile} of the class which contains the method implementation.
     * @throws InvalidInputException if {@code state == null || resolutionClass == null || 
     *         methodSignature == null || (!isStatic && !isSpecial && receiverClass == null)}.
     * @throws FrozenStateException if {@code state} is frozen.
     * @throws MethodNotFoundException if lookup fails and {@link java.lang.NoSuchMethodError} should be thrown.
     * @throws MethodNotAccessibleException  if lookup fails and {@link java.lang.IllegalAccessError} should be thrown.
     * @throws MethodAbstractException if lookup fails and {@link java.lang.AbstractMethodError} should be thrown.
     * @throws IncompatibleClassFileException if lookup fails and {@link java.lang.IncompatibleClassChangeError} should be thrown.
     * @throws ThreadStackEmptyException if {@code state} has an empty stack (i.e., no
     *         current method).
     */
    public static ClassFile lookupMethodImpl(State state, ClassFile resolutionClass, Signature methodSignature, boolean isInterface, boolean isSpecial, boolean isStatic, ClassFile receiverClass) 
    throws InvalidInputException, FrozenStateException, MethodNotFoundException, MethodNotAccessibleException, MethodAbstractException, IncompatibleClassFileException, ThreadStackEmptyException {
    	if (state == null || resolutionClass == null || methodSignature == null || (!isStatic && !isSpecial && receiverClass == null)) {
    	    throw new InvalidInputException("Invoked " + Util.class.getName() + ".lookupMethodImpl with a null parameter.");
    	}
        final ClassFile retVal;
        final ClassHierarchy hier = state.getClassHierarchy();
        if (isInterface) { 
            retVal = hier.lookupMethodImplInterface(receiverClass, resolutionClass, methodSignature);
        } else if (isSpecial) {
            final ClassFile currentClass = state.getCurrentClass();
            retVal = hier.lookupMethodImplSpecial(currentClass, resolutionClass, methodSignature);
        } else if (isStatic) {
            retVal = hier.lookupMethodImplStatic(resolutionClass, methodSignature);
        } else { //invokevirtual
            retVal = hier.lookupMethodImplVirtual(receiverClass, resolutionClass, methodSignature);
        }
        return retVal;
    }

    /**
     * Determines whether a base-level or a meta-level overriding implementation 
     * for a method exists. In the base-level case returns the signature of the
     * overriding method. In the meta-level case interrupts the execution of the
     * current algorithm and triggers continuation with the one implementing
     * the method.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param ctx an {@link ExecutionContext}. It must not be {@code null}.
     * @param implementationClass the {@link ClassFile} where the method implementation is, 
     *        or {@code null} if the method is classless.
     * @param methodSignatureImplementation the {@link Signature} of the implementation.
     *        It must not be {@code null}.
     * @param isInterface {@code true} iff the method is declared interface.
     * @param isSpecial {@code true} iff the method is declared special.
     * @param isStatic {@code true} iff the method is declared static.
     * @param isNative {@code true} iff the method is declared native.
     * @param doPop {@code true} iff in the case of a meta-level overriding
     *        implementation the topmost item on the operand stack must be 
     *        popped before transferring control (necessary to the implementation 
     *        of {@code MethodHandle.linkToXxxx} methods).
     * @return {@code null} if no overriding implementation exists, otherwise the
     *         {@link Signature} of a base-level overriding method.
     * @throws InvalidInputException if {@code state == null || ctx == null || 
     *         methodImplSignature == null}.
     * @throws MetaUnsupportedException if it is unable to find the specified {@link Algorithm}, 
     *         to load it, or to instantiate it for any reason (misses from the meta-level classpath, 
     *         has insufficient visibility, does not implement {@link Algorithm}...).
     * @throws ThreadStackEmptyException if {@code state}'s stack is empty.
     * @throws ClasspathException if a standard class is missing from the classpath.
     * @throws BaseUnsupportedException if the base-level overriding fails for some reason 
     *         (missing class, wrong classfile...).
     * @throws InterruptException if the execution fails or a meta-level implementation is found, 
     *         in which case the current {@link Algorithm} is interrupted with the 
     *         {@link Algorithm} for the overriding implementation as continuation. 
     * @throws InvalidNumberOfOperandsException if there are no operands on {@code state}'s operand stack
     *         and {@code doPop == true}.
     */
    public static Signature lookupMethodImplOverriding(State state, ExecutionContext ctx, ClassFile implementationClass, Signature methodSignatureImplementation, boolean isInterface, boolean isSpecial, boolean isStatic, boolean isNative, boolean doPop) 
    throws InvalidInputException, MetaUnsupportedException, InterruptException, ClasspathException, ThreadStackEmptyException, BaseUnsupportedException, InvalidNumberOfOperandsException {
        if (state == null || ctx == null || methodSignatureImplementation == null) {
            throw new InvalidInputException("Invoked " + Util.class.getName() + ".lookupMethodImplOverriding with a null parameter.");
        }
        if (ctx.isMethodBaseLevelOverridden(methodSignatureImplementation)) {
            try {
                final ClassHierarchy hier = state.getClassHierarchy();
                final Signature methodSignatureOverridingFromCtx = ctx.getBaseOverride(methodSignatureImplementation);
                final ClassFile classFileMethodOverridingFromCtx = hier.loadCreateClass(CLASSLOADER_APP, methodSignatureOverridingFromCtx.getClassName(), state.bypassStandardLoading());
                final ClassFile classFileMethodOverridingResolved = hier.resolveMethod(classFileMethodOverridingFromCtx, methodSignatureOverridingFromCtx, classFileMethodOverridingFromCtx.isInterface(), state.bypassStandardLoading()); //TODO is the isInterface parameter ok? And the accessor parameter?
                return new Signature(classFileMethodOverridingResolved.getClassName(), methodSignatureOverridingFromCtx.getDescriptor(), methodSignatureOverridingFromCtx.getName());
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException | 
                     RenameUnsupportedException | WrongClassNameException | ClassFileNotAccessibleException | 
                     IncompatibleClassFileException | MethodNotFoundException | MethodNotAccessibleException e) {
                throw new BaseUnsupportedException(e);
            }        
        } else {
            try {
                if (ctx.dispatcherMeta.isMeta(implementationClass, methodSignatureImplementation)) {
                    final Algo_INVOKEMETA<?, ?, ?, ?> algo = ctx.dispatcherMeta.select(methodSignatureImplementation);
                    algo.setFeatures(isInterface, isSpecial, isStatic, isNative, methodSignatureImplementation);
                    if (doPop) {
                    	state.popOperand();
                    }
                    continueWith(algo);
                }
            } catch (MethodNotFoundException e) {
                //this should never happen after resolution 
                failExecution(e);
            }
        }
        return null; //no overriding implementation
    }

    /**
     * Checks that the base-level overriding method returned by 
     * an invocation to {@link #lookupMethodImplOverriding}
     * can indeed override a method implementation.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param classFileMethodOverridden the {@link ClassFile} where the overridden method 
     *        implementation is, or {@code null} if the method is classless.
     * @param methodSignatureOverridden the {@link Signature} of the overridden 
     *        method implementation. It must not be {@code null}.
     * @param classFileMethodOverridingResolved the {@link ClassFile} where the overriding method implementation is, 
     *        or {@code null} if the method is classless.. It must not be {@code null}.
     * @param methodSignatureOverriding. It must not be {@code null}.
     * @throws BaseUnsupportedExceptionBaseUnsupportedException if the base-level overriding fails 
     *         because the overriding method is abstract or has a signature that is incompatible 
     *         with the one of the overridden method.
     */
    public static void checkOverridingMethodFits(State state, ClassFile classFileMethodOverridden, Signature methodSignatureOverridden, ClassFile classFileMethodOverriding, Signature methodSignatureOverriding) 
    throws BaseUnsupportedException, MethodNotFoundException {
        if (!classFileMethodOverriding.hasMethodImplementation(methodSignatureOverriding)) {
            throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " is abstract.");
        }
        final boolean overridingStatic;
        final boolean overriddenStatic;
        try {
            overridingStatic = classFileMethodOverriding.isMethodStatic(methodSignatureOverriding);
            overriddenStatic = (classFileMethodOverridden == null ? true : classFileMethodOverridden.isMethodStatic(methodSignatureOverridden));
        } catch (MethodNotFoundException e) {
            throw new BaseUnsupportedException(e);
        }
        if (overriddenStatic == overridingStatic) {
            if (methodSignatureOverridden.getDescriptor().equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else if (!overriddenStatic && overridingStatic) {
            if (descriptorAsStatic(methodSignatureOverridden).equals(methodSignatureOverriding.getDescriptor())) {
                return;
            }
        } else { //(overriddenStatic && !overridingStatic)
            if (descriptorAsStatic(methodSignatureOverriding).equals(methodSignatureOverridden.getDescriptor())) {
                return;
            }
        }
        throw new BaseUnsupportedException("The overriding method " + methodSignatureOverriding + " has signature incompatible with overridden " + methodSignatureOverridden);
    }

    private static String descriptorAsStatic(Signature sig) {
        return "(" + REFERENCE + sig.getClassName() + TYPEEND + sig.getDescriptor().substring(1);
    }

	
    /**
     * Checks if a method name is the name of a static signature polymorphic
     * method.
     * 
     * @param methodName a {@link String}.
     * @return {@code true} if {@code methodName} is one of  
     *         {@code linkToInterface}, {@code linkToSpecial}, {@code linkToStatic},
     *         or {@code linkToVirtual}. 
     */
    public static boolean isSignaturePolymorphicMethodStatic(String methodName) {
        return 
        (JAVA_METHODHANDLE_LINKTOINTERFACE.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.getName().equals(methodName));
    }
	
    /**
     * Checks if a method name is the name of an intrinsic signature polymorphic
     * method.
     * 
     * @param methodName a {@link String}.
     * @return {@code true} if {@code methodName} is one of {@code invokeBasic}, 
     *         {@code linkToInterface}, {@code linkToSpecial}, {@code linkToStatic},
     *         or {@code linkToVirtual}. 
     */
    public static boolean isSignaturePolymorphicMethodIntrinsic(String methodName) {
        return 
        (JAVA_METHODHANDLE_INVOKEBASIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOINTERFACE.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSPECIAL.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOSTATIC.getName().equals(methodName) ||
        JAVA_METHODHANDLE_LINKTOVIRTUAL.getName().equals(methodName));
    }
    
    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param ref a {@link Reference}.
     * @return a {@link String} corresponding to the value of 
     *         the string {@link Instance} referred by {@code ref}, 
     *         or {@code null} if {@code ref} does not refer an {@link Instance} 
     *         in {@code s} (also when {@code ref} is {@link Null}), or 
     *         if it refers an {@link Instance} but its 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or if its type is the 
     *         {@code java.lang.String} but its value field
     *         is not a concrete array of {@code char}s.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public static String valueString(State s, Reference ref) throws FrozenStateException {
        final Instance i;
        try {
            i = (Instance) s.getObject(ref);
        } catch (ClassCastException e) {
            return null;
        }
        if (i == null) {
        	return null;
        }
        return valueString(s, i);
    }
    
    /**
     * Converts a {@code java.lang.String} {@link Instance}
     * into a (meta-level) string.
     * 
     * @param s a {@link State}.
     * @param i an {@link Instance}. It must not be {@code null}. 
     * @return a {@link String} corresponding to the {@code value} of 
     *         the {@code i}, 
     *         or {@code null} if such {@link Instance}'s 
     *         {@link Instance#getType() type} is not the 
     *         {@code java.lang.String} class, or its value
     *         is not a simple array of {@code char}s.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public static String valueString(State s, Instance i) throws FrozenStateException {
        final ClassFile cf_JAVA_STRING = s.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING); //surely loaded
        if (cf_JAVA_STRING == null) {
            failExecution("Could not find class java.lang.String.");
        }
        if (cf_JAVA_STRING.equals(i.getType())) {
            final Reference valueRef = (Reference) i.getFieldValue(JAVA_STRING_VALUE);
            final Array value = (Array) s.getObject(valueRef);
            if (value == null) {
                //this happens when valueRef is symbolic and unresolved
                return null;
            }
            return value.valueString();
        } else {
            return null;
        }
    }

    /**
     * Utility function that writes a value to an array,
     * invoked by *aload and *astore algorithms. If the parameters
     * are incorrect fails symbolic execution.
     * 
     * @param state a {@link State}.
     * @param ctx an {@link ExecutionContext}.
     * @param arrayReference a {@link Reference} to an {@link Array} in the heap 
     *        of {@code State}.
     * @param index the index in the array where the value should be put.
     *        It must be a {@link Primitive} with type {@link Type#INT INT}.
     * @param valueToStore the {@link Value} to be stored in the array.
     * @throws DecisionException upon failure of the decision procedure.
     */
    public static void storeInArray(State state, ExecutionContext ctx, Reference arrayReference, Primitive index, Value valueToStore) 
    throws DecisionException {
        try {
        	final Calculator calc = ctx.getCalculator();
            final Array array = (Array) state.getObject(arrayReference);
            if (array.hasSimpleRep() && index instanceof Simplex) {
                array.setFast((Simplex) index, valueToStore);
            } else {
                final Iterator<? extends Array.AccessOutcomeIn> entries = array.entriesPossiblyAffectedByAccess(calc, index, valueToStore);
                ctx.decisionProcedure.constrainArrayForSet(state.getClassHierarchy(), entries, index);
                array.set(calc, index, valueToStore);
            }
        } catch (InvalidInputException | InvalidTypeException | ClassCastException | 
                 FastArrayAccessNotAllowedException e) {
            //this should never happen
            failExecution(e);
        }
    }
    
    public static void invokeClassLoaderLoadClass(State state, Calculator calc, PleaseLoadClassException e) 
    throws ClasspathException, ThreadStackEmptyException, InvalidInputException {
        try {
            //gets the initiating loader
            final int initiatingLoader = e.getInitiatingLoader();
            if (!state.hasInstance_JAVA_CLASSLOADER(initiatingLoader)) {
                //this should never happen
                failExecution("Unknown classloader identifier " + initiatingLoader + ".");
            }
            final ReferenceConcrete classLoaderReference = state.referenceToInstance_JAVA_CLASSLOADER(initiatingLoader);

            //makes the string for the class name
            final String className = binaryClassName(e.className());
            state.ensureStringLiteral(calc, className);
            final ReferenceConcrete classNameReference = state.referenceToStringLiteral(className);

            //upcalls ClassLoader.loadClass
            //first, creates the snippet
            final Snippet snippet = state.snippetFactoryNoWrap()
                .op_invokevirtual(JAVA_CLASSLOADER_LOADCLASS) //loads the class...
                .op_invokestatic(noclass_REGISTERLOADEDCLASS) //...and registers it with the initiating loader
                .op_return()
                .mk();
	    	final ClassFile cf_JAVA_CLASSLOADER = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASSLOADER); //surely loaded
            state.pushSnippetFrameNoWrap(snippet, 0, cf_JAVA_CLASSLOADER);
            //TODO if ClassLoader.loadClass finds no class we should either propagate the thrown ClassNotFoundException or wrap it inside a NoClassDefFoundError.
            //then, pushes the parameters for noclass_REGISTERLOADEDCLASS
            state.pushOperand(calc.valInt(initiatingLoader));
            //finally, pushes the parameters for JAVA_CLASSLOADER_LOADCLASS
            state.pushOperand(classLoaderReference);
            state.pushOperand(classNameReference);
        } catch (HeapMemoryExhaustedException exc) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
        } catch (InvalidProgramCounterException exc) {
            //this should never happen
            failExecution(exc);
        }
    }
    
    /**
     * Returns the outcomes of an access to an array, performing transitive
     * access to backing arrays, creating fresh symbols if necessary, and
     * producing a result that can be passed to {@link DecisionProcedureAlgorithms}
     * methods.
     * 
     * @param state a {@link State}. It must not be {@code null}.
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param arrayRef a {@link Reference} to the array that is being accessed.
     *         It must not be {@code null}.
     * @param index a {@link Primitive}, the index of the array access.
     *         It must not be {@code null}.
     * @return a {@link List}{@code <}{@link ArrayAccessInfo}{@code >}.
     * @throws InvalidInputException if any parameter is {@code null}, or
     *         {@code state} is frozen.
     */
    public static List<ArrayAccessInfo> getFromArray(State state, Calculator calc, Reference arrayRef, Primitive index) 
    throws InvalidInputException {
        if (state == null || calc == null || arrayRef == null || index == null) {
            throw new InvalidInputException("Invoked getFromArray with a null parameter.");
        }
        final ArrayList<ArrayAccessInfo> retVal = new ArrayList<>();
        final LinkedList<Reference> refToArraysToProcess = new LinkedList<>();
        final LinkedList<Expression> accessConditions = new LinkedList<>();
        final LinkedList<Term> indicesFormal = new LinkedList<>();
        final LinkedList<Primitive> offsets = new LinkedList<>();
        refToArraysToProcess.add(arrayRef);
        accessConditions.add(null);
        indicesFormal.add(null);
        offsets.add(calc.valInt(0));
        while (!refToArraysToProcess.isEmpty()) {
            final Reference refToArrayToProcess = refToArraysToProcess.remove();
            final Primitive referringArrayAccessCondition = accessConditions.remove();
            final Term referringArrayIndexFormal = indicesFormal.remove();
            final Primitive referringArrayOffset = offsets.remove();
            Array arrayToProcess = null; //to keep the compiler happy
            try {
                arrayToProcess = (Array) state.getObject(refToArrayToProcess);
            } catch (ClassCastException exc) {
                //this should never happen
                failExecution(exc);
            }
            if (arrayToProcess == null) {
                //this should never happen
                failExecution("An initial array that backs another array is null.");
            }
            Collection<Array.AccessOutcome> entries = null; //to keep the compiler happy
            try {
                final Primitive indexPlusOffset = calc.push(index).add(referringArrayOffset).pop();
                entries = arrayToProcess.get(calc, indexPlusOffset);
            } catch (InvalidOperandException | InvalidTypeException | InvalidInputException e) {
                //this should never happen
                failExecution(e);
            }
            for (Array.AccessOutcome e : entries) {
                if (e instanceof Array.AccessOutcomeInInitialArray) {
                    final Array.AccessOutcomeInInitialArray eCast = (Array.AccessOutcomeInInitialArray) e;
                    refToArraysToProcess.add(eCast.getInitialArray());
                    accessConditions.add(e.getAccessCondition());
                    indicesFormal.add(arrayToProcess.getIndex());
                    offsets.add(eCast.getOffset());
                } else { 
                    //puts in val the value of the current entry, or a fresh symbol, 
                    //or null if the index is out of bounds
                    Value val;
                    boolean fresh = false;  //true iff val is a fresh symbol
                    if (e instanceof Array.AccessOutcomeInValue) {
                        val = ((Array.AccessOutcomeInValue) e).getValue();
                        if (val == null) {
                            try {
                                final ClassFile memberClass = arrayToProcess.getType().getMemberClass();
                                final String memberType = memberClass.getInternalTypeName(); 
                                final String memberGenericSignature = memberClass.getGenericSignatureType();
                                val = (Value) state.createSymbolMemberArray(memberType, memberGenericSignature, arrayToProcess.getOrigin(), calc.push(index).add(referringArrayOffset).pop());
                            } catch (InvalidOperandException | InvalidTypeException exc) {
                                //this should never happen
                                failExecution(exc);
                            }
                            fresh = true;
                        }
                    } else { //e instanceof Array.AccessOutcomeOut
                        val = null;
                    }

                    try {
                        final Expression accessCondition;
                        final Term indexFormal;
                        if (referringArrayAccessCondition == null) {
                            accessCondition = e.getAccessCondition();
                            indexFormal = arrayToProcess.getIndex();
                        } else {
                            final Primitive entryAccessConditionShifted = calc.push(e.getAccessCondition()).replace(arrayToProcess.getIndex(), calc.push(referringArrayIndexFormal).add(referringArrayOffset).pop()).pop();
                            accessCondition = (Expression) calc.push(referringArrayAccessCondition).and(entryAccessConditionShifted).pop();
                            indexFormal = referringArrayIndexFormal;
                        }
                        retVal.add(new ArrayAccessInfo(refToArrayToProcess, accessCondition, indexFormal, index, val, fresh));
                    } catch (InvalidOperandException | InvalidTypeException exc) {
                        //this should never happen
                        failExecution(exc);
                    }
                }
            }
        }

        return retVal;
    }
    
    /** 
     * Do not instantiate!
     */
    private Util() { 
    	//nothing to do
    }
}
