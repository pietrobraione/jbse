package jbse.apps.run;

import static jbse.apps.Util.formatClauses;

import java.io.PrintStream;
import java.util.List;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jbse.apps.IO;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlwSat;
import jbse.dec.exc.DecisionException;
import jbse.mem.Clause;
import jbse.mem.SwitchTable;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternative_XCMPY.Values;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XSWITCH;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.Primitive;

/**
 * {@link DecisionProcedureAlgorithms} that presents all the decision alternatives
 * to the user via console and reads from the console the list of the alternatives
 * to keep.  
 * 
 * @author Pietro Braione
 */
public final class DecisionProcedureConsole extends DecisionProcedureAlgorithms {
    private static final String CLASS_NAME = DecisionProcedureConsole.class.getName().substring(DecisionProcedureConsole.class.getName().lastIndexOf('.') + 1);
    private static final String TURNSTILE = " |-SAT- ";
    private static final String PROMPT = "? > ";
    private final PrintStream[] ps;

    public DecisionProcedureConsole(PrintStream... ps) throws InvalidInputException {
        super(new DecisionProcedureAlwSat(null)); //component is used for storing assumptions, not for deciding
        this.ps = ps.clone();
    }

    @Override
    public List<Clause> getAssumptions() {
        try {
            return super.getAssumptions();
        } catch (DecisionException e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
    }

    @Override
    public boolean isSat(Expression exp) {
        boolean retVal;
        IO.println(this.ps, CLASS_NAME + ": Please solve the following clause:");
        IO.print(this.ps, CLASS_NAME + ": "); 
        IO.print(this.ps, formatClauses(this.getAssumptions()));
        IO.println(this.ps, TURNSTILE + exp);
        IO.println(this.ps, CLASS_NAME + ": Possible results: unsatisfiable (u), satisfiable (any other).");
        String ans = IO.readln(this.ps, PROMPT);

        if (ans.equals("u")) {
            retVal = false;
        } else  {
            retVal = true;
        }
        return retVal;
    }

    @Override
    protected Outcome decide_IFX_Nonconcrete(Primitive exp, SortedSet<DecisionAlternative_IFX> result) {
        final DecisionAlternative_IFX T = DecisionAlternative_IFX.toNonconcrete(true);
        final DecisionAlternative_IFX F = DecisionAlternative_IFX.toNonconcrete(false);
        if (exp instanceof Any) {
            result.add(T);
            result.add(F);
            return Outcome.val(false, true);
        } else {
            IO.println(this.ps, CLASS_NAME + ": Please solve the following condition:");
            IO.print(this.ps, CLASS_NAME + ": "); 
            IO.print(this.ps, formatClauses(this.getAssumptions()));
            IO.println(this.ps, TURNSTILE + exp);
            IO.println(this.ps, CLASS_NAME + ": Possible results: always true (t), always false (f), both (any other).");
            String ans = IO.readln(this.ps, PROMPT);

            if (ans.equals("t")) {
                result.add(T);
            } else if (ans.equals("f")) {
                result.add(F);
            } else {
                result.add(T);
                result.add(F);
            }
            return Outcome.val(result.size() > 1, true);
        }
    }

    @Override
    protected Outcome decide_XCMPY_Nonconcrete(Primitive val1, Primitive val2, SortedSet<DecisionAlternative_XCMPY> result) {
        final DecisionAlternative_XCMPY GT = DecisionAlternative_XCMPY.toNonconcrete(Values.GT);
        final DecisionAlternative_XCMPY EQ = DecisionAlternative_XCMPY.toNonconcrete(Values.EQ);
        final DecisionAlternative_XCMPY LT = DecisionAlternative_XCMPY.toNonconcrete(Values.LT);
        if (val1 instanceof Any || val2 instanceof Any) {
            result.add(GT);
            result.add(EQ);
            result.add(LT);
            return Outcome.val(false, true);
        } else {
            IO.println(this.ps, CLASS_NAME + ": Please solve the following comparison:");
            IO.print(this.ps, CLASS_NAME + ": "); 
            IO.print(this.ps, formatClauses(this.getAssumptions()));
            IO.println(this.ps, TURNSTILE + val1.toString() + ">=<" + val2.toString());
            IO.println(this.ps, CLASS_NAME + ": Possible results: always greater (G), equals (E), always less (L), greater or equal (g), less or equal (e), different (d), all cases possible (any other).");
            String ans = IO.readln(this.ps, PROMPT);

            if (ans.equals("G")) {
                result.add(GT);
            } else if (ans.equals("E")) {
                result.add(EQ);
            } else if (ans.equals("L")) {
                result.add(LT);
            } else if (ans.equals("g")) {
                result.add(GT);
                result.add(EQ);
            } else if (ans.equals("l")) {
                result.add(EQ);
                result.add(LT);
            } else if (ans.equals("d")) {
                result.add(GT);
                result.add(LT);
            } else {
                result.add(GT);
                result.add(EQ);
                result.add(LT);
            }

            return Outcome.val(result.size() > 1, true);
        }
    }

    @Override
    protected Outcome decide_XSWITCH_Nonconcrete(Primitive val, SwitchTable tab, SortedSet<DecisionAlternative_XSWITCH> result) {
        boolean shouldRefine;
        if (val instanceof Any) {
            for (int i : tab) {
                result.add(DecisionAlternative_XSWITCH.toNonconcrete(i));
            }
            result.add(DecisionAlternative_XSWITCH.toNonconcreteDefault());
            shouldRefine = false;
        } else {
            boolean none = true;
            do {
                IO.println(this.ps, CLASS_NAME + ": Please solve the following switch:");
                IO.print(this.ps, CLASS_NAME + ": "); 
                IO.print(this.ps, formatClauses(this.getAssumptions()));
                IO.println(this.ps, TURNSTILE + val + " in ");
                int lastDone = 0;

                //prints the table
                boolean firstToDo = true;
                boolean inRange = false;
                for (int i : tab) {
                    if (firstToDo) {
                        IO.print(this.ps, "" + i);
                        firstToDo = false;
                    } else if (i == lastDone + 1) {
                        inRange = true;
                    } else {
                        IO.print(this.ps, (inRange ? " .. " + lastDone : "") + ", " + i);
                        inRange = false;
                    }
                    lastDone = i;
                }
                if (inRange) {
                    IO.print(this.ps, " .. " + lastDone);
                }

                IO.println(this.ps);
                IO.println(this.ps, CLASS_NAME + ": Provide a blank-separated list of integer values in the range,"); 
                IO.println(this.ps, CLASS_NAME + ": an out-of-range values for the 'default' case.");
                final String ans = IO.readln(this.ps, PROMPT);

                final Pattern p = Pattern.compile("(-?\\d+|d)");
                final Matcher m = p.matcher(ans);

                while (m.find()) {
                    final String s = m.group();
                    try {
                        final int i = Integer.parseInt(s);
                        for (int k : tab) {
                            if (i == k) {
                                result.add(DecisionAlternative_XSWITCH.toNonconcrete(i));
                                none = false;
                            }
                        }
                        if (none) {
                            result.add(DecisionAlternative_XSWITCH.toNonconcreteDefault());
                            none = false;
                        }
                    } catch (NumberFormatException e) {
                        none = true;
                    }
                }
                if (none) {
                    IO.println(this.ps, CLASS_NAME + ": ERROR: invalid value. Please try again."); 
                }
            } while (none);
            shouldRefine = (result.size() > 1);
        }
        return Outcome.val(shouldRefine, true);
    }
}