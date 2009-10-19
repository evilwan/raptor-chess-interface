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
import raptor.chat.Seek;
import raptor.pref.PreferenceKeys;
import raptor.service.SeekService;
import raptor.service.ThreadService;
import raptor.service.SeekService.SeekServiceListener;

public class SeekTableWindowItem implements RaptorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.III,
			Quadrant.IV, Quadrant.V, Quadrant.VI, Quadrant.VII };

	protected SeekService service;
	protected Composite composite;
	protected Combo ratingsFilter;
	protected Combo ratedFilter;
	protected Button isShowingComputers;
	protected Button isShowingLightning;
	protected Button isShowingBlitz;
	protected Button isShowingStandard;
	protected Button isShowingCrazyhouse;
	protected Button isShowingFR;
	protected Button isShowingWild;
	protected Button isShowingAtomic;
	protected Button isShowingSuicide;
	protected Button isShowingLosers;
	protected Button isShowingUntimed;
	protected Table seeksTable;
	protected boolean isActive = false;
	protected TableColumn lastStortedColumn;
	protected boolean wasLastSortAscending;
	protected Seek[] currentSeeks;
	protected TableColumn adColumn;
	protected TableColumn ratingColumn;
	protected TableColumn typeDescriptionColumn;
	protected TableColumn nameColumn;
	protected TableColumn timeControlColumn;
	protected TableColumn ratingRangeColumn;
	protected TableColumn flagsColumn;
	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive) {
				service.refreshSeeks();
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

	protected SeekServiceListener listener = new SeekServiceListener() {
		public void seeksChanged(Seek[] seeks) {
			synchronized (seeksTable) {
				Comparator<Seek> comparator = getComparatorForRefresh();
				currentSeeks = seeks;
				Arrays.sort(currentSeeks, comparator);
				refreshTable();
			}
		}
	};

	public SeekTableWindowItem(SeekService service) {
		this.service = service;
		service.adSeekServiceListener(listener);
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
		service.removeSeekServiceLisetner(listener);
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
						+ PreferenceKeys.SEEK_GRAPH_QUADRANT);
	}

	public String getTitle() {
		return service.getConnector().getShortName() + "(Seeks)";
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
		label.setText("Rating >= ");
		ratingsFilter = new Combo(ratingFilterComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		ratingsFilter.add("0");
		ratingsFilter.add("700");
		ratingsFilter.add("1000");
		ratingsFilter.add("1100");
		ratingsFilter.add("1200");
		ratingsFilter.add("1300");
		ratingsFilter.add("1400");
		ratingsFilter.add("1500");
		ratingsFilter.add("1600");
		ratingsFilter.add("1700");
		ratingsFilter.add("1800");
		ratingsFilter.add("1900");
		ratingsFilter.add("2000");
		ratingsFilter.add("2100");
		ratingsFilter.add("2200");
		ratingsFilter.add("2300");
		ratingsFilter.select(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.SEEK_TABLE_RATINGS_INDEX));
		ratingsFilter.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_RATINGS_INDEX,
						ratingsFilter.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});
		label = new CLabel(ratingFilterComposite, SWT.LEFT);
		label.setText("Rated");
		ratedFilter = new Combo(ratingFilterComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		ratedFilter.add("Either");
		ratedFilter.add("Rated");
		ratedFilter.add("Unrated");
		ratedFilter.select(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.SEEK_TABLE_RATED_INDEX));
		ratedFilter.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_RATED_INDEX,
						ratedFilter.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});
		isShowingComputers = new Button(ratingFilterComposite, SWT.CHECK);
		isShowingComputers.setText("Show Computers");
		isShowingComputers.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_COMPUTERS));
		isShowingComputers.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_COMPUTERS,
						isShowingComputers.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		Composite typeFilterComposite = new Composite(composite, SWT.NONE);
		typeFilterComposite.setLayout(new GridLayout(3, false));
		isShowingLightning = new Button(typeFilterComposite, SWT.CHECK);
		isShowingLightning.setText("Show Lightning");
		isShowingLightning.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_LIGHTNING));
		isShowingLightning.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_LIGHTNING,
						isShowingLightning.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingBlitz = new Button(typeFilterComposite, SWT.CHECK);
		isShowingBlitz.setText("Show Blitz");
		isShowingBlitz.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_BLITZ));
		isShowingBlitz.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_BLITZ,
						isShowingBlitz.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingStandard = new Button(typeFilterComposite, SWT.CHECK);
		isShowingStandard.setText("Show Standard");
		isShowingStandard.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_STANDARD));
		isShowingStandard.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_STANDARD,
						isShowingStandard.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingAtomic = new Button(typeFilterComposite, SWT.CHECK);
		isShowingAtomic.setText("Show Atomic");
		isShowingAtomic.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_ATOMIC));
		isShowingAtomic.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_ATOMIC,
						isShowingAtomic.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingSuicide = new Button(typeFilterComposite, SWT.CHECK);
		isShowingSuicide.setText("Show Suicide");
		isShowingSuicide.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_SUICIDE));
		isShowingSuicide.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_SUICIDE,
						isShowingSuicide.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingLosers = new Button(typeFilterComposite, SWT.CHECK);
		isShowingLosers.setText("Show Losers");
		isShowingLosers.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_LOSERS));
		isShowingLosers.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_LOSERS,
						isShowingLosers.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingCrazyhouse = new Button(typeFilterComposite, SWT.CHECK);
		isShowingCrazyhouse.setText("Show Crazyhouse");
		isShowingCrazyhouse.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_CRAZYHOUSE));
		isShowingCrazyhouse.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_CRAZYHOUSE,
						isShowingCrazyhouse.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingFR = new Button(typeFilterComposite, SWT.CHECK);
		isShowingFR.setText("Show Fischer Random");
		isShowingFR.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_FR));
		isShowingFR.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_FR,
						isShowingFR.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		isShowingWild = new Button(typeFilterComposite, SWT.CHECK);
		isShowingWild.setText("Show Wild");
		isShowingWild.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_WILD));
		isShowingWild.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_WILD,
						isShowingWild.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});
		isShowingUntimed = new Button(typeFilterComposite, SWT.CHECK);
		isShowingUntimed.setText("Show Untimed");
		isShowingUntimed.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.SEEK_TABLE_SHOW_UNTIMED));
		isShowingUntimed.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_SHOW_UNTIMED,
						isShowingUntimed.getSelection());
				Raptor.getInstance().getPreferences().save();
				refreshTable();
			}
		});

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true,
				true));
		tableComposite.setLayout(new FillLayout());
		seeksTable = new Table(tableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);

		adColumn = new TableColumn(seeksTable, SWT.LEFT);
		ratingColumn = new TableColumn(seeksTable, SWT.LEFT);
		timeControlColumn = new TableColumn(seeksTable, SWT.LEFT);
		typeDescriptionColumn = new TableColumn(seeksTable, SWT.LEFT);
		nameColumn = new TableColumn(seeksTable, SWT.LEFT);
		ratingRangeColumn = new TableColumn(seeksTable, SWT.LEFT);
		flagsColumn = new TableColumn(seeksTable, SWT.LEFT);

		adColumn.setText("Ad");
		ratingColumn.setText("Elo");
		timeControlColumn.setText("Time");
		typeDescriptionColumn.setText("Type");
		nameColumn.setText("Name");
		ratingRangeColumn.setText("Rating Range");
		flagsColumn.setText("Flags");

		adColumn.setWidth(40);
		ratingColumn.setWidth(50);
		timeControlColumn.setWidth(50);
		typeDescriptionColumn.setWidth(60);
		nameColumn.setWidth(75);
		ratingRangeColumn.setWidth(78);
		flagsColumn.setWidth(40);

		adColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == adColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = adColumn;

					Arrays.sort(currentSeeks,
							wasLastSortAscending ? Seek.AD_ASCENDING_COMPARATOR
									: Seek.AD_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});

		ratingColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == ratingColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = ratingColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.RATING_ASCENDING_COMPARATOR
											: Seek.RATING_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});

		timeControlColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == timeControlColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = timeControlColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.TIME_CONTROL_ASCENDING_COMPARATOR
											: Seek.TIME_CONTROL_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});

		typeDescriptionColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == typeDescriptionColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = typeDescriptionColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.TYPE_DESCRIPTION_ASCENDING_COMPARATOR
											: Seek.TYPE_DESCRIPTION_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});

		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == nameColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = nameColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.NAME_ASCENDING_COMPARATOR
											: Seek.NAME_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});

		ratingRangeColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == ratingRangeColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = ratingRangeColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.RATING_RANGE_ASCENDING_COMPARATOR
											: Seek.RATING_RANGE_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});
		flagsColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				synchronized (seeksTable) {
					wasLastSortAscending = lastStortedColumn == null ? true
							: lastStortedColumn == flagsColumn ? !wasLastSortAscending
									: true;
					lastStortedColumn = flagsColumn;

					Arrays
							.sort(
									currentSeeks,
									wasLastSortAscending ? Seek.FLAGS_ASCENDING_COMPARATOR
											: Seek.FLAGS_DESCENDING_COMPARATOR);
					refreshTable();
				}
			}
		});
		seeksTable.setHeaderVisible(true);

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		buttonsComposite.setLayout(new RowLayout());

		Button acceptButton = new Button(buttonsComposite, SWT.PUSH);
		acceptButton.setText("Accept");
		acceptButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				int[] selectedIndexes = null;
				synchronized (seeksTable) {
					selectedIndexes = seeksTable.getSelectionIndices();
				}
				if (selectedIndexes == null || selectedIndexes.length == 0) {
					Raptor.getInstance().alert(
							"You must first select 1 or more seeks to accept.");
				} else {
					synchronized (seeksTable) {
						for (int i = 0; i < selectedIndexes.length; i++) {
							service.getConnector().acceptSeek(
									seeksTable.getItem(selectedIndexes[i])
											.getText(0));
						}
					}
				}
			}
		});

		if (currentSeeks != null) {
			Arrays.sort(currentSeeks, getComparatorForRefresh());
			refreshTable();
		}

		service.refreshSeeks();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshSeeks();
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

	protected Comparator<Seek> getComparatorForRefresh() {
		Comparator<Seek> comparator = null;
		if (lastStortedColumn == null) {
			comparator = Seek.RATING_DESCENDING_COMPARATOR;
			lastStortedColumn = ratingColumn;
			wasLastSortAscending = false;
		} else {
			if (lastStortedColumn == ratingColumn) {
				comparator = wasLastSortAscending ? Seek.RATING_ASCENDING_COMPARATOR
						: Seek.RATING_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == nameColumn) {
				comparator = wasLastSortAscending ? Seek.NAME_ASCENDING_COMPARATOR
						: Seek.NAME_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == adColumn) {
				comparator = wasLastSortAscending ? Seek.AD_ASCENDING_COMPARATOR
						: Seek.AD_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == flagsColumn) {
				comparator = wasLastSortAscending ? Seek.FLAGS_ASCENDING_COMPARATOR
						: Seek.FLAGS_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == ratingRangeColumn) {
				comparator = wasLastSortAscending ? Seek.RATING_RANGE_ASCENDING_COMPARATOR
						: Seek.RATING_RANGE_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == timeControlColumn) {
				comparator = wasLastSortAscending ? Seek.TIME_CONTROL_ASCENDING_COMPARATOR
						: Seek.TIME_CONTROL_DESCENDING_COMPARATOR;
			} else if (lastStortedColumn == typeDescriptionColumn) {
				comparator = wasLastSortAscending ? Seek.TYPE_DESCRIPTION_ASCENDING_COMPARATOR
						: Seek.TYPE_DESCRIPTION_DESCENDING_COMPARATOR;
			}
		}
		return comparator;
	}

	protected boolean passesFilterCriteria(Seek seek) {
		boolean result = true;
		int filterRating = Integer.parseInt(ratingsFilter.getText());
		int seekRating = seek.getRatingAsInt();
		if (seekRating >= filterRating) {
			if (ratedFilter.getSelectionIndex() == 1) {
				result = seek.isRated();
			} else if (ratedFilter.getSelectionIndex() == 2) {
				result = !seek.isRated();
			}
			if (result) {
				if (!isShowingComputers.getSelection()) {
					result = !seek.isComputer();
				}
				if (result) {
					switch (seek.getType()) {
					case standard:
						result = isShowingStandard.getSelection();
						break;
					case blitz:
						result = isShowingBlitz.getSelection();
						break;
					case lightning:
						result = isShowingLightning.getSelection();
						break;
					case atomic:
						result = isShowingAtomic.getSelection();
						break;
					case suicide:
						result = isShowingSuicide.getSelection();
						break;
					case losers:
						result = isShowingLosers.getSelection();
						break;
					case fischerRandom:
						result = isShowingFR.getSelection();
						break;
					case wild:
						result = isShowingWild.getSelection();
						break;
					case crazyhouse:
						result = isShowingCrazyhouse.getSelection();
						break;
					case untimed:
						result = isShowingUntimed.getSelection();
						break;
					}
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new Runnable() {
			public void run() {
				synchronized (seeksTable) {
					int[] selectedIndexes = seeksTable.getSelectionIndices();
					List<String> selectedAdsBeforeRefresh = new ArrayList<String>(
							selectedIndexes.length);

					for (int index : selectedIndexes) {
						String name = seeksTable.getItem(index).getText(0);
						selectedAdsBeforeRefresh.add(name);
					}

					TableItem[] items = seeksTable.getItems();
					for (TableItem item : items) {
						item.dispose();
					}

					for (Seek seek : currentSeeks) {
						if (passesFilterCriteria(seek)) {
							TableItem tableItem = new TableItem(seeksTable,
									SWT.NONE);
							tableItem.setText(new String[] { seek.getAd(),
									seek.getRating(), seek.getTimeControl(),
									seek.getTypeDescription(), seek.getName(),
									seek.getRatingRange(), seek.getFlags() });
						}
					}

					List<Integer> indexes = new ArrayList<Integer>(
							selectedAdsBeforeRefresh.size());
					TableItem[] newItems = seeksTable.getItems();
					for (int i = 0; i < newItems.length; i++) {
						if (selectedAdsBeforeRefresh.contains(newItems[i]
								.getText(0))) {
							indexes.add(i);
						}
					}
					if (indexes.size() > 0) {
						int[] indexesArray = new int[indexes.size()];
						for (int i = 0; i < indexes.size(); i++) {
							indexesArray[i] = indexes.get(i);
						}
						seeksTable.select(indexesArray);
					}
				}
			}
		});
	}
}
