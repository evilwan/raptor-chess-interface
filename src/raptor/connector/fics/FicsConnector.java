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
import raptor.action.RaptorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.fics.pref.FicsPage;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ConnectorQuadrantsPage;
import raptor.service.ActionService;
import raptor.service.ThreadService;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.BugGamesWindowItem;
import raptor.swt.BugPartnersWindowItem;
import raptor.swt.BugTeamsWindowItem;
import raptor.swt.RegExDialog;
import raptor.swt.SeekTableWindowItem;
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
	protected Action isShowingBugButtonsOnConnectAction;
	/**
	 * Raptor allows connecting to fics twice with different profiles. Override
	 * short name and change it to fics2 so users can distinguish the two.
	 */
	protected FicsConnector fics2 = null;

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

		seekTableAction = new Action("Show &Seek Table") {
			@Override
			public void run() {
				SeekTableWindowItem item = new SeekTableWindowItem(
						getSeekService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		bughouseArenaAvailPartnersAction = new Action(
				"Show Bughouse Available &Partners") {
			@Override
			public void run() {
				BugPartnersWindowItem item = new BugPartnersWindowItem(
						getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		bughouseArenaPartnershipsAction = new Action(
				"Show Bughouse Available &Teams") {
			@Override
			public void run() {
				BugTeamsWindowItem item = new BugTeamsWindowItem(
						getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		bughouseArenaGamesAction = new Action("Show Bughouse &Games") {
			@Override
			public void run() {
				BugGamesWindowItem item = new BugGamesWindowItem(
						getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
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
		bughouseArenaPartnershipsAction.setEnabled(false);
		bughouseArenaAvailPartnersAction.setEnabled(false);
		bughouseArenaGamesAction.setEnabled(false);
		seekTableAction.setEnabled(false);
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
						fics2.setSimulBugConnector(true);
						fics2.setSimulBugPartnerName(getUserName());
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
				SeekTableWindowItem item = new SeekTableWindowItem(fics2
						.getSeekService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		fics2.bughouseArenaAvailPartnersAction = new Action(
				"Show Bughouse Available &Partners") {
			@Override
			public void run() {
				BugPartnersWindowItem item = new BugPartnersWindowItem(fics2
						.getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		fics2.bughouseArenaPartnershipsAction = new Action(
				"Show Bughouse Available &Teams") {
			@Override
			public void run() {
				BugTeamsWindowItem item = new BugTeamsWindowItem(fics2
						.getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
			}
		};

		fics2.bughouseArenaGamesAction = new Action("Show Bughouse &Games") {
			@Override
			public void run() {
				BugGamesWindowItem item = new BugGamesWindowItem(fics2
						.getBughouseService());
				Raptor.getInstance().getWindow().addRaptorWindowItem(item);
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
				if (!Raptor.getInstance().getWindow().containsBugButtonsItem(
						fics2)) {
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							new BugButtonsWindowItem(fics2));
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
		fics2.seekTableAction.setEnabled(false);
		fics2.bughouseArenaPartnershipsAction.setEnabled(false);
		fics2.bughouseArenaAvailPartnersAction.setEnabled(false);
		fics2.bughouseArenaGamesAction.setEnabled(false);
		fics2.regexTabAction.setEnabled(false);
		fics2.bugbuttonsAction.setEnabled(false);
		fics2.isShowingBugButtonsOnConnectAction.setEnabled(true);

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

				if (isSimulBugConnector) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException ie) {
					}
					sendMessage("set bugopen on");

					if (StringUtils.isNotBlank(getSimulBugPartnerName())) {
						sendMessage("partner " + getSimulBugPartnerName());
					}
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
				if (fics2 != null
						&& fics2.isConnected()
						&& StringUtils.equalsIgnoreCase(event.getSource(),
								fics2.getUserName())) {
					if (LOG.isInfoEnabled()) {
						LOG.info("Created simul bughouse partnership with "
								+ fics2.getUserName());
					}
					isSimulBugConnector = true;
					simulBugPartnerName = event.getSource();
					fics2.isSimulBugConnector = true;
					fics2.simulBugPartnerName = getUserName();
				}
			} else if (event.getType() == ChatType.PARTNERSHIP_DESTROYED) {
				if (fics2 != null && isSimulBugConnector) {
					if (LOG.isInfoEnabled()) {
						LOG.info("Ended simul bughouse partnership with "
								+ fics2.getUserName());
					}
					isSimulBugConnector = false;
					simulBugPartnerName = null;
					fics2.isSimulBugConnector = false;
					fics2.simulBugPartnerName = null;
				}
			}
			super.publishEvent(event);
		}
	}
}
