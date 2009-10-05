package raptor.pref;

//import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import raptor.Raptor;

public class ChessBoardClocksPage extends FieldEditorPreferencePage {
	public static final String[][] SHOW_TENTHS_OPTIONS = {
			{ "At 10 Seconds", "" + (10 * 1000 + 1) },
			{ "At 1 Minute", "" + (60 * 1000 + 1) },
			{ "At 3 Minute", "" + (3 * 60 * 1000 + 1) },
			{ "At 5 Minute", "" + (5 * 60 * 1000 + 1) },
			{ "At 10 Minute", "" + (10 * 60 * 1000 + 1) },
			{ "Always", "" + Long.MAX_VALUE } };

	public static final String[][] SHOW_SECONDS_OPTIONS = {
			{ "At 60 Minutes", "" + (60 * 60 * 1000 + 1) },
			{ "At 30 Minutes", "" + (30 * 60 * 1000 + 1) },
			{ "At 15 Minutes", "" + (15 * 60 * 1000 + 1) },
			{ "At 10 Minutes", "" + (10 * 10 * 1000 + 1) },
			{ "Always", "" + Long.MAX_VALUE } };

	public ChessBoardClocksPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Clock Preferences");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS,
				"Play 10 second countdown sounds", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN,
				"Show Seconds:", 3, SHOW_SECONDS_OPTIONS,
				getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN,
				"Show Tenths of Seconds:", 3, SHOW_TENTHS_OPTIONS,
				getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR,
				"Active Clock Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR,
				"Inactive Clock Color:", getFieldEditorParent()));

		addField(new FontFieldEditor(PreferenceKeys.BOARD_CLOCK_FONT,
				"Clock Font:", getFieldEditorParent()));
	}
}