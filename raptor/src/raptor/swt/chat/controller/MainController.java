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
package raptor.swt.chat.controller;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.connector.Connector;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.service.GameService.Offer;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chat.ChatUtils;
import raptor.util.RaptorRunnable;

public class MainController extends ChatConsoleController {
	protected GameServiceListener listener = new GameServiceAdapter() {
		@Override
		public void offerIssued(Offer offer) {
			updateOffersPending();
		}

		@Override
		public void offerReceived(Offer offer) {
			updateOffersPending();
		}

		@Override
		public void offerRemoved(Offer offer) {
			updateOffersPending();
		}
	};

	public MainController(Connector connector) {
		super(connector);
		connector.getGameService().addGameServiceListener(listener);
	}

	@Override
	public boolean confirmClose() {
		boolean result = true;
		if (connector.isConnected()) {
			result = Raptor.getInstance().confirm(
					"Closing a main console will disconnect you from "
							+ connector.getShortName()
							+ ". Do you wish to proceed?");

			if (result) {
				connector.disconnect();
			}
		}
		return result;
	}

	@Override
	public void dispose() {
		connector.setSpeakingAllPersonTells(false);
		connector.getGameService().removeGameServiceListener(listener);
		super.dispose();
	}

	@Override
	public String getName() {
		return "Main";
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				getConnector().getShortName() + "-" + MAIN_TAB_QUADRANT);
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = SWTUtils.createToolbar(parent);
			ChatUtils.addActionsToToolbar(this,
					RaptorActionContainer.MainChatConsole, toolbar);
			adjustAwayButtonEnabled();
		} else {
			toolbar.setParent(parent);
		}
		return toolbar;
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return true;
	}

	@Override
	public boolean isAwayable() {
		return true;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return false;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}

	public void updateOffersPending() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				Offer[] offers = getConnector().getGameService().getOffers();
				ToolItem item = getToolItem(ToolBarItemKey.PendingChallenges);

				if (offers.length == 0) {
					if (item != null) {
						item.setImage(Raptor.getInstance().getIcon(
								"dimLightbulb"));
					}
				} else {
					if (item != null) {
						item.setImage(Raptor.getInstance().getIcon(
								"litLightbulb"));
					}
				}
			}
		});
	}
}
