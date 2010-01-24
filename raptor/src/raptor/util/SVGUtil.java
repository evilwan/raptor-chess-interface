/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.ImageData;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.app.beans.SVGIcon;

public class SVGUtil {
	public static final int MAX_PNG_IMAGE_SIZE = 15000;

	public static ImageData loadSVG(String filePath, int width, int height)
			throws IOException {
		File source = new File(filePath);
		SVGIcon icon = new SVGIcon();
		icon.setSvgURI(source.toURI());
		icon.setAntiAlias(true);

		// To do experiment around with bilinear(1) and bicubic(2).
		// 0 is "nearest neighbor" mode.
		icon.setInterpolation(0);

		int iconWidth = width <= 0 ? icon.getIconWidth() : width;
		int iconHeight = height <= 0 ? icon.getIconHeight() : height;

		icon.setClipToViewbox(false);
		icon.setPreferredSize(new Dimension(iconWidth, iconHeight));
		icon.setScaleToFit(true);
		BufferedImage image = new BufferedImage(iconWidth, iconHeight, 2);
		Graphics2D g = image.createGraphics();

		g.setClip(0, 0, iconWidth, iconHeight);
		icon.paintIcon(null, g, 0, 0);
		g.dispose();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				MAX_PNG_IMAGE_SIZE);

		ImageIO.write(image, "png", outputStream);

		SVGCache.getSVGUniverse().clear();

		return new ImageData(new ByteArrayInputStream(outputStream
				.toByteArray(), 0, outputStream.size()));
	}
}
