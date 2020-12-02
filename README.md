# JBSE

[![Project stats at https://www.openhub.net/p/JBSE](https://www.openhub.net/p/JBSE/widgets/project_thin_badge.gif)](https://www.openhub.net/p/JBSE) [![Join the chat at https://gitter.im/pietrobraione/jbse](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/pietrobraione/jbse?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Flattr this](http://button.flattr.com/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=pietro.braione&url=https%3A%2F%2Fgithub.com%2Fpietrobraione%2Fjbse)

## About

JBSE is a symbolic Java Virtual Machine for automated program analysis, verification and test generation. JBSE allows to execute an arbitrary Java method with symbolic inputs. A symbolic input stands for an arbitrary primitive or reference value on which JBSE does not make any initial assumption. During the execution JBSE introduces assumptions on the symbolic inputs, e.g. to decide whether at a certain point it must follow the "then" or "else" branch of a conditional statement, or to decide whether accessing a field with a symbolic reference yields a value or raises a `NullPointerException`. In these situations JBSE splits the possible cases and analyzes *all* of them, backtracking through the possible cases. This way JBSE can explore how a Java program behaves when fed with possibly infinite classes of inputs, differently from program testing that is always limited in investigating a single behavior a time.

JBSE is a library and can be integrated in any software that needs to perform an analysis of the runtime behavior of a Java bytecode program.

## Installing JBSE

Right now JBSE can be installed only by building it from source. Formal releases will be available when JBSE will be more feature-ready and stable. JBSE is built with Gradle version 6.7.1, that is included in the repository.

### Dependencies

JBSE has several dependencies. It must be built using a JDK version 8 - neither less, nor more. We suggest to use the latest [AdoptOpenJDK](https://adoptopenjdk.net/) v8 with HotSpot JVM (note that the JDK with the OpenJ9 JVM currently does not work, because there are some slight differences in the standard library classes). The Gradle wrapper `gradlew` included in the repository will take care to select the right version of Gradle and of the JDK. Gradle will automatically resolve and use the following compile-time-only dependency:

* [JavaCC](https://javacc.org) is used for compiling the parser for the JBSE settings files.
* [JUnit](http://junit.org) is used for running the test suite.

The runtime dependencies that are automatically resolved by Gradle and included in the build path are:

* The `tools.jar` library, that is part of every JDK 8 setup (note, *not* of the JRE).
* [Javassist](http://jboss-javassist.github.io/javassist/), that is used by JBSE for all the bytecode manipulation tasks. JBSE relies on a patched version of Javassist that is included in the `libs` folder.

There is an additional runtime dependency that is not handled by Gradle so you will need to fix it manually. JBSE needs to interact at runtime with an external numeric solver for pruning infeasible program paths. JBSE works well with [Z3](https://github.com/Z3Prover/z3) and [CVC4](https://cvc4.cs.stanford.edu/), but any SMT solver that is compatible with the SMTLIB v2 standard and supports the AUFNIRA logic should work. Both Z3 and CVC4 are distributed as standalone binaries and can be installed almost everywhere. We strongly advise to use Z3 because it is what we routinely use.

### Working under Eclipse

If you want to work (as us) under Eclipse 2020-09 for Java Developers, you are lucky: All the plugins that are necessary to import JBSE under Eclipse and make it work are already present in the distribution. The only caveat is that, since starting from version 2020-09 Eclipse requires at least Java 11 to run, you will need to install both Java 11 and Java 8. Gradle will automatically select the right version of Java when building JBSE. Note that If you use a different flavor, or an earlier version, of Eclipse you might need to install the egit and the Buildship plugins, both available in the Eclipse Marketplace. After that, you are ready to import JBSE under Eclipse:

* To avoid conflicts we advise to import JBSE under an empty workspace.
* JBSE uses the reserved `sun.misc.Unsafe` class, a thing that Eclipse forbids by default. To avoid Eclipse complaining about that you must modify the workspace preferences as follows: From the main menu choose Eclipse > Preferences... under macOS, or Window > Preferences... under Windows and Linux. On the left panel select Java > Compiler > Errors/Warnings, then on the right panel open the option group "Deprecated and restricted API", and for the option "Forbidden reference (access rules)" select the value "Warning" or "Info" or "Ignore".
* Be sure that Eclipse is aware of your installed JDK and JRE environments (main menu, Eclipse > Preference under macOS or Windows > Preferences under Windows and Linux, then select Java > Installed JREs... and add all the home directories of all the JDK and JRE environments you have installed).
* Switch to the Git perspective. If you cloned the Github JBSE repository from the command line, you can import the clone under Eclipse by clicking under the Git Repositories view the button for adding an existing repository. Otherwise you can clone the  repository by clicking the clone button, again available under the Git Repositories view. Eclipse does *not* want you to clone the repository under your Eclipse workspace, and instead wants you to follow the standard git convention of putting the git repositories in a `git` subdirectory of your home directory. If you clone the repository from a console, please follow this standard (if you clone the repository from the git perspective Eclipse will do this for you).
* Switch back to the Java perspective and from the main menu select File > Import... In the Select the Import Wizard window that pops up choose the Gradle > Existing Gradle Project wizard and press the Next button twice. In the Import Gradle Project window that is displayed, enter in the Project root directory field the path to the JBSE cloned git repository, and then press the Finish button to confirm. Now your workspace should have one Java project named `jbse`.

### Building JBSE

Once cloned the git repository and ensured the dependencies, you need to fix a couple of things that Gradle is not able to fix by itself. Gradle will not build the JBSE jar unless the (very small) JUnit test suite under the `src/test` directory passes. All tests should pass with no problem (we do regression testing before every commit), with the exception of the tests in the class `jbse.dec.DecisionProcedureTest` that require that you fix the path to the Z3 executable. You must modify line 46 and replace `/opt/local/bin/z3` with your local path to the Z3 executable. Once fixed this path, it should be enough to run the build Gradle task by invoking `gradlew build` from the command line, and JBSE should build without any other fuss.

If you work under Eclipse, consider that the Buildship Gradle plugin is not completely able to configure the imported projects automatically. As a consequence, after the import you will see some compilation errors due to the fact that the JBSE project did not generate some source files yet. Fix the situation as follows: In the Gradle Tasks view double-click on the jbse > build > build task to build JBSE with Gradle for the first time. Then, right-click the jbse project in the Package Explorer, and in the contextual menu that pops up select Gradle > Refresh Gradle Project. After that, you should see no more errors. From this moment you can rebuild JBSE by double clicking again on the jbse > build > build task in the Gradle Task view. You should not need to refresh the Gradle project anymore, unless you modify the `build.gradle` or `settings.gradle` files.

### Deploying JBSE

The `gradlew build` command will produce a jar file `build/libs/jbse-<VERSION>.jar` that also includes the `jbse.meta` package and its subpackages, containing the API that the code under analysis can invoke to issue assertions, assumptions, and otherwise control the analysis process itself. The jar file does not include the runtime dependencies (Javassist and `tools.jar`), so you need to deploy them together with it. To ease deployment, Gradle will also build an uber-jar `build/libs/jbse-<VERSION>-shaded.jar` containing Javassist (but not `tools.jar`). To avoid conflicts the uber jar renames the `javassist` package as `jbse.javassist`.

## Usage

You will find a comprehensive description of JBSE and instructions for using JBSE in its [user manual](https://jbse-manual.readthedocs.io) (currently under development). For a showcase of some of JBSE's capabilities you can checkout the [JBSE examples](https://github.com/pietrobraione/jbse-examples) project.
