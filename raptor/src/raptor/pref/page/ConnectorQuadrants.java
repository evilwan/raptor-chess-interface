package raptor.pref.page;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chess.ChessBoardWindowItem;

public class ConnectorQuadrants extends FieldEditorPreferencePage {
	protected String connectorShortName;

	public ConnectorQuadrants(String connectorShortName) {
		super(FLAT);
		setTitle(connectorShortName + " Quadrants");
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.connectorShortName = connectorShortName;
	}

	protected String[][] buildQuadrantArray(Quadrant[] quadrants) {
		String[][] result = new String[quadrants.length][2];
		for (int i = 0; i < quadrants.length; i++) {
			result[i][0] = quadrants[i].name();
			result[i][1] = quadrants[i].name();
		}
		return result;
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.CHESS_BOARD_QUADRANT, "Chess Board Quadrant:",
				buildQuadrantArray(ChessBoardWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUGHOUSE_GAME_2_QUADRANT,
				"Bughouse Board 2 Quadrant:",
				buildQuadrantArray(ChessBoardWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUG_BUTTONS_QUADRANT,
				"BughouseButtons Quadrant:",
				buildQuadrantArray(BugButtonsWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.MAIN_TAB_QUADRANT, "Main Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.MAIN_TAB_QUADRANT, "Main Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.CHANNEL_TAB_QUADRANT,
				"Channel Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PERSON_TAB_QUADRANT,
				"Person Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				"Bughouse Partner Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.REGEX_TAB_QUADRANT,
				"Regular Expression Console Quadrant:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));
	}
}
