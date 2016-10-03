package at.ac.tuwien.kr.alpha.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

	@Override
	public List<Predicate> getPredicateList() {
		return predicateList;
	}

	private ArrayList<Predicate> predicateList = new ArrayList<>();
	private HashMap<Predicate, ArrayList<PredicateInstance>> predicateInstances = new HashMap<>();


	@Override
	public List<String> getPredicateInstancesAsString(Predicate predicate) {
		List<PredicateInstance> predicateInstances = getPredicateInstances(predicate);
		ArrayList<String> stringInstances = new ArrayList<>();
		for (PredicateInstance predicateInstance : predicateInstances) {
			// Predicates of arity 0 have an empty list of instances.
			if (predicateInstance.termList.length == 0) {
				continue;
			}
			StringBuilder stringInstance = new StringBuilder();
			for (int i = 0; i < predicateInstance.termList.length; i++) {
				stringInstance.append(i != 0 ? ", " : "");
				stringInstance.append(termToString(predicateInstance.termList[i]));
			}
			stringInstances.add(stringInstance.toString());
		}
		return stringInstances;
	}

	private String termToString(Term term) {
		String stringTerm = "";
		if (term instanceof ConstantTerm) {
			stringTerm = ((ConstantTerm) term).constantSymbol.getSymbol();
		} else if (term instanceof FunctionTerm) {
			stringTerm = ((FunctionTerm) term).functionSymbol.getSymbol();
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


	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("{ ");
		for (int i = 0; i < predicateList.size(); i++) {
			ret.append(i != 0 ? ", "  : "");

			Predicate predicate = predicateList.get(i);
			List<String> instances = getPredicateInstancesAsString(predicate);

			if (instances.size() == 0) {
				ret.append(predicate.getPredicateName());
			} else {

				for (int j = 0; j < instances.size(); j++) {
					ret.append(j != 0 ? ", " : "");
					ret.append(predicate.getPredicateName());
					ret.append("(");
					ret.append(instances.get(j));
					ret.append(")");
				}
			}
		}
		ret.append(" }");
		return ret.toString();
	}
}
