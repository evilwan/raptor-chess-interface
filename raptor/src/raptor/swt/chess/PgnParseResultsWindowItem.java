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
package raptor.swt.chess;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.chess.Game;
import raptor.chess.GameComparator;
import raptor.chess.Result;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserError;
import raptor.pref.PreferenceKeys;
import raptor.swt.ItemChangedListener;
import raptor.swt.chess.controller.InactiveController;

/**
 * A window item that displays a list of games from a PGN file.
 * 
 * @author mindspan
 * 
 */
public class PgnParseResultsWindowItem implements RaptorWindowItem {
	private static final Log LOG = LogFactory
			.getLog(PgnParseResultsWindowItem.class);;

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII };

	protected Composite composite;
	protected List<PgnParserError> errors;
	protected List<Game> games;
	protected boolean isPassive = true;
	protected TableColumn lastStortedColumn;
	protected String title;
	protected boolean wasLastSortAscending;

	public PgnParseResultsWindowItem(String title, List<PgnParserError> errors,
			List<Game> games) {
		this.errors = errors;
		this.games = games;
		this.title = title;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				PreferenceKeys.APP_PGN_RESULTS_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		if (errors != null) {
			errors.clear();
			errors = null;
		}
		if (games != null) {
			games.clear();
			games = null;
		}
		if (composite != null && !composite.isDisposed()) {
			composite.dispose();
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed PgnParseResultsWindowItem");
		}

	}

	public Composite getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public String getPercentage(int count, int total) {
		if (total == 0 || count == 0) {
			return "0%";
		} else {
			return new BigDecimal((double) count / (double) total * 100.0)
					.setScale(2, BigDecimal.ROUND_HALF_UP).toString()
					+ "%";
		}
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_PGN_RESULTS_QUADRANT);
	}

	public String getTitle() {
		return title;
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		int finishedGames = 0;
		int whiteWins = 0;
		int blackWins = 0;
		int draws = 0;

		for (Game game : games) {
			if (game.getResult() == Result.WHITE_WON) {
				whiteWins++;
				finishedGames++;
			} else if (game.getResult() == Result.BLACK_WON) {
				blackWins++;
				finishedGames++;
			} else if (game.getResult() == Result.DRAW) {
				draws++;
				finishedGames++;
			}
		}

		Label gamesTotalLabel = new Label(composite, SWT.LEFT);

		gamesTotalLabel.setText("Games: " + games.size() + "   White Win: "
				+ getPercentage(whiteWins, finishedGames) + "   Black Win: "
				+ getPercentage(blackWins, finishedGames) + "   Draw: "
				+ getPercentage(draws, finishedGames));
		gamesTotalLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		Composite gamesTableComposite = new Composite(composite, SWT.NONE);
		gamesTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		gamesTableComposite.setLayout(new FillLayout());
		final Table gamesTable = new Table(gamesTableComposite, SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		final TableColumn type = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn date = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn white = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn whiteElo = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn black = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn blackElo = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn result = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn eco = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn opening = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn event = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn site = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn round = new TableColumn(gamesTable, SWT.LEFT);

		gamesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (gamesTable.getSelectionIndex() != -1) {
					openGame(gamesTable.getSelectionIndex());
				}
			}
		});

		date.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == date ? !wasLastSortAscending
								: true;
				lastStortedColumn = date;

				Collections.sort(games, new GameComparator(PgnHeader.Date,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		white.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == white ? !wasLastSortAscending
								: true;
				lastStortedColumn = white;

				Collections.sort(games, new GameComparator(PgnHeader.White,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		whiteElo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == whiteElo ? !wasLastSortAscending
								: true;
				lastStortedColumn = whiteElo;

				Collections.sort(games, new GameComparator(PgnHeader.WhiteElo,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		black.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == black ? !wasLastSortAscending
								: true;
				lastStortedColumn = black;

				Collections.sort(games, new GameComparator(PgnHeader.Black,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		blackElo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == blackElo ? !wasLastSortAscending
								: true;
				lastStortedColumn = blackElo;

				Collections.sort(games, new GameComparator(PgnHeader.BlackElo,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		result.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == result ? !wasLastSortAscending
								: true;
				lastStortedColumn = result;

				Collections.sort(games, new GameComparator(PgnHeader.Result,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		eco.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == eco ? !wasLastSortAscending
								: true;
				lastStortedColumn = eco;

				Collections.sort(games, new GameComparator(PgnHeader.ECO,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		opening.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == opening ? !wasLastSortAscending
								: true;
				lastStortedColumn = opening;

				Collections.sort(games, new GameComparator(PgnHeader.Opening,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		event.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == event ? !wasLastSortAscending
								: true;
				lastStortedColumn = event;

				Collections.sort(games, new GameComparator(PgnHeader.Event,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		site.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == site ? !wasLastSortAscending
								: true;
				lastStortedColumn = site;

				Collections.sort(games, new GameComparator(PgnHeader.Site,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});
		round.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == round ? !wasLastSortAscending
								: true;
				lastStortedColumn = round;

				Collections.sort(games, new GameComparator(PgnHeader.Round,
						wasLastSortAscending));
				disposeAllItems(gamesTable);
				populateGamesTable(gamesTable);
			}
		});

		type.setText("Type");
		date.setText("Date");
		white.setText("White");
		whiteElo.setText("White ELO");
		black.setText("Black");
		blackElo.setText("Black ELO");
		result.setText("Result");
		eco.setText("ECO");
		opening.setText("Opening");
		event.setText("Event");
		site.setText("Site");
		round.setText("Round");

		type.setWidth(80);
		date.setWidth(80);
		event.setWidth(150);
		site.setWidth(100);
		round.setWidth(50);
		white.setWidth(100);
		whiteElo.setWidth(60);
		black.setWidth(100);
		blackElo.setWidth(60);
		result.setWidth(50);
		eco.setWidth(40);
		opening.setWidth(300);
		gamesTable.setHeaderVisible(true);

		Collections.sort(games, new GameComparator(PgnHeader.Date, false));
		lastStortedColumn = date;
		wasLastSortAscending = false;

		populateGamesTable(gamesTable);

		Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		button.setText("View");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (gamesTable.getSelectionIndex() != -1) {
					openGame(gamesTable.getSelectionIndex());
				}
			}
		});

		if (!errors.isEmpty()) {

			Label errorsLabel = new Label(composite, SWT.LEFT);
			errorsLabel.setText("Errors: " + errors.size());
			errorsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));

			Composite errorTableComposite = new Composite(composite, SWT.NONE);
			errorTableComposite.setLayoutData(new GridData(SWT.FILL,
					SWT.CENTER, true, false));
			Table errorTable = new Table(errorTableComposite, SWT.BORDER
					| SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
					| SWT.FULL_SELECTION);
			TableColumn tc1 = new TableColumn(errorTable, SWT.LEFT);
			TableColumn tc2 = new TableColumn(errorTable, SWT.LEFT);
			TableColumn tc3 = new TableColumn(errorTable, SWT.LEFT);
			tc1.setText("PGN Error");
			tc2.setText("Action Taken");
			tc3.setText("Line Number");
			tc1.setWidth(200);
			tc2.setWidth(200);
			tc3.setWidth(80);
			errorTable.setHeaderVisible(true);
			errorTable.setSize(errorTable.computeSize(SWT.DEFAULT, 150, true));

			for (PgnParserError error : errors) {
				TableItem item = new TableItem(errorTable, SWT.NONE);
				item.setText(new String[] { error.getType().name(),
						error.getAction().name(), "" + error.getLineNumber() });
			}
		}
	}

	public void onActivate() {
		if (isPassive) {
			if (composite != null && !composite.isDisposed()) {
				composite.layout(true);
			}
			isPassive = false;
		}
	}

	public void onPassivate() {
		isPassive = true;
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

	protected void disposeAllItems(Table table) {
		TableItem[] items = table.getItems();
		for (TableItem item : items) {
			item.dispose();
		}
	}

	protected void openGame(int index) {
		Game selectedGame = games.get(index);
		Raptor.getInstance().getWindow().addRaptorWindowItem(
				new ChessBoardWindowItem(new InactiveController(selectedGame,
						selectedGame.getHeader(PgnHeader.White) + " vs "
								+ selectedGame.getHeader(PgnHeader.Black),
						false)));
	}

	protected void populateGamesTable(Table gamesTable) {

		for (Game game : games) {
			TableItem item = new TableItem(gamesTable, SWT.NONE);
			item.setText(new String[] {
					StringUtils.defaultString(
							game.getHeader(PgnHeader.Variant), "?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.Date),
							"?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.White),
							"?"),
					StringUtils.defaultString(game
							.getHeader(PgnHeader.WhiteElo), "?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.Black),
							"?"),
					StringUtils.defaultString(game
							.getHeader(PgnHeader.BlackElo), "?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.Result),
							"?"),
					StringUtils
							.defaultString(game.getHeader(PgnHeader.ECO), ""),
					StringUtils.defaultString(
							game.getHeader(PgnHeader.Opening), ""),
					StringUtils.defaultString(game.getHeader(PgnHeader.Event),
							"?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.Site),
							"?"),
					StringUtils.defaultString(game.getHeader(PgnHeader.Round),
							"?"), });
		}
	}

}
