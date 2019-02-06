package jbse.val;


public interface PrimitiveVisitor {
	void visitAny(Any x) throws Exception;
	void visitExpression(Expression e) throws Exception;
	void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception;
	void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) throws Exception; //TODO remove and add methods for subclasses
	void visitSimplex(Simplex x) throws Exception;
	void visitTerm(Term x) throws Exception;
	void visitNarrowingConversion(NarrowingConversion x) throws Exception; 
	void visitWideningConversion(WideningConversion x) throws Exception; 
}
