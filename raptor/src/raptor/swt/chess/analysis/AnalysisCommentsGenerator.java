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
	boolean singleBishopFired = false;
	
	boolean singleRookFired = false;
	boolean doubleRookFired =false;
	
	boolean doubleKnightFired = false;
	boolean singleKnightFired = false;
	
	boolean queenFired=false;
	
	boolean pawnFired=false;
	
	boolean blackCastleFired=false;
	boolean whiteCastleFired=false;
	
	int lastMaterial = 0;
	
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
	
	public String getComment(double previous, double current, double scoreDiff,
			boolean isWhite, BestLineFoundInfo thisPosBestLine, Game game) {
		
		
		if ((isWhite && previous < -2.0 && scoreDiff >= 2.0)
				|| (!isWhite && previous > 2.0 && scoreDiff >= 2.0))
			return "Loses the initiative."; //White or black had an advantage by over 2 points, then score changed by at least 2 and hence they lose the initiative 
		
		if ((isWhite && previous < -2.0 && scoreDiff >= 4.0)
				|| (!isWhite && previous > 2.0 && scoreDiff >= 4.0))
			return "Blunder!.";
				
		if (!queenFired){
			
			if (game.getPieceCount(GameConstants.WHITE, GameConstants.QUEEN) == 1
					&& game.getPieceCount(GameConstants.BLACK, GameConstants.QUEEN) == 0 ){
				queenFired = true;
				return "White has the significant advantage of a queen.";
				
			}
			else if (game.getPieceCount(GameConstants.BLACK, GameConstants.QUEEN) == 1
					&& game.getPieceCount(GameConstants.WHITE, GameConstants.QUEEN) == 0 ){
				queenFired = true;
				return "Black has the significant advantage of a queen.";
				
			}
		}
		
		if (!doubleBishopFired) {
			if (game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP) == 2
					&& game.getPieceCount(GameConstants.BLACK,
							GameConstants.BISHOP) == 0 && (previous > -1 || previous < 1)) //On the previous move, score was roughly even..
				{
				doubleBishopFired = true;
				return "White has the advantage of double bishops.";
			}
			else if (game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP) == 2
					&& game.getPieceCount(GameConstants.WHITE,
							GameConstants.BISHOP) == 0 && (previous > -1 || previous < 1)) 
			{
				doubleBishopFired = true;
				return "Black has the advantage of double bishops.";
			}//Delete if unwanted
			else if (!singleBishopFired){
				
				if ((game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP) == 1  //If white has 1 bishop and black has none and no knights
						&& game.getPieceCount(GameConstants.BLACK,
								GameConstants.BISHOP) == 0) 
								&& game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 0
								|| (game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP) == 2 //Or if white has 2 bishops and black has 1 bishop an 1 knight
										&& game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP) == 1)
												&& game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 1
													&& (previous > -1 || previous < 1) )//Tweak these values of 1 bishop advantage? I am not sure which values would be accurate. 
				{
					singleBishopFired = true;
					return "White has the advantage of a single bishop.";
					
					
				}
				else if ((game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP) == 1 //If Black has 1 bishop and White has none and no knights
						&& game.getPieceCount(GameConstants.WHITE,
								GameConstants.BISHOP) == 0) 
								&& game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 0
									||(game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP) == 2 //Or if Blck has 2 bishops and white has 1 bishop an 1 knight
										&& game.getPieceCount(GameConstants.WHITE,GameConstants.BISHOP) == 1) 
											&& game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 1
												&& (previous > -1 || previous < 1) ){
					singleBishopFired = true;
					return "Black has the advantage of a single bishop.";
				}
				
			}
			
			
		}	
		
		if (!doubleRookFired){
				
				if (game.getPieceCount(GameConstants.WHITE, GameConstants.ROOK) == 2
						&& game.getPieceCount(GameConstants.BLACK, GameConstants.ROOK) == 0 && (previous > -1 || previous <1)){
					doubleRookFired = true;
					return "White has the advantage of double rooks.";
					
				}
				else if (game.getPieceCount(GameConstants.BLACK, GameConstants.ROOK) == 2
						&& game.getPieceCount(GameConstants.WHITE, GameConstants.ROOK) == 0 && (previous > -1 || previous <1)){
					doubleRookFired = true;
					return "Black has the advantage of double rooks.";
					
				}//Delete if unwanted
				else if (!singleBishopFired){
					
					if ((game.getPieceCount(GameConstants.WHITE, GameConstants.ROOK) == 1
							&& game.getPieceCount(GameConstants.BLACK,
									GameConstants.ROOK) == 0) || (game.getPieceCount(GameConstants.WHITE, GameConstants.ROOK) == 2
											&& game.getPieceCount(GameConstants.BLACK,
													GameConstants.ROOK) == 1) && (previous > -1 || previous < 1) ){
						singleRookFired = true;
						return "White has the advantage of a single rook.";
						
						
					}
					else if ((game.getPieceCount(GameConstants.BLACK, GameConstants.ROOK) == 1
							&& game.getPieceCount(GameConstants.WHITE,
									GameConstants.ROOK) == 0) ||(game.getPieceCount(GameConstants.BLACK, GameConstants.ROOK) == 2
											&& game.getPieceCount(GameConstants.WHITE,
													GameConstants.ROOK) == 1) && (previous > -1 || previous < 1) ){
						singleRookFired = true;
						return "Black has the advantage of a single rook.";
					}
				
				}
					
					
				
		}
		
		if(!doubleKnightFired){
			
			if (game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 2
					&& game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 0 && (previous > -1 || previous <1)){
				doubleKnightFired = true;
				return "White has the advantage of double knights.";
				
			}
			else if (game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 2
					&& game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 0 && (previous > -1 || previous <1)){
				doubleKnightFired = true;
				return "Black has the advantage of double knights.";
				
			}//Delete if unwanted
			else if (!singleKnightFired){
				
				if ((game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 1  //If white has 1 knight and black has none and no bishops
						&& game.getPieceCount(GameConstants.BLACK,
								GameConstants.KNIGHT) == 0) && (game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP)) == 0
										|| (game.getPieceCount(GameConstants.WHITE, GameConstants.KNIGHT) == 2 //Of if white has 2 knights and black has 1 knight and 1 bishop
										&& game.getPieceCount(GameConstants.BLACK,
												GameConstants.KNIGHT) == 1) 
												 && (game.getPieceCount(GameConstants.BLACK, GameConstants.BISHOP)) ==1
												 && (previous > -1 || previous < 1) ){
					singleKnightFired = true;
					return "White has the advantage of a single knight.";
					
					
				}
				else if ((game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 1
						&& game.getPieceCount(GameConstants.WHITE,
								GameConstants.KNIGHT) == 0)  && (game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP)) == 0
								||(game.getPieceCount(GameConstants.BLACK, GameConstants.KNIGHT) == 2
										&& game.getPieceCount(GameConstants.WHITE,
												GameConstants.KNIGHT) == 1) 
												&& (game.getPieceCount(GameConstants.WHITE, GameConstants.BISHOP)) ==1
												&& (previous > -1 || previous < 1) ){
					
					singleKnightFired = true;
					return "Black has the advantage of a single knight.";
				}
			
			}
			
			
			
			
			
		}
		
		if(!pawnFired) //player has advantage of over 5 pawns. Can adjust threshold. Can add in score dependencies. 
		{
			int currentWhitePawnCount = game.getPieceCount(GameConstants.WHITE, GameConstants.PAWN);
			int currentBlackPawnCount = game.getPieceCount(GameConstants.BLACK, GameConstants.PAWN);
			int threshold = 2;
			
			if ((currentWhitePawnCount-currentBlackPawnCount) >=threshold)
			{
				
				pawnFired = true;
				return "White has an advantage of over " + threshold + " pawns over Black.";
			}
			else if (((currentBlackPawnCount-currentWhitePawnCount) >=threshold)){
				
				pawnFired = true;
				return "Black has an advantage of over " + threshold + " pawns over White.";
			}
		
			
		}
		
		//Need to be edited to check if a player has castled and opponent cannot, then make a castle advantage comment. Not sure of how to do this. 
		if(!whiteCastleFired){
			
			
			if (!game.canBlackCastleLong() && !game.canBlackCastleShort() && (game.canWhiteCastleLong() || game.canWhiteCastleLong())){
			
				
				whiteCastleFired = true;
				return "White has the advantage of being able to castle over Black.";
			}
		}
		
		if(!blackCastleFired){
			if (!game.canWhiteCastleLong() && !game.canWhiteCastleShort() && (game.canBlackCastleLong() || game.canBlackCastleLong())){
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

