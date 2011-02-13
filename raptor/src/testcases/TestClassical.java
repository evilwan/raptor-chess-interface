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
package testcases;

import static org.junit.Assert.assertTrue;
import static raptor.chess.GameFactory.createFromFen;
import static raptor.chess.GameFactory.createStartingPosition;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;

public class TestClassical implements GameConstants {

	private static final String[][] AMBIG_FEN_MOVES = {
			{ "Kg8", "Kh7", "Kg7", "Nd2", "Nef2", "Nc3", "Neg3", "Nd6", "Nf6",
					"Nc5", "Ng5", "Nhg3", "Nhf2" },
			{ "Kg8", "Kh7", "Kg7", "Nf7", "Ng6", "Ng4", "N5f3", "N5d3", "Nc4",
					"Nc6", "Nd7", "Ng2", "N1f3", "N1d3", "Nc2" },
			{ "Kg8", "Kh7", "Kg7", "Nf7", "Ng6", "Ng4", "N5f3", "N5d3", "Nc4",
					"Nc6", "Nd7", "Ng2", "Ne1f3", "N1d3", "Nc2", "Nh3", "Ngf3",
					"Ne2" },
			{ "Rxg7", "Rf6", "Re6", "Rd6", "Rc6", "Rb6", "Ra6", "Rh6", "Rg5",
					"Rg4", "Rg3", "Rg2", "Rgg1", "f6", "b4", "Bg4", "Bg2",
					"Bf1", "d4", "d3", "e3", "Ra2", "Ra3", "Na3", "Nc3", "Bb2",
					"Ba3", "Qc2", "Kf1", "Kf2", "O-O", "Rh2", "Rhg1", "Rf1" } };

	private static final String[] AMBIG_FEN_TESTS = {
			"k6K/8/8/8/4N3/8/8/7N w - - 3 4",
			"k6K/8/8/4N3/8/8/8/4N3 w - - 3 4",
			"k6K/8/8/4N3/8/8/8/4N1N1 w - - 3 4",
			"r1b1q2r/1p1pbkp1/24R1/p1p1pP1p/P3P2P/1P5B/3PP3/RNBQK2R w KQ - 2 17" };

	private static final String[][] AMBIG_P_CAPTURE_FEN_LEGAL_MOVESS = { {
			"Ka7", "Kc7", "Kb6", "d5", "Bg7", "Bf8", "cxb4", "cxb5", "c4",
			"Be6", "Bd7", "Bc8", "Bg6", "Bh7", "Be4", "Bd3", "Bc2", "Bb1",
			"Bg4", "Bh3", "Rg6", "Rg7", "Rg8", "Rh5", "Rg4", "Rg3", "Rg2",
			"Rg1", "Na1", "Nc1", "Nd2", "Na5", "Nd4" } };

	private static final String[] AMBIG_P_CAPTURE_FEN_TESTS = { "4R3/1k6/2pp3b/1Pp2br1/1P3P1B/Nn2PN2/P6K/8 b - - 2 55" };

	private static final String[][] CHECK_FEN_LEGAL_MOVES = { { "Kb1", "Kb2" },
			{ "Kb1", "Kb2", "Qa2" } };

	private static final String[] CHECK_FEN_TESTS = {
			"rk6/8/8/8/8/8/8/K5NN w - - 0 50",
			"r1k5/8/8/8/8/8/7Q/K5NN w - - 0 50" };

	private static final String[] CHECKMATE_FEN_TESTS = {
			"k6R/8/K7/8/8/8/8/8 b - - 0 50", "3k4/3Q4/3K4/8/8/8/8/8 b - - 0 50" };

	private static boolean DEBUG = true;

	private static final String[][] EP_FEN_LEAGALS = {
			{ "Ka2", "Kb1", "Kb2", "g6", "gxh6" },
			{ "Ka2", "Kb1", "Kb2", "g6" },
			{ "Ka7", "Kb8", "Kb8", "h3", "hxg3" } };

	private static final String[] EP_FEN_TESTS = {
			"k7/8/8/6Pp/8/8/8/K7 w - h6 0 50",
			"k7/8/8/6Pp/8/8/8/K7 w - - 0 50", "k7/8/8/8/6Pp/8/8/K7 b - g3 0 50" };

	private static final String[] IN_CHECK_TESTS = new String[] {};// "8/8/6Kk/8/8/8/8 b - - 0 805",
	// "8/8/6Kk/8/8/8/8 w - - 0 805" };

