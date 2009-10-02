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

	protected class RaptorSashForm extends SashForm {

		protected String key;

		public RaptorSashForm(Composite parent, int style, String key) {
			super(parent, style);
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public void loadFromPreferences() {
			setWeights(getPreferences().getCurrentLayoutSashWeights(key));
		}

		public void storePreferences() {
			getPreferences().setCurrentLayoutSashWeights(key, getWeights());
		}
	}

	/**
	 * A raptor tab folder. Keeps track of the quad its in.
	 */
	protected class RaptorTabFolder extends CTabFolder {
		protected Quadrant quad;

		public RaptorTabFolder(Composite composite, int style, Quadrant quad) {
			super(composite, style);
			this.quad = quad;
		}

		public RaptorTabItem getRaptorTabItemAt(int index) {
			return (RaptorTabItem) getItem(index);
		}

		public RaptorTabItem getRaptorTabItemSelection() {
			return (RaptorTabItem) getSelection();
		}

		public void raptorMaximize() {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering raptorMaximize quad " + quad);
			}
			switch (quad) {
			case I:
			case VIII:
				quad1quad234567quad8Sash.setMaximizedControl(this);
				this.setMaximized(true);
				break;
			case II:
				quad1quad234567quad8Sash
						.setMaximizedControl(quad234567Composite);
				quad2quad34567Sash.setMaximizedControl(this);
				break;
			case III:
			case IV:
				quad1quad234567quad8Sash
						.setMaximizedControl(quad234567Composite);
				quad2quad34567Sash.setMaximizedControl(quad34567Composite);
				quad34quad567Sash.setMaximizedControl(quad34Composite);
				quad3quad4Sash.setMaximizedControl(this);
				break;
			case V:
			case VI:
				quad1quad234567quad8Sash
						.setMaximizedControl(quad234567Composite);
				quad2quad34567Sash.setMaximizedControl(quad34567Composite);
				quad34quad567Sash.setMaximizedControl(quad567Composite);
				quad56quad7Sash.setMaximizedControl(quad56Composite);
				quad5quad6Sash.setMaximizedControl(this);
				this.setMaximized(true);
				break;
			case VII:
				quad1quad234567quad8Sash
						.setMaximizedControl(quad234567Composite);
				quad2quad34567Sash.setMaximizedControl(quad34567Composite);
				quad34quad567Sash.setMaximizedControl(quad567Composite);
				quad56quad7Sash.setMaximizedControl(this);
				this.setMaximized(true);
				break;
			}
		}
	}

	/**
	 * A raptor tab item. All items added to RaptorTabFoldrs should be
	 * RaptorTabItems
	 */
	protected class RaptorTabItem extends CTabItem {
		protected RaptorWindowItem raptorItem;
		protected ItemChangedListener listener;
		protected RaptorTabFolder raptorParent;
		protected boolean disposed = false;

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
			disposed = true;
			activeItems.remove(this);
			super.dispose();
		}

		public void reParent(RaptorTabFolder newParent) {
			raptorItem.removeItemChangedListener(listener);
			raptorItem.onReparent(newParent);
			new RaptorTabItem(newParent, getStyle(), raptorItem, false);
			dispose();
			restoreFolders();
		}
	}

	Log LOG = LogFactory.getLog(RaptorWindow.class);

	protected RaptorTabFolder[] folders = new RaptorTabFolder[Quadrant.values().length];
	protected RaptorSashForm[] sashes = new RaptorSashForm[6];

	protected Composite quad234567Composite;
	protected Composite quad34567Composite;
	protected Composite quad34Composite;
	protected Composite quad567Composite;
	protected Composite quad56Composite;
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
			LOG.debug("Adding raptor window item");
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
	 * Creates just the folder,sash,and quad composite controls.
	 */
	protected void createFolderAndSashControls() {
		createQuad1Quad234567QuadControls();
		createQuad2Quad34567Controls();
		createQuad34Quadv567Controls();
		createQuad3Quad4Controls();
		createQuad56Quad7Controls();
		createQuad5Quad6Controls();

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

	protected void createQuad1Quad234567QuadControls() {
		quad1quad234567quad8Sash = new RaptorSashForm(windowComposite,
				SWT.VERTICAL | SWT.SMOOTH,
				PreferenceKeys.QUAD1_QUAD234567_QUAD8_SASH_WEIGHTS);
		quad1quad234567quad8Sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));

		folders[Quadrant.I.ordinal()] = new RaptorTabFolder(
				quad1quad234567quad8Sash, SWT.BORDER, Quadrant.I);

		quad234567Composite = new Composite(quad1quad234567quad8Sash, SWT.NONE);
		quad234567Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		folders[Quadrant.VIII.ordinal()] = new RaptorTabFolder(
				quad1quad234567quad8Sash, SWT.BORDER, Quadrant.VIII);
	}

	protected void createQuad2Quad34567Controls() {
		quad2quad34567Sash = new RaptorSashForm(quad234567Composite,
				SWT.HORIZONTAL | SWT.SMOOTH,
				PreferenceKeys.QUAD2_QUAD234567_SASH_WEIGHTS);

		folders[Quadrant.II.ordinal()] = new RaptorTabFolder(
				quad2quad34567Sash, SWT.BORDER, Quadrant.II);

		quad34567Composite = new Composite(quad2quad34567Sash, SWT.NONE);
		quad34567Composite.setLayout(SWTUtils.createMarginlessFillLayout());
	}

	protected void createQuad34Quadv567Controls() {
		quad34quad567Sash = new RaptorSashForm(quad34567Composite, SWT.VERTICAL
				| SWT.SMOOTH, PreferenceKeys.QUAD34_QUAD567_SASH_WEIGHTS);

		quad34Composite = new Composite(quad34quad567Sash, SWT.NONE);
		quad34Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		quad567Composite = new Composite(quad34quad567Sash, SWT.NONE);
		quad567Composite.setLayout(SWTUtils.createMarginlessFillLayout());
	}

	protected void createQuad3Quad4Controls() {
		quad3quad4Sash = new RaptorSashForm(quad34Composite, SWT.HORIZONTAL
				| SWT.SMOOTH, PreferenceKeys.QUAD3_QUAD4_SASH_WEIGHTS);

		folders[Quadrant.III.ordinal()] = new RaptorTabFolder(quad3quad4Sash,
				SWT.BORDER, Quadrant.III);

		folders[Quadrant.IV.ordinal()] = new RaptorTabFolder(quad3quad4Sash,
				SWT.BORDER, Quadrant.IV);
	}

	protected void createQuad56Quad7Controls() {
		quad56quad7Sash = new RaptorSashForm(quad567Composite, SWT.HORIZONTAL
				| SWT.SMOOTH, PreferenceKeys.QUAD56_QUAD7_SASH_WEIGHTS);

		quad56Composite = new Composite(quad56quad7Sash, SWT.NONE);
		quad56Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		folders[Quadrant.VII.ordinal()] = new RaptorTabFolder(quad56quad7Sash,
				SWT.BORDER, Quadrant.VII);
	}

	protected void createQuad5Quad6Controls() {
		quad5quad6Sash = new RaptorSashForm(quad56Composite, SWT.VERTICAL
				| SWT.SMOOTH, PreferenceKeys.QUAD5_QUAD6_SASH_WEIGHTS);

		folders[Quadrant.V.ordinal()] = new RaptorTabFolder(quad5quad6Sash,
				SWT.BORDER, Quadrant.V);

		folders[Quadrant.VI.ordinal()] = new RaptorTabFolder(quad5quad6Sash,
				SWT.BORDER, Quadrant.VI);
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

	public void dispose() {
		if (pingLabelsMap != null) {
			pingLabelsMap.clear();
		}
		if (activeItems != null) {
			activeItems.clear();
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
			LOG.debug("Entering restoreFolders");
		}
		long startTime = System.currentTimeMillis();
		int[] items = new int[Quadrant.values().length];
		for (int i = 0; i < items.length; i++) {
			items[i] = countItems(Quadrant.values()[i]);
		}

		// Rename them to match the quad they are in so its less confusing
		int quad1Items = items[0];
		int quad2Items = items[1];
		int quad3Items = items[2];
		int quad4Items = items[3];
		int quad5Items = items[4];
		int quad6Items = items[5];
		int quad7Items = items[6];
		int quad8Items = items[7];

		int quad34Items = quad3Items + quad4Items;
		int quad56Items = quad5Items + quad6Items;
		int quad567Items = quad56Items + quad7Items;
		int quad34567Items = quad34Items + quad567Items;
		int quad234567Items = quad2Items + quad34567Items;

		if (quad1Items > 0) {
			getTabFolder(Quadrant.I).setVisible(true);
			quad234567Composite.setVisible(quad234567Items > 0);
			getTabFolder(Quadrant.VIII).setVisible(quad8Items > 0);

			if (quad234567Items > 0 || quad8Items > 0) {
				quad1quad234567quad8Sash.setVisible(true);
				quad1quad234567quad8Sash.setMaximizedControl(null);

			} else {
				quad1quad234567quad8Sash.setVisible(true);
				quad1quad234567quad8Sash
						.setMaximizedControl(getTabFolder(Quadrant.I));
			}
			quad1quad234567quad8Sash.layout();
		} else if (quad234567Items > 0) {
			quad234567Composite.setVisible(true);
			getTabFolder(Quadrant.I).setVisible(false);
			getTabFolder(Quadrant.VIII).setVisible(quad8Items > 0);

			quad1quad234567quad8Sash.setVisible(true);
			if (quad8Items > 0) {
				quad1quad234567quad8Sash.setMaximizedControl(null);

			} else {
				quad1quad234567quad8Sash
						.setMaximizedControl(quad234567Composite);
			}
			quad1quad234567quad8Sash.layout();
		} else if (quad8Items > 0) {
			getTabFolder(Quadrant.VIII).setVisible(true);
			quad234567Composite.setVisible(false);
			getTabFolder(Quadrant.I).setVisible(false);

			quad1quad234567quad8Sash.setVisible(true);
			quad1quad234567quad8Sash
					.setMaximizedControl(getTabFolder(Quadrant.VIII));
			quad1quad234567quad8Sash.layout();
		} else {
			quad1quad234567quad8Sash.setVisible(false);
			quad1quad234567quad8Sash.setMaximizedControl(null);
			quad1quad234567quad8Sash.layout();
		}

		if (quad2Items > 0 && quad34567Items > 0) {
			quad2quad34567Sash.setVisible(true);
			quad2quad34567Sash.setVisible(true);
			quad2quad34567Sash.setMaximizedControl(null);
		} else if (quad34567Items > 0) {
			quad2quad34567Sash.setVisible(true);
			quad2quad34567Sash.setMaximizedControl(quad34567Composite);
		} else if (quad2Items > 0) {
			quad2quad34567Sash.setVisible(true);
			quad2quad34567Sash.setMaximizedControl(getTabFolder(Quadrant.II));
		} else {
			quad2quad34567Sash.setVisible(false);
			quad2quad34567Sash.setMaximizedControl(null);
		}

		if (quad34Items > 0 && quad567Items > 0) {
			quad34quad567Sash.setVisible(true);
			quad34quad567Sash.setMaximizedControl(null);
		} else if (quad34Items > 0) {
			quad34quad567Sash.setVisible(true);
			quad34quad567Sash.setMaximizedControl(quad34Composite);
		} else if (quad567Items > 0) {
			quad34quad567Sash.setVisible(true);
			quad34quad567Sash.setMaximizedControl(quad567Composite);
		} else {
			quad34quad567Sash.setVisible(false);
			quad34quad567Sash.setMaximizedControl(null);
		}

		if (quad56Items > 0 && quad7Items > 0) {
			quad56quad7Sash.setVisible(true);
			quad56quad7Sash.setMaximizedControl(null);
		} else if (quad56Items > 0) {
			quad56quad7Sash.setVisible(true);
			quad56quad7Sash.setMaximizedControl(quad56Composite);
		} else if (quad7Items > 0) {
			quad56quad7Sash.setVisible(true);
			quad56quad7Sash.setMaximizedControl(getTabFolder(Quadrant.VII));

		} else {
			quad56quad7Sash.setVisible(false);
			quad56quad7Sash.setMaximizedControl(null);
		}

		if (quad3Items > 0 && quad4Items > 0) {
			quad3quad4Sash.setVisible(true);
			quad3quad4Sash.setMaximizedControl(null);
		} else if (quad3Items > 0) {
			quad3quad4Sash.setVisible(true);
			quad3quad4Sash.setMaximizedControl(getTabFolder(Quadrant.III));
		} else if (quad4Items > 0) {
			quad3quad4Sash.setVisible(true);
			quad3quad4Sash.setMaximizedControl(getTabFolder(Quadrant.IV));
		} else {
			quad3quad4Sash.setVisible(false);
			quad3quad4Sash.setMaximizedControl(null);
		}

		if (quad5Items > 0 && quad6Items > 0) {
			quad5quad6Sash.setVisible(true);
			quad5quad6Sash.setMaximizedControl(null);
		} else if (quad5Items > 0) {
			quad5quad6Sash.setVisible(true);
			quad5quad6Sash.setMaximizedControl(getTabFolder(Quadrant.V));
		} else if (quad6Items > 0) {
			quad5quad6Sash.setVisible(true);
			quad5quad6Sash.setMaximizedControl(getTabFolder(Quadrant.VI));
		} else {
			quad5quad6Sash.setVisible(false);
			quad5quad6Sash.setMaximizedControl(null);
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
