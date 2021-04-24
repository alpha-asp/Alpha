package at.ac.tuwien.kr.alpha.api.programs;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;

public interface Program<R extends Rule<? extends Head, ? extends Literal>> {

	List<Atom> getFacts();

	InlineDirectives getInlineDirectives();

	List<R> getRules();

}
