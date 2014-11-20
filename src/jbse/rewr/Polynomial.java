package jbse.rewr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

class Polynomial {
	/** {@link CalculatorRewriting} for the {@link Primitive} it represents. */
	private final CalculatorRewriting calc;
	
	/** The type. */
	private final char type;

	/** 
	 * The representation of the polynomial as a map from bases (i.e., monomials
	 * with scale set to 1) to their multipliers as {@link Simplex}. 
	 */
	private final Map<Monomial, Simplex> rep;

	private Polynomial(CalculatorRewriting calc, char type, Map<Monomial, Simplex> rep) {
		this.calc = calc;
		this.type = type;
		this.rep = rep;
	}

	public static Polynomial of(CalculatorRewriting calc, Primitive p) {
		return new PolynomialBuilder(calc, makeRep()).of(p).make();
	}

	private static Map<Monomial, Simplex> makeRep() {
		return new HashMap<Monomial, Simplex>(512);
	}

	public static PolynomialBuilder build(CalculatorRewriting calc) {
		return new PolynomialBuilder(calc, makeRep());
	}

	public static class PolynomialBuilder {
		private final CalculatorRewriting calc;
		private char type = Type.UNKNOWN;
		private final Map<Monomial, Simplex> rep;

		private PolynomialBuilder(CalculatorRewriting calc, Map<Monomial, Simplex> rep) {
			this.calc = calc;
			this.rep = rep;
		}

		public PolynomialBuilder of(Primitive p) {
			this.type = p.getType();
			final RepBuilder repBuilder = new RepBuilder();
			try {
				p.accept(repBuilder);
			} catch (RuntimeException exc) {
				throw exc;
			} catch (Exception exc) {
				//this cannot happen
				throw new AssertionError(exc);
			}
			return this;
		}

		public PolynomialBuilder as(Polynomial p) {
			this.type = p.type;
			this.rep.clear();
			this.rep.putAll(p.rep);
			return this;
		}

		public Polynomial make() {
			if (this.type == Type.UNKNOWN || this.type == Type.ERROR) {
				throw new UnexpectedInternalException();
			}
			return new Polynomial(this.calc, this.type, Collections.unmodifiableMap(this.rep));
		}

		public PolynomialBuilder addMonomial(Monomial m) 
		throws InvalidOperandException, InvalidTypeException {
			if (m == null) {
				throw new InvalidOperandException("tried to add a null monomial to a polynomial");
			}
			try {
				addMonomial(m.createBase(), m.getMultiplier());
			} catch (InvalidOperandException e) {
				//this should never happen after the previous null check
				throw new UnexpectedInternalException(e);
			}
			return this;
		}

		private void addMonomial(Monomial base, Simplex multiplier) 
		throws InvalidOperandException, InvalidTypeException {
			if (this.rep.containsKey(base)) {
				Simplex multiplierNew = (Simplex) this.rep.get(base).add(multiplier);
				if (multiplierNew.isZeroOne(true)) {
					this.rep.remove(base);
				} else {
					this.rep.put(base, (Simplex) multiplierNew);
				}
			} else if (multiplier.isZeroOne(true)) {
				return; //do nothing
			} else {
				this.rep.put(base, multiplier);
			}
		}

