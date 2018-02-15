package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.*;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class AnalyzeUnjustified {

	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules;
	private final PredicateDependencyGraph predicateDependencyGraph;
	private final AtomStore atomStore;
	private final WorkingMemory workingMemory;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private int renamingCounter;

	public AnalyzeUnjustified(Program program, AtomStore atomStore, WorkingMemory workingMemory, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram) {
		this.atomStore = atomStore;
		this.workingMemory = workingMemory;
		this.factsFromProgram = factsFromProgram;
		predicateDefiningRules = new LinkedHashMap<>();
		predicateDependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new LinkedHashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	private Map<Predicate, List<Atom>> assignedAtoms;
	public Set<Literal> analyze(int atomToJustify, Assignment currentAssignment) {
		Atom literal = atomStore.get(atomToJustify);
		assignedAtoms = new LinkedHashMap<>();
		for (int i = 1; i <= atomStore.getHighestAtomId(); i++) {
			Assignment.Entry entry = currentAssignment.get(i);
			if (entry == null) {
				continue;
			}
			Atom assignedAtom = atomStore.get(i);
			assignedAtoms.putIfAbsent(assignedAtom.getPredicate(), new ArrayList<>());
			assignedAtoms.get(assignedAtom.getPredicate()).add(assignedAtom);
		}
		return analyze(literal, currentAssignment);
	}

	private Set<Literal> analyze(Atom atom, Assignment currentAssignment) {
		LinkedHashSet<Literal> vL = new LinkedHashSet<>();
		LinkedHashSet<LitSet> vToDo = new LinkedHashSet<>(Collections.singleton(new LitSet(atom, new LinkedHashSet<>())));
		LinkedHashSet<LitSet> vDone = new LinkedHashSet<>();
		while (!vToDo.isEmpty()) {
			Iterator<LitSet> it = vToDo.iterator();
			LitSet x = it.next();
			it.remove();
			vDone.add(x);
			LinkedHashSet<LitSet> caredFor = new LinkedHashSet<>(vToDo);
			caredFor.addAll(vDone);
			ReturnExplainUnjust unjustRet = explainUnjust(x, caredFor, currentAssignment);
			vToDo.addAll(unjustRet.vToDo);
			vL.addAll(unjustRet.vL);
		}
		return vL;
	}

	private ReturnExplainUnjust explainUnjust(LitSet x, LinkedHashSet<LitSet> vD, Assignment currentAssignment) {
		Atom p = x.getLiteral();
		Set<Substitution> vN = new LinkedHashSet<>(x.getComplementSubstitutions());
		ReturnExplainUnjust ret = new ReturnExplainUnjust();

		// Line 2: construct set of all 'rules' such that head unifies with p.
		List<RuleAndUnifier> rulesUnifyingWithP = rulesHeadUnifyingWith(p);
		rulesLoop:
		for (RuleAndUnifier ruleUnifier : rulesUnifyingWithP) {
			Substitution sigma = ruleUnifier.unifier;
			List<Literal> bodyR = ruleUnifier.ruleBody;
			Atom sigmaHr = ruleUnifier.originalHead.substitute(sigma);
			// Line 3 below.
			for (Substitution sigmaN : vN) {
				if (Unification.unifyAtoms(p.substitute(sigmaN), sigmaHr) != null) {
					continue rulesLoop;
				}
			}
			// Line 5.
			for (Literal lit : bodyR) {
				/*if (!lit.isNegated()) {
					continue;
				}*/
				Atom pB = lit.substitute(sigma);
				for (LitSet litSet : vD) {
					Atom pD = litSet.getLiteral();
					Set<Substitution> vND = litSet.getComplementSubstitutions();
					Substitution sigmad = Unification.unifyAtoms(pD, pB);
					if (sigmad == null) {
						continue;
					}
					// Line 6.
					boolean isCovered = true;
					for (Substitution sigmaND : vND) {
						if (Unification.unifyAtoms(pD.substitute(sigmaND), pB.substitute(sigmad)) != null) {
							isCovered = false;
							break;
						}
					}
					if (isCovered) {
						Substitution sigmacirc = new Substitution(sigma).extendWith(sigmad);
						vN.add(sigmacirc);
					}
				}
			}
			// Line 8.
			for (Literal lit : bodyR) {
				if (!lit.isNegated()) {
					continue;
				}
				Atom lb = lit.substitute(sigma);
				for (Atom lg : getAssignedAtomsOverPredicate(lb.getPredicate())) {
					if (atomStore.contains(lg)) {
						int atomId = atomStore.getAtomId(lg);
						if (currentAssignment.getTruth(atomId) != ThriceTruth.TRUE) {
							continue;
						}
					} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
					Substitution sigmagb = Unification.unifyAtoms(lg, lb);
					if (sigmagb == null) {
						continue;
					}
					// Line 9.
					boolean isCovered = false;
					for (Substitution sigmaN : vN) {
						if (Unification.unifyAtoms(p.substitute(sigmaN), sigmaHr.substitute(sigmagb)) != null) {
							isCovered = true;
							break;
						}
					}
					if (!isCovered) {
						// Line 10
						Substitution sigmacirc = sigma.extendWith(sigmagb);
						vN.add(sigmacirc);
						ret.vL.add((Literal) lg);
					}
				}

			}
			// Line 11.
			List<Literal> bodyPos = new ArrayList<>();
			for (Literal literal : bodyR) {
				if (!literal.isNegated()) {
					bodyPos.add(literal);
				}
			}
			ret.vToDo.addAll(unjustCover(bodyPos, Collections.singleton(sigma), vN, currentAssignment));
		}
		return ret;
	}

	private Set<LitSet> unjustCover(List<Literal> vB, Set<Substitution> vY, Set<Substitution> vN, Assignment currentAssignment) {
		Set<LitSet> ret = new LinkedHashSet<>();
		// Line 1.
		if (vB.isEmpty() || vY.isEmpty()) {
			// Line 2.
			return Collections.emptySet();
		}
		// Line 3.
		Literal b = vB.get(0);

		// Line 4.
		Set<Substitution> vYp = new LinkedHashSet<>();
		List<Atom> assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(b.getPredicate());
		for (Atom atom : assignedAtomsOverPredicate) {
			// Check that atom is justified/true.
			if (atomStore.contains(atom)) {
				int atomId = atomStore.getAtomId(atom);
				if (currentAssignment.getTruth(atomId) != ThriceTruth.TRUE) {
					continue;
				}
			} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
			Substitution sigma = Unification.unifyRightAtom(atom, b);
			if (sigma == null) {
				continue;
			}
			unifierLoop:
			for (Substitution sigmaY : vY) {
				if (Unification.unifyAtoms(b.substitute(sigmaY), b.substitute(sigma)) != null) {
					for (Substitution sigmaN : vN) {
						if (Unification.unifyAtoms(b.substitute(sigmaN), b.substitute(sigma)) != null) {
							continue unifierLoop;
						}
					}
					vYp.add(sigma);
					break;
				}
				/*if (Substitution.isMorePrecise(sigma, sigmaY)) {
					for (Substitution sigmaN : vN) {
						if (Substitution.isMorePrecise(sigmaN, sigma)) {
							continue unifierLoop;
						}
					}
					vYp.add(sigma);
					break;
				}*/
			}
		}

		// Line 5.
		Set<Substitution> vYpUN = new LinkedHashSet<>(vYp);
		vYpUN.addAll(vN);
		ret.add(new LitSet(b, vYpUN));
		vB.remove(0);
		ret.addAll(unjustCover(vB, vYp, vN, currentAssignment));
		return ret;
	}

	private List<Atom> getAssignedAtomsOverPredicate(Predicate predicate) {
		// Find more substitutions, consider currentAssignment.
		List<Atom> assignedAtoms = this.assignedAtoms.get(predicate);
		List<Atom> assignedAtomsOverPredicate = new ArrayList<>(assignedAtoms != null ? assignedAtoms : Collections.emptyList());
		// Add instances from facts.
		LinkedHashSet<Instance> factsOverPredicate = factsFromProgram.get(predicate);
		if (factsOverPredicate != null) {
			for (Instance factInstance : factsOverPredicate) {
				assignedAtomsOverPredicate.add(new BasicAtom(predicate, factInstance.terms));
			}
		}
		return assignedAtomsOverPredicate;
	}


	private boolean coveredBy(Atom q, LitSet litSet) {
		Atom l = litSet.getLiteral();
		for (Substitution substitution : litSet.getComplementSubstitutions()) {
			if (Unification.unifyRightAtom(q, l.substitute(substitution)) != null) {
				return false;
			}
		}
		return true;
	}

	private boolean coveredBy(Substitution sigma, Set<Substitution> vN) {
		for (Substitution complement : vN) {
			if (Substitution.isMorePrecise(sigma, complement)) {
				continue;
			}
			return true;
		}
		return false;
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

		HashSet<NonGroundRule> rulesDefiningPredicate = getPredicateDefiningRules().get(predicate);
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
				// Unify rule head with literal to justify.
				headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			} else {
				// Create atom and empty rule body out of instance.
				headAtom = new BasicAtom(p.getPredicate(), factOrNonGroundRule.factInstance.terms);
				renamedBody = Collections.emptyList();
			}
			Substitution unifier = Unification.unifyAtoms(p, headAtom);
			// Skip if unification failed.
			if (unifier == null) {
				continue;
			}
			rulesWithUnifier.add(new RuleAndUnifier(renamedBody, unifier, factOrNonGroundRule, headAtom));
		}
		return rulesWithUnifier;
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
		final Substitution unifier;
		final Atom originalHead;

		private RuleAndUnifier(List<Literal> ruleBody, Substitution unifier, FactOrNonGroundRule original, Atom originalHead) {
			this.ruleBody = ruleBody;
			this.unifier = unifier;
			this.originalHead = originalHead;
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
