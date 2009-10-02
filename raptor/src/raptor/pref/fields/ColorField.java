package raptor.pref.fields;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.pref.RaptorPreferenceStore;

public class ColorField extends Composite implements RaptorPreferenceField {
	ColorSelector button;
	RaptorPreferencePage page;
	String preferenceKey;
	RGB initialColor;

	public ColorField(Composite parent, String preferenceKey, String labelText,
			RaptorPreferencePage page) {
		super(parent, SWT.NONE);
		this.page = page;
		this.preferenceKey = preferenceKey;

		setLayout(new GridLayout(2, false));
		Label label = new Label(this, SWT.NONE);
		label.setText(labelText);

		button = new ColorSelector(parent);
		button.setColorValue(page.getRaptorPrefs().getColor(preferenceKey)
				.getRGB());
		initialColor = button.getColorValue();
	}

	public boolean apply(RaptorPreferenceStore store) {
		page.getRaptorPrefs().setValue(preferenceKey, button.getColorValue());
		return true;
	}

	public boolean cancel(RaptorPreferenceStore store) {
		page.getRaptorPrefs().setValue(preferenceKey, initialColor);
		return true;
	}

	public void loadDefault(RaptorPreferenceStore store) {
		initialColor = page.getRaptorPrefs().getDefaultColor(preferenceKey);
		button.setColorValue(initialColor);
	}
}