	private static final String[] NOT_CHECKMATE_TESTS = new String[] { "k7/2K5/8/8/8/8/8/7R b - - 0 0" };

	private static final String[] PROMOTION_TEST = { "k7/7P/2K5/8/8/8/8/8 w - - 0 0|h7-h8=R|a8-a7" };

	private static final String[] PSEUDO_LEGAL_MOVE_TESTS = new String[] {
			"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0|a2-a3|a2-a4|b2-b3|b2-b4|c2-c3|c2-c4|d2-d3|d2-d4|e2-e3|e2-e4|f2-f3|f2-f4|g2-g3|g2-g4|h2-h3|h2-h4|b1-a3|b1-c3|g1-f3|g1-h3",
			"rnbqkbnr/pppppppp/8/8/3P5/8/PPP8PPPP/RNBQKBNR b KQkq - 0 0|a7-a6|a7-a5|b7-b6|b7-b5|c7-c6|c7-c5|d7-d6|d7-d5|e7-e6|e7-e5|f7-f6|f7-f5|g7-g6|g7-g5|h7-h6|h7-h5|b8-a6|b8-c6|g8-f6|g8-h6" };

	private static final String[] STALEMATE_FEN_TESTS = {
			"k7/7R/K7/1R/8/8/8/8 b - - 0 50",
			"3k4/3P4/3K4/8/8/8/8/8 b - - 0 50" };

