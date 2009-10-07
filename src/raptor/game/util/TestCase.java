/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.game.util;

import java.util.List;

import raptor.game.Game;
import raptor.game.Move;

public class TestCase {
	public void asserts(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	public void asserts(List<Move> listA, List<Move> listB, Game game) {
		for (Move moveA : listA) {
			int foundIndex = -1;
			for (int i = 0; i < listB.size(); i++) {
				if (listB.get(i).getLan().equals(moveA.getLan())) {
					foundIndex = i;
					break;
				}
			}
			if (foundIndex == -1) {
				throw new AssertionError("Could not find move "
						+ moveA.getLan() + " in " + listB + "\n" + game);
			} else {
				listB.remove(foundIndex);
			}
		}

		if (listB.size() != 0) {
			throw new AssertionError(
					"The following moves were not found in listA " + listB
							+ "\n" + game);
		}
	}

	public void assertsContains(List<Move> moveList, String lan) {
		boolean found = false;

		for (Move candidate : moveList) {
			if (candidate.getLan().equals(lan)) {
				found = true;
				break;
			}
		}

		asserts(found == true, "Could not find move " + lan + " in " + moveList);
	}

	public void assertsDoesntContains(List<Move> moveList, String lan) {
		boolean found = false;

		for (Move candidate : moveList) {
			if (candidate.getLan().equals(lan)) {
				found = true;
				break;
			}
		}

		asserts(found == false, "Found move " + lan + " in " + moveList);
	}

	public boolean contains(String string, String[] array) {
		boolean result = false;
		for (int i = 0; !result && i < array.length; i++) {
			result = array[i].equals(string);
		}
		return result;
	}
}
