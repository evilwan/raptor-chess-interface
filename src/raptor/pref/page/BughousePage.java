package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;

public class BughousePage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public static final String[][] BUG_ARENA_REFRESH = {
			{ "Every 2 Seconds", "" + 2 }, { "Every 3 Seconds", "" + 3 },
			{ "Every 4 Seconds", "" + 4 }, { "Every 5 Seconds", "" + 5 },
			{ "Every 6 Seconds", "" + 6 }, { "Every 7 Seconds", "" + 7 },
			{ "Every 8 Seconds", "" + 8 }, };

	public BughousePage() {
		super(GRID);
		setTitle("Bughouse");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD,
				"Auto open partners board on games I play",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD,
				"Auto open partners board on games I observe",
				getFieldEditorParent()));

		addField(new FontFieldEditor(PreferenceKeys.BUG_BUTTONS_FONT,
				"Button Font:", getFieldEditorParent()));

		ComboFieldEditor layoutsFieldEditor = new ComboFieldEditor(
				PreferenceKeys.BUG_ARENA_REFRESH_SECONDS,
				"Bug Who Screens Refresn Interval:", BUG_ARENA_REFRESH,
				getFieldEditorParent());
		addField(layoutsFieldEditor);

	}
}