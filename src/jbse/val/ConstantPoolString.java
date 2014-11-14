package jbse.val;

import jbse.bc.ClassFile;
import jbse.common.Type;
import jbse.val.exc.ValueDoesNotSupportNativeException;

/**
 * A {@link Value} representing a string from the constant pool. 
 * A {@code ConstantPoolString} is returned by a {@link ClassFile} 
 * and shall never escape into a {@link State}, it should only be used
 * to fetch or generate a suitable {@link Instance} in the 
 * {@link State}'s {@link Heap} with class {@link java.lang.String}.
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolString extends Value {
	private final String value;
	private final int hashCode;
	
	public ConstantPoolString(String value) { 
		super(Type.STRING_LITERAL); 
		this.value = value; 
		final int prime = 17;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		this.hashCode = result;
	}

	@Override
	public int hashCode() {
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
		final ConstantPoolString other = (ConstantPoolString) obj;
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}

	@Override
	public Object getValueForNative() throws ValueDoesNotSupportNativeException {
		throw new ValueDoesNotSupportNativeException();
	}

	@Override
	public String toString() {
		return this.value;
	}

	@Override
	public boolean isSymbolic() {
		return true;
	}
}
