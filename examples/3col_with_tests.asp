%%% Basic Encoding of the Graph 3-Coloring Problem %%%
% Graphs are interpreted as having undirected edges.

% Edges are undirected
edge(Y, X) :- edge(X, Y).

% Guess color for each vertex
red(V) :- vertex(V), not green(V), not blue(V).
green(V) :- vertex(V), not red(V), not blue(V).
blue(V) :- vertex(V), not red(V), not green(V).

% Check for invalid colorings
:- vertex(V1), vertex(V2), edge(V1, V2), red(V1), red(V2).
:- vertex(V1), vertex(V2), edge(V1, V2), green(V1), green(V2).
:- vertex(V1), vertex(V2), edge(V1, V2), blue(V1), blue(V2).

%% Verify that directed edges are converted to undirected ones.
#test no_asymmetric_edge(expect: >0) {
	given {
		vertex(a).
		vertex(b).
		vertex(c).
		edge(a, b).
		edge(b, c).
		edge(c, a).
	}
	assertForAll {
		:- edge(A, B), not edge(B, A).
	}	
}

#test triangle_colorings(expect: 6) {
	given {
		vertex(a).
		vertex(b).
		vertex(c).
		edge(a, b).
		edge(b, c).
		edge(c, a).
	}
	% Make sure all vertices are colored in all answer sets
	assertForAll {
		colored(V) :- vertex(V), red(V).
		colored(V) :- vertex(V), green(V).
		colored(V) :- vertex(V), blue(V).
		:- vertex(V), not colored(V).
	}
	% Make sure we do not have neighboring vertices of same color in any answer set
	assertForAll {
		:- edge(V1, V2), red(V1), red(V2).
		:- edge(V1, V2), green(V1), green(V2).
		:- edge(V1, V2), blue(V1), blue(V2).
	}
	% In at least one answer set, vertex a should be red
	assertForSome {
		:- not red(a).
	}
	% In at least one answer set, vertex a should be green
	assertForSome {
		:- not green(a).
	}
	% In at least one answer set, vertex a should be blue
	assertForSome {
		:- not blue(a).
	}
}
