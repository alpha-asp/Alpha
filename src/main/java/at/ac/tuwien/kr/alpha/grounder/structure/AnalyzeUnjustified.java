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
	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules;
	private final PredicateDependencyGraph predicateDependencyGraph;
	private final AtomStore atomStore;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private int renamingCounter;
	private int padDepth;

	public AnalyzeUnjustified(Program program, AtomStore atomStore, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram) {
		this.atomStore = atomStore;
		this.factsFromProgram = factsFromProgram;
		predicateDefiningRules = new LinkedHashMap<>();
		predicateDependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
		padDepth = 0;
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
		padDepth = 0;
		BasicAtom literal = (BasicAtom) atomStore.get(atomToJustify);
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

	private Set<Literal> analyze(BasicAtom atom, Assignment currentAssignment) {
		log(pad("Starting analyze, current assignment is: " + currentAssignment));
		LinkedHashSet<Literal> vL = new LinkedHashSet<>();
		LinkedHashSet<LitSet> vToDo = new LinkedHashSet<>(Collections.singleton(new LitSet(atom, new LinkedHashSet<>())));
		LinkedHashSet<LitSet> vDone = new LinkedHashSet<>();
		while (!vToDo.isEmpty()) {
			Iterator<LitSet> it = vToDo.iterator();
			LitSet x = it.next();
			it.remove();
			log("");
			log("Treating now: " + x);
			vDone.add(x);
			LinkedHashSet<LitSet> caredFor = new LinkedHashSet<>(vToDo);
			caredFor.addAll(vDone);
			ReturnExplainUnjust unjustRet = explainUnjust(x, caredFor, currentAssignment);
			log("Additional ToDo: " + unjustRet.vToDo);
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

	private ReturnExplainUnjust explainUnjust(LitSet x, LinkedHashSet<LitSet> vD, Assignment currentAssignment) {
		padDepth += 2;
		log("Begin explainUnjust(): " + x);
		log("Done already: " + vD);
		Atom p = x.getLiteral();

		ReturnExplainUnjust ret = new ReturnExplainUnjust();

		// Line 2: construct set of all 'rules' such that head unifies with p.
		log("Line 2.");
		List<RuleAndUnifier> rulesUnifyingWithP = rulesHeadUnifyingWith(p);
		log("Rules unifying with " + p + " are: " + rulesUnifyingWithP);
		rulesLoop:
		for (RuleAndUnifier ruleUnifier : rulesUnifyingWithP) {
			Substitution sigma = ruleUnifier.unifier;
			List<Literal> bodyR = ruleUnifier.ruleBody;
			Atom sigmaHr = ruleUnifier.originalHead.substitute(sigma);
			log("Considering now: " + ruleUnifier);
			// Line 3 below.
			log("Line 3.");
			Set<Substitution> vN = new LinkedHashSet<>(x.getComplementSubstitutions());
			for (Substitution sigmaN : vN) {
				// FIXME: changed below line to keep sigmaHr unchanged. Deviates from described Algorithm.
				if (Unification.unifyRightAtom(sigmaHr, p.substitute(sigmaN)) != null) {
					log("Line 4.");
					log("Unifier is excluded by: " + sigmaN);
					continue rulesLoop;
				}
			}
			log("Line 5.");
			Set<Substitution> vNp = new LinkedHashSet<>();
			for (Substitution substitution : vN) {
				//vNp.add(new Substitution(sigma).extendWith(substitution));
				Substitution merged = Substitution.mergeIntoLeft(substitution, sigma);
				// Ignore inconsistent merges.
				if (merged == null) {
					continue;
				}
				vNp.add(merged);
			}
			log("Adapting N to N'. Original N is " + vN);
			log("Adapted N' is " + vNp);
			// Line 6.
			log("Line 6.");
			for (Literal lit : bodyR) {
				if (lit.isNegated()) {
					continue;
				}
				Atom pB = lit.substitute(sigma);
				log("Checking whether (subsituted) body literal " + pB + " is already covered.");
				for (LitSet litSet : vD) {
					log("Checking whether " + pB + " is covered by: " + litSet);
					Atom pD = litSet.getLiteral();
					Set<Substitution> vND = litSet.getComplementSubstitutions();
					Substitution sigmad = Unification.unifyRightAtom(pB, pD);	// FIXME: different from paper, only unify pD, leave pB unchanged!
					if (sigmad == null) {
						log("Does not unify, skipping.");
						continue;
					}
					log("LitSet atom unifies with " + pB);
					// Line 6.
					log("Line 7.");
					// FIXME: different from paper, we use N' here to check if some instances are excluded by N', so that the cover still holds
					// FIXME: line 7 should read: if (pD, ND) covers (σd(pB), N')
					boolean isCovered = true;
					exceptionLoop:
					for (Substitution sigmaND : vND) {
						if (Unification.unifyRightAtom(pD.substitute(sigmaND), pB.substitute(sigmad)) != null) {
							log("Checking if seemingly excluding " + sigmaND + " is not also excluded by N'.");
							// Check coverage regarding N': if excluding substitution is also excluded by N' then this is still covered.
							for (Substitution sigmaNp : vNp) {
								if (Unification.unifyRightAtom(pD.substitute(sigmaND), pB.substitute(sigmaNp)) != null) {
									log("Excluding substitution " + sigmaND + " is itself excluded by " + sigmaNp);
									continue exceptionLoop;
								}
							}
							isCovered = false;
							log("Excluded " + sigmaND + " is not covered (and not excluded by N').");
							break;
						}
					}
					if (isCovered) {
						log("Line 8.");
						log(pB.substitute(sigmad) + " is covered by " + litSet);
						Substitution sigmacirc = new Substitution(sigma).extendWith(sigmad);
						vNp.add(sigmacirc);
						log("Extending litset N' with: " + sigmacirc + " coming from " + sigma + " and " + sigmad);
					} else {
						log("LitSet is not covered, ignoring.");
					}
				}
			}
			// Line 8.
			log("Line 9.");
			log("Searching for falsified negated literals in the body: " + bodyR);
			for (Literal lit : bodyR) {
				if (!lit.isNegated()) {
					continue;
				}
				Atom lb = lit.substitute(sigma);
				log("Found: " + lit + ", searching falsifying ground instances of " + lb + " (with unifier from the head) now.");
				for (Atom lg : getAssignedAtomsOverPredicate(lb.getPredicate())) {
					log("Considering: " + lg);
					if (atomStore.contains(lg)) {
						int atomId = atomStore.getAtomId(lg);
						if (!currentAssignment.getTruth(atomId).toBoolean()) {
							log("" + lg + " is not assigned TRUE or MBT. Skipping.");
							continue;
						}
					} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
					log("" + lg + " is TRUE or MBT.");
					Substitution sigmagb = Unification.unifyAtoms(lg, lb);
					if (sigmagb == null) {
						log("" + lg + " does not unify with " + lb + "");
						continue;
					}
					// Line 9.
					log("Line 10.");
					log("Checking if " + lb + " is already covered.");
					boolean isCovered = false;
					for (Substitution sigmaN : vN) {
						// FIXME: change from algorithm, only allow sigmaN(p) to be modified for unification.
						if (Unification.unifyRightAtom(sigmaHr.substitute(sigmagb), p.substitute(sigmaN)) != null) {
							log(lb + " is already covered by " + sigmaN);
							isCovered = true;
							break;
						}
					}
					if (!isCovered) {
						// Line 10.
						log("Line 11.");
						Substitution sigmacirc = new Substitution(sigma).extendWith(sigmagb);
						vNp.add(sigmacirc);
						log("Literal " + lg + " is not excluded and falsifies body literal " + lit);
						ret.vL.add((Literal) lg);
						log("Reasons extended by: " + lg);
					}
				}

			}
			// Line 11.
			log("Line 12.");
			List<Literal> bodyPos = new ArrayList<>();
			for (Literal literal : bodyR) {
				if (!literal.isNegated()) {
					bodyPos.add(literal);
				}
			}
			log("Calling UnjustCover() for positive body.");
			ret.vToDo.addAll(unjustCoverFixed(bodyPos, Collections.singleton(sigma), vNp, currentAssignment));
		}
		log("End explainUnjust().");
		padDepth -= 2;
		return ret;
	}

	private Set<LitSet> unjustCoverFixed(List<Literal> vB, Set<Substitution> vY, Set<Substitution> vN, Assignment currentAssignment) {
		padDepth += 2;
		log("Begin UnjustCoverFixed()");
		log("Finding unjustified body literals in: " + vB + " / " + vY + " excluded " + vN);
		Set<LitSet> ret = new LinkedHashSet<>();
		// Line 1.
		if (vB.isEmpty() || vY.isEmpty()) {
			// Line 2.
			log("Line 2.");
			log("End unjustCoverFixed().");
			padDepth -= 2;
			return Collections.emptySet();
		}
		// Line 3.
		log("Line 3.");
		int chosenLiteralPos = 0;
		Literal b = vB.get(chosenLiteralPos);
		log("Picked literal from body is: " + b);
		// Line 4.
		for (Substitution sigmaY : vY) {
			ArrayList<Literal> newB = new ArrayList<>(vB);        // FIXME: need to create copy of body list for each substitution. Differs from described algorithm.
			log("Line 4.");
			Atom bSigma = b.substitute(sigmaY);
			log("Treating substitution for: " + bSigma);
			Set<Substitution> vYp = new LinkedHashSet<>();

			log("Checking atoms over predicate: " + b.getPredicate());
			List<Atom> assignedAtomsOverPredicate = getAssignedAtomsOverPredicate(b.getPredicate());
			atomLoop:
			for (Atom atom : assignedAtomsOverPredicate) {
				// Check that atom is justified/true.
				log("Checking atom: " + atom);
				if (atomStore.contains(atom)) {
					int atomId = atomStore.getAtomId(atom);
					if (currentAssignment.getTruth(atomId) != ThriceTruth.TRUE) {
						log("Atom is not TRUE. Skipping.");
						continue;
					}
				} // Note: in case the atom is not in the atomStore, it came from a fact and hence is true.
				Substitution sigmap = Unification.unifyRightAtom(atom, b);
				if (sigmap == null) {
					log("Atom does not unify with picked body literal.");
					continue;
				}

				Atom bSigmap = b.substitute(sigmap);
				if (!bSigmap.isGround()) {
					throw oops("Resulting atom is not ground.");
				}
				if (Unification.unifyAtoms(bSigma, bSigmap) != null) {
					for (Substitution sigmaN : vN) {
						// FIXME: maybe this should use sigma' \circ sigma, like Bart wrote? Since bSigmap is ground, this should be implicitly given already.
						if (Unification.unifyAtoms(b.substitute(sigmaN), bSigmap) != null) {
							log("Atom is excluded by: " + sigmaN);
							continue atomLoop;
						}
					}
					log("Adding corresponding substitution to Y': " + sigmap);
					vYp.add(sigmap);
					// TODO: sigmap should be more precise than sigma!
					// TODO: more precise =?= sigma unifies into sigmap on normalized atom and variables of more precise is superset of variables assigned by less precise?
				}
			}

			log("Unjustified body literals: " + vYp);

			// Line 5.
			log("Line 5.");
			Set<Substitution> vYpUN = new LinkedHashSet<>();
			vYpUN.addAll(vYp);
			vYpUN.addAll(vN);
			/*for (Substitution sigmaN : vN) {
				Substitution merged = Substitution.mergeIntoLeft(sigmaY, sigmaN);
				// Ignore inconsistent merges.
				if (merged == null) {
					continue;
				}
				//Substitution sigmaNsimga = new Substitution(sigmaN).extendWith(sigmaY);
				vYpUN.add(merged);
			}*/
			LitSet toJustify = new LitSet(bSigma, vYpUN);
			if (!toJustify.coversNothing()) {
				log("New litset to do: " + toJustify);
				ret.add(toJustify);
			} else {
				log("Generated LitSet covers nothing. Ignoring: " + toJustify);
			}
			newB.remove(chosenLiteralPos);
			ret.addAll(unjustCoverFixed(newB, vYp, vN, currentAssignment));
			log("Literal set(s) to treat: " + ret);
		}
		log("End unjustCoverFixed().");
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
				headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			} else {
				// Create atom and empty rule body out of instance.
				headAtom = new BasicAtom(p.getPredicate(), factOrNonGroundRule.factInstance.terms);
				renamedBody = Collections.emptyList();
			}
			// Unify rule head with literal to justify.
			Substitution unifier = Unification.unifyAtoms(p, headAtom);
			// Note: maybe it is faster to first check unification and only rename the whole rule afterwards?
			// Skip if unification failed.
			if (unifier == null) {
				continue;
			}
			rulesWithUnifier.add(new RuleAndUnifier(renamedBody, unifier, headAtom));
		}
		return rulesWithUnifier;
	}

	private void log(String msg) {
		LOGGER.trace(pad(msg));
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

		private RuleAndUnifier(List<Literal> ruleBody, Substitution unifier, Atom originalHead) {
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
