package raptor.pref.page;

import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.fields.LabelButtonFieldEditor;

public class ChessBoardToolbarsPage extends FieldEditorPreferencePage {
	LabelButtonFieldEditor labelButtonFieldEditor;

	public ChessBoardToolbarsPage() {
		super(GRID);
		setTitle("Toolbars");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
	}
}