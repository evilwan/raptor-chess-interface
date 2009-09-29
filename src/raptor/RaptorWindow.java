package raptor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
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
import raptor.pref.PreferencesDialog;
import raptor.swt.ProfileWIndow;
import raptor.swt.SWTUtils;

/**
 * A raptor window is broken up quadrants. Each quadrant is tabbed. You can add
 * a RaptorWindowItem to any quadrant. Each quadrant can be maximized and
 * restored. When all of the tabs in a quadrannt are vacant, the area
 * disappears.
 */
public class RaptorWindow extends ApplicationWindow {

	/**
	 * A raptor tab folder. Keeps track of the quadrant its in.
	 */
	protected class RaptorTabFolder extends CTabFolder {
		protected Quadrant quadrant;

		public RaptorTabFolder(Composite composite, int style, Quadrant quadrant) {
			super(composite, style);
			this.quadrant = quadrant;
		}

		public RaptorTabItem getRaptorTabItemAt(int index) {
			return (RaptorTabItem) getItem(index);
		}

		public RaptorTabItem getRaptorTabItemSelection() {
			return (RaptorTabItem) getSelection();
		}

		public void raptorMaximize() {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Entering raptorMaximize quadrant " + quadrant);
			}
			switch (quadrant) {
			case I:
				quadrant1quadrant2345Sash.setMaximizedControl(this);
				this.setMaximized(true);
				break;
			case II:
				quadrant2quadrant345Sash.setMaximizedControl(this);
				quadrant1quadrant2345Sash
						.setMaximizedControl(quadrant2345Composite);
				this.setMaximized(true);
				break;
			case III:
			case IV:
				quadrant3quadrant4Sash.setMaximizedControl(this);
				quadrant34quadrant5Sash
						.setMaximizedControl(quadrant34Composite);
				quadrant2quadrant345Sash
						.setMaximizedControl(quadrant345Composite);
				quadrant1quadrant2345Sash
						.setMaximizedControl(quadrant2345Composite);
				this.setMaximized(true);
				break;
			case V:
				quadrant34quadrant5Sash.setMaximizedControl(this);
				quadrant2quadrant345Sash
						.setMaximizedControl(quadrant345Composite);
				quadrant1quadrant2345Sash
						.setMaximizedControl(quadrant2345Composite);
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

		public RaptorTabItem(RaptorTabFolder parent, int style,
				RaptorWindowItem item) {
			this(parent, style, item, true);
		}

		public RaptorTabItem(RaptorTabFolder parent, int style,
				RaptorWindowItem item, boolean isInitingItem) {
			super(parent, style);

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

			setControl(item.getControl());
			setText(item.getTitle());
			setShowClose(item.isCloseable());
			parent.layout(true);
			parent.setSelection(this);

		}

		public void refresh() {
			setText(raptorItem.getTitle());
			setShowClose(raptorItem.isCloseable());
		}

		public void reParent(RaptorTabFolder newParent) {
			setControl(null);
			raptorItem.getControl().setVisible(false);
			raptorItem.onReparent(newParent);
			new RaptorTabItem(newParent, getStyle(), raptorItem, false);
			dispose();
			restoreFolders();
		}
	}

	Log LOG = LogFactory.getLog(RaptorWindow.class);

	protected RaptorTabFolder[] folders = new RaptorTabFolder[Quadrant.values().length];

	protected Composite quadrant2345Composite;
	protected Composite quadrant345Composite;
	protected Composite quadrant34Composite;
	protected SashForm quadrant1quadrant2345Sash;
	protected SashForm quadrant2quadrant345Sash;
	protected SashForm quadrant3quadrant4Sash;
	protected SashForm quadrant34quadrant5Sash;

	protected int[] quadrant1quadrant2345SashWeights = new int[] { 10, 90 };
	protected int[] quadrant2quadrant345SashWeights = new int[] { 50, 50 };
	protected int[] quadrant34quadrant5SashWeights = new int[] { 80, 20 };
	protected int[] quadrant3quadrant4SashWeights = new int[] { 50, 50 };

	protected Composite windowComposite;
	protected Composite statusBar;
	protected Label statusLabel;
	protected Map<String, Label> pingLabelsMap = new HashMap<String, Label>();

	public RaptorWindow() {
		super(null);
		addMenuBar();
	}

