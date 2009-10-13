/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant);

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
	public Control getControl();

	/**
	 * Returns an image to display to represent the window item. Returns null if
	 * there is no image.
	 */
	public Image getImage();

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants();

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
