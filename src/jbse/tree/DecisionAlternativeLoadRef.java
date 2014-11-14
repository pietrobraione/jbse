package jbse.tree;

import jbse.val.ReferenceSymbolic;

public interface DecisionAlternativeLoadRef extends DecisionAlternativeLoad {
	@Override
	ReferenceSymbolic getValueToLoad();
}
