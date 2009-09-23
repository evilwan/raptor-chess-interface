package raptor.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;

public class FicsPage extends FieldEditorPreferencePage {
	public FicsPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Fics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceKeys.FICS_AUTO_CONNECT, "Auto Connect",
				getFieldEditorParent());
		addField(bfe);
		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				PreferenceKeys.FICS_KEEP_ALIVE, "Auto Keep Alive",
				getFieldEditorParent());
		addField(bfe2);
		BooleanFieldEditor bfe3 = new BooleanFieldEditor(
				PreferenceKeys.FICS_IS_LOGGING_GAMES,
				"Log games in pgn format", getFieldEditorParent());
		addField(bfe3);

		TextFieldEditor tfe = new TextFieldEditor(
				PreferenceKeys.FICS_LOGIN_SCRIPT, "Login Script",
				getFieldEditorParent());
		addField(tfe);
	}
}