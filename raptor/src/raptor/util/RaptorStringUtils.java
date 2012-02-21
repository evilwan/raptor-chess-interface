/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.util;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class RaptorStringUtils {

	protected static final SecureRandom random = new SecureRandom();

	protected static final String WORD_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ134567890";

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
	 * Expresses value in terms of digits. If value is 0 and digits is 2 "00"
	 * will be returned. If value is 1 and digits is 2 "01" is returned. If
	 * value is 123 and digits is 2 then "12" is returned.
	 */
	public static String defaultTimeString(int value, int digits) {
		String valueAsString = String.valueOf(value);
        String result = "";
        if (valueAsString.length() > digits) {
                for (int i = 0; i < digits; i++) {
                        result += valueAsString.charAt(i);
                }
        } else if (valueAsString.length() < digits) {
                result = valueAsString;
                while (result.length() < digits) {
                        result = "0" + result;
                }
        } else {
                result = valueAsString;
        }
        return result;

	}

	public static boolean equals(String[] array1, String[] array2) {
		boolean result = true;
		if (array1 == null && array2 != null) {
			result = false;
		} else if (array2 == null && array1 != null) {
			result = false;
		} else if (array1 == null && array2 == null) {
			result = true;
		} else if (array1.length != array2.length) {
			result = false;
		} else {
			for (int i = 0; i < array1.length; i++) {
				if (!StringUtils.equals(array1[i], array2[i])) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	public static String formatAsNumber(String stringOfDigits) {
		if (stringOfDigits.length() <= 3) {
			return stringOfDigits;
		} else {
			StringBuilder result = new StringBuilder(
					stringOfDigits.length() + 10);
			for (int i = 0; i < stringOfDigits.length(); i++) {
				if (i != 0 && (stringOfDigits.length() - i) % 3 == 0) {
					result.append(",");
				}
				result.append(stringOfDigits.charAt(i));
			}
			return result.toString();
		}
	}

	/**
	 * Generates a random word between 1 and n chars in length.
	 */
	public static String generateRandomWord(int n) {
		int chars = random.nextInt(n - 1);
		StringBuilder result = new StringBuilder(chars);
		for (int i = 0; i < chars + 1; i++) {
			result.append(WORD_CHARS
					.charAt(random.nextInt(WORD_CHARS.length())));
		}
		return result.toString();
	}

	/**
	 * Returns the boolean value for the specified value. If value is
	 * 'true','1',or 'on' true is returned. Otherwise false is returned.
	 */
	public static boolean getBooleanValue(String value) {
		return StringUtils.equals(value, "true")
				|| StringUtils.equals(value, "on")
				|| StringUtils.equals(value, "1");
	}

	public static String getFirstWord(String line) {
		int spaceIndex = line.indexOf(' ');
		if (spaceIndex == -1) {
			return line;
		} else {
			return line.substring(0, spaceIndex);
		}
	}

	/**
	 * Returns the value specified in bytes into megs. Currently only shows 2
	 * digits after the decimal and rounds half up.
	 */
	public static String getMegs(long bytes) {
		BigDecimal bigDecimal = new BigDecimal(bytes / 1048576.0);
		bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
		return bigDecimal.toString() + "Megs";
	}

	/**
	 * Returns an int[] from a string formatted in what toString(int[]) returns.
	 */
	public static int[] intArrayFromString(String string) {
		List<Integer> result = new ArrayList<Integer>(10);

		if (StringUtils.isNotBlank(string)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(string, ",",
					false);

			while (tok.hasMoreTokens()) {
				try {
					result.add(Integer.parseInt(tok.nextToken()));
				} catch (NumberFormatException nfe) {
					throw new IllegalStateException(nfe);
				}
			}
		}

		int[] arrayResult = new int[result.size()];
		for (int i = 0; i < result.size(); i++) {
			arrayResult[i] = result.get(i);
		}
		return arrayResult;
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

	public static String removeBeginingNewlines(String string) {
		if (string == null || string.length() == 0) {
			return string;
		} else {
			StringBuilder result = new StringBuilder(string);
			while (result.charAt(0) == '\n') {
				result.deleteCharAt(0);
			}
			return result.toString();
		}
	}

	public static String replaceAll(String source, String strToReplace,
			String replacement) {
		int toRemoveIndex = source.indexOf(strToReplace);

		if (toRemoveIndex != -1) {
			StringBuilder result = new StringBuilder(source.length());
			int sourceIndex = 0;

			while (toRemoveIndex != -1) {
                result.append(source.substring(sourceIndex, toRemoveIndex)).append(replacement);
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

	/**
	 * Returns a String[] of strings from a string that is formatted in what
	 * toString(String[]) returns.
	 */
	public static String[] stringArrayFromString(String string) {
		return stringArrayFromString(string, ',');
	}

	/**
	 * Returns a String[] of strings from a string that is formatted in what
	 * toString(String[]) returns.
	 */
	public static String[] stringArrayFromString(String string, char delimiter) {
		List<String> result = new ArrayList<String>(10);

		if (StringUtils.isNotBlank(string)) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(string, String.valueOf(delimiter), false);
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				result.add(token);
			}
		}
		return result.toArray(new String[0]);
	}

	/**
	 * Returns a String representing the contents of the array delimited by the
	 * a comma.
	 */
	public static String toDelimitedString(Object[] array) {
		return toDelimitedString(array, ",");
	}

	/**
	 * Returns a String representing the contents of the array delimited by the
	 * specified delimiter.
	 */
	public static String toDelimitedString(Object[] array, String delimiter) {
		StringBuilder result = new StringBuilder(100);
		if (array != null && array.length != 0) {
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
	 * Returns a comma delimited string containing values.
	 * 
	 * @param values
	 *            An int[]
	 * @return comma delimited result.
	 */
	public static String toString(int[] values) {
		String result = "";
		for (int value : values) {
			result += value + ",";
		}
		if (StringUtils.isNotBlank(result)) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	/**
	 * Returns a comma delimited array of strings.
	 */
	public static String toString(String[] values) {
		String valuesString = null; 
		StringBuffer valuesBuffer = new StringBuffer();
		for (String value : values) {
            valuesBuffer.append(value).append(",");
		}
		if (StringUtils.isNotBlank(valuesBuffer.toString())) {
			valuesString = valuesBuffer.substring(0, valuesBuffer.length() - 1);
		}
		return valuesString == null ? valuesBuffer.toString() : valuesString;
	}
}
