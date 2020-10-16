package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public abstract class AbstractAggregateTransformation extends ProgramTransformation<InputProgram, InputProgram> {

	public static final Predicate AGGREGATE_RESULT = Predicate.getInstance("aggregate_result", 2);

	@Override
	public InputProgram apply(InputProgram program) {
		// create a "indices" of aggregate literals and their occurrences in rules
		AggregateRewritingContext ctx = this.buildRewritingContext(program);
		// replace aggregate literals in rule bodies with rewritten versions
		List<BasicRule> rewrittenRules = this.rewriteAggregateLiterals(ctx, program);
		// add additional rules for aggregate evaluation (specific for each type of aggregate)
		InputProgram aggregateEncoding = this.encodeAggregates(ctx);
		InputProgram.Builder resultBuilder = InputProgram.builder();
		resultBuilder.addRules(rewrittenRules);
		resultBuilder.addFacts(program.getFacts());
		resultBuilder.addInlineDirectives(program.getInlineDirectives());
		resultBuilder.accumulate(aggregateEncoding);
		return resultBuilder.build();
	}

	// TODO context should be built externally!
	private AggregateRewritingContext buildRewritingContext(InputProgram program) {
		AggregateRewritingContext ctx = new AggregateRewritingContext();
		AggregateLiteral aggLit;
		for (BasicRule rule : program.getRules()) {
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral && this.shouldHandle(aggLit = (AggregateLiteral) lit)) {
					ctx.registerAggregateLiteral(aggLit, rule);
				}
			}
		}
		return ctx;
	}

	// TODO maybe possible in same loop as building context?? (probably not, needs to be done repeatedly..)
	private List<BasicRule> rewriteAggregateLiterals(AggregateRewritingContext ctx, InputProgram input) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : input.getRules()) {
			boolean hasAggregate = false;
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					hasAggregate = true;
					break;
				}
			}
			rewrittenRules.add(hasAggregate ? rewriteAggregateLiterals(ctx, rule) : rule);
		}
		return rewrittenRules;
	}

	// Transforms literals of format "VAR1 OP #AGG_FN{...} OP VAR2" into literals of format "_aggregate_result(AGG_ID,
	// output(VAR1, VAR2))".
	private BasicRule rewriteAggregateLiterals(AggregateRewritingContext ctx, BasicRule rule) {
		List<Literal> rewrittenBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral && ctx.getLiteralsToRewrite().contains((AggregateLiteral) lit)) {
				BasicAtom aggregateOutputAtom = ctx.getAggregateOutputAtom((AggregateLiteral) lit);
				rewrittenBody.add(new BasicLiteral(aggregateOutputAtom, !lit.isNegated()));
			} else {
				rewrittenBody.add(lit);
			}
		}
		return new BasicRule(rule.getHead(), rewrittenBody);
	}

	protected abstract boolean shouldHandle(AggregateLiteral lit);

	protected abstract InputProgram encodeAggregates(AggregateRewritingContext ctx);

	public static class AggregateRewritingContext {

		private int idCounter;
		private Map<AggregateLiteral, String> aggregateIds = new HashMap<>();
		private Map<AggregateLiteral, BasicAtom> aggregateOutputAtoms = new HashMap<>();
		private Map<AggregateLiteral, BasicRule> aggregateSourceRules = new HashMap<>();

		public void registerAggregateLiteral(AggregateLiteral lit, BasicRule source) {
			if (this.aggregateIds.containsKey(lit)) {
				return;
			}
			AggregateAtom atom = lit.getAtom();
			String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++this.idCounter);
			this.aggregateIds.put(lit, id);
			this.aggregateOutputAtoms.put(lit, this.buildAggregateOutputAtom(id, atom));
			this.aggregateSourceRules.put(lit, source);
		}

		private BasicAtom buildAggregateOutputAtom(String aggregateId, AggregateAtom atom) {
			return new BasicAtom(AGGREGATE_RESULT, ConstantTerm.getSymbolicInstance(aggregateId), atom.getLowerBoundTerm());
		}

		public String getAggregateId(AggregateLiteral lit) {
			return this.aggregateIds.get(lit);
		}

		public BasicAtom getAggregateOutputAtom(AggregateLiteral lit) {
			return this.aggregateOutputAtoms.get(lit);
		}

		public Set<AggregateLiteral> getLiteralsToRewrite() {
			return Collections.unmodifiableSet(this.aggregateIds.keySet());
		}
	}

}
