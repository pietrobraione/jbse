package jbse.algo.meta;

import static jbse.algo.Util.ensureInstance_JAVA_CLASS;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Null;
import jbse.val.Reference;

/**
 * Meta-level implementation of {@link java.lang.Class#getDeclaringClass0()}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_CLASS_GETDECLARINGCLASS0 extends Algo_INVOKEMETA_Nonbranching {
    private Reference declaringClass;

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 1;
    }

    @Override
    protected void cookMore(State state)
    throws ThreadStackEmptyException, InterruptException {
        //There are four kind of classes:
        //
        //1- Top-level: not nested (declared outside everything in packages, or so it seems)
        //2- Nested: declared inside another class/interface; among this there are static nested 
        //   (associated to the class) and inner (associated to an instance) 
        //3- Local: a particular kind of inner class, declared inside a block
        //4- Anonymous: a particular kind of local classes, they don't have a name
        //
        //By reversing the JVM code it seems that this method should return null
        //for top-level classes, local and anonymous classes, as these are not,
        //or are not considered, members of the class they are nested in.
        //Thus this method should return, only for static nested classes and 
        //proper (neither local nor anonymous) inner classes the name of the
        //container class.
        try {
            //gets the 'this' java.lang.Class instance from the heap 
            //and the name of the class it represents
            final Reference javaClassRef = (Reference) this.data.operand(0);
            if (state.isNull(javaClassRef)) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaringClass0 method is null.");
            }
            //TODO the next cast fails if javaClassRef is symbolic and expanded to a regular Instance. Handle the case.
            final Instance_JAVA_CLASS clazz = (Instance_JAVA_CLASS) state.getObject(javaClassRef);
            if (clazz == null) {
                //this should never happen
                failExecution("The 'this' parameter to java.lang.Class.getDeclaringClass0 method is symbolic and unresolved.");
            }
            
            //gets the classfile
            ClassFile cf = null; //to keep the compiler happy
            try {
                final String className = clazz.representedClass();
                cf = (clazz.isPrimitive() ? 
                      state.getClassHierarchy().getClassFilePrimitive(className) :
                      state.getClassHierarchy().getClassFile(className));
            } catch (BadClassFileException e) {
                //this should never happen
                failExecution(e);
            }
            
            //gets a reference to the java.lang.Class object for the container class
            final String container = cf.classContainer();
            if (container == null || cf.isArray() || cf.isPrimitive() || cf.isLocal() || cf.isAnonymous()) {
                this.declaringClass = Null.getInstance();
            } else {
                ensureInstance_JAVA_CLASS(state, container, container, this.ctx); //TODO should check the accessor?
                this.declaringClass = state.referenceToInstance_JAVA_CLASS(container);
            }            
        } catch (HeapMemoryExhaustedException e) {
            throwNew(state, OUT_OF_MEMORY_ERROR);
            exitFromAlgorithm();
        } catch (ClassCastException | BadClassFileException e) {
            throwVerifyError(state);
            exitFromAlgorithm();
        } catch (ClassFileNotAccessibleException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        state.pushOperand(this.declaringClass);
    }
}
