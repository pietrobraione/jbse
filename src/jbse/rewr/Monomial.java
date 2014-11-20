package jbse.rewr;

import java.util.Collections;
import java.util.Comparator;
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

/**
 * Class for monomials, i.e., the product of powers of numeric {@link Primitive}s 
 * which are not themselves monomials.
 * 
 * @author Pietro Braione
 */
class Monomial implements Comparable<Monomial> {
	/** {@link CalculatorRewriting} for the {@link Primitive} it represents. */
	private final CalculatorRewriting calc;
	
	/** The type. */
	private final char type;

	/** A {@link Simplex} constant multiplier for the monomial. */
	private final Simplex scale;

	/** 
	 * {@link Map} which associates each (non {@link Simplex}) multiplier
	 * in the monomial with its integer positive power.
	 */
	private final Map<Primitive, Integer> rep;

	/**
	 * A {@link Comparator} over all the {@link Primitive}s. It is used when
	 * constructing a normalized {@link Primitive} from the monomial's 
	 * representation.
	 */
	private final Comparator<Primitive> comparatorPrimitive = Collections.reverseOrder(new Comparator<Primitive>() {
		@Override
		public int compare(Primitive o1, Primitive o2) {
			int i = o1.getClass().getName().compareTo(o2.getClass().getName());
			if (i == 0) {
				return o1.toString().compareTo(o2.toString());
			}
			return i;
		}
	});

	private Monomial(CalculatorRewriting calc, char type, Simplex scale, Map<Primitive, Integer> rep) {
		this.calc = calc;
		this.type = type;
		this.scale = scale;
		this.rep = rep;
	}
	
	private static Map<Primitive, Integer> makeRep() {
		return new HashMap<Primitive, Integer>(512);
	}

	public static Monomial of(CalculatorRewriting calc, Primitive p) {
		return new MonomialBuilder(calc, makeRep()).of(p).make();
	}

	public static class MonomialBuilder {
		private final CalculatorRewriting calc;
		private char type = Type.UNKNOWN;
		private Simplex scale;
		private final Map<Primitive, Integer> rep;
		
		private MonomialBuilder(CalculatorRewriting calc, Map<Primitive, Integer> rep) {
			this.calc = calc;
			this.scale = (Simplex) this.calc.valInt(1); //wrong type! to be patched when type will be available
			this.rep = rep;
		}
		
