package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.parser.InlineDirectivesImpl;
import at.ac.tuwien.kr.alpha.core.programs.InputProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.PredicateInternalizer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

/**
 * Abstract base class for aggregate encoders. An aggregate encoder provides an encoding for a given aggregate literal,
 * i.e. it creates an ASP program that is semantically equivalent to the given literal.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public abstract class AbstractAggregateEncoder {

	protected static final String ELEMENT_TUPLE_FUNCTION_SYMBOL = "tuple";

	private final AggregateFunctionSymbol aggregateFunctionToEncode;
	private final Set<ComparisonOperator> acceptedOperators;

	protected AbstractAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, Set<ComparisonOperator> acceptedOperators) {
		this.aggregateFunctionToEncode = aggregateFunctionToEncode;
		this.acceptedOperators = acceptedOperators;
	}

	/**
	 * Encodes all aggregate literals in the given set of aggregate referenced by the given {@link AggregateInfo}.
	 * 
	 * @param aggregatesToEncode the aggregates to encode.
	 * @return all rules encoding the given aggregates as an {@link InputProgram}.
	 */
	public InputProgram encodeAggregateLiterals(Set<AggregateInfo> aggregatesToEncode) {
		InputProgramImpl.Builder programBuilder = InputProgramImpl.builder();
		for (AggregateInfo aggregateInfo : aggregatesToEncode) {
			programBuilder.accumulate(encodeAggregateLiteral(aggregateInfo));
		}
		return programBuilder.build();
	}

	/**
	 * Encodes the aggregate literal referenced by the given {@link AggregateInfo}.
	 * 
	 * @param aggregateToEncode
	 * @return
	 */
	public InputProgram encodeAggregateLiteral(AggregateInfo aggregateToEncode) {
		AggregateLiteral literalToEncode = aggregateToEncode.getLiteral();
		if (literalToEncode.getAtom().getAggregateFunction() != this.aggregateFunctionToEncode) {
			throw new IllegalArgumentException(
					"Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function " + literalToEncode.getAtom().getAggregateFunction());
		}
		if (!this.acceptedOperators.contains(literalToEncode.getAtom().getLowerBoundOperator())) {
			throw new IllegalArgumentException("Encoder " + this.getClass().getSimpleName() + " cannot encode aggregate function "
					+ literalToEncode.getAtom().getAggregateFunction() + " with operator " + literalToEncode.getAtom().getLowerBoundOperator());
		}
		String aggregateId = aggregateToEncode.getId();
		InputProgram literalEncoding = PredicateInternalizer.makePrefixedPredicatesInternal(encodeAggregateResult(aggregateToEncode), aggregateId);
		List<Rule<Head>> elementEncodingRules = new ArrayList<>();
		for (AggregateElement elementToEncode : literalToEncode.getAtom().getAggregateElements()) {
			Rule<Head> elementRule = encodeAggregateElement(aggregateToEncode, elementToEncode);
			elementEncodingRules.add(PredicateInternalizer.makePrefixedPredicatesInternal(elementRule, aggregateId));
		}
		return new InputProgramImpl(ListUtils.union(literalEncoding.getRules(), elementEncodingRules), literalEncoding.getFacts(), new InlineDirectivesImpl());
	}

	/**
	 * Encodes the "core" logic of an aggregate literal, i.e. rules that work on element tuples. Element tuples are derived
	 * by each aggregate element (see {@link AbstractAggregateEncoder#encodeAggregateElement}) and represent the values that
	 * are being aggregated.
	 * 
	 * @param aggregateToEncode
	 * @return
	 */
	protected abstract InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode);

	/**
	 * Encodes individual aggregate elements. For each aggregate element, a rule is created that fires for each tuple matching the element.
	 * 
	 * @param aggregateInfo
	 * @param element
	 * @return
	 */
	protected Rule<Head> encodeAggregateElement(AggregateInfo aggregateInfo, AggregateElement element) {
		BasicAtom headAtom = buildElementRuleHead(aggregateInfo.getId(), element, aggregateInfo.getAggregateArguments());
		return Rules.newRule(Heads.newNormalHead(headAtom),
				SetUtils.union(new LinkedHashSet<>(element.getElementLiterals()), aggregateInfo.getDependencies()));
	}

	/**
	 * Builds a the head atom for an aggregate element encoding rule of form
	 * <code>HEAD :- $element_literals$, $aggregate_dependencies$</code>, e.g.
	 * <code>count_1_element_tuple(count_1_args(Y), X) :- p(X, Y), q(Y)</code> for the rule body
	 * <code>N = #count{X : p(X, Y)}, q(Y)</code>.
	 * 
	 * @param aggregateId
	 * @param element
	 * @param aggregateArguments
	 * @return
	 */
	protected BasicAtom buildElementRuleHead(String aggregateId, AggregateElement element, Term aggregateArguments) {
		Predicate headPredicate = Predicates.getPredicate(this.getElementTuplePredicateSymbol(aggregateId), 2);
		FunctionTerm elementTuple = Terms.newFunctionTerm(ELEMENT_TUPLE_FUNCTION_SYMBOL, element.getElementTerms());
		return Atoms.newBasicAtom(headPredicate, aggregateArguments, elementTuple);
	}

	protected String getElementTuplePredicateSymbol(String aggregateId) {
		return aggregateId + "_element_tuple";
	}

	public AggregateFunctionSymbol getAggregateFunctionToEncode() {
		return this.aggregateFunctionToEncode;
	}

	public Set<ComparisonOperator> getAcceptedOperators() {
		return this.acceptedOperators;
	}

}
