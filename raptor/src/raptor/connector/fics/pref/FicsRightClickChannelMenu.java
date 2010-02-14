package raptor.connector.fics.pref;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.fields.LabelFieldEditor;
import raptor.pref.fields.ListFieldEditor;

public class FicsRightClickChannelMenu extends FieldEditorPreferencePage {
	public FicsRightClickChannelMenu() {
		super(FLAT);
		setTitle("Channel Popup Menu");
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new LabelFieldEditor(
				"none",
				WordUtils
						.wrap(
								"You can use $channel for the channel number right clicked on, and $userName for the logged in user name in the scripts below.",
								70)
						+ "\n ", getFieldEditorParent()));

		addField(new ListFieldEditor(PreferenceKeys.FICS_CHANNEL_COMMANDS,
				"Right Click Channel Commands:", getFieldEditorParent(), ',',
				75));
	}
}
