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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
import raptor.util.IntegerComparator;
import raptor.util.RaptorRunnable;
import raptor.util.RatingComparator;

public class BugGamesWindowItem implements RaptorConnectorWindowItem {

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII };

	protected BughouseService service;
	protected Composite composite;
	protected RaptorTable bugGamesTable;
	protected boolean isActive = false;

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
												PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
								this);
			}
		}
	};

	protected BughouseServiceListener listener = new BughouseServiceListener() {
		public void availablePartnershipsChanged(Partnership[] newPartnerships) {
		}

		public void gamesInProgressChanged(BugGame[] newGamesInProgress) {
			refreshTable();
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
		return service.getConnector().getShortName() + "(Bug Games)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		bugGamesTable = new RaptorTable(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		bugGamesTable
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bugGamesTable.addColumn("ID", SWT.LEFT, 10, true,
				new IntegerComparator());
		bugGamesTable.addColumn("PID", SWT.LEFT, 10, true,
				new IntegerComparator());
		bugGamesTable.addColumn("WElo", SWT.LEFT, 14, true,
				new RatingComparator());
		bugGamesTable.addColumn("WName", SWT.LEFT, 19, true, null);
		bugGamesTable.addColumn("BElo", SWT.LEFT, 14, true,
				new RatingComparator());
		bugGamesTable.addColumn("BName", SWT.LEFT, 19, true, null);
		bugGamesTable.addColumn("Time", SWT.LEFT, 14, true, null);

		bugGamesTable.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowDoubleClicked(MouseEvent event, String[] rowData) {
				service.getConnector().onObserveGame(rowData[0]);
			}
		});

		// sort twice so it will be on white elo descending when new data
		// arrives.
		bugGamesTable.sort(2);
		bugGamesTable.sort(2);

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
					int selectedIndex = bugGamesTable.getTable()
							.getSelectionIndex();
					if (selectedIndex != -1) {
						service.getConnector().onObserveGame(
								bugGamesTable.getTable().getItem(selectedIndex)
										.getText(0));
					} else {
						Raptor.getInstance().alert(
								"You must selected a game to observe.");
					}
				}

			}
		});
		service.refreshGamesInProgress();
		refreshTable();
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			service.refreshGamesInProgress();
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

	protected void refreshTable() {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (!bugGamesTable.isDisposed()) {
					synchronized (bugGamesTable.getTable()) {

						BugGame[] bugGames = service.getGamesInProgress();
						if (bugGames == null) {
							bugGames = new BugGame[0];
						}

						String[][] rowData = new String[bugGames.length * 2][7];
						for (int i = 0; i < bugGames.length; i++) {
							rowData[i * 2][0] = bugGames[i].getGame1Id();
							rowData[i * 2][1] = bugGames[i].getGame2Id();
							rowData[i * 2][2] = bugGames[i].getGame1White()
									.getRating();
							rowData[i * 2][3] = bugGames[i].getGame1White()
									.getName();
							rowData[i * 2][4] = bugGames[i].getGame1Black()
									.getRating();
							rowData[i * 2][5] = bugGames[i].getGame1Black()
									.getName();
							rowData[i * 2][6] = bugGames[i].getTimeControl()
									+ " " + (bugGames[i].isRated() ? "r" : "u");

							rowData[i * 2 + 1][0] = bugGames[i].getGame2Id();
							rowData[i * 2 + 1][1] = bugGames[i].getGame1Id();
							rowData[i * 2 + 1][2] = bugGames[i].getGame2White()
									.getRating();
							rowData[i * 2 + 1][3] = bugGames[i].getGame2White()
									.getName();
							rowData[i * 2 + 1][4] = bugGames[i].getGame2Black()
									.getRating();
							rowData[i * 2 + 1][5] = bugGames[i].getGame2Black()
									.getName();
							rowData[i * 2 + 1][6] = bugGames[i]
									.getTimeControl()
									+ " " + (bugGames[i].isRated() ? "r" : "u");
						}
						bugGamesTable.refreshTable(rowData);
					}
				}
			}
		});
	}
}
