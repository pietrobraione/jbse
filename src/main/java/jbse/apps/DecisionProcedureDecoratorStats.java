package jbse.apps;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedure;
import jbse.dec.DecisionProcedureDecorator;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.mem.exc.ContradictionException;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;

/**
 * A {@link DecisionProcedureDecorator} that logs the time spent 
 * and the number of invocations for each method invocation of 
 * its component.
 *  
 * @author Pietro Braione
 */
public final class DecisionProcedureDecoratorStats extends DecisionProcedureDecorator implements Stats {
	private long countPushAssumption = 0;
	private long countClearAssumptions = 0;
	private long countAddAssumptions = 0;
	private long countSetAssumptions = 0;
	private long countGetAssumptions = 0;
	private long countIsSat = 0;
	private long countIsSatAliases = 0;
	private long countIsSatExpands = 0;
	private long countIsSatNull = 0;
	private long countIsSatInitialized = 0;
	private long countIsSatNotInitialized = 0;
	private long countGetModel = 0;
	private long countSimplify = 0;
	private long timePushAssumption = 0;
	private long timeClearAssumptions = 0;
	private long timeAddAssumptions = 0;
	private long timeSetAssumptions = 0;
	private long timeGetAssumptions = 0;
	private long timeIsSat = 0;
	private long timeIsSatAliases = 0;
	private long timeIsSatExpands = 0;
	private long timeIsSatNull = 0;
	private long timeIsSatInitialized = 0;
	private long timeIsSatNotInitialized = 0;
	private long timeGetModel = 0;
	private long timeSimplify = 0;
    private long start;

    private void startTimer() {
        this.start = System.currentTimeMillis();
    }

    private long elapsed() {
        final long elapsed = System.currentTimeMillis() - this.start;
        return elapsed;
    }

    public DecisionProcedureDecoratorStats(DecisionProcedure component) throws InvalidInputException {
        super(component);
    }
    
	public long countPushAssumption() {
		return this.countPushAssumption;
	}
	
	public long countClearAssumptions() {
		return this.countClearAssumptions;
	}
	
	public long countAddAssumptions() {
		return this.countAddAssumptions;
	}
	
	public long countSetAssumptions() {
		return this.countSetAssumptions;
	}
	
	public long countGetAssumptions() {
		return this.countGetAssumptions;
	}
	
	public long countIsSat() {
		return this.countIsSat;
	}
	
	public long countIsSatAliases() {
		return this.countIsSatAliases;
	}
	
	public long countIsSatExpands() {
		return this.countIsSatExpands;
	}
	
	public long countIsSatNull() {
		return this.countIsSatNull;
	}
	
	public long countIsSatInitialized() {
		return this.countIsSatInitialized;
	}
	
	public long countIsSatNotInitialized() {
		return this.countIsSatNotInitialized;
	}
	
	public long countGetModel() {
		return this.countGetModel;
	}
	
	public long countSimplify() {
		return this.countSimplify;
	}
	
	public long timePushAssumption() {
		return this.timePushAssumption;
	}
	
	public long timeClearAssumptions() {
		return this.timeClearAssumptions;
	}
	
	public long timeAddAssumptions() {
		return this.timeAddAssumptions;
	}
	
	public long timeSetAssumptions() {
		return this.timeSetAssumptions;
	}
	
	public long timeGetAssumptions() {
		return this.timeGetAssumptions;
	}
	
	public long timeIsSat() {
		return this.timeIsSat;
	}
	
	public long timeIsSatAliases() {
		return this.timeIsSatAliases;
	}
	
	public long timeIsSatExpands() {
		return this.timeIsSatExpands;
	}
	
	public long timeIsSatNull() {
		return this.timeIsSatNull;
	}
	
	public long timeIsSatInitialized() {
		return this.timeIsSatInitialized;
	}
	
	public long timeIsSatNotInitialized() {
		return this.timeIsSatNotInitialized;
	}
	
	public long timeGetModel() {
		return this.timeGetModel;
	}
	
	public long timeSimplify() {
		return this.timeSimplify;
	}
	
