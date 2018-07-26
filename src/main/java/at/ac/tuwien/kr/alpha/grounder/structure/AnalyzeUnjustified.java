package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.*;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class AnalyzeUnjustified {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeUnjustified.class);
	private final ProgramAnalysis programAnalysis;
	private final AtomTranslator atomStore;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private int renamingCounter;
	private int padDepth;

	public AnalyzeUnjustified(ProgramAnalysis programAnalysis, AtomTranslator atomStore, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram) {
		this.programAnalysis = programAnalysis;
		this.atomStore = atomStore;
		this.factsFromProgram = factsFromProgram;
		padDepth = 0;
	}

	private Map<Predicate, List<Atom>> assignedAtoms;
	public Set<Literal> analyze(int atomToJustify, Assignment currentAssignment) {
		padDepth = 0;
		Atom atom = atomStore.get(atomToJustify);
		if (!(atom instanceof BasicAtom)) {
			throw oops("Starting atom must be a BasicAtom, but received: " + atom + " of type: " + atom.getClass());
		}
		// Calling code must make sure it is a BasicAtom and take precautions.
		// Potential solutions:
		// If atom instanceof RuleAtom and atom is MBT, then the corresponding rule body has a BasicAtom that is MBT.
		// If atom instanceof ChoiceAtom and atom is MBT, then the corresponding rule body has a BasicAtom that is MBT.
		// If atom instanceof RuleAtom and atom is FALSE, then this comes from a violated constraint in the end and the corresponding rule body can be taken as the single rule deriving the RuleAtom.
		assignedAtoms = new LinkedHashMap<>();
		for (int i = 1; i <= atomStore.getMaxAtomId(); i++) {
			Assignment.Entry entry = currentAssignment.get(i);
			if (entry == null) {
				continue;
			}
			Atom assignedAtom = atomStore.get(i);
			assignedAtoms.putIfAbsent(assignedAtom.getPredicate(), new ArrayList<>());
			assignedAtoms.get(assignedAtom.getPredicate()).add(assignedAtom);
		}
		return analyze((BasicAtom) atom, currentAssignment);
	}

	private Set<Literal> analyze(BasicAtom atom, Assignment currentAssignment) {
		log(pad("Starting analyze, current assignment is: {}"), currentAssignment);
		LinkedHashSet<Literal> vL = new LinkedHashSet<>();
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
		Atom p = x.getAtom();

		ReturnExplainUnjust ret = new ReturnExplainUnjust();

		// Construct set of all 'rules' such that head unifies with p.
		List<RuleAndUnifier> rulesUnifyingWithP = rulesHeadUnifyingWith(p);
		log("Rules unifying with {} are {}", p, rulesUnifyingWithP);
		rulesLoop:
		for (RuleAndUnifier ruleUnifier : rulesUnifyingWithP) {
			Unifier sigma = ruleUnifier.unifier;
			List<Literal> bodyR = ruleUnifier.ruleBody;
			Atom sigmaHr = ruleUnifier.originalHead.substitute(sigma);
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
			for (Literal lit : bodyR) {
				if (!lit.isNegated()) {
					continue;
				}
				Atom lb = lit.getAtom().substitute(sigma);
				log("Found: {}, searching falsifying ground instances of {} (with unifier from the head) now.", lit, lb);
				AssignedAtomsIterator assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(lb.getPredicate());
				while (assignedAtomsOverPredicate.hasNext()) {
					Atom lg = assignedAtomsOverPredicate.next();
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
			List<Literal> bodyPos = new ArrayList<>();
			for (Literal literal : bodyR) {
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

	private Set<LitSet> unjustCover(List<Literal> vB, Set<Unifier> vY, Set<Unifier> vN, Assignment currentAssignment) {
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
		Atom b = vB.get(chosenLiteralPos).getAtom();
		log("Picked literal from body is: {}", b);
		for (Unifier sigmaY : vY) {
			Atom bSigmaY = b.substitute(sigmaY);
			log("Treating substitution for: {}", bSigmaY);
			Set<Unifier> vYp = new LinkedHashSet<>();

			log("Checking atoms over predicate: {}", b.getPredicate());
			AssignedAtomsIterator assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(b.getPredicate());
			atomLoop:
			while (assignedAtomsOverPredicate.hasNext()) {
				Atom atom = assignedAtomsOverPredicate.next();
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

				Atom bSigma = b.substitute(sigma);
				if (!bSigma.isGround()) {
					throw oops("Resulting atom is not ground.");
				}
				if (Unification.instantiate(bSigmaY, bSigma) != null) {
					for (Unifier sigmaN : vN) {
						if (Unification.instantiate(b.substitute(sigmaN), bSigma) != null) {
							log("Atom is excluded by: {}", sigmaN);
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
			ArrayList<Literal> newB = new ArrayList<>(vB);
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

	private AssignedAtomsIterator getAssignedAtomsOverPredicate(Predicate predicate) {
		// Find more substitutions, consider currentAssignment.
		List<Atom> assignedAtoms = this.assignedAtoms.get(predicate);
		// Consider instances from facts.
		LinkedHashSet<Instance> factsOverPredicate = factsFromProgram.get(predicate);
		return new AssignedAtomsIterator(predicate, assignedAtoms, factsOverPredicate);
	}

	private static class AssignedAtomsIterator implements Iterator<Atom> {
		private final Predicate predicate;
		private final Iterator<Atom> assignedAtomsIterator;
		private final Iterator<Instance> factsIterator;

		public AssignedAtomsIterator(Predicate predicate, List<Atom> assignedAtoms, Set<Instance> facts) {
			this.predicate = predicate;
			this.assignedAtomsIterator = assignedAtoms == null ? Collections.emptyIterator() : assignedAtoms.iterator();
			this.factsIterator = facts == null ? Collections.emptyIterator() : facts.iterator();
		}

		@Override
		public boolean hasNext() {
			return assignedAtomsIterator.hasNext() || factsIterator.hasNext();
		}

		@Override
		public Atom next() {
			if (assignedAtomsIterator.hasNext()) {
				return assignedAtomsIterator.next();
			}
			if (factsIterator.hasNext()) {
				return new BasicAtom(predicate, factsIterator.next().terms);
			}
			throw new NoSuchElementException();
		}
	}


	private List<RuleAndUnifier> rulesHeadUnifyingWith(Atom p) {

		List<RuleAndUnifier> rulesWithUnifier = new ArrayList<>();
		Predicate predicate = p.getPredicate();
		// Check if literal is built-in with a fixed interpretation.
		if (p instanceof FixedInterpretationLiteral) {
			return Collections.emptyList();
		}
		ArrayList<FactOrNonGroundRule> definingRulesAndFacts = new ArrayList<>();
		// Get facts over the same predicate.
		LinkedHashSet<Instance> factInstances = factsFromProgram.get(predicate);
		if (factInstances != null) {
			for (Instance factInstance : factInstances) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(factInstance));
			}
		}

		HashSet<NonGroundRule> rulesDefiningPredicate = programAnalysis.getPredicateDefiningRules().get(predicate);
		if (rulesDefiningPredicate != null) {
			for (NonGroundRule nonGroundRule : rulesDefiningPredicate) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(nonGroundRule));
			}
		}
		for (FactOrNonGroundRule factOrNonGroundRule : definingRulesAndFacts) {
			boolean isNonGroundRule = factOrNonGroundRule.nonGroundRule != null;
			List<Literal> renamedBody;
			Atom headAtom;
			if (isNonGroundRule) {
				// First rename all variables in the rule.
				Rule rule = factOrNonGroundRule.nonGroundRule.getRule().renameVariables("_" + renamingCounter++);
				renamedBody = rule.getBody();
				if (!rule.getHead().isNormal()) {
					throw oops("NonGroundRule has no normal head.");
				}
				headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			} else {
				// Create atom and empty rule body out of instance.
				headAtom = new BasicAtom(p.getPredicate(), factOrNonGroundRule.factInstance.terms);
				renamedBody = Collections.emptyList();
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
		Set<Literal> vL;
		Set<LitSet> vToDo;

		ReturnExplainUnjust() {
			vL = new LinkedHashSet<>();
			vToDo = new LinkedHashSet<>();
		}
	}

	private static class RuleAndUnifier {
		final List<Literal> ruleBody;
		final Unifier unifier;
		final Atom originalHead;

		private RuleAndUnifier(List<Literal> ruleBody, Unifier unifier, Atom originalHead) {
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
		final NonGroundRule nonGroundRule;

		private FactOrNonGroundRule(Instance factInstance) {
			this.factInstance = factInstance;
			this.nonGroundRule = null;
		}

		private FactOrNonGroundRule(NonGroundRule nonGroundRule) {
			this.nonGroundRule = nonGroundRule;
			this.factInstance = null;
		}
	}

}
