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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.PreferenceUtils;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ConnectorService;
import raptor.swt.BrowserWindowItem;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.ItemChangedListener;
import raptor.swt.PgnProcessingDialog;
import raptor.swt.ProfileDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.controller.BughousePartnerController;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.InactiveController;
import raptor.util.BrowserUtils;

/**
 * A Raptor window is broken up into quadrants. Each quadrant is tabbed. You can
 * add a RaptorWindowItem to any quadrant. Each quadrant can be
 * maximized,minimized, and restored. When all of the tabs in a quadrannt are
 * vacant, the area disappears. You can drag and drop RaptorWindowItems between
 * visible quadrants.
 */
public class RaptorWindow extends ApplicationWindow {

	/**
	 * A sash form that loads and stores its weights in a specified key. A
	 * SashForm can only have a RaptorTabFolder or another RaptorSashFolder as a
	 * child.
	 */
	protected class RaptorSashForm extends SashForm {
		protected String key;

		public RaptorSashForm(Composite parent, int style, String key) {
			super(parent, style);
			this.key = key;
			setSashWidth(getPreferences().getInt(PreferenceKeys.APP_SASH_WIDTH));
		}

		/**
		 * Returns all of the items in the sash that are not in folders that are
		 * minimized.
		 * 
		 * @return
		 */
		public int getItemsInSash() {
			int result = 0;
			for (Control control : getTabList()) {
				if (control instanceof RaptorSashForm) {
					result += ((RaptorSashForm) control).getItemsInSash();
				} else if (control instanceof RaptorTabFolder) {
					RaptorTabFolder folder = (RaptorTabFolder) control;
					result += folder.getMinimized() ? 0 : folder.getItemCount();
				}
			}
			return result;
		}

		public String getKey() {
			return key;
		}

		public void loadFromPreferences() {
			setWeights(getPreferences().getCurrentLayoutSashWeights(key));
		}

		/**
		 * Restores the tab by setting it visible if it or one of its children
		 * contains items. This has a cascading effect if one of its children is
		 * another RaptorSashForm.
		 */
		public void restore() {
			Control lastChildToShow = null;
			int numberOfChildrenShowing = 0;

			Control[] children = getTabList();

			// First restore tab folders.
			for (Control element : children) {
				if (element instanceof RaptorTabFolder) {
					RaptorTabFolder folder = (RaptorTabFolder) element;
					if (folder.getItemCount() > 0 && !folder.getMinimized()) {
						RaptorTabFolder childFolder = (RaptorTabFolder) element;
						lastChildToShow = childFolder;
						childFolder.setVisible(true);
						childFolder.setMaximized(itemsManaged.size() == 1);
						childFolder.activate();
						numberOfChildrenShowing++;
					} else {
						folder.setVisible(false);
					}
				}
			}

			// Now restore sashes.
			for (Control element : children) {
				if (element instanceof RaptorSashForm) {
					if (((RaptorSashForm) element).getItemsInSash() > 0) {
						RaptorSashForm childSashForm = (RaptorSashForm) element;
						lastChildToShow = childSashForm;
						lastChildToShow.setVisible(true);
						numberOfChildrenShowing++;
					} else {
						if (element != null) {
							element.setVisible(false);
						}
					}
				}
			}
			setVisible(numberOfChildrenShowing > 0);
			setMaximizedControl(numberOfChildrenShowing == 1 ? lastChildToShow
					: null);
			if (isVisible()) {
				loadFromPreferences();
			}
		}

		public void storePreferences() {
			getPreferences().setCurrentLayoutSashWeights(key, getWeights());
		}

		public void storeSashWeights() {
			try {
				if (getMaximizedControl() == null && isVisible()) {
					getPreferences().setCurrentLayoutSashWeights(key,
							getWeights());
				}
			} catch (SWTException swet) {// Eat it its prob a disposed exception
			}
		}

		@Override
		public String toString() {
			return "RaptorSashForm " + key + " isVisible=" + isVisible()
					+ " maxControl=" + getMaximizedControl();
		}
	}

	/**
	 * A RaptorTabFolder which keeps track of the Quadrant it is in in and its
	 * parent RaptorSashForm. Also provides a raptorMaximize and a raptorRestore
	 * method to make maximizing and restoring easier. A RaptorTabFolder can
	 * only contain RaptorTabItems. A RaptorTabFolder can only have a
	 * RaptorSashForm as its parent. A RaptorTabFolder is also the only
	 * component in its Quadrant. Each Quadrant has a RaptorTabFolder.
	 */
	protected class RaptorTabFolder extends CTabFolder {
		protected Quadrant quad;
		protected RaptorSashForm raptorSash;

		public RaptorTabFolder(RaptorSashForm raptorSash, int style,
				Quadrant quad) {
			super(raptorSash, style);
			this.quad = quad;
			this.raptorSash = raptorSash;

			setSelectionBackground(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION));
			setSelectionForeground(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION_TEXT));

