package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.core.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.transformation.ArithmeticTermsRewriting;

/**
 * Encapsulates all transformations necessary to transform a given program into a @{link NormalProgram} that is understood by Alpha internally
 * 
 * Copyright (c) 2019-2021, the Alpha Team.
 */
public class NormalizeProgramTransformation extends ProgramTransformation<ASPCore2Program, NormalProgram> {

	private final AggregateRewritingConfig aggregateRewritingCfg;

	public NormalizeProgramTransformation(AggregateRewritingConfig aggregateCfg) {
		this.aggregateRewritingCfg = aggregateCfg;
	}

	@Override
	public NormalProgram apply(ASPCore2Program inputProgram) {
		ASPCore2Program tmpPrg;
		// Remove variable equalities.
		tmpPrg = new VariableEqualityRemoval().apply(inputProgram);
		// Transform choice rules.
		tmpPrg = new ChoiceHeadToNormal().apply(tmpPrg);
		// Transform aggregates.
		tmpPrg = new AggregateRewriting(aggregateRewritingCfg.isUseSortingGridEncoding(), aggregateRewritingCfg.isSupportNegativeValuesInSums()).apply(tmpPrg);
		// Transform enumeration atoms.
		tmpPrg = new EnumerationRewriting().apply(tmpPrg);
		EnumerationAtom.resetEnumerations();

		// Construct the normal program.
		NormalProgram retVal = NormalProgramImpl.fromInputProgram(tmpPrg);
		// Transform intervals
		retVal = new IntervalTermToIntervalAtom().apply(retVal);
		// Rewrite ArithmeticTerms.
		retVal = new ArithmeticTermsRewriting().apply(retVal);
		return retVal;
	}

}
