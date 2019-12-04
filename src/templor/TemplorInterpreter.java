package templor;

import templor.language_templor.Node;
import templor.language_templor.Parser;
import templor.language_templor.ParserException;
import templor.language_templor.LexerException;
import templor.walker.TemplorEngine;

import java.io.*;

/**
 * Created by Lam on 23/02/2017.
 */
public class TemplorInterpreter {

    public static void main(
            String[] args) {

        Reader in = null;
        System.err.println("test");
        if (args.length == 0) {
            // read from standard input
            in = new InputStreamReader(System.in);
        }
        else if (args.length == 1) {
            // read from given file
            try {
                in = new FileReader(args[0]);
            }
            catch (FileNotFoundException e) {
                System.err.println("INPUT ERROR: file not found '" + args[0]
                        + "'.");
                System.exit(1);
            }
        }
        else {
            System.err.println("COMMAND-LINE ERROR: too many arguments.");
            System.exit(1);
        }

        Node syntaxTree = null;

        try {
            // parse
            syntaxTree = new Parser(in).parse();
//            TreeVisualizer.printTree(syntaxTree);
        }
        catch (IOException e) {
            String inputName;
            if (args.length == 0) {
                inputName = "standard input";
            }
            else {
                inputName = "file '" + args[0] + "'";
            }
            System.err.println("INPUT ERROR: " + e.getMessage()
                    + " while reading " + inputName + ".");
            System.exit(1);
        }
        catch (ParserException e) {
            System.err.println("SYNTAX ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }
        catch (LexerException e) {
            System.err.println("LEXICAL ERROR: " + e.getMessage() + ".");
            System.exit(1);
        }

        TemplorEngine interpreterEngine = new TemplorEngine();
            // interpret
        interpreterEngine.visit(syntaxTree);

        // finish normally
        System.exit(0);
    }
}
