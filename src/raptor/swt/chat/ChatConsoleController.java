package raptor.swt.chat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ScrollBar;

import raptor.App;
import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.fics.FicsUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.ChatService.ChatListener;
import raptor.util.LaunchBrowser;

public abstract class ChatConsoleController implements PreferenceKeys,
		ChatTypes {
	public static final double CLEAN_PERCENTAGE = .33;
	public static final int TEXT_CHUNK_SIZE = 1000;
	private static final Log LOG = LogFactory
			.getLog(ChatConsoleController.class);

	protected ChatConsole chatConsole;
	protected ChatListener chatServiceListener = new ChatListener() {
		public void chatEventOccured(final ChatEvent event) {
			if (!chatConsole.isDisposed() && isAcceptingChatEvent(event)) {

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
			}
		}
	};

	protected MouseListener inputTextClickListener = new MouseAdapter() {

		public void mouseUp(MouseEvent e) {
			if (e.button == 1) {
				int caretPosition = chatConsole.inputText.getCaretOffset();

				String url = Utils.getUrl(chatConsole.inputText, caretPosition);
				if (url != null) {
					LaunchBrowser.openURL(url);
				}

				String quotedText = Utils.getQuotedText(chatConsole.inputText,
						caretPosition);
				if (quotedText != null) {
					chatConsole.getConnector().sendMessage(quotedText);
				}
			} else if (e.button == 3) {
				int caretPosition = chatConsole.inputText.getCaretOffset();
				// TO DO: add person popup.
				System.err.println("CaretPosition = " + caretPosition);
				System.err.println("Word="
						+ Utils.getWord(chatConsole.inputText, caretPosition));
				System.err.println("StrippedWord="
						+ Utils.getStrippedWord(chatConsole.inputText,
								caretPosition));
				System.err.println("isLikelyPerson="
						+ Utils.isLikelyPerson(Utils.getWord(
								chatConsole.inputText, caretPosition)));
				System.err.println("QuotedText="
						+ Utils.getQuotedText(chatConsole.inputText,
								caretPosition));
				System.err.println("WrappedWord="
						+ Utils.getWrappedWord(chatConsole.inputText,
								caretPosition));
				System.err.println("Channel="
						+ Utils.getChannel(Utils.getWord(chatConsole.inputText,
								caretPosition)));
				System.err.println("Url="
						+ Utils.getUrl(chatConsole.inputText, caretPosition));
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

	protected void addMouseListeners() {
		chatConsole.inputText.addMouseListener(inputTextClickListener);
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

		chatConsole.inputText.removeMouseListener(inputTextClickListener);
	}

	public String getPrenedText() {
		return prenedText;
	}

	public boolean hasUnseenText() {
		return hasUnseenText;
	}

	public void init() {
		addInputTextKeyListeners();
		addMouseListeners();
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
		boolean isScrollBarAtMax = false;
		ScrollBar scrollbar = chatConsole.inputText.getVerticalBar();
		if (scrollbar != null && scrollbar.isVisible()) {
			isScrollBarAtMax = scrollbar.getMaximum() == scrollbar
					.getSelection()
					+ scrollbar.getThumb();
			System.err.println("Scrollbar is at maximum=" + isScrollBarAtMax
					+ " max=" + scrollbar.getMaximum() + " sel="
					+ scrollbar.getSelection() + " thumbSize="
					+ scrollbar.getThumb());

		}

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
				- appendText.length();

		if (isScrollBarAtMax) {
			forceScrollToInputTextEnd();
		}

		onDecorateInputText(event, appendText, startIndex);
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

	protected void onDecorateInputText(ChatEvent event, String message,
			int textStartPosition) {
		decorateForegroundColor(event, message, textStartPosition);
		decorateQuotes(event, message, textStartPosition);
		decorateLinks(event, message, textStartPosition);
	}

	protected void decorateForegroundColor(ChatEvent event, String message,
			int textStartPosition) {
		Color color = chatConsole.preferences.getColor(event);
		if (color == null) {
			color = chatConsole.inputText.getForeground();
		}

		String prompt = chatConsole.getConnector().getPrompt();
		if (message.endsWith(prompt)) {
			message = message.substring(0, message.length() - prompt.length());
		}

		chatConsole.inputText
				.setStyleRange(new StyleRange(textStartPosition, message
						.length(), color, chatConsole.inputText.getBackground()));
	}

	protected void decorateQuotes(ChatEvent event, String message,
			int textStartPosition) {
		List<int[]> quotedRanges = new ArrayList<int[]>(5);

		int quoteIndex = message.indexOf("\"");
		if (quoteIndex == -1) {
			quoteIndex = message.indexOf("'");
		}

		while (quoteIndex != -1) {
			int endQuote = message.indexOf("\"", quoteIndex + 1);
			if (endQuote == -1) {
				endQuote = message.indexOf("'", quoteIndex + 1);
			}

			if (endQuote == -1) {
				break;
			} else {
				if (quoteIndex + 1 != endQuote) {
					quotedRanges
							.add(new int[] { quoteIndex + 1, endQuote });
				}
			}

			quoteIndex = message.indexOf("\"", endQuote + 1);
			if (quoteIndex == -1) {
				quoteIndex = message.indexOf("'", endQuote + 1);
			}
		}

		for (int[] quotedRange : quotedRanges) {
			Color underlineColor = chatConsole.getPreferences().getColor(
					CHAT_QUOTE_UNDERLINE_COLOR);
			StyleRange range = new StyleRange(textStartPosition + quotedRange[0], quotedRange[1]
					- quotedRange[0], underlineColor, chatConsole.inputText
					.getBackground());
			range.underline = true;
			chatConsole.inputText.setStyleRange(range);
		}
	}

	protected void decorateLinks(ChatEvent event, String message,
			int textStartPosition) {

	}

	protected void onSearch() {
		chatConsole.getDisplay().asyncExec(new Runnable() {
			public void run() {
				String searchString = chatConsole.outputText.getText();
				int start = chatConsole.inputText.getCaretOffset();

				if (start >= chatConsole.inputText.getCharCount()) {
					start = chatConsole.inputText.getCharCount() - 1;
				} else if (start - searchString.length() + 1 >= 0) {
					String text = chatConsole.inputText.getText(start
							- searchString.length(), start - 1);
					if (text.equals(searchString)) {
						start -= searchString.length();
					}
				}

				while (start > 0) {
					int charsBack = 0;
					if (start - TEXT_CHUNK_SIZE > 0) {
						charsBack = TEXT_CHUNK_SIZE;
					} else {
						charsBack = start;
					}

					String stringToSearch = chatConsole.inputText.getText(start
							- charsBack, start);
					int index = stringToSearch.lastIndexOf(searchString);
					if (index != -1) {
						System.err.println("Found " + (start + index) + " "
								+ (start + index + searchString.length()) + " "
								+ searchString);
						int textStart = start - charsBack + index;
						chatConsole.inputText.setSelection(textStart, textStart
								+ searchString.length());
						break;
					}
					start -= charsBack;
				}
			}
		});
	}

	public void onSendOutputText() {
		chatConsole.connector.sendMessage(chatConsole.outputText.getText());
		chatConsole.outputText.setText("");
	}

	public void onSave() {
		FileDialog fd = new FileDialog(chatConsole.getShell(), SWT.SAVE);
		fd.setText("Save Console Output.");
		fd.setFilterPath("");
		String[] filterExt = { "*.txt", "*.*" };
		fd.setFilterExtensions(filterExt);
		final String selected = fd.open();

		if (selected != null) {
			chatConsole.getDisplay().asyncExec(new Runnable() {
				public void run() {
					FileWriter writer = null;
					try {
						writer = new FileWriter(selected);
						writer.append("Raptor console log created on "
								+ new Date() + "\n");
						int i = 0;
						while (i < chatConsole.getInputText().getCharCount() - 1) {
							int endIndex = i + TEXT_CHUNK_SIZE;
							if (endIndex >= chatConsole.getInputText()
									.getCharCount()) {
								endIndex = i
										+ (chatConsole.getInputText()
												.getCharCount() - i) - 1;
							}

							System.err
									.println("i="
											+ i
											+ " endIndex="
											+ endIndex
											+ " textlen="
											+ chatConsole.getInputText()
													.getCharCount());
							String string = chatConsole.getInputText().getText(
									i, endIndex);
							writer.append(string);
							i = endIndex;
						}
						writer.flush();
						LOG.debug("Wrote file " + selected);
					} catch (Throwable t) {
						LOG.error("Error writing file: " + selected, t);
					} finally {
						if (writer != null) {
							try {
								writer.close();
							} catch (IOException ioe) {
							}
						}
					}
				}
			});
		}
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

	public String getSourceOfLastTellReceived() {
		return sourceOfLastTellReceived;
	}

	public void setSourceOfLastTellReceived(String sourceOfLastTellReceived) {
		this.sourceOfLastTellReceived = sourceOfLastTellReceived;
	}

	public abstract String getPrompt();

}