    @Override
    public void pushAssumption(Clause c) 
    throws InvalidInputException, DecisionException, ContradictionException {
    	++this.countPushAssumption;
        startTimer();
        super.pushAssumption(c);
        final long elapsed = elapsed();
        this.timePushAssumption += elapsed;
    }

    @Override
    public void clearAssumptions() 
    throws DecisionException {
    	++this.countClearAssumptions;
        startTimer();
        super.clearAssumptions();
        final long elapsed = elapsed();
        this.timeClearAssumptions += elapsed;
    }
    
    @Override
    public void addAssumptions(Iterable<Clause> assumptionsToAdd) 
    throws InvalidInputException, DecisionException, ContradictionException {
    	++this.countAddAssumptions;
        startTimer();
        super.addAssumptions(assumptionsToAdd);
        final long elapsed = elapsed();
        this.timeAddAssumptions += elapsed;
    }
    
    @Override
    public void addAssumptions(Clause... assumptionsToAdd) 
    throws InvalidInputException, DecisionException, ContradictionException {
    	++this.countAddAssumptions;
        startTimer();
        super.addAssumptions(assumptionsToAdd);
        final long elapsed = elapsed();
        this.timeAddAssumptions += elapsed;
    }

    @Override
    public void setAssumptions(Collection<Clause> newAssumptions) 
    throws InvalidInputException, DecisionException, ContradictionException {
    	++this.countSetAssumptions;
        startTimer();
        super.setAssumptions(newAssumptions);
        final long elapsed = elapsed();
        this.timeSetAssumptions += elapsed;
    }

    @Override
    public List<Clause> getAssumptions() 
    throws DecisionException {
    	++this.countGetAssumptions;
        startTimer();
        final List<Clause> result = super.getAssumptions();
        final long elapsed = elapsed();
        this.timeGetAssumptions += elapsed;
        return result;
    }

    @Override
    public boolean isSat(Expression exp) 
    throws InvalidInputException, DecisionException {
    	++this.countIsSat;
        startTimer();
        final boolean result = super.isSat(exp);
        final long elapsed = elapsed();
        this.timeIsSat += elapsed;
        return result;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o)
    throws InvalidInputException, DecisionException {
    	++this.countIsSatAliases;
        startTimer();
        final boolean result = super.isSatAliases(r, heapPos, o);
        final long elapsed = elapsed();
        this.timeIsSatAliases += elapsed;
        return result;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile)
    throws InvalidInputException, DecisionException {
    	++this.countIsSatExpands;
        startTimer();
        final boolean result = super.isSatExpands(r, classFile);
        final long elapsed = elapsed();
        this.timeIsSatExpands += elapsed;
        return result;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) 
    throws InvalidInputException, DecisionException {
    	++this.countIsSatNull;
        startTimer();
        final boolean result = super.isSatNull(r);
        final long elapsed = elapsed();
        this.timeIsSatNull += elapsed;
        return result;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) 
    throws InvalidInputException, DecisionException {
    	++this.countIsSatInitialized;
        startTimer();
        final boolean result = super.isSatInitialized(classFile);
        final long elapsed = elapsed();
        this.timeIsSatInitialized += elapsed;
        return result;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile)
    throws InvalidInputException, DecisionException {
    	++this.countIsSatNotInitialized;
        startTimer();
        final boolean result = super.isSatInitialized(classFile);
        final long elapsed = elapsed();
        this.timeIsSatNotInitialized += elapsed;
        return result;
    }
    
    @Override
    public Map<PrimitiveSymbolic, Simplex> getModel() throws DecisionException {
    	++this.countGetModel;
        startTimer();
        final Map<PrimitiveSymbolic, Simplex> result = super.getModel();
        final long elapsed = elapsed();
        this.timeGetModel += elapsed;
        return result;
    }
    
    @Override
    public Primitive simplify(Primitive c) throws DecisionException {
    	++this.countSimplify;
        startTimer();
        final Primitive result = super.simplify(c);
        final long elapsed = elapsed();
        this.timeSimplify += elapsed;
        return result;
    }
}
