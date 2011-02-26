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
package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;
import raptor.util.FileUtils;
import raptor.util.RaptorLogger;

public class GeneralPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public static final String[][] POLLING_REFRESH = {
			{ "Every 2 Seconds", "" + 2 }, { "Every 3 Seconds", "" + 3 },
			{ "Every 4 Seconds", "" + 4 }, { "Every 5 Seconds", "" + 5 },
			{ "Every 6 Seconds", "" + 6 }, { "Every 7 Seconds", "" + 7 },
			{ "Every 8 Seconds", "" + 8 }, };

	public GeneralPage() {
		super(FLAT);
		setTitle("General");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		LabelFieldEditor userHomeDir = new LabelFieldEditor(
				"NONE",
				"Raptor user's home directory: " + Raptor.USER_RAPTOR_HOME_PATH,
				getFieldEditorParent());
		addField(userHomeDir);

		LabelFieldEditor javaVersion = new LabelFieldEditor("NONE",
				WordUtils.wrap("Java Version: "
						+ System.getProperty("java.version"), 70),
				getFieldEditorParent());
		addField(javaVersion);

		LabelFieldEditor osVersion = new LabelFieldEditor("NONE", WordUtils
				.wrap("Operating System: " + System.getProperty("os.name")
						+ " " + System.getProperty("os.version"), 70),
				getFieldEditorParent());
		addField(osVersion);

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LAUNCHNG_HOME_PAGE,
				"Launch browser on startup", getFieldEditorParent()));

		BooleanFieldEditor launchExternalLinkx = new BooleanFieldEditor(
				PreferenceKeys.APP_OPEN_LINKS_IN_EXTERNAL_BROWSER,
				"Launch links in external browser", getFieldEditorParent());
		addField(launchExternalLinkx);

		addField(new BooleanFieldEditor(PreferenceKeys.APP_IS_LOGGING_CONSOLE,
				"Log console messages to " + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-Console.txt",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LOGGING_PERSON_TELLS,
				"Log person tells to " + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-${PersonName}.txt",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LOGGING_CHANNEL_TELLS,
				"Log person tells to " + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-${Channel}.txt",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LAUNCHNG_HOME_PAGE,
				"Launch browser on startup", getFieldEditorParent()));
		
		addField(new FileFieldEditor(
				PreferenceKeys.APP_PGN_FILE, "Save my games in PGN format to: ",
				getFieldEditorParent()));


		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS,
				"Show chess piece unicode chars (e.g. \u2654\u2655\u2656\u2657\u2658\u2659)",
				getFieldEditorParent()));

		BooleanFieldEditor isPlayingSound = new BooleanFieldEditor(
				PreferenceKeys.APP_SOUND_ENABLED, "Sound Enabled",
				getFieldEditorParent());
		addField(isPlayingSound);

		StringFieldEditor homePage = new StringFieldEditor(
				PreferenceKeys.APP_HOME_URL, "Browser Home Page:",
				getFieldEditorParent());
		addField(homePage);

		StringFieldEditor linuxBrowserPage = new StringFieldEditor(
				PreferenceKeys.APP_LINUX_UNIX_BROWSER_NAME,
				"External Browser Name (Linux/Unix Only) (Advanced):",
				getFieldEditorParent());
		addField(linuxBrowserPage);

		addField(new ComboFieldEditor(
				PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL,
				"Polling Refresh Interval (Seek Tab,BugWho Tab, etc):",
				POLLING_REFRESH, getFieldEditorParent()));

		addField(new LabelButtonFieldEditor("NONE", "", getFieldEditorParent(),
				"Reset Raptor To Defaults", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (Raptor
								.getInstance()
								.confirm(
										"You will lose all preferences,saved scripts,saved games,and image caching. "
												+ "Raptor will also exit after executing this action, and will "
												+ "have to be restarted. "
												+ "Do you wish to continue?")) {
							Raptor.getInstance().shutdownWithoutExit(true);
							RaptorLogger.releaseAll();
							FileUtils.deleteDir(Raptor.USER_RAPTOR_DIR);
							System.exit(0);
						}
					}
				}));
	}
}