package jbse.val;

/**
 * A Visitor for {@link Primitive} values.
 * 
 * @author Pietro Braione
 */
public interface PrimitiveVisitor {
	void visitAny(Any x) throws Exception;
	void visitExpression(Expression e) throws Exception;
	void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception;
	void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception;
	void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception;
	void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception;
	void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) throws Exception;
	void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception;
	void visitSimplex(Simplex x) throws Exception;
	void visitTerm(Term x) throws Exception;
	void visitNarrowingConversion(NarrowingConversion x) throws Exception; 
	void visitWideningConversion(WideningConversion x) throws Exception; 
}
