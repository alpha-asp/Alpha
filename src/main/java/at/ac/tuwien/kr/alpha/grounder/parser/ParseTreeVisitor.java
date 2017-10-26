package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.predicates.FixedInterpretationPredicate;
import at.ac.tuwien.kr.alpha.common.terms.*;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

import static java.util.Collections.emptyList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {
	private final Map<String, FixedInterpretationPredicate> externals;
	private final boolean acceptVariables;

	private Program inputProgram;
	private boolean isCurrentLiteralNegated;

	public ParseTreeVisitor(Map<String, FixedInterpretationPredicate> externals) {
		this(externals, true);
	}

	public ParseTreeVisitor(Map<String, FixedInterpretationPredicate> externals, boolean acceptVariables) {
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
			Literal literal = visitClassical_literal(classicalLiteralContext);

			if (literal.isNegated()) {
				throw notSupported(classicalLiteralContext);
			}

			predicates.add(literal.getPredicate());
			predicateInstances.compute(literal.getPredicate(), (k, v) -> {
				if (v == null) {
					v = new TreeSet<>();
				}
				v.add(literal);
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
		inputProgram.getFacts().add(visitHead(ctx.head()));
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
	public Atom visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		// disjunction : classical_literal (OR disjunction)?;
		if (ctx.disjunction() != null) {
			throw notSupported(ctx);
		}
		isCurrentLiteralNegated = false;
		return visitClassical_literal(ctx.classical_literal());
	}

	@Override
	public Atom visitHead(ASPCore2Parser.HeadContext ctx) {
		// head : disjunction | choice;
		if (ctx.choice() != null) {
			throw notSupported(ctx);
		}
		return visitDisjunction(ctx.disjunction());
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
	public Literal visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		BinaryOperator op;

		if (ctx.binop().EQUAL() != null) {
			op = BinaryOperator.EQ;
		} else if (ctx.binop().UNEQUAL() != null) {
			op = BinaryOperator.NE;
		} else if (ctx.binop().LESS() != null) {
			op = BinaryOperator.LT;
		} else if (ctx.binop().LESS_OR_EQ() != null) {
			op = BinaryOperator.LE;
		} else if (ctx.binop().GREATER() != null) {
			op = BinaryOperator.GT;
		} else if (ctx.binop().GREATER_OR_EQ() != null) {
			op = BinaryOperator.GE;
		} else {
			throw notSupported(ctx.binop());
		}

		if (isCurrentLiteralNegated) {
			op = op.getNegation();
		}

		return new ExternalAtom(op.toPredicate(), Arrays.asList(
			(Term) visit(ctx.term(0)),
			(Term) visit(ctx.term(1))
		), Collections.emptyList(), isCurrentLiteralNegated);
	}

	@Override
	public Literal visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (external_atom | classical_literal | builtin_atom);
		isCurrentLiteralNegated = ctx.NAF() != null;
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
	public Literal visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;
		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

		final List<Term> terms = visitTerms(ctx.terms());
		return new BasicAtom(new Predicate(ctx.ID().getText(), terms.size()), terms, isCurrentLiteralNegated);
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
		return ConstantTerm.getInstance(Symbol.getInstance(ctx.ID().getText()));
	}

	@Override
	public ConstantTerm visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		return ConstantTerm.getInstance(ctx.STRING().getText());
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
	public Literal visitExternal_atom(ASPCore2Parser.External_atomContext ctx) {
		// external_atom : AMPERSAND ID (SQUARE_OPEN input = terms SQUARE_CLOSE)? (PAREN_OPEN output = terms PAREN_CLOSE)?;

		if (ctx.MINUS() != null) {
			throw notSupported(ctx);
		}

		List<Term> outputTerms = visitTerms(ctx.output);
		List<VariableTerm> output = new ArrayList<>(outputTerms.size());

		for (Term t : outputTerms) {
			if (!(t instanceof VariableTerm)) {
				throw notSupported(ctx.output);
			}
			output.add((VariableTerm) t);
		}

		FixedInterpretationPredicate predicate = externals.get(ctx.ID().getText());

		// TODO: Throw if predicate is null.

		return new ExternalAtom(predicate, visitTerms(ctx.input), output, isCurrentLiteralNegated);
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
	public Object visitTerm_binopTerm(ASPCore2Parser.Term_binopTermContext ctx) {
		throw notSupported(ctx);
	}
}