		public MonomialBuilder of(Primitive p) {
			this.type = p.getType();
			try {
				this.scale = (Simplex) this.scale.to(this.type);
			} catch (InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			final RepBuilder repBuilder = new RepBuilder();
			try {
				p.accept(repBuilder);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			return this;
		}
		
		public Monomial make() throws UnexpectedInternalException {
			if (this.type == Type.UNKNOWN || this.type == Type.ERROR) {
				throw new UnexpectedInternalException(); //TODO throw better exception
			}
			return new Monomial(calc, this.type, this.scale, Collections.unmodifiableMap(this.rep));
		}

		private MonomialBuilder incExponent(Primitive p, int howMuch) {
			if (rep.containsKey(p)) {
				int pow = rep.get(p);
				if (pow + howMuch == 0) {
					rep.remove(p);
				} else {
					rep.put(p, pow + howMuch);
				}
			} else {
				rep.put(p, howMuch);
			}
			return this;
		}

		public MonomialBuilder base(Monomial m) {
			this.type = m.type;
			try {
				this.scale = (Simplex) this.scale.to(this.type);
			} catch (InvalidTypeException e) {
				//should never happen
				throw new UnexpectedInternalException(e);
			}
			this.rep.putAll(m.rep);
			return this;
		}
		
		public MonomialBuilder mul(Monomial first, Monomial other) 
		throws InvalidTypeException {
			Operator.typeCheck(Operator.MUL, first.type, other.type);
			base(first);
			try {
				this.scale = (Simplex) this.scale.mul(other.scale);
			} catch (InvalidOperandException e) {
				//this should never happen after type check
				throw new UnexpectedInternalException(e);
			}
			for (Entry<Primitive, Integer> e : other.rep.entrySet()) {
				incExponent(e.getKey(), e.getValue());
			}
			return this;
		}

		public MonomialBuilder gcd(Monomial first, Monomial other) 
		throws InvalidTypeException {
			Operator.typeCheck(Operator.DIV, first.type, other.type);
			this.type = first.type;
			this.scale = (Simplex) this.scale.to(this.type);
			for (Entry<Primitive, Integer> e : first.rep.entrySet()) {
				final Primitive multiplier = e.getKey(); 
				if (other.rep.containsKey(multiplier)) {
					this.rep.put(multiplier, Math.min(e.getValue(), other.rep.get(multiplier)));
				} 
			}
			return this;
		}

		public MonomialBuilder divNumer(Monomial first, Monomial other) 
		throws InvalidTypeException {
			Operator.typeCheck(Operator.DIV, first.type, other.type);
			this.type = first.type;
			if (first.scale.equals(other.scale)) {
				this.scale = (Simplex) this.scale.to(this.type);
			} else {
				this.scale = first.scale;
			}
			for (Entry<Primitive, Integer> e : first.rep.entrySet()) {
				final Primitive multiplier = e.getKey(); 
				final int powerFirst = e.getValue();
				final int powerGCD; //rather than calling gcd we calculate it here on-the-fly
				if (other.rep.containsKey(multiplier)) {
					final int powerOther = other.rep.get(multiplier);
					powerGCD = Math.min(powerFirst, powerOther); 
				} else {
					powerGCD = 0;
				}
				final int power = powerFirst - powerGCD;
				if (power > 0) {
					this.rep.put(multiplier, power);
				}
			}
			return this;
		}

		public MonomialBuilder divDenom(Monomial first, Monomial other) 
		throws InvalidTypeException {
			Operator.typeCheck(Operator.DIV, first.type, other.type);
			this.type = first.type;
			if (first.scale.equals(other.scale)) {
				this.scale = (Simplex) this.scale.to(this.type);
			} else {
				this.scale = other.scale;
			}
			this.rep.putAll(other.rep);
			for (Entry<Primitive, Integer> e : first.rep.entrySet()) {
				final Primitive key = e.getKey(); 
				final Integer valueFirst = e.getValue();
				final Integer valueGCD; //rather than calling gcd we calculate it here on-the-fly
				if (other.rep.containsKey(key)) {
					final Integer valueOther = other.rep.get(key);
					valueGCD = Math.min(valueFirst, valueOther); 
					final int valueDenom = valueOther - valueGCD;
					if (valueDenom == 0) {
						this.rep.remove(key);
					} else {
						this.rep.put(key, valueDenom);
					}
				}
			}
			return this;
		}

		public MonomialBuilder sqrtRoot(Monomial m) 
		throws InvalidTypeException {
			if (m.type != Type.DOUBLE) {
				throw new InvalidTypeException("Square root only accepts a double parameter.");
			}
			this.type = m.type;
			this.scale = (Simplex) this.scale.to(this.type);
			for (Entry<Primitive, Integer> e : m.rep.entrySet()) {
				final Primitive key = e.getKey();
				final int pow = e.getValue();
				if (pow / 2 != 0) {
					this.rep.put(key, pow / 2);
				}
			}
			return this;
		}

		public MonomialBuilder sqrtNonRoot(Monomial m) 
		throws InvalidTypeException {
			if (m.type != Type.DOUBLE) {
				throw new InvalidTypeException("Square root accepts only a double parameter.");
			}
			this.type = m.type;
			this.scale = m.scale;
			for (Entry<Primitive, Integer> e : m.rep.entrySet()) {
				final Primitive key = e.getKey();
				final int pow = e.getValue();
				if (pow % 2 != 0) {
					this.rep.put(key, pow & 2);
				}
			}
			return this;
		}

		private class RepBuilder implements PrimitiveVisitor {			
			public RepBuilder() { }

			@Override
			public void visitAny(Any x) {
				incExponent(x, 1);
			}

			@Override
			public void visitExpression(Expression e) {
				if (e.getOperator() == Operator.MUL) {
					try {
						e.getFirstOperand().accept(this);
						e.getSecondOperand().accept(this);
					} catch (RuntimeException exc) {
						throw exc;
					} catch (Exception exc) {
						//this should never happen
						throw new UnexpectedInternalException(exc);
					}
				} else if (e.getOperator() == Operator.NEG) {
					try {
						scale = (Simplex) scale.neg();
						e.getOperand().accept(this);
					} catch (RuntimeException exc) {
						throw exc;
					} catch (Exception exc) {
						//this should never happen
						throw new UnexpectedInternalException(exc);
					}
				} else {
					incExponent(e, 1);
				}
			}

			@Override
			public void visitFunctionApplication(FunctionApplication x) {
				incExponent(x, 1);
			}

			@Override
			public void visitWideningConversion(WideningConversion x) {
				incExponent(x, 1);
			}

			@Override
			public void visitNarrowingConversion(NarrowingConversion x) {
				incExponent(x, 1);
			}

			@Override
			public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
				incExponent(s, 1);
			}

			@Override
			public void visitSimplex(Simplex x) {
				try {
					scale = (Simplex) scale.mul(x);
				} catch (RuntimeException exc) {
					throw exc;
				} catch (Exception exc) {
					//this should never happen
					throw new UnexpectedInternalException(exc);
				}
			}

			@Override
			public void visitTerm(Term x) {
				incExponent(x, 1);
			}
		};
	};

	public char getType() {
		return this.type;
	}

	public Map<Primitive, Integer> representation() {
		return this.rep;
	}	

