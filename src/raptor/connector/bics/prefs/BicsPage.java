package raptor.connector.bics.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.TextFieldEditor;

public class BicsPage extends FieldEditorPreferencePage {
	public BicsPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Bics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceKeys.BICS_AUTO_CONNECT, "Auto Connect",
				getFieldEditorParent());
		addField(bfe);
		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				PreferenceKeys.BICS_KEEP_ALIVE, "Auto Keep Alive",
				getFieldEditorParent());
		addField(bfe2);
		BooleanFieldEditor bfe3 = new BooleanFieldEditor(
				PreferenceKeys.BICS_IS_LOGGING_GAMES,
				"Log games in pgn format", getFieldEditorParent());
		addField(bfe3);

		TextFieldEditor tfe = new TextFieldEditor(
				PreferenceKeys.BICS_LOGIN_SCRIPT, "Login Script",
				getFieldEditorParent());
		addField(tfe);
	}
}