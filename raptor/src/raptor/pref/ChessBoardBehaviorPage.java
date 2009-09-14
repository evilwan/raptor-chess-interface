package raptor.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.service.SWTService;

public class ChessBoardBehaviorPage extends FieldEditorPreferencePage {
	public ChessBoardBehaviorPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Chess Board Behavior");
		setPreferenceStore(SWTService.getInstance().getStore());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bfe = new BooleanFieldEditor(
				SWTService.BOARD_IS_SHOW_COORDINATES_KEY, "Show Coordinates",
				getFieldEditorParent());
		addField(bfe);

		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				SWTService.BOARD_IS_SHOWING_PIECE_JAIL, "Show Piece Jail",
				getFieldEditorParent());
		addField(bfe2);

	}
}
