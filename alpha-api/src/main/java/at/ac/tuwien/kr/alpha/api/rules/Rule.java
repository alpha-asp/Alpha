package at.ac.tuwien.kr.alpha.api.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;

public interface Rule<H extends Head, L extends Literal> {

	H getHead();

	Set<L> getBody();

	boolean isConstraint();
	
	// TODO clean up programs/rules mess
	Atom getHeadAtom();
	
	Set<L> getPositiveBody();
	
	Set<L> getNegativeBody();

}
