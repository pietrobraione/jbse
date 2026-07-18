# JBSE

[![Project stats at https://www.openhub.net/p/JBSE](https://www.openhub.net/p/JBSE/widgets/project_thin_badge.gif)](https://www.openhub.net/p/JBSE)

## About

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. JBSE allows to execute an arbitrary Java method with symbolic inputs. A symbolic input stands for an arbitrary primitive or reference value on which JBSE does not make any initial assumption. During the execution JBSE introduces assumptions on the symbolic inputs, e.g. to decide whether at a certain point it must follow the "then" or "else" branch of a conditional statement, or to decide whether accessing a field with a symbolic reference yields a value or raises a `NullPointerException`. In these situations JBSE splits the possible cases and analyzes *all* of them via backtracking. This way JBSE can explore how a Java program behaves when fed with possibly infinite classes of program behaviors, differently from program testing that is always limited in investigating a single behavior a time.

JBSE is a library and can be integrated in any software that needs to perform an analysis of the runtime behavior of a Java bytecode program.

## Installing JBSE

Right now JBSE can be installed only by building it from source. Formal releases will be available when JBSE will be more feature-ready and stable. 

## Building JBSE

JBSE is built with Gradle version 8.14.5, whose wrapper is included in the repository. First, ensure that all the dependencies are present, including Z3 (see section "Dependencies"). Then, clone the JBSE git repository. If you work from the command line, this means running `git clone`. Next, you may need to patch a test class as explained in the section "Patching the tests". Finally, run the build Gradle task, e.g. by invoking `./gradlew build` from the command line in the JBSE project directory.

### Dependencies

JBSE has several dependencies. It must be built using a JDK version 8 - neither less, nor more. Unfortunately we found issues running Gradle version 8.14.5 on Java version 8, so in order to run Gradle you will also need at least a JRE with a later version (in our experience version 21.0.2 is ok). We suggest to use the latest Eclipse Temurin from [Adoptium](https://adoptium.net/) with HotSpot JVM (note that the JDK with the OpenJ9 JVM currently does not work, because there are some slight differences in the standard library classes). If you are on an Apple Silicon Mac we suggest to use the JDK Zulu distribution from [Azul](https://www.azul.com/downloads/?package=jdk#zulu). The Gradle wrapper `gradlew` included in the repository will take care to select the right version of the JDK when building JBSE. Gradle will also automatically resolve and use the following compile-time-only dependency:

* [JavaCC](https://javacc.org), used for compiling the parser for the JBSE settings files, and
* [JUnit](http://junit.org), used for running the regression test suite.

The runtime dependencies that are automatically resolved by Gradle and included in the build path are:

* The `tools.jar` library, that is part of every JDK 8 setup (note, *not* of a JRE).
* [Javassist](http://jboss-javassist.github.io/javassist/), that is used by JBSE for all the bytecode manipulation tasks.

There is one additional runtime dependency that is not handled by Gradle so you will need to fix it manually. 

* JBSE needs to interact at runtime with an external SMT solver for pruning infeasible program paths. JBSE works well with [Z3](https://github.com/Z3Prover/z3) and [CVC4](https://cvc4.cs.stanford.edu/), but any SMT solver that is compatible with the SMTLIB v2 standard and supports the AUFNIRA logic should work. Both Z3 and CVC4 are distributed as standalone binaries and can be installed almost everywhere. We strongly advise to use Z3 because it is what we routinely use.

### Patching the tests

Once cloned the JBSE git repository and ensured all the dependencies, you might need to fix another dependency that Gradle is not able to fix by itself. Gradle will not build the JBSE project unless the (very small) regression test suite under the `src/test` directory passes. All tests should pass (we test JBSE before every commit), with the possible exception of the tests in the class `jbse.dec.DecisionProcedureTest`, that test the interaction with the SMT solver, and therefore might fail if they are not configured to reflect your current setup of your SMT solver of choice. This means tweaking the `jbse.dec.DecisionProcedureTest` class to point to the path of the executable of either Z3 or CVC4. Modify line 44 of file `jbse.dec.DecisionProcedureTest.java` and define the variable `COMMAND_LINE` to be either `Z3_COMMAND_LINE` or `CVC4_COMMAND_LINE` depending on whether you have Z3 or CVC4 installed on your development machine. In both cases, you must also modify line 45 and assign the variable `SMT_SOLVER_PATH` with the path to the Z3 or CVC4 executable on your development machine.

### Working under Eclipse

If you want to build and modify JBSE by using (as we do) the latest Eclipse for Java Developers, you are lucky: All the Eclipse plugins that are necessary to import and build JBSE are already present in the distribution. The only caveat is that the last Eclipse versions, the most recent currently being 2026-06, come with their own JRE on which Eclipse runs. This JRE is also used by the Buildship Eclipse plugin to run Gradle. Version 8.14.5 of Gradle happily runs on the JRE that comes with the current version of Eclipse, so if you work under Eclipse perhaps you do not strictly need to install a Java 21 JRE (but you still need to install a Java 8 JDK to build and run JBSE and TARDIS). If you use a flavor of Eclipse different from Eclipse for Java Developers you might need to install the egit and the Buildship plugins, both available from the Eclipse Marketplace. After that, to import JBSE under Eclipse follow these steps:

* To avoid conflicts we advise to import JBSE under an empty workspace.
* Be sure that the default Eclipse JRE for your workspace, the one that will be used to run your projects, is the JRE subdirectory of a full JDK 8 setup, *not* a standalone (i.e., not part of a JDK) JRE: Unfortunately, Eclipse looks for the Java 8 compiler based on the path of the Java 8 JRE. Do it as follows: From the main menu choose Eclipse > Preferences... under macOS, or Window > Preferences... under Windows and Linux. On the left panel select Java > Compiler and on the right combo box "Compiler compliance level" select "1.8". Then on the left panel select Java > Installed JREs... and on the right list tick the row corresponding to your JDK 8 setup (if it is not present, add it by pressing the "Add..." button).
* JBSE uses the reserved `sun.misc.Unsafe` class, a thing that Eclipse forbids by default. To avoid Eclipse complaining about that you must modify the workspace preferences as follows: From the main menu choose Eclipse > Preferences... under macOS, or Window > Preferences... under Windows and Linux. On the left panel select Java > Compiler > Errors/Warnings, then on the right panel open the option group "Deprecated and restricted API", and for the option "Forbidden reference (access rules)" select the value "Warning" or "Info" or "Ignore".
* Switch to the Git perspective. If you cloned the Github JBSE repository from the command line, you can import the clone under Eclipse by clicking under the Git Repositories view the button for adding an existing repository. Otherwise you can clone the repository by clicking the clone button, again available under the Git Repositories view. Eclipse does *not* want you to clone the repository under your Eclipse workspace, and instead wants you to follow the standard git convention of putting all the git repositories in a `git` subdirectory of your home directory. If you clone the repository from a console, please follow this standard (if you clone the repository from the git perspective Eclipse will do this for you).
* Switch back to the Java perspective and from the main menu select File > Import... In the Select the Import Wizard window that pops up choose the Gradle > Existing Gradle Project wizard and press the Next until the Import Gradle Project window is displayed. Then enter in the Project root directory field the path to the JBSE cloned git repository, press the Next button and check in the following window that Gradle is able to detect the JBSE project, and finally press the Finish button to confirm. Now your workspace should have one Java project named `jbse`.
* Don't forget to apply all the patches described at the beginning of the "Building JBSE" section.
* Unfortunately the Buildship Gradle plugin is not able to fully configure the imported JBSE project: As a consequence, after the import you will see some compilation errors due to the fact that the project did not generate some necessary source files. Fix the situation by following this procedure: In the Gradle Tasks view double-click on the jbse > build > build task to build JBSE with Gradle for the first time. Then, right-click the jbse project in the Package Explorer, and in the contextual menu that pops up select Gradle > Refresh Gradle Project. After that, you should see no more errors. From this moment you can rebuild JBSE by double clicking again on the jbse > build > build task in the Gradle Task view. You should not need to refresh the project anymore, unless you modify the `build.gradle` or `settings.gradle` files.

## Deploying JBSE

The `gradlew build` command will produce a jar `build/libs/jbse-<VERSION>.jar` that also includes the `jbse.meta` package and its subpackages, containing the API that the code under analysis can invoke to issue assertions, assumptions, and otherwise control the analysis process itself. The jar file does not include the runtime dependencies (Javassist and `tools.jar`), so you need to deploy them separately. To ease deployment, Gradle will also build an uber-jar `build/libs/jbse-<VERSION>-shaded.jar` containing Javassist (but not `tools.jar`). Should the application you link the JBSE uber-jar against need its own copy of Javassist for its internal purposes, the uber jar renames the `javassist` package on which JBSE depends as `jbse.javassist` to avoid conflicts.

## Usage

In the future you will find a full description of JBSE and instructions for using it in its [user manual](https://jbse-manual.readthedocs.io) (currently under development). In the meantime, for a showcase of some of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.
