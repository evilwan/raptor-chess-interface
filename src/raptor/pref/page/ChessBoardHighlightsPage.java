package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class ChessBoardHighlightsPage extends FieldEditorPreferencePage {

	public static final String[][] HIGHLIGHT_ANIMATION_DELAY_OPTIONS = {
			{ "100 milliseconds", "100" }, { "200 milliseconds", "200" },
			{ "300 milliseconds", "300" }, { "500 milliseconds", "500" },
			{ "1 second", "1000" } };

	public static final String[][] HIGHLIGHT_BORDER_PERCENTAGE_OPTIONS = {
			{ "2%", "2" }, { "3%", "3" }, { "5%", "4" }, { "8%", "8" },
			{ "10%", "10" } };

	public ChessBoardHighlightsPage() {
		super(GRID);
		setTitle("Highlights");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_OBS_MOVES,
				"Show highlights on observed and opponents moves",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES,
				"Show highlights on move list moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_MY_PREMOVES,
				"Show highlights on my premoves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_MY_MOVES,
				"Show highlights on my moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE,
				"Highlights fade away", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.HIGHLIGHT_ANIMATION_DELAY,
				"Highlight animation delay:", 5,
				HIGHLIGHT_ANIMATION_DELAY_OPTIONS, getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.HIGHLIGHT_WIDTH_PERCENTAGE,
				"Highlight border percentage of square size:", 5,
				HIGHLIGHT_BORDER_PERCENTAGE_OPTIONS, getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.HIGHLIGHT_MY_COLOR,
				"My highlight color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.HIGHLIGHT_OPPONENT_COLOR,
				"Opponent highlight color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.HIGHLIGHT_OBS_COLOR,
				"Observe highlight color:", getFieldEditorParent()));
	}

}
