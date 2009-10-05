package raptor.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsolePage extends FieldEditorPreferencePage {

	public static final String[][] CONSOLE_CHARS = {
			{ "1/4 Million Characters", "250000" },
			{ "1/2 Million Characters", "500000" },
			{ "1 Million Characters", "1000000" },
			{ "5 Million Characters", "5000000" },
			{ "10 Million Characters", "10000000" } };

	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChatConsolePage() {
		super(FLAT);
		setTitle("Chat Consoles");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		ComboFieldEditor consoleChars = new ComboFieldEditor(
				PreferenceKeys.CHAT_MAX_CONSOLE_CHARS, "Chat Console Size:",
				CONSOLE_CHARS, getFieldEditorParent());
		addField(consoleChars);

		BooleanFieldEditor addTimestamps = new BooleanFieldEditor(
				PreferenceKeys.CHAT_TIMESTAMP_CONSOLE,
				"Add Timestamps To Messages", getFieldEditorParent());
		addField(addTimestamps);

		BooleanFieldEditor underlineSingleQuotes = new BooleanFieldEditor(
				PreferenceKeys.CHAT_UNDERLINE_SINGLE_QUOTES,
				"Underline single quoted text", getFieldEditorParent());
		addField(underlineSingleQuotes);

		StringFieldEditor timestampFormat = new StringFieldEditor(
				PreferenceKeys.CHAT_TIMESTAMP_CONSOLE_FORMAT,
				"Message Timestamp Format (Advanced):", getFieldEditorParent());
		addField(timestampFormat);

		ColorFieldEditor consoleBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_CONSOLE_BACKGROUND_COLOR,
				"Console Window Background Color:", getFieldEditorParent());
		addField(consoleBackground);

		ColorFieldEditor inputTextBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_INPUT_BACKGROUND_COLOR,
				"Console Background Color:", getFieldEditorParent());
		addField(inputTextBackground);

		ColorFieldEditor outputTextForeground = new ColorFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_TEXT_COLOR, "Text To Send Color:",
				getFieldEditorParent());
		addField(outputTextForeground);

		ColorFieldEditor outputTextBackground = new ColorFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_BACKGROUND_COLOR,
				"Text To Send Background Color:", getFieldEditorParent());
		addField(outputTextBackground);

		ColorFieldEditor promptColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_PROMPT_COLOR,
				"Text To Send Prompt Label Color:", getFieldEditorParent());
		addField(promptColor);

		ColorFieldEditor quoteUnderlineColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_QUOTE_UNDERLINE_COLOR,
				"Quoted text Color:", getFieldEditorParent());
		addField(quoteUnderlineColor);

		ColorFieldEditor linkTextColor = new ColorFieldEditor(
				PreferenceKeys.CHAT_LINK_UNDERLINE_COLOR, "Links Color:",
				getFieldEditorParent());
		addField(linkTextColor);

		FontFieldEditor inputFont = new FontFieldEditor(
				PreferenceKeys.CHAT_INPUT_FONT, "Chat Console Font",
				getFieldEditorParent());
		addField(inputFont);

		FontFieldEditor outputFont = new FontFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_FONT, "Text To Send Font",
				getFieldEditorParent());
		addField(outputFont);

		FontFieldEditor promptFont = new FontFieldEditor(
				PreferenceKeys.CHAT_PROMPT_FONT,
				"Text To Send Prompt Label Font", getFieldEditorParent());
		addField(promptFont);
	}
}