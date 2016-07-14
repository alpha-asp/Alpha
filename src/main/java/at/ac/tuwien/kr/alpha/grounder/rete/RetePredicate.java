package at.ac.tuwien.kr.alpha.grounder.rete;

import at.ac.tuwien.kr.alpha.grounder.GrounderPredicate;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class RetePredicate implements GrounderPredicate{

	public final String predicateName;
	public final int arity;
	public final TupleStore tupleStore;

	// TODO: link/put memory of instances here?

	// TODO: put indices for attribute positions also here?

	private static HashMap<Pair<String, Integer>, RetePredicate> knownPredicates = new HashMap<>();

	public static RetePredicate getPredicate(String predicateName, int arity) {
		RetePredicate existingPredicate = knownPredicates.get(new MutablePair<>(predicateName, arity));
		if (existingPredicate != null) {
			return existingPredicate;
		} else {
			RetePredicate newPredicate = new RetePredicate(predicateName, arity);
			knownPredicates.put(new MutablePair<>(predicateName, arity), newPredicate);
			return newPredicate;
		}
	}

	private RetePredicate(String predicateName, int arity) {
		this.predicateName = predicateName;
		this.arity = arity;
		tupleStore = new TupleStore();
	}

	@Override
	public String getPredicateName() {
		return predicateName;
	}

	@Override
	public int getArity() {
		return arity;
	}
}
