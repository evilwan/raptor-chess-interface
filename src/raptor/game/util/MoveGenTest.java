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

import static raptor.game.util.GameUtils.createFromFen;
import static raptor.game.util.GameUtils.createStartingPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.Game.Type;

public class MoveGenTest extends TestCase implements GameConstants {

	public static void main(String args[]) {
		MoveGenTest test = new MoveGenTest();
		test.testOccupiedEmpty();
		test.testPawnCaptures();
		test.testThreeXRep();
		test.testBasicRepHashing();
		test.testRepHashing();
		test.testPieceDisappearBug();
		test.testPieceCounts();
		test.testValidCastle();
		test.testPromotions();
		test.testNotCheckmate();
		test.testInCheck();
		test.testLegalMoves();
		test.testLegals();
		System.out.println("All tests passed.");
	}

	String[] NOT_CHECKMATE_TESTS = new String[] { "k7/2K5/8/8/8/8/8/7R b - - 0 0" };
	String[] IN_CHECK_TESTS = new String[] {};// "8/8/6Kk/8/8/8/8 b - - 0 805",
	// "8/8/6Kk/8/8/8/8 w - - 0 805" };

	String[] LEGAL_MOVE_TESTS = new String[] {
			"k7/7p/8/pP6/8/8/8/7K w - a6 0 0|b5xa6|b5-b6|h1-g1|h1-g2|h1-h2",
			"1BR1QR2/1n2P2k/2Q1R2P/1P3B2/4P1N1/2K5/8/1N1r b - - 0 185",
			"k7/7P/2K5/8/8/8/8/8 w - - 0 0|c6-b5|c6-c5|c6-d5|c6-d6|c6-c7|c6-d7|c6-b6|h7-h8=Q|h7-h8=N|h7-h8=B|h7-h8=R" };

	String[] PSEUDO_LEGAL_MOVE_TESTS = new String[] {
			"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0|a2-a3|a2-a4|b2-b3|b2-b4|c2-c3|c2-c4|d2-d3|d2-d4|e2-e3|e2-e4|f2-f3|f2-f4|g2-g3|g2-g4|h2-h3|h2-h4|b1-a3|b1-c3|g1-f3|g1-h3",
			"rnbqkbnr/pppppppp/8/8/3P5/8/PPP8PPPP/RNBQKBNR b KQkq - 0 0|a7-a6|a7-a5|b7-b6|b7-b5|c7-c6|c7-c5|d7-d6|d7-d5|e7-e6|e7-e5|f7-f6|f7-f5|g7-g6|g7-g5|h7-h6|h7-h5|b8-a6|b8-c6|g8-f6|g8-h6" };

	String[] PROMOTION_TEST = { "k7/7P/2K5/8/8/8/8/8 w - - 0 0|h7-h8=R|a8-a7" };

	String[] VALID_CASTLE_TEST = {
			"k7/8/8/8/8/8/PPPPPPPP/R3K2R w QK - 0 0|O-O-O|O-O",
			"k7/8/8/8/8/8/PPPPPPPP/R3K2R w K - 0 0|O-O",
			"k7/8/8/8/8/8/PPPPPPPP/R3K2R w Q - 0 0|O-O-O",
			"r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b qk - 0 0|O-O-O|O-O",
			"r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b k - 0 0|O-O",
			"r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R b q - 0 0|O-O-O",
			"k7/8/8/8/8/8/PPPPPPPP/RN2K2R w QK - 0 0|O-O",
			"k7/8/8/8/8/8/PPPPPPPP/R3KN1R w QK - 0 0|O-O-O",
			"k5r1/8/8/8/8/8/PPPPPP1P/R3K2R w QK - 0 0|O-O-O",
			"2r3k1/8/8/8/8/8/PP1PPPPP/R3K2R w QK - 0 0|O-O",
			"2r3k1/8/8/8/8/8/PPPPrPPPP/R3K2R w QK - 0 0" };

