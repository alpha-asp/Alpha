/**
 * Copyright (c) 2016-2018, the Alpha Team.
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
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.*;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.*;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Copyright (c) 2016-2018, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {
	private final Map<String, PredicateInterpretation> externals;
	private final boolean acceptVariables;

	private Program inputProgram;
	private boolean isCurrentLiteralNegated;

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

		inputProgram = new Program(new ArrayList<>(), new ArrayList<>());
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
	public Head visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		// disjunction : classical_literal (OR disjunction)?;
		if (ctx.disjunction() != null) {
			throw notSupported(ctx);
		}
		isCurrentLiteralNegated = false;
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
		BasicAtom atom = (BasicAtom) visitClassical_literal(ctx.classical_literal());
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
	public List<Literal> visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | NAF? aggregate ) (COMMA body)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<Literal> literals = new ArrayList<>();
		do {
			if (ctx.naf_literal() == null) {
				throw notSupported(ctx.aggregate());
			}

			literals.add(visitNaf_literal(ctx.naf_literal()));
		} while ((ctx = ctx.body()) != null);

		return literals;
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
	public Literal visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		return new ComparisonLiteral(
			(Term) visit(ctx.term(0)),
			(Term) visit(ctx.term(1)),
			isCurrentLiteralNegated,
			visitBinop(ctx.binop())
		);
	}

	@Override
	public Literal visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (external_atom | classical_literal | builtin_atom);
		isCurrentLiteralNegated = ctx.NAF() != null;
		if (ctx.builtin_atom() != null) {
			return visitBuiltin_atom(ctx.builtin_atom());
		} else if (ctx.classical_literal() != null) {
			return new Literal(visitClassical_literal(ctx.classical_literal()), isCurrentLiteralNegated);
		} else if (ctx.external_atom() != null) {
			return new Literal(visitExternal_atom(ctx.external_atom()), isCurrentLiteralNegated);
		}
		throw notSupported(ctx);
	}

	@Override
	public Atom visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;
		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

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
	public ConstantTerm visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		return ConstantTerm.getInstance(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public ConstantTerm visitTerm_const(ASPCore2Parser.Term_constContext ctx) {
		return ConstantTerm.getSymbolicInstance(ctx.ID().getText());
	}

	@Override
	public ConstantTerm visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
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

		return VariableTerm.getInstance(ctx.VARIABLE().getText());
	}

	@Override
	public Term visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}

	@Override
	public Atom visitExternal_atom(ASPCore2Parser.External_atomContext ctx) {
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
	public Object visitStatement_weightConstraint(ASPCore2Parser.Statement_weightConstraintContext ctx) {
		throw notSupported(ctx);
	}

	@Override
	public Object visitStatement_gringoSharp(ASPCore2Parser.Statement_gringoSharpContext ctx) {
		throw notSupported(ctx);
	}

	@Override
	public Object visitTerm_minusTerm(ASPCore2Parser.Term_minusTermContext ctx) {
		throw notSupported(ctx);
	}

	public IntervalTerm visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx) {
		// interval : lower = (NUMBER | VARIABLE) DOT DOT upper = (NUMBER | VARIABLE);
		ASPCore2Parser.IntervalContext ictx = ctx.interval();
		String lowerText = ictx.lower.getText();
		String upperText = ictx.upper.getText();
		Term lower = ictx.lower.getType() == ASPCore2Lexer.NUMBER ? ConstantTerm.getInstance(Integer.parseInt(lowerText)) : VariableTerm.getInstance(lowerText);
		Term upper = ictx.upper.getType() == ASPCore2Lexer.NUMBER ? ConstantTerm.getInstance(Integer.parseInt(upperText)) : VariableTerm.getInstance(upperText);
		return IntervalTerm.getInstance(lower, upper);
	}

	@Override
	public ArithmeticTerm visitTerm_binopTerm(ASPCore2Parser.Term_binopTermContext ctx) {
		// term arithop term
		return ArithmeticTerm.getInstance((Term)visit(ctx.term(0)), visitArithop(ctx.arithop()), (Term)visit(ctx.term(1)));
	}

	@Override
	public ArithmeticTerm.ArithmeticOperator visitArithop(ASPCore2Parser.ArithopContext ctx) {
		// arithop : PLUS | MINUS | TIMES | DIV;
		if (ctx.PLUS() != null) {
			return ArithmeticTerm.ArithmeticOperator.PLUS;
		} else if (ctx.MINUS() != null) {
			return ArithmeticTerm.ArithmeticOperator.MINUS;
		} else if (ctx.TIMES() != null) {
			return ArithmeticTerm.ArithmeticOperator.TIMES;
		} else if (ctx.DIV() != null) {
			return ArithmeticTerm.ArithmeticOperator.DIV;
		} else {
			throw notSupported(ctx);
		}
	}
}
