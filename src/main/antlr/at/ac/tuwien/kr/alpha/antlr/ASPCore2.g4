grammar ASPCore2;

/* The ASP-Core-2 grammar in ANTLR v4 based on
 * https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.03c.pdf
 * (sections 4 and 5, pages 10-12).
 * It is extended a bit to parse widespread syntax used by gringo/clasp,
 * see productions "gringo_range" and "gringo_sharp".
 */

// <program> ::= [<statements>] [<query>]
program : statements? query?;

// <statements> ::= [<statements>] <statement>
statements : statement+;

// <query> ::= <classical_literal> QUERY_MARK
query : classical_literal QUERY_MARK;

// <statement> ::= CONS [<body>] DOT
//               | <head> [CONS [<body>]] DOT
//               | WCONS [<body>] DOT SQUARE_OPEN <weight_at_level> SQUARE_CLOSE
//               | <optimize> DOT
statement : CONS body DOT                # statement_constraint
          | head (CONS body?)? DOT       # statement_rule
          | WCONS body? DOT SQUARE_OPEN weight_at_level SQUARE_CLOSE # statement_weightConstraint
          | optimize DOT                 # statement_optimize
          | gringo_sharp                 # statement_gringoSharp;   // syntax extension

// <head> ::= <disjunction> | <choice>
head : disjunction | choice;

// <body> ::= [<body> COMMA] (<naf_literal> | [NAF] <aggregate>)
body : ( naf_literal | NAF? aggregate ) (COMMA body)?;

// <disjunction> ::= [<disjunction> OR] <classical_literal>
disjunction : classical_literal (OR disjunction)?;

// <choice> ::= [<term> <binop>] CURLY_OPEN [<choice_elements>] CURLY_CLOSE [<binop> <term>]
choice : (term binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (binop term)?;

// <choice_elements> ::= [<choice_elements> SEMICOLON] <choice_element>
choice_elements : choice_element (SEMICOLON choice_elements)?;

// <choice_element> ::= <classical_literal> [COLON [<naf_literals>]]
choice_element : classical_literal (COLON naf_literals?)?;

// <aggregate> ::= [<term> <binop>] <aggregate function> CURLY_OPEN [<aggregate_elements>] CURLY_CLOSE [<binop> <term>]
aggregate : (term binop)? aggregate_function CURLY_OPEN aggregate_elements CURLY_CLOSE (binop term)?;

// <aggregate_elements> ::= [<aggregate_elements> SEMICOLON] <aggregate_element>
aggregate_elements : aggregate_element (SEMICOLON aggregate_elements)?;

// <aggregate_element> ::= [<terms>] [COLON [<naf_literals>]]
aggregate_element : basic_terms? (COLON naf_literals?)?;

// <aggregate_function> ::= AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM
aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;

// <optimize> ::= <optimize_function> CURLY_OPEN [<optimize_elements>] CURLY_CLOSE
optimize : optimize_function CURLY_OPEN optimize_elements? CURLY_CLOSE;

//<optimize_elements> ::= [<optimize_elements> SEMICOLON] <optimize_element>
optimize_elements : optimize_element (SEMICOLON optimize_elements)?;

// <optimize_element> ::= <weight_at_level> [COLON [<naf_literals>]]
optimize_element : weight_at_level (COLON naf_literals?)?;

// <optimize_function> ::= MAXIMIZE | MINIMIZE
optimize_function : MAXIMIZE | MINIMIZE;

// <weight_at_level> ::= <term> [AT <term>] [COMMA <terms>]
weight_at_level : term (AT term)? (COMMA terms)?;

// <naf_literals> ::= [<naf_literals> COMMA] <naf_literal>
naf_literals : naf_literal (COMMA naf_literals)?;

// <naf_literal> ::= [NAF] <classical_literal> | <builtin_atom>
naf_literal : NAF? (classical_literal | builtin_atom);

// <classical_literal> ::= [MINUS] ID [PAREN_OPEN [<terms>] PAREN_CLOSE]
classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;

// <builtin_atom> ::= <term> <binop> <term>
builtin_atom : term binop term;

// <binop> ::= EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ
binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;

// <terms> ::= [<terms> COMMA] <term>
terms : term (COMMA terms)?;

// <term> ::= ID [PAREN_OPEN [<terms>] PAREN_CLOSE]
//          | NUMBER
//          | STRING
//          | VARIABLE
//          | ANONYMOUS_VARIABLE
//          | PAREN_OPEN <term> PAREN_CLOSE
//          | MINUS <term>
//          | <term> <arithop> term>
term : ID (PAREN_OPEN terms? PAREN_CLOSE)?  # term_constOrFunc
     | NUMBER                               # term_number
     | STRING                               # term_string
     | VARIABLE                             # term_variable
     | ANONYMOUS_VARIABLE                   # term_anonymousVariable
     | PAREN_OPEN term PAREN_CLOSE          # term_parenthesisedTerm
     | MINUS term                           # term_minusTerm
     | term arithop term                    # term_binopTerm
     | gringo_range                         # term_gringoRange; // syntax extension

gringo_range : (NUMBER | VARIABLE | ID) DOT DOT (NUMBER | VARIABLE | ID); // NOT Core2 syntax, but widespread

gringo_sharp : SHARP ~(DOT)* DOT; // NOT Core2 syntax, but widespread, matching not perfect due to possible earlier dots

basic_terms : basic_term (COMMA basic_terms)? ;

basic_term : ground_term | variable_term;

ground_term : /*SYMBOLIC_CONSTANT*/ ID | STRING | MINUS? NUMBER;

variable_term : VARIABLE | ANONYMOUS_VARIABLE;

// <arithop> ::= PLUS | MINUS | TIMES | DIV
arithop : PLUS | MINUS | TIMES | DIV;

ID : ('a'..'z') ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*;
VARIABLE : ('A'..'Z') ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*;
NUMBER : '0' | ('1'..'9') ('0'..'9')*;
STRING : '"' ( '\\"' | . )*? '"';

ANONYMOUS_VARIABLE : '_';
DOT : '.';
COMMA : ',';
QUERY_MARK : '?';
COLON : ':';
SEMICOLON : ';';
OR : '|';
NAF : 'not';
CONS : ':-';
WCONS : ':~';
PLUS : '+';
MINUS : '-';
TIMES : '*';
DIV : '/';
AT : '@';
SHARP : '#'; // NOT Core2 syntax but gringo

PAREN_OPEN : '(';
PAREN_CLOSE : ')';
SQUARE_OPEN : '[';
SQUARE_CLOSE : ']';
CURLY_OPEN : '{';
CURLY_CLOSE : '}';
EQUAL : '=';
UNEQUAL : '<>' | '!=';
LESS : '<';
GREATER : '>';
LESS_OR_EQ : '<=';
GREATER_OR_EQ : '>=';

AGGREGATE_COUNT : '#count';
AGGREGATE_MAX : '#max';
AGGREGATE_MIN : '#min';
AGGREGATE_SUM : '#sum';

MINIMIZE : '#minimi' ('z' | 's') 'e';
MAXIMIZE : '#maximi' ('z' | 's') 'e';

COMMENT : '%' ~[\r\n]* -> channel(HIDDEN);
MULTI_LINE_COMMEN : '%*' .*? '*%' -> channel(HIDDEN);
BLANK : [ \t\r\n\f]+ -> channel(HIDDEN) ;
