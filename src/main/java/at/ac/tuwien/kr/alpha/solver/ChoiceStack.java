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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

/**
 * The stack of choices. For each choice in the current path we remember the chosen atom,
 * the chosen value, and if the other value has been tried before (if we "backtracked" already).
 *
 */
class ChoiceStack implements Iterable<Integer> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceStack.class);

	private final AtomTranslator translator;
	private final Stack<Entry> delegate = new Stack<>();
	private final DebugWatcher debugWatcher;

	/**
	 * A helper class for halting the debugger when certain assignments occur on the choice stack.
	 *
	 * Example usage (called from DefaultSolver):
	 * choiceStack = new ChoiceStack(grounder, true);
	 * choiceStack.getDebugWatcher().watchAssignments("_R_(0,_C:red_V:7)=TRUE", "_R_(0,_C:green_V:8)=TRUE", "_R_(0,_C:red_V:9)=TRUE", "_R_(0,_C:red_V:4)=TRUE");
	 */
	class DebugWatcher {
		ArrayList<String> toWatchFor = new ArrayList<>();

		private void runWatcher() {
			String current = ChoiceStack.this.toString();
			boolean contained = true;
			for (String s : toWatchFor) {
				if (!current.contains(s)) {
					contained = false;
					break;
				}
			}
			if (contained) {
				LOGGER.debug("Marker hit.");	// Set debug breakpoint here to halt when desired assignment occurs.
			}
		}

		/**
		 * Registers atom assignments to watch for.
		 * @param toWatch one or more strings as they occur in ChoiceStack.toString()
		 */
		public void watchAssignments(String... toWatch) {
			toWatchFor = new ArrayList<>();
			Collections.addAll(toWatchFor, toWatch);
		}
	}

	public DebugWatcher getDebugWatcher() {
		return debugWatcher;
	}

	ChoiceStack(AtomTranslator translator, boolean enableDebugWatcher) {
		this.translator = translator;
		if (enableDebugWatcher) {
			this.debugWatcher = new DebugWatcher();
		} else {
			this.debugWatcher = null;
		}
	}

	public void push(int atom, boolean value) {
		delegate.push(new Entry(atom, value, false));
		if (debugWatcher != null) {
			debugWatcher.runWatcher();
		}
	}

	public void pushBacktrack(int atom, boolean value) {
		delegate.push(new Entry(atom, value, true));
		if (debugWatcher != null) {
			debugWatcher.runWatcher();
		}
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

	private class ChoiceIterator implements Iterator<Integer> {
		private final Iterator<Entry> iterator;

		ChoiceIterator(Iterator<Entry> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Integer next() {
			Entry next = iterator.next();
			return next.atom * (next.value ? 1 : -1);
		}
	}

	public ChoiceIterator iterator() {
		return new ChoiceIterator(delegate.iterator());
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

		Entry(int atom, boolean value, boolean backtracked) {
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
