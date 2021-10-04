package at.ac.tuwien.kr.alpha.core.programs.transformation;

import java.util.function.Supplier;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramTransformation;
import at.ac.tuwien.kr.alpha.core.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewriting;

/**
 * Encapsulates all transformations necessary to transform a given program into a @{link NormalProgram} that is understood by Alpha
 * internally
 * 
 * Copyright (c) 2019-2021, the Alpha Team.
 */
public class NormalizeProgramTransformation extends ProgramTransformation<InputProgram, NormalProgram> {

	private final Supplier<AggregateRewriting> aggregateRewritingFactory;

	public NormalizeProgramTransformation(Supplier<AggregateRewriting> aggregateRewritingFactory) {
		this.aggregateRewritingFactory = aggregateRewritingFactory;
	}

	@Override
	public NormalProgram apply(InputProgram inputProgram) {
		InputProgram tmpPrg;
		// Remove variable equalities.
		tmpPrg = new VariableEqualityRemoval().apply(inputProgram);
		// Transform choice rules.
		tmpPrg = new ChoiceHeadToNormal().apply(tmpPrg);
		// Transform aggregates.
		tmpPrg = aggregateRewritingFactory.get().apply(tmpPrg);
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
