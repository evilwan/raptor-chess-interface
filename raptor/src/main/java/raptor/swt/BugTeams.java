/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import raptor.Raptor;
import raptor.chat.BugGame;
import raptor.chat.Bugger;
import raptor.chat.Partnership;
import raptor.connector.Connector;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.BughouseService.BughouseServiceListener;
import raptor.service.ThreadService;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chat.ChatUtils;
import raptor.util.RaptorRunnable;

public class BugTeams extends Composite {
	protected BughouseService service;
	protected Combo availablePartnershipsFilter;
	protected Combo matchHighLowBoth;
	protected RaptorTable player1Table;
	protected RaptorTable player2Table;
	protected boolean isActive = false;
	protected Button isRated;
	protected static L10n local = L10n.getInstance();

	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive && !isDisposed()) {
				service.refreshAvailablePartnerships();
				ThreadService
						.getInstance()
						.scheduleOneShot(
								Raptor.getInstance()
										.getPreferences()
										.getInt(PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
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

	public BugTeams(Composite parent, final BughouseService service) {
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

		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		tableComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		player1Table = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player1Table.addColumn(local.getString("bugTeams1"), SWT.LEFT, 20,
				false, null);
		player1Table.addColumn(local.getString("bugTeams2"), SWT.LEFT, 45,
				false, null);
		player1Table.addColumn(local.getString("bugTeams3"), SWT.LEFT, 35,
				false, null);
		player1Table.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(BugTeams.this.getShell(), SWT.POP_UP);
				ChatUtils.addPersonMenuItems(menu, getConnector(), rowData[1]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(player1Table.getTable().toDisplay(event.x,
							event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!BugTeams.this.getDisplay().readAndDispatch()) {
							BugTeams.this.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}
		});

		player2Table = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		player2Table.addColumn(local.getString("bugTeams1"), SWT.LEFT, 20,
				false, null);
		player2Table.addColumn(local.getString("bugTeams2"), SWT.LEFT, 45,
				false, null);
		player2Table.addColumn(local.getString("bugTeams3"), SWT.LEFT, 35,
				false, null);
		player2Table.addRaptorTableListener(new RaptorTableAdapter() {

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				Menu menu = new Menu(BugTeams.this.getShell(), SWT.POP_UP);
				ChatUtils.addPersonMenuItems(menu, getConnector(), rowData[1]);
				if (menu.getItemCount() > 0) {
					menu.setLocation(player2Table.getTable().toDisplay(event.x,
							event.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!BugTeams.this.getDisplay().readAndDispatch()) {
							BugTeams.this.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}

		});

		isRated = new Button(BugTeams.this, SWT.CHECK);
		isRated.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false));
		isRated.setText(local.getString("bugTeams4"));
		isRated.setSelection(Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED));
		isRated.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance()
						.getPreferences()
						.setValue(PreferenceKeys.BUG_ARENA_TEAMS_IS_RATED,
								isRated.getSelection());
			}
		});

		Composite controlsComposite = new Composite(BugTeams.this, SWT.NONE);
		controlsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				true, false));
		controlsComposite.setLayout(new GridLayout(4, false));

		CLabel label = new CLabel(controlsComposite, SWT.LEFT);
		label.setText(local.getString("bugTeams5"));
		availablePartnershipsFilter = new Combo(controlsComposite,
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
						Raptor.getInstance()
								.getPreferences()
								.setValue(
										PreferenceKeys.BUG_ARENA_TEAMS_INDEX,
										availablePartnershipsFilter
												.getSelectionIndex());
						Raptor.getInstance().getPreferences().save();
						refreshTable();
					}
				});

		CLabel label2 = new CLabel(controlsComposite, SWT.LEFT);
		label2.setText(local.getString("bugTeams6"));
		matchHighLowBoth = new Combo(controlsComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		matchHighLowBoth.add(local.getString("bugTeams7"));
		matchHighLowBoth.add(local.getString("bugTeams8"));
		matchHighLowBoth.add(local.getString("bugTeams9"));
		matchHighLowBoth.select(Raptor.getInstance().getPreferences()
				.getInt(PreferenceKeys.BUG_ARENA_HI_LOW_INDEX));
		matchHighLowBoth.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance()
						.getPreferences()
						.setValue(PreferenceKeys.BUG_ARENA_HI_LOW_INDEX,
								matchHighLowBoth.getSelectionIndex());
			}
		});

		Composite matchComposite = new Composite(BugTeams.this, SWT.NONE);
		matchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true,
				false));
		matchComposite.setLayout(new GridLayout(4, false));

		CLabel matchSelectedLabel = new CLabel(matchComposite, SWT.LEFT);
		matchSelectedLabel.setText(local.getString("bugTeams10"));

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
						match(true, selectedIndexes[i], 1, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 1, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
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
						match(true, selectedIndexes[i], 2, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 2, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
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
						match(true, selectedIndexes[i], 3, 0);
						matchedSomeone = true;
					}

					selectedIndexes = player2Table.getTable()
							.getSelectionIndices();

					for (int i = 0; i < selectedIndexes.length; i++) {
						match(false, selectedIndexes[i], 3, 0);
						matchedSomeone = true;
					}
				}

				if (!matchedSomeone) {
					Raptor.getInstance().alert(local.getString("bugTeams11"));
				}
			}
		});

		CLabel matchAllLabel = new CLabel(matchComposite, SWT.LEFT);
		matchAllLabel.setText(local.getString("bugTeams12"));

		Button all10Button = new Button(matchComposite, SWT.PUSH);
		all10Button.setText("1 0");
		all10Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				synchronized (player1Table) {
					matchAll(1, 0);
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
					matchAll(2, 0);

				}
			}

		});

		Button all30Button = new Button(matchComposite, SWT.PUSH);
		all30Button.setText("3 0");
		all30Button.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				matchAll(3, 0);
			}
		});
		service.refreshAvailablePartnerships();
		refreshTable();
	}

	private void matchAll(int time, int inc) {
		synchronized (player1Table) {
			String matchHighLow = matchHighLowBoth.getText();
			boolean isUserHigh = isUserHigh();
			int items = player1Table.getTable().getItems().length;

			for (int i = 0; i < items; i++) {
				if (local.getString("bugTeams7").equals(matchHighLow)) {
					if ((isHigh(true, i) && isUserHigh)
							|| (!isHigh(true, i) && !isUserHigh)) {
						match(true, i, time, inc);
					} else {
						match(false, i, time, inc);

					}
				} else if (local.getString("bugTeams8").equals(matchHighLow)) {
					if ((!isHigh(true, i) && isUserHigh)
							|| (isHigh(true, i) && !isUserHigh)) {
						match(true, i, time, inc);
					} else {
						match(false, i, time, inc);

					}
				} else {
					match(true, i, time, inc);
					match(false, i, time, inc);
				}
			}
		}
	}

	private boolean isUserHigh() {
		int userRow = -1;
		boolean result = false;

		synchronized (player1Table) {
			int items = player1Table.getTable().getItems().length;
			for (int i = 0; userRow == -1 && i < items; i++) {
				if (player1Table.getTable().getItems()[i].getText(1)
						.equalsIgnoreCase(service.getConnector().getUserName())) {
					userRow = i;
					result = isHigh(true, i);
				}
			}

			if (userRow == -1) {
				for (int i = 0; userRow == -1 && i < items; i++) {
					if (player2Table.getTable().getItems()[i].getText(1)
							.equalsIgnoreCase(
									service.getConnector().getUserName())) {
						userRow = i;
						result = isHigh(false, i);
					}
				}
			}
		}
		return result;
	}

	private boolean isHigh(boolean isTable1, int row) {
		String player1Elo = player1Table.getRowText(row)[0];
		String player2Elo = player2Table.getRowText(row)[0];

		int player1EloInt = 0;
		int player2EloInt = 0;

		try {
			player1EloInt = Integer.parseInt(player1Elo);
		} catch (NumberFormatException e) {
		}
		try {
			player2EloInt = Integer.parseInt(player2Elo);
		} catch (NumberFormatException e2) {
		}

		return isTable1 ? player1EloInt > player2EloInt
				: player2EloInt > player1EloInt;
	}

	private void match(boolean isTable1, int row, int time, int inc) {
		String player1Name = player1Table.getRowText(row)[1];
		String player2Name = player2Table.getRowText(row)[1];

		String userName = service.getConnector().getUserName();

		if (!userName.equalsIgnoreCase(player1Name)
				&& !userName.equalsIgnoreCase(player2Name)) {
			service.getConnector().matchBughouse(
					isTable1 ? player1Name : player2Name,
					isRated.getSelection(), time, inc);
		}

	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshAvailablePartnerships();
			ThreadService
					.getInstance()
					.scheduleOneShot(
							Raptor.getInstance()
									.getPreferences()
									.getInt(PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
							timer);
		}

	}

	public void onPassivate() {
		if (isActive) {
			isActive = false;
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
		Collections.sort(result);
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