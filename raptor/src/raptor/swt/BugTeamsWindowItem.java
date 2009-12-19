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
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
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

public class BugTeamsWindowItem implements RaptorConnectorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };

	protected BughouseService service;
	protected Composite composite;
	protected Combo availablePartnershipsFilter;
	protected RaptorTable player1Table;
	protected RaptorTable player2Table;
	protected boolean isActive = false;

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
			refreshTable();
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

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				service.getConnector().getShortName() + "-"
						+ PreferenceKeys.BUG_WHO_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isActive = false;
		composite.dispose();
		service.removeBughouseServiceListener(listener);
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
						+ PreferenceKeys.BUG_WHO_QUADRANT);
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
		player1Table = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player1Table.addColumn("Elo", SWT.LEFT, 20, false, null);
		player1Table.addColumn("Name", SWT.LEFT, 45, false, null);
		player1Table.addColumn("Status", SWT.LEFT, 35, false, null);
		player1Table.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, rowData[1]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(player1Table.getTable().toDisplay(event.x,
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

		player2Table = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player2Table.addColumn("Rating", SWT.LEFT, 20, false, null);
		player2Table.addColumn("Name", SWT.LEFT, 45, false, null);
		player2Table.addColumn("Status", SWT.LEFT, 35, false, null);
		player2Table.addRaptorTableListener(new RaptorTableAdapter() {

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(composite.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, rowData[1]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(player2Table.getTable().toDisplay(event.x,
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
					selectedIndexes = player1Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 1, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 1, 0);
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
					selectedIndexes = player1Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 2, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 2, 0);
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
					selectedIndexes = player1Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player1Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 3, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						service.getConnector().matchBughouse(
								player2Table.getTable().getItem(
										selectedIndexes[i]).getText(1),
								isRated.getSelection(), 3, 0);
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
					TableItem[] table1Items = player1Table.getTable()
							.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 1, 0);
					}

					TableItem[] table2Items = player2Table.getTable()
							.getItems();
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
					TableItem[] table1Items = player1Table.getTable()
							.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 2, 0);
					}

					TableItem[] table2Items = player2Table.getTable()
							.getItems();
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
					TableItem[] table1Items = player1Table.getTable()
							.getItems();
					for (TableItem item : table1Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 3, 0);
					}

					TableItem[] table2Items = player2Table.getTable()
							.getItems();
					for (TableItem item : table2Items) {
						service.getConnector().matchBughouse(item.getText(1),
								isRated.getSelection(), 3, 0);
					}
				}
			}
		});
		service.refreshAvailablePartnerships();
		refreshTable();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshAvailablePartnerships();
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

	protected Partnership[] getFilteredPartnerships() {
		Partnership[] current = service.getAvailablePartnerships();
		if (current == null) {
			current = new Partnership[0];
		}
		List<Partnership> result = new ArrayList<Partnership>(current.length);
		for (Partnership partnership : current) {
			if (passesFilterCriteria(partnership)) {
				result.add(partnership);
			}
		}
		return result.toArray(new Partnership[0]);
	}

	protected boolean passesFilterCriteria(Partnership partnership) {
		int filterRating = Integer.parseInt(availablePartnershipsFilter
				.getText());
		int buggerRating = partnership.getTeamRating();
		return buggerRating >= filterRating;
	}

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (player1Table.isDisposed()) {
					return;
				}

				synchronized (player1Table.getTable()) {

					Partnership[] partnerships = getFilteredPartnerships();
					String[][] player1Data = new String[partnerships.length][3];
					String[][] player2Data = new String[partnerships.length][3];

					for (int i = 0; i < partnerships.length; i++) {
						Partnership partnership = partnerships[i];

						player1Data[i][0] = partnership.getBugger1()
								.getRating();
						player1Data[i][1] = partnership.getBugger1().getName();
						player1Data[i][2] = partnership.getBugger1()
								.getStatus().toString();

						player2Data[i][0] = partnership.getBugger2()
								.getRating();
						player2Data[i][1] = partnership.getBugger2().getName();
						player2Data[i][2] = partnership.getBugger2()
								.getStatus().toString();

					}
					player1Table.refreshTable(player1Data);
					player2Table.refreshTable(player2Data);
				}
			}
		});
	}
}
