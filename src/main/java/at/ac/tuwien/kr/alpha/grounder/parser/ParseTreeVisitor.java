package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BuiltinAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {

	private void notSupportedSyntax(RuleContext ctx) {
		throw new UnsupportedOperationException("Unsupported syntax encountered: " + ctx.getText());
	}

	@Override
	public String visitTerminal(TerminalNode node) {
		return node.getText();
	}

	@Override
	protected Object defaultResult() {
		return null;
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
			notSupportedSyntax(ctx.query());
		}

		if (ctx.statements() == null) {
			return Program.EMPTY;
		}
		inputProgram = new Program(new ArrayList<>(), new ArrayList<>());
		visitStatements(ctx.statements());
		return inputProgram;
	}

	private Program inputProgram;

	@Override
	public Object visitStatements(ASPCore2Parser.StatementsContext ctx) {
		// statements : statement+;
		for (ASPCore2Parser.StatementContext statementContext : ctx.statement()) {
			visit(statementContext);
		}
		return null;
	}

	@Override
	public Object visitStatement_constraint(ASPCore2Parser.Statement_constraintContext ctx) {
		// CONS body DOT
		inputProgram.getRules().add(new Rule(visitBody(ctx.body()), null));
		return null;
	}


	@Override
	public Object visitStatement_rule(ASPCore2Parser.Statement_ruleContext ctx) {
		// head (CONS body?)? DOT
		if (ctx.body() == null) {
			// fact
			inputProgram.getFacts().add(visitHead(ctx.head()));
		} else {
			// rule
			inputProgram.getRules().add(new Rule(visitBody(ctx.body()), visitHead(ctx.head())));
		}
		return null;
	}

	@Override
	public Atom visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		//disjunction : classical_literal (OR disjunction)?;
		if (ctx.disjunction() != null) {
			notSupportedSyntax(ctx);
		}
		return visitClassical_literal(ctx.classical_literal());
	}

	@Override
	public Atom visitHead(ASPCore2Parser.HeadContext ctx) {
		// head : disjunction | choice;
		if (ctx.choice() != null) {
			notSupportedSyntax(ctx);
		}
		return visitDisjunction(ctx.disjunction());
	}

	@Override
	public Object visitStatement_weightConstraint(ASPCore2Parser.Statement_weightConstraintContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public Object visitStatement_gringoSharp(ASPCore2Parser.Statement_gringoSharpContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public List<Literal> visitBody(ASPCore2Parser.BodyContext ctx) {
		// body : ( naf_literal | NAF? aggregate ) (COMMA body)?;
		if (ctx.naf_literal() == null) {
			notSupportedSyntax(ctx.aggregate());
		}

		List<Literal> bodyLiterals;
		if (ctx.body() != null) {
			bodyLiterals = visitBody(ctx.body());
		} else {
			bodyLiterals = new LinkedList<>();
		}

		bodyLiterals.add(0, visitNaf_literal(ctx.naf_literal()));

		return bodyLiterals;
	}

	@Override
	public Literal visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		Term left = (Term) visit(ctx.term(0));
		Term right = (Term) visit(ctx.term(1));
		List<Term> termList = new ArrayList<>(2);
		termList.add(left);
		termList.add(right);
		BuiltinAtom.BINOP binop;
		ASPCore2Parser.BinopContext parsedBinop = ctx.binop();
		// binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;
		if (parsedBinop.EQUAL() != null) {
			binop = BuiltinAtom.BINOP.EQ;
		} else if (parsedBinop.UNEQUAL() != null) {
			binop = BuiltinAtom.BINOP.NE;
		} else if (parsedBinop.LESS() != null) {
			binop = BuiltinAtom.BINOP.LT;
		} else if (parsedBinop.GREATER() != null) {
			binop = BuiltinAtom.BINOP.GT;
		} else if (parsedBinop.LESS_OR_EQ() != null) {
			binop = BuiltinAtom.BINOP.LE;
		} else if (parsedBinop.GREATER_OR_EQ() != null) {
			binop = BuiltinAtom.BINOP.GE;
		} else {
			throw new RuntimeException("Unknown binop encountered.");
		}
		return new BuiltinAtom(binop, termList, isCurrentLiteralNegated);
	}

	private boolean isCurrentLiteralNegated;

	@Override
	public Literal visitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		// naf_literal : NAF? (classical_literal | builtin_atom);
		isCurrentLiteralNegated = ctx.NAF() != null;
		if (ctx.builtin_atom() != null) {
			return visitBuiltin_atom(ctx.builtin_atom());
		} else {
			return visitClassical_literal(ctx.classical_literal());
		}
	}

	@Override
	public Literal visitClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		// classical_literal : MINUS? ID (PAREN_OPEN terms PAREN_CLOSE)?;
		if (ctx.MINUS() != null) {
			notSupportedSyntax(ctx);
		}

		List<Term> termList;
		if (ctx.terms() != null) {
			termList = visitTerms(ctx.terms());
		} else {
			termList = Collections.emptyList();
		}
		return new BasicAtom(new BasicPredicate(ctx.ID().getText(), termList.size()), termList, isCurrentLiteralNegated);
	}

	@Override
	public List<Term> visitTerms(ASPCore2Parser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		List<Term> termList;
		if (ctx.terms() != null) {
			termList = visitTerms(ctx.terms());
		} else {
			termList = new LinkedList<>();
		}

		termList.add(0, (Term)visit(ctx.term()));
		return termList;
	}

	@Override
	public ConstantTerm visitTerm_number(ASPCore2Parser.Term_numberContext ctx) {
		return ConstantTerm.getInstance(ctx.NUMBER().getText());
	}

	@Override
	public Term visitTerm_constOrFunc(ASPCore2Parser.Term_constOrFuncContext ctx) {
		// ID (PAREN_OPEN terms? PAREN_CLOSE)?
		if (ctx.PAREN_OPEN() == null) {
			// constant
			return ConstantTerm.getInstance(ctx.ID().getText());
		}
		// function term
		return FunctionTerm.getInstance(ctx.ID().getText(), visitTerms(ctx.terms()));
	}

	@Override
	public VariableTerm visitTerm_anonymousVariable(ASPCore2Parser.Term_anonymousVariableContext ctx) {
		return VariableTerm.getAnonymousInstance();
	}

	@Override
	public VariableTerm visitTerm_variable(ASPCore2Parser.Term_variableContext ctx) {
		return VariableTerm.getInstance(ctx.VARIABLE().getText());
	}

	@Override
	public Object visitTerm_minusTerm(ASPCore2Parser.Term_minusTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public Object visitTerm_binopTerm(ASPCore2Parser.Term_binopTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public Object visitTerm_gringoRange(ASPCore2Parser.Term_gringoRangeContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public ConstantTerm visitTerm_string(ASPCore2Parser.Term_stringContext ctx) {
		return ConstantTerm.getInstance(ctx.STRING().getText());
	}

	@Override
	public Term visitTerm_parenthesisedTerm(ASPCore2Parser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}
}
