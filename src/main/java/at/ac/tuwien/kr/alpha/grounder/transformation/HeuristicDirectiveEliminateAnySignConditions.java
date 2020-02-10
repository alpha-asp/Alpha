/*
 * Copyright (c) 2020 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.Directive;
import at.ac.tuwien.kr.alpha.common.HeuristicDirective;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveBody;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.asSet;

/**
 * Eliminates all any-sign conditions (i.e. heuristic atoms containing all signs T, M, and F) from heuristic directives.
 * Any-sign conditions in a positive heuristic condition can be eliminated by replacing the heuristic directive
 * by two new heuristic directives, one in which TMF is replaced by TM and one in which TMF is replaced by F.
 * Any-sign conditions in a negative heuristic condition can be eliminated by replacing the heuristic literal
 * by two new heuristic literals, one in which TMF is replaced by TM and one in which TMF is replaced by F.
 */
public class HeuristicDirectiveEliminateAnySignConditions implements ProgramTransformation {

	public static Set<ThriceTruth> SET_ALL_SIGNS = asSet(ThriceTruth.values());
	public static Set<ThriceTruth> SET_TM = asSet(ThriceTruth.TRUE, ThriceTruth.MBT);
	public static Set<ThriceTruth> SET_F = asSet(ThriceTruth.FALSE);

	@Override
	public void transform(Program inputProgram) {
		Collection<Directive> heuristicDirectives = inputProgram.getInlineDirectives().getDirectives(InlineDirectives.DIRECTIVE.heuristic);
		Queue<HeuristicDirective> queue = toQueue(heuristicDirectives);
		List<Directive> transformedDirectives = transformDirectives(queue);
		inputProgram.getInlineDirectives().replaceDirectives(InlineDirectives.DIRECTIVE.heuristic, transformedDirectives);
	}

	private static List<Directive> transformDirectives(Queue<HeuristicDirective> queue) {
		final List<Directive> finalDirectives = new ArrayList<>(queue.size());
		HeuristicDirective heuristicDirective;
		loopHeuristicDirectives: while ((heuristicDirective = queue.poll()) != null) {
			for (HeuristicDirectiveAtom atom : heuristicDirective.getBody().getBodyAtomsPositive()) {
				if (SET_ALL_SIGNS.equals(atom.getSigns())) {
					queue.addAll(eliminatePositiveAnySignCondition(heuristicDirective, atom));
					continue loopHeuristicDirectives;
				}
			}
			for (HeuristicDirectiveAtom atom : heuristicDirective.getBody().getBodyAtomsNegative()) {
				if (SET_ALL_SIGNS.equals(atom.getSigns())) {
					queue.add(eliminateNegativeAnySignCondition(heuristicDirective, atom));
					continue loopHeuristicDirectives;
				}
			}
			finalDirectives.add(heuristicDirective);
		}
		return finalDirectives;
	}

	private static Queue<HeuristicDirective> toQueue(Collection<Directive> heuristicDirectives) {
		Queue<HeuristicDirective> queue = new LinkedList<>();
		for (Directive heuristicDirective : heuristicDirectives) {
			queue.add((HeuristicDirective)heuristicDirective);
		}
		return queue;
	}

	private static Collection<HeuristicDirective> eliminatePositiveAnySignCondition(HeuristicDirective heuristicDirective, HeuristicDirectiveAtom atom) {
		final List<HeuristicDirective> transformedDirectives = new ArrayList<>(2);
		transformedDirectives.add(replaceSignOfPositiveCondition(heuristicDirective, atom, SET_TM));
		transformedDirectives.add(replaceSignOfPositiveCondition(heuristicDirective, atom, SET_F));
		return transformedDirectives;
	}

	private static HeuristicDirective replaceSignOfPositiveCondition(HeuristicDirective heuristicDirective, HeuristicDirectiveAtom atom, Set<ThriceTruth> signSet) {
		final List<HeuristicDirectiveAtom> transformedPositiveAtoms = new ArrayList<>(heuristicDirective.getBody().getBodyAtomsPositive());
		transformedPositiveAtoms.remove(atom);
		transformedPositiveAtoms.add(HeuristicDirectiveAtom.body(signSet, atom.getAtom()));
		final HeuristicDirectiveBody transformedBody = new HeuristicDirectiveBody(transformedPositiveAtoms, heuristicDirective.getBody().getBodyAtomsNegative());
		return new HeuristicDirective(heuristicDirective.getHead(), transformedBody, heuristicDirective.getWeightAtLevel());
	}

	private static HeuristicDirective eliminateNegativeAnySignCondition(HeuristicDirective heuristicDirective, HeuristicDirectiveAtom atom) {
		final List<HeuristicDirectiveAtom> transformedNegativeAtoms = new ArrayList<>(heuristicDirective.getBody().getBodyAtomsNegative());
		transformedNegativeAtoms.remove(atom);
		transformedNegativeAtoms.add(HeuristicDirectiveAtom.body(SET_TM, atom.getAtom()));
		transformedNegativeAtoms.add(HeuristicDirectiveAtom.body(SET_F, atom.getAtom()));
		final HeuristicDirectiveBody transformedBody = new HeuristicDirectiveBody(heuristicDirective.getBody().getBodyAtomsPositive(), transformedNegativeAtoms);
		return new HeuristicDirective(heuristicDirective.getHead(), transformedBody, heuristicDirective.getWeightAtLevel());
	}
}