	/**
	 * Adds a RaptorWindowItem to the RaptorWindow.
	 */
	public void addRaptorWindowItem(final RaptorWindowItem item) {
		addRaptorWindowItem(item, false);
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

		if (isAsynch) {
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
	 * Returns the number of items in the specified quadrant.
	 */
	public int countItems(Quadrant... quadrants) {
		int count = 0;
		for (Quadrant quadrant : quadrants) {
			count += getTabFolder(quadrant).getItemCount();
		}
		return count;
	}

	/**
	 * Creates the controls.
	 */
	@Override
	protected Control createContents(Composite parent) {
		getShell().setText(
				Raptor.getInstance().getPreferences().getString(
						PreferenceKeys.APP_NAME));
		getShell().setImage(
				Raptor.getInstance().getImage(
						"resources/common/images/raptorIcon.gif"));

		parent.setLayout(SWTUtils.createMarginlessFillLayout());

		windowComposite = new Composite(parent, SWT.NONE);
		windowComposite.setLayout(SWTUtils.createMarginlessGridLayout(1, true));

		createFolderAndSashControls();
		createStatusBarControls();
		return windowComposite;
	}

	/**
	 * Creates just the folder,sash,and quadrant composite controls.
	 */
	protected void createFolderAndSashControls() {
		quadrant1quadrant2345Sash = new SashForm(windowComposite,
				SWT.HORIZONTAL | SWT.SMOOTH);
		quadrant1quadrant2345Sash.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, true));

		folders[0] = new RaptorTabFolder(quadrant1quadrant2345Sash, SWT.BORDER,
				Quadrant.values()[0]);

		quadrant2345Composite = new Composite(quadrant1quadrant2345Sash,
				SWT.NONE);
		quadrant2345Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		quadrant2quadrant345Sash = new SashForm(quadrant2345Composite,
				SWT.VERTICAL | SWT.SMOOTH);
		folders[1] = new RaptorTabFolder(quadrant2quadrant345Sash, SWT.BORDER,
				Quadrant.values()[1]);

		quadrant345Composite = new Composite(quadrant2quadrant345Sash, SWT.NONE);
		quadrant345Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		quadrant34quadrant5Sash = new SashForm(quadrant345Composite,
				SWT.HORIZONTAL | SWT.SMOOTH);

		quadrant34Composite = new Composite(quadrant34quadrant5Sash, SWT.NONE);
		quadrant34Composite.setLayout(SWTUtils.createMarginlessFillLayout());

		quadrant3quadrant4Sash = new SashForm(quadrant34Composite, SWT.VERTICAL
				| SWT.SMOOTH);

		folders[2] = new RaptorTabFolder(quadrant3quadrant4Sash, SWT.BORDER,
				Quadrant.values()[2]);
		folders[3] = new RaptorTabFolder(quadrant3quadrant4Sash, SWT.BORDER,
				Quadrant.values()[3]);

		folders[4] = new RaptorTabFolder(quadrant34quadrant5Sash, SWT.BORDER,
				Quadrant.values()[4]);

		quadrant34quadrant5Sash.setWeights(quadrant34quadrant5SashWeights);
		quadrant3quadrant4Sash.setWeights(quadrant3quadrant4SashWeights);
		quadrant1quadrant2345Sash.setWeights(quadrant2quadrant345SashWeights);
		quadrant2quadrant345Sash.setWeights(quadrant2quadrant345SashWeights);

		for (int i = 0; i < folders.length; i++) {
			initQuadrantFolder(folders[i]);
		}

		quadrant1quadrant2345Sash.setVisible(false);
		quadrant2quadrant345Sash.setVisible(false);
		quadrant3quadrant4Sash.setVisible(false);
		quadrant34quadrant5Sash.setVisible(false);
	}

	/**
	 * Creates the menu items.
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuBar = new MenuManager("Raptor");
		MenuManager connectionsMenu = new MenuManager("&Connections");
		MenuManager configureMenu = new MenuManager("&Configure");
		MenuManager windowMenu = new MenuManager("&Window");
		MenuManager helpMenu = new MenuManager("&Help");

		connectionsMenu.add(new Action("Connect to &fics") {
			@Override
			public void run() {
			}
		});
		connectionsMenu.add(new Action("Profile") {
			@Override
			public void run() {
				ProfileWIndow profiler = new ProfileWIndow();
				profiler.setBlockOnOpen(false);
				profiler.open();
			}
		});
		configureMenu.add(new Action("Preferences") {
			@Override
			public void run() {
				new PreferencesDialog().run();
			}
		});
		helpMenu.add(new Action("&About") {
			@Override
			public void run() {
			}
		});
		windowMenu.add(new Action("&Cascade") {
			@Override
			public void run() {
			}
		});

		menuBar.add(connectionsMenu);
		menuBar.add(configureMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		return menuBar;
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
	 * Returns the RaptorTabFolder at the specified 0 based index.
	 */
	public RaptorTabFolder getTabFolder(int index) {
		return folders[index];
	}

	/**
	 * Returns the RaptorTabFolder at the quadrant.
	 */
	public RaptorTabFolder getTabFolder(Quadrant quadrant) {
		return folders[quadrant.ordinal()];
	}

