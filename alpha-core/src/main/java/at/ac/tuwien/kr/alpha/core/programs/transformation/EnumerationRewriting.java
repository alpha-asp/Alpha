package at.ac.tuwien.kr.alpha.core.programs.transformation;

import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import at.ac.tuwien.kr.alpha.core.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.parser.InlineDirectivesImpl;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;
import at.ac.tuwien.kr.alpha.core.rules.heads.NormalHeadImpl;

/**
 * Rewrites the ordinary atom whose name is given in the input program by the enumeration directive #enum_atom_is into
 * the special EnumerationAtom.
 *
 * Copyright (c) 2017-2020, the Alpha Team.
 */
public class EnumerationRewriting extends ProgramTransformation<InputProgramImpl, InputProgramImpl> {

	@Override
	public InputProgramImpl apply(InputProgramImpl inputProgram) {
		// Read enumeration predicate from directive.
		String enumDirective = inputProgram.getInlineDirectives().getDirectiveValue(InlineDirectivesImpl.DIRECTIVE.enum_predicate_is);
		if (enumDirective == null) {
			// Directive not set, nothing to rewrite.
			return inputProgram;
		}
		CorePredicate enumPredicate = CorePredicate.getInstance(enumDirective, 3);

		InputProgramImpl.Builder programBuilder = InputProgramImpl.builder().addInlineDirectives(inputProgram.getInlineDirectives());

		checkFactsAreEnumerationFree(inputProgram.getFacts(), enumPredicate);
		programBuilder.addFacts(inputProgram.getFacts());

		List<BasicRule> srcRules = new ArrayList<>(inputProgram.getRules());
		programBuilder.addRules(rewriteRules(srcRules, enumPredicate));
		return programBuilder.build();
	}

	private void checkFactsAreEnumerationFree(List<CoreAtom> srcFacts, CorePredicate enumPredicate) {
		for (CoreAtom fact : srcFacts) {
			if (fact.getPredicate().equals(enumPredicate)) {
				throw oops("Atom declared as enumeration atom by directive occurs in a fact: " + fact);
			}
		}
	}

	private List<BasicRule> rewriteRules(List<BasicRule> srcRules, CorePredicate enumPredicate) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : srcRules) {
			if (rule.getHead() != null && !(rule.getHead() instanceof NormalHeadImpl)) {
				throw oops("Encountered rule whose head is not normal: " + rule);
			}
			if (rule.getHead() != null && ((NormalHeadImpl) rule.getHead()).getAtom().getPredicate().equals(enumPredicate)) {
				throw oops("Atom declared as enumeration atom by directive occurs in head of the rule: " + rule);
			}
			List<CoreLiteral> modifiedBodyLiterals = new ArrayList<>(rule.getBody());
			Iterator<CoreLiteral> rit = modifiedBodyLiterals.iterator();
			LinkedList<CoreLiteral> rewrittenLiterals = new LinkedList<>();
			while (rit.hasNext()) {
				CoreLiteral literal = rit.next();
				if (!(literal instanceof BasicLiteral)) {
					continue;
				}
				BasicLiteral basicLiteral = (BasicLiteral) literal;
				if (!basicLiteral.getPredicate().equals(enumPredicate)) {
					continue;
				}
				rit.remove();
				rewrittenLiterals.add(new EnumerationAtom(basicLiteral.getAtom().getTerms()).toLiteral());
			}
			modifiedBodyLiterals.addAll(rewrittenLiterals);
			rewrittenRules.add(new BasicRule(rule.getHead(), modifiedBodyLiterals));
		}
		return rewrittenRules;
	}

}