	private static final String[] VALID_CASTLE_TEST = {
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

	public void assertOnlyLegals(String[] moves, List<Move> legalMoves) {
		assertTrue("Lengths didnt match " + moves.length + " "
				+ legalMoves.size(), moves.length == legalMoves.size());
		for (String move : moves) {
			boolean found = false;
			for (int j = 0; !found && j < legalMoves.size(); j++) {
				found = legalMoves.get(j).getSan().equals(move);
			}
			assertTrue("Couldnt find " + move + " in legal moves.", found);
		}
	}

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

	@Test
	public void testAmbigPawnCaptures() throws Exception {
		for (int i = 0; i < AMBIG_P_CAPTURE_FEN_TESTS.length; i++) {
			Game game = createFromFen(AMBIG_P_CAPTURE_FEN_TESTS[i],
					Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			dumpGame("Test Ambig Pawn Captures: ", game);
			assertOnlyLegals(AMBIG_P_CAPTURE_FEN_LEGAL_MOVESS[i], game
					.getLegalMoves().asList());

			// Test short alg by making all of the moves in short algebraic.
			for (int j = 0; j < AMBIG_P_CAPTURE_FEN_LEGAL_MOVESS[i].length; j++) {
				game.makeSanMove(AMBIG_P_CAPTURE_FEN_LEGAL_MOVESS[i][j]);
				game.rollback();
			}
		}
	}

	@Test
	public void testAmbiguousMoves() throws Exception {

		for (int i = 0; i < AMBIG_FEN_TESTS.length; i++) {
			Game game = createFromFen(AMBIG_FEN_TESTS[i], Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);

			dumpGame("Ambiguous move position: ", game);
			assertOnlyLegals(AMBIG_FEN_MOVES[i], game.getLegalMoves().asList());
		}
	}

	@Test
	public void testBasicRepHashing() {
		Game game = createStartingPosition(Variant.classic);

		long positionOnlyHash = game.getZobristPositionHash();
		long gameHash = game.getZobristGameHash();

		// System.out.println("Position hash: " + positionOnlyHash);
		// System.out.println("Game hash: " + gameHash);

		Move g1f3 = new Move(SQUARE_G1, SQUARE_F3, KNIGHT, WHITE, EMPTY);
		game.move(g1f3);
		game.rollback();

		asserts(game.getZobristPositionHash() == positionOnlyHash,
				"Position Hashes were not equal");
		asserts(game.getZobristGameHash() == gameHash,
				"Game Hashes were not equal");
	}

	@Test
	public void testCastling() throws Exception {
		Game game = createFromFen(
				"r2q1rk1/1p2bppp/1p1p2b1/4P3/1P2N3/5N2/1P4PP/R2QKB1R b KQ - 0 16",
				Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("Rxa1");
		assertTrue("Didnt change castle qside flag.", !game
				.canWhiteCastleLong());
		assertTrue("White can still castle kside.", game.canWhiteCastleShort());
	}

	@Test
	public void testCastlingPart2() {
		Game game = createStartingPosition(Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("e4");
		game.makeSanMove("g6");
		game.makeSanMove("d4");
		game.makeSanMove("Bg7");
		game.makeSanMove("Nf3");
		game.makeSanMove("c5");
		game.makeSanMove("Be3");
		game.makeSanMove("Qb6");
		game.makeSanMove("dxc5");
		game.makeSanMove("Qxb2");
		game.makeSanMove("Bd4");
		game.makeSanMove("Bxd4");
		game.makeSanMove("Qxd4");
		game.makeSanMove("Qc1");
		game.makeSanMove("Qd1");
		game.makeSanMove("Qxd1");
		game.makeSanMove("Kxd1");
		game.makeSanMove("Nf6");
		game.makeSanMove("Bd3");
		game.makeSanMove("Nc6");
		game.makeSanMove("c3");
		game.makeSanMove("O-O");
	}

	@Test
	public void testCheckMate() throws Exception {
		for (String element : CHECKMATE_FEN_TESTS) {
			Game game = createFromFen(element, Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			assertTrue("Position was not checkmate: "
					+ game.getLegalMoves().asArray() + "\n" + game, game
					.isCheckmate());
		}
	}

	@Test
	public void testCheckPart2() throws Exception {
		Game game = createFromFen("8/3k3p/4p3/4npK1/2PN4/1P6/7P/8 w - f6 0 42",
				Variant.classic);
		assertTrue("This position is not in check.\n" + game, !game.isInCheck());
	}

	@Test
	public void testChecks() throws Exception {
		for (int i = 0; i < CHECK_FEN_TESTS.length; i++) {
			Game game = createFromFen(CHECK_FEN_TESTS[i], Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			assertOnlyLegals(CHECK_FEN_LEGAL_MOVES[i], game.getLegalMoves()
					.asList());
		}
	}

	@Test
	public void testDisambiguityFromCheck() throws Exception {
		String fen = "5r2/3qp1kp/1p1p1rp1/p1pP4/P4P2/2Q5/1P4PP/4RR1K b  - - 1 49";
		Game game = createFromFen(fen, Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("Rf7");
	}

	@Test
	public void testEP() throws Exception {
		for (int i = 0; i < EP_FEN_TESTS.length; i++) {
			Game game = createFromFen(EP_FEN_TESTS[i], Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			dumpGame("Test EP: ", game);
			assertOnlyLegals(EP_FEN_LEAGALS[i], game.getLegalMoves().asList());

			// Test short alg by making all of the moves in short algebraic.
			for (int j = 0; j < EP_FEN_LEAGALS[i].length; j++) {
				game.makeSanMove(EP_FEN_LEAGALS[i][j]);
				game.rollback();
			}
		}
	}

	@Test
	public void testEP2() throws Exception {
		Game game = createFromFen(
				"rnbqkbnr/1pp1pppp/p7/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2",
				Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("exd6");
		assertTrue("Didnt clear EP square", game.getPiece(SQUARE_D5) == 0);

		game = createFromFen(
				"rn5r/pb2k3/2p5/P3Pppp/1p1pPNPP/bB1P4/2PK4/RNn1Q2R w - f6 0 32",
				Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		dumpGame("Before move exf6", game);
		game.makeSanMove("exf6");

		dumpGame("", game);

		game.rollback();
		boolean containsexf5 = false;
		boolean containsexf = false;
		List<Move> legalMoves = game.getLegalMoves().asList();
		for (Move move : legalMoves) {
			System.out.println(move.getSan());
			if (move.getSan().equals("exf5")) {
				containsexf5 = true;
			}
			if (move.getSan().equals("exf")) {
				containsexf = true;
			}
		}

		assertTrue("Position didnt contain exf5", containsexf5);
		assertTrue("Position contained exf", !containsexf);
	}

	@Test
	public void testEP3() throws Exception {
		Game game = createStartingPosition(Variant.bughouse);
		game.addState(Game.UPDATING_SAN_STATE);

		game.makeSanMove("e4");
		game.getLegalMoves();
		game.makeSanMove("Nf6");
		game.getLegalMoves();
		game.makeSanMove("e5");
		game.getLegalMoves();
		game.makeSanMove("Ne4");
		game.getLegalMoves();
		game.makeSanMove("Nc3");
		game.getLegalMoves();
		game.makeSanMove("d5");
		game.getLegalMoves();
		game.rollback();
		game.makeSanMove("d5");
		game.makeSanMove("exd6");
		game.rollback();
		game.setDropCount(WHITE, PAWN, 1);
		game.setDropCount(WHITE, KNIGHT, 1);
		game.setDropCount(BLACK, PAWN, 1);
		Arrays.toString(game.getLegalMoves().asArray());
		game.makeSanMove("exd6");

	}

	@Test
	public void testFromShortAlgebraic() throws Exception {
		Game game = createStartingPosition(Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		dumpGame("Initial Position", game);
		assertOnlyLegals(new String[] { "a3", "a4", "b3", "b4", "c3", "c4",
				"d3", "d4", "e3", "e4", "f3", "f4", "g3", "g4", "h3", "h4",
				"Na3", "Nc3", "Nh3", "Nf3" }, game.getLegalMoves().asList());

		List<Move> initialLegals = game.getLegalMoves().asList();
		for (int i = 0; i < initialLegals.size(); i++) {
			game.makeSanMove(initialLegals.get(i).getSan());
			dumpGame("Position after " + initialLegals.get(i).getSan(), game);
			assertOnlyLegals(new String[] { "a6", "a5", "b6", "b5", "c6", "c5",
					"d6", "d5", "e6", "e5", "f6", "f5", "g6", "g5", "h6", "h5",
					"Na6", "Nc6", "Nh6", "Nf6" }, game.getLegalMoves().asList());
			game.rollback();
		}
	}

	@Test
	public void testInCheck() {
		for (String fen : IN_CHECK_TESTS) {
			Game game = createFromFen(fen, Variant.classic);
			asserts(game.isInCheck(game.getColorToMove()),
					"User is in check in position:\n " + game);
		}
	}

	@Test
	public void testInitialPosition() throws Exception {
		Game game = createStartingPosition(Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		dumpGame("Initial Position", game);
		assertOnlyLegals(new String[] { "a3", "a4", "b3", "b4", "c3", "c4",
				"d3", "d4", "e3", "e4", "f3", "f4", "g3", "g4", "h3", "h4",
				"Na3", "Nc3", "Nh3", "Nf3" }, game.getLegalMoves().asList());

		List<Move> initialLegals = game.getLegalMoves().asList();
		for (int i = 0; i < initialLegals.size(); i++) {
			game.move(initialLegals.get(i));
			dumpGame("Position after " + initialLegals.get(i).getSan(), game);
			assertOnlyLegals(new String[] { "a6", "a5", "b6", "b5", "c6", "c5",
					"d6", "d5", "e6", "e5", "f6", "f5", "g6", "g5", "h6", "h5",
					"Na6", "Nc6", "Nh6", "Nf6" }, game.getLegalMoves().asList());
			game.rollback();
		}
	}

	@Test
	public void testLegals() {
		for (String test : PSEUDO_LEGAL_MOVE_TESTS) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Variant.classic);

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

	@Test
	public void testNotCheckmate() {
		for (String fen : NOT_CHECKMATE_TESTS) {
			Game game = createFromFen(fen, Variant.classic);
			asserts(!game.isCheckmate(), "Position is not checkmate: \n" + game);
		}
	}

	@Test
	public void testOccupiedEmpty() {
		Game game = createStartingPosition(Variant.classic);
		game.makeLanMove("e2-e4");
		game.makeLanMove("h7-h6");
		game.makeLanMove("d2-d4");
		game.makeLanMove("g7-g5");
		game.makeLanMove("f2-f4");
		game.makeLanMove("g5-f4");
		game.makeLanMove("c1-f4");
		game.makeLanMove("c7-c6");
		game.makeLanMove("f4-h6");
		game.makeLanMove("h8-h6");
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

		Game game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

	}

	@Test
	public void testPawnCaptures() {
		Game game = createStartingPosition(Variant.classic);
		game.makeLanMove("e2-e4");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e4-d5");
		game.rollback();
		game.rollback();
		game.rollback();

		Game game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Variant.classic);
		game.makeLanMove("e2-e4");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e4-e5");
		game.makeLanMove("f7-f5");
		game.makeLanMove("e5-f6");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Variant.classic);
		game.makeLanMove("e2-e4");
		game.makeLanMove("h7-h6");
		game.makeLanMove("e4-e5");
		game.makeLanMove("d7-d5");
		game.makeLanMove("e5-d6");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Variant.classic);
		game.makeLanMove("h2-h3");
		game.makeLanMove("d7-d5");
		game.makeLanMove("h3-h4");
		game.makeLanMove("d5-d4");
		game.makeLanMove("e2-e4");
		game.makeLanMove("d4-e3");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");

		game = createStartingPosition(Variant.classic);
		game.makeLanMove("h2-h3");
		game.makeLanMove("d7-d5");
		game.makeLanMove("h3-h4");
		game.makeLanMove("d5-d4");
		game.makeLanMove("c2-c4");
		game.makeLanMove("d4-c3");
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game.rollback();
		game2 = createStartingPosition(Variant.classic);
		asserts(game.getOccupiedBB() == game2.getOccupiedBB()
				&& game.getEmptyBB() == game2.getEmptyBB(),
				"Occupied is not the same.");
	}

	@Test
	public void testPieceDisappearBug() {
		Game game = createFromFen(
				"r1q1k2r/3ppppp/1pn5/p3N3/3PPB2/5Q2/PPP2PPP/R4bK1 w kq - 0 0",
				Variant.classic);
		asserts(game.getPiece(SQUARE_C8) == QUEEN, "c8 wasnt a queen\n" + game);
		game.makeLanMove("a1-f1");
		asserts(game.getPiece(SQUARE_C8) == QUEEN, "c8 wasnt a queen\n" + game);
	}

	@Test
	public void testPromotions() {
		for (String test : PROMOTION_TEST) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Variant.classic);
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
			Game game = createFromFen(split[0], Variant.classic);
			game.makeLanMove(split[1]);

			List<Move> expectedMoves = new ArrayList<Move>(split.length - 2);

			for (int i = 2; i < split.length; i++) {
				expectedMoves.add(game.makeLanMove(split[i]));
				game.rollback();
				game.rollback();
				Game game2 = createFromFen(split[0], Variant.classic);
				asserts(game.getOccupiedBB() == game2.getOccupiedBB()
						&& game.getEmptyBB() == game2.getEmptyBB(),
						"Occupied/empty are not the same.");
			}
		}
	}

	@Test
	public void testRepHashing() {
		Game game = createStartingPosition(Variant.classic);
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

	// @Test
	public void testSpeed() throws Exception {
		int numPositions = 200;
		int runs = 10;

		Random random = new SecureRandom();

		int nodes = 0;

		for (int i = 0; i < runs; i++) {
			Game game = createStartingPosition(Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);

			for (int j = 0; j < numPositions; j++) {
				List<Move> legals = game.getPseudoLegalMoves().asList();

				if (!legals.isEmpty()) {
					try {
						Move move = legals.get(random.nextInt(legals.size()));
						game.makeSanMove(move.getSan());
						nodes++;
					} catch (IllegalArgumentException ime) {
						// ime.printStackTrace();
					}
				} else {
					dumpGame("Game is mover. isStalemate: "
							+ game.isStalemate() + " " + " isCheckmate: "
							+ game.isCheckmate() + " Last move: "
							+ game.getLastMove().getSan(), game);
					break;
				}
			}
		}
		System.out.println(nodes);
	}

	@Test
	public void testStaleMate() throws Exception {
		for (String element : STALEMATE_FEN_TESTS) {
			Game game = createFromFen(element, Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			assertTrue("Position was not stalemate: "
					+ getMoves(game.getLegalMoves().asList()) + " "
					+ game.isInCheck() + "\n" + game, game.isStalemate());
		}
	}

	@Test
	public void testThreeXRep() {
		Game game = createStartingPosition(Variant.classic);
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

	// @Test
	public void testTreeWalk() throws Exception {

		int numMoves = 100;
		int runs = 50;

		Random random = new SecureRandom();

		for (int i = 0; i < runs; i++) {
			Game game = createStartingPosition(Variant.classic);
			game.addState(Game.UPDATING_SAN_STATE);
			long runStart = System.currentTimeMillis();

			for (int j = 0; j < numMoves; j++) {
				List<Move> legals = game.getLegalMoves().asList();

				if (!legals.isEmpty()) {
					Move move = legals.get(random.nextInt(legals.size()));

					if (DEBUG) {
						System.out.println("Attempting to make move: "
								+ move.getSan() + " start: "
								+ GameUtils.getSan(move.getFrom()) + " "
								+ GameUtils.getSan(move.getTo()));
					}
					game.move(move);
				} else {
					dumpGame("Game is mover. isStalemate: "
							+ game.isStalemate() + " " + " isCheckmate: "
							+ game.isCheckmate() + " Last move: "
							+ game.getLastMove().toString(), game);
					break;
				}
			}
			if (DEBUG) {
				System.out.println("Run finished: "
						+ (System.currentTimeMillis() - runStart));
			}
		}
	}

	@Test
	public void testValidCastle() {
		for (String test : VALID_CASTLE_TEST) {
			String[] split = test.split("\\|");
			Game game = createFromFen(split[0], Variant.classic);

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

				Game game2 = createFromFen(split[0], Variant.classic);
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
				Game game2 = createFromFen(split[0], Variant.classic);
				asserts(game.getOccupiedBB() == game2.getOccupiedBB()
						&& game.getEmptyBB() == game2.getEmptyBB(),
						"Occupied is not the same.");
			} else {
				assertsDoesntContains(moveList, "O-O-O");
			}
		}
	}

	@Test
	public void testWild5PieceCounts() throws Exception {
		Game game = createFromFen(
				"RNBKQBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbkqbnr w - - 0 1",
				Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.addState(Game.UPDATING_ECO_HEADERS_STATE);
		game.makeSanMove("Nf6");
		game.makeSanMove("Nf3");
		game.makeSanMove("NC6");
		game.makeSanMove("NC3");
		game.makeSanMove("b8=Q");
		game.makeSanMove("b1=Q");
		game.makeSanMove("g8=Q");
		game.makeSanMove("g1=Q");
		game.toString();
		assertTrue("Black Queen Count != 3\n" + game, game.getPieceCount(BLACK,
				QUEEN) == 3);
		assertTrue("Black Pawn Count != 6\n" + game, game.getPieceCount(BLACK,
				PAWN) == 6);
		assertTrue("White Queen Count != 3\n" + game, game.getPieceCount(WHITE,
				QUEEN) == 3);
		assertTrue("White Pawn Count != 6\n" + game, game.getPieceCount(WHITE,
				PAWN) == 6);

		game.makeSanMove("Qxb1");
		game.makeSanMove("axb1=Q");
		assertTrue("Black Queen Count != 3\n" + game, game.getPieceCount(BLACK,
				QUEEN) == 3);
		assertTrue("Black Pawnn Count != 5\n" + game, game.getPieceCount(BLACK,
				PAWN) == 5);
	}

	@Test
	public void testWild5PieceJailCounts() throws Exception {
		Game game = createFromFen(
				"RNBKQBNR/PPPPPPPP/8/8/8/8/pppppppp/rnbkqbnr w - - 0 1",
				Variant.classic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.addState(Game.UPDATING_ECO_HEADERS_STATE);
		game.makeSanMove("Nf6");
		game.makeSanMove("Nf3");
		game.makeSanMove("NC6");
		game.makeSanMove("NC3");
		game.makeSanMove("b8=Q");
		game.makeSanMove("b1=Q");
		game.makeSanMove("g8=Q");
		game.makeSanMove("g1=Q");
		game.toString();
		game.makeSanMove("Qxb1");
		game.toString();
		game.makeSanMove("axb1=Q");
		game.toString();
		game.rollback();
		game.makeSanMove("axb1=Q");

		int[] blackPieceJail = game.getPieceJailCounts(BLACK);
		int[] whitePieceJail = game.getPieceJailCounts(WHITE);

		assertTrue("Black pawns captured != 1\n" + game,
				blackPieceJail[PAWN] == 1);
		assertTrue("Black queens captured != 0\n" + game,
				blackPieceJail[QUEEN] == 0);
		assertTrue("Black knights captured != 0\n" + game,
				blackPieceJail[KNIGHT] == 0);
		assertTrue("Black bishops captured != 0\n" + game,
				blackPieceJail[BISHOP] == 0);
		assertTrue("Black rooks captured != 0\n" + game,
				blackPieceJail[ROOK] == 0);
		assertTrue("Black kings captured != 0\n" + game,
				blackPieceJail[KING] == 0);

		assertTrue("White pawns captured != 1\n" + game,
				whitePieceJail[PAWN] == 1);
		assertTrue("White queens captured != 0\n" + game,
				whitePieceJail[QUEEN] == 0);
		assertTrue("White knights captured != 0\n" + game,
				blackPieceJail[KNIGHT] == 0);
		assertTrue("White bishops captured != 0\n" + game,
				blackPieceJail[BISHOP] == 0);
		assertTrue("White rooks captured != 0\n" + game,
				blackPieceJail[ROOK] == 0);
		assertTrue("White kings captured != 0\n" + game,
				blackPieceJail[KING] == 0);

	}

	private void dumpGame(String message, Game position) {
		if (DEBUG) {
			System.out.println(message);
			System.out.println(position);
		}
	}

	private String getMoves(List<Move> moves) {
		String result = "";
		for (Move move : moves) {
			result += "'" + move.getSan() + "',";
		}
		return result;

	}
}
