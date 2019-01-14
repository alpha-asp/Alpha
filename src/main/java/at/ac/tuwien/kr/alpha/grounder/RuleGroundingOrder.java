/**
 * Copyright (c) 2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;

/**
 * A grounding order computed by {@link RuleGroundingOrders} for a specific {@link NonGroundRule} and a specific starting literal.
 */
public class RuleGroundingOrder {
	
	private Literal startingLiteral;
	private Literal[] otherLiterals;
	private int positionLastVarBound;
	private int pushedBackFrom = -1;
	
	/**
	 * @param startingLiteral
	 * @param otherLiterals
	 * @param positionLastVarBound
	 */
	RuleGroundingOrder(Literal startingLiteral, Literal[] otherLiterals, int positionLastVarBound) {
		super();
		this.startingLiteral = startingLiteral;
		this.otherLiterals = otherLiterals;
		this.positionLastVarBound = positionLastVarBound;
	}

	/**
	 * @return the startingLiteral
	 */
	public Literal getStartingLiteral() {
		return startingLiteral;
	}

	/**
	 * @return the otherLiterals
	 */
	public Literal[] getOtherLiterals() {
		return otherLiterals;
	}

	/**
	 * @return the position in {@link #getOtherLiterals()} from which on all variables are bound
	 */
	public int getPositionFromWhichAllVarsAreBound() {
		return positionLastVarBound + 1;
	}
	
	/**
	 * @return the pushedBackFrom
	 */
	public int getPushedBackFrom() {
		return pushedBackFrom;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(startingLiteral);
		sb.append(" : ");
		for (int i = 0; i < otherLiterals.length; i++) {
			if (i == positionLastVarBound + 1) {
				sb.append("| ");
			}
			sb.append(otherLiterals[i]);
			if (i < otherLiterals.length - 1) {
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}

	/**
	 * @param orderPosition
	 * @return
	 */
	public RuleGroundingOrder pushBack(int orderPosition) {
		// TODO: this ignores positionLastVarBound for now (because it is not used anyway)
		Literal[] reorderedOtherLiterals = new Literal[otherLiterals.length];
		int i = 0;
		for (; i < orderPosition; i++) {
			reorderedOtherLiterals[i] = otherLiterals[i];
		}
		for (; i < otherLiterals.length - 1; i++) {
			reorderedOtherLiterals[i] = otherLiterals[i + 1];
		}
		reorderedOtherLiterals[i] = otherLiterals[orderPosition];
		RuleGroundingOrder reorderedGroundingOrder = new RuleGroundingOrder(startingLiteral, reorderedOtherLiterals, positionLastVarBound);
		reorderedGroundingOrder.pushedBackFrom = (this.pushedBackFrom >= 0 ? this.pushedBackFrom : otherLiterals.length) - 1;
		return reorderedGroundingOrder;
	}

}
