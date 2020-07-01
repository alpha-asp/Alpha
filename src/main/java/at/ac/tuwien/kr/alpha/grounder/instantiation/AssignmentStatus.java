package at.ac.tuwien.kr.alpha.grounder.instantiation;


// Not to be confused with ThriceTruth, only using this in order to be able to
// distinguish between atoms that are true (or MBT) and ones that are unassigned
// NOTE: Could use a Boolean and null for unassigned, but would be weird to read
// for anyone not intimately familiar with the code
public enum AssignmentStatus {
	TRUE, FALSE, UNASSIGNED;
}
