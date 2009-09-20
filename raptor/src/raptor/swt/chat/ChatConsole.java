package raptor.swt.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Label;

import raptor.App;
import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ChatService.ChatListener;

public class ChatConsole extends Composite implements PreferenceKeys, ChatTypes {
	private static final Log LOG = LogFactory.getLog(ChatConsole.class);
	public static final double CLEAN_PERCENTAGE = .33;

	protected String inboundRegex;
	protected int inboundType = -1;
	protected StyledText inputText;
	protected boolean isDirty;
	protected boolean isMainConsole;
	protected String outboundRegex;
	protected int outboundType = -1;
	protected StyledText outputText;
	protected ChatWindow chatWindow;
	protected Composite controlsComposite = null;
	protected CoolBar coolbar = null;
	protected Button scrollLockButton = null;
	protected Button removeButton = null;
	protected Label pingLabel = null;
	protected Connector connector;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};

	KeyListener outputKeyListener = new KeyListener() {
		public void keyPressed(KeyEvent arg0) {
		}

		public void keyReleased(KeyEvent arg0) {
			// System.err.println("Char released: " + arg0.character);
			if (arg0.character == '\r') {
				fireSendOutput();
			}
		}
	};

	KeyListener inputTextKeyListener = new KeyListener() {
		public void keyPressed(KeyEvent arg0) {
		}

		public void keyReleased(KeyEvent arg0) {
			// System.err.println("Char released: " + arg0.character);
			if (arg0.character == '\r') {
				fireSendOutput();
			} else {
				outputText.append("" + arg0.character);
				outputText.setSelection(new Point(outputText.getCharCount(),
						outputText.getCharCount()));
				outputText.forceFocus();
			}
		}
	};

	String title;

	public ChatConsole(Composite parent, int style, boolean isMainConsole,
			ChatWindow chatWindow, Connector connector) {
		super(parent, style);
		this.connector = connector;
		this.chatWindow = chatWindow;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		setLayout(gridLayout);

		App.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);
		inputText = new StyledText(this, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI);
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setEditable(false);
		inputText.addKeyListener(inputTextKeyListener);

		outputText = new StyledText(this, SWT.SINGLE);
		outputText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		outputText.addKeyListener(outputKeyListener);

		updateFromPrefs();

		connector.getChatService().addChatServiceListener(new ChatListener() {
			public void chatEventOccured(final ChatEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {

						if (!ChatConsole.this.isDisposed()) {
							if (e.getType() == ChatTypes.OUTBOUND) {
								acceptOutbound(e);
							} else {
								acceptInbound(e);
							}
						}
					}
				});
			}
		});
	}

	public Label getLagLabel() {
		return pingLabel;
	}

	public boolean acceptInbound(ChatEvent event) {
		boolean result = true;

		if (StringUtils.isNotBlank(inboundRegex)) {
			result = event.getMessage().matches(inboundRegex);
		}
		if (inboundType != -1 && result) {
			result = this.inboundType == event.getType();
		}

		if (result) {
			append(event.getMessage());
		}
		return result;
	}

	public boolean acceptOutbound(ChatEvent event) {
		boolean result = true;

		if (StringUtils.isNotBlank(outboundRegex)) {
			result = event.getMessage().matches(outboundRegex);
		}
		if (outboundType != -1 && result) {
			result = outboundType == event.getType();
		}

		if (result) {
			append(event.getMessage());
		}
		return result;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isScrollLock() {
		return scrollLockButton.getSelection();
	}

	protected void addHyperLinks(String text, int startIndex) {
	}

	protected void fireSendOutput() {
		connector.sendMessage(outputText.getText());
		outputText.setText("");
	}

	protected void append(String text) {
		if (App.getInstance().getPreferences().getBoolean(
				CHAT_TIMESTAMP_CONSOLE)) {
			SimpleDateFormat format = new SimpleDateFormat(App.getInstance()
					.getPreferences().getString(CHAT_TIMESTAMP_CONSOLE_FORMAT));
			text = format.format(new Date()) + text;
		}

		text = '\n' + text;
		int positionBeforeAdding = inputText.getCharCount();
		inputText.append(text);
		inputText.setCaretOffset(inputText.getCharCount());
		inputText.setSelection(new Point(inputText.getCharCount(), inputText
				.getCharCount()));
		decorateText(text, positionBeforeAdding);
		clean();
	}

	protected void clean() {
		int charCount = inputText.getCharCount();
		if (charCount > App.getInstance().getPreferences().getInt(
				CHAT_MAX_CONSOLE_CHARS)) {
			LOG.info("Cleaning chat console");
			long startTime = System.currentTimeMillis();
			int cleanTo = (int) (charCount * CLEAN_PERCENTAGE);
			inputText.getContent().replaceTextRange(0, cleanTo, "");
			LOG.info("Cleaned console in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected void decorateText(String text, int startIndex) {
		addHyperLinks(text, startIndex);
	}

	public void dispose() {
		App.getInstance().getPreferences().removePropertyChangeListener(
				propertyChangeListener);
		chatWindow = null;
		super.dispose();
	}

	public String getInboundRegex() {
		return inboundRegex;
	}

	public int getInboundType() {
		return inboundType;
	}

	public String getOutboundRegex() {
		return outboundRegex;
	}

	public int getOutboundType() {
		return outboundType;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public boolean isScrollLockEnabled() {
		return scrollLockButton.getSelection();
	}

	public void save(String fileName) {
	}

	public int searchBackword(int startPosition, String searchString) {
		return -1;
	}

	public int searchForward(int startPosition, String searchString) {
		return -1;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void setInboundRegex(String inboundRegex) {
		this.inboundRegex = inboundRegex;
	}

	public void setInboundType(int inboundType) {
		this.inboundType = inboundType;
	}

	public void setOutboundRegex(String outboundRegex) {
		this.outboundRegex = outboundRegex;
	}

	public void setOutboundType(int outboundType) {
		this.outboundType = outboundType;
	}

	public void setScrollLockEnabled(boolean isScrollLockEnabled) {
		scrollLockButton.setSelection(isScrollLockEnabled);
	}

	public void updateFromPrefs() {
		RaptorPreferenceStore prefs = App.getInstance().getPreferences();
		inputText.setBackground(prefs.getColor(CHAT_INPUT_BACKGROUND_COLOR));
		inputText.setForeground(prefs.getColor(CHAT_INPUT_DEFAULT_TEXT_COLOR));
		inputText.setFont(prefs.getFont(CHAT_INPUT_FONT));
		outputText.setBackground(prefs.getColor(CHAT_OUTPUT_BACKGROUND_COLOR));
		outputText.setForeground(prefs.getColor(CHAT_OUTPUT_TEXT_COLOR));
		outputText.setFont(prefs.getFont(CHAT_OUTPUT_FONT));
	}
}
