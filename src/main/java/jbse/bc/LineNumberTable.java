package jbse.bc;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class representing a line number table.
 * 
 * @author Pietro Braione
 */
public final class LineNumberTable implements Iterable<LineNumberTable.Row> {
    public static final class Row {
        public final int start;
        public final int lineNumber;

        Row(int start, int lineNumber) {
            this.start = start;
            this.lineNumber = lineNumber;
        }

        @Override
        public String toString() {
            return "<" + this.start + " " + this.lineNumber + ">";
        }
    }

    private final Row[] rows;
    private int next;

    public LineNumberTable(int rowsNumber) {
        this.rows = new Row[rowsNumber];
        this.next = 0;
    }

    public void addRow(int start, int lineNumber) {
        //silently rejects if out of range
        if (this.next >= this.rows.length) {
            return;
        }
        
        this.rows[this.next] = new Row(start, lineNumber);
        ++this.next;
    }

    @Override
    public Iterator<Row> iterator() {
        return new MyIterator();
    }

    private class MyIterator implements Iterator<Row> {
        int i = 0;

        public boolean hasNext() {
            return (i < LineNumberTable.this.rows.length && 
            LineNumberTable.this.rows[i] != null);
        }

        public Row next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ++i;
            return LineNumberTable.this.rows[i - 1];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

