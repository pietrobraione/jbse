package jbse.dec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbse.bc.ClassFile;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * Class implementing a "no assumption" {@link DecisionProcedure} 
 * for which all clauses are satisfiable.
 * 
 * @author Pietro Braione
 *
 */
public class DecisionProcedureAlwSat implements DecisionProcedure {
	private Calculator calc; //unused, but gets useful in the Chain of Responsibility to inject it through the chain
    private ArrayList<Clause> cstack;

    public DecisionProcedureAlwSat(Calculator calc) {
    	this.calc = calc;
        this.cstack = new ArrayList<>();
    }
    
    @Override
    public Calculator getCalculator() {
    	return this.calc;
    }

    @Override
    public List<Clause> getAssumptions() {
    	return Collections.unmodifiableList(this.cstack);
    }

    @Override
    public void pushAssumption(Clause c) {
        this.cstack.add(c);
    }

    @Override
    public void clearAssumptions()  {
        this.cstack.clear();
    }

    @Override
    public boolean isSat(Expression exp) {
        return true;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) {
        return true;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) {
        return true;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile) {
        return true;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) {
        return true;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile) {
        return true;
    }
}
