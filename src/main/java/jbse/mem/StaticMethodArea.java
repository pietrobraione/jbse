package jbse.mem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jbse.bc.ClassFile;

/**
 * The static method area, where all the {@link Klass} objects of a 
 * JVM state are stored.
 * 
 * @author Pietro Braione
 *
 */
final class StaticMethodArea implements Cloneable {
    private StaticMethodArea delegate;
    private HashMap<ClassFile, Klass> objects;

    StaticMethodArea() {
    	this.delegate = null;
        this.objects = new HashMap<>();
    }

    boolean contains(ClassFile classFile) { 
        return (this.objects.containsKey(classFile) || (this.delegate != null && this.delegate.contains(classFile))); 
    }

    void set(ClassFile classFile, Klass k) {
        this.objects.put(classFile, k);
    }

    Klass get(ClassFile classFile) {
    	if (contains(classFile)) {
    		final Klass localKlass = this.objects.get(classFile);
    		if (localKlass == null) {
    			final KlassImpl trueKlass = getTheRealThing(classFile);
    			final KlassWrapper delegateKlass = trueKlass.makeWrapper(this, classFile);
    			set(classFile, delegateKlass);
    			return delegateKlass;
    		} else {
    			return localKlass;
    		}
    	} else {
    		return null;
    	}
    }
    
    private KlassImpl getTheRealThing(ClassFile classFile) {
    	final Klass localKlass = this.objects.get(classFile);
		if (localKlass == null) {
			return this.delegate.getTheRealThing(classFile);
		} else 	if (localKlass instanceof KlassWrapper) {
    		return ((KlassWrapper) localKlass).getDelegate();
    	} else {
    		return (KlassImpl) localKlass;
    	}
    }
    
    private HashSet<ClassFile> filledPositions() {
    	final HashSet<ClassFile> retVal = new HashSet<>();
    	retVal.addAll(this.objects.keySet());
    	if (this.delegate != null) {
    		retVal.addAll(this.delegate.filledPositions());
    	}
    	return retVal;
    }
    
    private void makeAllWrappers() {
        for (ClassFile pos : filledPositions()) {
            if (!this.objects.containsKey(pos)) {
            	final KlassImpl trueKlass = getTheRealThing(pos);
            	final KlassWrapper delegateObjekt = trueKlass.makeWrapper(this, pos);
            	this.objects.put(pos, delegateObjekt);
            }
        }
    }

    Map<ClassFile, Klass> getObjects() {
    	makeAllWrappers();
        return this.objects;
    }
    
    StaticMethodArea lazyClone() {
    	final StaticMethodArea a;
    	try {
    		a = (StaticMethodArea) super.clone();
    	} catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    	
    	a.delegate = this;
    	a.objects = new HashMap<>();
    	
    	return a;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(); 
        buf.append("[");
        boolean isFirst = true;
        final Set<Map.Entry<ClassFile, Klass>> entries = this.objects.entrySet();
        for (Map.Entry<ClassFile, Klass> e : entries) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }
            final Klass k = e.getValue();
            final String name = e.getKey().getClassName();
            buf.append(name);
            buf.append(":");
            buf.append("{");
            buf.append(k.toString());
            buf.append("}");
        }
        buf.append("]");
        return buf.toString();
    }

    @Override
    public StaticMethodArea clone() {
        final StaticMethodArea o;
        try {
            o = (StaticMethodArea) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        final HashMap<ClassFile, Klass> objectsClone = new HashMap<>();
        for (ClassFile pos : filledPositions()) {
            objectsClone.put(pos, getTheRealThing(pos).clone());
        }
        o.objects = objectsClone;

        return o;
    }
}
