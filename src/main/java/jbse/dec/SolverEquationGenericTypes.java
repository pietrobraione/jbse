package jbse.dec;

import static jbse.common.Type.eraseGenericParameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

	final class Equation {
		private final TypeTerm left, right;

		Equation(TypeTerm left, TypeTerm right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return this.left.toString() + " = " + this.right.toString();
		}
	}

	boolean solved = false;
	boolean hasSolution = false;
	final HashMap<Var, String> solution = new HashMap<>();
	final Partition<Var> partition = new Partition<>();
	
	private final ArrayList<Equation> equations = new ArrayList<>();

	void addEquation(TypeTerm lhs, TypeTerm rhs) throws InvalidInputException {
		if (this.solved) {
			throw new InvalidInputException("The system of equations has already been solved.");
		}
		if (lhs == null || rhs == null) {
			throw new InvalidInputException("Either the lhs or the rhs of the equation is null.");
		}
		this.equations.add(new Equation(lhs, rhs));
	}

	private void addEquations(TypeTerm[] lhss, TypeTerm[] rhss) throws InvalidInputException {
		if (lhss.length != rhss.length) {
			throw new InvalidInputException("Invoked addEquations with lhsTexts and rhsTexts having different lengths.");
		}
		for (int i = 0; i < lhss.length; ++i) {
			addEquation(lhss[i], rhss[i]);
		}
	}

	void solve() {
		while (!this.equations.isEmpty()) {
			final Equation e = this.equations.remove(0);
			if (e.left instanceof Var) {
				if (e.right instanceof Var) {
					this.partition.union((Var) e.left, (Var) e.right);
				} else {
					this.solution.put((Var) e.left, eraseGenericParameters(e.right.toString()));
				}
			} else {
				if (e.right instanceof Var) {
					this.solution.put((Var) e.right, eraseGenericParameters(e.left.toString()));
				} else {
					if (e.left instanceof Apply && e.right instanceof Apply) {
						final Apply applyLeft = (Apply) e.left;
						final Apply applyRight = (Apply) e.right;
						if (applyLeft.functor.equals(applyRight.functor) &&
								applyLeft.arity() == applyRight.arity()) {
							try {
								addEquations(applyLeft.args, applyRight.args);
							} catch (InvalidInputException e1) {
								//this should never happen
								throw new UnexpectedInternalException(e1);
							}
						} else {
							this.hasSolution = false;
							return;
						}
					} else {
						//one is an Apply and another one is Some: 
						//just discard the equation
					}
				}
			}
		}

		for (Var v : this.solution.keySet()) {
			final String res = this.solution.get(v);
			final Var v2 = this.partition.find(v);
			final String res2 = this.solution.get(v2);
			if (!res.equals(res2)) {
				this.hasSolution = false;
				return;
			}
		}

		this.hasSolution = true;
	}

	boolean hasSolution() {
		return this.hasSolution;
	}

	String getVariableValue(Var var) throws InvalidInputException {
		if (!this.hasSolution) {
			throw new InvalidInputException("The equations have no solution.");
		}
		final Var part = this.partition.find(var);
		return this.solution.get(part);
	}
}
