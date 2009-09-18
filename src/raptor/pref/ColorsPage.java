package raptor.pref;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.App;

public class ColorsPage extends FieldEditorPreferencePage implements
		PreferenceKeys {
	public ColorsPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(App.getInstance().getPreferences());
		setTitle("Colors");
	}

	@Override
	// protected Control createContents(Composite parent) {
	//
	// // Add a boolean field
	//
	// return parent;
	// }
	/*
	 * * Creates the field editors
	 */
	protected void createFieldEditors() {
		addField(new ColorFieldEditor(BOARD_BACKGROUND_COLOR,
				"Board Window Background Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_COORDINATES_COLOR,
				"Board Coordinates Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_HIGHLIGHT_COLOR,
				"Board Highlight Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_ACTIVE_CLOCK_COLOR,
				"Clock Active Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_INACTIVE_CLOCK_COLOR,
				"Clock Inactive Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_LAG_COLOR, "Lag Color:",
				getFieldEditorParent()));
		addField(new ColorFieldEditor(BOARD_PIECE_JAIL_COLOR,
				"Piece Jail Label Color:", getFieldEditorParent()));
	}
}