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

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.util.GameUtils;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.ScoreInfo;
import raptor.swt.chess.controller.AutomaticAnalysisController;

public class AnalysisCommentsGenerator {
	boolean doubleBishopFired = false;
	boolean singleBishopFired = false;
	
	boolean singleRookFired = false;
	boolean doubleRookFired =false;
	
	boolean doubleKnightFired = false;
	boolean singleKnightFired = false;
	
	boolean queenFired=false;
	
	boolean pawnFired=false;
	
	boolean blackCastleFired=false;
	boolean whiteCastleFired=false;
	
	boolean blackAlreadyCastled=false;
	boolean whiteAlreadyCastled=false;
	
	int lastMaterial = 0;
	
	static String[] advToStringTranslate = { "a queen", "double rooks", "a single rook", "double bishops", "a single bishop", "double knights", "a single knight", "over 2 pawns" };
	
	/**
	 * Apply to the given game first moves from thisPosBestLine as long as every applied move is
	 * a capture or a check.
	 * @return The copy of provided game with the above moves applied.
	 */
	private static Game extendMoves(BestLineFoundInfo thisPosBestLine, Game game) {
		Game newGame = game.deepCopy(true);
		UCIMove[] possibleMoves = thisPosBestLine.getMoves();
		for (UCIMove move: possibleMoves) {
			newGame.makeMove(move.getStartSquare(), move.getEndSquare());
			if (!newGame.getLastMove().isCapture() && !newGame.isInCheck()) {
				newGame.rollback();
				break;
			}
		}		
		return newGame;
	}
	
