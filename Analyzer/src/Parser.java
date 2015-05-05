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
//        System.out.println(token.type()+" "+token.value());
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
//    	System.out.println("\tprogram");
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   	// bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);				// {
//        System.out.println("\tDeclaration Start");
        Declarations decs = declarations();		// 		Declarations
//        System.out.println("\tStatement Start");
        Block stmts = statements();				//		Statements
        match(TokenType.RightBrace);			// }
        Program program = new Program(decs, stmts);
        return program;  // student exercises
    }
  
    private Declarations declarations () {
//    	System.out.println("\tdeclaration");
        // Declarations --> { Declaration }
    	Declarations decs = new Declarations();
    	while(isType()){
    		declaration(decs);
    	}
        return decs;  // student exercise
    }
  
    private void declaration (Declarations ds) {
//    	System.out.println("\tdeclaration");
        // Declaration  --> Type Identifier { , Identifier } ;
        // student exercise
    	Type t = type();
    	Variable var = new Variable(match(TokenType.Identifier));
    	Declaration dec = new Declaration(var, t);
    	ds.add(dec);
    	while(token.type().equals(TokenType.Comma)){
    		match(TokenType.Comma);
    		var = new Variable(match(TokenType.Identifier));
    		dec = new Declaration(var, t);
    		ds.add(dec);
    	}
    	match(TokenType.Semicolon);
    }
  
    private Type type () {
//    	System.out.println("\ttype");
        // Type  -->  int | bool | float | char
        Type t = null;
        // student exercise
        if(token.type().equals(TokenType.Int)) t = Type.INT;
        else if(token.type().equals(TokenType.Float)) t = Type.FLOAT;
        else if(token.type().equals(TokenType.Char)) t = Type.CHAR;
        else if(token.type().equals(TokenType.Bool)) t = Type.BOOL;
        else error("type error!");
        token = lexer.next();
        return t;          
    }
  
    private Statement statement() {
//    	System.out.println("\tstatement");
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement
        Statement s = new Skip();
        // student exercise
        if(token.type().equals(TokenType.LeftBrace)){
        	match(TokenType.LeftBrace);
        	s = statements();
        	match(TokenType.RightBrace);
        }
        else if(token.type().equals(TokenType.Identifier)) 	s = assignment();
        else if(token.type().equals(TokenType.If)) 			s = ifStatement();
        else if(token.type().equals(TokenType.While)) 		s = whileStatement();
        else error("statement error!");
        return s;
    }
  
    private Block statements () {
//    	System.out.println("\tblock");
        // Block --> '{' Statements '}'
        Block b = new Block();
        // student exercise
        while(isStatement()){
        	b.members.add(statement());
        }
        return b;
    }
  
    private Assignment assignment () {
        // Assignment --> Identifier = Expression ;
//    	System.out.println("\tassignment");
    	Variable var = new Variable(match(TokenType.Identifier));
    	match(TokenType.Assign);
    	Expression expr = expression();
    	match(TokenType.Semicolon);
    	Assignment ass = new Assignment(var,expr);
        return ass;  // student exercise
    }
  
    private Conditional ifStatement () {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
//    	System.out.println("\tif");
    	Conditional cond;
    	match(TokenType.If);
    	match(TokenType.LeftParen);
    	Expression expr = expression();
    	match(TokenType.RightParen);
    	Statement tStmt = statement();
    	if(token.type().equals(TokenType.Else)){
        	Statement eStmt = statement();
        	cond = new Conditional(expr,tStmt,eStmt);
    	} else cond = new Conditional(expr,tStmt); 
    	
        return cond;  // student exercise
    }
  
    private Loop whileStatement() {
        // WhileStatement --> while ( Expression ) Statement
//    	System.out.println("\twhile");
    	match(TokenType.While);
    	match(TokenType.LeftParen);
    	Expression expr = expression();
    	match(TokenType.RightParen);
    	Statement tStmt = statement();
    	Loop loop = new Loop(expr, tStmt);
        return loop;  // student exercise
    }

    private Expression expression () {
        // Expression --> Conjunction { || Conjunction }
//    	System.out.println("\texpression");
    	Expression expr = conjunction();
    	
    	while(token.type().equals(TokenType.Or)){
    		Operator op = new Operator(match(TokenType.Or));
    		Expression expr2 = conjunction();
    		expr = new Binary(op, expr, expr2); 
    	}
        return expr;  // student exercise
    }
  
    private Expression conjunction () {
        // Conjunction --> Equality { && Equality }
//    	System.out.println("\tconjunction");
    	Expression expr = equality();
    	while(token.type().equals(TokenType.And)){
    		Operator op = new Operator(match(TokenType.And));
    		Expression expr2 = equality();
    		expr = new Binary(op, expr, expr2);
    	}
        return expr;  // student exercise
    }
  
    private Expression equality () {
        // Equality --> Relation [ EquOp Relation ]
//    	System.out.println("\tequality");
    	Expression expr = relation();
    	if(isEqualityOp()){
    		Operator op = new Operator(match(token.type()));
    		Expression expr2 = relation();
    		expr = new Binary(op, expr, expr2);
    	}
        return expr;  // student exercise
    }

    private Expression relation (){
        // Relation --> Addition [RelOp Addition] 
//    	System.out.println("\tRelation");
    	Expression expr = addition();
    	if(isRelationalOp()){
    		Operator op = new Operator(match(token.type()));
    		Expression expr2 = addition();
    		expr = new Binary(op, expr, expr2);
    	}
        return expr;  // student exercise
    }
  
    private Expression addition () {
        // Addition --> Term { AddOp Term }
//    	System.out.println("\tAddition");
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
//    	System.out.println("\tTerm");
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
//    	System.out.println("\tFactor");
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
//    	System.out.println("\tPrimary");
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
    	Value val = null;
    	String tokenVal = token.value();
    	if(token.type().equals(TokenType.IntLiteral)){	
    		val = new IntValue(Integer.parseInt(tokenVal));
    		token = lexer.next();
    	}
    	else if(token.type().equals(TokenType.FloatLiteral)){
    		val = new FloatValue(Float.parseFloat(tokenVal)); 
    		token = lexer.next();
    	}
    	else if(token.type().equals(TokenType.CharLiteral)){
    		val = new CharValue(tokenVal.charAt(0));
    		token = lexer.next();
    	}
    	else if(token.type().equals(TokenType.True)){
    		val = new BoolValue(true);
    		token = lexer.next();
    	}
    	else if(token.type().equals(TokenType.False)){
    		val = new BoolValue(false);
    		token = lexer.next();
    	}
    	else  error("literal error!");
    	
        return val;  // student exercise
    }
  
    private boolean isStatement(){
    	return 	token.type().equals(TokenType.Semicolon) 	||
    			token.type().equals(TokenType.LeftBrace) 	||
    			token.type().equals(TokenType.If) 			||
    			token.type().equals(TokenType.While) 		||
    			token.type().equals(TokenType.Identifier);
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
