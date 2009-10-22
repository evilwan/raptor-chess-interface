package raptor.pref.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChatConsoleToolbarsPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChatConsoleToolbarsPage() {
		super(GRID);
		setTitle("Toolbars");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
	}
}