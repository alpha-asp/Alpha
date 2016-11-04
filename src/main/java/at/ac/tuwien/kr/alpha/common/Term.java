package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedVariable;

import java.util.ArrayList;
import java.util.List;

import static at.ac.tuwien.kr.alpha.common.FunctionTerm.getFunctionTerm;

/**
 * Common representation of Terms. Terms are constructed such that each term is represented by a unique object, hence
 * term equality can be checked by object reference comparison. Each concrete subclass of a Term must implement a
 * factory-like method to obtain instances.
 * Copyright (c) 2016, the Alpha Team.
 */
public abstract class Term {
	/**
	 * Converts a parsed term into a common term, replacing constants and function symbols with integer Ids. The
	 * Ids are recorded.
	 * @param parsedTerm
	 * @return
	 */
	public static Term convertFromParsedTerm(ParsedTerm parsedTerm) {
		if (parsedTerm instanceof ParsedConstant) {
			String content = ((ParsedConstant) parsedTerm).content;
			return ConstantTerm.getInstance(content);
		} else if (parsedTerm instanceof ParsedFunctionTerm) {
			String functionName = ((ParsedFunctionTerm) parsedTerm).functionName;
			ArrayList<Term> termlist = new ArrayList<>();
			for (int i = 0; i < ((ParsedFunctionTerm) parsedTerm).arity; i++) {
				Term term = convertFromParsedTerm(((ParsedFunctionTerm) parsedTerm).termList.get(i));
				termlist.add(term);
			}
			return getFunctionTerm(functionName, termlist);
		} else if (parsedTerm instanceof ParsedVariable) {
			if (((ParsedVariable) parsedTerm).isAnonymous) {
				return VariableTerm.getNewAnonymousVariable();
			} else {
				return VariableTerm.getInstance(((ParsedVariable) parsedTerm).variableName);
			}
		} else {
			throw new RuntimeException("Parsed program contains a term of unknown type: " + parsedTerm.getClass());
		}
	}

	public abstract boolean isGround();

	public abstract List<VariableTerm> getOccurringVariables();

	@Override
	public abstract String toString();
}
