package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.internalClassName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.zip.ZipFile;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.Primitive;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.util.zip.ZipFile#getEntryBytes(long, int)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_ZIPFILE_GETENTRYBYTES extends Algo_INVOKEMETA_Nonbranching {
    private byte[] entryBytes; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, SymbolicValueNotAllowedException, InvalidInputException {
        try {
            //gets the first (long jzentry) parameter
            final Primitive _jzentry = (Primitive) this.data.operand(0);
            if (_jzentry.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The long jzentry parameter to invocation of method java.util.zip.ZipFile.getEntryBytes cannot be a symbolic value.");
            }
            final long jzentry = ((Long) ((Simplex) _jzentry).getActualValue()).longValue();
            //TODO what if jzentry is not open?
            
            //gets the second (int type) parameter
            final Primitive _type = (Primitive) this.data.operand(1);
            if (_type.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int type parameter to invocation of method java.util.zip.ZipFile.getEntryBytes cannot be a symbolic value.");
            }
            final int type = ((Integer) ((Simplex) _type).getActualValue()).intValue();
            
            //invokes metacircularly the getEntryBytes method
            final Method method = ZipFile.class.getDeclaredMethod("getEntryBytes", long.class, int.class);
            method.setAccessible(true);
            this.entryBytes = (byte[]) method.invoke(null, state.getZipFileEntryJz(jzentry), type);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, this.ctx.getCalculator(), cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
                if (this.entryBytes == null) {
                    state.pushOperand(Null.getInstance());
                } else {
                    final ClassFile cf_arrayOf_BYTE = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + BYTE);
                    final ReferenceConcrete retVal = state.createArray(calc, null, calc.valInt(this.entryBytes.length), cf_arrayOf_BYTE);
                    final Array array = (Array) state.getObject(retVal);
                    for (int i = 0; i < this.entryBytes.length; ++i) {
                        array.setFast(calc.valInt(i), calc.valByte(this.entryBytes[i]));
                    }
                    state.pushOperand(retVal);
                }
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                     RenameUnsupportedException | WrongClassNameException | IncompatibleClassFileException | ClassFileNotAccessibleException | 
                     ClassCastException | FastArrayAccessNotAllowedException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
