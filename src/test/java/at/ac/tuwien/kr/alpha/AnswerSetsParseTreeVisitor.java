package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.antlr.AnswerSetsBaseVisitor;
import at.ac.tuwien.kr.alpha.common.Symbol;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class AnswerSetsParseTreeVisitor extends AnswerSetsBaseVisitor<Object> {
	@Override
	public List<Term> visitTerms(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.TermsContext ctx) {
		// terms : term (COMMA terms)?;
		if (ctx == null) {
			return emptyList();
		}

		final List<Term> terms = new ArrayList<>();
		do  {
			at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.TermContext term = ctx.term();
			terms.add((Term) visit(term));
		} while ((ctx = ctx.terms()) != null);

		return terms;
	}

	@Override
	public ConstantTerm visitTerm_number(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.Term_numberContext ctx) {
		return ConstantTerm.getInstance(Integer.parseInt(ctx.NUMBER().getText()));
	}

	@Override
	public ConstantTerm visitTerm_const(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.Term_constContext ctx) {
		return ConstantTerm.getInstance(Symbol.getInstance(ctx.ID().getText()));
	}

	@Override
	public ConstantTerm visitTerm_string(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.Term_stringContext ctx) {
		return ConstantTerm.getInstance(ctx.STRING().getText());
	}

	@Override
	public FunctionTerm visitTerm_func(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.Term_funcContext ctx) {
		return FunctionTerm.getInstance(ctx.ID().getText(), visitTerms(ctx.terms()));
	}

	@Override
	public Term visitTerm_parenthesisedTerm(at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser.Term_parenthesisedTermContext ctx) {
		return (Term) visit(ctx.term());
	}
}
