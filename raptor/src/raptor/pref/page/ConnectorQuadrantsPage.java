package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.BugGamesWindowItem;
import raptor.swt.SeekTableWindowItem;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chess.ChessBoardWindowItem;

public class ConnectorQuadrantsPage extends FieldEditorPreferencePage {
	protected String connectorShortName;

	public ConnectorQuadrantsPage(String connectorShortName) {
		super(GRID);
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
		Label textLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\tRaptor uses a quadrant system to layout content. "
										+ "If a quadrant contains no items, the quadrant disappears and the "
										+ "remaining quadrants consume the space."
										+ "You may drag and drop items between quadrants by dragging "
										+ "the tab and dropping anywhere in another quadrant."
										+ "Double click on a tab to maximize a quadrant, and double "
										+ "click again to restore it. Right clicking on a tab brings up a "
										+ "list of options as well.", 70)
						+ "\n\t"
						+ WordUtils.wrap(
								"On this page you can customize the quadrant the following content "
										+ "is originally created in.", 70));

		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));
		label.setImage(Raptor.getInstance().getImage(
				Raptor.RESOURCES_DIR + "/images/quadrants.png"));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.CHESS_BOARD_QUADRANT, "Chess Board:",
				buildQuadrantArray(ChessBoardWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUGHOUSE_GAME_2_QUADRANT, "Bughouse Board 2:",
				buildQuadrantArray(ChessBoardWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUG_BUTTONS_QUADRANT, "BughouseButtons:",
				buildQuadrantArray(BugButtonsWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.MAIN_TAB_QUADRANT, "Main Console:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.CHANNEL_TAB_QUADRANT, "Channel Console:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PERSON_TAB_QUADRANT, "Person Console:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				"Bughouse Partner Console:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.REGEX_TAB_QUADRANT,
				"Regular Expression Console:",
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUG_WHO_QUADRANT,
				"Bug Who (Partners,Teams,Games):",
				buildQuadrantArray(BugGamesWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.SEEK_TABLE_QUADRANT, "Seek Table:",
				buildQuadrantArray(SeekTableWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));
	}
}
