package jbse.algo;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Offsets.GETX_PUTX_OFFSET;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NO_SUCH_FIELD_ERROR;
import static jbse.common.Type.className;
import static jbse.common.Type.INT;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveOpStack;
import static jbse.common.Type.isReference;
import static jbse.common.Type.NULLREF;

import java.util.function.Supplier;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotAccessibleException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.exc.DecisionException;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

//TODO extract common superclass with Algo_GETX and eliminate duplicate code
/**
 * Abstract {@link Algorithm} managing all the put* bytecodes
 * (putfield, putstatic).
 * 
 * @author Pietro Braione
 */
abstract class Algo_PUTX extends Algorithm<
BytecodeData_1FI,
DecisionAlternative_NONE, 
StrategyDecide<DecisionAlternative_NONE>, 
StrategyRefine<DecisionAlternative_NONE>, 
StrategyUpdate<DecisionAlternative_NONE>> {

    protected Signature fieldSignatureResolved; //set by cook
    protected Value valueToPut; //set by subclass

    @Override
    protected final Supplier<BytecodeData_1FI> bytecodeData() {
        return BytecodeData_1FI::get;
    }

    @Override
    protected final BytecodeCooker bytecodeCooker() {
        return (state) -> {
            //gets the value to put
            this.valueToPut = valueToPut();
            
            //gets the class hierarchy
            final ClassHierarchy hier = state.getClassHierarchy();

            //performs field resolution
            String currentClassName = null; //it's final 
            try {
                currentClassName = state.getCurrentMethodSignature().getClassName();    
                this.fieldSignatureResolved = hier.resolveField(currentClassName, this.data.signature());
            } catch (ClassFileNotFoundException e) {
                throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
                exitFromAlgorithm();
            } catch (FieldNotFoundException e) {
                throwNew(state, NO_SUCH_FIELD_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotAccessibleException | FieldNotAccessibleException e) {
                throwNew(state, ILLEGAL_ACCESS_ERROR);
                exitFromAlgorithm();
            } catch (BadClassFileException e) {
                throwVerifyError(state);
                exitFromAlgorithm();
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                failExecution(e);
            }

            //checks the field
            try {
                final String fieldClassName = this.fieldSignatureResolved.getClassName();
                final ClassFile fieldClassFile = state.getClassHierarchy().getClassFile(fieldClassName);

                //checks that if the field is final is declared in the current class
                if (fieldClassFile.isFieldFinal(this.fieldSignatureResolved) &&
                    !fieldClassName.equals(currentClassName)) {
                    throwNew(state, ILLEGAL_ACCESS_ERROR);
                    exitFromAlgorithm();
                }

                //checks/converts the type of the value to be put
                final String fieldType = this.fieldSignatureResolved.getDescriptor();
                final char valueType = this.valueToPut.getType();
                if (isPrimitive(fieldType)) {
                    final char fieldTypePrimitive = fieldType.charAt(0);
                    if (isPrimitiveOpStack(fieldTypePrimitive)) {
                        if (valueType != fieldTypePrimitive) {
                            throwVerifyError(state);
                            exitFromAlgorithm();
                        }
                    } else if (valueType == INT) {
                        try {
                            this.valueToPut = ((Primitive) this.valueToPut).narrow(fieldTypePrimitive);
                        } catch (InvalidTypeException e) {
                            //this should never happen
                            failExecution(e);
                        }
                    } else {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                } else if (isReference(valueType)) {
                    final String valueObjectType = state.getObject((Reference) this.valueToPut).getType();
                    if (!state.getClassHierarchy().isAssignmentCompatible(valueObjectType, className(fieldType))) {
                        throwVerifyError(state);
                        exitFromAlgorithm();
                    }
                } else if (valueType == NULLREF) {
                    //nothing to do
                } else { //field has reference type, value has primitive type
                    throwVerifyError(state);
                    exitFromAlgorithm();
                }
                
                //bytecode-specific checks
                checkMore(state, fieldClassName, fieldClassFile);
            } catch (FieldNotFoundException | BadClassFileException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            destination(state).setFieldValue(this.fieldSignatureResolved, this.valueToPut);
        };
    }

    @Override
    protected final Class<DecisionAlternative_NONE> classDecisionAlternative() {
        return DecisionAlternative_NONE.class;
    }

    @Override
    protected final StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.FF;
        };
    }

    @Override
    protected final StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> { };
    }

    /**
     * Returns the value to be put.
     * 
     * @return a {@link Value}.
     */
    protected abstract Value valueToPut();

    /**
     * Checks whether the destination of this put (a static or nonstatic
     * field) is correct for the bytecode (bytecode-specific checks).
     * 
     * @param state the current {@link State}.
     * @param fieldClassName a {@link String}, the name of the class
     *        of the field.
     * @param fieldClassFile the {@link ClassFile} for {@code fieldClassName}.
     * @throws FieldNotFoundException if the field does not exist.
     * @throws BadClassFileException if the classfile for the field 
     *         does not exist or is ill-formed.
     * @throws DecisionException if the decision procedure fails.
     * @throws ClasspathException if a standard class is not found.
     * @throws InterruptException if the {@link Algorithm} must be interrupted.
     */
    protected abstract void checkMore(State state, String fieldClassName, ClassFile fieldClassFile)
    throws FieldNotFoundException, BadClassFileException, 
    DecisionException, ClasspathException, InterruptException;
    
    /**
     * Returns the destination puts the value to its destination. 
     * 
     * @param state a {@link State}.
     * @return the {@link Objekt} containing the field where the
     *         value must be put.
     * @throws InterruptException if the {@link Algorithm} must be interrupted.
     */
    protected abstract Objekt destination(State state)
    throws InterruptException;

    @Override
    protected final Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected final Supplier<Integer> programCounterUpdate() {
        return () -> GETX_PUTX_OFFSET;
    }
}
