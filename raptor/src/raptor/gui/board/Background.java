package raptor.gui.board;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

public class Background {
	private String name;

	private Image darkSquareImage;

	private Image lightSquareImage;
	private Device device;

	public Background(Device device, String name) {
		this.name = name;
		this.device = device;
		initImages();
	}

	public boolean equals(Object o) {
		if (o != null) {
			return getName().equals(((Background) o).getName());
		} else {
			return false;
		}

	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String getName() {
		return name;
	}

	private void initImages() {
		String suffix = "BMP";
		String darkImage = "SQUARE." + name + ".DARK." + suffix;
		String lightImage = "SQUARE." + name + ".LIGHT." + suffix;

		darkSquareImage = new Image(device, "resources/common/background/" + darkImage);
		lightSquareImage = new Image(device, "resources/common/background/" + lightImage);
	}

	public Image getScaledImage(boolean isLight, int width, int height) {

		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}

		Image source = isLight ? lightSquareImage : darkSquareImage;

		return new Image(device, source.getImageData().scaledTo(width, height));
	}

}
