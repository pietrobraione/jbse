package jbse.val;

public interface ValueVisitor extends PrimitiveVisitor, ReferenceVisitor {
	void visitDefaultValue(DefaultValue x);
}
