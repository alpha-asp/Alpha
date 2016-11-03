package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedBuiltinAtom;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class BuiltinAtom implements Atom {
	public final Term left;
	public final Term right;
	public final ParsedBuiltinAtom.BINOP binop;

	public BuiltinAtom(ParsedBuiltinAtom parsedBuiltinAtom) {
		binop = parsedBuiltinAtom.binop;
		left = Term.convertFromParsedTerm(parsedBuiltinAtom.terms.get(0));
		right = Term.convertFromParsedTerm(parsedBuiltinAtom.terms.get(1));
	}

	@Override
	public String toString() {
		return left + " " + binop + " " + right;
	}

	@Override
	public boolean isGround() {
		return left.isGround() && right.isGround();
	}

	@Override
	public List<VariableTerm> getOccurringVariables() {
		List<VariableTerm> vars = new ArrayList<>(2);
		if (left instanceof VariableTerm) {
			vars.add((VariableTerm) left);
		}
		if (right instanceof VariableTerm) {
			vars.add((VariableTerm) right);
		}
		return vars;
	}

	public static boolean evaluateBuiltinAtom(BuiltinAtom builtinAtom, NaiveGrounder.VariableSubstitution variableSubstitution) {
		int left = evaluateExpression(builtinAtom.left, variableSubstitution);
		int right = evaluateExpression(builtinAtom.right, variableSubstitution);
		switch (builtinAtom.binop) {
			case EQUAL:
				return  left == right;
			case UNEQUAL:
				return  left != right;
			case GREATER:
				return  left > right;
			case LESS:
				return left < right;
			case GREATER_OR_EQ:
				return left >= right;
			case LESS_OR_EQ:
				return left <= right;
		}
		throw new RuntimeException("Unknown binop: " + builtinAtom.binop);
	}

	private static int evaluateExpression(Term term, NaiveGrounder.VariableSubstitution variableSubstitution) {
		if (term instanceof VariableTerm) {
			return evaluateExpression(variableSubstitution.eval((VariableTerm) term), variableSubstitution);
		} else if (term instanceof ConstantTerm) {
			try {
				return Integer.parseInt(term.toString());
			} catch (NumberFormatException e) {
				throw new RuntimeException("Could not convert term " + term + " to a number.", e);
			}
		} else {
			throw new RuntimeException("Not supported term structure in builtin atom encountered: " + term);
		}
	}
}
