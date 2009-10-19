package raptor.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
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

public class BugTeamsWindowItem implements RaptorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.III,
			Quadrant.IV, Quadrant.V, Quadrant.VI, Quadrant.VII };

	protected BughouseService service;
	protected Composite composite;
	protected Combo availablePartnershipsFilter;
	protected Table player1Table;
	protected Table player2Table;
	protected boolean isActive = false;
	protected Partnership[] currentPartnerships;
	protected TableColumn player1RatingColumn;
	protected TableColumn player1NameColumn;
	protected TableColumn player1StatusColumn;
	protected TableColumn player2RatingColumn;
	protected TableColumn player2NameColumn;
	protected TableColumn player2StatusColumn;

	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive) {
				service.refreshAvailablePartnerships();
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
			synchronized (player1Table) {
				currentPartnerships = newPartnerships;
				Arrays.sort(currentPartnerships);
				refreshTable();
			}
		}

		public void gamesInProgressChanged(BugGame[] newGamesInProgress) {
		}

		public void unpartneredBuggersChanged(Bugger[] newUnpartneredBuggers) {
		}
	};

	public BugTeamsWindowItem(BughouseService service) {
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
		return service.getConnector().getShortName() + "(Bug Teams)";
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
		label.setText("Team Rating >=");
		availablePartnershipsFilter = new Combo(ratingFilterComposite,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		availablePartnershipsFilter.add("0");
		availablePartnershipsFilter.add("1000");
		availablePartnershipsFilter.add("2000");
		availablePartnershipsFilter.add("2500");
		availablePartnershipsFilter.add("3000");
		availablePartnershipsFilter.add("3200");
		availablePartnershipsFilter.add("3400");
		availablePartnershipsFilter.add("3600");
		availablePartnershipsFilter.add("3800");
		availablePartnershipsFilter.add("4000");
		availablePartnershipsFilter.add("4200");
		availablePartnershipsFilter.add("4400");
		availablePartnershipsFilter.select(Raptor.getInstance()
				.getPreferences().getInt(PreferenceKeys.BUG_ARENA_TEAMS_INDEX));
		availablePartnershipsFilter
				.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						Raptor.getInstance().getPreferences()
								.setValue(
										PreferenceKeys.BUG_ARENA_TEAMS_INDEX,
										availablePartnershipsFilter
												.getSelectionIndex());
						Raptor.getInstance().getPreferences().save();
						refreshTable();
					}
				});

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		tableComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		player1Table = new Table(tableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		player1RatingColumn = new TableColumn(player1Table, SWT.LEFT);
		player1NameColumn = new TableColumn(player1Table, SWT.LEFT);
		player1StatusColumn = new TableColumn(player1Table, SWT.LEFT);

		player1RatingColumn.setText("Rating");
		player1NameColumn.setText("Name");
		player1StatusColumn.setText("Status");

		player1RatingColumn.setWidth(50);
		player1NameColumn.setWidth(80);
		player1StatusColumn.setWidth(50);
		player1Table.setHeaderVisible(true);

		player2Table = new Table(tableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		player2RatingColumn = new TableColumn(player2Table, SWT.LEFT);
		player2NameColumn = new TableColumn(player2Table, SWT.LEFT);
		player2StatusColumn = new TableColumn(player2Table, SWT.LEFT);

		player2RatingColumn.setText("Rating");
		player2NameColumn.setText("Name");
		player2StatusColumn.setText("Status");

		player2RatingColumn.setWidth(50);
		player2NameColumn.setWidth(80);
		player2StatusColumn.setWidth(60);
		player2Table.setHeaderVisible(true);

		final Button isRated = new Button(composite, SWT.CHECK);
		isRated.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
		isRated.setText("Match Rated");
		isRated.setSelection(Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED));
		isRated.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED,
						isRated.getSelection());
			}
		});

		Composite matchComposite = new Composite(composite, SWT.NONE);
		matchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		matchComposite.setLayout(new GridLayout(4, false));

		CLabel matchSelectedLabel = new CLabel(matchComposite, SWT.LEFT);
		matchSelectedLabel.setText("Match selected:");

		Button selected10Button = new Button(matchComposite, SWT.PUSH);
		selected10Button.setText("1 0");
		selected10Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 1,
								0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 1,
								0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(
							"You must select atleast 1 team member.");
				}
			}
		});

		Button selected20Button = new Button(matchComposite, SWT.PUSH);
		selected20Button.setText("2 0");
		selected20Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 2,
								0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 2,
								0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(
							"You must select atleast 1 team member.");
				}
			}
		});

		Button selected30Button = new Button(matchComposite, SWT.PUSH);
		selected30Button.setText("3 0");
		selected30Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				boolean matchedSomeone = false;
				synchronized (player1Table) {
					selectedIndexes = player1Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 3,
								0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getItem(selectedIndexes[i])
										.getText(1), isRated.getSelection(), 3,
								0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(
							"You must select atleast 1 team member.");
				}
			}
		});

		CLabel matchAllLabel = new CLabel(matchComposite, SWT.LEFT);
		matchAllLabel.setText("Match all:");

		Button all10Button = new Button(matchComposite, SWT.PUSH);
		all10Button.setText("1 0");
		all10Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					TableItem[] table1Items = player1Table.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 1, 0);
					}

					TableItem[] table2Items = player2Table.getItems();
					for (TableItem item : table2Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 1, 0);
					}
				}
			}
		});

		Button all20Button = new Button(matchComposite, SWT.PUSH);
		all20Button.setText("2 0");
		all20Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					TableItem[] table1Items = player1Table.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 2, 0);
					}

					TableItem[] table2Items = player2Table.getItems();
					for (TableItem item : table2Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 2, 0);
					}
				}
			}

		});

		Button all30Button = new Button(matchComposite, SWT.PUSH);
		all30Button.setText("3 0");
		all30Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					TableItem[] table1Items = player1Table.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 3, 0);
					}

					TableItem[] table2Items = player2Table.getItems();
					for (TableItem item : table2Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 3, 0);
					}
				}
			}
		});

		if (currentPartnerships != null) {
			Arrays.sort(currentPartnerships);
			refreshTable();
		}
		service.getAvailablePartnerships();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.getAvailablePartnerships();
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

	protected boolean passesFilterCriteria(Partnership partnership) {
		int filterRating = Integer.parseInt(availablePartnershipsFilter
				.getText());
		int buggerRating = partnership.getTeamRating();
		return buggerRating >= filterRating;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				synchronized (player1Table) {

					List<String> selectedNamesBeforeRefresh = new ArrayList<String>(
							10);

					int[] selectedIndexesTable1 = player1Table
							.getSelectionIndices();
					for (int index : selectedIndexesTable1) {
						String name = player1Table.getItem(index).getText(1);
						selectedNamesBeforeRefresh.add(name);
					}
					int[] selectedIndexesTable2 = player2Table
							.getSelectionIndices();
					for (int index : selectedIndexesTable2) {
						String name = player2Table.getItem(index).getText(1);
						selectedNamesBeforeRefresh.add(name);
					}

					TableItem[] items = player1Table.getItems();
					for (TableItem item : items) {
						item.dispose();
					}
					TableItem[] items2 = player2Table.getItems();
					for (TableItem item : items2) {
						item.dispose();
					}

					for (Partnership partnership : currentPartnerships) {
						if (passesFilterCriteria(partnership)) {
							TableItem tableItem = new TableItem(player1Table,
									SWT.NONE);
							tableItem.setText(new String[] {
									partnership.getBugger1().getRating(),
									partnership.getBugger1().getName(),
									partnership.getBugger1().getStatus()
											.toString() });

							TableItem table2Item = new TableItem(player2Table,
									SWT.NONE);
							table2Item.setText(new String[] {
									partnership.getBugger2().getRating(),
									partnership.getBugger2().getName(),
									partnership.getBugger2().getStatus()
											.toString() });
						}
					}

					List<Integer> table1Indexes = new ArrayList<Integer>(
							selectedNamesBeforeRefresh.size());
					TableItem[] newItems = player1Table.getItems();
					for (int i = 0; i < newItems.length; i++) {
						if (selectedNamesBeforeRefresh.contains(newItems[i]
								.getText(1))) {
							table1Indexes.add(i);
						}
					}
					if (table1Indexes.size() > 0) {
						int[] indexesArray = new int[table1Indexes.size()];
						for (int i = 0; i < table1Indexes.size(); i++) {
							indexesArray[i] = table1Indexes.get(i);
						}
						player1Table.select(indexesArray);
					}

					List<Integer> table2Indexes = new ArrayList<Integer>(
							selectedNamesBeforeRefresh.size());
					TableItem[] newItems2 = player2Table.getItems();
					for (int i = 0; i < newItems2.length; i++) {
						if (selectedNamesBeforeRefresh.contains(newItems2[i]
								.getText(1))) {
							table2Indexes.add(i);
						}
					}
					if (table2Indexes.size() > 0) {
						int[] indexesArray = new int[table2Indexes.size()];
						for (int i = 0; i < table2Indexes.size(); i++) {
							indexesArray[i] = table2Indexes.get(i);
						}
						player2Table.select(indexesArray);
					}
				}
			}
		});
	}
}