	public void testBasicRepHashing() {
		Game game = createStartingPosition(Type.CLASSIC);

		long positionOnlyHash = game.getZobristPositionHash();
		long gameHash = game.getZobristGameHash();

		// System.out.println("Position hash: " + positionOnlyHash);
		// System.out.println("Game hash: " + gameHash);

		Move g1f3 = new Move(SQUARE_G1, SQUARE_F3, KNIGHT, WHITE, EMPTY);
		game.forceMove(g1f3);
		game.rollback();

		asserts(game.getZobristPositionHash() == positionOnlyHash,
				"Position Hashes were not equal");
		asserts(game.getZobristGameHash() == gameHash,
				"Game Hashes were not equal");
	}

	public void testInCheck() {
		for (String fen : IN_CHECK_TESTS) {
			Game game = createFromFen(fen, Type.CLASSIC);
			asserts(game.isInCheck(game.getColorToMove()),
					"User is in check in position:\n " + game);
		}
	}

	public void testLegalMoves() {
		for (String test : LEGAL_MOVE_TESTS) {
			String[] split = test.split("\\|");

			Game game = createFromFen(split[0], Type.CLASSIC);

			Move[] moves = game.getPseudoLegalMoves().asArray();
			for (Move move : moves) {
				if (game.move(move)) {
					game.rollback();
				}
			}

			if (split.length == 1) {
				asserts(game.getLegalMoves().asArray().length == 0,
						"Game contained legal mvoes in checkmate");
			} else {

				List<Move> expectedMoves = new ArrayList<Move>(split.length - 1);
				for (int i = 1; i < split.length; i++) {
					expectedMoves.add(game.makeLanMove(split[i]));
					game.rollback();
				}

				asserts(expectedMoves, game.getLegalMoves().asList(), game);
			}
		}
	}

	public void testLegals() {
		for (String test : PSEUDO_LEGAL_MOVE_TESTS) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Type.CLASSIC);

