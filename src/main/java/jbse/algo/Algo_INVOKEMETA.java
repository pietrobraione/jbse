package jbse.algo;

import java.util.function.Supplier;

import jbse.algo.BytecodeData_1KME.Kind;
import jbse.bc.Signature;
import jbse.tree.DecisionAlternative;

/**
 * Abstract {@link Algorithm} implementing the effect of 
 * a method call.
 * 
 * @author Pietro Braione
 *
 */
public abstract class Algo_INVOKEMETA<
R extends DecisionAlternative, 
DE extends StrategyDecide<R>, 
RE extends StrategyRefine<R>, 
UP extends StrategyUpdate<R>> 
extends Algorithm<BytecodeData_1KME, R, DE, RE, UP> {

    protected boolean isInterface; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isSpecial; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isStatic; //set by setter (called by Algo_INVOKEX_Abstract)
    protected boolean isOverriddenMethodNative; //set by setter (called by Algo_INVOKEX_Abstract)
    protected Signature methodSignatureImplementation; //set by setter (called by Algo_INVOKEX_Abstract)

    public final void setFeatures(boolean isInterface, boolean isSpecial, boolean isStatic, boolean isOverriddenMethodNative, Signature methodSignatureImplementation) {
        this.isInterface = isInterface;
        this.isSpecial = isSpecial;
        this.isStatic = isStatic;
        this.isOverriddenMethodNative = isOverriddenMethodNative;
        this.methodSignatureImplementation = methodSignatureImplementation;
    }

    @Override
    protected final Supplier<BytecodeData_1KME> bytecodeData() {
        return () -> BytecodeData_1KME.withMethod(Kind.kind(this.isInterface, this.isSpecial, this.isStatic)).get();
    }
}
