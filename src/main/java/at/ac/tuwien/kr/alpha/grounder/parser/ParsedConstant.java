package at.ac.tuwien.kr.alpha.grounder.parser;

/**
 * Created by Antonius Weinzierl on 7/5/16.
 */
public class ParsedConstant extends ParsedTerm {
	public String content;

	enum TYPE {STRING, NUMBER, CONSTANT}

	public TYPE type;

	@Override
	public String toString() {
		return content;
	}
}
