package raptor.swt.chat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.RaptorStyledText;

public class ChatConsole extends Composite implements PreferenceKeys {

	static final Log LOG = LogFactory.getLog(ChatConsole.class);

	protected StyledText inputText;
	protected StyledText outputText;
	protected Composite buttonComposite;
	protected Composite southControlsComposite;
	protected ChatConsoleController controller;
	protected Label promptLabel;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};

	public ChatConsole(Composite parent, int style) {
		super(parent, style);
	}

	protected void addButtons() {
		Button sendButton = new Button(buttonComposite, SWT.FLAT);
		sendButton.setImage(Raptor.getInstance().getIcon("enter"));
		sendButton.setToolTipText("Sends the message in the input field.");
		sendButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				controller.onSendOutputText();
			}
		});
	}

	public void createControls() {
		setLayout(new GridLayout(1, true));

		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);
		inputText = new RaptorStyledText(this, SWT.V_SCROLL | SWT.MULTI
				| SWT.BORDER);
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setEditable(false);
		inputText.setWordWrap(true);

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

		outputText = new RaptorStyledText(southControlsComposite, SWT.SINGLE
				| SWT.BORDER) {

		};
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

	@Override
	public void dispose() {
		if (propertyChangeListener != null) {
			Raptor.getInstance().getPreferences().removePropertyChangeListener(
					propertyChangeListener);
			propertyChangeListener = null;
		}
		if (controller != null) {
			controller.dispose();
		}

		LOG.info("Disposed chat console.");

		if (!isDisposed()) {
			super.dispose();
		}
	}

	public ChatConsoleController getController() {
		return controller;
	}

	public StyledText getInputText() {
		return inputText;
	}

	public StyledText getOutputText() {
		return outputText;
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	public void setController(ChatConsoleController controller) {
		this.controller = controller;
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore prefs = getPreferences();
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

}
