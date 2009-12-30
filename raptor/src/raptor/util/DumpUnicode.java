package raptor.util;

public class DumpUnicode {

	public static void main(String[] args) {
		for (int i = 0; i < args[0].length(); i++) {
			char character = args[0].charAt(i);
			System.err.println("\\u" + Integer.toString(character, 16));

		}
	}

}
