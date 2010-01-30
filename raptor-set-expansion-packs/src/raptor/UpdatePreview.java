package raptor;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

public class UpdatePreview {
	public static final String[] PIECE_TO_NAME = { "", "wp", "wb", "wn", "wr",
			"wq", "wk", "bp", "bb", "bn", "br", "bq", "bk" };

	public static void main(String[] args) throws Throwable {
		File sets = new File("sets");
		File[] files = sets.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				return file.isDirectory() && !file.getName().startsWith(".svn");
			}
		});

		for (File file : files) {
			System.err.println("Creating images for " + file.getName());
			File previewDir = new File("preview/" + file.getName());
			if (!previewDir.exists()) {
				previewDir.mkdirs();
			}
			for (int i = 1; i < PIECE_TO_NAME.length; i++) {

				ImageData data = new ImageData(file.getAbsolutePath() + "/50/"
						+ PIECE_TO_NAME[i] + ".png");
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] { data };
				loader.save(previewDir.getAbsolutePath() + "/"
						+ PIECE_TO_NAME[i] + ".png", SWT.IMAGE_PNG);
			}
		}
	}
}
