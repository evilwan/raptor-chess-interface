package raptor.connector.bics.pref;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelFieldEditor;
import raptor.pref.fields.ListFieldEditor;

public class BicsRightClickPersonMenu extends FieldEditorPreferencePage {
	public BicsRightClickPersonMenu() {
		super(FLAT);
		setTitle("Person Popup Menu");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(
				"none",
				WordUtils
						.wrap(
								"You can use $person for the person right clicked on, and $userName for the logged in user name in the scripts below.",
								70)
						+ "\n ", getFieldEditorParent()));

		addField(new ListFieldEditor(PreferenceKeys.FICS_PERSON_QUICK_COMMANDS,
				"Quick Person Commands:", getFieldEditorParent(), ',', 75));

		addField(new ListFieldEditor(PreferenceKeys.FICS_PERSON_COMMANDS,
				"Other Person Commands:", getFieldEditorParent(), ',', 75));
	}
}
