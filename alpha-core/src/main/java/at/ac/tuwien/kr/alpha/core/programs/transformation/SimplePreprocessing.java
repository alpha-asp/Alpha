package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.rule.NormalRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Simplifies a normal input program by deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<NormalProgram, NormalProgram>{

    @Override
    public NormalProgram apply(NormalProgram inputProgram) {

        List<NormalRule> srcRules = new ArrayList<>(inputProgram.getRules());
        List<NormalRule> transformedRules = new ArrayList<>();

        Iterator<NormalRule> ruleIterator = srcRules.iterator();

        while(ruleIterator.hasNext()) {
            NormalRule rule = ruleIterator.next();
            boolean redundantRule = false;

            Atom headAtom = rule.getHead().getAtom();
            Set<Literal> body = rule.getBody();
            Set<Literal> positiveBody = rule.getPositiveBody();
            Set<Literal> negativeBody = rule.getNegativeBody();

            //implements s3: deletes rule if body contains head atom
            Iterator<Literal> literalIterator = body.iterator();
            while(literalIterator.hasNext()) {
                Literal literal = literalIterator.next();
                if (literal.getAtom().equals(headAtom)) {
                    redundantRule = true;
                    break;
                }
            }

            //implements s2: deletes rule if body contains both positive and negative literal of an atom
            Iterator<Literal> positiveLiteralIterator = positiveBody.iterator();
            while(positiveLiteralIterator.hasNext()) {
                Literal positiveLiteral = positiveLiteralIterator.next();
                if (negativeBody.contains(positiveLiteral.negate())) {
                    redundantRule = true;
                    break;
                }
            }

            //implements s0: delete duplicate rules
            if (!redundantRule && !transformedRules.contains(rule)) {
                transformedRules.add(rule);
            }
        }
        return new NormalProgram(transformedRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
    }
}
