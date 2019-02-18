package jbse.rules;

import java.util.ArrayList;
import java.util.Collections;

public final class ClassInitRulesRepo implements Cloneable {
    private ArrayList<String> notInitializedClassPatterns = new ArrayList<>();

    public void addNotInitializedClassPattern(String... notInitializedClassPatterns) {
        Collections.addAll(this.notInitializedClassPatterns, notInitializedClassPatterns);
    }

    //TODO do it better!!!
    public boolean notInitializedClassesContains(String c) {
    	for (String pattern : this.notInitializedClassPatterns) {
    		if (c.matches(pattern)) {
    			return true;
    		}
    	}
		return false;
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

        o.notInitializedClassPatterns = (ArrayList<String>) this.notInitializedClassPatterns.clone();

        return o;
    }
}
