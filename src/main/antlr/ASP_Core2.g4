grammar ASP_Core2;

/* The ASP Core 2 grammar in ANTLR v4.
   It is extended a bit to parse widespread syntax used by gringo/clasp.
*/

program : statements? query?;

statements : statement statements?;

query : classical_literal QUERY_MARK;

statement : CONS body DOT
          | head (CONS body?)? DOT
          | WCONS body? DOT SQUARE_OPEN weight_at_level SQUARE_CLOSE
          | gringo_sharp;   // syntax extension

head : disjunction | choice;

body : ( naf_literal | NAF? aggregate ) (COMMA body)?;

disjunction : classical_literal (OR disjunction)?;

choice : (term binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (binop term)?;

choice_elements : choice_element (SEMICOLON choice_elements)?;

choice_element : classical_literal (COLON naf_literals?)?;

aggregate : (term binop)? aggregate_function CURLY_OPEN aggregate_elements CURLY_CLOSE (binop term)?;

aggregate_elements : aggregate_element (SEMICOLON aggregate_elements)?;

aggregate_element : basic_terms? (COLON naf_literals?)?;

aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;

weight_at_level : term (AT term)? (COMMA terms)?;

naf_literals : naf_literal (COMMA naf_literals)?;

naf_literal : NAF? (classical_literal | builtin_atom);

classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;

builtin_atom : term binop term;

binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;

terms : term (COMMA terms)?;

term : ID (PAREN_OPEN terms? PAREN_CLOSE)?
     | NUMBER
     | STRING
     | VARIABLE
     | ANONYMOUS_VARIABLE
     | PAREN_OPEN term PAREN_CLOSE
     | MINUS term
     | term arithop term
     | gringo_range;    // syntax extension

gringo_range : (NUMBER | VARIABLE | ID) DOT DOT (NUMBER | VARIABLE | ID);    // NOT Core2 syntax, but widespread

gringo_sharp : SHARP ~(DOT)* DOT; // NOT Core2 syntax, but widespread, matching not perfect due to possible earlier dots

basic_terms : basic_term (COMMA basic_terms)? ;

basic_term : ground_term | variable_term;

ground_term : /*SYMBOLIC_CONSTANT*/ ID | STRING | MINUS? NUMBER;

variable_term : VARIABLE | ANONYMOUS_VARIABLE;

arithop : PLUS | MINUS | TIMES | DIV;



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

ID : ('a'..'z') ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*;
VARIABLE : ('A'..'Z') ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*;
NUMBER : '0' | ('1'..'9') ('0'..'9')*;
STRING : '"' ( '\\"' | . )*? '"';

COMMENT : '%' ~[\r\n]* -> channel(HIDDEN);
MULTI_LINE_COMMEN : '%*' .*? '*%' -> channel(HIDDEN);
BLANK : [ \t\r\n\f]+ -> channel(HIDDEN) ;
