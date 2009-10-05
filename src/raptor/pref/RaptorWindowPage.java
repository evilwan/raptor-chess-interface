package raptor.pref;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;

public class RaptorWindowPage extends FieldEditorPreferencePage {
	public RaptorWindowPage() {
		super(FLAT);
		setTitle("Raptor Window");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {

		String[][] sliderWidthPreferences = { { "Tiny", "3" },
				{ "Small", "5" }, { "Medium", "8" }, { "Large", "11" },
				{ "Extra Wide", "15" } };
		ComboFieldEditor setFieldEditor = new ComboFieldEditor(
				PreferenceKeys.APP_SASH_WIDTH, "Window Area Adjuster Width:",
				sliderWidthPreferences, getFieldEditorParent());
		addField(setFieldEditor);

		ColorFieldEditor pingTimeColor = new ColorFieldEditor(
				PreferenceKeys.APP_PING_COLOR, "Ping Time Font Color",
				getFieldEditorParent());
		addField(pingTimeColor);

		addField(pingTimeColor);
		FontFieldEditor pingTimeFont = new FontFieldEditor(
				PreferenceKeys.APP_PING_FONT, "Ping Time Font",
				getFieldEditorParent());
		addField(pingTimeFont);

		ColorFieldEditor statusBarFontColor = new ColorFieldEditor(
				PreferenceKeys.APP_STATUS_BAR_COLOR, "Status Bar Font Color",
				getFieldEditorParent());
		addField(statusBarFontColor);

		FontFieldEditor statusBarFont = new FontFieldEditor(
				PreferenceKeys.APP_STATUS_BAR_FONT, "Status Bar Font",
				getFieldEditorParent());
		addField(statusBarFont);

	}
}