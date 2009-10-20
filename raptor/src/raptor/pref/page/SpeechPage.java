package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;
import raptor.pref.fields.LabelFieldEditor;
import raptor.service.SoundService;

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
				WordUtils
						.wrap(
								"Raptor uses speech in scripting. "
										+ "By default OSX will be automatically configured for speech. "
										+ "However other operating systems may desire to specify the process to use for speech. "
										+ "Linux/Unix users may want to configure say for instance. "
										+ "Windows users may want to take a look at this link to configure speech using a process: "
										+ "http://krolik.net/post/Say-exe-a-simple-command-line-text-to-speech-program-for-Windows.aspx.",
								70), getFieldEditorParent());
		addField(userHomeDir);

		final StringFieldEditor speechProcessName = new StringFieldEditor(
				PreferenceKeys.SPEECH_PROCESS_NAME, "Speech process name:",
				getFieldEditorParent());
		addField(speechProcessName);

		labelButtonFieldEditor = new LabelButtonFieldEditor(
				"NONE",
				"You can use this button to test the setting (Requires an apply): ",
				getFieldEditorParent(), "Test", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						SoundService.getInstance().textToSpeech(
								"Speech is setup correctly.");
					}
				});
		addField(labelButtonFieldEditor);
	}
}