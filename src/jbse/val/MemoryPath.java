package jbse.val;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A path is a sequence of {@link Access}es to a state's
 * memory that yields a value.
 * 
 * @author Pietro Braione
 *
 */
public final class MemoryPath implements Iterable<Access> {
    private final Access[] accesses;
    private final String toString;

    private MemoryPath(Access... accesses) {
        this.accesses = accesses.clone();
        this.toString = String.join(".", Arrays.stream(this.accesses).map(Object::toString).toArray(String[]::new));
    }
    
    public static MemoryPath mkStatic(String className) {
        return new MemoryPath(new AccessStatic(className));
    }
    
    public static MemoryPath mkLocalVariable(String variableName) {
        return new MemoryPath(new AccessLocalVariable(variableName));
    }
    
    public MemoryPath thenField(String fieldName) {
        return new MemoryPath(Stream.concat(Arrays.stream(this.accesses), Stream.of(new AccessField(fieldName))).toArray(Access[]::new));
    }
    
    public MemoryPath thenArrayMember(Primitive index) {
        return new MemoryPath(Stream.concat(Arrays.stream(this.accesses), Stream.of(new AccessArrayMember(index))).toArray(Access[]::new));
    }
    
    public MemoryPath thenArrayLength() {
        return new MemoryPath(Stream.concat(Arrays.stream(this.accesses), Stream.of(AccessArrayLength.instance())).toArray(Access[]::new));
    }

    @Override
    public Iterator<Access> iterator() {
        return new Iterator<Access>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return (this.index < accesses.length);
            }

            @Override
            public Access next() {
                return accesses[this.index++];
            }
        };
    }
    
    @Override
    public String toString() {
        return this.toString;
    }
}
