// Generated from at/ac/tuwien/kr/alpha/antlr/ASPCore2.g4 by ANTLR 4.7
package at.ac.tuwien.kr.alpha.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ASPCore2Parser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ASPCore2Visitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(ASPCore2Parser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(ASPCore2Parser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#query}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuery(ASPCore2Parser.QueryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statement_fact}
	 * labeled alternative in {@link ASPCore2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_fact(ASPCore2Parser.Statement_factContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statement_constraint}
	 * labeled alternative in {@link ASPCore2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statement_rule}
	 * labeled alternative in {@link ASPCore2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statement_weightConstraint}
	 * labeled alternative in {@link ASPCore2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_weightConstraint(ASPCore2Parser.Statement_weightConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code statement_directive}
	 * labeled alternative in {@link ASPCore2Parser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_directive(ASPCore2Parser.Statement_directiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#head}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHead(ASPCore2Parser.HeadContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBody(ASPCore2Parser.BodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#disjunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDisjunction(ASPCore2Parser.DisjunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#choice}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChoice(ASPCore2Parser.ChoiceContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#choice_elements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChoice_elements(ASPCore2Parser.Choice_elementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#choice_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChoice_element(ASPCore2Parser.Choice_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#aggregate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate(ASPCore2Parser.AggregateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#aggregate_elements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_elements(ASPCore2Parser.Aggregate_elementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#aggregate_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_element(ASPCore2Parser.Aggregate_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#aggregate_function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregate_function(ASPCore2Parser.Aggregate_functionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#weight_at_level}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeight_at_level(ASPCore2Parser.Weight_at_levelContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#naf_literals}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaf_literals(ASPCore2Parser.Naf_literalsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#naf_literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#classical_literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#builtin_atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#binop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinop(ASPCore2Parser.BinopContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(ASPCore2Parser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_number}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_number(ASPCore2Parser.Term_numberContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_anonymousVariable}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_const}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_const(ASPCore2Parser.Term_constContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_minusArithTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_minusArithTerm(ASPCore2Parser.Term_minusArithTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_bitxorArithTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_bitxorArithTerm(ASPCore2Parser.Term_bitxorArithTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_interval}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_variable}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_variable(ASPCore2Parser.Term_variableContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_timesdivmodArithTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_timesdivmodArithTerm(ASPCore2Parser.Term_timesdivmodArithTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_plusminusArithTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_plusminusArithTerm(ASPCore2Parser.Term_plusminusArithTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_powerArithTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_powerArithTerm(ASPCore2Parser.Term_powerArithTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_func}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_func(ASPCore2Parser.Term_funcContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_string}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_string(ASPCore2Parser.Term_stringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code term_parenthesisedTerm}
	 * labeled alternative in {@link ASPCore2Parser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(ASPCore2Parser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#external_atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternal_atom(ASPCore2Parser.External_atomContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective(ASPCore2Parser.DirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#directive_enumeration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective_enumeration(ASPCore2Parser.Directive_enumerationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#basic_terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasic_terms(ASPCore2Parser.Basic_termsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#basic_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBasic_term(ASPCore2Parser.Basic_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#ground_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGround_term(ASPCore2Parser.Ground_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#variable_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable_term(ASPCore2Parser.Variable_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#answer_set}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnswer_set(ASPCore2Parser.Answer_setContext ctx);
	/**
	 * Visit a parse tree produced by {@link ASPCore2Parser#answer_sets}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnswer_sets(ASPCore2Parser.Answer_setsContext ctx);
}