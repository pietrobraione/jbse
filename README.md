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

First, JBSE is a Java Virtual Machine. As with any Java Virtual Machine, be it symbolic or not, we must specify where the binaries to be executed are, in other words, we need to specify the classpath. In this case the classpath will contain two paths, one for the target `smalldemos.ifx.IfExample` class, and one for the `jbse.meta.Analysis` class that contains the `ass3rt` method invoked by `m`. 

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

(under Eclipse all the binaries are emitted to a hidden `bin` directory in the project). The `addClasspath` method is varargs, you can specify as many path as you want. Next, we must specify which method JBSE must run (remember, it can run any method). We do it by setting the method's *signature*:

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

A method signature has three parts: The name of the class that contains the method (`"smalldemos/ifx/IfExample"`), the method *descriptor* listing the types of the parameters and of the return value (`"(I)V"`), and finally the name of the method (`"m"`). Note that class names are specified in the [internal classfile format](http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#14757), and that the syntax of method descriptors can be found [here](http://docs.oracle.com/javase/specs/jvms/se5.0/html/ClassFile.doc.html#1169).

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

Then, we specify which execution steps `Run` must show on the output. By default `Run` prints the whole JVM state (program counter, stack, heap, static memory) after the execution of every bytecode. We can change this behaviour by specifying, e.g., to print the current state after the execution of a *source* statement, or to print only the last state of an execution trace. We will stick with the latter option to minimize the produced output.   

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

If you now run the `RunIf.main` method you will obtain an output of this kind:

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
* `{V0}`, `{V1}`, `{V2}`... (primitives) and `{R0}`, `{R1}`, `{R2}`... (references) are the symbolic initial values of the program inputs. To track down which initial value a symbol correspond to (what we call the symbol's *origin*) you may read the `Path condition:` section of the dump: after the `where:` row you will find a sequence of equation that associate symbols with their origins. The list is in general incomplete, but it contains the most important associations. For instance, you can see that `{R0} == {ROOT}:this`, where `{ROOT}` is a moniker for the *root frame*, i.e., the frame for the invocation of the initial method (`m` in this example), and `this` indicates the "this" parameter of the `m` method. Similarly, `{V2} == {ROOT}:x` indicates that `{V2}` is the value of the `x` parameter for the initial `m(x)` invocation.
* `.1.1[22]` and `.1.2[20]` are the identifiers of the symbolic states. The initial state is always `.1[0]`, its successors are `.1[1]`, `.1[2]`, etc. until some decision must be made on an expression that involves symbolic values. In this example, the first decision is taken at the first `if (x > 0)` statement. Since at that moment `x` has still `{V2}` as value, and no assumption is done on `{V2}`, two outcomes are possible: Either `{V2} > 0`, and the execution must take the "then" branch, or `{V2} <= 0`, and the execution must take the "else" branch. JBSE therefore produces *two* successor states, one by assuming `{V2} > 0` and one by assuming `{V2} <= 0`, gives them the identifiers `.1.1[0]` and `.1.2[0]` respectively, and adds the two assumptions to `{V2}` to their *path conditions*. When the execution of the `.1.1[...]` side arrives at the second if statement, JBSE detects that the execution cannot take the "else" branch (otherwise, the path condition would be the contradictory `... {V2} > 0 && ... {V2} <= 0 ...`, that has no solution for any value of `{V2}`) and does *not* create another branch. Similarly for the `.1.2[...]` side.
* The two leaf states can be used to extract summaries for `m`. A summary is extracted from the path condition of a leaf state and the values of its variables and objects fields. For example, from the `.1.1[22]` leaf we can extrapolate that `{V2} > 0 => {R0}.a == 0 && {R0}.b == 0`, and from `.1.2[20]` that `{V2} <= 0 => {R0}.a == 1 && {R0}.b == 1`. This proves that, for *every* possible value of the `x` parameter, the execution of `m` always satisfies the assertion. Beware! The leaf is the *final* state and as such contains the final values of the variables. For example, `Object[0]` is the initial "this" object (the path condition contains the clause `{R0} == Object[0]`), but the values of its fields in the leaf states are the final values. The initial values have been lost as the code does not use them before overwriting them.
* The last rows report some statistics. Here we are interested in the total number of traces (two traces, as discussed above) and in the number of safe traces, i.e., the traces that pass all the assertions (also two, as expected).

### Support to custom assumptions ###

JBSE shines in its support to specifying custom assumptions on the symbolic input values. Let us reconsider our running example and suppose that the method `m` cannot, by contract, invoked with a `x` value less than zero. We can instruct JBSE and constrain the analysis to values for `x` greater or equal to zero by means of the `jbse.meta.Analysis.assume` method:

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

The last rows of the dump change as follows:

```
...
.1.2 trace violates an assumption.
Symbolic execution finished at Wed Dec 10 17:01:50 CET 2014.
Analyzed states: 38, Analyzed traces: 2, Safe: 1, Unsafe: 0, Out of scope: 0, Violating assumptions: 1.
Elapsed time: 682 msec, Average speed: 55 states/sec, Elapsed time in decision procedure: 83 msec (12,17% of total).
```

The traces are still two, but only one is safe. The other trace is counted in the number of the traces that violate assumptions.

Often the `Analysis.assume` method is all one needs to specify custom analysis assumptions, but in many relevant cases it does not. This is especially true when one wants to specify assumptions on the structure of the initial heap, i.e., assumptions on the symbolic reference values. An important issue with the `Analysis.assume` method is that its unconditional use for enforcing assumptions on the heap may cause early, and useless, splitting of path condition cases. Let us consider, for example, the case where we want to assume that `{ROOT}:list.header.next.next.next` is not null, for a symbolic parameter `node` with class `Node`, the type of the nodes of a linked list data structure. Injecting the statement `assume(list.header.next.next.next != null)` causes JBSE to access `{ROOT}:list` first, then `{ROOT}:list.header`, then `{ROOT}:list.header.next`, then `{ROOT}:list.header.next.next` and finally `{ROOT}:list.header.next.next.next`. JBSE must therefore analyze all the combination of cases where *any* of them can be null or not null. This causes an adverse, and essentially useless, combinatorial explosion in the number of paths that can hinder the ability of symbolic execution to analyze the code. This situation can be avoided by enforce the weaker, and in most practical cases equivalent, assumption stating that `{ROOT}:list.header.next.next.next` is not null *if* it is ever accessed during symbolic execution. A related issue is that often one would like to express assumptions that involve all the objects of some kind, e.g., all the objects reachable from some sequence of references. If, for example, we would like to enforce the assumption that the example list does not store `null` values, we need a way to express that *all* the  `{ROOT}:list.header.next.next.(...).next.value` sequences of heap accesses do not yield `null`.

JBSE implements many techniques for specifying rich classes of assumptions on the structure of the heap while avoiding the explosion in the number of analyzed traces:

* Conservative repOk methods: By annotating with the `jbse.meta.annotations.ConservativeRepOk` a parameterless method with boolean return type, JBSE will recognize it as a conservative repOk method for all the objects of that class. JBSE will execute it every time it assumes a new path condition clause. JBSE will pass as `this` parameter it a copy of the (symbolic) initial state specialized on the current path condition plus the new clause. The method must inspect the state and check whether it does not satisfy the assumption (in this case the method must return `false`) or it still might (in this case the method must *conservatively* return true).
* LICS rules: A LICS rule has a head and a tail. The head is a regular expression describing a set of symbolic references, the tail specifies a constraint on them. For instance, the rule `{ROOT}:list/header(/next)*/value not null` requires that all the values stored in `list` are not null, and the rule `{ROOT}:list/header(/next)* aliases nothing` forbids the reference to the nodes in `list` to point to nodes previously assumed during symbolic execution, excluding the presence of loops in the list.
* Triggers: A trigger is a method that is executed whenever JBSE makes some assumption on a symbolic value. The main use case for triggers is to update ghost instrumentation variables whenever symbolic execution refines its assumptions on the heap structure. For example, if the linked list has a `size` field we can enforce it to be greater than the number of the assumed nodes of the list by injecting instrumentation code in the List class as follows:

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
        l._initialSize = l.size;   //stores the initial size for future reference
        l._initialSizeBound = 0; //initially we do not assume anything on the number of nodes in l
        assume(l._initialSize >= l._initialSizeBound);
    }
    
    private static void _triggerAssumeListNode(List l) {
        ++l._initialSizeBound; //we assume that there is another node
        assume(l._initialSize >= l._initialSizeBound);
    }
    
    private static void _triggerAssumeListComplete(List l) {
        assume(l._initialSize == l._initialSizeBound); //no more nodes
    }
}
```

and trigger the methods with the following rules (the syntax is a bit simplified for the sake of the exposition):

```
{ROOT}:list expands to instanceof List triggers List:(LList;):_triggerAssumeList
{ROOT}:list/header(/next)* expands to instanceof Node triggers List:(LList;):_triggerAssumeListNode
{ROOT}:list/header(/next)* is null triggers List:(LList;):_triggerAssumeListComplete
```

The first trigger rule states that, when JBSE assumes the existence of `{ROOT}:list` it must run the `_triggerAssumeList` method. This method stores the symbolic initial list size in `_initialSize`, and initializes a counter for the assumed list nodes in `_initialSizeBound`. The second trigger rule fires the `_triggerAssumeListNode` method, that increments `_initialSizeBounds`, whenever JBSE assumes the existence of another node in the list. Both triggers enforce the invariant that the initial list size is greater or equal to the total number of list nodes assumed by JBSE when they fire. Finally, JBSE fires the `_triggerAssumeListComplete` method after assuming that the chain of list nodes is terminated by a `null`. The trigger enforces the initial list size to be exactly equal to the number of assumed nodes.

### More goodies ###

A comprehensive description of the features of JBSE will be available in its user manual, currently under development. For a partial overview of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.