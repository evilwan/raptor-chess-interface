package raptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.pref.PreferenceUtil;
import raptor.pref.RaptorPreferenceStore;
import raptor.service.ConnectorService;
import raptor.swt.BrowserWindowItem;
import raptor.swt.ItemChangedListener;
import raptor.swt.SWTUtils;

/**
 * A raptor window is broken up quadrants. Each quadrant is tabbed. You can add
 * a RaptorWindowItem to any quad. Each quad can be maximized and restored. When
 * all of the tabs in a quadrannt are vacant, the area disappears.
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
		}

		public int getItemsInSash() {
			int result = 0;
			for (Control control : getTabList()) {
				if (control instanceof RaptorSashForm) {
					result += ((RaptorSashForm) control).getItemsInSash();
				} else if (control instanceof RaptorTabFolder) {
					result += ((RaptorTabFolder) control).getItemCount();
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
		 * Restores the tab by setting it visibile if it or one of its children
		 * contains items. This has a cascading effect if one of its children is
		 * another RaptorSashForm.
		 */
		public void restore() {
			Control lastChildToShow = null;
			int numberOfChildrenShowing = 0;

			Control[] children = getTabList();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof RaptorTabFolder) {
					if (((RaptorTabFolder) children[i]).getItemCount() > 0) {
						RaptorTabFolder childFolder = ((RaptorTabFolder) children[i]);
						lastChildToShow = childFolder;
						childFolder.setVisible(true);
						childFolder.setMaximized(activeItems.size() == 1);
						childFolder.activate();
						numberOfChildrenShowing++;
					} else {
						children[i].setVisible(false);
					}
				}
			}

			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof RaptorSashForm) {
					if (((RaptorSashForm) children[i]).getItemsInSash() > 0) {
						RaptorSashForm childSashForm = ((RaptorSashForm) children[i]);
						lastChildToShow = childSashForm;
						lastChildToShow.setVisible(true);
						numberOfChildrenShowing++;
					} else {
						children[i].setVisible(false);
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
		}

		/**
		 * Activates the current selected item.
		 */
		public void activate() {
			if (getRaptorTabItemSelection() != null) {
				getRaptorTabItemSelection().raptorItem.onActivate();
			}
		}

		@Override
		public void dispose() {
			quad = null;
			raptorSash = null;
			super.dispose();
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
			this.setMaximized(true);
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
	}

	/**
	 * A RaptorTabItem. All items added to RaptorTabFoldrs should be
	 * RaptorTabItems RaptorTabItems can only contain RaptorWindowItem controls.
	 */
	protected class RaptorTabItem extends CTabItem {
		protected RaptorWindowItem raptorItem;
		protected ItemChangedListener listener;
		protected RaptorTabFolder raptorParent;
		protected boolean disposed = false;
		protected boolean wasReparentedWithoutCreatingControl = true;

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
						getDisplay().asyncExec(new Runnable() {
							public void run() {

								if (LOG.isDebugEnabled()) {
									LOG
											.debug("Item changed, updating text,title,showClose");
								}
								try {
									setText(item.getTitle());
									setImage(item.getImage());
									setShowClose(true);
								} catch (SWTException swt) {
									// Just eat it. It is probably a widget is
									// disposed exception
									// and i can't figure out how to avoid it.
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
			parent.layout(true, true);
			parent.setSelection(this);
			raptorItem.onActivate();
			activeItems.add(this);
		}

		@Override
		public void dispose() {
			if (!wasReparentedWithoutCreatingControl) {
				disposed = true;
				if (activeItems != null) {
					activeItems.remove(this);
				}
			}
			raptorItem = null;
			raptorParent = null;
			super.dispose();
		}

		public void reParent(RaptorTabFolder newParent) {
			raptorItem.removeItemChangedListener(listener);
			if (raptorItem.onReparent(newParent)) {
				setControl(null);
				wasReparentedWithoutCreatingControl = true;
				activeItems.remove(this);
			}
			else {
				wasReparentedWithoutCreatingControl = false;
			}
			new RaptorTabItem(newParent, getStyle(), raptorItem, false);
			dispose();
			restoreFolders();
		}

		@Override
		public String toString() {
			return "RaptorTabItem: " + getText() + " isVisible="
					+ getControl().isVisible();
		}
	}

	Log LOG = LogFactory.getLog(RaptorWindow.class);

	protected RaptorTabFolder[] folders = new RaptorTabFolder[Quadrant.values().length];
	protected RaptorSashForm[] sashes = new RaptorSashForm[6];

	protected RaptorSashForm quad1quad234567quad8Sash;
	protected RaptorSashForm quad2quad34567Sash;
	protected RaptorSashForm quad3quad4Sash;
	protected RaptorSashForm quad34quad567Sash;
	protected RaptorSashForm quad56quad7Sash;
	protected RaptorSashForm quad5quad6Sash;

	protected Composite windowComposite;
	protected Composite statusBar;
	protected Label statusLabel;
	protected Map<String, Label> pingLabelsMap = new HashMap<String, Label>();
	protected List<RaptorTabItem> activeItems = Collections
			.synchronizedList(new ArrayList<RaptorTabItem>());

	public RaptorWindow() {
		super(null);
		addMenuBar();
	}

	/**
	 * Adds a RaptorWindowItem to the RaptorWindow.
	 */
	public void addRaptorWindowItem(final RaptorWindowItem item) {
		addRaptorWindowItem(item, true);
	}

	/**
	 * Adds a RaptorWindowItem to the RaptorWindow. If isAsynch the window item
	 * is added asynchronously, otherwise it is added synchronously.
	 */
	public void addRaptorWindowItem(final RaptorWindowItem item,
			boolean isAsynch) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adding raptor window item " + item);
		}

		if (!isAsynch) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					RaptorTabFolder folder = getTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item);
					restoreFolders();
				}
			});
		} else {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					RaptorTabFolder folder = getTabFolder(item
							.getPreferredQuadrant());
					new RaptorTabItem(folder, SWT.NONE, item);
					restoreFolders();
				}
			});

		}
	}

	/**
	 * Returns the number of items in the specified quad.
	 */
	public int countItems(Quadrant... quads) {
		int count = 0;
		for (Quadrant quad : quads) {
			count += getTabFolder(quad).getItemCount();
		}
		return count;
	}

	/**
	 * Creates the controls.
	 */
	@Override
	protected Control createContents(Composite parent) {
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
						"resources/common/images/raptorIcon.gif"));

		parent.setLayout(SWTUtils.createMarginlessGridLayout(1, true));

		windowComposite = new Composite(parent, SWT.NONE);
		windowComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		windowComposite.setLayout(SWTUtils.createMarginlessGridLayout(1, true));

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

		for (int i = 0; i < folders.length; i++) {
			initQuadrantFolder(folders[i]);
		}

		sashes[0] = quad1quad234567quad8Sash;
		sashes[1] = quad2quad34567Sash;
		sashes[2] = quad34quad567Sash;
		sashes[3] = quad3quad4Sash;
		sashes[4] = quad56quad7Sash;
		sashes[5] = quad5quad6Sash;

		for (int i = 0; i < sashes.length; i++) {
			sashes[i].loadFromPreferences();
			sashes[i].setVisible(false);
			sashes[i].setMaximizedControl(null);
		}
	}

	/**
	 * Creates the menu items.
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("Raptor");
		MenuManager configureMenu = new MenuManager("&Configure");
		MenuManager helpMenu = new MenuManager("&Help");

		Connector[] connectors = ConnectorService.getInstance().getConnectors();

		for (Connector connector : connectors) {
			MenuManager manager = connector.getMenuManager();
			if (manager != null) {
				menuBar.add(manager);
			}
		}

		configureMenu.add(new Action("Preferences") {
			@Override
			public void run() {
				PreferenceUtil.launchPreferenceDialog();
			}
		});
		helpMenu.add(new Action("&About") {
			@Override
			public void run() {
				Raptor.getInstance().alert("Comming soon.");
			}
		});
		helpMenu.add(new Action("&Fics Commands Help") {
			@Override
			public void run() {
				Raptor
						.getInstance()
						.getRaptorWindow()
						.addRaptorWindowItem(
								new BrowserWindowItem(
										"Fics Commands Help",
										Raptor
												.getInstance()
												.getPreferences()
												.getString(
														PreferenceKeys.FICS_COMMANDS_HELP_URL)));
			}
		});

		menuBar.add(configureMenu);
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
				true, true));

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
		statusBar
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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

	/**
	 * Disposes all the resources that will not be cleaned up when this window
	 * is closed.
	 */
	public void dispose() {
		if (pingLabelsMap != null) {
			pingLabelsMap = null;
		}
		if (activeItems != null) {
			activeItems.clear();
			activeItems = null;
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
				synchronized (activeItems) {
					for (RaptorTabItem currentTabItem : activeItems) {
						if (currentTabItem.raptorItem == item) {
							tabItem = currentTabItem;
							break;
						}
					}
				}
				if (item != null) {
					tabItem.dispose();
					item.dispose();
					restoreFolders();
				}
			}
		});
	}

	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	/**
	 * Returns the quad the current window item is in. Null if the windowItem is
	 * not being managed.
	 */
	public Quadrant getQuadrant(RaptorWindowItem windowItem) {
		Quadrant result = null;
		synchronized (activeItems) {
			for (RaptorTabItem currentTabItem : activeItems) {
				if (currentTabItem.raptorItem == windowItem) {
					result = currentTabItem.raptorParent.quad;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Returns an array of all RaptorWindowItems currently active.
	 */
	public RaptorWindowItem[] getRaptorWindowItems() {
		return activeItems.toArray(new RaptorWindowItem[0]);
	}

	/**
	 * Returns the RaptorTabFolder at the specified 0 based index.
	 */
	public RaptorTabFolder getTabFolder(int index) {
		return folders[index];
	}

	/**
	 * Returns the RaptorTabFolder at the quad.
	 */
	public RaptorTabFolder getTabFolder(Quadrant quad) {
		return folders[quad.ordinal()];
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
	 * Initializes a quad folder. Also sets up all of the listeners on the
	 * folder.
	 * 
	 * @param folder
	 */
	protected void initQuadrantFolder(final RaptorTabFolder folder) {
		folder.setSimple(false);
		folder.setUnselectedImageVisible(false);
		folder.setUnselectedCloseVisible(false);
		folder.setMaximizeVisible(true);

		folder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				((RaptorTabItem) folder.getSelection()).raptorItem.onActivate();
			}
		});

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void close(CTabFolderEvent event) {
				RaptorTabItem item = (RaptorTabItem) folder.getSelection();
				if (item.raptorItem.confirmClose()) {
					event.doit = true;
					// item.raptorItem.dispose();
					// item.dispose();
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
				folder.raptorMaximize();
			}

			@Override
			public void minimize(CTabFolderEvent event) {
				folder.getRaptorTabItemSelection().raptorItem.onPassivate();
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
				System.err.println("Mouse double click " + e.count);
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
					for (int i = 0; i < Quadrant.values().length; i++) {
						if (i != folder.quad.ordinal()) {
							final Quadrant currentQuadrant = Quadrant.values()[i];
							MenuItem item = new MenuItem(menu, SWT.PUSH);
							item.setText("Move to quandrant "
									+ currentQuadrant.name());
							item.addListener(SWT.Selection, new Listener() {
								public void handleEvent(Event e) {
									RaptorTabItem item = folder
											.getRaptorTabItemSelection();
									if (item.raptorItem.confirmQuadrantMove()) {
										item.reParent(folders[currentQuadrant
												.ordinal()]);
									}
								}
							});
						}
					}
					menu.setLocation(folder.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!folder.getDisplay().readAndDispatch())
							folder.getDisplay().sleep();
					}
					menu.dispose();
				}
			}
		});
	}

	/**
	 * Returns true if the item is being managed by the RaptorWindow. As items
	 * are closed and disposed they are no longer managed.
	 */
	public boolean isBeingManaged(final RaptorWindowItem item) {
		boolean result = false;
		synchronized (activeItems) {
			for (RaptorTabItem currentTabItem : activeItems) {
				if (currentTabItem.raptorItem == item) {
					result = true;
					break;
				}
			}
		}
		return result;
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

		for (int i = 0; i < sashes.length; i++) {
			sashes[i].restore();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Leaving restoreFolders execution in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
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

		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				statusLabel
						.setText(StringUtils.defaultString(newStatusMessage));
			}
		});
	}
}
