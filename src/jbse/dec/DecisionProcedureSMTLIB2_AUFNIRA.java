package jbse.dec;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.rewr.CalculatorRewriting;

import java.io.IOException;

public final class DecisionProcedureSMTLIB2_AUFNIRA extends DecisionProcedureExternal {
	public DecisionProcedureSMTLIB2_AUFNIRA(DecisionProcedure next, CalculatorRewriting calc, String solverPath) throws DecisionException {
		super(next, calc);
		try {
			this.extIf = new DecisionProcedureExternalInterfaceSMTLIB2_AUFNIRA(calc, solverPath);
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected boolean canPopAssumptions() {
		return true; //TODO should query the external tool for capabilities?
	}
}
