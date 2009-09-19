package raptor.util;

public class RaptorStringUtils {

	/**
	 * Expresses value in terms of digits. If value is 0 and digits is 2 "00"
	 * will be returned. If value is 1 and digits is 2 "01" is returned. If
	 * value is 123 and digits is 2 then "XX" is returned.
	 */
	public static String defaultTimeString(int value, int digits) {
		String valueAsString = "" + value;
		String result = null;
		if (valueAsString.length() > digits) {
			for (int i = 0; i < digits; i++) {
				result += "X";
			}
		} else if (valueAsString.length() < digits) {
			result = valueAsString;
			while (result.length() < digits) {
				result = "0" + result;
			}
		}
		else {
			result = valueAsString;
		}
		return result;
	}

	/**
	 * Returns a String representing the contents of the array delimited by the
	 * specified delimiter.
	 */
	public static String toDelimitedString(Object[] array, String delimiter) {
		StringBuilder result = new StringBuilder(100);
		if (array == null) {
			result.append("null");
		} else if (array.length == 0) {
			result.append("empty");
		} else {
			for (int i = 0; i < array.length; i++) {
				result.append(array[i].toString());

				if (i < array.length - 1) {
					result.append(delimiter);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Returns a String representing the contents of the array delimited by the
	 * a comma.
	 */
	public static String toDelimitedString(Object[] array) {
		return toDelimitedString(array, ",");
	}

	/**
	 * A fast utility method that does'nt use REGEX and returns the number of
	 * character in source.
	 */
	public static int count(String source, char character) {
		int result = 0;
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == character) {
				result++;
			}
		}
		return result;
	}

	/**
	 * A fast utility method that does'nt use REGEX and removes all of toRemove
	 * from source.
	 */
	public static String removeAll(String source, char toRemove) {
		int toRemoveIndex = source.indexOf(toRemove);

		if (toRemoveIndex != -1) {
			StringBuilder result = new StringBuilder(source.length());
			int sourceIndex = 0;

			while (toRemoveIndex != -1) {
				result.append(source.substring(sourceIndex, toRemoveIndex));
				sourceIndex = toRemoveIndex + 1;

				toRemoveIndex = source.indexOf(toRemove, sourceIndex);
				if (toRemoveIndex == -1) {
					result.append(source
							.substring(sourceIndex, source.length()));
				}
			}
			return result.toString();
		} else {
			return source;
		}
	}

	/**
	 * A fast utility method that does'nt use REGEX and removes all of the
	 * specified strToRemove from source.
	 */
	public static String removeAll(String source, String strToRemove) {
		int toRemoveIndex = source.indexOf(strToRemove);

		if (toRemoveIndex != -1) {
			StringBuilder result = new StringBuilder(source.length());
			int sourceIndex = 0;

			while (toRemoveIndex != -1) {
				result.append(source.substring(sourceIndex, toRemoveIndex));
				sourceIndex = toRemoveIndex + strToRemove.length();

				toRemoveIndex = source.indexOf(strToRemove, sourceIndex);
				if (toRemoveIndex == -1) {
					result.append(source
							.substring(sourceIndex, source.length()));
				}
			}
			return result.toString();
		} else {
			return source;
		}
	}

	public static String replaceAll(String source, String strToReplace,
			String replacement) {
		int toRemoveIndex = source.indexOf(strToReplace);

		if (toRemoveIndex != -1) {
			StringBuilder result = new StringBuilder(source.length());
			int sourceIndex = 0;

			while (toRemoveIndex != -1) {
				result.append(source.substring(sourceIndex, toRemoveIndex)
						+ replacement);
				sourceIndex = toRemoveIndex + strToReplace.length();

				toRemoveIndex = source.indexOf(strToReplace, sourceIndex);
				if (toRemoveIndex == -1) {
					result.append(source
							.substring(sourceIndex, source.length()));
				}
			}
			return result.toString();
		} else {
			return source;
		}
	}

}
