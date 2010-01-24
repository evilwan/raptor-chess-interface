/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import org.apache.commons.lang.math.NumberUtils;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ObserveController;

public class AddTabAlias extends RaptorAlias {
	public AddTabAlias() {
		super(
				"+tab",
				"Adds a chanel game or person tab. ",
				"'+tab [channelNumber | name | games]'"
						+ "Examples: '+tab 13' (Adds a tab for channel 13), "
						+ "'+tab johnthegreat' (Adds a person tab for johnthegreat)' "
						+ "+tab games' (Adds a tab for all games you are observing).");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWithIgnoreCase(command, "+tab")) {
			String whatsLeft = command.substring(5).trim();

			if (whatsLeft.contains(" ")) {
				return new RaptorAliasResult(null, "Invalid command: "
						+ command + "\n" + getUsage());
			} else if (whatsLeft.equals("games")) {
				RaptorWindowItem[] items = Raptor.getInstance().getWindow()
						.getWindowItems(ChessBoardWindowItem.class);

				String games = "";

				for (RaptorWindowItem item : items) {
					ChessBoardWindowItem chessBoardItem = (ChessBoardWindowItem) item;
					if (chessBoardItem.getConnector() == controller
							.getConnector()
							&& chessBoardItem.getController() instanceof ObserveController) {
						ChatUtils.openGameChatTab(
								chessBoardItem.getConnector(), chessBoardItem
										.getController().getGame().getId(),
								false);
						games += chessBoardItem.getController().getGame()
								.getId()
								+ " ";
					}
				}

				if (StringUtils.isBlank(games)) {
					return new RaptorAliasResult(null,
							"You are not observing any games.");
				} else {
					return new RaptorAliasResult(null,
							"Added game tell tab(s): " + games + ".");
				}
			} else if (NumberUtils.isDigits(whatsLeft)) {
				ChatUtils.openChannelTab(controller.getConnector(), whatsLeft,
						false);
				return new RaptorAliasResult(null, "Added channel tab: "
						+ whatsLeft + ".");
			} else {
				ChatUtils.openPersonTab(controller.getConnector(), whatsLeft,
						false);
				return new RaptorAliasResult(null, "Added person tab: "
						+ whatsLeft + ".");
			}
		} else {
			return null;
		}
	}
}
