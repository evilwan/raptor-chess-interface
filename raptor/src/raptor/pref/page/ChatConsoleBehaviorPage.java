package raptor.pref.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsoleBehaviorPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChatConsoleBehaviorPage() {
		super(GRID);
		setTitle("Behavior");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {

	}
}