package at.ac.tuwien.kr.alpha;

//import at.ac.tuwien.kr.alpha.grounder.CompletionGenerator;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.config.SystemConfig;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.grounder.structure.AnalyzeUnjustified;
import at.ac.tuwien.kr.alpha.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.solver.DefaultSolver;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

import static at.ac.tuwien.kr.alpha.Main.main;
import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class MyTestPlayground {
	static final String LS = System.lineSeparator();

	@Test
	@Ignore
	public void backwardChaining() {
		// Idea: when :- q(X), p(X) collect all X in a query(X) atom.
		// Use a proved() predicate, one for to-prove (demo/query?) and then halt if no to-prove is not already proved.
		// if we have a rule q(X) :- r(X)
		String facts = "rule(p(a), true). rule(p(b), true). rule(p(d), true).";
		String rules = "rule( q(a), conj(p(a), p(b)) ).";
		String query = "query(q(a)).";
		String backwardMetaInterpreter = "evaluated(A) :- query(A), rule(A, true)." +
			"query(Body) :- query(A), rule(A,Body)." +
			"evaluated(A) :- query(A), rule(A,Body), evaluated(Body)." +
			"query(A) :- query(conj(A,Rest))." +
			"query(Rest) :- query(conj(A,Rest)), evaluated(A)." +
			"evaluated(conj(A,Rest)) :- query(conj(A,Rest)), evaluated(A), evaluated(Rest).";
		// Replace each variable X by: var(x).
		// So: rule( q(X), conj(p(X) ) becomes rule( q(var(x), conj(p(var(x)))).
		String varMI = "checkUnification(A,H,B) :- query(A), rule(H,B)." +
			"unifies(A,A,B) :- checkUnification(A,A,B)." +
			"unifies(p(X),p(X),B) :- checkUnification(p(X),p(X),B)."; // need a way to translate p(X) into T(...). Either need new builtins to do that, or represent everything as lists/functionterms.
		String unifies = "query(Bs) :- query(A), rule(H,B), unifies(H,A,U), substitute(B,U,Bs).";

		String reasoner = "demo(X, X) :- query(X)." +
			"demo(B,D) :- rule( X, B ), demo(X,D)." +
			"demo(B1,D) :- demo(f(B1,B2),D)." +
			"demo(B2,D) :- demo(f(B1,B2),D)." +
			"demo(F,D) :- fact(F), query(D).";
		String program = facts + rules + query + backwardMetaInterpreter;
		main(new String[]{"-d", "-n", "10", "-str", program});
		/*
		{
		evaluated(p(a)), evaluated(p(b)),
		fact(p(a), true), fact(p(b), true), fact(p(d), true),
		query(true), query(p(a)), query(p(b)), query(q(a)), query(conj(p(a), conj(p(b), true))), query(conj(p(b), true)),

		rule(q(a), conj(p(a), conj(p(b), true))) }

		 */
	}

	@Test
	@Ignore
	public void testJustificationAnalysisUNSATIssue() {
		String program = "% cycle\n" +
			"cycle_max(1).\n" +
			"% declare hardware existance\n" +
			"operation(sum).\n" +
			"% configure hardware operation delays\n" +
			"hw_op_cycles(sum,1).\n" +
			"% configure minimum and maximum hardware instances\n" +
			"hw_op_max(sum,1).\n" +
			"% ================ PREPERATION HELPER ================\n" +
			"value(Z,0) :- input_value(Z).\n" +
			"hw_op_inst(OP,1) :- operation(OP).\n" +
			"{ hw_op_inst(OP,N1) }:- operation(OP), hw_op_inst(OP,N), hw_op_max(OP,K), N1 = N+1, N<K.\n" +
			"cycle(0).\n" +
			"{ cycle(N1) } :- N1 = N+1, cycle(N), cycle_max(K), N<K. \n" +
			"% ================ HARDWARE OPERATIONS ================\n" +
			"% use a hardware for it\n" +
			"% hw_op(operation OP, argument X, argument Y, result Z, instance I, timestep N)\n" +
			"{ hw_op(OP,X,Y,Z,I,N) } :- operation(OP), value(X,N), value(Y,N), calculate(OP,X,Y,Z), hw_op_inst(OP,I), cycle(N), hw_op_cycles(OP, M), NM = N+M, cycle(NM).\n" +
			"% same hardware can be used only once per cycle\n" +
			":- hw_op(OP,X1,_,_,I,N), hw_op(OP,X2,_,_,I,N), X1!=X2.\n" +
			":- hw_op(OP,_,Y1,_,I,N), hw_op(OP,_,Y2,_,I,N), Y1!=Y2.\n" +
			"% hardware implementation\n" +
			"value(Z,NM) :- NM = N+M, operation(OP), hw_op(OP,_,_,Z,_,N), hw_op_cycles(OP, M).\n" +
			"% ================ TARGET  ================\n" +
			":- not value(X,N), cycle_max(N), output_value(X).\n" +
			"input_value(a).\n" +
			"calculate(sum,a,a,x).\n" +
			"output_value(x).\n";

		main(new String[]{"--stats", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testParserHaltedOnRecoverableError() {
		String program = "%file: parser_error.lp\n" +
			"input_value(a).\n" +
			"output_value(a).\n" +
			"\n" +
			"cycle_max_max(3).\n" +
			"cycle_max(0).\n" +
			"{ cycle_max(N+1) } :- cycle_max(N), cycle_max_max(M), N<M.\n" +
			"cycle_last(N) :- cycle(N), ~cycle(N+1).\n" +
			"\n" +
			"value(Z,0) :- input_value(Z).\n" +
			"\n" +
			"cycle(0).\n" +
			"{ cycle(N+1) } :- cycle(N), cycle_max(K), N<K.\n" +
			"\n" +
			":- not value(X,N), cycle_max(N), output_value(X).";

		main(new String[]{"--stats", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionTreeEvaluation() {
		//Logger tracer = (Logger) LoggerFactory.getLogger(CompletionGenerator.class);
		//tracer.setLevel(Level.TRACE);
		String program = "dom(1..3000)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- r(X)." +
			"r(X) :- q(X)." +
			"p(X) :- q(X)." +
			":- pdom(S), not p(S).\n" +
			"pdom(700..1400).";
		/*program = "n(100). m(10)." +
			"dom(1..N) :- n(N)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- q(X).\n" +
			"p(X) :- p(Y), X=Y+10, Y < 100.\n" +
			":- not p(Sp), Sp = 10+1.\n" +
			":- p(Sp), Sp=10+S, S = 0..9." +
		"";*/
		main(new String[]{"--stats", "-b", "naive",
			"-cjs",
			"OnlyCompletion",
//			"OnlyJustification",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionRecursive() {
	//	Logger tracer = (Logger) LoggerFactory.getLogger(CompletionGenerator.class);
	//	tracer.setLevel(Level.TRACE);
		int vN = 1000;
		int vM = 1000;
		int vL = 20000;
		String program = "n(1000).\n" +
//			"m(1000).\n" +
//			"l(20000)." +
			"ndom(X) :- X = 1.." + vN + ".\n" +
			"{q(X)} :- ndom(X).\n" +
			"p(X) :- q(X).\n" +
			"p(X) :- p( Y ), X = Y + " + vM + ", X < " + (vM + vL + 1) + ".\n" +
			":- not p(" + (vL + vM) + ").\n";
		for (int i = 1; i <= vM - 1; i++) {
			program += ":- p(" + (vL + i) + ").\n";
		}
			//":- p(LIM), m(M), l(L), LIM = L+I, I=1..Mm1, Mm1 = M-1.";
		System.out.println(program);
		main(new String[]{"--stats", //"-b", "naive",
			"-cjs",
//			"OnlyCompletion",
			"OnlyJustification",
			"-n", "10", "-str", program, "-f", "q", "-f", "p"});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}


	@Test
	@Ignore
	public void testMBTIssueExponential() {
		String program = "dom(1..36)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- r(X)." +
			"r(X) :- q(X)." +
			"p(X) :- q(X)." +
			":- not p(5)." +
			":- not p(7).\n";
		main(new String[]{"--stats", "-b", "naive",
			"-cjs", "OnlyCompletion",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testRunawayGrounding() {
		String program = "" +
			"\n";
		System.out.println(program);
		// Legacy config: onlyJustification, disableCompletionForMultipleRules (disableCompletionBackwards)
		main(new String[]{"--stats", //"-b", "naive",
			"-cjs", "None",
			//	"--disableCompletionForMultipleRules",
			//	"--disableCompletionBackwards",
			//	"--disableCompletionJustificationAfterClosing",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionVSJustification() throws InterruptedException {
		Thread.sleep(10000);
		String program = "time(X) :- X=0.\n" +
			"time(T1) :- time(T), T1 = T+1, max(M), not max(T1).\n" + // %not max(T1).
	//	"time(0..Tm1) :- max(T), Tm1 = T-1 ." +
			"val(X,Y) :- X=0, Y=0. \n" +
			"val(T1,V) :- time(T1), val(T,V), T1 = T+1, not inc(T). \n" +
			"val(T1,V1) :- time(T1), val(T,V), T1 = T+1, V1 = V+1, inc(T). \n" +
			"{inc(T)} :- time(T).\n" +
	// Runaway:		":- inc(T), not time(T)." +
			/*"inc(T) :- not ninc(T), time(T).\n" +
			"ninc(T) :- not inc(T), time(T).\n" +*/
			//"zero(0).\n" +
			":- goal(T,V), not val(T,V).\n" +
			":- val(T,V), not goal(T,V), inc(T), not inc(T1), T1=T+1, time(T1).\n" +
			"%instance\n" +
			"max(100). \n" +
			"goal(50,37).\n" +
			"goal(99,80).\n" +
			"\n";
		System.out.println(program);
		// Legacy config: onlyJustification, disableCompletionForMultipleRules (disableCompletionBackwards)
		main(new String[]{"--stats", //"-b", "naive",
			"-cjs", "Both",
		//	"--disableCompletionForMultipleRules",
		//	"--disableCompletionBackwards",
		//	"--disableCompletionJustificationAfterClosing",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionFDUnsatBugFullyGround() {
		main(new String[]{"--stats", //"-b", "naive",
			"-cjs", "Both",
			"-n", "10", "-i", "./benchmarks/testing/completion_clingo_ground.txt"});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionFD() {
		String program = "dom(1..36). s(5,6)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- r(X), s(X,Y), Y = X+1." +
			"r(X) :- q(X)." +
			"p(X) :- q(X)." +
			":- not p(5)." +
			":- not p(7).\n";
		main(new String[]{"--stats", "-b", "naive",
			"-cjs", "OnlyCompletion",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testCompletionFDEquationRewriting() {
		String program = "dom(1..36). s(4)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- r(Xm1), s(Xm1), X = Xm1+1." +
			"r(X) :- q(X)." +
			"p(X) :- q(X)." +
			":- not p(5)." +
			":- not p(7).\n";
		main(new String[]{"--stats", "-b", "naive",
			"-cjs", "OnlyCompletion",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testNegatedNonEquality() {
		String program = "dom(1..4)." +
			"p(X,Y) :- dom(X), dom(Y), not Y <= X.\n" +
			"\n";
		main(new String[]{"--stats", "-b", "naive",
			"-cjs", "OnlyCompletion",
			"-n", "10", "-str", program});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testMBTIssueExponentialDisabledJustification() {
		String program = "dom(1..22)." +
			"{q(X)} :- dom(X).\n" +
			"p(X) :- q(X)." +
			"r(X) :- q(X)." +
			"p(X) :- r(X)." +
			":- not p(5)." +
			":- not p(7).\n";
		main(new String[]{"-disableJustifications", "-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testMBTIssueProjection() {
		String program = "dom(1..200)." +
			"{q(X,Y)} :- dom(X), dom(Y), X < Y.\n" +
			"p(X) :- q(X,Y)." +
			":- not p(5)." +
			":- not p(7).\n";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testMBTLogisticsPlanning() {
		String program = "time(0..12)." +
			"loco(0, id(1), a)." +
			"loco(0, id(2), c)." +
			"connection(a, b, 3)." +
			"connection(b, d, 1)." +
			"connection(d, e, 5)." +
			"connection(a, c, 4)." +
			"connection(a, f, 2)." +

			"{ locomove(T, Id, S1, S2, D) } :- loco(T,Id,S1), S1 != transit, connection(S1,S2,D), time(T), TE=T+D+1, time(TE)." +
			"transit(T1,Id) :- locomove(T, Id, _, _, D), time(T1), T < T1, T1 <= TD, TD=T+D." +
			"loco(T,Id,transit) :- transit(T,Id)." +
			":- locomove(T, Id, S1, S2, _), locomove(T, Id, S1, S3, _), S2 != S3." +
			"loco(T1,Id,S) :- loco(T,Id,S), S != transit, T1 = T+1, time(T1), not transit(T1,Id)." +
			"loco(TE,Id,S2) :- locomove(T, Id, _, S2, D), TE = T+D+1, time(TE)." +
			"connection(V1,V2,D) :- connection(V2,V1,D)." +
			":- not loco(11, id(2), f)." +
			//":- not loco(5, id(1), b).\n" +
			"";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "20000", "-f", "locomove", "-f", "loco", "-f", "transit", "-str", program});
	}

	@Test
	@Ignore
	public void testMBTLogisticsPlanningDebug() {
		String program = "time(0..5)." +
			"loco(0, id(2), c)." +
			"connection(a, c, 2)." +
			"connection(a, f, 1)." +

			"{ locomove(T, Id, S1, S2, D) } :- loco(T,Id,S1), S1 != transit, connection(S1,S2,D), time(T), TE=T+D+1, time(TE)." +
			"transit(T1,Id) :- locomove(T, Id, _, _, D), time(T1), T < T1, T1 <= TD, TD=T+D." +
			"loco(T,Id,transit) :- transit(T,Id)." +
			":- locomove(T, Id, S1, S2, _), locomove(T, Id, S1, S3, _), S2 != S3." +
			"loco(T1,Id,S) :- loco(T,Id,S), S != transit, T1 = T+1, time(T1), not transit(T1,Id)." +
			"loco(TE,Id,S2) :- locomove(T, Id, _, S2, D), TE = T+D+1, time(TE)." +
			"connection(V1,V2,D) :- connection(V2,V1,D)." +
			":- not loco(5, id(2), f)." +
			//":- not loco(5, id(1), b).\n" +
			"";
		System.out.println(program);
		main(new String[]{//"-disableJustifications",
			"-d", "-g", "naive", "-s", "default", "-n", "20", "-f", "locomove", "-f", "loco", "-f", "transit", "-str", program});
	}

	@Test
	@Ignore
	public void testMBTLogisticsPlanningDebugShrink() {
		String program = "time(0..4)." +
			"loco(0, c)." +
			"connection(a, c, 1)." +
			"connection(a, f, 1)." +

			"{ locomove(T, S1, S2, D) } :- loco(T,S1), S1 != transit, connection(S1,S2,D), time(T), TE=T+D+1, time(TE)." +
			"transit(T1) :- locomove(T, _, _, D), time(T1), T < T1, T1 <= TD, TD=T+D." +
			"loco(T,transit) :- transit(T)." +
			":- locomove(T, S1, S2, _), locomove(T, S1, S3, _), S2 != S3." +
			"loco(T1,S) :- loco(T,S), S != transit, T1 = T+1, time(T1), not transit(T1)." +
			"loco(TE,S2) :- locomove(T, _, S2, D), TE = T+D+1, time(TE)." +
			"connection(V1,V2,D) :- connection(V2,V1,D)." +
			":- not loco(4, f)." +
			//":- not loco(5, id(1), b).\n" +
			"";
		System.out.println(program);
		main(new String[]{//"-disableJustifications",
			"-d", "-g", "naive", "-s", "default", "-n", "20", "-f", "locomove", "-f", "loco", "-f", "transit", "-str", program});
	}

	@Test
	@Ignore
	public void mbtBug() {
		String program = "{p(0)}.\n" +
			"{p(X)} :- p(Y), s(Y,X).\n" +
			//"r(A) :- p(A).\n" +
			//"r(B) :- p(B), dom(0).\n" +
			":- not p(7).\n" +
			"s(Y,X) :- X = Y+1, dom(Y), dom(X).\n" +
			"dom(0..20).";
		System.out.println(program);
		main(new String[]{//"-disableJustifications",
				//"-d",
				"-g", "naive", "-s", "default", "-n", "20", "-str", program});
	}

	@Test
	@Ignore
	public void testRoboMove() {
		String program = "time(0..12)." +
			"robo(0, id1, a)." +
			"robo(0, id2, c)." +
			"connection(a, b)." +
			"connection(b, d)." +
			"connection(d, e)." +
			"connection(a, c)." +
			"connection(a, f)." +

			"{ robomove(T, Id, S1, S2) } :- robo(T,Id,S1), connection(S1,S2), time(T)." +
			":- robomove(T, Id, S1, S2), robomove(T,Id,S1,S3), S2 != S3." +
			"moving(T,Id) :- robomove(T,Id,_,_)." +
			"robo(T1,Id,S) :- robo(T,Id,S), T1 = T+1, time(T1), not moving(T,Id)." +
			"robo(T1,Id,S2) :- robomove(T, Id, _, S2), T1 = T+1, time(T1)." +
			"connection(V1,V2) :- connection(V2,V1)." +
			":- not robo(11, id2, f)." +
			":- not robo(5, id1, b).\n" +
			"";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-f", "robomove", "-f", "robo", "-str", program});
	}


	// Other benchmark instances: Planning problem with big max time.


	@Test
	@Ignore
	public void testMBTIssuePlusGroundingExplosion() {
		String program = "{p}.\n" +
			":- p.\n" +
			"node(1..100)." +
			"{ q(N1,N2,N3,N4, N5) } :- node(N1), node(N2), node(N3), node(N4), node(N5), p.";
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testAggregatesBug() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		String program = "n(1..3)." + LS
			+ "{x(N)} :- n(N)." + LS
			+ "min(2)." + LS
			+ "max(2)." + LS
			+ "ok :- min(M), M <= #count { N : n(N), x(N) }, not exceedsMax." + LS
			+ "exceedsMax :- max(M), M1 = M + 1, M1 <= #count { N : n(N), x(N) }." + LS;
		System.out.println(program);
		main(new String[]{//"-disableJustifications",
				"-DebugEnableInternalChecks", "-n", "10", "-str", program});
	}

	/*
	@Test
	@Ignore
	public void testScoringIndifference() {
		int s = 1;
		double scoref, scoreplus;
		double oldNormalizedScore = 0.0f, currentNormalizedScore;
		while (true) {
			scoref = s;
			scoreplus = scoref + 1.0f;
			if (scoref == scoreplus) {
				System.out.println("Can no longer increase score by 1.0f.");
				break;
			}
			if ( s <= 94911150 ) {
				currentNormalizedScore = scoref / scoreplus; // scores become indifferent for s=94911151, requires about 0.1xx sec
			} else {
			/*if ( s == 94911151) {
				System.out.println("Breaking because old count hit.");
				break;
			}* /
				scoref += 1.01f;
				currentNormalizedScore = (1.0d - (1.0d / Math.log(scoref))); // requires 1.7xx sec
			}
			//System.out.println("CurrentNormalizedScore: " + currentNormalizedScore);
			if (currentNormalizedScore == oldNormalizedScore) {
				System.out.println("Scores are indifferent now.");
				break;
			}
			oldNormalizedScore = currentNormalizedScore;
			s++;
			if (s == Integer.MAX_VALUE) {
				System.out.println("Breaking because int/long overflows.");
				break;
			}
			if ( s == 94919990) {
				System.out.println("Breaking because old count hit.");
				break;
			}
		}
		System.out.println("s=" + s);
	}*/


	@Test
	@Ignore
	public void testArithmeticRewriting() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		String program = "p(3+2). p(4)." + LS +
			"r(X-1) :- p(X)." + LS +
			"s(a, f(6))." + LS +
			"q(X) :- p(X), s(a, f(X+2)).";
		main(new String[]{//"-disableJustifications",
			"-DebugEnableInternalChecks", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void testBug() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{//"-disableJustifications",
			"-i", "./benchmarks/issues/domind_house_bug.txt",
			//"-DebugEnableInternalChecks",
			"-n", "10"});
	}

	/*String program = "\n" +
		"{p}\n" +
		"\n" +
		"\n" +
		"q(X,Y,Z,...} :- p\n" +
		"\n" +
		":- p\n" +
		"\n" +
		"q(N1...N100) :- node(N1), node(N100), p";*/

	@Test
	@Ignore
	public void testProgramFullyGrounded() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			"-e", "424804216927132",
		//	"-b", "BERKMIN",
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/grounded_instance_0001.asp",
			//"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			//"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0002-graph_colouring-125-0.asp"
			//"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp"
		});
	}

	@Test
	@Ignore
	public void testJustificationIssue() {
		//Logger root = (Logger) LoggerFactory.getLogger(AnalyzeUnjustified.class);
		//root.setLevel(Level.TRACE);

		String program = "" +
			"colours(C1,C2) :- colour(C1), colour(C2), C1 < C2.\n" +
			"notnext(C1,C3) :- colours(C1,C2), colours(C2,C3).\n" +
			"next(C1,C2)    :- colours(C1,C2), not notnext(C1,C2).\n" +
			"later(C2)      :- next(C1,C2).\n" +
			"index(C1,1)    :- colour(C1), not later(C1).\n" +
			"index(C2,I1)  :- next(C1,C2), index(C1,I), I1=I+1.\n" +
			"\n" +
			"chosenColour(N,C)    :- node(N), index(C,I), I <= N, not notChosenColour(N,C).\n" +
			"notChosenColour(N,C) :- chosenColour(N,CC), index(C,I), I <= N, C != CC.\n" +
			"notChosenColour(N,C) :- chosenColour(NN,C), link(NN,N), NN < N.\n" +
			"\n" +
			"colored(N) :- chosenColour(N,C).\n" +
			":- node(N), not colored(N).\n";

		main(new String[]{"-n", "1", //"-DebugEnableInternalChecks",
			//"-e", "424804216927132",
			"-e", "0",
			"-b", "VSIDS_PHASE_SAVING",
			"-rs",
			"-str", program,
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
			"-i", "./benchmarks/competition2015/GraphColouring/0001-graph_colouring-125-0.asp",
			//"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			//"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0002-graph_colouring-125-0.asp"
			//"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp"
		});
	}

	@Test
	@Ignore
	public void testDomainSpecificHeuristics() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			//"-e", "424804216927132",
			"-b", "BERKMIN",
			//"-disableJustifications",
			"-i", "./benchmarks/testing/domainspecific/pup_encoding.asp",
			"-i", "./benchmarks/testing/domainspecific/pup_instance70.asp",
		});
	}

	@Test
	@Ignore
	public void testProgram() throws InterruptedException {
		Thread.sleep(10000);
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			//"-e", "0", //"424804216927132",
	//		"-rs",
	//		"-ph", "alltrue",
			//"-dnd",
	//		"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/grounded_instance_0001.asp",
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/encoding_choice.asp",
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp",
	//		"-b", "VSIDS_PHASE_SAVING"
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
	//		"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/encoding_altered.asp",
	//		"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			//"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0002-graph_colouring-125-0.asp"
			//"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp"
		});
	}

	@Test
	@Ignore
	public void testVsidsPhaseSavingBug() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		Logger tracer = (Logger) LoggerFactory.getLogger(AnalyzeUnjustified.class);
		//tracer.setLevel(Level.TRACE);
		tracer = (Logger) LoggerFactory.getLogger(DefaultSolver.class);
		tracer.setLevel(Level.DEBUG);
		String dir = "/home/as/projects/dynacon/alpha-benchmarks/vsids+phasesaving/problems/GraphColouring/";
		main(new String[]{"-n", "1", //"-DebugEnableInternalChecks",
			//"-e", "0", //"424804216927132",
			"-rs",
			"-b", "VSIDS_PHASE_SAVING",
			"-ph", "allfalse",
			//"-dnd",
			"-i", dir + "encoding-new-arith.asp",
			"-i", dir + "instances/0001-graph_colouring-125-0.asp"
			//"-disableJustifications",
		});
	}

	@Test
	@Ignore
	public void testFourFours() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "1", //"-DebugEnableInternalChecks",
			//"-e", "424804216927132",
			//"-dnd",
		//	"-rs",
			"-i", "./benchmarks/testing/binfours_martin_alpha.lp",
			"-b", "VSIDS_PHASE_SAVING",
			"-ph", "alltrue",
			//"-b", "GDD_VSIDS",
			//"-disableJustifications",
			"-str", "n(232).",
			"-f", "op", "-f", "eval"
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
		});
	}

	@Test
	@Ignore
	public void testFourFoursOwn() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		String program = "fours(1..4)." +
			"dom(1,0..7). dom(2,1..8). dom(3,2..9). dom(4,3..10).\n" +
			"pos(1,0).\n" +
			"{ pos(2,X) : dom(2,X) }." +
			"{ pos(3,X) : dom(3,X) }." +
			"{ pos(4,X) : dom(4,X) }." +
			":- pos(F,X), pos(F,Y), X != Y." +
			":- pos(F,X), pos(Fp,Y), F<Fp, X >= Y." +
			"hasPos(F) :- pos(F,X)." +
			":- not hasPos(F), fours(F)." +
			"op(P,4) :- pos(_,P). \n" +
			"opb(add). opb(mul). opu(neg). opu(bnot)." +
			"dom(opu,1..11)." +
			"dom(opb,3..11)." +
			"{ op(P,O) : opu(O) } :- dom(opu,P)." +
			"{ op(P,O) : opb(O) } :- dom(opb,P)." +
			":- op(P,O), op(P,Op), O != Op. % no two ops at same place\n" +
			":- op(P,add), P1 = P-1, P2=P-2, not op(P1,4), not op(P2,bnot).\n" +
			":- op(P,add), P1 = P-1, P2=P-2, P3=P-3, not op(P1,neg), not op(P2,4), not op(P3,bnot).\n" +
			":- op(P,add), P1 = P-1, P2=P-2, P3=P-3, not op(P1,bnot), not op(P2,4), not op(P3,bnot).\n" +
			":- op(P,mul), P1 = P-1, P2=P-2, not op(P1,4), not op(P2,bnot).\n" +
			":- op(P,mul), P1 = P-1, P2=P-2, P3=P-3, not op(P1,neg), not op(P2,4), not op(P3,bnot).\n" +
			":- op(P,mul), P1 = P-1, P2=P-2, P3=P-3, not op(P1,bnot), not op(P2,4), not op(P3,bnot).\n" +
			"occupied(P) :- op(P,_)." +
			"freepos(P) :- P=0..11, not occupied(P)." +
			":- freepos(P), op(Pp,_), P<Pp.\n" +
			"muladd(P) :- op(P,mul).\n" +
			"muladd(P) :- op(P,add).\n" +
			"three :- muladd(P1), muladd(P2), muladd(P3), P1 != P2, P2 != P3, P1 != P3.\n" +
			":- not three.\n" +
			"stack(0,l(4,nil)) :- op(0,4).\n" +
			"stack(T1,l(4,S)) :- T1 = T+1, op(T1,4), stack(T,S).\n" +
			"stack(T1,l(Vneg,S)) :- T1 = T+1, op(T1,neg), stack(T,l(V,S)), Vneg = (256-V)\\256. \n" +
			"stack(T1,l(Vbnot,S)) :- T1 = T+1, op(T1,bnot), stack(T,l(V,S)), Vbnot = (255-V)\\256. \n" +
			"stack(T1,l(V,S)) :- T1 = T+1, op(T1,add), stack(T,l(V1,l(V2,S))), V = (V1 + V2)\\256. \n" +
			"stack(T1,l(V,S)) :- T1 = T+1, op(T1,mul), stack(T,l(V1,l(V2,S))), V = (V1 * V2)\\256. \n" +
			"maxop(P) :- occupied(P), P1=P+1, freepos(P1).\n" +
			"eval(N) :- maxop(P), stack(P,l(N,nil)).\n" +
		//	"evlts :- eval(N). :- not evlts.\n" +
			":- n(N), not eval(N)." +

	//		"evaluates :- eval(N)." +
	//		":- not evaluates." +
	//		":- n(N), not eval(N)." +
	//		"stacked(T) :- stack(T,_).\n" +
	//		":- op(T,_), not stacked(T)." +
		//	"final(T) :- stacked(T), T1 = T+1, not stacked(T1).\n" +
		//	":- final(T), T1=T+1, T1 < 11, not freepos(T1)." +
		//	":- op(T,_), not stacked(T).\n" +
			"";
		System.out.println(program);
		main(new String[]{"-n", "3",
			//"-e", "424804216927132",
			//"-dnd",
			//"-dj",
			"-rs",
			"-str", program,
			"-b", "VSIDS_PHASE_SAVING",
			"-ph", "alltrue",
			//"-disableJustifications",
		//	"-cjs", "OnlyJustification",
			"-str", "n(0).",
			"-f", "pos", "-f", "op", "-f", "stack", "-f", "eval"
		});
	}

	@Test
	@Ignore
	public void testFib() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		String program = "fib(0,0). fib(1,1)." +
			"upto(22)." +
			"fib(N,F) :- fib(N1,F1), fib(N2,F2), N1=N-1, N2=N-2, F = F1+F2, N=0..U, upto(U).";
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			"-str", program
		});
	}

	@Test
	@Ignore
	public void testAggregateRewriting() {
		String program = "a :- 2 <= #count { X : p(X,Y) }, q(Y). q(5). p(1,5). p(2,5). b :- 5 <= #sum { X : p(X,Y) }, q(Y).";
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}


	@Test
	@Ignore
	public void testAggregateColoring() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			//"-e", "424804216927132",
			//"-b", "Berkminliteral",
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
			"-f", "pos",
			"-f", "depleted",
			"-f", "goto",
			"-f", "visited",
		//	"-b", "NAIVE",
			"-gtc", "permissive",
//			"-str", "visited_enough :- visited(X), visited(Y), visited(Z), X != Y, Y != Z, X != Z.",
//			"-str", ":- not visited_enough.",
		//	"-str", ":- not goto(361,195). :- not goto(263, 196). :- not goto(132, 197). :- not goto(194,198). :- not goto(120,199). :- not goto(98,200).",
//			"-rc", "22334",
			// goto(286, 199), goto(361, 198), goto(361, 200)
			"-i", "./benchmarks/aaai19/longrunplanning/instance400_2.lp"
		});
	}

	// sorting_network_span(R, 1..I) :- sorting_network_input_number(R,I).
	// 			"sorting_network_span(R,I) :- sorting_network_input_number(R,I).\n" +
	//			"sorting_network_span(R,Im1) :- sorting_network_span(R,I), I<1, Im1=I-1.\n" +
	@Test
	@Ignore
	public void anotherTempTest() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10",
			"-str", "foo(R,1..I) :- input(R,I)." +
			"bar(R,I) :- input(R,I)." +
			"bar(R,Im1) :- bar(R,I), I<"
//			"-str", "visited_enough :- visited(X), visited(Y), visited(Z), X != Y, Y != Z, X != Z.",
//			"-str", ":- not visited_enough.",
			//	"-str", ":- not goto(361,195). :- not goto(263, 196). :- not goto(132, 197). :- not goto(194,198). :- not goto(120,199). :- not goto(98,200).",
		});
	}

	@Test
	public void printRule() {
		StringBuilder sb = new StringBuilder();
		StringBuilder combi = new StringBuilder("go :- ");
		for (int j = 1; j <= 10; j++) {
			combi.append("go(").append(j).append("), ");
			sb.append("go(").append(j).append(") :- ");
			for (int i = 1; i <= 100; i++) {
				sb.append("colored(").append(i * j).append("), ");
			}
			sb.append(".\n");
		}
		combi.append(".");
		sb.append(".");
		System.out.println(sb.toString());
		System.out.println(combi.toString());
		for (int i = 0; i < 1000; i++) {
			System.out.println("enum(aggregate_arguments(1),element_tuple(" + i + "," + i + ")," + i + ").");
		//	System.out.println("foo(" + i + "," + i + ").");
		}
	}

	@Test
	@Ignore
	public void testMemoryOverheadExceeded() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10", //"-DebugEnableInternalChecks",
			//"-e", "424804216927132",
			//"-b", "BERKMINLITERAL",
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
			"-i", "./benchmarks/testing/domainspecific/encoding2.encodingAllCorners.asp",
			"-i", "./benchmarks/testing/domainspecific/11-packing-0-0.asp"
		});
	}

	@Test
	@Ignore
	public void failsAtNoGoodLearningSize36() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
		main(new String[]{"-n", "10",
			//"-disableJustifications",
			//"-i", "./benchmarks/testing/nonpartition/further/instance1000_1_3.lp", //"-f", "delete"
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/encoding_altered.asp",
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			//"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp"
		});
	}

	@Test
	@Ignore
	public void testJustification() {
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-i", "benchmarks/inap/graph5col/instances/instance500_2000_5.lp"});
	}

	@Test
	@Ignore
	public void testMalformedInputIgnored() {
		String program = "foo(a) :- p(b).\n" +
			"// rule :- q.\n" +
			"r(1).\n" +
			"r(2).\n";
		System.out.println("Executing main.");
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
		System.out.println("Done.");

	}

	@Test
	@Ignore
	public void actualAggregate() {
		String program = "dom(1..3)." +
			"bound(1..4)." +
			"{ value(X) : dom(X) }." +
			"num(K) :-  K <= #count {X : value(X) }, bound(K). %K=1..4. % fails for now";
		main(new String[]{"-d", "-normalizationCountingGrid", "-g", "naive", "-s", "default", "-n", "0", "-str", program});

	}

	@Test
	@Ignore
	public void doubleAggregate() {
		String program = "dom(1..4)." +
			"bound(1..5)." +
			"{ value(X) : dom(X) }." +
			"{ value2(X) : dom(X) }." +
			"numa(K) :-  K <= #count {X : value(X) }, bound(K)." +
			"numb(K) :-  K <= #count {X : value2(X) }, bound(K).";
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-f", "value", "-f", "value2", "-f", "numa", "-f", "numb", "-n", "0", "-str", program});

	}

	@Test
	@Ignore
	public void summationAggregateGuess() {
		// Summation aggregate over guessed numbers, gets interesting over 200
		String program = "dom(1..250)." +
			"bound(1)." +
			"bound(X1) :- sum_larger_equal_than(X), X1 = X +1." +
			"{ in(X) } :- dom(X)." +
			//":- in(X), in(Y), in(Z), X !=Y, Y != Z, X != Z." +
			"sum_larger_equal_than(K) :- K <= #sum { X : in(X)}, bound(K).";

		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-f", "in", "-f", "sum_larger_equal_than", "-n", "100", "-str", program});
	}

	@Test
	@Ignore
	public void exponentialAggregate() {
		int size = 1000;
		StringBuilder expVars = new StringBuilder();
		for (int i = 1; i <= size; i++) {
			if (i == 1) {
				expVars.append("X" + i);
			} else {
				expVars.append(", X" + i);
			}
		}
		StringBuilder expSel = new StringBuilder();
		for (int i = 1; i <= size; i++) {
			if (i == 1) {
				expSel.append("dely(X" + i + ")");
			} else {
				expSel.append(", dely(X" + i + ")");
			}
		}
		int sizeExp = (int) Math.pow(2, size);
		String program = "dom(0..1).\n" +
			"{ a;b;c }.\n" +
			":- a, b.\n" +
//			":- " + size/2 + " <= #count { X : sel(X)}.\n" +
//			"exp(" + expVars + ") :- " + expSel + ".\n" +
			"dely(X) :- a,b, dom(X).\n" +
			"holds :- 2 <= #count { " + expVars + " : " + expSel + " }.\n";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void aggregateGrowth() {
		// TODO: use the version where the product X * Y * Z occurs inside the aggregate, then mention the time for running it without the aggregate (should be almost 0 for clingo.
		String program = "dom(1..1000)." +
			"sdom(1..30)." +
			"sel(X) :- not nsel(X), sdom(X)." +
			"nsel(X) :- not sel(X), sdom(X)." +
			":- sel(X), sel(Y), X != Y." +
			"p(X, Y, Z) :- sel(X), sel(Y), sel(Z)." +
			"num(K) :- K <= #count { X,Y,Z : p(X,Y,Z)}, dom(K).";
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program});
	}

	@Test
	@Ignore
	public void aggregateGrowthBetter() {
		// Increase dom for blow-up, gets interesting in the 30s.
		String program = "dom(1..35)." +
			"{ sel(X) } :- dom(X)." +
			":- sel(X), sel(Y), X != Y." +
			"num(K) :- K <= #count { X,Y,Z : sel(X), sel(Y), sel(Z)}, dom(K).";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void aggregateLazyNormalization() {
		// Increase sdom for blow-up, gets interesting in the 200s.
		String program = "dom(1)." +
			"sdom(1..160)." +
			"sel(X) :- not nsel(X), sdom(X)." +
			"nsel(X) :- not sel(X), sdom(X)." +
			":- sel(X), sel(Y), sel(Z), X != Y, X != Z, Y != Z." +
			"{ a;b;c}." +
			":- a,b,c." +
			":- b,c." +
			":- a,c." +
			"p(X, Y, Z) :- sel(X), sel(Y), sel(Z)." +
			"num(K) :- K <= #count { X,Y,Z : p(X,Y,Z),a,b,c}, dom(K)." +
			"";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void aggregateLazyNormalizationBetter() {
		// Increase sdom for blow-up in steps of 10. Gets interesting after 50.
		// Also: compare runtime of program without aggregate and with aggregate.
		String program = "dom(1..1000)." +
			"sdom(1..160)." +
			"{ sel(X,Y) } :- sdom(X), sdom(Y)." +
			"{ a;b }." +
			":- a,b." +
			"num(K) :- 1 <= #count { X,Y : sel(X,Y)}, a,b,dom(K)." +
			"";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-str", program});
	}

	@Test
	@Ignore
	public void aggregateGlobalVariableTest() {
		// TODO: seems to work now?
		String program = "dom(1..5)." +
			"p(2,1)." +
			"p(3,1)." +
			"p(4,2)." +
			"p(5,2)." +
			"p(6,3)." +
			"out(V) :- 2 <= #count { X : p(X,V) }, dom(V).";
		System.out.println(program);
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program});
	}

	@Test
	@Ignore
	public void indegCountAggregate() {
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/ijcai18/testing/indegcount.lp"});
	}

	@Test
	@Ignore
	public void summationAggregateTest() {
		String program = "x :- K <= #sum {3,a:a; 4,b:b; 5,c:c}, K = 6. {a; b; c}.";

		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program});
	}

	@Test
	@Ignore
	public void summationAggregateOutput() {
		String program = "dom(1..26). x(K) :- K <= #sum {3,a:a; 4,b:b; 5,c:c}, dom(K). {a; b; c}.";

		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-f", "x", "-f", "a", "-f", "b", "-f", "c", "-str", program});
	}

	@Test
	@Ignore
	public void summationAggregateVariable() {
		String program = "dom(1..2600). bound(1..10000). x(K) :- K <= #sum {X : p(X)}, bound(K). {p(X)} :- dom(X), X2 = X * 2, dom(X2).";

		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "10", "-f", "x", "-f", "p", "-str", program});
	}

	@Test
	@Ignore
	public void testArithmetics() {
	String program = "numops(1..24).\n" +
		"foo(X) :- X = 1+3." +
		"op(plus).\n" +
		"op(times).\n" +
		"\n" +
		"op(T,O) :- numops(T), not nop(T,O), op(O).\n" +
		"nop(T,O) :- numops(T), not op(T,O), op(O).\n" +
		"\n" +
		":- op(T,O), op(T,Oo), O != Oo.\n" +
		"%nop(T,O) :- numops(T), op(O), op(T,Oo), Oo != O.\n" +
		"\n" +
		"eval(1,1).\n" +
		"eval(T1, Res) :- eval(T, In), T1 = T+1, op(T1,plus), Res = In + T.\n" +
		"eval(T1, Res) :- eval(T, In), T1 = T+1, op(T1,times), Res = In * T.\n" +
		"\n" +
		"\n" +
		":- op(T1,times), op(T2,times), op(T3,times), T1 != T2, T2 != T3, T3 != T1.";
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "100", "-str", program});
	}

	@Test
	@Ignore
	public void testGrowthAggregate() {
		// num(K) :-  K <= #count {X,Y,Z : p(X,Y,Z) }, dom(K).
		String program = "dom(1..1000)." +
			"sdom(1..30)." +
			"sel(X) :- not nsel(X), sdom(X)." +
			"nsel(X) :- not sel(X), sdom(X)." +
			":- sel(X), sel(Y), X != Y." +
			"p(X, Y, Z) :- sel(X), sel(Y), sel(Z)." +
			"num(K) :- sorting_network_output(aggregate_arguments(-731776545), K), dom(K).\n" +
			"sorting_network_input(aggregate_arguments(-731776545), element_tuple(X, Y, Z)) :- p(X, Y, Z).\n" +
			"sorting_network_bound(aggregate_arguments(-731776545), K) :- dom(K).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_input_number(R, I), D = 0.\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_comparator(I, _, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, J, D1), D1 = D - 1, sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, J, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_wire_value(R, J, D1), sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_passthrough(I, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_input_range(R, 1..I) :- sorting_network_input_number(R, I).\n" +
			"sorting_network_relevant_depth(R, D) :- sorting_network_odd_even_level(R, _, _, D).\n" +
			"sorting_network_part(R, G) :- sorting_network_input_range(R, I), I1 = I - 1, G = G1 + 1, sorting_network_log2(I1, G1).\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), sorting_network_wire_value(R, K, D), sorting_network_sorted_count(N, D), K <= N.\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), K <= 0.\n" +
			"sorting_network_odd_even_level(R, 1, 1, 1) :- sorting_network_part(R, 1).\n" +
			"sorting_network_odd_even_level(R, L, P1, DL) :- P1 = P + 1, L = 1..P1, DL = D + L, sorting_network_odd_even_level(R, P, P, D), sorting_network_part(R, P1).\n" +
			"sorting_network_odd_even_comparator(1, P, I, J) :- sorting_network_odd_even_level(_, 1, P, _), sorting_network_input_range(_, I), I < J, J = ((I - 1) ^ 2 ** (P - 1)) + 1.\n" +
			"sorting_network_odd_even_comparator(L, P, I, J) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), J = I + S, 1 < L, N != 0, N != B - 1, N \\ 2 = 1, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_odd_even_passthrough(L, P, I) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), 1 < L, N = 0, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_odd_even_passthrough(L, P, I) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), 1 < L, N = B - 1, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_comparator(I, J, D) :- sorting_network_odd_even_comparator(L, P, I, J), sorting_network_odd_even_level(_, L, P, D).\n" +
			"sorting_network_passthrough(I, D) :- sorting_network_odd_even_passthrough(L, P, I), sorting_network_odd_even_level(_, L, P, D).\n" +
			"sorting_network_sorted_count(1, 0).\n" +
			"sorting_network_sorted_count(N, D) :- sorting_network_log2(N, P), sorting_network_odd_even_level(_, P, P, D).\n" +
			"sorting_network_log2(Ip2, I) :- Ip2 = 2 ** I, I = 0..30.\n" +
			"#enumeration_predicate_is sorting_network_index.\n" +
			"sorting_network_input_number(A, I) :- sorting_network_input(A, X), sorting_network_index(A, X, I).";

		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program, "-f", "sel", "-f", "num", "-f", "p"});
	}

	@Test
	@Ignore
	public void testAggregateNormalizationNoNegation() {
		String program = "head(K) :- sorting_network_output(aggregate_arguments(-878723272), K), bound(K).\n" +
			"sorting_network_input(aggregate_arguments(-878723272), element_tuple(X)) :- value(X).\n" +
			"sorting_network_bound(aggregate_arguments(-878723272), K) :- bound(K).\n" +
			"domain(1..3).\n" +
			"bound(1..4).\n" +
			"{ value(X) : domain(X) }.\n" +
			//"#show head / 1.\n" +
			//"#show value / 1.\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_input_number(R, I), D = 0.\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_comparator(I, _, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, J, D1), D1 = D - 1, sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, J, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_wire_value(R, J, D1), sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_passthrough(I, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_input_range(R, 1..I) :- sorting_network_input_number(R, I).\n" +
			"sorting_network_relevant_depth(R, D) :- sorting_network_odd_even_level(R, _, _, D).\n" +
			"sorting_network_part(R, G) :- sorting_network_input_range(R, I), I1 = I - 1, G = G1 + 1, sorting_network_log2(I1, G1).\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), sorting_network_wire_value(R, K, D), sorting_network_sorted_count(N, D), K <= N.\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), K <= 0.\n" +
			"sorting_network_odd_even_level(R, 1, 1, 1) :- sorting_network_part(R, 1).\n" +
			"sorting_network_odd_even_level(R, L, P1, DL) :- P1 = P + 1, L = 1..P1, DL = D + L, sorting_network_odd_even_level(R, P, P, D), sorting_network_part(R, P1).\n" +
			"sorting_network_odd_even_comparator(1, P, I, J) :- sorting_network_odd_even_level(_, 1, P, _), sorting_network_input_range(_, I), I < J, J = ((I - 1) ^ 2 ** (P - 1)) + 1.\n" +
			"sorting_network_odd_even_comparator(L, P, I, J) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), J = I + S, 1 < L, N != 0, N != B - 1, N \\ 2 = 1, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_odd_even_passthrough(L, P, I) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), 1 < L, N = 0, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_odd_even_passthrough(L, P, I) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), 1 < L, N = B - 1, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_comparator(I, J, D) :- sorting_network_odd_even_comparator(L, P, I, J), sorting_network_odd_even_level(_, L, P, D).\n" +
			"sorting_network_passthrough(I, D) :- sorting_network_odd_even_passthrough(L, P, I), sorting_network_odd_even_level(_, L, P, D).\n" +
			"sorting_network_sorted_count(1, 0).\n" +
			"sorting_network_sorted_count(N, D) :- sorting_network_log2(N, P), sorting_network_odd_even_level(_, P, P, D).\n" +
			"sorting_network_log2(Ip2, I) :- Ip2 = 2 ** I, I = 0..30.\n" +
			"#enumeration_predicate_is sorting_network_index.\n" +
			"sorting_network_input_number(A, I) :- sorting_network_input(A, X), sorting_network_index(A, X, I).";

		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program, "-f", "head", "-f", "value"});
		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program});
	}


	@Test
	@Ignore
	public void testAggregateNormalization() {
		String program = "% INPUT:\n" +
			"% head(K) :- K <= #count { X : value(X) }, bound(K).\n" +
			"% domain(1..10).\n" +
			"% bound(1..11).\n" +
			"% { value(X) : domain(X) }.\n" +

			"% OUTPUT:\n" +
			"head(K) :- sorting_network_output(aggregate_arguments(-878723272), K), bound(K).\n" +
			"sorting_network_input(aggregate_arguments(-878723272), element_tuple(X)) :- value(X).\n" +
			"sorting_network_bound(aggregate_arguments(-878723272), K) :- bound(K).\n" +
			"domain(1..10).\n" +
			"bound(1..13).\n" +
			"{ value(X) : domain(X) }.\n" +
			//"#show head / 1.\n" +
			//"#show value / 1.\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_input_number(R, I), D = 0.\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_comparator(I, _, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, J, D1), D1 = D - 1, sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, J, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, sorting_network_wire_value(R, J, D1), sorting_network_comparator(I, J, D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_wire_value(R, I, D) :- sorting_network_wire_value(R, I, D1), D1 = D - 1, not sorting_network_defined_wire(I, D), sorting_network_defined_wire_number(I), sorting_network_defined_depth(D), sorting_network_relevant_depth(R, D).\n" +
			"sorting_network_input_range(R, 1..I) :- sorting_network_input_number(R, I).\n" +
			"sorting_network_relevant_depth(R, D) :- sorting_network_odd_even_level(R, _, _, D).\n" +
			"sorting_network_part(R, G1) :- sorting_network_input_range(R, I), sorting_network_log2(I1, G), I1 = I - 1, G1 = G + 1.\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), sorting_network_wire_value(R, K, D), sorting_network_sorted_count(N, D), K <= N.\n" +
			"sorting_network_output(R, K) :- sorting_network_bound(R, K), K <= 0.\n" +
			"sorting_network_odd_even_level(R, 1, 1, 1) :- sorting_network_part(R, 1).\n" +
			"sorting_network_odd_even_level(R, L, P1, DpL) :- P1 = P + 1, DpL = D + L, L = 1..P1, sorting_network_odd_even_level(R, P, P, D), sorting_network_part(R, P1).\n" +
			"sorting_network_odd_even_comparator(1, P, I, J) :- sorting_network_odd_even_level(_, 1, P, _), sorting_network_input_range(_, I), sorting_network_input_range(_, J), I < J, J = ((I - 1) ^ 2 ** (P - 1)) + 1.\n" +
			"foo(1, P, I, J) :- sorting_network_odd_even_level(_, 1, P, _), sorting_network_input_range(_, I), sorting_network_input_range(_, J), I < J, J = ((I - 1) ^ 2 ** (P - 1)) + 1.\n" +
			"sorting_network_odd_even_comparator(L, P, I, J) :- sorting_network_odd_even_level(_, L, P, _), sorting_network_input_range(_, I), sorting_network_input_range(_, J), J = I + S, 1 < L, N != 0, N != B1, B1 = B - 1, N2 = N \\ 2, N2 = 1, N = (I - 1) / S - ((I - 1) / S / B) * B, S = 2 ** (P - L), B = 2 ** L.\n" +
			"sorting_network_comparator(I, J, D) :- sorting_network_odd_even_comparator(L, P, I, J), sorting_network_odd_even_level(_, L, P, D).\n" +
			"sorting_network_sorted_count(1, 0).\n" +
			"sorting_network_sorted_count(N, D) :- sorting_network_log2(N, P), sorting_network_odd_even_level(_, P, P, D).\n" +
			"sorting_network_defined_wire(I, D) :- sorting_network_comparator(I, _, D).\n" +
			"sorting_network_defined_wire(I, D) :- sorting_network_comparator(_, I, D).\n" +
			"sorting_network_defined_depth(D) :- sorting_network_comparator(_, _, D).\n" +
			"sorting_network_defined_wire_number(I) :- sorting_network_comparator(I, _, D).\n" +
			"sorting_network_defined_wire_number(I) :- sorting_network_comparator(_, I, D).\n" +
			"sorting_network_log2(P, I) :- P = 2 ** I, I = 0..30.\n" +
			"#enumeration_predicate_is sorting_network_index.\n" +
			"sorting_network_input_number(A, I) :- sorting_network_input(A, X), sorting_network_index(A, X, I).";

		main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "0", "-str", program, "-f", "head", "-f", "value"});
		//main(new String[]{"-d", "-g", "naive", "-s", "default", "-n", "6", "-str", program});
	}

	@Ignore
	@Test
	public void testAggregate() {
		main(new String[]{"-n", "10",
			//"-disableJustifications",
			"-i", "./benchmarks/ijcai18/kr18/instance1000_20000_2.lp"
		});
	}

	@Test
	@Ignore
	public void testBugProgram() {
		String program = "h_assign_1(U1,T,X,W) :- order(X,T,O), assign(U,T1,Y), order(Y,T1,O-1), maxOrder(M)," +
			"maxUnit(MU), comUnit(U1), U1<=U, maxElem(ME), W=10*MU*(M-O)+2*(ME-X)+U1." +
			"order(1,now1,3)." +
			"order(2,now2,2)." +
			"maxOrder(7)." +
			"maxUnit(11)." +
			"comUnit(5)." +
			"assign(6,now2,2)." +
			"maxElem(13).\n";
		System.out.println(program);
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.TRACE);
		main(new String[]{"-n", "10", "-str", program, "-DebugEnableInternalChecks"});
	}

	@Test
	@Ignore
	public void testFailingProgram() {
		String program = "x :- c1, c2, not x.\n" +
			"c1 :- not a1.\n" +
			"c1 :- not b1.\n" +
			"c2 :- not a2.\n" +
			"c2 :- not b2.\n" +
			"a1 :- not b1.\n" +
			"b1 :- not a1.\n" +
			"a2 :- not b2.\n" +
			"b2 :- not a2.";
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.TRACE);
		main(new String[]{"-g", "naive", "-s", "default", "-n", "1", "-str", program, "-DebugEnableInternalChecks", "-d", "-b", "naive"});
	}

	@Test
	@Ignore
	public void runWithInternalChecksOnFormerBugs() {
		String p1 = "x :- c1, c2, not x.\n" +
			"c1 :- not a1.\n" +
			"c1 :- not b1.\n" +
			"c2 :- not a2.\n" +
			"c2 :- not b2.\n" +
			"a1 :- not b1.\n" +
			"b1 :- not a1.\n" +
			"a2 :- not b2.\n" +
			"b2 :- not a2.\n";
		String p2 = "x :- c1, c2, c3, c4, not x.\n" +
			"c1 :- not a1.\n" +
			"c1 :- not b1.\n" +
			"c2 :- not a2.\n" +
			"c2 :- not b2.\n" +
			"c3 :- not a3.\n" +
			"c3 :- not b3.\n" +
			"c4 :- not a4.\n" +
			"c4 :- not b4.\n" +
			"a1 :- not b1.\n" +
			"b1 :- not a1.\n" +
			"a2 :- not b2.\n" +
			"b2 :- not a2.\n" +
			"a3 :- not b3.\n" +
			"b3 :- not a3.\n" +
			"a4 :- not b4.\n" +
			"b4 :- not a4.";
		String p3 = "x :- c1, c2, c3, c4, c5, c6, c7, c8, not x.\n" +
			"c1 :- not a1.\n" +
			"c1 :- not b1.\n" +
			"c2 :- not a2.\n" +
			"c2 :- not b2.\n" +
			"c3 :- not a3.\n" +
			"c3 :- not b3.\n" +
			"c4 :- not a4.\n" +
			"c4 :- not b4.\n" +
			"c5 :- not a5.\n" +
			"c5 :- not b5.\n" +
			"c6 :- not a6.\n" +
			"c6 :- not b6.\n" +
			"c7 :- not a7.\n" +
			"c7 :- not b7.\n" +
			"c8 :- not a8.\n" +
			"c8 :- not b8.\n" +
			"a1 :- not b1.\n" +
			"b1 :- not a1.\n" +
			"a2 :- not b2.\n" +
			"b2 :- not a2.\n" +
			"a3 :- not b3.\n" +
			"b3 :- not a3.\n" +
			"a4 :- not b4.\n" +
			"b4 :- not a4.\n" +
			"a5 :- not b5.\n" +
			"b5 :- not a5.\n" +
			"a6 :- not b6.\n" +
			"b6 :- not a6.\n" +
			"a7 :- not b7.\n" +
			"b7 :- not a7.\n" +
			"a8 :- not b8.\n" +
			"b8 :- not a8.\n";
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.TRACE);
		main(new String[]{"-DebugEnableInternalChecks", "-d", "-g", "naive", "-s", "default", "-n", "1", "-b", "naive", "-store", "alpharoaming", "-str", p1});
		main(new String[]{"-DebugEnableInternalChecks", "-d", "-g", "naive", "-s", "default", "-n", "1", "-b", "naive", "-store", "alpharoaming", "-str", p2});
		main(new String[]{"-DebugEnableInternalChecks", "-d", "-g", "naive", "-s", "default", "-n", "1", "-b", "naive", "-store", "alpharoaming", "-str", p3});
	}

	@Test
	@Ignore
	public void testLargeInputProgram() {
		// Other bug: 3col reports seemingly much too many answer sets: Alpha reports 120 while Clingo reports 48.

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		//root.setLevel(Level.TRACE);

		main(new String[]{"-e", "0", "-g", "naive", "-s", "default", "-n", "10",
			//"-branchingHeuristic", "naive",
			"-i", "./benchmarks/inap/cutedge/instances/instance300_10_1.lp", "-f", "delete"
			//"-i", //"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			//"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp"
			});


		// Good benchmark:
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "0", "-i", "./benchmarks/testing/explo_ground/explo_linear.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "0", "-i", "./benchmarks/testing/typo.lp"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/malformed.txt"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/instance1000_1_10.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/configuration2.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/issues/simple_issue.txt"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/nonpartition/instance1000_2_3.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/cutedge/instance500_30_1.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "0", "-i", "./benchmarks/testing/reachability/instance10000_2_1.lp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "0", "-i", "./benchmarks/testing/explo_ground/testparser.lp"});

		//main(new String[]{"-branchingHeuristic", "naive", "-g", "naive", "-s", "default", "-n", "19", "-i", "./benchmarks/issues/alpha-benchmarking-4/instances-wheel/wheel-07-shuffled03.asp"});


		/*for (int i = 1; i <= 24; i++) {
			//i = 3;
			main(new String[]{"-branchingHeuristic", "naive", "-g", "naive", "-s", "default", "-n", "6", "-i", "./benchmarks/issues/alpha-benchmarking-4/instances-wheel/wheel-07-shuffled" + String.format("%02d", i) + ".asp"});
			//i = 24;
		}//*/

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "1", "-i", "./benchmarks/testing/foundedness/assignment.asp"});


		/*
		main(new String[]{"-e", "3048951718888128", "-g", "naive", "-s", "default", "-n", "200",
			"-branchingHeuristic", "naive",
			"-i", "./benchmarks/competition2013/ASPCOMP2013-instances/b27/encoding_altered.asp",
			"-i", //"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0001-graph_colouring-125-0.asp"
			"./benchmarks/competition2013/ASPCOMP2013-instances/b27/instances/0004-graph_colouring-125-0.asp" }); //*/

		//main(new String[]{"-DebugEnableInternalChecks", "-g", "naive", "-s", "default", "-e", "97598271567626", "-n", "2", "-i", "./benchmarks/siemens/vehicle_normal_small.asp"});

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/alphahex/test1"}); // slow grounding, why?

		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/testing/factoryconfiguration/3col_test.asp"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "0", "-i", "./benchmarks/testing/explo_ground/explo3.lp"});


		// Following exceptions are all: Nothing to propagate after backtracking from conflict-causing guess. Should not happen.
		// Exception with branching heuristic disabled and running on fallback/naive heuristics only with seed: 299072405563477
		// 299289589515148 (after long time)
		//main(new String[]{"-g", "naive", "-s", "default", "-e", "299072405563477", "-n", "2", "-i", "./benchmarks/omiga/omiga-testcases/locstrat/locstrat-200.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "20", "-i", "./benchmarks/omiga/omiga-testcases/cutedge/cutedge-100-50.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "400", "-i", "./benchmarks/omiga/omiga-testcases/3col/3col-20-38.txt"});

		//main(new String[]{"-DebugEnableInternalChecks", "-sort", "-g", "naive", "-s", "default", "-n", "400", "-i", "./benchmarks/omiga/omiga-testcases/3col/3col-20-38.txt"});
		//main(new String[]{"-g", "naive", "-s", "naive", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-4.txt"});


		// Bug with 611959842434098
		//main(new String[]{"-DebugEnableInternalChecks", "-e", "611959842434098", "-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/siemens/vehicle_normal_small.asp"});
		//main(new String[]{"-branchingHeuristic", "gdd-pyro", "-g", "naive", "-s", "default", "-n", "40", "-i", "./benchmarks/siemens/vehicle_normal.asp"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/siemens/vehicle_normal_small.asp", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-1.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-4.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-4.txt"});
	}

	@Test
	@Ignore
	public void generateSuccessors() {
		int maxInt = 1000;
		String successors = "";
		for (int i = 0; i < maxInt; i++) {
			successors += "succ(" + i + "," + (i + 1) + ").\n";
		}
		System.out.println(successors);
	}

	@Test
	@Ignore
	public void generateTagselRules() {
		int maxValue = 8;
		for (int posCounter = 0; posCounter < maxValue; posCounter++) {
			for (int negCounter = 0; negCounter < maxValue; negCounter++) {
				// tags(T) :- tagreq(T,p,P1, P2, n, N1, N2), sel(P1), sel(P2), nsel(N1), nsel(N2)
				String posTerms = "";
				String posPreds = "";
				String negTerms = "";
				String negPreds = "";
				for (int i = 0; i < posCounter; i++) {
					posTerms += ", P" + (i + 1);
					posPreds += ", sel(P" + (i + 1) + ")";
				}
				for (int i = 0; i < negCounter; i++) {
					negTerms += ", N" + (i + 1);
					negPreds += ", nsel(N" + (i + 1) + ")";
				}
				String tagrule = "tags(T) :- tagreq(T, p" + posTerms + ", n" +  negTerms + ")" + posPreds + negPreds + ".";
				System.out.println(tagrule);
			}
		}
	}


	@Test
	@Ignore
	public void generateRandomTagreq33() {
		int maxDomainSize = 14;
		int maxTag = 3;
		HashSet<String> tagreqs = new HashSet<>();
		while (tagreqs.size() < 20000) {
			int tag = ThreadLocalRandom.current().nextInt(1, maxTag + 1);
			int p1 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			int p2 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			int p3 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			int n1 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			int n2 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			int n3 = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
			String tagreq = "tagreq(t" + tag + ", p, d" + p1 + ", d" + p2 + ", d" + p3 +  ", n, d" + n1 +  ", d" + n2 +  ", d" + n3 + ").";
			tagreqs.add(tagreq);
		}
		for (String tagreq : tagreqs) {
			System.out.println(tagreq);
		}
	}

	@Test
	@Ignore
	public void generateTagreqs33() {
		int maxDomainSize = 14;
		int maxTag = 3;
		//HashSet<String> tagreqs = new HashSet<>();
		ArrayList<ArrayList<Integer>> availableDomainElementsAtPosition = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			ArrayList<Integer> set = new ArrayList<>();
			availableDomainElementsAtPosition.add(set);
			for (int j = 0; j < maxDomainSize; j++) {
				set.add(j + 1);
			}
		}
		int emptyPositions = 0;
		while (emptyPositions < 6) {
			int tag = ThreadLocalRandom.current().nextInt(1, maxTag + 1);
			String tagreq = "tagreq(t" + tag + ", p";
			int[] domainElements = new int[6];
			for (int i = 0; i < 6; i++) {
				ArrayList<Integer> available = availableDomainElementsAtPosition.get(i);
				if (available.isEmpty()) {
					domainElements[i] = ThreadLocalRandom.current().nextInt(1, maxDomainSize + 1);
				} else {
					int pos = ThreadLocalRandom.current().nextInt(0, available.size());
					domainElements[i] = available.remove(pos);
					if (available.isEmpty()) {
						emptyPositions++;
					}
				}
			}
			tagreq += ", d" + domainElements[0] + ", d" + domainElements[1] + ", d" + domainElements[2]
					+ ", n, d" + domainElements[3] +  ", d" + domainElements[4] +  ", d" + domainElements[5] + ").";
			System.out.println(tagreq);
		}
		/*while(tagreqs.size() < 20) {

			int p1 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);
			int p2 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);
			int p3 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);
			int n1 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);
			int n2 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);
			int n3 = ThreadLocalRandom.current().nextInt(1,maxDomainSize + 1);

			tagreqs.add(tagreq);
		}
		for (String tagreq : tagreqs) {
			System.out.println(tagreq);
		}*/
	}

	@Ignore
	@Test
	public void testDouble() {
		double minD = Double.MIN_VALUE * 1e100;
		System.out.println(minD);
	}

	@Ignore
	@Test
	public void testBicycleColoring() {
		String input = "maxV(14).\n" +
			"v(1..N) :- maxV(N).\n" +
			"c(red). c(blue). c(green).\n" +
			"e(1,2..N) :- maxV(N).\n" +
			"e(N0,N1) :- maxV(N), N0 = 2..N, N1 = N0+1, N1 <= N.\n" +
			"e(N,2) :- maxV(N).\n" +
			"col(V,C) :- v(V), c(C), not ncol(V,C).\n" +
			"ncol(V,C) :- col(V,D), c(C), C!=D.\n" +
			":- e(V,U), col(V,C), col(U,C).\n";

		main(new String[]{"-n", "0",
			//"-disableJustifications",
			"-str", input
		});

	}

	@Ignore
	@Test
	public void testArithmeticFacts() {
		String input = "arith(14**2)." +
			"yes :- f(7) <= f(4+X), X = 5.\n";

		main(new String[]{"-n", "0", "-str", input});

	}


	@Test
	@Ignore
	public void testLSystems() {
		String input = "% L-System:\n" +
			"% A -> ABA\n" +
			"% B -> BBB\n" +
			"\n" +
			"w(0,0,a).\n" +
			"prod(a,aba).\n" +
			"prod(b,bbb).\n" +
			"% B -> C\n" +
			"% C -> C\n" +
			"prod(b,c).\n" +
			"prod(c,c).\n" +
			"str(aba,0,a).\n" +
			"str(aba,1,b).\n" +
			"str(aba,2,a).\n" +
			"strlen(aba,3).\n" +
			"str(bbb,0,b).\n" +
			"str(bbb,1,b).\n" +
			"str(bbb,2,b).\n" +
			"strlen(bbb,3).\n" +
			"str(ccc,0,c).\n" +
			"str(ccc,1,c).\n" +
			"str(ccc,2,c).\n" +
			"strlen(ccc,3).\n" +
			"str(c,0,c).\n" +
			"strlen(c,1).\n" +
			"\n" +
			"maxstep(11).\n" +
			"\n" +
			":- w(T,P,c), w(T,P3,c), P3 = P+3. % Exclude c--c patterns.\n" +
			"\n" +
			"% Encoding for non-deterministic L-Systems (Lindenmayer system) with constraints.\n" +
			"\n" +
			"steps(0..N) :- maxstep(N).\n" +
			"\n" +
			"% Select exactly one possible production rule.\n" +
			"nselectedProd(T,P,Prod) :- w(T,P,C), prod(C,Prod), not selectedProd(T,P,Prod).\n" +
			"selectedProd(T,P,Prod) :- w(T,P,C), prod(C,Prod), not nselectedProd(T,P,Prod).\n" +
			":- selectedProd(T,P,Prod1), selectedProd(T,P,Prod2), Prod1 != Prod2.\n" +
			"didSelect(T,P) :- selectedProd(T,P,_).\n" +
			":- not didSelect(T,P), w(T,P,_).\n" +
			"\n" +
			"% Compute position where right-hand side for next production starts.\n" +
			"cursor(T,0,0) :- steps(T).\n" +
			"cursor(T,P1,NewP) :- selectedProd(T,P,Prod), P1 = P+1, \n" +
			"\tcursor(T,P,CurP), strlen(Prod,L), NewP = CurP+L.\n" +
			"\n" +
			"% Derive the new characters from the production.\n" +
			"w(T1,NewP,C) :- T1 = T+1, steps(T1), selectedProd(T,P,Prod), I = 0..L, strlen(Prod,L),\n" +
			"str(Prod,I,C), cursor(T,P,CurP), NewP = I+CurP.\n";

		main(new String[]{"-n", "10", "-b", "VSIDS_PHASE_SAVING", "-rs",
			"-str", input
		});
		StringBuilder sb;
		StringJoiner sj;

	}


	@Ignore
	@Test
	public void testSchur() {
		String input = "number(1..13).\n" +
			"part(1). part(2). part(3).\n" +
			"inpart(X,1) :- not inpart(X,2), not inpart(X,3), number(X).\n" +
			"inpart(X,2) :- not inpart(X,1), not inpart(X,3), number(X).\n" +
			"inpart(X,3) :- not inpart(X,1), not inpart(X,2), number(X).\n" +
			":- number(X), number(Y), part(P), inpart(X,P), inpart(Y,P), inpart(Z,P), T=Y+ 1, X < T, Z=X+Y.\n";

		main(new String[]{"-n", "0", "-goa",
			//"-disableJustifications",
			"-str", input
		});

	}

	protected Solver getInstance(Program program) {
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		SystemConfig config = new SystemConfig();
		config.setSolverName("default");
		config.setNogoodStoreName("alpharoaming");
		config.setSeed(0);
		config.setBranchingHeuristic(BranchingHeuristicFactory.Heuristic.valueOf("VSIDS"));
		config.setDebugInternalChecks(true);
		config.setDisableJustificationSearch(false);
		config.setReplayChoices(Arrays.asList(21, 26, 36, 56, 91, 96, 285, 166, 101, 290, 106, 451, 445, 439, 448,
			433, 427, 442, 421, 415, 436, 409, 430, 397, 391, 424, 385, 379,
			418, 373, 412, 406, 394, 388, 382, 245, 232, 208 // include 208->fails with 38 decisions, not include->works
		));
		return SolverFactory.getInstance(config, atomStore, grounder);
	}

	@Ignore
	@Test
	public void testHanoiTower() throws IOException {
		String instance = "simple";
		final ProgramParser parser = new ProgramParser();
		Logger info = (Logger) LoggerFactory.getLogger(ChoiceManager.class);
		info.setLevel(Level.DEBUG);
		System.out.println("FOO");
		Program parsedProgram = parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_Alpha.asp")));
		parsedProgram.accumulate(parser.parse(CharStreams.fromPath(Paths.get("src", "test", "resources", "HanoiTower_instances", instance + ".asp"))));
		Solver solver = getInstance(parsedProgram);
		Optional<AnswerSet> answerSet = solver.stream().findFirst();
		assertTrue(answerSet.isPresent());
		System.out.println(answerSet.get());
		checkGoal(parsedProgram, answerSet.get());
	}

	/**
	 * Conducts a very simple, non-comprehensive goal check (i.e. it may classify answer sets as correct that are actually wrong) by checking if for every goal/3
	 * fact in the input there is a corresponding on/3 atom in the output.
	 */
	private void checkGoal(Program parsedProgram, AnswerSet answerSet) {
		Predicate ongoal = Predicate.getInstance("ongoal", 2);
		Predicate on = Predicate.getInstance("on", 3);
		int steps = getSteps(parsedProgram);
		SortedSet<Atom> onInstancesInAnswerSet = answerSet.getPredicateInstances(on);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(ongoal.getName()) && atom.getPredicate().getArity() == ongoal.getArity()) {
				Term expectedTop = atom.getTerms().get(0);
				Term expectedBottom = atom.getTerms().get(1);
				Term expectedSteps = ConstantTerm.getInstance(steps);
				Atom expectedAtom = new BasicAtom(on, expectedSteps, expectedBottom, expectedTop);
				assertTrue("Answer set does not contain " + expectedAtom, onInstancesInAnswerSet.contains(expectedAtom));
			}
		}
	}

	private int getSteps(Program parsedProgram) {
		Predicate steps = Predicate.getInstance("steps", 1);
		for (Atom atom : parsedProgram.getFacts()) {
			if (atom.getPredicate().getName().equals(steps.getName()) && atom.getPredicate().getArity() == steps.getArity()) {
				return Integer.parseInt(atom.getTerms().get(0).toString());
			}
		}
		throw new IllegalArgumentException("No steps atom found in input program.");
	}
}
