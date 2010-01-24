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
package raptor.connector.fics;

import static raptor.chess.util.GameUtils.getChessPieceCharacter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.connector.fics.pref.FicsPage;
import raptor.connector.ics.IcsConnector;
import raptor.connector.ics.IcsConnectorContext;
import raptor.connector.ics.IcsParser;
import raptor.connector.ics.IcsUtils;
import raptor.connector.ics.dialog.IcsLoginDialog;
import raptor.pref.PreferenceKeys;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ConnectorMessageBlockPage;
import raptor.pref.page.ConnectorQuadrantsPage;
import raptor.service.ActionScriptService;
import raptor.service.ThreadService;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.FicsSeekDialog;
import raptor.swt.RegularExpressionEditorDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsole;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.RegExController;
import raptor.swt.chess.controller.PlayingMouseAction;
import raptor.util.RaptorStringTokenizer;

/**
 * The connector used to connect to www.freechess.org.
 */
public class FicsConnector extends IcsConnector implements PreferenceKeys,
		GameConstants {
	private static final Log LOG = LogFactory.getLog(FicsConnector.class);
	protected static final String[][] PROBLEM_ACTIONS = {
			{ "Tactics", "tell puzzlebot gettactics" },
			{ "Mate", "tell puzzlebot getmate" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kpk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play kpkp" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(PAWN, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play kppkp" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kqk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(ROOK, false),
					"tell endgamebot play kqkr" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(QUEEN, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(QUEEN, false),
					"tell endgamebot play kqpkq" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play krk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true)
							+ getChessPieceCharacter(PAWN, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(ROOK, false),
					"tell endgamebot play krpk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play krkp" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(ROOK, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(KNIGHT, false),
					"tell endgamebot play krkn" },
			{ "separator", "separator" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(BISHOP, true)
							+ getChessPieceCharacter(BISHOP, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kbbk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(BISHOP, true)
							+ getChessPieceCharacter(KNIGHT, true) + " vs "
							+ getChessPieceCharacter(KING, false),
					"tell endgamebot play kbnk" },
			{
					getChessPieceCharacter(KING, true)
							+ getChessPieceCharacter(KNIGHT, true)
							+ getChessPieceCharacter(KNIGHT, true) + " vs "
							+ getChessPieceCharacter(KING, false)
							+ getChessPieceCharacter(PAWN, false),
					"tell endgamebot play knnkp" } };

	protected MenuManager ficsMenu;
	protected Action autoConnectAction;
	protected Action connectAction;
	protected List<Action> onlyEnabledOnConnectActions = new ArrayList<Action>(
			20);

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

	protected Object extendedCensorSync = new Object();
	protected static final String EXTENDED_CENSOR_FILE_NAME = Raptor.USER_RAPTOR_HOME_PATH
			+ "/fics/extendedCensor.txt";

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

		synchronized (this) {
			if (isConnected()) {
				try {
					for (Game game : getGameService().getAllActiveGames()) {
						if (game.isInState(Game.PLAYING_STATE)) {
							onResign(game);
						}
					}
				} catch (Throwable t) {
					LOG.warn("Error trying to resign game:", t);
				}

				super.disconnect();
				connectAction.setEnabled(true);
				if (autoConnectAction != null) {
					autoConnectAction.setEnabled(true);
				}
				for (Action action : onlyEnabledOnConnectActions) {
					action.setEnabled(false);
				}
			}
		}
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
		return ficsMenu;
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
				new PreferenceNode(
						"ficsMenuActions",
						new ActionContainerPage(
								"Fics Menu URLs",
								"\tOn this page you can configure the actions shown in the Fics Links "
										+ "menu. You can add new actions on the Action Scripts Page.",
								RaptorActionContainer.FicsMenu)),
				new PreferenceNode("fics",
						new ConnectorMessageBlockPage("fics")),
				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics")),
				new PreferenceNode("fics", new ConnectorQuadrantsPage("fics2")), };

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

	/**
	 * This method is overridden to support simul bughouse.
	 */
	@Override
	public void publishEvent(final ChatEvent event) {
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

	protected void addProblemActions(MenuManager problemMenu) {
		for (int i = 0; i < PROBLEM_ACTIONS.length; i++) {
			if (PROBLEM_ACTIONS[i][0].equals("separator")) {
				problemMenu.add(new Separator());
			} else {
				final String action = PROBLEM_ACTIONS[i][1];
				Action problemAction = new Action(PROBLEM_ACTIONS[i][0]) {
					public void run() {
						sendMessage(action);
					}
				};
				problemAction.setEnabled(false);
				onlyEnabledOnConnectActions.add(problemAction);
				problemMenu.add(problemAction);
			}

		}
	}

	@Override
	protected void connect(final String profileName) {
		synchronized (this) {
			if (!isConnected()) {
				for (Action action : onlyEnabledOnConnectActions) {
					action.setEnabled(true);
				}
				connectAction.setEnabled(false);
				if (autoConnectAction != null) {
					autoConnectAction.setChecked(getPreferences().getBoolean(
							context.getPreferencePrefix() + "auto-connect"));
					autoConnectAction.setEnabled(true);
				}

				super.connect(profileName);

				if (isConnecting) {
					if (getPreferences().getBoolean(
							context.getPreferencePrefix()
									+ "show-bugbuttons-on-connect")) {
						Raptor.getInstance().getWindow().addRaptorWindowItem(
								new BugButtonsWindowItem(this));
					}
				}
			}
		}
	}

	/**
	 * Creates the connectionsMenu and all of the actions associated with it.
	 */
	protected void createMenuActions() {
		ficsMenu = new MenuManager("&Fics");
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

		Action disconnectAction = new Action("&Disconnect") {
			@Override
			public void run() {
				disconnect();
			}
		};

		Action reconnectAction = new Action("&Reconnect") {
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

		Action seekTableAction = new Action("&Seeks") {
			@Override
			public void run() {
				SWTUtils.openSeekTableWindowItem(FicsConnector.this);
			}
		};

		Action bugwhoAction = new Action("Bug Who") {
			@Override
			public void run() {
				SWTUtils.openBugWhoWindowItem(FicsConnector.this);
			}
		};

		Action bugbuttonsAction = new Action("Bug &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(FicsConnector.this);
			}
		};

		Action gamesAction = new Action("&Games") {
			@Override
			public void run() {
				SWTUtils.openGamesWindowItem(FicsConnector.this);
			}
		};

		Action regexTabAction = new Action("&Regular Expression") {
			@Override
			public void run() {
				RegularExpressionEditorDialog regExDialog = new RegularExpressionEditorDialog(
						Raptor.getInstance().getWindow().getShell(),
						getShortName() + " Regular Expression Dialog",
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

		autoConnectAction = new Action("Toggle Auto &Login",
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				getPreferences().setValue(
						context.getPreferencePrefix() + "auto-connect",
						isChecked());
				getPreferences().save();
			}
		};

		fics2.connectAction = new Action("&Connect") {
			@Override
			public void run() {
				IcsLoginDialog dialog = new IcsLoginDialog(context
						.getPreferencePrefix(), "Fics Simultaneous Login");
				if (isConnected()) {
					dialog.setShowingSimulBug(true);
				}
				dialog.setShowingAutoLogin(false);
				dialog.open();
				if (autoConnectAction != null) {
					autoConnectAction.setChecked(getPreferences().getBoolean(
							context.getPreferencePrefix() + "auto-connect"));
				}
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

		Action fics2DisconnectAction = new Action("&Disconnect") {
			@Override
			public void run() {
				fics2.disconnect();
			}
		};

		Action fics2ReconnectAction = new Action("&Reconnect") {
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

		Action fics2SeekTableAction = new Action("&Seeks") {
			@Override
			public void run() {
				SWTUtils.openSeekTableWindowItem(fics2);
			}
		};

		Action fics2GamesAction = new Action("&Games") {
			@Override
			public void run() {
				SWTUtils.openGamesWindowItem(fics2);
			}
		};

		Action fics2bugwhoAction = new Action("Bug &Who") {
			@Override
			public void run() {
				SWTUtils.openBugWhoWindowItem(fics2);
			}
		};

		Action fics2RegexTabAction = new Action("&Regular Expression") {
			@Override
			public void run() {
				RegularExpressionEditorDialog regExDialog = new RegularExpressionEditorDialog(
						Raptor.getInstance().getWindow().getShell(),
						getShortName() + " Regular Expression Dialog",
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

		Action fics2BugbuttonsAction = new Action("Bughouse &Buttons") {
			@Override
			public void run() {
				SWTUtils.openBugButtonsWindowItem(fics2);
			}
		};

		Action showSeekDialogAction = new Action("Seek A Game") {
			public void run() {
				FicsSeekDialog dialog = new FicsSeekDialog(Raptor.getInstance()
						.getWindow().getShell());
				String seek = dialog.open();
				if (seek != null) {
					sendMessage(seek);
				}
			}
		};

		MenuManager actions = new MenuManager("Actions");

		RaptorAction[] scripts = ActionScriptService.getInstance().getActions(
				Category.IcsCommands);
		for (final RaptorAction raptorAction : scripts) {
			Action action = new Action(raptorAction.getName()) {
				public void run() {
					raptorAction.setConnectorSource(FicsConnector.this);
					raptorAction.run();
				}
			};
			action.setEnabled(false);
			action.setToolTipText(raptorAction.getDescription());
			onlyEnabledOnConnectActions.add(action);
			actions.add(action);
		}

		connectAction.setEnabled(true);
		disconnectAction.setEnabled(false);
		reconnectAction.setEnabled(false);
		autoConnectAction.setEnabled(true);
		bugwhoAction.setEnabled(false);
		seekTableAction.setEnabled(false);
		regexTabAction.setEnabled(false);
		bugbuttonsAction.setEnabled(false);
		showSeekDialogAction.setEnabled(false);
		gamesAction.setEnabled(false);

		onlyEnabledOnConnectActions.add(bugwhoAction);
		onlyEnabledOnConnectActions.add(disconnectAction);
		onlyEnabledOnConnectActions.add(reconnectAction);
		onlyEnabledOnConnectActions.add(regexTabAction);
		onlyEnabledOnConnectActions.add(seekTableAction);
		onlyEnabledOnConnectActions.add(bugbuttonsAction);
		onlyEnabledOnConnectActions.add(showSeekDialogAction);
		onlyEnabledOnConnectActions.add(gamesAction);

		fics2.connectAction.setEnabled(true);
		fics2DisconnectAction.setEnabled(false);
		fics2ReconnectAction.setEnabled(false);
		fics2SeekTableAction.setEnabled(false);
		fics2bugwhoAction.setEnabled(false);
		fics2RegexTabAction.setEnabled(false);
		fics2BugbuttonsAction.setEnabled(false);
		fics2GamesAction.setEnabled(false);

		fics2.onlyEnabledOnConnectActions.add(fics2bugwhoAction);
		fics2.onlyEnabledOnConnectActions.add(fics2DisconnectAction);
		fics2.onlyEnabledOnConnectActions.add(fics2ReconnectAction);
		fics2.onlyEnabledOnConnectActions.add(fics2RegexTabAction);
		fics2.onlyEnabledOnConnectActions.add(fics2SeekTableAction);
		fics2.onlyEnabledOnConnectActions.add(fics2BugbuttonsAction);
		fics2.onlyEnabledOnConnectActions.add(fics2GamesAction);

		autoConnectAction.setChecked(getPreferences().getBoolean(
				context.getPreferencePrefix() + "auto-connect"));

		ficsMenu.add(connectAction);
		ficsMenu.add(disconnectAction);
		ficsMenu.add(reconnectAction);
		ficsMenu.add(autoConnectAction);

		MenuManager fics2Menu = new MenuManager(
				"&Another Simultaneous Connection");
		MenuManager fics2TabsMenu = new MenuManager("&Tabs");
		fics2Menu.add(fics2.connectAction);
		fics2Menu.add(fics2DisconnectAction);
		fics2Menu.add(fics2ReconnectAction);
		fics2Menu.add(new Separator());
		fics2TabsMenu.add(fics2GamesAction);
		fics2TabsMenu.add(fics2SeekTableAction);
		fics2TabsMenu.add(new Separator());
		fics2TabsMenu.add(fics2BugbuttonsAction);
		fics2TabsMenu.add(fics2bugwhoAction);
		fics2TabsMenu.add(new Separator());
		fics2TabsMenu.add(fics2RegexTabAction);
		fics2Menu.add(fics2TabsMenu);
		ficsMenu.add(fics2Menu);

		ficsMenu.add(new Separator());
		ficsMenu.add(actions);
		MenuManager tabsMenu = new MenuManager("&Tabs");
		tabsMenu.add(gamesAction);
		tabsMenu.add(seekTableAction);
		tabsMenu.add(new Separator());
		tabsMenu.add(bugbuttonsAction);
		tabsMenu.add(bugwhoAction);
		tabsMenu.add(new Separator());
		tabsMenu.add(regexTabAction);
		ficsMenu.add(tabsMenu);
		MenuManager linksMenu = new MenuManager("&Links");
		RaptorAction[] ficsMenuActions = ActionScriptService.getInstance()
				.getActions(RaptorActionContainer.FicsMenu);
		for (final RaptorAction raptorAction : ficsMenuActions) {
			if (raptorAction instanceof Separator) {
				linksMenu.add(new Separator());
			} else {
				Action action = new Action(raptorAction.getName()) {
					@Override
					public void run() {
						raptorAction.setConnectorSource(FicsConnector.this);
						raptorAction.run();
					}
				};
				action.setToolTipText(raptorAction.getDescription());
				linksMenu.add(action);
			}
		}
		ficsMenu.add(linksMenu);
		MenuManager problems = new MenuManager("&Puzzles");
		addProblemActions(problems);
		ficsMenu.add(problems);
		ficsMenu.add(showSeekDialogAction);
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

	protected boolean isSmartMoveEnabled() {
		return isSmartMoveOption(getPreferences().getString(
				PreferenceKeys.PLAYING_CONTROLLER
						+ PreferenceKeys.RIGHT_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences()
						.getString(
								PreferenceKeys.PLAYING_CONTROLLER
										+ PreferenceKeys.LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.LEFT_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MIDDLE_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MISC1_MOUSE_BUTTON_ACTION))
				|| isSmartMoveOption(getPreferences().getString(
						PreferenceKeys.PLAYING_CONTROLLER
								+ PreferenceKeys.MISC2_MOUSE_BUTTON_ACTION));
	}

	protected boolean isSmartMoveOption(String option) {
		return option != null
				&& (option.equals(PlayingMouseAction.SmartMove.toString())
						|| option.equals(PlayingMouseAction.RandomCapture
								.toString())
						|| option.equals(PlayingMouseAction.RandomMove
								.toString()) || option
						.equals(PlayingMouseAction.RandomRecapture.toString()));
	}

	protected void loadExtendedCensorList() {
		if (new File(EXTENDED_CENSOR_FILE_NAME).exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Loading " + EXTENDED_CENSOR_FILE_NAME);
			}
			synchronized (extendedCensorSync) {
				extendedCensorList.clear();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(
							EXTENDED_CENSOR_FILE_NAME));
					String currentLine = null;
					while ((currentLine = reader.readLine()) != null) {
						String user = currentLine.trim();
						if (StringUtils.isNotBlank(user)) {
							extendedCensorList.add(IcsUtils.stripTitles(user)
									.toLowerCase());
						}
					}

				} catch (Throwable t) {
					onError("Error reading " + EXTENDED_CENSOR_FILE_NAME, t);

				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (Throwable t) {
						}
					}
				}
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("No extended censor list found.");
			}
		}
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
				sendMessage("iset pendinfo 1", true);

				if (getPreferences().getBoolean(
						PreferenceKeys.FICS_NO_WRAP_ENABLED)) {
					sendMessage("iset nowrap 1", true);
				}
				sendMessage("iset smartmove "
						+ (isSmartMoveEnabled() ? "1" : "0"), true);
				sendMessage(
						"iset premove "
								+ (getPreferences().getBoolean(
										BOARD_PREMOVE_ENABLED) ? "1" : "0"),
						true);
				sendMessage("set interface "
						+ getPreferences().getString(APP_NAME));
				sendMessage("set style 12", true);
				sendMessage("set bell 0", true);
				sendMessage("set ptime 0", true);

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

	protected void writeExtendedCensorList() {
		synchronized (extendedCensorSync) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(EXTENDED_CENSOR_FILE_NAME, false);
				for (String user : extendedCensorList) {
					writer.write(user + "\n");
				}
				writer.flush();

			} catch (Throwable t) {
				onError("Error writing " + EXTENDED_CENSOR_FILE_NAME, t);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}
}
