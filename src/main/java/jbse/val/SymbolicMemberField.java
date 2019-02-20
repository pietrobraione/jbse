package jbse.val;


/**
 * Interface that must be implemented by all the {@link SymbolicMember}
 * values whose origin is a field in an object (non array). 
 * 
 * @author Pietro Braione
 */
public interface SymbolicMemberField extends SymbolicMember {
    /**
     * Returns the name of the origin field.
     * 
     * @return a {@link String}. 
     */
    String getFieldName();
    
    /**
     * Returns the name of the class 
     * where the field is declared.
     * 
     * @return a {@link String}. 
     */
    String getFieldClass();
}
