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

import org.junit.Test;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnUtils;

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
	
	@Test
	public void testEPRollback() {
		Game game = GameFactory.createStartingPosition(Variant.atomic);
		//game.addState(Game.UPDATING_SAN_STATE);
		game.setId("1");
		game.addState(Game.UPDATING_SAN_STATE);
		game.addState(Game.UPDATING_ECO_HEADERS_STATE);
		game.setHeader(PgnHeader.Date,
				PgnUtils.longToPgnDate(System.currentTimeMillis()));
		game.setHeader(PgnHeader.Round, "?");
		game.setHeader(PgnHeader.Site, "freechess.org");
		game.setHeader(PgnHeader.TimeControl, PgnUtils
				.timeIncMillisToTimeControl(5,
						0));
		game.setHeader(PgnHeader.BlackRemainingMillis, ""
				+ 131);
		game.setHeader(PgnHeader.WhiteRemainingMillis, ""
				+ 131);
		game.setHeader(PgnHeader.WhiteClock,
				PgnUtils.timeToClock(5));
		game.setHeader(PgnHeader.BlackClock,
				PgnUtils.timeToClock(5));
		game.setHeader(PgnHeader.BlackElo, "1343");
		game.setHeader(PgnHeader.WhiteElo, "1445");
		game.setHeader(PgnHeader.Event, 5 / 60000
				+ " " + 0 / 1000 + " "
				+  "unrated" + " "
				+   "atmoic");
		game.makeSanMove("a4");
		game.getLegalMoves();
		game.makeSanMove("a6");
		game.getLegalMoves();
		game.makeSanMove("a5");
		game.getLegalMoves();
		game.makeSanMove("b5");
		game.getLegalMoves();
		game.makeSanMove("axb6");
		game.getLegalMoves();
		game.makeSanMove("Nc6");
		game.getLegalMoves();
		game.makeSanMove("b4");
		game.getLegalMoves();
		game.makeSanMove("a5");
		game.getLegalMoves();
		game.makeSanMove("b5");
		game.getLegalMoves();
	}

}
