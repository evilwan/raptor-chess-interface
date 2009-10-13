package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelButtonFieldEditor;

public class BughousePage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

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
	}
}