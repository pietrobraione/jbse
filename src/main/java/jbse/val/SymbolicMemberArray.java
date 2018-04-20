package jbse.val;


/**
 * Interface that must be implemented by all the {@link SymbolicMember}
 * values whose origin is a slot in an array.  
 * 
 * @author Pietro Braione
 */
public interface SymbolicMemberArray extends SymbolicMember {
    /**
     * Returns the index of the origin array
     * slot.
     * 
     * @return a {@link Primitive}. 
     */
    Primitive getIndex();
}
