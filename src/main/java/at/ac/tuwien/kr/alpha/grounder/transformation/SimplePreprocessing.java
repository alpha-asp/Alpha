package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.grounder.Unification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simplifies an internal input program by deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<InternalProgram, InternalProgram>{

    @Override
    public InternalProgram apply(InternalProgram inputProgram) {
        List<InternalRule> srcRules = new ArrayList<>(inputProgram.getRules());
        List<InternalRule> transformedRules = new ArrayList<>();
        Iterator<InternalRule> ruleIterator = srcRules.iterator();

        while(ruleIterator.hasNext()) {
            InternalRule rule = ruleIterator.next();
            boolean redundantRule = false;

            Atom headAtom = rule.getHead().getAtom();
            Set<Literal> body = rule.getBody();
            Set<Literal> positiveBody = rule.getPositiveBody();
            Set<Literal> negativeBody = rule.getNegativeBody();

            if (checkForHeadInBody(body,headAtom)) {
                redundantRule = true;
            }
            if (checkForConflictingBodyLiterals(positiveBody,negativeBody)) {
                redundantRule = true;
            }
            //implements s0: delete duplicate rules
            if (!(redundantRule || transformedRules.contains(rule))) {
                transformedRules.add(rule);
            }
        }
        deleteConflictingRules(transformedRules, inputProgram.getFacts());

        simplifyRules(transformedRules, inputProgram.getFacts());

        return new InternalProgram(transformedRules, inputProgram.getFacts());
    }

    /**
     * This method checks if a rule contains a literal in both the positive and the negative body.
     * implements s2
     */
    private boolean checkForConflictingBodyLiterals(Set<Literal> positiveBody, Set<Literal> negativeBody) {
        Iterator<Literal> positiveLiteralIterator = positiveBody.iterator();
        while(positiveLiteralIterator.hasNext()) {
            Literal positiveLiteral = positiveLiteralIterator.next();
            //TODO: implement literal.equals()
            if (negativeBody.contains(positiveLiteral.negate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method checks if the head atom occurs in the rule's body.
     * implements s3
     */
    private boolean checkForHeadInBody(Set<Literal> body, Atom headAtom) {
        Iterator<Literal> literalIterator = body.iterator();
        while(literalIterator.hasNext()) {
            Literal literal = literalIterator.next();
            //TODO: implement atom.equals()
            if (literal.getAtom().equals(headAtom)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method deletes Rules with bodies containing not derivable literals or negated literals, that are facts.
     * implements s9
     */
    private List<InternalRule> deleteConflictingRules (List<InternalRule> rules, List<Atom> facts) {
        Iterator<InternalRule> ruleIterator = rules.iterator();
        List<InternalRule> transformedRules = new ArrayList<>();

        while(ruleIterator.hasNext()) {
            InternalRule rule = ruleIterator.next();
            Iterator<Literal> literalIterator = rule.getBody().iterator();

            while(literalIterator.hasNext()) {
                Literal literal = literalIterator.next();
                if (literal.isNegated()) {
                    if (!facts.contains(literal.getAtom())) {
                        transformedRules.add(rule);
                    }
                }
                else {
                    if (isDerivable(literal, rules)) {
                        transformedRules.add(rule);
                    }
                }
            }
        }
        return transformedRules;
    }


    /**
     * This method removes literals from rule bodies, that are already facts (when positive)
     * or not derivable (when negated).
     * implements s10
     */
    private List<InternalRule> simplifyRules (List<InternalRule> rules, List<Atom> facts) {
        Iterator<InternalRule> ruleIterator = rules.iterator();
        List<InternalRule> transformedRules = new ArrayList<>();

        while(ruleIterator.hasNext()) {
            InternalRule rule = ruleIterator.next();
            Iterator<Literal> literalIterator = rule.getBody().iterator();

            while(literalIterator.hasNext()) {
                Literal literal = literalIterator.next();
                if (literal.isNegated()) {
                    if (facts.contains(literal.getAtom())) {
                        transformedRules.add(rule);
                    }
                    else transformedRules.add(rule.removeLiteral(literal));
                }
                else {
                    if (!isDerivable(literal, rules)) {
                        transformedRules.add(rule.removeLiteral(literal));
                    }
                    else transformedRules.add(rule);
                }
            }
        }
        return transformedRules;
    }


    /**
     * This method checks whether a literal is derivable, ie. it is unifiable with the head atom of a rule.
     * implements s5 conditions
     */
    private boolean isDerivable(Literal literal, List<InternalRule> rules){
        Iterator<InternalRule> ruleIterator = rules.iterator();

        while(ruleIterator.hasNext()) {
            InternalRule rule = ruleIterator.next();
            if (Unification.unifyAtoms(literal.getAtom(),rule.getHeadAtom()) != null) {
                return true;
            }
        }
        return false;
    }
}
