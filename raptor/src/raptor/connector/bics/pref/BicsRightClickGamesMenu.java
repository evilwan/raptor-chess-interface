package raptor.connector.bics.pref;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelFieldEditor;
import raptor.pref.fields.ListFieldEditor;

public class BicsRightClickGamesMenu extends FieldEditorPreferencePage {
	public BicsRightClickGamesMenu() {
		super(FLAT);
		setTitle("Games Popup Menu");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(
				"none",
				WordUtils
						.wrap(
								"You can use $gameId for the game id right clicked on, and $userName for the logged in user name in the scripts below.",
								70)
						+ "\n ", getFieldEditorParent()));
		
		addField(new ListFieldEditor(PreferenceKeys.FICS_GAME_COMMANDS,
				"Right Click Game Commands:", getFieldEditorParent(), ',', 75));
	}
}