			addControlListener(new ControlListener() {

				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					storeAllSashWeights();
				}
			});
		}

		/**
		 * Activates the current selected item.
		 */
		public void activate() {
			if (getRaptorTabItemSelection() != null) {
				getRaptorTabItemSelection().raptorItem.onActivate();
			}
		}

		/**
		 * Returns the RaptorTabItem at the specified index.
		 */
		public RaptorTabItem getRaptorTabItemAt(int index) {
			return (RaptorTabItem) getItem(index);
		}

		/**
		 * Returns the selected RaptorTabItem null if there are none.
		 */
		public RaptorTabItem getRaptorTabItemSelection() {
			return (RaptorTabItem) getSelection();
		}

		/**
		 * Passivates all of the items in this tab. If includeSelection is true
		 * the current selection is passivated, otherwise its left alone.
		 * 
		 * @param includeSelection
		 */
		public void passivate(boolean includeSelection) {
			for (int i = 0; i < getItemCount(); i++) {
				if (!includeSelection
						&& getRaptorTabItemAt(i) == getRaptorTabItemSelection()) {
				} else {
					getRaptorTabItemAt(i).raptorItem.onPassivate();
				}
			}
		}

		public void passivateActiveateItems() {
			for (int i = 0; i < getItemCount(); i++) {
				if (getMinimized()) {
					getRaptorTabItemAt(i).raptorItem.onPassivate();
				} else if (i == getSelectionIndex()) {
					getRaptorTabItemAt(i).raptorItem.onActivate();
				} else {
					getRaptorTabItemAt(i).raptorItem.onPassivate();
				}
			}
		}

		/**
		 * Maximizes this folder in the window.
		 */
		public void raptorMaximize() {

			// First hide all folders besides ourself.
			for (RaptorTabFolder folder : folders) {
				if (folder != this) {
					folder.setVisible(false);
					folder.passivate(true);
					folder.setMaximized(false);
				}
			}
			setVisible(true);

			for (RaptorSashForm form : sashes) {
				if (form == raptorSash) {
					form.setVisible(true);
					form.setMaximizedControl(this);
				} else {
					form.setVisible(false);
					form.setMaximizedControl(null);
				}
			}

			List<RaptorSashForm> parents = new ArrayList<RaptorSashForm>(10);
			Control currentSashParent = raptorSash;

			// Build a list of all the parents. The last entry in the list will
			// be the greatest ancestor.
			while (currentSashParent instanceof RaptorSashForm) {
				parents.add((RaptorSashForm) currentSashParent);
				currentSashParent = currentSashParent.getParent();
			}

			// Now walk the list backwards maximizing the sash forms to the
			// child added.
			for (int i = 0; i < parents.size() - 1; i++) {
				parents.get(i + 1).setVisible(true);
				parents.get(i + 1).setMaximizedControl(parents.get(i));
			}
			setMaximized(true);
		}

		/**
		 * Override to make sure setSelection(int index) gets invoked.
		 */
		@Override
		public void setSelection(CTabItem item) {
			for (int i = 0; i < getItemCount(); i++) {
				if (getItem(i) == item) {
					setSelection(i);
				}
			}
		}

		/**
		 * Overridden to add the toolbar to the CTabFolder.
		 */
		@Override
		public void setSelection(int index) {
			super.setSelection(index);
			passivateActiveateItems();
			updateToolbar(true);
		}

		@Override
		public void setVisible(boolean isVisible) {
			super.setVisible(isVisible);
		}

		@Override
		public String toString() {
			return "RaptorTabFolder " + quad + " isVisible=" + isVisible()
					+ " itemCount=" + getItemCount() + " currentSelection="
					+ getRaptorTabItemSelection();
		}

		public void updateToolbar(boolean force) {

			// This method currently leaks toolBars.
			// However, it is the only way I have found to pull this off.
			// Care is taken in RaptorWindowItems to remove all menu items from
			// tool bars.
			// But the actual tool bar control will be leaked.
			if (!getMinimized() || force) {
				long startTime = System.currentTimeMillis();
				RaptorTabItem currentSelection = (RaptorTabItem) getSelection();

				Control currentSelectionToolbar = null;
				if (currentSelection != null) {
					if (LOG.isDebugEnabled()) {
						LOG
								.debug("In updateToolbar selected RaptorWindowItem="
										+ currentSelection.raptorItem
										+ " quad=" + quad);
					}

					currentSelectionToolbar = currentSelection.raptorItem
							.getToolbar(this);
				}
				Control existingControl = getTopRight();
				if (currentSelection != null) {
					if (existingControl != currentSelectionToolbar) {
						if (existingControl != null
								&& !existingControl.isDisposed()) {
							existingControl.setVisible(false);
							setTopRight(null);
						}
						if (currentSelectionToolbar != null) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Setting up toolbar. ");
							}
							setTopRight(currentSelectionToolbar, SWT.RIGHT);
							setTabHeight(Math.max(currentSelectionToolbar
									.computeSize(SWT.DEFAULT, SWT.DEFAULT).y,
									getTabHeight()));
							currentSelectionToolbar.setVisible(true);
							currentSelectionToolbar.redraw();
							layout(true);
						}
					} else if (existingControl != null
							&& existingControl.isVisible() == false) {
						existingControl.setVisible(true);
						existingControl.redraw();
					}
				} else if (existingControl != null
						&& !existingControl.isDisposed()) {
					existingControl.setVisible(false);
					setTopRight(null);
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Updated toolbar in "
							+ (System.currentTimeMillis() - startTime));
				}
			}
		}
	}

	/**
	 * A RaptorTabItem. All items added to RaptorTabFoldrs should be
	 * RaptorTabItems RaptorTabItems can only contain RaptorWindowItem controls.
	 */
	protected class RaptorTabItem extends CTabItem {
		protected boolean disposed = false;
		protected ItemChangedListener listener;
		protected RaptorWindowItem raptorItem;
		protected RaptorTabFolder raptorParent;

		public RaptorTabItem(RaptorTabFolder parent, int style,
				RaptorWindowItem item) {
			this(parent, style, item, true);
		}

		public RaptorTabItem(RaptorTabFolder parent, int style,
				final RaptorWindowItem item, boolean isInitingItem) {
			super(parent, style);
			raptorParent = parent;

			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating RaptorTabItem.");
			}

			raptorItem = item;
			if (isInitingItem) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("initing item.");
				}
				item.init(parent);
			}

			item.addItemChangedListener(listener = new ItemChangedListener() {
				public void itemStateChanged() {
					if (!disposed && item.getControl() != null
							&& !item.getControl().isDisposed()) {
						getShell().getDisplay().asyncExec(new Runnable() {
							public void run() {

								if (LOG.isDebugEnabled()) {
									LOG
											.debug("Item changed, updating text,title,showClose");
								}
								try {
									setText(item.getTitle());
									setImage(item.getImage());
									setShowClose(true);
									if (raptorParent.getSelection() == RaptorTabItem.this) {
										raptorParent.updateToolbar(true);
									}
								} catch (SWTException swt) {
									if (LOG.isDebugEnabled()) {
										LOG
												.debug(
														"Error handling item state changed:",
														swt);
									}
									// Just eat it. It is probably a
									// widget is
									// disposed exception
									// and i can't figure out how to
									// avoid it.
								}
							}
						});
					}
				}
			});

			setControl(item.getControl());
			setText(item.getTitle());
			setImage(item.getImage());
			setShowClose(true);
			// parent.layout(true, true);
			parent.setSelection(this);
			itemsManaged.add(this);
		}

		@Override
		public void dispose() {
			try {
				super.dispose();
			} catch (Throwable t) {
			}// Eat it. Prob an already disposed issue.
			itemsManaged.remove(this);
			try {
				if (raptorItem != null) {
					raptorItem.dispose();
				}
			} catch (Throwable t) {
			} // Just eat it its prob a widget is already disposed exception.
			raptorItem = null;
			raptorParent = null;

		}

		public void onMoveTo(RaptorTabFolder newParent) {
			if (!raptorItem.getControl().isReparentable()) {
				Raptor
						.getInstance()
						.alert(
								"You can only move between quadrants if reparenting is supported in your SWT environment.");
			} else {
				// This code is quite tricky and must happen in an exact order
				// or subtle issues occur with tool bars.
				raptorItem.removeItemChangedListener(listener);

				// Set the control to null so it can be re-parented.
				setControl(null);

				// Re-parent the control
				raptorItem.getControl().setParent(newParent);

				// Now add the new raptor tab item to the new parent.
				new RaptorTabItem(newParent, getStyle(), raptorItem, false);

				// Invoke after move so the item can adjust to its new parent if
				// needed.
				raptorItem.afterQuadrantMove(newParent.quad);

				// We will lose the raptorParent reference on dispose,
				// and we need it to adjust the toolbar after disposing.
				RaptorTabFolder oldParent = raptorParent;

				// Dispose of the window item.
				// Notice that raptorItem is set to null so the raptorItem will
				// not be disposed.
				raptorItem = null;
				dispose();

				// Removes the tool bar on the current parent.
				// This will allow it to be re-parented on the new parent.
				oldParent.updateToolbar(true);

				// Forces an update on the tool bar of the new parent.
				newParent.updateToolbar(true);

				// And finally restore the folders so if the quad being
				// moved to is empty, it gets restored.
				restoreFolders();
			}
		}

		@Override
		public String toString() {
			return "RaptorTabItem: " + getText() + " isVisible=" + " quadrant="
					+ raptorParent.quad + getControl().isVisible();
		}
	}

	protected List<RaptorTabItem> itemsManaged = Collections
			.synchronizedList(new ArrayList<RaptorTabItem>());

	protected RaptorTabItem dragStartItem;

	protected RaptorTabFolder[] folders = new RaptorTabFolder[Quadrant.values().length];
	protected ToolBar foldersMinimizedToolbar;

	protected CoolItem foldersMinimiziedCoolItem;
	protected boolean isExitDrag = false;
	protected boolean isInDrag = false;
	protected CoolBar leftCoolbar;
	Log LOG = LogFactory.getLog(RaptorWindow.class);
	protected Map<String, Label> pingLabelsMap = new HashMap<String, Label>();

	protected RaptorSashForm quad1quad234567quad8Sash;
	protected RaptorSashForm quad2quad34567Sash;
	protected RaptorSashForm quad34quad567Sash;
	protected RaptorSashForm quad3quad4Sash;

	protected RaptorSashForm quad56quad7Sash;
	protected RaptorSashForm quad5quad6Sash;
	protected RaptorSashForm[] sashes = new RaptorSashForm[6];
	protected Composite statusBar;
	protected Label statusLabel;

	protected Composite windowComposite;

	public RaptorWindow() {
		super(null);
		addMenuBar();
	}

	/**
	 * Adds a RaptorWindowItem to the RaptorWindow asynchronously.
	 * 
	 * An item listener will be added to the window item. This listeners
	 * itemChanged should be invoked whenever the title,or icon of the
	 * RaptorWindowItem change.
	 * 
	 * onPassivate and onActivate will be invoked as the item becomes invisible
	 * and visible again. Multiple calls to activate may occur even if the
	 * RaptorWindowItem is visible so the RaptorWindowItem should handle these
	 * as well.
	 */
	public void addRaptorWindowItem(final RaptorWindowItem item) {
		addRaptorWindowItem(item, true);
	}

	/**
	 * Adds a RaptorWindowItem to the RaptorWindow either synchronously or
	 * asynchronously depending on isAsynch.
	 * 
	 * An item listener will be added to the window item. This listeners
	 * itemChanged should be invoked whenever the title,or icon of the
	 * RaptorWindowItem change.
	 * 
	 * onPassivate and onActivate will be invoked as the item becomes invisible
	 * and visible again. Multiple calls to activate may occur even if the
	 * RaptorWindowItem is visible so the RaptorWindowItem should handle these
	 * as well.
	 */
	public void addRaptorWindowItem(final RaptorWindowItem item,
			boolean isAsynch) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding raptor window item " + item);
		}

		if (!isAsynch) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					RaptorTabFolder folder = getRaptorTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item);
					restoreFolders();
				}
			});
		} else {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					RaptorTabFolder folder = getRaptorTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item);
					restoreFolders();
				}
			});

		}
	}

	/**
	 * Returns true if this RaptorWindow is managing a channel tell tab for the
	 * specified channel.
	 */
	public boolean containsBugButtonsItem(Connector connector) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof BugButtonsWindowItem) {
					BugButtonsWindowItem item = (BugButtonsWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getConnector() == connector) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if this RaptorWindow is managing a channel tell tab for the
	 * specified channel.
	 */
	public boolean containsChannelItem(Connector connector, String channel) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem item = (ChatConsoleWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getController().getConnector() == connector
							&& item.getController() instanceof ChannelController) {
						ChannelController controller = (ChannelController) item
								.getController();
						if (StringUtils.equalsIgnoreCase(controller
								.getChannel(), channel)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if this RaptorWindow is managing a partner tell tab.
	 */
	public boolean containsPartnerTellItem(Connector connector) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem item = (ChatConsoleWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getController().getConnector() == connector
							&& item.getController() instanceof BughousePartnerController) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if this RaptorWindow is managing a personal tell tab for the
	 * specified person.
	 */
	public boolean containsPersonalTellItem(Connector connector, String person) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem item = (ChatConsoleWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getController().getConnector() == connector
							&& item.getController() instanceof PersonController) {
						PersonController controller = (PersonController) item
								.getController();
						if (StringUtils.equalsIgnoreCase(
								controller.getPerson(), person)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns true if atleast one window item of the specified type is being
	 * managed.
	 */
	@SuppressWarnings("unchecked")
	public boolean containsWindowItems(Class windowItemClass) {
		boolean result = false;
		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (windowItemClass.isInstance(currentTabItem.raptorItem)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the number of items in the specified quadrants.
	 */
	public int countItems(Quadrant... quads) {
		int count = 0;
		for (Quadrant quad : quads) {
			count += getRaptorTabFolder(quad).getItemCount();
		}
		return count;
	}

	/**
	 * Disposes all the resources that will not be cleaned up when this window
	 * is closed.
	 */
	public void dispose() {
		if (pingLabelsMap != null) {
			pingLabelsMap.clear();
			pingLabelsMap = null;
		}
		if (itemsManaged != null) {
			itemsManaged.clear();
			itemsManaged = null;
		}
		if (folders != null) {
			folders = null;
		}
		if (sashes != null) {
			sashes = null;
		}
	}

	/**
	 * Disposes an item and removes it from the RaptorWindow. After this method
	 * is invoked the item passed in is disposed.
	 */
	public void disposeRaptorWindowItem(final RaptorWindowItem item) {
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				RaptorTabItem tabItem = null;
				synchronized (itemsManaged) {
					for (RaptorTabItem currentTabItem : itemsManaged) {
						if (currentTabItem.raptorItem == item) {
							tabItem = currentTabItem;
							break;
						}
					}
				}
				if (item != null) {
					tabItem.dispose();
					restoreFolders();
				}
			}
		});
	}

	/**
	 * Forces the current window item in view.
	 */
	public void forceFocus(RaptorWindowItem windowItem) {
		synchronized (itemsManaged) {
			boolean wasRestored = false;

			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorItem == windowItem) {
					currentTabItem.raptorParent.setSelection(currentTabItem);
					if (currentTabItem.raptorParent.getMinimized()) {
						restoreFolders();
						wasRestored = true;
					}
					break;
				}
			}

			// Now check to see if a folder is maximized. If one is then do a
			// restore.
			if (!wasRestored) {
				boolean isFolderMaximized = false;
				for (RaptorTabFolder folder : folders) {
					if (folder.getMaximized()) {
						isFolderMaximized = true;
						break;
					}
				}

				if (isFolderMaximized) {
					restoreFolders();
				}
			}
		}
	}

	/**
	 * If a browser window items is currently being displayed it is returned.
	 * Otherwise null is returned.
	 */
	public BrowserWindowItem getBrowserWindowItem() {
		BrowserWindowItem result = null;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof BrowserWindowItem) {
					result = (BrowserWindowItem) folder.getRaptorTabItemAt(i).raptorItem;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the ChessBoardWindowItem for the specified game id, null if its
	 * not found.
	 * 
	 * @param gameId
	 *            The game id.
	 * @return null if not found, otherwise the ChessBoardWindowItem.
	 */
	public ChessBoardWindowItem getChessBoardWindowItem(String gameId) {
		ChessBoardWindowItem result = null;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChessBoardWindowItem) {
					ChessBoardWindowItem item = (ChessBoardWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (!(item.getController() instanceof InactiveController)
							&& item.getController().getGame().getId().equals(
									gameId)) {
						result = item;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Searches in the specified quadrant for an inactive game which can be
	 * taken over. If one is found taht game is returned. Otherwise null is
	 * returned.
	 * 
	 * @param gameId
	 *            The quadrant to search in.
	 * @return null if not found, otherwise the ChessBoardWindowItem.
	 */
	public ChessBoardWindowItem getChessBoardWindowItemToTakeOver(
			Quadrant quadrant) {
		ChessBoardWindowItem result = null;

		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorParent.quad == quadrant
						&& currentTabItem.raptorItem instanceof ChessBoardWindowItem) {
					ChessBoardWindowItem item = (ChessBoardWindowItem) currentTabItem.raptorItem;

					if (item.getController() instanceof InactiveController) {
						if (((InactiveController) item.getController())
								.canBeTakenOver()) {
							result = item;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the quad the current window item is in. Null if the windowItem is
	 * not being managed.
	 */
	public Quadrant getQuadrant(RaptorWindowItem windowItem) {
		Quadrant result = null;
		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorItem == windowItem) {
					result = currentTabItem.raptorParent.quad;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the RaptorTabFolder at the quad.
	 */
	public RaptorTabFolder getRaptorTabFolder(Quadrant quad) {
		RaptorTabFolder result = folders[quad.ordinal()];
		return result;
	}

	/**
	 * Returns an array of all RaptorWindowItems currently active.
	 */
	public RaptorWindowItem[] getRaptorWindowItems() {
		return itemsManaged.toArray(new RaptorWindowItem[0]);
	}

	/**
	 * Returns all RaptorWindowItems that are being managed and are of the
	 * specified class type and are currently selected within their parent
	 * RaptorTabFolder.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	@SuppressWarnings("unchecked")
	public RaptorWindowItem[] getSelectedWindowItems(Class windowItemClass) {
		List<RaptorWindowItem> result = new ArrayList<RaptorWindowItem>(10);

		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (windowItemClass.isInstance(currentTabItem.raptorItem)
						&& !currentTabItem.raptorParent.getMinimized()
						&& currentTabItem.raptorParent
								.getRaptorTabItemSelection() == currentTabItem) {
					result.add(currentTabItem.raptorItem);
				}
			}
		}

		return result.toArray(new RaptorWindowItem[0]);
	}

	/**
	 * Returns the RaptorTabFolder at the specified 0 based index.
	 */
	public RaptorTabFolder getTabFolder(int index) {
		return folders[index];
	}

	/**
	 * Returns all RaptorWindowItems that are being managed and are of the
	 * specified class type.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	@SuppressWarnings("unchecked")
	public RaptorWindowItem[] getWindowItems(Class windowItemClass) {
		List<RaptorWindowItem> result = new ArrayList<RaptorWindowItem>(10);

		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (windowItemClass.isInstance(currentTabItem.raptorItem)) {
					result.add(currentTabItem.raptorItem);
				}
			}
		}

		return result.toArray(new RaptorWindowItem[0]);
	}

	/**
	 * Returns true if the item is being managed by the RaptorWindow. As items
	 * are closed and disposed they are no longer managed.
	 */
	public boolean isBeingManaged(final RaptorWindowItem item) {
		boolean result = false;
		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorItem == item) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Sets the ping time on the window for the specified connector. If time is
	 * -1 the label will disappear.
	 */
	public void setPingTime(final Connector connector, final long pingTime) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setPingTime " + connector.getShortName() + " "
					+ pingTime);
		}

		if (getShell() == null || getShell().getDisplay() == null
				|| getShell().getDisplay().isDisposed()) {
			return;
		}

		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				Label label = pingLabelsMap.get(connector.getShortName());
				if (label == null) {
					if (pingTime == -1) {
						return;
					}
					label = new Label(statusBar, SWT.NONE);
					GridData gridData = new GridData();
					gridData.grabExcessHorizontalSpace = false;
					gridData.grabExcessVerticalSpace = false;
					gridData.horizontalAlignment = SWT.END;
					gridData.verticalAlignment = SWT.CENTER;
					label.setLayoutData(gridData);
					pingLabelsMap.put(connector.getShortName(), label);
					label.setFont(Raptor.getInstance().getPreferences()
							.getFont(PreferenceKeys.APP_PING_FONT));
					label.setForeground(Raptor.getInstance().getPreferences()
							.getColor(PreferenceKeys.APP_PING_COLOR));
				}
				if (pingTime == -1) {
					label.setVisible(false);
					label.dispose();
					pingLabelsMap.remove(connector.getShortName());
					statusBar.layout(true, true);
				} else {
					label.setText(connector.getShortName() + " ping "
							+ pingTime + "ms");
					statusBar.layout(true, true);
					label.redraw();
				}
			}
		});
	}

	/**
	 * Sets the windows status message.
	 */
	public void setStatusMessage(final String newStatusMessage) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("setStatusMessage " + newStatusMessage);
		}

		if (getShell() == null || getShell().getDisplay() == null
				|| getShell().getDisplay().isDisposed()) {
			return;
		}

		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				statusLabel
						.setText(StringUtils.defaultString(newStatusMessage));
			}
		});
	}

	public void storeAllSashWeights() {
		for (RaptorSashForm sash : sashes) {
			sash.storeSashWeights();
		}
	}

	/**
	 * Adjusts the left coolbar for quadrants minimized.
	 */
	protected void adjustToFoldersItemsMinimizied() {
		ToolItem[] folderMinItems = foldersMinimizedToolbar.getItems();
		for (ToolItem item : folderMinItems) {
			item.dispose();
		}

		boolean isAFolderMinimized = false;
		for (RaptorTabFolder folder : folders) {
			if (folder.getMinimized()) {
				isAFolderMinimized = true;
				break;
			}
		}

		if (isAFolderMinimized) {
			for (Quadrant quadrant : Quadrant.values()) {
				if (getRaptorTabFolder(quadrant).getMinimized()) {
					ToolItem item = new ToolItem(foldersMinimizedToolbar,
							SWT.PUSH);
					item.setText(quadrant.name());
					item.setToolTipText("Restore quadrant " + quadrant.name());
					final Quadrant finalQuad = quadrant;
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							getRaptorTabFolder(finalQuad).setMinimized(false);
							restoreFolders();
						}
					});
				}
			}
		}

		foldersMinimizedToolbar.pack();
		foldersMinimiziedCoolItem.setPreferredSize(foldersMinimizedToolbar
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		if (!isAFolderMinimized) {
			leftCoolbar.setVisible(false);
		} else {
			leftCoolbar.setVisible(true);
		}
		windowComposite.layout();

	}

	/**
	 * Creates the controls.
	 */
	@Override
	protected Control createContents(Composite parent) {
		getShell().addControlListener(new ControlAdapter() {
			// public void controlMoved(ControlEvent e) {
			// storeBounds();
			// }

			@Override
			public void controlResized(ControlEvent e) {
				storeBounds();
			}
		});
		getShell().addListener(SWT.Close, new Listener() {
			public void handleEvent(Event e) {
				Raptor.getInstance().shutdown();
			}
		});

		getShell().setText(
				Raptor.getInstance().getPreferences().getString(
						PreferenceKeys.APP_NAME));
		getShell().setImage(
				Raptor.getInstance().getImage(
						Raptor.RESOURCES_DIR + "images/raptorIcon.gif"));

		parent.setLayout(SWTUtils.createMarginlessGridLayout(1, true));

		windowComposite = new Composite(parent, SWT.NONE);
		windowComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		windowComposite
				.setLayout(SWTUtils.createMarginlessGridLayout(2, false));

		createLeftCoolbar();
		createFolderAndSashControls();
		createStatusBarControls();
		return windowComposite;
	}

	/**
	 * Creates just the folder,sash,and quad composite controls. Initially they
	 * all start out not visible and noy maximized.
	 */
	protected void createFolderAndSashControls() {
		createQuad1Quad234567QuadControls();

		for (RaptorTabFolder folder : folders) {
			initFolder(folder);
		}

		sashes[0] = quad1quad234567quad8Sash;
		sashes[1] = quad2quad34567Sash;
		sashes[2] = quad34quad567Sash;
		sashes[3] = quad3quad4Sash;
		sashes[4] = quad56quad7Sash;
		sashes[5] = quad5quad6Sash;

		for (RaptorSashForm sashe : sashes) {
			sashe.loadFromPreferences();
			sashe.setVisible(false);
			sashe.setMaximizedControl(null);
		}
	}

	protected void createLeftCoolbar() {
		leftCoolbar = new CoolBar(windowComposite, SWT.FLAT | SWT.VERTICAL);
		leftCoolbar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true,
				1, 1));
		foldersMinimizedToolbar = new ToolBar(leftCoolbar, SWT.FLAT
				| SWT.VERTICAL);

		foldersMinimiziedCoolItem = new CoolItem(leftCoolbar, SWT.NONE);
		foldersMinimiziedCoolItem.setControl(foldersMinimizedToolbar);
		foldersMinimiziedCoolItem.setPreferredSize(foldersMinimizedToolbar
				.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Creates the menu items.
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("Main");
		MenuManager fileMenu = new MenuManager("File");
		MenuManager helpMenu = new MenuManager("&Help");

		fileMenu.add(new Action("View PGN File") {
			@Override
			public void run() {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText("Select the pgn file to view");
				fd.setFilterPath("");
				String[] filterExt = { "*.pgn", "*.bpgn", "*.*" };
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					PgnProcessingDialog dialog = new PgnProcessingDialog(
							getShell(), selected);
					dialog.open();
				}
			}
		});
		fileMenu.add(new Action("View my saved games") {
			@Override
			public void run() {
				File file = new File(Raptor.GAMES_PGN_FILE);
				if (!file.exists()) {
					Raptor.getInstance().alert(
							"You currently do not have any games saved in "
									+ Raptor.GAMES_PGN_FILE);
				} else {
					PgnProcessingDialog dialog = new PgnProcessingDialog(
							getShell(), Raptor.GAMES_PGN_FILE);
					dialog.open();
				}
			}
		});
		fileMenu.add(new Separator());
		fileMenu.add(new Action("Preferences") {
			@Override
			public void run() {
				PreferenceUtils.launchPreferenceDialog();
			}
		});
		fileMenu.add(new Separator());
		fileMenu.add(new Action("Mini Profiler") {
			@Override
			public void run() {
				ProfileDialog dialog = new ProfileDialog();
				dialog.open();
			}
		});
		// Please leave these commented out. It is useful to enable for testing.
		// fileMenu.add(new Action("Create Test Board") {
		// @Override
		// public void run() {
		// Game game = GameFactory.createStartingPosition(Variant.classic);
		// game.addState(Game.UPDATING_SAN_STATE);
		// game.addState(Game.UPDATING_ECO_HEADERS_STATE);
		// game.addState(Game.IS_CLOCK_TICKING_STATE);
		// game.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
		// .currentTimeMillis()));
		// game.setHeader(PgnHeader.Round, "?");
		// game.setHeader(PgnHeader.Site, "Test Game Site");
		// game.setHeader(PgnHeader.TimeControl, PgnUtils
		// .timeIncMillisToTimeControl(180000, 0));
		// game.setHeader(PgnHeader.BlackRemainingMillis, "" + 46728);
		// game.setHeader(PgnHeader.WhiteRemainingMillis, "" + 153857);
		// game.setHeader(PgnHeader.WhiteClock, PgnUtils
		// .timeToClock(180000));
		// game.setHeader(PgnHeader.BlackClock, PgnUtils
		// .timeToClock(180000));
		// game.setHeader(PgnHeader.BlackElo, "----");
		// game.setHeader(PgnHeader.WhiteElo, "----");
		// game.setHeader(PgnHeader.Event, "blitz 3 0 rated");
		// game.makeSanMove("e4");
		// game.makeSanMove("e5");
		// game.makeSanMove("Nf3");
		// game.makeSanMove("Nc6");
		// game.makeSanMove("Bb5");
		// game.makeSanMove("a6");
		// InactiveController controller = new InactiveController(game);
		// ChessBoardWindowItem item = new ChessBoardWindowItem(controller);
		// addRaptorWindowItem(item, false);
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E2,
		// GameConstants.SQUARE_E7, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_D7,
		// GameConstants.SQUARE_D1, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_A8,
		// GameConstants.SQUARE_D8, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_BLACK)));
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_H8,
		// GameConstants.SQUARE_E8, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_CYAN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_B1,
		// GameConstants.SQUARE_A3, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_B1,
		// GameConstants.SQUARE_D2, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_G1,
		// GameConstants.SQUARE_H3, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_G1,
		// GameConstants.SQUARE_E2, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E7,
		// GameConstants.SQUARE_C6, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E7,
		// GameConstants.SQUARE_G6, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_B7,
		// GameConstants.SQUARE_A5, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_G7,
		// GameConstants.SQUARE_H5, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E4,
		// GameConstants.SQUARE_H7, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_RED)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E4,
		// GameConstants.SQUARE_A8, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GREEN)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E4,
		// GameConstants.SQUARE_H1, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_BLUE)));
		//
		// item.getBoard().getArrowDecorator().addArrow(
		// new Arrow(GameConstants.SQUARE_E4,
		// GameConstants.SQUARE_B1, Raptor.getInstance()
		// .getDisplay().getSystemColor(
		// SWT.COLOR_GRAY)));
		// }
		// });
		// menuBar.add(fileMenu);
		// fileMenu.add(new Action("Create Test Crazyhouse Board") {
		// @Override
		// public void run() {
		// Game game = GameFactory
		// .createStartingPosition(Variant.crazyhouse);
		// game.addState(Game.UPDATING_SAN_STATE);
		// game.addState(Game.UPDATING_ECO_HEADERS_STATE);
		// game.addState(Game.IS_CLOCK_TICKING_STATE);
		// game.setHeader(PgnHeader.Date, PgnUtils.longToPgnDate(System
		// .currentTimeMillis()));
		// game.setHeader(PgnHeader.Round, "?");
		// game.setHeader(PgnHeader.Site, "Test Game Site");
		// game.setHeader(PgnHeader.TimeControl, PgnUtils
		// .timeIncMillisToTimeControl(180000, 0));
		// game.setHeader(PgnHeader.BlackRemainingMillis, "" + 46728);
		// game.setHeader(PgnHeader.WhiteRemainingMillis, "" + 153857);
		// game.setHeader(PgnHeader.WhiteClock, PgnUtils
		// .timeToClock(180000));
		// game.setHeader(PgnHeader.BlackClock, PgnUtils
		// .timeToClock(180000));
		// game.setHeader(PgnHeader.BlackElo, "----");
		// game.setHeader(PgnHeader.WhiteElo, "----");
		// game.setHeader(PgnHeader.Event, "crazyhouse 3 0 rated");
		// game.makeSanMove("e4");
		// game.makeSanMove("d5");
		// game.makeSanMove("ed");
		// game.makeSanMove("Qd5");
		// InactiveController controller = new InactiveController(game);
		//
		// addRaptorWindowItem(new ChessBoardWindowItem(controller));
		// }
		// });
		
		
		 menuBar.add(fileMenu);

		Connector[] connectors = ConnectorService.getInstance().getConnectors();

		for (Connector connector : connectors) {
			MenuManager manager = connector.getMenuManager();
			if (manager != null) {
				menuBar.add(manager);
			}
		}

		helpMenu.add(new Action("&Raptor Home Page") {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/");

			}
		});
		menuBar.add(helpMenu);
		return menuBar;
	}

	/**
	 * Creates the sash hierarchy.
	 */
	protected void createQuad1Quad234567QuadControls() {
		quad1quad234567quad8Sash = new RaptorSashForm(windowComposite,
				SWT.VERTICAL | SWT.SMOOTH,
				PreferenceKeys.QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS);
		quad1quad234567quad8Sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true, 1, 1));

		folders[Quadrant.I.ordinal()] = new RaptorTabFolder(
				quad1quad234567quad8Sash, SWT.BORDER, Quadrant.I);

		quad2quad34567Sash = new RaptorSashForm(quad1quad234567quad8Sash,
				SWT.HORIZONTAL | SWT.SMOOTH,
				PreferenceKeys.QUAD2_QUAD234567_SASH_WEIGHTS);

		folders[Quadrant.VIII.ordinal()] = new RaptorTabFolder(
				quad1quad234567quad8Sash, SWT.BORDER, Quadrant.VIII);

		folders[Quadrant.II.ordinal()] = new RaptorTabFolder(
				quad2quad34567Sash, SWT.BORDER, Quadrant.II);

		quad34quad567Sash = new RaptorSashForm(quad2quad34567Sash, SWT.VERTICAL
				| SWT.SMOOTH, PreferenceKeys.QUAD34_QUAD567_SASH_WEIGHTS);

		quad3quad4Sash = new RaptorSashForm(quad34quad567Sash, SWT.HORIZONTAL
				| SWT.SMOOTH, PreferenceKeys.QUAD3_QUAD4_SASH_WEIGHTS);

		quad56quad7Sash = new RaptorSashForm(quad34quad567Sash, SWT.HORIZONTAL
				| SWT.SMOOTH, PreferenceKeys.QUAD56_QUAD7_SASH_WEIGHTS);

		folders[Quadrant.III.ordinal()] = new RaptorTabFolder(quad3quad4Sash,
				SWT.BORDER, Quadrant.III);

		folders[Quadrant.IV.ordinal()] = new RaptorTabFolder(quad3quad4Sash,
				SWT.BORDER, Quadrant.IV);

		quad5quad6Sash = new RaptorSashForm(quad56quad7Sash, SWT.VERTICAL
				| SWT.SMOOTH, PreferenceKeys.QUAD5_QUAD6_SASH_WEIGHTS);

		folders[Quadrant.V.ordinal()] = new RaptorTabFolder(quad5quad6Sash,
				SWT.BORDER, Quadrant.V);

		folders[Quadrant.VI.ordinal()] = new RaptorTabFolder(quad5quad6Sash,
				SWT.BORDER, Quadrant.VI);

		folders[Quadrant.VII.ordinal()] = new RaptorTabFolder(quad56quad7Sash,
				SWT.BORDER, Quadrant.VII);
	}

	/**
	 * Creates the status bar controls.
	 */
	protected void createStatusBarControls() {
		statusBar = new Composite(windowComposite, SWT.NONE);
		statusBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		GridLayout layout = new GridLayout(10, false);
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginHeight = 0;
		statusBar.setLayout(layout);

		statusLabel = new Label(statusBar, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.CENTER;
		statusLabel.setLayoutData(gridData);
		statusLabel.setFont(Raptor.getInstance().getPreferences().getFont(
				PreferenceKeys.APP_STATUS_BAR_FONT));
		statusLabel.setForeground(Raptor.getInstance().getPreferences()
				.getColor(PreferenceKeys.APP_STATUS_BAR_COLOR));
	}

	protected RaptorTabFolder getFolderContainingCursor() {
		Control control = getShell().getDisplay().getCursorControl();

		while (control != null && !(control instanceof RaptorTabFolder)
				&& !(control instanceof Shell)) {
			control = control.getParent();
		}

		if (control instanceof RaptorTabFolder) {
			return (RaptorTabFolder) control;
		} else {
			return null;
		}
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	/**
	 * Initializes a quad folder. Also sets up all of the listeners on the
	 * folder.
	 * 
	 * @param folder
	 */
	protected void initFolder(final RaptorTabFolder folder) {
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(true);
		folder.setMaximizeVisible(true);
		folder.setMinimizeVisible(true);

		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorTabItem selection = folder.getRaptorTabItemSelection();
				folder.passivateActiveateItems();
				folder.setTopRight(selection.raptorItem.getToolbar(folder),
						SWT.RIGHT);
			}
		});

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				RaptorTabItem item = (RaptorTabItem) event.item;
				if (item.raptorItem.confirmClose()) {
					event.doit = true;
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							restoreFolders();
						}
					});
				} else {
					event.doit = false;
				}
			}

			@Override
			public void maximize(CTabFolderEvent event) {
				folder.setMaximized(true);
				folder.raptorMaximize();
			}

			@Override
			public void minimize(CTabFolderEvent event) {
				// folder.getRaptorTabItemSelection().raptorItem.onPassivate();
				folder.setMinimized(true);
				restoreFolders();
			}

			@Override
			public void restore(CTabFolderEvent event) {
				restoreFolders();

				folder.getDisplay().timerExec(100, new Runnable() {
					public void run() {
						folder.getRaptorTabItemSelection().raptorItem
								.onActivate();
					}
				});
			}
		});

		folder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (e.count == 2) {
					if (folder.getMaximized()) {
						restoreFolders();
					} else {
						folder.raptorMaximize();
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					Menu menu = new Menu(folder.getShell(), SWT.POP_UP);

					if (folder.getItemCount() > 0) {
						MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText("Close");
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								if (folder.getRaptorTabItemSelection() != null) {
									if (folder.getRaptorTabItemSelection().raptorItem
											.confirmClose()) {
										folder.getRaptorTabItemSelection()
												.dispose();

										if (folder.getItemCount() == 0) {
											restoreFolders();
										} else {
											folder.setSelection(folder
													.getSelectionIndex());
										}
									}
								}
							}
						});
					}
					if (folder.getItemCount() > 1) {
						MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText("Close Others");
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								List<RaptorTabItem> itemsToClose = new ArrayList<RaptorTabItem>(
										folder.getItemCount());
								for (int i = 0; i < folder.getItemCount(); i++) {
									if (i != folder.getSelectionIndex()
											&& folder.getRaptorTabItemAt(i).raptorItem
													.confirmClose()) {
										itemsToClose.add(folder
												.getRaptorTabItemAt(i));
									}
								}
								for (RaptorTabItem item : itemsToClose) {
									item.dispose();
								}
								folder.setSelection(folder.getSelectionIndex());
							}
						});
					}
					if (folder.getItemCount() > 0) {
						MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText("Close All");
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								List<RaptorTabItem> itemsToClose = new ArrayList<RaptorTabItem>(
										folder.getItemCount());
								for (int i = 0; i < folder.getItemCount(); i++) {
									if (folder.getRaptorTabItemAt(i).raptorItem
											.confirmClose()) {
										itemsToClose.add(folder
												.getRaptorTabItemAt(i));
									}
								}
								for (RaptorTabItem item : itemsToClose) {
									item.dispose();
								}

								if (folder.getItemCount() == 0) {
									restoreFolders();
								} else {
									folder.setSelection(folder
											.getSelectionIndex());
								}
							}
						});
					}

					new MenuItem(menu, SWT.SEPARATOR);

					Quadrant[] availableQuadrants = folder
							.getRaptorTabItemSelection().raptorItem
							.getMoveToQuadrants();
					for (int i = 0; i < availableQuadrants.length; i++) {
						if (availableQuadrants[i] != folder.quad) {
							final Quadrant currentQuadrant = availableQuadrants[i];
							MenuItem item = new MenuItem(menu, SWT.PUSH);
							item.setText("Move to " + currentQuadrant.name());
							item.addListener(SWT.Selection, new Listener() {
								public void handleEvent(Event e) {
									RaptorTabItem item = folder
											.getRaptorTabItemSelection();
									item.onMoveTo(folders[currentQuadrant
											.ordinal()]);

								}
							});
						}
					}

					MenuItem imageMenuItem = new MenuItem(menu, SWT.CASCADE);
					imageMenuItem.setImage(Raptor.getInstance().getImage(
							Raptor.RESOURCES_DIR + "/images/quadrantsSmall"
									+ folder.quad.toString() + ".png"));

					menu.setLocation(folder.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!folder.getDisplay().readAndDispatch()) {
							folder.getDisplay().sleep();
						}
					}
					menu.dispose();
				}
			}
		});

		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
				case SWT.DragDetect: {
					Point p = folder.toControl(getShell().getDisplay()
							.getCursorLocation());
					CTabItem item = folder.getItem(p);
					if (item == null) {
						return;
					}
					isInDrag = true;
					isExitDrag = false;
					dragStartItem = (RaptorTabItem) item;
					getShell().setCursor(
							getShell().getDisplay().getSystemCursor(
									SWT.CURSOR_HAND));
					break;
				}

				case SWT.MouseUp: {
					if (!isInDrag) {
						return;
					}
					getShell().setCursor(
							Raptor.getInstance().getCursorRegistry()
									.getDefaultCursor());

					RaptorTabFolder dropFolder = getFolderContainingCursor();
					if (dropFolder != null
							&& dropFolder != dragStartItem.raptorParent) {

						boolean canMove = false;
						for (int i = 0; i < dragStartItem.raptorItem
								.getMoveToQuadrants().length; i++) {
							if (dragStartItem.raptorItem.getMoveToQuadrants()[i] == dropFolder.quad) {
								canMove = true;
								break;
							}
						}

						if (canMove) {
							dragStartItem.onMoveTo(dropFolder);
						} else {
							Raptor
									.getInstance()
									.alert(
											"You can't move this item to quadrant "
													+ dropFolder.quad
													+ ". You can only move it to quadrants: "
													+ Arrays
															.toString(dragStartItem.raptorItem
																	.getMoveToQuadrants()));
						}
					}
					isInDrag = false;
					isExitDrag = false;
					dragStartItem = null;
					break;
				}
				}
			}
		};
		folder.addListener(SWT.DragDetect, listener);
		folder.addListener(SWT.MouseUp, listener);

	}

	/**
	 * Initialzes the windows bounds.
	 */
	@Override
	protected void initializeBounds() {
		Rectangle screenBounds = getPreferences().getCurrentLayoutRectangle(
				PreferenceKeys.WINDOW_BOUNDS);

		if (screenBounds.width == -1 || screenBounds.height == -1) {
			Rectangle fullViewBounds = Display.getCurrent().getPrimaryMonitor()
					.getBounds();
			screenBounds.width = fullViewBounds.width;
			screenBounds.height = fullViewBounds.height;
		}
		getShell().setSize(screenBounds.width, screenBounds.height);
		getShell().setLocation(screenBounds.x, screenBounds.y);
	}

	/**
	 * Restores the window to a non maximized state. If a Quadrant contains no
	 * items, it is not visible.
	 */
	protected void restoreFolders() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Entering RaptorWindow.restoreFolders");
		}
		long startTime = System.currentTimeMillis();

		for (RaptorSashForm sashe : sashes) {
			sashe.restore();
		}

		/**
		 * Set the selected item on all of the folders again. This ensures the
		 * toolbars will all be correct and activate/passivate will get invoked.
		 */
		for (int i = 0; i < folders.length; i++) {
			if (folders[i].getItemCount() > 0 && !folders[i].getMinimized()) {
				folders[i].setSelection(folders[i].getSelectionIndex());
			} else {
				folders[i].updateToolbar(false);
				folders[i].passivateActiveateItems();
			}
		}

		adjustToFoldersItemsMinimizied();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Leaving restoreFolders execution in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	protected void storeBounds() {
		getPreferences().setCurrentLayoutRectangle(
				PreferenceKeys.WINDOW_BOUNDS, getShell().getBounds());
	}
}
