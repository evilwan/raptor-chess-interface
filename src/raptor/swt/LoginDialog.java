package raptor.swt;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;

public class LoginDialog extends Dialog implements PreferenceKeys {
	private static class Profile {
		public static Profile[] getProfiles() {
			Profile[] profiles = new Profile[3];

			profiles[0] = new Profile();
			profiles[0].setServerName("FICS");
			profiles[0].setServerAddress("freechess.org");
			profiles[0].setServerPort(5000);

			profiles[1] = new Profile();
			profiles[1].setServerName("BICS");
			profiles[1].setServerAddress("chess.sipay.ru");
			profiles[1].setServerPort(5000);

			profiles[2] = new Profile();
			profiles[2].setServerName("BICS Dev");
			profiles[2].setServerAddress("dev.chess.sipay.ru");
			profiles[2].setServerPort(5000);
			return profiles;
		}

		private String serverName = "";
		private String serverAddress = "";
		private int serverPort = 0;

		public String getServerAddress() {
			return serverAddress;
		}

		public String getServerName() {
			return serverName;
		}

		public int getServerPort() {
			return serverPort;
		}

		public void setServerAddress(String serverAddress) {
			this.serverAddress = serverAddress;
		}

		public void setServerName(String serverName) {
			this.serverName = serverName;
		}

		public void setServerPort(int serverPort) {
			this.serverPort = serverPort;
		}
	}

	private static final Log LOG = LogFactory.getLog(LoginDialog.class);
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

	protected boolean wasLoginPressed;

	public LoginDialog() {
		super(Raptor.getInstance().getAppWindow().getShell());
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
		getShell().setText("Raptor Login");

		final Composite content = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginWidth = 0;
		content.setLayout(new GridLayout(2, false));

		profileLabel = new Label(content, SWT.NONE);
		profileLabel.setText("Profile");
		profile = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
		// profile.add("FICS");

		Profile[] myProfiles = Profile.getProfiles();
		for (int i = 0; i < myProfiles.length; i++) {
			profile.add(myProfiles[i].getServerName());
		}
		profile.select(0);
		// profile.getSelectionIndex();

		handleLabel = new Label(content, SWT.NONE);
		handleLabel.setText("Login:");
		handleField = new Text(content, SWT.BORDER);
		handleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		handleField.setText(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.FICS_USER_NAME));

		passwordLabel = new Label(content, SWT.NONE);
		passwordLabel.setText("Password:");
		passwordField = new Text(content, SWT.BORDER);
		passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passwordField.setText(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.FICS_PASSWORD));

		serverLabel = new Label(content, SWT.NONE);
		serverLabel.setText("Server:");
		serverField = new Text(content, SWT.BORDER);
		serverField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverField.setText(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.FICS_SERVER_URL));

		portLabel = new Label(content, SWT.NONE);
		portLabel.setText("Port:");
		portField = new Text(content, SWT.BORDER);
		portField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portField.setText(Raptor.getInstance().getPreferences().getString(
				PreferenceKeys.FICS_PORT));

		guestLoginCheckBox = new Button(content, SWT.CHECK);
		guestLoginCheckBox.setText("Login as guest");
		GridData data = new GridData();
		data.horizontalSpan = 2;
		guestLoginCheckBox.setLayoutData(data);
		guestLoginCheckBox.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_IS_ANON_GUEST)
				|| Raptor.getInstance().getPreferences().getBoolean(
						FICS_IS_NAMED_GUEST));

		timesealEnabledCheckBox = new Button(content, SWT.CHECK);
		timesealEnabledCheckBox.setText("Timeseal");
		data = new GridData();
		data.horizontalSpan = 2;
		timesealEnabledCheckBox.setLayoutData(data);
		timesealEnabledCheckBox.setSelection(Raptor.getInstance()
				.getPreferences().getBoolean(FICS_TIMESEAL_ENABLED));

		autoLoginCheckBox = new Button(content, SWT.CHECK);
		autoLoginCheckBox.setText("Auto log me in next time.");
		data = new GridData();
		data.horizontalSpan = 2;
		autoLoginCheckBox.setLayoutData(data);
		autoLoginCheckBox.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(FICS_AUTO_CONNECT));

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
						saveOptions();
						wasLoginPressed = true;
						close();
					}
				}
			}
		};

		loginButton.addSelectionListener(selectionListener);
		guestLoginCheckBox.addSelectionListener(selectionListener);

		adjustToCheckBoxControls();

		content.pack();
		parent.pack();
		return content;
	}

	protected void saveOptions() {
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();

		prefs.setValue(PreferenceKeys.FICS_USER_NAME, handleField.getText());
		prefs.setValue(PreferenceKeys.FICS_PASSWORD, passwordField.getText());
		prefs.setValue(PreferenceKeys.FICS_SERVER_URL, serverField.getText());
		prefs.setValue(PreferenceKeys.FICS_PORT, portField.getText());
		prefs.setValue(PreferenceKeys.FICS_IS_NAMED_GUEST, guestLoginCheckBox
				.getSelection()
				&& StringUtils.isNotBlank(handleField.getText()));
		prefs.setValue(PreferenceKeys.FICS_IS_ANON_GUEST, guestLoginCheckBox
				.getSelection()
				&& StringUtils.isBlank(handleField.getText()));
		prefs.setValue(PreferenceKeys.FICS_TIMESEAL_ENABLED,
				timesealEnabledCheckBox.getSelection());
		prefs.setValue(PreferenceKeys.FICS_AUTO_CONNECT, autoLoginCheckBox
				.getSelection());

		try {
			prefs.save();
		} catch (IOException ioe) {
			LOG.error("Error occured saving preferences", ioe);
			throw new RuntimeException(ioe);
		}
	}

	public boolean wasLoginPressed() {
		return wasLoginPressed;
	}
}
