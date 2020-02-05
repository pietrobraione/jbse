package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_ZIPFILE_JZFILE;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.internalClassName;
import static jbse.common.Util.unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.jar.JarFile;
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
import jbse.mem.Array;
import jbse.mem.Instance;
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
 * Meta-level implementation of {@link java.util.jar.JarFile#getMetaInfEntryNames()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_JARFILE_GETMETAINFENTRYNAMES extends Algo_INVOKEMETA_Nonbranching {
    private String[] entryNames; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, 
    SymbolicValueNotAllowedException, FrozenStateException {
        try {
            //gets the 'this' parameter
            final Reference jarFileRef = (Reference) this.data.operand(0);
            if (state.isNull(jarFileRef)) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.util.jar.JarFile.getMetaInfEntryNames was null.");
            }
            final Instance _jarFile = (Instance) state.getObject(jarFileRef);
            if (_jarFile == null) {
                //this should never happen
                failExecution("The 'this' parameter to invocation of method java.util.jar.JarFile.getMetaInfEntryNames was a symbolic unresolved reference.");
            }
            
            //gets the jzfile field value in the 'this' object
            final Primitive _jzfile = (Primitive) _jarFile.getFieldValue(JAVA_ZIPFILE_JZFILE);
            if (_jzfile.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The 'this' parameter to invocation of method java.util.jar.JarFile.getMetaInfEntryNames cannot have its jzfile field set with a symbolic value.");
            }
            final long jzfile = ((Long) ((Simplex) _jzfile).getActualValue()).longValue();
            
            //creates a mock JarFile with only the jzfile field set (this works with Hotspot)
            final JarFile jarFile = (JarFile) unsafe().allocateInstance(JarFile.class);
            final Field jzfileField = ZipFile.class.getDeclaredField("jzfile");
            jzfileField.setAccessible(true);
            jzfileField.set(jarFile, jzfile);
            
            //invokes metacircularly the getEntryBytes method
            final Method method = JarFile.class.getDeclaredMethod("getMetaInfEntryNames");
            method.setAccessible(true);
            this.entryNames = (String[]) method.invoke(jarFile);
        } catch (InvocationTargetException e) {
            final String cause = internalClassName(e.getCause().getClass().getName());
            throwNew(state, this.ctx.getCalculator(), cause);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        } catch (InstantiationException | SecurityException | NoSuchFieldException | 
                 NoSuchMethodException | IllegalAccessException e) {
            //this should not happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            final Calculator calc = this.ctx.getCalculator();
            try {
                if (this.entryNames == null) {
                    state.pushOperand(Null.getInstance());
                } else {
                    final ClassFile cf_arrayOf_JAVA_STRING = state.getClassHierarchy().loadCreateClass("" + ARRAYOF + REFERENCE + JAVA_STRING + TYPEEND);
                    final ReferenceConcrete retVal = state.createArray(calc, null, calc.valInt(this.entryNames.length), cf_arrayOf_JAVA_STRING);
                    final Array array = (Array) state.getObject(retVal);
                    for (int i = 0; i < this.entryNames.length; ++i) {
                        final String entryNames_i = this.entryNames[i];
                        if (entryNames_i == null) {
                            array.setFast(calc.valInt(i), Null.getInstance());
                        } else {
                            state.ensureStringLiteral(calc, entryNames_i);
                            final ReferenceConcrete _entryNames_i = state.referenceToStringLiteral(entryNames_i);
                            array.setFast(calc.valInt(i), _entryNames_i);
                        }
                    }
                    state.pushOperand(retVal);
                }
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, calc, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | 
                     BadClassFileVersionException | RenameUnsupportedException | 
                     WrongClassNameException | IncompatibleClassFileException | 
                     ClassFileNotAccessibleException | ClassCastException | 
                     FastArrayAccessNotAllowedException e) {
                //this should never happen
                failExecution(e);
            }
        };
    }
}
