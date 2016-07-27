package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BasicAnswerSet implements AnswerSet {

	public void setPredicateList(ArrayList<Predicate> predicateList) {
		this.predicateList = predicateList;
	}

	public void setPredicateInstances(HashMap<Predicate, ArrayList<PredicateInstance>> predicateInstances) {
		this.predicateInstances = predicateInstances;
	}

	public void setTermIdStringMap(HashMap<Integer, String> termIdStringMap) {
		this.termIdStringMap = termIdStringMap;
	}

	private ArrayList<Predicate> predicateList = new ArrayList<>();
	private HashMap<Predicate, ArrayList<PredicateInstance>> predicateInstances = new HashMap<>();
	private HashMap<Integer, String> termIdStringMap = new HashMap<>();

	@Override
	public List<Predicate> getPredicateList() {
		return predicateList;
	}

	@Override
	public List<String> getPredicateInstacesAsString(Predicate predicate) {
		List<PredicateInstance> predicateInstances = getPredicateInstances(predicate);
		ArrayList<String> stringInstances = new ArrayList<>();
		for (PredicateInstance predicateInstance : predicateInstances) {
			String stringInstance = "";
			for (int i = 0; i < predicateInstance.termList.size(); i++) {
				stringInstance += i != 0 ? ", " : "";
				stringInstance += termToString(predicateInstance.termList.get(i));
			}
		}
		return stringInstances;
	}

	private String termToString(Term term) {
		String stringTerm = "";
		if (term instanceof ConstantTerm) {
			stringTerm = termIdStringMap.get(((ConstantTerm) term).constantId);
		} else if (term instanceof FunctionTerm) {
			stringTerm = termIdStringMap.get(((FunctionTerm) term).functionSymbol);
			if (((FunctionTerm) term).termList.size() > 0) {
				stringTerm += "(";
				boolean isFirst = true;
				for (Term funcTerm : ((FunctionTerm) term).termList) {
					if (isFirst) {
						isFirst = false;
					} else {
						stringTerm += ", ";
					}
					stringTerm += termToString(funcTerm);
				}
				stringTerm += ")";
			}
		}
		return stringTerm;
	}

	@Override
	public List<PredicateInstance> getPredicateInstances(Predicate predicate) {
		return predicateInstances.get(predicate);
	}

	@Override
	public Map<Integer, String> getTermIdToStringMap() {
		return termIdStringMap;
	}

	public String toString() {
		String ret = "{ ";
		for (int i = 0; i < predicateList.size(); i++) {
			ret += i != 0 ? ", "  : "";

			Predicate predicate = predicateList.get(i);
			List<String> instances = getPredicateInstacesAsString(predicate);

			if (instances.size() == 0) {
				ret += predicate.getPredicateName();
			} else {

				for (int j = 0; j < instances.size(); j++) {
					ret += j != 0 ? ", " : "";
					ret += ", " + predicate.getPredicateName() + "(" + instances.get(j) + ")";
				}
			}
		}
		ret += " }";
		return ret;
	}
}
