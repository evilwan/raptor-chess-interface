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
package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.chess.Variant;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.ToolBarItemKey;

public class MatchWinnerAction extends AbstractRaptorAction {
	public MatchWinnerAction() {
		setName("Winners");
		setDescription("Matches the winner of this game automatically when it is over.");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof ObserveController) {
				matchWinner(getChessBoardControllerSource());
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardWindowItem chessBoardWindowItem = (ChessBoardWindowItem) item;
				if (chessBoardWindowItem.getController() instanceof ObserveController) {
					matchWinner(chessBoardWindowItem.getController());
				}
			}
		}
	}

	protected void matchWinner(ChessBoardController controller) {
		if (getChessBoardControllerSource().isToolItemSelected(
				ToolBarItemKey.MATCH_WINNER)) {
			if (controller.getGame().getVariant() == Variant.bughouse
					|| controller.getGame().getVariant() == Variant.fischerRandomBughouse) {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"Winners please");
			} else {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"Winner please");
			}
		} else {
			if (controller.getGame().getVariant() == Variant.bughouse
					|| controller.getGame().getVariant() == Variant.fischerRandomBughouse) {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"No longer calling winners.");
			} else {
				getChessBoardControllerSource().getConnector().kibitz(
						getChessBoardControllerSource().getGame(),
						"No longer calling winner.");
			}
		}
	}
}