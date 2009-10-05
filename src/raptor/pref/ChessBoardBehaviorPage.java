package raptor.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;

public class ChessBoardBehaviorPage extends FieldEditorPreferencePage {
	public ChessBoardBehaviorPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Behavior");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_JAIL, "Show Piece Jail",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_SHOW_COORDINATES, "Show Coordinates",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING,
				"Play Move Sound When Observing", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_USING_CROSSHAIRS_CURSOR,
				"Invisible Move Enabled (Crosshairs cursor on drag and drops)",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_IS_SHOWING_PIECE_UNICODE_CHARS,
				"Show chess piece unicode chars (e.g. \u2654\u2655\u2656\u2657\u2658\u2659)",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.BOARD_PREMOVE_ENABLED,
				"Premove Enabled", getFieldEditorParent()));

		addField(new BooleanFieldEditor(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED,
				"Queueing Premove Enabled", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.BOARD_SMARTMOVE_ENABLED,
				"Smartmove Enabled (Middle Click)", getFieldEditorParent()));
	}
}
