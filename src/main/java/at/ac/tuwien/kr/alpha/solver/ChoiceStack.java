/**
 * Copyright (c) 2016, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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
package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AtomTranslator;

import java.util.Stack;

/**
 * The stack of choices. For each choice in the current path we remember the chosen atom,
 * the chosen value, and if the other value has been tried before (if we "backtracked" already).
 *
 */
class ChoiceStack {
	private final AtomTranslator translator;
	private final Stack<Entry> delegate = new Stack<>();

	ChoiceStack(AtomTranslator translator) {
		this.translator = translator;
	}

	public void push(int atom, boolean value) {
		delegate.push(new Entry(atom, value, false));
	}

	public void pushBacktrack(int atom, boolean value) {
		delegate.push(new Entry(atom, value, true));
	}

	public void remove() {
		delegate.pop();
	}

	public int peekAtom() {
		return delegate.peek().atom;
	}

	public boolean peekValue() {
		return delegate.peek().value;
	}
	
	public boolean peekBacktracked() {
		return delegate.peek().backtracked;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	public int size() {
		return delegate.size();
	}

	private class Entry {
		int atom;
		boolean value;
		boolean backtracked;

		public Entry(int atom, boolean value, boolean backtracked) {
			this.atom = atom;
			this.value = value;
			this.backtracked = backtracked;
		}

		@Override
		public String toString() {
			return translator.atomToString(atom) + "=" + (value ? "TRUE" : "FALSE");
		}
	}
}
