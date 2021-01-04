package at.ac.tuwien.kr.alpha.core.api;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Predicate;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;

/**
 * Maps between public (i.e. published) API types residing in the <code>alpha-api</code> module and implementations used by the solver
 * internally.
 * 
 * Copyright (c) 2020-2021, the Alpha Team.
 */
public class PublicToCoreApiMapper {

	public static CorePredicate mapPredicate(Predicate predicate) {
		if (predicate instanceof CorePredicate) {
			return (CorePredicate) predicate;
		} else {
			return CorePredicate.getInstance(predicate.getName(), predicate.getArity());
		}
	}

	public static CoreAtom mapAtom(Atom atom) {
		// TODO
		return null;
	}

}
