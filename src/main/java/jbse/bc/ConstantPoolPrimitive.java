package jbse.bc;

public final class ConstantPoolPrimitive extends ConstantPoolValue {
    private final Number number;
    private final int hashCode;
    
    private ConstantPoolPrimitive(Number number) {
        this.number = number;
        final int prime = 11;
        int result = 1;
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        this.hashCode = result;
    }
    
    public ConstantPoolPrimitive(int number) {
        this(Integer.valueOf(number));
    }
    
    public ConstantPoolPrimitive(float number) {
        this(Float.valueOf(number));
    }

    public ConstantPoolPrimitive(long number) {
        this(Long.valueOf(number));
    }

    public ConstantPoolPrimitive(double number) {
        this(Double.valueOf(number));
    }

    @Override
    public final Number getValue() {
        return this.number;
    }
    
    @Override
    public final int hashCode() {
        return this.hashCode;
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
        final ConstantPoolPrimitive other = (ConstantPoolPrimitive) obj;
        if (this.number == null) {
            if (other.number != null) {
                return false;
            }
        } else if (!this.number.equals(other.number)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return this.number.toString();
    }
}
