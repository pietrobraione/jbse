package jbse.bc;

import jbse.val.Value;

/**
 * A {@link Value} representing a string literal of the 
 * constant pool. 
 * 
 * @author Pietro Braione
 *
 */
public final class ConstantPoolString extends ConstantPoolValue {
	private final String value;
	private final int hashCode;
	
	public ConstantPoolString(String value) { 
		this.value = value; 
		final int prime = 17;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		this.hashCode = result;
	}
	
	@Override
	public String getValue() {
	    return this.value;
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
	public String toString() {
		return this.value;
	}
}
