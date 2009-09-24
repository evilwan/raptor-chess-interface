package raptor.swt.chat;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;

import raptor.Raptor;
import raptor.chat.ChatEvent;
import raptor.chat.ChatTypes;
import raptor.connector.fics.FicsUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
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
	protected boolean ignoreAwayList;
	protected String prenedText;
	protected String sourceOfLastTellReceived;
	protected List<ChatEvent> awayList = new ArrayList<ChatEvent>(100);

	protected KeyListener inputTextKeyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent arg0) {
			if (arg0.character == '\r') {
				onSendOutputText();
				chatConsole.outputText.forceFocus();
				chatConsole.outputText.setSelection(chatConsole.outputText
						.getCharCount());
			} else if (FicsUtils.LEGAL_CHARACTERS.indexOf(arg0.character) != -1
					&& arg0.stateMask == 0) {
				onAppendOutputText("" + arg0.character);
			} else {
				chatConsole.outputText.forceFocus();
				chatConsole.outputText.setSelection(chatConsole.outputText
						.getCharCount());
			}
		}
	};

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

	public ChatConsoleController() {
	}

	public ChatConsole getChatConsole() {
		return chatConsole;
	}

	public void setChatConsole(ChatConsole chatConsole) {
		this.chatConsole = chatConsole;
	}

	public void onAway() {
		ignoreAwayList = true;
		onAppendChatEventToInputText(new ChatEvent(null, ChatTypes.OUTBOUND,
				"Direct tells you missed while you were away:"));
		for (ChatEvent event : awayList) {
			onAppendChatEventToInputText(event);
		}
		awayList.clear();
		ignoreAwayList = false;
		adjustAwayButtonEnabled();
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
		adjustAwayButtonEnabled();
	}

	public abstract boolean isAcceptingChatEvent(ChatEvent inboundEvent);

	public abstract boolean isPrependable();

	public abstract boolean isSearchable();

	public void onAppendChatEventToInputText(ChatEvent event) {

		if (!ignoreAwayList && event.getType() == ChatTypes.TELL
				|| event.getType() == ChatTypes.PARTNER_TELL) {
			awayList.add(event);
			adjustAwayButtonEnabled();
		}

		String appendText = null;
		int startIndex = 0;

		// synchronize on chatConsole so the scrolling will be handled
		// appropriately if there are multiple events being
		// published at the same time.
		synchronized (chatConsole) {
			boolean isScrollBarAtMax = false;
			ScrollBar scrollbar = chatConsole.inputText.getVerticalBar();
			if (scrollbar != null && scrollbar.isVisible()) {
				isScrollBarAtMax = scrollbar.getMaximum() == scrollbar
						.getSelection()
						+ scrollbar.getThumb();
			}

			String messageText = event.getMessage();
			String date = "";
			if (Raptor.getInstance().getPreferences().getBoolean(
					CHAT_TIMESTAMP_CONSOLE)) {
				SimpleDateFormat format = new SimpleDateFormat(Raptor
						.getInstance().getPreferences().getString(
								CHAT_TIMESTAMP_CONSOLE_FORMAT));
				date = format.format(new Date(event.getTime()));
			}

			appendText = (chatConsole.inputText.getCharCount() == 0 ? "" : "\n")
					+ date + messageText;

			chatConsole.inputText.append(appendText);
			startIndex = chatConsole.inputText.getCharCount()
					- appendText.length();

			if (isScrollBarAtMax
					&& ((chatConsole.inputText.getSelection().y - chatConsole.inputText
							.getSelection().x) == 0)) {
				onForceAutoScroll();
			}
		}

		onDecorateInputText(event, appendText, startIndex);
		reduceInputTextIfNeeded();
	}

	public void onForceAutoScroll() {
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
		playSounds(event);
	}

	protected void onDecorateInputText(final ChatEvent event,
			final String message, final int textStartPosition) {
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
		if (event.getType() != OUTBOUND) {
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

						// If there is a newline between the quotes ignore it.
						int newLine = message.indexOf("\n", quoteIndex);

						// If there is just one character and the a space after
						// the
						// first quote ignore it.
						boolean isASpaceTwoCharsAfterQuote = message
								.charAt(quoteIndex + 2) == ' ';

						// If the quotes dont match ignore it.
						boolean doQuotesMatch = message.charAt(quoteIndex) == message
								.charAt(endQuote);

						if (!(newLine > quoteIndex && newLine < endQuote)
								&& !isASpaceTwoCharsAfterQuote && doQuotesMatch) {
							quotedRanges.add(new int[] { quoteIndex + 1,
									endQuote });

						}
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
				StyleRange range = new StyleRange(textStartPosition
						+ quotedRange[0], quotedRange[1] - quotedRange[0],
						underlineColor, chatConsole.inputText.getBackground());
				range.underline = true;
				chatConsole.inputText.setStyleRange(range);
			}
		}
	}

	protected void decorateLinks(ChatEvent event, String message,
			int textStartPosition) {
		if (event.getType() != OUTBOUND) {
			List<int[]> linkRanges = new ArrayList<int[]>(5);

			// First check http://,https://,www.
			int startIndex = message.indexOf("http://");
			if (startIndex == -1) {
				startIndex = message.indexOf("https://");
				if (startIndex == -1) {
					startIndex = message.indexOf("www.");
				}
			}
			while (startIndex != -1 && startIndex < message.length()) {
				int endIndex = startIndex + 1;
				while (endIndex < message.length()) {

					// On ICS servers line breaks follow the convention \n\\
					// This code underlines links that have line breaks in them.
					if (message.charAt(endIndex) == '\n'
							&& message.length() > endIndex + 1
							&& message.charAt(endIndex + 1) == '\\') {
						endIndex += 2;

						// Move past the white space and then continue on with
						// the
						// main loop.
						while (endIndex < message.length()
								&& (Character.isWhitespace(message
										.charAt(endIndex)))) {
							endIndex++;
						}
						continue;
					} else if (Character.isWhitespace(message.charAt(endIndex))) {
						break;
					}
					endIndex++;
				}

				if (message.charAt(endIndex - 1) == '.') {
					endIndex--;
				}

				linkRanges.add(new int[] { startIndex, endIndex });

				startIndex = message.indexOf("http://", endIndex + 1);
				if (startIndex == -1) {
					startIndex = message.indexOf("https://", endIndex + 1);
					if (startIndex == -1) {
						startIndex = message.indexOf("www.", endIndex + 1);
					}
				}
			}

			// Next check ending with .com,.org,.edu
			int endIndex = message.indexOf(".com");
			if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
				endIndex = message.indexOf(".org");
				if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
					endIndex = message.indexOf(".edu");
				}
			}
			if (endIndex != -1 && isInRanges(startIndex, linkRanges)) {
				endIndex = -1;
			}
			int linkEnd = endIndex + 4;
			while (endIndex != -1) {
				startIndex = endIndex--;
				while (startIndex >= 0) {
					// On ICS servers line breaks follow the convention \n\\
					// This code underlines links that have line breaks in them.
					if (Character.isWhitespace(message.charAt(startIndex))) {
						break;
					}
					startIndex--;
				}

				// Filter out emails.
				int atIndex = message.indexOf("@", startIndex);
				if (atIndex == -1 || atIndex > linkEnd) {
					linkRanges.add(new int[] { startIndex + 1, linkEnd });
				}

				endIndex = message.indexOf(".com", linkEnd + 1);
				if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
					endIndex = message.indexOf(".org", linkEnd + 1);
					if (endIndex == -1 || isInRanges(startIndex, linkRanges)) {
						endIndex = message.indexOf(".com", linkEnd + 1);
					}
				}
				if (endIndex != -1 && isInRanges(startIndex, linkRanges)) {
					endIndex = -1;
				}
				linkEnd = endIndex + 4;
			}

			// add all the ranges that were found.
			for (int[] linkRange : linkRanges) {
				Color underlineColor = chatConsole.getPreferences().getColor(
						CHAT_LINK_UNDERLINE_COLOR);
				StyleRange range = new StyleRange(textStartPosition
						+ linkRange[0], linkRange[1] - linkRange[0],
						underlineColor, chatConsole.inputText.getBackground());
				range.underline = true;
				chatConsole.inputText.setStyleRange(range);
			}
		}
	}

	protected boolean isInRanges(int location, List<int[]> ranges) {
		boolean result = false;
		for (int[] range : ranges) {
			if (location >= range[0] && location <= range[1]) {
				result = true;
				break;
			}
		}
		return result;
	}

	protected void playSounds(ChatEvent event) {
		if (event.getType() == ChatTypes.TELL
				|| event.getType() == ChatTypes.PARTNER_TELL) {
			SoundService.getInstance().playSound("chat");
		}
	}

	protected void onSearch() {
		chatConsole.getDisplay().asyncExec(new Runnable() {
			public void run() {
				String searchString = chatConsole.outputText.getText();
				if (StringUtils.isBlank(searchString)) {
					MessageBox box = new MessageBox(chatConsole.getShell(),
							SWT.ICON_INFORMATION | SWT.OK);
					box
							.setMessage("You must enter text in the input field to search on.");
					box.setText("Alert");
					box.open();
				} else {
					boolean foundText = false;
					searchString = searchString.toUpperCase();
					int start = chatConsole.inputText.getCaretOffset();

					if (start >= chatConsole.inputText.getCharCount()) {
						start = chatConsole.inputText.getCharCount() - 1;
					} else if (start - searchString.length() + 1 >= 0) {
						String text = chatConsole.inputText.getText(start
								- searchString.length(), start - 1);
						if (text.equalsIgnoreCase(searchString)) {
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

						String stringToSearch = chatConsole.inputText.getText(
								start - charsBack, start).toUpperCase();
						int index = stringToSearch.lastIndexOf(searchString);
						if (index != -1) {
							int textStart = start - charsBack + index;
							chatConsole.inputText.setSelection(textStart,
									textStart + searchString.length());
							foundText = true;
							break;
						}
						start -= charsBack;
					}

					if (!foundText) {
						MessageBox box = new MessageBox(chatConsole.getShell(),
								SWT.ICON_INFORMATION | SWT.OK);
						box.setMessage("Could not find any occurances of '"
								+ searchString + "'.");
						box.setText("Alert");
						box.open();
					}
				}
			}
		});
	}

	public void onSendOutputText() {
		chatConsole.connector.sendMessage(chatConsole.outputText.getText());
		chatConsole.outputText.setText("");
		awayList.clear();
		adjustAwayButtonEnabled();
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
							String string = chatConsole.getInputText().getText(
									i, endIndex);
							writer.append(string);
							i = endIndex;
						}
						writer.flush();
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

	protected void adjustAwayButtonEnabled() {
		chatConsole.setButtonEnabled(!awayList.isEmpty(),
				ChatConsole.AWAY_BUTTON);
	}

	protected void reduceInputTextIfNeeded() {
		int charCount = chatConsole.inputText.getCharCount();
		if (charCount > Raptor.getInstance().getPreferences().getInt(
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
