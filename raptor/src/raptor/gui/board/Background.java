package raptor.gui.board;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

public class Background {
	public static final String BACKGROUND_DIR = "resources/common/background/";
	private String name;

	private Image darkSquareImage;

	private Image lightSquareImage;
	private Device device;

	public Background(Device device, String name) {
		this.name = name;
		this.device = device;
		initImages();
	}

	public static String[] getBackgrounds() {
		List<String> result = new LinkedList<String>();

		File file = new File(BACKGROUND_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return arg1.startsWith("SQUARE.")
						&& arg1.indexOf("LIGHT") != -1;
			}

		});

		Collections.sort(result);
		return (String[]) result.toArray(new String[0]);
	}

	public boolean equals(Object o) {
		if (o != null) {
			return getName().equals(((Background) o).getName());
		} else {
			return false;
		}

	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String getName() {
		return name;
	}

	private void initImages() {
		String suffix = "BMP";
		String darkImage = "SQUARE." + name + ".DARK." + suffix;
		String lightImage = "SQUARE." + name + ".LIGHT." + suffix;

		darkSquareImage = new Image(device, BACKGROUND_DIR + darkImage);
		lightSquareImage = new Image(device, BACKGROUND_DIR + lightImage);
	}

	public Image getScaledImage(boolean isLight, int width, int height) {

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		Image source = isLight ? lightSquareImage : darkSquareImage;

		return new Image(device, source.getImageData().scaledTo(width, height));
	}

}
