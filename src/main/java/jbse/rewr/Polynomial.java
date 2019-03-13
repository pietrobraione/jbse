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
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveVisitor;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;

class Polynomial {
	/** The type. */
	private final char type;

	/** 
	 * The representation of the polynomial as a map from bases (i.e., monomials
	 * with scale set to 1) to their multipliers as {@link Simplex}. 
	 */
	private final Map<Monomial, Simplex> rep;

	private Polynomial(char type, Map<Monomial, Simplex> rep) {
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
			return new Polynomial(this.type, Collections.unmodifiableMap(this.rep));
		}

		public PolynomialBuilder addMonomial(Monomial m) 
		throws InvalidOperandException, InvalidTypeException {
			if (m == null) {
				throw new InvalidOperandException("tried to add a null monomial to a polynomial");
			}
			try {
				addMonomial(m.createBase(this.calc), m.getMultiplier());
			} catch (InvalidOperandException e) {
				//this should never happen after the previous null check
				throw new UnexpectedInternalException(e);
			}
			return this;
		}

		private void addMonomial(Monomial base, Simplex multiplier) 
		throws InvalidOperandException, InvalidTypeException {
			if (this.rep.containsKey(base)) {
				Simplex multiplierNew = (Simplex) this.calc.push(this.rep.get(base)).add(multiplier).pop();
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
					final Monomial base = eThis.getKey().mul(this.calc, eOther.getKey());
					final Simplex multiplier;
					try {
						multiplier = (Simplex) this.calc.push(eThis.getValue()).mul(eOther.getValue()).pop();
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
				final Simplex multiplier = (Simplex) this.calc.push(eThis.getValue()).neg().pop();
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

		public PolynomialBuilder divNumer(Polynomial numer, Polynomial denom)
		throws InvalidOperandException, InvalidTypeException {
			return div(numer, denom, numer.rep.entrySet());
		}

		public PolynomialBuilder divDenom(Polynomial numer, Polynomial denom)
		throws InvalidOperandException, InvalidTypeException {
			return div(numer, denom, denom.rep.entrySet());
		}
		
		private PolynomialBuilder div(Polynomial numer, Polynomial denom, Set<Entry<Monomial, Simplex>> entrySet)
		throws InvalidOperandException, InvalidTypeException {
			if (numer == null || denom == null) {
				throw new InvalidOperandException("One operand of a polynomial division is null.");
			}
			Operator.typeCheck(Operator.DIV, numer.type, denom.type);
			this.type = numer.type;
			final Monomial gcdMonomials = numer.gcdMonomials(this.calc).gcd(this.calc, denom.gcdMonomials(this.calc));
			final boolean allMultipliersEqual = allMultipliersEqual(numer.rep, denom.rep);
			final Simplex gcdMultipliersNumer = (Simplex) numer.gcdMultipliers(this.calc);
			final Simplex gcdMultipliersDenom = (Simplex) denom.gcdMultipliers(this.calc);
			final Simplex gcdMultipliers;
			if (Type.isPrimitiveIntegral(this.type)) {
				gcdMultipliers = gcdSimplex(gcdMultipliersNumer, gcdMultipliersDenom, this.calc);
			} else {
				gcdMultipliers = gcdMultipliersDenom;
			}			 
			final Primitive denomPrimitive = denom.toPrimitive(this.calc);
			final Simplex zero = (Simplex) this.calc.pushInt(0).to(this.type).pop();
			final boolean denomIsSimplexNegative = denomPrimitive instanceof Simplex && this.calc.push(denomPrimitive).lt(zero).pop().surelyTrue();
			final Simplex one = (Simplex) this.calc.pushInt(1).to(this.type).pop();
			for (Entry<Monomial, Simplex> e : entrySet) {
				try {
					final Simplex multiplierDivByGcd = (Simplex) this.calc.push(e.getValue()).div(this.calc.push(gcdMultipliers).to(e.getValue().getType()).pop()).pop();
					this.rep.put(e.getKey().div(this.calc, gcdMonomials)[0], 
							(allMultipliersEqual ? one :
							 denomIsSimplexNegative ? (Simplex) this.calc.push(multiplierDivByGcd).neg().pop() :
							 multiplierDivByGcd));
				} catch (InvalidOperandException | InvalidTypeException exc) {
					//this should never happen
					throw new UnexpectedInternalException(exc);
				}
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
		
		private class RepBuilder implements PrimitiveVisitor {			
			public RepBuilder() { }

			@Override
			public void visitAny(Any x) 
			throws InvalidOperandException, InvalidTypeException {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
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
						PolynomialBuilder.this.calc.pushInt(-1).to(secondOperand.getType()).mul(secondOperand).pop().accept(this);
					}
				} else if (e.getOperator() == Operator.NEG) {
					final Primitive operand = e.getOperand();
					PolynomialBuilder.this.calc.pushInt(-1).to(operand.getType()).mul(operand).pop().accept(this);
				} else {
					final Monomial m = Monomial.of(PolynomialBuilder.this.calc, e);
					addMonomial(m);
				}
			}

			@Override
			public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x)
			throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
				addMonomial(m);
			}

			@Override
			public void visitNarrowingConversion(NarrowingConversion x)
			throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
				addMonomial(m);
			}

			@Override
			public void visitWideningConversion(WideningConversion x)
			throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
				addMonomial(m);
			}

