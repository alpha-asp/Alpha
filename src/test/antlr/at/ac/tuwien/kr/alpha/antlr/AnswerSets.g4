grammar AnswerSets;

answerSets : answerSet+;

answerSet : CURLY_OPEN atoms? CURLY_CLOSE;

atoms: atom (COMMA atoms)?;

atom: ID (PAREN_OPEN terms PAREN_CLOSE)?;

terms : term (COMMA terms)?;

term : ID                                   # term_const
     | ID (PAREN_OPEN terms? PAREN_CLOSE)   # term_func
     | NUMBER                               # term_number
     | STRING                               # term_string
     | PAREN_OPEN term PAREN_CLOSE          # term_parenthesisedTerm
     | MINUS term                           # term_minusTerm;

COMMA : ',';
MINUS : '-';
TIMES : '*';
DIV : '/';

PAREN_OPEN : '(';
PAREN_CLOSE : ')';
CURLY_OPEN : '{';
CURLY_CLOSE : '}';

ID : ('a'..'z') ( 'A'..'Z' | 'a'..'z' | '0'..'9' | '_' )*;
NUMBER : '0' | ('1'..'9') ('0'..'9')*;
STRING : '"' ( '\\"' | . )*? '"';

BLANK : [ \t\r\n\f]+ -> channel(HIDDEN) ;