	/**
	 * Check if the provided game contains castling moves for a given color. 
	 * @return True if the side is already castled.
	 */
	private boolean notAlreadyCastled(Game game, int color) {
		if (color == GameConstants.WHITE && whiteAlreadyCastled ||
				color == GameConstants.BLACK && blackAlreadyCastled)
			return false;
		
		for (Move mv: game.getMoveList().asArray()) {
			if (mv.getColor() == color && (mv.isCastleLong() || mv.isCastleShort())) {
				if (color == GameConstants.WHITE)
					whiteAlreadyCastled = true;
				else
					blackAlreadyCastled = true;
					
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Is there a queen advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isQueenAdvantage(int color, Game game, Game extendedGame, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return (game.getPieceCount(color, GameConstants.QUEEN) == 1
				&& game.getPieceCount(oppositeColor, GameConstants.QUEEN) == 0)
				&& (extendedGame.getPieceCount(color, GameConstants.QUEEN) == 1
				&& extendedGame.getPieceCount(oppositeColor, GameConstants.QUEEN) == 0)
				&& (!considerScore || ((current > 4.25 && color == GameConstants.WHITE) || (current < -4.25 && color == GameConstants.BLACK)));
	}
	
	/**
	 * Is there a double bishop advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isDBishopAdvantage(int color, Game game, Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return game.getPieceCount(color, GameConstants.BISHOP) == 2
			&& game.getPieceCount(oppositeColor,
				GameConstants.BISHOP) == 0 && (previous > -1 || previous < 1) //On the previous move, score was roughly even..
				&& extendedGame.getPieceCount(color, GameConstants.BISHOP) == 2
				&& extendedGame.getPieceCount(oppositeColor,
						GameConstants.BISHOP) == 0
						&& (!considerScore || ((current > 2.6 && color == GameConstants.WHITE) || (current < -2.6 && color == GameConstants.BLACK)));
	}
	
	/**
	 * Is there a single bishop advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isSBishopAdvantage(int color, Game game, Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return ((game.getPieceCount(color, GameConstants.BISHOP)-game.getPieceCount(oppositeColor,
						GameConstants.BISHOP) == 1) 
						|| (game.getPieceCount(color, GameConstants.BISHOP) == 2 //Or if white has 2 bishops and black has 1 bishop an 1 knight
								&& game.getPieceCount(oppositeColor, GameConstants.BISHOP) == 1))
											&& (previous > -1 || previous < 1) //Tweak these values of 1 bishop advantage? I am not sure which values would be accurate. 
											&& (extendedGame.getPieceCount(color, GameConstants.BISHOP)-extendedGame.getPieceCount(oppositeColor,
													GameConstants.BISHOP) == 1 
													|| (extendedGame.getPieceCount(color, GameConstants.BISHOP) == 2) //Or if white has 2 bishops and black has 1 bishop an 1 knight
															&& extendedGame.getPieceCount(oppositeColor, GameConstants.BISHOP) == 1)
																	&& (!considerScore || ((current > 1 && color == GameConstants.WHITE) || (current < -1 && color == GameConstants.BLACK)))
																	&& game.getPieceCount(color, GameConstants.KNIGHT) == game.getPieceCount(oppositeColor, GameConstants.KNIGHT)
																	 && extendedGame.getPieceCount(color, GameConstants.KNIGHT) == extendedGame.getPieceCount(oppositeColor, GameConstants.KNIGHT);
	}
	
	/**
	 * Is there a double rook advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isDRookAdvantage(int color, Game game,
			Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return game.getPieceCount(color, GameConstants.ROOK) == 2
				&& game.getPieceCount(oppositeColor, GameConstants.ROOK) == 0
				&& (previous > -1 || previous < 1)
				&& extendedGame.getPieceCount(color, GameConstants.ROOK) == 2
				&& extendedGame.getPieceCount(oppositeColor, GameConstants.ROOK) == 0
				&& (!considerScore || ((current > 4.25 && color == GameConstants.WHITE) || (current < -4.25 && color == GameConstants.BLACK)));
	}
	
	/**
	 * Is there a single rook advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isSRookAdvantage(int color, Game game, Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return (game.getPieceCount(color, GameConstants.ROOK)-game.getPieceCount(oppositeColor,
						GameConstants.ROOK) == 1)  && (previous > -1 || previous < 1)
										&& (extendedGame.getPieceCount(color, GameConstants.ROOK)-extendedGame.getPieceCount(oppositeColor,
												GameConstants.ROOK) == 1) 
																&& (!considerScore || ((current > 2.1 && color == GameConstants.WHITE) || (current < -2.1 && color == GameConstants.BLACK)));
	}
	
	/**
	 * Is there a double knight advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isDKnightAdvantage(int color, Game game,
			Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return game.getPieceCount(color, GameConstants.KNIGHT) == 2
				&& game
						.getPieceCount(oppositeColor,
								GameConstants.KNIGHT) == 0
				&& (previous > -1 || previous < 1)
				&&  extendedGame.getPieceCount(color, GameConstants.KNIGHT) == 2
				&& extendedGame
				.getPieceCount(oppositeColor,
						GameConstants.KNIGHT) == 0
						&& (!considerScore || ((current > 2.6 && color == GameConstants.WHITE) || (current < -2.6 && color == GameConstants.BLACK)));
	}
	
	/**
	 * Is there a single knight advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isSKnightAdvantage(int color, Game game, Game extendedGame, double previous, double current, boolean considerScore) {
		int oppositeColor = (color == GameConstants.WHITE) ? GameConstants.BLACK
				: GameConstants.WHITE;

		return ((game.getPieceCount(color, GameConstants.KNIGHT)-game.getPieceCount(oppositeColor,
						GameConstants.KNIGHT) == 1) 
										 && (previous > -1 || previous < 1))										 
										&& ((extendedGame.getPieceCount(color, GameConstants.KNIGHT)-extendedGame.getPieceCount(oppositeColor,
						GameConstants.KNIGHT) == 1) 
								|| (extendedGame.getPieceCount(color, GameConstants.KNIGHT) == 2 //Of if white has 2 knights and black has 1 knight and 1 bishop
								&& extendedGame.getPieceCount(oppositeColor,
										GameConstants.KNIGHT) == 1))
										 && (!considerScore || ((current > 1 && color == GameConstants.WHITE) || (current < -1 && color == GameConstants.BLACK)))
										 && game.getPieceCount(color, GameConstants.BISHOP) == game.getPieceCount(oppositeColor, GameConstants.BISHOP)
										 && extendedGame.getPieceCount(color, GameConstants.BISHOP) == extendedGame.getPieceCount(oppositeColor, GameConstants.BISHOP);
	}
	
	/**
	 * Is there a pawn advantage for a given side.
	 * @param considerScore Whether take into account the score returned by the chess engine 
	 */
	private static boolean isPawnAdvantage(int color, Game game, Game extendedGame, double current, boolean considerScore) {
		int currentWhitePawnCount = game.getPieceCount(GameConstants.WHITE, GameConstants.PAWN);
		int currentBlackPawnCount = game.getPieceCount(GameConstants.BLACK, GameConstants.PAWN);
		int extendedWhitePawnCount = extendedGame.getPieceCount(GameConstants.WHITE, GameConstants.PAWN);
		int extendedBlackPawnCount = extendedGame.getPieceCount(GameConstants.BLACK, GameConstants.PAWN);
		int threshold = 2;
		
		if (color == GameConstants.WHITE)
			return (currentWhitePawnCount-currentBlackPawnCount) >=threshold && 
				(extendedWhitePawnCount-extendedBlackPawnCount) >=threshold && (!considerScore || current > 1.5);
		else 
			return ((currentBlackPawnCount-currentWhitePawnCount) >=threshold) &&
				(extendedBlackPawnCount-extendedWhitePawnCount) >=threshold && (!considerScore || current < -1.5);		
	}	
	
	/**
	 * Constructs a boolean array that contains true values on the respective indices 
	 * for specific advantage types. The meaning of each index is easily seen from the code. 
	 */
	boolean[] getAdvantageVector(int color, Game game, Game extendedGame, double previous, double current, boolean considerScore) {
		boolean[] advVector = new boolean[8];
		
		advVector[0] = isQueenAdvantage(color, game, extendedGame, current, considerScore) && (!considerScore || !queenFired);
		advVector[1] = isDRookAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !doubleRookFired);
		advVector[2] = isSRookAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !singleRookFired) && !advVector[2];
		advVector[3] = isDBishopAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !doubleBishopFired);
		advVector[4] = isSBishopAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !singleBishopFired) && !advVector[3];		
		advVector[5] = isDKnightAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !doubleKnightFired);
		advVector[6] = isSKnightAdvantage(color, game, extendedGame, previous, current, considerScore) && (!considerScore || !singleKnightFired) && !advVector[5];
		advVector[7] = isPawnAdvantage(color, game, extendedGame, current, considerScore) && !pawnFired;
		
		return advVector;
	}
	
	/**
	 * Returns the number of advantages a given advantage vector has. (basically counts 'trues' in the array)	
	 */
	static int getAdvantageMultiplicity(boolean[] advVector) {
		int advMultiplicity = 0;
		for (boolean val: advVector) {
			if (val)
				advMultiplicity++;
		}
		return advMultiplicity;
	}
	
	/**
	 * Returns the string with natural language description of each advantage in the given vector.
	 */
	static String getAdvantageName(boolean[] advVector) {
		String result = "";
		int multiplicity = getAdvantageMultiplicity(advVector);
		
		if (multiplicity == 1)
			return advToStringTranslate[ArrayUtils.indexOf(advVector, true)];
		
		for (int i = 0; i < advVector.length; i++) {
			if (advVector[i] && multiplicity >= 3) {
				result += advToStringTranslate[i]+", ";
				multiplicity--;
			}
			else if (advVector[i] && multiplicity == 2) {
				result += advToStringTranslate[i]+" and ";
				multiplicity--;
			}
			else if (advVector[i] && multiplicity == 1) {
				result += advToStringTranslate[i];
				break;
			}
		}
		return result;
	}
	
	/**
	 * Sets to 'fired' state the respective advantage switches
	 */
	void fireAdvantages(boolean[] advVector) {
		if (!queenFired)
			queenFired = advVector[0];
		if (!doubleRookFired)
			doubleRookFired = advVector[1];
		if (!singleRookFired)
			singleRookFired = advVector[2];
		if (!doubleBishopFired)
			doubleBishopFired = advVector[3];
		if (!singleBishopFired)
			singleBishopFired = advVector[4];
		if (!doubleKnightFired)
			doubleKnightFired = advVector[5];
		if (!singleKnightFired)
			singleKnightFired = advVector[6];
		if (!pawnFired)
			pawnFired = advVector[7];
	}
	
	int pieceMaterialScore(int piece) {
		if (piece == 0)
			return 0;
		
		if (piece == GameConstants.PAWN)
			return 1;
		else if (piece == GameConstants.KNIGHT
				|| piece == GameConstants.BISHOP)
			return 3;
		else if (piece == GameConstants.ROOK)
			return 5;
		else
			return 9;
	}
	
	//I added in simple comments for single and double bishop, rook, knight and a queen advantage. Also a castling advantage and a 'blunder!' comment. I would like to add more.
	//Sorry for my coding style I am fairly new to Java and I find it hard to write code using your bracketing but I will edit my code back to your format after it is fully finished.
	
	//More ideas for comments that I am unsure of how to implement
	// - If someone is in check, the piece checking is a knight (is it possible to find this out through the code? can only find game.isInCheck() ) , then the next move the knight goes on to capture a piece - comment 'Nice fork!'
	// - Can do similar as above but for bishops? 
	// - Can check if a piece is pinned by a rook, ie there is a King/Queen along diagonal behind a piece that a bishop is attacking - much harder to do!
	// - As below, can develop 'What a sacrifice'!
	// - Add comments for 'Queens gambit accepted' - but I think this may be done at the bottom of the screen by another part of the program already? Would be helpful to put it in as a comment alot?
	// - Add more precise analysis for 'Blunder', 'Bad move', 'Very bad move' analysis. 
	// - What other information can we draw from the Stockfish engine other than a value of each position and how can we use it to compute the above?\
	// - Have raptor export these comments with a pgn file for the game.

	
	//I made these all single if statements with the more urgent comments higher than others. I had problems with it was all one long if else....
	
	public String getComment(List<ScoreInfo> positionScores, AutomaticAnalysisController controller, double scoreDiff,
			boolean isWhite, BestLineFoundInfo thisPosBestLine, Game game) {
		
		double previous = controller.asDouble(positionScores.get(positionScores.size()-2));
		double current = controller.asDouble(positionScores.get(positionScores.size()-1));
		
		if (current == Double.MAX_VALUE || current == Double.MIN_VALUE 
				|| game.isCheckmate())
			return "";
		
		Game extendedGame = extendMoves(thisPosBestLine, game);	
		
		if ((isWhite && previous < -2.0 && scoreDiff >= 2.0)
				|| (!isWhite && previous > 2.0 && scoreDiff >= 2.0)) {	
			String result = "Loses the initiative."; //White or black had an advantage by over 2 points, then score changed by at least 2 and hence they lose the initiative
			if (game.isInCheck())
				result += " Was not wise make this check.";
			
			return result;
		}
		
		if ((isWhite && previous < -2.0 && scoreDiff >= 4.0)
				|| (!isWhite && previous > 2.0 && scoreDiff >= 4.0))
			return "Blunder!";

		// forks recognition code
		if (positionScores.size() >= 3) {
			double minThSc = controller.asDouble(positionScores
					.get(positionScores.size() - 3));
			double minThDiff = Math.abs(minThSc - current);
			if (game.isInCheck()
					&& game.getPiece(thisPosBestLine.getMoves()[1]
							.getEndSquare()) != GameConstants.EMPTY
					&& ((isWhite && minThSc > -1.0 && minThDiff >= 2.0) || (!isWhite
							&& minThSc < 1.0 && minThDiff >= 2.0)))
				return "Nice fork!";
		}
		
		/*System.out.println("This move: " + game.getLastMove().getSan());
		int materialBefore = GameUtils.getMaterialScore(game);
		Move lastMove = game.getLastMove();
		materialBefore += (isWhite ? pieceMaterialScore(lastMove.getCapture()) :
			-pieceMaterialScore(lastMove.getCapture()));
		System.out.println("materialBefore: " + materialBefore);
		System.out.println("materialExtend: " + GameUtils.getMaterialScore(extendedGame));
		
		if (((isWhite && current > 2 && GameUtils.getMaterialScore(extendedGame) < materialBefore) 
				|| (!isWhite && current < -2 && GameUtils.getMaterialScore(extendedGame) > materialBefore) 
				&& lastMove.isCapture())) {
			return "SAC";			
		}*/
		
		boolean[] whiteAdvVec = getAdvantageVector(GameConstants.WHITE, game, extendedGame, previous, current, true);
		boolean[] blackAdvVec = getAdvantageVector(GameConstants.BLACK, game, extendedGame, previous, current, true);
		
		int whiteAdvMultiplicity = getAdvantageMultiplicity(whiteAdvVec);		
		int blackAdvMultiplicity = getAdvantageMultiplicity(blackAdvVec);

		if (whiteAdvMultiplicity > 0) {
			String comment = "White has the advantage of ";
			boolean[] blackAdvVecScoreless = getAdvantageVector(GameConstants.BLACK, game, extendedGame, previous, current, false);
			int blackScorelessMul = getAdvantageMultiplicity(blackAdvVecScoreless);
			comment += getAdvantageName(whiteAdvVec);
			if (blackScorelessMul > 0)
				comment += " versus " + getAdvantageName(blackAdvVecScoreless) + " in black";

			fireAdvantages(whiteAdvVec);
			return comment+".";
		}
		else if (blackAdvMultiplicity > 0) {
			String comment = "Black has the advantage of ";
			boolean[] whiteAdvVecScoreless = getAdvantageVector(GameConstants.WHITE, game, extendedGame, previous, current, false);
			int whiteScorelessMul = getAdvantageMultiplicity(whiteAdvVecScoreless);
			comment += getAdvantageName(blackAdvVec);
			if (whiteScorelessMul > 0)
				comment += " versus " + getAdvantageName(whiteAdvVecScoreless) + " in white";

			fireAdvantages(blackAdvVec);
			return comment+".";
		}



		//Need to be edited to check if a player has castled and opponent cannot, then make a castle advantage comment. Not sure of how to do this.
		if(!whiteCastleFired){
			
			
			if (!game.canBlackCastleLong() && !game.canBlackCastleShort() && (game.canWhiteCastleLong() || game.canWhiteCastleLong()) 
					&& notAlreadyCastled(game, GameConstants.BLACK)) {
			
				
				whiteCastleFired = true;
				return "White has the advantage of being able to castle over Black.";
			}
		}
		
		if(!blackCastleFired){
			if (!game.canWhiteCastleLong() && !game.canWhiteCastleShort() && (game.canBlackCastleLong() || game.canBlackCastleLong())
					&& notAlreadyCastled(game, GameConstants.WHITE)) {
				blackCastleFired = true;
				return "Black has the advantage of being able to castle over White.";
			}
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
		

		return "";
	}
}

