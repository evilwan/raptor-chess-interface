package raptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class SetCreator {
	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };

	public static class OutputStreamDumper implements Runnable {
		InputStream inputStreamToDump;
		PrintStream destinationStream;

		public OutputStreamDumper(InputStream inputStreamToDump,
				PrintStream destinationStream) {
			this.inputStreamToDump = inputStreamToDump;
			this.destinationStream = destinationStream;
		}

		public void run() {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						inputStreamToDump));
				String line = null;
				while ((line = reader.readLine()) != null) {
					destinationStream.println(line);
				}
			} catch (IOException ioe) {
				System.err.println(ioe);
			} finally {
				try {
					reader.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	/**
	 * @param args
	 *            directoryName authorName licenseFile
	 */
	public static void main(String[] args) throws Exception {
		System.err
				.println("Set creator uses Batik 1.7 to convert svg files into pgn files Raptor can understand.");
		if (args.length < 2 || args[0].equals("?") || args[0].equals("help")
				|| args[0].equals("-help") || args[0].equals("-?")) {
			System.err.println("Usage: setCreator sourceDirectoryName setName");
			System.err
					.println("\tThe following files corresponding to the set must be in sourceDirectoryName:");
			for (int i = 1; i < PIECE_TO_NAME.length; i++) {
				System.err.println("\t\t" + PIECE_TO_NAME[i] + ".svg");
			}
			System.err
					.println("\tAfter the pgn files are created they will appear in the target/setName directory. ");
			System.err
					.println("Before submitting your set you may include a author.txt file and a license.txt file which ");
			System.err
					.println("will show up in raptor preferences when people view your set.");
			for (int i = 1; i < PIECE_TO_NAME.length; i++) {
				System.err.println("\t\t" + PIECE_TO_NAME[i] + ".svg");
			}
		}
		createSet(args[0], args[1]);
	}

	public static void createAllSets() throws Exception {
		File file = new File("set");
		File[] sets = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory()
						&& !pathname.getName().startsWith(".");
			}
		});

		for (File setDir : sets) {
			createSet(setDir.getAbsolutePath(), setDir.getName());
		}
	}

	public static void createSet(String sourceDirectory, String setName)
			throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.println("Creating pngs from set " + sourceDirectory + "..."
				+ " set is named " + setName);
		for (int i = 8; i <= 100; i += 2) {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "java",
					"-jar", "batik-1.7/batik-rasterizer.jar", "-d",
					"target/" + setName + "/" + i, "-w", "" + i, "-h", "" + i,
					getSVGChessPieceName(sourceDirectory, 1),
					getSVGChessPieceName(sourceDirectory, 2),
					getSVGChessPieceName(sourceDirectory, 3),
					getSVGChessPieceName(sourceDirectory, 4),
					getSVGChessPieceName(sourceDirectory, 5),
					getSVGChessPieceName(sourceDirectory, 6),
					getSVGChessPieceName(sourceDirectory, 7),
					getSVGChessPieceName(sourceDirectory, 8),
					getSVGChessPieceName(sourceDirectory, 9),
					getSVGChessPieceName(sourceDirectory, 10),
					getSVGChessPieceName(sourceDirectory, 11),
					getSVGChessPieceName(sourceDirectory, 12) });

			Process process = builder.start();
			Thread outputDumper = new Thread(new OutputStreamDumper(process
					.getInputStream(), System.out));
			Thread errorDumper = new Thread(new OutputStreamDumper(process
					.getErrorStream(), System.err));
			outputDumper.start();
			errorDumper.start();
			int error = process.waitFor();

			if (error != 0) {
				System.err.println("Stopped due to error.");
				System.exit(1);
			}

			FileUtils.copyFiles(new File(sourceDirectory), new File("target/"
					+ setName));

			System.out.println("Created pngs of size " + i + "x" + i);
		}
		System.out.println("Finished set conversion in "
				+ (System.currentTimeMillis() - startTime) / 1000.0
				+ " seconds");
		System.out.println("The chess set directory is located at: "
				+ "target/" + setName);
	}

	/**
	 * Returns the path to the specified svg chess piece.
	 */
	public static String getSVGChessPieceName(String sourceDirectory, int piece) {
		return sourceDirectory + "/" + PIECE_TO_NAME[piece] + ".svg";
	}

}
