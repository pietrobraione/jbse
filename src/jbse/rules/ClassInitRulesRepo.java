package jbse.rules;

import java.util.Collections;
import java.util.HashSet;

public final class ClassInitRulesRepo implements Cloneable {
    private HashSet<String> notInitializedClasses = new HashSet<>();

	public void addNotInitializedClass(String... notInitializedClasses) {
        Collections.addAll(this.notInitializedClasses, notInitializedClasses);
	}
	
	//TODO do it better!!!
	public boolean notInitializedClassesContains(String c) {
		return this.notInitializedClasses.contains(c);
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public ClassInitRulesRepo clone() {
        final ClassInitRulesRepo o;
        try {
            o = (ClassInitRulesRepo) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); //will not happen
        }
        
        o.notInitializedClasses = (HashSet<String>) this.notInitializedClasses.clone();
        
        return o;
	}
}
