package jbse.val;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;

/**
 * An access to an array member through an index.
 * 
 * @author Pietro Braione
 *
 */
public final class AccessArrayMember extends AccessNonroot {
    private final Primitive index;
    private final String toString;
    private final int hashCode;

    public AccessArrayMember(Primitive index) {
        this.index = index;
        final OriginStringifier os = new OriginStringifier();
        try {
            index.accept(os);
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
        this.toString = "[" + os.result + "]";
        final int prime = 5903;
        this.hashCode = prime + ((this.index == null) ? 0 : this.index.hashCode());
    }
    
    private static String javaPrimitiveType(char type) {
        if (type == Type.BOOLEAN) {
            return "boolean";
        } else if (type == Type.BYTE) {
            return "byte";
        } else if (type == Type.CHAR) {
            return "char";
        } else if (type == Type.DOUBLE) {
            return "double";
        } else if (type == Type.FLOAT) {
            return "float";
        } else if (type == Type.INT) {
            return "int";
        } else if (type == Type.LONG) {
            return "long";
        } else if (type == Type.SHORT) {
            return "short";
        } else {
            return null;
        }
    }

    private static class OriginStringifier implements PrimitiveVisitor {
        String result = null;

        @Override
        public void visitAny(Any x) {
            this.result = x.toString();
        }

        @Override
        public void visitExpression(Expression e) throws Exception {
            if (e.isUnary()) {
                e.getOperand().accept(this);
                final String argStr = this.result;
                final Operator operator = e.getOperator();
                this.result = (operator == Operator.NEG ? "-" : operator.toString()) + " (" + argStr + ")";
            } else {
                e.getFirstOperand().accept(this);
                final String firstOperandStr = this.result;
                e.getSecondOperand().accept(this);
                final String secondOperandStr = this.result;
                this.result = "(" + firstOperandStr +") " + e.getOperator().toString() + " (" + secondOperandStr + ")";
            }
        }

        @Override
        public void visitFunctionApplication(FunctionApplication x)
        throws Exception {
            final StringBuilder b = new StringBuilder();
            b.append(x.getOperator());
            b.append('(');
            boolean firstDone = false;
            for (Primitive arg : x.getArgs()) {
                if (firstDone) {
                    b.append(", ");
                } else {
                    firstDone = true;
                }
                arg.accept(this);
                b.append(this.result);
            }
            b.append(')');
            this.result = b.toString();
        }

        @Override
        public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
            this.result = s.getOrigin().toString();
        }

        @Override
        public void visitSimplex(Simplex x) {
            this.result = x.toString();
        }

        @Override
        public void visitTerm(Term x) {
            this.result = x.toString();
        }

        @Override
        public void visitNarrowingConversion(NarrowingConversion x)
        throws Exception {
            x.getArg().accept(this);
            this.result = "(" + javaPrimitiveType(x.getType()) + ") (" + this.result + ")";
            
        }

        @Override
        public void visitWideningConversion(WideningConversion x)
        throws Exception {
            x.getArg().accept(this);
        }
        
    }
    
    public Primitive index() {
        return this.index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AccessArrayMember other = (AccessArrayMember) obj;
        if (this.index == null) {
            if (other.index != null) {
                return false;
            }
        } else if (!this.index.equals(other.index)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
