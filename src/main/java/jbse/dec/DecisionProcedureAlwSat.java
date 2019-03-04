package jbse.dec;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jbse.bc.ClassFile;
import jbse.mem.Clause;
import jbse.mem.Objekt;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.ReferenceSymbolic;

/**
 * Class implementing a "no assumption" {@link DecisionProcedure} 
 * for which all clauses are satisfiable.
 * 
 * @author Pietro Braione
 *
 */
public class DecisionProcedureAlwSat implements DecisionProcedure {
	private Calculator calc; //unused, but gets useful in the Chain of Responsibility to inject it through the chain
    private ArrayDeque<Clause> cstack;

    public DecisionProcedureAlwSat(Calculator calc) {
    	this.calc = calc;
        this.cstack = new ArrayDeque<Clause>();
    }
    
    @Override
    public Calculator getCalculator() {
    	return this.calc;
    }

    @Override
    public Collection<Clause> getAssumptions() {
        return new Collection<Clause>() {
            //code taken from jdk8 Collections.UnmodifiableCollection,
            //with cstack.descendingIterator() instead of cstack.iterator()
            @Override
            public int size() { return cstack.size(); }

            @Override
            public boolean isEmpty() { return cstack.isEmpty(); }

            @Override
            public boolean contains(Object o) { return cstack.contains(o); }

            @Override
            public Iterator<Clause> iterator() { 
                return new Iterator<Clause>() {
                    private final Iterator<? extends Clause> i = cstack.descendingIterator();

                    @Override public boolean hasNext() {return i.hasNext();}
                    @Override public Clause next()     {return i.next();}
                    @Override public void remove()     {throw new UnsupportedOperationException();}
                    @Override
                    public void forEachRemaining(Consumer<? super Clause> action) {
                        // Use backing collection version
                        i.forEachRemaining(action);
                    }
                };
            }

            @Override
            public Object[] toArray() { return cstack.toArray(); }

            @Override
            public <T> T[] toArray(T[] a) { return cstack.toArray(a); }

            @Override
            public boolean add(Clause e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return cstack.containsAll(c);
            }

            @Override
            public boolean addAll(Collection<? extends Clause> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }

            // Override default methods in Collection
            @Override
            public void forEach(Consumer<? super Clause> action) {
                cstack.forEach(action);
            }

            @Override
            public boolean removeIf(Predicate<? super Clause> filter) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Spliterator<Clause> spliterator() {
                return (Spliterator<Clause>) cstack.spliterator();
            }

            @Override
            public Stream<Clause> stream() {
                return (Stream<Clause>) cstack.stream();
            }

            @Override
            public Stream<Clause> parallelStream() {
                return (Stream<Clause>) cstack.parallelStream();
            }

            @Override
            public String toString() {
                final StringBuilder buf = new StringBuilder();
                boolean firstDone = false;
                for (Iterator<Clause> it = cstack.descendingIterator(); it.hasNext(); ) {
                    if (firstDone) {
                        buf.append(", ");
                    } else {
                        firstDone = true;
                    }
                    buf.append(it.next().toString());
                }
                buf.append("]");
                return buf.toString();
            }
        };
    }

    @Override
    public void pushAssumption(Clause c) {
        this.cstack.push(c);
    }

    @Override
    public void clearAssumptions()  {
        this.cstack.clear();
    }

    @Override
    public boolean isSat(Expression exp) {
        return true;
    }

    @Override
    public boolean isSatNull(ReferenceSymbolic r) {
        return true;
    }

    @Override
    public boolean isSatAliases(ReferenceSymbolic r, long heapPos, Objekt o) {
        return true;
    }

    @Override
    public boolean isSatExpands(ReferenceSymbolic r, ClassFile classFile) {
        return true;
    }

    @Override
    public boolean isSatInitialized(ClassFile classFile) {
        return true;
    }

    @Override
    public boolean isSatNotInitialized(ClassFile classFile) {
        return true;
    }
}
