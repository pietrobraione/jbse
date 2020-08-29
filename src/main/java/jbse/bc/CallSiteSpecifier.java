package jbse.bc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A call site specifier contains the information
 * from a CONSTANT_InvokeDynamic constant pool entry.
 * It is immutable.
 * 
 * @author Pietro Braione
 */
public final class CallSiteSpecifier {
    private final String descriptor;
    private final String name;
    private final Signature bootstrapMethodSignature;
    private final List<ConstantPoolValue> bootstrapParameters;
    
    public CallSiteSpecifier(String descriptor, String name, Signature bootstrapMethodSignature, ConstantPoolValue... bootstrapParameters) {
    	this.descriptor = descriptor;
    	this.name = name;
    	this.bootstrapMethodSignature = bootstrapMethodSignature;
    	this.bootstrapParameters = Collections.unmodifiableList(Arrays.asList(bootstrapParameters));
    }

	public String getDescriptor() {
		return this.descriptor;
	}

	public String getName() {
		return this.name;
	}

	public Signature getBootstrapMethodSignature() {
		return this.bootstrapMethodSignature;
	}

	public List<ConstantPoolValue> getBootstrapParameters() {
		return this.bootstrapParameters;
	}
}
