package jbse.dec;

import java.io.IOException;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.dec.DecisionException;
import jbse.exc.dec.ExternalProtocolInterfaceException;
import jbse.mem.Objekt;
import jbse.mem.ReferenceSymbolic;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterDivisionEqualsZero;


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
	throws DecisionException, UnexpectedInternalException {
		return delegateIsSatAliases(r, heapPos, o);
	}
	
	@Override
	protected boolean isSatExpandsImpl(ReferenceSymbolic r, String className) 
	throws DecisionException, UnexpectedInternalException {
		return delegateIsSatExpands(r, className);
	}
	
	@Override
	protected boolean isSatNullImpl(ReferenceSymbolic r)
	throws DecisionException, UnexpectedInternalException {
		return delegateIsSatNull(r);
	}
	
	@Override
	protected boolean isSatInitializedImpl(String className)
	throws DecisionException, UnexpectedInternalException {
		return delegateIsSatInitialized(className);
	}
	
	@Override
	protected boolean isSatNotInitializedImpl(String className)
	throws DecisionException, UnexpectedInternalException {
		return delegateIsSatNotInitialized(className);
	}
}
