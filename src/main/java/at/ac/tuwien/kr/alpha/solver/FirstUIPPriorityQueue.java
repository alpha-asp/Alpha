package at.ac.tuwien.kr.alpha.solver;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class FirstUIPPriorityQueue {
	private final PriorityQueue<Assignment.Entry> delegate = new PriorityQueue<>(new EntryComparatorPropagationLevel());
	private final Set<Assignment.Entry> alreadyAdded = new HashSet<>();
	private final int decisionLevel;

	private int lastPollPropagationLevel = Integer.MAX_VALUE;

	public FirstUIPPriorityQueue(int decisionLevel) {
		this.decisionLevel = decisionLevel;
	}

	private class EntryComparatorPropagationLevel implements Comparator<Assignment.Entry> {

		@Override
		public int compare(Assignment.Entry o1, Assignment.Entry o2) {
			if (o1 == null) {
				return o2 == null ? 0 : +1;
			} else if (o2 == null) {
				return -1;
			} else if (o1.equals(o2)) {
				return 0;
			} else if (o1.getPropagationLevel() > o2.getPropagationLevel()) {
				return -1;
			} else if (o1.getPropagationLevel() < o2.getPropagationLevel()) {
				return +1;
			} else {
				throw new RuntimeException("Incomparable assignment entries given. Should not happen.");
			}
		}
	}

	/**
	 * Adds a new entry to the queue. The entry is sorted into the queue according to its propagationLevel (highest
	 * propagationLevel first). If the decisionLevel of the entry does not equal the one of the
	 * FirstUIPPriorityQueue, the entry is ignored. Duplicate entries are ignored.
	 * @param entry the entry to add.
	 */
	public void add(Assignment.Entry entry) {
		if (entry.getDecisionLevel() != decisionLevel) {
			// Ignore assignments from lower decision levels..
			return;
		}
		if (entry.getPropagationLevel() > lastPollPropagationLevel) {
			throw new RuntimeException("Adding to 1UIP queue an entry with higher propagationLevel than returned by the last poll. Should not happen.");
		}
		if (alreadyAdded.contains(entry)) {
			// Ignore already added assignments.
			return;
		}
		delegate.add(entry);
		alreadyAdded.add(entry);
	}

	/**
	 * Retrieves the first element (i.e., the entry with the highest propagationLevel) from the queue and removes it.
	 * @return null if the queue is entry.
	 */
	public Assignment.Entry poll() {
		Assignment.Entry firstEntry = delegate.poll();
		if (firstEntry == null) {
			return null;
		}
		lastPollPropagationLevel = firstEntry.getPropagationLevel();
		return firstEntry;
	}

	/**
	 * Returns the size of the queue.
	 * @return the size of the underlying queue.
	 */
	public int size() {
		return delegate.size();
	}

}