	public boolean isNumber() {
		return (this.rep.size() == 0);
	}

	public boolean isZeroOne(boolean zero) {
		return (this.isNumber() && this.scale.isZeroOne(zero));
	}

	public Simplex getMultiplier() {
		return this.scale;
	}

	/**
	 * Returns the base of this {@link Monomial}.
	 * 
	 * @return a {@link Monomial}, identical to this
	 *         except for the scale which is equal to 1.
	 * @throws InvalidTypeException  
	 */
	public Monomial createBase() {
		return new MonomialBuilder(this.calc, makeRep()).base(this).make();
	}

	private Primitive makePrimitive(boolean normalize, Set<Primitive> bases) {
		Primitive retVal = this.scale;
		for (Primitive base : bases) {
			final Primitive baseNew = (normalize ? this.calc.applyRewriters(base, new RewriterNormalize()) : base);
			final int exp = this.rep.get(base);
			for (int i = 1; i <= exp; ++i) {
				if ((retVal instanceof Simplex) && ((Simplex) retVal).isZeroOne(false)) {
					retVal = baseNew;
				} else {
					try {
						retVal = Expression.makeExpressionBinary(this.calc, retVal, Operator.MUL, baseNew);
					} catch (InvalidOperandException | InvalidOperatorException | InvalidTypeException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
				}
			}
		}
		return retVal;
	}

	private volatile Primitive toPrimitive;
	
	public Primitive toPrimitive() {
		Primitive retVal = this.toPrimitive;
		if (retVal == null) {
			if (this.scale.isZeroOne(true)) {
				this.toPrimitive = this.scale;
			} else {
				this.toPrimitive = makePrimitive(false, this.rep.keySet());
			}
			retVal = this.toPrimitive;
		}
		return retVal;
	}
	
	private volatile Primitive toPrimitiveNormalized;

	public Primitive toPrimitiveNormalized() {
		Primitive retVal = this.toPrimitiveNormalized;
		if (retVal == null) {
			if (this.scale.isZeroOne(true)) {
				this.toPrimitiveNormalized = this.scale;
			} else {
				final TreeSet<Primitive> keysSorted = new TreeSet<Primitive>(this.comparatorPrimitive);
				keysSorted.addAll(this.rep.keySet());
				this.toPrimitiveNormalized = makePrimitive(true, keysSorted);
			}
			retVal = this.toPrimitiveNormalized;
		}
		return retVal;
	}

	public Monomial mul(Monomial other) 
	throws InvalidTypeException {
		return new MonomialBuilder(this.calc, makeRep()).mul(this, other).make();
	}

	public Monomial gcd(Monomial other)
	throws InvalidTypeException {
		return new MonomialBuilder(this.calc, makeRep()).gcd(this, other).make();
	}

	public Monomial[] div(Monomial other) 
	throws InvalidTypeException {
		final Monomial numer = new MonomialBuilder(this.calc, makeRep()).divNumer(this, other).make();
		final Monomial denom = new MonomialBuilder(this.calc, makeRep()).divDenom(this, other).make();
		return new Monomial[] { numer, denom };
	}

	public Monomial[] sqrt()
	throws InvalidTypeException {
		final Monomial sqrt = new MonomialBuilder(this.calc, makeRep()).sqrtRoot(this).make();
		final Monomial etc = new MonomialBuilder(this.calc, makeRep()).sqrtNonRoot(this).make();
		return new Monomial[] { sqrt, etc };
	}

	@Override
	public int compareTo(Monomial o) {
		try {
			return this.comparatorPrimitive.compare(this.toPrimitive(), o.toPrimitive());
		} catch (UnexpectedInternalException e) {
			throw new RuntimeException(e); //TODO ugly!
		}
		//alternative: return this.comparatorPrimitive.compare(this.toPrimitiveNormalized(), o.toPrimitiveNormalized());
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
		final Monomial other = (Monomial) obj;
		if (scale == null) {
			if (other.scale != null) {
				return false;
			}
		} else if (!scale.equals(other.scale)) {
			return false;
		}
		if (rep == null) {
			if (other.rep != null) {
				return false;
			}
		} else if (!rep.equals(other.rep)) {
			return false;
		}
		return true;
		//alternative: return (compareTo(other) == 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((scale == null) ? 0 : scale.hashCode());
		result = prime * result + ((rep == null) ? 0 : rep.hashCode());
		return result;
		//alternative: return toPrimitiveNormalized().hashCode();
	}

	@Override
	public String toString() {
	    final StringBuilder sb = new StringBuilder();
		sb.append(this.scale.toString());
		for (Entry<Primitive, Integer> e : this.rep.entrySet()) {
			sb.append("*");
			sb.append(e.getKey().toString());
			sb.append("^");
			sb.append(e.getValue().toString());
		}
		return sb.toString();
	}

}
