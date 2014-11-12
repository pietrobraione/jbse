package jbse.dec;

import jbse.exc.dec.DecisionException;
import jbse.exc.dec.ExternalProtocolInterfaceException;
import jbse.mem.Objekt;
import jbse.mem.ReferenceSymbolic;
import jbse.rewr.CalculatorRewriting;

import java.io.IOException;

public final class DecisionProcedureZ3 extends DecisionProcedureExternal {
	public DecisionProcedureZ3(DecisionProcedure next, CalculatorRewriting calc, String z3Path) throws DecisionException {
		super(next, calc);
		try {
			this.extIf = new DecisionProcedureExternalInterfaceZ3(calc, z3Path);
		} catch (ExternalProtocolInterfaceException e) {
			throw new DecisionException(e.getMessage());
		} catch (IOException e) {
			throw new DecisionException();
		}
	}
	
	@Override
	protected boolean canPopAssumptions() {
		return true;
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
