package jbse.common;

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
     * Do not instantiate it!
     */
    private Util() {
        //intentionally empty
    }
}
