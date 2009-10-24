package testcases;

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;

public class TestAtomic {
	@Test
	public void testE5() {
		Game game = GameFactory.createStartingPosition(Variant.atomic);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("e3");
		game.makeSanMove("e6");
		game.makeSanMove("Nh3");
		game.makeSanMove("h6");
		game.makeSanMove("Nc3");
		game.makeSanMove("Bb4");
		game.makeSanMove("Nf4");
		game.makeSanMove("d5");
		game.makeSanMove("Ng6");
		game.makeSanMove("fxg6");
		game.makeSanMove("Qh5+");
		game.makeSanMove("g6");
		game.makeSanMove("Qe5");
		game.makeSanMove("Qh4");
		game.makeSanMove("g3");
		game.makeSanMove("Qf4");
		game.makeSanMove("f3");
		game.makeSanMove("Qxe5");
		game.makeSanMove("Bb5+");
		game.makeSanMove("c6");
		game.makeSanMove("Bf1");
		game.makeSanMove("e5");
		game.makeSanMove("Bh3");
	}

}
