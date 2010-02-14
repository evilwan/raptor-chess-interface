package raptor.pref.page;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.service.SeekService.SeekType;

public class SeekPage extends FieldEditorPreferencePage {

	public static final String[][] SEEK_OUTPUT_TYPES = {
			{ "Show all seeks", SeekType.AllSeeks.toString() },
			{ "Show formula filtered seeks",
					SeekType.FormulaFiltered.toString() } };

	public SeekPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle("Seeks");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(PreferenceKeys.SEEK_OUTPUT_TYPE, "Seek output type:",
				SEEK_OUTPUT_TYPES, getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_COMPUTER_COLOR,
				"Seek Graph Computer Color:", getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_MANY_COLOR,
				"Seek Graph Many Color:", getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_RATED_COLOR,
				"Seek Graph Rated Color:", getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_UNRATED_COLOR,
				"Seek Graph Unrated Color:", getFieldEditorParent()));
	}

}
