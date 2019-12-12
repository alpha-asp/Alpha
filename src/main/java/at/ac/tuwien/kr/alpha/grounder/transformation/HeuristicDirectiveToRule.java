/**
 * Copyright (c) 2018-2019 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.solver.heuristics.HeuristicsConfiguration;

import java.util.Collection;
import java.util.Iterator;

/**
 * Converts all {@link HeuristicDirective}s to {@link Rule}s if
 * {@link HeuristicsConfiguration#isRespectDomspecHeuristics()} is {@code true},
 * otherwise removes all heuristic directives.
 */
public class HeuristicDirectiveToRule implements ProgramTransformation {

	private final HeuristicsConfiguration heuristicsConfiguration;

	public HeuristicDirectiveToRule(HeuristicsConfiguration heuristicsConfiguration) {
		this.heuristicsConfiguration = heuristicsConfiguration;
	}

	@Override
	public void transform(Program inputProgram) {
		Collection<Directive> heuristicDirectives = inputProgram.getInlineDirectives().getDirectives(InlineDirectives.DIRECTIVE.heuristic);
		if (heuristicDirectives == null) {
			return;
		}
		if (heuristicsConfiguration.isRespectDomspecHeuristics()) {
			Iterator<Directive> directivesIterator = heuristicDirectives.iterator();
			while (directivesIterator.hasNext()) {
				Directive directive = directivesIterator.next();
				transformAndAddToProgram((HeuristicDirective) directive, inputProgram);
				directivesIterator.remove();
			}
		} else {
			heuristicDirectives.clear();
		}
	}

	private void transformAndAddToProgram(HeuristicDirective heuristicDirective, Program program) {
		Head head = new DisjunctiveHead(HeuristicAtom.fromHeuristicDirective(heuristicDirective));
		Rule rule = new Rule(head, heuristicDirective.getBody());
		program.getRules().add(rule);
	}
}
