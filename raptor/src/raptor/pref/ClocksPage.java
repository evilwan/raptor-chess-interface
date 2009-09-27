package raptor.pref;

//import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;

import raptor.Raptor;

public class ClocksPage extends FieldEditorPreferencePage {
	public ClocksPage() {
		// Use the "flat" layout
		super(FLAT);
		setTitle("Clock Preferences");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {

		/*
		 * RadioGroupFieldEditor(String name, String labelText, int numColumns,
		 * String[][] labelAndValues, Composite parent) Creates a radio group
		 * field editor. RadioGroupFieldEditor(String name, String labelText,
		 * int numColumns, String[][] labelAndValues, Composite parent, boolean
		 * useGroup) Creates a radio group field editor.
		 */

		/*
		 * IntegerFieldEditor(String name, String labelText, Composite parent)
		 * Creates an integer field editor.
		 */

		IntegerFieldEditor r1 = new IntegerFieldEditor("seconds",
				"Show Seconds After (Number of Minutes): ",
				getFieldEditorParent());
		addField(r1);

		String[][] showMS = { { "At 10 Seconds", "10" },
				{ "At 1 Minute", "60" }, { "Always", "9999999999" } };
		RadioGroupFieldEditor r2 = new RadioGroupFieldEditor("milliseconds",
				"Show Milliseconds:", 3, showMS, getFieldEditorParent());
		addField(r2);

		/*
		 * BooleanFieldEditor bfe2 = new BooleanFieldEditor(
		 * PreferenceKeys.FICS_IS_ANON_GUEST, "Anon Guest",
		 * getFieldEditorParent()); addField(bfe2);
		 */
	}
}