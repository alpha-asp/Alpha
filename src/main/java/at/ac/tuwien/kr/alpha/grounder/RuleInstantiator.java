package at.ac.tuwien.kr.alpha.grounder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;

/**
 * Provides ground instantiations for rules.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class RuleInstantiator {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleInstantiator.class);

	// TODO currentlyKnownPositiveInstances should be a "window into working memory" -> think of something!
	public List<Substitution> instantiateLiteral(Literal lit, Substitution partialSubstitution, Object currentlyKnownPositiveInstances){
		return null;
	}
}