			if (split.length == 1) {
				asserts(game.getPseudoLegalMoves().asArray().length == 0,
						"Game contained legal mvoes in checkmate\n" + game);
			} else {
				Move[] moveList = game.getLegalMoves().asArray();

				asserts(moveList.length == split.length - 1,
						"Invalid expected number of moves " + split.length
								+ " " + moveList.length + " " + game);

				for (Move move : moveList) {
					boolean foundMove = false;
					for (int i = 1; i < split.length; i++) {
						if (move.getLan().equals(split[i])) {
							foundMove = true;
							split[i] = null;
							break;
						}
					}

					if (!foundMove) {
						asserts(foundMove, "Could not find move: "
								+ move.getLan() + " in the list of legals.\n"
								+ game);
					}
				}

				for (int i = 1; i < split.length; i++) {
					asserts(split[i] == null,
							"The following move was not returned as a legal: "
									+ split[i] + " \n");
				}
			}
		}
	}

	public void testNotCheckmate() {
		for (String fen : NOT_CHECKMATE_TESTS) {
			Game game = createFromFen(fen, Type.CLASSIC);
			asserts(!game.isCheckmate(), "Position is not checkmate: \n" + game);
		}
	}

	public void testOccupiedEmpty() {
		Game game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("e2-e4");
		game.makeLanMove("h7-h6");
		game.makeLanMove("d2-d4");
		game.makeLanMove("g7-g5");
		game.makeLanMove("f2-f4");
		game.makeLanMove("g5xf4");
		game.makeLanMove("c1xf4");
		game.makeLanMove("c7-c6");
		game.makeLanMove("f4xh6");
		game.makeLanMove("h8xh6");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();

		Game game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

	}

	public void testPawnCaptures() {
		Game game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("e2-e4");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e4xd5");
		game.rollback();
		game.rollback();
		game.rollback();

		Game game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("e2-e4");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e4-e5");
		game.makeLanMove("f7-f5");
		game.makeLanMove("e5xf6");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("e2-e4");
		game.makeLanMove("h7-h6");
		game.makeLanMove("e4-e5");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e5xd6");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("h2-h3");
		game.makeLanMove("d7-d5");
		game.makeLanMove("h3-h4");
		game.makeLanMove("d5-d4");
		game.makeLanMove("e2-e4");
		game.makeLanMove("d4xe3");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("h2-h3");
		game.makeLanMove("d7-d5");
		game.makeLanMove("h3-h4");
		game.makeLanMove("d5-d4");
		game.makeLanMove("c2-c4");
		game.makeLanMove("d4xc3");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Type.CLASSIC);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");
	}

	public void testPieceCounts() {
		Game game = createStartingPosition(Type.CLASSIC);
		asserts(game.getPieceCount(WHITE, PAWN) == 8, "Not 8 white pawns.");
		asserts(game.getPieceCount(BLACK, PAWN) == 8, "Not 8 black pawns.");
		asserts(game.getPieceCount(WHITE, KNIGHT) == 2, "Not 2 white knights.");
		asserts(game.getPieceCount(BLACK, KNIGHT) == 2, "Not 2 black knights.");
		asserts(game.getPieceCount(WHITE, BISHOP) == 2, "Not 2 white bishops.");
		asserts(game.getPieceCount(BLACK, BISHOP) == 2, "Not 2 black bishops.");
		asserts(game.getPieceCount(WHITE, ROOK) == 2, "Not 2 white rooks.");
		asserts(game.getPieceCount(BLACK, ROOK) == 2, "Not 2 black rooks.");
		asserts(game.getPieceCount(WHITE, QUEEN) == 1, "Not 1 white queen.");
		asserts(game.getPieceCount(BLACK, QUEEN) == 1, "Not 1 black queen.");
		asserts(game.getPieceCount(WHITE, KING) == 1, "Not 1 white king.");
		asserts(game.getPieceCount(BLACK, KING) == 1, "Not 1 black king.");

		game.makeLanMove("e2-e4");
		game.makeLanMove("g8-f6");
		game.makeLanMove("d2-d4");
		game.makeLanMove("f6xe4");
		asserts(game.getPieceCount(WHITE, PAWN) == 7, "Not 7 white pawns\n"
				+ game);
		game.makeLanMove("d4-d5");
		game.makeLanMove("e4-c3");
		game.makeLanMove("d5-d6");
		game.makeLanMove("c3xb1");
		game.makeLanMove("a1xb1");
		asserts(game.getPieceCount(WHITE, KNIGHT) == 1, "Not 1 white knight\n"
				+ game);
		asserts(game.getPieceCount(BLACK, KNIGHT) == 1, "Not 1 black knight\n"
				+ game);
		game.makeLanMove("a7-a6");
		game.makeLanMove("d6xc7");
		game.makeLanMove("a6-a5");
		game.makeLanMove("c7xb8=Q");
		asserts(game.getPieceCount(WHITE, PAWN) == 6, "Not 6 white pawns "
				+ game.getPieceCount(WHITE, PAWN) + "\n" + game);
		asserts(game.getPieceCount(WHITE, QUEEN) == 2, "Not 2 white queens\n"
				+ game);
		asserts(game.getPieceCount(BLACK, KNIGHT) == 0, "Not 0 black knights\n"
				+ game);
		game.makeLanMove("a8xb8");
		asserts(game.getPieceCount(WHITE, QUEEN) == 1, "Not 1 white queen\n"
				+ game);
		game.rollback();
		asserts(game.getPieceCount(WHITE, QUEEN) == 2, "Not 2 white queen\n"
				+ game);
		game.rollback();
		asserts(game.getPieceCount(WHITE, PAWN) == 7, "Not 7 white pawns\n"
				+ game);
		asserts(game.getPieceCount(WHITE, QUEEN) == 1, "Not 1 white queens\n"
				+ game);
		asserts(game.getPieceCount(BLACK, KNIGHT) == 1, "Not 1 black knights\n"
				+ game);
	}

	public void testPieceDisappearBug() {
		Game game = createFromFen(
				"r1q1k2r/3ppppp/1pn5/p3N3/3PPB2/5Q2/PPP2PPP/R4bK1 w kq - 0 0",
				Type.CLASSIC);
		asserts(game.getPiece(SQUARE_C8) == QUEEN, "c8 wasnt a queen\n" + game);
		game.makeLanMove("a1xf1");
		asserts(game.getPiece(SQUARE_C8) == QUEEN, "c8 wasnt a queen\n" + game);
	}

	public void testPromotions() {
		for (String test : PROMOTION_TEST) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Type.CLASSIC);
			game.makeLanMove(split[1]);

			List<Move> expectedMoves = new ArrayList<Move>(split.length - 2);

			for (int i = 2; i < split.length; i++) {
				expectedMoves.add(game.makeLanMove(split[i]));
				game.rollback();
			}

			asserts(expectedMoves, game.getLegalMoves().asList(), game);
		}

		for (String test : PROMOTION_TEST) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Type.CLASSIC);
			game.makeLanMove(split[1]);

			List<Move> expectedMoves = new ArrayList<Move>(split.length - 2);

			for (int i = 2; i < split.length; i++) {
				expectedMoves.add(game.makeLanMove(split[i]));
				game.rollback();
				game.rollback();
				Game game2 = createFromFen(split[0], Type.CLASSIC);
				asserts(game.getOccupiedBB() == game2.getOccupiedBB()
						&& game.getEmptyBB() == game2.getEmptyBB(),
						"Occupied/empty are not the same.");
			}
		}
	}

	public void testRepHashing() {
		Game game = createStartingPosition(Type.CLASSIC);
		Random random = new Random();
		long positionOnlyHash = game.getZobristPositionHash();
		long gameHash = game.getZobristGameHash();

		for (int i = 0; i < 60; i++) {
			// System.out.println("Position hash: " + positionOnlyHash);
			// System.out.println("Game hash: " + gameHash);

			Move[] legals = game.getLegalMoves().asArray();
			Move move = legals[random.nextInt(legals.length)];
			game.move(move);
			game.rollback();
			asserts(game.getZobristPositionHash() == positionOnlyHash,
					"Position Hashes were not equal");
			asserts(game.getZobristGameHash() == gameHash,
					"Game Hashes were not equal");

			game.move(move);

			positionOnlyHash = game.getZobristPositionHash();
			gameHash = game.getZobristGameHash();
		}
	}

	public void testThreeXRep() {
		Game game = createStartingPosition(Type.CLASSIC);
		game.makeLanMove("g1-h3");
		asserts(game.getRepCount() == 1, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("g8-h6");
		asserts(game.getRepCount() == 1, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("h3-g1");
		asserts(game.getRepCount() == 1, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("h6-g8");
		asserts(game.getRepCount() == 2, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("g1-h3");
		asserts(game.getRepCount() == 2, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("g8-h6");
		asserts(game.getRepCount() == 2, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("h3-g1");
		asserts(game.getRepCount() == 2, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("h6-g8");
		asserts(game.getRepCount() == 3, "Invalid rep count "
				+ game.getRepCount());
		game.makeLanMove("g1-h3");
		asserts(game.getRepCount() == 3, "Invalid rep count"
				+ game.getRepCount());
	}

	public void testValidCastle() {
		for (String test : VALID_CASTLE_TEST) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Type.CLASSIC);

			boolean isCastleKingside = contains("O-O", split);
			boolean isCastleQueenside = contains("O-O-O", split);

			List<Move> moveList = game.getLegalMoves().asList();

			if (isCastleKingside) {
				assertsContains(moveList, "O-O");
				game.makeLanMove("O-O");
				game.rollback();
				asserts(
						(game.getCastling(game.getColorToMove()) & CASTLE_SHORT) != 0,
						"Rollback erased castle kingside");

				Game game2 = createFromFen(split[0], Type.CLASSIC);
				asserts(game.getOccupiedBB() == game2.getOccupiedBB()
						&& game.getEmptyBB() == game2.getEmptyBB(),
						"Occupied is not the same.");

			} else {
				assertsDoesntContains(moveList, "O-O");
			}

			if (isCastleQueenside) {
				assertsContains(moveList, "O-O-O");
				game.makeLanMove("O-O-O");
				game.rollback();
				asserts(
						(game.getCastling(game.getColorToMove()) & CASTLE_LONG) != 0,
						"Rollback erased castle queenside");
				Game game2 = createFromFen(split[0], Type.CLASSIC);
				asserts(game.getOccupiedBB() == game2.getOccupiedBB()
						&& game.getEmptyBB() == game2.getEmptyBB(),
						"Occupied is not the same.");
			} else {
				assertsDoesntContains(moveList, "O-O-O");
			}
		}
	}

}
