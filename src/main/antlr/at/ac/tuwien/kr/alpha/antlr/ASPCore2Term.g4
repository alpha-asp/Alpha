grammar ASPCore2Term;

import ASPLexer;

terms : term (COMMA terms)?;

term : ID                                   # term_const
     | ID (PAREN_OPEN terms? PAREN_CLOSE)   # term_func
     | NUMBER                               # term_number
     | STRING                               # term_string
     | VARIABLE                             # term_variable
     | ANONYMOUS_VARIABLE                   # term_anonymousVariable
     | PAREN_OPEN term PAREN_CLOSE          # term_parenthesisedTerm
     | MINUS term                           # term_minusTerm
     | term arithop term                    # term_binopTerm
     | interval                             # term_interval; // syntax extension

arithop : PLUS | MINUS | TIMES | DIV;

interval : lower = (NUMBER | VARIABLE) DOT DOT upper = (NUMBER | VARIABLE); // NOT Core2 syntax, but widespread