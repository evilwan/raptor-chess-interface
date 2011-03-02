/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.DirectoryDialog;
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
import raptor.international.L10n;
import raptor.layout.Layout;
import raptor.pref.PreferenceKeys;
import raptor.pref.PreferenceUtils;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.AliasService;
import raptor.service.ConnectorService;
import raptor.service.LayoutService;
import raptor.service.MemoService;
import raptor.service.SoundService;
import raptor.service.ThemeService;
import raptor.service.ThemeService.Theme;
import raptor.swt.BrowserWindowItem;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.ChessSetInstallDialog;
import raptor.swt.ItemChangedListener;
import raptor.swt.PgnProcessingDialog;
import raptor.swt.ProfileDialog;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.controller.BughousePartnerController;
import raptor.swt.chat.controller.ChannelController;
import raptor.swt.chat.controller.GameChatController;
import raptor.swt.chat.controller.PersonController;
import raptor.swt.chat.controller.RegExController;
import raptor.swt.chess.ChessBoardWindowItem;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.PlayingController;
import raptor.util.BrowserUtils;
import raptor.util.FileUtils;
import raptor.util.OSUtils;
import raptor.util.RaptorLogger;
import raptor.util.RaptorRunnable;
import raptor.util.RegExUtils;

/**
 * A Raptor window is broken up into quadrants. Each quadrant is tabbed. You can
 * add a RaptorWindowItem to any quadrant. Each quadrant can be
 * maximized,minimized, and restored. When all of the tabs in a quadrannt are
 * vacant, the area disappears. You can drag and drop RaptorWindowItems between
 * visible quadrants.
 */
public class RaptorWindow extends ApplicationWindow {
	protected static L10n local = L10n.getInstance();
	
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
		protected RaptorWindowSashForm raptorSash;
		

