package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2BaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.*;
import at.ac.tuwien.kr.alpha.common.terms.*;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParseTreeVisitor extends ASPCore2BaseVisitor<Object> {

	private Program inputProgram;
	private boolean isCurrentLiteralNegated;

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
		inputProgram.getRules().add(new Rule(null, visitBody(ctx.body())));
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
			inputProgram.getRules().add(new Rule(visitHead(ctx.head()), visitBody(ctx.body())));
		}
		return null;
	}

	@Override
	public Atom visitDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		//disjunction : classical_literal (OR disjunction)?;
		if (ctx.disjunction() != null) {
			notSupportedSyntax(ctx);
		}
		isCurrentLiteralNegated = false;
		return visitClassical_literal(ctx.classical_literal());
	}

	@Override
	public Atom visitHead(ASPCore2Parser.HeadContext ctx) {
		// head : disjunction | choice;
		if (ctx.choice() != null) {
			return visitChoice(ctx.choice());
		}
		return visitDisjunction(ctx.disjunction());
	}

	@Override
	public ChoiceHead visitChoice(ASPCore2Parser.ChoiceContext ctx) {
		// choice : (lt=term lop=binop)? CURLY_OPEN choice_elements? CURLY_CLOSE (uop=binop ut=term)?;
		Term lt = null;
		BuiltinAtom.BINOP lop = null;
		Term ut = null;
		BuiltinAtom.BINOP uop = null;
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
	public List<AbstractMap.SimpleEntry<BasicAtom, List<Literal>>> visitChoice_elements(ASPCore2Parser.Choice_elementsContext ctx) {
		// choice_elements : choice_element (SEMICOLON choice_elements)?;
		List<AbstractMap.SimpleEntry<BasicAtom, List<Literal>>> choiceElements;
		if (ctx.choice_elements() != null) {
			choiceElements = visitChoice_elements(ctx.choice_elements());
		} else {
			choiceElements = new LinkedList<>();
		}
		choiceElements.add(0, visitChoice_element(ctx.choice_element()));
		return choiceElements;
	}

	@Override
	public AbstractMap.SimpleEntry<BasicAtom, List<Literal>> visitChoice_element(ASPCore2Parser.Choice_elementContext ctx) {
		// choice_element : classical_literal (COLON naf_literals?)?;
		BasicAtom atom = (BasicAtom) visitClassical_literal(ctx.classical_literal());
		if (ctx.naf_literals() != null) {
			return new AbstractMap.SimpleEntry<>(atom, visitNaf_literals(ctx.naf_literals()));
		} else {
			return new AbstractMap.SimpleEntry<>(atom, Collections.emptyList());
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
	public BuiltinAtom.BINOP visitBinop(ASPCore2Parser.BinopContext ctx) {
		// binop : EQUAL | UNEQUAL | LESS | GREATER | LESS_OR_EQ | GREATER_OR_EQ;
		if (ctx.EQUAL() != null) {
			return BuiltinAtom.BINOP.EQ;
		} else if (ctx.UNEQUAL() != null) {
			return BuiltinAtom.BINOP.NE;
		} else if (ctx.LESS() != null) {
			return BuiltinAtom.BINOP.LT;
		} else if (ctx.GREATER() != null) {
			return BuiltinAtom.BINOP.GT;
		} else if (ctx.LESS_OR_EQ() != null) {
			return BuiltinAtom.BINOP.LE;
		} else if (ctx.GREATER_OR_EQ() != null) {
			return BuiltinAtom.BINOP.GE;
		} else {
			throw new RuntimeException("Unknown binop encountered.");
		}
	}

	@Override
	public Literal visitBuiltin_atom(ASPCore2Parser.Builtin_atomContext ctx) {
		// builtin_atom : term binop term;
		Term left = (Term) visit(ctx.term(0));
		Term right = (Term) visit(ctx.term(1));
		List<Term> termList = new ArrayList<>(2);
		termList.add(left);
		termList.add(right);
		BuiltinAtom.BINOP binop = visitBinop(ctx.binop());
		return new BuiltinAtom(binop, termList, isCurrentLiteralNegated);
	}

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
	public IntervalTerm visitTerm_interval(ASPCore2Parser.Term_intervalContext ctx) {
		// interval : lower = (NUMBER | VARIABLE) DOT DOT upper = (NUMBER | VARIABLE);
		ASPCore2Parser.IntervalContext ictx = ctx.interval();
		String lowerText = ictx.lower.getText();
		String upperText = ictx.upper.getText();
		Term lower = ictx.lower.getType() == ASPCore2Lexer.NUMBER ? ConstantTerm.getInstance(lowerText) : VariableTerm.getInstance(lowerText);
		Term upper = ictx.upper.getType() == ASPCore2Lexer.NUMBER ? ConstantTerm.getInstance(upperText) : VariableTerm.getInstance(upperText);
		return IntervalTerm.getInstance(lower, upper);
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
