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
package raptor.game;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SuicideGame extends Game {
	private static final Log LOG = LogFactory.getLog(SuicideGame.class);

	public SuicideGame() {
		setType(Type.SUICIDE);
	}

	@Override
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = getPseudoLegalMoves();

		LOG.debug("Possible Moves = "
				+ java.util.Arrays.toString(result.asArray()));

		boolean containsCaptures = false;
		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			Move move = result.getHighPriority(i);
			forceMove(move);

			if (!isLegalPosition()) {
				result.removeHighPriority(i);
				i--;
			} else {
				if (move.isCapture()) {
					containsCaptures = true;
				}
			}

			rollback();
		}

		LOG.debug("containsCapture = " + containsCaptures);

		for (int i = 0; i < result.getLowPrioritySize(); i++) {
			if (containsCaptures) {
				result.removeLowPriority(i);
				i--;
				continue;
			} // remove all, since there are no non-capture legal moves if can
			// capture

			Move move = result.getLowPriority(i);
			forceMove(move);

			if (!isLegalPosition() || move.isCastleKSide()
					|| move.isCastleQSide()) { // castling
				// not
				// allowed in
				// suicide
				result.removeLowPriority(i);
				i--;
			}

			rollback();
		}

		LOG.debug("Possible Moves = "
				+ java.util.Arrays.toString(result.asArray()));

		return result;
	}

	@Override
	public boolean isLegalPosition() {
		return true;
	}
}
