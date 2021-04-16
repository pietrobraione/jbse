package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwVerifyError;
import static jbse.algo.Util.valueString;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_THREAD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyDecide;
import jbse.algo.StrategyRefine;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.Signature;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.FrozenStateException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.KlassPseudoReference;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.PrimitiveVisitor;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidTypeException;

public final class Algo_JBSE_BASE_MAKEKLASSSYMBOLIC_DO extends Algo_INVOKEMETA_Nonbranching {
    private ClassFile classFile; //set by cookMore
    private Klass theKlass; //set by cookMore
    private ClassFile cf_JAVA_CLASS; //set by cookMore
    private ClassFile cf_JAVA_CLASSLOADER; //set by cookMore
    private ClassFile cf_JAVA_THREAD; //set by cookMore

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 2;
    }

    @Override
    protected void cookMore(State state) throws FrozenStateException, SymbolicValueNotAllowedException, ClasspathException, InterruptException {
        try {
            //gets the first (int classLoader) parameter
            final Primitive definingClassLoaderPrimitive = (Primitive) this.data.operand(0);
            if (definingClassLoaderPrimitive.isSymbolic()) {
                throw new SymbolicValueNotAllowedException("The int definingClassLoader parameter to invocation of method jbse.base.Base.makeKlassSymbolic_do cannot be a symbolic int.");
            }
            final int definingClassLoader = ((Integer) ((Simplex) definingClassLoaderPrimitive).getActualValue()).intValue();

            //gets the second (String className) parameter
            final Reference classNameRef = (Reference) this.data.operand(1);
            if (state.isNull(classNameRef)) {
                //this should never happen
                failExecution("The 'this' parameter to jbse.base.Base.makeKlassSymbolic_do method is null.");
            }
            final String className = valueString(state, classNameRef);
            if (className == null) {
                throw new SymbolicValueNotAllowedException("The String className parameter to invocation of method jbse.base.Base.makeKlassSymbolic_do cannot be a symbolic String.");
            }
            this.classFile = state.getClassHierarchy().getClassFileClassArray(definingClassLoader, className);
            if (this.classFile == null) {
                //this should never happen
                failExecution("Invoked method jbse.base.Base.makeKlassSymbolic_do for a class that has not been initialized (no classfile).");
            }
            this.theKlass = state.getKlass(this.classFile);
            if (this.theKlass == null) {
                //this should never happen
                failExecution("Invoked method jbse.base.Base.makeKlassSymbolic_do for a class that has not been initialized (no Klass object).");
            }

            this.cf_JAVA_CLASS = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASS); //surely loaded
            this.cf_JAVA_CLASSLOADER = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASSLOADER); //surely loaded
            this.cf_JAVA_THREAD = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_THREAD); //surely loaded

        } catch (ClassCastException e) {
            throwVerifyError(state, this.ctx.getCalculator());
            exitFromAlgorithm();
        }
    }

    @Override
    protected StrategyDecide<DecisionAlternative_NONE> decider() {
        return (state, result) -> {
            result.add(DecisionAlternative_NONE.instance());
            return DecisionProcedureAlgorithms.Outcome.TF;
        };
    }

    private ArrayList<ReferenceSymbolic> assumeAliases;
    private ArrayList<ReferenceSymbolic> assumeAliasesTargets;
    private ArrayList<ReferenceSymbolic> assumeExpands;
    private ArrayList<Long> assumeExpandsTargets;
    private ArrayList<ReferenceSymbolic> assumeNull;

    @Override
    protected StrategyRefine<DecisionAlternative_NONE> refiner() {
        return (state, alt) -> {
            //initializes the lists of assumptions
            this.assumeExpands = new ArrayList<>();
            this.assumeExpandsTargets = new ArrayList<>();
            this.assumeAliases = new ArrayList<>();
            this.assumeAliasesTargets = new ArrayList<>();
            this.assumeNull = new ArrayList<>();

            //makes this.theKlass symbolic
            final KlassPseudoReference origin = state.createSymbolKlassPseudoReference(state.getHistoryPoint(), this.classFile);
            this.theKlass.makeSymbolic(origin);
            this.theKlass.setIdentityHashCode(state.createSymbolIdentityHashCode(this.theKlass)); //TODO is it useless??

            //completes the transformation of this.theKlass recursively, 
            //by transforming all the references in its fields into symbolic 
            //references, and transforming the concrete objects they refer 
            //recursively. At the same time calculates all 
            //the assumptions to be pushed on the path condition
            completeSymbolic(state, this.theKlass, new HashSet<>());

            //push all the assumptions, first all expands assumptions, 
            //so we have a complete backbone, then aliases and nulls
            for (int i = 0; i < this.assumeExpands.size(); ++i) {
                state.assumeExpandsAlreadyPresent(this.assumeExpands.get(i), this.assumeExpandsTargets.get(i));
            }
            for (int i = 0; i < this.assumeAliases.size(); ++i) {
                state.assumeAliases(this.assumeAliases.get(i), this.assumeAliasesTargets.get(i));
            }
            for (int i = 0; i < this.assumeNull.size(); ++i) {
                state.assumeNull(this.assumeNull.get(i));
            }
        };
    }

    /**
     * Completes (recursively) the transformation of 
     * an object from concrete to symbolic by transforming
     * all the concrete references in it into symbolic 
     * references pointing to the same objects (which are 
     * recursively made symbolic).
     * 
     * @param state a {@link State}.
     * @param currentObjekt the {@link Objekt} that must 
     *        be transformed. It should have already been 
     *        prepared by setting its origin (by invoking
     *        {@link Objekt#makeSymbolic(ReferenceSymbolic)})
     *        and its identity hash code (by invoking 
     *        {@link Objekt#setIdentityHashCode(Primitive)}).
     * @param visited a {@link HashSet}{@code <}{@link Objekt}{@code >}, 
     *        the {@link Objekt}s that have already been transformed.
     * @throws InvalidInputException  if {@code currentObjekt} has a
     *         field with a concrete reference to an object that cannot
     *         be made symbolic (should never happen).
     * @throws InvalidTypeException when attempted to put a value with 
     *         a wrong type in an object (should never happen).
     *         
     */
    private void completeSymbolic(State state, Objekt currentObjekt, HashSet<Objekt> visited) throws InvalidInputException, InvalidTypeException {
        visited.add(currentObjekt);

        if (currentObjekt instanceof Instance || currentObjekt instanceof Klass) {
            for (Map.Entry<Signature, Variable> entry : currentObjekt.fields().entrySet()) {
                final Signature sig = entry.getKey();
                final Variable var = entry.getValue();
                final String fieldClass = sig.getClassName();
                final String fieldName = var.getName();
                final String fieldType = var.getType();
                final Value fieldValue = var.getValue();
                if (fieldValue instanceof ReferenceConcrete) {
                    final ReferenceConcrete ref = (ReferenceConcrete) fieldValue;
                    if (state.isNull(ref)) {
                        //makes the new value of the field
                        final ReferenceSymbolic newFieldValue = (ReferenceSymbolic) state.createSymbolMemberField(fieldType, fieldType, currentObjekt.getOrigin(), fieldName, fieldClass);

                        //sets the field
                        var.setValue(newFieldValue);

                        //records the resolution
                        this.assumeNull.add(newFieldValue);
                    } else {
                        //it is an expansion/alias, unless it is a non-symbolic class
                        //(java.lang.Class, java.lang.ClassLoader, java.lang.Thread):
                        //In such case we leave the field's value concrete, as it is
                        //the best approximation we can do.
                        final Objekt o = state.getObject(ref); 
                        final ClassFile oClass = o.getType();
                        if (oClass == null || 
                            (!oClass.equals(this.cf_JAVA_CLASS) && !oClass.isSubclass(this.cf_JAVA_CLASSLOADER) && !oClass.isSubclass(this.cf_JAVA_THREAD))) {
                            //makes the new value of the field
                            final ReferenceSymbolic newFieldValue = (ReferenceSymbolic) state.createSymbolMemberField(fieldType, fieldType, currentObjekt.getOrigin(), fieldName, fieldClass);

                            //sets the field
                            var.setValue(newFieldValue);

                            if (o.isSymbolic()) {
                                //records the resolution by alias
                                this.assumeAliases.add(newFieldValue);
                                this.assumeAliasesTargets.add(o.getOrigin());
                            } else {
                                //records the resolution by expansion
                                this.assumeExpands.add(newFieldValue);
                                this.assumeExpandsTargets.add(ref.getHeapPosition());
                            }

                            //determines if the referred object must also
                            //become symbolic
                            if (!visited.contains(o) && !o.isSymbolic()) {
                                //makes o symbolic
                                o.makeSymbolic(newFieldValue);
                                o.setIdentityHashCode(state.createSymbolIdentityHashCode(o));
                                //Here we suppose that it is enough even if o is an array;
                                //This means that o will not be backed by an initial array.
                                //It should however not be a problem, since o, being the
                                //transformation of a concrete object into a symbolic one,
                                //does not need to be further refined.

                                //completes transformation of o recursively
                                completeSymbolic(state, o, visited);
                            }
                        }
                    }
                } else if (fieldValue instanceof ReferenceSymbolic) {
                    //makes the new value of the field
                    //TODO fix the generic signature
                    final ReferenceSymbolic newFieldValue = (ReferenceSymbolic) state.createSymbolMemberField(fieldType, fieldType, currentObjekt.getOrigin(), fieldName, fieldClass);

                    //sets the field
                    var.setValue(newFieldValue);

                    //calculates the assumption
                    final ReferenceSymbolic ref = (ReferenceSymbolic) fieldValue;
                    if (state.isNull(ref)) {
                        //it is resolved by null
                        this.assumeNull.add(newFieldValue);
                    } else {
                        //it is resolved by alias
                        //we assert that ref must be resolved
                        //(I really can't figure out a situation where it
                        //can happen that a symbolic reference stored in
                        //a static field by a <clinit> method is not 
                        //initialized)
                        final Objekt o = state.getObject(ref);
                        if (o == null) {
                            failExecution("Unexpected unresolved symbolic reference contained in a statically initialized object (while trying to transform it into a symbolic object).");
                        }
                        this.assumeAliases.add(newFieldValue);
                        this.assumeAliasesTargets.add(o.getOrigin());

                        //the object is symbolic, thus it must not be made symbolic
                        //(it already is) nor recursively explored
                    }
                } //else, do nothing
            }
        } else if (currentObjekt instanceof Array) {
            final Array currentArray = (Array) currentObjekt;
            final ClassFile currentArrayMemberClass = currentArray.getType().getMemberClass();
            if (!currentArrayMemberClass.isPrimitiveOrVoid()) {
                final String currentArrayMemberType = currentArrayMemberClass.getInternalTypeName();
                for (Iterator<? extends AccessOutcomeIn> it = currentArray.entries().iterator(); it.hasNext(); ) {
                    final AccessOutcomeIn entry = it.next();
                    if (entry instanceof AccessOutcomeInValue) {
                        final AccessOutcomeInValue entryCast = (AccessOutcomeInValue) entry;
                        final Value entryValue = entryCast.getValue();

                        //determines the index
                        final IndexVisitor v = new IndexVisitor(currentArray.getIndex());
                        final Expression accessCondition = entryCast.getAccessCondition();
                        try {
                            accessCondition.accept(v);
                        } catch (UnexpectedInternalException e) {
                            throw e;
                        } catch (Exception e) {
                            //this should never happen
                            failExecution(e);
                        }
                        final Primitive indexActual = v.indexActual;

                        if (entryValue instanceof ReferenceConcrete) {							
                            //makes the new value of the entry
                            //TODO fix the generic signature
                            final ReferenceSymbolic newEntryValue = (ReferenceSymbolic) state.createSymbolMemberArray(currentArrayMemberType, currentArrayMemberType, currentArray.getOrigin(), indexActual);

                            //sets the entry
                            entryCast.setValue(newEntryValue);

                            //calculates the assumption
                            final ReferenceConcrete ref = (ReferenceConcrete) entryValue;
                            if (state.isNull(ref)) {
                                //it is null
                                this.assumeNull.add(newEntryValue);
                            } else {
                                //it is an expansion, unless it is a non-symbolic class
                                //(java.lang.Class, java.lang.ClassLoader, java.lang.Thread)
                                final Objekt o = state.getObject(ref); 
                                final ClassFile oClass = o.getType();
                                if (oClass == null || 
                                (!oClass.equals(this.cf_JAVA_CLASS) && !oClass.isSubclass(this.cf_JAVA_CLASSLOADER) && !oClass.isSubclass(this.cf_JAVA_THREAD))) {
                                    //records the expansion
                                    this.assumeExpands.add(newEntryValue);
                                    this.assumeExpandsTargets.add(ref.getHeapPosition());

                                    //determines if the referred object must also
                                    //become symbolic
                                    if (!visited.contains(o) && o != null && !o.isSymbolic()) {
                                        //makes o symbolic
                                        o.makeSymbolic(newEntryValue);
                                        o.setIdentityHashCode(state.createSymbolIdentityHashCode(o));
                                        //Here we suppose that it is enough even if o is an array;
                                        //This means that o will not be backed by an initial array.
                                        //It should however not be a problem, since o, being the
                                        //transformation of a concrete object into a symbolic one,
                                        //does not need to be further refined.

                                        //completes transformation of o recursively
                                        completeSymbolic(state, o, visited);
                                    }
                                }
                            }
                        } else if (entryValue instanceof ReferenceSymbolic) {
                            //makes the new value of the field
                            //TODO fix the generic signature
                            final ReferenceSymbolic newEntryValue = (ReferenceSymbolic) state.createSymbolMemberArray(currentArrayMemberType, currentArrayMemberType, currentArray.getOrigin(), indexActual);

                            //sets the entry
                            entryCast.setValue(newEntryValue);

                            //calculates the assumption
                            final ReferenceSymbolic ref = (ReferenceSymbolic) entryValue;
                            if (state.isNull(ref)) {
                                //it is null
                                this.assumeNull.add(newEntryValue);
                            } else {
                                //it is an alias: records it
                                this.assumeAliases.add(newEntryValue);
                                this.assumeAliasesTargets.add(ref);

                                //we assert that ref must be resolved
                                //(I really can't figure out a situation where it
                                //can happen that a symbolic reference stored in
                                //a static array member by a <clinit> method is not 
                                //initialized)
                                final Objekt o = state.getObject(ref);
                                if (o == null) {
                                    failExecution("Unexpected unresolved symbolic reference contained in a statically initialized object (while trying to transform it into a symbolic object).");
                                }
                                //the object is symbolic, thus it must not be made symbolic
                                //(it already is) nor recursively explored
                            }
                        } //else, do nothing
                    }
                }
            }
        }
    }

    class IndexVisitor implements PrimitiveVisitor {
        private final Term indexFormal;
        private Primitive indexActual = null;

        public IndexVisitor(Term indexFormal) {
            this.indexFormal = indexFormal;
        }

        @Override
        public void visitAny(Any x) {
            throw new UnexpectedInternalException("Found Any value as a clause of an array entry access condition.");
        }

        @Override
        public void visitExpression(Expression e) throws Exception {
            if (e.getOperator() == Operator.AND) {
                e.getFirstOperand().accept(this);
                if (this.indexActual == null) {
                    e.getSecondOperand().accept(this);
                }
            } else if (e.getOperator() == Operator.EQ && 
            e.getFirstOperand().equals(this.indexFormal)) {
                this.indexActual = e.getSecondOperand();
            } //else, do nothing
        }

        @Override
        public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
            throw new UnexpectedInternalException("Found PrimitiveSymbolicApply value as a clause of an array entry access condition.");
        }

        @Override
        public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) {
            throw new UnexpectedInternalException("Found PrimitiveSymbolicAtomic value as a clause of an array entry access condition.");
        }

        @Override
        public void visitSimplex(Simplex x) {
            //do nothing (this handles the unlikely case where true is a clause of the array access expression)
        }

        @Override
        public void visitTerm(Term x) {
            throw new UnexpectedInternalException("Found Term value as a clause of an array entry access condition.");
        }

        @Override
        public void visitNarrowingConversion(NarrowingConversion x) {
            throw new UnexpectedInternalException("Found NarrowingConversion value as a clause of an array entry access condition.");
        }

        @Override
        public void visitWideningConversion(WideningConversion x) {
            throw new UnexpectedInternalException("Found WideningConversion value as a clause of an array entry access condition.");
        }	
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> { }; //nothing to do
    }

}
