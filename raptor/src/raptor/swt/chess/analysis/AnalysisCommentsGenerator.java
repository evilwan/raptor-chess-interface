/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chess.analysis;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.engine.uci.info.BestLineFoundInfo;

public class AnalysisCommentsGenerator {
	boolean doubleBishopFired = false;
	int lastMaterial = 0;

	public String getComment(double previous, double current, double scoreDiff,
			boolean isWhite, BestLineFoundInfo thisPosBestLine, Game game) {
		if ((isWhite && previous < -2.0 && scoreDiff >= 2.0)
				|| (!isWhite && previous > 2.0 && scoreDiff >= 2.0))
			return " Loses the initiative.";
		else if (!doubleBishopFired) {
			if (game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP) == 2
					&& game.getPieceCount(GameConstants.BLACK,
							GameConstants.BISHOP) == 0 && (previous > -1 || previous < 1)) {
				doubleBishopFired = true;
				return " White has the advantage of double bishops.";
			}
			else if (game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP) == 2
					&& game.getPieceCount(GameConstants.WHITE,
							GameConstants.BISHOP) == 0 && (previous > -1 || previous < 1)) {
				doubleBishopFired = true;
				return " Black has the advantage of double bishops.";
			}			
			/*else if (((isWhite && current > 2) || (!isWhite && current < -2)) ) {				
				Game gameCopy = game.deepCopy(true);
				gameCopy.rollback();
				int materialPrevious = GameUtils.getMaterialScore(gameCopy);
				gameCopy.move(game.getLastMove());
				int captureSquare = game.getLastMove().getTo();
				for (UCIMove move: thisPosBestLine.getMoves()) {
					if (move.isPromotion()) {
						gameCopy.makeMove(
								move.getStartSquare(),
								move.getEndSquare(),
								move.getPromotedPiece());
					} else {
						gameCopy.makeMove(
								move.getStartSquare(),
								move.getEndSquare());
					}
					
					if (!(move.getEndSquare() == captureSquare 
							|| gameCopy.isInCheck()))
						break;
				}
				int materialScore = GameUtils.getMaterialScore(gameCopy);
				
				if (isWhite && (materialPrevious-materialScore) <= -2 
						|| !isWhite && (materialPrevious-materialScore) >= 2)
					return " What a sacrifice!";
			}*/
		}

		return "";
	}
}
