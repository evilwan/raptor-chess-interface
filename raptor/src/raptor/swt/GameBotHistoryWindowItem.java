/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010,2010 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.connector.Connector;
import raptor.connector.fics.FicsConnector;
import raptor.connector.fics.GameBotService.GameBotListener;
import raptor.pref.PreferenceKeys;
import raptor.swt.RaptorTable.RaptorTableAdapter;
import raptor.swt.chat.ChatConsoleWindowItem;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chat.controller.PersonController;
import raptor.util.RaptorRunnable;

public class GameBotHistoryWindowItem implements RaptorConnectorWindowItem {
	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };
	public static final SimpleDateFormat GAME_BOT_PAGE_DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yy HH:mm");

	protected FicsConnector connector;
	protected Composite composite;
	protected RaptorTable gameBotPageTable;
	protected List<String[][]> pages = new ArrayList<String[][]>(100);
	protected int currentPage = -1;
	protected CLabel statusLabel;
	protected Composite settings;
	protected boolean isActive = false;
	protected boolean hasRetrievedAllPages = false;
	protected boolean isPlayerNotFound = false;
	protected Button previousPageButton;
	protected Button nextPageButton;
	protected String player;

	protected GameBotListener listener = new GameBotListener() {
		@Override
		public void gameBotPageArrived(String[][] rows, boolean hasNextPage) {
			if (rows.length > 0) {
				pages.add(convertPage(rows));
				hasRetrievedAllPages = !hasNextPage;
				currentPage++;
				showPage(currentPage);
			}
		}

		@Override
		public void gameBotPlayerNotInDB(String playerName) {
			statusLabel.setText(playerName
					+ " is not in the fics games database.");
			isPlayerNotFound = true;
		}
	};

	protected String[][] convertPage(String[][] gamesBotPage) {
		// gameBotPageTable.addColumn("ID", SWT.LEFT, 10, false, null);
		// gameBotPageTable.addColumn("Date", SWT.LEFT, 10, false, null);
		// gameBotPageTable.addColumn("Type", SWT.LEFT, 8, false, null);
		// gameBotPageTable.addColumn("White", SWT.LEFT, 20, false, null);
		// gameBotPageTable.addColumn("W ELO", SWT.LEFT, 8, false, null);
		// gameBotPageTable.addColumn("Black", SWT.LEFT, 20, false, null);
		// gameBotPageTable.addColumn("B ELO", SWT.LEFT, 8, false, null);
		// gameBotPageTable.addColumn("Result", SWT.LEFT, 10, true, null);
		// 241433469:aire:CDay:1525:1624:0-1:blitz:1:5:0:Fla:A40:82:1264389026

		String[][] result = new String[gamesBotPage.length][9];

		for (int i = 0; i < result.length; i++) {
			result[i][0] = gamesBotPage[i][0];
			result[i][1] = GAME_BOT_PAGE_DATE_FORMAT.format(new Date(Long
					.parseLong(gamesBotPage[i][13]) * 1000));
			result[i][2] = gamesBotPage[i][8] + " " + gamesBotPage[i][9] + " "
					+ (gamesBotPage[i][7].equals("1") ? "r" : "u") + " "
					+ convertType(gamesBotPage[i][6]);
			result[i][3] = gamesBotPage[i][1];
			result[i][4] = gamesBotPage[i][3];
			result[i][5] = gamesBotPage[i][2];
			result[i][6] = gamesBotPage[i][4];
			result[i][7] = gamesBotPage[i][5];
		}
		return result;
	}

	protected String convertType(String type) {
		String result = type;
		if (type.equals("blitz") || type.equals("standard")
				|| type.equals("lightning")) {
			result = "";
		} else if (type.equals("crazyhouse")) {
			result = "zh";
		} else if (type.equals("atomic")) {
			result = "atom";
		} else if (type.equals("suicide")) {
			result = "sui";
		} else if (type.equals("loosers")) {
			result = "loos";
		}
		return result;
	}

	public GameBotHistoryWindowItem(FicsConnector connector, String player) {
		this.connector = connector;
		this.player = player;
		connector.getGameBotService().addGameBotListener(listener);
	}

	public void setPlayerName(String player) {
		if (composite != null) {
			pages.clear();
			statusLabel.setText("Retrieving games for player " + player + ".");
			this.player = player;
			hasRetrievedAllPages = false;
			isPlayerNotFound = false;
			currentPage = -1;
			gameBotPageTable.clearTable();
			connector.getGameBotService().history(player);
		}
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				getConnector().getShortName() + "-"
						+ PreferenceKeys.GAME_BOT_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isActive = false;
		composite.dispose();
		connector.getGameBotService().removeGameBotListener(listener);
	}

	public Connector getConnector() {
		return connector;
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
				getConnector().getShortName() + "-"
						+ PreferenceKeys.GAMES_TAB_QUADRANT);
	}

	public String getTitle() {
		return getConnector().getShortName() + "(GameBot History)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(SWTUtils
				.createMarginlessRowLayout(SWT.HORIZONTAL));
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Button firstPageButton = new Button(buttonComposite, SWT.FLAT);
		firstPageButton.setImage(Raptor.getInstance().getIcon("first"));
		firstPageButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (!isPlayerNotFound && currentPage != -1 && pages.size() > 0) {
					currentPage = 0;
					showPage(currentPage);
				}
			}
		});

		Button previousPageButton = new Button(buttonComposite, SWT.FLAT);
		previousPageButton.setImage(Raptor.getInstance().getIcon("back"));
		previousPageButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (!isPlayerNotFound && currentPage > 0) {
					currentPage--;
					showPage(currentPage);
				}
			}
		});

		Button nextPageButton = new Button(buttonComposite, SWT.FLAT);
		nextPageButton.setImage(Raptor.getInstance().getIcon("next"));
		nextPageButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (!isPlayerNotFound && currentPage == pages.size() - 1) {
					if (!hasRetrievedAllPages) {
						statusLabel.setText("Player " + player
								+ ": Retrieving next page...");
						composite.layout(true, true);
						connector.getGameBotService().nextPage();
					}
				} else if (!isPlayerNotFound && currentPage != -1) {
					currentPage++;
					showPage(currentPage);
				}
			}
		});

		statusLabel = new CLabel(buttonComposite, SWT.LEFT);
		statusLabel.setText("                             ");

		gameBotPageTable = new RaptorTable(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		gameBotPageTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

		gameBotPageTable.addColumn("ID", SWT.LEFT, 0, false, null);
		gameBotPageTable.addColumn("Date", SWT.LEFT, 24, false, null);
		gameBotPageTable.addColumn("Type", SWT.LEFT, 12, false, null);
		gameBotPageTable.addColumn("White", SWT.LEFT, 20, false, null);
		gameBotPageTable.addColumn("W ELO", SWT.LEFT, 8, false, null);
		gameBotPageTable.addColumn("Black", SWT.LEFT, 20, false, null);
		gameBotPageTable.addColumn("B ELO", SWT.LEFT, 8, false, null);
		gameBotPageTable.addColumn("Result", SWT.LEFT, 8, true, null);

		gameBotPageTable.addRaptorTableListener(new RaptorTableAdapter() {
			@Override
			public void rowDoubleClicked(MouseEvent event, String[] rowData) {
				connector.getGameBotService().examine(rowData[0]);
			}

			@Override
			public void rowRightClicked(MouseEvent event, String[] rowData) {
				// TO DO add fics games lookup.
			}
		});
		connector.getGameBotService().history(player);
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
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

	protected void showPage(final int pageId) {
		Raptor.getInstance().getDisplay().asyncExec(new RaptorRunnable() {
			@Override
			public void execute() {
				if (gameBotPageTable.isDisposed()) {
					return;
				}
				synchronized (gameBotPageTable.getTable()) {
					statusLabel.setText("Player " + player + " ("
							+ (currentPage + 1) + " of "
							+ (hasRetrievedAllPages ? pages.size() : "?")
							+ " pages.)");
					gameBotPageTable.refreshTable(pages.get(pageId));
					composite.layout(true, true);
				}
			}
		});
	}
}
