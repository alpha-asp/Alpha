package at.ac.tuwien.kr.alpha.api.impl;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.core.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.core.parser.ParseTreeVisitor;

// TODO this class should be called something different since it's turning into a general "atom set snippet parser"
// TODO this is duplicated from core module, need to pull out test utils into separate testsupport module
public class AnswerSetsParser {
	
	private static final ParseTreeVisitor VISITOR = new ParseTreeVisitor(Collections.emptyMap(), false);

	public static Set<AnswerSet> parse(String s) {
		try {
			return parse(CharStreams.fromString(s));
		} catch (RecognitionException | ParseCancellationException e) {
			// If there were issues parsing the given string, we
			// throw something that suggests that the input string
			// is malformed.
			throw new IllegalArgumentException("Could not parse answer sets.", e);
		}
	}

	public static Set<AnswerSet> parse(CharStream stream) {
		final ASPCore2Parser parser = new ASPCore2Parser(new CommonTokenStream(new ASPCore2Lexer(stream)));

		// Try SLL parsing mode (faster but may terminate incorrectly).
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.removeErrorListeners();
		parser.setErrorHandler(new BailErrorStrategy());

		return VISITOR.translate(parser.answer_sets());
	}

	public static Set<BasicAtom> parseAtoms(String s) {
		Set<AnswerSet> tmp = parse("{" + s + "}");
		if(tmp.size() > 1) {
			throw new IllegalArgumentException("Cannot parse multiset of atoms! Use answer set parsing methods instead!");
		}
		if(tmp.isEmpty()) {
			return Collections.emptySet();
		}
		AnswerSet as = tmp.stream().findFirst().get();
		Set<BasicAtom> atoms = as.getPredicates()
			.stream()
				.map(
						(pred) -> as.getPredicateInstances(pred).stream()
							.map((atom) -> {
								if(!(atom instanceof BasicAtom)) {
									throw new IllegalArgumentException("Expected a BasicAtom, but got: " + atom);
								}
								return (BasicAtom) atom;
							}).collect(Collectors.toSet())
				)
				.reduce(new TreeSet<BasicAtom>(), (result, element) -> {
					result.addAll(element);
					return result;
				});
		return atoms;
	}
}
