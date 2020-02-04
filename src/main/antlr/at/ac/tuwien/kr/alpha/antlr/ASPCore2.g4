grammar ASPCore2;

import ASPLexer;

/* The ASP-Core-2 grammar in ANTLR v4 based on
 * https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.01c.pdf
 * (sections 4 and 5, pages 10-12).
 * It is extended a bit to parse widespread syntax (e.g. used by gringo/clasp).
 * It is also extended by heuristic directives for Alpha.
 */

program : statements? query? EOF;

statements : statement+;

query : classical_literal QUERY_MARK;

statement : head DOT                     # statement_fact
          | CONS body DOT                # statement_constraint
          | head CONS body DOT           # statement_rule
          | WCONS body? DOT weight_annotation        # statement_weightConstraint
          | directive                    # statement_directive;   // NOT Core2 syntax.

head : disjunction | choice;

body : ( naf_literal | aggregate ) (COMMA body)?;

disjunction : classical_literal (OR disjunction)?;

choice : (lt=term lop=binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (uop=binop ut=term)?;

choice_elements : choice_element (SEMICOLON choice_elements)?;

choice_element : classical_literal (COLON naf_literals?)?;

aggregate : NAF? (lt=term lop=binop)? aggregate_function CURLY_OPEN aggregate_elements CURLY_CLOSE (uop=binop ut=term)?;

aggregate_elements : aggregate_element (SEMICOLON aggregate_elements)?;

aggregate_element : basic_terms? (COLON naf_literals?)?;

aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;

weight_annotation : SQUARE_OPEN weight_at_level SQUARE_CLOSE;

weight_at_level : term (AT term)? (COMMA terms)?;

naf_literals : naf_literal (COMMA naf_literals)?;

naf_literal : NAF? (external_atom | classical_literal | builtin_atom);

classical_literal : MINUS? basic_atom;

basic_atom : ID (PAREN_OPEN terms PAREN_CLOSE)?;

builtin_atom : term binop term;

binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;

terms : term (COMMA terms)?;

term : ID                                   # term_const
     | ID (PAREN_OPEN terms? PAREN_CLOSE)   # term_func
     | NUMBER                               # term_number
     | QUOTED_STRING                        # term_string
     | VARIABLE_OR_HEU_SIGNS                # term_variable
     | ANONYMOUS_VARIABLE                   # term_anonymousVariable
     | PAREN_OPEN term PAREN_CLOSE          # term_parenthesisedTerm
     | interval                             # term_interval // Syntax extension.
     | MINUS term                           # term_minusArithTerm
     |<assoc=right> term POWER term         # term_powerArithTerm
     | term (TIMES | DIV | MODULO) term     # term_timesdivmodArithTerm
     | term (PLUS | MINUS) term             # term_plusminusArithTerm
     | term BITXOR term                     # term_bitxorArithTerm
     ;

interval : lower = (NUMBER | VARIABLE_OR_HEU_SIGNS) DOT DOT upper = (NUMBER | VARIABLE_OR_HEU_SIGNS); // NOT Core2 syntax, but widespread

external_atom : MINUS? AMPERSAND ID (SQUARE_OPEN input = terms SQUARE_CLOSE)? (PAREN_OPEN output = terms PAREN_CLOSE)?; // NOT Core2 syntax.

directive : directive_enumeration | directive_heuristic;  // NOT Core2 syntax, allows solver specific directives. Further directives shall be added here.

directive_enumeration : SHARP 'enumeration_predicate_is' ID DOT;  // NOT Core2 syntax, used for aggregate translation.

directive_heuristic : SHARP 'heuristic' heuristic_head_atom (heuristic_body)? DOT heuristic_weight_annotation?;

heuristic_head_atom : (heuristic_head_sign)? basic_atom;

heuristic_head_sign : VARIABLE_OR_HEU_SIGNS; // HEU_SIGN_T | HEU_SIGN_F;

heuristic_body : COLON heuristic_body_literal (COMMA heuristic_body_literal)*;

heuristic_body_literal : NAF? heuristic_body_atom;

heuristic_body_atom : (heuristic_body_signs)* basic_atom;

heuristic_body_signs : VARIABLE_OR_HEU_SIGNS; // HEU_SIGN_T | HEU_SIGN_M | HEU_SIGN_F;

heuristic_weight_annotation : SQUARE_OPEN heuristic_weight_at_level SQUARE_CLOSE;

heuristic_weight_at_level : term (AT term)?;

basic_terms : basic_term (COMMA basic_terms)? ;

basic_term : ground_term | variable_term;

ground_term : /*SYMBOLIC_CONSTANT*/ ID | QUOTED_STRING | MINUS? NUMBER;

variable_term : VARIABLE_OR_HEU_SIGNS | ANONYMOUS_VARIABLE;

answer_set : CURLY_OPEN classical_literal? (COMMA classical_literal)* CURLY_CLOSE;

answer_sets: answer_set* EOF;