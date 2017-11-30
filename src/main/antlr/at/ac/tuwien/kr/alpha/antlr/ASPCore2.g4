grammar ASPCore2;

import ASPLexer;

/* The ASP-Core-2 grammar in ANTLR v4 based on
 * https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.01c.pdf
 * (sections 4 and 5, pages 10-12).
 * It is extended a bit to parse widespread syntax (e.g. used by gringo/clasp).
 */

program : statements? query?;

statements : statement+;

query : classical_literal QUERY_MARK;

statement : head DOT                     # statement_fact
          | CONS body DOT                # statement_constraint
          | head CONS body DOT           # statement_rule
          | WCONS body? DOT SQUARE_OPEN weight_at_level SQUARE_CLOSE # statement_weightConstraint
          | gringo_sharp                 # statement_gringoSharp;   // syntax extension

head : disjunction | choice;

body : ( naf_literal | NAF? aggregate ) (COMMA body)?;

disjunction : classical_literal (OR disjunction)?;

choice : (lt=term lop=binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (uop=binop ut=term)?;

choice_elements : choice_element (SEMICOLON choice_elements)?;

choice_element : classical_literal (COLON naf_literals?)?;

aggregate : (term binop)? aggregate_function CURLY_OPEN aggregate_elements CURLY_CLOSE (binop term)?;

aggregate_elements : aggregate_element (SEMICOLON aggregate_elements)?;

aggregate_element : basic_terms? (COLON naf_literals?)?;

aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;

weight_at_level : term (AT term)? (COMMA terms)?;

naf_literals : naf_literal (COMMA naf_literals)?;

naf_literal : NAF? (external_atom | classical_literal | builtin_atom);

classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;

builtin_atom : term binop term;

binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;

terms : term (COMMA terms)?;

term : ID                                   # term_const
     | ID (PAREN_OPEN terms? PAREN_CLOSE)   # term_func
     | NUMBER                               # term_number
     | QUOTED_STRING                        # term_string
     | VARIABLE                             # term_variable
     | ANONYMOUS_VARIABLE                   # term_anonymousVariable
     | PAREN_OPEN term PAREN_CLOSE          # term_parenthesisedTerm
     | MINUS term                           # term_minusTerm
     | term arithop term                    # term_binopTerm
     | interval                             # term_interval; // syntax extension

arithop : PLUS | MINUS | TIMES | DIV;

interval : lower = (NUMBER | VARIABLE) DOT DOT upper = (NUMBER | VARIABLE); // NOT Core2 syntax, but widespread

external_atom : MINUS? AMPERSAND ID (SQUARE_OPEN input = terms SQUARE_CLOSE)? (PAREN_OPEN output = terms PAREN_CLOSE)?; // NOT Core2 syntax.

gringo_sharp : SHARP ~(DOT)* DOT; // NOT Core2 syntax, but widespread, matching not perfect due to possible earlier dots

basic_terms : basic_term (COMMA basic_terms)? ;

basic_term : ground_term | variable_term;

ground_term : /*SYMBOLIC_CONSTANT*/ ID | QUOTED_STRING | MINUS? NUMBER;

variable_term : VARIABLE | ANONYMOUS_VARIABLE;

answer_set : CURLY_OPEN classical_literal? (COMMA classical_literal)* CURLY_CLOSE;

answer_sets: answer_set*;