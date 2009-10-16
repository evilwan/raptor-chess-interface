package raptor.pref.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;

public class SpeechPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public SpeechPage() {
		super(FLAT);
		setTitle("Speech");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		LabelFieldEditor userHomeDir = new LabelFieldEditor(
				"NONE",
				"Raptor uses speech in scripting.\n"
						+ "By default OSX will be automatically configured for speech. \n"
						+ "However other operating systems may desire to specify the process to use for speech.\n"
						+ "Linux/Unix users may want to configure say for instance.\n"
						+ "Windows users may want to take a look at this link to configure speech using a process:\n"
						+ "http://krolik.net/post/Say-exe-a-simple-command-line-text-to-speech-program-for-Windows.aspx.\n"
						+ "A restart will be required after setting this command.",
				getFieldEditorParent());
		addField(userHomeDir);

		StringFieldEditor timesealInitString = new StringFieldEditor(
				PreferenceKeys.SPEECH_PROCESS_NAME, "Speech process name:",
				getFieldEditorParent());
		addField(timesealInitString);
	}
}