package at.ac.tuwien.kr.alpha.api.program;

import java.util.Set;

import at.ac.tuwien.kr.alpha.api.rules.Head;
import at.ac.tuwien.kr.alpha.api.rules.Rule;

public interface Program {

	Set<? extends Atom> getFacts();

	Set<Rule<? extends Head>> getRules();

}
