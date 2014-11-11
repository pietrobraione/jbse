package jbse.apps.settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jbse.apps.run.RunParameters;
import jbse.jvm.EngineParameters;
import jbse.jvm.RunnerParameters;


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
	 * @param fname the name of the file containing the settings. The
	 *        constructor will read the file.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws ParseException if the content of the file is not correct.
	 */
	public SettingsReader(String fname) throws FileNotFoundException, ParseException {
		try (final BufferedReader reader = new BufferedReader(new FileReader(fname))) {
			parser = new SettingsParser(reader);
			parser.start();
		} catch (IOException e) {
			// does nothing
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
		//TODO specific parameters
	}
	
	
	/**
	 * Fills a {@link RunnerParameters} object with the data read 
	 * from the settings file.
	 * 
	 * @param params the object to be filled. Note that the operation
	 *        will add, rather than replace, to previous data in it.
	 */
	public void fillRunnerParameters(RunnerParameters params) {
		//TODO specific parameters
		this.fillEngineParameters(params.getEngineParameters());
	}
	
	/**
	 * Fills a {@link RunParameters} object with the data read 
	 * from the settings file.
	 * 
	 * @param params the object to be filled. Note that the operation
	 *        will add, rather than replace, to previous data in it.
	 */
	public void fillRunParameters(RunParameters params) {
		this.fillRunnerParameters(params.getRunnerParameters());
		for (String cname : parser.notInitializedClasses) {
			params.addNotInitializedClasses(cname);
		}
		for (String[] rule : parser.expandTo) {
			params.addExpandTo(rule[0], rule[1], rule[2], rule[3], rule[4], rule[5], rule[6]);
		}
		for (String[] rule : parser.resolveAliasOrigin) {
			params.addResolveAliasOrigin(rule[0], rule[1], rule[2], rule[3], rule[4], rule[5], rule[6]);
		}
		for (String[] rule : parser.resolveAliasInstanceof) {
			params.addResolveAliasInstanceof(rule[0], rule[1], rule[2], rule[3], rule[4], rule[5], rule[6]);
		}
		for (String[] rule : parser.resolveNotNull) {
			params.addResolveNotNull(rule[0], rule[1]);
		}
		for (String[] rule : parser.resolveNull) {
			params.addResolveNull(rule[0], rule[1], rule[2], rule[3], rule[4], rule[5]);
		}
	}	
}
