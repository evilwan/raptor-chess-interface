/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.problemgen;

import java.io.FileReader;
import java.util.Date;

import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.MoveList;
import raptor.chess.Variant;
import raptor.chess.pgn.LenientPgnParserListener;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserError;
import raptor.chess.pgn.StreamingPgnParser;
import raptor.engine.uci.UCIBestMove;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIInfo;
import raptor.engine.uci.UCIInfoListener;
import raptor.engine.uci.UCIMove;
import raptor.engine.uci.info.BestLineFoundInfo;
import raptor.engine.uci.info.ScoreInfo;

public class ProblemGenerator {
	protected class CandidateInfoListener implements UCIInfoListener {
		protected UCIBestMove bestMove;
		protected ScoreInfo score;
		protected BestLineFoundInfo bestLineFound;
		protected boolean hasIssuedStop = false;

		public void engineSentBestMove(UCIBestMove uciBestMove) {
			bestMove = uciBestMove;
		}

		public void engineSentInfo(UCIInfo[] infos) {
			for (UCIInfo info : infos) {
				if (info instanceof ScoreInfo) {
					score = (ScoreInfo) info;
					if (!hasIssuedStop && score.getMateInMoves() != 0
							|| !score.isLowerBoundScore()
							&& !score.isUpperBoundScore()) {
						hasIssuedStop = true;
						new Thread(new Runnable() {
							public void run() {
								engine.stop();
							}
						}).start();
					}
				} else if (info instanceof BestLineFoundInfo) {
					bestLineFound = (BestLineFoundInfo) info;
				}
			}
		}

		public boolean isFinished() {
			return bestMove != null;
		}
	}

	protected class ProblemInfoListener implements UCIInfoListener {
		protected UCIBestMove bestMove;
		protected ScoreInfo score;
		protected BestLineFoundInfo bestLineFound;
		protected double worstScore = .55599;
		protected double bestScore = .55599;
		protected boolean isWhitesMove;

		public void engineSentBestMove(UCIBestMove uciBestMove) {
			bestMove = uciBestMove;
		}

		public void engineSentInfo(UCIInfo[] infos) {
			for (UCIInfo info : infos) {
				if (info instanceof ScoreInfo) {
					score = (ScoreInfo) info;
					if (score.getMateInMoves() == 0) {
						double scoreInPawns = getScoreInPawns(score,
								isWhitesMove);

						if (bestScore == .55599) {
							worstScore = bestScore = scoreInPawns;
						}
						if (scoreInPawns > bestScore) {
							bestScore = scoreInPawns;
						} else if (scoreInPawns < worstScore) {
							worstScore = scoreInPawns;
						}
					}
				} else if (info instanceof BestLineFoundInfo) {
					bestLineFound = (BestLineFoundInfo) info;
				}
			}
		}

		public boolean isFinished() {
			return bestMove != null;
		}
	}

	public static void main(String[] args) throws Exception {
		UCIEngine engine = new UCIEngine();
		engine.setUsingThreadService(false);
		engine.setProcessPath("/Applications/HIARCS/Hiarcs12.1SPUCI");
		new ProblemGenerator(engine,
				"/Users/mindspan/raptor/raptor/projectFiles/test/Alekhine4Pawns.pgn");
	}

	protected int numGames;
	protected UCIEngine engine;
	protected StreamingPgnParser parser;
	protected String outputFile = "/Users/mindspan/problemGeneratorOutput.txt";

	protected LenientPgnParserListener parserLisetener = new LenientPgnParserListener() {

		@Override
		public void errorEncountered(PgnParserError error) {

		}

		@Override
		public void gameParsed(Game game, int lineNumber) {
			if (game.getVariant() == Variant.classic) {
				checkGameForCandidates(game);
			}
		}
	};

	public ProblemGenerator(UCIEngine engine, String pgnFile) throws Exception {
		this.engine = engine;
		parser = new StreamingPgnParser(new FileReader(pgnFile),
				Integer.MAX_VALUE);
		parser.addPgnParserListener(parserLisetener);
		parser.parse();
	}

