package raptor.connector.ics.dialog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;

/**
 * The ics login dialog.
 */
public class IcsLoginDialog extends Dialog implements PreferenceKeys {

	private static final Log LOG = LogFactory.getLog(IcsLoginDialog.class);
	protected Combo profile;
	protected Label profileLabel;
	protected Label handleLabel;
	protected Label passwordLabel;
	protected Label serverLabel;
	protected Label portLabel;
	protected Text handleField;
	protected Text serverField;
	protected Text portField;
	protected Button autoLoginCheckBox;
	protected Button guestLoginCheckBox;
	protected Button timesealEnabledCheckBox;
	protected Text passwordField;
	protected Button loginButton;
	protected String lastSelection;

	protected boolean wasLoginPressed;
	protected String profilePrefix;
	protected String title;

	public IcsLoginDialog(String profilePrefix, String title) {
		super(Raptor.getInstance().getRaptorWindow().getShell());
		this.profilePrefix = profilePrefix;
		this.title = title;
	}

	protected void adjustToCheckBoxControls() {
		if (guestLoginCheckBox.getSelection()) {
			passwordField.setText("");
			passwordLabel.setEnabled(false);
			passwordField.setEnabled(false);
			handleLabel.setEnabled(true);
			handleField.setEnabled(true);
		} else {
			passwordLabel.setEnabled(true);
			passwordField.setEnabled(true);
			handleLabel.setEnabled(true);
			handleField.setEnabled(true);
		}
	}

