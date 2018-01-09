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
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class HeuristicAtom implements Literal {
	public static final String PREDICATE_HEURISTIC = ASPCore2Lexer.VOCABULARY.getLiteralName(ASPCore2Lexer.PREDICATE_HEURISTIC).replace("'", "");

	private final List<Term> terms;
	private final Predicate predicate = Predicate.getInstance(PREDICATE_HEURISTIC, 2, true);
	private final boolean ground;

	private final Integer weight;
	private final Integer level;

	public HeuristicAtom(List<Term> terms, ASPCore2Parser.Naf_heuristicContext ctx) {
		if (terms.size() < 1 || terms.size() > 2) {
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
		this.weight = getConstant(this.terms.get(0), ctx);
		this.level = getConstant(this.terms.get(1), ctx);

		this.ground = this.weight != null && this.level != null;
	}

	private String getErrorMsg(ASPCore2Parser.Naf_heuristicContext ctx) {
		return "Invalid syntax" +
				((ctx != null) ? " in line " + ctx.getStart().getLine() : "") + "! ";
	}

	public HeuristicAtom(List<Term> terms) {
		this(terms, null);
	}

	private Integer getConstant(Term term, ASPCore2Parser.Naf_heuristicContext ctx) {
		if (term instanceof FunctionTerm) {
			throw new RuntimeException(getErrorMsg(ctx) + "Function terms cannot be used in heuristic atoms.");
		}
		if (!term.isGround()) {
			return null;
		}
		if (term instanceof ConstantTerm) {
			return (Integer) ((ConstantTerm<?>) term).getObject();
		}
		throw new RuntimeException(getErrorMsg(ctx) +
				PREDICATE_HEURISTIC + "(Weight) or " + PREDICATE_HEURISTIC + "(Weight,Level) was expected.");
	}

	public Integer getWeight() {
		return weight;
	}

	public Integer getLevel() {
		return level;
	}

	@Override
	public Type getType() {
		return Type.HEURISTIC_ATOM;
	}

	@Override
	public boolean isNegated() {
		return true;
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
	public List<VariableTerm> getBindingVariables() {
		LinkedList<VariableTerm> bindingVariables = new LinkedList<>();
		for (Term term : terms) {
			bindingVariables.addAll(term.getOccurringVariables());
		}
		return bindingVariables;
	}

	@Override
	public List<VariableTerm> getNonBindingVariables() {
		LinkedList<VariableTerm> nonbindingVariables = new LinkedList<>();
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
		final StringBuilder sb = new StringBuilder("not ");
		sb.append(predicate.getName());
		if (!terms.isEmpty()) {
			sb.append("(");
			sb.append(Util.join(this.terms));
			sb.append(")");
		}
		return sb.toString();
	}
}
