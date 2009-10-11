package raptor.swt.chess;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
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
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserError;
import raptor.pref.PreferenceKeys;
import raptor.swt.ItemChangedListener;
import raptor.swt.chess.controller.InactiveController;

public class PgnParseResultsWindowItem implements RaptorWindowItem {

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

	}

	protected void disposeAllItems(Table table) {
		TableItem[] items = table.getItems();
		for (TableItem item : items) {
			item.dispose();
		}
	}

	public Composite getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				PreferenceKeys.GAME_QUADRANT);
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

		Label gamesTotalLabel = new Label(composite, SWT.LEFT);
		gamesTotalLabel.setText("Games: " + games.size());
		gamesTotalLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		final Table gamesTable = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		gamesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableColumn type = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn date = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn white = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn whiteElo = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn black = new TableColumn(gamesTable, SWT.LEFT);
		final TableColumn blackElo = new TableColumn(gamesTable, SWT.LEFT);
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
		eco.setWidth(40);
		opening.setWidth(100);
		gamesTable.setHeaderVisible(true);

		Collections.sort(games, new GameComparator(PgnHeader.Date, false));
		lastStortedColumn = date;
		wasLastSortAscending = false;

		populateGamesTable(gamesTable);

		Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
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

			Table errorTable = new Table(composite, SWT.BORDER | SWT.H_SCROLL
					| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

			errorTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
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

	protected void openGame(int index) {
		Game selectedGame = games.get(index);
		Raptor.getInstance().getRaptorWindow().addRaptorWindowItem(
				new ChessBoardWindowItem(new InactiveController(selectedGame,
						selectedGame.getHeader(PgnHeader.White) + " vs "
								+ selectedGame.getHeader(PgnHeader.Black))));
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

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

}
