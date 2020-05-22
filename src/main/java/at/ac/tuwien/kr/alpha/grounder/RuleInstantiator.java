package at.ac.tuwien.kr.alpha.grounder;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
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

	// FIXME we should find a global contract for when to call "substitute(substitution)" on a literal
	
	// TODO currentlyKnownPositiveInstances should be a "window into working memory" -> think of something!
	// TODO think about method name
	// Method contract: literal is already substituted with given partialSubstitution
	// Method contract: knownInstances is an instance storage containing all instances to consider when grounding givewn literal,
	// supposed to be null for FixedInterpretationLiteral and EnumerationLiteral
	public List<Substitution> instantiateLiteral(Literal lit, Substitution partialSubstitution, IndexedInstanceStorage knownInstances){
		List<Substitution> substitutions;
		if(lit instanceof FixedInterpretationLiteral) {
			FixedInterpretationLiteral fixedInterpretationLiteral = (FixedInterpretationLiteral) lit;
			substitutions = fixedInterpretationLiteral.getSatisfyingSubstitutions(partialSubstitution);
		} else if(lit instanceof EnumerationLiteral) {
			// TODO getSatisfyingSubstitutions from enum
			// Idea:
			// - substitute literal
			// - in EnumLiteral#getSatisfyingSubstitutions: if substituted literal is SAT, add other SAT substitutions,
			//   else, return empty
		} else {
			// lit seems to be a basic literal, so its satisfiability w.r.t. partialSubstitution
			// is decided based on knownInstances by the instantiationStrategy
			if(this.instantiationStrategy.acceptSubstitutedLiteral(lit, knownInstances)) {
				substitutions = Collections.singletonList(partialSubstitution);
			}else {
				substitutions = Collections.emptyList();
			}
		}
		return substitutions;
	}
}
