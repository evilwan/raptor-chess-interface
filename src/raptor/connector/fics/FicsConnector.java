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
package raptor.connector.fics;

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
import raptor.connector.fics.pref.FicsPage;
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
import raptor.swt.RegExDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.RegExController;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class FicsConnector extends IcsConnector implements PreferenceKeys {
	private static final Log LOG = LogFactory.getLog(FicsConnector.class);

	protected MenuManager connectionsMenu;
	protected Action autoConnectAction;
	protected Action bughouseArenaAvailPartnersAction;
	protected Action bughouseArenaPartnershipsAction;
	protected Action bughouseArenaGamesAction;
	protected Action connectAction;
	protected Action disconnectAction;
	protected Action reconnectAction;
	protected Action regexTabAction;
	protected Action seekTableAction;
	protected Action bugbuttonsAction;

	/**
	 * Raptor allows connecting to fics twice with different profiles. Override
	 * short name and change it to fics2 so users can distinguish the two.
	 * 
	 * This is also used to handle partnerships in simul bug.
	 * 
	 * If this is fics 1 it will contain the fics2 connector. If this is fics2
	 * it will be null.
	 */
	protected FicsConnector fics2 = null;

	/**
	 * This is used by the fics2 connector. You need a reference back to fics1
	 * to handle simul bug partnerships. If this is fics2 it will contain the
	 * fics1 connector, otherwise its null.
	 */
	protected FicsConnector fics1 = null;
	protected String partnerOnConnect;

	public FicsConnector() {
		this(new IcsConnectorContext(new IcsParser(false)));
	}

	public FicsConnector(IcsConnectorContext context) {
		super(context);
		context.getParser().setConnector(this);
		initFics2();
		createMenuActions();

	}

	@Override
	public void disconnect() {
		super.disconnect();
		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		bughouseArenaAvailPartnersAction.setEnabled(false);
		bughouseArenaPartnershipsAction.setEnabled(false);
		bughouseArenaGamesAction.setEnabled(false);
		seekTableAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		bugbuttonsAction.setEnabled(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (fics2 != null) {
			fics2.dispose();
			fics2 = null;
		}
		// stop circular refs for easier gc.
		fics1 = null;
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
		return new FicsPage();
	}

	/**
	 * Returns an array of the secondary preference nodes.
	 */
	public PreferenceNode[] getSecondaryPreferenceNodes() {
		return new PreferenceNode[] {
				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics")),
				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics2")),
				new PreferenceNode(
						"ficsMenuActions",
						new ActionContainerPage(
								"Fics Menu Actions",
								"\tOn this page you can configure the actions shown in the fics "
										+ "menu.You can add new actions on the Action Scripts Page.",
								RaptorActionContainer.FicsMenu)) };

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
		// Check the fics2 connection.
		if (!result && fics2 != null && fics2.isConnected()) {
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
			autoConnectAction.setEnabled(true);
			disconnectAction.setEnabled(true);
			reconnectAction.setEnabled(true);
			bughouseArenaAvailPartnersAction.setEnabled(true);
			bughouseArenaPartnershipsAction.setEnabled(true);
			bughouseArenaGamesAction.setEnabled(true);
			seekTableAction.setEnabled(true);
			regexTabAction.setEnabled(true);
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
		connectionsMenu = new MenuManager("&Fics");
		connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Fics Login");
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

		seekTableAction = new Action("Show &Seek Table") {
			@Override
			public void run() {
				SWTUtils.openSeekTableWindowItem(FicsConnector.this);
			}
		};

		bughouseArenaAvailPartnersAction = new Action(
				"Show Bughouse Available &Partners") {
			@Override
			public void run() {
				SWTUtils.openBugPartnersWindowItem(FicsConnector.this);
			}
		};

		bughouseArenaPartnershipsAction = new Action(
				"Show Bughouse Available &Teams") {
			@Override
			public void run() {
				SWTUtils.openBugTeamsWindowItem(FicsConnector.this);
			}
		};

		bughouseArenaGamesAction = new Action("Show Bughouse &Games") {
			@Override
			public void run() {
				SWTUtils.openBugGamesWindowItem(FicsConnector.this);
			}
		};

		bugbuttonsAction = new Action("Show Bughouse &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(FicsConnector.this);
			}
		};

		regexTabAction = new Action("&Add Regular Expression Tab") {
			@Override
			public void run() {
				RegExDialog regExDialog = new RegExDialog(Raptor.getInstance()
						.getWindow().getShell(), getShortName()
						+ " Regular Expression Dialog",
						"Enter the regular expression below:");
				String regEx = regExDialog.open();
				if (StringUtils.isNotBlank(regEx)) {
					final RegExController controller = new RegExController(
							FicsConnector.this, regEx);
					ChatConsoleWindowItem chatConsoleWindowItem = new ChatConsoleWindowItem(
							controller);
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							chatConsoleWindowItem, false);
					ChatUtils
							.appendPreviousChatsToController((ChatConsole) chatConsoleWindowItem
									.getControl());
				}
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
		autoConnectAction.setEnabled(true);
		bughouseArenaPartnershipsAction.setEnabled(false);
		bughouseArenaAvailPartnersAction.setEnabled(false);
		bughouseArenaGamesAction.setEnabled(false);
		seekTableAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		bugbuttonsAction.setEnabled(false);

		autoConnectAction.setChecked(getPreferences().getBoolean(
				context.getPreferencePrefix() + "auto-connect"));

		connectionsMenu.add(connectAction);
		connectionsMenu.add(disconnectAction);
		connectionsMenu.add(reconnectAction);
		connectionsMenu.add(autoConnectAction);

		connectionsMenu.add(new Separator());
		connectionsMenu.add(seekTableAction);
		connectionsMenu.add(bugbuttonsAction);
		connectionsMenu.add(bughouseArenaAvailPartnersAction);
		connectionsMenu.add(bughouseArenaPartnershipsAction);
		connectionsMenu.add(bughouseArenaGamesAction);
		connectionsMenu.add(new Separator());
		connectionsMenu.add(regexTabAction);
		connectionsMenu.add(new Separator());

		RaptorAction[] ficsMenuActions = ActionService.getInstance()
				.getActions(RaptorActionContainer.FicsMenu);
		for (final RaptorAction raptorAction : ficsMenuActions) {
			if (raptorAction instanceof Separator) {
				connectionsMenu.add(new Separator());
			} else {
				Action action = new Action(raptorAction.getName()) {
					@Override
					public void run() {
						raptorAction.setConnectorSource(FicsConnector.this);
						raptorAction.run();
					}
				};
				action.setToolTipText(raptorAction.getDescription());
				connectionsMenu.add(action);
			}
		}

		connectionsMenu.add(new Separator());

		MenuManager fics2Menu = new MenuManager(
				"&Another Simultaneous Connection");
		fics2.connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Fics Simultaneous Login");
				if (isConnected()) {
					dialog.setShowingSimulBug(true);
				}
				dialog.open();
				autoConnectAction.setChecked(getPreferences().getBoolean(
						context.getPreferencePrefix() + "auto-connect"));
				if (dialog.wasLoginPressed()) {
					fics2.connect(dialog.getSelectedProfile());

					if (dialog.isSimulBugLogin()) {
						// This is used to automatically send the partner
						// message after login.
						// When the partnership is received a
						// PARTNERSHIP_CREATED message
						// is fired and the names of the bug partners are set in
						// that see the overridden publishEvent for how that
						// works.
						fics2.partnerOnConnect = userName;
						// Force bug-open to get the partnership message.
						sendMessage("set bugopen on");
					}
				}
			}
		};

		fics2.disconnectAction = new Action("&Disconnect") {
			@Override
			public void run() {
				fics2.disconnect();
			}
		};

		fics2.reconnectAction = new Action("&Reconnect") {
			@Override
			public void run() {
				fics2.disconnect();
				// Sleep half a second for everything to adjust.
				try {
					Thread.sleep(500);
				} catch (InterruptedException ie) {
				}
				fics2.connect(fics2.currentProfileName);
			}
		};

		fics2.seekTableAction = new Action("Show &Seek Table") {
			@Override
			public void run() {
				SWTUtils.openSeekTableWindowItem(fics2);
			}
		};

		fics2.bughouseArenaAvailPartnersAction = new Action(
				"Show Bughouse Available &Partners") {
			@Override
			public void run() {
				SWTUtils.openBugPartnersWindowItem(fics2);
			}
		};

		fics2.bughouseArenaPartnershipsAction = new Action(
				"Show Bughouse Available &Teams") {
			@Override
			public void run() {
				SWTUtils.openBugTeamsWindowItem(fics2);
			}
		};

		fics2.bughouseArenaGamesAction = new Action("Show Bughouse &Games") {
			@Override
			public void run() {
				SWTUtils.openBugGamesWindowItem(fics2);
			}
		};

		fics2.regexTabAction = new Action("&Add Regular Expression Tab") {
			@Override
			public void run() {
				RegExDialog regExDialog = new RegExDialog(Raptor.getInstance()
						.getWindow().getShell(), getShortName()
						+ " Regular Expression Dialog",
						"Enter the regular expression below:");
				String regEx = regExDialog.open();
				if (StringUtils.isNotBlank(regEx)) {
					final RegExController controller = new RegExController(
							fics2, regEx);
					ChatConsoleWindowItem chatConsoleWindowItem = new ChatConsoleWindowItem(
							controller);
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							chatConsoleWindowItem, false);
					ChatUtils
							.appendPreviousChatsToController((ChatConsole) chatConsoleWindowItem
									.getControl());
				}
			}
		};

		fics2.bugbuttonsAction = new Action("Show Bughouse &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(fics2);
			}
		};

		fics2.autoConnectAction = new Action("Auto &Login",
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
			}
		};

		fics2.connectAction.setEnabled(true);
		fics2.disconnectAction.setEnabled(false);
		fics2.reconnectAction.setEnabled(false);
		fics2.seekTableAction.setEnabled(false);
		fics2.bughouseArenaPartnershipsAction.setEnabled(false);
		fics2.bughouseArenaAvailPartnersAction.setEnabled(false);
		fics2.bughouseArenaGamesAction.setEnabled(false);
		fics2.regexTabAction.setEnabled(false);
		fics2.bugbuttonsAction.setEnabled(false);

		fics2Menu.add(fics2.connectAction);
		fics2Menu.add(fics2.disconnectAction);
		fics2Menu.add(fics2.reconnectAction);
		fics2Menu.add(new Separator());
		fics2Menu.add(fics2.seekTableAction);
		fics2Menu.add(fics2.bugbuttonsAction);
		fics2Menu.add(fics2.bughouseArenaAvailPartnersAction);
		fics2Menu.add(fics2.bughouseArenaPartnershipsAction);
		fics2Menu.add(fics2.bughouseArenaGamesAction);
		fics2Menu.add(new Separator());
		fics2Menu.add(fics2.regexTabAction);
		connectionsMenu.add(fics2Menu);
	}

	protected void initFics2() {
		fics2 = new FicsConnector(
				new IcsConnectorContext(new IcsParser(false)) {
					@Override
					public String getDescription() {
						return "Free Internet Chess Server Another Simultaneous Connection";
					}

					@Override
					public String getShortName() {
						return "fics2";
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
			protected void initFics2() {

			}

		};
		fics2.fics1 = this;
	}

	@Override
	protected void onSuccessfulLogin() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				isConnecting = false;
				fireConnected();
				hasVetoPower = false;
				sendMessage("iset defprompt 1", true);
				sendMessage("iset gameinfo 1", true);
				sendMessage("iset ms 1", true);
				sendMessage("iset allresults 1", true);
				sendMessage("iset startpos 1", true);
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
				sendMessage("set interface "
						+ getPreferences().getString(APP_NAME));
				sendMessage("set style 12", true);
				sendMessage("set bell 0", true);
				sendMessage("set ptime 0", true);
				sendMessage("set unobserve 3", true);

				String loginScript = getPreferences().getString(
						FICS_LOGIN_SCRIPT);
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

				sendMessage("iset lock 1", true);
				hasVetoPower = true;
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
				if (fics2 != null
						&& isConnected()
						&& fics2.isConnected()
						&& StringUtils.equalsIgnoreCase(IcsUtils.stripTitles(
								event.getSource()).trim(), fics2.getUserName())) {
					// here we are in fics1 where a partnership was created.
					if (LOG.isDebugEnabled()) {
						LOG.debug("Created simul bughouse partnership with "
								+ fics2.getUserName());
					}
					isSimulBugConnector = true;
					simulBugPartnerName = event.getSource();
					fics2.isSimulBugConnector = true;
					fics2.simulBugPartnerName = getUserName();
				} else if (fics2 == null
						&& fics1 != null
						&& fics1.isConnected()
						&& isConnected()
						&& StringUtils.equalsIgnoreCase(IcsUtils.stripTitles(
								event.getSource()).trim(), fics1.getUserName())) {
					// here we are in fics2 when a partnership was created.
					if (LOG.isDebugEnabled()) {
						LOG.debug("Created simul bughouse partnership with "
								+ fics1.getUserName());
					}
					isSimulBugConnector = true;
					simulBugPartnerName = event.getSource();
					fics1.isSimulBugConnector = true;
					fics1.simulBugPartnerName = getUserName();
				}

				if (!isSimulBugConnector
						&& getPreferences()
								.getBoolean(
										PreferenceKeys.FICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP)) {
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
				if (fics2 != null) {
					fics2.isSimulBugConnector = false;
					fics2.simulBugPartnerName = null;
				}
				if (fics1 != null) {
					fics1.isSimulBugConnector = false;
					fics1.simulBugPartnerName = null;
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
