package testcases;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.util.GameUtils;

public class TestCrazyhouse implements GameConstants {

	@Test
	public void testDropToAvoidCheck() {
		Game game = GameUtils.createFromFen(
				"rn2k2Q/ppp4p/5p2/4p3/1bnpP2n/8/PPPP1PPP/R2QK1R b Qq - 0 37",
				Game.Type.CRAZYHOUSE);
		game.addState(Game.UPDATING_SAN_STATE);
		System.err.println(game.getDropCountsString());
		game.makeSanMove("B@f8");

	}

	@Test
	public void testInitial() {
		Game game = GameUtils.createStartingPosition(Game.Type.CRAZYHOUSE);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("e4");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("d5");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("exd");
		game.rollback();
		game.makeSanMove("exd");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("h6");
		System.err.println("P@h6");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.makeSanMove("P@h3");
		System.err.println("After p@h3");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		System.out.println(game);
	}

	@Test
	public void testPromotionMask() {
		String[] moves = { "d4", "e6", "Nc3", "d5", "e4", "dxe4", "Nxe4",
				"Nc6", "Nf3", "P@g4", "Ne5", "Qxd4", "Qxd4", "Nxd4", "Bd3",
				"Q@a5+", "Q@c3", "Qxe5", "P@f4", "N@f3+", "gxf3", "Nxf3+",
				"Kd1", "Qxc3", "bxc3", "P@g2", "Re1", "Nxe1", "Q@g3", "Nxd3",
				"cxd3", "B@f3+", "N@e2", "R@f1+", "N@e1" };

		Game game = GameUtils.createStartingPosition(Game.Type.CRAZYHOUSE);
		game.addState(Game.UPDATING_SAN_STATE);
		for (int i = 0; i < moves.length; i++) {
			game.makeSanMove(moves[i]);
		}
		game.makeSanMove("g1=Q");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("Qxg1");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("Rxg1");
		System.err.println(game.getDropCountsString());
		game.makeSanMove("P@h4");
		System.err.println(game.getDropCountsString());

	}

	@Test
	public void testPromotionMask2() {
		String[] moves = { "d4", "e6", "Nf3", "d5", "Bf4", "Nf6", "e3", "Be7",
				"Bd3", "O-O", "O-O", "Ne4", "Nbd2", "f5", "Ne5", "Nc6", "f3",
				"Nxe5", "Bxe5", "Nxd2", "Qxd2", "N@g6", "N@f4", "Nxe5", "dxe5",
				"B@h6", "Rae1", "Bc5", "Kh1", "N@h4", "N@h5", "Qg5", "N@h3",
				"Qe7", "Bxf5", "exf5", "Qxd5+", "B@e6", "Nxe6", "Bxe6", "Qd2",
				"Bcxe3", "Rxe3", "N@c4", "P@f6", "Nxd2", "fxe7", "Q@g6",
				"exf8=Q+", "Rxf8", "R@g3", "Bxe3", "Rxg6", "hxg6", "P@h7+",
				"Kxh7", "B@f2", "P@e2", "Bxe3", };

		Game game = GameUtils.createStartingPosition(Game.Type.CRAZYHOUSE);
		game.addState(Game.UPDATING_SAN_STATE);
		for (int i = 0; i < moves.length; i++) {
			game.makeSanMove(moves[i]);
		}
		// System.err.println(game);
		System.err.println("Before exf1=Q+");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.makeSanMove("exf1=Q+");
		assertTrue(
				"F1 did not contain a promoted queen.",
				(game.getPieceWithPromoteMask(SQUARE_F1) & PROMOTED_MASK) != 0
						&& (game.getPieceWithPromoteMask(SQUARE_F1) & NOT_PROMOTED_MASK) == QUEEN);
		System.err.println("After exf1=Q+");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.rollback();
		System.err.println("After rollback exf1=Q+");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.makeSanMove("exf1=Q+");
		System.err.println("After exf1=Q+");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		// game.toString();
		Move[] pseudoLegals = game.getPseudoLegalMoves().asArray();
		for (Move move : pseudoLegals) {
			if (game.move(move)) {
				System.err.println("CHECKPOINT After game move " + move);
				System.err.println(game.getDropCountsString());
				System.err.println(game.getPieceCountsString());

				game.rollback();
				System.err.println("CHECKPOINT After move rollback " + move);
				System.err.println(game.getDropCountsString());
				System.err.println(game.getPieceCountsString());

			}
		}
		// System.err.println("After game dump");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());

		game.makeSanMove("B@g1");
		System.err.println("After B@g1");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.makeSanMove("Qxg1+");
		System.err.println("After Qxg1+");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		game.makeSanMove("Kxg1");
		System.err.println("After Kxg1");
		System.err.println(game.getDropCountsString());
		System.err.println(game.getPieceCountsString());
		System.err.println(game);
		// "B@g1", "Qxg1+", "Kxg1", "B@h6", "Ng5+", "Bxg5",
		// "Q@e1", "Ndxf3+", "Kf2", "N@e4+", "Kf1", "R@h1+", "Ke2", "Nxe1",

	}

