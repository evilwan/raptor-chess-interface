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
package testcases;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.GameFactory;
import raptor.chess.Move;
import raptor.chess.Variant;

public class TestCrazyhouse implements GameConstants {

	@Test
	public void testDropToAvoidCheck() {
		Game game = GameFactory.createFromFen(
				"rn2k2Q/ppp4p/5p2/4p3/1bnpP2n/8/PPPP1PPP/R2QK1R b Qq - 0 37",
				Variant.crazyhouse);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("B@f8");

	}

	@Test
	public void testInitial() {
		Game game = GameFactory.createStartingPosition(Variant.crazyhouse);
		game.addState(Game.UPDATING_SAN_STATE);
		game.makeSanMove("e4");
		game.makeSanMove("d5");
		game.makeSanMove("exd");
		game.rollback();
		game.makeSanMove("exd");
		game.makeSanMove("h6");
		game.makeSanMove("P@h3");
	}

	@Test
	public void testPromotionMask() {
		String[] moves = { "d4", "e6", "Nc3", "d5", "e4", "dxe4", "Nxe4",
				"Nc6", "Nf3", "P@g4", "Ne5", "Qxd4", "Qxd4", "Nxd4", "Bd3",
				"Q@a5+", "Q@c3", "Qxe5", "P@f4", "N@f3+", "gxf3", "Nxf3+",
				"Kd1", "Qxc3", "bxc3", "P@g2", "Re1", "Nxe1", "Q@g3", "Nxd3",
				"cxd3", "B@f3+", "N@e2", "R@f1+", "N@e1" };

		Game game = GameFactory.createStartingPosition(Variant.crazyhouse);
		game.addState(Game.UPDATING_SAN_STATE);
		for (int i = 0; i < moves.length; i++) {
			game.makeSanMove(moves[i]);
		}
		game.makeSanMove("g1=Q");
		game.makeSanMove("Qxg1");
		game.makeSanMove("Rxg1");
		game.makeSanMove("P@h4");
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

		Game game = GameFactory.createStartingPosition(Variant.crazyhouse);
		game.addState(Game.UPDATING_SAN_STATE);
		for (int i = 0; i < moves.length; i++) {
			game.makeSanMove(moves[i]);
		}
		game.makeSanMove("exf1=Q+");
		assertTrue(
				"F1 did not contain a promoted queen.",
				(game.getPieceWithPromoteMask(SQUARE_F1) & PROMOTED_MASK) != 0
						&& (game.getPieceWithPromoteMask(SQUARE_F1) & NOT_PROMOTED_MASK) == QUEEN);
		game.rollback();
		game.makeSanMove("exf1=Q+");
		Move[] pseudoLegals = game.getPseudoLegalMoves().asArray();
		for (Move move : pseudoLegals) {
			if (game.move(move)) {
				game.rollback();
			}
		}
		game.makeSanMove("B@g1");
		game.makeSanMove("Qxg1+");
		game.makeSanMove("Kxg1");
	}

	@Test
	public void testTanBug() {
		String[] moves = { "Nf3", "d5", "e3", "c5", "c3", "Nc6", "Be2", "Bf5",
				"d3", "Nf6", "O-O", "e6", "Bd2", "Bd6", "Na3", "a6", "c4",
				"O-O", "b4", "Nxb4", "Bxb4", "cxb4", "Nc2", "a5", "Nfd4",
				"Bg6", "Nb5", "Be5", "d4", "Bc7", "c5", "Rc8", "Ne1", "Qd7",
				"a4", "Rfe8", "Nf3", "Ne4", "Bd3", "f5", "Bxe4", "fxe4",
				"Nxc7", "exf3", "Nxe8", "fxg2", "Re1", "Rxe8", "Qg4", "Qf7",
				"f4", "Be4", "Rac1", "Rc8", "Re2", "b6", "c6", "b5", "axb5" };

		Game game = GameFactory.createStartingPosition(Variant.crazyhouse);
		game.addState(Game.UPDATING_SAN_STATE);
		for (int i = 0; i < moves.length; i++) {
			game.getLegalMoves();
			game.makeSanMove(moves[i]);
		}
		// System.err.println("AFTER d5 \n" +
		// GameUtils.getString(game.getOccupiedBB()));
		// game.getLegalMoves();
		// System.err.println("AFTER getlegals \n" +
		// GameUtils.getString(game.getOccupiedBB()));
		// //System.err.println(game);
		// game.makeSanMove("fxe4");
		// System.err.println("AFTER fxe4 \n" +
		// GameUtils.getString(game.getOccupiedBB()));
		// System.err.println(game);

		// game.makeSanMove("fxe4");

		// game.getLegalMoves();
		// System.err.println(game);
	}
}
