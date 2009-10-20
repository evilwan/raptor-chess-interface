package raptor.pref.page;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
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
		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PTELL,
				"Play 'chat' sound on all partner tells.",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceKeys.CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL,
				"Play 'chat' sound on all person tells.",
				getFieldEditorParent()));
	}
}