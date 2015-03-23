package jbse.dec;

import java.io.IOException;

import jbse.dec.exc.DecisionException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
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
}
