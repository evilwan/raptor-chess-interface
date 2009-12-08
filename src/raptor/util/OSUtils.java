package raptor.util;

public class OSUtils {
	public static boolean isLikelyLinux() {
		String osName = System.getProperty("os.name");
		if (!osName.startsWith("Mac OS") && !osName.startsWith("Windows")) {
			return true;
		}
		return false;
	}

	public static boolean isLikelyOSX() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			return true;
		}
		return false;
	}

	public static boolean isLikelyWindows() {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) {
			return true;
		}
		return false;
	}

}
