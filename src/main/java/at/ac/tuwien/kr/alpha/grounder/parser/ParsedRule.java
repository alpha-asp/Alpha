package at.ac.tuwien.kr.alpha.grounder.parser;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class ParsedRule extends CommonParsedObject {
	public final ParsedAtom head;
	public final List<ParsedAtom> body;

	public ParsedRule(List<ParsedAtom> body, ParsedAtom head) {
		this.head = head;
		this.body = body;
	}

	public ParsedRule(List<ParsedAtom> body) {
		this(body, null);
	}

	public NonGroundRule toNonGroundRule(Map<String, ExternalEvaluable> externals) {
		final List<Atom> pos = new ArrayList<>(this.body.size() / 2);
		final List<Atom> neg = new ArrayList<>(this.body.size() / 2);

		for (ParsedAtom bodyAtom : this.body) {
			(bodyAtom.isNegated() ? neg : pos).add(bodyAtom.toAtom(externals));
		}

		// Construct head if the given parsedRule is no constraint
		final Atom head = this.head != null ? this.head.toAtom(externals) : null;

		return new NonGroundRule(pos, neg, head);
	}

	@Override
	public boolean addTo(ParsedProgram program) {
		return program.addRule(this);
	}
}
