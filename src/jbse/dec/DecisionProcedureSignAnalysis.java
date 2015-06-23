package jbse.dec;

import java.util.HashMap;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.ClauseAssume;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.Rewriter;
import jbse.rewr.exc.NoResultException;
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
 * Decides expressions with shape {@code expr rel_op number} or
 * {@code number rel_op expr}. In the cases it may infer something
 * on the sign of expr from the sign of it subexpressions it
 * may detect whether the predicate contradicts this fact.
 * 
 * @author Pietro Braione
 *
 */
public final class DecisionProcedureSignAnalysis extends DecisionProcedureChainOfResponsibility {
	/**
	 * The abstract lattice of sign predicates.
	 * 
	 * @author Pietro Braione
	 *
	 */
	private enum SignPredicate {
		/** No value (bottom, false). */
		BOT {
			@Override
			SignPredicate mul(SignPredicate r) {
				return BOT;
			}

			@Override
			SignPredicate add(SignPredicate r) {
				return BOT;
			}

			@Override
			SignPredicate neg() {
				return BOT;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				return BOT;
			}

			@Override
			SignPredicate not() {
				return UNK;
			}
		},

		/** 
		 * Equal to zero.
		 */
		EQ {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT) {
					return r.mul(this);
				}
				return EQ;
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT) {
					return r.add(this);
				}
				return r;
			}

			@Override
			SignPredicate neg() {
				return EQ;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT) {
					return r.and(this);
				}
				if (r == EQ || r == GE || r == LE || r == UNK) {
					return EQ;
				}
				return BOT; //r == GT || r == LT || r == NE
			}

			@Override
			SignPredicate not() {
				return NE;
			}
		},
		
		/** 
		 * Any value (top, true).
		 */
		UNK {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT || r == EQ) {
					return r.mul(this);
				}
				return UNK;
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ) {
					return r.add(this);
				}
				return UNK;
			}

			@Override
			SignPredicate neg() {
				return UNK;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ) {
					return r.and(this);
				}
				if (r == UNK) {
					return UNK;
				}
				return r;
			}

			@Override
			SignPredicate not() {
				return BOT;
			}
		},
		
		/** 
		 * Greater or equal to zero.
		 */
		GE {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK) {
					return r.mul(this);
				}
				if (r == GE || r == GT) {
					return GE;
				}
				if (r == NE) {
					return UNK;
				}
				return LE; //r == LE || r == LT
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK) {
					return r.add(this);
				}
				if (r == GE || r == GT) {
					return r;
				}
				return UNK; //r == LE || r == LT || r == NE
			}

			@Override
			SignPredicate neg() {
				return LE;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK) {
					return r.and(this);
				}
				if (r == GE) {
					return GE;
				}
				if (r == NE || r == GT) {
					return GT;
				}
				if (r == LE) {
					return EQ;
				}
				return BOT; //r == LT
			}

			@Override
			SignPredicate not() {
				return LT;
			}
		},
		
		/** 
		 * Greater than zero.
		 */
		GT {
			@Override
			SignPredicate mul(SignPredicate r) {
				return r;
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE) {
					return r.add(this);
				}
				if (r == GT) {
					return GT;
				}
				return UNK; //r == LE || r == LT || r == NE
			}

			@Override
			SignPredicate neg() {
				return LT;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE) {
					return r.and(this);
				}
				if (r == GT || r == NE) {
					return GT;
				}
				return BOT; //r == LE || r == LT
			}

			@Override
			SignPredicate not() {
				return LE;
			}
		}, 
		
		/** 
		 * Less or equal to zero.
		 */
		LE {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT) {
					return r.mul(this);
				}
				if (r == LE || r == LT) {
					return GE;
				}
				return UNK; //r == NE
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT) {
					return r.add(this);
				}
				if (r == LE || r == LT) {
					return r;
				}
				return UNK; //r == NE
			}

			@Override
			SignPredicate neg() {
				return GE;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT) {
					return r.and(this);
				}
				if (r == LE) {
					return LE;
				}
				return LT; //r == LT || r == NE
			}

			@Override
			SignPredicate not() {
				return GT;
			}
		},
		
		/** 
		 * Less than zero.
		 */
		LT {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE) {
					return r.mul(this);
				}
				if (r == NE) {
					return NE;
				}
				return GT; //r == LT;
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE) {
					return r.add(this);
				}
				if (r == LT) {
					return LT;
				}
				return UNK; //r == NE
			}

			@Override
			SignPredicate neg() {
				return GT;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE) {
					return r.and(this);
				}
				return LT; //r == LT || r == NE
			}

			@Override
			SignPredicate not() {
				return GE;
			}
		},
		
		/** 
		 * Not equal to zero.
		 */
		NE {
			@Override
			SignPredicate mul(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE || r == LT) {
					return r.mul(this);
				}
				return NE; //r == NE
			}

			@Override
			SignPredicate add(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE || r == LT) {
					return r.add(this);
				}
				return UNK; //r == NE
			}

			@Override
			SignPredicate neg() {
				return NE;
			}

			@Override
			SignPredicate and(SignPredicate r) {
				if (r == BOT || r == EQ || r == UNK || r == GE || r == GT || r == LE || r == LT) {
					return r.and(this);
				}
				return NE; //r == NE
			}

			@Override
			SignPredicate not() {
				return EQ;
			}
		};
		
		/**
		 * Abstracts the arithmetic multiplication operation
		 * in the sign abstract domain.
		 * 
		 * @param r a {@link SignPredicate}.
		 * @return the {@link SignPredicate} representing the
		 *         best (less general) approximation of the set of values 
		 *         obtained by multiplying a value in {@code this}
		 *         to a value in {@code r}. 
		 */
		abstract SignPredicate mul(SignPredicate r);
		
		/**
		 * Abstracts the arithmetic sum operation
		 * in the sign abstract domain.
		 * 
		 * @param r a {@link SignPredicate}.
		 * @return the {@link SignPredicate} representing the
		 *         best (less general) approximation of the set of values 
		 *         obtained by adding a value in {@code this}
		 *         to a value in {@code r}. 
		 */
		abstract SignPredicate add(SignPredicate r);
		
		/**
		 * Abstracts the arithmetic negation operation
		 * in the sign abstract domain.
		 * 
		 * @return the {@link SignPredicate} representing the
		 *         best (less general) approximation of the set of values 
		 *         obtained by negating a value in {@code this}. 
		 */
		abstract SignPredicate neg();

		/**
		 * Returns the greatest lower bound (logical and) of this {@link SignPredicate} 
		 * and another one. The greatest lower bound of two {@link SignPredicate}s 
		 * is the best approximation as a {@link SignPredicate} of the intersection 
		 * of the sets of numbers they represent.
		 * 
		 * @param r a {@link SignPredicate}.
		 * @return a {@link SignPredicate} which is the greatest among
		 *         all the predicates contained both in {@code this} and
		 *         {@code r}. As examples,  
		 *         {@link #LE}{@code .and(}{@link #GE}{@code ) == }{@link #EQ},
		 *         and {@link #LT}{@code .and(}{@link #GT}{@code ) == }{@link #BOT}.
		 *         
		 */
		abstract SignPredicate and(SignPredicate r);
		
		/**
		 * Returns the complement (logical negation) of this {@link SignPredicate}.
		 * 
		 * @return a {@link SignPredicate} which is the logical negation of 
		 *         {@code this}. As examples,  
		 *         {@link #LE}{@code .not() == }{@link #GT},
		 *         and {@link #NE}{@code .not() == }{@link #EQ}.
		 *         
		 */
		abstract SignPredicate not();

		/**
		 * Returns the least upper bound (logical or) of this {@link SignPredicate} 
		 * and another one. The least upper bound of two {@link SignPredicate}s 
		 * is the best approximation as a {@link SignPredicate} of the union of the 
		 * sets of numbers they represent.
		 * 
		 * @param r a {@link SignPredicate}.
		 * @return a {@link SignPredicate} which is the smallest among
		 *         all the predicates containing both {@code this} and
		 *         {@code r}. As examples,  
		 *         {@link #LT}{@code .or(}{@link #EQ}{@code ) == }{@link #LE},
		 *         and {@link #LT}{@code .or(}{@link #GE}{@code ) == }{@link #UNK}.
		 *         
		 */
		final SignPredicate or(SignPredicate r) {
			return this.not().and(r.not()).not(); //De Morgan
		}
		
		/**
		 * Checks whether this {@link SignPredicate} contradicts
		 * another one.
		 * 
		 * @param r a {@link SignPredicate}.
		 * @return {@code true} iff {@code this.}{@link #and} {@code (r) == }
		 *         {@link #BOT}, i.e., iff {@code this} and {@code r}  
		 *         are disjoint. 
		 */
		final boolean contradicts(SignPredicate r) {
			return (this.and(r) == BOT); 
		}
	}
	
	/** Caches the {@link SignPredicate}s of all the discovered path predicates. */
	private HashMap<Primitive, SignPredicate> preds = new HashMap<Primitive, SignPredicate>();

	/**
	 * Constructor.
	 * 
	 * @param next The next {@link DecisionProcedure} in the 
	 *        Chain Of Responsibility.
	 * @param calc a {@link CalculatorRewriting}.
	 */
	public DecisionProcedureSignAnalysis(DecisionProcedure next, CalculatorRewriting calc) {
		super(next, calc);
		this.rewriters = new Rewriter[] { new RewriterSimplifyTrivialExpressions() }; //explicit assignment: no constructor call is allowed before super()
	}

	@Override
	protected void pushAssumptionLocal(ClauseAssume c) {
		final Primitive p = c.getCondition();
		if (p instanceof Expression) {
			final Expression exp = (Expression) p;
			if (isTrivial(exp)) {
				final Primitive operand = getOperand(exp);
				final SignPredicate predicateOperand = fetch(operand);
				final SignPredicate predicateRange = bestApproxRange(exp);
				final SignPredicate bestPredicate = predicateOperand.and(predicateRange);
				this.preds.put(operand, bestPredicate);
			}
		}
	}
		
	@Override
	protected void clearAssumptionsLocal() {
		this.preds.clear();
	}
	
	@Override
	protected boolean isSatLocal(ClassHierarchy hier, Expression exp, Expression expSimpl) 
	throws DecisionException {
		if (isTrivial(expSimpl)) {
			final SignPredicate predicateOperand = deduceSignPredicate(getOperand(expSimpl));
			final SignPredicate predicateRange = bestApproxRange(expSimpl);
			if (predicateOperand.contradicts(predicateRange)) {
				return false;
			}
		}
		return true; //out of the theory
	}
	

	/**
	 * Determines whether an {@link Expression} is "trivial", 
	 * i.e., it has form {@code exp rel_op number} or {@code number rel_op exp}.
	 * 
	 * @param exp an {@link Expression}.
	 * @return {@code true} iff {@code exp} has not shape
	 *         {@code subexp rel_op number} or {@code number rel_op subexp}.
	 */
	private static boolean isTrivial(Expression exp) {
		final Operator operator = exp.getOperator();
		if (operator != Operator.EQ &&
			operator != Operator.NE &&
			operator != Operator.LE &&
			operator != Operator.LT &&
			operator != Operator.GE &&
			operator != Operator.GT) {
			return false;
		}
		final Primitive operandFirst = exp.getFirstOperand();
		final Primitive operandSecond = exp.getSecondOperand();
		if (!((operandFirst instanceof Simplex) || (operandSecond instanceof Simplex))) {
			return false;
		}
		return true;
	}
	
	/**
	 * Returns the {@code subexp} of an expression in the form
	 * {@code subexp rel_op number} or {@code number rel_op subexp}.  
	 * 
	 * @param exp an {@link Expression}. It must be in form 
	 *        {@code subexp rel_op number} or {@code number rel_op subexp}, 
	 *        otherwise the method might not behave as expected.
	 * @return a {@link Primitive}, the {@code subexp}.
	 */
	private static Primitive getOperand(Expression exp) {
		final Primitive operandFirst = exp.getFirstOperand();
		final Primitive operandSecond = exp.getSecondOperand();
		if (operandFirst instanceof Simplex) {
			return operandSecond;
		} else {
			return operandFirst;
		}
	}
	
	/**
	 * Returns the {@code rel_op} of an expression in the form
	 * {@code subexp rel_op number} or {@code number rel_op subexp}, 
	 * suitably twisted if the expression has the second form.
	 * 
	 * @param exp an {@link Expression}. It must be in form 
	 *        {@code subexp rel_op number} or {@code number rel_op subexp}, 
	 *        otherwise the method might not behave as expected.
	 * @return an {@link Operator}, either {@code rel_op} or 
	 *         {@code rel_op.}{@link Operator#twist() twist()} according to 
	 *         whether {@code exp} is either in form 
	 *         {@code subexp rel_op number} or {@code number rel_op subexp}.
	 */
	private static Operator getOperator(Expression exp) {
		final Primitive operandSecond = exp.getSecondOperand();
		if (operandSecond instanceof Simplex) {
			return exp.getOperator();
		} else {
			return exp.getOperator().twist();
		}
	}

	/**
	 * Returns the {@code number} of an expression in the form
	 * {@code subexp rel_op number} or {@code number rel_op subexp}.  
	 * 
	 * @param exp an {@link Expression}. It must be in form 
	 *        {@code subexp rel_op number} or {@code number rel_op subexp}, 
	 *        otherwise the method might not behave as expected.
	 * @return a {@link Simplex}, the {@code number}.
	 */
	private static Simplex getNumber(Expression exp) {
		final Primitive operandFirst = exp.getFirstOperand();
		final Primitive operandSecond = exp.getSecondOperand();
		if (operandFirst instanceof Simplex) {
			return (Simplex) operandFirst;
		} else {
			return (Simplex) operandSecond;
		}
	}
	
	private SignPredicate fetch(Primitive x) {
		SignPredicate retVal = this.preds.get(x);
		if (retVal == null) {
			if (x instanceof Expression) {
				final Primitive xNeg;
				try {
					xNeg = x.neg();
				} catch (InvalidTypeException e) {
					//TODO blame the caller
					throw new UnexpectedInternalException(e);
				}
				retVal = this.preds.get(xNeg);
				if (retVal == null) {
					retVal = SignPredicate.UNK;
				} else {
					retVal = retVal.neg();
				}
			} else {
				retVal = SignPredicate.UNK;
			}
		}
		return retVal;
	}

	/**
	 * Deduces the sign of a {@link Primitive}.
	 * 
	 * @param p a {@link Primitive}. It must have integral or floating type.
	 * @return a {@link SignPredicate}, the best it can deduce from the 
	 *         current path condition.
	 */
	private SignPredicate deduceSignPredicate(Primitive p) {
		final SignPredicatePrimitiveVisitor v = new SignPredicatePrimitiveVisitor();
		try {
			p.accept(v);
		} catch (Exception e) {
			throw new AssertionError(); //never happens
		}
		return v.result;
	}
	
	private static SignPredicate bestApproxRange(Expression exp) {
		final Operator operator = getOperator(exp);
		final Simplex num = getNumber(exp);
		final char type = getOperand(exp).getType();
		
		//if num is zero, the range is given by the operator
		if (num.isZeroOne(true)) {
			return operatorToSignPredicate(operator);
		}
		
		//if num is 1 or -1 and exp is integral, the range is shifted by one
		if (Type.isPrimitiveIntegral(type)) {
			try {
			    if (num.isZeroOne(false)) {
			        if (operator == Operator.EQ || operator == Operator.GE || operator == Operator.GT) {
			            return SignPredicate.GT;
			        } else if (operator == Operator.NE || operator == Operator.LE) {
			            return SignPredicate.UNK;
			        } else if (operator == Operator.LT) {
			            return SignPredicate.LE;
			        } else {
			            throw new UnexpectedInternalException("invalid operator " + operator.toString());
			        }
			    } else if (((Simplex) num.neg()).isZeroOne(false)) {
			        if (operator == Operator.EQ || operator == Operator.LE || operator == Operator.LT) {
			            return SignPredicate.LT;
			        } else if (operator == Operator.NE || operator == Operator.GE) {
			            return SignPredicate.UNK;
			        } else if (operator == Operator.GT) {
			            return SignPredicate.GE;
			        } else {
			            throw new UnexpectedInternalException("invalid operator " + operator.toString());
			        }
			    } //else fall through
			} catch (InvalidTypeException e) {
				//TODO blame the caller
				throw new UnexpectedInternalException(e);
			}
		} 
		
		//all other cases (i.e., num is neither 1 nor -1, or exp is not integral)
		final SignPredicate numSign = signOf(num);
		if (numSign == SignPredicate.GT) {
			if (operator == Operator.EQ || operator == Operator.GE || operator == Operator.GT) {
				return SignPredicate.GT;
			} else {
				return SignPredicate.UNK;
			}
		} else if (numSign == SignPredicate.LT) {
			if (operator == Operator.EQ || operator == Operator.LE || operator == Operator.LT) {
				return SignPredicate.LT;
			} else {
				return SignPredicate.UNK;
			}
		} else {
			throw new AssertionError();
		}
	}

	private static SignPredicate signOf(Simplex num) {
		if (num.isZeroOne(true)) {
			return SignPredicate.EQ;
		} else {
			Object o = num.getActualValue();
			final char type = num.getType();
			final boolean gt;
			if (Type.isPrimitiveIntegral(type)) {
				long v = ((Number) o).longValue();
				gt = (v > 0);
			} else { //Type.isPrimitiveFloating(type))
				double v = ((Number) o).doubleValue();
				gt = (v > 0);
			}
			return (gt ? SignPredicate.GT : SignPredicate.LT);
		}
	}

	private static SignPredicate operatorToSignPredicate(Operator operator) {
		switch (operator) {
		case EQ:
			return SignPredicate.EQ;
		case NE:
			return SignPredicate.NE;
		case LE:
			return SignPredicate.LE;
		case LT:
			return SignPredicate.LT;
		case GE:
			return SignPredicate.GE;
		case GT:
			return SignPredicate.GT;
		default:
			return null;
		}		
	}
	
	private class SignPredicatePrimitiveVisitor implements PrimitiveVisitor {
		/**
		 * A poor man sign detection for some sums of 
		 * trigonometric functions with sufficiently big constants.
		 * 
		 * @author Pietro Braione
		 *
		 */
		private class TrigPrimitiveVisitor implements PrimitiveVisitor {
			SignPredicate result;

			@Override
			public void visitExpression(Expression e) throws Exception {
				final Operator operator = e.getOperator();
				if (operator == Operator.ADD || operator == Operator.SUB) {
					final Primitive first = e.getFirstOperand();
					final Primitive second = e.getSecondOperand();
					final Simplex simplex;
					final FunctionApplication funAppl;
					if (first instanceof Simplex && second instanceof FunctionApplication) {
						simplex = (Simplex) first;
						funAppl = (FunctionApplication) second;
					} else if (second instanceof Simplex && first instanceof FunctionApplication) {
						simplex = (Simplex) second;
						funAppl = (FunctionApplication) first;
					} else {
						this.result = SignPredicate.UNK;
						return;
					}
					final double val;
					try {
						val = ((Number) simplex.getActualValue()).doubleValue();
					} catch (ClassCastException exc) {
						this.result = SignPredicate.UNK;
						return;
					}
					final String fun = funAppl.getOperator();
					if (fun.equals(FunctionApplication.ASIN) || fun.equals(FunctionApplication.ATAN)) {
						if (operator == Operator.ADD && val >= Math.PI/2) {
							this.result = SignPredicate.GE;
							return;
						} else if (operator == Operator.SUB && val <= Math.PI/2) {
							this.result = SignPredicate.LE;
							return;
						} //else fall through
					} // else fall through
				} //else fall through
				
				this.result = SignPredicate.UNK;
			}

			@Override
			public void visitAny(Any x) throws Exception { 
				this.result = SignPredicate.UNK;
			}

			@Override
			public void visitFunctionApplication(FunctionApplication x) { 
				this.result = SignPredicate.UNK;
			}

			@Override
			public void visitWideningConversion(WideningConversion x) throws Exception {
				x.getArg().accept(this); //same sign as its arg				
			}	

			@Override
			public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
				x.getArg().accept(this);
				this.result = this.result.or(SignPredicate.EQ); //narrowing can round to zero
			}

			@Override
			public void visitPrimitiveSymbolic(PrimitiveSymbolic s) { 
				this.result = SignPredicate.UNK;
			}

			@Override
			public void visitSimplex(Simplex x) { 
				this.result = SignPredicate.UNK;
			}

			@Override
			public void visitTerm(Term x) { 
				this.result = SignPredicate.UNK;
			}
		}
		
		SignPredicate result = null;
		TrigPrimitiveVisitor vTrig = new TrigPrimitiveVisitor();
		
		@Override
		public void visitAny(Any x) {
			this.result = SignPredicate.UNK;
		}

		@Override
		public void visitExpression(Expression e) {
			try {
				final SignPredicate infoFromArguments;
				final Operator operator = e.getOperator();
				if (operator == Operator.MUL || operator == Operator.DIV) {
					e.getFirstOperand().accept(this);
					final SignPredicate first = this.result;
					e.getSecondOperand().accept(this);
					final SignPredicate second = this.result;
					infoFromArguments = first.mul(second);
				} else if (operator == Operator.ADD || operator == Operator.SUB) {
					e.getFirstOperand().accept(this);
					final SignPredicate first = this.result;
					e.getSecondOperand().accept(this);
					final SignPredicate second = this.result;
					e.accept(this.vTrig);
					infoFromArguments = first.add(operator == Operator.ADD ? second : second.neg()).and(this.vTrig.result);
				} else if (operator == Operator.NEG) {
					e.getOperand().accept(this);
					final SignPredicate sign = this.result;
					infoFromArguments = sign.neg();
				}  else {
					infoFromArguments = SignPredicate.UNK;
				}
				final SignPredicate infoFromCurrentAssumptions = fetch(e);
				this.result = infoFromCurrentAssumptions.and(infoFromArguments);
			} catch (Exception exc) {
				throw new AssertionError(); //never happens
			}
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x) {
			final SignPredicate infoFromOperator;
			final String operator = x.getOperator();
			if (operator.equals(FunctionApplication.EXP)) {
				infoFromOperator = SignPredicate.GT; //does nothing, indeed
			} else if (operator.equals(FunctionApplication.ABS) || 
					operator.equals(FunctionApplication.SQRT) ||
					operator.equals(FunctionApplication.ACOS)) {
				infoFromOperator = SignPredicate.GE;
            } else if (operator.equals(FunctionApplication.POW)) {
                final Primitive arg = x.getArgs()[1];
                if (arg instanceof Simplex) {
                    final Simplex argSimplex = (Simplex) arg;
                    if (argSimplex.isZeroOne(true)) {
                        //exponent is zero
                        infoFromOperator = SignPredicate.GT;
                    } else {
                        final double exponent = ((Double) argSimplex.getActualValue()).doubleValue();
                        final double exponentCeil = Math.ceil(exponent);
                        if (exponent > 1 && exponent == exponentCeil && ((long) exponentCeil) % 2 == 0) {
                            //exponent is positive, integral and even
                            infoFromOperator = SignPredicate.GE;
                        } else {
                            infoFromOperator = SignPredicate.UNK;
                        }
                    }
                } else {
                    infoFromOperator = SignPredicate.UNK;
                }
			} else if (operator.equals(FunctionApplication.COS)) {
				final Primitive arg = x.getArgs()[0];
				if (arg instanceof FunctionApplication) {
					final String argOperator = ((FunctionApplication) arg).getOperator();
					if (argOperator.equals(FunctionApplication.SIN) ||
						argOperator.equals(FunctionApplication.COS) ||
						argOperator.equals(FunctionApplication.ATAN)) {
						//all these functions have value in (-PI/2, PI/2) where cos is positive
						infoFromOperator = SignPredicate.GT; 
					} else if (argOperator.equals(FunctionApplication.ASIN)) {
						//all these functions have value in [-PI/2, PI/2] where cos is nonnegative
						infoFromOperator = SignPredicate.GE;
					} else {
						infoFromOperator = SignPredicate.UNK;
					}
				} else {
					infoFromOperator = SignPredicate.UNK;
				}
			} else if (operator.equals(FunctionApplication.SIN)) {
				//sin(atan(x)) has the same sign of x
				final Primitive arg = x.getArgs()[0];
				if (arg instanceof FunctionApplication) {
					final FunctionApplication argFA = (FunctionApplication) arg;
					final String argOperator = argFA.getOperator();
					if (argOperator.equals(FunctionApplication.ATAN)) {
						final Primitive argArg = argFA.getArgs()[0];
						try {
							argArg.accept(this);
						} catch (Exception e) {
							//shall never happen
							throw new AssertionError();
						}
						infoFromOperator = this.result;
					} else {
						infoFromOperator = SignPredicate.UNK;
					}
				} else {
					infoFromOperator = SignPredicate.UNK;
				}
			} else {
				infoFromOperator = SignPredicate.UNK;
			}
			final SignPredicate infoFromCurrentAssumptions = fetch(x);
			this.result = infoFromCurrentAssumptions.and(infoFromOperator);
		}

		@Override
		public void visitWideningConversion(WideningConversion x)
		throws Exception {
			x.getArg().accept(this); //same sign as argument
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x)
		throws Exception {
			final Primitive arg = x.getArg();
			arg.accept(this);
			final SignPredicate argSign = this.result;
			if (argSign == SignPredicate.EQ) {
				return; //zero is always narrowed to zero
			}
			if (Type.isPrimitiveIntegral(arg.getType())) {
				//narrowing of integrals (to integrals) can lose sign 
				//and magnitude
				this.result = SignPredicate.UNK;  
			} else { //from is floating, 
				//all other narrowing conversion preserve sign but may
				//lose magnitude
				this.result = argSign.or(SignPredicate.EQ);
			}
		}

		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
			this.result = fetch(s);
		}

		@Override
		public void visitSimplex(Simplex x) {
			final SignPredicate sign = signOf(x);
			this.result = sign;
		}

		@Override
		public void visitTerm(Term x) {
			this.result = fetch(x);
		}
	}

	/**
	 * A {@link Rewriter} which simplifies every "trivial" comparison expression with form 
	 * {@code exp rel_op 0} or {@code 0 rel_op exp} by cancelling every multiplicative subterm 
	 * of {@code exp} with sign {@link SignPredicate#LT}, {@link SignPredicate#EQ}, or {@link SignPredicate#LT}. 
	 * It also cancels the few monotone function applications we are aware of.
	 * 
	 * @author Pietro Braione
	 */
	private class RewriterSimplifyTrivialExpressions extends Rewriter {
		public RewriterSimplifyTrivialExpressions() { }
		
		@Override
		protected void rewriteExpression(Expression x) throws NoResultException {
			final RewriterSimplifyTrivialExpressionsSubexpression r = 
					new RewriterSimplifyTrivialExpressionsSubexpression();
			if (isTrivial(x) && getNumber(x).isZeroOne(true)) {
				final Primitive operandRewr = calc.applyRewriters(getOperand(x), r);
				try {
					setResult(calc.applyBinary(operandRewr, (r.twist ? getOperator(x).twist() : getOperator(x)), getNumber(x)));
				} catch (InvalidOperatorException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				} catch (InvalidOperandException | InvalidTypeException e) {
					//this is likely due to invalid operand rewriting
					throw new NoResultException(e);
					//TODO distinguish the case where it is due to an internal error
				}
			} else {
				super.rewriteExpression(x);
			}
		}
		
		/**
		 * A {@link Rewriter} which cancels every multiplicative subterm 
		 * in an expression. 
		 * Its implementation is quick and dirty: it replaces subterms 
		 * with respectively -1, 0 and 1, and relies on other rewriters
		 * to do the rest of the work.
		 * 
		 * @author Pietro Braione
		 */
		private class RewriterSimplifyTrivialExpressionsSubexpression extends Rewriter {
			//should we twist the 
			boolean twist = false;
			
			public RewriterSimplifyTrivialExpressionsSubexpression() { }

			private boolean setResultBasedOnSign(Primitive x) 
			throws NoResultException {
				final char type = x.getType();
				final SignPredicate signPredicate = deduceSignPredicate(x);
				try {
					if (signPredicate == SignPredicate.GT) {
						setResult(calc.valInt(1).to(type));
						return true;
					}
					if (signPredicate == SignPredicate.EQ) {
						setResult(calc.valInt(0).to(type));
						return true;
					}
					if (signPredicate == SignPredicate.LT) {
						setResult(calc.valInt(-1).to(type));
						return true;
					}
				} catch (InvalidTypeException e) {
					//this should never happen
					throw new UnexpectedInternalException(e);
				}
				return false;
			}

			@Override
			protected void rewriteExpression(Expression x) throws NoResultException {
				final boolean done = setResultBasedOnSign(x);
				if (done) {
					return;
				}

				//else
				final Operator operator = x.getOperator();
				if (operator == Operator.MUL || operator == Operator.DIV) {
					super.rewriteExpression(x);
					//if we end up with an expression with shape K * subexpr,
					//K / subexpr, or subexpr / K, with K constant, then we 
					//remove K
					Primitive xSimpl = getResult();
					if (xSimpl instanceof Expression) {
						Expression xSimplE = (Expression) xSimpl;
						final Primitive firstOperandSimpl = xSimplE.getFirstOperand();
						final Primitive secondOperandSimpl = xSimplE.getSecondOperand();
						boolean firstIsConstant = (firstOperandSimpl instanceof Simplex);
						boolean secondIsConstant = (secondOperandSimpl instanceof Simplex);
						try {
							if (firstIsConstant) {
								final char type = firstOperandSimpl.getType();
								setResult(secondOperandSimpl);
								if (((Simplex) firstOperandSimpl).lt(calc.valInt(0).to(type)).surelyTrue()) {
									this.twist = ! this.twist; //switches
								}
							}
							if (secondIsConstant) {
								final char type = secondOperandSimpl.getType();
								setResult(firstOperandSimpl);
								if (((Simplex) secondOperandSimpl).lt(calc.valInt(0).to(type)).surelyTrue()) {
									this.twist = ! this.twist; //switches
								}
							}
						} catch (InvalidTypeException | InvalidOperandException e) {
							//this should never happen
							throw new UnexpectedInternalException(e);
						}
					} //else, we leave the result as is
				} else {
					setResult(x);
				}
			}

			@Override
			protected void rewriteFunctionApplication(FunctionApplication x)
			throws NoResultException {
				final boolean done = setResultBasedOnSign(x);
				if (done) {
					return;
				}

				//else
				final String operator = x.getOperator();
				if (operator.equals(FunctionApplication.ATAN) ||
					operator.equals(FunctionApplication.ASIN)) { //TODO is asin simplification problematic as asin(x) requires a bounded x?
					setResult(rewrite(x.getArgs()[0]));
				} else {
					setResult(x);
				}
			}

			@Override
			protected void rewritePrimitiveSymbolic(PrimitiveSymbolic x)
			throws NoResultException {
				final boolean done = setResultBasedOnSign(x);
				if (done) {
					return;
				}

				//else
				setResult(x);
			}

			@Override
			protected void rewriteSimplex(Simplex x) throws NoResultException {
				setResult(x);
			}

			@Override
			protected void rewriteTerm(Term x) 
			throws NoResultException {
				final boolean done = setResultBasedOnSign(x);
				if (done) {
					return;
				}

				//else
				setResult(x);
			}
		}
	}
}
