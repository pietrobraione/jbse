package jbse.dec;

import static jbse.common.Type.eraseGenericParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

final class SolverEquationGenericTypes {
	static abstract class TypeTerm { }
	static final class Apply extends TypeTerm {
		private final String functor;
		private final TypeTerm[] args;
		private final int hashCode;

		Apply(String functor, TypeTerm... args) {
			this.functor = functor;
			this.args = args;
			final int prime = 19;
			int result = 1;
			result = prime * result + Arrays.hashCode(this.args);
			result = prime * result + ((this.functor == null) ? 0 : this.functor.hashCode());
			this.hashCode = result;
		}

		int arity() { return this.args.length; }

		@Override
		public String toString() {
			return this.functor + (this.args.length == 0 ? "" : ("<" + String.join(", ", Arrays.asList(this.args).stream().map(TypeTerm::toString).collect(Collectors.toList())) + ">"));
		}

		@Override
		public int hashCode() {
			return this.hashCode;
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
			final Apply other = (Apply) obj;
			if (!Arrays.equals(this.args, other.args)) {
				return false;
			}
			if (this.functor == null) {
				if (other.functor != null) {
					return false;
				}
			} else if (!this.functor.equals(other.functor)) {
				return false;
			}
			return true;
		}
	}
	static final class Var extends TypeTerm {
		private final String name;
		private final int hashCode;

		Var(String name) {
			this.name = name;
			final int prime = 43;
			int result = 1;
			result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
			this.hashCode = result;
		}

		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public int hashCode() {
			return this.hashCode;
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
			final Var other = (Var) obj; {
				if (this.name == null) {
					if (other.name != null) {
						return false;
					}
				} else if (!this.name.equals(other.name)) {
					return false;
				}
				return true;
			}


		}
	}
	static final class Some extends TypeTerm { 
		private Some() { }
		private static final Some INSTANCE = new Some();
		static Some instance() { return INSTANCE; }

		@Override
		public String toString() {
			return "*";
		}
	}

	static final class Equation {
		private final TypeTerm left, right;
		private final int hashCode;

		Equation(TypeTerm left, TypeTerm right) {
			this.left = left;
			this.right = right;
			final int prime = 31;
			int result = 1;
			result = prime * result + left.hashCode() + right.hashCode();
			this.hashCode = result;
		}

		@Override
		public int hashCode() {
			return this.hashCode;
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
			final Equation other = (Equation) obj;
			if (this.left.equals(other.left) && this.right.equals(other.right)) {
				return true;
			}
			if (this.left.equals(other.right) && this.right.equals(other.left)) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return this.left.toString() + " = " + this.right.toString();
		}
	}

	boolean solved = false;
	boolean hasSolution = false;
	final HashMap<Var, String> solution = new HashMap<>();
	
	private final ArrayList<Equation> equations = new ArrayList<>();
	private final HashSet<Var> variables = new HashSet<>();
	private final ArrayList<Equation> equationsCurrent = new ArrayList<>();
	private final ArrayList<Equation> equationsProcessed = new ArrayList<>();

	void addEquation(TypeTerm lhs, TypeTerm rhs) throws InvalidInputException {
		if (this.solved) {
			throw new InvalidInputException("The system of equations has already been solved.");
		}
		if (lhs == null || rhs == null) {
			throw new InvalidInputException("Either the lhs or the rhs of the equation is null.");
		}
		this.equations.add(new Equation(lhs, rhs));
		addVariables(lhs);
		addVariables(rhs);
	}
	
	void addEquations(TypeTerm[] lhss, TypeTerm[] rhss) throws InvalidInputException {
		if (lhss.length != rhss.length) {
			throw new InvalidInputException("Invoked SolverEquationGenericTypes.addEquations with lhss and rhss having different lengths.");
		}
		for (int i = 0; i < lhss.length; ++i) {
			addEquation(lhss[i], rhss[i]);
		}
	}

	private void addVariables(TypeTerm t) {
		if (t instanceof Var) {
			this.variables.add((Var) t);
		} else if (t instanceof Apply) {
			final Apply a = (Apply) t;
			for (TypeTerm arg : a.args) {
				addVariables(arg);
			}
		}
	}

	private void addEquationCurrent(TypeTerm lhs, TypeTerm rhs) {
		if (this.solved || lhs == null || rhs == null) {
			throw new UnexpectedInternalException("Invoked SolverEquationGenericTypes.addEquationScratch with wrong parameter.");
		}
		this.equationsCurrent.add(new Equation(lhs, rhs));
	}

	private void addEquationsCurrent(TypeTerm[] lhss, TypeTerm[] rhss) {
		if (lhss.length != rhss.length) {
			throw new UnexpectedInternalException("Invoked SolverEquationGenericTypes.addEquationsScratch with lhss and rhss having different lengths.");
		}
		for (int i = 0; i < lhss.length; ++i) {
			addEquationCurrent(lhss[i], rhss[i]);
		}
	}

