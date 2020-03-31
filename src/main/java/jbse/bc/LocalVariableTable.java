package jbse.bc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jbse.common.Type;

/**
 * Class for local variable tables and local variable type tables.
 * 
 * @author Pietro Braione
 */
public class LocalVariableTable implements Iterable<LocalVariableTable.Row> {
    public static class Row {
        public final int slot;
        public final String descriptor;
        public final String name;
        public final int start;
        public final int length;

        public Row(int slot, String descriptor, String name, int start, int length) {
            this.slot = slot;
            this.descriptor = descriptor;
            this.name = name;
            this.start = start;
            this.length = length;
        }

        @Override
        public String toString() {
            return "<" + slot + " " + descriptor + " " + name + " " + start + " " + length + ">";
        }
    }

    /** Total number of slots */
    private final int slots;

    /** Map slot number -> rows for it */
    private final Map<Integer, Set<Row>> entries;

    /**
     * Constructor returning an empty local variable table.
     * 
     * @param slots the number of slots of the local table.
     */
    public LocalVariableTable(int slots) {
        this.slots = slots;
        this.entries = new HashMap<Integer, Set<Row>>();
    }

    /**
     * Adds a row to this {@link LocalVariableTable}.
     * 
     * @param slot the number of the slot.
     * @param descriptor a descriptor (local variable table) or 
     *        generic signature type (local variable type table).
     * @param name the name of the variable.
     * @param start the start of the program counter.
     * @param length the length of the code.
     */
    public void addRow(int slot, String descriptor, String name, int start, int length) {
        //silently rejects ill-formed rows
        if (slot >= this.slots) {
            return;
        }
        if ((descriptor.charAt(0) == Type.DOUBLE || descriptor.charAt(0) == Type.LONG)
            && slot >= this.slots - 1) {
            return;
        }

        //gets/creates the set of rows associated to the slot number
        final Set<Row> sub;
        if (this.entries.containsKey(slot)) {
            sub = this.entries.get(slot);
        } else {
            sub = new HashSet<>();
            this.entries.put(slot, sub);
        }

        //adds a new row to the set
        final Row r = new Row(slot, descriptor, name, start, length);
        sub.add(r);
    }

    /**
     * Given a slot number returns all the rows for it.
     * 
     * @param slot the number of the slot.
     * @return an {@link Iterable}{@code <}{@link Row}{@code >} of all 
     *         the rows associated to {@code slot} (empty iff no such 
     *         rows exist).
     */
    public Iterable<Row> rows(int slot) {
        Set<Row> retVal = this.entries.get(slot);
        if (retVal == null) {
            retVal = new HashSet<>();
        }
        return retVal;
    }

    /**
     * Given a slot number and a program counter, 
     * returns the corresponding row.
     * 
     * @param slot the number of the slot.
     * @param curPC a program counter.
     * @return the {@link Row} associated to {@code slot}
     *         and {@code curPC}, or {@code null} if no
     *         such row exists.
     */
    public Row row(int slot, int curPC) {
        for (Row r : this.rows(slot)) {
            if (r.start <= curPC && curPC < r.start + r.length) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns the total number of the slots.
     * 
     * @return an {@code int}.
     */
    public int getSlots() {
        return this.slots;
    }

    @Override
    public Iterator<Row> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<Row> {
        private Iterator<Map.Entry<Integer, Set<Row>>> itOuter;
        private Iterator<Row> itSub;

        public MyIterator() {
            //turns on the outer iterator
        	final Set<Map.Entry<Integer, Set<Row>>> entries = LocalVariableTable.this.entries.entrySet();
            this.itOuter = entries.iterator();
            this.itSub = null;
        }

        /**
         * Checks if {@code this.itSub} is inert.
         * 
         * @return {@code true} iff {@code this.itSub} is 
         *         inert, i.e., iff it does not provide a next element. 
         */
        private boolean itSubInert() {
            return (this.itSub == null || this.itSub.hasNext() == false);
        }

        public boolean hasNext() {
            return (!this.itSubInert() || this.itOuter.hasNext());
        }

        public Row next() {
            //checks precondition
            if (!this.hasNext())
                throw new NoSuchElementException();

            //if the inner iterator is inert, turns it on
            if (this.itSubInert()) {
                final Map.Entry<Integer, Set<Row>> e = this.itOuter.next();
                final Set<Row> subEntries = e.getValue();
                this.itSub = subEntries.iterator();
            }

            //returns the next item
            return this.itSub.next();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}