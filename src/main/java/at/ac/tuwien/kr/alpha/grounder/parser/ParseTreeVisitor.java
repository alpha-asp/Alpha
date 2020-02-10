/*
 * Copyright (c) 2016-2020, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
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
package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser.Directive_heuristicContext;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser.Weight_annotationContext;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser.Weight_at_levelContext;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.common.ChoiceHead;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.EnumerationDirective;
import at.ac.tuwien.kr.alpha.common.Head;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.WeightAtLevel;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveBody;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveLiteral;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives.DIRECTIVE;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static at.ac.tuwien.kr.alpha.Util.oops;
import static java.util.Collections.emptyList;

public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {
	private final Map<String, PredicateInterpretation> externals;
	private final boolean acceptVariables;

	private Program inputProgram;
	private InlineDirectives inlineDirectives;

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
	 * Translates a program context (referring to a node in an ATN specific to ANTLR)
	 * to the internal representation of Alpha.
	 */
	public Program translate(ASPCore2Parser.ProgramContext input) {
		return visitProgram(input);
	}

	/**
	 * Translates a context for answer sets (referring to a node in an ATN specific to ANTLR)
	 * to the representation that Alpha uses.
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

		return new BasicAnswerSet(predicates, predicateInstances);
	}

	@Override
	public String visitTerminal(TerminalNode node) {
		return node.getText();
	}

	/*protected CommonParsedObject aggregateResult(CommonParsedObject aggregate, CommonParsedObject nextResult) {
		ListOfParsedObjects aggList;
		if (aggregate instanceof ListOfParsedObjects) {
			aggList = (ListOfParsedObjects) aggregate;
			((ListOfParsedObjects) aggregate).add(nextResult);
		}  else {
			aggList = new ListOfParsedObjects(new ArrayList<>());
		}

		if (aggregate != null) {	// default result is null, ignore it
			aggList.add(aggregate);
		}

		if (nextResult instanceof ListOfParsedObjects) {
			aggList.addAll((ListOfParsedObjects) nextResult);
		} else {
			aggList.add(nextResult);
		}

		return aggList;
	}*/

	@Override
	public Program visitProgram(ASPCore2Parser.ProgramContext ctx) {
		// program : statements? query?;
		if (ctx.query() != null) {
			throw notSupported(ctx.query());
		}

		if (ctx.statements() == null) {
			return Program.EMPTY;
		}
		inlineDirectives = new InlineDirectives();
		inputProgram = new Program(new ArrayList<>(), new ArrayList<>(), inlineDirectives);
		visitStatements(ctx.statements());
		return inputProgram;
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
		if (head.isNormal()) {
			inputProgram.getFacts().add(((DisjunctiveHead)head).disjunctiveAtoms.get(0));
		} else {
			// Treat facts with choice or disjunction in the head like a rule.
			inputProgram.getRules().add(new Rule(head, emptyList()));
		}
		return null;
	}



	@Override
	public Object visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		inputProgram.getRules().add(new Rule(null, visitBody(ctx.body())));
		return null;
	}

	@Override
	public Object visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head CONS body DOT
		inputProgram.getRules().add(new Rule(visitHead(ctx.head()), visitBody(ctx.body())));
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
		return new DisjunctiveHead(Collections.singletonList(visitClassical_literal(ctx.classical_literal())));
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
		return new ChoiceHead(visitChoice_elements(ctx.choice_elements()), lt, lop, ut, uop);
	}

	@Override
	public List<ChoiceHead.ChoiceElement> visitChoice_elements(ASPCore2Parser.Choice_elementsContext ctx) {
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
			return new ChoiceHead.ChoiceElement(atom, visitNaf_literals(ctx.naf_literals()));
		} else {
			return new ChoiceHead.ChoiceElement(atom, Collections.emptyList());
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
		// directive_enumeration : SHARP 'enum_predicate_is' ID DOT;
		inlineDirectives.addDirective(InlineDirectives.DIRECTIVE.enum_predicate_is, new EnumerationDirective(ctx.ID().getText()));
		return null;
	}

	@Override
	public List<Literal> visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | aggregate ) (COMMA body)?;
		if (ctx == null) {
			return emptyList();
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
		List<Term> basicTerms = ctx.basic_terms() != null ? visitBasic_terms(ctx.basic_terms()) : null;
		if (ctx.naf_literals() != null) {
			return new AggregateAtom.AggregateElement(basicTerms, visitNaf_literals(ctx.naf_literals()));
		}
		return new AggregateAtom.AggregateElement(basicTerms, Collections.emptyList());
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
		// ground_term : ID | QUOTED_STRING | MINUS? NUMBER;
		if (ctx.ID() != null) {
			return ConstantTerm.getSymbolicInstance(ctx.ID().getText());
		} else if (ctx.QUOTED_STRING() != null) {
			String quotedString = ctx.QUOTED_STRING().getText();
			return ConstantTerm.getInstance(quotedString.substring(1, quotedString.length() - 1));
		} else {
			int multiplier = 1;
			if (ctx.MINUS() != null) {
				multiplier = -1;
			}
			return ConstantTerm.getInstance(multiplier * Integer.parseInt(ctx.NUMBER().getText()));
		}
	}

	@Override
	public Term visitVariable_term(ASPCore2Parser.Variable_termContext ctx) {
		// variable_term : variable | ANONYMOUS_VARIABLE;
		if (ctx.variable() != null) {
			return VariableTerm.getInstance(ctx.variable().getText());
		} else {
			return VariableTerm.getAnonymousInstance();
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
			(Term) visit(ctx.term(0)),
			(Term) visit(ctx.term(1)),
			visitBinop(ctx.binop())
		);
	}

	@Override
	public Literal visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
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
	public Atom visitAtom(ASPCore2Parser.AtomContext ctx) {
		if (ctx.builtin_atom() != null) {
			return visitBuiltin_atom(ctx.builtin_atom());
		} else if (ctx.classical_literal() != null) {
			return visitClassical_literal(ctx.classical_literal());
		} else if (ctx.external_atom() != null) {
			return visitExternal_atom(ctx.external_atom());
		}
		throw notSupported(ctx);
	}

	@Override
	public WeightAtLevel visitWeight_annotation(Weight_annotationContext ctx) {
		// SQUARE_OPEN weight_at_level SQUARE_CLOSE
		if (ctx == null) {
			return new WeightAtLevel(null, null);
		}
		return visitWeight_at_level(ctx.weight_at_level());
	}
	
	@Override
	public WeightAtLevel visitWeight_at_level(Weight_at_levelContext ctx) {
		// term (AT term)? (COMMA terms)?
		Term weight;
		Term level = null;
		weight = (Term) visit(ctx.term(0));
		if (ctx.AT() != null) {
			level = (Term) visit(ctx.term(1));
		}
		// FIXME: further terms are currently ignored

		return new WeightAtLevel(weight, level);
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
	public BasicAtom visitBasic_atom(ASPCore2Parser.Basic_atomContext ctx) {
		// basic_atom : ID (PAREN_OPEN terms PAREN_CLOSE)?;
		final List<Term> terms = visitTerms(ctx.terms());
		return new BasicAtom(Predicate.getInstance(ctx.ID().getText(), terms.size()), terms);
	}

	@Override
	public List<Term> visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<Term> terms = new ArrayList<>();
		do  {
			ASPCore2Parser.TermContext term = ctx.term();
			terms.add((Term) visit(term));
		} while ((ctx = ctx.terms()) != null);

		return terms;
	}

	@Override
	public ConstantTerm<?> visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		return ConstantTerm.getInstance(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public ConstantTerm<?> visitTerm_const(ASPCore2Parser.Term_constContext ctx) {
		return ConstantTerm.getSymbolicInstance(ctx.ID().getText());
	}

	@Override
	public ConstantTerm<?> visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		String quotedString = ctx.QUOTED_STRING().getText();
		return ConstantTerm.getInstance(quotedString.substring(1, quotedString.length() - 1));
	}

	@Override
	public FunctionTerm visitTerm_func(ASPCore2Parser.Term_funcContext ctx) {
		return FunctionTerm.getInstance(ctx.ID().getText(), visitTerms(ctx.terms()));
	}

	@Override
	public VariableTerm visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}

		return VariableTerm.getAnonymousInstance();
	}

	@Override
	public VariableTerm visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		if (!acceptVariables) {
			throw notSupported(ctx);
		}

		return VariableTerm.getInstance(ctx.variable().getText());
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

		List<Term> outputTerms = visitTerms(ctx.output);

		return new ExternalAtom(
			Predicate.getInstance(predicateName, outputTerms.size()),
			interpretation,
			visitTerms(ctx.input),
			outputTerms
		);
	}

	@Override
	public Object visitDirective_heuristic(Directive_heuristicContext ctx) {
		// directive_heuristic : SHARP 'heuristic' heuristic_head_atom (heuristic_body)? DOT heuristic_weight_annotation?;
		final HeuristicDirectiveAtom head = visitHeuristic_head_atom(ctx.heuristic_head_atom());
		final HeuristicDirectiveBody body = visitHeuristic_body(ctx.heuristic_body());
		final WeightAtLevel weightAtLevel = visitHeuristic_weight_annotation(ctx.heuristic_weight_annotation());
		inputProgram.getInlineDirectives().addDirective(DIRECTIVE.heuristic, new HeuristicDirective(head, body, weightAtLevel));
		return null;
	}

	@Override
	public HeuristicDirectiveAtom visitHeuristic_head_atom(ASPCore2Parser.Heuristic_head_atomContext ctx) {
		// heuristic_head_atom : (heuristic_head_sign)? basic_atom;
		final ThriceTruth sign = visitHeuristic_head_sign(ctx.heuristic_head_sign());
		final BasicAtom atom = visitBasic_atom(ctx.basic_atom());
		return HeuristicDirectiveAtom.head(sign, atom);
	}

	@Override
	public ThriceTruth visitHeuristic_head_sign(ASPCore2Parser.Heuristic_head_signContext ctx) {
		// heuristic_head_sign : HEU_SIGN_T | HEU_SIGN_F;
		if (ctx != null) {
			if (ctx.HEU_SIGN_T() != null) {
				return ThriceTruth.TRUE;
			} else if (ctx.HEU_SIGN_F() != null) {
				return ThriceTruth.FALSE;
			}
		}
		return null;
	}

	@Override
	public HeuristicDirectiveBody visitHeuristic_body(ASPCore2Parser.Heuristic_bodyContext ctx) {
		// heuristic_body : COLON heuristic_body_literal (COMMA heuristic_body_literal)*;
		final List<HeuristicDirectiveLiteral> bodyLiterals = new ArrayList<>();
		if (ctx != null) {
			for (ASPCore2Parser.Heuristic_body_literalContext literalContext : ctx.heuristic_body_literal()) {
				bodyLiterals.add(visitHeuristic_body_literal(literalContext));
			}
		}
		return new HeuristicDirectiveBody(bodyLiterals);
	}

	@Override
	public HeuristicDirectiveLiteral visitHeuristic_body_literal(ASPCore2Parser.Heuristic_body_literalContext ctx) {
		// heuristic_body_literal : NAF? heuristic_body_atom;
		return new HeuristicDirectiveLiteral(visitHeuristic_body_atom(ctx.heuristic_body_atom()), ctx.NAF() == null);
	}

	@Override
	public HeuristicDirectiveAtom visitHeuristic_body_atom(ASPCore2Parser.Heuristic_body_atomContext ctx) {
		// heuristic_body_atom : (heuristic_body_sign? basic_atom) | builtin_atom | external_atom;
		final Set<ThriceTruth> heuristicSigns = visitHeuristic_body_sign(ctx.heuristic_body_sign());
		Atom atom;
		if (ctx.basic_atom() != null) {
			atom = visitBasic_atom(ctx.basic_atom());
		} else if (ctx.builtin_atom() != null) {
			atom = visitBuiltin_atom(ctx.builtin_atom());
		} else if (ctx.external_atom() != null) {
			atom = visitExternal_atom(ctx.external_atom());
		} else {
			throw oops("No known atom found in heuristic body atom");
		}
		return HeuristicDirectiveAtom.body(heuristicSigns, atom);
	}

	@Override
	public Set<ThriceTruth> visitHeuristic_body_sign(ASPCore2Parser.Heuristic_body_signContext ctx) {
		if (ctx == null) {
			return null;
		}
		final Set<ThriceTruth> heuristicSigns = new HashSet<>();
		if (!ctx.HEU_SIGN_T().isEmpty()) {
			heuristicSigns.add(ThriceTruth.TRUE);
		}
		if (!ctx.HEU_SIGN_F().isEmpty()) {
			heuristicSigns.add(ThriceTruth.FALSE);
		}
		if (!ctx.HEU_SIGN_M().isEmpty()) {
			heuristicSigns.add(ThriceTruth.MBT);
		}
		if (!ctx.HEU_BODY_SIGN().isEmpty()) {
			for (TerminalNode compoundBodySign : ctx.HEU_BODY_SIGN()) {
				for (char signChar : compoundBodySign.getText().toCharArray()) {
					heuristicSigns.add(ThriceTruth.fromChar(signChar));
				}
			}
		}
		return heuristicSigns;
	}

	@Override
	public WeightAtLevel visitHeuristic_weight_annotation(ASPCore2Parser.Heuristic_weight_annotationContext ctx) {
		// heuristic_weight_annotation : SQUARE_OPEN heuristic_weight_at_level SQUARE_CLOSE;
		if (ctx == null) {
			return new WeightAtLevel(null, null);
		}
		return visitHeuristic_weight_at_level(ctx.heuristic_weight_at_level());
	}

	@Override
	public WeightAtLevel visitHeuristic_weight_at_level(ASPCore2Parser.Heuristic_weight_at_levelContext ctx) {
		// heuristic_weight_at_level : term (AT term)?;
		Term weight;
		Term level = null;
		weight = (Term) visit(ctx.term(0));
		if (ctx.AT() != null) {
			level = (Term) visit(ctx.term(1));
		}

		return new WeightAtLevel(weight, level);
	}

	public IntervalTerm visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx) {
		//interval : (lowerNum=NUMBER | lowerVar=variable) DOT DOT (upperNum=NUMBER | upperVar=variable);
		ASPCore2Parser.IntervalContext ictx = ctx.interval();
		Term lower = ictx.lowerNum != null ? ConstantTerm.getInstance(Integer.parseInt(ictx.lowerNum.getText())) : VariableTerm.getInstance(ictx.lowerVar.getText());
		Term upper = ictx.upperNum != null ? ConstantTerm.getInstance(Integer.parseInt(ictx.upperNum.getText())) : VariableTerm.getInstance(ictx.upperVar.getText());
		return IntervalTerm.getInstance(lower, upper);
	}

	@Override
	public Object visitTerm_minusArithTerm(ASPCore2Parser.Term_minusArithTermContext ctx) {
		// | MINUS term
		return ArithmeticTerm.MinusTerm.getInstance((Term)visit(ctx.term()));
	}

	@Override
	public Object visitTerm_timesdivmodArithTerm(ASPCore2Parser.Term_timesdivmodArithTermContext ctx) {
		// | term (TIMES | DIV | MODULO) term
		ArithmeticTerm.ArithmeticOperator op = ctx.TIMES() != null ? ArithmeticTerm.ArithmeticOperator.TIMES
			: ctx.DIV() != null ? ArithmeticTerm.ArithmeticOperator.DIV
			: ArithmeticTerm.ArithmeticOperator.MODULO;
		return ArithmeticTerm.getInstance((Term)visit(ctx.term(0)), op, (Term)visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_plusminusArithTerm(ASPCore2Parser.Term_plusminusArithTermContext ctx) {
		// | term (PLUS | MINUS) term
		ArithmeticTerm.ArithmeticOperator op = ctx.PLUS() != null ? ArithmeticTerm.ArithmeticOperator.PLUS : ArithmeticTerm.ArithmeticOperator.MINUS;
		return ArithmeticTerm.getInstance((Term)visit(ctx.term(0)), op, (Term)visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_powerArithTerm(ASPCore2Parser.Term_powerArithTermContext ctx) {
		// |<assoc=right> term POWER term
		ArithmeticTerm.ArithmeticOperator op = ArithmeticTerm.ArithmeticOperator.POWER;
		return ArithmeticTerm.getInstance((Term)visit(ctx.term(0)), op, (Term)visit(ctx.term(1)));
	}

	@Override
	public Object visitTerm_bitxorArithTerm(ASPCore2Parser.Term_bitxorArithTermContext ctx) {
		// | term BITXOR term
		return ArithmeticTerm.getInstance((Term)visit(ctx.term(0)), ArithmeticTerm.ArithmeticOperator.BITXOR, (Term)visit(ctx.term(1)));
	}
}
