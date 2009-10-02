package raptor.pref.fields;

public abstract class FontField {// extends Composite implements
	// RaptorPreferenceField {
	// RaptorPreferencePage page;
	// String fontPreferenceKey;
	// String colorPreferenceKey;
	// Font initialFont;
	// Color initialColor;
	//
	// public FontField(Composite parent, String fontPreferenceKey,
	// String labelText, RaptorPreferencePage page) {
	// this(parent, fontPreferenceKey, null, labelText, page);
	// }
	//
	// public FontField(Composite parent, String fontPreferenceKey,String
	// colorPreferenceKey, String labelText,
	// RaptorPreferencePage page) {
	// super(parent, SWT.NONE);
	// this.page = page;
	// this.fontPreferenceKey = fontPreferenceKey;
	// this.colorPreferenceKey = colorPreferenceKey;
	//
	// setLayout(new GridLayout(3, false));
	// Label label = new Label(this, SWT.NONE);
	// label.setText(labelText);
	// initialFont = page.getRaptorPrefs().getFont(fontPreferenceKey);
	//
	// Label sampleText = new Label(this, SWT.NONE);
	// sampleText.setText("name: " + initialFont.getFontData()[0].getName() +
	// " height: "
	// + initialFont.getFontData()[0].getHeight());
	// sampleText.setForeground(initialColor =
	// page.getRaptorPrefs().getColor(colorPreferenceKey));
	// sampleText.setFont(initialFont);
	//
	// Button button = new Button(this, SWT.PUSH);
	// button.setText("Change");
	// button.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// FontDialog dialog = new FontDialog(getShell());
	// dialog.setFontList(initialFont.getFontData());
	// dialog.setRGB(initialColor.getRGB());
	// dialog.open();
	// Raptor.getInstance().getColorRegistry().
	// initialColor = dialog.getRGB()
	// }
	// });
	// }
	//
	// public boolean apply(RaptorPreferenceStore store) {
	// page.getRaptorPrefs().setValue(preferenceKey, button.getColorValue());
	// return true;
	// }
	//
	// public boolean cancel(RaptorPreferenceStore store) {
	// page.getRaptorPrefs().setValue(preferenceKey, initialColor);
	// return true;
	// }
	//
	// public void loadDefault(RaptorPreferenceStore store) {
	// initialColor = page.getRaptorPrefs().getDefaultColor(preferenceKey);
	// button.setColorValue(initialColor);
	// }
}
