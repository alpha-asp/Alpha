package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

import java.util.Iterator;
import java.util.LinkedList;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Rewrites the ordinary atom whose name is given in the input program by the enumeration directive #enum_atom_is into
 * the special EnumerationAtom.
 * Copyright (c) 2017, the Alpha Team.
 */
public class EnumerationRewriting implements ProgramTransformation  {
	@Override
	public void transform(Program inputProgram) {
		// Read enumeration predicate from directive.
		String enumDirective = inputProgram.getInlineDirectives().getDirectiveValue(InlineDirectives.DIRECTIVE.enum_predicate_is);
		if (enumDirective == null) {
			// Directive not set, nothing to rewrite.
			return;
		}
		Predicate enumPredicate = Predicate.getInstance(enumDirective, 3);

		// Rewrite all enumeration atoms occurring in facts.
		Iterator<Atom> it = inputProgram.getFacts().iterator();
		LinkedList<Atom> rewrittenFacts = new LinkedList<>();
		while (it.hasNext()) {
			Atom atom = it.next();
			if (!atom.getPredicate().equals(enumPredicate)) {
				continue;
			}
			it.remove();
			rewrittenFacts.add(new BasicAtom(EnumerationAtom.ENUMERATION_PREDICATE, atom.getTerms()));
		}
		inputProgram.getFacts().addAll(rewrittenFacts);

		// Rewrite all enumeration atoms in rules.
		for (Rule rule : inputProgram.getRules()) {
			if (rule.getHead() != null && !rule.getHead().isNormal()) {
				throw oops("Encountered rule whose head is not normal: " + rule);
			}
			if (rule.getHead() != null && ((DisjunctiveHead)rule.getHead()).disjunctiveAtoms.get(0).getPredicate().equals(enumPredicate)) {
				throw oops("Atom declared as enumeration atom by directive occurs in head of the rule: " + rule);
			}
			Iterator<Literal> rit = rule.getBody().iterator();
			LinkedList<Literal> rewrittenLiterals = new LinkedList<>();
			while (rit.hasNext()) {
				Literal literal = rit.next();
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
			rule.getBody().addAll(rewrittenLiterals);
		}
	}
}