		public RaptorTabFolder(RaptorWindowSashForm raptorSash, int style,
				Quadrant quad) {
			super(raptorSash, style);
			this.quad = quad;
			this.raptorSash = raptorSash;

			setSimple(false);
			setUnselectedImageVisible(true);
			setUnselectedCloseVisible(true);
			setMaximizeVisible(true);
			setMinimizeVisible(true);

			setSelectionBackground(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION));
			setSelectionForeground(getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION_TEXT));
		}

		/**
		 * Activates the current selected item.
		 */
		public void activate() {
			if (getRaptorTabItemSelection() != null) {
				getRaptorTabItemSelection().raptorItem.onActivate();
			}
		}

		public boolean contains(RaptorWindowItem item) {
			boolean result = false;
			for (int i = 0; i < getItemCount(); i++) {
				if (getRaptorTabItemAt(i).raptorItem == item) {
					result = true;
					break;
				}
			}
			return result;
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

			for (RaptorWindowSashForm form : sashes) {
				if (form == raptorSash) {
					form.setVisible(true);
					form.setMaximizedControl(this);
				} else {
					form.setVisible(false);
					form.setMaximizedControl(null);
				}
			}

			List<RaptorWindowSashForm> parents = new ArrayList<RaptorWindowSashForm>(
					10);
			Control currentSashParent = raptorSash;

			// Build a list of all the parents. The last entry in the list will
			// be the greatest ancestor.
			while (currentSashParent instanceof RaptorWindowSashForm) {
				parents.add((RaptorWindowSashForm) currentSashParent);
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
						LOG.debug("In updateToolbar selected RaptorWindowItem="
								+ currentSelection.raptorItem + " quad=" + quad);
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
			this(parent, style, item, true, true);
		}

		public RaptorTabItem(RaptorTabFolder parent, int style,
				final RaptorWindowItem item, boolean isInitingItem,
				boolean isSelecting) {
			super(parent, style);
			init(parent, item, isInitingItem, isSelecting);
		}

		public RaptorTabItem(RaptorTabFolder parent, int style,
				final RaptorWindowItem item, boolean isInitingItem,
				boolean isSelecting, int index) {
			super(parent, style, index);
			init(parent, item, isInitingItem, isSelecting);
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
			onMoveTo(newParent, -1);
		}

		public void onMoveTo(RaptorTabFolder newParent, int index) {
			if (!raptorItem.getControl().isReparentable()) {
				Raptor.getInstance()
						.alert(local.getString("rapWinL1"));
			} else {
				// This code is quite tricky and must happen in an exact order
				// or subtle issues occur with tool bars.
				raptorItem.removeItemChangedListener(listener);

				// Set the control to null so it can be re-parented.
				setControl(null);

				// Re-parent the control
				raptorItem.getControl().setParent(newParent);

				// Now add the new raptor tab item to the new parent.
				if (index == -1) {
					new RaptorTabItem(newParent, getStyle(), raptorItem, false,
							true);
				} else {
					new RaptorTabItem(newParent, getStyle(), raptorItem, false,
							true, index);
				}

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

		protected void init(RaptorTabFolder parent,
				final RaptorWindowItem item, boolean isInitingItem,
				boolean isSelecting) {
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
						getShell().getDisplay().asyncExec(new RaptorRunnable() {
							@Override
							public void execute() {

								if (LOG.isDebugEnabled()) {
									LOG.debug("Item changed, updating text,title,showClose");
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
										LOG.debug(
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
			if (isSelecting) {
				parent.setSelection(this);
			}
			itemsManaged.add(this);
		}
	}

	/**
	 * A sash form that loads and stores its weights in a specified key. A
	 * SashForm can only have a RaptorTabFolder or another RaptorSashFolder as a
	 * child.
	 */
	protected class RaptorWindowSashForm extends SashForm {
		protected String key;

		public RaptorWindowSashForm(Composite parent, int style, String key) {
			super(parent, style | SWT.SMOOTH);
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
				if (control instanceof RaptorWindowSashForm) {
					result += ((RaptorWindowSashForm) control).getItemsInSash();
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

		@Override
		public void layout(boolean changed) {
			long startTime = System.currentTimeMillis();
			super.layout(changed);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Laid out " + key + " in "
						+ (System.currentTimeMillis() - startTime));
			}
		}

		public void loadFromPreferences() {
			setWeights(getPreferences().getIntArray(key));
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
				if (element instanceof RaptorWindowSashForm) {
					if (((RaptorWindowSashForm) element).getItemsInSash() > 0) {
						RaptorWindowSashForm childSashForm = (RaptorWindowSashForm) element;
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

			layout();
		}

		public void storeSashWeights() {
			try {
				if (getMaximizedControl() == null && isVisible()) {
					getPreferences().setValue(key, getWeights());
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

	protected List<RaptorTabItem> itemsManaged = Collections
			.synchronizedList(new ArrayList<RaptorTabItem>());

	protected RaptorTabItem dragStartItem;

	protected RaptorTabFolder[] folders = new RaptorTabFolder[Quadrant.values().length];

	protected boolean isExitDrag = false;
	protected boolean isInDrag = false;
	protected CoolBar leftCoolbar;
	RaptorLogger LOG = RaptorLogger.getLog(RaptorWindow.class);
	protected Map<String, Label> pingLabelsMap = new HashMap<String, Label>();

	protected RaptorWindowSashForm quad9quad12345678;
	protected RaptorWindowSashForm quad1quad2345678;
	protected RaptorWindowSashForm quad2345quad678Sash;
	protected RaptorWindowSashForm quad2quad3quad4quad5Sash;
	protected RaptorWindowSashForm quad67quad8Sash;
	protected RaptorWindowSashForm quad6quad7Sash;
	protected RaptorWindowSashForm[] sashes = new RaptorWindowSashForm[6];
	protected Composite statusBar;
	protected Combo zoomCombo;
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
		addRaptorWindowItem(item, true, true);
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
		addRaptorWindowItem(item, isAsynch, true);
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
			boolean isAsynch, final boolean isSelecting) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding raptor window item " + item);
		}

		if (!isAsynch) {
			getShell().getDisplay().syncExec(new RaptorRunnable() {
				@Override
				public void execute() {
					RaptorTabFolder folder = getRaptorTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item, true, folder
							.getItemCount() == 0 ? true : isSelecting);
					folder.setMinimized(false);
					restoreFolders();
				}
			});
		} else {
			getShell().getDisplay().asyncExec(new RaptorRunnable() {
				@Override
				public void execute() {
					RaptorTabFolder folder = getRaptorTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item, true, folder
							.getItemCount() == 0 ? true : isSelecting);
					folder.setMinimized(false);
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
						if (StringUtils.equalsIgnoreCase(
								controller.getChannel(), channel)) {
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
	 * Returns true if this RaptorWindow is managing a game chat tell tab for
	 * the specified gameId.
	 */
	public boolean containsGameChatTab(Connector connector, String gameId) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem item = (ChatConsoleWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getController().getConnector() == connector
							&& item.getController() instanceof GameChatController) {
						GameChatController controller = (GameChatController) item
								.getController();
						if (StringUtils.equalsIgnoreCase(
								controller.getGameId(), gameId)) {
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
	 * Returns true if this RaptorWindow is managing a channel tell tab for the
	 * specified channel.
	 */
	public boolean containsRegExItem(Connector connector, String pattern) {
		boolean result = false;
		for (RaptorTabFolder folder : folders) {
			for (int i = 0; i < folder.getItemCount(); i++) {
				if (folder.getRaptorTabItemAt(i).raptorItem instanceof ChatConsoleWindowItem) {
					ChatConsoleWindowItem item = (ChatConsoleWindowItem) folder
							.getRaptorTabItemAt(i).raptorItem;
					if (item.getController().getConnector() == connector
							&& item.getController() instanceof RegExController) {
						RegExController controller = (RegExController) item
								.getController();
						if (StringUtils
								.equals(controller.getPattern(), pattern)) {
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
	public boolean containsWindowItems(
			@SuppressWarnings("rawtypes") Class windowItemClass) {
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
		getShell().getDisplay().syncExec(new RaptorRunnable() {
			@Override
			public void execute() {
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
	public void forceFocus(final RaptorWindowItem windowItem) {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (Raptor.getInstance().isShutdown()) {
					return;
				}

				boolean wasRestored = false;
				synchronized (itemsManaged) {
					for (RaptorTabItem currentTabItem : itemsManaged) {
						if (currentTabItem.raptorItem == windowItem) {
							currentTabItem.raptorParent
									.setSelection(currentTabItem);
							if (currentTabItem.raptorParent.getMinimized()) {
								currentTabItem.raptorParent.setMinimized(false);
								restoreFolders();
								wasRestored = true;
							}
							break;
						}
					}
				}

				// Now check to see if a folder is maximized. If one is then
				// do a
				// restore.
				if (!wasRestored) {
					boolean isFolderMaximized = false;
					for (RaptorTabFolder folder : folders) {
						if (folder.getMaximized()
								&& !folder.contains(windowItem)) {
							isFolderMaximized = true;
							break;
						}
					}

					if (isFolderMaximized) {
						restoreFolders();
					}
				}

				// Now make the current item the selection in the folder.
				outer: for (RaptorTabFolder folder : folders) {
					if (folder.contains(windowItem)) {
						for (int i = 0; i < folder.getItemCount(); i++) {
							if (folder.getRaptorTabItemAt(i).raptorItem == windowItem) {
								folder.setSelection(i);
								break outer;
							}
						}
					}
				}
			}
		});
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
							&& item.getController().getGame().getId()
									.equals(gameId)) {
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
					if (item.isTakeOverable()) {
						result = item;
						break;
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
	 * Returns all RaptorWindowItems that are being managed and are of the
	 * specified class type and are currently selected within their parent
	 * RaptorTabFolder.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	public RaptorWindowItem[] getSelectedWindowItems(
			@SuppressWarnings("rawtypes") Class windowItemClass) {
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
	 * Returns an array of all RaptorWindowItem's being managed.
	 * 
	 * @return
	 */
	public RaptorWindowItem[] getWindowItems() {
		List<RaptorWindowItem> result = new ArrayList<RaptorWindowItem>(10);

		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				result.add(currentTabItem.raptorItem);
			}
		}

		return result.toArray(new RaptorWindowItem[0]);
	}

	/**
	 * Returns all RaptorWindowItems that are being managed and are of the
	 * specified class type.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	public RaptorWindowItem[] getWindowItems(
			@SuppressWarnings("rawtypes") Class windowItemClass) {
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
	 * Returns all RaptorWindowItems that are being managed and belong to the
	 * specified connetor.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	public RaptorConnectorWindowItem[] getWindowItems(Connector connector) {
		List<RaptorConnectorWindowItem> result = new ArrayList<RaptorConnectorWindowItem>(
				10);
		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorItem instanceof RaptorConnectorWindowItem) {
					RaptorConnectorWindowItem connectorItem = (RaptorConnectorWindowItem) currentTabItem.raptorItem;
					if (connectorItem.getConnector() == connector) {
						result.add((RaptorConnectorWindowItem) currentTabItem.raptorItem);
					}
				}
			}
		}
		return result.toArray(new RaptorConnectorWindowItem[0]);
	}

	/**
	 * Returns all RaptorWindowItems that are being managed and belong to the
	 * specified connector.
	 * 
	 * @param windowItemClass
	 *            The window item class.
	 * @return The result.
	 */
	public RaptorWindowItem[] getWindowItems(Quadrant quadrant) {
		List<RaptorWindowItem> result = new ArrayList<RaptorWindowItem>(10);
		synchronized (itemsManaged) {
			for (RaptorTabItem currentTabItem : itemsManaged) {
				if (currentTabItem.raptorParent.quad == quadrant) {
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
	public void setPingTime(final Connector connectorToSet, final long pingTime) {
		if (Raptor.getInstance().isShutdown()
				|| Raptor.getInstance().isDisposed() || zoomCombo == null
				|| statusBar == null) {
			return;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("setPingTime " + connectorToSet.getShortName() + " "
					+ pingTime);
		}

		Raptor.getInstance().getDisplay().syncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (pingLabelsMap != null && connectorToSet != null) {
					Label label = pingLabelsMap.get(connectorToSet
							.getShortName());
					if (label == null) {
						zoomCombo.dispose();
						label = new Label(statusBar, SWT.NONE);
						GridData gridData = new GridData();
						gridData.grabExcessHorizontalSpace = false;
						gridData.grabExcessVerticalSpace = false;
						gridData.horizontalAlignment = SWT.END;
						gridData.verticalAlignment = SWT.CENTER;
						label.setLayoutData(gridData);
						pingLabelsMap.put(connectorToSet.getShortName(), label);
						label.setFont(Raptor.getInstance().getPreferences()
								.getFont(PreferenceKeys.APP_PING_FONT, false));
						label.setForeground(Raptor.getInstance()
								.getPreferences()
								.getColor(PreferenceKeys.APP_PING_COLOR));
						label.setToolTipText("An estimate of ping time.");
						createZoomCombo();
						statusBar.layout(true, true);
					}
					if (pingTime == -1) {
						label.setVisible(false);
						label.dispose();
						pingLabelsMap.remove(connectorToSet.getShortName());
						statusBar.layout(true, true);
					} else {
						label.setText(" " + connectorToSet.getShortName()
								+ " (" + pingTime + "ms) ");
						label.setVisible(true);
						statusBar.layout(true, true);
						label.redraw();
					}
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

		getShell().getDisplay().syncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				statusLabel.setText(StringUtils.defaultString(newStatusMessage));
			}
		});
	}

	public void storeAllSashWeights() {
		for (RaptorWindowSashForm sash : sashes) {
			sash.storeSashWeights();
		}
	}

	public void storeWindowPreferences() {
		// Don't use a RaptorRunnable for this it might be called when shutdown.
		Raptor.getInstance().getDisplay().syncExec(new Runnable() {
			public void run() {

				getPreferences().setValue(PreferenceKeys.APP_WINDOW_BOUNDS,
						getShell().getBounds());
				storeAllSashWeights();
			}
		});
	}

	/**
	 * Adjusts the left coolbar for quadrants minimized.
	 */
	protected void adjustToFoldersItemsMinimizied() {
		boolean isAFolderMinimized = false;
		for (RaptorTabFolder folder : folders) {
			if (folder.getMinimized()) {
				isAFolderMinimized = true;
				break;
			}
		}

		if (isAFolderMinimized) {
			leftCoolbar.setVisible(true);
			ToolBar foldersMinimizedToolbar = null;

			CoolItem foldersMinimiziedCoolItem = null;

			if (leftCoolbar.getItemCount() > 0) {
				foldersMinimiziedCoolItem = leftCoolbar.getItem(0);
				foldersMinimizedToolbar = (ToolBar) foldersMinimiziedCoolItem
						.getControl();

				ToolItem[] items = foldersMinimizedToolbar.getItems();
				for (ToolItem item : items) {
					item.dispose();
				}
			} else {
				foldersMinimiziedCoolItem = new CoolItem(leftCoolbar, SWT.NONE);
				foldersMinimizedToolbar = new ToolBar(leftCoolbar, SWT.FLAT
						| SWT.VERTICAL);
				foldersMinimiziedCoolItem.setControl(foldersMinimizedToolbar);
				foldersMinimiziedCoolItem
						.setPreferredSize(foldersMinimizedToolbar.computeSize(
								SWT.DEFAULT, SWT.DEFAULT));
			}

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

			foldersMinimizedToolbar.pack();
			foldersMinimiziedCoolItem.setPreferredSize(foldersMinimizedToolbar
					.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		} else {
			if (leftCoolbar.getItemCount() > 0) {
				CoolItem foldersMinimiziedCoolItem = leftCoolbar.getItem(0);
				ToolBar foldersMinimizedToolbar = (ToolBar) foldersMinimiziedCoolItem
						.getControl();

				ToolItem[] items = foldersMinimizedToolbar.getItems();
				for (ToolItem item : items) {
					item.dispose();
				}

				foldersMinimizedToolbar.dispose();
				foldersMinimiziedCoolItem.dispose();
			}
			leftCoolbar.setVisible(false);
		}
		windowComposite.layout(true);
	}

	/**
	 * Creates the controls.
	 */
	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(
				Raptor.getInstance().getPreferences()
						.getString(PreferenceKeys.APP_NAME));
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

		sashes[0] = quad9quad12345678;
		sashes[1] = quad1quad2345678;
		sashes[2] = quad2quad3quad4quad5Sash;
		sashes[3] = quad2345quad678Sash;
		sashes[4] = quad67quad8Sash;
		sashes[5] = quad6quad7Sash;

		for (RaptorWindowSashForm sashe : sashes) {
			sashe.loadFromPreferences();
			sashe.setVisible(false);
			sashe.setMaximizedControl(null);
		}
	}

	protected void createLeftCoolbar() {
		leftCoolbar = new CoolBar(windowComposite, SWT.FLAT | SWT.VERTICAL);
		leftCoolbar.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true,
				1, 1));
		leftCoolbar.setVisible(false);
	}

	protected void buildFileMenu(MenuManager fileMenu) {
		fileMenu.add(new Action(local.getString("rapWinL2")) {
			@Override
			public void run() {
				String lastFile = getPreferences().getString(
						PreferenceKeys.BOARD_LAST_OPEN_PGN);

				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				if (StringUtils.isNotBlank(lastFile)) {
					fd.setFilterPath(lastFile);
				} else {
					fd.setFilterPath("");
				}
				fd.setText(local.getString("rapWinL3"));
				String[] filterExt = { "*.pgn", "*" };
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					getPreferences().setValue(
							PreferenceKeys.BOARD_LAST_OPEN_PGN, selected);
					PgnProcessingDialog dialog = new PgnProcessingDialog(
							getShell(), selected);
					dialog.open();
				}
			}
		});
		fileMenu.add(new Action(local.getString("rapWinL4")) {
			@Override
			public void run() {
				File file = new File(getPreferences().getString(
						PreferenceKeys.APP_PGN_FILE));
				if (!file.exists()) {
					Raptor.getInstance().alert(
							local.getString("rapWinL5")
									+ file.getAbsolutePath());
				} else {
					PgnProcessingDialog dialog = new PgnProcessingDialog(
							getShell(), file.getAbsolutePath());
					dialog.open();
				}
			}
		});
		fileMenu.add(new Separator());
		fileMenu.add(new Action(local.getString("rapWinL6")) {
			@Override
			public void run() {
				String html = MemoService.getInstance().getMemosHTML();
				BrowserUtils.openHtml(html);
			}
		});
		fileMenu.add(new Separator());
		fileMenu.add(new Action(local.getString("rapWinL7")) {
			@Override
			public void run() {
				if (OSUtils.isLikelyWindows()) {
					BrowserUtils.openExternalUrl("file://"
							+ Raptor.USER_RAPTOR_HOME_PATH
							+ "\\logs\\console\\");
				} else {
					BrowserUtils.openExternalUrl("file://"
							+ Raptor.USER_RAPTOR_HOME_PATH + "/logs/console/");
				}
			}
		});
		fileMenu.add(new Action(local.getString("rapWinL8")) {
			@Override
			public void run() {
				String html = FileUtils
						.fileAsString(Raptor.USER_RAPTOR_HOME_PATH
								+ "/logs/error.log");
				if (html != null) {
					html = "<html>\n<head>\n<title></title>\n</head>\n<body>\n<h1>RAPTOR ERROR LOG</h1>\n<pre>\n"
							+ html + "</pre>\n</body>\n</html>\n";
					BrowserUtils.openHtml(html);
				}
			}
		});

		fileMenu.add(new Separator());
		fileMenu.add(new Action(local.getString("rapWinL9")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/AdditionalChessSets");
			}
		});
		fileMenu.add(new Action(local.getString("rapWinL10")) {
			@Override
			public void run() {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setFilterPath("");

				fd.setText(local.getString("rapWinL11"));
				String[] filterExt = { "*.zip" };
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					ChessSetInstallDialog dialog = new ChessSetInstallDialog(
							getShell(), selected);
					dialog.open();
				}
			}
		});

		if (!OSUtils.isLikelyOSXCocoa()) {
			fileMenu.add(new Separator());
			fileMenu.add(new Action(local.getString("rapWinL12")) {
				@Override
				public void run() {
					PreferenceUtils.launchPreferenceDialog();
				}
			});
		}

		String osName = System.getProperty("os.name");
		if (!osName.startsWith("Mac OS")) {
			fileMenu.add(new Separator());
			fileMenu.add(new Action(local.getString("rapWinL13")) {
				@Override
				public void run() {
					Raptor.getInstance().shutdown();
				}
			});
		}
	}

	protected void buildWindowMenu(MenuManager windowMenu) {
		final MenuManager layoutsMenu = new MenuManager(local.getString("rapWinL14"));
		layoutsMenu.add(new Action(local.getString("rapWinL15")) {
			@Override
			public void run() {
				String layoutName = Raptor.getInstance().promptForText(
						local.getString("rapWinL16"));
				if (StringUtils.isNotBlank(layoutName)) {
					final Layout newLayout = LayoutService.getInstance()
							.saveCurrentAsCustomLayout(layoutName);

					layoutsMenu.add(new Action(newLayout.getName(), null) {
						public void run() {
							newLayout.apply();
						}
					});
				}
			}
		});
		layoutsMenu.add(new Action(local.getString("rapWinL17")) {
			@Override
			public void run() {
				DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
				fd.setFilterPath("");

				fd.setText(local.getString("rapWinL18"));
				final String directory = fd.open();
				
				if (!StringUtils.isBlank(directory)) {
					final String layoutName = Raptor.getInstance().promptForText(local.getString("rapWinL19"));
					if (StringUtils.isNotBlank(layoutName)) {
						LayoutService.getInstance().exportCurrentLayout(layoutName,directory);
						Raptor.getInstance().alert("Exported " + directory + "/" + layoutName + ".properties" );

					}
				}
			}
		});
		layoutsMenu.add(new Action(local.getString("rapWinL20")) {
			@Override
			public void run() {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setFilterPath("");

				fd.setText(local.getString("rapWinL21"));
				String[] filterExt = { "*.properties"};
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				
				if (!StringUtils.isBlank(selected)) {
					final Layout layout = LayoutService.getInstance().importLayout(selected);
					layoutsMenu.add(new Action(layout.getName()) {
						@Override
						public void run() {
							layout.apply();
						}
					});
					Raptor.getInstance().alert(local.getString("rapWinL22") + layout.getName() );
				}
				
			}
		});
		layoutsMenu.add(new Separator());

		MenuManager bughouseLayoutsMenu = new MenuManager(local.getString("rapWinL23"));
		Layout[] bughouseLayouts = LayoutService.getInstance()
				.getBughouoseSystemLayouts();
		for (final Layout bugLayout : bughouseLayouts) {
			bughouseLayoutsMenu.add(new Action(bugLayout.getName(), null) {
				public void run() {
					bugLayout.apply();
				}
			});
		}
		layoutsMenu.add(bughouseLayoutsMenu);

		Layout[] layouts = LayoutService.getInstance()
				.getNonBughouseSystemLayouts();
		for (final Layout layout : layouts) {
			layoutsMenu.add(new Action(layout.getName(), null) {
				public void run() {
					layout.apply();
				}
			});
		}

		//layoutsMenu.add(new Separator());
		Layout[] customLayouts = LayoutService.getInstance().getCustomLayouts();
		for (final Layout layout : customLayouts) {
			layoutsMenu.add(new Action(layout.getName(), null) {
				public void run() {
					layout.apply();
				}
			});
		}
		windowMenu.add(layoutsMenu);

		final MenuManager themesMenu = new MenuManager(local.getString("rapWinL24"));
		windowMenu.add(themesMenu);
		themesMenu.add(new Action(local.getString("rapWinL25")) {
			@Override
			public void run() {
				String themeName = Raptor.getInstance().promptForText(
						local.getString("rapWinL26"));
				if (StringUtils.isNotBlank(themeName)) {
					final Theme newTheme = ThemeService.getInstance().saveCurrentAsTheme(themeName);
					themesMenu.add(new Action(newTheme.getName()) {
						@Override
						public void run() {
							ThemeService.getInstance().applyTheme(newTheme);
						}
					});
				}
			}
		});
		themesMenu.add(new Action(local.getString("rapWinL17")) {
			@Override
			public void run() {
				DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
				fd.setFilterPath("");

				fd.setText(local.getString("rapWinL27"));
				final String directory = fd.open();
				
				if (!StringUtils.isBlank(directory)) {
					final String themeName = Raptor.getInstance().promptForText(local.getString("rapWinL28"));
					if (StringUtils.isNotBlank(themeName)) {
						ThemeService.getInstance().exportCurrentTheme(themeName,directory);
						Raptor.getInstance().alert("Exported " + directory + "/" + themeName + ".properties" );

					}
				}
			}
		});
		themesMenu.add(new Action(local.getString("rapWinL20")) {
			@Override
			public void run() {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setFilterPath("");

				fd.setText(local.getString("rapWinL29"));
				String[] filterExt = { "*.properties"};
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				
				if (!StringUtils.isBlank(selected)) {
					final Theme theme = ThemeService.getInstance().importTheme(selected);
					themesMenu.add(new Action(theme.getName()) {
						@Override
						public void run() {
							ThemeService.getInstance().applyTheme(theme);
						}
					});
					Raptor.getInstance().alert("Added theme " + theme.getName() );
				}
				
			}
		});
		
		themesMenu.add(new Separator());		

		String[] themeNames = ThemeService.getInstance().getThemeNames();
		for (String themeName : themeNames) {
			final Theme theme = ThemeService.getInstance().getTheme(themeName);

			themesMenu.add(new Action(theme.getName()) {
				@Override
				public void run() {
					ThemeService.getInstance().applyTheme(theme);
				}
			});
		}
	}

	public void buildHelpMenu(MenuManager helpMenu) {

		if (!OSUtils.isLikelyOSXCocoa()) {
			helpMenu.add(new Action(getPreferences().getString(
					PreferenceKeys.APP_NAME)) {
				@Override
				public void run() {
					SoundService.getInstance()
							.play(Raptor.RESOURCES_DIR
									+ "sounds/misc/raptorRoar.wav");
				}
			});

			helpMenu.add(new Action(local.getString("rapWinL30")) {
				@Override
				public void run() {
					BrowserUtils
							.openUrl("http://code.google.com/p/raptor-chess-interface/");
				}
			});
		}
		helpMenu.add(new Action(local.getString("rapWinL31")) {
			@Override
			public void run() {
				String html = FileUtils.fileAsString("bsd-new-license.html");
				if (html != null) {
					BrowserUtils.openHtml(html);
				}
			}
		});
		helpMenu.add(new Action(local.getString("rapWinL32")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/Thanks");
			}
		});
		helpMenu.add(new Action(local.getString("rapWinL33")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/ThirdPartyContent");

			}
		});
		helpMenu.add(new Separator());

		MenuManager raptorHelp = new MenuManager(local.getString("rapWinL34"));
		raptorHelp.add(new Action(local.getString("rapWinL35")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/NewToRaptor");
			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL36")) {
			@Override
			public void run() {
				BrowserUtils
						.openHtml(AliasService.getInstance().getAliasHtml());
			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL37")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/FAQ");

			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL38")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/LinuxSoundIssues");
			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL39")) {
			@Override
			public void run() {
				BrowserUtils.openHtml(RegExUtils.getRegularExpressionHelpHtml());
			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL40")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/Scripting");
			}
		});
		raptorHelp.add(new Action(local.getString("rapWinL41")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/UsefulTips");

			}
		});
		helpMenu.add(raptorHelp);

		MenuManager ficsHelp = new MenuManager(local.getString("rapWinL42"));
		ficsHelp.add(new Action(local.getString("rapWinL43")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/wiki/NewToFics");
			}
		});
		ficsHelp.add(new Action(local.getString("rapWinL44")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://www.freechess.org/Help/AllFiles.html");

			}
		});
		ficsHelp.add(new Action(local.getString("rapWinL45")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://www.freechess.org/Help/ficsfaq.html");

			}
		});
		ficsHelp.add(new Action(local.getString("rapWinL46")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://www.freechess.org/Help/index.html");
			}
		});
		helpMenu.add(ficsHelp);

		helpMenu.add(new Separator());
		helpMenu.add(new Action(local.getString("rapWinL47")) {
			@Override
			public void run() {
				ProfileDialog dialog = new ProfileDialog();
				dialog.open();
			}
		});

		helpMenu.add(new Action(local.getString("rapWinL48")) {
			@Override
			public void run() {
				BrowserUtils
						.openUrl("http://code.google.com/p/raptor-chess-interface/issues/entry");
			}
		});
	}

	/**
	 * Creates the menu items.
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager(local.getString("rapWinL49"));
		MenuManager fileMenu = new MenuManager(local.getString("rapWinL50"));
		MenuManager windowMenu = new MenuManager(local.getString("rapWinL51"));
		MenuManager helpMenu = new MenuManager(local.getString("rapWinL52"));

		buildFileMenu(fileMenu);
		menuBar.add(fileMenu);

		Connector[] connectors = ConnectorService.getInstance().getConnectors();
		for (Connector connector : connectors) {
			MenuManager manager = connector.getMenuManager();
			if (manager != null) {
				menuBar.add(manager);
			}
		}

		buildWindowMenu(windowMenu);
		menuBar.add(windowMenu);

		buildHelpMenu(helpMenu);
		menuBar.add(helpMenu);

		if (OSUtils.isLikelyOSXCocoa()) {
			try {
				CocoaUIEnhancer enhancer = new CocoaUIEnhancer(getPreferences()
						.getString(PreferenceKeys.APP_NAME));
				enhancer.hookApplicationMenu(Raptor.getInstance().getDisplay(),
						new Listener() {
							public void handleEvent(Event event) {
								Raptor.getInstance().shutdown();
							}
						}, new org.eclipse.jface.action.Action() {
							@Override
							public void run() {
								BrowserUtils
										.openUrl("http://code.google.com/p/raptor-chess-interface/");
							}
						}, new org.eclipse.jface.action.Action() {
							@Override
							public void run() {
								PreferenceUtils.launchPreferenceDialog();
							}
						});
			} catch (Throwable t) {
				// Just eat it for now.
			}

		}

		return menuBar;
	}

	/**
	 * Creates the sash hierarchy.
	 */
	protected void createQuad1Quad234567QuadControls() {

		quad9quad12345678 = new RaptorWindowSashForm(windowComposite,
				SWT.HORIZONTAL,
				PreferenceKeys.APP_QUAD9_QUAD12345678_SASH_WEIGHTS);

		quad9quad12345678.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));

		folders[Quadrant.IX.ordinal()] = new RaptorTabFolder(quad9quad12345678,
				SWT.BORDER, Quadrant.IX);

		quad1quad2345678 = new RaptorWindowSashForm(quad9quad12345678,
				SWT.VERTICAL, PreferenceKeys.APP_QUAD1_QUAD2345678_SASH_WEIGHTS);

		folders[Quadrant.I.ordinal()] = new RaptorTabFolder(quad1quad2345678,
				SWT.BORDER, Quadrant.I);

		quad2345quad678Sash = new RaptorWindowSashForm(quad1quad2345678,
				SWT.VERTICAL, PreferenceKeys.APP_QUAD2345_QUAD678_SASH_WEIGHTS);

		quad2quad3quad4quad5Sash = new RaptorWindowSashForm(
				quad2345quad678Sash, SWT.HORIZONTAL,
				PreferenceKeys.APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS);

		quad67quad8Sash = new RaptorWindowSashForm(quad2345quad678Sash,
				SWT.HORIZONTAL, PreferenceKeys.APP_QUAD67_QUAD8_SASH_WEIGHTS);

		folders[Quadrant.II.ordinal()] = new RaptorTabFolder(
				quad2quad3quad4quad5Sash, SWT.BORDER, Quadrant.II);

		folders[Quadrant.III.ordinal()] = new RaptorTabFolder(
				quad2quad3quad4quad5Sash, SWT.BORDER, Quadrant.III);

		folders[Quadrant.IV.ordinal()] = new RaptorTabFolder(
				quad2quad3quad4quad5Sash, SWT.BORDER, Quadrant.IV);

		folders[Quadrant.V.ordinal()] = new RaptorTabFolder(
				quad2quad3quad4quad5Sash, SWT.BORDER, Quadrant.V);

		quad6quad7Sash = new RaptorWindowSashForm(quad67quad8Sash,
				SWT.VERTICAL, PreferenceKeys.APP_QUAD6_QUAD7_SASH_WEIGHTS);

		folders[Quadrant.VI.ordinal()] = new RaptorTabFolder(quad6quad7Sash,
				SWT.BORDER, Quadrant.VI);

		folders[Quadrant.VII.ordinal()] = new RaptorTabFolder(quad6quad7Sash,
				SWT.BORDER, Quadrant.VII);

		folders[Quadrant.VIII.ordinal()] = new RaptorTabFolder(quad67quad8Sash,
				SWT.BORDER, Quadrant.VIII);
	}

	/**
	 * Creates the status bar controls.
	 */
	protected void createStatusBarControls() {
		if (Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.APP_SHOW_STATUS_BAR)) {
			statusBar = new Composite(windowComposite, SWT.NONE);
			statusBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false, 2, 1));
			GridLayout statusBarLayout = SWTUtils.createMarginlessGridLayout(
					20, false);
			statusBar.setLayout(statusBarLayout);

			statusLabel = new Label(statusBar, SWT.NONE);
			statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			statusLabel.setFont(Raptor.getInstance().getPreferences()
					.getFont(PreferenceKeys.APP_STATUS_BAR_FONT, false));
			statusLabel.setForeground(Raptor.getInstance().getPreferences()
					.getColor(PreferenceKeys.APP_STATUS_BAR_COLOR));

			createZoomCombo();
		}
	}

	protected void createZoomCombo() {
		zoomCombo = new Combo(statusBar, SWT.READ_ONLY | SWT.BORDER);
		zoomCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
				false, 1, 1));
		zoomCombo.add("25%");
		zoomCombo.add("50%");
		zoomCombo.add("70%");
		zoomCombo.add("80%");
		zoomCombo.add("90%");
		zoomCombo.add("100%");
		zoomCombo.add("110%");
		zoomCombo.add("120%");
		zoomCombo.add("130%");
		zoomCombo.add("150%");
		zoomCombo.add("175%");
		zoomCombo.add("200%");
		zoomCombo.add("250%");
		zoomCombo
				.setToolTipText(local.getString("rapWinL53"));

		int selection = -1;

		int resizePercentage = (int) (Raptor.getInstance().getPreferences()
				.getDouble(PreferenceKeys.APP_ZOOM_FACTOR) * 100);
		String resizeValue = resizePercentage + "%";
		for (int i = 0; i < zoomCombo.getItemCount(); i++) {
			if (resizeValue.equals(zoomCombo.getItem(i))) {
				selection = i;
				break;
			}
		}
		zoomCombo.select(selection);

		zoomCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selection = zoomCombo.getText();
				if (StringUtils.isNotBlank(selection)) {
					selection = selection.substring(0, selection.length() - 1);
					double resize = Integer.parseInt(selection) / 100.0;
					Raptor.getInstance().getPreferences()
							.setValue(PreferenceKeys.APP_ZOOM_FACTOR, resize);
				}

			}
		});
		zoomCombo.setFont(Raptor.getInstance().getPreferences()
				.getFont(PreferenceKeys.APP_STATUS_BAR_FONT, false));
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
	 * Overridden to cleanly shut down raptor.
	 */
	@Override
	protected void handleShellCloseEvent() {

		for (RaptorConnectorWindowItem item : getWindowItems(ConnectorService
				.getInstance().getConnector("fics"))) {
			if (item instanceof ChessBoardWindowItem)
				if (((ChessBoardWindowItem) item).getController() instanceof PlayingController) {
					if (!Raptor.getInstance().confirm(
							local.getString("rapWinL54")))
						return;
				}
		}

		storeWindowPreferences();
		Raptor.getInstance().shutdown();
	}

	public void resetLayout() {
		RaptorTabItem[] tabItems = null;
		synchronized (itemsManaged) {
			tabItems = itemsManaged.toArray(new RaptorTabItem[0]);
		}
		LOG.info("Resetting layout ...");
		long time = System.currentTimeMillis();
		for (RaptorTabItem item : tabItems) {
			Quadrant currentQuadrant = item.raptorParent.quad;
			Quadrant newQuadrant = item.raptorItem.getPreferredQuadrant();
			if (currentQuadrant != newQuadrant) {
				item.onMoveTo(folders[newQuadrant.ordinal()]);
			}
		}

		for (RaptorWindowSashForm raptorSash : sashes) {
			raptorSash.loadFromPreferences();

		}

		LOG.info("Reset layout in " + (System.currentTimeMillis() - time)
				+ "ms");
	}

	/**
	 * Initializes a quad folder. Also sets up all of the listeners on the
	 * folder.
	 * 
	 * @param folder
	 */
	protected void initFolder(final RaptorTabFolder folder) {

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
					getShell().getDisplay().asyncExec(new RaptorRunnable() {
						@Override
						public void execute() {
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
				if (SWTUtils.isRightClick(e)) {
					final RaptorTabItem raptorTabItem = (RaptorTabItem) folder
							.getItem(new Point(e.x, e.y));
					if (raptorTabItem == null) {
						return;
					}
					Menu menu = new Menu(folder.getShell(), SWT.POP_UP);

					if (folder.getItemCount() > 0) {
						MenuItem item = new MenuItem(menu, SWT.PUSH);
						item.setText(local.getString("close"));
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								if (folder.getRaptorTabItemSelection() != null) {
									if (raptorTabItem.raptorItem.confirmClose()) {
										raptorTabItem.dispose();

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
						item.setText(local.getString("rapWinL55"));
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								List<RaptorTabItem> itemsToClose = new ArrayList<RaptorTabItem>(
										folder.getItemCount());
								for (int i = 0; i < folder.getItemCount(); i++) {
									if (folder.getItem(i) != raptorTabItem
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
						item.setText(local.getString("rapWinL56"));
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

					int itemIndex = -1;
					for (int i = 0; i < folder.getItemCount(); i++) {
						if (folder.getItem(i) == raptorTabItem) {
							itemIndex = i;
						}
					}

					final int finalItemIndex = itemIndex;

					if (itemIndex != -1 && itemIndex > 0
							|| itemIndex < folder.getItemCount() - 1) {
						new MenuItem(menu, SWT.SEPARATOR);
					}

					if (itemIndex != -1 && itemIndex > 0) {
						MenuItem moveLeft = new MenuItem(menu, SWT.PUSH);
						moveLeft.setText(local.getString("rapWinL57"));
						moveLeft.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								raptorTabItem.onMoveTo(folder,
										finalItemIndex - 2 < 0 ? 0
												: finalItemIndex - 2);
							}
						});
					}
					if (itemIndex != -1
							&& itemIndex < folder.getItemCount() - 1) {
						MenuItem moveLeft = new MenuItem(menu, SWT.PUSH);
						moveLeft.setText(local.getString("rapWinL58"));
						moveLeft.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								raptorTabItem.onMoveTo(folder,
										finalItemIndex + 2);
							}
						});
					}

					Quadrant[] availableQuadrants = folder
							.getRaptorTabItemSelection().raptorItem
							.getMoveToQuadrants();

					if (availableQuadrants.length > 0) {
						new MenuItem(menu, SWT.SEPARATOR);
					}

					if (OSUtils.isLikelyWindows()) {
						MenuItem defaultMenuItem = new MenuItem(menu,
								SWT.CASCADE);
						defaultMenuItem.setText(local.getString("rapWinL59"));

						Menu moveToMenu = new Menu(defaultMenuItem);
						defaultMenuItem.setMenu(moveToMenu);

						for (int i = 0; i < availableQuadrants.length; i++) {
							if (availableQuadrants[i] != folder.quad) {
								final Quadrant currentQuadrant = availableQuadrants[i];
								MenuItem moveToItem = new MenuItem(moveToMenu,
										SWT.PUSH);
								moveToItem.setText("Quad "
										+ currentQuadrant.toString());
								moveToItem.addListener(SWT.Selection,
										new Listener() {
											public void handleEvent(Event e) {
												{
													raptorTabItem
															.onMoveTo(folders[currentQuadrant
																	.ordinal()]);
												}
											}
										});
								moveToItem
										.setImage(Raptor
												.getInstance()
												.getImage(
														Raptor.RESOURCES_DIR
																+ "images/quadrantsSmall"
																+ currentQuadrant
																		.toString()
																+ ".png"));
								new MenuItem(moveToMenu, SWT.SEPARATOR);
							}
						}
					} else {
						for (int i = 0; i < availableQuadrants.length; i++) {
							final Quadrant currentQuadrant = availableQuadrants[i];
							if (currentQuadrant != folder.quad) {
								MenuItem moveToItem = new MenuItem(menu,
										SWT.PUSH);
								moveToItem.setText("Move to "
										+ currentQuadrant.name());

								moveToItem.addListener(SWT.Selection,
										new Listener() {
											public void handleEvent(Event e) {
												raptorTabItem
														.onMoveTo(folders[currentQuadrant
																.ordinal()]);

											}
										});
							}
						}
					}

					final MenuItem imageMenuItem = new MenuItem(menu, SWT.NONE);
					imageMenuItem.setImage(Raptor.getInstance().getImage(
							Raptor.RESOURCES_DIR + "images/quadrantsSmall"
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
						// Handles dragging and dropping into different folders.

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
							Raptor.getInstance()
									.alert(local.getString("rapWinL60")
											+ dropFolder.quad
											+ local.getString("rapWinL61")
											+ Arrays.toString(dragStartItem.raptorItem
													.getMoveToQuadrants()));
						}
					} else if (dropFolder != null
							&& dropFolder == dragStartItem.raptorParent) {
						// Handles dragging and dropping within the same folder.

						RaptorTabItem itemAtCursor = (RaptorTabItem) dropFolder
								.getItem(dragStartItem.raptorParent
										.toControl(Raptor.getInstance()
												.getDisplay()
												.getCursorLocation()));
						if (itemAtCursor != null
								&& itemAtCursor != dragStartItem) {
							int index = folder.indexOf(itemAtCursor);
							int oldIndex = folder.indexOf(dragStartItem);

							System.err.println("new index=" + index
									+ " oldIndex=" + oldIndex);

							if (oldIndex != -1 && index != -1) {
								int newIndex = oldIndex < index ? index + 1
										: index;
								dragStartItem.onMoveTo(folder, newIndex);
							}
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
		Rectangle screenBounds = getPreferences().getRectangle(
				PreferenceKeys.APP_WINDOW_BOUNDS);

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

		for (RaptorWindowSashForm sashe : sashes) {
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
}
