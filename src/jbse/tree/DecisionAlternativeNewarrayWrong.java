package jbse.tree;

public class DecisionAlternativeNewarrayWrong extends DecisionAlternativeNewarray {
	private static final String WRONG_ID = "F";
	private static final int WRONG_BN = 2;
	private static final int HASH_CODE = 629977903;

	DecisionAlternativeNewarrayWrong(boolean isConcrete) {
		super(isConcrete, (isConcrete ? 1 : WRONG_BN));
	}

	@Override
	public DecisionAlternativeNewarrayWrong mkNonconcrete() {
		return new DecisionAlternativeNewarrayWrong(false);
	}
	
	@Override
	public boolean ok() {
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return HASH_CODE;
	}

	@Override
	public String toString() {
		return WRONG_ID;
	}
}
