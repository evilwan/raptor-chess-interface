package raptor.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Partnership;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.ThreadService;
import raptor.service.BughouseService.BughouseServiceListener;

public class BugPartnersWindowItem implements RaptorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.III,
			Quadrant.IV, Quadrant.V, Quadrant.VI, Quadrant.VII };

	protected BughouseService service;
	protected Composite composite;
	protected Combo availablePartnersFilter;
	protected Table availablePartnersTable;
	protected boolean isActive = false;
	protected TableColumn lastStortedColumn;
	protected boolean wasLastSortAscending;
	protected Bugger[] currentBuggers;
	protected TableColumn ratingColumn;
	protected TableColumn nameColumn;
	protected TableColumn statusColumn;
	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive) {
				service.refreshUnpartneredBuggers();
				ThreadService
						.getInstance()
						.scheduleOneShot(
								Raptor
										.getInstance()
										.getPreferences()
										.getInt(
												PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
								this);
			}
		}
	};

	protected BughouseServiceListener listener = new BughouseServiceListener() {
		public void availablePartnershipsChanged(Partnership[] newPartnerships) {
		}

		public void gamesInProgressChanged(BugGame[] newGamesInProgress) {
		}

		public void unpartneredBuggersChanged(Bugger[] newUnpartneredBuggers) {
			synchronized (availablePartnersTable) {
				Comparator<Bugger> comparator = getComparatorForRefresh();
				currentBuggers = newUnpartneredBuggers;
				Arrays.sort(currentBuggers, comparator);
				refreshTable();
			}
		}
	};

	public BugPartnersWindowItem(BughouseService service) {
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
		return service.getConnector().getShortName() + "(Partners)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite ratingFilterComposite = new Composite(composite, SWT.NONE);
		ratingFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		ratingFilterComposite.setLayout(new RowLayout());
		CLabel label = new CLabel(ratingFilterComposite, SWT.LEFT);
		label.setText("Rating >=");
		availablePartnersFilter = new Combo(ratingFilterComposite,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		availablePartnersFilter.add("0");
		availablePartnersFilter.add("1000");
		availablePartnersFilter.add("1200");
		availablePartnersFilter.add("1400");
		availablePartnersFilter.add("1500");
		availablePartnersFilter.add("1600");
		availablePartnersFilter.add("1700");
		availablePartnersFilter.add("1800");
		availablePartnersFilter.add("1900");
		availablePartnersFilter.add("2000");
		availablePartnersFilter.add("2100");
		availablePartnersFilter.add("2200");
		availablePartnersFilter.select(Raptor.getInstance().getPreferences()
				.getInt(PreferenceKeys.BUG_ARENA_PARTNERS_INDEX));
		availablePartnersFilter.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BUG_ARENA_PARTNERS_INDEX,
						availablePartnersFilter.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true,
				true));
		tableComposite.setLayout(new FillLayout());
		availablePartnersTable = new Table(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		ratingColumn = new TableColumn(availablePartnersTable, SWT.LEFT);
		nameColumn = new TableColumn(availablePartnersTable, SWT.LEFT);
		statusColumn = new TableColumn(availablePartnersTable, SWT.LEFT);

		ratingColumn.setText("Rating");
		nameColumn.setText("Name");
		statusColumn.setText("Status");

		ratingColumn.setWidth(50);
		nameColumn.setWidth(115);
		statusColumn.setWidth(90);

		ratingColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (availablePartnersTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == ratingColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = ratingColumn;

					Arrays.sort(currentBuggers,
							wasLastSortAscending ? Bugger.BY_RATING_ASCENDING
									: Bugger.BY_RATING_DESCENDING);
					refreshTable();
				}
			}
		});

		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				synchronized (availablePartnersTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == nameColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = nameColumn;

					Arrays.sort(currentBuggers,
							wasLastSortAscending ? Bugger.BY_NAME_ASCENDING
									: Bugger.BY_NAME_DESCENDING);
					refreshTable();
				}
			}
		});

		statusColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				synchronized (availablePartnersTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == statusColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = statusColumn;

					Arrays.sort(currentBuggers,
							wasLastSortAscending ? Bugger.BY_STATUS_ASCENDING
									: Bugger.BY_STATUS_DESCENDING);
					refreshTable();
				}
			}
		});
		availablePartnersTable.setHeaderVisible(true);
		// availablePartnersTable.setSize(availablePartnersTable.computeSize(
		// SWT.DEFAULT, 250, true));

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		buttonsComposite.setLayout(new RowLayout());

		Button partnerSelected = new Button(buttonsComposite, SWT.PUSH);
		partnerSelected.setText("Partner Selected");
		partnerSelected.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				synchronized (availablePartnersTable) {
					selectedIndexes = availablePartnersTable
							.getSelectionIndices();
				}
				if (selectedIndexes == null || selectedIndexes.length == 0) {
					Raptor.getInstance().alert(
							"You must first some select buggers to partner.");
				} else {
					synchronized (availablePartnersTable) {
						for (int i = 0; i < selectedIndexes.length; i++) {
							service.getConnector().onPartner(
									availablePartnersTable.getItem(
											selectedIndexes[i]).getText(1));
						}
					}
				}
			}
		});

		Button partnerAll = new Button(buttonsComposite, SWT.PUSH);
		partnerAll.setText("Partner All");
		partnerAll.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (availablePartnersTable) {
					TableItem[] items = availablePartnersTable.getItems();
					for (TableItem item : items) {
						service.getConnector().onPartner(item.getText(1));
					}
				}
			}
		});

		if (currentBuggers != null) {
			Arrays.sort(currentBuggers, getComparatorForRefresh());
			refreshTable();
		}

		service.getUnpartneredBuggers();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.getUnpartneredBuggers();
			ThreadService
					.getInstance()
					.scheduleOneShot(
							Raptor
									.getInstance()
									.getPreferences()
									.getInt(
											PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
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

	protected Comparator<Bugger> getComparatorForRefresh() {
		Comparator<Bugger> comparator = null;
		if (lastStortedColumn == null) {
			comparator = Bugger.BY_RATING_DESCENDING;
			lastStortedColumn = ratingColumn;
			wasLastSortAscending = false;
		} else {
			if (lastStortedColumn == ratingColumn) {
				comparator = wasLastSortAscending ? Bugger.BY_RATING_ASCENDING
						: Bugger.BY_RATING_DESCENDING;
			} else if (lastStortedColumn == nameColumn) {
				comparator = wasLastSortAscending ? Bugger.BY_NAME_ASCENDING
						: Bugger.BY_NAME_DESCENDING;
			} else if (lastStortedColumn == statusColumn) {
				comparator = wasLastSortAscending ? Bugger.BY_STATUS_ASCENDING
						: Bugger.BY_STATUS_DESCENDING;
			}
		}
		return comparator;
	}

	protected boolean passesFilterCriteria(Bugger bugger) {
		int filterRating = Integer.parseInt(availablePartnersFilter.getText());
		int buggerRating = bugger.getRatingAsInt();
		return buggerRating >= filterRating;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				synchronized (availablePartnersTable) {
					int[] selectedIndexes = availablePartnersTable
							.getSelectionIndices();
					List<String> selectedNamesBeforeRefresh = new ArrayList<String>(
							selectedIndexes.length);

					for (int index : selectedIndexes) {
						String name = availablePartnersTable.getItem(index)
								.getText(1);
						selectedNamesBeforeRefresh.add(name);
					}

					TableItem[] items = availablePartnersTable.getItems();
					for (TableItem item : items) {
						item.dispose();
					}

					for (Bugger bugger : currentBuggers) {
						if (passesFilterCriteria(bugger)) {
							TableItem tableItem = new TableItem(
									availablePartnersTable, SWT.NONE);
							tableItem.setText(new String[] {
									bugger.getRating(), bugger.getName(),
									bugger.getStatus().toString() });
						}
					}

					List<Integer> indexes = new ArrayList<Integer>(
							selectedNamesBeforeRefresh.size());
					TableItem[] newItems = availablePartnersTable.getItems();
					for (int i = 0; i < newItems.length; i++) {
						if (selectedNamesBeforeRefresh.contains(newItems[i]
								.getText(1))) {
							indexes.add(i);
						}
					}
					if (indexes.size() > 0) {
						int[] indexesArray = new int[indexes.size()];
						for (int i = 0; i < indexes.size(); i++) {
							indexesArray[i] = indexes.get(i);
						}
						availablePartnersTable.select(indexesArray);
					}
				}
			}
		});
	}
}
