package jbse.dec;

import java.util.ArrayList;
import java.util.Iterator;

import jbse.bc.ClassHierarchy;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.rules.LICSRuleExpandsTo;
import jbse.rules.LICSRuleAliases;
import jbse.rules.LICSRulesRepo;
import jbse.val.ReferenceSymbolic;

/**
 * A decision procedure based on LICS reference resolution rules.
 * 
 * @author Pietro Braione
 */
public final class DecisionProcedureLICS extends DecisionProcedureChainOfResponsibility {
	private final LICSRulesRepo rulesRepo;
	
	/** Stores all the {@link ClauseAssumeExpands} that are pushed. */
	private final ArrayList<ClauseAssumeExpands> expansions = new ArrayList<>();

	public DecisionProcedureLICS(DecisionProcedure next, CalculatorRewriting calc, LICSRulesRepo rulesRepo) {
		super(next, calc);
		this.rulesRepo = rulesRepo.clone(); //safety copy
	}

	@Override
	protected void clearAssumptionsLocal() {
		this.expansions.clear();
	}

	@Override
	protected void pushAssumptionLocal(ClauseAssumeExpands c) {
		this.expansions.add(c);
	}
	
    //TODO support pop of assumptions

	@Override
	protected boolean isSatExpandsLocal(ClassHierarchy hier, ReferenceSymbolic ref, String className) {
		//gets the rules matching ref
		final ArrayList<LICSRuleExpandsTo> rules = this.rulesRepo.matchingLICSRulesExpandsTo(ref);

		//1- if no rule matches ref, no constraint applies
		//and returns true
		if (rules.isEmpty()) {
			return true;
		}

		//2- if ref satisfies some rules, returns true 
		for (LICSRuleExpandsTo rule : rules) {
			if (rule.satisfies(className)) {
				return true;
			}
		}
		
		//3- no matching rule is satisfied
		return false;
	}

	@Override
	protected boolean isSatAliasesLocal(ClassHierarchy hier, ReferenceSymbolic ref, long heapPos, Objekt o) {
		//gets the rules matching ref
		final ArrayList<LICSRuleAliases> rulesMax = this.rulesRepo.matchingLICSRulesAliasesMax(ref);
		final ArrayList<LICSRuleAliases> rulesNonMax = this.rulesRepo.matchingLICSRulesAliasesNonMax(ref);

		//1- if no rule matches ref, no constraint applies
		//and returns true
		if (rulesMax.isEmpty() && rulesNonMax.isEmpty()) {
			return true;
		}
		
		//2- if ref satisfies at least one "max"
		//rule and o's origin has length not less than
		//the current maxLen for the rule, returns true
nextRule:
		for (LICSRuleAliases rule : rulesMax) {
			if (rule.satisfies(ref, o)) {
				final int oLen = o.getOrigin().toString().length();
				for (Objekt oOther : objectsSymbolic()) {
					if (oLen < oOther.getOrigin().toString().length() && 
							rule.satisfies(ref, oOther)) {
						continue nextRule;
					}
				}
				return true;
			}
		}

		//3- if ref satisfies some non-"max" rules, 
		//returns true 
		for (LICSRuleAliases rule : rulesNonMax) {
			if (rule.satisfies(ref, o)) {
				return true;
			}
		}
		
		//4- no matching rule is satisfied
		return false;
	}
	
	private Iterable<Objekt> objectsSymbolic() {
		return new Iterable<Objekt>() {
			@Override
			public Iterator<Objekt> iterator() {
				return new Iterator<Objekt>() {
					private final Iterator<ClauseAssumeExpands> it = 
							DecisionProcedureLICS.this.expansions.iterator();
					
					@Override
					public boolean hasNext() {
						return this.it.hasNext();
					}

					@Override
					public Objekt next() {
						final ClauseAssumeExpands c = this.it.next();
						return c.getObjekt();
					}
				};
			}
		};
	}

	@Override
	protected boolean isSatNullLocal(ClassHierarchy hier, ReferenceSymbolic ref) {
		final boolean notNull = this.rulesRepo.someMatchingLICSRulesNotNull(ref);
		return !notNull;
	}
}
