/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import raptor.swt.chess.ChessBoardUtils;
import raptor.util.SVGUtil;

public class ChessSetOptimizationDialog extends ProgressBarDialog {
	public static final int END_SIZE = 100;

	private static final Log LOG = LogFactory
			.getLog(ChessSetOptimizationDialog.class);
	public static final int START_SIZE = 6;

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
		setExecuteTime(info.length);
		setMayCancel(true);
		setProcessMessage("Converting set " + setName
				+ ". This may take a few minutes ...");
	}

	@Override
	protected String process(int executionTime) {
		try {
			int size = START_SIZE + 2 * (executionTime - 1);

			for (int i = 1; i < 13; i++) {
				String userCacheFileName = ChessBoardUtils
						.getUserImageCachePieceName(setName, i, size, size);

				// Only execute if the file doesnt exist.
				if (!new File(userCacheFileName).exists()) {
					// Load the svg.
					String svgFileName = ChessBoardUtils.getSVGChessPieceName(
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
