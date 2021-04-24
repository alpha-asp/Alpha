package at.ac.tuwien.kr.alpha.api.programs.literals;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;

public interface AggregateLiteral extends ASPCore2Literal {

	@Override
	AggregateAtom getAtom();
	
}
