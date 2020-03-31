package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.invokeClassLoaderLoadClass;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.INCOMPATIBLE_CLASS_CHANGE_ERROR;
import static jbse.bc.Signatures.INDEX_OUT_OF_BOUNDS_EXCEPTION;
import static jbse.bc.Signatures.LINKAGE_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.UNSUPPORTED_CLASS_VERSION_ERROR;
import static jbse.common.Type.internalClassName;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.exc.AlreadyDefinedClassException;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Instance_JAVA_CLASSLOADER;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;
import jbse.val.exc.InvalidTypeException;

/**
 * Meta-level implementation of {@link java.lang.ClassLoader#defineClass1(String, byte[], int, int, ProtectionDomain, String)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASSLOADER_DEFINECLASS1 extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile classFile; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 7;
    }

    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, ClasspathException, InterruptException, 
    SymbolicValueNotAllowedException, InvalidInputException, InvalidTypeException, 
    RenameUnsupportedException {
    	final Calculator calc = this.ctx.getCalculator();
        try {
            //gets the first ('this') parameter
            final Reference thisReference = (Reference) this.data.operand(0);
            if (state.isNull(thisReference)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.ClassLoader.defineClass1 method is null.");
            }
            final Instance_JAVA_CLASSLOADER classLoaderInstance = (Instance_JAVA_CLASSLOADER) state.getObject(thisReference);
            final int classLoader = classLoaderInstance.classLoaderIdentifier();
            
            //gets the second (String name) parameter
            final Reference nameReference = (Reference) this.data.operand(1);
            final String name;
            if (state.isNull(nameReference)) {
                name = null; //in this case, name must be ignored
            } else {
                name = internalClassName(valueString(state, nameReference));
                if (name == null) {
                    throw new SymbolicValueNotAllowedException("The String name parameter to invocation of method java.util.zip.ZipFile.open cannot be a symbolic String.");
                }
            }
            
            //gets the third (byte[] b) parameter
            final Reference bufReference = (Reference) this.data.operand(2);
            if (state.isNull(bufReference)) {
                throwNew(state, calc, NULL_POINTER_EXCEPTION);
                exitFromAlgorithm();
            }
            final Array _buf = (Array) state.getObject(bufReference);
            if (!_buf.isSimple()) {
                throw new SymbolicValueNotAllowedException("The byte[] b parameter to invocation of method java.lang.ClassLoader.defineClass1 is not simple.");
            }
            
            //gets the fourth (int off) parameter
            final Primitive _ofst = (Primitive) this.data.operand(3);
            if (_ofst.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int ofst parameter to invocation of method java.io.FileInputStream.readBytes cannot be a symbolic value.");
            }
            final int ofst = ((Integer) ((Simplex) _ofst).getActualValue()).intValue();
            
            //gets the fifth (int len) parameter
            final Primitive _len = (Primitive) this.data.operand(4);
            if (_len.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int len parameter to invocation of method java.io.FileInputStream.readBytes cannot be a symbolic value.");
            }
            final int len = ((Integer) ((Simplex) _len).getActualValue()).intValue();
            
            //checks offset and length
            final int bufLength = ((Integer) ((Simplex) _buf.getLength()).getActualValue()).intValue();
            if (ofst < 0 || len < 0 || bufLength - ofst < len) {
                throwNew(state, calc, INDEX_OUT_OF_BOUNDS_EXCEPTION);
                exitFromAlgorithm();
            }
            
            //sets the bytecode
            final byte[] buf = new byte[len];
            for (int i = ofst; i < ofst + len; ++i) {
                final Simplex _buf_i = (Simplex) ((Array.AccessOutcomeInValue) _buf.get(calc, calc.valInt(i)).iterator().next()).getValue(); 
                buf[i - ofst] = ((Byte) _buf_i.getActualValue()).byteValue();
            }
            
            //defines the class
            this.classFile = state.getClassHierarchy().defineClass(classLoader, name, buf, state.bypassStandardLoading(), false);
            state.ensureInstance_JAVA_CLASS(calc, this.classFile);
        } catch (PleaseLoadClassException e) {
            invokeClassLoaderLoadClass(state, calc, e);
            exitFromAlgorithm();
        } catch (ClassFileNotFoundException e) {
            //this is how Hotspot behaves
            //TODO this exception should wrap a ClassNotFoundException
            throwNew(state, calc, NO_CLASS_DEFINITION_FOUND_ERROR);
            exitFromAlgorithm();
        } catch (AlreadyDefinedClassException e) {
            throwNew(state, calc, LINKAGE_ERROR);
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
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, calc, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassFileIllFormedException e) {
            //TODO throw LinkageError instead
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        } catch (ClassCastException e) {
            throwVerifyError(state, calc);
            exitFromAlgorithm();
        }
    }

    @Override
    protected final StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.pushOperand(state.referenceToInstance_JAVA_CLASS(this.classFile));
        };
    }
}
