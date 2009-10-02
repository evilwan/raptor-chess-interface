package raptor.pref;

import org.eclipse.jface.preference.PreferencePage;

public abstract class RaptorPage extends PreferencePage {
	// public RaptorPage() {
	// // Use the "flat" layout
	// super(FLAT);
	// setTitle("Raptor");
	// setPreferenceStore(Raptor.getInstance().getPreferences());
	// this.
	// }
	//	
	// String homePage;
	// Button soundEnabled;
	//	
	//
	// @Override
	// protected Composite createContents(Composite parent) {
	//
	// Composite composite = new Composite(parent, SWT.NONE);
	// composite.setLayout(new GridLayout(2,false));
	// StringFieldEditor homePage = new StringFieldEditor(
	// PreferenceKeys.APP_HOME_URL, "Startup Url:", composite);
	// homePage.getTextControl(homePage)setLayoutData(new
	// GridData(SWT.BEGINNING,SWT.CENTER,false,false,2,1));
	// addField(homePage);
	// get
	//
	// BooleanFieldEditor isPlayingSound = new BooleanFieldEditor(
	// PreferenceKeys.APP_SOUND_ENABLED, "Sound Enabled",
	// getFieldEditorParent());
	// addField(isPlayingSound);
	//		
	// ColorFieldEditor pingTimeColor = new ColorFieldEditor(
	// PreferenceKeys.APP_PING_COLOR, "Ping Time Font Color",
	// getFieldEditorParent());
	//		
	// addField(pingTimeColor);
	// FontFieldEditor pingTimeFont = new FontFieldEditor(
	// PreferenceKeys.APP_PING_FONT, "Ping Time Font",
	// getFieldEditorParent());
	// addField(pingTimeFont);
	//
	// ColorFieldEditor statusBarFontColor = new ColorFieldEditor(
	// PreferenceKeys.APP_STATUS_BAR_COLOR, "Status Bar Font Color",
	// getFieldEditorParent());
	// addField(statusBarFontColor);
	// FontFieldEditor statusBarFont = new FontFieldEditor(
	// PreferenceKeys.APP_STATUS_BAR_FONT, "Status Bar Font",
	// getFieldEditorParent());
	// addField(statusBarFont);
	//
	// Label label = new Label(getFieldEditorParent(), SWT.NONE);
	// label.setText("Raptor user's home directory: "
	// + Raptor.USER_RAPTOR_HOME_PATH);)
	//
	// File file = new File(Raptor.USER_RAPTOR_DIR + "/imagecache");
	// final File[] files = file.listFiles(new FilenameFilter() {
	//
	// public boolean accept(File dir, String name) {
	// return name.endsWith(".png");
	// }
	// });
	//
	// long size = 0;
	// if (files != null) {
	// for (File currentFile : files) {
	// size += currentFile.length();
	// }
	// }
	//
	// Composite composite1 = new Composite(getFieldEditorParent(), SWT.NONE);
	// composite1.setLayout(new RowLayout());
	// Label imageCacheSize = new Label(composite1, SWT.NONE);
	// imageCacheSize
	// .setText("Current image cache size: " + size / 1024 + "K");
	// Button clear = new Button(composite1, SWT.PUSH);
	// clear.setText("Clear Cache");
	// clear.addSelectionListener(new SelectionAdapter() {
	// @Override
	// public void widgetSelected(SelectionEvent e) {
	// for (File currentFile : files) {
	// currentFile.delete();
	// }
	// }
	// });
	// }
}