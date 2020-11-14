package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.ListUtils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.EnumerationRewriting;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public abstract class StringtemplateBasedAggregateEncoder extends AbstractAggregateEncoder {

	private final ProgramParser parser = new ProgramParser();
	private final ST encodingTemplate;
	private final boolean needsBoundRule;

	protected StringtemplateBasedAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, ComparisonOperator acceptedOperator, ST encodingTemplate) {
		super(aggregateFunctionToEncode, Collections.singleton(acceptedOperator));
		this.encodingTemplate = encodingTemplate;
		if (acceptedOperator == ComparisonOperator.EQ) {
			this.needsBoundRule = false;
		} else if (acceptedOperator == ComparisonOperator.LE) {
			this.needsBoundRule = true;
		} else {
			throw new IllegalArgumentException("This encoder is incompatible with comparision operator: " + acceptedOperator);
		}
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		
		// Generate encoding
		ST coreEncodingTemplate = new ST(this.encodingTemplate);
		coreEncodingTemplate.add("result_predicate", aggregateToEncode.getOutputAtom().getPredicate().getName());
		coreEncodingTemplate.add("id", aggregateId);
		coreEncodingTemplate.add("element_tuple", this.getElementTuplePredicateSymbol(aggregateId));
		String coreEncodingAsp = coreEncodingTemplate.render();

		// Create the basic program
		InputProgram coreEncoding = new EnumerationRewriting().apply(parser.parse(coreEncodingAsp));

		if (this.needsBoundRule) {
			BasicRule boundRule = this.buildBoundRule(aggregateToEncode, ctx);
			// Combine core encoding and bound rule
			return new InputProgram(ListUtils.union(coreEncoding.getRules(), Collections.singletonList(boundRule)), coreEncoding.getFacts(),
					new InlineDirectives());
		} else {
			return coreEncoding;
		}
	}

	private BasicRule buildBoundRule(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		BasicAtom bound = new BasicAtom(Predicate.getInstance(aggregateToEncode.getId() + "_bound", 2),
				aggregateToEncode.getAggregateArguments(), aggregateToEncode.getLiteral().getAtom().getLowerBoundTerm());
		return new BasicRule(new NormalHead(bound), new ArrayList<>(ctx.getDependencies(aggregateToEncode.getId())));
	}

}
