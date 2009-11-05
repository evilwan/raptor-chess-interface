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
package raptor.pref;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;

import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.connector.Connector;
import raptor.pref.page.ActionContainerPage;
import raptor.pref.page.ActionKeyBindingsPage;
import raptor.pref.page.ActionScriptsPage;
import raptor.pref.page.BughousePage;
import raptor.pref.page.ChatConsoleChannelColorsPage;
import raptor.pref.page.ChatConsoleMessageColorsPage;
import raptor.pref.page.ChatConsolePage;
import raptor.pref.page.ChatConsoleRightClickScripts;
import raptor.pref.page.ChatConsoleToolbarsPage;
import raptor.pref.page.ChessBoardArrowsPage;
import raptor.pref.page.ChessBoardBehaviorPage;
import raptor.pref.page.ChessBoardClocksPage;
import raptor.pref.page.ChessBoardColorsPage;
import raptor.pref.page.ChessBoardFontsPage;
import raptor.pref.page.ChessBoardHighlightsPage;
import raptor.pref.page.ChessBoardPage;
import raptor.pref.page.ChessBoardResultsPage;
import raptor.pref.page.ChessBoardToolbarsPage;
import raptor.pref.page.MessageEventScripts;
import raptor.pref.page.RaptorPage;
import raptor.pref.page.RaptorWindowPage;
import raptor.pref.page.RaptorWindowQuadrantsPage;
import raptor.pref.page.SoundPage;
import raptor.pref.page.SpeechPage;
import raptor.service.ConnectorService;

/**
 * A class containing utility methods for Preferences.
 */
public class PreferenceUtils {

	/**
	 * Launches the preference dialog.
	 * 
	 * All connectors in the ConnectorService have their preference nodes added.
	 */
	public static void launchPreferenceDialog() {
		// Create the preference manager
		PreferenceManager mgr = new PreferenceManager('/');

		mgr.addToRoot(new PreferenceNode("raptor", new RaptorPage()));
		mgr
				.addToRoot(new PreferenceNode("raptorWindow",
						new RaptorWindowPage()));
		mgr.addTo("raptorWindow", new PreferenceNode("layout1",
				new RaptorWindowQuadrantsPage("1")));

		mgr.addToRoot(new PreferenceNode("chatConsole", new ChatConsolePage()));
		// Currently unused but keeping it around in case more options are
		// added.
		// mgr.addTo("chatConsole", new PreferenceNode("behavior",
		// new ChatConsoleBehaviorPage()));
		mgr.addTo("chatConsole", new PreferenceNode("messageColors",
				new ChatConsoleMessageColorsPage()));
		mgr.addTo("chatConsole", new PreferenceNode("channelColors",
				new ChatConsoleChannelColorsPage()));
		mgr.addTo("chatConsole", new PreferenceNode("rightClickScripts",
				new ChatConsoleRightClickScripts()));
		mgr.addTo("chatConsole", new PreferenceNode("toolbar",
				new ChatConsoleToolbarsPage()));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"main",
								new ActionContainerPage(
										"Main",
										"\tOn this page you can configure the toolbar for the "
												+ "main chat console. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.MainChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"person",
								new ActionContainerPage(
										"Person",
										"\tOn this page you can configure the toolbar for the "
												+ "person chat console. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.PersonChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"channel",
								new ActionContainerPage(
										"Channel",
										"\tOn this page you can configure the toolbar for the "
												+ "channel chat console. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.ChannelChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"partner",
								new ActionContainerPage(
										"Partner",
										"\tOn this page you can configure the toolbar for the "
												+ "bughouse partner console. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.BughousePartnerChatConsole)));
		mgr
				.addTo(
						"chatConsole/toolbar",
						new PreferenceNode(
								"regex",
								new ActionContainerPage(
										"Regular Expression",
										"\tOn this page you can configure the toolbar for the "
												+ "regular expression console. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.RegExChatConsole)));

		mgr.addToRoot(new PreferenceNode("chessBoard", new ChessBoardPage()));
		mgr.addTo("chessBoard", new PreferenceNode("behavior",
				new ChessBoardBehaviorPage()));
		mgr.addTo("chessBoard", new PreferenceNode("clocks",
				new ChessBoardClocksPage()));
		mgr.addTo("chessBoard", new PreferenceNode("colors",
				new ChessBoardColorsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("fonts",
				new ChessBoardFontsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("arrows",
				new ChessBoardArrowsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("highlights",
				new ChessBoardHighlightsPage()));
		mgr.addTo("chessBoard", new PreferenceNode("results",
				new ChessBoardResultsPage()));

		mgr.addTo("chessBoard", new PreferenceNode("toolbar",
				new ChessBoardToolbarsPage()));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"playing",
								new ActionContainerPage(
										"Playing",
										"\tOn this page you can configure the toolbar "
												+ "for the playing chess board,on fics this is shown when you are playing a "
												+ "game. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.PlayingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"observing",
								new ActionContainerPage(
										"Observing",
										"\tOn this page you can configure the toolbar "
												+ "for the observing chess board,on fics this is shown when you are observing a "
												+ "game. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.ObservingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"examining",
								new ActionContainerPage(
										"Examining",
										"\tOn this page you can configure the toolbar "
												+ "for the examine chess board,on fics this is shown when you are examining a game. "
												+ "You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.ExaminingChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"setup",
								new ActionContainerPage(
										"Setup",
										"\tOn this page you can configure the toolbar for the setup "
												+ "chess board,on fics this is shown during bsetup. You can add new actions on the "
												+ "Action Scripts Page.",
										RaptorActionContainer.SetupChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"bugSuggest",
								new ActionContainerPage(
										"Bughouse Suggest",
										"\tOn this page you can configure the toolbar for the "
												+ "bughouse suggest chess board, i.e. your partners chess board when you are playing a bughouse "
												+ "game. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.BughouseSuggestChessBoard)));
		mgr
				.addTo(
						"chessBoard/toolbar",
						new PreferenceNode(
								"inactive",
								new ActionContainerPage(
										"Inactive",
										"\tOn this page you can configure the toolbar for the inactive "
												+ "chessboards. You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.InactiveChessBoard)));

		mgr.addToRoot(new PreferenceNode("bughouse", new BughousePage()));
		mgr
				.addTo(
						"bughouse",
						new PreferenceNode(
								"buttons",
								new ActionContainerPage(
										"Buttons",
										"\tOn this page you can configure the bughouse button actions shown in the bughouse"
												+ "buttons screen.You can add new actions on the Action Scripts Page.",
										RaptorActionContainer.BugButtons)));

		mgr.addToRoot(new PreferenceNode("scripts", new MessageEventScripts()));

		mgr.addToRoot(new PreferenceNode("actionScripts",
				new ActionScriptsPage()));

		mgr.addToRoot(new PreferenceNode("actionScriptKeys",
				new ActionKeyBindingsPage()));

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
		PreferenceDialog dlg = new PreferenceDialog(Raptor.getInstance()
				.getWindow().getShell(), mgr);

		// Open the dialog
		dlg.open();
	}
}
