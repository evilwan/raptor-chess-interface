package raptor.pref;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;

import raptor.Raptor;

public class FicsConnectionPage extends FieldEditorPreferencePage {
	public FicsConnectionPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Fics Connection");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor sfe = new StringFieldEditor(
				PreferenceKeys.FICS_USER_NAME, "User", getFieldEditorParent());
		addField(sfe);
		StringFieldEditor sfe2 = new StringFieldEditor(
				PreferenceKeys.FICS_USER_NAME, "Password",
				getFieldEditorParent());
		addField(sfe2);
		BooleanFieldEditor bfe = new BooleanFieldEditor(
				PreferenceKeys.FICS_IS_NAMED_GUEST, "Named Guest",
				getFieldEditorParent());
		addField(bfe);

		BooleanFieldEditor bfe2 = new BooleanFieldEditor(
				PreferenceKeys.FICS_IS_ANON_GUEST, "Anon Guest",
				getFieldEditorParent());
		addField(bfe2);
	}
}