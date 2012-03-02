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
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.PreferenceUtils;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;
import raptor.util.FileUtils;
import raptor.util.RaptorLogger;

public class GeneralPage extends FieldEditorPreferencePage {
	protected static L10n local = L10n.getInstance();
	
	/* Last set language before the dialog launch */
	private static String lastLang;

	public static final String[][] POLLING_REFRESH = {
			{ local.getString("everySec", 2), "" + 2 }, { local.getString("everySec", 3), "" + 3 },
			{ local.getString("everySec", 4), "" + 4 }, { local.getString("everySec", 5), "" + 5 },
			{ local.getString("everySec", 6), "" + 6 }, { local.getString("everySec", 7), "" + 7 },
			{ local.getString("everySec", 8), "" + 8 }, };
	
	public static final String[][] LANGUAGES = {
		{"English", "en"}, {"Italiano", "it"}
	};

	public GeneralPage() {
		super(FLAT);
		setTitle(local.getString("general"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		lastLang = Raptor.getInstance().
			getPreferences().getString(PreferenceKeys.APP_LOCALE);
			
		LabelFieldEditor userHomeDir = new LabelFieldEditor(
				"NONE",
				local.getString("genP1") + Raptor.USER_RAPTOR_HOME_PATH,
				getFieldEditorParent());
		addField(userHomeDir);

		LabelFieldEditor javaVersion = new LabelFieldEditor("NONE",
				WordUtils.wrap(local.getString("genP2")
						+ System.getProperty("java.version"), 70),
				getFieldEditorParent());
		addField(javaVersion);

		LabelFieldEditor osVersion = new LabelFieldEditor("NONE", WordUtils
				.wrap(local.getString("genP3") + System.getProperty("os.name")
						+ " " + System.getProperty("os.version"), 70),
				getFieldEditorParent());
		addField(osVersion);
		
		addField(new ComboFieldEditor(PreferenceKeys.APP_LOCALE,
				local.getString("language"), LANGUAGES,
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				"app-update",
				local.getString("genP18"), getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LAUNCHING_LOGIN_DIALOG,
				local.getString("genP17"), getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LAUNCHING_HOME_PAGE,
				local.getString("genP4"), getFieldEditorParent()));
		
		BooleanFieldEditor launchExternalLinkx = new BooleanFieldEditor(
				PreferenceKeys.APP_OPEN_LINKS_IN_EXTERNAL_BROWSER,
				local.getString("genP5"), getFieldEditorParent());
		addField(launchExternalLinkx);

		addField(new BooleanFieldEditor(PreferenceKeys.APP_IS_LOGGING_CONSOLE,
				local.getString("genP6") + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-Console.txt",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LOGGING_PERSON_TELLS,
				local.getString("genP7") + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-${PersonName}.txt",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_LOGGING_CHANNEL_TELLS,
				local.getString("genP8") + Raptor.USER_RAPTOR_HOME_PATH
						+ "/logs/console/${ConnectorName}-${Channel}.txt",
				getFieldEditorParent()));
		
		addField(new FileFieldEditor(
				PreferenceKeys.APP_PGN_FILE, local.getString("genP9"),
				getFieldEditorParent()));


		addField(new BooleanFieldEditor(
				PreferenceKeys.APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS,
				local.getString("genP10"),
				getFieldEditorParent()));

		BooleanFieldEditor isPlayingSound = new BooleanFieldEditor(
				PreferenceKeys.APP_SOUND_ENABLED, local.getString("genP11"),
				getFieldEditorParent());
		addField(isPlayingSound);

		StringFieldEditor homePage = new StringFieldEditor(
				PreferenceKeys.APP_HOME_URL, local.getString("genP12"),
				getFieldEditorParent());
		addField(homePage);

		StringFieldEditor linuxBrowserPage = new StringFieldEditor(
				PreferenceKeys.APP_LINUX_UNIX_BROWSER_NAME,
				local.getString("genP13"),
				getFieldEditorParent());
		addField(linuxBrowserPage);

		addField(new ComboFieldEditor(
				PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL,
				local.getString("genP14"),
				POLLING_REFRESH, getFieldEditorParent()));

		addField(new LabelButtonFieldEditor("NONE", "", getFieldEditorParent(),
				local.getString("genP15"), new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (Raptor
								.getInstance()
								.confirm(local.getString("genP16"))) {
							Raptor.getInstance().shutdownWithoutExit(true);
							RaptorLogger.releaseAll();
							FileUtils.deleteDir(Raptor.USER_RAPTOR_DIR);
							System.exit(0);
						}
					}
				}));
	}
	
	public boolean performOk() {
		super.performOk();
		String currLocale = Raptor.getInstance().
			getPreferences().getString(PreferenceKeys.APP_LOCALE);
		
		if (!currLocale.equals(lastLang)) {
			L10n.updateLanguage(false);
			PreferenceUtils.restartDialog();
		}		
		
		return true;
	}
}