	public void checkGameForCandidates(Game game) {
		System.err.println(new Date() + " " + "Checking game: " + numGames++
				+ " " + game.getHeader(PgnHeader.Event) + " "
				+ game.getHeader(PgnHeader.White) + "("
				+ game.getHeader(PgnHeader.WhiteElo) + ") vs "
				+ game.getHeader(PgnHeader.Black) + "("
				+ game.getHeader(PgnHeader.BlackElo) + ")");

		MoveList moveList = game.getMoveList().deepCopy();
		if (moveList.getSize() > 20) {
			while (game.getMoveList().getSize() > 10) {
				game.rollback();
			}
			if (!engine.isConnected()) {
				engine.connect();
			}
			engine.newGame();
			engine.isReady();

			while (moveList.getSize() > game.getHalfMoveCount()) {
				engine.setPosition(game.toFen(), null);
				if (isCandidate()) {
					System.err
							.println("Found candidate. Testing to see if its a real problem.");
					testForProblem(game, moveList);
					break;
				} else {
					game.move(moveList.get(game.getHalfMoveCount()));
				}
			}
		}
	}

	public String getLine(Game game, BestLineFoundInfo info) {
		BestLineFoundInfo bestLineFoundInfo = info;
		StringBuilder line = new StringBuilder(100);
		game.addState(Game.UPDATING_SAN_STATE);
		game.clearState(Game.UPDATING_ECO_HEADERS_STATE);

		boolean isFirstMove = true;

		for (UCIMove move : bestLineFoundInfo.getMoves()) {
			try {
				Move gameMove = null;

				if (move.isPromotion()) {
					gameMove = game.makeMove(move.getStartSquare(), move
							.getEndSquare(), move.getPromotedPiece());
				} else {
					gameMove = game.makeMove(move.getStartSquare(), move
							.getEndSquare());
				}

				String san = gameMove.getSan();
				String moveNumber = isFirstMove && !gameMove.isWhitesMove() ? gameMove
						.getFullMoveCount()
						+ ") ... "
						: gameMove.isWhitesMove() ? gameMove.getFullMoveCount()
								+ ") " : "";
				line.append((line.toString().equals("") ? "" : " ") + moveNumber + san
						+ (game.isInCheck() ? "+" : "")
						+ (game.isCheckmate() ? "#" : ""));
				isFirstMove = false;
			} catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
		return line.toString();
	}

	protected double getScoreInPawns(ScoreInfo score, boolean isWhitesMove) {
		return !isWhitesMove ? -1 * score.getValueInCentipawns() / 100.0
				: score.getValueInCentipawns() / 100.0;
	}

	protected void handleNewProblem(Game game, BestLineFoundInfo bestLine) {
		System.err.println("\nFound problem:\n" + game.toFen() + "\n"
				+ getLine(game, bestLine) + "\n\n");
	}

	protected boolean isCandidate() {
		boolean result = false;

		CandidateInfoListener listener = new CandidateInfoListener();
		engine.go("infinite", listener);
		while (!listener.isFinished()) {
			try {
				Thread.sleep(100);
			} catch (Throwable t) {
			}
		}

		if (listener.score.getMateInMoves() > 0
				|| Math.abs(listener.score.getValueInCentipawns() / 100.0) > 3) {
			result = true;
		}
		return result;
	}

	protected void testForProblem(Game game, MoveList moveList) {
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		for (int i = 0; i < 4; i++) {
			engine.newGame();
			engine.setPosition(game.toFen(), null);
			engine.isReady();
			ProblemInfoListener listener = new ProblemInfoListener();
			listener.isWhitesMove = game.isWhitesMove();
			engine.go("infinite", listener);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
			}
			engine.stop();
			while (!listener.isFinished()) {
				try {
					Thread.sleep(100);
				} catch (Throwable t) {
				}
			}
			double finalScore = getScoreInPawns(listener.score, game
					.isWhitesMove());
			if (listener.score.getMateInMoves() != 0
					|| Math.abs(listener.bestScore - listener.worstScore) >= 2.0
					&& Math.abs(finalScore) >= 2.0) {
				handleNewProblem(game, listener.bestLineFound);
				break;
			}
			game.move(moveList.get(game.getHalfMoveCount()));
		}
	}
}
