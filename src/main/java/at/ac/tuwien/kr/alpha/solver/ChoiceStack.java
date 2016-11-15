package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AtomTranslator;
import at.ac.tuwien.kr.alpha.grounder.Grounder;

import java.util.Stack;

class ChoiceStack {
	private final AtomTranslator translator;
	private final Stack<Pair> delegate = new Stack<>();

	ChoiceStack(AtomTranslator translator) {
		this.translator = translator;
	}

	public void push(int atom, boolean value) {
		delegate.push(new Pair(atom, value));
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

	@Override
	public String toString() {
		return delegate.toString();
	}

	public int size() {
		return delegate.size();
	}

	private class Pair {
		int atom;
		boolean value;

		public Pair(int atom, boolean value) {
			this.atom = atom;
			this.value = value;
		}

		@Override
		public String toString() {
			return translator.atomToString(atom) + "=" + (value ? "TRUE" : "FALSE");
		}
	}
}
