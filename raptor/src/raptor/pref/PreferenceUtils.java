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
package raptor.pref;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.connector.Connector;
import raptor.international.L10n;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ActionKeyBindingsPage;
import raptor.pref.page.ActionScriptsPage;
import raptor.pref.page.BughousePage;
import raptor.pref.page.ChatConsoleBehaviorPage;
import raptor.pref.page.ChatConsoleChannelColorsPage;
import raptor.pref.page.ChatConsoleMessageColorsPage;
import raptor.pref.page.ChatConsolePage;
import raptor.pref.page.ChatConsoleRightClickScripts;
import raptor.pref.page.ChatConsoleTagsPage;
import raptor.pref.page.ChatConsoleToolbarsPage;
import raptor.pref.page.ChessBoardArrowsPage;
import raptor.pref.page.ChessBoardBehaviorPage;
import raptor.pref.page.ChessBoardColorsPage;
import raptor.pref.page.ChessBoardFontsPage;
import raptor.pref.page.ChessBoardHighlightsPage;
import raptor.pref.page.ChessBoardMouseActions;
import raptor.pref.page.ChessBoardPage;
import raptor.pref.page.ChessBoardResultsPage;
import raptor.pref.page.ChessBoardToolbarsPage;
import raptor.pref.page.ChessEnginesPage;
import raptor.pref.page.GeneralPage;
import raptor.pref.page.InactiveMouseActionsPage;
import raptor.pref.page.ObservingMouseActionsPage;
import raptor.pref.page.PlayingMouseActionsPage;
import raptor.pref.page.RaptorWindowPage;
import raptor.pref.page.RaptorWindowQuadrantsPage;
import raptor.pref.page.ChatEventScripts;
import raptor.pref.page.ScriptsPage;
import raptor.pref.page.SeekPage;
import raptor.pref.page.SoundPage;
import raptor.pref.page.SpeechPage;
import raptor.pref.page.UciEnginesPage;
import raptor.pref.page.XboardEnginesPage;
import raptor.service.ConnectorService;

/**
 * A class containing utility methods for Preferences.
 */
public class PreferenceUtils {

	protected static L10n local = L10n.getInstance();
	private static PreferenceDialog dlg;
	
	/**
	 * Launches the preference dialog.
	 * 
	 * All connectors in the ConnectorService have their preference nodes added.
	 */
	public static void launchPreferenceDialog() {
		create();

		// Open the dialog
		dlg.open();
	}
	
	public static void restartDialog() {
		dlg.close();
		create();
		dlg.open();
	}
	
