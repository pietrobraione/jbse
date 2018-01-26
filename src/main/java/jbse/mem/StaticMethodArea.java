package jbse.mem;

import java.util.HashMap;
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
public final class StaticMethodArea implements Cloneable {
    private HashMap<ClassFile, Klass> objTable;

    public StaticMethodArea() {
        this.objTable = new HashMap<>();
    }

    public boolean contains(ClassFile classFile) { 
        return this.objTable.containsKey(classFile); 
    }

    public Klass get(ClassFile classFile) {
        return this.objTable.get(classFile);
    }

    public Klass set(ClassFile classFile, Klass k) {
        return this.objTable.put(classFile, k);
    }

    public Map<ClassFile, Klass> getObjects() {
        return this.objTable;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(); 
        buf.append("[");
        boolean isFirst = true;
        final Set<Map.Entry<ClassFile, Klass>> entries = this.objTable.entrySet();
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

        //objTable
        final HashMap<ClassFile, Klass> objTableClone = new HashMap<>();
        for (Map.Entry<ClassFile, Klass> e : this.objTable.entrySet()) {
            final Klass val = e.getValue();
            objTableClone.put(e.getKey(), val.clone());
        }
        o.objTable = objTableClone;

        return o;
    }
}
