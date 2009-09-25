package raptor.swt.chat;

import org.eclipse.swt.custom.StyledText;

import raptor.util.RaptorStringTokenizer;

public class Utils {
	public static final String VALID_PERSON_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * Removes the ICS channel wrapping around a specified channel word
	 * returning only the channel number.
	 * 
	 * Returns -1 if the specified word is not a channel.
	 */
	public static int getChannel(String word) {

		int result = -1;
		if (word != null) {
			int openParenIndex = word.lastIndexOf("(");
			int closeParenIndex = word.lastIndexOf(")");

			if (openParenIndex != -1 && closeParenIndex != -1
					&& openParenIndex < closeParenIndex) {
				try {
					result = Integer.parseInt(word.substring(
							openParenIndex + 1, closeParenIndex));
				} catch (NumberFormatException nfe) {
					result = -1;
				}

			}
		}
		return result;
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
	public static String getStrippedWord(StyledText text, int position) {
		String word = getWord(text, position);

		if (word != null) {
			return stripWord(word);
		} else {
			return null;
		}
	}

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

			return text.getText(lineStart + 1, lineEnd - 1);

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

			return result;

		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Returns true if the specified word is probably a persons name.
	 */
	public static boolean isLikelyPerson(String word) {
		if (word != null && word.length() > 2) {
			boolean result = true;
			for (int i = 0; result && i < word.length(); i++) {
				result = VALID_PERSON_CHARS.indexOf(word.charAt(i)) != -1;
			}
			return result;
		} else {
			return false;
		}
	}

	/**
	 * Returns the word with all characters in: ()~!@?#$%^&*_+|}{'\";/?<>.,
	 * :[]1234567890\t\r\n removed.
	 */
	public static String stripWord(String word) {
		if (word != null) {
			RaptorStringTokenizer stringtokenizer = new RaptorStringTokenizer(
					word, "()~!@?#$%^&*_+|}{'\";/?<>., :[]1234567890\t\r\n");
			if (stringtokenizer.hasMoreTokens())
				return stringtokenizer.nextToken();
			else
				return word;
		}
		return null;
	}
}
