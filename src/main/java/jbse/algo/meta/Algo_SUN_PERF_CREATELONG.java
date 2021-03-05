package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.JAVA_DIRECTBYTEBUFFER;
import static jbse.bc.Signatures.JAVA_DIRECTBYTEBUFFER_INIT;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Util.unsafe;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;
import sun.misc.Unsafe;

/**
 * Meta-level implementation of {@link sun.misc.Perf#createLong(String, int, int, long)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_PERF_CREATELONG extends Algo_INVOKEMETA_Nonbranching {
    private static final int VU_INVALID = 0;
    private static final int V_LAST = 3;
    private static final int U_LAST = 6;
    
    private String name; //set by cookMore
    private long value; //set by cookMore
    private ClassFile cf_JAVA_DIRECTBYTEBUFFER; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 5;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, UndefinedResultException, SymbolicValueNotAllowedException, 
    ClasspathException, ThreadStackEmptyException, InvalidInputException, RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
        //gets the first (String name) parameter
        final Reference nameReference = (Reference) this.data.operand(1);
        if (state.isNull(nameReference)) {
            throwNew(state, calc, NULL_POINTER_EXCEPTION);
            exitFromAlgorithm();
        }
        this.name = valueString(state, nameReference);
        if (this.name == null) {
            throw new SymbolicValueNotAllowedException("The String name parameter to invocation of method sun.misc.Perf.createLong cannot be a symbolic String.");
        }
        
        //gets the second (int variability) parameter
        final Primitive _variability = (Primitive) this.data.operand(2);
        if (_variability.isSymbolic()) {
            throw new SymbolicValueNotAllowedException("The int variability parameter to invocation of method sun.misc.Perf.createLong cannot be a symbolic value.");
        }
        final int variability = ((Integer) ((Simplex) _variability).getActualValue()).intValue();
        
        //checks variability
        if (variability <= VU_INVALID || variability > V_LAST) {
            throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
            exitFromAlgorithm();
        }
        
        //gets the third (int units) parameter
        final Primitive _units = (Primitive) this.data.operand(3);
        if (_units.isSymbolic()) {
            throw new SymbolicValueNotAllowedException("The int units parameter to invocation of method sun.misc.Perf.createLong cannot be a symbolic value.");
        }
        final int units = ((Integer) ((Simplex) _units).getActualValue()).intValue();
        
        //checks units
        if (units <= VU_INVALID || units > U_LAST) {
            throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
            exitFromAlgorithm();
        }

        //gets the fourth (long value) parameter
        final Primitive _value = (Primitive) this.data.operand(4);
        if (_value.isSymbolic()) {
            throw new SymbolicValueNotAllowedException("The long value parameter to invocation of method sun.misc.Perf.createLong cannot be a symbolic value.");
        }
        this.value = ((Long) ((Simplex) _value).getActualValue()).longValue();

        //loads the classfile for class java.nio.DirectByteBuffer
        try {
            this.cf_JAVA_DIRECTBYTEBUFFER = state.getClassHierarchy().loadCreateClass(state.getCurrentClass().getDefiningClassLoader(), JAVA_DIRECTBYTEBUFFER, state.bypassStandardLoading());
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
            exitFromAlgorithm();
        //TODO are the following behaviors ok?
        } catch (ClassFileNotFoundException e) {
            //TODO this exception should wrap a ClassNotFoundException
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
        } catch (BadClassFileVersionException e) {
            throwNew(state, calc, UNSUPPORTED_CLASS_VERSION_ERROR);
            exitFromAlgorithm();
        } catch (WrongClassNameException e) {
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR); //without wrapping a ClassNotFoundException
            exitFromAlgorithm();
        } catch (IncompatibleClassFileException e) {
            throwNew(state, calc, INCOMPATIBLE_CLASS_CHANGE_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, calc, ILLEGAL_ACCESS_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            //TODO throw LinkageError instead
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
        	final Calculator calc = this.ctx.getCalculator();
            try {
                //checks if name is already registers, and if it is not it registers it
                try {
                    state.registerPerfCounter(this.name);
                } catch (InvalidInputException e) {
                    throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
                    exitFromAlgorithm();
                }

                //allocates some raw memory for a long
                final Unsafe unsafe = unsafe();
                final long address = unsafe.allocateMemory(Long.BYTES);
                state.addMemoryBlock(address, Long.BYTES);
                
                //initializes the memory
                unsafe.putLong(address, this.value);
                
                //creates the new java.nio.DirectByteBuffer and returns it
                final ReferenceConcrete refDirectByteBuffer = state.createInstance(calc, this.cf_JAVA_DIRECTBYTEBUFFER);
                state.pushOperand(refDirectByteBuffer);
                
                //pushes a frame for the constructor of the new object
                state.pushFrame(calc, this.cf_JAVA_DIRECTBYTEBUFFER, JAVA_DIRECTBYTEBUFFER_INIT, false, this.pcOffset, refDirectByteBuffer, calc.valLong(address), calc.valInt(Long.BYTES));
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (NullMethodReceiverException | MethodNotFoundException | MethodCodeNotFoundException | 
                     InvalidSlotException | InvalidProgramCounterException e) {
                //this should never happen
                //TODO really?
                failExecution(e);
            }
        };
    }
    
    @Override
    protected Supplier<Boolean> isProgramCounterUpdateAnOffset() {
        return () -> true;
    }

    @Override
    protected Supplier<Integer> programCounterUpdate() {
        return () -> 0; //nothing to add to the program counter of the pushed frame
    }
}
