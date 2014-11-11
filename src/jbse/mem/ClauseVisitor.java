package jbse.mem;

public interface ClauseVisitor {
	void visitClauseAssume(ClauseAssume c) throws Exception;
	void visitClauseAssumeAliases(ClauseAssumeAliases c) throws Exception;
	void visitClauseAssumeClassInitialized(ClauseAssumeClassInitialized c) throws Exception;
	void visitClauseAssumeClassNotInitialized(ClauseAssumeClassNotInitialized c) throws Exception;
	void visitClauseAssumeExpands(ClauseAssumeExpands c) throws Exception;
	void visitClauseAssumeNull(ClauseAssumeNull c) throws Exception;
}
