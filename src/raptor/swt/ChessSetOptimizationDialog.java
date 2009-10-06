package raptor.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.swt.chess.BoardUtils;
import raptor.util.SVGUtil;

public class ChessSetOptimizationDialog extends ProgressBarDialog {
	private static final Log LOG = LogFactory
			.getLog(ChessSetOptimizationDialog.class);

	public static final int END_SIZE = 100;
	public static final int START_SIZE = 10;

	protected String[] info = null;
	protected String setName;

	protected String[] svgPieceNames = new String[13];

	public ChessSetOptimizationDialog(Shell parent, String title, String setName) {
		super(parent);
		this.setName = setName;
		setShellTitle(title);
		initGuage();
	}

	@Override
	public void initGuage() {
		List<String> messages = new ArrayList<String>(50);

		for (int i = START_SIZE; i <= END_SIZE; i += 2) {
			messages.add("Creating pngs of size " + i + "x" + i);
		}
		info = messages.toArray(new String[0]);
		this.setExecuteTime(info.length);
		this.setMayCancel(true);
		this.setProcessMessage("Converting set " + setName
				+ ". This may take a few minutes ...");
	}

	@Override
	protected String process(int executionTime) {
		try {
			int size = START_SIZE + 2 * (executionTime - 1);

			for (int i = 1; i < 13; i++) {
				String userCacheFileName = BoardUtils
						.getUserImageCachePieceName(setName, i, size, size);

				// Only execute if the file doesnt exist.
				if (!(new File(userCacheFileName)).exists()) {
					// Load the svg.
					String svgFileName = BoardUtils.getSVGChessPieceName(
							setName, i);
					ImageData svgImageData = SVGUtil.loadSVG(svgFileName, size,
							size);

					// Save the pgn
					ImageLoader loader = new ImageLoader();
					loader.data = new ImageData[] { svgImageData };

					loader.save(userCacheFileName, SWT.IMAGE_PNG);
				}
			}

		} catch (Exception e) {
			LOG.error(e);
			Raptor.getInstance().onError("Error optimizing set " + setName, e);
		}

		return info[executionTime - 1];
	}
}
