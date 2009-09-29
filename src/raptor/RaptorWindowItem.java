package raptor;

import org.eclipse.swt.widgets.Composite;

/**
 * An interface defining a RaptorWindowItem. RaptorWindowItems support
 * reparenting.
 * 
 */
public interface RaptorWindowItem {

	/**
	 * Returns true if the window can be reparented.
	 * 
	 * @return
	 */
	public boolean confirmReparenting();

	/**
	 * Disposes of the window item.
	 */
	public void dispose();

	/**
	 * Returns the window items control.
	 */
	public Composite getControl();

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
	 * Returns true if the window item is closeable.
	 */
	public boolean isCloseable();

	/**
	 * Invoked when the window item is visible to the user.
	 */
	public void onActivate();

	/**
	 * Invoked when the window item is invisible to the user.
	 */
	public void onPassivate();

	/**
	 * If confirm reparenting returns true, this method should be supported. It
	 * reparents the control to the new parent.
	 */
	public void onReparent(Composite newParent);
}
