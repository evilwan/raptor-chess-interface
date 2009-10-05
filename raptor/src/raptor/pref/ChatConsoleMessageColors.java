package raptor.pref;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsoleMessageColors extends FieldEditorPreferencePage {

	public static final String[][] CONSOLE_CHARS = {
			{ "1/4 Million Characters", "250000" },
			{ "1/2 Million Characters", "500000" },
			{ "1 Million Characters", "1000000" },
			{ "5 Million Characters", "5000000" },
			{ "10 Million Characters", "10000000" } };

	public ChatConsoleMessageColors() {
		super(FLAT);
		setTitle("Message Colors");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	LabelButtonFieldEditor labelButtonFieldEditor;

	@Override
	protected void createFieldEditors() {
		ColorFieldEditor defaultMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_INPUT_DEFAULT_TEXT_COLOR,
				"Default Message Color:", getFieldEditorParent());
		addField(defaultMessages);

		ColorFieldEditor challengeMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.CHALLENGE + "-color",
				"Challenge Message Color:", getFieldEditorParent());
		addField(challengeMessages);

		ColorFieldEditor cshoutMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.CSHOUT + "-color", "C-Shout Message Color:",
				getFieldEditorParent());
		addField(cshoutMessages);

		ColorFieldEditor shoutMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.SHOUT + "-color", "Shout Message Color:",
				getFieldEditorParent());
		addField(shoutMessages);

		ColorFieldEditor kibitzMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.KIBITZ + "-color", "Kibitz Message Color:",
				getFieldEditorParent());
		addField(kibitzMessages);

		ColorFieldEditor whisperMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.WHISPER + "-color",
				"Whisper Message Color:", getFieldEditorParent());
		addField(whisperMessages);

		ColorFieldEditor ptellMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.PARTNER_TELL + "-color",
				"Partner Tell Message Color:", getFieldEditorParent());
		addField(ptellMessages);

		ColorFieldEditor outboundMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.OUTBOUND + "-color", "Sent Message Color:",
				getFieldEditorParent());
		addField(outboundMessages);

		ColorFieldEditor internalMessages = new ColorFieldEditor(
				PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
						+ ChatType.INTERNAL + "-color",
				"Raptor Message Color:", getFieldEditorParent());
		addField(internalMessages);
	}
}