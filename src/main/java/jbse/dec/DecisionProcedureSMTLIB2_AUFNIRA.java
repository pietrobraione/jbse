package jbse.dec;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.val.Calculator;

import java.io.IOException;
import java.util.List;

/**
 * A decision procedure for solvers compatible with SMTLIB 2 specification
 * supporting the AUFNIRA logic and interactive mode. Tested and working with
 * Z3 and CVC4, models only work for Z3.
 * 
 * @author Pietro Braione
 * @author Diego Piazza
 *
 */
public final class DecisionProcedureSMTLIB2_AUFNIRA extends DecisionProcedureExternal {
	public DecisionProcedureSMTLIB2_AUFNIRA(DecisionProcedure next, Calculator calc, List<String> solverCommandLine) throws DecisionException {
		super(next, calc);
		try {
			this.extIf = new DecisionProcedureExternalInterfaceSMTLIB2_AUFNIRA(calc, solverCommandLine);
		} catch (ExternalProtocolInterfaceException | IOException e) {
			throw new DecisionException(e);
		}
	}
	
	@Override
	protected boolean canPopAssumptions() {
		return true; //TODO should query the external tool for capabilities?
	}
}
