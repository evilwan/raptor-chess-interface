package testcases;

import junit.framework.Assert;

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.WildGame;

public class TestWild {
	
	private void assertShortCastingMoves(Game game, Move[] moves) {
		Move m = null, m1 = null;
		for (Move move: moves) {
			if (move.isCastleShort()) {
				m = move;	
				game.move(m);
				Move[] moves1 = game.getPseudoLegalMoves().asArray();
				for (Move move1: moves1) {
					if (move1.isCastleShort()) {
						m1 = move1;
					}
				}
			}
		}
		Assert.assertTrue(m != null);
		Assert.assertTrue(m1 != null);
		Assert.assertTrue(m1 != null);
	}
	
	private void assertLongCastingMoves(Game game, Move[] moves) {
		Move m = null, m1 = null;
		for (Move move: moves) {
			if (move.isCastleLong()) {
				m = move;	
				game.move(m);
				Move[] moves1 = game.getPseudoLegalMoves().asArray();
				for (Move move1: moves1) {
					if (move1.isCastleLong()) {
						m1 = move1;
					}
				}
			}
		}
		Assert.assertTrue(m != null);
		Assert.assertTrue(m1 != null);
	}
	
	
	@Test
	public void testShortCastlingGeneration() {
		WildGame game = (WildGame) GameFactory.createFromFen(
				"r2kqbnr/ppp1pppp/2n5/3p4/4P1b1/5N2/PPPPBPPP/RNBQK2R w KQkq - 4 4",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		Move[] moves = game.getPseudoLegalMoves().asArray();
		
		assertShortCastingMoves(game, moves); 
		
		game = (WildGame) GameFactory.createFromFen(
				"rbnqk2r/ppp1pppp/2n5/3p4/4P1b1/5N2/PPPPBPPP/R2KQBNR w KQkq - 4 4",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		moves = game.getPseudoLegalMoves().asArray();
		
		assertShortCastingMoves(game, moves);
	}
	
	@Test
	public void testLongCastlingGeneration() {
		WildGame game = (WildGame) GameFactory.createFromFen(
				"r3k2r/ppp1pppp/2n5/3p4/4P1b1/5N2/PPPPBPPP/R2K3R w KQkq - 4 4",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		Move[] moves = game.getPseudoLegalMoves().asArray();
		
		assertLongCastingMoves(game, moves);
		
		game = (WildGame) GameFactory.createFromFen(
				"r2k3r/ppp1pppp/2n5/3p4/4P1b1/5N2/PPPPBPPP/R3K2R w KQkq - 4 4",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		moves = game.getPseudoLegalMoves().asArray();
		
		assertLongCastingMoves(game, moves); 
	}
	
	@Test
	public void testCastlingGenerationWithChecks() {
		WildGame game = (WildGame) GameFactory.createFromFen(
				"r1nqk2r/ppp2ppp/2n5/2Bp4/4Pbb1/5N2/PPP2PPP/R2KQBNR w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		Move[] moves = game.getPseudoLegalMoves().asArray();
		
		Move m = null, m1 = null;
		for (Move move: moves) {
			if (move.isCastleShort()) {
				m = move;	
				game.makeSanMove("a6");
				Move[] moves1 = game.getPseudoLegalMoves().asArray();
				for (Move move1: moves1) {
					if (move1.isCastleShort()) {
						m1 = move1;
					}
				}
			}
		}
		Assert.assertTrue(m == null);
		Assert.assertTrue(m1 == null);
	}
	
	@Test
	public void testCastlingRights() {
		WildGame game = (WildGame) GameFactory.createFromFen(
				"r2kq2r/ppp2ppp/2n5/2Bp4/4Pbb1/5N2/PPP2PPP/R2KQ2R w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		game.makeSanMove("Rb1");		
		Assert.assertFalse(game.canWhiteCastleShort());
		Assert.assertTrue(game.canWhiteCastleLong());
		
		game.makeSanMove("Rb8");		
		Assert.assertFalse(game.canBlackCastleShort());
		Assert.assertTrue(game.canBlackCastleLong());
		
		game.rollback();
		game.rollback();
		
		game.makeSanMove("Rg1");		
		Assert.assertFalse(game.canWhiteCastleLong());
		Assert.assertTrue(game.canWhiteCastleShort());
		
		game.makeSanMove("Rg8");		
		Assert.assertFalse(game.canBlackCastleLong());
		Assert.assertTrue(game.canBlackCastleShort());
	}
	
	@Test
	public void testCastlingMove() {
		WildGame game = (WildGame) GameFactory.createFromFen(
				"r2kq2r/ppp2ppp/2n5/2Bp4/4P3/5N2/PPP2PPP/R2KQ2R w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();

		game.makeSanMove("O-O");
		Assert.assertEquals(
				"r2kq2r/ppp2ppp/2n5/2Bp4/4P3/5N2/PPP2PPP/1KR1Q2R b kq - 1 1",
				game.toFen());

		game.makeSanMove("O-O");
		Assert.assertEquals(
				"1kr1q2r/ppp2ppp/2n5/2Bp4/4P3/5N2/PPP2PPP/1KR1Q2R w - - 2 2",
				game.toFen());		
		
		game = (WildGame) GameFactory.createFromFen(
				"r2qk2r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R2QK2R w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();

		game.makeSanMove("O-O");
		Assert.assertEquals(
				"r2qk2r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R2Q1RK1 b kq - 1 1",
				game.toFen());

		game.makeSanMove("O-O");
		Assert.assertEquals(
				"r2q1rk1/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R2Q1RK1 w - - 2 2",
				game.toFen());
		
		game = (WildGame) GameFactory.createFromFen(
				"r3k2r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R3K2R w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		game.makeSanMove("O-O-O");
		Assert.assertEquals(
				"r3k2r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/2KR3R b kq - 1 1",
				game.toFen());

		game.makeSanMove("O-O-O");
		Assert.assertEquals(
				"2kr3r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/2KR3R w - - 2 2",
				game.toFen());
		
		game = (WildGame) GameFactory.createFromFen(
				"r2k3r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R2K3R w KQkq - 0 1",
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();
		
		game.makeSanMove("O-O-O");
		Assert.assertEquals(
				"r2k3r/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R3RK2 b kq - 1 1",
				game.toFen());

		game.makeSanMove("O-O-O");
		Assert.assertEquals(
				"r3rk2/ppp2ppp/2n5/3p4/4P3/5N2/PPP2PPP/R3RK2 w - - 2 2",
				game.toFen());
	}
	
	@Test
	public void testRollback() {
		String fen1 = "r2kq2r/ppp2ppp/2n5/2Bp4/4P3/5N2/PPP2PPP/R2KQ2R w KQkq - 0 1";
		WildGame game = (WildGame) GameFactory.createFromFen(fen1,
				Variant.wild);
		game.addState(Game.UPDATING_SAN_STATE);
		game.initialPositionIsSet();

		game.makeSanMove("O-O");
		game.rollback();
		Assert.assertEquals(fen1, game.toFen());
	}
}
