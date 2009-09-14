package raptor.gui.chat;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import raptor.service.SWTService;

public class ChatConsole extends Composite {
	public static final double CLEAN_PERCENTAGE = .33;

	String inboundRegex;
	int inboundType = -1;
	StyledText inputText;
	boolean isDirty;
	boolean isScrollLockEnabled;
	String outboundRegex;
	int outboundType = -1;
	StyledText outputText;
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			redraw();
		}
	};

	String title;

	public ChatConsole(Composite parent, int style) {
		super(parent, style);
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

		outputText = new StyledText(this, SWT.SINGLE);
		outputText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		updateFromPrefs();
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

	protected void addHyperLinks(String text, int startIndex) {
	}

	protected void append(String text) {
		int positionBeforeAdding = inputText.getCharCount();
		inputText.append(text);
		decorateText(text, positionBeforeAdding);
		clean();
	}

	protected void clean() {
		int charCount = inputText.getCharCount();
		if (charCount > SWTService.getInstance().getStore().getInt(
				SWTService.CHAT_MAX_CONSOLE_CHARS)) {
			int cleanTo = (int) (charCount * CLEAN_PERCENTAGE);
			inputText.getContent().replaceTextRange(0, cleanTo, "");
		}
	}

	protected void decorateText(String text, int startIndex) {
		addHyperLinks(text, startIndex);
	}

	public void dispose() {
		SWTService.getInstance().getStore().removePropertyChangeListener(
				propertyChangeListener);
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
