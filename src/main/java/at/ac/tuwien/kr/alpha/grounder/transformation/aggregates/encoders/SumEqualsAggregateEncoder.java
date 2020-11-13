package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import java.util.Collections;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.EnumerationRewriting;
import at.ac.tuwien.kr.alpha.grounder.transformation.PredicateInternalizer;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public class SumEqualsAggregateEncoder extends AbstractAggregateEncoder {

	//@formatter:off
	// TODO look into also delegating to cnt_candidate from underlying sorting grid encoder
	private static final ST SUM_EQ_LITERAL_ENCODING = Util.aspStringTemplate(
			"#enumeration_predicate_is $enumeration$."
			+ "$aggregate_result$(ARGS, VAL) :- $leq$(ARGS, VAL), not $leq$(ARGS, NEXTVAL), NEXTVAL = VAL + 1."
			+ "$leq$($aggregate_arguments$, $value_var$) :- $value_leq_sum_lit$, $sum_candidate_lit$."
			+ "$id$_sum_element_at_index(ARGS, VAL, IDX) :- $element_tuple$(ARGS, TPL, VAL), $enumeration$(ARGS, TPL, IDX)."
			+ "$id$_sum_element_index(ARGS, IDX) :- $id$_sum_element_at_index(ARGS, _, IDX)."
			// In case all elements are false, 0 is a candidate sum
			+ "$id$_sum_at_idx_candidate(ARGS, 0, 0) :- $id$_sum_element_at_index(ARGS, _, _)."
			// Assuming the element with index I is false, all candidate sums up to the last index are also valid candidates for this index
			+ "$id$_sum_at_idx_candidate(ARGS, I, CSUM) :- $id$_sum_element_index(ARGS, I), $id$_sum_at_idx_candidate(ARGS, IPREV, CSUM), IPREV = I - 1."
			// Assuming the element with index I is true, all candidate sums up to the last index plus the value of the element at
			// index I are candidate sums."
			+ "$id$_sum_at_idx_candidate(ARGS, I, CSUM) :- $id$_sum_element_at_index(ARGS, VAL, I), $id$_sum_at_idx_candidate(ARGS, IPREV, PSUM), IPREV = I - 1, CSUM = PSUM + VAL."
			// Project indices away, we only need candidate values for variable binding.
			+ "$sum_candidate$(ARGS, CSUM) :- $id$_sum_at_idx_candidate(ARGS, _, CSUM).");
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	public SumEqualsAggregateEncoder() {
		super(AggregateFunctionSymbol.SUM, SetUtils.hashSet(ComparisonOperator.EQ));
	}

	@Override
	// TODO look into generalizing this!
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();
		AggregateAtom sourceAtom = lit.getAtom();
		VariableTerm valueVar = VariableTerm.getInstance("SUM");
		// Build a new AggregateLiteral representing the "SUM <= #sum{...}" part of the encoding.
		AggregateLiteral candidateLeqSum = new AggregateLiteral(
				new AggregateAtom(ComparisonOperator.LE, valueVar, AggregateFunctionSymbol.SUM, sourceAtom.getAggregateElements()), true);
		String sumCandidatePredicateSymbol = aggregateId + "_candidate";
		BasicLiteral sumCandidate = new BasicLiteral(
				new BasicAtom(Predicate.getInstance(sumCandidatePredicateSymbol, 2), aggregateToEncode.getAggregateArguments(), valueVar), true);
		// Create an encoder for the newly built " <= #sum{..}" literal and rewrite it.
		// Note that the literal itself is not written into the encoding of the original literal,
		// but only its substitute "aggregate result" literal.
		AggregateRewritingContext candidateLeqSumCtx = AggregateRewritingContext.createChildContext(ctx);
		String candidateLeqSumId = candidateLeqSumCtx.registerAggregateLiteral(candidateLeqSum, Collections.singleton(sumCandidate));
		// The encoder won't encode AggregateElements of the newly created literal separately but alias them
		// with the element encoding predicates for the original literal.
		AbstractAggregateEncoder candidateLeqEncoder = new SumLessOrEqualEncoder();
		InputProgram candidateLeqEncoding = candidateLeqEncoder
				.encodeAggregateLiteral(candidateLeqSumCtx.getAggregateInfo(candidateLeqSumId), candidateLeqSumCtx);
		// Create a fresh template to make sure attributes are empty at each call to encodeAggregateResult.
		ST encodingTemplate = new ST(SUM_EQ_LITERAL_ENCODING);
		encodingTemplate.add("id", aggregateId);
		encodingTemplate.add("aggregate_result", aggregateToEncode.getOutputAtom().getPredicate().getName());
		encodingTemplate.add("leq", aggregateId + "_leq");
		encodingTemplate.add("sum_candidate", sumCandidatePredicateSymbol);
		encodingTemplate.add("value_var", valueVar.toString());
		encodingTemplate.add("value_leq_sum_lit", candidateLeqSumCtx.getAggregateInfo(candidateLeqSumId).getOutputAtom().toString());
		encodingTemplate.add("sum_candidate_lit", sumCandidate.toString());
		encodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		encodingTemplate.add("enumeration", aggregateId + "_enum");
		encodingTemplate.add("aggregate_arguments", aggregateToEncode.getAggregateArguments());
		String resultEncodingAsp = encodingTemplate.render();
		InputProgram resultEncoding = PredicateInternalizer.makePrefixedPredicatesInternal(new EnumerationRewriting().apply(parser.parse(resultEncodingAsp)),
				candidateLeqSumId);
		return InputProgram.builder(resultEncoding).accumulate(candidateLeqEncoding).build();
	}

	@Override
	protected Atom buildElementRuleHead(String aggregateId, AggregateElement element, AggregateRewritingContext ctx) {
		Predicate headPredicate = Predicate.getInstance(this.getElementTuplePredicateSymbol(aggregateId), 3);
		AggregateInfo aggregate = ctx.getAggregateInfo(aggregateId);
		Term aggregateArguments = aggregate.getAggregateArguments();
		FunctionTerm elementTuple = FunctionTerm.getInstance(AbstractAggregateEncoder.ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		return new BasicAtom(headPredicate, aggregateArguments, elementTuple, element.getElementTerms().get(0));
	}

}
