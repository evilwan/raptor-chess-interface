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
package raptor.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.chess.pgn.PgnHeader;

/**
 * Follows FICS suicide chess rules.
 */
public class LosersGame extends ClassicGame {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(LosersGame.class);

	public LosersGame() {
		setHeader(PgnHeader.Variant, Variant.losers.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LosersGame deepCopy(boolean ignoreHashes) {
		LosersGame result = new LosersGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * In losers you must make a capture if its possible. This method narrows
	 * down the list to only captures if there is one possible.
	 * 
	 * 
	 * {@inheritDoc}
	 * 
	 */
	@Override
	public PriorityMoveList getLegalMoves() {
		PriorityMoveList result = super.getLegalMoves();
		boolean hasCapture = false;
		for (int i = 0; i < result.getHighPrioritySize(); i++) {
			setSan(result.getHighPriority(i));
			if (result.getHighPriority(i).isCapture()) {
				hasCapture = true;
				break;
			}
		}
		if (!hasCapture) {
			for (int i = 0; i < result.getLowPrioritySize(); i++) {
				setSan(result.getLowPriority(i));
				if (result.getLowPriority(i).isCapture()) {
					hasCapture = true;
					break;
				}
			}
		}

		if (hasCapture) {
			PriorityMoveList onlyCaptures = new PriorityMoveList();
			for (int i = 0; i < result.getHighPrioritySize(); i++) {
				if (result.getHighPriority(i).isCapture()) {
					onlyCaptures.appendHighPriority(result.getHighPriority(i));
				}
			}
			for (int i = 0; i < result.getLowPrioritySize(); i++) {
				if (result.getLowPriority(i).isCapture()) {
					onlyCaptures.appendHighPriority(result.getHighPriority(i));
				}
			}
			return onlyCaptures;
		} else {
			return result;
		}
	}
}
