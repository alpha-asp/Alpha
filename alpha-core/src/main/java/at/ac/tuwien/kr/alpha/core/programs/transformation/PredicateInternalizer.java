package at.ac.tuwien.kr.alpha.core.programs.transformation;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;
import at.ac.tuwien.kr.alpha.core.rules.heads.Head;
import at.ac.tuwien.kr.alpha.core.rules.heads.NormalHeadImpl;

/**
 *
 * Rewrites all predicates of a given Program such that they are internal and hence hidden from answer sets.
 *
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class PredicateInternalizer {

	static InputProgramImpl makePredicatesInternal(InputProgramImpl program) {
		InputProgramImpl.Builder prgBuilder = InputProgramImpl.builder();
		for (CoreAtom atom : program.getFacts()) {
			prgBuilder.addFact(PredicateInternalizer.makePredicateInternal(atom));
		}
		for (BasicRule rule : program.getRules()) {
			prgBuilder.addRule(PredicateInternalizer.makePredicateInternal(rule));
		}
		prgBuilder.addInlineDirectives(program.getInlineDirectives());
		return prgBuilder.build();
	}

	private static BasicRule makePredicateInternal(BasicRule rule) {
		Head newHead = null;
		if (rule.getHead() != null) {
			if (!(rule.getHead() instanceof NormalHeadImpl)) {
				throw new UnsupportedOperationException("Cannot make predicates in rules internal whose head is not normal.");
			}
			newHead = new NormalHeadImpl(makePredicateInternal(((NormalHeadImpl) rule.getHead()).getAtom()));
		}
		List<CoreLiteral> newBody = new ArrayList<>();
		for (CoreLiteral bodyElement : rule.getBody()) {
			// Only rewrite BasicAtoms.
			if (bodyElement instanceof BasicLiteral) {
				newBody.add(makePredicateInternal(bodyElement.getAtom()).toLiteral());
			} else {
				// Keep other body element as is.
				newBody.add(bodyElement);
			}
		}
		return new BasicRule(newHead, newBody);
	}

	private static CoreAtom makePredicateInternal(CoreAtom atom) {
		CorePredicate newInternalPredicate = CorePredicate.getInstance(atom.getPredicate().getName(), atom.getPredicate().getArity(), true);
		return new BasicAtom(newInternalPredicate, atom.getTerms());
	}
}
