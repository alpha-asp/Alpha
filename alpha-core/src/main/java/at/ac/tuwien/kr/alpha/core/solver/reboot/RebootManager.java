/**
 * Copyright (c) 2022, the Alpha Team.
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
package at.ac.tuwien.kr.alpha.core.solver.reboot;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.core.programs.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.core.programs.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.programs.atoms.Literals;
import at.ac.tuwien.kr.alpha.core.programs.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class RebootManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(RebootManager.class);

	private final AtomStore atomStore;
	private final AtomizedNoGoodCollection enumerationNoGoods;
	private final AtomizedNoGoodCollection learnedNoGoods;
	private final List<RuleAtom> discoveredRuleAtoms;
	private int filteredCounter;

	public RebootManager(AtomStore atomStore) {
		this.atomStore = atomStore;
		this.enumerationNoGoods = new AtomizedNoGoodCollection(atomStore);
		this.learnedNoGoods = new AtomizedNoGoodCollection(atomStore);
		this.discoveredRuleAtoms = new LinkedList<>();
		this.filteredCounter = 0;
	}

	public void newEnumerationNoGood(NoGood noGood) {
		enumerationNoGoods.add(noGood);
		discoverRuleAtoms(noGood);
	}

	public void newLearnedNoGood(NoGood noGood) {
		if (!scanForAtomsToFilter(noGood)) {
			learnedNoGoods.add(noGood);
			discoverRuleAtoms(noGood);
		} else {
			filteredCounter++;
		}
	}

	public void newLearnedNoGoods(Collection<NoGood> noGoods) {
		noGoods.forEach(this::newLearnedNoGood);
	}

	public List<NoGood> getEnumerationNoGoods() {
		return enumerationNoGoods.getNoGoods();
	}

	public List<NoGood> getLearnedNoGoods() {
		return learnedNoGoods.getNoGoods();
	}

	public List<RuleAtom> getDiscoveredRuleAtoms() {
		LOGGER.debug("Number of rule atoms: " + discoveredRuleAtoms.size());
		LOGGER.debug("Number of filtered out nogoods: " + filteredCounter);
		return discoveredRuleAtoms;
	}

	private void discoverRuleAtoms(NoGood noGood) {
		noGood.stream().forEach((literalId) -> {
			Atom atom = atomStore.get(Literals.atomOf(literalId));
			if (atom instanceof RuleAtom) {
				discoveredRuleAtoms.add((RuleAtom) atom);
			}
		});
	}

	private boolean scanForAtomsToFilter(NoGood noGood) {
		for (int literalId : noGood) {
			Atom atom = atomStore.get(Literals.atomOf(literalId));
			if (atom instanceof EnumerationAtom || atom instanceof ChoiceAtom) {
				return true;
			}
		}
		return false;
	}
}
