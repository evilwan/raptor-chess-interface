package raptor.pref.page;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.SeekService.SeekType;

public class SeekPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();

	public static final String[][] SEEK_OUTPUT_TYPES = {
			{ local.getString("showAllSeeks"), SeekType.AllSeeks.toString() },
			{ local.getString("showFormula"),
					SeekType.FormulaFiltered.toString() } };

	public SeekPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle(local.getString("seeks"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new ComboFieldEditor(PreferenceKeys.SEEK_OUTPUT_TYPE, local.getString("seekOutType"),
				SEEK_OUTPUT_TYPES, getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_COMPUTER_COLOR,
				local.getString("seekGraphCompCol"), getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_MANY_COLOR,
				local.getString("seekGraphManyCol"), getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_RATED_COLOR,
				local.getString("seekGraphRatCol"), getFieldEditorParent()));

		addField(new ColorFieldEditor(PreferenceKeys.SEEK_GRAPH_UNRATED_COLOR,
				local.getString("seekGraphUnratCol"), getFieldEditorParent()));
	}

}
