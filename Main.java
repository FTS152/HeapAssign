import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.*;

public class Main{
    public static void main(String[] args) {
        try {
            CharStream input = CharStreams.fromFileName(args[0]);    

            HeapAssignLexer lexer = new HeapAssignLexer(input);
            HeapAssignParser parser = new HeapAssignParser(new CommonTokenStream(lexer));
            parser.addParseListener(new MyListener());

            // Start parsing
            parser.prog(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}