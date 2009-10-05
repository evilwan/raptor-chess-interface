package raptor.pref;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;

public class ChessBoardColorsPage extends FieldEditorPreferencePage {
	public ChessBoardColorsPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle("Colors");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		ColorFieldEditor defaultMessages = new ColorFieldEditor(
				PreferenceKeys.BOARD_BACKGROUND_COLOR, "Background Color:",
				getFieldEditorParent());
		addField(defaultMessages);

		ColorFieldEditor pieceJailBackground = new ColorFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR,
				"Piece Jail/Drop Square Background Color:",
				getFieldEditorParent());
		addField(pieceJailBackground);

		ColorFieldEditor coordinatesColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_COLOR, "Coordinates Color:",
				getFieldEditorParent());
		addField(coordinatesColor);

		ColorFieldEditor highlightColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_HIGHLIGHT_COLOR, "Highlight Color:",
				getFieldEditorParent());
		addField(highlightColor);

		ColorFieldEditor lagLabelColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_LAG_COLOR, "Lag Color:",
				getFieldEditorParent());
		addField(lagLabelColor);

		ColorFieldEditor lagOver20LabelColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR,
				"Lag Over 20 Seconds Color:", getFieldEditorParent());
		addField(lagOver20LabelColor);

		ColorFieldEditor playerNameRatingColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_PLAYER_NAME_COLOR, "Name/Rating Color:",
				getFieldEditorParent());
		addField(playerNameRatingColor);

		ColorFieldEditor gameDescriptionColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_GAME_DESCRIPTION_COLOR,
				"Game Type Color:", getFieldEditorParent());
		addField(gameDescriptionColor);

		ColorFieldEditor openingDescription = new ColorFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_COLOR, "Opening Color:",
				getFieldEditorParent());
		addField(openingDescription);

		ColorFieldEditor premovesColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_PREMOVES_COLOR, "Premoves Color:",
				getFieldEditorParent());
		addField(premovesColor);

		ColorFieldEditor lastMoveColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_STATUS_COLOR, "Last Move Color:",
				getFieldEditorParent());
		addField(lastMoveColor);

		ColorFieldEditor resultColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_RESULT_COLOR, "Result Color:",
				getFieldEditorParent());
		addField(resultColor);

		ColorFieldEditor jailLabelColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_COLOR,
				"Piece Jail/Drop Square Number Color:", getFieldEditorParent());
		addField(jailLabelColor);
	}
}