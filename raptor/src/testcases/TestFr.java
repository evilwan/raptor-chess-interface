package testcases;

import junit.framework.Assert;

import org.junit.Test;

import raptor.chess.FischerRandomGame;
import raptor.chess.GameFactory;
import raptor.chess.Variant;

public class TestFr {
	@Test
	public void testCastlingBlackQs() {
		String fen1 = "rk2bqr1/p1p1bppp/p2np1n1/8/3P4/5B2/PPP2PPP/RKN1BQR1 w KQkq - 0 8";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen1,
				Variant.fischerRandom);
		
		game.makeSanMove("Bxa8");
		Assert.assertEquals("Bk2bqr1/p1p1bppp/p2np1n1/8/3P4/8/PPP2PPP/RKN1BQR1 b KQk - 0 8", game.toFen());
	}
	
	@Test
	public void testCastlingWhiteKs() {
		String fen1 = "rq1bknnr/pbpppppp/1p6/8/5P2/1P4P1/P1PPP2P/RQBBKNNR b KQkq f3 0 3";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen1,
					Variant.fischerRandom);
		game.makeSanMove("Bxh1");
		Assert.assertEquals("rq1bknnr/p1pppppp/1p6/8/5P2/1P4P1/P1PPP2P/RQBBKNNb w Qkq - 0 4", game.toFen());
	}
	
	@Test
	public void testCastlingBlackKs() {
		String fen1 = "rq1bkn1r/p1pppp1p/1p4pn/8/5P2/1P2P1P1/PBPP3P/RQ1BKNNb w Qkq - 2 6";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen1,
					Variant.fischerRandom);
		game.makeSanMove("Bxh8");
		Assert.assertEquals("rq1bkn1B/p1pppp1p/1p4pn/8/5P2/1P2P1P1/P1PP3P/RQ1BKNNb b Qq - 0 6", game.toFen());
	}
	
	@Test
	public void testCastlingWhiteQs() {
		String fen1 = "rqkrnnbb/pppppp1p/6p1/8/8/1P4P1/P1PPPP1P/RQKRNNBB b KQkq - 0 2";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen1,
					Variant.fischerRandom);
		game.makeSanMove("Bxa1");
		Assert.assertEquals("rqkrnnb1/pppppp1p/6p1/8/8/1P4P1/P1PPPP1P/bQKRNNBB w Kkq - 0 3", game.toFen());
	}
	
	@Test
	public void testCastlingRollback() {
		String fen1 = "rqkrnnbb/pppppp1p/6p1/8/8/1P4P1/P1PPPP1P/RQKRNNBB b KQkq - 0 2";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen1,
					Variant.fischerRandom);
		game.makeSanMove("Bxa1");
		game.rollback();
		Assert.assertEquals(fen1, game.toFen());
	}
	
	@Test
	public void testOtherCastling() {
		String fen = "rkb1qrnb/ppp1p3/2np4/6pp/4Pp2/2NP3P/PPP2PPB/RK2QRNB w KQkq - 0 9";
		FischerRandomGame game = (FischerRandomGame) GameFactory.createFromFen(fen,
				Variant.fischerRandom);
		game.makeSanMove("O-O-O");
		Assert.assertEquals("rkb1qrnb/ppp1p3/2np4/6pp/4Pp2/2NP3P/PPP2PPB/2KRQRNB b kq - 1 9", game.toFen());
		game.rollback();
		Assert.assertEquals(fen, game.toFen());
		
		fen = "rk2qr1b/1pp1n3/p2p4/2nPp3/2P2pp1/3P4/PP1QNPPB/2K1RR1B b kq - 3 19";
		game = (FischerRandomGame) GameFactory.createFromFen(fen,
				Variant.fischerRandom);
		game.makeSanMove("O-O-O");
		Assert.assertEquals("2krqr1b/1pp1n3/p2p4/2nPp3/2P2pp1/3P4/PP1QNPPB/2K1RR1B w - - 4 20", game.toFen());
	}
	
}
