# JBSE

[![Project stats at https://www.openhub.net/p/JBSE](https://www.openhub.net/p/JBSE/widgets/project_thin_badge.gif)](https://www.openhub.net/p/JBSE) [![Join the chat at https://gitter.im/pietrobraione/jbse](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/pietrobraione/jbse?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Flattr this](http://button.flattr.com/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=pietro.braione&url=https%3A%2F%2Fgithub.com%2Fpietrobraione%2Fjbse)


## About

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. If you are not sure about what "symbolic execution" is you can refer to the corresponding [Wikipedia article](https://en.wikipedia.org/wiki/Symbolic_execution) or to some [textbook](https://ix.cs.uoregon.edu/~michal/book/). But if you are really impatient, symbolic execution is to testing what symbolic equation solving is to numeric equation solving. While numeric equations, e.g. `x^2 - 2 x + 1 = 0`, have numbers as their parameters, symbolic equations, e.g. `x^2 - b x + 1 = 0` may have numbers *or symbols* as their parameters. A symbol stands for an infinite, arbitrary set of possible numeric values, e.g., `b` in the previous example stands for an arbitrary real value. Solving a symbolic equation is therefore equivalent to solving a possibly infinite set of numeric equations, that is, the set of all the equations that we obtain by replacing its symbolic parameters with arbitrary numbers. When solving an equation, be it numeric or symbolic, we may need to split cases: For example, a quadratic equation in one variable may have two, one or zero real solutions, depending on the sign of the discriminant. Solving a numeric equation means following exactly one of the possible cases, while symbolic equation solving may require to follow more than one of them. For example, the `x^2 - 2 x + 1 = 0` equation falls in the "zero discriminant" case and thus has one solution, while the `x^2 - b x + 1 = 0` equation may fall in any of the three cases depending on the possible values of `b`: If `|b| > 2` the discriminant is greater than zero and the equation has two real solutions, if `b = 2` or `b = -2` the discriminant is zero and the equation has one real solution. Finally, if `-2 < b < 2`, the discriminant is less than zero and the equation has no real solutions. Since all the three subsets for `b` are nonempty any of the three cases may hold. As a consequence, the solution of a symbolic equation is usually expressed as a set of *summaries*. A summary associates a condition on the symbolic parameters with a corresponding possible result of the equation, where the result can be a number *or* an expression in the symbols. For our running example the solution produces as summaries `|b| > 2 => x = [b + sqrt(b^2 - 4)] / 2`, `|b| > 2 => x = [b - sqrt(b^2 - 4)] / 2`, `b = 2 => x = 1`, and `b = -2 => x = -1`. Note that summaries overlap where a combination of parameters values (`|b| > 2` in the previous case) yield multiple results, and that the union of the summaries does not span the whole domain for `b`, because some values for `b` yield no result.

JBSE allows the inputs of a Java program to be either concrete values (usual Java primitive or reference values) *or symbols*. A symbol stands for an arbitrary primitive or reference value on which JBSE does not make any initial assumption. During the execution JBSE may *need* to make assumptions on the symbolic inputs, e.g. to decide whether it must follow the "then" or "else" branch of a conditional statement, or to decide whether accessing a field with a symbolic reference yields a value or raises a `NullPointerException`. In these situations JBSE splits the possible cases and analyzes *all* of them. In the case of the symbolic reference it first assumes that it is null, and continues the execution by raising the exception. At the end of the execution it backtracks and assumes the opposite, i.e., that the symbolic reference refers to some (nonnull) object. This way JBSE can explore how a Java program behaves when fed with possibly infinite classes of inputs, while testing is always limited in investigating a single behavior a time.

## Installing JBSE

Right now JBSE can be installed only by building it from source. Formal releases will be available when JBSE will be more feature-ready and stable.

## Building JBSE

JBSE is built with Gradle. Once cloned the git repository and ensured the dependencies (see section "Dependencies"), it should be enough to run the build Gradle task by invoking `gradlew build`.

## Dependencies

JBSE has several dependencies. It must be built using a JDK version 8 - neither less, nor more. The Gradle wrapper `gradlew` included in the repository will take care to select the right version of Gradle. Gradle will automatically resolve and use the following compile-time-only dependency:

* [JavaCC](https://javacc.org) is used for compiling the parser for the JBSE settings files.
* [JUnit](http://junit.org) is used for running the test suite.

The runtime dependencies that are automatically resolved by Gradle and included in the build path are:

* The `tools.jar` library, that is part of every JDK 8 setup (note, *not* of the JRE).
* [Javassist](http://jboss-javassist.github.io/javassist/), that is used by JBSE for all the bytecode manipulation tasks.

There is an additional runtime dependencies that is not handled by Gradle so you will need to fix it manually. JBSE needs to interact at runtime with an external numeric solver for pruning infeasible program paths. JBSE works well with [Z3](https://github.com/Z3Prover/z3) and, to a less extent, with [CVC4](https://cvc4.cs.stanford.edu/), but any SMT solver that supports the AUFNIRA logic should work. Both are standalone binaries and can be installed almost everywhere. We strongly advise to use Z3 because it is what we routinely use.

## Working under Eclipse

If you want to work (as us) under Eclipse 2018-12 for Java Developers, you are lucky: All the plugins that are necessary to import JBSE under Eclipse and make it work are already present in the distribution. If you use another version of Eclipse you must install the egit and the Buildship plugins, both available in the Eclipse Marketplace. After that, you are ready to import SUSHI under Eclipse:

* To avoid conflicts we advise to import JBSE under an empty workspace.
* Be sure that the default Eclipse JRE is the JRE subdirectory of a full JDK 8 setup, *not* a standalone (i.e., not part of a JDK) JRE.
* JBSE uses the reserved `sun.misc.Unsafe` class, a thing that Eclipse forbids by default. To avoid Eclipse complaining about that you must modify the workspace preferences as follows: From the main menu choose Eclipse > Preferences... under macOS, or Window > Preferences... under Windows and Linux. On the left panel select Java > Compiler > Errors/Warnings, then on the right panel open the option group "Deprecated and restricted API", and for the option "Forbidden reference (access rules)" select the value "Warning" or "Info" or "Ignore".
* Switch to the Git perspective. If you cloned the Github JBSE repository from the command line, you can import the clone under Eclipse by clicking under the Git Repositories view the button for adding an existing repository. Otherwise you can clone the  repository by clicking the clone button, again available under the Git Repositories view. Eclipse does *not* want you to clone the repository under your Eclipse workspace, and instead wants you to follow the standard git convention of putting the git repositories in a `git` subdirectory of your home directory. If you clone the repository from a console, please follow this standard (if you clone the repository from the git perspective Eclipse will do this for you).
* Switch back to the Java perspective and from the main menu select File > Import... In the Select the Import Wizard window that pops up choose the Gradle > Existing Gradle Project wizard and press the Next button twice. In the Import Gradle Project window that is displayed, enter in the Project root directory field the path to the JBSE cloned git repository, and then press the Finish button to confirm. Now your workspace should have one Java project named `jbse`.
* Unfortunately the Buildship Gradle plugin is not able to fully configure the imported projects: As a consequence, after the import you will see some compilation errors due to the fact that the JBSE project did not generate some source files yet. Fix the situation by following this procedure: In the Gradle Tasks view double-click on the sushi > build > build task to build all the projects. Then, right-click the jbse project in the Package Explorer, and in the contextual menu that pops up select Gradle > Refresh Gradle Project. After that, you should see no more errors.

## Testing JBSE

When you are done you may try the (very small) JUnit test suite under the `src/test` directory by running `gradlew test`. As said before, running the tests depends on the presence of JUnit 4, a dependency that Gradle fixes automatically. All tests should pass, with the possible exception of the tests in the class `jbse.dec.DecisionProcedureTest` that require that you fix the path to the Z3 executable. You must modify line 46 and replace `/opt/local/bin/z3` with your local path to the Z3 executable.

## Deploying JBSE

The `gradlew build` command will produce a jar file `build/libs/jbse-<VERSION>.jar` that also includes the `jbse.meta` package and its subpackages, containing the API that the code under analysis can invoke to issue assertions, assumptions, and otherwise control the analysis process itself. The jar file does not include the runtime dependencies (Javassist and `tools.jar`), so you need to deploy them together with it. To ease deployment, Gradle will also build an uber-jar `build/libs/jbse-<VERSION>-shaded.jar` containing Javassist (but not `tools.jar`). To avoid conflicts the uber jar renames the `javassist` package as `jbse.javassist`.

## Usage

### Basic example

We now illustrate how to set up a basic symbolic execution session with JBSE. Create a new project with name `example` in the same Eclipse workspace where JBSE resides, and set its project dependencies to include JBSE. Add to the new project this class:

```Java
package smalldemos.ifx;

import static jbse.meta.Analysis.ass3rt;

public class IfExample {
    boolean a, b;
    public void m(int x) {
        if (x > 0) {
            a = true;
        } else {
            a = false;
        }
        if (x > 0) {
            b = true;
        } else {
            b = false;
        }
        ass3rt(a == b);
    }
}
```

This is the classic "double-if" example, and it illustrates a simple situation where symbolic execution needs to detect and prune infeasible paths. The assertion at the last line of `m` always holds, because if the initial value of `x` is positive, then the execution takes the two "then" branches, otherwise it takes the two "else" branches. In no cases the execution can take the "then" branch on one if statement and the "else" branch on the other, therefore `a` and `b` will be always equal, either both `true` or both `false`, and the method is correct with respect to the assertion. Note that there is no `main` function: Indeed, JBSE can execute *any* method!

The most direct way to run a symbolic execution and obtain some output about it is to use the `jbse.apps.run.Run` class. A `Run` object takes as input the specification of a Java method and runs it by assigning symbolic values to all the method parameters, including `this` and its fields. The result of the symbolic execution is printed to the console, and you may configure a `Run` object and decide what you want to see.

Now we will write a `main` method that creates a `Run` objects, configures it and starts symbolic execution. Configurations of `Run` objects are stored in `jbse.apps.run.RunParameters` objects. We therefore build a `RunParameters` object and pass it to the constructor of `Run`. Finally, we invoke `Run.run`. That's all.

```Java
package smalldemos.ifx;

import jbse.apps.run.RunParameters;
import jbse.apps.run.Run;
...

public class RunIf {
    public static void main(String[] args)	{
        final RunParameters p = new RunParameters();
        set(p);
        final Run r = new Run(p);
        r.run();
    }
	
    private static void set(RunParameters p) {
        ...
    }
}
``` 

Well, that's not *exactly* all. Which parameters should we set, and how?

First, JBSE is a Java Virtual Machine. As with any Java Virtual Machine, be it symbolic or not, we must specify the classpath where JBSE will find the binaries of the program to be executed, the so-called user classpath. In this case the user classpath will contain two paths, one for the target `smalldemos.ifx.IfExample` class, and one for the `jbse.meta.Analysis` class that contains the `ass3rt` method invoked by `m`. Note that under Eclipse all the binaries are emitted to a hidden `bin` project directory, and that the implicit execution directory of an Eclipse project is the project root directory. This means that, if the current directory is the home of the `example` project, and if the `jbse` git repository clone is at `/home/guest/jbse`, the required paths should be approximately as follows:

```Java
...
public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addUserClasspath("./bin", "/home/guest/jbse/target/classes");
        ...
    }
}
``` 

Note that `addUserClasspath` is a varargs method, so you can list as many path strings as you want. Next, we must specify which method JBSE must run (remember, JBSE can symbolically execute *any* method). We do it by setting the method's *signature*:

```Java
...
public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addUserClasspath("./bin", "/home/guest/jbse/target/classes");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        ...
    }
}
``` 

A method signature has three parts: The name in [internal classfile format](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.2.1) (`"smalldemos/ifx/IfExample"`) of the class that contains the method, a [method descriptor](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.3.3) listing the types of the parameters and of the return value (`"(I)V"`), and finally the name of the method (`"m"`). You can use the `javap` command, included with every JDK setup, to obtain the internal format signatures of methods: `javap -s my.Class` prints the list of all the methods in `my.Class` with their signatures in internal format.

Another essential parameter is the specification of which decision procedure JBSE must interface with in order to detect unfeasible paths. Without a decision procedure JBSE conservatively assumes that all paths are feasible, and thus report that every assertion you put in your code can be violated, be it possible or not. Supposing that you want to use Z3 and that the binary of Z3 is located at `/opt/local/bin/z3`, you must add the following lines:

```Java
...
import static jbse.apps.run.RunParameters.DecisionProcedureType.Z3;

public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addUserClasspath("./bin", "/home/guest/jbse/target/classes");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(Z3);
        p.setExternalDecisionProcedurePath("/opt/local/bin/z3");
        ...
    }
}
``` 

Now that we have set the essential parameters we turn to the parameters that customize the output. First, we ask JBSE to put a copy of the output in a dump file for offline inspection. At the purpose, create an `out` directory in the example project and add the following line to the `set(RunParameters)` method:

```Java
...
public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addUserClasspath("./bin", "/home/guest/jbse/target/classes");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(Z3);
        p.setExternalDecisionProcedurePath("/opt/local/bin/z3");
        p.setOutputFileName("./out/runIf_z3.txt");
        ...
    }
}
``` 

Then, we specify which execution steps `Run` must show on the output. By default `Run` dumps the whole JVM state (program counter, stack, heap, static memory) after the execution of every bytecode, but we can customize the output format in many ways. For instance, we can instruct JBSE to print the current state after the execution of a *source code* statement, or to print only the last state of all the execution traces. We will stick to the latter option by specifying the `LEAVES` step show mode option. To minimize the amount of produced output we will also select the `TEXT` state format mode, that does not print the unreachable and the standard library objects created during the execution, and omits some (scarecly interesting) path condition clauses.      

```Java
...
import static jbse.apps.run.RunParameters.StateFormatMode.TEXT;
import static jbse.apps.run.RunParameters.StepShowMode.LEAVES;

public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addUserClasspath("./bin", "/home/guest/jbse/target/classes");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(Z3);
        p.setExternalDecisionProcedurePath("/opt/local/bin/z3");
        p.setOutputFileName("./out/runIf_z3.txt");
        p.setStateFormatMode(TEXT);
        p.setStepShowMode(LEAVES);
    }
}
``` 

Finally, run `RunIf.main`. The `out/runIf_z3.txt` file will contain something like this:

```
This is the Java Bytecode Symbolic Executor's Run Tool (JBSE v.0.9.0-SNAPSHOT).
Connecting to Z3 at /opt/local/bin/z3.
Starting symbolic execution of method smalldemos/ifx/IfExample:(I)V:m at Sat Dec 15 10:06:40 CET 2018.
.1.1[22] 
Leaf state
Path condition: 
	{R0} == Object[4727] (fresh) &&
	{V3} > 0 &&
	where:
	{R0} == {ROOT}:this &&
	{V3} == {ROOT}:x
Heap: {
	Object[4727]: {
		Origin: {ROOT}:this
		Class: (2, smalldemos/ifx/IfExample)
		Field[0]: Name: b, Type: Z, Value: true (type: Z)
		Field[1]: Name: a, Type: Z, Value: true (type: Z)
	}
}

.1.1 trace is safe.
.1.2[20] 
Leaf state
Path condition: 
	{R0} == Object[4727] (fresh) &&
	{V3} <= 0 &&
	where:
	{R0} == {ROOT}:this &&
	{V3} == {ROOT}:x
Heap: {
	Object[4727]: {
		Origin: {ROOT}:this
		Class: smalldemos/ifx/IfExample
		Field[0]: Name: b, Type: Z, Value: false (type: Z)
		Field[1]: Name: a, Type: Z, Value: false (type: Z)
	}
}

.1.2 trace is safe.
Symbolic execution finished at Sat Dec 15 10:06:43 CET 2018.
Analyzed states: 729958, Analyzed traces: 2, Safe: 2, Unsafe: 0, Out of scope: 0, Violating assumptions: 0, Unmanageable: 0.
Elapsed time: 2 sec 620 msec, Average speed: 278609 states/sec, Elapsed time in decision procedure: 7 msec (0,27% of total).
```

Let's analyze the output.
* `{V0}`, `{V1}`, `{V2}`... (primitives) and `{R0}`, `{R1}`, `{R2}`... (references) are the symbolic initial values of the program inputs. To track down which initial value a symbol correspond to (what we call the symbol's *origin*) you may read the `Path condition:` section of a final symbolic state. After the `where:` row you will find a sequence of equations that associate some of the symbols with their origins. The list is incomplete, but it contains the associations we care of. For instance you can see that `{R0} == {ROOT}:this`; `{ROOT}` is a moniker for the *root frame*, i.e., the invocation frame of the initial method `m`, and `this` indicates the "this" parameter. Overall, the equation means that the origin of `{R0}` is the instance of the `IfExample` class to which the `m` message is sent. Similarly, `{V3} == {ROOT}:x` indicates that `{V2}` is the value of the `x` parameter of the initial `m(x)` invocation.
* `.1.1[22]` and `.1.2[20]` are the identifiers of the final (*leaf*) symbolic states, i.e., the states that return from the initial call to `m`. The state identifiers follow the structure of the symbolic execution. The initial state has always identifier `.1[0]`, and its immediate successors have identifiers `.1[1]`, `.1[2]`, etc. until JBSE must take some decision involving symbolic values. In this example, JBSE takes the first decision when it hits the first `if (x > 0)` statement. Since at that point of the execution `x` has still value `{V3}` and JBSE has not yet made any assumption on the possible value of `{V3}`, two outcomes are possible: Either `{V3} > 0`, and the execution takes the "then" branch, or `{V3} <= 0`, and the execution takes the "else" branch. JBSE therefore produces *two* successor states, gives them the identifiers `.1.1[0]` and `.1.2[0]`, and adds the assumptions `{V3} > 0` and `{V3} <= 0` to their respective *path conditions*. A path condition gathers all the assumptions on the symbolic inputs that JBSE had to introduce when assuming that the execution follows a given trace. When the execution of the `.1.1` trace hits the second `if` statement, JBSE detects that the execution cannot take the "else" branch (otherwise, the path condition would be `... {V3} > 0 && ... {V3} <= 0 ...`, that has no solutions for any value of `{V3}`) and does *not* create another branch. Similarly for the `.1.2` trace.
* The two leaf states can be used to extract summaries for `m`. A summary is extracted from the path condition and the values of the variables and objects fields of a leaf state. In our example from the `.1.1[22]` leaf we can extrapolate that `{V3} > 0 => {R0}.a == true && {R0}.b == true`, and from `.1.2[20]` that `{V3} <= 0 => {R0}.a == false && {R0}.b == false`. This proves that for *every* possible value of the `x` parameter the execution of `m` always satisfies the assertion. 
* Beware! The dump shows the *final*, not the *initial* state of the symbolic execution. For example, `Object[0]` is the initial `this` object (the path clause `{R0} == Object[0]` states this), but the values of its fields displayed at `.1.1[22]` and `.1.2[20]` are the values of the fields at that final states. The initial, symbolic values of these fields are lost because the code under analysis never uses them. If you want to display all the details of the initial state you should select a step show mode that also prints the initial state.
* The last rows report some statistics. Here we are interested in the total number of traces (two traces, as discussed above), the number of safe traces, i.e., the traces that pass all the assertions (also two as expected), and the number of unsafe traces, that falsify some assertion (zero as expected).

### Introducing assumptions

An area where JBSE stands apart from all the other symbolic executors is its support to specifying custom *assumptions* on the symbolic inputs. Assumptions are indispensable to express preconditions over the input parameters of a method, invariants of data structures, and in general to constrain the range of the possible values of the symbolic inputs, either to exclude meaningless inputs, or just to reduce the scope of the analysis. Let us reconsider our running example and suppose that the method `m` has a precondition stating that it cannot be invoked with a value for `x` that is less than zero. Let us also suppose that we are interested in proving the correctness of `m` alone, thus we are not interested to analyze how `m` behaves when we invoke it violating its precondition. The easiest way to constrain the initial value of `x` is by injecting at the entry point of `m` a call to the `jbse.meta.Analysis.assume` method as follows:

```Java
...
import static jbse.meta.Analysis.assume;

public class IfExample {
    boolean a, b;
    public void m(int x) {
        assume(x > 0);
        if (x > 0) {
        ...
    }
}
```

When JBSE hits an `assume` method invocation it evaluates its argument, then it either continues the execution of the trace (if `true`) or discards it and backtracks to the next trace (if `false`). With the above changes the last rows of the dump will be as follows:

```
...
.1.2 trace violates an assumption.
Symbolic execution finished at Sat Dec 15 10:26:55 CET 2018.
Analyzed states: 729950, Analyzed traces: 2, Safe: 1, Unsafe: 0, Out of scope: 0, Violating assumptions: 1, Unmanageable: 0.
Elapsed time: 2 sec 625 msec, Average speed: 278076 states/sec, Elapsed time in decision procedure: 7 msec (0,27% of total).
```

The traces are still two, but now one is reported as a trace violating an assumption. Putting the `assume` invocation at the entry of `m` ensures that the useless traces are discarded as soon as possible.

In some cases this is all one needs, usually when one needs to constrain symbolic *numeric* input values. When we want to enforce assumptions on symbolic *reference* inputs, the `Analysis.assume` method is in most cases unsuitable. The reason is,  `Analysis.assume` evaluates its argument when it is invoked, which is OK for symbolic numeric inputs, but on symbolic references may cause an explosion in the number of paths. Let us consider, for example, a case where the method to be analyzed operates on a parameter `list` with class `List`. The class implements the singly-linked list data structure, where nodes have class `Node` and store values with type `Object`. Let's say we want to assume that the fourth value in the list is not `null`. If we follow the previous pattern and inject at the method entry point the statement `assume(list.header.next.next.next.value != null)`, the first thing JBSE will do when executing the method is to access `{ROOT}:list`, then `{ROOT}:list.header`, then `{ROOT}:list.header.next`, then `{ROOT}:list.header.next.next` and then `{ROOT}:list.header.next.next.next`. All these references are symbolic, and any heap access might potentially rise a `NullPointerException`. JBSE therefore must split cases and analyze what happens when any of the above references is either `null` or not `null` right at the entry of the method. Actually, JBSE does more than this: It analyzes whether any of the above references is either alias (i.e., equal to) or not alias of *any other one*. This usually causes an early combinatorial explosion of the total number of paths to be analyzed, of which just one is pruned by the `assume` invocation. A possible way to avoid the issue is moving the `assume` later in the code, close to the points where `{ROOT}:list.header.next.next.next.value` is accessed for the first time, a procedure that is in general complex and error-prone. It would be much better if the symbolic executor were automatically able to impose the assumption's constraint and prune the trace only when, and if, the reference is first accessed during symbolic execution. Another issue is that in some cases we would like to express assumptions over an arbitrarily big set of symbolic references. If, for example, we would like to assume that `list` does not store `null` values at *any* position, we should specify that *all* the symbolic references `{ROOT}:list.header.value`, `{ROOT}:list.header.next.value`, `{ROOT}:list.header.next.next.value`... are not `null`. A similar problem arises if we want to specify that the singly-linked list `list` has no loops. Expressing this kind of constraints by using `Analysis.assume` is impossible in many cases, and impractical in almost all the others.

JBSE implements a number of techniques that empower its users by allowing them to specify rich classes of assumptions on the heap shape, while keeping the number of analyzed traces under control. Analyzing them in details is beyond the scope of this brief introduction to JBSE. We will just say that the key techniques implemented by JBSE are:

* [Conservative repOk methods](http://dx.doi.org/10.1145/1013886.1007526): By annotating with `jbse.meta.annotations.ConservativeRepOk` a parameterless method with boolean return type, JBSE will recognize it as a conservative repOk method for all the objects of that class. JBSE will execute it every time it assumes a new path condition clause. JBSE will pass as `this` parameter it a copy of the (symbolic) initial state specialized on the current path condition plus the new clause. The method must inspect the state and check whether it does not satisfy the assumption (in this case the method must return `false`) or it still might (in this case the method must *conservatively* return true).
* [LICS rules](http://dx.doi.org/10.1145/2491411.2491433): A LICS rule has a head and a tail. The head is a regular expression specifying a set of symbolic references, the tail specifies a constraint on them. For instance, the rule `{ROOT}:list.header(.next)*.value not null` specifies that all the values stored in `list` are not null, and the rule `{ROOT}:list.header(.next)* aliases nothing` forbids the `next` references of the nodes in `list` to point to the nodes whose existence JBSE assumed earlier in the trace, thus excluding the presence of loops in the chain of nodes.
* [Triggers](http://dx.doi.org/10.1145/2491411.2491433): Triggers are user-defined instrumentation methods that we ask JBSE to execute right after adding to the path condition an assumption on a symbolic reference belonging to a set specified by a regular expression. Triggers are meant to be used for updating ghost variables and enforcing assumptions that cannot be expressed by LICS rules alone. For example, if `list` has a `size` field we can enforce the precondition that `size` is equal to the number of `Node`s in the list by establishing a relationship between the initial value of `size` and the number of the nodes that JBSE progressively assumes to be present in the list during the symbolic execution. It is sufficient to inject the following instrumentation code in the `List` class:

```Java
import static jbse.meta.Analysis.assume;

public class List {
    private int size;
    private Node header;
    
    //instrumentation variables
    private int _initialSize;
    private int _initialSizeBound;
    
    //instrumentation methods
    private static void _triggerAssumeList(List l) {
        l._initialSize = l.size;   //saves the initial size for future reference
        l._initialSizeBound = 0;   //initially we do not assume anything on the number of nodes in l
        assume(l._initialSize >= l._initialSizeBound);
    }
    
    private static void _triggerAssumeListNode(List l) {
        ++l._initialSizeBound;     //we assume that there is another node
        assume(l._initialSize >= l._initialSizeBound);
    }
    
    private static void _triggerAssumeListComplete(List l) {
        assume(l._initialSize == l._initialSizeBound); //no more nodes
    }
}
```

and to instruct JBSE by the following trigger rules (the syntax is simplified for the sake of clarity):

```
{ROOT}:list expands to instanceof List triggers _triggerAssumeList({ROOT}:list)
{ROOT}:list.header(.next)* expands to instanceof Node triggers _triggerAssumeListNode({ROOT}:list)
{ROOT}:list.header(.next)* is null triggers _triggerAssumeListComplete({ROOT}:list)
```

The first trigger rule states that, when JBSE assumes the existence of the `{ROOT}:list` object it must run the `_triggerAssumeList` method. This method stores the symbolic initial list size in `_initialSize`, and initializes a counter for the assumed list nodes in `_initialSizeBound`. The second trigger rule fires the `_triggerAssumeListNode` method, that increments `_initialSizeBounds`, whenever JBSE assumes the existence of another node in the list. Both triggers enforce the invariant that the initial list size is greater or equal to the total number of list nodes assumed by JBSE. Finally, JBSE fires the `_triggerAssumeListComplete` method after assuming that the chain of list nodes is terminated by a `null`, which is tantamount to assuming that there are no more nodes in the list. The trigger enforces the initial list size to be exactly equal to the number of assumed nodes.

### More goodies

JBSE has many more features. You will find a comprehensive description of JBSE and instructions for using it in its user manual (currently under development). For a showcase of some of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.
