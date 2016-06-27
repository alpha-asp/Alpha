package at.ac.tuwien.kr.alpha;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.*;

public class Listener extends ASPCore2BaseListener {
	private final List<Rule> rules;
	private Stack<Atom> body;
	private boolean mode = true;

	Listener() {
		this.rules = new ArrayList<>();
	}

	@Override
	public void enterProgram(ASPCore2Parser.ProgramContext ctx) {
		super.enterProgram(ctx);
	}

	@Override
	public void enterStatement(ASPCore2Parser.StatementContext ctx) {
		super.enterStatement(ctx);
		ctx.head().disjunction().classical_literal().ID();

		if (ctx.head().disjunction().classical_literal().MINUS() != null) {
			no("negations in head");
		}
	}

	@Override
	public void exitStatement(ASPCore2Parser.StatementContext ctx) {
		super.exitStatement(ctx);
	}

	@Override
	public void enterHead(ASPCore2Parser.HeadContext ctx) {

	}

	@Override
	public void enterDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		if (ctx.OR() != null) {
			no("disjunctions in head");
		}
		super.enterDisjunction(ctx);
	}

	@Override
	public void enterChoice(ASPCore2Parser.ChoiceContext ctx) {
		no("choices");
	}

	@Override
	public void enterAggregate(ASPCore2Parser.AggregateContext ctx) {
		no("aggregates");
	}

	TIntObjectMap<String> x = new TIntObjectHashMap<>();

	Set<Term> variableNameSet = new HashSet<>();
	List<Term> variableNameList = new ArrayList<>();

	@Override
	public void enterTerm(ASPCore2Parser.TermContext ctx) {
		if (ctx.arithop() != null) {
			no("arithmetic operations");
		}
		if (ctx.MINUS() != null) {
			no("negated terms");
		}
		if (ctx.ANONYMOUS_VARIABLE() != null) {
			no("anonymous variables");
		}
		if (ctx.NUMBER() != null) {
			no("numbers");
		}
		if (ctx.STRING() != null) {
			no("strings");
		}
		super.enterTerm(ctx);

		if (ctx.VARIABLE() != null) {
			String variableName = ctx.VARIABLE().getSymbol().getText();
			variableNameSet.add(variableName);
		}



		new ctx.ID().getSymbol().getText()
	}

	private static boolean isVariable(String token) {
		return token != null && !token.isEmpty() && Character.isUpperCase(token.charAt(0));
	}

	@Override
	public void exitTerm(ASPCore2Parser.TermContext ctx) {
		Term t;
		if (ctx.ID() != null) {
			String[] variableNames = variableNameSet.stream().toArray(String[]::new);
			variableNameList.stream().map(v -> new Variable(Arrays.binarySearch(variableNames, v)));

			Predicate p = Predicate.getInstance(ctx.ID().getSymbol().getText(), )
			for ()
			String[] variableNames = variableNameSet.toArray(ctx.ID().getSymbol().getText(), );
			Term[] arguments =
			Predicate p = new Predicate(, )
			t = new Predicate()
		}
		super.exitTerm(ctx);
	}

	/**
	 * Shorthand for throwing a {@link UnsupportedOperationException}
	 * @param feature the feature that is no supported
	 */
	private static void no(String feature) {
		throw new UnsupportedOperationException(feature + " no supported");
	}
}
