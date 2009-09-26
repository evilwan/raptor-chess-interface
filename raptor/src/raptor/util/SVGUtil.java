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
		System.out.println("Painting");
		icon.paintIcon(null, g, 0, 0);
		System.out.println("Painted");
		g.dispose();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				MAX_PNG_IMAGE_SIZE);

		ImageIO.write(image, "png", outputStream);

		SVGCache.getSVGUniverse().clear();

		return new ImageData(new ByteArrayInputStream(outputStream
				.toByteArray(), 0, outputStream.size()));
	}
}
