package raptor.layout;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;

public abstract class AbstractLayout implements Layout {
	protected String pathToImage;
	protected String name;
	protected Map<String, String> preferenceAdjustments = new HashMap<String, String>();

	public AbstractLayout(String pathToImage, String name) {
		this.pathToImage = pathToImage;
		this.name = name;
		addAdjusmentsToClearConnectorLayoutInfo();
	}

	protected void addCrossConnectorSetting(String key, String value) {
		preferenceAdjustments.put("fics-" + key, value);
		preferenceAdjustments.put("fics2-" + key, value);
		preferenceAdjustments.put("bics-" + key, value);
		preferenceAdjustments.put("bics2-" + key, value);
	}

	protected void addAdjusmentsToClearConnectorLayoutInfo() {
		preferenceAdjustments.put("fics-Primary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("fics-Secondary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("fics-Tertiary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-Primary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-Secondary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-Tertiary-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
	}

	public String getPathToImage() {
		return pathToImage;
	}

	public void setPathToImage(String pathToImage) {
		this.pathToImage = pathToImage;
	}

	@Override
	public Image getImage() {
		if (pathToImage == null) {
			return null;
		} else {
			return Raptor.getInstance().getImage(pathToImage);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getPreferenceAdjustments() {
		return preferenceAdjustments;
	}

	public void setPreferenceAdjustments(
			Map<String, String> preferenceAdjustments) {
		this.preferenceAdjustments = preferenceAdjustments;
	}

	public void apply() {
		for (String key : preferenceAdjustments.keySet()) {
			String value = preferenceAdjustments.get(key);
			if (value == null) {
				Raptor.getInstance().getPreferences().setToDefault(key);
			} else {
				Raptor.getInstance().getPreferences().setValue(key,
						preferenceAdjustments.get(key));
			}
		}
		Raptor.getInstance().getWindow().resetLayout();
	}
}