	void solve() {
		this.equationsCurrent.addAll(this.equations);
		while (true) {
			boolean failed = removeFunctors();
			if (failed) {
				this.solved = true;
				this.hasSolution = false;
				break;
			}
			failed = incrementSolution();
			if (failed) {
				this.solved = true;
				this.hasSolution = false;
				break;
			}
			if (allVariablesAssigned()) {
				this.solved = true;
				this.hasSolution = true;
				break;
			}
			final boolean noProgress = generateNewEquations();
			if (noProgress) {
				this.solved = true;
				this.hasSolution = true; //infinite solutions for some (unassigned) variables
				break;
			}
		}
	}
	
	private boolean removeFunctors() {
		boolean failed = false;
		ArrayList<Equation> toProcess = new ArrayList<>();
		
		for (Equation e : this.equationsCurrent) {
			if (e.left instanceof Apply && e.right instanceof Apply) {
				final Apply applyLeft = (Apply) e.left;
				final Apply applyRight = (Apply) e.right;
				if (applyLeft.functor.equals(applyRight.functor) &&
				applyLeft.arity() == applyRight.arity()) {
					toProcess.add(e);
				} else {
					failed = true;
					break;
				}
			}
		}
		for (Equation e : toProcess) {
			this.equationsCurrent.remove(e);
			this.equationsProcessed.add(e);
			final Apply applyLeft = (Apply) e.left;
			final Apply applyRight = (Apply) e.right;
			addEquationsCurrent(applyLeft.args, applyRight.args);
		}
		return failed;
	}
	
	
	private boolean incrementSolution() {
		boolean failed = false;
		for (Equation e : this.equationsCurrent) {
			final Var variable;
			final String value;
			if (e.left instanceof Var && !(e.right instanceof Var)) {
				variable = (Var) e.left;
				value = eraseGenericParameters(e.right.toString());
			} else if (!(e.left instanceof Var) && e.right instanceof Var) {
				variable = (Var) e.right;
				value = eraseGenericParameters(e.left.toString());
			} else {
				continue;
			}
			if (this.solution.containsKey(variable) && !this.solution.get(variable).equals(value)) {
				failed = true;
				break;
			} else {
				this.solution.put(variable, value);
			}
		}
		return failed;
	}
	
	private boolean allVariablesAssigned() {
		return this.solution.keySet().equals(this.variables);
	}
	
	private boolean generateNewEquations() {
		final Partition<TypeTerm> partition = new Partition<>();
		for (Equation e : this.equationsCurrent) {
			partition.union(e.left, e.right);
		}
		
		boolean noProgress = true;
		for (int i = 0; i < this.equationsCurrent.size() - 1; ++i) {
			for (int k = i + 1; k < this.equationsCurrent.size(); ++k) {
				final Equation ei = this.equationsCurrent.get(i);
				final Equation ek = this.equationsCurrent.get(k);
				if (partition.find(ei.left).equals(partition.find(ek.left))) {
					//tries to add equation ei.left = ek.left
					if (!ei.left.equals(ek.left)) {
						final Equation eNew = new Equation(ei.left, ek.left);
						if (!this.equationsCurrent.contains(eNew) && !this.equationsProcessed.contains(eNew)) {
							noProgress = false;
							this.equationsCurrent.add(eNew);
						}
					}
					//tries to add equation ei.left = ek.right
					if (!ei.left.equals(ek.right)) {
						final Equation eNew = new Equation(ei.left, ek.right);
						if (!this.equationsCurrent.contains(eNew) && !this.equationsProcessed.contains(eNew)) {
							noProgress = false;
							this.equationsCurrent.add(eNew);
						}
					}
					//tries to add equation ei.right = ek.left
					if (!ei.right.equals(ek.left)) {
						final Equation eNew = new Equation(ei.right, ek.left);
						if (!this.equationsCurrent.contains(eNew) && !this.equationsProcessed.contains(eNew)) {
							noProgress = false;
							this.equationsCurrent.add(eNew);
						}
					}
					//tries to add equation ei.right = ek.right
					if (!ei.right.equals(ek.right)) {
						final Equation eNew = new Equation(ei.right, ek.right);
						if (!this.equationsCurrent.contains(eNew) && !this.equationsProcessed.contains(eNew)) {
							noProgress = false;
							this.equationsCurrent.add(eNew);
						}
					}
				}
			}
		}
		return noProgress;
	}
	
	boolean hasSolution() {
		return this.hasSolution;
	}

	String getVariableValue(Var var) throws InvalidInputException {
		if (!this.hasSolution) {
			throw new InvalidInputException("The equations have no solution.");
		}
		return this.solution.get(var);
	}
}
