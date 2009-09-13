package raptor.gui.board;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Background {
	public static final String BACKGROUND_DIR = "resources/common/background/";
	private String name;

	private Image darkSquareImage;

	private Image lightSquareImage;

	private ImageRegistry imageRegistry = new ImageRegistry();

	public Background(String name) {
		this.name = name;
		initImages();
	}
	
	public void dispose() {
		imageRegistry.dispose();
		darkSquareImage.dispose();
		lightSquareImage.dispose();
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

		darkSquareImage = new Image(Display.getCurrent(), BACKGROUND_DIR + darkImage);
		lightSquareImage = new Image(Display.getCurrent(), BACKGROUND_DIR + lightImage);
	}

	public Image getScaledImage(boolean isLight, int width, int height) {

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}
		
		String key = getName() + "_" + isLight + "_" + width + "x" + height;
		
		Image result = imageRegistry.get(key);
		
		if (result == null) {
			Image source = isLight ? lightSquareImage : darkSquareImage;
			result =  new Image(Display.getCurrent(), source.getImageData().scaledTo(width, height));
			imageRegistry.put(key, result);
			return result;
		}
		else {
			return result;
		}
	}

}
