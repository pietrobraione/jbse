package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_SLOT;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHOD_CLAZZ;
import static jbse.bc.Signatures.JAVA_METHOD_SLOT;
import static jbse.bc.Signatures.JAVA_PARAMETER;
import static jbse.bc.Signatures.JAVA_PARAMETER_EXECUTABLE;
import static jbse.bc.Signatures.JAVA_PARAMETER_INDEX;
import static jbse.bc.Signatures.JAVA_PARAMETER_MODIFIERS;
import static jbse.bc.Signatures.JAVA_PARAMETER_NAME;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.ClassFile.ParameterInfo;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.reflect.Executable#getParameters0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_EXECUTABLE_GETPARAMETERS0 extends Algo_INVOKEMETA_Nonbranching {
	private ClassFile classFileContainer; //set by cookMore
	private Signature methodSignature; //set by cookMore
	
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws FrozenStateException, SymbolicValueNotAllowedException, ClasspathException, InterruptException {
        final Calculator calc = this.ctx.getCalculator();
        try {           
            //gets the Executable represented by 'this'
            final Reference thisRef = (Reference) this.data.operand(0);
            if (state.isNull(thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.reflect.Executable.getParameters0 method is null.");
            }
            final Instance thisObject = (Instance) state.getObject(thisRef);
            
            //gets the relevant information about the executable: container class and slot
            final int slot;
            if (JAVA_METHOD.equals(thisObject.getType().getClassName())) {
            	//gets the container class
            	final Reference refClazz = (Reference) thisObject.getFieldValue(JAVA_METHOD_CLAZZ);
            	final Instance_JAVA_CLASS instanceClazz = (Instance_JAVA_CLASS) state.getObject(refClazz);
            	this.classFileContainer = instanceClazz.representedClass();
            	
                //gets the slot
                final Primitive _slot = (Primitive) thisObject.getFieldValue(JAVA_METHOD_SLOT);
                if (_slot.isSymbolic()) {
                    throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect.Method object is symbolic.");
                }
                slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            } else { //it is a JAVA_CONSTRUCTOR
            	//the container class
            	final Reference refClazz = (Reference) thisObject.getFieldValue(JAVA_CONSTRUCTOR_CLAZZ);
            	final Instance_JAVA_CLASS instanceClazz = (Instance_JAVA_CLASS) state.getObject(refClazz);
            	this.classFileContainer = instanceClazz.representedClass();
            	
                //gets the slot
                final Primitive _slot = (Primitive) thisObject.getFieldValue(JAVA_CONSTRUCTOR_SLOT);
                if (_slot.isSymbolic()) {
                    throw new SymbolicValueNotAllowedException("The slot field in a java.lang.reflect.Method object is symbolic.");
                }
                slot = ((Integer) ((Simplex) _slot).getActualValue()).intValue();
            }
            
            //gets the method signature
            final Signature[] declaredMethods = this.classFileContainer.getDeclaredMethods();
            if (slot < 0 || slot >= declaredMethods.length) {
                //invalid slot number
                throwNew(state, calc, INTERNAL_ERROR); //TODO is it right?
                exitFromAlgorithm();
            }
            this.methodSignature = declaredMethods[slot];
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final ClassHierarchy hier = state.getClassHierarchy();
            final Calculator calc = this.ctx.getCalculator();
            
            
        	ParameterInfo[] paramsInfo = null; //to keep the compiler happy
			try {
				paramsInfo = this.classFileContainer.getMethodParameters(this.methodSignature);
			} catch (MethodNotFoundException e) {
				//this should never happen
				failExecution(e);
			}
        	
        	ReferenceConcrete result = null; //to keep the compiler happy
        	if (paramsInfo == null) {
        		result = Null.getInstance();
        	} else {
				try {
					final ClassFile cfJAVA_PARAMETER = hier.loadCreateClass(JAVA_PARAMETER);
					final ClassFile cf_arrayOf_JAVA_PARAMETER = hier.loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_PARAMETER + TYPEEND);
	        		result = state.createArray(calc, null, calc.valInt(paramsInfo.length), cf_arrayOf_JAVA_PARAMETER);
	        		final Array arrayResult = (Array) state.getObject(result);
	        		for (int i = 0; i < paramsInfo.length; ++i) {
	        			final Simplex _i = calc.valInt(i);
	        			final Reference refParam = state.createInstance(calc, cfJAVA_PARAMETER);
	        			arrayResult.setFast(_i, refParam);
	        			final Instance instanceParam = (Instance) state.getObject(refParam);
	        			instanceParam.setFieldValue(JAVA_PARAMETER_EXECUTABLE, this.data.operand(0));
	        			instanceParam.setFieldValue(JAVA_PARAMETER_INDEX, _i);
	        			instanceParam.setFieldValue(JAVA_PARAMETER_MODIFIERS, calc.valInt(paramsInfo[i].accessFlags));
	        			state.ensureStringLiteral(calc, paramsInfo[i].name);
	        			instanceParam.setFieldValue(JAVA_PARAMETER_NAME, state.referenceToStringLiteral(paramsInfo[i].name));
	        		}
				} catch (HeapMemoryExhaustedException e) {
                    throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                    exitFromAlgorithm();
				} catch (ClassFileNotFoundException e) {
                    //TODO this exception should we wrap/throw a ClassNotFoundException?
                    throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR);
                    exitFromAlgorithm();
				} catch (BadClassFileVersionException e) {
                    throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
                    exitFromAlgorithm();
				} catch (WrongClassNameException e) {
                    throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                    exitFromAlgorithm();
				} catch (ClassFileNotAccessibleException e) {
                    throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
                    exitFromAlgorithm();
				} catch (IncompatibleClassFileException e) {
                    throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
                    exitFromAlgorithm();
				} catch (ClassFileIllFormedException e) {
                    //TODO should throw a subclass of LinkageError
                    throwVerifyError(state, calc);
                    exitFromAlgorithm();
				} catch (RenameUnsupportedException | FastArrayAccessNotAllowedException e) {
					//this should never happen
					failExecution(e);
				}
        	}

            //returns the array
            state.pushOperand(result);
        };
    }
}