	/**
	 * Initialzes the windows bounds.
	 */
	@Override
	protected void initializeBounds() {
		Rectangle fullScreenBounds = Display.getCurrent().getPrimaryMonitor()
				.getBounds();
		getShell().setSize(fullScreenBounds.width, fullScreenBounds.height);
		getShell().setLocation(0, 0);
	}

	/**
	 * Initializes a quadrant folder. Also sets up all of the listeners on the
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
				if (item.raptorItem.isCloseable()) {
					event.doit = true;
					item.dispose();
				}
				restoreFolders();
			}

			@Override
			public void maximize(CTabFolderEvent event) {
				folder.raptorMaximize();

			}

			@Override
			public void restore(CTabFolderEvent event) {
				restoreFolders();
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
					for (int i = 0; i < 5; i++) {
						if (i != folder.quadrant.ordinal()) {
							final Quadrant currentQuadrant = Quadrant.values()[i];
							MenuItem item = new MenuItem(menu, SWT.PUSH);
							item.setText("Move to quandrant "
									+ currentQuadrant.name());
							item.addListener(SWT.Selection, new Listener() {
								public void handleEvent(Event e) {
									RaptorTabItem item = folder
											.getRaptorTabItemSelection();
									if (item.raptorItem.confirmReparenting()) {
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

		int quad1Items = items[0];
		int quad2Items = items[1];
		int quad2345Items = items[1] + items[2] + items[3] + items[4];
		int quad345Items = items[2] + items[3] + items[4];
		int quad34Items = items[2] + items[3];
		int quad5Items = items[4];
		int quad4Items = items[3];
		int quad3Items = items[2];

		if (quad1Items == 0) {
			if (quad2345Items > 0) {
				quadrant1quadrant2345Sash.setVisible(true);
				quadrant1quadrant2345Sash
						.setMaximizedControl(quadrant2345Composite);

			} else {
				quadrant1quadrant2345Sash.setVisible(false);
				quadrant1quadrant2345Sash.setMaximizedControl(null);
			}
		} else if (quad1Items > 0) {
			quadrant1quadrant2345Sash.setVisible(true);
			if (quad2345Items > 0) {
				quadrant1quadrant2345Sash.setMaximizedControl(null);

			} else {
				quadrant1quadrant2345Sash
						.setMaximizedControl(getTabFolder(Quadrant.I));
			}

		}

		if (quad2345Items > 0) {
			quadrant2quadrant345Sash.setVisible(true);
			if (quad2Items > 0) {
				quadrant2quadrant345Sash
						.setMaximizedControl(quad345Items > 0 ? null
								: getTabFolder(Quadrant.II));
			} else {
				quadrant2quadrant345Sash
						.setMaximizedControl(quadrant345Composite);
			}
		}

		if (quad345Items > 0) {
			quadrant34quadrant5Sash.setVisible(true);
			if (quad34Items > 0) {
				quadrant34quadrant5Sash
						.setMaximizedControl(quad5Items > 0 ? null
								: quadrant34Composite);
			} else {
				quadrant34quadrant5Sash
						.setMaximizedControl(getTabFolder(Quadrant.V));
			}
		}

		if (quad34Items > 0) {
			quadrant3quadrant4Sash.setVisible(true);
			if (quad3Items > 0) {
				quadrant3quadrant4Sash
						.setMaximizedControl(quad4Items > 0 ? null
								: getTabFolder(Quadrant.III));
			} else {
				quadrant3quadrant4Sash
						.setMaximizedControl(getTabFolder(Quadrant.IV));
			}
		}

		for (int i = 0; i < items.length; i++) {
			RaptorTabFolder folder = getTabFolder(i);
			folder.setMaximized(false);
			folder.setMaximized(false);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Leaving restoreFolders execution in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	/**
	 * Sets the ping time on the window for the specified connector.
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

					label = new Label(statusBar, SWT.NONE);
					GridData gridData = new GridData();
					gridData.grabExcessHorizontalSpace = false;
					gridData.grabExcessVerticalSpace = false;
					gridData.horizontalAlignment = SWT.END;
					gridData.verticalAlignment = SWT.CENTER;
					label.setLayoutData(gridData);
					pingLabelsMap.put(connector.getShortName(), label);
					label.setFont(Raptor.getInstance().getPreferences()
							.getFont(PreferenceKeys.APP_LAG_FONT));
					label.setForeground(Raptor.getInstance().getPreferences()
							.getColor(PreferenceKeys.APP_LAG_COLOR));
				}
				label.setText(connector.getShortName() + " ping " + pingTime
						+ "ms");
				statusBar.layout(true, true);
				label.redraw();
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
