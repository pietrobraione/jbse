package jbse.algo.meta;


import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.ClassFile;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.Instance_METALEVELBOX;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} for completing the semantics of
 * linking of a call site (usually from an invokedynamic bytecode).
 * The first parameter is a {@link ReferenceConcrete} to the 
 * {@code java.lang.invoke.MemberName} returned by the upcall to 
 * {@code java.lang.invoke.MethodHandleNatives.linkCallSite}, 
 * the second parameter is a {@link ReferenceConcrete} to the appendix,
 * the third parameter is a  {@link Reference} to the {@link Instance_JAVA_CLASS} 
 * for the container class of the dynamic call site, the fourth parameter
 * is a meta-level box containing the method descriptor of the dynamic call site, 
 * the fifth parameter is a meta-level box containing the method name of the dynamic 
 * call site, and the sixth parameter is a meta-level box containing the program
 * counter of the dynamic call site.
 * 
 * @author Pietro Braione
 */
public final class Algo_noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile containerClass; //set by cookMore
    private String descriptor; //set by cookMore
    private String name; //set by cookMore
    private int programCounter; //set by cookMore
    
    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 6;
    }
    
    @Override
    protected void cookMore(State state) 
    throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException, FrozenStateException {
    	final Instance_JAVA_CLASS containerClassInstance = (Instance_JAVA_CLASS) state.getObject((Reference) this.data.operand(2));
    	this.containerClass = containerClassInstance.representedClass();
    	final Instance_METALEVELBOX bDescriptor = (Instance_METALEVELBOX) state.getObject((Reference) this.data.operand(3));
    	this.descriptor = (String) bDescriptor.get();
    	final Instance_METALEVELBOX bName = (Instance_METALEVELBOX) state.getObject((Reference) this.data.operand(4));
    	this.name = (String) bName.get();
    	final Instance_METALEVELBOX bProgramCounter = (Instance_METALEVELBOX) state.getObject((Reference) this.data.operand(5));
    	this.programCounter = ((Integer) bProgramCounter.get()).intValue();
    }
    
    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            state.linkCallSite(this.containerClass, this.descriptor, this.name, this.programCounter, (ReferenceConcrete) this.data.operand(0), (ReferenceConcrete) this.data.operand(1));
        };
    }
}
