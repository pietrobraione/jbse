package jbse.dec;

import java.io.IOException;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterDivisionEqualsZero;
import jbse.val.ReferenceSymbolic;


public class DecisionProcedureSicstus extends DecisionProcedureExternal {
	public DecisionProcedureSicstus(DecisionProcedure component, CalculatorRewriting calc, String sicstusPath) throws DecisionException {
		super(component, calc, new RewriterDivisionEqualsZero());
        try {
        	this.extIf = new DecisionProcedureExternalInterfaceSicstus(calc, sicstusPath);
		} catch (ExternalProtocolInterfaceException e) {
			throw new DecisionException(e);
		} catch (IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected boolean isSatAliasesImpl(ReferenceSymbolic r, long heapPos, Objekt o) 
	throws DecisionException {
		return delegateIsSatAliases(r, heapPos, o);
	}
	
	@Override
	protected boolean isSatExpandsImpl(ReferenceSymbolic r, String className) 
	throws DecisionException {
		return delegateIsSatExpands(r, className);
	}
	
	@Override
	protected boolean isSatNullImpl(ReferenceSymbolic r)
	throws DecisionException {
		return delegateIsSatNull(r);
	}
	
	@Override
	protected boolean isSatInitializedImpl(String className)
	throws DecisionException {
		return delegateIsSatInitialized(className);
	}
	
	@Override
	protected boolean isSatNotInitializedImpl(String className)
	throws DecisionException {
		return delegateIsSatNotInitialized(className);
	}
}
