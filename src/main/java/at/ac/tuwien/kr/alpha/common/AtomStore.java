package at.ac.tuwien.kr.alpha.common;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;

import java.util.Iterator;
import java.util.List;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isNegated;

/**
 * Translates atoms between integer (solver) and object (grounder) representation.
 */
public interface AtomStore {

	/**
	 * Returns true whenever the atom is a valid choice point (i.e., it represents a rule body).
	 * @param atom
	 * @return
	 */
	boolean isAtomChoicePoint(int atom);

	/**
	 * Returns the highest atomId in use.
	 * @return the highest atomId in use.
	 */
	int getMaxAtomId();

	/**
	 * Translates an atom represented as int into an Atom object.
	 * @param atom the atom to translate.
	 * @return the Atom object represented by the int.
	 */
	Atom get(int atom);

	/**
	 * Translates an atom represented as Atom object into an int.
	 * @param atom the Atom object to translate.
	 * @return the int representing the Atom object.
	 */
	int get(Atom atom);

	/**
	 * If the given ground atom is not already stored, associates it with a new integer (ID) and stores it, else
	 * returns the current associated atom ID. Hence, multiple calls with the same parameter will return the same
	 * value.
	 * @param groundAtom the ground atom to look up in the store.
	 * @return the integer ID of the ground atom, possibly newly assigned.
	 */
	int putIfAbsent(Atom groundAtom);

	/**
	 * Returns whether the given ground atom is known to the AtomStore.
	 * @param groundAtom the ground atom to test.
	 * @return true if the ground atom is already associated an integer ID.
	 */
	boolean contains(Atom groundAtom);

	/**
	 * Returns a list of currently known but unassigned atoms.
	 * @param assignment the current assignment.
	 * @return a list of atoms not having assigned a truth value.
	 */
	List<Integer> getUnassignedAtoms(Assignment assignment);

	String atomToString(int atom);

	default String literalToString(int literal) {
		return (isNegated(literal) ? "-" : "+") + "(" + atomToString(atomOf(literal)) + ")";
	}

	/**
	 * Prints the NoGood such that literals are structured atoms instead of integers.
	 * @param noGood the nogood to translate
	 * @return the string representation of the NoGood.
	 */
	default <T extends NoGood> String noGoodToString(T noGood) {
		StringBuilder sb = new StringBuilder();

		if (noGood.hasHead()) {
			sb.append("*");
		}
		sb.append("{");

		for (Iterator<Integer> iterator = noGood.iterator(); iterator.hasNext();) {
			sb.append(literalToString(iterator.next()));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}
}
