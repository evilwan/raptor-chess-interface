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
package raptor.pref.page;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;
import raptor.util.RaptorStringUtils;

public class RaptorPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public static final String[][] POLLING_REFRESH = {
			{ "Every 2 Seconds", "" + 2 }, { "Every 3 Seconds", "" + 3 },
			{ "Every 4 Seconds", "" + 4 }, { "Every 5 Seconds", "" + 5 },
			{ "Every 6 Seconds", "" + 6 }, { "Every 7 Seconds", "" + 7 },
			{ "Every 8 Seconds", "" + 8 }, };

	public RaptorPage() {
		super(FLAT);
		setTitle("Raptor");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		LabelFieldEditor userHomeDir = new LabelFieldEditor(
				"NONE",
				"Raptor user's home directory: " + Raptor.USER_RAPTOR_HOME_PATH,
				getFieldEditorParent());
		addField(userHomeDir);

		BooleanFieldEditor isPlayingSound = new BooleanFieldEditor(
				PreferenceKeys.APP_SOUND_ENABLED, "Sound Enabled",
				getFieldEditorParent());
		addField(isPlayingSound);

		BooleanFieldEditor isSavingGamePgn = new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LOGGING_GAMES, "Save my games to "
						+ Raptor.GAMES_PGN_FILE, getFieldEditorParent());
		addField(isSavingGamePgn);

		BooleanFieldEditor launchExternalLinkx = new BooleanFieldEditor(
				PreferenceKeys.APP_OPEN_LINKS_IN_EXTERNAL_BROWSER,
				"Launch links in external browser", getFieldEditorParent());
		addField(launchExternalLinkx);

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LAUNCHNG_HOME_PAGE,
				"Launch startup browser", getFieldEditorParent()));

		StringFieldEditor homePage = new StringFieldEditor(
				PreferenceKeys.APP_HOME_URL, "Startup Browser Url:",
				getFieldEditorParent());
		addField(homePage);

		File file = new File(Raptor.USER_RAPTOR_DIR + "/imagecache");
		final File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".png");
			}
		});

		long size = 0;
		if (files != null) {
			for (File currentFile : files) {
				size += currentFile.length();
			}
		}

		StringFieldEditor linuxBrowserPage = new StringFieldEditor(
				PreferenceKeys.APP_LINUX_UNIX_BROWSER_NAME,
				"External Browser Name (Linux/Unix Only) (Advanced):",
				getFieldEditorParent());
		addField(linuxBrowserPage);

		StringFieldEditor timesealInitString = new StringFieldEditor(
				PreferenceKeys.TIMESEAL_INIT_STRING,
				"Timeseal Init String (Advanced):", getFieldEditorParent());
		addField(timesealInitString);

		labelButtonFieldEditor = new LabelButtonFieldEditor(
				"NONE",
				"Current image cache size: "
						+ RaptorStringUtils.getMegs(size)
						+ "\n (Advanced: This could negatively impact Raptor performance)",
				getFieldEditorParent(), "Clear Cache", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						for (File currentFile : files) {
							currentFile.delete();
						}

						File file = new File(Raptor.USER_RAPTOR_DIR
								+ "/imagecache");
						final File[] files = file
								.listFiles(new FilenameFilter() {

									public boolean accept(File dir, String name) {
										return name.endsWith(".png");
									}
								});

						long size = 0;
						if (files != null) {
							for (File currentFile : files) {
								size += currentFile.length();
							}
						}
						labelButtonFieldEditor
								.setLabelText("Current image cache size: "
										+ size / 1024 + "K");
					}

				});
		addField(labelButtonFieldEditor);

		addField(new ComboFieldEditor(
				PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL,
				"Polling Refresh Interval (Seek Table,Bug Partners,Bug Teams,etc):",
				POLLING_REFRESH, getFieldEditorParent()));
	}
}