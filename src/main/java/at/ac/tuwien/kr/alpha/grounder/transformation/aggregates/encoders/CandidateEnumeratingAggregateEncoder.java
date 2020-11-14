package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

public abstract class CandidateEnumeratingAggregateEncoder extends AbstractAggregateEncoder {

	private final ProgramParser parser = new ProgramParser();

	private final ST coreEncodingTemplate;

	/*
	 * isEmbeddedEncoder and inputNumberSourceAtom are only set when creating an "embedded" encoder that is used by a
	 * CountEqualsAggregateEncoder to generate a sorting grid encoding as part of another encoding.
	 */
	private final boolean isEmbeddedEncoder;
	private final BasicAtom candidateSource;

	public CandidateEnumeratingAggregateEncoder(ST coreEncodingTemplate, AggregateFunctionSymbol function, ComparisonOperator... operators) {
		super(function, SetUtils.hashSet(operators));
		this.isEmbeddedEncoder = false;
		this.candidateSource = null;
		this.coreEncodingTemplate = coreEncodingTemplate;
	}

	private CandidateEnumeratingAggregateEncoder(ST coreEncodingTemplate, BasicAtom inputNumberSource, AggregateFunctionSymbol function,
			ComparisonOperator... operators) {
		super(function, SetUtils.hashSet(operators));
		this.isEmbeddedEncoder = true;
		this.candidateSource = inputNumberSource;
		this.coreEncodingTemplate = coreEncodingTemplate;
	}

	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		String aggregateId = aggregateToEncode.getId();
		AggregateLiteral lit = aggregateToEncode.getLiteral();

		// Generate sorting grid core encoding
		ST coreEncodingTemplate = new ST(this.coreEncodingTemplate);
		coreEncodingTemplate.add("result_predicate", aggregateToEncode.getOutputAtom().getPredicate().getName());
		coreEncodingTemplate.add("id", aggregateId);
		coreEncodingTemplate.add("candidate", this.getCandidatePredicateSymbol(aggregateId));
		String coreEncodingAsp = coreEncodingTemplate.render();

		// Create the basic sorting network program
		InputProgram coreEncoding = parser.parse(coreEncodingAsp);

		// Generate bound rule
		BasicAtom bound = new BasicAtom(Predicate.getInstance(aggregateId + "_bound", 2),
				aggregateToEncode.getAggregateArguments(), lit.getAtom().getLowerBoundTerm());
		BasicRule boundRule = new BasicRule(new NormalHead(bound), new ArrayList<>(ctx.getDependencies(aggregateId)));

		// Generate input number rule
		BasicRule candidateRule = this.createCandidateRule(aggregateToEncode);

		List<BasicRule> additionalRules = new ArrayList<>();
		additionalRules.add(boundRule);
		additionalRules.add(candidateRule);
		return new InputProgram(ListUtils.union(coreEncoding.getRules(), additionalRules), coreEncoding.getFacts(),
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

	protected String getCandidatePredicateSymbol(String aggregateId) {
		return aggregateId + "_candidate";
	}

	protected abstract BasicRule createCandidateRule(AggregateInfo aggregateToEncode);

	protected boolean isEmbeddedEncoder() {
		return this.isEmbeddedEncoder;
	}

	protected BasicAtom getCandidateSource() {
		return this.candidateSource;
	}

}
