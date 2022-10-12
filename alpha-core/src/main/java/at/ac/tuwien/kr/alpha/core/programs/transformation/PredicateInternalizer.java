package at.ac.tuwien.kr.alpha.core.programs.transformation;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.BasicLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.programs.InputProgram;

/**
 *
 * Rewrites all predicates of a given Program such that they are internal and hence hidden from answer sets.
 *
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class PredicateInternalizer {

	public static ASPCore2Program makePrefixedPredicatesInternal(ASPCore2Program program, String prefix) {
		InputProgram.Builder prgBuilder = InputProgram.builder();
		for (Atom atom : program.getFacts()) {
			if (atom.getPredicate().getName().startsWith(prefix)) {
				prgBuilder.addFact(PredicateInternalizer.makePredicateInternal((BasicAtom) atom));
			} else {
				prgBuilder.addFact(atom);
			}
		}
		for (Rule<Head> rule : program.getRules()) {
			prgBuilder.addRule(PredicateInternalizer.makePrefixedPredicatesInternal(rule, prefix));
		}
		prgBuilder.addInlineDirectives(program.getInlineDirectives());
		return prgBuilder.build();
	}

	public static Rule<Head> makePrefixedPredicatesInternal(Rule<Head> rule, String prefix) {
		Head newHead = null;
		if (rule.getHead() != null) {
			if (!(rule.getHead() instanceof NormalHead)) {
				throw new UnsupportedOperationException("Cannot make predicates in rules internal whose head is not normal.");
			}
			NormalHead head = (NormalHead) rule.getHead();
			if (head.getAtom().getPredicate().getName().startsWith(prefix)) {
				newHead = Heads.newNormalHead(makePredicateInternal(head.getAtom()));
			} else {
				newHead = head;
			}
		}
		List<Literal> newBody = new ArrayList<>();
		for (Literal bodyElement : rule.getBody()) {
			// Only rewrite BasicAtoms.
			if (bodyElement instanceof BasicLiteral) {
				if (bodyElement.getAtom().getPredicate().getName().startsWith(prefix)) {
					newBody.add(makePredicateInternal((BasicAtom) bodyElement.getAtom()).toLiteral(!bodyElement.isNegated()));
				} else {
					newBody.add(bodyElement);
				}
			} else {
				// Keep other body element as is.
				newBody.add(bodyElement);
			}
		}
		return Rules.newRule(newHead, newBody);
	}

	private static BasicAtom makePredicateInternal(BasicAtom atom) {
		Predicate newInternalPredicate = Predicates.getPredicate(atom.getPredicate().getName(), atom.getPredicate().getArity(), true);
		return Atoms.newBasicAtom(newInternalPredicate, atom.getTerms());
	}
}
