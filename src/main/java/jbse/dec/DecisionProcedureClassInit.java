package jbse.dec;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;

import java.util.LinkedHashSet;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
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
//TODO really crappy, nonmonotonic (ugh!) implementation. Rewrite.
public final class DecisionProcedureClassInit extends DecisionProcedureChainOfResponsibility {
    private final ClassInitRulesRepo rulesRepo;

    /**
     * Stores all the classes from the {@link ClauseAssumeClassInitialized} 
     * that are pushed. 
     */
    private final LinkedHashSet<ClassFile> assumedPreInitialized = new LinkedHashSet<>();

    /**
     * Stores all the classes from the {@link ClauseAssumeClassNotInitialized} 
     * that are pushed. 
     */
    private final LinkedHashSet<ClassFile> assumedNotPreInitialized = new LinkedHashSet<>();

    public DecisionProcedureClassInit(DecisionProcedure next, ClassInitRulesRepo rulesRepo) 
    throws InvalidInputException {
        super(next);
        this.rulesRepo = rulesRepo.clone(); //safety copy
    }
    
    public DecisionProcedureClassInit(Calculator calc, ClassInitRulesRepo rulesRepo) 
    throws InvalidInputException {
    	super(calc);
    	this.rulesRepo = rulesRepo.clone(); //safety copy
    }

    @Override
    protected void clearAssumptionsLocal() {
        this.assumedPreInitialized.clear();
        this.assumedNotPreInitialized.clear();
    }
    
    @Override
    protected void pushAssumptionLocal(ClauseAssumeClassInitialized c) throws DecisionException {
        //if a class is initialized, then its superclasses are,
        //thus upwards-closes the assumption        
        for (ClassFile cf : c.getClassFile().superclasses()) {
            this.assumedPreInitialized.add(cf);
        }
        //TODO also superinterfaces?
    }

    @Override
    protected void pushAssumptionLocal(ClauseAssumeClassNotInitialized c) {
        this.assumedNotPreInitialized.add(c.getClassFile());
    }

    //TODO support pop of assumptions

    @Override
    protected boolean isSatInitializedLocal(ClassFile classFile) {
        //looks into contradictory assumptions
        if (this.assumedNotPreInitialized.contains(classFile)) {
            return false;
        }
        
        //looks into supporting assumptions
        if (this.assumedPreInitialized.contains(classFile)) {
            return true;
        }
        
        //then, defaults:
        
        //primitive classes are always assumed to be preinitialized
        if (classFile.isPrimitiveOrVoid()) {
            return true;
        }
        
        //array classes follow the same destiny of their
        //respective member's class
        if (classFile.isArray()) {
            return isSatInitializedLocal(classFile.getMemberClass());
        }
        
        //a class that is loaded with a custom classloader is never
        //assumed to be preinitialized
        if (classFile.getDefiningClassLoader() > CLASSLOADER_APP) {
            return false;
        }
        
        //as last resort, applies rules
        return !this.rulesRepo.notInitializedClassesContains(classFile.getClassName());
    }

    @Override
    protected boolean isSatNotInitializedLocal(ClassFile classFile) {
        //looks into contradictory assumptions
        if (this.assumedPreInitialized.contains(classFile)) {
            return false;
        }
        
        //looks into supporting assumptions
        if (this.assumedNotPreInitialized.contains(classFile)) {
            return true;
        }
        
        //then, defaults:
        
        //primitive classes are always assumed to be preinitialized
        if (classFile.isPrimitiveOrVoid()) {
            return false;
        }
        
        //array classes follow the same destiny of their
        //respective member's class
        if (classFile.isArray()) {
            return isSatNotInitializedLocal(classFile.getMemberClass());
        }
        
        //a class that is loaded with a custom classloader is never
        //assumed to be preinitialized
        if (classFile.getDefiningClassLoader() > CLASSLOADER_APP) {
            return true;
        }
        
        //finally, rules
        return this.rulesRepo.notInitializedClassesContains(classFile.getClassName());
    }
}
