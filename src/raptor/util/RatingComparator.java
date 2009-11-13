package raptor.util;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

public class RatingComparator implements Comparator<String> {

	public static int getRatingAsInt(String string) {
		if (string.equals("----")) {
			return 0;
		} else if (string.equals("++++")) {
			return 1;
		} else {
			int result = 0;
			try {
				result = Integer.parseInt(StringUtils.replaceChars(string,
						"EP", ""));
			} catch (NumberFormatException nfe) {
			}
			return result;
		}
	}

	public int compare(String string1, String string2) {
		int rating1 = getRatingAsInt(string1);
		int rating2 = getRatingAsInt(string2);
		return rating1 < rating2 ? 1 : rating1 == rating2 ? 0 : -1;
	}
}
