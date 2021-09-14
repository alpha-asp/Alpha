package at.ac.tuwien.kr.alpha.api.rules;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;

public interface Rule<H extends Head> {

	H getHead();

	Set<Literal> getBody();

	boolean isConstraint();
	
	Set<Literal> getPositiveBody();
	
	Set<Literal> getNegativeBody();
}
