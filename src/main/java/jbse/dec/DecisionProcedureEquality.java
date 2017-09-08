package jbse.dec;

import java.util.LinkedHashMap;

import jbse.bc.ClassHierarchy;
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


/**
 * A poor man decision procedure for equalities and inequalities 
 * (not really congruence closure).
 * 
 * @author Pietro Braione
 *
 */
public final class DecisionProcedureEquality extends DecisionProcedureChainOfResponsibility {
	private final Partition equivalence = new Partition();

	public DecisionProcedureEquality(DecisionProcedure component, CalculatorRewriting calc) {
		super(component, calc);
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
	protected boolean isSatLocal(ClassHierarchy hier, Expression exp, Expression expSimpl) 
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

	private static class PrimitiveVisitorEquality implements PrimitiveVisitor {
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
										((Simplex) toCheckFirstFirst.neg()).isZeroOne(false)) {
									this.first = toCheckFirstSecond;
									this.second = toCheckSecond;
									return;
								} else if (toCheckFirstSecond instanceof Simplex &&
										((Simplex) toCheckFirstSecond.neg()).isZeroOne(false)) {
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
										((Simplex) toCheckSecondFirst.neg()).isZeroOne(false)) {
									this.first = toCheckFirst;
									this.second = toCheckSecondSecond;
									return;
								} else if (toCheckSecondSecond instanceof Simplex &&
										((Simplex) toCheckSecondSecond.neg()).isZeroOne(false)) {
									this.first = toCheckFirst;
									this.second = toCheckSecondFirst;
									return;						
								}
							}
						}
						//manages a + b == 0 (by default)
						this.first = toCheckFirst.neg();
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
		public void visitFunctionApplication(FunctionApplication x) {
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
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s)
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
		protected void rewriteFunctionApplication(FunctionApplication x)
		throws NoResultException {
			final Primitive xEq = equivalence.find(x);
			if (x.equals(xEq)) {
				super.rewriteFunctionApplication(x);
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
	
	/**
	 * Union-find partition of primitives.
	 * 
	 * @author Pietro Braione
	 */
	private static class Partition {
		private final LinkedHashMap<Primitive, PartitionNode> nodes = new LinkedHashMap<Primitive, PartitionNode>();
		
		void union(Primitive elemFirst, Primitive elemSecond) {
			if (elemFirst.equals(elemSecond)) {
				return;
			}
			final int firstLength = elemFirst.toString().length();
			final int secondLength = elemSecond.toString().length();
			final boolean firstShorter = (firstLength < secondLength);
			final PartitionNode partitionFirst = (firstShorter ? rootNode(elemFirst) : rootNode(elemSecond));
			final PartitionNode partitionSecond = (firstShorter ? rootNode(elemSecond) : rootNode(elemFirst));
			final PartitionNode partitionLower, partitionHigher; 
			if (partitionFirst.rank < partitionSecond.rank) {
				partitionLower = partitionFirst;
				partitionHigher = partitionSecond;
			} else { 
				partitionLower = partitionSecond;
				partitionHigher = partitionFirst;
				if (partitionLower.rank == partitionHigher.rank) {
					++partitionHigher.rank;
				}
			}
			partitionLower.parent = partitionHigher;
		}
		
		Primitive find (Primitive elem) {
			PartitionNode node = this.nodes.get(elem);
			if (node == null) {
				return elem;
			}
			return findRootAndCompress(node).element;
		}
		
		/* aggressive closure, seemingly offers no advantage
		private static class PrimitivePair {
			Primitive oldValue;
			Primitive newValue;
			PrimitivePair(Primitive oldValue, Primitive newValue) {
				this.oldValue = oldValue;
				this.newValue = newValue;
			}
		}
		
		void close(CalculatorRewriting calc, Rewriter[] rewriters) {
			final ArrayList<PrimitivePair> normalized = new ArrayList<PrimitivePair>();
			for (final Primitive oldValue : this.nodes.keySet()) {
				final Primitive newValue = calc.applyRewriters(oldValue, rewriters); 		//TODO may cause divergence!!!!!!!!!
				normalized.add(new PrimitivePair(oldValue, newValue));
			}
			for (final PrimitivePair p : normalized) {
				this.union(p.oldValue, p.newValue);
			}
		}*/
		
		void reset() {
			this.nodes.clear();
		}

		private PartitionNode findRootAndCompress(PartitionNode node) {
			if (node.parent != node) {
				node.parent = findRootAndCompress(node.parent);
			}
			return node.parent;
		}
		
		private PartitionNode rootNode(Primitive elem) {
			PartitionNode elemNode = this.nodes.get(elem);
			if (elemNode == null) {
				elemNode = new PartitionNode(elem);
				this.nodes.put(elem, elemNode);
			}
			return findRootAndCompress(elemNode);
		}
		
		private static class PartitionNode {
			final Primitive element;
			PartitionNode parent;
			int rank;
			
			PartitionNode(Primitive element) {
				this.element = element;
				this.parent = this;
				this.rank = 0;
			}
			
			@Override
			public String toString() {
				return ">" + this.parent.element +"(r" + this.rank + ")";
			}
		}
	}
}
