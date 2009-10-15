package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public class ChessBoardArrowsPage extends FieldEditorPreferencePage {

	public static final String[][] ARROW_ANIMATION_DELAY_OPTIONS = {
			{ "100 milliseconds", "100" }, { "200 milliseconds", "200" },
			{ "300 milliseconds", "300" }, { "500 milliseconds", "500" },
			{ "1 second", "1000" } };

	public static final String[][] ARROW_BORDER_PERCENTAGE_OPTIONS = {
			{ "8%", "8" }, { "10%", "10" }, { "12%", "12" }, { "15%", "15" },
			{ "18%", "18" } };

	public ChessBoardArrowsPage() {
		super(GRID);
		setTitle("Arrows");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceKeys.ARROW_SHOW_ON_OBS_MOVES,
				"Show arrows on opponent and observed moves",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.ARROW_SHOW_ON_MY_MOVES,
				"Show arrows on my moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.ARROW_SHOW_ON_MOVE_LIST_MOVES,
				"Show arrows on move list moves", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.ARROW_SHOW_ON_MY_PREMOVES,
				"Show arrows as my premoves are made", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.ARROW_FADE_AWAY_MODE,
				"Arrows fade away", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.ARROW_ANIMATION_DELAY, "Arrow animation delay:",
				5, ARROW_ANIMATION_DELAY_OPTIONS, getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceKeys.ARROW_WIDTH_PERCENTAGE,
				"Arrow percentage of square size:", 5,
				ARROW_BORDER_PERCENTAGE_OPTIONS, getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.ARROW_MY_COLOR,
				"My Arrow Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.ARROW_OPPONENT_COLOR,
				"Opponent Arrow Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(PreferenceKeys.ARROW_OBS_COLOR,
				"Observe Arrow Color:", getFieldEditorParent()));
	}
}