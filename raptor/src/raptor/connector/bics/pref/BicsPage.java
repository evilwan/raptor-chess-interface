package raptor.connector.bics.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.TextFieldEditor;

public class BicsPage extends FieldEditorPreferencePage {
	public BicsPage() {
		super(FLAT);
		setTitle("Bics");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceKeys.BICS_KEEP_ALIVE,
				"Keep Alive (Sends date to stop the 60 minute idle kick out)",
				getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.BICS_AUTO_CONNECT,
				"Auto Connect", getFieldEditorParent()));

		addField(new BooleanFieldEditor(PreferenceKeys.BICS_IS_LOGGING_GAMES,
				"Log games in PGN (Comming Soon)", getFieldEditorParent()));

		addField(new TextFieldEditor(PreferenceKeys.BICS_LOGIN_SCRIPT,
				"Login Script:", getFieldEditorParent()));
	}
}