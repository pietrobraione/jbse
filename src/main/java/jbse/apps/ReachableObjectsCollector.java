package jbse.apps;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import jbse.bc.ClassFile;
import jbse.mem.Frame;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Value;

/**
 * A collector for the reachable objects
 * in a {@link State}'s heap.
 *  
 * @author Pietro Braione
 *
 */
public class ReachableObjectsCollector {
    /**
     * Returns the heap positions of the objects
     * that are reachable from the roots of a 
     * {@link State} (i.e., the local variables and
     * the operands in the operand stacks, for all the
     * frames in the state's thread stack).
     * 
     * @param s a {@link State}. It must not be {@code null}.
     * @param includeRootObject a {@code boolean}, if {@code true}, 
     *        then it also includes in the roots the root object 
     *        of symbolic execution and all its static fields.
     *        The root object is assumed to be the object at heap
     *        position {@code 0L}.
     * @param includeStatic a {@code boolean}, if {@code true}, 
     *        then it includes in the roots all the static 
     *        fields.
     * @return a {@link Set}{@code <}{@link Long}{@code >}
     *         containing all the heap positions of the objects
     *         reachable
     */
    public Set<Long> reachable(State s, boolean includeStatic, long rootObject, ClassFile rootClass) {
        if (s == null) {
            throw new NullPointerException();
        }
        
        final HashSet<Long> reachable = new HashSet<>();
        
        //possibly adds the root object and its static fields
        if (rootObject >= 0) {
            reachable.add(rootObject);
            final ClassFile rootObjectClass = s.getObject(new ReferenceConcrete(rootObject)).getType();
            final Klass k = s.getKlass(rootObjectClass);
            final Map<String, Variable> fields = k.fields();
            for (Variable var : fields.values()) {
                final Value v = var.getValue();
                addIfReference(reachable, s, v);
            }
        }
        
        //possibly adds the root class' static fields
        if (rootClass != null) {
            final Klass k = s.getKlass(rootClass);
            final Map<String, Variable> fields = k.fields();
            for (Variable var : fields.values()) {
                final Value v = var.getValue();
                addIfReference(reachable, s, v);
            }
        }
        
        //visits the stack
        for (Frame f : s.getStack()) {
            //variables
            final SortedMap<Integer, Variable> vars = f.localVariables();
            for (Variable var : vars.values()) {
                final Value v = var.getValue();
                addIfReference(reachable, s, v);
            }
            
            //operands
            for (Value v : f.operands()) {
                addIfReference(reachable, s, v);
            }
        }
        
        //possibly visits the static method area
        if (includeStatic) {
            Map<ClassFile, Klass> staticMethodArea = s.getStaticMethodArea();
            for (Klass k : staticMethodArea.values()) {
                final Map<String, Variable> fields = k.fields();
                for (Variable var : fields.values()) {
                    final Value v = var.getValue();
                    addIfReference(reachable, s, v);
                }
            }
        }
        
        //closes reachable
        HashSet<Long> toVisit = new HashSet<>(reachable);
        for (long nextObject : toVisit) {
            final HashSet<Long> toVisitNext = new HashSet<>();
            final Objekt o = s.getObject(new ReferenceConcrete(nextObject));
            final Map<String, Variable> fields = o.fields();
            for (Variable var : fields.values()) {
                final Value v = var.getValue();
                addIfReferenceAndMarkNext(reachable, toVisitNext, s, v);
            }
            if (toVisitNext.isEmpty()) {
                break;
            } else {
                toVisit = toVisitNext;
            }
        }
        
        return reachable;
    }
    
    private void addIfReference(Set<Long> set, State s, Value v) {
        if (v instanceof Reference) {
            final Reference ref = (Reference) v;
            if (s.isNull(ref)) {
                return;
            }
            if (ref instanceof ReferenceConcrete) {
                set.add(((ReferenceConcrete) ref).getHeapPosition());
            } else if (s.resolved((ReferenceSymbolic) ref)) {
                set.add(s.getResolution((ReferenceSymbolic) ref));
            }
        }
    }
    
    private void addIfReferenceAndMarkNext(Set<Long> reachable, Set<Long> next, State s, Value v) {
        if (v instanceof Reference) {
            final Reference ref = (Reference) v;
            if (s.isNull(ref)) {
                return;
            }
            if (ref instanceof ReferenceConcrete) {
                final long heapPosition = ((ReferenceConcrete) ref).getHeapPosition();                
                if (!reachable.contains(heapPosition)) {
                    reachable.add(heapPosition);
                    next.add(heapPosition);
                }
            } else if (s.resolved((ReferenceSymbolic) ref)) {
                final long heapPosition = s.getResolution((ReferenceSymbolic) ref);
                if (!reachable.contains(heapPosition)) {
                    reachable.add(heapPosition);
                    next.add(heapPosition);
                }
            }
        }
    }
}
