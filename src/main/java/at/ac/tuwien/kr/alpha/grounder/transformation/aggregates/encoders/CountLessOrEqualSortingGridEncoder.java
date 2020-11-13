package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.EnumerationRewriting;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class CountLessOrEqualSortingGridEncoder extends AbstractAggregateEncoder {

	//@formatter:off
	private static final String PHI = "N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L";
	private static final ST CNT_LE_ENCODING = Util.aspStringTemplate(
			"#enumeration_predicate_is $enumeration$." +
			// aggregate result rule
			"$aggregate_result$(ARGS, K) :- $aggregate_id$_sorting_network_bound(ARGS, K), K<=0." +
			"$aggregate_result$(ARGS, K) :- "
			+ "$aggregate_id$_sorting_network_bound(ARGS, K), $aggregate_id$_sorting_network_v(ARGS, K, D), "
			+ "$aggregate_id$_sorting_network_done(N, D), K<=N." +
			// Assign indices to element tuples
			"$aggregate_id$_sorting_network_input_number(ARGS, I) :- $element_tuple$(ARGS, X), $enumeration$(ARGS, X, I)." +
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

	public CountLessOrEqualSortingGridEncoder() {
		super(AggregateFunctionSymbol.COUNT, SetUtils.hashSet(ComparisonOperator.LE));
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();
		ST encodingTemplate = new ST(CNT_LE_ENCODING);
		encodingTemplate.add("aggregate_result", aggregateToEncode.getOutputAtom().getPredicate().getName());
		encodingTemplate.add("aggregate_id", aggregateId);
		encodingTemplate.add("enumeration", aggregateId + "_enum");
		encodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		String resultEncodingAsp = encodingTemplate.render();
		InputProgram resultEncoding = new EnumerationRewriting().apply(parser.parse(resultEncodingAsp));
		BasicAtom sortingNetworkBound = new BasicAtom(Predicate.getInstance(aggregateId + "_sorting_network_bound", 2),
				aggregateToEncode.getAggregateArguments(), lit.getAtom().getLowerBoundTerm());
		BasicRule sortingNetworkBoundRule = new BasicRule(new NormalHead(sortingNetworkBound), new ArrayList<>(ctx.getDependencies(aggregateId)));
		return new InputProgram(ListUtils.union(resultEncoding.getRules(), Collections.singletonList(sortingNetworkBoundRule)), resultEncoding.getFacts(),
				new InlineDirectives());
	}

}
