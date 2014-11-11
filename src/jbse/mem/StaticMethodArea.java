package jbse.mem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The static method area, where all the {@link Klass} objects of a 
 * JVM state are stored.
 * 
 * @author Pietro Braione
 *
 */
public class StaticMethodArea implements Cloneable {
	private HashMap<String, Klass> objTable;
	
	public StaticMethodArea() {
		this.objTable = new HashMap<String, Klass>();
	}
	
	public boolean contains(String className) { 
		return this.objTable.containsKey(className); 
	}

	public Klass get(String className) {
    	return this.objTable.get(className);
    }

	public Klass set(String className, Klass k) {
    	return this.objTable.put(className, k);
    }
	
    public Map<String, Klass> getObjects() {
        return this.objTable;
    }
    
	@Override
	public String toString() {
        String tmpRet = "[";
        int j = 0;
        Set<Map.Entry<String, Klass>> entries = this.objTable.entrySet();
        for (Map.Entry<String, Klass> ee : entries) {
        	Klass k = ee.getValue();
        	String c = ee.getKey();
            tmpRet +=  c + ":" + "{" + k.toString() + "}";
            if (j < entries.size() - 1) 
            	tmpRet += ", ";
            j++;
        }
        tmpRet +="]";
        return(tmpRet);
	}
    
	@Override
	public StaticMethodArea clone() {
		final StaticMethodArea o;
		try {
			o = (StaticMethodArea) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
		
		//objTable
		HashMap<String, Klass> objTableClone = new HashMap<String, Klass>();
		for (Map.Entry<String, Klass> e : this.objTable.entrySet()) {
			Klass val = e.getValue();
			objTableClone.put(e.getKey(), val.clone());
		}
		o.objTable = objTableClone;
		
		return o;
	}
}
