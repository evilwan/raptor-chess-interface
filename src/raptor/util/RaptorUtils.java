package raptor.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;

public class RaptorUtils {

	/**
	 * Returns a String[][] containing all of the icon names.
	 */
	public static String[] getAllIconNames() {
		File file = new File(Raptor.RESOURCES_DIR + "icons");

		String[] names = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.endsWith(".png");
			}
		});

		Arrays.sort(names);

		String[] result = new String[names.length + 1];
		result[0] = "<None>";
		for (int i = 0; i < names.length; i++) {
			String name = StringUtils.remove(names[i], ".png");
			result[i + 1] = name;
		}
		return result;

	}
}
