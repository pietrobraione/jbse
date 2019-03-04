package jbse.rewr;

import jbse.val.Rewriter;

public abstract class RewriterCalculatorRewriting extends Rewriter {
	protected CalculatorRewriting calc; //will be injected by the CalculatorRewriting itself
	
	@Override
	public RewriterCalculatorRewriting clone() {
		return (RewriterCalculatorRewriting) super.clone();
	}
}
