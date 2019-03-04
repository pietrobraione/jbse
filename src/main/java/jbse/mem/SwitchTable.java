package jbse.mem;

import java.util.Iterator;
import java.util.NoSuchElementException;

import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * A switch table. It is a list of entries having the form (val, ofst), where
 * val is a switch value and ofst the corresponding jump offset. It is an
 * {@link Iterable}<{@link Integer}>, where the iteration returns all the
 * <em>values</em>. Moreover, the table stores also the special offset for
 * the default case.
 * 
 * @author Pietro Braione
 */
public class SwitchTable implements Iterable<Integer> {
    /** {@code true} if tableswitch, {@code false} if lookupswitch. */
    private final boolean ts;

    /** The method's bytecode. */
    private final byte[] code;

    /** The size of the entry in bytes. */
    private final int entrySizeInBytes;

    /** The default jump offset. */
    private final int deflt;

    /** The lowest possible value for the index. */
    private final int low; 
    
    /** The highest possible value for the index. */
    private final int high;
    
    /** 
     * The offset in code of the start of the table
     * (the match-offset pairs in case of lookupswitch, 
     * the jump offsets in case of tableswitch). 
     */
    private final int tableStart; 
    
    /** 
     * The offset in code of the end of the table
     * (i.e., the offset of the bytecode instruction 
     * next to this switch instruction). 
     */
    private final int tableEnd; 

    /**
     * Constructor.
     * 
     * @param f a {@link Frame}. Its current program counter must point
     *        to the switch bytecode.
     * @param isTableSwitch {@code true} if the bytecode is a tableswitch, 
     *        {@code false} if it is a lookupswitch.
     * @throws InvalidProgramCounterException upon failure to access to the
     *         table (wrong program counter or bytecode).
     */
    public SwitchTable(Frame f, boolean isTableSwitch) 
    throws InvalidProgramCounterException {
        this.ts = isTableSwitch;
        this.code = f.getCode();

        //skips the alignment bytes
        final byte[] ops = (this.ts ? new byte[12] : new byte[8]);
        int ofst = 0;
        do {
            ++ofst;
        } while ((f.getProgramCounter() + ofst) % 4 != 0);

        //gets the default offset bytes and the number of pairs
        //(in case of lookupswitch) or the low/high indices 
        //(in case of tableswitch)
        for (int i = 0; i < ops.length; i++) {
            ops[i] = f.getInstruction(ofst);
            ++ofst;
        }

        this.entrySizeInBytes = (this.ts ? 4 : 8);
        this.deflt = Util.byteCat(ops[0], ops[1], ops[2], ops[3]);
        this.low = (this.ts ? Util.byteCat(ops[4], ops[5], ops[6], ops[7]) : 1);
        this.high = (this.ts ? Util.byteCat(ops[8], ops[9], ops[10], ops[11]) : Util.byteCat(ops[4], ops[5], ops[6], ops[7]));
        this.tableStart = f.getProgramCounter() + ofst;
        this.tableEnd = this.tableStart + (this.high - this.low + 1) * this.entrySizeInBytes;
    }

    private class SwitchTableIterator implements Iterator<Integer> {
        /** If a tableswitch, directly the value of the entry, else index to lookup it */
        private int i;

        /** The value of the current entry. */
        private int entryVal;

        public SwitchTableIterator() {
            this.i = (ts ? low : tableStart);
            this.entryVal = 0;
        }

        @Override
        public boolean hasNext() {
            return (ts ? this.i <= high : this.i < tableEnd);
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Integer retVal;
            if (ts) {
                retVal = i;
            } else {
                byte[] t = new byte[entrySizeInBytes];
                for (int k = 0; k < entrySizeInBytes; k++) {
                    t[k] = code[i + k];
                }
                retVal = Util.byteCat(t[0], t[1], t[2], t[3]);
                this.entryVal = Util.byteCat(t[4], t[5], t[6], t[7]);
            }
            i += (ts ? 1 : entrySizeInBytes);
            return retVal;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public int getEntryVal() {
            return this.entryVal;
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new SwitchTableIterator();
    }

    /**
     * Returns the jump offset for the default alternative.
     * @return an {@code int}, the new value for the program 
     *         counter in the default case.
     */
    public int jumpOffsetDefault() {
        return this.deflt;
    }

    /**
     * Returns the jump offset associated to a specific switch value.
     * @param value a switch value.
     * @return the offset associate to {@code value} in the 
     *         table, or {@code this.}{@link #jumpOffsetDefault()}
     *         in the case {@code value} is not
     *         a key of the table. 
     */
    public int jumpOffset(int value) {
        int jumpOffset;
        byte[] t = new byte[this.entrySizeInBytes];
        if (this.ts) {
            if (value >= this.low && value <= this.high) {
                for (int i = 0; i < this.entrySizeInBytes; i++) {
                    t[i] = this.code[this.tableStart + (value - this.low) * this.entrySizeInBytes + i];
                }
                jumpOffset = Util.byteCat(t[0], t[1], t[2], t[3]);
            } else {
                jumpOffset = this.deflt;
            }
        } else { 
            //search of entry (unoptimized linear search)
            jumpOffset = this.deflt;
            for (SwitchTableIterator it = new SwitchTableIterator(); it.hasNext(); ) {
                int match = it.next();
                if (value == match) {
                    jumpOffset = it.getEntryVal();
                    break; //slight acceleration
                } else if (value < match) {
                    break; //slight acceleration
                }
            }
        }
        return jumpOffset;	
    }

    //TODO bring these method outside and eliminate dependence on calc
    /**
     * Returns an {@link Expression} denoting the fact that a suitable
     * selector is not a key of the {@link SwitchTable}.
     * 
     * @param calc a {@link Calculator}. It must not be {@code null}.
     * @param selector a {@link Primitive} of int type.
     * @return an {@link Expression} denoting the fact that {@code selector} 
     *         is not a key of the {@link SwitchTable}.
     * @throws InvalidInputException if {@code calc == null || selector == null}.
     * @throws InvalidTypeException if {@code selector} is not an {@code int}. 
     */
    public Expression getDefaultClause(Calculator calc, Primitive selector) 
    throws InvalidInputException, InvalidTypeException {
        if (calc == null || selector == null) {
            throw new InvalidInputException("Attempted to get the default clause of a switch table with a null calc or selector.");
        }
        if (selector.getType() != Type.INT) {
            throw new InvalidTypeException("Used a switch selector with type " + selector.getType());
        }
        try {
            Primitive e;
            if (this.ts) {
                //here we exploit the fact that a tableswitch specifies a range
                //to produce a shorter expression, but all the then branch of this
                //if statement could be deleted altogether, and the code would
                //work nevertheless.
                e = calc.push(selector).lt(calc.pushInt(this.low).pop()).or(calc.push(selector).gt(calc.valInt(this.high)).pop()).pop();
            } else {
                e = calc.valBoolean(true);
                for (int match : this) {
                    e = calc.push(e).and(calc.push(selector).ne(calc.valInt(match)).pop()).pop();
                }
            }
            return (Expression) e;
        } catch (InvalidOperandException | InvalidTypeException exc) {
            //this should never happen
            throw new UnexpectedInternalException(exc);
        }
    }
}