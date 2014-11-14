package jbse.dec;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.val.ReferenceSymbolic;

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
