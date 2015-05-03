import java.util.*;

public class Parser {
    // Recursive descent parser that inputs a C++Lite program and 
    // generates its abstract syntax.  Each method corresponds to
    // a concrete syntax grammar rule, which appears as a comment
    // at the beginning of the method.
  
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { // Open the C++Lite source program
        lexer = ts;                          // as a token stream, and
        token = lexer.next();            // retrieve its first Token
    }
  
    private String match (TokenType t) { // * return the string of a token if it matches with t *
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    private void error(String tok) {
        System.err.println("Syntax error: expecting: " + tok 
                           + "; saw: " + token);
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
        Program program = new Program(declarations(), statements());
        match(TokenType.RightBrace);
        return program;  // student exercise
    }
  
    private Declarations declarations () {
        // Declarations --> { Declaration }
    	ArrayList<Declaration> decArr = new ArrayList<Declaration>();
    	Declarations decs = new Declarations(decArr);
    	while(isType()){
    		declaration(decs);
    	}
        return decs;  // student exercise
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise
    	Declaration dec = new Declaration(new Variable(match(TokenType.Identifier)), type());
    	ds.add(dec);
    	while(token.type().equals(Token.commaTok)){
    		declaration(ds);
    	}    	
    }
  
    private Type type () {
        // Type  -->  int | bool | float | char
        Type t = null;
        // student exercise
        if(token.type().equals(TokenType.Int)) t = Type.INT;
        else if(token.type().equals(TokenType.Float)) t = Type.FLOAT;
        else if(token.type().equals(TokenType.Char)) t = Type.CHAR;
        else if(token.type().equals(TokenType.Bool)) t = Type.BOOL;
        return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = new Skip();
        // student exercise
        return s;
    }
  
    private Block statements () {
        // Block --> '{' Statements '}'
        Block b = new Block();
        // student exercise
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
    	Variable var = new Variable(match(TokenType.Identifier));
    	match(TokenType.Assign);
    	Expression expr = expression();
    	Assignment ass = new Assignment(var,expr);
        return ass;  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
    	Conditional cond;
    	match(TokenType.If);
    	match(TokenType.LeftParen);
    	Expression expr = expression();
    	match(TokenType.RightParen);
    	match(TokenType.LeftBrace);
    	Statement tStmt = statement();
    	match(TokenType.RightBrace);
    	if(token.type().equals(TokenType.Else)){
    		match(TokenType.LeftBrace);
        	Statement eStmt = statement();
        	match(TokenType.RightBrace);
        	cond = new Conditional(expr,tStmt,eStmt);
    	} else cond = new Conditional(expr,tStmt); 
    	
        return cond;  // student exercise
    }
  
    private Loop whileStatement() {
        // WhileStatement --> while ( Expression ) Statement
    	match(TokenType.While);
    	match(TokenType.LeftParen);
    	Expression expr = expression();
    	match(TokenType.RightParen);
    	match(TokenType.LeftBrace);
    	Statement tStmt = statement();
    	match(TokenType.RightBrace);
    	Loop loop = new Loop(expr, tStmt);
        return loop;  // student exercise
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
    	Expression expr = conjunction();
    	while(token.type().equals(TokenType.Or)){
    		match(TokenType.Or);
    		
    	}
        return expr;  // student exercise
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
        return null;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
        return null;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
        return null;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term () {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
            return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
        return null;  // student exercise
    }
  

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }
    
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer());
        Program prog = parser.program();
        prog.display();           					// display abstract syntax tree
    } //main

} // Parser
