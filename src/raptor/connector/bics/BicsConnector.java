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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.connector.bics.pref.BicsPage;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ConnectorQuadrantsPage;
import raptor.service.ThreadService;
import raptor.swt.BugButtonsWindowItem;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class BicsConnector extends IcsConnector implements PreferenceKeys {

	public static class BicsConnectorContext extends IcsConnectorContext {
		public BicsConnectorContext() {
			super(new IcsParser());
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
	 * Raptor allows connecting to fics twice with different profiles. Override
	 * short name and change it to fics2 so users can distinguish the two.
	 */
	protected BicsConnector bics2 = null;
	protected MenuManager connectionsMenu;

	protected Action autoConnectAction;
	protected Action bughouseArenaAction;
	protected Action connectAction;
	protected Action disconnectAction;
	protected Action reconnectAction;
	protected Action bugbuttonsAction;
	protected Action isShowingBugButtonsOnConnectAction;

	public BicsConnector() {
		this(new BicsConnectorContext());
	}

	public BicsConnector(BicsConnectorContext context) {
		super(context);
		(context.getParser()).setConnector(this);
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
		isShowingBugButtonsOnConnectAction.setEnabled(true);
		bugbuttonsAction.setEnabled(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (bics2 != null) {
			bics2.dispose();
			bics2 = null;
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
		return new BicsPage();
	}

	/**
	 * Returns an array of the secondary preference nodes.
	 */
	public PreferenceNode[] getSecondaryPreferenceNodes() {
		return new PreferenceNode[] {
				new PreferenceNode("bics", new ConnectorQuadrantsPage("bics")),
				new PreferenceNode("bics", new ConnectorQuadrantsPage("bics2")) };

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
				if (!Raptor.getInstance().getWindow().containsBugButtonsItem(
						BicsConnector.this)) {
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							new BugButtonsWindowItem(BicsConnector.this));
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
		isShowingBugButtonsOnConnectAction.setEnabled(true);
		isShowingBugButtonsOnConnectAction.setChecked(getPreferences()
				.getBoolean(
						context.getPreferencePrefix()
								+ "show-bugbuttons-on-connect"));
		connectionsMenu.add(connectAction);
		connectionsMenu.add(disconnectAction);
		connectionsMenu.add(reconnectAction);
		connectionsMenu.add(autoConnectAction);
		connectionsMenu.add(isShowingBugButtonsOnConnectAction);
		connectionsMenu.add(new Separator());
		connectionsMenu.add(bugbuttonsAction);
		connectionsMenu.add(bughouseArenaAction);
		connectionsMenu.add(new Separator());

		MenuManager bics2Menu = new MenuManager(
				"&Another Simultaneous Connection");
		bics2.connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Bics Simultaneous Login");
				dialog.open();
				if (dialog.wasLoginPressed()) {
					bics2.connect(dialog.getSelectedProfile());
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
				if (!Raptor.getInstance().getWindow().containsBugButtonsItem(
						BicsConnector.this)) {
					Raptor.getInstance().getWindow().addRaptorWindowItem(
							new BugButtonsWindowItem(bics2));
				}
			}
		};

		bics2.isShowingBugButtonsOnConnectAction = new Action(
				"Show Bug Buttons On Connect") {
			@Override
			public void run() {
			}
		};

		bics2.connectAction.setEnabled(true);
		bics2.disconnectAction.setEnabled(false);
		bics2.reconnectAction.setEnabled(false);
		bics2.bughouseArenaAction.setEnabled(false);
		bics2.bugbuttonsAction.setEnabled(false);
		bics2.isShowingBugButtonsOnConnectAction.setEnabled(true);

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
			}
		});
	}
}
