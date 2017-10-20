package at.ac.tuwien.kr.alpha.grounder.atoms;

/**
 * Represents ground-instance enumeration atom of form: enum(aggId, term, sequenceNo).
 * The semantics of this is: if enum(A,T1, N1) and enum(A,T2,N2) are both true and T1 != T2, then N1 != N2.
 * Furthermore, If enum(A,T1,N1) is true with N>0 then enum(A,T2,N1-1) is true for some T1 != T2
 * and both, T1 and T2, are ground instances the grounder encountered during the search so far.
 *
 * Copyright (c) 2017, the Alpha Team.
 */
public class EnumerationAtom {
}
