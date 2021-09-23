package at.ac.tuwien.kr.alpha.core.parser.evolog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.actions.Action;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.actions.Actions;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ParseTreeVisitor;

public class EvologParseTreeVisitor extends ASPCore2ParseTreeVisitor {

	private final Map<String, Action> actionRegistry = new HashMap<>();

	public EvologParseTreeVisitor(Map<String, PredicateInterpretation> externals, boolean acceptVariables) {
		super(externals, acceptVariables);
		actionRegistry.put("printLine", Actions::printLine);
	}

	public EvologParseTreeVisitor(Map<String, PredicateInterpretation> externals) {
		this(externals, true);
	}

	@Override
	public Head visitAction(ASPCore2Parser.ActionContext ctx) {
		BasicAtom atom = visitClassical_literal(ctx.classical_literal());
		VariableTerm actionResultTerm = visitVariable_term(ctx.variable_term());
		String actionId = ctx.ID().getText();
		if (!actionRegistry.containsKey(actionId)) {
			throw new UnsupportedOperationException("Could not resolve action name " + actionId);
		}
		List<Term> actionInputTerms = visitTerms(ctx.terms());
		return Heads.newActionHead(atom, actionRegistry.get(actionId), actionInputTerms, actionResultTerm);
	}

}
