package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewriting;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingConfig;

/**
 * Encapsulates all transformations necessary to transform a given program into a @{link NormalProgram} that is
 * understood by Alpha internally
 * 
 * Copyright (c) 2019-2020, the Alpha Team.
 */
public class NormalizeProgramTransformation extends ProgramTransformation<InputProgram, NormalProgram> {

	private boolean useNormalizationGrid;

	public NormalizeProgramTransformation(boolean useNormalizationGrid) {
		this.useNormalizationGrid = useNormalizationGrid;
	}

	@Override
	public NormalProgram apply(InputProgram inputProgram) {
		InputProgram tmpPrg;
		// Transform choice rules.
		tmpPrg = new ChoiceHeadToNormal().apply(inputProgram);
		// Transform cardinality aggregates.
		// TODO do config properly
		AggregateRewritingConfig aggCfg = new AggregateRewritingConfig(true);
		tmpPrg = new AggregateRewriting(aggCfg).apply(tmpPrg);
		// Transform enumeration atoms.
		tmpPrg = new EnumerationRewriting().apply(tmpPrg);
		EnumerationAtom.resetEnumerations();

		// Construct the normal program.
		NormalProgram retVal = NormalProgram.fromInputProgram(tmpPrg);
		// Transform intervals - CAUTION - this MUST come before VariableEqualityRemoval!
		retVal = new IntervalTermToIntervalAtom().apply(retVal);
		// Remove variable equalities.
		// TODO move this up, needs to happen before aggregate rewriting! (do we need to do this twice in case any subsequent
		// transformation produces such equalitites?)
		retVal = new VariableEqualityRemoval().apply(retVal);
		return retVal;
	}

}
