package at.ac.tuwien.kr.alpha.grounder.instantiation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;

public class LiteralInstantiationStrategyTest {

	@Test
	public void workingMemoryBasedInstantiationAcceptLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		LiteralInstantiationStrategy strategy = new WorkingMemoryBasedInstantiationStrategy(workingMemory);
		Literal positiveAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		assertEquals(AssignmentStatus.TRUE, strategy.getTruthForGroundLiteral(positiveAcceptedLiteral));
		Literal negativeAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), false);
		assertEquals(AssignmentStatus.TRUE, strategy.getTruthForGroundLiteral(negativeAcceptedLiteral));
	}

	@Test
	public void workingMemoryBasedInstantiationRejectLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		LiteralInstantiationStrategy strategy = new WorkingMemoryBasedInstantiationStrategy(workingMemory);
		Literal positiveRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), true);
		assertEquals(AssignmentStatus.FALSE, strategy.getTruthForGroundLiteral(positiveRejectedLiteral));
		Literal negativeRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), false);
		assertEquals(AssignmentStatus.FALSE, strategy.getTruthForGroundLiteral(negativeRejectedLiteral));
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to check the truth
	 * (i.e. {@link AssignmentStatus}) of the positive ground literal "p(a)".
	 * 
	 * In this case, the instantiation strategy does not have an assignment set (as
	 * is the case when {@link NaiveGrounder} is in bootstrap),
	 * so we expect the instantiation strategy to determine that p(a) is TRUE.
	 * Furthermore, the stale atom set (used by {@link NaiveGrounder} to clean up
	 * atoms that should be deleted from working memory) must stay empty.
	 */
	@Test
	public void defaultLazyGroundingNoAssignmentGroundLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		BasicAtom pOfA = new BasicAtom(p, ConstantTerm.getSymbolicInstance("a"));
		WorkingMemory workingMemory = new WorkingMemory();
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, new AtomStoreImpl(),
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(null);
		

		AssignmentStatus assignmentStatus = strategy.getTruthForGroundLiteral(new BasicLiteral(pOfA, true));
		assertEquals(AssignmentStatus.TRUE, assignmentStatus);
		assertTrue(staleSet.isEmpty());
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to find ground
	 * instances for the partially ground positive literal "q(a, X)".
	 * 
	 * In this case, the instantiation strategy does not have an assignment set (as
	 * is the case when {@link NaiveGrounder} is in bootstrap), so we expect the
	 * assignment status (i.e. assignment status of the found ground instance)
	 * passed back with the substitution to be TRUE. Furthermore, the stale atom set
	 * (used by {@link NaiveGrounder} to clean up atoms that should be deleted from
	 * working memory) must stay empty.
	 */
	@Test
	public void defaultLazyGroundingNoAssignmentSubstituteNonGroundLiteral() {
		Predicate q = Predicate.getInstance("q", 2);
		BasicAtom nonGroundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), VariableTerm.getInstance("X"));
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(q);
		workingMemory.addInstance(new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b")), true);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, new AtomStoreImpl(),
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(null);

		List<ImmutablePair<Substitution, AssignmentStatus>> result = strategy.getAcceptedSubstitutions(new BasicLiteral(nonGroundAtom, true),
				new Substitution());
		assertEquals(1, result.size());
		ImmutablePair<Substitution, AssignmentStatus> substitutionInfo = result.get(0);
		Substitution substitution = substitutionInfo.left;
		AssignmentStatus assignmentStatus = substitutionInfo.right;
		assertEquals(AssignmentStatus.TRUE, assignmentStatus);
		assertTrue(substitution.isVariableSet(VariableTerm.getInstance("X")));
		assertEquals(ConstantTerm.getSymbolicInstance("b"), substitution.eval(VariableTerm.getInstance("X")));
		assertTrue(staleSet.isEmpty());
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to check the truth
	 * (i.e. {@link AssignmentStatus}) of the positive ground literal "p(a)".
	 * 
	 * In this case, the instantiation strategy has an empty assignment,
	 * so we expect the instantiation strategy to determine that p(a) is UNASSIGNED.
	 * Since UNASSIGNED and FALSE atoms are (potentially) stale in working memory,
	 * we expect the atom "p(a)" to be added to the stale set by the instantiation
	 * strategy.
	 */
	@Test
	public void defaultLazyGroundingCheckUnassignedGroundLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		BasicAtom pOfA = new BasicAtom(p, ConstantTerm.getSymbolicInstance("a"));
		WorkingMemory workingMemory = new WorkingMemory();
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		AssignmentStatus assignmentStatus = strategy.getTruthForGroundLiteral(new BasicLiteral(pOfA, true));
		assertEquals(AssignmentStatus.UNASSIGNED, assignmentStatus);

		assertEquals(1, staleSet.size());
		assertTrue(staleSet.contains(pOfA));
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to check the truth
	 * (i.e. {@link AssignmentStatus}) of the positive ground literal "p(a)".
	 * 
	 * In this case, the instantiation strategy has an assignment where the atom
	 * "p(a)" is assigned ThriceTruth.FALSE, so we expect the instantiation strategy
	 * to determine that p(a) is FALSE. Since UNASSIGNED and FALSE atoms are
	 * (potentially) stale in working memory, we expect the atom "p(a)" to be added
	 * to the stale set by the instantiation strategy.
	 */
	@Test
	public void defaultLazyGroundingCheckFalseGroundLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		BasicAtom pOfA = new BasicAtom(p, ConstantTerm.getSymbolicInstance("a"));
		WorkingMemory workingMemory = new WorkingMemory();
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		atomStore.putIfAbsent(pOfA);
		assignment.growForMaxAtomId();
		assignment.assign(atomStore.get(pOfA), ThriceTruth.FALSE);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		AssignmentStatus assignmentStatus = strategy.getTruthForGroundLiteral(new BasicLiteral(pOfA, true));
		assertEquals(AssignmentStatus.FALSE, assignmentStatus);

		assertEquals(1, staleSet.size());
		assertTrue(staleSet.contains(pOfA));
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to check the truth
	 * (i.e. {@link AssignmentStatus}) of the positive ground literal "p(a)".
	 * 
	 * In this case, the instantiation strategy has an assignment where the atom
	 * "p(a)" is assigned ThriceTruth.TRUE, so we expect the instantiation strategy
	 * to determine that p(a) is TRUE. Furthermore, the stale atom set
	 * (used by {@link NaiveGrounder} to clean up atoms that should be deleted from
	 * working memory) must stay empty.
	 */
	@Test
	public void defaultLazyGroundingCheckTrueGroundLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		BasicAtom pOfA = new BasicAtom(p, ConstantTerm.getSymbolicInstance("a"));
		WorkingMemory workingMemory = new WorkingMemory();
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		atomStore.putIfAbsent(pOfA);
		assignment.growForMaxAtomId();
		assignment.assign(atomStore.get(pOfA), ThriceTruth.TRUE);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);
		
		AssignmentStatus assignmentStatus = strategy.getTruthForGroundLiteral(new BasicLiteral(pOfA, true));
		assertEquals(AssignmentStatus.TRUE, assignmentStatus);

		assertTrue(staleSet.isEmpty());
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to check the truth
	 * (i.e. {@link AssignmentStatus}) of the positive ground literal "p(a)".
	 * 
	 * In this case, the instantiation strategy has an assignment where the atom
	 * "p(a)" is assigned ThriceTruth.MBT, so we expect the instantiation strategy
	 * to determine that p(a) is TRUE. Furthermore, the stale atom set
	 * (used by {@link NaiveGrounder} to clean up atoms that should be deleted from
	 * working memory) must stay empty.
	 */
	@Test
	public void defaultLazyGroundingCheckMustBeTrueGroundLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		BasicAtom pOfA = new BasicAtom(p, ConstantTerm.getSymbolicInstance("a"));
		WorkingMemory workingMemory = new WorkingMemory();
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		atomStore.putIfAbsent(pOfA);
		assignment.growForMaxAtomId();
		assignment.assign(atomStore.get(pOfA), ThriceTruth.MBT);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		AssignmentStatus assignmentStatus = strategy.getTruthForGroundLiteral(new BasicLiteral(pOfA, true));
		assertEquals(AssignmentStatus.TRUE, assignmentStatus);

		assertTrue(staleSet.isEmpty());
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to find the ground
	 * instance "q(a, b)" for the partially ground positive literal "q(a, X)".
	 * 
	 * In this case, the instantiation strategy has an empty assignment, so we
	 * expect the assignment status (i.e. assignment status of the found ground
	 * instance) passed back with the substitution to be UNASSIGNED. Since
	 * UNASSIGNED and FALSE atoms are (potentially) stale in working memory, we
	 * expect the atom "q(a, b)" to be added to the stale set by the instantiation
	 * strategy.
	 */
	@Test
	public void defaultLazyGroundingSubstituteNonGroundLiteralWithUnassignedInstance() {
		Predicate q = Predicate.getInstance("q", 2);
		BasicAtom nonGroundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), VariableTerm.getInstance("X"));
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(q);
		workingMemory.addInstance(new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b")), true);
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		List<ImmutablePair<Substitution, AssignmentStatus>> result = strategy.getAcceptedSubstitutions(new BasicLiteral(nonGroundAtom, true),
				new Substitution());
		assertEquals(1, result.size());
		ImmutablePair<Substitution, AssignmentStatus> substitutionInfo = result.get(0);
		Substitution substitution = substitutionInfo.left;
		AssignmentStatus assignmentStatus = substitutionInfo.right;
		assertEquals(AssignmentStatus.UNASSIGNED, assignmentStatus);
		assertTrue(substitution.isVariableSet(VariableTerm.getInstance("X")));
		assertEquals(ConstantTerm.getSymbolicInstance("b"), substitution.eval(VariableTerm.getInstance("X")));

		assertEquals(1, staleSet.size());
		assertTrue(staleSet.contains(new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b"))));
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to find the ground
	 * instance "q(a, b)" for the partially ground positive literal "q(a, X)".
	 * 
	 * In this case, the instantiation strategy has an assignment where q(a, b) is
	 * assigned ThriceTruth.TRUE, so we expect the assignment status (i.e.
	 * assignment status of the found ground instance) passed back with the
	 * substitution to be TRUE. Furthermore, we expect the stale atom set to stay
	 * empty.
	 */
	@Test
	public void defaultLazyGroundingSubstituteNonGroundLiteralWithTrueInstance() {
		Predicate q = Predicate.getInstance("q", 2);
		BasicAtom nonGroundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), VariableTerm.getInstance("X"));
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(q);
		workingMemory.addInstance(new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b")), true);
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		BasicAtom groundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b"));
		atomStore.putIfAbsent(groundAtom);
		assignment.growForMaxAtomId();
		assignment.assign(atomStore.get(groundAtom), ThriceTruth.TRUE);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		List<ImmutablePair<Substitution, AssignmentStatus>> result = strategy.getAcceptedSubstitutions(new BasicLiteral(nonGroundAtom, true),
				new Substitution());
		assertEquals(1, result.size());
		ImmutablePair<Substitution, AssignmentStatus> substitutionInfo = result.get(0);
		Substitution substitution = substitutionInfo.left;
		AssignmentStatus assignmentStatus = substitutionInfo.right;
		assertEquals(AssignmentStatus.TRUE, assignmentStatus);
		assertTrue(substitution.isVariableSet(VariableTerm.getInstance("X")));
		assertEquals(ConstantTerm.getSymbolicInstance("b"), substitution.eval(VariableTerm.getInstance("X")));

		assertTrue(staleSet.isEmpty());
	}

	/**
	 * Uses {@link DefaultLazyGroundingInstantiationStrategy} to find the ground
	 * instance "q(a, b)" for the partially ground positive literal "q(a, X)".
	 * 
	 * In this case, the instantiation strategy has an assignment where q(a, b) is
	 * assigned ThriceTruth.FALSE, so we expect an empty list from
	 * {@link LiteralInstantiationStrategy#getAcceptedSubstitutions(Literal, Substitution)}.
	 * Furthermore, we expect the atom q(a, b) to be added to the stale atom set.
	 */
	@Test
	public void defaultLazyGroundingSubstituteNonGroundLiteralWithFalseInstance() {
		Predicate q = Predicate.getInstance("q", 2);
		BasicAtom nonGroundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), VariableTerm.getInstance("X"));
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(q);
		workingMemory.addInstance(new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b")), true);
		AtomStore atomStore = new AtomStoreImpl();
		WritableAssignment assignment = new TrailAssignment(atomStore);
		BasicAtom groundAtom = new BasicAtom(q, ConstantTerm.getSymbolicInstance("a"), ConstantTerm.getSymbolicInstance("b"));
		atomStore.putIfAbsent(groundAtom);
		assignment.growForMaxAtomId();
		assignment.assign(atomStore.get(groundAtom), ThriceTruth.FALSE);
		LinkedHashSet<Atom> staleSet = new LinkedHashSet<>();
		DefaultLazyGroundingInstantiationStrategy strategy = new DefaultLazyGroundingInstantiationStrategy(workingMemory, atomStore,
				Collections.emptyMap(), false);
		strategy.setStaleWorkingMemoryEntries(staleSet);
		strategy.setCurrentAssignment(assignment);

		List<ImmutablePair<Substitution, AssignmentStatus>> result = strategy.getAcceptedSubstitutions(new BasicLiteral(nonGroundAtom, true),
				new Substitution());
		assertTrue(result.isEmpty());

		assertEquals(1, staleSet.size());
		assertTrue(staleSet.contains(groundAtom));
	}

}
	