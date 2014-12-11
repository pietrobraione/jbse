JBSE
====

Introduction
------------

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. If you are not confident about what "symbolic execution" means, please read the corresponding [Wikipedia article](http://en.wikipedia.org/wiki/Symbolic_execution). But if you are really impatient, symbolic execution is to testing what symbolic equation solving is to numeric equation solving. Numeric equations, e.g. `x^2 - 2 x + 1 = 0`, have numbers as their parameters, while symbolic equations, e.g. `x^2 - b x + 1 = 0` may have numbers *or symbols* as their parameters. Symbols stand for an infinite, arbitrary set of possible values, e.g., `b` in the previous example stands for an arbitrary real value. When solving an equation, be it numeric or symbolic, we usually need to split cases: For example, second-degree equations may have two, one or zero real solutions, depending on the sign of the discriminant. Numeric equation solving follows exactly one of these possible cases, while symbolic equation solving may require following more than one of them. For example, the `x^2 - 2 x + 1 = 0` equation falls in the "zero discriminant" case and thus has one solution, while the `x^2 - b x + 1 = 0` equation may fall in any of the three cases depending on the actual value of `b`: If `|b| > 2` the discriminant is greater than zero and the equation has two real solutions, if `b = 2` or `b = -2` the discriminant is zero and the equation has one real solution. Finally, if `-2 < b < 2`, the discriminant is less than zero and the equation has no real solutions. All the three subsets for the possible values of `b` are nonempty, thus any of the three cases may hold. As a consequence, the solution of a symbolic equation is usually expressed as a set of *summaries*. A summary associates a condition on the symbolic parameters with a corresponding possible result of the equation, where the result can be a number *or* an expression in the symbols. For our running example the solution produces as summaries `|b| > 2 => x = [b + sqrt(b^2 - 4)] / 2`, `|b| > 2 => x = [b - sqrt(b^2 - 4)] / 2`, `b = 2 => x = 1`, and `b = -2 => x = -1`. Note that summaries overlap where a combination of parameters values (`|b| > 2` in the previous case) yield multiple results, and that the union of the summaries does not span the whole domain for `b`, because some values for `b` yield no result.

JBSE allows the inputs of a Java program to be either concrete values (ints, floats, even Java objects) *or symbols*. A symbol stands for an arbitrary primitive or references on which JBSE does not make any assumption. During the execution JBSE may *need* to make assumptions on the symbolic inputs, e.g. to decide whether it must follow the "then" or "else" branch of a conditional statement, or to decide whether accessing a field with a symbolic reference yields a value or raises a null pointer exception. In these situations JBSE splits the possible cases and analyzes *all* of them. In the case of the symbolic reference it first assumes that it is null, and continues the execution by raising the exception. At the end of the execution it backtracks and assumes the opposite, i.e., that the symbolic reference refers to some (nonnull) object. This way JBSE can explore how a Java program when fed with possibly infinite classes of inputs, while testing is always limited in investigating a single behavior a time.

Installing JBSE
---------------

Currently JBSE can be installed only from source. This git repository provides an Eclipse project that can be used to build it.

### Building JBSE ###

JBSE has several build dependencies, some of which are included in the `lib` subdirectory:

* [Javassist](http://www.javassist.org): JBSE uses Javassist for all the bytecode manipulation tasks. The version included in this project is version 3.4.
* [JDD](http://javaddlib.sourceforge.net/jdd/): JBSE uses JDD for storing and manipulating boolean formulas. The version included in this project is version 1.03.
* [JavaCC](https://java.net/projects/javacc/): Necessary for compiling the settings parser. Under Eclipse you can install the [Eclipse JavaCC plugin](http://eclipse-javacc.sourceforge.net), but notice that there is a bug, at least up to version 1.5.27, that prevents JavaCC files to be compiled if the "Build automatically" option is active.

JBSE interacts with an external numeric solver for pruning infeasible program paths. Currently JBSE may interact with either [Sicstus](https://sicstus.sics.se), [Z3](http://z3.codeplex.com), or [CVC3](http://www.cs.nyu.edu/acsys/cvc3/). JBSE connects to Sicstus via the Java PrologBeans library that is included with the Sicstus distribution, so you need to configure the Eclipse project classpath to point to that library. No build dependencies are necessary for Z3 and CVC3 support.

### Testing JBSE ###

Under the `tst` directory you will find a (quite small) suite of JUnit test cases. To run them you need JUnit 4.

### Deploying JBSE ###

Once the JBSE Eclipse project has been compiled, you can export JBSE as a jar file and use it in your project. You must deploy Javassist, JDD and the Java PrologBeans jars with JBSE, as these jars are used by JBSE at runtime. The `lib` directory contains copies of the jars that you may want to redistribute with JBSE. The jar must contain all the binaries, and possibly the source of the `jbse.meta.Analysis` and `jbse.meta.annotations.*` classes, if you want their javadocs in your favorite IDE.

Using JBSE
----------

### Basic example ###

We now illustrate how to set up a basic symbolic execution session with JBSE. Create a new project in the same Eclipse workspace where JBSE resides, and set its project dependencies to include JBSE. Add to the new project this class:

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

This is the classic "double-if" example, that illustrates how symbolic execution must prune infeasible paths. The assertion at the last line of `m` always holds, because if the initial value of `x` is positive, then the execution takes the two "then" branches, otherwise it takes the two "else" branches. In no cases the execution can take the "then" branch on one if statement and the "else" branch on the other, therefore `a` and `b` will be always equal, either both `true` or both `false`. Note that there is no `main` function: Indeed, JBSE can execute *any* method!

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

First, JBSE is a Java Virtual Machine. As with any Java Virtual Machine, be it symbolic or not, we must specify where the binaries to be executed are, in other words, we need to specify the classpath. In this case the classpath will contain two paths, one for the target `smalldemos.ifx.IfExample` class, and one for the `jbse.meta.Analysis` class that contains the `ass3rt` method invoked by `m`. Under Eclipse all the binaries are emitted to a hidden `bin` project directory and the implicit execution directory is the root directory of the project. When the two project directories are subdirectories of the Eclipse workspace root you can specify the required paths relative to the demo project directory as follows:

```Java
...
public class RunIf {
    ...
	private static void set(RunParameters p) {
	    p.addClasspath("./bin", "../jbse/bin");
	    ...
	}
}
``` 

Note that `addClasspath` is a varargs method, so you can list as many path strings as you want. Next, we must specify which method JBSE must run (remember, it can run any method). We do it by setting the method's *signature*:

```Java
...
public class RunIf {
    ...
	private static void set(RunParameters p) {
	    p.addClasspath("./bin", "../jbse/bin");
	    p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
	    ...
	}
}
``` 

A method signature has three parts: The name in [internal classfile format](http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#14757) (`"smalldemos/ifx/IfExample"`) of the class that contains the method, a *descriptor* listing the types of the parameters and of the return value (`"(I)V"`), and finally the name of the method (`"m"`). The syntax of method descriptors is specified [here](http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#1169).

Another hardly dispensable parameter is the specification of which decision procedure JBSE must interface with in order to filter unfeasible paths. Without it JBSE would assume all paths feasible, and thus it would erroneously report that some execution can violate the assertion. Our advice is to use either Sicstus or Z3 because the support to CVC3 is immature. 

```Java
...
import jbse.apps.run.RunParameters.DecisionProcedureType;

public class RunIf {
    ...
	private static void set(RunParameters p) {
	    p.addClasspath("./bin", "../jbse/bin");
	    p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath("/usr/bin/z3");
	    ...
	}
}
``` 

These are the indispensable parameters to perform a sensible symbolic execution. Now we add some parameters to customize the output. First, we ask JBSE to put a copy of the output in a dump file for offline inspection. At the purpose, create an `out` directory in the example project and add the following line to the `set` method:

```Java
...
import jbse.apps.run.RunParameters.DecisionProcedureType;

public class RunIf {
    ...
	private static void set(RunParameters p) {
	    p.addClasspath("./bin", "../jbse/bin");
	    p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath("/usr/bin/z3");
		p.setOutputFileName("out/runIf_z3.txt");
	    ...
	}
}
``` 

Then, we specify which execution steps `Run` must show on the output. By default `Run` dumps the whole JVM state (program counter, stack, heap, static memory) after the execution of every bytecode. We can change this behaviour and, e.g., print the current state after the execution of a *source code* statement, or to print only the last state of all the execution traces. We will stick to the latter option to minimize the produced output.   

```Java
...
import jbse.apps.run.RunParameters.StepShowMode;

public class RunIf {
    ...
	private static void set(RunParameters p) {
	    p.addClasspath("./bin", "../jbse/bin");
	    p.setMethodSignature("smalldemos/ifx/IfExample", "(I)V", "m");
		p.setDecisionProcedureType(DecisionProcedureType.Z3);
		p.setExternalDecisionProcedurePath("/usr/bin/z3");
		p.setOutputFileName("out/runIf_z3.txt");
	    p.setStepShowMode(StepShowMode.LEAVES);
	}
}
``` 

Finally, run `RunIf.main`. The `out/runIf_z3.txt` file will contain something like thist:

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
* `{V0}`, `{V1}`, `{V2}`... (primitives) and `{R0}`, `{R1}`, `{R2}`... (references) are the symbolic initial values of the program inputs. To track down which initial value a symbol correspond to (what we call the symbol's *origin*) you may read the `Path condition:` section of a final symbolic state. After the `where:` row you will find a sequence of equation that associate some of the symbols with their origins. The list is incomplete, but it contains the associations we care of. For instance you can see that `{R0} == {ROOT}:this`; `{ROOT}` is a moniker for the *root frame*, i.e., the invocation frame of the initial method `m`, and `this` indicates the "this" parameter. Overall, the equation means that the origin of `{R0}` is the instance of the `IfExample` class to which the `m` message is sent. Similarly, `{V2} == {ROOT}:x` indicates that `{V2}` is the value of the `x` parameter of the initial `m(x)` invocation.
* `.1.1[22]` and `.1.2[20]` are the identifiers of the final (*leaf*) symbolic states. The initial state has always `.1[0]` as identifier, its immediate successors have identifiers `.1[1]`, `.1[2]`, etc. until JBSE must take some decision involving symbolic values. In this example, JBSE takes the first decision when it hits the first `if (x > 0)` statement. Since at that moment `x` has still `{V2}` as value, and no assumption is done on `{V2}`, two outcomes are possible: Either `{V2} > 0`, and the execution takes the "then" branch, or `{V2} <= 0`, and the execution takes the "else" branch. JBSE therefore produces *two* successor states, gives them the identifiers `.1.1[0]` and `.1.2[0]`, and adds the assumptions `{V2} > 0` and `{V2} <= 0` to their respective *path conditions*. A path condition cumulates all the assumptions on the symbolic inputs that JBSE had to introduce along a trace. When the execution of the `.1.1[...]` trace hits the second `if` statement, JBSE detects that the execution cannot take the "else" branch (otherwise, the path condition would have shape `... {V2} > 0 && ... {V2} <= 0 ...`, that has no solutions for any value of `{V2}`) and does *not* create another branch. Similarly for the `.1.2[...]` trace.
* The two leaf states can be used to extract summaries for `m`. A summary is extracted from the path condition and the values of the variables and objects fields of a leaf state. In our example from the `.1.1[22]` leaf we can extrapolate that `{V2} > 0 => {R0}.a == 0 && {R0}.b == 0`, and from `.1.2[20]` that `{V2} <= 0 => {R0}.a == 1 && {R0}.b == 1`. This proves that for *every* possible value of the `x` parameter the execution of `m` always satisfies the assertion. 
* Beware! The dump shows the *final*, not the *initial* state of the symbolic execution. For example, although `Object[0]` is the initial "this" object (because the path condition contains the clause `{R0} == Object[0]`), the values of its fields displayed at the `.1.1[22]` and `.1.2[20]` are the final values. The initial ones have been lost during the execution because the code under analysis never uses them. If you want to display all the details of the initial state you should select a step show mode that also prints the initial state.
* The last rows report some statistics. Here we are interested in the total number of traces (two traces, as discussed above) and in the number of safe traces, i.e., the traces that pass all the assertions (also two as expected).

### Support to custom assumptions ###

An area where JBSE stands apart from all the other symbolic executors is the support to the specification of custom assumptions on symbolic inputs. Custom assumptions are indispensable to express method preconditions, data structure invariants, and in general to constrain the scope of the analysis to the relevant combinations of symbolic inputs. Let us reconsider our running example and suppose that the method `m` has a precondition stating that it cannot be invoked with a `x` value less than zero. Let us also suppose that we are interested in proving the correctness of the `m` method alone, not in combination with its possible callers that we want to leave unspecified, so we are not interested in what happens when we invoke `m` with a `x` value that violates the precondition. The easiest way to constrain the initial value of `x` is by injecting at the entry point of `m` a call to the `jbse.meta.Analysis.assume` method:

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

The traces are still two, but one is counted among those violating assumptions. Putting the `assume` invocation at the entry of `m` ensures that the useless traces are discarded as soon as possible.

In some cases this is all one needs, but usually it is not if we want to enforce assumptions on the initial structure of the heap. The `Analysis.assume` method evaluates its argument when it is invoked. As a consequence, its indiscriminate use on symbolic references may cause an explosion in the number of paths. Let us consider, for example, a case where the method to be analyzed operates on a parameter `list` with class `List`. The class implements the singly-linked list data structure, where nodes have class `Node` and store values with type `Object`. Let's say we want to assume that the fourth value in the list is not `null`. If we follow the previous pattern and inject at the method entry point the statement `assume(list.header.next.next.next.value != null)`, the first thing JBSE will do when executing the method is to access `{ROOT}:list`, then `{ROOT}:list.header`, then `{ROOT}:list.header.next`, then `{ROOT}:list.header.next.next` and then `{ROOT}:list.header.next.next.next`. All these accesses may potentially rise a `NullPointerException`, and JBSE will explore all the cases where any of the above references is null or not null. In general the explosion of the total number of paths can be combinatorial. To avoid the issue we could either move the `assume` method invocation later in the code, close to the points where `{ROOT}:list.header.next.next.next.value` is possibly accessed (a procedure that is in general complex and error-prone), or we have a way to let symbolic execution evaluate these kind of assumptions lazily, when the reference is accessed, if ever. A different but related issue is that in some cases we would like to express assumptions over an arbitrarily big set of symbolic references. If, for example, we would like to assume that `list` does not store `null` values at *any* position, we should specify that *all* the symbolic references `{ROOT}:list.header.value`, `{ROOT}:list.header.next.value`, `{ROOT}:list.header.next.next.value`... are not `null`. Expressing this kind of constraints by using `Analysis.assume` is either impractical or plainly impossible.

JBSE implements a number of techniques that empower its users by allowing the specification of rich classes of assumptions, while keeping the number of analyzed traces under control. The main techniques are:

* Conservative repOk methods: By annotating with the `jbse.meta.annotations.ConservativeRepOk` a parameterless method with boolean return type, JBSE will recognize it as a conservative repOk method for all the objects of that class. JBSE will execute it every time it assumes a new path condition clause. JBSE will pass as `this` parameter it a copy of the (symbolic) initial state specialized on the current path condition plus the new clause. The method must inspect the state and check whether it does not satisfy the assumption (in this case the method must return `false`) or it still might (in this case the method must *conservatively* return true).
* LICS rules: A LICS rule has a head and a tail. The head is a regular expression describing a set of symbolic references, the tail specifies a constraint on them. For instance, the rule `{ROOT}:list.header(.next)*.value not null` specifies that all the values stored in `list` are not null, and the rule `{ROOT}:list.header(.next)* aliases nothing` forbids the reference to the nodes in `list` to point to the nodes JBSE assumed to be present in `list` earlier, thus excluding the presence of loops in the chain of nodes.
* Triggers: A trigger is a user-defined instrumentation method that JBSE executes whenever makes some kind of assumptions on some sets of symbolic references, also expressed by means of a regular expression. The main use case for triggers is for updating ghost variables and enforcing further assumptions that cannot be expressed by plain LICS rules. For example, if the linked list has a `size` field we can establish a relationship between the initial value of this fields and the number of the nodes that JBSE assumes to be present in the list during the symbolic execution. This can be done by injecting instrumentation code in the `List` class as follows:

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

and introducing the following trigger rules (the syntax is simplified for the sake of clarity):

```
{ROOT}:list expands to instanceof List triggers List:(LList;):_triggerAssumeList
{ROOT}:list.header(.next)* expands to instanceof Node triggers List:(LList;):_triggerAssumeListNode
{ROOT}:list.header(.next)* is null triggers List:(LList;):_triggerAssumeListComplete
```

The first trigger rule states that, when JBSE assumes the existence of the `{ROOT}:list` object it must run the `_triggerAssumeList` method. This method stores the symbolic initial list size in `_initialSize`, and initializes a counter for the assumed list nodes in `_initialSizeBound`. The second trigger rule fires the `_triggerAssumeListNode` method, that increments `_initialSizeBounds`, whenever JBSE assumes the existence of another node in the list. Both triggers enforce the invariant that the initial list size is greater or equal to the total number of list nodes assumed by JBSE when they fire. Finally, JBSE fires the `_triggerAssumeListComplete` method after assuming that the chain of list nodes is terminated by a `null`. The trigger enforces the initial list size to be exactly equal to the number of assumed nodes.

### More goodies ###

JBSE has many more features than these. You will find a comprehensive description of JBSE and instructions for using it in its user manual (currently under development). For a showcase of some of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.