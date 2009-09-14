package raptor.pref;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.service.SWTService;

public class ColorsPage extends FieldEditorPreferencePage {
	public ColorsPage() {
		// Use the "grid" layout
		super(GRID);
		setPreferenceStore(SWTService.getInstance().getStore());
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
		addField(new ColorFieldEditor(
				SWTService.BOARD_BACKGROUND_COLOR_KEY,
				"Board Window Background Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				SWTService.BOARD_COORDINATES_COLOR,
				"Board Coordinates Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				SWTService.BOARD_HIGHLIGHT_COLOR_KEY,
				"Board Highlight Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				SWTService.BOARD_ACTIVE_CLOCK_COLOR,
				"Clock Active Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(
				SWTService.BOARD_INACTIVE_CLOCK_COLOR,
				"Clock Inactive Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(SWTService.BOARD_LAG_COLOR,
				"Lag Color:", getFieldEditorParent()));
		addField(new ColorFieldEditor(SWTService.BOARD_PIECE_JAIL_COLOR,
				"Piece Jail Label Color:", getFieldEditorParent()));
	}
}