package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.grounder.Predicate;

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

	public void setPredicateInstances(HashMap<Predicate, ArrayList<Integer>> predicateInstances) {
		this.predicateInstances = predicateInstances;
	}

	public void setTermIdStringMap(HashMap<Integer, String> termIdStringMap) {
		this.termIdStringMap = termIdStringMap;
	}

	private ArrayList<Predicate> predicateList = new ArrayList<>();
	private HashMap<Predicate, ArrayList<Integer>> predicateInstances = new HashMap<>();
	private HashMap<Integer, String> termIdStringMap = new HashMap<>();

	@Override
	public List<Predicate> getPredicateList() {
		return predicateList;
	}

	@Override
	public List<String> getPredicateInstacesAsString(Predicate predicate) {
		List<Integer> predicateInstancesAsTermId = getPredicateInstancesAsTermId(predicate);
		Map<Integer, String> termIdToString = getTermIdToStringMap();
		ArrayList<String> stringInstances = new ArrayList<>();
		for (Integer instanceId : predicateInstancesAsTermId) {
			stringInstances.add(termIdToString.get(instanceId));
		}
		return stringInstances;
	}

	@Override
	public List<Integer> getPredicateInstancesAsTermId(Predicate predicate) {
		return predicateInstances.get(predicate);
	}

	@Override
	public Map<Integer, String> getTermIdToStringMap() {
		return termIdStringMap;
	}

	public String toString() {
		String ret = "";
		boolean isFirst = true;
		for (Predicate predicate : predicateList) {
			List<String> instances = getPredicateInstacesAsString(predicate);
			for (String instance : instances) {
				if (isFirst) {
					ret += "{ " + predicate.getPredicateName() + "(" + instance + ")";
					isFirst = false;
				} else {
					ret += ", " + predicate.getPredicateName() + "(" + instance + ")";
				}
			}
		}
		ret += " }";
		return ret;
	}
}
