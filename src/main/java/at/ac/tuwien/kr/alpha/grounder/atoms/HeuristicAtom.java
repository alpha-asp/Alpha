/**
 * Copyright (c) 2017-2018, the Alpha Team.
 * All rights reserved.
 *
 * Additional changes made by Siemens.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common.atoms;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Lexer;
import at.ac.tuwien.kr.alpha.antlr.ASPCore2Parser;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.*;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * TODO: move to package at.ac.tuwien.kr.alpha.grounder.atoms? (because since heuristic atoms are not allowed in input programs anymore, this is a purely internal atom)
 * TODO: docs
 *
 */
public class HeuristicAtom implements Atom {
	public static final String PREDICATE_HEURISTIC = ASPCore2Lexer.VOCABULARY.getLiteralName(ASPCore2Lexer.PREDICATE_HEURISTIC).replace("'", "");

	private final List<Term> terms;
	private final Predicate predicate;
	private final boolean ground;

	/**
	 * Constructs a heuristic atom that has been parsed from the input program.
	 * @deprecated because heuristic atoms in the input program will be dropped in favor of {@link HeuristicDirective}s.
	 */
	@Deprecated
	public HeuristicAtom(List<Term> terms, ASPCore2Parser.Naf_heuristicContext ctx) {
		if (ctx != null && (terms.size() < 1 || terms.size() > 2)) {
			throw new RuntimeException(getErrorMsg(ctx) +
					PREDICATE_HEURISTIC + "(Weight) or " + PREDICATE_HEURISTIC + "(Weight,Level) was " +
					"expected, but " + terms.size() + " terms found!");
		}

		List<Term> local = new ArrayList<>(2);
		local.addAll(terms);
		if (terms.size() < 2) {
			local.add(ConstantTerm.getInstance(1));
		}
		this.terms = Collections.unmodifiableList(local);
		this.predicate = Predicate.getInstance(PREDICATE_HEURISTIC, 2, true);
		this.ground = local.stream().allMatch(Term::isGround);
	}
	
	/**
	 * Constructs a heuristic atom using information from a {@link HeuristicDirective}.
	 */
	public HeuristicAtom(Atom head, Term weight, Term level, Term sign) {
		terms = new ArrayList<>(4);
		terms.add(weight);
		terms.add(level);
		terms.add(sign);
		terms.add(head.toFunctionTerm());
		this.predicate = Predicate.getInstance(PREDICATE_HEURISTIC, 4, true);
		this.ground = terms.stream().allMatch(Term::isGround);
	}

	private String getErrorMsg(ASPCore2Parser.Naf_heuristicContext ctx) {
		return "Invalid syntax" +
				((ctx != null) ? " in line " + ctx.getStart().getLine() : "") + "! ";
	}

	public HeuristicAtom(List<Term> terms) {
		this(terms, null);
	}

	/**
	 * Constructs a HeuristicOn / HeuristicOff atom.
	 */
	public HeuristicAtom(Predicate predicate, HeuristicAtom groundHeuristicAtom, int headId) {
		if (!groundHeuristicAtom.isGround()) {
			throw oops("Should be ground but isn't: " + groundHeuristicAtom);
		}
		terms = new ArrayList<>(4);
		terms.add(groundHeuristicAtom.getWeight());
		terms.add(groundHeuristicAtom.getLevel());
		terms.add(groundHeuristicAtom.getSign());
		terms.add(ConstantTerm.getInstance(headId));
		this.predicate = predicate;
		this.ground = true;
	}

	public Term getWeight() {
		return terms.get(0);
	}

	public Term getLevel() {
		return terms.get(1);
	}
	
	public Term getSign() {
		if (terms.size() > 2) {
			return terms.get(2);
		} else {
			return null;
		}
	}
	
	public FunctionTerm getHead() {
		if (terms.size() > 3) {
			return (FunctionTerm) terms.get(3);
		} else {
			return null;
		}
	}

	@Override
	public Predicate getPredicate() {
		return this.predicate;
	}

	@Override
	public List<Term> getTerms() {
		return this.terms;
	}

	@Override
	public boolean isGround() {
		return this.ground;
	}
	
	@Override
	public Literal toLiteral(boolean negated) {
		return new HeuristicLiteral(this, negated);
	}

	@Override
	public Set<VariableTerm> getBindingVariables() {
		Set<VariableTerm> bindingVariables = new HashSet<>();
		for (Term term : terms) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		return bindingVariables;
	}

	@Override
	public Set<VariableTerm> getNonBindingVariables() {
		Set<VariableTerm> nonbindingVariables = new HashSet<>();
		for (Term term : terms) {
			nonbindingVariables.addAll(term.getOccurringVariables());
		}
		return nonbindingVariables;
	}

	@Override
	public HeuristicAtom substitute(Substitution substitution) {
		return new HeuristicAtom(terms.stream()
				.map(t -> t.substitute(substitution))
				.collect(Collectors.toList()));
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(predicate.getName());
		if (!terms.isEmpty()) {
			sb.append("(");
			sb.append(Util.join(this.terms));
			sb.append(")");
		}
		return sb.toString();
	}

	public static HeuristicAtom fromHeuristicDirective(HeuristicDirective heuristicDirective) {
		return new HeuristicAtom(heuristicDirective.getHead(), heuristicDirective.getWeight(), heuristicDirective.getLevel(), heuristicDirective.getSign());
	}
}
