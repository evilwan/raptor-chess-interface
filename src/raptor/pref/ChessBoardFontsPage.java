package raptor.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;

public class ChessBoardFontsPage extends FieldEditorPreferencePage {
	public ChessBoardFontsPage() {
		super(GRID);
		setTitle("Fonts");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		FontFieldEditor coordinatesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_FONT, "Coordinates Font:",
				getFieldEditorParent());
		addField(coordinatesFont);

		FontFieldEditor lagFont = new FontFieldEditor(
				PreferenceKeys.BOARD_LAG_FONT, "Total Lag Font:",
				getFieldEditorParent());
		addField(lagFont);

		FontFieldEditor playerNameFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PLAYER_NAME_FONT,
				"Player Name/Rating Font:", getFieldEditorParent());
		addField(playerNameFont);

		FontFieldEditor pieceJailFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_FONT,
				"Piece Jail/Drop Square Number Font:", getFieldEditorParent());
		addField(pieceJailFont);

		FontFieldEditor openingDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, "Opening Font:",
				getFieldEditorParent());
		addField(openingDescriptionFont);

		FontFieldEditor premovesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, "Premoves Font:",
				getFieldEditorParent());
		addField(premovesFont);

		FontFieldEditor gameDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_GAME_DESCRIPTION_FONT, "Game Type Font:",
				getFieldEditorParent());
		addField(gameDescriptionFont);

		FontFieldEditor lastMoveFont = new FontFieldEditor(
				PreferenceKeys.BOARD_STATUS_FONT, "Last Move Font:",
				getFieldEditorParent());
		addField(lastMoveFont);

		FontFieldEditor resultFont = new FontFieldEditor(
				PreferenceKeys.BOARD_RESULT_FONT, "Result Font:",
				getFieldEditorParent());
		addField(resultFont);

		FontFieldEditor outputFont = new FontFieldEditor(
				PreferenceKeys.CHAT_OUTPUT_FONT, "Text To Send Font:",
				getFieldEditorParent());
		addField(outputFont);

		FontFieldEditor promptFont = new FontFieldEditor(
				PreferenceKeys.CHAT_PROMPT_FONT,
				"Text To Send Prompt Label Font:", getFieldEditorParent());
		addField(promptFont);
	}
}