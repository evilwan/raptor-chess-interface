package raptor.pref.fields;

import raptor.pref.RaptorPreferenceStore;

public interface RaptorPreferenceField {
	public boolean apply(RaptorPreferenceStore store);

	public boolean cancel(RaptorPreferenceStore store);

	public void loadDefault(RaptorPreferenceStore store);
}
