package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.antlr.ASPCore2TermBaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2TermParser;
import at.ac.tuwien.kr.alpha.common.Symbol;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.antlr.v4.runtime.RuleContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class TermParseTreeVisitor extends ASPCore2TermBaseVisitor<Object> {
	private void notSupportedSyntax(RuleContext ctx) {
		throw new UnsupportedOperationException("Unsupported syntax encountered: " + ctx.getText());
	}

	@Override
	public List<Term> visitTerms(ASPCore2TermParser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<Term> terms = new ArrayList<>();
		do  {
			ASPCore2TermParser.TermContext term = ctx.term();
			terms.add((Term) visit(term));
		} while ((ctx = ctx.terms()) != null);

		return terms;
	}

	@Override
	public ConstantTerm visitTerm_number(ASPCore2TermParser.Term_numberContext ctx) {
		return ConstantTerm.getInstance(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public ConstantTerm visitTerm_const(ASPCore2TermParser.Term_constContext ctx) {
		return ConstantTerm.getInstance(Symbol.getInstance(ctx.ID().getText()));
	}

	@Override
	public ConstantTerm visitTerm_string(ASPCore2TermParser.Term_stringContext ctx) {
		return ConstantTerm.getInstance(ctx.STRING().getText());
	}

	@Override
	public FunctionTerm visitTerm_func(ASPCore2TermParser.Term_funcContext ctx) {
		return FunctionTerm.getInstance(ctx.ID().getText(), visitTerms(ctx.terms()));
	}

	@Override
	public VariableTerm visitTerm_anonymousVariable(ASPCore2TermParser.Term_anonymousVariableContext ctx) {
		return VariableTerm.getAnonymousInstance();
	}

	@Override
	public VariableTerm visitTerm_variable(ASPCore2TermParser.Term_variableContext ctx) {
		return VariableTerm.getInstance(ctx.VARIABLE().getText());
	}

	@Override
	public Term visitTerm_parenthesisedTerm(ASPCore2TermParser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}


	@Override
	public Object visitTerm_minusTerm(ASPCore2TermParser.Term_minusTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}

	@Override
	public Object visitTerm_binopTerm(ASPCore2TermParser.Term_binopTermContext ctx) {
		notSupportedSyntax(ctx);
		return null;
	}
}
