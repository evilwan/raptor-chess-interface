package raptor.layout;

import org.eclipse.swt.graphics.Image;

public interface Layout {
    public String getName();
    public Image getImage();
    public void apply();
}
