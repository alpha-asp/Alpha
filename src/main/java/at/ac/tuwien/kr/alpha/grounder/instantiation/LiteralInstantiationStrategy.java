package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

public interface LiteralInstantiationStrategy {

	AssignmentStatus getTruthForGroundLiteral(Literal groundLiteral);
	
	List<ImmutablePair<Substitution, AssignmentStatus>> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution);

}
