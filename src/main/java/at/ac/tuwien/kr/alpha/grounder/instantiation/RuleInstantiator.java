package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationLiteral;

/**
 * Provides ground instantiations for rules.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class RuleInstantiator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleInstantiator.class);

	private final InstantiationStrategy instantiationStrategy;

	public RuleInstantiator(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}

	// FIXME we should find a global contract for when to call
	// "substitute(substitution)" on a literal

	// TODO think about method name
	// Method contract: literal is NOT substituted with given partialSubstitution
	// Method contract: knownInstances is an instance storage containing all
	// instances to consider when grounding given literal,
	// supposed to be null for FixedInterpretationLiteral and EnumerationLiteral
	public LiteralInstantiationResult instantiateLiteral(Literal lit, Substitution partialSubstitution, InstanceStorageView knownInstances) {
		LOGGER.trace("Instantiating literal: {}", lit);
		LiteralInstantiationResult retVal;
		List<Substitution> substitutions;
		if (lit instanceof FixedInterpretationLiteral) {
			FixedInterpretationLiteral substitutedFixedInterpretationLiteral = (FixedInterpretationLiteral) lit.substitute(partialSubstitution);
			// TODO check if we should push back
			substitutions = substitutedFixedInterpretationLiteral.getSatisfyingSubstitutions(partialSubstitution);
			retVal = substitutions.isEmpty() ? LiteralInstantiationResult.stopBinding() : LiteralInstantiationResult.continueBinding(substitutions);
		} else if (lit instanceof EnumerationLiteral) {
			EnumerationLiteral enumerationLiteral = (EnumerationLiteral) lit;
			retVal = LiteralInstantiationResult.continueBinding(
					Collections.singletonList(enumerationLiteral.addEnumerationIndexToSubstitution(partialSubstitution)));
		} else {
			Literal substitutedLiteral = lit.substitute(partialSubstitution);
			if (substitutedLiteral.isGround()) {
				//@formatter:off
				// lit seems to be a basic literal, so its satisfiability w.r.t. partialSubstitution
				// is decided based on knownInstances by the instantiationStrategy
				retVal = this.instantiationStrategy.acceptSubstitutedLiteral(substitutedLiteral, knownInstances) ? 
						LiteralInstantiationResult.continueBinding(Collections.singletonList(partialSubstitution)) 
						: LiteralInstantiationResult.stopBinding();
				//@formatter:on
			} else {
				// instantiate literal based on instantiation strategy
				substitutions = this.instantiationStrategy.getAcceptedSubstitutions(substitutedLiteral, partialSubstitution, knownInstances);
				retVal = substitutions.isEmpty() ? LiteralInstantiationResult.stopBinding() : LiteralInstantiationResult.continueBinding(substitutions);
			}
		}
		return retVal;
	}
}
