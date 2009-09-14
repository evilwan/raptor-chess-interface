package util;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import raptor.gui.board.Background;

public class ImageUtil {

	public static void main(String args[]) throws Exception {

		// String setName = "Spacial";
		// long startTine = System.currentTimeMillis();
		//
		// String prefix = Set.SET_DIR + setName + "/";
		// String[] fileNames = new File(prefix).list(new FilenameFilter() {
		// public boolean accept(File arg0, String arg1) {
		// return arg1.indexOf(".") > 1;
		// }
		// });
		// String suffix = fileNames[0].substring(fileNames[0].lastIndexOf('.'),
		// fileNames[0].length());
		//
		// ImageData[] images = new ImageData[Set.PIECE_TO_NAME.length];
		//
		// for (int i = 1; i < Set.PIECE_TO_NAME.length; i++) {
		// String fileName = prefix + Set.PIECE_TO_NAME[i] + suffix;
		// images[i] = new ImageData(fileName);
		// }
		//
		// File newDir = new File(prefix + "100x100");
		// if (newDir.exists()) {
		// newDir.delete();
		// }
		// newDir.mkdir();
		// for (int i = 1; i < images.length; i++) {
		// String fileName = prefix + "100x100/" + Set.PIECE_TO_NAME[i] +
		// suffix;
		// ImageLoader imageLoader = new ImageLoader();
		// imageLoader.data = new ImageData[] { images[i].scaledTo(100, 100) };
		// imageLoader.save(fileName, SWT.IMAGE_PNG);
		// }

		String backgroundName = "Wood3";

		String prefix = Background.BACKGROUND_DIR + backgroundName + "/";
		String[] fileNames = new File(prefix).list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.indexOf(".") > 1;
			}
		});
		String suffix = fileNames[0].substring(fileNames[0].lastIndexOf('.'),
				fileNames[0].length());

		ImageData lightSquare = new ImageData(prefix + "light" + suffix);
		ImageData drakSquare = new ImageData(prefix + "dark" + suffix);

		File newDir = new File(prefix + "/100x100");
		if (newDir.exists()) {
			newDir.delete();
		}
		newDir.mkdir();

		ImageLoader imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { lightSquare.scaledTo(100, 100) };
		imageLoader.save(prefix + "/100x100/light" + suffix, SWT.IMAGE_JPEG);

		imageLoader = new ImageLoader();
		imageLoader.data = new ImageData[] { drakSquare.scaledTo(100, 100) };
		imageLoader.save(prefix + "/100x100/dark" + suffix, SWT.IMAGE_JPEG);
	}

}
