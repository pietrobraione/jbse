package jbse.mem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Reference;
import jbse.val.ReferenceArrayImmaterial;
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
public final class ReachableObjectsCollector {
    /**
     * Returns the heap positions of the objects
     * that are reachable from the roots of a 
     * {@link State} (i.e., the local variables and
     * the operands in the operand stacks, for all the
     * frames in the state's thread stack, the root
     * object and the root class, the string literals, 
     * all the {@link Instance_JAVA_CLASS}, all the 
     * {@link Instance_JAVA_CLASSLOADER}, all the 
     * {@link Instance}s of {@link java.lang.invoke.MethodType}, 
     * the main {@link Thread} and {@link ThreadGroup}).
     * 
     * @param s a {@link State}. It must not be {@code null}.
     * @param precise a {@code boolean}, if {@code true}, 
     *        then it includes in the roots for collection all 
     *        the static fields, the string literals, the classes,
     *        including the primitive ones, the classloaders, the
     *        method types, the threads and the thread groups.
     * @return a {@link Set}{@code <}{@link Long}{@code >}
     *         containing all the heap positions of the objects
     *         reachable from the collection roots.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    public Set<Long> reachable(State s, boolean precise) throws FrozenStateException {
        try {
            final boolean emptyStack = s.getStack().isEmpty();
            final Reference rootObjectReference = (emptyStack ? null : s.getRootObjectReference());
            final long rootObjectPosition = (rootObjectReference == null ? -1 : rootObjectReference instanceof ReferenceConcrete ? ((ReferenceConcrete) rootObjectReference).getHeapPosition() : s.getResolution((ReferenceSymbolic) rootObjectReference));
            final ClassFile rootClass = (emptyStack ? null : s.getRootClass());
            return reachable(s, precise, rootObjectPosition, rootClass);
        } catch (ThreadStackEmptyException e) {
            throw new UnexpectedInternalException(e);
        }
    }
    /**
     * Returns the heap positions of the objects
     * that are reachable from the roots of a 
     * {@link State} (i.e., the local variables and
     * the operands in the operand stacks, for all the
     * frames in the state's thread stack, the root
     * object and the root class, the string literals, 
     * all the {@link Instance_JAVA_CLASS}, all the 
     * {@link Instance_JAVA_CLASSLOADER}, all the 
     * {@link Instance}s of {@link java.lang.invoke.MethodType}).
     * 
     * @param s a {@link State}. It must not be {@code null}.
     * @param precise a {@code boolean}, if {@code true}, 
     *        then it includes in the roots for collection all 
     *        the static fields, the string literals, the classes,
     *        including the primitive ones, the classloaders, the
     *        method types, the threads and the thread groups.
     * @param rootObject a {@code long}. If {@code rootObject >= 0}
     *        this parameter is interpreted as the heap position of 
     *        the root object, and all its static and nonstatic 
     *        fields are also considered as roots for collection.
     * @param rootClass a {@link ClassFile}. If {@code rootClass != null}
     *        all the static fields of the root class are also considered 
     *        as roots for collection.
     * @return a {@link Set}{@code <}{@link Long}{@code >}
     *         containing all the heap positions of the objects
     *         reachable from the collection roots.
     * @throws FrozenStateException if {@code s} is frozen.
     */
    private Set<Long> reachable(State s, boolean precise, long rootObject, ClassFile rootClass) throws FrozenStateException {
        if (s == null) {
            throw new NullPointerException();
        }
        
        final HashSet<Long> reachable = new HashSet<>();
        
        //if the state is stuck, possibly adds the return
        //value and/or the thrown exception
        if (s.isStuck()) {
    		addIfReference(reachable, s, s.getStuckException());
    		addIfReference(reachable, s, s.getStuckReturn());
        }
        
        //possibly adds the root object and its static fields
        if (rootObject >= 0) {
            reachable.add(rootObject);
            final ClassFile rootObjectClass = s.getObject(new ReferenceConcrete(rootObject)).getType();
            final Klass k = s.getKlass(rootObjectClass);
            final Map<Signature, Variable> fields = k.fields();
            for (Variable var : fields.values()) {
                final Value v = var.getValue();
                addIfReference(reachable, s, v);
            }
        }
        
        //possibly adds the root class' static fields
        if (rootClass != null) {
            final Klass k = s.getKlass(rootClass);
            final Map<Signature, Variable> fields = k.fields();
            for (Variable var : fields.values()) {
                final Value v = var.getValue();
                addIfReference(reachable, s, v);
            }
        }
        
        //visits the path condition
        for (Clause c : s.getPathCondition()) {
            if (c instanceof ClauseAssumeReferenceSymbolic) {
                final ReferenceSymbolic r = ((ClauseAssumeReferenceSymbolic) c).getReference();
                addIfReference(reachable, s, r);
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
        if (precise) {
            final Map<ClassFile, Klass> staticMethodArea = s.getStaticMethodArea();
            for (Klass k : staticMethodArea.values()) {
                final Map<Signature, Variable> fields = k.fields();
                for (Variable var : fields.values()) {
                    final Value v = var.getValue();
                    addIfReference(reachable, s, v);
                }
            }
        }
        
        //possibly adds the objects in the state's object dictionary
        if (precise) {
            s.getObjectsInDictionary().stream()
                .filter(r -> !s.isNull(r))
                .map(ReferenceConcrete::getHeapPosition)
                .forEachOrdered(reachable::add);
        }
        
        //possibly adds the main thread and thread group
        if (precise) {
        	reachable.add(s.getMainThread().getHeapPosition());
        	reachable.add(s.getMainThreadGroup().getHeapPosition());
        }

        //closes reachable
        HashSet<Long> toVisit = new HashSet<>(reachable);
        while (true) {
            final HashSet<Long> toVisitNext = new HashSet<>();
            for (long nextObject : toVisit) {
                final Objekt o = s.getObject(new ReferenceConcrete(nextObject));
                final Map<Signature, Variable> fields = o.fields();
                for (Variable var : fields.values()) {
                    final Value v = var.getValue();
                    addIfReferenceAndMarkNext(reachable, toVisitNext, s, v);
                }
                if (o instanceof Array) {
                    final Array a = (Array) o;
                    for (Array.AccessOutcomeIn entry : a.values()) {
                        final Value v;
                        if (entry instanceof Array.AccessOutcomeInInitialArray) {
                            v = ((Array.AccessOutcomeInInitialArray) entry).getInitialArray();
                        } else { //(entry instanceof Array.AccessOutcomeInValue) 
                            v = ((Array.AccessOutcomeInValue) entry).getValue();
                        }
                        addIfReferenceAndMarkNext(reachable, toVisitNext, s, v);
                    }
                }
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
            } else if (ref instanceof ReferenceArrayImmaterial) {
            	//do nothing: the reference does not refer (yet) to any object
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
