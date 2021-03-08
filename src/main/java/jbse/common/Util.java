package jbse.common;

import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import jbse.common.exc.UnexpectedInternalException;
import sun.misc.Unsafe;

/**
 * Class that provides some utility constants and static methods.
 */
public final class Util {
    // symbols
    public static final String ROOT_FRAME_MONIKER = "{ROOT}:";
    
    /**
     * Given a byte, returns its value interpreted
     * as it were unsigned.
     * 
     * @param b a {@code byte}.
     * @return a {@code short}, the zero-extension of {@code b}.
     */
    public static short asUnsignedByte(byte b) {
        return (short) (((short) 0x00FF) & ((short) b));
    }

    /**
     * Given four bytes, returns the int resulting by their bitwise
     * concatenation
     */
    public static int byteCat(byte first, byte second, byte third, byte fourth) {
        int first_32 = first;
        int second_32 = second;
        int third_32 = third;
        int fourth_32 = fourth;

        first_32 = first_32 << 24;
        second_32 = (second_32 << 16) & 0xFF0000;
        third_32 = (third_32 << 8) & 0xFF00;
        fourth_32 = fourth_32 & 0xFF;

        return (first_32 | second_32 | third_32 | fourth_32);
    }

    /**
     * Given two bytes, returns the int resulting by their bitwise
     * concatenation.
     */
    public static int byteCat(byte first, byte second) {
        return byteCat((byte) 0, (byte) 0, first, second);
    }

    /**
     * Given two bytes, returns the short resulting from their bitwise
     * concatenation.
     */
    public static short byteCatShort(byte first, byte second) {
        // narrowing just discards the higher order bits,
        // which however are zeros
        return (short) byteCat(first, second);
    }

    /**
     * Returns the {@link sun.misc.Unsafe} singleton instance.
     */
    public static Unsafe unsafe() {
        try {
            final Field fieldUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            fieldUnsafe.setAccessible(true);
            return (Unsafe) fieldUnsafe.get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }
    
	private static final PathMatcher JAR_EXTENSION = FileSystems.getDefault().getPathMatcher("glob:**.jar");
	
	/**
	 * Checks if a path stands for a jar file.
	 * 
	 * @param path a {@link Path}.
	 * @return {@code true} iff {@code path} is the path of a
	 *         file with .jar extension.
	 */
	public static boolean isJarFile(Path path) {
		return Files.exists(path) && !Files.isDirectory(path) && JAR_EXTENSION.matches(path);
	}

    /**
     * Do not instantiate it!
     */
    private Util() {
        //intentionally empty
    }

	/**
	 * Returns an {@link Iterable} that scans a {@link List} in 
	 * reverse order, from tail to head.
	 * 
	 * @param list a {@link List}{@code <T>}. It must not be {@code null}.
	 * @return an {@link Iterable}{@code <T>}.
	 */
	public static <T> Iterable<T> reverse(final List<T> list) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					private ListIterator<T> delegate = list.listIterator(list.size());
	
					@Override
					public boolean hasNext() {
						return this.delegate.hasPrevious();
					}
	
					@Override
					public T next() {
						return this.delegate.previous();
					}
	
					@Override
					public void remove() {
						this.delegate.remove();
					}
				};
			}
		};
	}
}
