package esl.cuenet.mapper.parsers;

import esl.cuenet.mapper.lispparser.*;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;

public class LispParser {

    @Test
    public void doTest() throws ParseException {

        String example1 = "(:axioms\n" +
            "  (:map @yale_bib:book book)\n" +
            "  (:map @cmu_bib:book book))";


        String example2 = "(:axioms\n" +
            "  (:map @yale_bib:Book Book)\n" +
            "  (:map @cmu_bib:Book Book))";

        String example3 = "(:name (:lookup first-name) (:lookup last-name))";

        parseFile("/home/arjun/Sandbox/emme90-workspace/ept-collator/src/main/javacc/test/test.2.map");

        test(example1);
        test(example2);
        test(example3);

    }
    
    private void test(String example) throws ParseException {

        LispParser_1 parser = null;

        parser = new LispParser_1( new StringReader(example));
        parser.document();

        System.out.println("");

    }
    
    public void parseFile(String filename) throws ParseException {


        try {
            LispParser_1 parser = new LispParser_1( new FileInputStream(filename));
            parser.document();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("");

    }

}