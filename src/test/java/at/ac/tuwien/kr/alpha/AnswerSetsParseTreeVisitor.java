package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.antlr.AnswerSetsBaseVisitor;
import at.ac.tuwien.kr.alpha.antlr.AnswerSetsParser;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.common.Symbol;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.*;

import static java.util.Collections.emptyList;

public class AnswerSetsParseTreeVisitor extends AnswerSetsBaseVisitor<Object> {
	@Override
	public Set<AnswerSet> visitAnswerSets(AnswerSetsParser.AnswerSetsContext ctx) {
		Set<AnswerSet> result = new TreeSet<>();

		for (AnswerSetsParser.AnswerSetContext answerSetContext : ctx.answerSet()) {
			result.add(visitAnswerSet(answerSetContext));
		}

		return result;
	}

	@Override
	public AnswerSet visitAnswerSet(AnswerSetsParser.AnswerSetContext ctx) {
		if (ctx.atoms() == null) {
			return BasicAnswerSet.EMPTY;
		}

		AnswerSetsParser.AtomsContext atomsContext = ctx.atoms();

		SortedSet<Predicate> predicates = new TreeSet<>();
		Map<Predicate, SortedSet<Atom>> predicateInstances = new TreeMap<>();

		do
		{
			final Atom atom = visitAtom(atomsContext.atom());
			predicates.add(atom.getPredicate());
			predicateInstances.compute(atom.getPredicate(), (k, v) -> {
				if (v == null) {
					v = new TreeSet<>();
				}
				v.add(atom);
				return v;
			});

		} while ((atomsContext = atomsContext.atoms()) != null);

		return new BasicAnswerSet(predicates, predicateInstances);
	}

	@Override
	public Atom visitAtom(AnswerSetsParser.AtomContext ctx) {
		final List<Term> terms = visitTerms(ctx.terms());
		return new BasicAtom(new BasicPredicate(ctx.ID().getText(), terms.size()), terms, false);
	}

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
