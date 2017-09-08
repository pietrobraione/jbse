package jbse.val;


public interface PrimitiveVisitor {
	void visitAny(Any x) throws Exception;
	void visitExpression(Expression e) throws Exception;
	void visitFunctionApplication(FunctionApplication x) throws Exception;
	void visitPrimitiveSymbolic(PrimitiveSymbolic s) throws Exception;
	void visitSimplex(Simplex x) throws Exception;
	void visitTerm(Term x) throws Exception;
	void visitNarrowingConversion(NarrowingConversion x) throws Exception; 
	void visitWideningConversion(WideningConversion x) throws Exception; 
}
