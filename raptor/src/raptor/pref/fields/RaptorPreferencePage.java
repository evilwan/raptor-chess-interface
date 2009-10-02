package raptor.pref.fields;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;

import raptor.pref.RaptorPreferenceStore;

public abstract class RaptorPreferencePage extends PreferencePage {

	List<RaptorPreferenceField> fields = new ArrayList<RaptorPreferenceField>(
			10);

	public void addField(RaptorPreferenceField field) {
		fields.add(field);
	}

	public RaptorPreferenceStore getRaptorPrefs() {
		return (RaptorPreferenceStore) getPreferenceStore();
	}

	@Override
	protected void performApply() {
		for (RaptorPreferenceField field : fields) {
			field.apply((RaptorPreferenceStore) getPreferenceStore());
		}
	}

	@Override
	public boolean performCancel() {
		boolean result = true;
		for (RaptorPreferenceField field : fields) {
			if (!field.cancel((RaptorPreferenceStore) getPreferenceStore())) {
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	protected void performDefaults() {
		for (RaptorPreferenceField field : fields) {
			field.loadDefault((RaptorPreferenceStore) getPreferenceStore());
		}
	}

	@Override
	public boolean performOk() {
		boolean result = true;
		for (RaptorPreferenceField field : fields) {
			if (!field.apply((RaptorPreferenceStore) getPreferenceStore())) {
				result = false;
				break;
			}
		}
		return result;
	}

}
