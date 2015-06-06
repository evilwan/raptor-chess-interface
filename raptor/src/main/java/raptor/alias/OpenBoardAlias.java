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
package raptor.alias;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.controller.InactiveController;
import raptor.util.RaptorLogger;
import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

public class OpenBoardAlias extends RaptorAlias {
	private static final RaptorLogger LOG = RaptorLogger.getLog(OpenBoardAlias.class);

	public OpenBoardAlias() {
		super(
				"openboard",
				"Brings up an a classic chess inactive board from the starting position. Optionally you can add a variant.",
				"openboard [variant] [FEN].\nVariants supported: "
						+ WordUtils.wrap(RaptorStringUtils.toDelimitedString(
								Variant.values(), ", "), 70)
						+ ".\n Examples: \"openboard\"\n \"openboard suicide\"\n "
						+ "\"openboard rnbbkrqn/pppppppp/8/8/8/8/PPPPPPPP/RNBBKRQN w KQkq - 0 1\"\n "
						+ "\"openboard suicide rnbbkrqn/pppppppp/8/8/8/8/PPPPPPPP/RNBBKRQN w KQkq - 0 1\"\b");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWithIgnoreCase(command, "openboard")) {
			String whatsLeft = command.substring(9).trim();

			String variant = Variant.classic.toString();
			String fen = null;

			if (!StringUtils.isBlank(whatsLeft)) {
				RaptorStringTokenizer tok = new RaptorStringTokenizer(
						whatsLeft, " ", true);
				variant = tok.nextToken();
				try {
					if (Variant.valueOf(variant) == null) {
						fen = whatsLeft;
						variant = Variant.classic.toString();
					}
					if (tok.hasMoreTokens()) {
						fen = tok.getWhatsLeft();
					}
				} catch (Throwable t) {
					fen = whatsLeft;
					variant = Variant.classic.toString();
				}
			}

			try {
				Game game = null;
				if (fen == null) {
					game = GameFactory.createStartingPosition(Variant
							.valueOf(variant));
				} else {
					game = GameFactory.createFromFen(fen, Variant
							.valueOf(variant));
				}
				game.addState(Game.UNTIMED_STATE);
				game.addState(Game.UPDATING_ECO_HEADERS_STATE);
				game.addState(Game.UPDATING_SAN_STATE);
				ChessBoardUtils.openBoard(new InactiveController(game,
						"openboard " + variant + " Position", false));
				return new RaptorAliasResult(null, "Position created.");
			} catch (Throwable t) {
				LOG.info("Error parsing openboard:", t);
				return new RaptorAliasResult(null, "Invalid command: "
						+ getUsage());
			}
		}
		return null;
	}
}