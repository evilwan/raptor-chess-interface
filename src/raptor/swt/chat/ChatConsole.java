package raptor.swt.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Label;

import raptor.service.SWTService;

public class ChatConsole extends Composite {
	private static final Log LOG = LogFactory.getLog(ChatConsole.class);
	public static final double CLEAN_PERCENTAGE = .33;
	public static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(
			"kk:mm ");

	String inboundRegex;
	int inboundType = -1;
	StyledText inputText;
	boolean isDirty;
	boolean isMainConsole;
	String outboundRegex;
	int outboundType = -1;
	StyledText outputText;
	ChatWindow chatWindow;
	Composite controlsComposite = null;
	CoolBar coolbar = null;
	Button scrollLockButton = null;
	Button removeButton = null;
	Label pingLabel = null;

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
			if (arg0.character == '\n') {
				fireSendOutput();
			}
		}
	};

	String title;

	public ChatConsole(Composite parent, int style, boolean isMainConsole,
			ChatWindow chatWindow) {
		super(parent, style);
		this.chatWindow = chatWindow;

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.makeColumnsEqualWidth = true;
		setLayout(gridLayout);

		SWTService.getInstance().getStore().addPropertyChangeListener(
				propertyChangeListener);
		inputText = new StyledText(this, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI);
		inputText.setLayoutData(new GridData(GridData.FILL_BOTH));
		inputText.setEditable(false);

		controlsComposite = new Composite(parent, SWT.NONE);
		controlsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		controlsComposite.setLayout(new GridLayout(2, true));

		outputText = new StyledText(controlsComposite, SWT.SINGLE);
		outputText.setLayoutData(GridData.FILL_BOTH);

		coolbar = new CoolBar(controlsComposite, SWT.HORIZONTAL);
		coolbar.setLayout(new RowLayout());

		setupCoolbar();

		updateFromPrefs();
	}
	
	protected void setupCoolbar() {

		scrollLockButton = new Button(coolbar, SWT.TOGGLE);
		scrollLockButton.setText("Auto Scroll");
		scrollLockButton.setSelection(true);

		if (!isMainConsole) {
			removeButton = new Button(coolbar, SWT.PUSH);
			removeButton.setText("Remove Tab");
		} else {
			pingLabel = new Label(coolbar, SWT.NONE);
		}
		SWT.I
		
	}

	public Label getLagLabel() {
		return pingLabel;
	}

	public boolean acceptInbound(String text, int type) {
		boolean result = true;

		if (inboundRegex != null) {
			result = text.matches(text);
		}
		if (inboundType != -1 && result) {
			result = this.inboundType == type;
		}

		if (result) {
			append(text);
		}
		return result;
	}

	public boolean acceptOutbound(String text, int type) {
		boolean result = true;

		if (outboundRegex != null) {
			result = text.matches(outboundRegex);
		}
		if (outboundType != -1 && result) {
			result = outboundType == type;
		}

		if (result) {
			append(text);
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
		chatWindow.sendOutput(outputText.getText());
		outputText.setText("");
	}

	protected void append(String text) {
		if (SWTService.getInstance().getStore().getBoolean(
				SWTService.CHAT_TIMESTAMP_CONSOLE)) {
			text = TIMESTAMP_FORMAT.format(new Date()) + text;
		}
		int positionBeforeAdding = inputText.getCharCount();
		inputText.append(text);
		decorateText(text, positionBeforeAdding);
		clean();
	}

	protected void clean() {
		int charCount = inputText.getCharCount();
		if (charCount > SWTService.getInstance().getStore().getInt(
				SWTService.CHAT_MAX_CONSOLE_CHARS)) {
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
		SWTService.getInstance().getStore().removePropertyChangeListener(
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
		return isScrollLockEnabled;
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
		this.isScrollLockEnabled = isScrollLockEnabled;
	}

	public void updateFromPrefs() {
		SWTService swt = SWTService.getInstance();
		inputText.setBackground(swt
				.getColor(SWTService.CHAT_INPUT_BACKGROUND_COLOR));
		inputText.setForeground(swt
				.getColor(SWTService.CHAT_INPUT_DEFAULT_TEXT_COLOR));
		inputText.setFont(swt.getFont(SWTService.CHAT_INPUT_FONT));
		outputText.setBackground(swt
				.getColor(SWTService.CHAT_OUTPUT_BACKGROUND_COLOR));
		outputText.setForeground(swt
				.getColor(SWTService.CHAT_OUTPUT_TEXT_COLOR));
		outputText.setFont(swt.getFont(SWTService.CHAT_OUTPUT_FONT));
	}
}
