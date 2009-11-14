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
package raptor.connector.bics;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.RaptorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chess.Game;
import raptor.connector.bics.pref.BicsPage;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.IcsUtils;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ConnectorQuadrantsPage;
import raptor.service.ActionService;
import raptor.service.ThreadService;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.SWTUtils;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class BicsConnector extends IcsConnector implements PreferenceKeys {
	private static final Log LOG = LogFactory.getLog(BicsConnector.class);

	public static class BicsConnectorContext extends IcsConnectorContext {
		public BicsConnectorContext() {
			super(new IcsParser(true));
		}

		@Override
		public String getDescription() {
			return "Bughouse Internet Chess Server";
		}

		@Override
		public String getEnterPrompt() {
			return "\":";
		}

		@Override
		public String getLoggedInMessage() {
			return "**** Starting BICS session as ";
		}

		@Override
		public String getLoginErrorMessage() {
			return "\n*** ";
		}

		@Override
		public String getLoginPrompt() {
			return "login: ";
		}

		@Override
		public String getPasswordPrompt() {
			return "password:";
		}

		@Override
		public String getPreferencePrefix() {
			return "bics-";
		}

		@Override
		public String getPrompt() {
			return "fics%";
		}

		@Override
		public String getRawPrompt() {
			return "\n" + getPrompt() + " ";
		}

		@Override
		public int getRawPromptLength() {
			return getRawPrompt().length();
		}

		@Override
		public String getShortName() {
			return "bics";
		}
	}

	/**
	 * Raptor allows connecting to bics twice with different profiles. Override
	 * short name and change it to bics2 so users can distinguish the two.
	 * 
	 * This is also used to handle partnerships in simul bug.
	 * 
	 * If this is bics 1 it will contain the bics2 connector. If this is bics2
	 * it will be null.
	 */
	protected BicsConnector bics2 = null;

	/**
	 * This is used by the bics2 connector. You need a reference back to bics1
	 * to handle simul bug partnerships. If this is bics2 it will contain the
	 * bics1 connector, otherwise its null.
	 */
	protected BicsConnector bics1 = null;
	protected MenuManager connectionsMenu;

	protected Action autoConnectAction;
	protected Action bughouseArenaAction;
	protected Action connectAction;
	protected Action disconnectAction;
	protected Action reconnectAction;
	protected Action bugbuttonsAction;
	protected String partnerOnConnect;

	public BicsConnector() {
		this(new BicsConnectorContext());
	}

	public BicsConnector(BicsConnectorContext context) {
		super(context);
		context.getParser().setConnector(this);
		initBics2();
		createMenuActions();

	}

	@Override
	public void disconnect() {
		super.disconnect();
		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		bughouseArenaAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		bugbuttonsAction.setEnabled(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (bics2 != null) {
			bics2.dispose();
			bics2 = null;
		}
		bics1 = null;
	}

	/**
	 * Returns the menu manager for this connector.
	 */
	public MenuManager getMenuManager() {
		return connectionsMenu;
	}

	/**
	 * Return the preference node to add to the root preference dialog. This
	 * preference node will show up with the connectors first name. You can add
	 * secondary nodes by implementing getSecondaryPreferenceNodes. These nodes
	 * will show up below the root node.
	 */
	public PreferencePage getRootPreferencePage() {
		return new BicsPage();
	}

	/**
	 * Returns an array of the secondary preference nodes.
	 */
	public PreferenceNode[] getSecondaryPreferenceNodes() {
		return new PreferenceNode[] {
				new PreferenceNode("bics", new ConnectorQuadrantsPage("bics")),
				new PreferenceNode("bics", new ConnectorQuadrantsPage("bics2")),
				new PreferenceNode(
						"bicsMenuActions",
						new ActionContainerPage(
								"Bics Menu Actions",
								"\tOn this page you can configure the actions shown in the bics "
										+ "menu.You can add new actions on the Action Scripts Page.",
								RaptorActionContainer.BicsMenu)) };

	}

	/**
	 * Returns true if isConnected and a user is playing a game.
	 */
	public boolean isLoggedInUserPlayingAGame() {
		boolean result = false;
		if (isConnected()) {
			for (Game game : getGameService().getAllActiveGames()) {
				if (game.isInState(Game.PLAYING_STATE)) {
					result = true;
					break;
				}
			}
		}
		// Check the bics2 connection.
		if (!result && bics2 != null && bics2.isConnected()) {
			for (Game game : getGameService().getAllActiveGames()) {
				if (game.isInState(Game.PLAYING_STATE)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void connect(final String profileName) {
		super.connect(profileName);
		if (isConnecting) {
			connectAction.setEnabled(false);
			autoConnectAction.setChecked(getPreferences().getBoolean(
					context.getPreferencePrefix() + "auto-connect"));
			disconnectAction.setEnabled(true);
			reconnectAction.setEnabled(true);
			bughouseArenaAction.setEnabled(true);
			bugbuttonsAction.setEnabled(true);

			if (getPreferences().getBoolean(
					context.getPreferencePrefix()
							+ "show-bugbuttons-on-connect")) {
				Raptor.getInstance().getWindow().addRaptorWindowItem(
						new BugButtonsWindowItem(this));
			}
		}
	}

	/**
	 * Creates the connectionsMenu and all of the actions associated with it.
	 */
	protected void createMenuActions() {
		connectionsMenu = new MenuManager("&Bics");
		connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Bics Login");
				dialog.open();
				getPreferences().setValue(
						context.getPreferencePrefix() + "profile",
						dialog.getSelectedProfile());
				autoConnectAction.setChecked(getPreferences().getBoolean(
						context.getPreferencePrefix() + "auto-connect"));
				getPreferences().save();
				if (dialog.wasLoginPressed()) {
					connect();
				}
			}
		};

		disconnectAction = new Action("&Disconnect") {
			@Override
			public void run() {
				disconnect();
			}
		};

		reconnectAction = new Action("&Reconnect") {
			@Override
			public void run() {
				disconnect();
				// Sleep half a second for everything to adjust.
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
				}
				connect(currentProfileName);
			}
		};

		bughouseArenaAction = new Action("Show &Bughouse Arena") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Bughouse Areana Comming soon");
			}
		};

		bugbuttonsAction = new Action("Show Bughouse &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(BicsConnector.this);
			}
		};

		autoConnectAction = new Action("Auto &Login", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				getPreferences().setValue(
						context.getPreferencePrefix() + "auto-connect",
						isChecked());
				getPreferences().save();
			}
		};

		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		bughouseArenaAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		bugbuttonsAction.setEnabled(false);
		connectionsMenu.add(connectAction);
		connectionsMenu.add(disconnectAction);
		connectionsMenu.add(reconnectAction);
		connectionsMenu.add(autoConnectAction);
		connectionsMenu.add(new Separator());
		connectionsMenu.add(bugbuttonsAction);
		// connectionsMenu.add(bughouseArenaAction);
		connectionsMenu.add(new Separator());
		RaptorAction[] ficsMenuActions = ActionService.getInstance()
				.getActions(RaptorActionContainer.BicsMenu);
		for (final RaptorAction raptorAction : ficsMenuActions) {
			if (raptorAction instanceof Separator) {
				connectionsMenu.add(new Separator());
			} else {
				Action action = new Action(raptorAction.getName()) {
					@Override
					public void run() {
						raptorAction.setConnectorSource(BicsConnector.this);
						raptorAction.run();
					}
				};
				action.setToolTipText(raptorAction.getDescription());
				connectionsMenu.add(action);
			}
		}
		connectionsMenu.add(new Separator());

		MenuManager bics2Menu = new MenuManager(
				"&Another Simultaneous Connection");
		bics2.connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Bics Simultaneous Login");
				if (isConnected()) {
					dialog.setShowingSimulBug(true);
				}
				dialog.open();
				autoConnectAction.setChecked(getPreferences().getBoolean(
						context.getPreferencePrefix() + "auto-connect"));

				if (dialog.wasLoginPressed()) {
					bics2.connect(dialog.getSelectedProfile());

					if (dialog.isSimulBugLogin()) {
						// set bics2s simul bug connector.
						// This is used to automatically send the partner
						// message after login.
						// When the partnership is received a
						// PARTNERSHIP_CREATED message
						// is fired and the names of the bug partners are set in
						// that see the overridden publishEvent for how that
						// works.
						bics2.setSimulBugConnector(true);
						bics2.partnerOnConnect = userName;
						// Force bug-open to get the partnership message.
						sendMessage("set bugopen on");
					}
				}
			}
		};

		bics2.disconnectAction = new Action("&Disconnect") {
			@Override
			public void run() {
				bics2.disconnect();
			}
		};

		bics2.reconnectAction = new Action("&Reconnect") {
			@Override
			public void run() {
				bics2.disconnect();
				// Sleep half a second for everything to adjust.
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
				}
				bics2.connect(bics2.currentProfileName);
			}
		};

		bics2.bughouseArenaAction = new Action("Show &Bughouse Arena") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Bughouse Areana Comming soon");
			}
		};
		bics2.autoConnectAction = new Action("Auto &Login",
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
			}
		};

		bics2.bugbuttonsAction = new Action("Show Bughouse &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(bics2);
			}
		};

		bics2.connectAction.setEnabled(true);
		bics2.disconnectAction.setEnabled(false);
		bics2.reconnectAction.setEnabled(false);
		// bics2.bughouseArenaAction.setEnabled(false);
		bics2.bugbuttonsAction.setEnabled(false);

		bics2Menu.add(bics2.connectAction);
		bics2Menu.add(bics2.disconnectAction);
		bics2Menu.add(bics2.reconnectAction);
		bics2Menu.add(new Separator());
		bics2Menu.add(bics2.bugbuttonsAction);
		bics2Menu.add(bics2.bughouseArenaAction);
		bics2Menu.add(new Separator());

		connectionsMenu.add(bics2Menu);

	}

	protected void initBics2() {
		bics2 = new BicsConnector(new BicsConnectorContext() {
			@Override
			public String getDescription() {
				return "Bughouse Internet Chess Server Another Simultaneous Connection";
			}

			@Override
			public String getShortName() {
				return "bics2";
			}
		}) {

			/**
			 * Override not needed.
			 */
			@Override
			public PreferencePage getRootPreferencePage() {
				return null;
			}

			/**
			 * Override not needed.
			 */
			@Override
			public PreferenceNode[] getSecondaryPreferenceNodes() {
				return null;
			}

			/**
			 * Override not needed.
			 */
			@Override
			protected void createMenuActions() {
			}

			/**
			 * Override the initFics2 method to do nothing to avoid the
			 * recursion.
			 */
			@Override
			protected void initBics2() {

			}

		};
	}

	@Override
	protected void onSuccessfulLogin() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				isConnecting = false;
				fireConnected();
				sendMessage("iset gameinfo 1", true);
				sendMessage("iset ms 1", true);
				sendMessage(
						"iset premove "
								+ (getPreferences().getBoolean(
										BOARD_PREMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage(
						"iset smartmove "
								+ (getPreferences().getBoolean(
										BOARD_SMARTMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage("iset startpos 1", true);
				sendMessage("set interface "
						+ getPreferences().getString(APP_NAME));
				sendMessage("set style 12", true);
				sendMessage("set bell 0", true);

				String loginScript = getPreferences().getString(
						BICS_LOGIN_SCRIPT);
				if (StringUtils.isNotBlank(loginScript)) {
					RaptorStringTokenizer tok = new RaptorStringTokenizer(
							loginScript, "\n\r", true);
					while (tok.hasMoreTokens()) {
						try {
							Thread.sleep(50L);
						} catch (InterruptedException ie) {
						}
						sendMessage(tok.nextToken().trim());
					}
				}

				if (StringUtils.isNotBlank(partnerOnConnect)) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException ie) {
					}
					sendMessage("set bugopen on");
					sendMessage("partner " + partnerOnConnect);
					partnerOnConnect = null;
				}
			}
		});
	}

	/**
	 * This method is overridden to support simul bughouse.
	 */
	@Override
	protected void publishEvent(final ChatEvent event) {
		if (chatService != null) { // Could have been disposed.
			if (event.getType() == ChatType.PARTNERSHIP_CREATED) {
				if (bics2 != null
						&& isConnected()
						&& bics2.isConnected()
						&& StringUtils.equalsIgnoreCase(IcsUtils.stripTitles(
								event.getSource()).trim(), bics2.getUserName())) {
					// here we are in fics1 where a partnership was created.
					if (LOG.isDebugEnabled()) {
						LOG.debug("Created simul bughouse partnership with "
								+ bics2.getUserName());
					}
					isSimulBugConnector = true;
					simulBugPartnerName = event.getSource();
					bics2.isSimulBugConnector = true;
					bics2.simulBugPartnerName = getUserName();
				} else if (bics1 == null
						&& bics1 != null
						&& bics1.isConnected()
						&& isConnected()
						&& StringUtils.equalsIgnoreCase(IcsUtils.stripTitles(
								event.getSource()).trim(), bics1.getUserName())) {
					// here we are in fics2 when a partnership was created.
					if (LOG.isDebugEnabled()) {
						LOG.debug("Created simul bughouse partnership with "
								+ bics1.getUserName());
					}
					isSimulBugConnector = true;
					simulBugPartnerName = event.getSource();
					bics1.isSimulBugConnector = true;
					bics1.simulBugPartnerName = getUserName();
				}

				if (!isSimulBugConnector
						&& getPreferences()
								.getBoolean(
										PreferenceKeys.BICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP)) {
					SWTUtils.openBugButtonsWindowItem(this);
				}
			} else if (event.getType() == ChatType.PARTNERSHIP_DESTROYED) {
				isSimulBugConnector = false;
				simulBugPartnerName = null;

				if (LOG.isDebugEnabled()) {
					LOG
							.debug("Partnership destroyed. Resetting partnership information.");
				}

				// clear out the fics2 or fics1 depending on what this is.
				if (bics2 != null) {
					bics2.isSimulBugConnector = false;
					bics2.simulBugPartnerName = null;
				}
				if (bics1 != null) {
					bics1.isSimulBugConnector = false;
					bics1.simulBugPartnerName = null;
				}

				// Remove bug buttons if up displayed you have no partner.
				RaptorWindowItem[] windowItems = Raptor.getInstance()
						.getWindow().getWindowItems(BugButtonsWindowItem.class);
				for (RaptorWindowItem item : windowItems) {
					BugButtonsWindowItem bugButtonsItem = (BugButtonsWindowItem) item;
					if (bugButtonsItem.getConnector() == this) {
						Raptor.getInstance().getWindow()
								.disposeRaptorWindowItem(bugButtonsItem);
					}
				}
			}
			super.publishEvent(event);
		}
	}
}
