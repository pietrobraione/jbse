package jbse.apps;

import static jbse.apps.Util.LINE_SEP;
import static jbse.apps.Util.FILE_SEP;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.mem.Frame;
import jbse.mem.HeapObjekt;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.ReachableObjectsCollector;
import jbse.mem.SnippetFrameWrap;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Any;
import jbse.val.DefaultValue;
import jbse.val.Expression;
import jbse.val.KlassPseudoReference;
import jbse.val.NarrowingConversion;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicAtomic;
import jbse.val.ReferenceArrayImmaterial;
import jbse.val.ReferenceConcrete;
import jbse.val.Primitive;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.ReferenceSymbolicAtomic;
import jbse.val.ReferenceSymbolicLocalVariable;
import jbse.val.ReferenceSymbolicMemberArray;
import jbse.val.ReferenceSymbolicMemberField;
import jbse.val.ReferenceSymbolicMemberMapKey;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.ValueVisitor;
import jbse.val.WideningConversion;

/**
 * A {@link Formatter} which produces a complex, fully 
 * descriptive rendition of a {@link State}.
 * 
 * @author Pietro Braione
 */
public final class StateFormatterText implements Formatter {
	private final List<Path> srcPath;
    private final boolean fullPrint;
    private StringBuilder output = new StringBuilder();


    public StateFormatterText(List<Path> srcPath, boolean fullPrint) {
        this.srcPath = new ArrayList<>(srcPath);
        this.fullPrint = fullPrint;
    }

    @Override
    public void formatState(State s) {
        try {
        	final Set<Long> reachable = (this.fullPrint ? null : new ReachableObjectsCollector().reachable(s, false));
			formatState(s, this.output, this.srcPath, this.fullPrint, reachable, true, "\t", "");
		} catch (FrozenStateException e) {
			this.output.delete(0, this.output.length());
		}
    }

    @Override
    public String emit() {
        return this.output.toString();
    }

    @Override
    public void cleanup() {
        this.output = new StringBuilder();
    }

    private static void formatState(State state, StringBuilder sb, List<Path> srcPath, boolean fullPrint, Set<Long> reachable, boolean breakLines, String indentTxt, String indentCurrent) 
    throws FrozenStateException {
        final String lineSep = (breakLines ? LINE_SEP : "");
        sb.append(state.getBranchIdentifier()); sb.append("["); sb.append(state.getSequenceNumber()); sb.append("] "); sb.append(lineSep);
        if (state.isStuck()) {
            sb.append("Leaf state");
            if (state.getStuckException() != null) {
                sb.append(", raised exception: "); sb.append(state.getStuckException().toString());
            } else if (state.getStuckReturn() != null) {
                sb.append(", returned value: "); sb.append(state.getStuckReturn().toString());
            } //else, append nothing
            sb.append(lineSep);
        } else if (state.getStackSize() == 0) {
        	sb.append("(empty stack)"); sb.append(lineSep);
        } else {
            try {
                sb.append("Method signature: "); sb.append(state.getCurrentMethodSignature()); sb.append(lineSep);
                sb.append("Program counter: "); sb.append(state.getCurrentProgramCounter()); sb.append(lineSep);
                final BytecodeFormatter bfmt = new BytecodeFormatter();
                sb.append("Next bytecode: "); sb.append(bfmt.format(state)); sb.append(lineSep); 
                sb.append("Source line: "); sourceLine(state.getCurrentFrame(), sb, srcPath); sb.append(lineSep);
            } catch (ThreadStackEmptyException e) {
            	//this should never happen
            	throw new UnexpectedInternalException(e);
            }
        }
        sb.append("Path condition: "); formatPathCondition(state, sb, fullPrint, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep);
        sb.append("Static store: {"); sb.append(lineSep); formatStaticMethodArea(state, sb, fullPrint, reachable, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep); sb.append("}"); sb.append(lineSep);
        sb.append("Heap: {"); sb.append(lineSep); formatHeap(state, sb, fullPrint, reachable, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep); sb.append("}"); sb.append(lineSep);
        if (state.getStackSize() > 0) {
            sb.append("Stack: {"); sb.append(lineSep); formatStack(state, sb, srcPath, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep); sb.append("}");
        }
        sb.append(lineSep);
    }
    
