package jbse.val;

/**
 * A Visitor for {@link Reference} values.
 * 
 * @author Pietro Braione
 *
 */
public interface ReferenceVisitor {
	void visitReferenceArrayImmaterial(ReferenceArrayImmaterial x) throws Exception;
	void visitReferenceConcrete(ReferenceConcrete x) throws Exception; //including null;
	void visitKlassPseudoReference(KlassPseudoReference x) throws Exception;
	void visitReferenceSymbolicApply(ReferenceSymbolicApply x) throws Exception;
	void visitReferenceSymbolicLocalVariable(ReferenceSymbolicLocalVariable x) throws Exception;
	void visitReferenceSymbolicMemberArray(ReferenceSymbolicMemberArray x) throws Exception;
	void visitReferenceSymbolicMemberField(ReferenceSymbolicMemberField x) throws Exception;
	void visitReferenceSymbolicMemberMapKey(ReferenceSymbolicMemberMapKey x) throws Exception;
	void visitReferenceSymbolicMemberMapValue(ReferenceSymbolicMemberMapValue x) throws Exception;
}
