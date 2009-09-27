package raptor.swt.chat;

import org.eclipse.swt.custom.StyledText;

import raptor.chat.ChatEvent;
import raptor.chat.ChatLogger.ChatEventParseListener;
import raptor.service.ThreadService;

public class Utils {

	public static void appendPreviousChatsToController(final ChatConsole console) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				console.getConnector().getChatService().getChatLogger()
						.parseFile(new ChatEventParseListener() {

							public void onNewEventParsed(final ChatEvent event) {
								console.getDisplay().asyncExec(new Runnable() {
									public void run() {
										try {
											if (!console.isDisposed()) {
												console.getController()
														.onChatEvent(event);
											}
										} catch (Throwable t) {
											console
													.getConnector()
													.onError(
															"appendPreviousChatsToController",
															t);
										}
									}
								});
							}
						});
			}
		});
	}

	/**
	 * Returns null if the current position isn't quoted text, otherwise returns
	 * the text in quotes. Both single and double quotes are supported.
	 */
	public static String getQuotedText(StyledText text, int position) {
		try {
			int quoteStart;
			int quoteStop;

			int currentPosition = position;
			char currentChar = text.getText(currentPosition,
					currentPosition + 1).charAt(0);

			while (currentChar != '\"' && currentChar != '\'') {
				if (currentChar == '\r' || currentChar == '\n') {
					return null;
				}
				currentChar = text.getText(--currentPosition,
						currentPosition + 1).charAt(0);
			}

			quoteStart = currentPosition;
			currentPosition = position;
			currentChar = text.getText(currentPosition, currentPosition + 1)
					.charAt(0);

			while (currentChar != '\"' && currentChar != '\'') {
				if (currentChar == '\r' || currentChar == '\n') {
					return null;
				}
				currentChar = text.getText(++currentPosition,
						currentPosition + 1).charAt(0);
			}

			quoteStop = currentPosition;

			return text.getText(quoteStart + 1, quoteStop - 1);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns the stripped word at the specified position, null if there is not
	 * one.
	 */
	// public static String getStrippedWord(StyledText text, int position) {
	// String word = getWord(text, position);
	//
	// if (word != null) {
	// return stripWord(word);
	// } else {
	// return null;
	// }
	// }
	/**
	 * Returns the url at the specified position, null if there is not one. This
	 * method handles ICS wrapping and will remove it and return just the url.
	 */
	public static String getUrl(StyledText text, int position) {
		String candidateWord = getWrappedWord(text, position);
		if (candidateWord != null
				&& (candidateWord.startsWith("http://") || candidateWord
						.startsWith("https://"))) {
			return candidateWord;
		} else if (candidateWord != null
				&& (candidateWord.endsWith(".com")
						|| candidateWord.endsWith(".org")
						|| candidateWord.endsWith(".edu") || candidateWord
						.startsWith("www.")) && !candidateWord.contains("@")) {
			if (candidateWord.endsWith(".") || candidateWord.endsWith(",")) {
				candidateWord = candidateWord.substring(0, candidateWord
						.length() - 1);
			}
			return "http://" + candidateWord;
		} else {
			return null;
		}
	}

	/**
	 * Returns the word at the specified position, null if there is not one.
	 */
	public static String getWord(StyledText text, int position) {
		try {
			int lineStart;
			int lineEnd;

			int currentPosition = position;
			char currentChar = text.getText(currentPosition,
					currentPosition + 1).charAt(0);

			while (currentPosition > 0 && !Character.isWhitespace(currentChar)) {
				currentChar = text.getText(--currentPosition,
						currentPosition + 1).charAt(0);
			}

			lineStart = currentPosition;

			currentPosition = position;
			currentChar = text.getText(currentPosition, currentPosition + 1)
					.charAt(0);

			while (currentPosition < text.getCharCount()
					&& !Character.isWhitespace(currentChar)) {
				currentChar = text.getText(++currentPosition,
						currentPosition + 1).charAt(0);
			}

			lineEnd = currentPosition;

			return trimDateStampFromWord(text.getText(lineStart + 1,
					lineEnd - 1));

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns null if the current position isn't a wrapped word, otherwise
	 * returns the word with the ICS wrapping removed.
	 */
	public static String getWrappedWord(StyledText text, int position) {
		try {
			String result = null;
			int lineStart;
			int lineEnd;

			int currentPosition = position;
			char currentChar = text.getText(currentPosition,
					currentPosition + 1).charAt(0);

			while (currentPosition > 0 && !Character.isWhitespace(currentChar)) {
				currentChar = text.getText(--currentPosition,
						currentPosition + 1).charAt(0);
			}

			lineStart = currentPosition;

			currentPosition = position;
			currentChar = text.getText(currentPosition, currentPosition + 1)
					.charAt(0);

			while (currentPosition < text.getCharCount()
					&& !Character.isWhitespace(currentChar)) {
				currentChar = text.getText(++currentPosition,
						currentPosition + 1).charAt(0);
			}
			lineEnd = currentPosition;
			result = text.getText(lineStart + 1, lineEnd - 1);

			// now check to see if its a wrap
			while (Character.isWhitespace(currentChar)
					&& currentPosition < text.getCharCount()) {
				currentChar = text.getText(++currentPosition,
						currentPosition + 1).charAt(0);
			}
			while (currentChar == '\\') {
				currentChar = text.getText(++currentPosition,
						currentPosition + 1).charAt(0);
				while (Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = text.getText(++currentPosition,
							currentPosition + 1).charAt(0);
				}

				lineStart = currentPosition - 1;
				while (!Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = text.getText(++currentPosition,
							currentPosition + 1).charAt(0);
				}

				lineEnd = currentPosition;
				result += text.getText(lineStart + 1, lineEnd - 1);

				while (Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = text.getText(++currentPosition,
							currentPosition + 1).charAt(0);
				}
			}

			if (result != null) {
				return trimDateStampFromWord(result);
			}
			return result;

		} catch (Exception e) {
			return null;
		}
	}

	public static String trimDateStampFromWord(String word) {
		if (word.startsWith("[")) {
			int closingBrace = word.indexOf("]");
			if (closingBrace != -1) {
				return word.substring(closingBrace + 1);
			}
		}
		return word;
	}

}
