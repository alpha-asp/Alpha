package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedTreeVisitor extends ASPCore2BaseVisitor<CommonParsedObject> {

	private Object notSupportedSyntax(RuleContext ctx) {
		throw new UnsupportedOperationException("Unsupported syntax encountered: " + ctx.getText());
	}

	@Override
	public CommonParsedObject visitTerminal(TerminalNode node) {
		ParsedTerminal ret = new ParsedTerminal();
		ret.terminal = node.getText();
		return ret;
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
			((ListOfParsedObjects) aggregate).objects.add(nextResult);
		}  else {
			aggList = new ListOfParsedObjects();
		}
		if (aggregate != null) {	// default result is null, ignore it
			aggList.objects.add(aggregate);
		}
		if (nextResult instanceof ListOfParsedObjects) {
			aggList.objects.addAll(((ListOfParsedObjects) nextResult).objects);
		} else {
			aggList.objects.add(nextResult);
		}
		return aggList;
	}


	@Override
	public CommonParsedObject visitProgram(ASPCore2Parser.ProgramContext ctx) {
		if (ctx.query() != null) {
			notSupportedSyntax(ctx.query());
		}
		if (ctx.statements() == null) {
			throw new RuntimeException("Input program is empty.");
		}
		ParsedProgram program = new ParsedProgram();
		for (CommonParsedObject parsedObject :
			((ListOfParsedObjects)visitStatements(ctx.statements())).objects) {
			if (parsedObject instanceof ParsedFact) {
				program.facts.add((ParsedFact) parsedObject);
			} else if (parsedObject instanceof ParsedConstraint) {
				program.constraints.add((ParsedConstraint) parsedObject);
			} else if (parsedObject instanceof ParsedRule) {
				program.rules.add((ParsedRule) parsedObject);
			} else {
				throw new UnsupportedOperationException("Unknown parsed object encountered during program parsing: " + parsedObject);
			}
		}
		return program;
	}

	@Override
	public CommonParsedObject visitStatements(ASPCore2Parser.StatementsContext ctx) {
		// statements : statement+;
		ListOfParsedObjects statementList = new ListOfParsedObjects();
		for (ASPCore2Parser.StatementContext statementContext : ctx.statement()) {
			CommonParsedObject statement = visit(statementContext);
			statementList.objects.add(statement);
		}
		return statementList;
	}

	@Override
	public CommonParsedObject visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | NAF? aggregate ) (COMMA body)?;
		ListOfParsedObjects bodyList = new ListOfParsedObjects();
		if (ctx.naf_literal() != null) {
			ParsedAtom nafLiteral = (ParsedAtom) visitNaf_literal(ctx.naf_literal());
			bodyList.objects.add(nafLiteral);
		} else {
			notSupportedSyntax(ctx.aggregate());
		}
		if (ctx.body() != null) {
			bodyList.objects.addAll(((ListOfParsedObjects) visitBody(ctx.body())).objects);
		}
		return bodyList;
	}

	@Override
	public CommonParsedObject visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		ParsedConstraint cons = new ParsedConstraint();
		ListOfParsedObjects bodyList = (ListOfParsedObjects) visitBody(ctx.body());
		cons.body = new ArrayList<>();
		for (CommonParsedObject atom :
			bodyList.objects) {
			cons.body.add((ParsedAtom) atom);
		}
		return cons;
	}

	@Override
	public CommonParsedObject visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		//disjunction : classical_literal (OR disjunction)?;
		ListOfParsedObjects disjunctList = new ListOfParsedObjects();
		disjunctList.objects.add(visitClassical_literal(ctx.classical_literal()));
		if (ctx.disjunction() != null) {
			disjunctList.objects.addAll(((ListOfParsedObjects) visitDisjunction(ctx.disjunction())).objects);
		}
		return disjunctList;
	}

	@Override
	public CommonParsedObject visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head (CONS body?)? DOT
		if (ctx.body() == null) {
			// fact
			ParsedFact fact = new ParsedFact();
			fact.fact = (ParsedAtom) ((ListOfParsedObjects)visitHead(ctx.head())).objects.get(0);
			return fact;

		} else {
			// rule
			ParsedRule rule = new ParsedRule();
			rule.head = (ParsedAtom)((ListOfParsedObjects)visitHead(ctx.head())).objects.get(0);
			ListOfParsedObjects bodyList = (ListOfParsedObjects) visitBody(ctx.body());
			for (CommonParsedObject atom :
				bodyList.objects) {
				rule.body.add((ParsedAtom) atom);
			}
			return rule;
		}
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
	public CommonParsedObject visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		if (ctx.builtin_atom() != null) {
			notSupportedSyntax(ctx.builtin_atom());
		}
		boolean isNegated = ctx.NAF() != null;
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

		ParsedAtom atom = new ParsedAtom();
		atom.predicate = ctx.ID().getText();
		if (ctx.terms() != null) {
			atom.terms = new ArrayList<>();
			for (CommonParsedObject term:
				((ListOfParsedObjects) visitTerms(ctx.terms())).objects) {
				atom.terms.add((ParsedTerm) term);
			}
			atom.arity = atom.terms.size();
		} else {
			atom.arity = 0;
		}
		return atom;
	}

	@Override
	public CommonParsedObject visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		ListOfParsedObjects termlist = new ListOfParsedObjects();
		termlist.objects.add(visit(ctx.term()));
		if (ctx.terms() != null) {
			termlist.objects.addAll(((ListOfParsedObjects)visitTerms(ctx.terms())).objects);
		}
		return termlist;
	}

	@Override
	public CommonParsedObject visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		ParsedConstant ret = new ParsedConstant();
		ret.type = ParsedConstant.TYPE.NUMBER;
		ret.content = ctx.NUMBER().getText();
		return ret;
	}

	@Override
	public CommonParsedObject visitTerm_constOrFunc(ASPCore2Parser.Term_constOrFuncContext ctx) {
		// ID (PAREN_OPEN terms? PAREN_CLOSE)?
		if (ctx.PAREN_OPEN() != null) {
			// function term
			ParsedFunctionTerm funcTerm = new ParsedFunctionTerm();
			funcTerm.functionName = ctx.ID().getText();

			funcTerm.termList = new ArrayList<>();
			for (CommonParsedObject commonTerm :
				((ListOfParsedObjects)visitTerms(ctx.terms())).objects) {
				funcTerm.termList.add((ParsedTerm)commonTerm);
			}
			funcTerm.arity = funcTerm.termList.size();
			return funcTerm;

		} else {
			// constant
			ParsedConstant constant = new ParsedConstant();
			constant.type = ParsedConstant.TYPE.CONSTANT;
			constant.content = ctx.ID().getText();
			return constant;
		}
	}

	@Override
	public CommonParsedObject visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		ParsedVariable ret = new ParsedVariable();
		ret.isAnonymous = true;
		ret.variableName = null;
		return ret;
	}

	@Override
	public CommonParsedObject visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		ParsedVariable ret = new ParsedVariable();
		ret.variableName = ctx.VARIABLE().getText();
		return ret;
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
		ParsedConstant ret = new ParsedConstant();
		ret.type = ParsedConstant.TYPE.STRING;
		ret.content = ctx.STRING().getText();
		return ret;
	}

	@Override
	public CommonParsedObject visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return visit(ctx.term());
	}
}
