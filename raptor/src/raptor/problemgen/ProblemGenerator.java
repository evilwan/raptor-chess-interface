package raptor.problemgen;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	protected class ProblemGeneratorUCIInfoListener implements UCIInfoListener {
		protected UCIBestMove bestMove;
		protected ScoreInfo score;
		protected BestLineFoundInfo bestLineFound;

		public void engineSentBestMove(UCIBestMove uciBestMove) {
			bestMove = uciBestMove;
		}

		public void engineSentInfo(UCIInfo[] infos) {
			for (UCIInfo info : infos) {
				if (info instanceof ScoreInfo) {
					score = (ScoreInfo) info;
				} else if (info instanceof BestLineFoundInfo) {
					bestLineFound = (BestLineFoundInfo) info;
				}
			}
		}

		public boolean isFinished() {
			return bestMove != null;
		}
	}

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
				checkForProblems(game);
			}
		}
	};

	public static void main(String[] args) throws Exception {
		UCIEngine engine = new UCIEngine();
		engine.setUsingThreadService(false);
		engine.setProcessPath("/Applications/HIARCS/Hiarcs12.1SPUCI");
		new ProblemGenerator(engine,
				"/Users/mindspan/raptor/raptor/projectFiles/test/Alekhine4Pawns.pgn");
	}

	public ProblemGenerator(UCIEngine engine, String pgnFile) throws Exception {
		this.engine = engine;
		parser = new StreamingPgnParser(new FileReader(pgnFile),
				Integer.MAX_VALUE);
		parser.addPgnParserListener(parserLisetener);
		parser.parse();
	}

	public void checkForProblems(Game game) {
		System.err.println("Checking game: " + game.getHeader(PgnHeader.Event)
				+ " " + game.getHeader(PgnHeader.White) + "("
				+ game.getHeader(PgnHeader.WhiteElo) + ") vs "
				+ game.getHeader(PgnHeader.Black) + "("
				+ game.getHeader(PgnHeader.BlackElo) + ")");

		MoveList moveList = game.getMoveList().deepCopy();
		if (moveList.getSize() > 10) {
			while (game.getMoveList().getSize() > 10) {
				game.rollback();
			}
			if (!engine.isConnected()) {
				engine.connect();
			}
			engine.newGame();
			engine.isReady();

			System.err.println(moveList.getSize() + " "
					+ game.getHalfMoveCount());
			while (moveList.getSize() > game.getHalfMoveCount()) {
				engine.setPosition(game.toFen(), null);

				System.err.println("Checking position: " + game.toFen());

				ProblemGeneratorUCIInfoListener listener = new ProblemGeneratorUCIInfoListener();
				engine.go("infinite", listener);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				engine.stop();
				while (!listener.isFinished()) {
					try {
						Thread.sleep(100);
					} catch (Throwable t) {
					}
				}

				if (listener.score.getMateInMoves() > 0
						|| !listener.score.isLowerBoundScore()
						&& !listener.score.isUpperBoundScore()
						&& Math
								.abs(listener.score.getValueInCentipawns() / 100.0) > 3) {
					String output = game.getHeader(PgnHeader.Event) + " "
							+ game.getHeader(PgnHeader.White) + "("
							+ game.getHeader(PgnHeader.WhiteElo) + ") vs "
							+ game.getHeader(PgnHeader.Black) + "("
							+ game.getHeader(PgnHeader.BlackElo) + ")\n"
							+ game.toFen() + "\n"
							+ getLine(game, listener.bestLineFound) + "\n\n";

					FileWriter writer = null;
					try {
						writer = new FileWriter(outputFile, true);
						writer.append(output);
						writer.flush();
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					} finally {
						try {
							writer.close();
						} catch (IOException ioe) {
						}
					}
					System.err.println("Found problem:\n" + output);

					return;
				}
				game.move(moveList.get(game.getHalfMoveCount()));
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
				line.append((line.equals("") ? "" : " ") + moveNumber + san
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
}
