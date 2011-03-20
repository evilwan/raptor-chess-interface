package raptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class SquareCreator {
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
				.println("Square creator uses Batik 1.7 to convert svg files into pgn files Raptor can understand.");
		if (args.length < 2 || args[0].equals("?") || args[0].equals("help")
				|| args[0].equals("-help") || args[0].equals("-?")) {
			System.err.println("Usage: squareCreator sourceDirectoryName squareName");
			System.err
					.println("\tThe following files corresponding to the square must be in sourceDirectoryName:");
			System.err.println("\t\tlight.svg  (The light square svg file.");
			System.err.println("\t\tdark.svg  (The dark square svg file.");
			System.err
					.println("\tAfter the pgn files are created they will appear in the target/squareName directory. ");
			System.err
					.println("Before submitting your set you may include a author.txt file and a license.txt file which ");
			System.err
					.println("will show up in raptor preferences when people view your squares.");
		}
		createSquares(args[0], args[1]);
	}

	public static void createSquares(String sourceDirectory, String squareName)
			throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.println("Creating pngs from squareDirectory " + sourceDirectory + "..."
				+ " it will be named " + squareName);
		for (int i = 10; i <= 200; i += 1) {
			ProcessBuilder builder = new ProcessBuilder(new String[] { "java",
					"-jar", "batik-1.7/batik-rasterizer.jar", "-d",
					"target/" + squareName + "/" + i, "-w", "" + i, "-h", "" + i,
					sourceDirectory + "/dark.svg",
					sourceDirectory + "/light.svg" });

			Process process = builder.start();
			Thread outputDumper = new Thread(new OutputStreamDumper(
					process.getInputStream(), System.out));
			Thread errorDumper = new Thread(new OutputStreamDumper(
					process.getErrorStream(), System.err));
			outputDumper.start();
			errorDumper.start();
			int error = process.waitFor();

			if (error != 0) {
				System.err.println("Stopped due to error.");
				System.exit(1);
			}

			FileUtils.copyFiles(new File(sourceDirectory), new File("target/"
					+ squareName));

			System.out.println("Created pngs of size " + i + "x" + i);
		}
		System.out.println("Finished set conversion in "
				+ (System.currentTimeMillis() - startTime) / 1000.0
				+ " seconds");
		System.out.println("The chess set directory is located at: "
				+ "target/" + squareName);
	}
}
