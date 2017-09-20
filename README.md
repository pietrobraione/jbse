JBSE
====


[![Project stats at https://www.openhub.net/p/JBSE](https://www.openhub.net/p/JBSE/widgets/project_thin_badge.gif)](https://www.openhub.net/p/JBSE) [![Join the chat at https://gitter.im/pietrobraione/jbse](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/pietrobraione/jbse?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Flattr this](http://button.flattr.com/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=pietro.braione&url=https%3A%2F%2Fgithub.com%2Fpietrobraione%2Fjbse)


Introduction
------------

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. If you are not sure about what "symbolic execution" is you can refer to the corresponding [Wikipedia article](http://en.wikipedia.org/wiki/Symbolic_execution) or to some [textbook](http://ix.cs.uoregon.edu/~michal/book/). But if you are really impatient, symbolic execution is to testing what symbolic equation solving is to numeric equation solving. While numeric equations, e.g. `x^2 - 2 x + 1 = 0`, have numbers as their parameters, symbolic equations, e.g. `x^2 - b x + 1 = 0` may have numbers *or symbols* as their parameters. A symbol stands for an infinite, arbitrary set of possible numeric values, e.g., `b` in the previous example stands for an arbitrary real value. Solving a symbolic equation is therefore equivalent to solving a possibly infinite set of numeric equations, that is, the set of all the equations that we obtain by replacing its symbolic parameters with arbitrary numbers. When solving an equation, be it numeric or symbolic, we may need to split cases: For example, a quadratic equation in one variable may have two, one or zero real solutions, depending on the sign of the discriminant. Solving a numeric equation means following exactly one of the possible cases, while symbolic equation solving may require to follow more than one of them. For example, the `x^2 - 2 x + 1 = 0` equation falls in the "zero discriminant" case and thus has one solution, while the `x^2 - b x + 1 = 0` equation may fall in any of the three cases depending on the possible values of `b`: If `|b| > 2` the discriminant is greater than zero and the equation has two real solutions, if `b = 2` or `b = -2` the discriminant is zero and the equation has one real solution. Finally, if `-2 < b < 2`, the discriminant is less than zero and the equation has no real solutions. Since all the three subsets for `b` are nonempty any of the three cases may hold. As a consequence, the solution of a symbolic equation is usually expressed as a set of *summaries*. A summary associates a condition on the symbolic parameters with a corresponding possible result of the equation, where the result can be a number *or* an expression in the symbols. For our running example the solution produces as summaries `|b| > 2 => x = [b + sqrt(b^2 - 4)] / 2`, `|b| > 2 => x = [b - sqrt(b^2 - 4)] / 2`, `b = 2 => x = 1`, and `b = -2 => x = -1`. Note that summaries overlap where a combination of parameters values (`|b| > 2` in the previous case) yield multiple results, and that the union of the summaries does not span the whole domain for `b`, because some values for `b` yield no result.

JBSE allows the inputs of a Java program to be either concrete values (usual Java primitive or reference values) *or symbols*. A symbol stands for an arbitrary primitive or reference value on which JBSE does not make any initial assumption. During the execution JBSE may *need* to make assumptions on the symbolic inputs, e.g. to decide whether it must follow the "then" or "else" branch of a conditional statement, or to decide whether accessing a field with a symbolic reference yields a value or raises a `NullPointerException`. In these situations JBSE splits the possible cases and analyzes *all* of them. In the case of the symbolic reference it first assumes that it is null, and continues the execution by raising the exception. At the end of the execution it backtracks and assumes the opposite, i.e., that the symbolic reference refers to some (nonnull) object. This way JBSE can explore how a Java program behaves when fed with possibly infinite classes of inputs, while testing is always limited in investigating a single behavior a time.

Installing JBSE
---------------

Right now JBSE can be installed only by building it from source.

### Building JBSE ###

This repository provides a Maven POM file that must be used to build JBSE. It should be enough to clone the git repository and then run `mvn compile`. If you work (as us) under Eclipse you must install the egit Eclipse plugin (you will find it in the Eclipse Marketplace), the m2e plugin, and possibly the m2e connector for git. We advise *not* to rely on the Eclipse default mechanism to import a Maven project from a repository, but rather to clone the git repository, then run `mvn eclipse:eclipse` to create the Eclipse project files, and finally import the project under Eclipse. Our routine is as follows:

* Clone the Github JBSE repository by switching to the git perspective, and selecting the clone button under the Git Repositories view;
* Click on the icon of the cloned repository, and right-click the Working Tree folder, then select Import Maven Projects... to import the project into the workspace and add the Maven nature to it; Now your current workspace should have a Java project named `jbse`, and if you switch back to the Java perspective and look at it in the Package you will notice that the project raises some compilation errors;
* Create a new run configuration of Maven Build type, and put eclipse:eclipse in the Goals text field, then run it;
* Finally, right-click on the JBSE project in the Package Explorer and then select Refresh: Now the JBSE project should show no errors.

The supported Maven goals are:

* clean
* compile
* test
* package

### Dependencies ###

JBSE has several build dependencies, that are automatically resolved by Maven:

* [Javassist](http://jboss-javassist.github.io/javassist/): JBSE uses Javassist at runtime for all the bytecode manipulation tasks. It is also needed at runtime.
* [JavaCC](https://javacc.org): Necessary for compiling the parser for the JBSE settings files. It is not needed at runtime.
* [JUnit](http://junit.org): Necessary for running the tests. It is not needed at runtime.

JBSE also needs to interact at runtime an external numeric solver for pruning infeasible program paths. JBSE works well with [Z3](https://github.com/Z3Prover/z3) and, to a less extent, with [CVC4](http://cvc4.cs.stanford.edu/), but any SMT solver that supports the AUFNIRA logic should work. The dependency on the solver is not handled by Maven so you need to download and install at least one of them on the machine that runs JBSE. We strongly advise to use Z3 because it is what we routinely use.

### Testing JBSE ###

When you are done you may try the (very small) JUnit test suite under the `src/test` directory by running `mvn test`. As said before, running the tests depends on the presence of JUnit 4, a dependency that Maven fixes automatically. All tests should pass, with the possible exception of the tests in the class `jbse.dec.DecisionProcedureTest` that require that you fix the path to the Z3 executable. You must modify line 54 and replace `/opt/local/bin/z3` with your local path to the Z3 executable.

### Deploying JBSE ###

Once JBSE is compiled, you can export JBSE as a jar file to be used in your project by running `mvn package`. The command will generate a `jbse-<VERSION>.jar` file in the `target` directory of the project. Note that `jbse-<VERSION>.jar` also includes the `jbse.meta` package and its subpackages, containing the API that the code under analysis can invoke to issue assertions, assumptions, and otherwise control the analysis process itself. The jar file does not include the runtime dependencies (Javassist), so you need to deploy the Javassist jar together with it. To ease deployment, Maven will also build an uber jar containing all the runtime dependencies. You will find it in the `target` directory as the file `jbse-shaded-<VERSION>.jar`. To avoid conflicts the uber jar renames the `javassist` package as `jbse.javassist`.

Using JBSE
----------

### Basic example ###

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

First, JBSE is a Java Virtual Machine. As with any Java Virtual Machine, be it symbolic or not, we must specify the classpath where JBSE will find the binaries. In this case the classpath will contain three paths, one for the target `smalldemos.ifx.IfExample` class, one for the `jbse.meta.Analysis` class that contains the `ass3rt` method invoked by `m`, and one for the Java standard library, containing the indispensable classes of the kind of `java.lang.Object`. A version of the latter that JBSE is able to use is under the `data` directory of the `jbse` project. For the other paths, note that under Eclipse all the binaries are emitted to a hidden `bin` project directory, and that the implicit execution directory of an Eclipse project is the project root directory. This means that, if the current directory is the home of the `example` project, and if the `jbse` git repository clone is at `/home/guest/jbse`, the required paths should be approximately as follows:

```Java
...
public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addClasspath("./bin", "/home/guest/jbse/target/jbse-0.8.0-SNAPSHOT.jar", "/home/guest/jbse/data/rt.jar");
        ...
    }
}
``` 

Note that `addClasspath` is a varargs method, so you can list as many path strings as you want. Next, we must specify which method JBSE must run (remember, JBSE can symbolically execute *any* method). We do it by setting the method's *signature*:

```Java
...
public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addClasspath("./bin", "/home/guest/jbse/target/jbse-0.8.0-SNAPSHOT.jar", "/home/guest/jbse/data/rt.jar");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        ...
    }
}
``` 

A method signature has three parts: The name in [internal classfile format](http://docs.oracle.com/javase/specs/jvms/se6/html/ClassFile.doc.html#14757) (`"smalldemos/ifx/IfExample"`) of the class that contains the method, a [method descriptor](http://docs.oracle.com/javase/specs/jvms/se6/html/ClassFile.doc.html#1169) listing the types of the parameters and of the return value (`"(I)V"`), and finally the name of the method (`"m"`). You can use the `javap` command, included with every JDK setup, to obtain the internal format signatures of methods: `javap -s my.Class` prints the list of all the methods in `my.Class` with their signatures in internal format.

Another essential parameter is the specification of which decision procedure JBSE must interface with in order to detect unfeasible paths. Without a decision procedure JBSE conservatively assumes that all paths are feasible, and thus report that every assertion you put in your code can be violated, be it possible or not.

```Java
...
import jbse.apps.run.RunParameters.DecisionProcedureType;

public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addClasspath("./bin", "/home/guest/jbse/target/jbse-0.8.0-SNAPSHOT.jar", "/home/guest/jbse/data/rt.jar");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath("/usr/bin/z3");
        ...
    }
}
``` 

Now that we have set the essential parameters we turn to the parameters that customize the output. First, we ask JBSE to put a copy of the output in a dump file for offline inspection. At the purpose, create an `out` directory in the example project and add the following line to the `set(RunParameters)` method:

```Java
...
import jbse.apps.run.RunParameters.DecisionProcedureType;

public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addClasspath("./bin", "/home/guest/jbse/target/jbse-0.8.0-SNAPSHOT.jar", "/home/guest/jbse/data/rt.jar");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath("/usr/bin/z3");
        p.setOutputFileName("out/runIf_z3.txt");
        ...
    }
}
``` 

Then, we specify which execution steps `Run` must show on the output. By default `Run` dumps the whole JVM state (program counter, stack, heap, static memory) after the execution of every bytecode, but we can customize the output format in many ways. For instance, we can instruct JBSE to print the current state after the execution of a *source code* statement, or to print only the last state of all the execution traces. We will stick to the latter option to minimize the amount of produced output.   

```Java
...
import jbse.apps.run.RunParameters.DecisionProcedureType;
import jbse.apps.run.RunParameters.StepShowMode;

public class RunIf {
    ...
    private static void set(RunParameters p) {
        p.addClasspath("./bin", "/home/guest/jbse/target/jbse-0.8.0-SNAPSHOT.jar", "/home/guest/jbse/data/rt.jar");
        p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
        p.setDecisionProcedureType(DecisionProcedureType.Z3);
        p.setExternalDecisionProcedurePath("/usr/bin/z3");
        p.setOutputFileName("out/runIf_z3.txt");
        p.setStepShowMode(StepShowMode.LEAVES);
    }
}
``` 

Finally, run `RunIf.main`. The `out/runIf_z3.txt` file will contain something like this:

```
This is the Java Bytecode Symbolic Executor's Run Tool (JBSE v.0.5).
Connecting to Z3 at /usr/bin/.
Starting symbolic execution of method smalldemos/ifx/IfExample:(I)V:m at Wed Dec 10 15:49:07 CET 2014.
.1.1[22] 
Leaf state
Path condition: 
	{R0} == Object[0] (fresh) &&
	pre_init(smalldemos/ifx/IfExample) &&
	{V2} > 0 &&
	pre_init(jbse/meta/Analysis)
	where:
	{R0} == {ROOT}:this &&
	{V2} == {ROOT}:x
Static store: {
	Class[smalldemos/ifx/IfExample]: {
		Origin: [smalldemos/ifx/IfExample]
		Class: KLASS
	}
	Class[jbse/meta/Analysis]: {
		Origin: [jbse/meta/Analysis]
		Class: KLASS
		Field[0]: Name: r, Type: Ljava/util/Random;, Value: {R1} (type: L)
		Field[1]: Name: mayViolateAssumptions, Type: Z, Value: {V3} (type: Z)
	}
}
Heap: {
	Object[0]: {
		Origin: {ROOT}:this
		Class: smalldemos/ifx/IfExample
		Field[0]: Name: b, Type: Z, Value: 1 (type: I)
		Field[1]: Name: a, Type: Z, Value: 1 (type: I)
	}
}

.1.1 trace is safe.
.1.2[20] 
Leaf state
Path condition: 
	{R0} == Object[0] (fresh) &&
	pre_init(smalldemos/ifx/IfExample) &&
	{V2} <= 0 &&
	pre_init(jbse/meta/Analysis)
	where:
	{R0} == {ROOT}:this &&
	{V2} == {ROOT}:x
Static store: {
	Class[smalldemos/ifx/IfExample]: {
		Origin: [smalldemos/ifx/IfExample]
		Class: KLASS
	}
	Class[jbse/meta/Analysis]: {
		Origin: [jbse/meta/Analysis]
		Class: KLASS
		Field[0]: Name: r, Type: Ljava/util/Random;, Value: {R1} (type: L)
		Field[1]: Name: mayViolateAssumptions, Type: Z, Value: {V3} (type: Z)
	}
}
Heap: {
	Object[0]: {
		Origin: {ROOT}:this
		Class: smalldemos/ifx/IfExample
		Field[0]: Name: b, Type: Z, Value: 0 (type: I)
		Field[1]: Name: a, Type: Z, Value: 0 (type: I)
	}
}

.1.2 trace is safe.
Symbolic execution finished at Wed Dec 10 15:49:07 CET 2014.
Analyzed states: 44, Analyzed traces: 2, Safe: 2, Unsafe: 0, Out of scope: 0, Violating assumptions: 0.
Elapsed time: 67 msec, Average speed: 656 states/sec, Elapsed time in decision procedure: 8 msec (11,94% of total).
```

Let's analyze the output.
* `{V0}`, `{V1}`, `{V2}`... (primitives) and `{R0}`, `{R1}`, `{R2}`... (references) are the symbolic initial values of the program inputs. To track down which initial value a symbol correspond to (what we call the symbol's *origin*) you may read the `Path condition:` section of a final symbolic state. After the `where:` row you will find a sequence of equations that associate some of the symbols with their origins. The list is incomplete, but it contains the associations we care of. For instance you can see that `{R0} == {ROOT}:this`; `{ROOT}` is a moniker for the *root frame*, i.e., the invocation frame of the initial method `m`, and `this` indicates the "this" parameter. Overall, the equation means that the origin of `{R0}` is the instance of the `IfExample` class to which the `m` message is sent. Similarly, `{V2} == {ROOT}:x` indicates that `{V2}` is the value of the `x` parameter of the initial `m(x)` invocation.
* `.1.1[22]` and `.1.2[20]` are the identifiers of the final (*leaf*) symbolic states, i.e., the states that return from the initial call to `m`. The state identifiers follow the structure of the symbolic execution. The initial state has always identifier `.1[0]`, and its immediate successors have identifiers `.1[1]`, `.1[2]`, etc. until JBSE must take some decision involving symbolic values. In this example, JBSE takes the first decision when it hits the first `if (x > 0)` statement. Since at that point of the execution `x` has still value `{V2}` and JBSE has not yet made any assumption on the possible value of `{V2}`, two outcomes are possible: Either `{V2} > 0`, and the execution takes the "then" branch, or `{V2} <= 0`, and the execution takes the "else" branch. JBSE therefore produces *two* successor states, gives them the identifiers `.1.1[0]` and `.1.2[0]`, and adds the assumptions `{V2} > 0` and `{V2} <= 0` to their respective *path conditions*. A path condition cumulates all the assumptions on the symbolic inputs that JBSE had to introduce when assuming that the execution follows a given trace. When the execution of the `.1.1` trace hits the second `if` statement, JBSE detects that the execution cannot take the "else" branch (otherwise, the path condition would be `... {V2} > 0 && ... {V2} <= 0 ...`, that has no solutions for any value of `{V2}`) and does *not* create another branch. Similarly for the `.1.2` trace.
* The two leaf states can be used to extract summaries for `m`. A summary is extracted from the path condition and the values of the variables and objects fields of a leaf state. In our example from the `.1.1[22]` leaf we can extrapolate that `{V2} > 0 => {R0}.a == 0 && {R0}.b == 0`, and from `.1.2[20]` that `{V2} <= 0 => {R0}.a == 1 && {R0}.b == 1`. This proves that for *every* possible value of the `x` parameter the execution of `m` always satisfies the assertion. 
* Beware! The dump shows the *final*, not the *initial* state of the symbolic execution. For example, `Object[0]` is the initial `this` object (because the path condition contains the clause `{R0} == Object[0]`), but the values of its fields displayed at the `.1.1[22]` and `.1.2[20]` are the values at that states. The initial, symbolic values of these fields are lost during the symbolic execution, because the code under analysis never uses them. If you want to display all the details of the initial state you should select a step show mode that also prints the initial state.
* The last rows report some statistics. Here we are interested in the total number of traces (two traces, as discussed above), the number of safe traces, i.e., the traces that pass all the assertions (also two as expected), and the number of unsafe traces, that falsify some assertion (zero as expected).

### Introducing assumptions ###

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
Symbolic execution finished at Wed Dec 10 17:01:50 CET 2014.
Analyzed states: 38, Analyzed traces: 2, Safe: 1, Unsafe: 0, Out of scope: 0, Violating assumptions: 1.
Elapsed time: 682 msec, Average speed: 55 states/sec, Elapsed time in decision procedure: 83 msec (12,17% of total).
```

The traces are still two, but now one is reported as a trace violating an assumption. Putting the `assume` invocation at the entry of `m` ensures that the useless traces are discarded as soon as possible.

In some cases this is all one needs, usually when one needs to constrain symbolic *numeric* input values. When we want to enforce assumptions on symbolic *reference* inputs, the `Analysis.assume` method is in most cases unsuitable. The reason is,  `Analysis.assume` evaluates its argument when it is invoked, which is OK for symbolic numeric inputs, but on symbolic references may cause an explosion in the number of paths. Let us consider, for example, a case where the method to be analyzed operates on a parameter `list` with class `List`. The class implements the singly-linked list data structure, where nodes have class `Node` and store values with type `Object`. Let's say we want to assume that the fourth value in the list is not `null`. If we follow the previous pattern and inject at the method entry point the statement `assume(list.header.next.next.next.value != null)`, the first thing JBSE will do when executing the method is to access `{ROOT}:list`, then `{ROOT}:list.header`, then `{ROOT}:list.header.next`, then `{ROOT}:list.header.next.next` and then `{ROOT}:list.header.next.next.next`. All these references are symbolic, and any heap access might potentially rise a `NullPointerException`. JBSE therefore must split cases and analyze what happens when any of the above references is either `null` or not `null` right at the entry of the method. In some cases this effect may cause an early combinatorial explosion of the total number of paths to be analyzed. A possible way to avoid the issue is moving the `assume` method invocation later in the code, close to the points where `{ROOT}:list.header.next.next.next.value` is accessed for the first time, a procedure that is in general complex and error-prone. It would be much better if the symbolic executor were able to evaluate these kind of assumptions lazily, when the reference is first accessed, if ever, during symbolic execution. Another issue is that in some cases we would like to express assumptions over an arbitrarily big set of symbolic references. If, for example, we would like to assume that `list` does not store `null` values at *any* position, we should specify that *all* the symbolic references `{ROOT}:list.header.value`, `{ROOT}:list.header.next.value`, `{ROOT}:list.header.next.next.value`... are not `null`. A similar problem arises if we want to specify that the singly-linked list `list` has no loops. Expressing this kind of constraints by using `Analysis.assume` is impossible in many cases, and impractical in most of the others.

JBSE implements a number of techniques that empower its users by allowing them to specify rich classes of assumptions on the heap shape, while keeping the number of analyzed traces under control. The key techniques are:

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

### More goodies ###

JBSE has many more features. You will find a comprehensive description of JBSE and instructions for using it in its user manual (currently under development). For a showcase of some of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.
