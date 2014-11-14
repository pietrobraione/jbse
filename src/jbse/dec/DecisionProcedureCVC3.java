package jbse.dec;

import java.io.IOException;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.rewr.CalculatorRewriting;


public class DecisionProcedureCVC3 extends DecisionProcedureExternal {
	public DecisionProcedureCVC3(DecisionProcedure component, CalculatorRewriting calc, String path) throws DecisionException {
		super(component, calc);
        try {
        	this.extIf = new DecisionProcedureExternalInterfaceCVC3(calc, path);
		} catch (ExternalProtocolInterfaceException e) {
			throw new DecisionException();
		} catch (IOException e) {
			throw new DecisionException();
		}
	}
}
