package raptor.util;

import java.util.Comparator;

public class IntegerComparator implements Comparator<String> {

	public static int getInteger(String string) {
		int result = 0;
		try {
			result = Integer.parseInt(string);
		} catch (NumberFormatException nfe) {
		}
		return result;

	}

	public int compare(String string1, String string2) {
		int value1 = getInteger(string1);
		int value2 = getInteger(string2);
		return value1 < value2 ? 1 : value1 == value2 ? 0 : -1;
	}
}
