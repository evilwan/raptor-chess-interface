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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.connector.fics.pref.FicsPage;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ConnectorQuadrants;
import raptor.service.ThreadService;
import raptor.swt.BrowserWindowItem;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.RegExDialog;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.RegExController;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class FicsConnector extends IcsConnector implements PreferenceKeys {

	protected MenuManager connectionsMenu;
	protected Action autoConnectAction;
	protected Action bughouseArenaAction;
	protected Action connectAction;
	protected Action disconnectAction;
	protected Action reconnectAction;
	protected Action regexTabAction;
	protected Action seekGraphAction;
	protected Action bugbuttonsAction;
	protected Action isShowingBugButtonsOnConnectAction;
	/**
	 * Raptor allows connecting to fics twice with different profiles. Override
	 * short name and change it to fics2 so users can distinguish the two.
	 */
	protected FicsConnector fics2 = null;

	public FicsConnector() {
		this(new IcsConnectorContext(new FicsParser()));
	}

	public FicsConnector(IcsConnectorContext context) {
		super(context);
		((FicsParser) context.getParser()).setConnector(this);
		initFics2();
		createMenuActions();

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
			bughouseArenaAction.setEnabled(true);
			seekGraphAction.setEnabled(true);
			regexTabAction.setEnabled(true);
			bugbuttonsAction.setEnabled(true);
			isShowingBugButtonsOnConnectAction.setChecked(getPreferences()
					.getBoolean(
							context.getPreferencePrefix()
									+ "show-bugbuttons-on-connect"));

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

		seekGraphAction = new Action("Show &Seek Graph") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Seek Graph Comming soon");
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
				if (!Raptor.getInstance().getWindow().containsBugButtonsItem(
						FicsConnector.this)) {
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							new BugButtonsWindowItem(FicsConnector.this));
				}
			}
		};

		isShowingBugButtonsOnConnectAction = new Action(
				"Show Bug Buttons On Connect", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				getPreferences().setValue(
						context.getPreferencePrefix()
								+ "show-bugbuttons-on-connect", isChecked());
				getPreferences().save();
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
		bughouseArenaAction.setEnabled(false);
		seekGraphAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		bugbuttonsAction.setEnabled(false);
		isShowingBugButtonsOnConnectAction.setEnabled(true);
		isShowingBugButtonsOnConnectAction.setChecked(getPreferences()
				.getBoolean(
						context.getPreferencePrefix()
								+ "show-bugbuttons-on-connect"));
		autoConnectAction.setChecked(getPreferences().getBoolean(
				context.getPreferencePrefix() + "auto-connect"));

		connectionsMenu.add(connectAction);
		connectionsMenu.add(disconnectAction);
		connectionsMenu.add(reconnectAction);
		connectionsMenu.add(autoConnectAction);
		connectionsMenu.add(isShowingBugButtonsOnConnectAction);
		connectionsMenu.add(new Separator());
		connectionsMenu.add(bugbuttonsAction);
		connectionsMenu.add(bughouseArenaAction);
		connectionsMenu.add(seekGraphAction);
		connectionsMenu.add(new Separator());
		connectionsMenu.add(regexTabAction);
		connectionsMenu.add(new Separator());

		Action adjudicateAGame = new Action("Adjudicate a game") {

			@Override
			public void run() {
				Raptor.getInstance().getWindow().addRaptorWindowItem(
						new BrowserWindowItem("Adjudicate Games", Raptor
								.getInstance().getPreferences().getString(
										PreferenceKeys.FICS_ADJUDICATE_URL)));
			}
		};
		Action ficsSite = new Action("www.freechess.org") {

			@Override
			public void run() {
				Raptor
						.getInstance()
						.getWindow()
						.addRaptorWindowItem(
								new BrowserWindowItem(
										"www.freechess.org",
										Raptor
												.getInstance()
												.getPreferences()
												.getString(
														PreferenceKeys.FICS_FREECHESS_ORG_URL)));
			}
		};
		Action ficsGamesSite = new Action("www.ficsgames.com") {

			@Override
			public void run() {
				Raptor.getInstance().getWindow().addRaptorWindowItem(
						new BrowserWindowItem("www.ficsgames.com", Raptor
								.getInstance().getPreferences().getString(
										PreferenceKeys.FICS_FICS_GAMES_URL)));
			}
		};
		Action ficsTeamLeague = new Action("Fics Team League") {

			@Override
			public void run() {
				Raptor.getInstance().getWindow().addRaptorWindowItem(
						new BrowserWindowItem("Fics Team League", Raptor
								.getInstance().getPreferences().getString(
										PreferenceKeys.FICS_TEAM_LEAGUE_URL)));
			}
		};
		connectionsMenu.add(adjudicateAGame);
		connectionsMenu.add(ficsSite);
		connectionsMenu.add(ficsGamesSite);
		connectionsMenu.add(ficsTeamLeague);
		connectionsMenu.add(new Separator());

		MenuManager fics2Menu = new MenuManager(
				"&Another Simultaneous Connection");
		fics2.connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Fics Simultaneous Login");
				dialog.open();
				if (dialog.wasLoginPressed()) {
					fics2.connect(dialog.getSelectedProfile());
				}
				autoConnectAction.setChecked(getPreferences().getBoolean(
						context.getPreferencePrefix() + "auto-connect"));
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

		fics2.seekGraphAction = new Action("Show &Seek Graph") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Seek Graph Comming soon");
			}
		};

		fics2.bughouseArenaAction = new Action("Show &Bughouse Arena") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Bughouse Areana Comming soon");
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

		fics2.bugbuttonsAction = new Action("Show Bughouse &Buttons") {
			@Override
			public void run() {
				if (!Raptor.getInstance().getWindow().containsBugButtonsItem(
						FicsConnector.this)) {
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							new BugButtonsWindowItem(FicsConnector.this));
				}
			}
		};

		fics2.isShowingBugButtonsOnConnectAction = new Action(
				"Show Bug Buttons On Connect") {
			@Override
			public void run() {
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
		fics2.bughouseArenaAction.setEnabled(false);
		fics2.seekGraphAction.setEnabled(false);
		fics2.regexTabAction.setEnabled(false);
		fics2.bugbuttonsAction.setEnabled(false);
		fics2.isShowingBugButtonsOnConnectAction.setEnabled(true);

		fics2Menu.add(fics2.connectAction);
		fics2Menu.add(fics2.disconnectAction);
		fics2Menu.add(fics2.reconnectAction);
		fics2Menu.add(new Separator());
		fics2Menu.add(fics2.bugbuttonsAction);
		fics2Menu.add(fics2.bughouseArenaAction);
		fics2Menu.add(fics2.seekGraphAction);
		fics2Menu.add(new Separator());
		fics2Menu.add(regexTabAction);
		connectionsMenu.add(fics2Menu);
	}

	@Override
	public void disconnect() {
		super.disconnect();
		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		bughouseArenaAction.setEnabled(false);
		seekGraphAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		isShowingBugButtonsOnConnectAction.setEnabled(true);
		bugbuttonsAction.setEnabled(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (fics2 != null) {
			fics2.dispose();
			fics2 = null;
		}
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
				new PreferenceNode("fics", new ConnectorQuadrants("fics")),
				new PreferenceNode("fics", new ConnectorQuadrants("fics2")) };

	}

	protected void initFics2() {
		fics2 = new FicsConnector(new IcsConnectorContext(new FicsParser()) {
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
			protected void createMenuActions() {
			}

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
			 * Override the initFics2 method to do nothing to avoid the
			 * recursion.
			 */
			@Override
			protected void initFics2() {

			}

		};
	}

	@Override
	protected void onSuccessfulLogin() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				isConnecting = false;
				fireConnected();
				sendMessage("iset defprompt 1", true);
				sendMessage("iset gameinfo 1", true);
				sendMessage("iset ms 1", true);
				sendMessage("iset allresults 1", true);
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
			}
		});
	}

}
