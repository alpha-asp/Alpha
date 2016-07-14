package at.ac.tuwien.kr.alpha;

public class NoGood {

	public int[] noGoodLiterals;
	public int posHeadLiteral;

	public NoGood(int size) {
		noGoodLiterals = new int[size];
		posHeadLiteral = -1;	// by default, NoGood has no head literal set
	}
}
