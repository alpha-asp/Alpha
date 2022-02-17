package at.ac.tuwien.kr.alpha.api.programs.literals;

import java.util.function.Function;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;

public interface AggregateLiteral extends Literal {

	@Override
	AggregateAtom getAtom();
	
	AggregateLiteral renameVariables(Function<String, String> mapping);

}
