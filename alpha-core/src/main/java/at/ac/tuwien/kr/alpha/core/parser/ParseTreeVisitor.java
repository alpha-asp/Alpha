/**
 * Copyright (c) 2016-2023, the Alpha Team.
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

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.*;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.ChoiceHead;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.ChoiceHead.ChoiceElement;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.programs.terms.*;
import at.ac.tuwien.kr.alpha.api.programs.tests.Assertion;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.commons.AnswerSets;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.Programs.ASPCore2ProgramBuilder;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.programs.tests.Tests;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.function.IntPredicate;

/**
 * Copyright (c) 2016-2018, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {
	private final Map<String, PredicateInterpretation> externals;
	private final boolean acceptVariables;

	private InlineDirectives inlineDirectives;

	/*
	 * Since verifiers for tests are ASP programs in themselves, we need to parse nested programs.
	 * Therefore, have a stack onto which we "park" a program builder for the outer scope (i.e. main program)
	 * while we parse the inner scope (i.e. test verifier).
	 */
	private ASPCore2ProgramBuilder currentLevelProgramBuilder;
	private Stack<ASPCore2ProgramBuilder> programBuilders = new Stack<>();

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
	public ASPCore2Program translate(ASPCore2Parser.ProgramContext input) {
		return visitProgram(input);
	}

	/**
	 * Translates a context for answer sets (referring to a node in an ATN specific to ANTLR) to the representation that Alpha uses.
	 */
	public Set<AnswerSet> translate(ASPCore2Parser.Answer_setsContext input) {
		return visitAnswer_sets(input);
	}

	@Override
	public Set<AnswerSet> visitAnswer_sets(ASPCore2Parser.Answer_setsContext ctx) {
		Set<AnswerSet> result = new TreeSet<>();

		for (ASPCore2Parser.Answer_setContext answerSetContext : ctx.answer_set()) {
			result.add(visitAnswer_set(answerSetContext));
		}

		return result;
	}

	@Override
	public AnswerSet visitAnswer_set(ASPCore2Parser.Answer_setContext ctx) {
		SortedSet<Predicate> predicates = new TreeSet<>();
		Map<Predicate, SortedSet<Atom>> predicateInstances = new TreeMap<>();

		for (ASPCore2Parser.Classical_literalContext classicalLiteralContext : ctx.classical_literal()) {
			Atom atom = visitClassical_literal(classicalLiteralContext);

			predicates.add(atom.getPredicate());
			predicateInstances.compute(atom.getPredicate(), (k, v) -> {
				if (v == null) {
					v = new TreeSet<>();
				}
				v.add(atom);
				return v;
			});

		}

		return AnswerSets.newAnswerSet(predicates, predicateInstances);
	}

	@Override
	public String visitTerminal(TerminalNode node) {
		return node.getText();
	}

	@Override
	public ASPCore2Program visitProgram(ASPCore2Parser.ProgramContext ctx) {
		// program : statements? query?;
		if (ctx.query() != null) {
			throw notSupported(ctx.query());
		}

		if (ctx.statements() == null) {
			return Programs.emptyProgram();
		}
		inlineDirectives = Programs.newInlineDirectives();
		currentLevelProgramBuilder = Programs.builder();
		visitStatements(ctx.statements());
		currentLevelProgramBuilder.addInlineDirectives(inlineDirectives);
		return currentLevelProgramBuilder.build();
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
		Head head = visitHead(ctx.head());
		if (head instanceof NormalHead) {
			currentLevelProgramBuilder.addFact(((NormalHead) head).getAtom());
		} else {
			// Treat facts with choice or disjunction in the head like a rule.
			currentLevelProgramBuilder.addRule(Rules.newRule(head, Collections.emptyList()));
		}
		return null;
	}

	@Override
	public Object visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		currentLevelProgramBuilder.addRule(Rules.newRule(null, visitBody(ctx.body())));
		return null;
	}

	@Override
	public Object visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head CONS body DOT
		currentLevelProgramBuilder.addRule(Rules.newRule(visitHead(ctx.head()), visitBody(ctx.body())));
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
	public Head visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		// disjunction : classical_literal (OR disjunction)?;
		if (ctx.disjunction() != null) {
			throw notSupported(ctx);
		}
		return Heads.newNormalHead(visitClassical_literal(ctx.classical_literal()));
	}

	@Override
	public Head visitHead(ASPCore2Parser.HeadContext ctx) {
		// head : disjunction | choice;
		if (ctx.choice() != null) {
			return visitChoice(ctx.choice());
		}
		return visitDisjunction(ctx.disjunction());
	}

	@Override
	public Head visitChoice(ASPCore2Parser.ChoiceContext ctx) {
		// choice : (lt=term lop=binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (uop=binop ut=term)?;
		Term lt = null;
		ComparisonOperator lop = null;
		Term ut = null;
		ComparisonOperator uop = null;
		if (ctx.lt != null) {
			lt = (Term) visit(ctx.lt);
			lop = visitBinop(ctx.lop);
		}
		if (ctx.ut != null) {
			ut = (Term) visit(ctx.ut);
			uop = visitBinop(ctx.uop);
		}
		return Heads.newChoiceHead(visitChoice_elements(ctx.choice_elements()), lt, lop, ut, uop);
	}

	@Override
	public List<ChoiceElement> visitChoice_elements(ASPCore2Parser.Choice_elementsContext ctx) {
		// choice_elements : choice_element (SEMICOLON choice_elements)?;
		List<ChoiceHead.ChoiceElement> choiceElements;
		if (ctx.choice_elements() != null) {
			choiceElements = visitChoice_elements(ctx.choice_elements());
		} else {
			choiceElements = new LinkedList<>();
		}
		choiceElements.add(0, visitChoice_element(ctx.choice_element()));
		return choiceElements;
	}

	@Override
	public ChoiceHead.ChoiceElement visitChoice_element(ASPCore2Parser.Choice_elementContext ctx) {
		// choice_element : classical_literal (COLON naf_literals?)?;
		BasicAtom atom = visitClassical_literal(ctx.classical_literal());
		if (ctx.naf_literals() != null) {
			return Heads.newChoiceElement(atom, visitNaf_literals(ctx.naf_literals()));
		} else {
			return Heads.newChoiceElement(atom, Collections.emptyList());
		}
	}

	@Override
	public List<Literal> visitNaf_literals(ASPCore2Parser.Naf_literalsContext ctx) {
		// naf_literals : naf_literal (COMMA naf_literals)?;
		List<Literal> literals;
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
		// directive_enumeration : DIRECTIVE_ENUM id DOT;
		inlineDirectives.addDirective(InlineDirectives.DIRECTIVE.enum_predicate_is, visitId(ctx.id()));
		return null;
	}

	@Override
	public Object visitDirective_test(ASPCore2Parser.Directive_testContext ctx) {
		// directive_test : DIRECTIVE_TEST id PAREN_OPEN test_satisfiability_condition PAREN_CLOSE CURLY_OPEN test_input test_assert* CURLY_CLOSE;
		String name = visitId(ctx.id());
		IntPredicate answerSetCountVerifier = visitTest_satisfiability_condition(ctx.test_satisfiability_condition());
		Set<BasicAtom> input = visitTest_input(ctx.test_input());
		List<Assertion> assertions;
		if (ctx.test_assert() == null) {
			assertions = Collections.emptyList();
		} else {
			assertions = new ArrayList<>();
			for (ASPCore2Parser.Test_assertContext assertionCtx : ctx.test_assert()) {
				assertions.add(visitTest_assert(assertionCtx));
			}
		}
		TestCase testCase = Tests.newTestCase(name, answerSetCountVerifier, input, assertions);
		currentLevelProgramBuilder.addTestCase(testCase);
		return null;
	}

	@Override
	public List<Literal> visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | aggregate ) (COMMA body)?;
		if (ctx == null) {
			return Collections.emptyList();
		}

		final List<Literal> literals = new ArrayList<>();
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
		Term lt = null;
		ComparisonOperator lop = null;
		Term ut = null;
		ComparisonOperator uop = null;
		if (ctx.lt != null) {
			lt = (Term) visit(ctx.lt);
			lop = visitBinop(ctx.lop);
		}
		if (ctx.ut != null) {
			ut = (Term) visit(ctx.ut);
			uop = visitBinop(ctx.uop);
		}
		AggregateAtom.AggregateFunctionSymbol aggregateFunction = visitAggregate_function(ctx.aggregate_function());
		List<AggregateAtom.AggregateElement> aggregateElements = visitAggregate_elements(ctx.aggregate_elements());
		return Atoms.newAggregateAtom(lop, lt, uop, ut, aggregateFunction, aggregateElements).toLiteral(isPositive);
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
		List<Term> basicTerms = ctx.basic_terms() != null ? visitBasic_terms(ctx.basic_terms()) : null;
		if (ctx.naf_literals() != null) {
			return Atoms.newAggregateElement(basicTerms, visitNaf_literals(ctx.naf_literals()));
		}
		return Atoms.newAggregateElement(basicTerms, Collections.emptyList());
	}

	@Override
	public List<Term> visitBasic_terms(ASPCore2Parser.Basic_termsContext ctx) {
		// basic_terms : basic_term (COMMA basic_terms)? ;
		List<Term> termList = new ArrayList<>();
		do {
			termList.add(visitBasic_term(ctx.basic_term()));
		} while ((ctx = ctx.basic_terms()) != null);
		return termList;
	}

	@Override
	public Term visitBasic_term(ASPCore2Parser.Basic_termContext ctx) {
		// basic_term : ground_term | variable_term;
		if (ctx.ground_term() != null) {
			return visitGround_term(ctx.ground_term());
		} else {
			return visitVariable_term(ctx.variable_term());
		}
	}

	@Override
	public Term visitGround_term(ASPCore2Parser.Ground_termContext ctx) {
		// ground_term : id | QUOTED_STRING | numeral;
		if (ctx.id() != null) {
			// id
			return Terms.newSymbolicConstant(visitId(ctx.id()));
		} else if (ctx.QUOTED_STRING() != null) {
			// QUOTED_STRING
			String quotedString = ctx.QUOTED_STRING().getText();
			return Terms.newConstant(quotedString.substring(1, quotedString.length() - 1));
		} else {
			// numeral
			return Terms.newConstant(visitNumeral(ctx.numeral()));
		}
	}

	@Override
	public Term visitVariable_term(ASPCore2Parser.Variable_termContext ctx) {
		// variable_term : VARIABLE | ANONYMOUS_VARIABLE;
		if (ctx.VARIABLE() != null) {
			return Terms.newVariable(ctx.VARIABLE().getText());
		} else {
			return Terms.newAnonymousVariable();
		}
	}

	@Override
	public AggregateAtom.AggregateFunctionSymbol visitAggregate_function(ASPCore2Parser.Aggregate_functionContext ctx) {
		// aggregate_function : AGGREGATE_COUNT | AGGREGATE_MAX | AGGREGATE_MIN | AGGREGATE_SUM;
		if (ctx.AGGREGATE_COUNT() != null) {
			return AggregateAtom.AggregateFunctionSymbol.COUNT;
		} else if (ctx.AGGREGATE_MAX() != null) {
			return AggregateAtom.AggregateFunctionSymbol.MAX;
		} else if (ctx.AGGREGATE_MIN() != null) {
			return AggregateAtom.AggregateFunctionSymbol.MIN;
		} else if (ctx.AGGREGATE_SUM() != null) {
			return AggregateAtom.AggregateFunctionSymbol.SUM;
		} else {
			throw notSupported(ctx);
		}
	}

	@Override
	public ComparisonOperator visitBinop(ASPCore2Parser.BinopContext ctx) {
		// binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;
		if (ctx.EQUAL() != null) {
			return ComparisonOperators.EQ;
		} else if (ctx.UNEQUAL() != null) {
			return ComparisonOperators.NE;
		} else if (ctx.LESS() != null) {
			return ComparisonOperators.LT;
		} else if (ctx.LESS_OR_EQ() != null) {
			return ComparisonOperators.LE;
		} else if (ctx.GREATER() != null) {
			return ComparisonOperators.GT;
		} else if (ctx.GREATER_OR_EQ() != null) {
			return ComparisonOperators.GE;
		} else {
			throw notSupported(ctx);
		}
	}

	@Override
	public ComparisonAtom visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		return Atoms.newComparisonAtom(
				(Term) visit(ctx.term(0)),
				(Term) visit(ctx.term(1)),
				visitBinop(ctx.binop()));
	}

	@Override
	public Literal visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (external_atom | classical_literal | builtin_atom);
		boolean isCurrentLiteralNegated = ctx.NAF() != null;
		if (ctx.builtin_atom() != null) {
			return Literals.fromAtom(visitBuiltin_atom(ctx.builtin_atom()), !isCurrentLiteralNegated);
		} else if (ctx.classical_literal() != null) {
			return Literals.fromAtom(visitClassical_literal(ctx.classical_literal()), !isCurrentLiteralNegated);
		} else if (ctx.external_atom() != null) {
			return Literals.fromAtom(visitExternal_atom(ctx.external_atom()), !isCurrentLiteralNegated);
		}
		throw notSupported(ctx);
	}

	@Override
	public String visitId(ASPCore2Parser.IdContext ctx) {
		// id : ID | TEST_EXPECT | TEST_UNSAT | TEST_GIVEN | TEST_ASSERT_ALL | TEST_ASSERT_SOME;
		return ctx.getText();
	}

	@Override
	public BasicAtom visitBasic_atom(ASPCore2Parser.Basic_atomContext ctx) {
		// basic_atom : ID (PAREN_OPEN terms PAREN_CLOSE)?;
		List<Term> terms = visitTerms(ctx.terms());
		return Atoms.newBasicAtom(Predicates.getPredicate(visitId(ctx.id()), terms.size()), terms);
	}

	@Override
	public BasicAtom visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? basic_atom;
		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}
		return visitBasic_atom(ctx.basic_atom());
	}

	@Override
	public List<Term> visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		if (ctx == null) {
			return Collections.emptyList();
		}

		final List<Term> terms = new ArrayList<>();
		do {
			ASPCore2Parser.TermContext term = ctx.term();
			terms.add((Term) visit(term));
		} while ((ctx = ctx.terms()) != null);

		return terms;
	}

	@Override
	public ConstantTerm<Integer> visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		// term : numeral
		return Terms.newConstant(visitNumeral(ctx.numeral()));
	}

	public Integer visitNumeral(ASPCore2Parser.NumeralContext ctx) { 
		// numeral : MINUS? NUMBER;
		int absValue = Integer.valueOf(ctx.NUMBER().getText());
		return ctx.MINUS() != null ? -1 * absValue : absValue;
	}

	@Override
	public ConstantTerm<String> visitTerm_const(ASPCore2Parser.Term_constContext ctx) {
		return Terms.newSymbolicConstant(visitId(ctx.id()));
	}

	@Override
	public ConstantTerm<String> visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		String quotedString = ctx.QUOTED_STRING().getText().replace("\\\"", "\"");
		return Terms.newConstant(quotedString.substring(1, quotedString.length() - 1));
	}

	@Override
	public FunctionTerm visitTerm_func(ASPCore2Parser.Term_funcContext ctx) {
		return Terms.newFunctionTerm(visitId(ctx.id()), visitTerms(ctx.terms()));
	}

	@Override
	public VariableTerm visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}
		return Terms.newAnonymousVariable();
	}

	@Override
	public VariableTerm visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}
		return Terms.newVariable(ctx.VARIABLE().getText());
	}

	@Override
	public Term visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}

	@Override
	public ExternalAtom visitExternal_atom(ASPCore2Parser.External_atomContext ctx) {
		// external_atom : AMPERSAND id (SQUARE_OPEN input = terms SQUARE_CLOSE)? (PAREN_OPEN output = terms PAREN_CLOSE)?;

		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

		final String predicateName = visitId(ctx.id());
		final PredicateInterpretation interpretation = externals.get(predicateName);

		if (interpretation == null) {
			throw new IllegalArgumentException("Unknown interpretation name encountered: " + predicateName);
		}

		List<Term> outputTerms = visitTerms(ctx.output);

		return Atoms.newExternalAtom(
				Predicates.getPredicate(predicateName, outputTerms.size()),
				interpretation,
				visitTerms(ctx.input),
				outputTerms);
	}

	@Override
	public IntervalTerm visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx) {
		// interval : lower = interval_bound DOT DOT upper = interval_bound;
		ASPCore2Parser.IntervalContext ictx = ctx.interval();
		Term lower = visitInterval_bound(ictx.lower);
		Term upper = visitInterval_bound(ictx.upper);
		return Terms.newIntervalTerm(lower, upper);
	}

	@Override 
	public Term visitInterval_bound(ASPCore2Parser.Interval_boundContext ctx) {
		// interval_bound : numeral | VARIABLE;
		if (ctx.numeral() != null) {
			return Terms.newConstant(visitNumeral(ctx.numeral()));
		} else {
			return Terms.newVariable(ctx.VARIABLE().getText());
		}
	}

	@Override
	public Object visitTerm_minusArithTerm(ASPCore2Parser.Term_minusArithTermContext ctx) {
		// | MINUS term
		return Terms.newMinusTerm((Term) visit(ctx.term()));
	}

	@Override
	public Object visitTerm_timesdivmodArithTerm(ASPCore2Parser.Term_timesdivmodArithTermContext ctx) {
		// | term (TIMES | DIV | MODULO) term
		ArithmeticOperator op = ctx.TIMES() != null ? ArithmeticOperator.TIMES
				: ctx.DIV() != null ? ArithmeticOperator.DIV : ArithmeticOperator.MODULO;
		return Terms.newArithmeticTerm((Term) visit(ctx.term(0)), op, (Term) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_plusminusArithTerm(ASPCore2Parser.Term_plusminusArithTermContext ctx) {
		// | term (PLUS | MINUS) term
		ArithmeticOperator op = ctx.PLUS() != null ? ArithmeticOperator.PLUS : ArithmeticOperator.MINUS;
		return Terms.newArithmeticTerm((Term) visit(ctx.term(0)), op, (Term) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_powerArithTerm(ASPCore2Parser.Term_powerArithTermContext ctx) {
		// |<assoc=right> term POWER term
		ArithmeticOperator op = ArithmeticOperator.POWER;
		return Terms.newArithmeticTerm((Term) visit(ctx.term(0)), op, (Term) visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_bitxorArithTerm(ASPCore2Parser.Term_bitxorArithTermContext ctx) {
		// | term BITXOR term
		return Terms.newArithmeticTerm((Term) visit(ctx.term(0)), ArithmeticOperator.BITXOR, (Term) visit(ctx.term(1)));
	}

	public IntPredicate visitTest_satisfiability_condition(ASPCore2Parser.Test_satisfiability_conditionContext ctx) {
		// 'expect' COLON ('unsat' | (binop? NUMBER));
		if (ctx.binop() == null && ctx.NUMBER() == null) {
			// 'unsat'
			return Tests.newIsUnsatCondition();
		} else {
			// binop? NUMBER
			int num = Integer.valueOf(ctx.NUMBER().getText());
			if (ctx.binop() == null) {
				return Tests.newAnswerSetCountCondition(ComparisonOperators.EQ, num);
			} else {
				ComparisonOperator op = visitBinop(ctx.binop());
				return Tests.newAnswerSetCountCondition(op, num);
			}
		}
	}

	@Override
	public Set<BasicAtom> visitTest_input(ASPCore2Parser.Test_inputContext ctx) {
		if (ctx.basic_atom() == null) {
			return Collections.emptySet();
		}
		Set<BasicAtom> result = new LinkedHashSet<>();
		for (ASPCore2Parser.Basic_atomContext atomCtx : ctx.basic_atom()) {
			result.add(visitBasic_atom(atomCtx));
		}
		return result;
	}

	public Assertion visitTest_assert(ASPCore2Parser.Test_assertContext ctx) {
		if (ctx.test_assert_all() != null) {
			return visitTest_assert_all(ctx.test_assert_all());
		} else if (ctx.test_assert_some() != null) {
			return visitTest_assert_some(ctx.test_assert_some());
		} else {
			throw new IllegalArgumentException("Unsupported assertion mode at: " + ctx.getText());
		}
	}

	@Override
	public Assertion visitTest_assert_all(ASPCore2Parser.Test_assert_allContext ctx) {
		// 'assert' 'for' 'all' CURLY_OPEN statements? CURLY_CLOSE;
		return visitTestVerifier(Assertion.Mode.FOR_ALL, ctx.statements());
	}

	@Override
	public Assertion visitTest_assert_some(ASPCore2Parser.Test_assert_someContext ctx) {
		// 'assert' 'for' 'some' CURLY_OPEN statements? CURLY_CLOSE;
		return visitTestVerifier(Assertion.Mode.FOR_SOME, ctx.statements());
	}

	public Assertion visitTestVerifier(Assertion.Mode assertionMode, ASPCore2Parser.StatementsContext ctx) {
		if (ctx == null) { // empty verifier for a test case is OK
			return Tests.newAssertion(assertionMode, Programs.emptyProgram());
		}
		List<ASPCore2Parser.StatementContext> stmts = ctx.statement();
		programBuilders.push(currentLevelProgramBuilder);
		currentLevelProgramBuilder = new ASPCore2ProgramBuilder();
		for (ASPCore2Parser.StatementContext stmtCtx : stmts) {
			visit(stmtCtx);
		}
		ASPCore2Program verifier = currentLevelProgramBuilder.build();
		currentLevelProgramBuilder = programBuilders.pop();
		return Tests.newAssertion(assertionMode, verifier);
	}

}
