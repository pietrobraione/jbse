package jbse.apps;

import jbse.mem.State;

public abstract class StateFormatterJUnitTest implements Formatter {
    protected String output = "";    
    private int testCounter = 0;

    @Override
    public final void formatPrologue() {
        this.output = 
            "import static org.junit.Assert.*;\n" +
            "import org.junit.Test;\n" +
            "\n" +
            "public class TestSuite {\n" 
            
            //TODO aggiungere eventuali metodi/variabili;
            //per favore, indentare il testo in output 
            //usando il seguente standard: ogni livello 
            //aggiuntivo sono 4 spazi di indentazione.
            //NON usare il carattere \t per indentare!!!
            
            ;

    }

    @Override
    public final void formatState(State s) {
        this.output = 
            "    @Test\n" +
            "    void testCase" + this.testCounter +"() {\n" +
            "        //test case for state " + s.getIdentifier() + "[" + s.getSequenceNumber() + "]\n" +
            
            //TODO aggiungere le istruzioni per implementare il caso di test; 
            //per favore, rispettare l'indentazione come sopra (indentare 
            //con gli spazi, un livello in piu' = 4 spazi, no \t).
            
            "    }\n";
        ++this.testCounter;
    }
    
    public final void formatEpilogue() {
        this.output = "}\n";
    }

    @Override
    public void cleanup() {
        this.output = "";        
    }
}
