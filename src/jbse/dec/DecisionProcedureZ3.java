package jbse.dec;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.rewr.CalculatorRewriting;

import java.io.IOException;

public final class DecisionProcedureZ3 extends DecisionProcedureExternal {
	public DecisionProcedureZ3(DecisionProcedure next, CalculatorRewriting calc, String z3Path) throws DecisionException {
		super(next, calc);
		try {
			this.extIf = new DecisionProcedureExternalInterfaceZ3(calc, z3Path);
		} catch (ExternalProtocolInterfaceException e) {
			throw new DecisionException(e);
		} catch (IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected boolean canPopAssumptions() {
		return true;
	}
}
