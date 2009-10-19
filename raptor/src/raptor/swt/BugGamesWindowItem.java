package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.bughouse.Bugger;
import raptor.bughouse.BughouseGame;
import raptor.bughouse.Partnership;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.ThreadService;
import raptor.service.BughouseService.BughouseServiceListener;

public class BugGamesWindowItem implements RaptorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.III,
			Quadrant.IV, Quadrant.V, Quadrant.VI, Quadrant.VII };

	protected BughouseService service;
	protected Composite composite;
	protected Table bugGamesTable;
	protected boolean isActive = false;
	protected BughouseGame[] currentGames;
	protected TableColumn gameId;
	protected TableColumn whiteRating;
	protected TableColumn whiteName;
	protected TableColumn blackRating;
	protected TableColumn blackName;
	protected TableColumn timeControl;
	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive) {
				service.refreshGamesInProgress();
				ThreadService
						.getInstance()
						.scheduleOneShot(
								Raptor
										.getInstance()
										.getPreferences()
										.getInt(
												PreferenceKeys.BUG_ARENA_REFRESH_SECONDS) * 1000,
								this);
			}
		}
	};

	protected BughouseServiceListener listener = new BughouseServiceListener() {
		public void availablePartnershipsChanged(Partnership[] newPartnerships) {
		}

		public void gamesInProgressChanged(BughouseGame[] newGamesInProgress) {
			synchronized (bugGamesTable) {
				currentGames = newGamesInProgress;
				refreshTable();
			}
		}

		public void unpartneredBuggersChanged(Bugger[] newUnpartneredBuggers) {

		}
	};

	public BugGamesWindowItem(BughouseService service) {
		this.service = service;
		service.addBughouseServiceListener(listener);
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	public void afterQuadrantMove(Quadrant newQuadrant) {
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isActive = false;
		composite.dispose();
		service.removeBughouseServiceListener(listener);
	}

	public Control getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				service.getConnector().getShortName() + "-"
						+ PreferenceKeys.BUG_ARENA_QUADRANT);
	}

	public String getTitle() {
		return service.getConnector().getShortName() + "(Bug Games)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		tableComposite.setLayout(new FillLayout());
		bugGamesTable = new Table(tableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

		gameId = new TableColumn(bugGamesTable, SWT.LEFT);
		whiteRating = new TableColumn(bugGamesTable, SWT.LEFT);
		whiteName = new TableColumn(bugGamesTable, SWT.LEFT);
		blackRating = new TableColumn(bugGamesTable, SWT.LEFT);
		blackName = new TableColumn(bugGamesTable, SWT.LEFT);
		timeControl = new TableColumn(bugGamesTable, SWT.LEFT);

		gameId.setText("ID");
		whiteRating.setText("WElo");
		whiteName.setText("WName");
		blackRating.setText("BElo");
		blackName.setText("BName");
		timeControl.setText("Time");

		gameId.setWidth(40);
		whiteRating.setWidth(60);
		whiteName.setWidth(90);
		blackRating.setWidth(60);
		blackName.setWidth(90);
		timeControl.setWidth(60);

		bugGamesTable.setHeaderVisible(true);
		// availablePartnersTable.setSize(availablePartnersTable.computeSize(
		// SWT.DEFAULT, 250, true));

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		buttonsComposite.setLayout(new RowLayout());

		Button obsButton = new Button(buttonsComposite, SWT.PUSH);
		obsButton.setText("Observe");
		obsButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (bugGamesTable) {
					int selectedIndex = bugGamesTable.getSelectionIndex();
					if (selectedIndex != -1) {
						service.getConnector()
								.onObserveGame(
										bugGamesTable.getItem(selectedIndex)
												.getText(0));
					} else {
						Raptor.getInstance().alert(
								"You must selected a game to observe.");
					}
				}

			}
		});

		if (currentGames != null) {
			refreshTable();
		}

		service.refreshGamesInProgress();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshGamesInProgress();
			ThreadService.getInstance().scheduleOneShot(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.BUG_ARENA_REFRESH_SECONDS) * 1000,
					timer);
		}

	}

	public void onPassivate() {
		if (isActive) {
			isActive = false;
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				synchronized (bugGamesTable) {
					int selectedIndex = bugGamesTable.getSelectionIndex();
					String selectedGameId = null;
					if (selectedIndex != -1) {
						selectedGameId = bugGamesTable.getItem(selectedIndex)
								.getText(0);

					}

					TableItem[] items = bugGamesTable.getItems();
					for (TableItem item : items) {
						item.dispose();
					}

					for (BughouseGame game : currentGames) {
						TableItem item = new TableItem(bugGamesTable, SWT.NONE);
						item.setText(new String[] {
								game.getGame1Id(),
								game.getGame1White().getRating(),
								game.getGame1White().getName(),
								game.getGame1Black().getRating(),
								game.getGame1Black().getName(),
								game.getTimeControl() + " "
										+ (game.isRated() ? "r" : "u") });
						TableItem item2 = new TableItem(bugGamesTable, SWT.NONE);
						item2.setText(new String[] {
								game.getGame2Id(),
								game.getGame2White().getRating(),
								game.getGame2White().getName(),
								game.getGame2Black().getRating(),
								game.getGame2Black().getName(),
								game.getTimeControl() + " "
										+ (game.isRated() ? "r" : "u") });
					}

					if (selectedGameId != null) {
						TableItem[] itemsAfter = bugGamesTable.getItems();
						for (int i = 0; i < itemsAfter.length; i++) {
							if (itemsAfter[i].getText(0).equals(selectedGameId)) {
								bugGamesTable.setSelection(i);
								break;
							}
						}
					}
				}
			}
		});
	}
}
