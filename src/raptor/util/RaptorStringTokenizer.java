package raptor.util;

/**
 * It works just like java.util.StringTokenizer with some differences. You can
 * specify if blocks of delimiters are eaten. For instance:
 * "test1 test2 test3  test4" will return: "test1" "test2" "test3" "" "test4" if
 * not eating blocks of delimiters and it will return "test1" "test2" "test3"
 * "test4" if isEatingBlocksOfDelimiters.
 * 
 * You can also change the delimiters between calls by invoking
 * changeDelimiters.
 * 
 * You can also obtain what is left to tokenize by calling getWhatsLeft() You
 * can also obtain the number of current index its on in the string passed into
 * the constructor with getCurrentCharIndex.
 */
public class RaptorStringTokenizer {

	private String source;

	private String delimiters;

	private boolean isEatingBlocksOfDelimiters = false;

	private int currentIndex = 0;

	public RaptorStringTokenizer(String string, String delimiters) {
		source = string;
		this.delimiters = delimiters;
	}

	public RaptorStringTokenizer(String string, String delimiters,
			boolean isEatingBlocksOfDelimiters) {
		source = string;
		this.delimiters = delimiters;
		this.isEatingBlocksOfDelimiters = isEatingBlocksOfDelimiters;
	}

	public boolean isEatingBlocksOfDelimiters() {
		return isEatingBlocksOfDelimiters;
	}

	public void setEatingBlocksOfDelimiters(boolean isEatingBlocksOfDelimiters) {
		this.isEatingBlocksOfDelimiters = isEatingBlocksOfDelimiters;
	}

	public void changeDelimiters(String newDelimiters) {
		synchronized (this) {
			delimiters = newDelimiters;
		}
	}

	public boolean hasMoreTokens() {
		synchronized (this) {
			if (isEmpty()) {
				return false;
			} else {
				if (isEatingBlocksOfDelimiters()) {
					trimStartingDelimiters();
				}
				return !isEmpty();
			}
		}
	}

	private boolean isEmpty() {
		return currentIndex >= source.length();
	}

	private void trimStartingDelimiters() {
		while (!isEmpty()
				&& delimiters.indexOf(source.charAt(currentIndex)) != -1) {
			currentIndex++;
		}
	}

	/**
	 * Returns null if there is nothing left.
	 */
	public String nextToken() {
		String result = null;
		synchronized (this) {
			if (isEmpty()) {
				return null;
			} else {
				if (isEatingBlocksOfDelimiters()) {
					trimStartingDelimiters();
				}

				int nearestDelimeter = -1;
				for (int i = 0; i < delimiters.length(); i++) {
					int delimiter = source.indexOf(delimiters.charAt(i),
							currentIndex);
					if (nearestDelimeter == -1
							|| (delimiter != -1 && delimiter < nearestDelimeter)) {
						nearestDelimeter = delimiter;
					}
				}

				if (nearestDelimeter == -1) {
					result = source.substring(currentIndex);
					currentIndex = source.length();
				} else {
					result = source.substring(currentIndex, nearestDelimeter);
					currentIndex = nearestDelimeter + 1;
					if (isEatingBlocksOfDelimiters()) {
						// Now trim all the delimiters that are at the begining
						// of
						// source.
						trimStartingDelimiters();
					}
				}
			}
		}
		return result;
	}

	public String getWhatsLeft() {
		if (isEmpty()) {
			return "";
		} else {
			return source.substring(currentIndex);
		}
	}

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public int indexInWhatsLeft(char token) {
		return source.indexOf(token, currentIndex);
	}

	public String substringSource(int start, int end) {
		return source.substring(start, end);
	}

}
