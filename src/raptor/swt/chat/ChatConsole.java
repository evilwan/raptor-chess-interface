package raptor.swt.chat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.App;
import raptor.chat.ChatTypes;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;

public class ChatConsole extends Composite implements PreferenceKeys, ChatTypes {

	public static final String SCROLL_LOCK_ON_TOOLTIP = "Scroll Lock Enabled";
	public static final String SCROLL_LOCK_OFF_TOOLTIP = "Scroll Lock Disabled";
	public static final String SCROLL_LOCK_ON_ICON = "locked";
	public static final String SCROLL_LOCK_OFF_ICON = "unlocked";

	public static final String SEND_BUTTON = "send";
	public static final String SCROLL_LOCK_BUTTON = "scrollLock";
	public static final String SEARCH_BUTTON = "send";
	public static final String PREPEND_TEXT_BUTTON = "send";

	protected StyledText inputText;
	protected StyledText outputText;
	protected Composite buttonComposite;
	protected Composite southControlsComposite;
	protected Connector connector;
	protected RaptorPreferenceStore preferences;
	protected ChatConsoleController controller;
	protected Label promptLabel;
	protected Map<String, Button> buttonMap = new HashMap<String, Button>();

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};

	String title;

	public ChatConsole(Composite parent, int style) {
		super(parent, style);
	}

	protected void addButtons() {
		Button sendButton = new Button(buttonComposite, SWT.FLAT);
		sendButton.setImage(preferences.getIcon("southWest"));
		sendButton.setToolTipText("Sends the message in the input text area.");
		sendButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSendOutputText();
			}
		});
		buttonMap.put(SEND_BUTTON, sendButton);

		final Button scrollLockButton = new Button(buttonComposite, SWT.FLAT);
		scrollLockButton.setImage(preferences.getIcon("locked"));
		scrollLockButton.setToolTipText(SCROLL_LOCK_ON_TOOLTIP);
		scrollLockButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				controller
						.setScrollLockEnabled(scrollLockButton.getToolTipText()
								.equals(SCROLL_LOCK_ON_TOOLTIP) ? false : true);

			}
		});
		buttonMap.put(SCROLL_LOCK_BUTTON, scrollLockButton);

		if (controller.isSearchable()) {
			Button searchButton = new Button(buttonComposite, SWT.FLAT);
			searchButton.setImage(preferences.getIcon("search"));
			searchButton
					.setToolTipText("Searches backward for the message in the console text.");
			searchButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent arg0) {
					controller.onSearch();
				}
			});
			buttonMap.put(SEARCH_BUTTON, searchButton);
		}

		if (controller.isPrependable()) {
			Button prependText = new Button(buttonComposite, SWT.CHECK);
			prependText.setImage(preferences.getIcon("redStar"));
			prependText.setToolTipText("Prepends " + prependText
					+ " to all messages sent.");
			prependText.setSelection(true);
			buttonMap.put(PREPEND_TEXT_BUTTON, prependText);
		}
	}

	public void dispose() {
		App.getInstance().getPreferences().removePropertyChangeListener(
				propertyChangeListener);
		if (controller != null) {
			controller.dispose();
		}
		buttonMap.clear();
		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	public ChatConsoleController getController() {
		return controller;
	}

	public Button getButton(String button) {
		return buttonMap.get(button);
	}

	public RaptorPreferenceStore getPreferences() {
		return preferences;
	}

	public String getTitle() {
		return title;
	}

	public void createControls() {

		setLayout(new GridLayout(1, true));

		App.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);
		inputText = new StyledText(this, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI);
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setEditable(false);

		southControlsComposite = new Composite(this, SWT.NONE);
		southControlsComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginRight = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginTop = 0;
		southControlsComposite.setLayout(gridLayout);

		promptLabel = new Label(southControlsComposite, SWT.NONE);
		promptLabel.setText(controller.getPrompt());

		outputText = new StyledText(southControlsComposite, SWT.SINGLE);
		outputText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		buttonComposite = new Composite(southControlsComposite, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginBottom = 0;
		rowLayout.marginHeight = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.pack = false;
		rowLayout.wrap = false;
		buttonComposite.setLayout(rowLayout);
		addButtons();

		updateFromPrefs();
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public void setController(ChatConsoleController controller) {
		this.controller = controller;
	}

	public void setButtonEnabled(boolean isEnabled, String key) {
		Button item = buttonMap.get(key);
		if (item != null) {
			item.setEnabled(isEnabled);
		}
	}

	public void setPreferences(RaptorPreferenceStore preferences) {
		this.preferences = preferences;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore prefs = App.getInstance().getPreferences();
		Color consoleBackground = prefs.getColor(CHAT_CONSOLE_BACKGROUND_COLOR);
		
		buttonComposite.setBackground(consoleBackground);
		southControlsComposite.setBackground(consoleBackground);
		setBackground(consoleBackground);

		inputText.setBackground(prefs.getColor(CHAT_INPUT_BACKGROUND_COLOR));
		inputText.setForeground(prefs.getColor(CHAT_INPUT_DEFAULT_TEXT_COLOR));
		inputText.setFont(prefs.getFont(CHAT_INPUT_FONT));

		outputText.setBackground(prefs.getColor(CHAT_OUTPUT_BACKGROUND_COLOR));
		outputText.setForeground(prefs.getColor(CHAT_OUTPUT_TEXT_COLOR));
		outputText.setFont(prefs.getFont(CHAT_OUTPUT_FONT));

		promptLabel.setFont(prefs.getFont(CHAT_PROMPT_FONT));
		promptLabel.setForeground(prefs.getColor(CHAT_PROMPT_COLOR));
		promptLabel
				.setBackground(prefs.getColor(CHAT_CONSOLE_BACKGROUND_COLOR));
	}

	public StyledText getInputText() {
		return inputText;
	}

	public StyledText getOutputText() {
		return outputText;
	}

}
