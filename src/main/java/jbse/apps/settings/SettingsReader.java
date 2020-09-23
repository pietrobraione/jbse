package jbse.apps.settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jbse.apps.run.RunParameters;
import jbse.bc.Signature;
import jbse.jvm.EngineParameters;
import jbse.jvm.RunnerParameters;
import jbse.rules.ClassInitRulesRepo;
import jbse.rules.LICSRulesRepo;
import jbse.rules.TriggerRulesRepo;


/**
 * A class that reads a settings file, parses it 
 * and creates a corresponding {@link EngineParameters} 
 * object (or subclass). 
 *  
 * @author Pietro Braione
 */
//TODO format and many other settings
public class SettingsReader {
    private SettingsParser parser;

    /**
     * Constructor.
     * 
     * @param filePath a {@link String}, the path of the file containing the settings.
     * @throws IOException if some error occurs while trying to access the file.
     * @throws ParseException if the content of the file is not correct.
     */
    public SettingsReader(String filePath) throws IOException, ParseException {
        this(Paths.get(filePath));
    }

    /**
     * Constructor.
     * 
     * @param filePath the {@link Path} of the file containing the settings.
     * @throws IOException if some error occurs while trying to access the file.
     * @throws ParseException if the content of the file is not correct.
     */
    public SettingsReader(Path filePath) throws IOException, ParseException {
        try (final BufferedReader reader = Files.newBufferedReader(filePath)) {
            this.parser = new SettingsParser(reader);
            this.parser.start();
        }
    }

    public void fillRulesClassInit(ClassInitRulesRepo repo) {
        for (String cname : this.parser.notInitializedClasses) {
            repo.addNotInitializedClassPattern(cname);
        }
    }

    public void fillRulesLICS(LICSRulesRepo repo) {
        for (String[] rule : this.parser.expandToLICS) {
            repo.addExpandTo(rule[0], rule[1], rule[2]);
        }
        for (String[] rule : this.parser.resolveAliasOriginLICS) {
            repo.addResolveAliasOrigin(rule[0], rule[1], rule[2]);
        }
        for (String[] rule : this.parser.resolveAliasInstanceofLICS) {
            repo.addResolveAliasInstanceof(rule[0], rule[1], rule[2]);
        }
        for (String[] rule : this.parser.resolveAliasNeverLICS) {
            repo.addResolveAliasNever(rule[0], rule[1], rule[2]);
        }
        for (String[] rule : this.parser.resolveNotNullLICS) {
            repo.addResolveNotNull(rule[0], rule[1]);
        }
    }

    public void fillRulesTrigger(TriggerRulesRepo repo) {
        for (String[] rule : this.parser.expandToTrigger) {
            repo.addExpandTo(rule[0], rule[1], rule[2], new Signature(rule[3], rule[4], rule[5]), rule[6]);
        }
        for (String[] rule : this.parser.resolveAliasOriginTrigger) {
            repo.addResolveAliasOrigin(rule[0], rule[1], rule[2], new Signature(rule[3], rule[4], rule[5]), rule[6]);
        }
        for (String[] rule : this.parser.resolveAliasInstanceofTrigger) {
            repo.addResolveAliasInstanceof(rule[0], rule[1], rule[2], new Signature(rule[3], rule[4], rule[5]), rule[6]);
        }
        for (String[] rule : this.parser.resolveNullTrigger) {
            repo.addResolveNull(rule[0], rule[1], new Signature(rule[2], rule[3], rule[4]), rule[5]);
        }
    }

    public void fillExpansionBackdoor(Map<String, Set<String>> expansionBackdoor) {
        for (String[] rule : this.parser.expansionBackdoor) {
            final String toExpand = rule[0];
            final String classAllowed = rule[1];
            Set<String> classesAllowed = expansionBackdoor.get(toExpand);
            if (classesAllowed == null) {
                classesAllowed = new HashSet<>();
                expansionBackdoor.put(toExpand, classesAllowed);
            }
            classesAllowed.add(classAllowed);
        }        
    }

    /**
     * Fills an {@link EngineParameters} object with the data read 
     * from the settings file.
     * 
     * @param params the object to be filled. Note that the operation
     *        will add, rather than replace, to previous data in it.
     */
    public void fillEngineParameters(EngineParameters params) {
        fillRulesTrigger(params.getTriggerRulesRepoRaw());
        fillExpansionBackdoor(params.getExpansionBackdoor());
    }

    /**
     * Fills a {@link RunnerParameters} object with the data read 
     * from the settings file.
     * 
     * @param params the object to be filled. Note that the operation
     *        will add, rather than replace, to previous data in it.
     */
    public void fillRunnerParameters(RunnerParameters params) {
        fillEngineParameters(params.getEngineParameters());
        //TODO specific parameters
    }

    /**
     * Fills a {@link RunParameters} object with the data read 
     * from the settings file.
     * 
     * @param params the object to be filled. Note that the operation
     *        will add, rather than replace, to previous data in it.
     */
    public void fillRunParameters(RunParameters params) {
        fillRunnerParameters(params.getRunnerParameters());
        fillRulesLICS(params.getLICSRulesRepo());
        fillRulesClassInit(params.getClassInitRulesRepo());
    }
}