	// 6
	// 05:23:46,297 ERROR LenientPgnParserListener:507 - Invalid move
	// encountered
	// java.lang.IllegalArgumentException: Illegal move P@g6+
	// emptyBB occupiedBB notColorToMoveBB color[WHITE] color[BLACK]
	// 1 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 1 0 0
	// 0 0 0 1 1 1 1 0 1 1 1 0 0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 0 0
	// 0 0 1
	// 1 1 1 1 0 0 1 1 0 0 0 0 1 1 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 1 0 0
	// 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0
	// 1 1 1
	// 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 1
	// 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0
	// 0 0 0
	// 0 0 0 1 0 1 0 0 1 1 1 0 1 0 1 1 0 0 0 1 0 1 0 0 1 1 1 0 1 0 1 1 0 0 0 0 0
	// 0 0 0
	// 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 1
	//
	// [WHITE][PAWN] [WHITE][KNIGHT] [WHITE][BISHOP] [WHITE][ROOK]
	// [WHITE][QUEEN] [WHITE][KING]
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 1 1 1 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 1 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	//
	// [BLACK][PAWN] [BLACK][KNIGHT] [BLACK][BISHOP] [BLACK][ROOK]
	// [BLACK][QUEEN] [BLACK][KING]
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	//
	// |*|*|*|*|*|r|*|*| To Move: White Last Move: gxh5
	// |p|p|p|*|*|*|*|k| Piece counts [5 0 1 0 1 1][6 3 2 2 0 1]
	// |*|*|*|*|b|p|*|*| Moves: 78 EP: - Castle: -
	// |*|*|*|*|P|p|b|p| FEN: 5r2/ppp4k/4bp2/4Ppbp/4n2n/4B3/PPP1K1PP/4n2r w - -
	// 0 78
	// |*|*|*|*|n|*|*|n| State: 32 Type=CRAZYHOUSE Result= emptyBB occupiedBB
	// notColorToMoveBB color[WHITE] color[BLACK]
	// 1 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 1 0 0
	// 0 0 0 1 1 1 1 0 1 1 1 0 0 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 0 0
	// 0 0 1
	// 1 1 1 1 0 0 1 1 0 0 0 0 1 1 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 1 0 0
	// 1 1 1 1 0 0 0 0 0 0 0 0 1 1 1 1 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0
	// 1 1 1
	// 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 1
	// 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 1 1 1 1 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0
	// 0 0 0
	// 0 0 0 1 0 1 0 0 1 1 1 0 1 0 1 1 0 0 0 1 0 1 0 0 1 1 1 0 1 0 1 1 0 0 0 0 0
	// 0 0 0
	// 1 1 1 1 0 1 1 0 0 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 1
	//
	// [WHITE][PAWN] [WHITE][KNIGHT] [WHITE][BISHOP] [WHITE][ROOK]
	// [WHITE][QUEEN] [WHITE][KING]
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 1 1 1 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 1 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	//
	// [BLACK][PAWN] [BLACK][KNIGHT] [BLACK][BISHOP] [BLACK][ROOK]
	// [BLACK][QUEEN] [BLACK][KING]
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 1
	// 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 1 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0
	// 0 0 0 0 0 0 0 0 0 0 0
	//
	// |*|*|*|*|*|r|*|*| To Move: White Last Move: gxh5
	// |p|p|p|*|*|*|*|k| Piece counts [5 0 1 0 1 1][6 3 2 2 0 1]
	// |*|*|*|*|b|p|*|*| Moves: 78 EP: - Castle: -
	// |*|*|*|*|P|p|b|p| FEN: 5r2/ppp4k/4bp2/4Ppbp/4n2n/4B3/PPP1K1PP/4n2r w - -
	// 0 78
	// |*|*|*|*|n|*|*|n|
	// |*|*|*|*|B|*|*|*| Event: FICS rated crazyhouse game Site=FICS
	// Time=2009.10.07
	// |P|P|P|*|K|*|P|P| WhiteName: Drukkarg BlackName=Hummeress WhiteTime=0
	// whiteLag=0 blackRemainingTImeMillis = 0 blackLag=0
	// |*|*|*|*|n|*|*|r| initialWhiteTimeMillis: 0 initialBlackTimeMillis=0
	// initialWhiteIncMillis=0 initialBlackIncMillis=0
	//
	// Legals=[Bc1, Bg1, Bd2, Bf2, Bd4, Bf4, Bc5, Bb6, a3, a4, b3, b4, c3, c4,
	// g3, g4,
	// h3, Kd1, B@a1, B@b1, B@c1, B@d1, B@f1, B@g1, B@d2, B@f2, B@a3, B@b3,
	// B@c3, B@d3,
	// B@f3, B@g3, B@h3, B@a4, B@b4, B@c4, B@d4, B@f4, B@g4, B@a5, B@b5, B@c5,
	// B@d5,
	// B@a6, B@b6, B@c6, B@d6, B@g6, B@h6, B@d7, B@e7, B@f7, B@g7, B@a8, B@b8,
	// B@c8,
	// B@d8, B@e8, B@g8, B@h8, Q@a1, Q@b1, Q@c1, Q@d1, Q@f1, Q@g1, Q@d2, Q@f2,
	// Q@a3,
	// Q@b3, Q@c3, Q@d3, Q@f3, Q@g3, Q@h3, Q@a4, Q@b4, Q@c4, Q@d4, Q@f4, Q@g4,
	// Q@a5,
	// Q@b5, Q@c5, Q@d5, Q@a6, Q@b6, Q@c6, Q@d6, Q@g6, Q@h6, Q@d7, Q@e7, Q@f7,
	// Q@g7,
	// Q@a8, Q@b8, Q@c8, Q@d8, Q@e8, Q@g8, Q@h8, Bxg5, Bxa7, exf]Movelist=[d4,
	// e6, Nf3, d5, Bf4, Nf6, e3, Be7, Bd3, O-O, O-O, Ne4, Nbd2, f5, Ne5,
	// Nc6, f3, Nxe5, Bxe5, Nxd2, Qxd2, N@g6, N@f4, Nxe5, dxe5, B@h6, Rae1, Bc5,
	// Kh1,
	// N@h4, N@h5, Qg5, N@h3, Qe7, Bxf5, exf5, Qxd5+, B@e6, Nxe6, Bxe6, Qd2,
	// Bcxe3,
	// Rxe3, N@c4, P@f6, Nxd2, fxe7, Q@g6, exf8=Q+, Rxf8, R@g3, Bxe3, Rxg6,
	// hxg6,
	// P@h7+, Kxh7, B@f2, P@e2, Bxe3, exf1=Q+, B@g1, Qxg1+, Kxg1, B@h6, Ng5+,
	// Bxg5,
	// Q@e1, Ndxf3+, Kf2, N@e4+, Kf1, R@h1+, Ke2, Nxe1, Nf6+, gxf6, Q@h5+, gxh5]
	// Drop counts [WP=0 WN=0 WB=1 WR=0 WQ=1 WK=0][BP=3 BN= 1 BB=0 BR=2 BQ=2
	// BK=0]
	// at raptor.game.Game.makeSanMove(Game.java:1933)
	// at
	// raptor.game.pgn.LenientPgnParserListener.makeGameMoveFromWord(LenientPgnParserListener.java:151)
	// at
	// raptor.game.pgn.LenientPgnParserListener.onMoveWord(LenientPgnParserListener.java:505)
	// at
	// raptor.game.pgn.AbstractPgnParser.fireMoveWord(AbstractPgnParser.java:55)
	// at raptor.game.pgn.SimplePgnParser.parse(SimplePgnParser.java:275)
	// at testcases.TestPgnParsing.testCrazyhosueFile(TestPgnParsing.java:150)
	// at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	// at
	// sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	// at
	// sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	// at java.lang.reflect.Method.invoke(Method.java:585)
	// at
	// org.junit.internal.runners.TestMethodRunner.executeMethodBody(TestMethodRunner.java:99)
	// at
	// org.junit.internal.runners.TestMethodRunner.runUnprotected(TestMethodRunner.java:81)
	// at
	// org.junit.internal.runners.BeforeAndAfterRunner.runProtected(BeforeAndAfterRunner.java:34)
	// at
	// org.junit.internal.runners.TestMethodRunner.runMethod(TestMethodRunner.java:75)
	// at
	// org.junit.internal.runners.TestMethodRunner.run(TestMethodRunner.java:45)
	// at
	// org.junit.internal.runners.TestClassMethodsRunner.invokeTestMethod(TestClassMethodsRunner.java:66)
	// at
	// org.junit.internal.runners.TestClassMethodsRunner.run(TestClassMethodsRunner.java:35)
	// at
	// org.junit.internal.runners.TestClassRunner$1.runUnprotected(TestClassRunner.java:42)
	// at
	// org.junit.internal.runners.BeforeAndAfterRunner.runProtected(BeforeAndAfterRunner.java:34)
	// at
	// org.junit.internal.runners.TestClassRunner.run(TestClassRunner.java:52)
	// at
	// org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:45)
	// at
	// org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
	// at
	// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:460)
	// at
	// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:673)
	// at
	// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:386)
	// at
	// org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:196)
}