    private static void formatPathCondition(State s, StringBuilder sb, boolean fullPrint, boolean breakLines, String indentTxt, String indentCurrent) 
    throws FrozenStateException {
        final String lineSep = (breakLines ? LINE_SEP : "");
        final StringBuilder expression = new StringBuilder();
        final StringBuilder where = new StringBuilder();
        boolean doneFirstExpression = false;
        boolean doneFirstWhere = false;
        HashSet<String> doneSymbols = new HashSet<String>();
        for (Clause c : s.getPathCondition()) {
            if (c instanceof ClauseAssume) {
                expression.append(doneFirstExpression ? (" &&" + lineSep) : ""); expression.append(indentCurrent);
                doneFirstExpression = true;
                final Primitive cond = ((ClauseAssume) c).getCondition();
                formatValue(s, expression, cond);
                final StringBuilder expressionWhereCondition = new StringBuilder();
                final boolean some = formatValueForPathCondition(cond, expressionWhereCondition, breakLines, indentTxt, indentCurrent, doneSymbols);
                if (some) {
                    where.append(doneFirstWhere ? (" &&" + lineSep) : ""); where.append(indentCurrent); where.append(expressionWhereCondition);
                    doneFirstWhere = true;
                } //else does nothing
            } else if (c instanceof ClauseAssumeReferenceSymbolic) {
                expression.append(doneFirstExpression ? (" &&" + lineSep) : ""); expression.append(indentCurrent);
                doneFirstExpression = true;
                final ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) c).getReference(); 
                expression.append(ref.toString()); expression.append(" == ");
                if (s.isNull(ref)) {
                    expression.append("null");
                } else {
                    final ReferenceSymbolic tgtOrigin = s.getObject(ref).getOrigin();
                    expression.append("Object["); expression.append(s.getResolution(ref)); expression.append("] ("); expression.append(ref.equals(tgtOrigin) ? "fresh" : ("aliases " + tgtOrigin)); expression.append(")");
                }
                final StringBuilder referenceFormatted = new StringBuilder();
                final boolean someText = formatValueForPathCondition(ref, referenceFormatted, breakLines, indentTxt, indentCurrent, doneSymbols); 
                if (someText) {
                    if (doneFirstWhere) {
                        where.append(" &&"); where.append(lineSep);
                    }
                    where.append(indentCurrent); where.append(referenceFormatted);
                    doneFirstWhere = true;
                }
            } else if (c instanceof ClauseAssumeClassInitialized) {
                if (fullPrint) {
                    expression.append(doneFirstExpression ? (" &&" + lineSep) : ""); expression.append(indentCurrent);
                    doneFirstExpression = true;
                    expression.append(c.toString());
                }
            } else { //(c instanceof ClauseAssumeClassNotInitialized)
                expression.append(doneFirstExpression ? (" &&" + lineSep) : ""); expression.append(indentCurrent);
                doneFirstExpression = true;
                expression.append(c.toString());
            }
        }
        if (expression.length() > 0) {
            sb.append(lineSep); sb.append(expression);
        }
        if (where.length() > 0) {
            sb.append(lineSep); sb.append(indentCurrent); sb.append("where:"); sb.append(lineSep); sb.append(where);
        }
    }

    private static boolean formatExpressionForPathCondition(Expression e, StringBuilder sb, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
        final Primitive firstOp = e.getFirstOperand();
        final Primitive secondOp = e.getSecondOperand();
        boolean someFirstOp = false;
        if (firstOp != null) {
            someFirstOp = formatValueForPathCondition(firstOp, sb, breakLines, indentTxt, indentCurrent, done);
        }
        final StringBuilder second = new StringBuilder();
        final boolean someSecondOp = formatValueForPathCondition(secondOp, second, breakLines, indentTxt, indentCurrent, done);
        if (!someFirstOp || !someSecondOp) {
            //does nothing
        } else {
            final String lineSep = (breakLines ? LINE_SEP : "");
            sb.append(" &&"); sb.append(lineSep); sb.append(indentCurrent);
        }
        sb.append(second);
        return (someFirstOp || someSecondOp);
    }

    private static boolean formatValueForPathCondition(Value v, StringBuilder sb, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
        if (v instanceof Expression) {
            return formatExpressionForPathCondition((Expression) v, sb, breakLines, indentTxt, indentCurrent, done);
        } else if (v instanceof PrimitiveSymbolicAtomic || v instanceof ReferenceSymbolicAtomic) {
            if (done.contains(v.toString())) {
                return false;
            } else {
                done.add(v.toString());
                sb.append(v.toString()); sb.append(" == "); sb.append(((Symbolic) v).asOriginString());
                return true;
            }
        } else if (v instanceof PrimitiveSymbolicApply) {
            return formatFunctionApplicationForPathCondition((PrimitiveSymbolicApply) v, sb, breakLines, indentTxt, indentCurrent, done);
        } else if (v instanceof ReferenceSymbolicApply) {
            return formatFunctionApplicationForPathCondition((ReferenceSymbolicApply) v, sb, breakLines, indentTxt, indentCurrent, done);
        } else if (v instanceof WideningConversion) {
            final WideningConversion pWiden = (WideningConversion) v;
            return formatValueForPathCondition(pWiden.getArg(), sb, breakLines, indentTxt, indentCurrent, done);
        } else if (v instanceof NarrowingConversion) {
            final NarrowingConversion pNarrow = (NarrowingConversion) v;
            return formatValueForPathCondition(pNarrow.getArg(), sb, breakLines, indentTxt, indentCurrent, done);
        } else { //(v instanceof DefaultValue || v instanceof Any || v instanceof Simplex || v instanceof Term || v instanceof ReferenceConcrete || 
                 // v instanceof ReferenceArrayImmaterial || v instanceof KlassPseudoReference)
            return false;
        }
    }

    private static boolean formatFunctionApplicationForPathCondition(PrimitiveSymbolicApply a, StringBuilder sb, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
        boolean some = false;
        boolean firstDone = false;
        final String lineSep = (breakLines ? LINE_SEP : "");
        for (Value v : a.getArgs()) {
            final StringBuilder arg = new StringBuilder();
            final boolean argSome = formatValueForPathCondition(v, arg, breakLines, indentTxt, indentCurrent, done);
            some = some || argSome;
            if (argSome) {
            	if (firstDone) {
            		sb.append(" &&"); sb.append(lineSep); sb.append(indentCurrent); 
            	} else {
            		firstDone = true;
            	}
            	sb.append(arg);
            }
        }
        return some;
    }
    
    private static boolean formatFunctionApplicationForPathCondition(ReferenceSymbolicApply a, StringBuilder sb, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
        boolean first = true;
        boolean some = false;
        final String lineSep = (breakLines ? LINE_SEP : "");
        for (Value v : a.getArgs()) {
            final StringBuilder arg = new StringBuilder();
            final boolean argSome = formatValueForPathCondition(v, arg, breakLines, indentTxt, indentCurrent, done);
            some = some || argSome;
            if (argSome) {
                //does nothing
            } else { 
                if (!first) {
                    sb.append(" &&"); sb.append(lineSep); sb.append(indentCurrent);
                }
                sb.append(arg);
                first = false;
            }
        }
        return some;
    }
    
    //copied from java.util.stream.Collectors
    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }


    private static void formatHeap(State s, StringBuilder sb, boolean fullPrint, Set<Long> reachable, boolean breakLines, String indentTxt, String indentCurrent) 
    throws FrozenStateException {
        final String lineSep = (breakLines ? LINE_SEP : "");
        final Map<Long, Objekt> h = s.getHeap();
        final Set<Map.Entry<Long, Objekt>> entries;
        if (fullPrint) {
            entries = h.entrySet();
        } else {
            entries = h.entrySet().stream()
                      .filter(e -> reachable.contains(e.getKey()))
                      .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), throwingMerger(), TreeMap::new)).entrySet();
        }
        final int heapSize = h.size();
        sb.append(indentCurrent);
        int j = 0;
        for (Map.Entry<Long, Objekt> e : entries) {
            Objekt o = e.getValue();
            if (o != null) {
            	sb.append("Object["); sb.append(e.getKey()); sb.append("]: "); sb.append("{");
            	formatObject(s, sb, o, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep);
            	sb.append(indentCurrent); sb.append("}");
            	if (j < heapSize - 1) {
            		sb.append(lineSep); sb.append(indentCurrent);
            	}
            }
            j++;
        }
    }
    
    private static class ValueClassMentionDetector implements ValueVisitor {
    	private Map.Entry<ClassFile, Klass> e;
    	private boolean mentionsClass;
    	
    	ValueClassMentionDetector(Map.Entry<ClassFile, Klass> e) {
    		this.e = e;
    	}
    	
    	boolean mentionsClass() {
    		return this.mentionsClass;
    	}

    	@Override
    	public void visitAny(Any x) {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitExpression(Expression e) {
    		try {
    			if (e.isUnary()) {
    				e.getOperand().accept(this);
    			} else {
    				e.getFirstOperand().accept(this);
    				if (!this.mentionsClass) {
    					e.getSecondOperand().accept(this);
    				}
    			}
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
    		try {
    			for (Value v : x.getArgs()) {
    				v.accept(this);
    				if (this.mentionsClass) {
    					return;
    				}
    			}
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitSimplex(Simplex x) {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitTerm(Term x) throws Exception {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitNarrowingConversion(NarrowingConversion x) {
    		try {
    			x.getArg().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitWideningConversion(WideningConversion x) {
    		try {
    			x.getArg().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitReferenceArrayImmaterial(ReferenceArrayImmaterial x) {
    		//the class of the immaterial reference is not displayed, but we
    		//save it nevertheless
    		this.mentionsClass = (x.getArrayType().equals(this.e.getKey())); 
    	}

    	@Override
    	public void visitReferenceConcrete(ReferenceConcrete x) {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitKlassPseudoReference(KlassPseudoReference x) {
    		this.mentionsClass = (x.getClassFile().equals(this.e.getKey())); 
    	}

    	@Override
    	public void visitReferenceSymbolicApply(ReferenceSymbolicApply x) {
    		try {
    			for (Value v : x.getArgs()) {
    				v.accept(this);
    				if (this.mentionsClass) {
    					return;
    				}
    			}
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitReferenceSymbolicLocalVariable(ReferenceSymbolicLocalVariable x) {
    		this.mentionsClass = false;
    	}

    	@Override
    	public void visitReferenceSymbolicMemberArray(ReferenceSymbolicMemberArray x) {
    		try {
    			x.getContainer().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitReferenceSymbolicMemberField(ReferenceSymbolicMemberField x) {
    		try {
    			x.getContainer().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitReferenceSymbolicMemberMapKey(ReferenceSymbolicMemberMapKey x) {
    		try {
    			x.getContainer().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitReferenceSymbolicMemberMapValue(ReferenceSymbolicMemberMapValue x) {
    		try {
    			x.getContainer().accept(this);
    		} catch (RuntimeException exc) {
    			throw exc;
    		} catch (Exception exc) {
    			//cannot happen;
    		}
    	}

    	@Override
    	public void visitDefaultValue(DefaultValue x) {
    		this.mentionsClass = false;
    	}            	        				  
    }

    private static void formatStaticMethodArea(State state, StringBuilder sb, boolean fullPrint, Set<Long> reachable, boolean breakLines, String indentTxt, String indentCurrent) 
    throws FrozenStateException {
        final String lineSep = (breakLines ? LINE_SEP : "");
        final Map<ClassFile, Klass> a = state.getStaticMethodArea();
        final Set<Map.Entry<ClassFile, Klass>> entries;
        if (fullPrint) {
            entries = a.entrySet();
        } else {
        	entries = a.entrySet().stream()
        	          .filter(e -> {
        	        	  try {
        	        		  for (long pos : reachable) {
        	        			  final HeapObjekt ho = state.getObject(new ReferenceConcrete(pos));
        	        			  if (ho.getType().equals(e.getKey())) {
        	        				  return true;
        	        			  }
        	        			  for (Variable var : ho.fields().values()) {
        	        				  final ValueClassMentionDetector v = new ValueClassMentionDetector(e);
        	        				  var.getValue().accept(v);
        	        				  if (v.mentionsClass()) {
        	        					  return true;
        	        				  }
        	        			  }
        	        		  }
        	        	  } catch (RuntimeException exc) {
        	        		  throw exc;
        	        	  } catch (Exception exc) {
        	        		  throw new RuntimeException(exc);
        	        	  }
        	        	  return false;
        	          }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(), throwingMerger(), TreeMap::new)).entrySet();
        }
        sb.append(indentCurrent);
        boolean doneFirst = false;
        for (Map.Entry<ClassFile, Klass> ee : entries) {
            final Klass k = ee.getValue();
            if (k.getStoredFieldSignatures().size() > 0) { //only klasses with fields will be printed
                if (doneFirst) {
                    sb.append(lineSep); sb.append(indentCurrent);
                }
                doneFirst = true;
                final ClassFile c = ee.getKey();
                sb.append("Class[("); sb.append(c.getDefiningClassLoader()); sb.append(", "); sb.append(c.getClassName()); sb.append(")]: {");
                formatObject(state, sb, k, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep);
                sb.append(indentCurrent); sb.append("}");
            }
        }
    }

    private static void formatObject(State s, StringBuilder sb, Objekt o, boolean breakLines, String indentTxt, String indentCurrent) {
        final String lineSep = (breakLines ? LINE_SEP : "");
        if (o.getOrigin() != null) {
            sb.append(lineSep); sb.append(indentCurrent); sb.append("Origin: "); sb.append(o.getOrigin().asOriginString());
        }
        //explicit dispatch on type
        if (o instanceof Array) {
            final Array a = (Array) o;
            formatArray(s, sb, a, breakLines, indentTxt, indentCurrent);
        } else if (o instanceof Instance) {
            final Instance i = (Instance) o;
            formatInstance(s, sb, i, breakLines, indentTxt, indentCurrent);
        } else if (o instanceof Klass) {
            final Klass k = (Klass) o;
            formatKlass(s, sb, k, breakLines, indentTxt, indentCurrent);
        }
    }

    private static void formatArray(State s, StringBuilder sb, Array a, boolean breakLines, String indentTxt, String indentCurrent) {
        if (a.isInitial()) {
            sb.append(" (initial)");
        }
        final String lineSep = (breakLines ? LINE_SEP : "");
        sb.append(lineSep); sb.append(indentCurrent); sb.append("Type: "); sb.append(a.getType());
        sb.append(lineSep); sb.append(indentCurrent); sb.append("Length: "); sb.append(a.getLength()); 
        sb.append(lineSep); sb.append(indentCurrent); sb.append("Items: {");
        final String indentOld = indentCurrent;
        if (!a.hasSimpleRep()) {
            indentCurrent += indentTxt;
        }
        //if it is an array of chars, then it prints it in a string style
        final boolean printAsString = a.isSimple() && (a.getType().getMemberClass().getClassName().equals("char"));
        if (printAsString) {
            sb.append("\"");
        }
        boolean skipComma = true;
        boolean hasUnknownValues = false;
        boolean hasKnownValues = false;
        final StringBuilder buf = new StringBuilder();
        for (Array.AccessOutcomeIn e : a.values()) {
            if (a.hasSimpleRep()) {
                hasKnownValues = true; //the entries will surely have values
                buf.append(skipComma ? "" : ", ");
                formatArrayEntry(s, buf, e, false);
                if (!printAsString) {
                    skipComma = false;
                }
            } else {
                final StringBuilder entryFormatted = new StringBuilder();
                final boolean nothing = formatArrayEntry(s, entryFormatted, e, true);
                if (nothing) {
                    hasUnknownValues = true;
                } else {
                    hasKnownValues = true;
                    buf.append(lineSep);
                    buf.append(indentCurrent);
                    buf.append(entryFormatted);
                }
            }
        }
        sb.append(buf);
        if (printAsString) {
            sb.append("\"");
        }
        if (!a.hasSimpleRep()) {
            if (hasUnknownValues) {
                sb.append(lineSep); sb.append(indentCurrent); sb.append("(no assumption on "); sb.append(hasKnownValues ? "other " : ""); sb.append("values)");
            }
            sb.append(lineSep);
            indentCurrent = indentOld;
            sb.append(indentCurrent);
        }
        sb.append("}");
    }

    private static boolean formatArrayEntry(State s, StringBuilder sb, Array.AccessOutcomeIn e, boolean showExpression) {
        final StringBuilder val = new StringBuilder();
        if (e instanceof Array.AccessOutcomeInValue) {
            final Array.AccessOutcomeInValue eCast = (Array.AccessOutcomeInValue) e; 
            if (eCast.getValue() == null) {
                return true;
            }
            formatValue(s, val, eCast.getValue());
        } else {
            final Array.AccessOutcomeInInitialArray eCast = (Array.AccessOutcomeInInitialArray) e;
            val.append(eCast.getInitialArray().toString()); val.append("[_ + "); val.append(eCast.getOffset()); val.append("]");
        }
        if (showExpression) {
            formatValue(s, sb, e.getAccessCondition());
            sb.append(" -> ");
        }
        sb.append(val);
        return false;
    }

    private static void formatInstance(State s, StringBuilder sb, Instance i, boolean breakLines, String indentTxt, String indentCurrent) {
        final String lineSep = (breakLines ? LINE_SEP : "");
        sb.append(lineSep);
        sb.append(indentCurrent);
        sb.append("Class: ");
        sb.append(i.getType());
        int z = 0;
        for (Map.Entry<Signature, Variable> e : i.fields().entrySet()) {
            sb.append(lineSep);
            sb.append(indentCurrent);
            sb.append("Field[");
            sb.append(z);
            sb.append("]: ");
            formatVariable(s, sb, e.getValue());
            ++z;
        }
    }

    private static void formatKlass(State s, StringBuilder sb, Klass k, boolean breakLines, String indentTxt, String indentCurrent) {
        final String lineSep = (breakLines ? LINE_SEP : "");
        sb.append(lineSep);
        int z = 0;
        for (Map.Entry<Signature, Variable> e : k.fields().entrySet()) {
            if (z > 0) {
                sb.append(lineSep);
            }
            sb.append(indentCurrent);
            sb.append("Field[");
            sb.append(z);
            sb.append("]: ");
            formatVariable(s, sb, e.getValue());
            ++z;
        }
    }

    private static void formatVariable(State s, StringBuilder sb, Variable v) {
        sb.append("Name: "); sb.append(v.getName()); sb.append(", Type: "); sb.append(v.getType()); sb.append(", Value: ");
        final Value val = v.getValue(); 
        if (val == null) {
            sb.append("ERROR: no value has been assigned to this variable.");
        } else {
            formatValue(s, sb, val); sb.append(" "); formatType(sb, val);
        }
    }

    private static void formatValue(State s, StringBuilder sb, Value val) {
        if (val.getType() == Type.CHAR && val instanceof Simplex) {
            final char c = ((Character) ((Simplex) val).getActualValue()).charValue();
            if (c == '\t') {
                sb.append("\\t");
            } else if (c == '\b') {
                sb.append("\\b");
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\f') {
                sb.append("\\f");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\"') {
                sb.append("\\\"");
            } else if (c == '\'') {
                sb.append("\\\'");
            } else if (c == '\\') {
                sb.append("\\\\");
            } else if (c == '\u0000') {
                sb.append("\\u0000");
            } else {
                sb.append(c);
            }
        } else {
            sb.append(val.toString());
        } 
        if (val instanceof ReferenceSymbolic) {
            final ReferenceSymbolic ref = (ReferenceSymbolic) val;
            if (s.resolved(ref)) {
                if (s.isNull(ref)) {
                    sb.append(" == null");
                } else {
                    sb.append(" == Object["); sb.append(s.getResolution(ref)); sb.append("]");
                }
            }
        }
    }

    private static void formatType(StringBuilder sb, Value val) {
        sb.append("(type: "); sb.append(val.getType()); sb.append(")");		
    }

    private static void formatStack(State s, StringBuilder sb, List<Path> srcPath, boolean breakLines, String indentTxt, String indentCurrent) 
    throws FrozenStateException {
        final String lineSep = (breakLines ? LINE_SEP : "");
        final Iterable<Frame> stack = s.getStack();
        final int size = s.getStackSize();
        sb.append(indentCurrent);
        int j = 0;
        for (Frame f : stack) {
            sb.append("Frame["); sb.append(j); sb.append("]: {"); sb.append(lineSep); 
            formatFrame(s, sb, f, srcPath, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep);
            sb.append(indentCurrent); sb.append("}");
            if (j < size - 1) 
                sb.append(lineSep); sb.append(indentCurrent);
            j++;
        }
    }

    private static void formatFrame(State s, StringBuilder sb, Frame f, List<Path> srcPath, boolean breakLines, String indentTxt, String indentCurrent) {
        final String lineSep = (breakLines ? LINE_SEP : "");
        sb.append(indentCurrent); sb.append("Method signature: "); sb.append(f.getMethodSignature().toString());
        if (f instanceof SnippetFrameWrap) {
            sb.append(" (executing snippet, will resume with program counter "); sb.append(((SnippetFrameWrap) f).getContextFrame().getReturnProgramCounter()); sb.append(")");
        }
        sb.append(lineSep);
        sb.append(indentCurrent); sb.append("Program counter: "); sb.append(f.getProgramCounter()); sb.append(lineSep);
        sb.append(indentCurrent); sb.append("Program counter after return: "); 
        sb.append((f.getReturnProgramCounter() == Frame.UNKNOWN_PC) ? "<UNKNOWN>" : f.getReturnProgramCounter()); sb.append(lineSep);
        final ClassHierarchy hier = s.getClassHierarchy();
        final BytecodeFormatter bfmt = new BytecodeFormatter();
        sb.append(indentCurrent); sb.append("Next bytecode: "); sb.append(bfmt.format(f, hier)); sb.append(lineSep); 
        sb.append(indentCurrent); sb.append("Source line: "); sourceLine(f, sb, srcPath); sb.append(lineSep);
        sb.append(indentCurrent); sb.append("Operand Stack: {"); sb.append(lineSep); formatOperandStack(s, sb, f, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep); sb.append(indentCurrent); sb.append("}"); sb.append(lineSep);
        sb.append(indentCurrent); sb.append("Local Variables: {"); sb.append(lineSep); formatLocalVariables(s, sb, f, breakLines, indentTxt, indentCurrent + indentTxt); sb.append(lineSep); sb.append(indentCurrent); sb.append("}");
    }

    private static void formatOperandStack(State s, StringBuilder sb, Frame f, boolean breakLines, String indentTxt, String indentCurrent) {
        sb.append(indentCurrent);
        final String lineSep = (breakLines ? LINE_SEP : "");
        int i = 0;
        final int last = f.operands().size() - 1;
        for (Value v : f.operands()) {
            sb.append("Operand[");
            sb.append(i);
            sb.append("]: ");
            formatValue(s, sb, v);
            sb.append(" ");
            formatType(sb, v);
            if (i < last)  {
                sb.append(lineSep);
                sb.append(indentCurrent);
            }
            ++i;
        }
    }

    private static void formatLocalVariables(State s, StringBuilder sb, Frame f, boolean breakLines, String indentTxt, String indentCurrent) {
        sb.append(indentCurrent);
        boolean isFirst = true;
        final Map<Integer, Variable> lva = f.localVariables();
        final TreeSet<Integer> slots = new TreeSet<>(lva.keySet());
        final String lineSep = (breakLines ? LINE_SEP : "");
        for (int i : slots) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(lineSep);
                sb.append(indentCurrent);
            }
            sb.append("Variable[");
            sb.append(i);
            sb.append("]: ");
            formatVariable(s, sb, lva.get(i));
        }
    }

    private static void sourceLine(Frame f, StringBuilder sb, List<Path> srcPath) {
        int sourceRow = f.getSourceRow();
        if (sourceRow == -1) { 
            sb.append("<UNKNOWN>");
        } else { 
            sb.append("("); sb.append(sourceRow); sb.append("): ");
            final String row;
            if (srcPath == null) {
                row = null;
            } else {
                row = Util.getSrcFileRow(f.getMethodSignature().getClassName(), srcPath, FILE_SEP, sourceRow);
            }
            sb.append(row == null ? "<UNKNOWN>" : row);
        }
    }
}
