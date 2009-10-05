package raptor.connector.fics.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.TextFieldEditor;

public class FicsPage extends FieldEditorPreferencePage {
	public FicsPage() {
		super(FLAT);
		setTitle("Fics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				PreferenceKeys.FICS_KEEP_ALIVE,
				"Keep Alive (Sends date to stop the 60 minute idle kick out)",
				getFieldEditorParent());
		addField(bfe2);

		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceKeys.FICS_AUTO_CONNECT, "Auto Connect",
				getFieldEditorParent());
		addField(bfe);

		addField(new BooleanFieldEditor(PreferenceKeys.FICS_IS_LOGGING_GAMES,
				"Log games in PGN (Comming Soon)", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_FREECHESS_ORG_URL,
				"Fics url:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_COMMANDS_HELP_URL,
				"Fics Commands Help url:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_ADJUDICATE_URL,
				"Ajudicate url:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_TEAM_LEAGUE_URL,
				"Team League url:", getFieldEditorParent()));

		addField(new StringFieldEditor(PreferenceKeys.FICS_FICS_GAMES_URL,
				"Fics Games url:", getFieldEditorParent()));

		addField(new TextFieldEditor(PreferenceKeys.FICS_LOGIN_SCRIPT,
				"Login Script:", getFieldEditorParent()));
	}
}