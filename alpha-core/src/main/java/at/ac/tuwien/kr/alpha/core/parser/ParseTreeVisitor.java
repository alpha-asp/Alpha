/**
 * Copyright (c) 2016-2021, the Alpha Team.
 * All rights reserved.
 * <p>
 * Additional changes made by Siemens.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.core.parser;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.program.Literal;
import at.ac.tuwien.kr.alpha.api.rules.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.core.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.core.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.core.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.core.atoms.ExternalLiteral;
import at.ac.tuwien.kr.alpha.core.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.core.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.core.common.CoreAnswerSet;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.VariableTermImpl;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.Rules;
import at.ac.tuwien.kr.alpha.core.rules.heads.ChoiceHeadImpl;

/**
 * Copyright (c) 2016-2021, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {
	private final Map<String, PredicateInterpretation> externals;
	private final boolean acceptVariables;

	private InputProgramImpl.Builder programBuilder;
	private InlineDirectivesImpl inlineDirectives;

	public ParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		this(externals, true);
	}

	public ParseTreeVisitor(Map<String, PredicateInterpretation> externals, boolean acceptVariables) {
		this.externals = externals;
		this.acceptVariables = acceptVariables;
	}

	private UnsupportedOperationException notSupported(RuleContext ctx) {
		return new UnsupportedOperationException("Unsupported syntax encountered: " + ctx.getText());
	}

	/**
	 * Translates a program context (referring to a node in an ATN specific to ANTLR) to the internal representation of Alpha.
	 */
	public InputProgramImpl translate(ASPCore2Parser.ProgramContext input) {
		return visitProgram(input);
	}

	/**
	 * Translates a context for answer sets (referring to a node in an ATN specific to ANTLR) to the representation that Alpha uses.
	 */
	public Set<CoreAnswerSet> translate(ASPCore2Parser.Answer_setsContext input) {
		return visitAnswer_sets(input);
	}

	@Override
	public Set<CoreAnswerSet> visitAnswer_sets(ASPCore2Parser.Answer_setsContext ctx) {
		Set<CoreAnswerSet> result = new TreeSet<>();

		for (ASPCore2Parser.Answer_setContext answerSetContext : ctx.answer_set()) {
			result.add(visitAnswer_set(answerSetContext));
		}

		return result;
	}

	@Override
	public CoreAnswerSet visitAnswer_set(ASPCore2Parser.Answer_setContext ctx) {
		SortedSet<CorePredicate> predicates = new TreeSet<>();
		Map<CorePredicate, SortedSet<CoreAtom>> predicateInstances = new TreeMap<>();

		for (ASPCore2Parser.Classical_literalContext classicalLiteralContext : ctx.classical_literal()) {
			CoreAtom atom = visitClassical_literal(classicalLiteralContext);

			predicates.add(atom.getPredicate());
			predicateInstances.compute(atom.getPredicate(), (k, v) -> {
				if (v == null) {
					v = new TreeSet<>();
				}
				v.add(atom);
				return v;
			});

		}

		return new BasicAnswerSet(predicates, predicateInstances);
	}

	@Override
	public String visitTerminal(TerminalNode node) {
		return node.getText();
	}

	@Override
	public InputProgramImpl visitProgram(ASPCore2Parser.ProgramContext ctx) {
		// program : statements? query?;
		if (ctx.query() != null) {
			throw notSupported(ctx.query());
		}

		if (ctx.statements() == null) {
			return InputProgramImpl.EMPTY;
		}
		inlineDirectives = new InlineDirectivesImpl();
		programBuilder = InputProgramImpl.builder();
		visitStatements(ctx.statements());
		programBuilder.addInlineDirectives(inlineDirectives); // TODO inline directives
		return programBuilder.build();
	}

	@Override
	public Object visitStatements(ASPCore2Parser.StatementsContext ctx) {
		// statements : statement+;
		for (ASPCore2Parser.StatementContext statementContext : ctx.statement()) {
			visit(statementContext);
		}
		return null;
	}

	@Override
	public Object visitStatement_fact(ASPCore2Parser.Statement_factContext ctx) {
		// head DOT
		if (ctx.head().disjunction() != null) {
			if (ctx.head().disjunction().disjunction() != null) {
				// more than one disjunctive element
				notSupported(ctx);
			} else {
				programBuilder.addFact(visitClassical_literal(ctx.head().disjunction().classical_literal()));
			}
		} else {
			handleRule(ctx.head(), null);
		}
		return null;
	}

	private void handleRule(ASPCore2Parser.HeadContext headCtx, ASPCore2Parser.BodyContext bodyCtx) {
		List<Literal> body = null; // TODO
		if (headCtx == null) {
			// constraint
			programBuilder.addNormalRule(Rules.newConstraint(body));
		} else if (headCtx.disjunction() != null) {
			// normal or disjunctive rule
			if (headCtx.disjunction().disjunction() != null) {
				// more than one disjunctive element
				notSupported(headCtx);
			} else {
				programBuilder.addNormalRule(Rules.newNormalRule(visitClassical_literal(headCtx.disjunction().classical_literal()), body));
			}
		} else if (headCtx.choice() != null) {
			// choice rule
			ChoiceHead head = visitChoice(headCtx.choice());
			programBuilder.addChoiceRule(Rules.newChoiceRule(head, body));
		} else {
			notSupported(headCtx);
		}
	}

	@Override
	public Object visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		handleRule(null, ctx.body());
		return null;
	}

	@Override
	public Object visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head CONS body DOT
		handleRule(ctx.head(), ctx.body());
		return null;
	}

	@Override
	public Object visitStatement_weightConstraint(ASPCore2Parser.Statement_weightConstraintContext ctx) {
		// WCONS body? DOT SQUARE_OPEN weight_at_level SQUARE_CLOSE
		throw notSupported(ctx);
	}

	@Override
	public Object visitStatement_directive(ASPCore2Parser.Statement_directiveContext ctx) {
		// directive
		visitDirective(ctx.directive());
		// Parsed directives are globally stored, nothing to return here.
		return null;
	}

	@Override
	public ChoiceHead visitChoice(ASPCore2Parser.ChoiceContext ctx) {
		// choice : (lt=term lop=binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (uop=binop ut=term)?;
		CoreTerm lt = null;
		ComparisonOperator lop = null;
		CoreTerm ut = null;
		ComparisonOperator uop = null;
		if (ctx.lt != null) {
			lt = (CoreTerm) visit(ctx.lt);
			lop = visitBinop(ctx.lop);
		}
		if (ctx.ut != null) {
			ut = (CoreTerm) visit(ctx.ut);
			uop = visitBinop(ctx.uop);
		}
		return new ChoiceHeadImpl(visitChoice_elements(ctx.choice_elements()), lt, lop, ut, uop);
	}

	@Override
	public List<ChoiceHeadImpl.ChoiceElement> visitChoice_elements(ASPCore2Parser.Choice_elementsContext ctx) {
		// choice_elements : choice_element (SEMICOLON choice_elements)?;
		List<ChoiceHeadImpl.ChoiceElement> choiceElements;
		if (ctx.choice_elements() != null) {
			choiceElements = visitChoice_elements(ctx.choice_elements());
		} else {
			choiceElements = new LinkedList<>();
		}
		choiceElements.add(0, visitChoice_element(ctx.choice_element()));
		return choiceElements;
	}

	@Override
	public ChoiceHeadImpl.ChoiceElement visitChoice_element(ASPCore2Parser.Choice_elementContext ctx) {
		// choice_element : classical_literal (COLON naf_literals?)?;
		BasicAtom atom = (BasicAtom) visitClassical_literal(ctx.classical_literal());
		if (ctx.naf_literals() != null) {
			return new ChoiceHeadImpl.ChoiceElement(atom, visitNaf_literals(ctx.naf_literals()));
		} else {
			return new ChoiceHeadImpl.ChoiceElement(atom, Collections.emptyList());
		}
	}

	@Override
	public List<CoreLiteral> visitNaf_literals(ASPCore2Parser.Naf_literalsContext ctx) {
		// naf_literals : naf_literal (COMMA naf_literals)?;
		List<CoreLiteral> literals;
		if (ctx.naf_literals() != null) {
			literals = visitNaf_literals(ctx.naf_literals());
		} else {
			literals = new LinkedList<>();
		}
		literals.add(0, visitNaf_literal(ctx.naf_literal()));
		return literals;
	}

	@Override
	public Object visitDirective_enumeration(ASPCore2Parser.Directive_enumerationContext ctx) {
		// directive_enumeration : SHARP 'enum_predicate_is' ID DOT;
		inlineDirectives.addDirective(InlineDirectivesImpl.DIRECTIVE.enum_predicate_is, ctx.ID().getText());
		return null;
	}

	@Override
	public List<CoreLiteral> visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | aggregate ) (COMMA body)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<CoreLiteral> literals = new ArrayList<>();
		do {
			if (ctx.naf_literal() != null) {
				literals.add(visitNaf_literal(ctx.naf_literal()));
			} else {
				literals.add(visitAggregate(ctx.aggregate()));
			}
		} while ((ctx = ctx.body()) != null);

		return literals;
	}

	@Override
	public AggregateLiteral visitAggregate(ASPCore2Parser.AggregateContext ctx) {

		// aggregate : NAF? (lt=term lop=binop)? aggregate_function CURLY_OPEN aggregate_elements CURLY_CLOSE (uop=binop ut=term)?;
		boolean isPositive = ctx.NAF() == null;
		CoreTerm lt = null;
		ComparisonOperator lop = null;
		CoreTerm ut = null;
		ComparisonOperator uop = null;
		if (ctx.lt != null) {
			lt = (CoreTerm) visit(ctx.lt);
			lop = visitBinop(ctx.lop);
		}
		if (ctx.ut != null) {
			ut = (CoreTerm) visit(ctx.ut);
			uop = visitBinop(ctx.uop);
		}
		AggregateAtom.AGGREGATEFUNCTION aggregateFunction = visitAggregate_function(ctx.aggregate_function());
		List<AggregateAtom.AggregateElement> aggregateElements = visitAggregate_elements(ctx.aggregate_elements());
		return new AggregateAtom(lop, lt, uop, ut, aggregateFunction, aggregateElements).toLiteral(isPositive);
	}

	@Override
	public List<AggregateAtom.AggregateElement> visitAggregate_elements(ASPCore2Parser.Aggregate_elementsContext ctx) {
		// aggregate_elements : aggregate_element (SEMICOLON aggregate_elements)?;
		final List<AggregateAtom.AggregateElement> aggregateElements = new ArrayList<>();
		do {
			aggregateElements.add(visitAggregate_element(ctx.aggregate_element()));
		} while ((ctx = ctx.aggregate_elements()) != null);

		return aggregateElements;
	}

	@Override
	public AggregateAtom.AggregateElement visitAggregate_element(ASPCore2Parser.Aggregate_elementContext ctx) {
		// aggregate_element : basic_terms? (COLON naf_literals?)?;
		List<CoreTerm> basicTerms = ctx.basic_terms() != null ? visitBasic_terms(ctx.basic_terms()) : null;
		if (ctx.naf_literals() != null) {
			return new AggregateAtom.AggregateElement(basicTerms, visitNaf_literals(ctx.naf_literals()));
		}
		return new AggregateAtom.AggregateElement(basicTerms, Collections.emptyList());
	}

	@Override
	public List<CoreTerm> visitBasic_terms(ASPCore2Parser.Basic_termsContext ctx) {
		// basic_terms : basic_term (COMMA basic_terms)? ;
		List<CoreTerm> termList = new ArrayList<>();
		do {
			termList.add(visitBasic_term(ctx.basic_term()));
		} while ((ctx = ctx.basic_terms()) != null);
		return termList;
	}

	@Override
	public CoreTerm visitBasic_term(ASPCore2Parser.Basic_termContext ctx) {
		// basic_term : ground_term | variable_term;
		if (ctx.ground_term() != null) {
			return visitGround_term(ctx.ground_term());
		} else {
			return visitVariable_term(ctx.variable_term());
		}
	}

	@Override
	public CoreTerm visitGround_term(ASPCore2Parser.Ground_termContext ctx) {
		// ground_term : ID | QUOTED_STRING | MINUS? NUMBER;
		if (ctx.ID() != null) {
			return CoreConstantTerm.getSymbolicInstance(ctx.ID().getText());
		} else if (ctx.QUOTED_STRING() != null) {
			String quotedString = ctx.QUOTED_STRING().getText();
			return CoreConstantTerm.getInstance(quotedString.substring(1, quotedString.length() - 1));
		} else {
			int multiplier = 1;
			if (ctx.MINUS() != null) {
				multiplier = -1;
			}
			return CoreConstantTerm.getInstance(multiplier * Integer.parseInt(ctx.NUMBER().getText()));
		}
	}

	@Override
	// TODO proper "core" type
	public CoreTerm visitVariable_term(ASPCore2Parser.Variable_termContext ctx) {
		// variable_term : VARIABLE | ANONYMOUS_VARIABLE;
		if (ctx.VARIABLE() != null) {
			return VariableTermImpl.getInstance(ctx.VARIABLE().getText());
		} else {
			return VariableTermImpl.getAnonymousInstance();
		}
	}

	@Override
	public AggregateAtom.AGGREGATEFUNCTION visitAggregate_function(ASPCore2Parser.Aggregate_functionContext ctx) {
		// aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;
		if (ctx.AGGREGATE_COUNT() != null) {
			return AggregateAtom.AGGREGATEFUNCTION.COUNT;
		} else if (ctx.AGGREGATE_MAX() != null) {
			return AggregateAtom.AGGREGATEFUNCTION.MAX;
		} else if (ctx.AGGREGATE_MIN() != null) {
			return AggregateAtom.AGGREGATEFUNCTION.MIN;
		} else if (ctx.AGGREGATE_SUM() != null) {
			return AggregateAtom.AGGREGATEFUNCTION.SUM;
		} else {
			throw notSupported(ctx);
		}
	}

	@Override
	public ComparisonOperator visitBinop(ASPCore2Parser.BinopContext ctx) {
		// binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;
		if (ctx.EQUAL() != null) {
			return ComparisonOperator.EQ;
		} else if (ctx.UNEQUAL() != null) {
			return ComparisonOperator.NE;
		} else if (ctx.LESS() != null) {
			return ComparisonOperator.LT;
		} else if (ctx.LESS_OR_EQ() != null) {
			return ComparisonOperator.LE;
		} else if (ctx.GREATER() != null) {
			return ComparisonOperator.GT;
		} else if (ctx.GREATER_OR_EQ() != null) {
			return ComparisonOperator.GE;
		} else {
			throw notSupported(ctx);
		}
	}

	@Override
	public ComparisonAtom visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		return new ComparisonAtom(
				(CoreTerm) visit(ctx.term(0)),
				(CoreTerm) visit(ctx.term(1)),
				visitBinop(ctx.binop()));
	}

	@Override
	public CoreLiteral visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (external_atom | classical_literal | builtin_atom);
		boolean isCurrentLiteralNegated = ctx.NAF() != null;
		if (ctx.builtin_atom() != null) {
			return new ComparisonLiteral(visitBuiltin_atom(ctx.builtin_atom()), !isCurrentLiteralNegated);
		} else if (ctx.classical_literal() != null) {
			return new BasicLiteral(visitClassical_literal(ctx.classical_literal()), !isCurrentLiteralNegated);
		} else if (ctx.external_atom() != null) {
			return new ExternalLiteral(visitExternal_atom(ctx.external_atom()), !isCurrentLiteralNegated);
		}
		throw notSupported(ctx);
	}

	@Override
	public BasicAtom visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;
		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

		final List<CoreTerm> terms = visitTerms(ctx.terms());
		return new BasicAtom(CorePredicate.getInstance(ctx.ID().getText(), terms.size()), terms);
	}

	@Override
	public List<CoreTerm> visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<CoreTerm> terms = new ArrayList<>();
		do {
			ASPCore2Parser.TermContext term = ctx.term();
			terms.add((CoreTerm) visit(term));
		} while ((ctx = ctx.terms()) != null);

		return terms;
	}

	@Override
	public CoreConstantTerm<?> visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		return CoreConstantTerm.getInstance(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public CoreConstantTerm<?> visitTerm_const(ASPCore2Parser.Term_constContext ctx) {
		return CoreConstantTerm.getSymbolicInstance(ctx.ID().getText());
	}

	@Override
	public CoreConstantTerm<?> visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		String quotedString = ctx.QUOTED_STRING().getText().replace("\\\"", "\"");
		return CoreConstantTerm.getInstance(quotedString.substring(1, quotedString.length() - 1));
	}

	@Override
	public FunctionTerm visitTerm_func(ASPCore2Parser.Term_funcContext ctx) {
		return FunctionTerm.getInstance(ctx.ID().getText(), visitTerms(ctx.terms()));
	}

	@Override
	// TODO proper "core" type
	public VariableTerm visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}

		return VariableTermImpl.getAnonymousInstance();
	}

	@Override
	// TODO proper "core" type
	public VariableTerm visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}

		return VariableTermImpl.getInstance(ctx.VARIABLE().getText());
	}

	@Override
	public Term visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}

	@Override
	public ExternalAtom visitExternal_atom(ASPCore2Parser.External_atomContext ctx) {
		// external_atom : AMPERSAND ID (SQUARE_OPEN input = terms SQUARE_CLOSE)? (PAREN_OPEN output = terms PAREN_CLOSE)?;

		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

		final String predicateName = ctx.ID().getText();
		final PredicateInterpretation interpretation = externals.get(predicateName);

		if (interpretation == null) {
			throw new IllegalArgumentException("Unknown interpretation name encountered: " + predicateName);
		}

		List<CoreTerm> outputTerms = visitTerms(ctx.output);

		return new ExternalAtom(
				CorePredicate.getInstance(predicateName, outputTerms.size()),
				interpretation,
				visitTerms(ctx.input),
				outputTerms);
	}

	@Override
	// TODO proper "core" type
	public IntervalTerm visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx) {
		// interval : lower = (NUMBER | VARIABLE) DOT DOT upper = (NUMBER | VARIABLE);
		ASPCore2Parser.IntervalContext ictx = ctx.interval();
		String lowerText = ictx.lower.getText();
		String upperText = ictx.upper.getText();
		CoreTerm lower = ictx.lower.getType() == ASPCore2Lexer.NUMBER ? CoreConstantTerm.getInstance(Integer.parseInt(lowerText))
				: VariableTermImpl.getInstance(lowerText);
		CoreTerm upper = ictx.upper.getType() == ASPCore2Lexer.NUMBER ? CoreConstantTerm.getInstance(Integer.parseInt(upperText))
				: VariableTermImpl.getInstance(upperText);
		return IntervalTerm.getInstance(lower, upper);
	}

	@Override
	public Object visitTerm_minusArithTerm(ASPCore2Parser.Term_minusArithTermContext ctx) {
		// | MINUS term
		return ArithmeticTerm.MinusTerm.getInstance((CoreTerm) visit(ctx.term()));
	}

	@Override
	public Object visitTerm_timesdivmodArithTerm(ASPCore2Parser.Term_timesdivmodArithTermContext ctx) {
		// | term (TIMES | DIV | MODULO) term
		ArithmeticTerm.ArithmeticOperator op = ctx.TIMES() != null ? ArithmeticTerm.ArithmeticOperator.TIMES
				: ctx.DIV() != null ? ArithmeticTerm.ArithmeticOperator.DIV : ArithmeticTerm.ArithmeticOperator.MODULO;
		return ArithmeticTerm.getInstance((CoreTerm) visit(ctx.term(0)), op, (CoreTerm) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_plusminusArithTerm(ASPCore2Parser.Term_plusminusArithTermContext ctx) {
		// | term (PLUS | MINUS) term
		ArithmeticTerm.ArithmeticOperator op = ctx.PLUS() != null ? ArithmeticTerm.ArithmeticOperator.PLUS : ArithmeticTerm.ArithmeticOperator.MINUS;
		return ArithmeticTerm.getInstance((CoreTerm) visit(ctx.term(0)), op, (CoreTerm) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_powerArithTerm(ASPCore2Parser.Term_powerArithTermContext ctx) {
		// |<assoc=right> term POWER term
		ArithmeticTerm.ArithmeticOperator op = ArithmeticTerm.ArithmeticOperator.POWER;
		return ArithmeticTerm.getInstance((CoreTerm) visit(ctx.term(0)), op, (CoreTerm) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_bitxorArithTerm(ASPCore2Parser.Term_bitxorArithTermContext ctx) {
		// | term BITXOR term
		return ArithmeticTerm.getInstance((CoreTerm) visit(ctx.term(0)), ArithmeticTerm.ArithmeticOperator.BITXOR, (CoreTerm) visit(ctx.term(1)));
	}
}
