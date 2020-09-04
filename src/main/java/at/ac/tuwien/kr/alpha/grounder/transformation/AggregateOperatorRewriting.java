package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public class AggregateOperatorRewriting extends ProgramTransformation<InputProgram, InputProgram> {

	private final AggregateRewritingConfig config;

	public AggregateOperatorRewriting(AggregateRewritingConfig config) {
		this.config = config;
	}

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		// TODO this is just debugging
		for (BasicRule rule : inputProgram.getRules()) {
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					System.out.println("Found an aggregate in rule : " + rule);
					AggregateLiteral aggLit = (AggregateLiteral) lit;
					System.out.println("Aggregate func = " + aggLit.getAtom().getAggregatefunction());
					System.out.println("Aggregate vars = " + Util.join("{", aggLit.getAtom().getAggregateVariables(), "}"));
					System.out.println("Aggregate elems = " + Util.join("{", aggLit.getAtom().getAggregateElements(), "}"));
					for (AggregateElement aggElem : aggLit.getAtom().getAggregateElements()) {
						System.out.println("Aggregate Element " + aggElem);
						System.out.println("ElementTerms = " + Util.join("{", aggElem.getElementTerms(), "}"));
						System.out.println("ElementLiterals = " + Util.join("{", aggElem.getElementLiterals(), "}"));
					}
				}
			}
		}
		return inputProgram;
	}

}
