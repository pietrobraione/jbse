package jbse.dec;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;

import java.util.LinkedHashSet;

import jbse.bc.ClassFile;
import jbse.dec.exc.DecisionException;
import jbse.mem.ClauseAssumeClassInitialized;
import jbse.mem.ClauseAssumeClassNotInitialized;
import jbse.rules.ClassInitRulesRepo;
import jbse.val.Calculator;

/**
 * A decision procedure based on the specification of class initializations.
 * 
 * @author Pietro Braione
 */
//TODO really crappy, nonmonotonic implementation. Rewrite.
public final class DecisionProcedureClassInit extends DecisionProcedureChainOfResponsibility {
    private final ClassInitRulesRepo rulesRepo;

    /**
     * Stores all the classes from the 
     * {@link ClauseAssumeClassInitialized} 
     * that are pushed. 
     */
    private final LinkedHashSet<ClassFile> init = new LinkedHashSet<>();

    /**
     * Stores all the classes from the 
     * {@link ClauseAssumeClassNotInitialized} 
     * that are pushed. 
     */
    private final LinkedHashSet<ClassFile> notInit = new LinkedHashSet<>();

    public DecisionProcedureClassInit(DecisionProcedure next, Calculator calc, ClassInitRulesRepo rulesRepo) {
        super(next, calc);
        this.rulesRepo = rulesRepo.clone(); //safety copy
    }

    @Override
    protected void clearAssumptionsLocal() {
        this.init.clear();
        this.notInit.clear();
    }
    
    @Override
    protected void pushAssumptionLocal(ClauseAssumeClassInitialized c) throws DecisionException {
        //if a class is initialized, then its superclasses are
        for (ClassFile cf : c.getClassFile().superclasses()) {
            this.init.add(cf);
        }
        //TODO also superinterfaces?
    }

    @Override
    protected void pushAssumptionLocal(ClauseAssumeClassNotInitialized c) {
        this.notInit.add(c.getClassFile());
    }

    //TODO support pop of assumptions

    @Override
    protected boolean isSatInitializedLocal(ClassFile classFile) {
        //we only support mutually exclusive initialized/not-initialized cases
        //TODO drop mutual exclusion of class initialized/not-initialized cases and branch bytecodes during initialization based on assumptions
        return !isSatNotInitializedLocal(classFile);
    }

    @Override
    protected boolean isSatNotInitializedLocal(ClassFile classFile) {
        if (this.init.contains(classFile)) {
            return false;
        }
        return (classFile.getDefiningClassLoader() > CLASSLOADER_APP || 
                this.rulesRepo.notInitializedClassesContains(classFile.getClassName()) || 
                this.notInit.contains(classFile));
    }
}
