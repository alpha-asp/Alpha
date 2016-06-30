package at.ac.tuwien.kr.alpha;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * A {@link org.antlr.v4.runtime.tree.ParseTreeListener} that translates
 * a concrete syntax tree generated from the ASP Core 2 Input Language
 * Specification into a {@link Program}.
 *
 * @see <a href="https://github.com/antlr/antlr4/blob/master/doc/listeners.md">Parse Tree Listeners</a>
 */
public class Listener extends ASPCore2BaseListener {
	private Stack<Atom> bodyPos = new Stack<>();
	private Stack<Atom> bodyNeg = new Stack<>();

	private Stack<Integer> terms = new Stack<>();
	private Stack<Rule> rules = new Stack<>();

	private final BidiMap<Integer, String> variables = new DualHashBidiMap<>();
	private final BidiMap<Integer, String> constants = new DualHashBidiMap<>();
	private final BidiMap<Integer, String> predicates = new DualHashBidiMap<>();

	private Atom head;
	private int predicate;
	private Program program;

	public Program getProgram() {
		return program;
	}

	@Override
	public void exitProgram(ASPCore2Parser.ProgramContext ctx) {
		super.exitProgram(ctx);
		program = new Program(
			constants.values().toArray(new String[constants.size()]),
			predicates.values().toArray(new String[predicates.size()]),
			rules.toArray(new Rule[rules.size()])
		);
	}

	@Override
	public void exitStatement(ASPCore2Parser.StatementContext ctx) {
		super.exitStatement(ctx);
		rules.push(new Rule(
			variables.values().toArray(new String[variables.size()]),
			head,
			bodyPos.toArray(new Atom[bodyPos.size()]),
			bodyNeg.toArray(new Atom[bodyNeg.size()])
		));

		// Reset both parts of the rule (head and body)
		// and variables to nothing.
		head = null;
		bodyPos.clear();
		bodyNeg.clear();
		variables.clear();
	}

	@Override
	public void exitHead(ASPCore2Parser.HeadContext ctx) {
		super.exitHead(ctx);
		head = new Atom(predicate, ArrayUtils.toPrimitive(terms.toArray(new Integer[terms.size()])));

		// Reset predicate and terms to nothing.
		predicate = 0;
		terms.clear();
	}

	@Override
	public void exitNaf_literal(ASPCore2Parser.Naf_literalContext ctx) {
		super.exitNaf_literal(ctx);
		if (ctx.builtin_atom() != null) {
			no("builtin atoms");
		}

		Stack<Atom> stack = ctx.NAF() != null ? bodyNeg : bodyPos;
		stack.push(new Atom(predicate, ArrayUtils.toPrimitive(terms.toArray(new Integer[terms.size()]))));

		// Reset predicate and terms to nothing.
		predicate = 0;
		terms.clear();
	}

	@Override
	public void enterClassical_literal(ASPCore2Parser.Classical_literalContext ctx) {
		super.enterClassical_literal(ctx);
		// If we are visiting this predicate for the first time,
		// add it to the global set of predicates.
		predicate = putIfNeeded(predicates, ctx.ID().getSymbol().getText());
	}

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
		if (ctx.terms() != null) {
			no("functions");
		}
		super.enterTerm(ctx);

		if (ctx.VARIABLE() != null) {
			terms.push(+putIfNeeded(variables, ctx.VARIABLE().getSymbol().getText()));
		} else {
			terms.push(-putIfNeeded(constants, ctx.ID().getSymbol().getText()));
		}
	}

	@Override
	public void enterStatement(ASPCore2Parser.StatementContext ctx) {
		super.enterStatement(ctx);

		// NOTE(flowlo): Is MINUS really a negation or arithmetic?
		if (ctx.head().disjunction().classical_literal().MINUS() != null) {
			no("negations in head");
		}
	}

	@Override
	public void enterDisjunction(ASPCore2Parser.DisjunctionContext ctx) {
		if (ctx.OR() != null) {
			no("disjunctions");
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

	/**
	 * Shorthand for throwing a {@link UnsupportedOperationException}
	 * @param feature the feature that is no supported
	 */
	private static void no(String feature) {
		throw new UnsupportedOperationException(feature + " no supported");
	}

	private static <V> int putIfNeeded(BidiMap<Integer, V> m, V v) {
		if (m.containsValue(v)) {
			return m.getKey(v);
		}

		int k = m.size() + 1;
		m.put(k, v);
		return k;
	}
}
