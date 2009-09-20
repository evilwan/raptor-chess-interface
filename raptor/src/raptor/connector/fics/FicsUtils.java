package raptor.connector.fics;

import java.util.StringTokenizer;

import raptor.game.Game;

public class FicsUtils {
	public static final String LEGAL_CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 "
			+ "!@#$%^&*()-=_+`~[{]}\\|;:'\",<.>/?";

	public static final String BLITZ_IDENTIFIER = "blitz";

	public static final String LIGHTNING_IDENTIFIER = "lightning";

	public static final String WILD_IDENTIFIER = "wild";

	public static final String STANDARD_IDENTIFIER = "standard";

	public static final String SUICIDE_IDENTIFIER = "suicide";

	public static final String ATOMIC_IDENTIFIER = "atomic";

	public static final String BUGHOUSE_IDENTIFIER = "bughouse";

	public static final String LOSERS_IDENTIFIER = "losers";

	public static final String CRAZYHOUSE_IDENTIFIER = "crazyhouse";

	public static final String UNTIMED_IDENTIFIER = "untimed";

	public static final int UNTIMED_GAME_TYPE = 999;

	/**
	 * Returns the game type constant for the specified identifier.
	 * 
	 */
	public static int identifierToGameType(String identifier) {
		int result = -1;

		if (identifier.indexOf(SUICIDE_IDENTIFIER) != -1)
			result = Game.SUCIDE;
		else if (identifier.indexOf(BUGHOUSE_IDENTIFIER) != -1)
			result = Game.BUGHOUSE;
		else if (identifier.indexOf(CRAZYHOUSE_IDENTIFIER) != -1)
			result = Game.CRAZY_HOUSE;
		else if (identifier.indexOf(STANDARD_IDENTIFIER) != -1)
			result = Game.STANDARD;
		else if (identifier.indexOf(WILD_IDENTIFIER) != -1)
			result = Game.WILD;
		else if (identifier.indexOf(LIGHTNING_IDENTIFIER) != -1)
			result = Game.LIGHTNING;
		else if (identifier.indexOf(BLITZ_IDENTIFIER) != -1)
			result = Game.BLITZ;
		else if (identifier.indexOf(ATOMIC_IDENTIFIER) != -1)
			result = Game.ATOMIC;
		else if (identifier.indexOf(LOSERS_IDENTIFIER) != -1)
			result = Game.LOSERS;
		else if (identifier.indexOf(UNTIMED_IDENTIFIER) != -1)
			result = UNTIMED_GAME_TYPE;

		else
			throw new IllegalArgumentException("Unknown identifier "
					+ identifier
					+ " encountered. Please notify someone on the raptor team "
					+ "so they can implement this new game type.");

		return result;
	}

	public static int removeRatingDecorators(String rating) {
		String ratingWithoutDecorators = "";

		for (int i = 0; i < rating.length(); i++) {
			if (Character.isDigit(rating.charAt(i))) {
				ratingWithoutDecorators += rating.charAt(i);
			}
		}
		return Integer.parseInt(ratingWithoutDecorators);
	}

	/**
	 * Filters out illegal chars, and appends a \n to the passed in message.
	 * This also converts unicode chars into Maciejg format. See
	 * maciejgFormatToUnicode for more info.
	 */
	public static void filterOutbound(StringBuilder message) {
		for (int i = 0; i < message.length(); i++) {
			char currentChar = message.charAt(i);
			if (LEGAL_CHARACTERS.indexOf(currentChar) == -1) {
				if (currentChar > 256) {
					int charAsInt = (int) currentChar;
					String stringVersion = Integer.toString(charAsInt, 16);
					String replacement = "&#x" + stringVersion + ";";
					message.replace(i, i + 1, replacement);
					i += replacement.length() - 1;
				} else {
					message.deleteCharAt(i);
					i--;
				}
			}
		}
		message.append('\n');
	}

	public static String removeTitles(String playerName) {
		StringTokenizer stringtokenizer = new StringTokenizer(playerName,
				"()~!@#$%^&*_+|}{';/., :[]");
		if (stringtokenizer.hasMoreTokens())
			return stringtokenizer.nextToken();
		else
			return playerName;
	}

	/**
	 * Maciejg format, named after him because of his finger notes. Unicode
	 * chars are represented as &#x3b1; &#x3b2; &#x3b3; &#x3b4; &#x3b5; &#x3b6;
	 * unicode equivalent \u03B1,\U03B2,...
	 */
	public static String maciejgFormatToUnicode(String inputString) {
		StringBuilder builder = new StringBuilder(inputString);
		int unicodePrefix = 0;
		while ((unicodePrefix = builder.indexOf("&#x", unicodePrefix)) != -1) {
			int endIndex = builder.indexOf(";", unicodePrefix);
			if (endIndex != -1 && (endIndex - unicodePrefix) <= 8) {
				String unicodeHex = builder.substring(unicodePrefix + 3,
						endIndex).toUpperCase();
				try {
					int intValue = Integer.parseInt(unicodeHex, 16);
					String replacement = new String(
							new char[] { (char) intValue });
					builder.replace(unicodePrefix, unicodePrefix + 7,
							replacement);
				} catch (NumberFormatException nfe) {
					unicodePrefix = endIndex;
				}
			}
		}
		return builder.toString();
	}
}
