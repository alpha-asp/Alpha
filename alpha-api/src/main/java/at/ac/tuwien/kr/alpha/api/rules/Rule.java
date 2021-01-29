package at.ac.tuwien.kr.alpha.api.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.program.Atom;
import at.ac.tuwien.kr.alpha.api.program.Literal;

public interface Rule<H extends Head> {

	H getHead();

	Set<Literal> getBody();

	boolean isConstraint();
	
	// TODO clean up programs/rules mess
	Atom getHeadAtom();
}
