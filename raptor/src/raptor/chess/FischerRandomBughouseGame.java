/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getFile;
import raptor.chess.pgn.PgnHeader;

/**
 * Fischer Random Bughouse Game.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program. Also this wont work for bgpn without some
 * changes.
 */
public class FischerRandomBughouseGame extends BughouseGame {
	protected int initialLongRookFile;
	protected int initialShortRookFile;
	protected int initialKingFile;

	public FischerRandomBughouseGame() {
		setHeader(PgnHeader.Variant, Variant.fischerRandomBughouse.name());
		addState(Game.DROPPABLE_STATE);
		addState(Game.FISCHER_RANDOM_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FischerRandomBughouseGame deepCopy(boolean ignoreHashes) {
		FischerRandomBughouseGame result = new FischerRandomBughouseGame();
		result.initialLongRookFile = initialLongRookFile;
		result.initialShortRookFile = initialShortRookFile;
		result.initialKingFile = initialKingFile;
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * This method should be invoked after the initial position is setup. It
	 * handles setting castling information used later on during the game.
	 */
	public void initialPositionIsSet() {
		initialKingFile = getFile(bitscanForward(getPieceBB(WHITE, KING)));
		long rookBB = getPieceBB(WHITE, ROOK);
		int firstRook = getFile(bitscanForward(rookBB));
		rookBB = bitscanClear(rookBB);
		int secondRook = getFile(bitscanForward(rookBB));
		if (firstRook < initialKingFile) {
			initialLongRookFile = firstRook;
			initialShortRookFile = secondRook;
		} else {
			initialLongRookFile = secondRook;
			initialShortRookFile = firstRook;
		}
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		FischerRandomUtils.generatePseudoKingCastlingMoves(this, fromBB, moves,
				initialKingFile, initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void makeCastlingMove(Move move) {
		FischerRandomUtils.makeCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void rollbackCastlingMove(Move move) {
		FischerRandomUtils.rollbackCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		FischerRandomUtils.updateCastlingRightsForNonEpNonCastlingMove(this,
				move, initialShortRookFile, initialLongRookFile);
	}
}
