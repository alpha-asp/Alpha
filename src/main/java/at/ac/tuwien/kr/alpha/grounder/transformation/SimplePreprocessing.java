package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;
import at.ac.tuwien.kr.alpha.grounder.Unification;
import org.apache.poi.util.Internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simplifies an internal input program by simplifying and deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<InternalProgram, InternalProgram>{

    @Override
    public InternalProgram apply(InternalProgram inputProgram) {
        List<InternalRule> srcRules = new ArrayList<>(inputProgram.getRules());
        List<InternalRule> transformedRules = new ArrayList<>();

        for (InternalRule rule : srcRules) {
            boolean redundantRule = false;

            Atom headAtom = rule.getHead().getAtom();
            Set<Literal> body = rule.getBody();
            Set<Literal> positiveBody = rule.getPositiveBody();
            Set<Literal> negativeBody = rule.getNegativeBody();
            InternalRule simplifiedRule = null;

            //implements s0: delete duplicate rules
            if (transformedRules.contains(rule)) {  //TODO: Add InternalRule.equals()
                redundantRule = true;
            }
            //implemnts s2
            if (checkForConflictingBodyLiterals(positiveBody, negativeBody)) {
                redundantRule = true;
            }
            //implements s3
            if (checkForHeadInBody(body, headAtom)) {
                redundantRule = true;
            }
            //implements s9
            if(checkForUnreachableLiterals(srcRules, rule, inputProgram.getFacts())) {
                redundantRule = true;
            }
            //implements s10
            simplifiedRule = checkForSimplifiableRule(srcRules, rule, inputProgram.getFacts());
            if(simplifiedRule != null) {
                rule = simplifiedRule;
            }

            if(!redundantRule) {
                transformedRules.add(rule);
            }
        }

        return new InternalProgram(transformedRules, inputProgram.getFacts());
    }

    /**
     * This method checks if a rule contains a literal in both the positive and the negative body.
     * implements s2
     */
    private boolean checkForConflictingBodyLiterals(Set<Literal> positiveBody, Set<Literal> negativeBody) {
        for (Literal positiveLiteral : positiveBody) {
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
        for (Literal literal : body) {
            //TODO: implement Atom.equals()
            if (literal.getAtom().equals(headAtom)) {
                return true;
            }
        }
        return false;
    }


    /**
     * This method checks for rules with bodies containing not derivable literals or negated literals, that are facts.
     * implements s9
     */
    private boolean checkForUnreachableLiterals (List<InternalRule> rules, InternalRule rule, List<Atom> facts) {
        for (Literal literal : rule.getBody()) {
            if (literal.isNegated()) {
                if (!facts.contains(literal.getAtom())) {
                    return false;
                }
            } else {
                if (isDerivable(literal, rules)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method checks for literals from rule bodies, that are already facts (when positive)
     * or not derivable (when negated).
     * implements s10
     */
    private InternalRule checkForSimplifiableRule (List<InternalRule> rules, InternalRule rule, List<Atom> facts) {
        for (Literal literal : rule.getBody()) {
            if (literal.isNegated()) {
                if (facts.contains(literal.getAtom())) {
                    return null;
                } else return rule.removeLiteral(literal);
            } else {
                if (!isDerivable(literal, rules)) {
                    return rule.removeLiteral(literal);
                } else return null;
            }
        }
        return null;
    }




    /**
     * This method checks whether a literal is derivable, ie. it is unifiable with the head atom of a rule.
     * implements s5 conditions
     */
    private boolean isDerivable(Literal literal, List<InternalRule> rules){
        for (InternalRule rule : rules) {
            if (Unification.unifyAtoms(literal.getAtom(), rule.getHeadAtom()) != null) {
                return true;
            }
        }
        return false;
    }
}