	private static void create() {
		// Create the preference manager
		PreferenceManager mgr = new PreferenceManager('/');

		mgr.addToRoot(new PreferenceNode("general", new GeneralPage()));
		mgr.addTo("general", new PreferenceNode("layout1",
				new RaptorWindowQuadrantsPage("1")));
		mgr.addTo("general", new PreferenceNode("window",
				new RaptorWindowPage()));
		mgr.addToRoot(new PreferenceNode("bughouse", new BughousePage()));
		mgr
				.addTo(
						"bughouse",
						new PreferenceNode(
								"buttons",
								new ActionContainerPage(
										local.getString("prefUtil1"),
										local.getString("prefUtil2"),
										RaptorActionContainer.BugButtons)));
		mgr.addToRoot(new PreferenceNode("chessBoard", new ChessBoardPage()));
		mgr.addTo("chessBoard", new PreferenceNode("arrows",
				new ChessBoardArrowsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("behavior",
				new ChessBoardBehaviorPage()));
		mgr.addTo("chessBoard", new PreferenceNode("colors",
				new ChessBoardColorsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("fonts",
				new ChessBoardFontsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("highlights",
				new ChessBoardHighlightsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("mouseActions",
				new ChessBoardMouseActions()));
		mgr.addTo("chessBoard/mouseActions", new PreferenceNode("inactive",
				new InactiveMouseActionsPage()));
		mgr.addTo("chessBoard/mouseActions", new PreferenceNode("playing",
				new PlayingMouseActionsPage()));
		mgr.addTo("chessBoard/mouseActions", new PreferenceNode("observing",
				new ObservingMouseActionsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("toolbar",
				new ChessBoardToolbarsPage()));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"bugSuggest",
								new ActionContainerPage(
										local.getString("prefUtil3"),
										local.getString("prefUtil4"),
										RaptorActionContainer.BughouseSuggestChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"examining",
								new ActionContainerPage(
										local.getString("prefUtil5"),
										local.getString("prefUtil6"),
										RaptorActionContainer.ExaminingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"inactive",
								new ActionContainerPage(
										local.getString("prefUtil7"),
										local.getString("prefUtil8"),
										RaptorActionContainer.InactiveChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"observing",
								new ActionContainerPage(
										local.getString("prefUtil9"),
										local.getString("prefUtil10"),
										RaptorActionContainer.ObservingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"playing",
								new ActionContainerPage(
										local.getString("prefUtil11"),
										local.getString("prefUtil12"),
										RaptorActionContainer.PlayingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"setup",
								new ActionContainerPage(
										local.getString("prefUtil13"),
										local.getString("prefUtil14"),
										RaptorActionContainer.SetupChessBoard)));
		mgr.addTo("chessBoard", new PreferenceNode("results",
				new ChessBoardResultsPage()));
		mgr.addToRoot(new PreferenceNode("chatConsole", new ChatConsolePage()));
		// Currently unused but keeping it around in case more options are
		// added.
		mgr.addTo("chatConsole", new PreferenceNode("behavior",
				new ChatConsoleBehaviorPage()));
		mgr.addTo("chatConsole", new PreferenceNode("channelColors",
				new ChatConsoleChannelColorsPage()));
		mgr.addTo("chatConsole", new PreferenceNode("messageColors",
				new ChatConsoleMessageColorsPage()));
		mgr.addTo("chatConsole", new PreferenceNode("tags",
				new ChatConsoleTagsPage()));
		mgr.addTo("chatConsole", new PreferenceNode("toolbar",
				new ChatConsoleToolbarsPage()));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"channel",
								new ActionContainerPage(
										local.getString("prefUtil15"),
										local.getString("prefUtil16"),
										RaptorActionContainer.ChannelChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"main",
								new ActionContainerPage(
										local.getString("prefUtil17"),
										local.getString("prefUtil18"),
										RaptorActionContainer.MainChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"partner",
								new ActionContainerPage(
										local.getString("prefUtil19"),
										local.getString("prefUtil20"),
										RaptorActionContainer.BughousePartnerChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"person",
								new ActionContainerPage(
										local.getString("prefUtil21"),
										local.getString("prefUtil22"),
										RaptorActionContainer.PersonChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"regex",
								new ActionContainerPage(
										local.getString("prefUtil23"),
										local.getString("prefUtil24"),
										RaptorActionContainer.RegExChatConsole)));
		mgr.addToRoot(new PreferenceNode("engines", new ChessEnginesPage()));
		mgr.addTo("engines", new PreferenceNode("uciEngines",
				new UciEnginesPage()));
		mgr.addTo("engines", new PreferenceNode("xboardEngines",
				new XboardEnginesPage()));
		mgr.addToRoot(new PreferenceNode("scripts", new ScriptsPage()));
		mgr.addTo("scripts", new PreferenceNode("actionScripts",
				new ActionScriptsPage()));
		mgr.addTo("scripts", new PreferenceNode("actionScriptKeys",
				new ActionKeyBindingsPage()));
		mgr.addTo("scripts", new PreferenceNode("regex",
				new ChatEventScripts()));
		mgr.addTo("scripts", new PreferenceNode("rightClickScripts",
				new ChatConsoleRightClickScripts()));
		mgr.addToRoot(new PreferenceNode("seeks", new SeekPage()));
		mgr.addToRoot(new PreferenceNode("sound", new SoundPage()));
		mgr.addToRoot(new PreferenceNode("speech", new SpeechPage()));

		// Add the connector preference nodes.
		Connector[] connectors = ConnectorService.getInstance().getConnectors();
		for (Connector connector : connectors) {

			PreferencePage root = connector.getRootPreferencePage();
			if (root != null) {
				mgr
						.addToRoot(new PreferenceNode(connector.getShortName(),
								root));
				PreferenceNode[] secondaries = connector
						.getSecondaryPreferenceNodes();
				if (secondaries != null && secondaries.length > 0) {
					for (PreferenceNode node : secondaries) {
						mgr.addTo(connector.getShortName(), node);
					}
				}
			}
		}

		// Create the preferences dialog
		dlg = new PreferenceDialog(Raptor.getInstance()
				.getWindow().getShell(), mgr);
	}
}
