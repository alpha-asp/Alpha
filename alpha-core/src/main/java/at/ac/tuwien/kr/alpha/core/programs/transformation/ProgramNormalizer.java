package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.core.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateTransformer;

/**
 * Encapsulates all transformations necessary to transform a given program into a @{link NormalProgram} that is understood by Alpha internally
 * 
 * Copyright (c) 2019-2021, the Alpha Team.
 */
public class ProgramNormalizer extends ProgramTransformer<InputProgram, NormalProgram> {

	private final VariableEqualityTransformer equalityTransformer;
	private final ChoiceHeadNormalizer choiceHeadNormalizer;
	private final AggregateTransformer aggregateTransformer;
	private final EnumerationTransformer enumerationTransformer;
	private final IntervalTermTransformer intervalTermTransformer;
	private final ArithmeticTermTransformer arithmeticTermTransformer;

	public ProgramNormalizer(VariableEqualityTransformer equalityTransformer, ChoiceHeadNormalizer choiceHeadNormalizer, AggregateTransformer aggregateTransformer, EnumerationTransformer enumerationTransformer, IntervalTermTransformer intervalTermTransformer, ArithmeticTermTransformer arithmeticTermTransformer) {
		this.equalityTransformer = equalityTransformer;
		this.choiceHeadNormalizer = choiceHeadNormalizer;
		this.aggregateTransformer = aggregateTransformer;
		this.enumerationTransformer = enumerationTransformer;
		this.intervalTermTransformer = intervalTermTransformer;
		this.arithmeticTermTransformer = arithmeticTermTransformer;
	}

	@Override
	public NormalProgram transform(InputProgram inputProgram) {
		InputProgram tmpPrg;
		// Remove variable equalities.
		tmpPrg = equalityTransformer.transform(inputProgram);
		// Transform choice rules.
		tmpPrg = choiceHeadNormalizer.transform(tmpPrg);
		// Transform aggregates.
		tmpPrg = aggregateTransformer.transform(tmpPrg);
		// Transform enumeration atoms.
		tmpPrg = enumerationTransformer.transform(tmpPrg);
		EnumerationAtom.resetEnumerations();

		// Construct the normal program.
		NormalProgram retVal = NormalProgramImpl.fromInputProgram(tmpPrg);
		// Transform intervals.
		retVal = intervalTermTransformer.transform(retVal);
		// Rewrite ArithmeticTerms.
		retVal = arithmeticTermTransformer.transform(retVal);
		return retVal;
	}

}
