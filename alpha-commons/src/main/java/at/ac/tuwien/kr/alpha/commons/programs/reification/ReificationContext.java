package at.ac.tuwien.kr.alpha.commons.programs.reification;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IdGenerator;

class ReificationContext implements IdGenerator<ConstantTerm<?>>{

	private final IdGenerator<ConstantTerm<?>> idGenerator;
	private final Map<Predicate, ConstantTerm<?>> predicateTable = new HashMap<>();

	private final Set<BasicAtom> reifiedItems = new LinkedHashSet<>();

	ReificationContext(IdGenerator<ConstantTerm<?>> idGenerator) {
		this.idGenerator = idGenerator;
	}

	ConstantTerm<?> computePredicateId(Predicate predicate) {
		return predicateTable.computeIfAbsent(predicate, (pred) -> idGenerator.getNextId());		
	}

	@Override
	public ConstantTerm<?> getNextId() {
		return idGenerator.getNextId();
	}

	void addAtom(BasicAtom item) {
		reifiedItems.add(item);
	}

	private Set<BasicAtom> generatePredicateAtoms() {
		Set<BasicAtom> predicateAtoms = new LinkedHashSet<>();
		for (Map.Entry<Predicate, ConstantTerm<?>> entry : predicateTable.entrySet()) {
			predicateAtoms.add(Atoms.newBasicAtom(Reifier.PREDICATE, entry.getValue(), Terms.newConstant(entry.getKey().getName()),
					Terms.newConstant(entry.getKey().getArity())));
		}
		return predicateAtoms;
	}

	Set<BasicAtom> computeReifiedProgram() {
		Set<BasicAtom> reifiedProgram = new LinkedHashSet<>(reifiedItems);
		reifiedProgram.addAll(generatePredicateAtoms());
		return reifiedProgram;
	}

}