			@Override
			public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s)
			throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, s);
				addMonomial(m);
			}

			@Override
			public void visitSimplex(Simplex x) throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
				addMonomial(m);
			}

			@Override
			public void visitTerm(Term x) throws Exception {
				final Monomial m = Monomial.of(PolynomialBuilder.this.calc, x);
				addMonomial(m);
			}
		}
	}

	private Primitive makePrimitive(CalculatorRewriting calc, boolean normalized, Set<Monomial> bases) {
		try {
			final Primitive zero = calc.pushInt(0).to(this.type).pop();
			Primitive retVal = zero;
			for (Monomial base : bases) {
				final Monomial m = base.mul(calc, Monomial.of(calc, calc.push(this.rep.get(base)).to(this.type).pop()));
				final Primitive mPrimitive = (normalized ? m.toPrimitiveNormalized(calc) : m.toPrimitive()); 
				if (retVal.equals(zero)) {
					retVal = mPrimitive;
				} else {
					retVal = Expression.makeExpressionBinary(retVal, Operator.ADD, mPrimitive);
				}
			}
			return retVal;
		} catch (InvalidTypeException | InvalidOperandException | 
				InvalidOperatorException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	private volatile Primitive toPrimitive;

	public Primitive toPrimitive(CalculatorRewriting calc) {
		Primitive retVal = this.toPrimitive;
		if (retVal == null) {
			this.toPrimitive = makePrimitive(calc, false, this.rep.keySet());
			retVal = this.toPrimitive;
		}
		return retVal;
	}

	private volatile Primitive toPrimitiveNormalized;

	public Primitive toPrimitiveNormalized(CalculatorRewriting calc) {
		Primitive retVal = this.toPrimitiveNormalized;
		if (retVal == null) {
			final TreeSet<Monomial> keysSorted = new TreeSet<>();
			keysSorted.addAll(this.rep.keySet());
			this.toPrimitiveNormalized = makePrimitive(calc, true, keysSorted);
			retVal = this.toPrimitiveNormalized;
		}
		return retVal;
	}

	public Simplex getMultiplier(CalculatorRewriting calc, Monomial m) {
		if (this.rep.containsKey(m)) {
			return this.rep.get(m);
		} else {
			try {
				return (Simplex) calc.pushInt(0).to(this.type).pop();
			} catch (InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
	}

	public Simplex getConstantTerm(CalculatorRewriting calc) {
		try {
			final Monomial one = Monomial.of(calc, calc.pushInt(1).to(this.type).pop()); 
			return getMultiplier(calc, one);
		} catch (InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	public Map<Monomial, Simplex> representation() {
		return this.rep;
	}	

	public Polynomial mul(CalculatorRewriting calc, Polynomial other) 
	throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(calc, makeRep()).mul(this, other).make();
	}

	public Polynomial neg(CalculatorRewriting calc) 
	throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(calc, makeRep()).neg(this).make();
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

	public Polynomial add(CalculatorRewriting calc, Polynomial other) 
	throws InvalidOperandException, InvalidTypeException {
		return new PolynomialBuilder(calc, makeRep()).add(this, other).make();
	}

	private Monomial gcdMonomials(CalculatorRewriting calc) throws InvalidTypeException {
		Monomial retVal = null;
		for (Monomial m : this.rep.keySet()) {
			if (retVal == null) {
				retVal = m;
			} else {
				retVal = retVal.gcd(calc, m);
			}
		}
		return (retVal == null ? Monomial.of(calc, calc.pushInt(1).to(this.type).pop()) : retVal);
	}

	private Primitive gcdMultipliers(CalculatorRewriting calc) throws InvalidTypeException {
		Simplex retVal = null;
		for (Simplex m : this.rep.values()) {
			if (retVal == null) {
				retVal = abs(m, calc);
			} else if (Type.isPrimitiveIntegral(m.getType())) {
				retVal = gcdSimplex(retVal, m, calc);
			} else {
				retVal = null;
				break;
			}
		}
		return (retVal == null ? calc.pushInt(1).to(this.type).pop() : retVal);
	}
	
	private static Simplex abs(Simplex s, CalculatorRewriting calc) {
		try {
			return (calc.push(s).lt(calc.pushInt(0).to(s.getType()).pop()).pop().surelyTrue() ?
					(Simplex) calc.push(s).neg().pop() : s);
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	private static Simplex gcdSimplex(Simplex first, Simplex other, CalculatorRewriting calc) throws InvalidTypeException {
		Operator.typeCheck(Operator.DIV, first.getType(), other.getType());
		final char type = first.getType();
		if (Type.isPrimitiveFloating(type)) {
			//no gcd for floating point values, sorry
			return (Simplex) calc.pushInt(1).to(type).pop();
		}
		long a = Math.abs(((type == Type.INT) ? Long.valueOf((Integer) first.getActualValue()) : Long.valueOf((Long) first.getActualValue())));
		long b = Math.abs(((type == Type.INT) ? Long.valueOf((Integer) other.getActualValue()) : Long.valueOf((Long) other.getActualValue())));
		while (a != b) {
			if (a < b) {
				b = b - a;
			} else {
				a = a - b;
			}
		}
		return (Simplex) calc.pushLong(a).to(type).pop();
	}

	public Polynomial[] div(CalculatorRewriting calc, Polynomial other) 
	throws InvalidOperandException, InvalidTypeException {
		final Polynomial denom = new PolynomialBuilder(calc, makeRep()).divDenom(this, other).make();
		final Polynomial numer = new PolynomialBuilder(calc, makeRep()).divNumer(this, other).make();
		if (numer.isZeroOne(true) || denom.isZeroOne(false)) {
			return new Polynomial[] { numer, null };
		}
		final Polynomial one = of(calc, calc.pushInt(1).to(this.type).pop()); 
		if (numer.equals(denom)) {
			return new Polynomial[] { one, null };
		}
		final Polynomial minusOne = of(calc, calc.pushInt(-1).to(this.type).pop()); 
		if (numer.neg(calc).equals(denom)) {
			return new Polynomial[] { minusOne, null };
		}
		return new Polynomial[] { numer, denom };		
	}

	public Polynomial[] sqrt(CalculatorRewriting calc) throws InvalidTypeException {
		if (this.type != Type.DOUBLE) {
			throw new InvalidTypeException("can calculate square roots only of doubles");
		}
		
		try {
			//if this polynomial is a monomial, pack
			if (this.rep.size() == 1) {
				final Entry<Monomial, Simplex> e = this.rep.entrySet().iterator().next();
				Monomial rebuiltMonomial = e.getKey().mul(calc, Monomial.of(calc, e.getValue()));
				final Monomial[] sqrtMonomial = rebuiltMonomial.sqrt(calc);
				final PolynomialBuilder sqrt = new PolynomialBuilder(calc, makeRep());
				sqrt.type = this.type;
				sqrt.addMonomial(sqrtMonomial[0]);
				final PolynomialBuilder etc = new PolynomialBuilder(calc, makeRep());
				etc.type = this.type;
				etc.addMonomial(sqrtMonomial[1]);
				return new Polynomial[] { sqrt.make(), etc.make() };
			}

			//builds a return value with one as square root and
			//this polynomial as the value under the root sign. 
			//It will be returned whenever it will be unable to 
			//calculate a sensible square root.
			final Polynomial one = of(calc, calc.pushDouble(1.0d).pop()); 
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
			final Monomial aSqrt[] = a.sqrt(calc);
			final Monomial bSqrt[] = b.sqrt(calc);
			if (! (aSqrt[1].isZeroOne(false) && bSqrt[1].isZeroOne(false))) {
				return sameAsInput;
			}

			//divides c for the square root of a times the square root of b
			//and checks that the result is one
			final Monomial[] ratio = c.div(calc, aSqrt[0].mul(calc, bSqrt[0]));
			if (!(ratio[0].isZeroOne(false) && ratio[1].isZeroOne(false))) {
				return sameAsInput;
			}

			//if everything has succeeds, returns the square root
			final PolynomialBuilder sqrt = new PolynomialBuilder(calc, makeRep());
			sqrt.type = this.type;
			sqrt.addMonomial(aSqrt[0], calc.valDouble(1.0d));
			sqrt.addMonomial(bSqrt[0], calc.valDouble(cPositive ? 1.0d : -1.0d));
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
		final Polynomial other = (Polynomial) obj;
		if (this.rep == null) {
			if (other.rep != null) {
				return false;
			}
		} else if (!this.rep.equals(other.rep)) {
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
