package raptor.swt.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;

import raptor.App;
import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.fics.FicsUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.ChatService.ChatListener;

public abstract class ChatConsoleController implements PreferenceKeys,
		ChatTypes {
	public static final double CLEAN_PERCENTAGE = .33;
	private static final Log LOG = LogFactory
			.getLog(ChatConsoleController.class);

	protected ChatConsole chatConsole;
	protected ChatListener chatServiceListener = new ChatListener() {
		public void chatEventOccured(final ChatEvent event) {
			if (!chatConsole.getDisplay().isDisposed()
					&& !chatConsole.isDisposed() && isAcceptingChatEvent(event)) {

				chatConsole.getDisplay().asyncExec(new Runnable() {
					public void run() {
						onChatEvent(event);
					}
				});
			}
		}
	};

	protected boolean hasUnseenText;
	protected String prenedText;
	protected String sourceOfLastTellReceived;

	protected KeyListener inputTextKeyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent arg0) {
			if (arg0.character == '\r') {
				onSendOutputText();
				chatConsole.outputText.forceFocus();
				chatConsole.outputText.setSelection(chatConsole.outputText
						.getCharCount());
			} else if (FicsUtils.LEGAL_CHARACTERS.indexOf(arg0.character) != -1) {
				onAppendOutputText("" + arg0.character);
			} else {
				chatConsole.outputText.forceFocus();
				chatConsole.outputText.setSelection(chatConsole.outputText
						.getCharCount());
			}
		}
	};

	protected boolean isScrollLockEnabled = true;

	protected KeyListener outputKeyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent arg0) {
			if (arg0.character == '\r') {
				onSendOutputText();
			}
		}
	};

	protected KeyListener outputHistoryListener = new KeyAdapter() {
		protected List<String> sentText = new ArrayList<String>(50);
		protected int sentTextIndex = 0;

		public void keyReleased(KeyEvent arg0) {
			if (arg0.keyCode == SWT.ARROW_UP) {
				System.err.println("In outputHistoryListener arrow up");

				if (sentTextIndex >= 0) {
					if (sentTextIndex > 0) {
						sentTextIndex--;
					}
					if (!sentText.isEmpty()) {
						chatConsole.outputText.setText(sentText
								.get(sentTextIndex));
						chatConsole.outputText
								.setSelection(chatConsole.inputText
										.getCharCount() + 1);
					}
				}
			} else if (arg0.keyCode == SWT.ARROW_DOWN) {
				System.err.println("In outputHistoryListener arrow down");

				if (sentTextIndex < sentText.size() - 1) {
					sentTextIndex++;
					chatConsole.outputText.setText(sentText.get(sentTextIndex));
					chatConsole.outputText.setSelection(chatConsole.inputText
							.getCharCount() + 1);
				} else {
					chatConsole.outputText.setText("");
				}
			} else if (arg0.character == '\r') {
				System.err.println("In outputHistoryListener CR");

				if (sentText.size() > 50) {
					sentText.remove(0);
				}
				sentText.add(chatConsole.outputText.getText().substring(0,
						chatConsole.outputText.getText().length()));
				sentTextIndex = sentText.size() - 1;
			}
		}
	};

	protected KeyListener functionKeyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent arg0) {
			if (arg0.keyCode == SWT.F3) {
				chatConsole.connector.onAcceptKeyPress();
			} else if (arg0.keyCode == SWT.F4) {
				chatConsole.connector.onDeclineKeyPress();
			} else if (arg0.keyCode == SWT.F6) {
				chatConsole.connector.onAbortKeyPress();
			} else if (arg0.keyCode == SWT.F7) {
				chatConsole.connector.onRematchKeyPress();
			} else if (arg0.keyCode == SWT.F9) {
				if (sourceOfLastTellReceived != null) {
					chatConsole.outputText.setText(chatConsole.connector
							.getTellToString(sourceOfLastTellReceived));
					chatConsole.outputText.setSelection(chatConsole.outputText
							.getCharCount() + 1);
				}
			} else if (arg0.keyCode == SWT.SCROLL_LOCK) {
				setScrollLockEnabled(!isScrollLockEnabled());
			}
		}
	};

	public ChatConsoleController(ChatConsole console) {
		this.chatConsole = console;
	}

	protected void addInputTextKeyListeners() {

		chatConsole.outputText.addKeyListener(functionKeyListener);
		chatConsole.outputText.addKeyListener(outputHistoryListener);
		chatConsole.outputText.addKeyListener(outputKeyListener);

		chatConsole.inputText.addKeyListener(inputTextKeyListener);
		chatConsole.inputText.addKeyListener(functionKeyListener);
		chatConsole.inputText.addKeyListener(outputHistoryListener);
	}

	protected void adjustScrollLockButton() {
		Button scrollLockButton = chatConsole
				.getButton(ChatConsole.SCROLL_LOCK_BUTTON);
		if (scrollLockButton != null) {
			if (isScrollLockEnabled) {
				scrollLockButton.setImage(chatConsole.preferences
						.getIcon("locked"));
				scrollLockButton
						.setToolTipText(ChatConsole.SCROLL_LOCK_ON_TOOLTIP);
			} else {
				scrollLockButton.setImage(chatConsole.preferences
						.getIcon("unlocked"));
				scrollLockButton
						.setToolTipText(ChatConsole.SCROLL_LOCK_OFF_TOOLTIP);
			}
		}
	}

	public void dispose() {
		chatConsole.connector.getChatService().removeChatServiceListener(
				chatServiceListener);
		chatConsole.outputText.removeKeyListener(functionKeyListener);
		chatConsole.outputText.removeKeyListener(outputHistoryListener);
		chatConsole.outputText.removeKeyListener(outputKeyListener);

		chatConsole.inputText.removeKeyListener(inputTextKeyListener);
		chatConsole.inputText.removeKeyListener(functionKeyListener);
		chatConsole.inputText.removeKeyListener(outputHistoryListener);
	}

	public String getPrenedText() {
		return prenedText;
	}

	public boolean hasUnseenText() {
		return hasUnseenText;
	}

	public void init() {
		addInputTextKeyListeners();
		registerForChatEvents();
		adjustScrollLockButton();
	}

	public abstract boolean isAcceptingChatEvent(ChatEvent inboundEvent);

	public abstract boolean isPrependable();

	public boolean isScrollLockEnabled() {
		return isScrollLockEnabled;
	}

	public abstract boolean isSearchable();

	public void onAppendChatEventToInputText(ChatEvent event) {

		boolean atEndPriorToEvent = chatConsole.inputText.getCharCount() - 1 <= chatConsole.inputText
				.getCaretOffset();
		String messageText = event.getMessage();
		String date = "";
		if (App.getInstance().getPreferences().getBoolean(
				CHAT_TIMESTAMP_CONSOLE)) {
			SimpleDateFormat format = new SimpleDateFormat(App.getInstance()
					.getPreferences().getString(CHAT_TIMESTAMP_CONSOLE_FORMAT));
			date = format.format(new Date());
		}

		String appendText = (chatConsole.inputText.getCharCount() == 0 ? ""
				: "\n")
				+ date + messageText;

		chatConsole.inputText.append(appendText);
		int startIndex = chatConsole.inputText.getCharCount()
				- messageText.length();
		int endIndex = chatConsole.inputText.getCharCount();

		if (atEndPriorToEvent && isScrollLockEnabled) {
			forceScrollToInputTextEnd();
		} else if (!atEndPriorToEvent && isScrollLockEnabled) {
			setScrollLockEnabled(false);
		}

		onDecorateInputText(event, startIndex, endIndex);
		reduceInputTextIfNeeded();
	}

	public void forceScrollToInputTextEnd() {
		chatConsole.inputText.setCaretOffset(chatConsole.inputText
				.getCharCount());
		chatConsole.inputText.setSelection(new Point(chatConsole.inputText
				.getCharCount(), chatConsole.inputText.getCharCount()));
	}

	public void onAppendOutputText(String string) {
		chatConsole.outputText.append(string);
		chatConsole.outputText.setSelection(chatConsole.outputText
				.getCharCount());
		chatConsole.outputText.forceFocus();
	}

	public void onChatEvent(ChatEvent event) {
		if (event.getType() == TELL) {
			sourceOfLastTellReceived = event.getSource();
		}
		onAppendChatEventToInputText(event);
	}

	protected void onDecorateInputText(ChatEvent event, int textStartPosition,
			int textEndPosition) {
		// TO DO.
	}

	protected void onSearch() {
		// TO DO
	}

	public void onSendOutputText() {
		chatConsole.connector.sendMessage(chatConsole.outputText.getText());
		chatConsole.outputText.setText("");
	}

	protected void reduceInputTextIfNeeded() {
		int charCount = chatConsole.inputText.getCharCount();
		if (charCount > App.getInstance().getPreferences().getInt(
				CHAT_MAX_CONSOLE_CHARS)) {
			LOG.info("Cleaning chat console");
			long startTime = System.currentTimeMillis();
			int cleanTo = (int) (charCount * CLEAN_PERCENTAGE);
			chatConsole.inputText.getContent().replaceTextRange(0, cleanTo, "");
			LOG.info("Cleaned console in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected void registerForChatEvents() {
		chatConsole.connector.getChatService().addChatServiceListener(
				chatServiceListener);
	}

	public void setHasUnseenText(boolean hasUnseenText) {
		this.hasUnseenText = hasUnseenText;
	}

	public void setPrenedText(String prenedText) {
		this.prenedText = prenedText;
	}

	public void setScrollLockEnabled(boolean isScrollLockEnabled) {
		if (this.isScrollLockEnabled != isScrollLockEnabled) {
			this.isScrollLockEnabled = isScrollLockEnabled;
			adjustScrollLockButton();

			if (isScrollLockEnabled) {
				forceScrollToInputTextEnd();
			}
		}
	}

	public String getSourceOfLastTellReceived() {
		return sourceOfLastTellReceived;
	}

	public void setSourceOfLastTellReceived(String sourceOfLastTellReceived) {
		this.sourceOfLastTellReceived = sourceOfLastTellReceived;
	}

	public abstract String getPrompt();

}
