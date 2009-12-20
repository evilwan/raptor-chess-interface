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
package raptor.swt.chat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.swt.ItemChangedListener;
import raptor.swt.chat.controller.BughousePartnerController;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.GameChatController;
import raptor.swt.chat.controller.MainController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chat.controller.RegExController;
import raptor.util.RaptorRunnable;

public class ChatConsoleWindowItem implements RaptorConnectorWindowItem {
	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };

	public static final int TEXT_BLOCK = 5000;
	ChatConsole console;
	ChatConsoleController controller;
	boolean isPassive = true;
	Quadrant quadrant;

	public ChatConsoleWindowItem(ChatConsoleController controller) {
		this.controller = controller;
	}

	public ChatConsoleWindowItem(ChatConsoleController controller,
			Quadrant quadrant) {
		this.controller = controller;
		this.quadrant = quadrant;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		controller.addItemChangedListener(listener);
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		if (controller instanceof MainController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.MAIN_TAB_QUADRANT, newQuadrant);
		} else if (controller instanceof PersonController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.PERSON_TAB_QUADRANT, newQuadrant);
		} else if (controller instanceof BughousePartnerController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
					newQuadrant);
		} else if (controller instanceof ChannelController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.PERSON_TAB_QUADRANT, newQuadrant);
		} else if (controller instanceof RegExController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.REGEX_TAB_QUADRANT, newQuadrant);
		} else if (controller instanceof GameChatController) {
			Raptor.getInstance().getPreferences().setValue(
					getConnector().getShortName() + "-"
							+ PreferenceKeys.GAME_CHAT_TAB_QUADRANT,
					newQuadrant);
		}
	}

	public boolean confirmClose() {
		return controller.confirmClose();
	}

	public void dispose() {
		if (console != null) {
			console.dispose();
			console = null;
		}
		if (controller != null) {
			controller.dispose();
			controller = null;
		}
	}

	public Connector getConnector() {
		return getController().getConnector();
	}

	public ChatConsole getConsole() {
		return console;
	}

	public Composite getControl() {
		return console;
	}

	public ChatConsoleController getController() {
		return controller;
	}

	public Image getImage() {
		return controller != null ? controller.getIconImage() : null;
	}

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public Quadrant getPreferredQuadrant() {
		return quadrant != null ? quadrant : controller != null ? controller
				.getPreferredQuadrant() : Quadrant.III;
	}

	public String getTitle() {
		return controller != null ? controller.getTitle() : "ERROR";
	}

	public Control getToolbar(Composite parent) {
		return controller.getToolbar(parent);
	}

	public void init(Composite parent) {
		console = new ChatConsole(parent, SWT.NONE);
		console.setLayoutDeferred(true);
		console.setController(controller);
		controller.setChatConsole(console);
		console.createControls();
		controller.init();
	}

	public boolean isCloseable() {
		return controller != null ? controller.isCloseable() : true;
	}

	public void onActivate() {
		if (isPassive) {
			console.setLayoutDeferred(false);
			if (console != null && !console.isDisposed() && controller != null) {
				console.getDisplay().syncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								controller.onForceAutoScroll();
								controller.chatConsole.outputText.forceFocus();
							}
						});
			}
			console.getController().onActivate();
			isPassive = false;
		}
		else {
			console.getController().onActivate();
		}
	}

	public void onPassivate() {
		if (!isPassive) {
			console.setLayoutDeferred(true);
			console.getController().onPassivate();
			isPassive = true;
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		if (controller != null) {
			controller.removeItemChangedListener(listener);
		}
	}
}
