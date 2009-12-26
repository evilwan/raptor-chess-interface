package raptor.pref.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ScriptsPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public ScriptsPage() {
		super(FLAT);
		setTitle("Scripts");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
	}
}