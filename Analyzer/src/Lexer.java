import java.io.*;
import java.awt.FileDialog;
import java.awt.Frame;

public class Lexer {

    private boolean isEof = false;								// is the file over?
    private char ch = ' '; 										// blank character
    private BufferedReader input;								// to read line by line
    private String line = "";									// temp line 
    private int lineno = 0;										// line 
    private int col = 1;										// 
    private final String letters = "abcdefghijklmnopqrstuvwxyz"	// a string that composed by all alphabets 
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String digits = "0123456789";					// a string that composed by all digits
    private final char eolnCh = '\n';							// end of line
    private final char eofCh = '\004';							// 
    

    public Lexer () { // source filename
        try {
            input = new BufferedReader (new FileReader("C:\\a.txt"));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found: " + "C:/a.txt");
            System.exit(1);
        }
    }

    private char nextChar() { // Return next char
        if (ch == eofCh)
            error("Attempt to read past end of file");
        col++;
        if (col >= line.length()) {
            try {
                line = input.readLine( );
            } catch (IOException e) {
                System.err.println(e);
                System.exit(1);
            } // try
            if (line == null) // at end of file
                line = "" + eofCh;
            else {
                // System.out.println(lineno + ":\t" + line);
                lineno++;
                line += eolnCh;
            } // if line
            col = 0;
        } // if col
        return line.charAt(col);
    }
            

    public Token next( ) { // Return next token
        do {
            if (isLetter(ch)) { // ident or keyword
                String spelling = concat(letters + digits);
                return Token.keyword(spelling);
            } else if (isDigit(ch)) { // int or float literal
                String number = concat(digits);
                if (ch != '.')  // int Literal
                    return Token.mkIntLiteral(number);
                number += concat(digits);
                return Token.mkFloatLiteral(number);
            } else switch (ch) {
            case ' ': case '\t': case '\r': case eolnCh:	// white spaces
                ch = nextChar();
                break;
            
            case '/':  // divide or comment
                ch = nextChar();
                if (ch != '/')  return Token.divideTok;
                // comment
                do {
                    ch = nextChar();
                } while (ch != eolnCh);
                ch = nextChar();
                break;
            
            case '\'':  // char literal
                char ch1 = nextChar();
                nextChar(); // get '
                ch = nextChar();
                return Token.mkCharLiteral("" + ch1);
                
            case eofCh: return Token.eofTok;
            
            case '+': ch = nextChar();
                return Token.plusTok;
            case '-': ch = nextChar();
            	return Token.minusTok;
            case '*': ch = nextChar();
            	return Token.multiplyTok;
            case '(': ch = nextChar();
        		return Token.leftParenTok;
            case ')': ch = nextChar();
        		return Token.rightParenTok;
            case '{': ch = nextChar();
    			return Token.leftBraceTok;
            case '}': ch = nextChar();
    			return Token.rightBraceTok;
            case '[': ch = nextChar();
            	return Token.leftBracketTok;
            case ']': ch = nextChar();
            	return Token.rightBracketTok;
            case ';': ch = nextChar();
        		return Token.semicolonTok;
            case ',': ch = nextChar();
        		return Token.commaTok;
                // - * ( ) { } ; ,  student exercise
                
            case '&': check('&'); return Token.andTok;
            case '|': check('|'); return Token.orTok;

            case '=':
                return chkOpt('=', Token.assignTok,
                                   Token.eqeqTok);
            case '<': ch = nextChar();
            	if(ch=='='){
            		nextChar();
            		ch = nextChar();
            		return Token.lteqTok;
            	}
        		return Token.ltTok;
            case '>': ch = nextChar();
            	if(ch=='='){
            		nextChar();
            		ch = nextChar();
            		return Token.gteqTok;
            	}
        		return Token.gtTok;
            case '!': ch = nextChar();
            	if(ch=='='){
            		nextChar();
            		ch = nextChar();
            		return Token.noteqTok;
            	}
            	return Token.notTok;
                // < > !  student exercise 

            default:  error("Illegal character " + ch); 
            } // switch
        } while (true);
    } // next


    private boolean isLetter(char c) {
        return (c>='a' && c<='z' || c>='A' && c<='Z');
    }
  
    private boolean isDigit(char c) {
        return (c>='0' && c<='9');  	// student exercise
    }

    private void check(char c) {
        ch = nextChar();
        if (ch != c) 
            error("Illegal character, expecting " + c);
        ch = nextChar();
    }

    private Token chkOpt(char c, Token one, Token two) {
    	ch = nextChar();
    	if(ch==c){ 
    		ch = nextChar();
    		return two;
    	}
        return one;  // student exercise
    }

    private String concat(String set) {
        String r = "";
        do {
            r += ch;
            ch = nextChar();
        } while (set.indexOf(ch) >= 0);				// if there are no char match with ch break!!
        return r;
    }

    public void error (String msg) {
        System.err.print(line);
        System.err.println("Error: column " + col + " " + msg);
        System.exit(1);
    }

//    static public void main ( String[] argv ) {
//    	
//        Lexer lexer = new Lexer();
//        Token tok = lexer.next( );
//        while (tok != Token.eofTok) {
//            System.out.println(tok.toString());
//            tok = lexer.next( );
//        } 
//    } // main
}

