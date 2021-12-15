package at.ac.tuwien.kr.alpha.commons.programs;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;

public class ReificationHelper {

	private final Supplier<ConstantTerm<?>> idProvider;

	public ReificationHelper(Supplier<ConstantTerm<?>> idProvider) {
		this.idProvider = idProvider;
	}

	public Set<BasicAtom> reifyProgram(ASPCore2Program program) {
		Set<BasicAtom> reified = new LinkedHashSet<>();
		for (Rule<? extends Head> rule : program.getRules()) {
			reified.addAll(reifyRule(rule));
		}
		return reified;
	}

	public Set<BasicAtom> reifyRule(Rule<? extends Head> rule) {
		Set<BasicAtom> reified = new LinkedHashSet<>();
		reified.addAll(reifyHead(rule.getHead()));
		for (Literal lit : rule.getBody()) {
			reified.addAll(reifyLiteral(lit));
		}
		return null;
	}

	public Set<BasicAtom> reifyHead(Head head) {
		// TODO
		return null;
	}

	public Set<BasicAtom> reifyLiteral(Literal lit) {
		// TODO
		return null;
	}

	public Set<BasicAtom> reifyAtom(Atom atom) {
		// TODO
		return null;
	}

	public Set<BasicAtom> reifyTerm(Term term) {
		// TODO
		return null;
	}

}
