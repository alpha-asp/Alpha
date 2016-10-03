package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.PredicateInstance;
import at.ac.tuwien.kr.alpha.common.Term;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.common.ConstantTerm.getConstantTerm;
import static at.ac.tuwien.kr.alpha.common.FunctionTerm.getFunctionTerm;
import static at.ac.tuwien.kr.alpha.common.VariableTerm.getVariableTerm;

/**
 * This class stores ground atoms and provides the translation from an (integer) atomId to a (structured) predicate instance.
 * Copyright (c) 2016, the Alpha Team.
 */
public class AtomStore {
	private ArrayList<PredicateInstance> atomIdsToInternalPredicateInstances = new ArrayList<>();
	private HashMap<PredicateInstance, AtomId> predicateInstancesToAtomIds = new HashMap<>();
	private IntIdGenerator atomIdGenerator = new IntIdGenerator();

	private ArrayList<AtomId> releasedAtomIds = new ArrayList<>();	// contains atomIds ready to be garbage collected if necessary.

	public AtomStore() {
		// Create atomId for falsum (currently not needed, but it gets atomId 0, which cannot represent a negated literal).
		createAtomId(new PredicateInstance(new BasicPredicate("\u22A5", 0), new Term[0]));
	}

	/**
	 * Returns the AtomId associated with a given ground predicate instance (=ground atom).
	 * @param groundAtom
	 * @return
	 */
	public AtomId getAtomId(PredicateInstance groundAtom) {
		return predicateInstancesToAtomIds.get(groundAtom);
	}

	/**
	 * Returns the structured ground atom associated with the given atomId.
	 * @param atomId
	 * @return
	 */
	public PredicateInstance getPredicateInstance(AtomId atomId) {
		try {
			return atomIdsToInternalPredicateInstances.get(atomId.atomId);
		} catch (IndexOutOfBoundsException e) {
			throw new RuntimeException("AtomStore: Unknown atomId encountered: " + atomId.atomId);
		}
	}

	/**
	 * Creates a new atomId representing the given ground atom. Multiple calls with the same parameter result in
	 * the same atomId (duplicates check).
	 * @param groundAtom
	 * @return
	 */
	public AtomId createAtomId(PredicateInstance groundAtom) {
		AtomId potentialId = predicateInstancesToAtomIds.get(groundAtom);
		if (potentialId == null) {
			AtomId newAtomId = new AtomId(atomIdGenerator.getNextId());
			predicateInstancesToAtomIds.put(groundAtom, newAtomId);
			atomIdsToInternalPredicateInstances.add(newAtomId.atomId, groundAtom);
			return newAtomId;
		} else {
			return potentialId;
		}
	}

	public boolean isAtomExisting(PredicateInstance groundAtom) {
		AtomId potentialId = predicateInstancesToAtomIds.get(groundAtom);
		return potentialId != null;
	}

	/**
	 * Removes the given atom from the AtomStore.
	 * @param atomId
	 */
	public void releaseAtomId(AtomId atomId) {
		releasedAtomIds.add(atomId);
		// HINT: Additionally removing the terms used in the instance might be beneficial in some cases.
	}


	/**
	 * Converts a parsed term into a common term, replacing constants and function symbols with integer Ids. The
	 * Ids are recorded.
	 * @param parsedTerm
	 * @return
	 */
	public static Term convertFromParsedTerm(ParsedTerm parsedTerm) {
		if (parsedTerm instanceof ParsedConstant) {
			String content = ((ParsedConstant) parsedTerm).content;
			return getConstantTerm(content);
		} else if (parsedTerm instanceof ParsedFunctionTerm) {
			String functionName = ((ParsedFunctionTerm) parsedTerm).functionName;
			ArrayList<Term> termlist = new ArrayList<>();
			for (int i = 0; i < ((ParsedFunctionTerm) parsedTerm).arity; i++) {
				Term term = convertFromParsedTerm(((ParsedFunctionTerm) parsedTerm).termList.get(i));
				termlist.add(term);
			}
			return getFunctionTerm(functionName, termlist);
		} else if (parsedTerm instanceof ParsedVariable) {
			return getVariableTerm(((ParsedVariable) parsedTerm).variableName);
		} else {
			throw new RuntimeException("Parsed program contains a term of unknown type: " + parsedTerm.getClass());
		}
	}

	public String printAtomIdTermMapping() {
		String ret = "";
		for (Map.Entry<PredicateInstance, AtomId> entry : predicateInstancesToAtomIds.entrySet()) {
			ret += entry.getValue().atomId + " <-> " + entry.getKey().toString() + "\n";
		}
		return ret;
	}

}
