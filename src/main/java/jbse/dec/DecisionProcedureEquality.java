package jbse.dec;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.ClauseAssume;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.Rewriter;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;
import jbse.val.exc.NoResultException;

/**
 * A poor man decision procedure for equalities and inequalities 
 * (not really congruence closure).
 * 
 * @author Pietro Braione
 *
 */
public final class DecisionProcedureEquality extends DecisionProcedureChainOfResponsibility {
	private final Partition<Primitive> equivalence = new Partition<>();

	public DecisionProcedureEquality(DecisionProcedure component) 
	throws InvalidInputException {
		super(component);
		this.rewriters = new Rewriter[] { new RewriterUnify() }; //explicit assignment: no constructor call is allowed before super()
	}

	@Override
	protected void pushAssumptionLocal(ClauseAssume c) {
		final Primitive p = c.getCondition();
		final PrimitiveVisitorEquality vEq = new PrimitiveVisitorEquality();
		try {
			p.accept(vEq);
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
		if (vEq.isEquality && !vEq.negated) {
			this.equivalence.union(vEq.first, vEq.second);
			//(see Partition.close, commented out) this.equivalence.close(this.calc, this.rewriters);
		}
	}

	@Override
	protected void clearAssumptionsLocal() {
		this.equivalence.reset();
	}

	@Override
	protected boolean isSatLocal(Expression exp, Expression expSimpl) 
	throws DecisionException {
		final PrimitiveVisitorEquality vEq = new PrimitiveVisitorEquality();
		try {
			expSimpl.accept(vEq);
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
		if (vEq.isEquality && vEq.first.equals(vEq.second)) {
			return !vEq.negated;
		} else {
			//it is still possible that the original not-simplified 
			//expression works better.
			try {
				exp.accept(vEq);
			} catch (Exception e) {
				throw new UnexpectedInternalException();
			}
			if (vEq.isEquality && vEq.first.equals(vEq.second)) {
				return !vEq.negated;
			} else {
				return true; //out of the theory
			}
		}
	}

	private class PrimitiveVisitorEquality implements PrimitiveVisitor {
		boolean isEquality, negated;
		Primitive first, second;

		@Override
		public void visitAny(Any x) throws Exception {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitExpression(Expression e) throws Exception {
			boolean twist = false;
			final Expression expE;
			if (e.getOperator() == Operator.NOT) {
				Primitive p = e.getOperand();
				if (p instanceof Expression) {
					expE = (Expression) p;
					twist = true;
				} else {
					expE = e;
				}
			} else {
				expE = e;
			}
			final Operator operator = expE.getOperator();
			if (operator == Operator.EQ || operator == Operator.NE) {
				this.isEquality = true;
				this.negated = (operator == Operator.NE);
				if (twist) {
					negated = ! negated;
				}

				final Primitive firstOperand = expE.getFirstOperand();
				final Primitive secondOperand = expE.getSecondOperand();
				final Primitive toCheck;
				if (firstOperand instanceof Simplex && 
						((Simplex) firstOperand).isZeroOne(true)) {
					toCheck = secondOperand;
				} else if (secondOperand instanceof Simplex && 
						((Simplex) secondOperand).isZeroOne(true)) {
					toCheck = firstOperand;
				} else {
					this.first = firstOperand;
					this.second = secondOperand;
					return;
				}
				if (toCheck instanceof Expression) {
					Expression toCheckExp = (Expression) toCheck;
					if (toCheckExp.getOperator() == Operator.SUB) {
						this.first = toCheckExp.getFirstOperand();
						this.second = toCheckExp.getSecondOperand();
						return;
					} else if (toCheckExp.getOperator() == Operator.ADD) {
						final Primitive toCheckFirst = toCheckExp.getFirstOperand();
						final Primitive toCheckSecond = toCheckExp.getSecondOperand();
						//manages -a + b == 0, -1 * a + b == 0, a * -1 + b == 0
						if (toCheckFirst instanceof Expression) {
							final Expression toCheckFirstExp = (Expression) toCheckFirst;
							if (toCheckFirstExp.getOperator() == Operator.NEG) {
								this.first = toCheckFirstExp.getOperand();
								this.second = toCheckSecond;
								return;
							} else if (toCheckFirstExp.getOperator() == Operator.MUL) {
								final Primitive toCheckFirstFirst = toCheckFirstExp.getFirstOperand();
								final Primitive toCheckFirstSecond = toCheckFirstExp.getSecondOperand();
								if (toCheckFirstFirst instanceof Simplex &&
								    ((Simplex) DecisionProcedureEquality.this.calc.push(toCheckFirstFirst).neg().pop()).isZeroOne(false)) {
									this.first = toCheckFirstSecond;
									this.second = toCheckSecond;
									return;
								} else if (toCheckFirstSecond instanceof Simplex &&
								           ((Simplex) DecisionProcedureEquality.this.calc.push(toCheckFirstSecond).neg().pop()).isZeroOne(false)) {
									this.first = toCheckFirstFirst;
									this.second = toCheckSecond;
									return;
								} 
							}
						}
						//manages a + -b == 0, a + -1 * b == 0, a + b * -1 == 0
						if (toCheckSecond instanceof Expression) {
							final Expression toCheckSecondExp = (Expression) toCheckSecond;
							if (toCheckSecondExp.getOperator() == Operator.NEG) {
								this.first = toCheckFirst;
								this.second = toCheckSecondExp.getOperand();
								return;
							} else if (toCheckSecondExp.getOperator() == Operator.MUL) {
								final Primitive toCheckSecondFirst = toCheckSecondExp.getFirstOperand();
								final Primitive toCheckSecondSecond = toCheckSecondExp.getSecondOperand();
								if (toCheckSecondFirst instanceof Simplex &&
										((Simplex) DecisionProcedureEquality.this.calc.push(toCheckSecondFirst).neg().pop()).isZeroOne(false)) {
									this.first = toCheckFirst;
									this.second = toCheckSecondSecond;
									return;
								} else if (toCheckSecondSecond instanceof Simplex &&
										((Simplex) DecisionProcedureEquality.this.calc.push(toCheckSecondSecond).neg().pop()).isZeroOne(false)) {
									this.first = toCheckFirst;
									this.second = toCheckSecondFirst;
									return;						
								}
							}
						}
						//manages a + b == 0 (by default)
						this.first = DecisionProcedureEquality.this.calc.push(toCheckFirst).neg().pop();
						this.second = toCheckSecond;
						return;
					}
				}
				//default
				this.first = firstOperand;
				this.second = secondOperand;
			} else {
				this.isEquality = false;
				this.first = this.second = null;
			}
		}

		@Override
		public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitWideningConversion(WideningConversion x) {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s)
		throws Exception {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitSimplex(Simplex x) throws Exception {
			this.isEquality = false;
			this.first = this.second = null;
		}

		@Override
		public void visitTerm(Term x) throws Exception {
			this.isEquality = false;
			this.first = this.second = null;
		}
	}

	private class RewriterUnify extends Rewriter {
		public RewriterUnify() { }

		@Override
		protected void rewriteExpression(Expression x) 
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewriteExpression(x);
				final Primitive xSub = this.getResult();
				if (x.equals(xSub)) {
					return;
				} else {
					setResult(rewrite(xSub));
				}
			} else {
				setResult(rewrite(xEq));
			}
		}

		@Override
		protected void rewritePrimitiveSymbolicApply(PrimitiveSymbolicApply x)
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewritePrimitiveSymbolicApply(x);
				final Primitive xSub = this.getResult();
				if (x.equals(xSub)) {
					return;
				} else {
					setResult(rewrite(xSub));
				}
			} else {
				setResult(rewrite(xEq));
			}
		}
		
		@Override
		protected void rewriteWideningConversion(WideningConversion x)
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewriteWideningConversion(x);
				final Primitive xSub = this.getResult();
				if (x.equals(xSub)) {
					return;
				} else {
					setResult(rewrite(xSub));
				}
			} else {
				setResult(rewrite(xEq));
			}
		}
		
		@Override
		protected void rewriteNarrowingConversion(NarrowingConversion x)
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewriteNarrowingConversion(x);
				final Primitive xSub = this.getResult();
				if (x.equals(xSub)) {
					return;
				} else {
					setResult(rewrite(xSub));
				}
			} else {
				setResult(rewrite(xEq));
			}
		}

		@Override
		protected void rewritePrimitiveSymbolic(PrimitiveSymbolic x)
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewritePrimitiveSymbolic(x);
			} else {
				setResult(rewrite(xEq));
			}
		}

		@Override
		protected void rewriteTerm(Term x) 
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewriteTerm(x);
			} else {
				setResult(rewrite(xEq));
			}
		}
	}
}
