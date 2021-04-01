package at.ac.tuwien.kr.alpha.api.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.Literal;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;

public interface Rule<H extends Head> {

	H getHead();

	Set<Literal> getBody();

	boolean isConstraint();
	
	// TODO clean up programs/rules mess
	Atom getHeadAtom();
	
	Set<Literal> getPositiveBody();
	
	Set<Literal> getNegativeBody();
}
