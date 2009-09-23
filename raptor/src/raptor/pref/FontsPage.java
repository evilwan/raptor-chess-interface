package raptor.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;

public class FontsPage extends FieldEditorPreferencePage implements
		PreferenceKeys {
	public FontsPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Fonts");
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
		addField(new FontFieldEditor(BOARD_COORDINATES_FONT,
				"Board Coordinates Font:", getFieldEditorParent()));
		addField(new FontFieldEditor(BOARD_PLAYER_NAME_FONT,
				"Board Player Name Font:", getFieldEditorParent()));
		addField(new FontFieldEditor(BOARD_CLOCK_FONT, "Clock Lag Color:",
				getFieldEditorParent()));
		addField(new FontFieldEditor(BOARD_LAG_FONT, "Lag Font:",
				getFieldEditorParent()));

		addField(new FontFieldEditor(BOARD_PIECE_JAIL_FONT,
				"Piece Jail Label Font:", getFieldEditorParent()));
	}
}