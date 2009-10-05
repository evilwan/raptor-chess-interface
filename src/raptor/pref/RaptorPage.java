package raptor.pref;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;

public class RaptorPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

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

		BooleanFieldEditor launchExternalLinkx = new BooleanFieldEditor(
				PreferenceKeys.APP_OPEN_LINKS_IN_EXTERNAL_BROWSER,
				"Launch links in external browser", getFieldEditorParent());
		addField(launchExternalLinkx);

		StringFieldEditor homePage = new StringFieldEditor(
				PreferenceKeys.APP_HOME_URL, "Startup Url:",
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

		labelButtonFieldEditor = new LabelButtonFieldEditor("NONE",
				"Current image cache size: " + size / 1024 + "K",
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
	}
}