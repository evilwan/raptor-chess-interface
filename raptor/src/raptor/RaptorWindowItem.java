package raptor;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

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
	 * Returns true if the RaptorWindowItem can be move to another quadrant,
	 * false otherwise. This method can be used to prompt the user to determine
	 * if it should be moved.
	 */
	public boolean confirmQuadrantMove();

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
	 * If confirmQuadrantMove returns true, this method should be supported. It
	 * reparents the control to the new parent.
	 * 
	 * It should return true if the component was able to be reparented without
	 * recreating the control. False otherwise.
	 */
	public boolean onReparent(Composite newParent);

	/**
	 * Removes an item changed listener.
	 */
	public void removeItemChangedListener(ItemChangedListener listener);
}
