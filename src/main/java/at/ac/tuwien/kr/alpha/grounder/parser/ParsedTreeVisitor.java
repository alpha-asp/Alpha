package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedTreeVisitor extends ASPCore2BaseVisitor<CommonParsedObject> {

	private Object notSupportedSyntax(RuleContext ctx) {
		throw new UnsupportedOperationException("Unsupported syntax encountered: " + ctx.getText());
	}

	@Override
	public CommonParsedObject visitTerminal(TerminalNode node) {
		return new ParsedTerminal(node.getText());
	}

	@Override
	protected CommonParsedObject defaultResult() {
		return null;
	}

	@Override
	protected CommonParsedObject aggregateResult(CommonParsedObject aggregate, CommonParsedObject nextResult) {
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
	}

	@Override
	public CommonParsedObject visitProgram(ASPCore2Parser.ProgramContext ctx) {
		if (ctx.query() != null) {
			notSupportedSyntax(ctx.query());
		}

		if (ctx.statements() == null) {
			return ParsedProgram.EMPTY;
		}

		return new ParsedProgram((ListOfParsedObjects) visitStatements(ctx.statements()));
	}

	@Override
	public CommonParsedObject visitStatements(ASPCore2Parser.StatementsContext ctx) {
		// statements : statement+;
		return new ListOfParsedObjects(
			ctx.statement()
				.parallelStream()
				.map(this::visit)
				.collect(Collectors.toList())
		);
	}

	@Override
	public CommonParsedObject visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | NAF? aggregate ) (COMMA body)?;

		if (ctx.naf_literal() == null) {
			notSupportedSyntax(ctx.aggregate());
		}

		List<CommonParsedObject> body = new ArrayList<>();

		body.add(visitNaf_literal(ctx.naf_literal()));

		if (ctx.body() != null) {
			body.addAll((ListOfParsedObjects) visitBody(ctx.body()));
		}

		return new ListOfParsedObjects(body);
	}

	@Override
	public CommonParsedObject visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		final ListOfParsedObjects bodyList = (ListOfParsedObjects) visitBody(ctx.body());
		return new ParsedConstraint(bodyList.parallelStream().map(o -> (ParsedAtom)o).collect(Collectors.toList()));
	}

	@Override
	public CommonParsedObject visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		//disjunction : classical_literal (OR disjunction)?;
		List<CommonParsedObject> disjunctList = new ArrayList<>();
		disjunctList.add(visitClassical_literal(ctx.classical_literal()));

		if (ctx.disjunction() != null) {
			disjunctList.addAll((ListOfParsedObjects) visitDisjunction(ctx.disjunction()));
		}

		return new ListOfParsedObjects(disjunctList);
	}

	@Override
	public CommonParsedObject visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head (CONS body?)? DOT
		if (ctx.body() == null) {
			// fact
			return new ParsedFact((ParsedAtom) ((ListOfParsedObjects)visitHead(ctx.head())).get(0));
		}

		// rule
		List<ParsedAtom> bodyList = new ArrayList<>();
		for (CommonParsedObject atom : (ListOfParsedObjects) visitBody(ctx.body())) {
			bodyList.add((ParsedAtom) atom);
		}
		return new ParsedRule(bodyList, (ParsedAtom)((ListOfParsedObjects)visitHead(ctx.head())).get(0));
	}

	@Override
	public CommonParsedObject visitStatement_weightConstraint(ASPCore2Parser.Statement_weightConstraintContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public CommonParsedObject visitStatement_gringoSharp(ASPCore2Parser.Statement_gringoSharpContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public CommonParsedObject visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		ParsedTerm left = (ParsedTerm) visit(ctx.term(0));
		ParsedTerm right = (ParsedTerm) visit(ctx.term(1));
		ParsedBuiltinAtom.BINOP binop;
		ASPCore2Parser.BinopContext parsedBinop = ctx.binop();
		// binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;
		if (parsedBinop.EQUAL() != null) {
			binop = ParsedBuiltinAtom.BINOP.EQ;
		} else if (parsedBinop.UNEQUAL() != null) {
			binop = ParsedBuiltinAtom.BINOP.NE;
		} else if (parsedBinop.LESS() != null) {
			binop = ParsedBuiltinAtom.BINOP.LT;
		} else if (parsedBinop.GREATER() != null) {
			binop = ParsedBuiltinAtom.BINOP.GT;
		} else if (parsedBinop.LESS_OR_EQ() != null) {
			binop = ParsedBuiltinAtom.BINOP.LE;
		} else if (parsedBinop.GREATER_OR_EQ() != null) {
			binop = ParsedBuiltinAtom.BINOP.GE;
		} else {
			throw new RuntimeException("Unknown binop encountered.");
		}
		return new ParsedBuiltinAtom(left, binop, right);
	}

	@Override
	public CommonParsedObject visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (classical_literal | builtin_atom);
		boolean isNegated = ctx.NAF() != null;
		if (ctx.builtin_atom() != null) {
			ParsedBuiltinAtom builtinAtom = (ParsedBuiltinAtom) visitBuiltin_atom(ctx.builtin_atom());
			return isNegated ? builtinAtom.getNegation() : builtinAtom;
		}
		ParsedAtom atom = (ParsedAtom)visitClassical_literal(ctx.classical_literal());
		atom.isNegated = isNegated;
		return atom;
	}

	@Override
	public CommonParsedObject visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;
		if (ctx.MINUS() != null) {
			notSupportedSyntax(ctx);
		}


		List<ParsedTerm> terms = new ArrayList<>();
		if (ctx.terms() != null) {
			for (CommonParsedObject term : (ListOfParsedObjects) visitTerms(ctx.terms())) {
				terms.add((ParsedTerm) term);
			}
		}
		return new ParsedAtom(ctx.ID().getText(), terms);
	}

	@Override
	public CommonParsedObject visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		List<CommonParsedObject> terms = new ArrayList<>();
		terms.add(visit(ctx.term()));
		if (ctx.terms() != null) {
			terms.addAll((ListOfParsedObjects) visitTerms(ctx.terms()));
		}
		return new ListOfParsedObjects(terms);
	}

	@Override
	public CommonParsedObject visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		return new ParsedConstant(ctx.NUMBER().getText(), ParsedConstant.Type.NUMBER);
	}

	@Override
	public CommonParsedObject visitTerm_constOrFunc(ASPCore2Parser.Term_constOrFuncContext ctx) {
		// ID (PAREN_OPEN terms? PAREN_CLOSE)?
		if (ctx.PAREN_OPEN() == null) {
			// constant
			return new ParsedConstant(ctx.ID().getText(), ParsedConstant.Type.CONSTANT);
		}

		// function term
		final List<ParsedTerm> terms = new ArrayList<>();
		for (CommonParsedObject commonTerm : (ListOfParsedObjects)visitTerms(ctx.terms())) {
			terms.add((ParsedTerm)commonTerm);
		}
		return new ParsedFunctionTerm(ctx.ID().getText(), terms);
	}

	@Override
	public CommonParsedObject visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		return ParsedVariable.ANONYMOUS;
	}

	@Override
	public CommonParsedObject visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		return new ParsedVariable(ctx.VARIABLE().getText());
	}

	@Override
	public CommonParsedObject visitTerm_minusTerm(ASPCore2Parser.Term_minusTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public CommonParsedObject visitTerm_binopTerm(ASPCore2Parser.Term_binopTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public CommonParsedObject visitTerm_gringoRange(ASPCore2Parser.Term_gringoRangeContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public CommonParsedObject visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		return new ParsedConstant(ctx.STRING().getText(), ParsedConstant.Type.STRING);
	}

	@Override
	public CommonParsedObject visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return visit(ctx.term());
	}
}
