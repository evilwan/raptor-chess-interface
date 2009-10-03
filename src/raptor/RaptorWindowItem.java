package raptor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.swt.ItemChangedListener;

/**
 * An interface defining a RaptorWindowItem. RaptorWindowItems support
 * reparenting.
 * 
 */
public interface RaptorWindowItem {

	/**
	 * Adds an item changed listener. This listeners stateChanged method should
	 * be invoked whenever the title,image, or closeable information changes.
	 */
	public void addItemChangedListener(ItemChangedListener listener);

	/**
	 * Returns true if the RaptorWindowItem can be closed, false otherwise. This
	 * method can be used to prompt the user to determine if it should be
	 * closed.
	 */
	public boolean confirmClose();

	/**
	 * Disposes of the window item.
	 */
	public void dispose();

	/**
	 * Returns the window items control.
	 */
	public Composite getControl();

	/**
	 * Returns an image to display to represent the window item. Returns null if
	 * there is no image.
	 */
	public Image getImage();

	/**
	 * Returns the preferred quadrant the window item would like to be added to.
	 */
	public Quadrant getPreferredQuadrant();

	/**
	 * Returns the window items title.
	 */
	public String getTitle();

	/**
	 * Returns the toolbar to use for the window item. It should be created with
	 * the specified parent.
	 */
	public Control getToolbar(Composite parent);

	/**
	 * Initializes the window item to the new parent.
	 */
	public void init(Composite parent);

	/**
	 * Invoked when the window item is visible to the user.
	 */
	public void onActivate();

	/**
	 * Invoked when the window item is invisible to the user.
	 */
	public void onPassivate();

	/**
	 * Removes an item changed listener.
	 */
	public void removeItemChangedListener(ItemChangedListener listener);
}
