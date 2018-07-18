package jbse.algo.meta;

import static jbse.algo.Util.failExecution;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * An {@link Algorithm} for an auxiliary method for the implementation of
 * {@link jbse.algo.Util#invokeClassLoaderLoadClass(State, jbse.bc.exc.PleaseLoadClassException)}. 
 * It registers a loaded class in the current state's loaded class cache. Its first parameter is 
 * an {@code int}, the identifier of a (initiating) classloader, its second parameter is an
 * {@link Instance_JAVA_CLASS} produced by a call to {@code java.lang.ClassLoader#loadClass(String)}.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_noclass_REGISTERLOADEDCLASS extends Algo_INVOKEMETA_Nonbranching {
    private int classLoader; //set by cookMore
    private ClassFile classFile; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }
    
    @Override
    protected void cookMore(State state) throws FrozenStateException {
        try {
            //gets the classloader identifier
            final Primitive classLoaderPrimitive = (Primitive) this.data.operand(0);
            if (classLoaderPrimitive.isSymbolic()) {
                //this should never happen
                failExecution("Attempted to register a loaded class with a symbolic classloader identifier.");
            }
            this.classLoader = ((Integer) ((Simplex) classLoaderPrimitive).getActualValue()).intValue();
            
            //gets the classfile
            final Reference classReference = (Reference) this.data.operand(1);
            final Instance_JAVA_CLASS classObject = (Instance_JAVA_CLASS) state.getObject(classReference);
            if (classObject == null) {
                //this should never happen
                failExecution("Attempted to register a loaded class with a symbolic reference to the java.lang.Class instance.");
            }
            this.classFile = classObject.representedClass();
        } catch (ClassCastException e) {
            //this should never happen
            failExecution(e);
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.getClassHierarchy().addClassFileClassArray(this.classLoader, this.classFile);
        };
    }
}
