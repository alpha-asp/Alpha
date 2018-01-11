/**
 * Copyright (c) 2018 Siemens AG
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.HeuristicAtom;
import at.ac.tuwien.kr.alpha.common.heuristics.DomainSpecificHeuristicValues;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;

import java.util.HashMap;
import java.util.Map;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Records a mapping between rule atom IDs and their corresponding domain-specific heuristic values
 */
public class DomainSpecificHeuristicsRecorder {

	private Map<Integer, DomainSpecificHeuristicValues> newValues = new HashMap<>();

	/**
	 * Adds the given mapping between rule body and domain-specific heuristic information
	 * 
	 * @param bodyId
	 *          the ID of a ground {@link RuleAtom}
	 * @param groundHeuristicAtom
	 *          the ground heuristic information taken from the corresponding ground rule
	 */
	public void record(int bodyId, HeuristicAtom groundHeuristicAtom) {
		if (!groundHeuristicAtom.isGround()) {
			oops("Atom is not ground: " + groundHeuristicAtom);
		}
		newValues.put(bodyId, new DomainSpecificHeuristicValues(bodyId, groundHeuristicAtom));
	}

	/**
	 * Gets the set of mappings not yet retrieved and resets the cache
	 * 
	 * @return a set of new mappings between rule atom IDs and their corresponding domain-specific heuristic values
	 */
	public Map<Integer, DomainSpecificHeuristicValues> getAndReset() {
		Map<Integer, DomainSpecificHeuristicValues> currentValues = newValues;
		newValues = new HashMap<>();
		return currentValues;
	}
}
