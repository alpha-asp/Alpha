package at.ac.tuwien.kr.alpha.core.programs.transformation;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.BasicLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.heads.NormalHeadImpl;
import at.ac.tuwien.kr.alpha.core.programs.ASPCore2ProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.LegacyBasicRule;

/**
 *
 * Rewrites all predicates of a given Program such that they are internal and hence hidden from answer sets.
 *
 * Copyright (c) 2018-2020, the Alpha Team.
 */
// TODO make this type-safe using visitor
public class PredicateInternalizer {

	static ASPCore2Program makePredicatesInternal(ASPCore2Program program) {
		ASPCore2ProgramImpl.Builder prgBuilder = ASPCore2ProgramImpl.builder();
		for (Atom atom : program.getFacts()) {
			prgBuilder.addFact(PredicateInternalizer.makePredicateInternal(atom));
		}
		for (Rule<Head> rule : program.getRules()) {
			prgBuilder.addRule(PredicateInternalizer.makePredicateInternal(rule));
		}
		prgBuilder.addInlineDirectives(program.getInlineDirectives());
		return prgBuilder.build();
	}

	private static Rule<Head> makePredicateInternal(Rule<Head> rule) {
		Head newHead = null;
		if (rule.getHead() != null) {
			if (!(rule.getHead() instanceof NormalHeadImpl)) {
				throw new UnsupportedOperationException("Cannot make predicates in rules internal whose head is not normal.");
			}
			newHead = new NormalHeadImpl(makePredicateInternal(((NormalHead) rule.getHead()).getAtom()));
		}
		List<Literal> newBody = new ArrayList<>();
		for (Literal bodyElement : rule.getBody()) {
			// Only rewrite BasicAtoms.
			if (bodyElement instanceof BasicLiteral) {
				newBody.add(makePredicateInternal(bodyElement.getAtom()).toLiteral());
			} else {
				// Keep other body element as is.
				newBody.add(bodyElement);
			}
		}
		return new LegacyBasicRule(newHead, newBody);
	}

	private static Atom makePredicateInternal(Atom atom) {
		Predicate newInternalPredicate = Predicates.getPredicate(atom.getPredicate().getName(), atom.getPredicate().getArity(), true);
		return Atoms.newBasicAtom(newInternalPredicate, atom.getTerms());
	}
}
