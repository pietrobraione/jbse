package jbse.tree;

import jbse.mem.ReferenceSymbolic;

public interface DecisionAlternativeLoadRef extends DecisionAlternativeLoad {
	@Override
	ReferenceSymbolic getValueToLoad();
}
