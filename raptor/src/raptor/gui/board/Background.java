package raptor.gui.board;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import raptor.service.SWTService;

public class Background {
	private static final Log LOG = LogFactory.getLog(Background.class);
	public static final String BACKGROUND_DIR = "resources/common/square/";
	public static final int LIGHT_IMAGE_INDEX = 0;
	public static final int DARK_IMAGE_INDEX = 1;

	public static String[] getBackgrounds() {
		List<String> result = new LinkedList<String>();

		File file = new File(BACKGROUND_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (int i = 0; i < files.length; i++) {
			StringTokenizer tok = new StringTokenizer(files[i].getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	public static Image[] getImageMolds(String name) {
		LOG.info("Loading background stock" + name);
		long startTine = System.currentTimeMillis();

		String prefix = BACKGROUND_DIR + name + "/";
		String[] fileNames = new File(prefix).list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.indexOf(".") > 1;
			}

		});
		String suffix = fileNames[0].substring(fileNames[0].lastIndexOf('.'),
				fileNames[0].length());

		String darkImageFileName = prefix + "dark" + suffix;
		String darkImageKey = name + "_dark_stock";

		String lightImageFileName = prefix + "light" + suffix;
		String lightImageKey = name + "_light_stock";

		Image darkImage = SWTService.getInstance().getImageRegistry().get(
				darkImageKey);
		Image lightImage = SWTService.getInstance().getImageRegistry().get(
				lightImageKey);

		if (darkImage == null) {
			darkImage = new Image(Display.getCurrent(), darkImageFileName);
			SWTService.getInstance().getImageRegistry().put(darkImageKey,
					darkImage);
		}
		if (lightImage == null) {
			lightImage = new Image(Display.getCurrent(), lightImageFileName);
			SWTService.getInstance().getImageRegistry().put(lightImageKey,
					lightImage);
		}

		LOG.info("Loaded background stock " + name + " "
				+ (System.currentTimeMillis() - startTine) + "ms");

		return new Image[] { lightImage, darkImage };
	}

	private String name;

	private ImageRegistry imageRegistry = new ImageRegistry();

	public Background(String name) {
		this.name = name;
	}

	public void dispose() {
		imageRegistry.dispose();
	}

	@Override
	public boolean equals(Object o) {
		if (o != null) {
			return getName().equals(((Background) o).getName());
		} else {
			return false;
		}

	}

	public Image getImageMold(boolean isLight) {
		String imageKey = name + "_" + (isLight ? "light" : "dark") + "_stock";
		Image image = SWTService.getInstance().getImageRegistry().get(imageKey);
		if (image == null) {
			image = isLight ? getImageMolds(name)[LIGHT_IMAGE_INDEX]
					: getImageMolds(name)[DARK_IMAGE_INDEX];
		}
		return image;
	}

	public String getName() {
		return name;
	}

	public Image getScaledImage(boolean isLight, int width, int height) {

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		String key = getName() + "_" + isLight + "_" + width + "x" + height;

		Image result = imageRegistry.get(key);

		if (result == null) {
			result = new Image(Display.getCurrent(), getImageMold(isLight)
					.getImageData().scaledTo(width, height));
			imageRegistry.put(key, result);
			return result;
		} else {
			return result;
		}
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

}
