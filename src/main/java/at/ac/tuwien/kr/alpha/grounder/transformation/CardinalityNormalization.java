package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BodyElement;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class CardinalityNormalization implements ProgramTransformation {
	@Override
	public void transform(Program inputProgram) {
		// Transforms all cardinality-aggregates into normal logic rules employing a lazy-grounded sorting circuit.
		String cardinalitySortingCircuit =
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

		// Connect/Rewrite every aggregate in each rule.
		for (Rule rule : inputProgram.getRules()) {
			rewriteAggregates(rule, inputProgram);
		}


		Program cardinalityEncoding = new ProgramParser().parse(cardinalitySortingCircuit);
		// Add cardinality encoding to program.
		inputProgram.getRules().addAll(cardinalityEncoding.getRules());

	}

	private void rewriteAggregates(Rule rule, Program inputProgram) {
		for (BodyElement literal : rule.getBody()) {
			// TODO: if literal is an aggregate, rewrite it and add rules connecting it to the sorting circuit.
			int i  = 4 + 3; // TODO: remove.

			// Example rewriting/connection:
			// num(K) :-  K <= #count {X,Y,Z : p(X,Y,Z) }, dom(K).
			// is rewritten into:
			// num(K) :- sorting_network_output(aggregate_arguments(-731776545), K), dom(K).
			// sorting_network_input(aggregate_arguments(-731776545), element_tuple(X, Y, Z)) :- p(X, Y, Z).
			// sorting_network_bound(aggregate_arguments(-731776545), K) :- dom(K).
		}
	}
}
