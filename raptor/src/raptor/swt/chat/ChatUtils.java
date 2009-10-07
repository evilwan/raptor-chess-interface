/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.swt.chat;

import org.eclipse.swt.custom.StyledText;

import raptor.chat.ChatEvent;
import raptor.chat.ChatLogger.ChatEventParseListener;
import raptor.service.ThreadService;

public class ChatUtils {

	/**
	 * Appends all of the previous chat events to the controller. This method
	 * executes asynchronously.
	 */
	public static void appendPreviousChatsToController(final ChatConsole console) {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				console.getController().setSoundDisabled(true);
				console.getController().getConnector().getChatService()
						.getChatLogger().parseFile(
								new ChatEventParseListener() {

									public void onNewEventParsed(
											final ChatEvent event) {
										console.getDisplay().syncExec(
												new Runnable() {
													public void run() {
														try {
															if (!console
																	.isDisposed()) {
																console
																		.getController()
																		.onChatEvent(
																				event);
															}
														} catch (Throwable t) {
															console
																	.getController()
																	.getConnector()
																	.onError(
																			"appendPreviousChatsToController",
																			t);
														}
													}
												});
									}

									public void onParseCompleted() {
										console.getController()
												.setSoundDisabled(false);
									}
								});
			}
		});
	}

	/**
	 * Returns the character at the specified position in the StyledText.
	 */
	public static char charAt(StyledText text, int position) {
		return text.getText(position, position + 1).charAt(0);
	}

	/**
	 * Returns null if the current position isn't quoted text, otherwise returns
	 * the text in quotes. Both single and double quotes are supported.
	 */
	public static String getQuotedText(StyledText text, int position) {
		try {
			int quoteStart = -1;
			int quoteEnd = -1;

			int currentPosition = position;
			char currentChar = charAt(text, currentPosition);

			if (currentChar == '\"' || currentChar == '\'') {
				quoteEnd = position;
				currentChar = charAt(text, --currentPosition);
			}

			while (currentChar != '\"' && currentChar != '\'') {
				if (currentChar == '\r' || currentChar == '\n') {
					return null;
				}
				currentChar = charAt(text, --currentPosition);
			}

			quoteStart = currentPosition;

			if (quoteEnd == -1) {
				currentPosition = position + 1;
				currentChar = charAt(text, currentPosition);

				while (currentChar != '\"' && currentChar != '\'') {
					if (currentChar == '\r' || currentChar == '\n') {
						return null;
					}
					currentChar = charAt(text, ++currentPosition);
				}

				quoteEnd = currentPosition;
			}
			return text.getText(quoteStart + 1, quoteEnd - 1);
		} catch (Exception e) {
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
			char currentChar = charAt(text, currentPosition);

			while (currentPosition > 0 && !Character.isWhitespace(currentChar)) {
				currentChar = charAt(text, --currentPosition);
			}

			lineStart = currentPosition;

			currentPosition = position;
			currentChar = charAt(text, currentPosition);

			while (currentPosition < text.getCharCount()
					&& !Character.isWhitespace(currentChar)) {
				currentChar = charAt(text, ++currentPosition);
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
			char currentChar = charAt(text, currentPosition);

			while (currentPosition > 0 && !Character.isWhitespace(currentChar)) {
				currentChar = charAt(text, --currentPosition);
			}

			lineStart = currentPosition;

			currentPosition = position;
			currentChar = charAt(text, currentPosition);

			while (currentPosition < text.getCharCount()
					&& !Character.isWhitespace(currentChar)) {
				currentChar = charAt(text, ++currentPosition);
			}
			lineEnd = currentPosition;
			result = text.getText(lineStart + 1, lineEnd - 1);

			// now check to see if its a wrap
			while (Character.isWhitespace(currentChar)
					&& currentPosition < text.getCharCount()) {
				currentChar = charAt(text, ++currentPosition);
			}
			while (currentChar == '\\') {
				charAt(text, ++currentPosition);
				while (Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = charAt(text, ++currentPosition);
				}

				lineStart = currentPosition - 1;
				while (!Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = charAt(text, ++currentPosition);
				}

				lineEnd = currentPosition;
				result += text.getText(lineStart + 1, lineEnd - 1);

				while (Character.isWhitespace(currentChar)
						&& currentPosition < text.getCharCount()) {
					currentChar = charAt(text, ++currentPosition);
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