		public PolynomialBuilder mul(Polynomial first, Polynomial other) 
		throws InvalidOperandException, InvalidTypeException {
			if (first == null || other == null) {
				throw new InvalidOperandException("one of the operands of polynomial multiplication is null");
			}
			Operator.typeCheck(Operator.MUL, first.type, other.type);
			this.type = first.type;
			for (Entry<Monomial, Simplex> eThis : first.rep.entrySet()) {
				for (Entry<Monomial, Simplex> eOther : other.rep.entrySet()) {
					final Monomial base = eThis.getKey().mul(eOther.getKey());
					final Simplex multiplier;
					try {
						multiplier = (Simplex) eThis.getValue().mul(eOther.getValue());
					} catch (InvalidOperandException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					addMonomial(base, multiplier);
				}
			}
			return this;
		}

		public PolynomialBuilder neg(Polynomial p) 
		throws InvalidOperandException, InvalidTypeException {
			if (p == null) {
				throw new InvalidOperandException("tried to negate a null polynomial");
			}
			Operator.typeCheck(Operator.NEG, p.type);
			this.type = p.type;
			for (Entry<Monomial, Simplex> eThis : p.rep.entrySet()) {
				final Monomial base = eThis.getKey();
				final Simplex multiplier = (Simplex) eThis.getValue().neg();
				try {
					addMonomial(base, multiplier);
				} catch (InvalidOperandException | InvalidTypeException exc) {
					//this should never happen
					throw new UnexpectedInternalException(exc);
				}
			}
			return this;
		}

		public PolynomialBuilder add(Polynomial first, Polynomial other)
		throws InvalidOperandException, InvalidTypeException {
			if (first == null || other == null) {
				throw new InvalidOperandException("one operand of a polynomial addition is null");
			}
			Operator.typeCheck(Operator.ADD, first.type, other.type);
			this.type = first.type;
			this.rep.putAll(first.rep);
			for (Entry<Monomial, Simplex> eOther : other.rep.entrySet()) {
				try {
					addMonomial(eOther.getKey(), eOther.getValue());
				} catch (InvalidOperandException | InvalidTypeException exc) {
					//this should never happen
					throw new UnexpectedInternalException(exc);
				}
			}
			return this;
		}

		public PolynomialBuilder divNumer(Polynomial first, Polynomial other)
		throws InvalidOperandException, InvalidTypeException {
			if (first == null || other == null) {
				throw new InvalidOperandException("one operand of a polynomial division is null");
			}
			Operator.typeCheck(Operator.DIV, first.type, other.type);
			this.type = first.type;
			final Monomial gcd = first.gcdMonomials().gcd(other.gcdMonomials());
			final boolean allMultipliersEqual = allMultipliersEqual(first.rep, other.rep);
			final Primitive otherPrimitive = other.toPrimitive();
			final boolean otherIsSimplexFloat = Type.isPrimitiveFloating(other.type) && otherPrimitive instanceof Simplex;
			final boolean allMultipliersDivisibleByOther = Type.isPrimitiveIntegral(other.type) && otherPrimitive instanceof Simplex
					&& allMultipliersDivisibleBy(first.rep, (Simplex) otherPrimitive);
			final Simplex one = (Simplex) this.calc.valInt(1).to(this.type);
			for (Entry<Monomial, Simplex> e : first.rep.entrySet()) {
				try {
					this.rep.put(e.getKey().div(gcd)[0], 
							(allMultipliersEqual ? one :
								(otherIsSimplexFloat || allMultipliersDivisibleByOther) ? (Simplex) e.getValue().div(otherPrimitive) :
									e.getValue()));
				} catch (InvalidOperandException | InvalidTypeException exc) {
					//this should never happen
					throw new UnexpectedInternalException(exc);
				}
			}
			return this;
		}

		public PolynomialBuilder divDenom(Polynomial first, Polynomial other)
		throws InvalidOperandException, InvalidTypeException {
			if (first == null || other == null) {
				throw new InvalidOperandException("one operand of a polynomial division is null");
			}
			Operator.typeCheck(Operator.DIV, first.type, other.type);
			this.type = first.type;
			final Monomial gcd = first.gcdMonomials().gcd(other.gcdMonomials());
			final boolean allMultipliersEqual = allMultipliersEqual(first.rep, other.rep);
			final Primitive otherPrimitive = other.toPrimitive();
			final boolean otherIsSimplexFloat = Type.isPrimitiveFloating(other.type) && otherPrimitive instanceof Simplex;
			final boolean allMultipliersDivisibleByOther = Type.isPrimitiveIntegral(other.type) && otherPrimitive instanceof Simplex
					&& allMultipliersDivisibleBy(first.rep, (Simplex) otherPrimitive);
			final Simplex one = (Simplex) this.calc.valInt(1).to(this.type);
			for (Entry<Monomial, Simplex> e : other.rep.entrySet()) {
				this.rep.put(e.getKey().div(gcd)[0], 
						((allMultipliersEqual || otherIsSimplexFloat || allMultipliersDivisibleByOther) ? one : 
						e.getValue()));
			}
			return this;
		}

		private static boolean allMultipliersEqual(Map<Monomial, Simplex> firstRep, Map<Monomial, Simplex> otherRep) {
			Simplex previous = null;
			for (Simplex s : firstRep.values()) {
				if (previous != null && !previous.equals(s)) {
					return false;
				}
				previous = s;
			}
			for (Simplex s : otherRep.values()) {
				if (previous != null && !previous.equals(s)) {
					return false;
				}
				previous = s;
			}
			return true;
		}
		
		private boolean allMultipliersDivisibleBy(Map<Monomial, Simplex> rep, Simplex otherPrimitive) 
		throws InvalidOperandException, InvalidTypeException {
			Simplex previous = null;
			final Simplex zero = (Simplex) this.calc.valInt(0).to(otherPrimitive.getType()); 
			for (Simplex s : rep.values()) {
				if (previous != null && ((Boolean) ((Simplex) previous.rem(otherPrimitive).ne(zero)).getActualValue())) {
					return false;
				}
				previous = s;
			}
			return true;
		}

		private class RepBuilder implements PrimitiveVisitor {			
			public RepBuilder() { }

			@Override
			public void visitAny(Any x) 
			throws InvalidOperandException, InvalidTypeException {
				final Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}

			@Override
			public void visitExpression(Expression e) throws Exception {
				final Operator operator = e.getOperator();
				if (operator == Operator.ADD || operator == Operator.SUB) {
					e.getFirstOperand().accept(this);
					if (operator == Operator.ADD) {
						e.getSecondOperand().accept(this);
					} else {
						final Primitive secondOperand = e.getSecondOperand();
						calc.valInt(-1).to(secondOperand.getType()).mul(secondOperand).accept(this);
					}
				} else if (e.getOperator() == Operator.NEG) {
					final Primitive operand = e.getOperand();
					calc.valInt(-1).to(operand.getType()).mul(operand).accept(this);
				} else {
					final Monomial m = Monomial.of(calc, e);
					addMonomial(m);
				}
			}

			@Override
			public void visitFunctionApplication(FunctionApplication x)
			throws Exception {
				Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}

			@Override
			public void visitNarrowingConversion(NarrowingConversion x)
			throws Exception {
				Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}

			@Override
			public void visitWideningConversion(WideningConversion x)
			throws Exception {
				Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}

			@Override
			public void visitPrimitiveSymbolic(PrimitiveSymbolic s)
			throws Exception {
				Monomial m = Monomial.of(calc, s);
				addMonomial(m);
			}

			@Override
			public void visitSimplex(Simplex x) throws Exception {
				Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}

			@Override
			public void visitTerm(Term x) throws Exception {
				Monomial m = Monomial.of(calc, x);
				addMonomial(m);
			}
		}
	}

