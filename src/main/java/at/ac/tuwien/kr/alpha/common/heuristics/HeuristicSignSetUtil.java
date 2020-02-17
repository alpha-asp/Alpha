/*
 *  Copyright (c) 2020 Siemens AG
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *  2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package at.ac.tuwien.kr.alpha.common.heuristics;

import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.asSet;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.FALSE;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.MBT;
import static at.ac.tuwien.kr.alpha.solver.ThriceTruth.TRUE;

public class HeuristicSignSetUtil {

	private static final String NAME_SIGN_SET_T = "t";
	private static final String NAME_SIGN_SET_TM = "tm";
	private static final String NAME_SIGN_SET_M = "m";
	private static final String NAME_SIGN_SET_F = "f";
	private static final Map<Set<ThriceTruth>,String> SIGN_SET_TO_NAME = new HashMap<>();
	private static final Map<String, Set<ThriceTruth>> NAME_TO_SIGN_SET = new HashMap<>();

	static {
		SIGN_SET_TO_NAME.put(asSet(TRUE), NAME_SIGN_SET_T);
		SIGN_SET_TO_NAME.put(asSet(TRUE, MBT), NAME_SIGN_SET_TM);
		SIGN_SET_TO_NAME.put(asSet(MBT), NAME_SIGN_SET_M);
		SIGN_SET_TO_NAME.put(asSet(FALSE), NAME_SIGN_SET_F);
		NAME_TO_SIGN_SET.put(NAME_SIGN_SET_T, asSet(TRUE));
		NAME_TO_SIGN_SET.put(NAME_SIGN_SET_TM, asSet(TRUE, MBT));
		NAME_TO_SIGN_SET.put(NAME_SIGN_SET_M, asSet(MBT));
		NAME_TO_SIGN_SET.put(NAME_SIGN_SET_F, asSet(FALSE));
	}

	public static String toName(Set<ThriceTruth> signSet) {
		return SIGN_SET_TO_NAME.get(signSet);
	}

	public static Set<ThriceTruth> toSignSet(String name) {
		return NAME_TO_SIGN_SET.get(name.toLowerCase());
	}

	/**
	 * Only the following sign sets are allowed and processable: T, TM, M, F.
	 * @param signSet the sign set to check
	 * @return {@code true} iff the given sign set is allowed and processable
	 */
	public static boolean isProcessable(Set<ThriceTruth> signSet) {
		return SIGN_SET_TO_NAME.containsKey(signSet);
	}

	public static boolean isF(Set<ThriceTruth> signSet) {
		return signSet.size() == 1 && signSet.iterator().next().equals(ThriceTruth.FALSE);
	}
}
