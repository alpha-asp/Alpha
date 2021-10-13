package at.ac.tuwien.kr.alpha.grounder.transformation;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.externals.Externals;
import at.ac.tuwien.kr.alpha.api.externals.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Terms;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Copyright (c) 2021, the Alpha Team.
 */
public class ArithmeticTermsRewritingTest {

	private final Map<String, PredicateInterpretation> externalsOfThisClass = Externals.scan(ArithmeticTermsRewritingTest.class);
	private final ProgramParser parser = new ProgramParser(externalsOfThisClass);	// Create parser that knows an implementation of external atom &extArithTest[]().

	@Predicate(name = "extArithTest")
	public static Set<List<ConstantTerm<Integer>>> externalForArithmeticTermsRewriting(Integer in) {
		List<ConstantTerm<Integer>> terms = Terms.asTermList(
			in * 314);
		return Collections.singleton(terms);
	}

	@Test
	public void rewriteRule() {
		NormalProgram inputProgram = NormalProgram.fromInputProgram(parser.parse("p(X+1) :- q(Y/2), r(f(X*2),Y), X-2 = Y*3, X = 0..9."));
		assertEquals(1, inputProgram.getRules().size());
		ArithmeticTermsRewriting arithmeticTermsRewriting = new ArithmeticTermsRewriting();
		NormalProgram rewrittenProgram = arithmeticTermsRewriting.apply(inputProgram);
		// Expect the rewritten program to be one rule with: p(_A0) :- _A0 = X+1,  _A1 = Y/2, q(_A1), _A2 = X*2, r(f(_A2),Y), X-2 = Y*3, X = 0..9.
		assertEquals(1, rewrittenProgram.getRules().size());
		NormalRule rewrittenRule = rewrittenProgram.getRules().get(0);
		assertTrue(rewrittenRule.getHeadAtom().getTerms().get(0) instanceof VariableTerm);
		assertEquals(7, rewrittenRule.getBody().size());
	}

	@Test
	public void rewriteExternalAtom() {
		NormalProgram inputProgram = NormalProgram.fromInputProgram(parser.parse("p :- Y = 13, &extArithTest[Y*5](Y-4)."));
		assertEquals(1, inputProgram.getRules().size());
		ArithmeticTermsRewriting arithmeticTermsRewriting = new ArithmeticTermsRewriting();
		NormalProgram rewrittenProgram = arithmeticTermsRewriting.apply(inputProgram);
		assertEquals(1, rewrittenProgram.getRules().size());
		NormalRule rewrittenRule = rewrittenProgram.getRules().get(0);
		assertEquals(4, rewrittenRule.getBody().size());
		List<Literal> externalLiterals = rewrittenRule.getBody().stream().filter(lit -> lit instanceof ExternalLiteral).collect(toList());
		assertEquals(1, externalLiterals.size());
		ExternalAtom rewrittenExternal = ((ExternalLiteral) externalLiterals.get(0)).getAtom();
		assertEquals(1, rewrittenExternal.getInput().size());
		assertTrue(rewrittenExternal.getInput().get(0) instanceof VariableTerm);
		assertEquals(1, rewrittenExternal.getOutput().size());
		assertTrue(rewrittenExternal.getOutput().get(0) instanceof VariableTerm);
	}

}