	private Primitive makePrimitive(boolean normalized, Set<Monomial> bases) {
		try {
			final Primitive zero = this.calc.valInt(0).to(this.type);
			Primitive retVal = zero;
			for (Monomial base : bases) {
				Monomial m = base.mul(Monomial.of(this.calc, this.rep.get(base).to(this.type)));
				Primitive mPrimitive = (normalized ? m.toPrimitiveNormalized() : m.toPrimitive()); 
				if (retVal.equals(zero)) {
					retVal = mPrimitive;
				} else {
					retVal = Expression.makeExpressionBinary(calc, retVal, Operator.ADD, mPrimitive);
				}
			}
			return retVal;
		} catch (InvalidTypeException | InvalidOperandException | InvalidOperatorException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	private volatile Primitive toPrimitive;

	public Primitive toPrimitive() {
		Primitive retVal = this.toPrimitive;
		if (retVal == null) {
			this.toPrimitive = makePrimitive(false, this.rep.keySet());
			retVal = this.toPrimitive;
		}
		return retVal;
	}

	private volatile Primitive toPrimitiveNormalized;

	public Primitive toPrimitiveNormalized() {
		Primitive retVal = this.toPrimitiveNormalized;
		if (retVal == null) {
			final TreeSet<Monomial> keysSorted = new TreeSet<Monomial>();
			keysSorted.addAll(this.rep.keySet());
			this.toPrimitiveNormalized = makePrimitive(true, keysSorted);
			retVal = this.toPrimitiveNormalized;
		}
		return retVal;
	}

	public Simplex getMultiplier(Monomial m) {
		if (this.rep.containsKey(m)) {
			return this.rep.get(m);
		} else {
			try {
				return (Simplex) this.calc.valInt(0).to(this.type);
			} catch (InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
	}

	public Simplex getConstantTerm() {
		try {
			final Monomial one = Monomial.of(this.calc, this.calc.valInt(1).to(this.type)); 
			return this.getMultiplier(one);
		} catch (InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	public Map<Monomial, Simplex> representation() {
		return this.rep;
	}	

	public Polynomial mul(Polynomial other) 
	throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(this.calc, makeRep()).mul(this, other).make();
	}

	public Polynomial neg() 
			throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(this.calc, makeRep()).neg(this).make();
	}

	public boolean isZeroOne(boolean zero) {
		if (zero) {
			for (Entry<Monomial, Simplex> e : this.rep.entrySet()) {
				if (e.getValue().isZeroOne(zero)) { //NB: the monomial (key) can't be zero!
					continue;
				}
				return false;
			}
			return true;
		} else {
			if (this.rep.entrySet().size() == 1) {
				final Entry<Monomial, Simplex> e = this.rep.entrySet().iterator().next();
				if (e.getKey().isZeroOne(false) && e.getValue().isZeroOne(false)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	public Polynomial add(Polynomial other) 
			throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(this.calc, makeRep()).add(this, other).make();
	}

	public Monomial gcdMonomials() throws InvalidTypeException {
		Monomial retVal = null;
		for (Monomial m : this.rep.keySet()) {
			if (retVal == null) {
				retVal = m;
			} else {
				retVal = retVal.gcd(m);
			}
		}
		return (retVal == null ? Monomial.of(this.calc, this.calc.valInt(1).to(this.type)) : retVal);
	}

	public Polynomial[] div(Polynomial other) 
	throws InvalidOperandException, InvalidTypeException {
		final Polynomial denom = new PolynomialBuilder(this.calc, makeRep()).divDenom(this, other).make();
		final Polynomial numer = new PolynomialBuilder(this.calc, makeRep()).divNumer(this, other).make();
		if (numer.isZeroOne(true) || denom.isZeroOne(false)) {
			return new Polynomial[] { numer, null };
		}
		final Polynomial one = of(this.calc, this.calc.valInt(1).to(this.type)); 
		if (numer.equals(denom)) {
			return new Polynomial[] { one, null };
		}
		final Polynomial minusOne = of(this.calc, this.calc.valInt(-1).to(this.type)); 
		if (numer.neg().equals(denom)) {
			return new Polynomial[] { minusOne, null };
		}
		return new Polynomial[] { numer, denom };		
	}

	public Polynomial[] sqrt() throws InvalidTypeException {
		if (this.type != Type.DOUBLE) {
			throw new InvalidTypeException("can calculate square roots only of doubles");
		}
		
		try {
			//if this polynomial is a monomial, pack
			if (this.rep.size() == 1) {
				final Entry<Monomial, Simplex> e = this.rep.entrySet().iterator().next();
				Monomial rebuiltMonomial = e.getKey().mul(Monomial.of(this.calc, e.getValue()));
				final Monomial[] sqrtMonomial = rebuiltMonomial.sqrt();
				final PolynomialBuilder sqrt = new PolynomialBuilder(this.calc, makeRep());
				sqrt.type = this.type;
				sqrt.addMonomial(sqrtMonomial[0]);
				final PolynomialBuilder etc = new PolynomialBuilder(this.calc, makeRep());
				etc.type = this.type;
				etc.addMonomial(sqrtMonomial[1]);
				return new Polynomial[] { sqrt.make(), etc.make() };
			}

			//builds a return value with one as square root and
			//this polynomial as the value under the root sign. 
			//It will be returned whenever it will be unable to 
			//calculate a sensible square root.
			final Polynomial one = of(this.calc, this.calc.valDouble(1).to(Type.DOUBLE)); 
			final Polynomial[] sameAsInput = new Polynomial[] { one, this };

			//if it is not the sum of three monomials, it is not a square
			if (this.rep.size() != 3) {
				return sameAsInput;
			}

			//determines whether two monomials have 1 as multiplier 
			//and the third has 2 as multiplier; in the case, it stores the formers
			//in a and b, and the latter in c
			int onesCount = 0;
			int twosCount = 0;
			Monomial a = null, b = null, c = null;
			boolean cPositive = true;
			for (Entry<Monomial, Simplex> e : this.rep.entrySet()) {
				final Simplex value = e.getValue();
				if (value.isZeroOne(false)) {
					++onesCount;
					if (a == null) {
						a = e.getKey();
					} else {
						b = e.getKey();
					}
				} else {
					if (Type.isPrimitiveIntegral(value.getType())) {
						final Number n = (Number) value.getActualValue();
						final long l = n.longValue();
						if (l == 2 || l == -2) {
							++twosCount;
							c = e.getKey();
							if (l < 0) {
								cPositive = false;
							}
						}
					} else if (Type.isPrimitiveFloating(value.getType())) {
						final Number n = (Number) value.getActualValue();
						final double d = n.doubleValue();
						if (d == 2.0 || d == -2.0) {
							++twosCount;
							c = e.getKey();
							if (d < 0) {
								cPositive = false;
							}
						}
					}
				}
			}
			if (onesCount != 2 || twosCount != 1) {
				return sameAsInput;
			}

			//takes the square root of a and b and
			//checks nothing remains under the square root sign
			final Monomial aSqrt[] = a.sqrt();
			final Monomial bSqrt[] = b.sqrt();
			if (! (aSqrt[1].isZeroOne(false) && bSqrt[1].isZeroOne(false))) {
				return sameAsInput;
			}

			//divides c for the square root of a times the square root of b
			//and checks that the result is one
			final Monomial[] ratio = c.div(aSqrt[0].mul(bSqrt[0]));
			if (!(ratio[0].isZeroOne(false) && ratio[1].isZeroOne(false))) {
				return sameAsInput;
			}

			//if everything has succeeds, returns the square root
			final PolynomialBuilder sqrt = new PolynomialBuilder(this.calc, makeRep());
			sqrt.type = this.type;
			sqrt.addMonomial(aSqrt[0], this.calc.valDouble(1));
			sqrt.addMonomial(bSqrt[0], this.calc.valDouble(cPositive ? 1 : -1));
			return new Polynomial[] { sqrt.make(), one };
		} catch (InvalidOperandException exc) {
			//this should never happen
			throw new UnexpectedInternalException(exc);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rep == null) ? 0 : rep.hashCode());
		return result;
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
		Polynomial other = (Polynomial) obj;
		if (rep == null) {
			if (other.rep != null) {
				return false;
			}
		} else if (!rep.equals(other.rep)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (this.rep.isEmpty()) {
			return (Type.isPrimitiveIntegral(this.type) ? "0" : "0.0");
		}
		final StringBuilder sb = new StringBuilder();
		boolean firstDone = false;
		for (Entry<Monomial, Simplex> e : this.rep.entrySet()) {
			if (firstDone) {
				sb.append(" + ");
			} else {
				firstDone = true;
			}
			sb.append(e.getValue().toString());
			sb.append("*");
			sb.append(e.getKey().toString());
		}
		return sb.toString();
	}
}
