package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import java.util.Collections;
import java.util.List;

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
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.AggregateRewritingContext.AggregateInfo;

public class CountEqualsAggregateEncoder extends AbstractAggregateEncoder {

	private static final String ELEMENT_TUPLE_FUNCTION_SYMBOL = "tuple";
	
	//@formatter:off
	private static final ST CNT_EQ_LITERAL_ENCODING = Util.aspStringTemplate(
				"#enumeration_predicate_is $enumeration$."
				+ "$aggregate_result$(VAL) :- $leq$(VAL), not $leq$(NEXTVAL), NEXTVAL = VAL + 1."
				+ "$leq$($value_var$) :- $value_leq_cnt_lit$, $cnt_candidate_lit$."
				+ "$cnt_candidate$(ORDINAL) :- $element_tuple$(TUPLE), $enumeration$($aggregate_id$, TUPLE, ORDINAL).");
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	public CountEqualsAggregateEncoder() {
		super(AggregateFunctionSymbol.COUNT, SetUtils.hashSet(ComparisonOperator.EQ));
	}

	@Override
	protected List<BasicRule> encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();
		AggregateAtom sourceAtom = lit.getAtom();
		VariableTerm valueVar = VariableTerm.getInstance("CNT");
		// Build a new AggregateLiteral representing the "CNT <= #count{...}" part of the encoding.
		AggregateLiteral candidateLeqCount = new AggregateLiteral(
				new AggregateAtom(ComparisonOperator.LE, valueVar, AggregateFunctionSymbol.COUNT, sourceAtom.getAggregateElements()), true);
		String cntCandidatePredicateSymbol = aggregateId + "_candidate";
		BasicLiteral cntCandidate = new BasicLiteral(new BasicAtom(Predicate.getInstance(cntCandidatePredicateSymbol, 1), valueVar), true);
		// Create an encoder for the newly built " <= #count{..}" literal and rewrite it.
		// Note that the literal itself is not written into the encoding of the original literal,
		// but only its substitute "aggregate result" literal.
		AggregateRewritingContext candidateLeqCountCtx = new AggregateRewritingContext(ctx);
		String candidateLeqCntId = candidateLeqCountCtx.registerAggregateLiteral(candidateLeqCount, Collections.singleton(cntCandidate));
		// The encoder won't encode AggregateElements of the newly created literal separately but alias them
		// with the element encoding predicates for the original literal.
		AbstractAggregateEncoder candidateLeqEncoder = new CountLessOrEqualDelegateAggregateEncoder(aggregateId);
		List<BasicRule> candidateLeqEncoding = candidateLeqEncoder
				.encodeAggregateLiteral(candidateLeqCountCtx.getAggregateInfo(candidateLeqCntId), candidateLeqCountCtx);
		// Create a fresh template to make sure attributes are empty at each call to encodeAggregateResult.
		ST encodingTemplate = new ST(CNT_EQ_LITERAL_ENCODING);
		encodingTemplate.add("aggregate_result", aggregateToEncode.getLiteral().getPredicate().getName());
		encodingTemplate.add("leq", aggregateId + "_leq");
		encodingTemplate.add("cnt_candidate", cntCandidatePredicateSymbol);
		encodingTemplate.add("value_var", valueVar.toString());
		encodingTemplate.add("value_leq_cnt_lit", candidateLeqCountCtx.getAggregateInfo(candidateLeqCntId).getOutputAtom().toString());
		encodingTemplate.add("cnt_candidate_lit", cntCandidate.toString());
		encodingTemplate.add("element_tuple", aggregateId + "_element_tuple");
		encodingTemplate.add("enumeration", aggregateId + "_enum");
		encodingTemplate.add("aggregate_id", aggregateId);
		String resultEncodingAsp = encodingTemplate.render();
		InputProgram resultEncoding = new EnumerationRewriting().apply(parser.parse(resultEncodingAsp));
		return ListUtils.union(resultEncoding.getRules(), candidateLeqEncoding);
	}

	@Override
	protected BasicRule encodeAggregateElement(String aggregateId, AggregateElement element) {
		FunctionTerm elementTuple = FunctionTerm.getInstance(ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		Atom headAtom = new BasicAtom(Predicate.getInstance(aggregateId + "_element_tuple", 1), elementTuple);
		return new BasicRule(new NormalHead(headAtom), element.getElementLiterals());
	}

}
