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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;

import raptor.Raptor;
import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Partnership;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.ThreadService;
import raptor.service.BughouseService.BughouseServiceListener;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.PersonController;
import raptor.util.RaptorRunnable;
import raptor.util.RatingComparator;

public class BugPartners extends Composite {

	public static final String[] getRatings() {
		return new String[] { "0", "1", "700", "1000", "1100", "1200", "1300",
				"1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100",
				"2200", "2300", "2400", "2500", "2600", "2700", "2800", "3000",
				"9999" };
	}

	protected BughouseService service;
	protected Combo minAvailablePartnersFilter;
	protected Combo maxAvailablePartnersFilter;
	protected boolean isActive = false;

	protected RaptorTable table;

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
			refreshTable();
		}
	};

	public BugPartners(Composite parent, final BughouseService service) {
		super(parent, SWT.NONE);
		this.service = service;
		init();
		service.addBughouseServiceListener(listener);
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				isActive = false;
				service.removeBughouseServiceListener(listener);
			}
		});
	}

	public Connector getConnector() {
		return service.getConnector();
	}

	public void init() {
		setLayout(new GridLayout(1, false));

		Composite ratingFilterComposite = new Composite(this, SWT.NONE);
		ratingFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		ratingFilterComposite.setLayout(new RowLayout());
		minAvailablePartnersFilter = new Combo(ratingFilterComposite,
				SWT.DROP_DOWN | SWT.READ_ONLY);

		for (String rating : getRatings()) {
			minAvailablePartnersFilter.add(rating);
		}
		minAvailablePartnersFilter.select(Raptor.getInstance().getPreferences()
				.getInt(PreferenceKeys.BUG_ARENA_PARTNERS_INDEX));
		minAvailablePartnersFilter
				.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						Raptor.getInstance().getPreferences().setValue(
								PreferenceKeys.BUG_ARENA_PARTNERS_INDEX,
								minAvailablePartnersFilter.getSelectionIndex());
						Raptor.getInstance().getPreferences().save();
						refreshTable();
					}
				});

		CLabel label = new CLabel(ratingFilterComposite, SWT.LEFT);
		label.setText(">= Rating <= ");
		maxAvailablePartnersFilter = new Combo(ratingFilterComposite,
				SWT.DROP_DOWN | SWT.READ_ONLY);
		for (String rating : getRatings()) {
			maxAvailablePartnersFilter.add(rating);
		}
		maxAvailablePartnersFilter.select(Raptor.getInstance().getPreferences()
				.getInt(PreferenceKeys.BUG_ARENA_MAX_PARTNERS_INDEX));
		maxAvailablePartnersFilter
				.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}

					public void widgetSelected(SelectionEvent e) {
						Raptor.getInstance().getPreferences().setValue(
								PreferenceKeys.BUG_ARENA_MAX_PARTNERS_INDEX,
								maxAvailablePartnersFilter.getSelectionIndex());
						Raptor.getInstance().getPreferences().save();
						refreshTable();
					}
				});

		table = new RaptorTable(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		table.addColumn("Rating", SWT.LEFT, 25, true, new RatingComparator());
		table.addColumn("Name", SWT.LEFT, 50, true, null);
		table.addColumn("Status", SWT.LEFT, 25, true, null);

		// Sort twice so it will sort by rating descending.
		table.sort(0);
		table.sort(0);

		table.addRaptorTableListener(new RaptorTableAdapter() {

			@Override
			public void rowDoubleClicked(MouseEvent event, String[] rowData) {
				service.getConnector().onPartner(rowData[1]);

			}

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(BugPartners.this.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, rowData[1]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(table.getTable().toDisplay(event.x,
							event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!BugPartners.this.getDisplay().readAndDispatch()) {
							BugPartners.this.getDisplay().sleep();
						}
					}
				}
				menu.dispose();

			}
		});

		Composite buttonsComposite = new Composite(this, SWT.NONE);
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
				synchronized (table.getTable()) {
					selectedIndexes = table.getTable().getSelectionIndices();
				}
				if (selectedIndexes == null || selectedIndexes.length == 0) {
					Raptor.getInstance().alert(
							"You must first some select buggers to partner.");
				} else {
					synchronized (table.getTable()) {
						for (int i = 0; i < selectedIndexes.length; i++) {
							service.getConnector().onPartner(
									table.getTable()
											.getItem(selectedIndexes[i])
											.getText(1));
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
				synchronized (table.getTable()) {
					TableItem[] items = table.getTable().getItems();
					for (TableItem item : items) {
						service.getConnector().onPartner(item.getText(1));
					}
				}
			}
		});
		service.refreshUnpartneredBuggers();
		refreshTable();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshUnpartneredBuggers();
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

	protected Bugger[] getFilteredBuggers() {
		Bugger[] buggers = service.getUnpartneredBuggers();
		List<Bugger> result = new ArrayList<Bugger>(buggers.length);
		for (Bugger bugger : buggers) {
			if (passesFilterCriteria(bugger)) {
				result.add(bugger);
			}
		}
		return result.toArray(new Bugger[0]);
	}

	protected boolean passesFilterCriteria(Bugger bugger) {
		int minFilterRating = Integer.parseInt(minAvailablePartnersFilter
				.getText());
		int maxFilterRating = Integer.parseInt(maxAvailablePartnersFilter
				.getText());
		if (minFilterRating >= maxFilterRating) {
			int tmp = maxFilterRating;
			maxFilterRating = minFilterRating;
			minFilterRating = tmp;
		}
		int buggerRating = bugger.getRatingAsInt();
		return buggerRating >= minFilterRating
				&& buggerRating <= maxFilterRating;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (table.isDisposed()) {
					return;
				}

				synchronized (table.getTable()) {
					Bugger[] currentBuggers = getFilteredBuggers();
					String[][] newData = new String[currentBuggers.length][3];
					for (int i = 0; i < currentBuggers.length; i++) {
						newData[i][0] = currentBuggers[i].getRating();
						newData[i][1] = currentBuggers[i].getName();
						newData[i][2] = currentBuggers[i].getStatus()
								.toString();
					}
					table.refreshTable(newData);
				}
			}
		});
	}
}
