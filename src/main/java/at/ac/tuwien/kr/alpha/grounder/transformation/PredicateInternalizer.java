package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.head.Head;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.impl.BasicRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Rewrites all predicates of a given Program such that they are internal and hence hidden from answer sets.
 * Copyright (c) 2018, the Alpha Team.
 */
public class PredicateInternalizer {

	static Program makePredicatesInternal(Program program) {
		Program internalizedProgram = new Program();
		for (Atom atom : program.getFacts()) {
			internalizedProgram.getFacts().add(makePredicateInternal(atom));

		}
		for (BasicRule rule : program.getRules()) {
			internalizedProgram.getRules().add(makePredicateInternal(rule));
		}
		internalizedProgram.getInlineDirectives().accumulate(program.getInlineDirectives());
		return internalizedProgram;
	}

	private static BasicRule makePredicateInternal(BasicRule rule) {
		Head newHead = null;
		if (rule.getHead() != null) {
			if (!rule.getHead().isNormal()) {
				throw new UnsupportedOperationException("Cannot make predicates in rules internal whose head is not normal.");
			}
			newHead = new DisjunctiveHead(Collections.singletonList(
				makePredicateInternal(((DisjunctiveHead)rule.getHead()).disjunctiveAtoms.get(0))));
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
		return new BasicRule(newHead, newBody);
	}

	private static Atom makePredicateInternal(Atom atom) {
		Predicate newInternalPredicate = Predicate.getInstance(atom.getPredicate().getName(),
			atom.getPredicate().getArity(), true);
		return new BasicAtom(newInternalPredicate, atom.getTerms());
	}
}
