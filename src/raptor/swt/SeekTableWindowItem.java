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
package raptor.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.chat.Seek;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SeekService;
import raptor.service.ThreadService;
import raptor.service.SeekService.SeekServiceListener;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.PersonController;
import raptor.util.IntegerComparator;
import raptor.util.RaptorRunnable;
import raptor.util.RatingComparator;

public class SeekTableWindowItem implements RaptorConnectorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };

	public static final String[] getRatings() {
		return new String[] { "0", "1", "700", "1000", "1100", "1200", "1300",
				"1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100",
				"2200", "2300", "2400", "2500", "2600", "2700", "2800", "3000",
				"9999" };
	}

	protected SeekService service;
	protected Composite composite;
	protected Combo minRatingsFilter;
	protected Combo maxRatingsFilter;
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
	protected RaptorTable seeksTable;
	protected SeekGraph seekGraph;
	protected Composite settings;
	protected boolean isActive = false;

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
			refreshSeekView();
		}
	};

	public SeekTableWindowItem(SeekService service) {
		this.service = service;
		service.adSeekServiceListener(listener);
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				service.getConnector().getShortName() + "-"
						+ PreferenceKeys.SEEK_TABLE_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isActive = false;
		composite.dispose();
		service.removeSeekServiceLisetner(listener);
	}

	public Connector getConnector() {
		return service.getConnector();
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
						+ PreferenceKeys.SEEK_TABLE_QUADRANT);
	}

	public String getTitle() {
		return service.getConnector().getShortName() + "(Seeks)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	protected void buildSettingsComposite(Composite parent) {
		Composite ratingFilterComposite = new Composite(parent, SWT.NONE);
		ratingFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		ratingFilterComposite.setLayout(new RowLayout());
		minRatingsFilter = new Combo(ratingFilterComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);

		for (String rating : getRatings()) {
			minRatingsFilter.add(rating);
		}

		minRatingsFilter.select(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.SEEK_TABLE_RATINGS_INDEX));
		minRatingsFilter.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_RATINGS_INDEX,
						minRatingsFilter.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshSeekView();
			}
		});

		RaptorLabel label = new RaptorLabel(ratingFilterComposite, SWT.LEFT);
		label.setText(">= Rating <=");
		maxRatingsFilter = new Combo(ratingFilterComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		for (String rating : getRatings()) {
			maxRatingsFilter.add(rating);
		}
		maxRatingsFilter.select(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.SEEK_TABLE_MAX_RATINGS_INDEX));
		maxRatingsFilter.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.SEEK_TABLE_MAX_RATINGS_INDEX,
						maxRatingsFilter.getSelectionIndex());
				Raptor.getInstance().getPreferences().save();
				refreshSeekView();
			}
		});

		Composite ratedComposite = new Composite(parent, SWT.NONE);
		ratedComposite.setLayout(new RowLayout());
		ratedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		label = new RaptorLabel(ratedComposite, SWT.LEFT);
		label.setText("Rated:");
		ratedFilter = new Combo(ratedComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		ratedFilter.add("Rated and Unrated");
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
				refreshSeekView();
			}
		});

		Composite typeFilterComposite = new Composite(parent, SWT.NONE);
		typeFilterComposite.setLayout(new GridLayout(3, false));

		isShowingComputers = new Button(typeFilterComposite, SWT.CHECK);
		isShowingComputers.setText("Computers");
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
				refreshSeekView();
			}
		});

		isShowingLightning = new Button(typeFilterComposite, SWT.CHECK);
		isShowingLightning.setText("Lightning");
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
				refreshSeekView();
			}
		});

		isShowingBlitz = new Button(typeFilterComposite, SWT.CHECK);
		isShowingBlitz.setText("Blitz");
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
				refreshSeekView();
			}
		});

		isShowingStandard = new Button(typeFilterComposite, SWT.CHECK);
		isShowingStandard.setText("Standard");
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
				refreshSeekView();
			}
		});

		isShowingAtomic = new Button(typeFilterComposite, SWT.CHECK);
		isShowingAtomic.setText("Atomic");
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
				refreshSeekView();
			}
		});

		isShowingSuicide = new Button(typeFilterComposite, SWT.CHECK);
		isShowingSuicide.setText("Suicide");
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
				refreshSeekView();
			}
		});

		isShowingLosers = new Button(typeFilterComposite, SWT.CHECK);
		isShowingLosers.setText("Losers");
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
				refreshSeekView();
			}
		});

		isShowingCrazyhouse = new Button(typeFilterComposite, SWT.CHECK);
		isShowingCrazyhouse.setText("Crazyhouse");
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
				refreshSeekView();
			}
		});

		isShowingFR = new Button(typeFilterComposite, SWT.CHECK);
		isShowingFR.setText("Fischer Random");
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
				refreshSeekView();
			}
		});

		isShowingWild = new Button(typeFilterComposite, SWT.CHECK);
		isShowingWild.setText("Wild");
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
				refreshSeekView();
			}
		});
		isShowingUntimed = new Button(typeFilterComposite, SWT.CHECK);
		isShowingUntimed.setText("Untimed");
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
				refreshSeekView();
			}
		});

	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		final TabFolder tabFolder = new TabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TabItem controlsTab = new TabItem(tabFolder, SWT.NONE);
		controlsTab.setText("Settings");
		settings = new Composite(tabFolder, SWT.NONE);
		settings.setLayout(new GridLayout(1, false));
		buildSettingsComposite(settings);
		controlsTab.setControl(settings);

		TabItem tableTab = new TabItem(tabFolder, SWT.NULL);
		tableTab.setText("Seek Table");

		Composite tableComposite = new Composite(tabFolder, SWT.NONE);
		tableComposite.setLayout(new GridLayout(1, false));
		tableTab.setControl(tableComposite);

		seeksTable = new RaptorTable(tableComposite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		seeksTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		seeksTable.addColumn("Ad", SWT.LEFT, 8, true, new IntegerComparator());
		seeksTable.addColumn("Elo", SWT.LEFT, 11, true, new RatingComparator());
		seeksTable.addColumn("Time", SWT.LEFT, 11, true, null);
		seeksTable.addColumn("Type", SWT.LEFT, 14, true, null);
		seeksTable.addColumn("Name", SWT.LEFT, 27, true, null);
		seeksTable.addColumn("Rating Range", SWT.LEFT, 19, true, null);
		seeksTable.addColumn("Flags", SWT.LEFT, 10, true, null);

		// Sort twice so when data is refreshed it will be on elo descending.
		seeksTable.sort(1);
		seeksTable.sort(1);

		seekGraph = new SeekGraph(tabFolder, service);
		TabItem graphTab = new TabItem(tabFolder, SWT.NULL);
		graphTab.setText("Seek Graph");
		graphTab.setControl(seekGraph);

		seeksTable.addRaptorTableListener(new RaptorTableAdapter() {

			@Override
			public void rowDoubleClicked(MouseEvent event, String[] rowData) {
				service.getConnector().acceptSeek(rowData[0]);
			}

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, rowData[4]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(seeksTable.getTable().toDisplay(event.x,
							event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!composite.getDisplay().readAndDispatch()) {
							composite.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}
		});

		tabFolder.setSelection(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.SEEK_TABLE_SELECTED_TAB));
		
		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(PreferenceKeys.SEEK_TABLE_SELECTED_TAB, tabFolder.getSelectionIndex());	
			}
		});
		
		service.refreshSeeks();
		refreshSeekView();
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

	protected void addPersonMenuItems(Menu menu, String word) {
		if (getConnector().isLikelyPerson(word)) {
			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
			final String person = getConnector().parsePerson(word);
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText("Add a tab for person: " + person);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					if (!Raptor.getInstance().getWindow()
							.containsPersonalTellItem(getConnector(), person)) {
						ChatConsoleWindowItem windowItem = new ChatConsoleWindowItem(
								new PersonController(getConnector(), person));
						Raptor.getInstance().getWindow().addRaptorWindowItem(
								windowItem, false);
						ChatUtils.appendPreviousChatsToController(windowItem
								.getConsole());
					}
				}
			});

			final String[][] connectorPersonItems = getConnector()
					.getPersonActions(person);
			if (connectorPersonItems != null) {
				for (int i = 0; i < connectorPersonItems.length; i++) {
					if (connectorPersonItems[i][0].equals("separator")) {
						new MenuItem(menu, SWT.SEPARATOR);
					} else {
						item = new MenuItem(menu, SWT.PUSH);
						item.setText(connectorPersonItems[i][0]);
						final int index = i;
						item.addListener(SWT.Selection, new Listener() {
							public void handleEvent(Event e) {
								getConnector().sendMessage(
										connectorPersonItems[index][1]);
							}
						});
					}
				}
			}
		}
	}

	protected Seek[] getFilteredSeeks() {
		Seek[] currentSeeks = service.getSeeks();
		if (currentSeeks == null) {
			currentSeeks = new Seek[0];
		}
		List<Seek> result = new ArrayList<Seek>(currentSeeks.length);
		for (Seek seek : currentSeeks) {
			if (passesFilterCriteria(seek)) {
				result.add(seek);
			}
		}
		return result.toArray(new Seek[0]);
	}

	protected boolean passesFilterCriteria(Seek seek) {
		boolean result = true;
		int minFilterRating = Integer.parseInt(minRatingsFilter.getText());
		int maxFilterRating = Integer.parseInt(maxRatingsFilter.getText());
		if (minFilterRating >= maxFilterRating) {
			int tmp = maxFilterRating;
			maxFilterRating = minFilterRating;
			minFilterRating = tmp;
		}
		int seekRating = seek.getRatingAsInt();
		if (seekRating >= minFilterRating && seekRating <= maxFilterRating) {
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

	protected void refreshSeekView() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (seeksTable.isDisposed()) {
					return;
				}
				synchronized (seeksTable.getTable()) {
					Seek[] seeks = getFilteredSeeks();
					seekGraph.replaceBy(seeks);

					String[][] data = new String[seeks.length][7];
					for (int i = 0; i < data.length; i++) {
						Seek seek = seeks[i];
						data[i][0] = seek.getAd();
						data[i][1] = seek.getRating();
						data[i][2] = seek.getTimeControl();
						data[i][3] = seek.getTypeDescription();
						data[i][4] = seek.getName();
						data[i][5] = seek.getRatingRange();
						data[i][6] = seek.getFlags();
					}
					seeksTable.refreshTable(data);
				}
			}
		});
	}
}
