package raptor.util;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;

import raptor.swt.chess.ChessSquareBackground;
import raptor.swt.chess.ChessSet;

public class ImageUtil {

	public static void main(String args[]) throws Exception {

		makeAllSquares100x100Pgn();
		makeAllSets100x100PgnTransparent();

	}

	public static void removeBlackness(String setName) {
		long startTine = System.currentTimeMillis();

		String prefix = ChessSet.SET_DIR + setName + "/";
		System.out.println("Removing blackness from set " + setName);

		String[] fileNames = new File(prefix).list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.indexOf(".") > 1;
			}
		});
		String suffix = fileNames[0].substring(fileNames[0].lastIndexOf('.'),
				fileNames[0].length());

		ImageData[] images = new ImageData[ChessSet.PIECE_TO_NAME.length];

		for (int j = 1; j < ChessSet.PIECE_TO_NAME.length; j++) {
			String fileName = prefix + ChessSet.PIECE_TO_NAME[j] + suffix;

			System.out.println("Operating on " + fileName);
			for (int i = 1; i < 35; i++) {
				images[j] = new ImageData(fileName);
				images[j].transparentPixel = images[j].palette
						.getPixel(new RGB(i, i, i));
				ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] { images[j] };
				imageLoader.save(fileName, SWT.IMAGE_PNG);
			}
		}
	}

	public static void makeAllSets100x100PgnTransparent() {
		File setsDir = new File(ChessSet.SET_DIR);
		File[] dirs = setsDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return !arg1.startsWith(".")
						&& new File(arg0, arg1).isDirectory();
			}
		});

		for (int i = 0; i < dirs.length; i++) {
			String setName = dirs[i].getName();
			long startTine = System.currentTimeMillis();

			String prefix = ChessSet.SET_DIR + setName + "/";
			System.out.println("Processing Chess Set" + prefix);

			String[] fileNames = new File(prefix).list(new FilenameFilter() {
				public boolean accept(File arg0, String arg1) {
					return arg1.indexOf(".") > 1;
				}
			});
			String suffix = fileNames[0].substring(fileNames[0]
					.lastIndexOf('.'), fileNames[0].length());

			ImageData[] images = new ImageData[ChessSet.PIECE_TO_NAME.length];

			for (int j = 1; j < ChessSet.PIECE_TO_NAME.length; j++) {
				String fileName = prefix + ChessSet.PIECE_TO_NAME[j] + suffix;
				images[j] = new ImageData(fileName);

				if (suffix.equalsIgnoreCase(".bmp")) {
					if (fileName.contains("KnightsThemePack-")) {
						int transPixel = images[j].palette.getPixel(new RGB(0,
								0, 0));
						images[j].transparentPixel = transPixel;
					} else {
						int transPixel = images[j].palette.getPixel(new RGB(
								255, 255, 0));
						images[j].transparentPixel = transPixel;
					}
				}
			}

			if (!suffix.equalsIgnoreCase(".png")
					|| (images[1].width != 100 && images[1].height != 100)) {
				for (int j = 1; j < images.length; j++) {
					File oldFile = new File(prefix + ChessSet.PIECE_TO_NAME[j]
							+ suffix);
					oldFile.delete();

					String fileName = prefix + "" + ChessSet.PIECE_TO_NAME[j]
							+ ".png";
					ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { images[j].scaledTo(
							100, 100) };
					System.err.println("\tSaved " + fileName);
					imageLoader.save(fileName, SWT.IMAGE_PNG);
				}
			} else {
				System.err.println("\tSet is already standardized.");
			}

		}
	}

	public static void makeAllSquares100x100Pgn() {
		File backgroundDir = new File(ChessSquareBackground.BACKGROUND_DIR);
		File[] dirs = backgroundDir.listFiles(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return !arg1.startsWith(".")
						&& new File(arg0, arg1).isDirectory();
			}

		});

		for (int i = 0; i < dirs.length; i++) {
			String prefix = ChessSquareBackground.BACKGROUND_DIR + dirs[i].getName() + "/";
			System.err.println("Processing Square Background " + prefix);

			String[] fileNames = new File(prefix).list(new FilenameFilter() {
				public boolean accept(File arg0, String arg1) {
					return arg1.indexOf(".") > 1;
				}
			});

			if (fileNames.length > 0) {
				String oldSuffix = fileNames[0].substring(fileNames[0]
						.lastIndexOf('.'), fileNames[0].length());

				ImageData lightSquare = new ImageData(prefix + "light"
						+ oldSuffix);
				ImageData drakSquare = new ImageData(prefix + "dark"
						+ oldSuffix);

				if ((lightSquare.width != 100 && lightSquare.height != 100)
						|| !oldSuffix.equalsIgnoreCase(".png")) {

					File oldFile = new File(prefix + "light" + oldSuffix);
					oldFile.delete();
					oldFile = new File(prefix + "dark" + oldSuffix);
					oldFile.delete();

					ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { lightSquare.scaledTo(
							100, 100) };
					imageLoader.save(prefix + "/light" + ".png", SWT.IMAGE_PNG);
					System.err.println("\tSaved " + prefix + "light" + ".png");

					imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] { drakSquare.scaledTo(
							100, 100) };
					imageLoader.save(prefix + "/dark" + ".png", SWT.IMAGE_PNG);
					System.err.println("\tSaved " + prefix + "dark" + ".png");
				} else {
					System.err
							.println("\tSquare Background is already standardized.");
				}
			} else {
				System.err.println("\t Directory was empty, skipping.");
			}
		}
	}

}
