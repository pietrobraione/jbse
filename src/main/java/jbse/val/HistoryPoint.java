package jbse.val;

import java.util.ArrayList;

/**
 * Class for history points in symbolic execution. A history point identifies a
 * state in the symbolic execution by specifying the identifier of the branch 
 * in the symbolic tree and the sequence number (distance from the branch)
 * where the state is. It is immutable.
 * 
 * @author Pietro Braione
 *
 */
public final class HistoryPoint {
    public static final String BRANCH_IDENTIFIER_SEPARATOR_COMPACT = ".";
    public static final String BRANCH_IDENTIFIER_DEFAULT_COMPACT = "1";
    public static final String BRANCH_IDENTIFIER_SEPARATOR_LONG = "|";
    public static final String BRANCH_IDENTIFIER_DEFAULT_LONG = "ROOT";
    
    private final boolean compact;
    
    private final ArrayList<String> branchIdentifier;
    
    private final int sequenceNumber;
    
    /**
     * Constructor for the root history point.
     * 
     * @param compact a {@code boolean}.
     */
    private HistoryPoint(boolean compact) {
        this.compact = compact;
        this.branchIdentifier = new ArrayList<>();
        this.sequenceNumber = 0;
    }
    
    /**
     * Private constructor.
     * 
     * @param compact a {@code boolean}.
     * @param branchIdentifier an {@link ArrayList}{@code <}{@link String}{@code >}.
     * @param sequenceNumber an {@code int}.
     */
    private HistoryPoint(boolean compact, ArrayList<String> branchIdentifier, int sequenceNumber) {
        this.compact = compact;
        this.branchIdentifier = branchIdentifier;
        this.sequenceNumber = sequenceNumber;
    }
    
    /**
     * Factory method. Builds the starting pre-initial {@link HistoryPoint}.
     */
    public static HistoryPoint preInitial(boolean compact) {
        return new HistoryPoint(compact);
    }
    
    /**
     * Factory method. Builds the initial {@link HistoryPoint} next in time
     * to this one.
     * 
     * @return a {@link HistoryPoint}.
     */
    public HistoryPoint initial() {
        final ArrayList<String> nextBranchIdentifier = new ArrayList<>();
        nextBranchIdentifier.add(this.compact ? BRANCH_IDENTIFIER_DEFAULT_COMPACT : BRANCH_IDENTIFIER_DEFAULT_LONG);
        return new HistoryPoint(this.compact, nextBranchIdentifier, 0);
    }
    
    /**
     * Factory method. Builds a {@link HistoryPoint} next in time
     * on the same branch.
     * 
     * @return a {@link HistoryPoint}.
     */
    public HistoryPoint next() {
        return new HistoryPoint(this.compact, this.branchIdentifier, this.sequenceNumber + 1);
    }
    
    /**
     * Factory method. Builds a {@link HistoryPoint} next in time
     * on a subbranch.
     * 
     * @param additionalBranch a {@link String} that identifies the
     *        subbranch.
     * @return a {@link HistoryPoint}.
     */
    public HistoryPoint nextBranch(String additionalBranch) {
        final ArrayList<String> nextBranchIdentifier = new ArrayList<>(this.branchIdentifier);
        nextBranchIdentifier.add(additionalBranch);
        return new HistoryPoint(this.compact, nextBranchIdentifier, 0);
    }
    
    /**
     * Returns the branch identifier as a {@link String}.
     * 
     * @return a {@link String}.
     */
    public String getBranchIdentifier() {
        final StringBuilder retVal = new StringBuilder();
        for (String b : this.branchIdentifier) {
            retVal.append(this.compact ? BRANCH_IDENTIFIER_SEPARATOR_COMPACT : BRANCH_IDENTIFIER_SEPARATOR_LONG);
            retVal.append(b);
        }
        return retVal.toString();
    }
    
    /**
     * Returns the sequence number.
     * 
     * @return an {@code int}.
     */
    public int getSequenceNumber() {
        return this.sequenceNumber;
    }
    
    /**
     * Checks whether this {@link HistoryPoint} comes before
     * another one.
     * 
     * @param other a {@link HistoryPoint}. It must not be {@code null}.
     * @return {@code true} iff this object's branch identifier is a 
     *         proper prefix of {@code other}'s branch identifier, or if
     *         the two branch identifiers are equal and 
     *         {@code this.}{@link #getSequenceNumber() getSequenceNumber}{@code () <= other.}{@link #getSequenceNumber() getSequenceNumber}{@code ()}.
     * @throws NullPointerException if {@code other == null}.
     */
    public boolean comesBefore(HistoryPoint other) {
        if (this.branchIdentifier.size() > other.branchIdentifier.size()) {
            return false;
        }
        
        for (int i = 0; i < this.branchIdentifier.size(); ++i) {
            if (!this.branchIdentifier.get(i).equals(other.branchIdentifier.get(i))) {
                return false;
            }
        }
        
        if (this.branchIdentifier.size() == other.branchIdentifier.size() && this.sequenceNumber > other.sequenceNumber) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.branchIdentifier.hashCode();
        result = prime * result + this.sequenceNumber;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HistoryPoint other = (HistoryPoint) obj;
        if (!this.branchIdentifier.equals(other.branchIdentifier)) {
            return false;
        }
        if (this.sequenceNumber != other.sequenceNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getBranchIdentifier() + "[" + getSequenceNumber() + "]";
    }
}
