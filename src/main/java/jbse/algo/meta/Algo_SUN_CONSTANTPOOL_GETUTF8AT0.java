package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.SUN_CONSTANTPOOL_CONSTANTPOOLOOP;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolUtf8;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.exc.ClasspathException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link sun.reflect.ConstantPool#getUTF8At0(Object, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_SUN_CONSTANTPOOL_GETUTF8AT0 extends Algo_INVOKEMETA_Nonbranching {
    private String value;
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 3;
    }

    @Override
    protected void cookMore(State state) throws FrozenStateException, UndefinedResultException, 
    SymbolicValueNotAllowedException, ClasspathException, InterruptException {
        final Calculator calc = this.ctx.getCalculator();
        
        //gets the classfile
        Reference constantPoolOopRef = (Reference) this.data.operand(1);
        if (state.isNull(constantPoolOopRef)) {
            //gets the first ('this') parameter
            final Reference thisRef = (Reference) this.data.operand(0);
            if (state.isNull(thisRef)) {
                //this should never happen
                failExecution("The 'this' parameter to sun.reflect.ConstantPool.getUTF8At0 method is null.");
            }
            final Instance cpool = (Instance) state.getObject(thisRef);
            constantPoolOopRef = (Reference) cpool.getFieldValue(SUN_CONSTANTPOOL_CONSTANTPOOLOOP);
            if (state.isNull(constantPoolOopRef)) {
                throw new UndefinedResultException("Unexpected null value in field constantPoolOop in a sun.reflect.ConstantPool object.");
            }
        }
        //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
        final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(constantPoolOopRef);
        if (clazz == null) {
            //this should never happen
            failExecution("The 'this' parameter to java.lang.Class.isInstance method is symbolic and unresolved.");
        }
        final ClassFile representedClass = clazz.representedClass();
        
        //gets the index
        final Primitive _index = (Primitive) this.data.operand(2);
        if (_index.isSymbolic()) {
            throw new SymbolicValueNotAllowedException("The int index parameter to invocation of method sun.reflect.ConstantPool.getUTF8At0 cannot be a symbolic value.");
        }
        final int index = ((Integer) ((Simplex) _index).getActualValue()).intValue();

        //gets the value
        try {
            this.value = ((ConstantPoolUtf8) representedClass.getValueFromConstantPool(index)).getValue();
        } catch (InvalidIndexException | ClassCastException e) {
            throwNew(state, calc, ILLEGAL_ARGUMENT_EXCEPTION);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            throwVerifyError(state, calc);
        }
    }


    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
                state.ensureStringLiteral(calc, this.value);
                final Reference toPush = state.referenceToStringLiteral(this.value);
                state.pushOperand(toPush);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
        };
    }
}
