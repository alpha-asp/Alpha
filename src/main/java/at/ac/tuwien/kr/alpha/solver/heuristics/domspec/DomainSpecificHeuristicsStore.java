/**
 * Copyright (c) 2018 Siemens AG
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
package at.ac.tuwien.kr.alpha.solver.heuristics.domspec;

import at.ac.tuwien.kr.alpha.common.heuristics.HeuristicDirectiveValues;
import at.ac.tuwien.kr.alpha.solver.Checkable;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;

import java.util.Collection;
import java.util.List;

/**
 * Stores information on heuristic directives obtained from the grounder
 */
public interface DomainSpecificHeuristicsStore extends Checkable {
	
	void addInfo(int heuristicId, HeuristicDirectiveValues values);
	
	/**
	 * Retrieves and removes the element with the highest priority
	 * @return
	 */
	HeuristicDirectiveValues poll();

	/**
	 * Returns the IDs of all values that have the same priority as the value on top of the stack. Values can be obtained by {@link #getValues(int)}
	 * TODO: is a List for efficiency reasons, could this also be a Collection?
	 * @return
	 */
	List<Integer> pollIDsWithHighestPriority();

	/**
	 * Inserts the given heuristic IDs back into the store
	 * @param heuristicIDs
	 */
	void offer(Collection<Integer> heuristicIDs);
	
	/**
	 * Retrieves, but does not remove, the element with the highest priority
	 * @return
	 */
	HeuristicDirectiveValues peek();
	
	HeuristicDirectiveValues getValues(int heuristicId);
	
	void setChoiceManager(ChoiceManager choiceManager);

}
