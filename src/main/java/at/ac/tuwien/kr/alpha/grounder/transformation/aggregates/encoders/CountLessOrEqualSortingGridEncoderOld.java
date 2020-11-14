package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class CountLessOrEqualSortingGridEncoderOld extends AbstractAggregateEncoder {

	//@formatter:off
	private static final String PHI = "N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L";
	private static final ST SORTING_GRID_CORE = Util.aspStringTemplate(
			// aggregate result rule
			"$aggregate_result$(ARGS, K) :- $aggregate_id$_sorting_network_bound(ARGS, K), K<=0." +
			"$aggregate_result$(ARGS, K) :- "
			+ "$aggregate_id$_sorting_network_bound(ARGS, K), $aggregate_id$_sorting_network_v(ARGS, K, D), "
			+ "$aggregate_id$_sorting_network_done(N, D), K<=N." +

			// Sorting network encoding
			"$aggregate_id$_sorting_network_span(ARGS, I) :- $aggregate_id$_sorting_network_input_number(ARGS, I)." +
			"$aggregate_id$_sorting_network_span(ARGS, Im1) :- $aggregate_id$_sorting_network_span(ARGS, I), 1<I, Im1 = I-1." +
			"$aggregate_id$_sorting_network_v(ARGS, I, D) :- $aggregate_id$_sorting_network_input_number(ARGS, I), D=0." +
			"$aggregate_id$_sorting_network_v(ARGS, I, D) :- "
			+ "$aggregate_id$_sorting_network_v(ARGS, I, D1), D1=D-1, $aggregate_id$_sorting_network_comp(I, _, D), "
			+ "$aggregate_id$_sorting_network_dh(ARGS, D)." +
			"$aggregate_id$_sorting_network_v(ARGS, I, D) :- "
			+ "$aggregate_id$_sorting_network_v(ARGS, J, D1), "
			+ "D1=D-1, $aggregate_id$_sorting_network_comp(I, J, D), $aggregate_id$_sorting_network_dh(ARGS, D)." +
			"$aggregate_id$_sorting_network_v(ARGS, J, D) :- "
			+ "$aggregate_id$_sorting_network_v(ARGS, I, D1), D1=D-1, $aggregate_id$_sorting_network_comp(I, J, D), "
			+ "$aggregate_id$_sorting_network_dh(ARGS, D), $aggregate_id$_sorting_network_v(ARGS, J, D1)." +
			"$aggregate_id$_sorting_network_v(ARGS, I, D) :- "
			+ "$aggregate_id$_sorting_network_v(ARGS, I, D1), D1=D-1, "
			+ "$aggregate_id$_sorting_network_pass(I, D), $aggregate_id$_sorting_network_dh(ARGS, D)." +
			"$aggregate_id$_sorting_network_span_project(I) :- $aggregate_id$_sorting_network_span(_, I)." +
			"$aggregate_id$_sorting_network_part(P) :- "
			+ "$aggregate_id$_sorting_network_span_project(I), Im1=I-1, $aggregate_id$_sorting_network_log2(Im1, P1), P=P1+1." +
			"$aggregate_id$_sorting_network_lvl(1,1,1) :- $aggregate_id$_sorting_network_part(1)." +
			"$aggregate_id$_sorting_network_lvl(L,P1,DL) :- "
			+ "$aggregate_id$_sorting_network_lvl(P,P,D), "
			+ "P1=P+1, $aggregate_id$_sorting_network_part(P1), L=1..P1, DL=D+L." +
			"$aggregate_id$_sorting_network_comp(I,J,D) :- "
			+ "$aggregate_id$_sorting_network_lvl(1,P,D), $aggregate_id$_sorting_network_span_project(I), I<J, J=((I-1)^(2**(P-1)))+1." +
			"$aggregate_id$_sorting_network_comp(I,J,D) :- "
			+ "$aggregate_id$_sorting_network_lvl(L,P,D), $aggregate_id$_sorting_network_span_project(I), "
			+ "J=I+S, 1<L, N!=0, N!=B-1, N \\ 2 = 1, " + PHI + ".\n" +
			"$aggregate_id$_sorting_network_pass(I,D) :- "
			+ "$aggregate_id$_sorting_network_lvl(L,P,D), $aggregate_id$_sorting_network_span_project(I), 1<L, N=0, " + PHI + ".\n" +
			"$aggregate_id$_sorting_network_pass(I,D) :- $aggregate_id$_sorting_network_lvl(L,P,D), "
			+ "$aggregate_id$_sorting_network_span_project(I), 1<L, N=B-1, " + PHI + ".\n" +
			"$aggregate_id$_sorting_network_dh(ARGS, 1..D) :- $aggregate_id$_sorting_network_span(ARGS, N1), N1=N+1, $aggregate_id$_sorting_network_done(N,_), "
			+ "N2=N*2, $aggregate_id$_sorting_network_done(N2,D).\n" +
			"$aggregate_id$_sorting_network_done(N,D) :- $aggregate_id$_sorting_network_log2(N,P), $aggregate_id$_sorting_network_lvl(P,P,D).\n" +
			"$aggregate_id$_sorting_network_done(1,0).\n" +
			"$aggregate_id$_sorting_network_log2(Ip2, I) :- Ip2 = 2 ** I, I = 0..30.");	
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	/*
	 * isEmbeddedEncoder and inputNumberSourceAtom are only set when creating an "embedded" encoder that is used by a
	 * CountEqualsAggregateEncoder to generate a sorting grid encoding as part of another encoding.
	 */
	private final boolean isEmbeddedEncoder;
	private final BasicAtom inputNumberSource;

	public CountLessOrEqualSortingGridEncoderOld() {
		super(AggregateFunctionSymbol.COUNT, SetUtils.hashSet(ComparisonOperator.LE));
		this.isEmbeddedEncoder = false;
		this.inputNumberSource = null;
	}

	private CountLessOrEqualSortingGridEncoderOld(BasicAtom inputNumberSource) {
		super(AggregateFunctionSymbol.COUNT, SetUtils.hashSet(ComparisonOperator.LE));
		this.isEmbeddedEncoder = true;
		this.inputNumberSource = inputNumberSource;
	}

	/**
	 * Creates a new {@link CountLessOrEqualSortingGridEncoderOld} that will use the given predicate as source for its
	 * "sorting_network_input_number" atom. E.g. Creating an embedded encoder with input number source "p/2" will
	 * result in the rule "sorting_network_input_number(ARGS, I) :- p(ARGS, I)." in the final aggregate encoding. Callers
	 * need to make sure that the given predicate has arity 2 and that one variable represents the aggregate arguments (i.e.
	 * global variables) and the other an input value to the sorting network.
	 * An embedded decoder will not generate a rule for element tuples, since these are only used to derive sorting network
	 * input numbers.
	 * 
	 * @param inputNumberSource
	 * @return
	 */
	public static CountLessOrEqualSortingGridEncoderOld createEmbeddedEncoder(BasicAtom inputNumberSource) {
		if (inputNumberSource.getPredicate().getArity() != 2) {
			throw new IllegalArgumentException("Can only use binary predicate atoms as source for sorting network input numbers!");
		}
		return new CountLessOrEqualSortingGridEncoderOld(inputNumberSource);
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();

		String enumName = aggregateId + "_enum";
		String elementTupleSymbol = this.getElementTuplePredicateSymbol(aggregateId);

		// Generate sorting grid core encoding
		ST sortingGridCoreTemplate = new ST(SORTING_GRID_CORE);
		sortingGridCoreTemplate.add("aggregate_result", aggregateToEncode.getOutputAtom().getPredicate().getName());
		sortingGridCoreTemplate.add("aggregate_id", aggregateId);
		sortingGridCoreTemplate.add("enumeration", enumName);
		sortingGridCoreTemplate.add("element_tuple", elementTupleSymbol);
		String sortingGridCoreEncodingAsp = sortingGridCoreTemplate.render();

		// Create the basic sorting network program
		InputProgram sortingGridCoreEncoding = parser.parse(sortingGridCoreEncodingAsp);

		// Generate bound rule
		BasicAtom sortingNetworkBound = new BasicAtom(Predicate.getInstance(aggregateId + "_sorting_network_bound", 2),
				aggregateToEncode.getAggregateArguments(), lit.getAtom().getLowerBoundTerm());
		BasicRule sortingNetworkBoundRule = new BasicRule(new NormalHead(sortingNetworkBound), new ArrayList<>(ctx.getDependencies(aggregateId)));

		// Generate input number rule
		BasicRule inputNumberRule = this.createInputNumberRule(aggregateToEncode);

		List<BasicRule> additionalRules = new ArrayList<>();
		additionalRules.add(sortingNetworkBoundRule);
		additionalRules.add(inputNumberRule);
		return new InputProgram(ListUtils.union(sortingGridCoreEncoding.getRules(), additionalRules), sortingGridCoreEncoding.getFacts(),
				new InlineDirectives());
	}

	@Override
	protected Optional<BasicRule> encodeAggregateElement(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		if (this.isEmbeddedEncoder) {
			return Optional.empty();
		} else {
			return super.encodeAggregateElement(aggregateId, element, ctx);
		}
	}

	private BasicRule createInputNumberRule(AggregateInfo aggregateToEncode) {
		BasicRule retVal;
		if (this.isEmbeddedEncoder) {
			retVal = this.createInputNumberRuleFromAtom(aggregateToEncode, this.inputNumberSource);
		} else {
			retVal = this.createDefaultInputNumberRule(aggregateToEncode);
		}
		return retVal;
	}

	// "$aggregate_id$_sorting_network_input_number(ARGS, I) :- $element_tuple$(ARGS, X), $enumeration$(ARGS, X, I).");
	private BasicRule createDefaultInputNumberRule(AggregateInfo aggregateToEncode) {
		String aggregateId = aggregateToEncode.getId();
		VariableTerm aggregateArgumentsVar = VariableTerm.getInstance("ARGS");
		VariableTerm indexVar = VariableTerm.getInstance("IDX");
		VariableTerm elementTupleVar = VariableTerm.getInstance("TPL");

		BasicAtom headAtom = this.createInputNumberAtom(aggregateId, aggregateArgumentsVar, indexVar);
		BasicAtom elementTupleAtom = new BasicAtom(
				Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateToEncode.getId()), 2),
				aggregateArgumentsVar, elementTupleVar);

		List<Term> enumTerms = new ArrayList<>();
		enumTerms.add(aggregateArgumentsVar);
		enumTerms.add(elementTupleVar);
		enumTerms.add(indexVar);
		BasicAtom enumerationAtom = new EnumerationAtom(enumTerms);

		return BasicRule.getInstance(new NormalHead(headAtom), elementTupleAtom.toLiteral(), enumerationAtom.toLiteral());
	}

	private BasicRule createInputNumberRuleFromAtom(AggregateInfo aggregateToEncode, BasicAtom sourceAtom) {
		String aggregateId = aggregateToEncode.getId();
		return BasicRule.getInstance(new NormalHead(this.createInputNumberAtom(aggregateId, aggregateToEncode.getAggregateArguments(), sourceAtom.getTerms().get(1))),
				sourceAtom.toLiteral());
	}

	private BasicAtom createInputNumberAtom(String aggregateId, Term argsTerm, Term idxTerm) {
		return new BasicAtom(Predicate.getInstance(this.getInputNumberPredicateName(aggregateId), 2), argsTerm, idxTerm);
	}

	private String getInputNumberPredicateName(String aggregateId) {
		return aggregateId + "_sorting_network_input_number";
	}

}
