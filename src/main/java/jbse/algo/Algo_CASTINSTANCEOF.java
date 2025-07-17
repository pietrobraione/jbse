package jbse.algo;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.bc.Offsets.CASTINSTANCEOF_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.HeapObjekt;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_IsNotSubclass;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_IsSubclass;
import jbse.tree.DecisionAlternative_CASTINSTANCEOF_Null;
import jbse.val.Reference;

/**
 * Abstract {@link Algorithm} implementing the 
 * checkcast and the instanceof bytecodes.
 * 
 * @author Pietro Braione
 *
 */
abstract class Algo_CASTINSTANCEOF extends Algorithm<
BytecodeData_1CL,
DecisionAlternative_CASTINSTANCEOF,
StrategyDecide<DecisionAlternative_CASTINSTANCEOF>, 
StrategyRefine<DecisionAlternative_CASTINSTANCEOF>, 
StrategyUpdate<DecisionAlternative_CASTINSTANCEOF>> {

    protected ClassFile classCast; //result of the check, for the subclasses of this algorithm
    protected boolean isNull; //result of the check, for the subclasses of this algorithm
    protected boolean isSubclass; //result of the check, for the subclasses of this algorithm
    protected boolean isNotSubclass; //result of the check, for the subclasses of this algorithm
    protected boolean refine; //result of the check, for the subclasses of this algorithm

    @Override
    protected final Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected final Supplier<BytecodeData_1CL> bytecodeData() {
        return BytecodeData_1CL::get;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> { 
            try {
                //gets the operand
                final Reference referenceObj = (Reference) this.data.operand(0);

                //checks whether the object's class is a subclass 
                //of the class name from the constant pool
                if (state.isNull(referenceObj)) {
                    this.isNull = true;
                    this.isSubclass = false;
                    this.isNotSubclass = false;
                } else {
                    this.isNull = false;
                    //performs resolution of the class name
                    final ClassFile currentClass = state.getCurrentClass();    
                    this.classCast = state.getClassHierarchy().resolveClass(currentClass, this.data.className(), state.bypassStandardLoading());
                    
                    //gets the object's class
                    final HeapObjekt obj = state.getObject(referenceObj);
                    final ClassFile classObj = obj.getType();
                    if (obj.isSymbolic()) {
                    	this.refine = this.classCast.isSubclass(classObj);
                    	this.isSubclass = classObj.isSubclass(this.classCast) || this.refine || this.classCast.isInterface();
                    } else {
                    	this.refine = false;
                    	this.isSubclass = classObj.isSubclass(this.classCast);
                    }
                    this.isNotSubclass = !classObj.isSubclass(this.classCast);
                }
            } catch (PleaseLoadClassException e) {
                invokeClassLoaderLoadClass(state, this.ctx.getCalculator(), e);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException e) {
                //TODO this exception should wrap a ClassNotFoundException
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileVersionException e) {
                throwNew(state, this.ctx.getCalculator(), UNSUPPORTED_CLASS_VERSION_ERROR);
                exitFromAlgorithm();
            } catch (WrongClassNameException e) {
                throwNew(state, this.ctx.getCalculator(), NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
                exitFromAlgorithm();
            } catch (IncompatibleClassFileException e) {
                throwNew(state, this.ctx.getCalculator(), INCOMPATIBLE_CLASS_CHANGE_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException e) {
                throwNew(state, this.ctx.getCalculator(), ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (ClassCastException | ClassFileIllFormedException e) {
                throwVerifyError(state, this.ctx.getCalculator());
                exitFromAlgorithm();
            } catch (RenameUnsupportedException e) {
            	//this should never happen
            	failExecution(e);
            }
        };
    }

    @Override
    protected final Class<DecisionAlternative_CASTINSTANCEOF> classDecisionAlternative() {
        return DecisionAlternative_CASTINSTANCEOF.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_CASTINSTANCEOF> decider() {
        return (state, result) -> { 
        	if (this.isNull) {
        		result.add(DecisionAlternative_CASTINSTANCEOF_Null.instance());
        	}
        	if (this.isSubclass) {
        		result.add(DecisionAlternative_CASTINSTANCEOF_IsSubclass.instance(this.refine));
        	}
        	if (this.isNotSubclass) {
        		result.add(DecisionAlternative_CASTINSTANCEOF_IsNotSubclass.instance());
        	}
            return DecisionProcedureAlgorithms.Outcome.val(false, result.size() > 1);
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_CASTINSTANCEOF> refiner() {
        return (state, alt) -> { };
    }

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> CASTINSTANCEOF_OFFSET;
    }
}
