package at.ac.tuwien.kr.alpha.core.grounder.structure;

import at.ac.tuwien.kr.alpha.api.program.*;
import at.ac.tuwien.kr.alpha.core.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.core.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.core.atoms.CoreAtom;
import at.ac.tuwien.kr.alpha.core.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.core.common.Assignment;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.CorePredicate;
import at.ac.tuwien.kr.alpha.core.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.core.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.core.grounder.Instance;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.grounder.Unifier;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.solver.ThriceTruth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static at.ac.tuwien.kr.alpha.core.util.Util.oops;

import java.util.*;

/**
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class AnalyzeUnjustified {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeUnjustified.class);
	private final InternalProgram programAnalysis;
	private final AtomStore atomStore;
	private final Map<CorePredicate, LinkedHashSet<Instance>> factsFromProgram;
	private int renamingCounter;
	private int padDepth;

	public AnalyzeUnjustified(InternalProgram programAnalysis, AtomStore atomStore, Map<CorePredicate, LinkedHashSet<Instance>> factsFromProgram) {
		this.programAnalysis = programAnalysis;
		this.atomStore = atomStore;
		this.factsFromProgram = factsFromProgram;
		padDepth = 0;
	}

	private Map<CorePredicate, List<CoreAtom>> assignedAtoms;

	public Set<CoreLiteral> analyze(int atomToJustify, Assignment currentAssignment) {
		padDepth = 0;
		CoreAtom atom = atomStore.get(atomToJustify);
		if (!(atom instanceof BasicAtom)) {
			throw oops("Starting atom must be a BasicAtom, but received: " + atom + " of type: " + atom.getClass());
		}
		//@formatter:off
		// Calling code must make sure it is a BasicAtom and take precautions.
		// Potential solutions:
		// If atom instanceof RuleAtom and atom is MBT, then the corresponding rule body has a BasicAtom that is MBT.
		// If atom instanceof ChoiceAtom and atom is MBT, then the corresponding rule body has a BasicAtom that is MBT.
		// If atom instanceof RuleAtom and atom is FALSE, then this comes from a violated constraint in the end and the corresponding rule body can be taken as the single rule deriving the RuleAtom.
		//@formatter:on
		assignedAtoms = new LinkedHashMap<>();
		for (int i = 1; i <= atomStore.getMaxAtomId(); i++) {
			ThriceTruth truth = currentAssignment.getTruth(i);
			if (truth == null) {
				continue;
			}
			CoreAtom assignedAtom = atomStore.get(i);
			assignedAtoms.putIfAbsent(assignedAtom.getPredicate(), new ArrayList<>());
			assignedAtoms.get(assignedAtom.getPredicate()).add(assignedAtom);
		}
		return analyze((BasicAtom) atom, currentAssignment);
	}

	private Set<CoreLiteral> analyze(BasicAtom atom, Assignment currentAssignment) {
		log(pad("Starting analyze, current assignment is: {}"), currentAssignment);
		LinkedHashSet<CoreLiteral> vL = new LinkedHashSet<>();
		LinkedHashSet<LitSet> vToDo = new LinkedHashSet<>(Collections.singleton(new LitSet(atom, new LinkedHashSet<>())));
		LinkedHashSet<LitSet> vDone = new LinkedHashSet<>();
		while (!vToDo.isEmpty()) {
			Iterator<LitSet> it = vToDo.iterator();
			LitSet x = it.next();
			it.remove();
			log("");
			log("Treating now: {}", x);
			vDone.add(x);
			ReturnExplainUnjust unjustRet = explainUnjust(x, currentAssignment);
			log("Additional ToDo: {}", unjustRet.vToDo);
			// Check each new LitSet if it does not already occur as done.
			for (LitSet todoLitSet : unjustRet.vToDo) {
				if (!vDone.contains(todoLitSet)) {
					vToDo.add(todoLitSet);
				}
			}
			vL.addAll(unjustRet.vL);
		}
		return vL;
	}

	private ReturnExplainUnjust explainUnjust(LitSet x, Assignment currentAssignment) {
		padDepth += 2;
		log("Begin explainUnjust(): {}", x);
		CoreAtom p = x.getAtom();

		ReturnExplainUnjust ret = new ReturnExplainUnjust();

		// Construct set of all 'rules' such that head unifies with p.
		List<RuleAndUnifier> rulesUnifyingWithP = rulesHeadUnifyingWith(p);
		log("Rules unifying with {} are {}", p, rulesUnifyingWithP);
		rulesLoop:
		for (RuleAndUnifier ruleUnifier : rulesUnifyingWithP) {
			Unifier sigma = ruleUnifier.unifier;
			Set<CoreLiteral> bodyR = ruleUnifier.ruleBody;
			CoreAtom sigmaHr = ruleUnifier.originalHead.substitute(sigma);
			log("Considering now: {}", ruleUnifier);
			Set<Unifier> vN = new LinkedHashSet<>(x.getComplementSubstitutions());
			for (Unifier sigmaN : vN) {
				if (Unification.instantiate(p.substitute(sigmaN), sigmaHr) != null) {
					log("Unifier is excluded by: {}", sigmaN);
					continue rulesLoop;
				}
			}
			Set<Unifier> vNp = new LinkedHashSet<>();
			for (Unifier substitution : vN) {
				Unifier merged = Unifier.mergeIntoLeft(substitution, sigma);
				// Ignore inconsistent merges.
				if (merged == null) {
					continue;
				}
				vNp.add(merged);
			}
			log("Adapting N to N'. Original N is {}", vN);
			log("Adapted N' is {}", vNp);
			log("Searching for falsified negated literals in the body: {}", bodyR);
			for (CoreLiteral lit : bodyR) {
				if (!lit.isNegated()) {
					continue;
				}
				CoreAtom lb = lit.getAtom().substitute(sigma);
				log("Found: {}, searching falsifying ground instances of {} (with unifier from the head) now.", lit, lb);
				AssignedAtomsIterator assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(lb.getPredicate());
				while (assignedAtomsOverPredicate.hasNext()) {
					CoreAtom lg = assignedAtomsOverPredicate.next();
					log("Considering: {}", lg);
					if (atomStore.contains(lg)) {
						int atomId = atomStore.get(lg);
						if (!currentAssignment.getTruth(atomId).toBoolean()) {
							log("{} is not assigned TRUE or MBT. Skipping.", lg);
							continue;
						}
					} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
					log("{} is TRUE or MBT.", lg);
					Unifier sigmagb = Unification.unifyAtoms(lg, lb);
					if (sigmagb == null) {
						log("{} does not unify with {}", lg, lb);
						continue;
					}
					log("Checking if {} is already covered.", lb);
					boolean isCovered = false;
					for (Unifier sigmaN : vN) {
						if (Unification.instantiate(p.substitute(sigmaN), sigmaHr.substitute(sigmagb)) != null) {
							log("{} is already covered by {}", lb, sigmaN);
							isCovered = true;
							break;
						}
					}
					if (!isCovered) {
						Unifier sigmacirc = new Unifier(sigma).extendWith(sigmagb);
						vNp.add(sigmacirc);
						log("Literal {} is not excluded and falsifies body literal {}", lg, lit);
						ret.vL.add(lg.toLiteral());
						log("Reasons extended by: {}", lg);
					}
				}

			}
			List<CoreLiteral> bodyPos = new ArrayList<>();
			for (CoreLiteral literal : bodyR) {
				if (!literal.isNegated()) {
					bodyPos.add(literal);
				}
			}
			log("Calling UnjustCover() for positive body.");
			ret.vToDo.addAll(unjustCover(bodyPos, Collections.singleton(sigma), vNp, currentAssignment));
		}
		log("End explainUnjust().");
		padDepth -= 2;
		return ret;
	}

	private Set<LitSet> unjustCover(List<CoreLiteral> vB, Set<Unifier> vY, Set<Unifier> vN, Assignment currentAssignment) {
		padDepth += 2;
		log("Begin UnjustCoverFixed()");
		log("Finding unjustified body literals in: {} / {} excluded {}", vB, vY, vN);
		Set<LitSet> ret = new LinkedHashSet<>();
		if (vB.isEmpty() || vY.isEmpty()) {
			log("End unjustCover().");
			padDepth -= 2;
			return Collections.emptySet();
		}
		int chosenLiteralPos = 0;
		// Find a body literal that is not a ComparisonLiteral, because these do not generate/have atoms assigned.
		for (int i = 0; i < vB.size(); i++) {
			if (!(vB.get(i) instanceof ComparisonLiteral)) {
				chosenLiteralPos = i;
				break;
			}
		}
		CoreAtom b = vB.get(chosenLiteralPos).getAtom();
		log("Picked literal from body is: {}", b);
		for (Unifier sigmaY : vY) {
			CoreAtom bSigmaY = b.substitute(sigmaY);
			log("Treating substitution for: {}", bSigmaY);
			Set<Unifier> vYp = new LinkedHashSet<>();

			log("Checking atoms over predicate: {}", b.getPredicate());
			AssignedAtomsIterator assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(b.getPredicate());
			atomLoop:
			while (assignedAtomsOverPredicate.hasNext()) {
				CoreAtom atom = assignedAtomsOverPredicate.next();
				// Check that atom is justified/true.
				log("Checking atom: {}", atom);
				if (atomStore.contains(atom)) {
					int atomId = atomStore.get(atom);
					if (currentAssignment.getTruth(atomId) != ThriceTruth.TRUE) {
						log("Atom is not TRUE. Skipping.");
						continue;
					}
				} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
				Unifier sigma = Unification.instantiate(b, atom);
				if (sigma == null) {
					log("Atom does not unify with picked body literal.");
					continue;
				}

				CoreAtom bSigma = b.substitute(sigma);
				if (!bSigma.isGround()) {
					throw oops("Resulting atom is not ground.");
				}
				Set<VariableTerm> variablesOccurringInSigma = sigma.getMappedVariables();
				if (Unification.instantiate(bSigmaY, bSigma) != null) {
					for (Unifier sigmaN : vN) {
						ArrayList<CoreTerm> occurringVariables = new ArrayList<>(variablesOccurringInSigma);
						occurringVariables.addAll(sigmaN.getMappedVariables());
						BasicAtom genericAtom = new BasicAtom(CorePredicate.getInstance("_", occurringVariables.size(), true), occurringVariables);
						CoreAtom genericSubstituted = genericAtom.substitute(sigmaN).renameVariables("_analyzeTest");
						if (Unification.instantiate(genericSubstituted, genericAtom.substitute(sigma)) != null) {
							log("Atom {} is excluded by: {} via {}", genericSubstituted, sigmaN, sigma);
							continue atomLoop;
						}
					}
					log("Adding corresponding substitution to Y': {}", sigma);
					vYp.add(sigma);
				}
			}

			log("Unjustified body literals: {}", vYp);

			Set<Unifier> vYpUN = new LinkedHashSet<>();
			vYpUN.addAll(vYp);
			vYpUN.addAll(vN);
			LitSet toJustify = new LitSet(bSigmaY, vYpUN);
			if (!toJustify.coversNothing()) {
				log("New litset to do: {}", toJustify);
				ret.add(toJustify);
			} else {
				log("Generated LitSet covers nothing. Ignoring: {}", toJustify);
			}
			ArrayList<CoreLiteral> newB = new ArrayList<>(vB);
			newB.remove(chosenLiteralPos);
			ret.addAll(unjustCover(newB, vYp, vN, currentAssignment));
			log("Literal set(s) to treat: {}", ret);
		}
		log("End unjustCover().");
		padDepth -= 2;
		return ret;
	}

	private String pad(String string) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < padDepth; i++) {
			sb.append(" ");
		}
		sb.append(string);
		return sb.toString();
	}

	private AssignedAtomsIterator getAssignedAtomsOverPredicate(CorePredicate predicate) {
		// Find more substitutions, consider currentAssignment.
		List<CoreAtom> assignedAtoms = this.assignedAtoms.get(predicate);
		// Consider instances from facts.
		LinkedHashSet<Instance> factsOverPredicate = factsFromProgram.get(predicate);
		return new AssignedAtomsIterator(predicate, assignedAtoms, factsOverPredicate);
	}

	private static class AssignedAtomsIterator implements Iterator<CoreAtom> {
		private final CorePredicate predicate;
		private final Iterator<CoreAtom> assignedAtomsIterator;
		private final Iterator<Instance> factsIterator;

		public AssignedAtomsIterator(CorePredicate predicate, List<CoreAtom> assignedAtoms, Set<Instance> facts) {
			this.predicate = predicate;
			this.assignedAtomsIterator = assignedAtoms == null ? Collections.emptyIterator() : assignedAtoms.iterator();
			this.factsIterator = facts == null ? Collections.emptyIterator() : facts.iterator();
		}

		@Override
		public boolean hasNext() {
			return assignedAtomsIterator.hasNext() || factsIterator.hasNext();
		}

		@Override
		public CoreAtom next() {
			if (assignedAtomsIterator.hasNext()) {
				return assignedAtomsIterator.next();
			}
			if (factsIterator.hasNext()) {
				return new BasicAtom(predicate, factsIterator.next().terms);
			}
			throw new NoSuchElementException();
		}
	}

	private List<RuleAndUnifier> rulesHeadUnifyingWith(CoreAtom p) {

		List<RuleAndUnifier> rulesWithUnifier = new ArrayList<>();
		CorePredicate predicate = p.getPredicate();

		ArrayList<FactOrNonGroundRule> definingRulesAndFacts = new ArrayList<>();
		// Get facts over the same predicate.
		LinkedHashSet<Instance> factInstances = factsFromProgram.get(predicate);
		if (factInstances != null) {
			for (Instance factInstance : factInstances) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(factInstance));
			}
		}

		HashSet<InternalRule> rulesDefiningPredicate = programAnalysis.getPredicateDefiningRules().get(predicate);
		if (rulesDefiningPredicate != null) {
			for (InternalRule nonGroundRule : rulesDefiningPredicate) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(nonGroundRule));
			}
		}
		for (FactOrNonGroundRule factOrNonGroundRule : definingRulesAndFacts) {
			boolean isNonGroundRule = factOrNonGroundRule.nonGroundRule != null;
			Set<CoreLiteral> renamedBody;
			CoreAtom headAtom;
			if (isNonGroundRule) {
				// First rename all variables in the rule.
				InternalRule rule = factOrNonGroundRule.nonGroundRule.renameVariables("_" + renamingCounter++);
				renamedBody = rule.getBody();
				headAtom = rule.getHeadAtom();
			} else {
				// Create atom and empty rule body out of instance.
				headAtom = new BasicAtom(p.getPredicate(), factOrNonGroundRule.factInstance.terms);
				renamedBody = Collections.emptySet();
			}
			// Unify rule head with literal to justify.
			Unifier unifier = Unification.unifyAtoms(p, headAtom);
			// Note: maybe it is faster to first check unification and only rename the whole rule afterwards?
			// Skip if unification failed.
			if (unifier == null) {
				continue;
			}
			rulesWithUnifier.add(new RuleAndUnifier(renamedBody, unifier, headAtom));
		}
		return rulesWithUnifier;
	}

	private void log(String msg, Object... refs) {
		LOGGER.trace(pad(msg), refs);
	}

	private static class ReturnExplainUnjust {
		Set<CoreLiteral> vL;
		Set<LitSet> vToDo;

		ReturnExplainUnjust() {
			vL = new LinkedHashSet<>();
			vToDo = new LinkedHashSet<>();
		}
	}

	private static class RuleAndUnifier {
		final Set<CoreLiteral> ruleBody;
		final Unifier unifier;
		final CoreAtom originalHead;

		private RuleAndUnifier(Set<CoreLiteral> ruleBody, Unifier unifier, CoreAtom originalHead) {
			this.ruleBody = ruleBody;
			this.unifier = unifier;
			this.originalHead = originalHead;
		}

		@Override
		public String toString() {
			return unifier + "@" + originalHead + " :- " + ruleBody;
		}
	}

	private static class FactOrNonGroundRule {
		final Instance factInstance;
		final InternalRule nonGroundRule;

		private FactOrNonGroundRule(Instance factInstance) {
			this.factInstance = factInstance;
			this.nonGroundRule = null;
		}

		private FactOrNonGroundRule(InternalRule nonGroundRule) {
			this.nonGroundRule = nonGroundRule;
			this.factInstance = null;
		}
	}

}