	@Override
	public Composite createContents(Composite parent) {
		getShell().setText(title);

		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		profileLabel = new Label(content, SWT.NONE);
		profileLabel.setText("Profile:");
		profile = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
		profile.add("Primary");
		profile.add("Secondary");
		profile.add("Tertiary");

		profile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String itemSelected = profile.getItem(profile
						.getSelectionIndex());
				storeProfile(lastSelection);
				loadFromProfile(itemSelected);
				lastSelection = itemSelected;
			}
		});

		handleLabel = new Label(content, SWT.NONE);
		handleLabel.setText("Login:");
		handleField = new Text(content, SWT.BORDER);
		handleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		passwordLabel = new Label(content, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordField = new Text(content, SWT.BORDER);
		passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		serverLabel = new Label(content, SWT.NONE);
		serverLabel.setText("Server:");
		serverField = new Text(content, SWT.BORDER);
		serverField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		portLabel = new Label(content, SWT.NONE);
		portLabel.setText("Port:");
		portField = new Text(content, SWT.BORDER);
		portField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		guestLoginCheckBox = new Button(content, SWT.CHECK);
		guestLoginCheckBox.setText("Login as guest");
		GridData data = new GridData();
		data.horizontalSpan = 2;
		guestLoginCheckBox.setLayoutData(data);

		timesealEnabledCheckBox = new Button(content, SWT.CHECK);
		timesealEnabledCheckBox.setText("Timeseal");
		data = new GridData();
		data.horizontalSpan = 2;
		timesealEnabledCheckBox.setLayoutData(data);

		autoLoginCheckBox = new Button(content, SWT.CHECK);
		autoLoginCheckBox.setText("Auto log me in next time.");
		data = new GridData();
		data.horizontalSpan = 2;
		autoLoginCheckBox.setLayoutData(data);

		loginButton = new Button(content, SWT.PUSH);
		loginButton.setText("Login");
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.CENTER;
		loginButton.setLayoutData(data);

		SelectionListener selectionListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (e.widget == guestLoginCheckBox
						|| e.widget == timesealEnabledCheckBox) {
					adjustToCheckBoxControls();
				} else if (e.widget == loginButton) {
					String handleText = handleField.getText();
					String passwordText = passwordField.getText();

					if (StringUtils.isNotEmpty(handleText)
							&& (handleText.length() < 3 || handleText.length() > 17)) {
						MessageBox box = new MessageBox(content.getShell(),
								SWT.ERROR | SWT.OK);
						box
								.setMessage("Login must be between 3 and 17 chracters.");
						box.setText("Invalid Login.");
						box.open();
					} else if (StringUtils.isNotBlank(handleText)
							&& !handleText.matches("[a-zA-Z]*")) {
						MessageBox box = new MessageBox(content.getShell(),
								SWT.ERROR | SWT.OK);
						box.setMessage("Handle must contain only letters.");
						box.setText("Invalid Login.");
						box.open();
					} else if (!guestLoginCheckBox.getSelection()
							&& StringUtils.isBlank(handleText)) {
						MessageBox box = new MessageBox(content.getShell(),
								SWT.ERROR | SWT.OK);
						box
								.setMessage("You must enter a handle if you are not logging in as a guest.");
						box.setText("Invalid Login.");
						box.open();
					} else if (!guestLoginCheckBox.getSelection()
							&& StringUtils.isBlank(passwordText)) {
						MessageBox box = new MessageBox(content.getShell(),
								SWT.ERROR | SWT.OK);
						box
								.setMessage("You must enter a password if you are not logging in as a guest.");
						box.setText("Invalid Password.");
						box.open();
					} else {
						String itemSelected = profile.getItem(profile
								.getSelectionIndex());
						lastSelection = itemSelected;
						storeProfile(itemSelected);
						try {
							Raptor.getInstance().getPreferences().save();
						} catch (Throwable t) {
						}
						wasLoginPressed = true;
						close();
					}
				}
			}
		};

		loginButton.addSelectionListener(selectionListener);
		guestLoginCheckBox.addSelectionListener(selectionListener);

		String currentProfile = Raptor.getInstance().getPreferences()
				.getString(profilePrefix + "profile");
		lastSelection = currentProfile;
		loadFromProfile(currentProfile);

		int selectedIndex = 0;
		for (int i = 0; i < profile.getItems().length; i++) {
			if (StringUtils.equals(profile.getItem(i), currentProfile)) {
				selectedIndex = i;
				break;
			}
		}
		profile.select(selectedIndex);
		adjustToCheckBoxControls();

		content.pack();
		parent.pack();
		return content;
	}

	public String getSelectedProfile() {
		return lastSelection;
	}

	@Override
	protected void initializeBounds() {
		super.initializeBounds();
		Shell shell = this.getShell();
		Monitor primary = shell.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	protected void loadFromProfile(String profileName) {
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();
		String prefix = profilePrefix + profileName + "-";
		handleField.setText(prefs.getString(prefix + "user-name"));
		serverField.setText(prefs.getString(prefix + "server-url"));
		portField.setText(prefs.getString(prefix + "port"));
		passwordField.setText(prefs.getString(prefix + "password"));
		guestLoginCheckBox.setSelection(prefs.getBoolean(prefix
				+ "is-anon-guest")
				|| prefs.getBoolean(prefix + "is-named-guest"));
		timesealEnabledCheckBox.setSelection(prefs.getBoolean(prefix
				+ "timeseal-enabled"));
		autoLoginCheckBox.setSelection(prefs.getBoolean(profilePrefix
				+ "auto-connect"));
		LOG.info("Loaded loadFromProfile " + profileName);
		adjustToCheckBoxControls();
	}

	@Override
	public int open() {
		return super.open();
	}

	protected void storeProfile(String profileName) {
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();
		String prefix = profilePrefix + profileName + "-";

		prefs.setValue(prefix + "user-name", StringUtils
				.defaultString(handleField.getText()));
		prefs.setValue(prefix + "server-url", StringUtils
				.defaultString(serverField.getText()));
		prefs.setValue(prefix + "password", StringUtils
				.defaultString(passwordField.getText()));

		try {
			prefs.setValue(prefix + "port", Integer.parseInt(StringUtils
					.defaultString(portField.getText())));
		} catch (NumberFormatException nfe) {
		}

		prefs.setValue(prefix + "is-named-guest", guestLoginCheckBox
				.getSelection()
				&& StringUtils.isNotBlank(handleField.getText()));
		prefs.setValue(prefix + "is-anon-guest", guestLoginCheckBox
				.getSelection()
				&& StringUtils.isBlank(handleField.getText()));
		prefs.setValue(prefix + "timeseal-enabled", timesealEnabledCheckBox
				.getSelection());
		prefs.setValue(profilePrefix + "auto-connect", autoLoginCheckBox
				.getSelection());

		System.err.println("Stored " + prefix + " values");

		// Don't store off the the profileName-profile here.
		// It should'nt be stored for fics2/bics2 logins.
		prefs.save();
		LOG.info("Saved " + profileName);
	}

	public boolean wasLoginPressed() {
		return wasLoginPressed;
	}
}
