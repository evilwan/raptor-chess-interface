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
package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.Arrow;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.Highlight;

/**
 * A controller used when setting up a position. When the controller receives a
 * setupGameBecameExamined call from the GameService on the backing controller,
 * it changes the controller over to an examine controller.
 * 
 * All moves and adjustments made by this controller are sent directly to the
 * backing connector
 */
public class SetupController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ExamineController.class);

	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							onPlayGameEndSound();
							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);

							inactiveController.setBoard(board);
							inactiveController
									.setItemChangedListeners(itemChangedListeners);

							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);

							// board.clearCoolbar();
							connector.getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();

							unexamineOnDispose = false;
							SetupController.this.dispose();
						} catch (Throwable t) {
							connector
									.onError("SetupController.gameInactive", t);
						}
					}
				});

			}
		}

		@Override
		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							setupPositionUpdated();
						} catch (Throwable t) {
							connector.onError(
									"ExamineController.gameStateChanged", t);
						}
					}
				});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							setupOnIllegalMove(move);
						} catch (Throwable t) {
							connector.onError("ExamineController.illegalMove",
									t);
						}
					}
				});
			}
		}

		@Override
		public void setupGameBecameExamined(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							ExamineController examineController = new ExamineController(
									getGame(), connector);
							getBoard().setController(examineController);
							examineController
									.setItemChangedListeners(itemChangedListeners);
							examineController.setBoard(board);
							board.setWhitePieceJailOnTop(true);
							connector.getGameService()
									.removeGameServiceListener(listener);

							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);

							examineController.init();
							examineController.fireItemChanged();

							unexamineOnDispose = false;
							SetupController.this.dispose();

						} catch (Throwable t) {
							connector.onError(
									"SetupController.setupGameBecameExamined",
									t);
						}
					}
				});
			}
		}
	};
	protected ToolBar toolbar;
	protected boolean unexamineOnDispose = true;

	public SetupController(Game game, Connector connector) {
		super(game, connector);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isDisposed()) {
			board.getGameDescriptionLabel().setText(
					"Setting up a chess position");
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return true;
	}

	@Override
	public void dispose() {
		try {
			connector.getGameService().removeGameServiceListener(listener);
			if (unexamineOnDispose && getGame().isInState(Game.ACTIVE_STATE)) {
				connector.onUnexamine(getGame());
			}

			if (toolbar != null) {
				toolbar.setVisible(false);
				SWTUtils.clearToolbar(toolbar);
				toolbar = null;
			}
			super.dispose();
		} catch (Throwable t) {
		}// Eat it its prob a disposed exception
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Setup " + getGame().getId() + ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			BoardUtils.addSetupIconsToToolbar(this, toolbar);
			new ToolItem(toolbar, SWT.SEPARATOR);
		} else if (toolbar.getParent() != parent) {
			toolbar.setParent(parent);
		}
		return toolbar;
	}

	@Override
	public void init() {
		board.setWhitePieceJailOnTop(false);
		board.setWhiteOnTop(false);
		refresh();
		onPlayGameStartSound();
		connector.getGameService().addGameServiceListener(listener);
	}

	@Override
	public void onToolbarButtonAction(ToolBarItemKey key, String... args) {
		switch (key) {
		case FEN:
			Raptor.getInstance().promptForText(
					"FEN for game " + game.getHeader(PgnHeader.White) + " vs "
							+ game.getHeader(PgnHeader.Black), game.toFen());
			break;
		case FLIP:
			onFlip();
			break;
		case SETUP_CLEAR:
			connector.onSetupClear(getGame());
			break;
		case SETUP_START:
			connector.onSetupStartPosition(getGame());
			break;
		case SETUP_DONE:
			connector.onSetupComplete(getGame());
			break;
		case SETUP_FROM_FEN:
			String result = Raptor.getInstance().promptForText(
					"Enter the FEN to set the position to:");
			if (result != null) {
				connector.onSetupFromFEN(getGame(), result);
			}
			break;

		}
	}

	public void setupOnIllegalMove(String move) {
		SoundService.getInstance().playSound("illegalMove");
		if (move != null) {
			board.getStatusLabel().setText("Illegal Move: " + move);
		}

		refreshBoard();
	}

	public void setupPositionUpdated() {
		if (LOG.isDebugEnabled()) {
			LOG.info("besetupPositionUpdated " + getGame().getId() + " ...");
		}
		long startTime = System.currentTimeMillis();

		refresh();
		onPlayMoveSound();

		if (LOG.isDebugEnabled()) {
			LOG.info("examinePositionUpdate in " + getGame().getId() + "  "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();
		refresh();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
			board.getSquare(square).setPiece(GameConstants.EMPTY);
		}
		board.redrawSquares();
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
		}

		if (fromSquare == toSquare || BoardUtils.isPieceJailSquare(toSquare)) {
			refresh();
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Game game = getGame();
		Move move = null;

		if (BoardUtils.isPieceJailSquare(fromSquare)) {
			move = BoardUtils.createDropMove(fromSquare, toSquare);
		} else {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare));
		}
		// Always make the move first. It appears faster this way to the user.
		connector.makeMove(game, move);

		if (move != null) {
			refreshForMove(move);
		} else {
			setupOnIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	/**
	 * Provides a menu the user can use to drop and clear pieces.
	 */
	@Override
	public void userRightClicked(final int square) {
		if (!BoardUtils.isPieceJailSquare(square)) {

			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			if (getGame().getPiece(square) != EMPTY) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("Clear " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						connector.onSetupClearSquare(getGame(), square);
					}
				});
			} else {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("White P@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White N@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White B@ " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White R@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White Q@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, QUEEN, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White K@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, WHITE);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.SEPARATOR);

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black P@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black N@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black B@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black R@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black Q@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, QUEEN, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black K@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, BLACK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});
			}
			menu.setLocation(board.getSquare(square).toDisplay(10, 10));
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!board.getControl().getDisplay().readAndDispatch()) {
					board.getControl().getDisplay().sleep();
				}
			}
			menu.dispose();
		}
	}

	@Override
	protected void adjustClockColors() {
		if (!isDisposed()) {
			if (getGame().getColorToMove() == WHITE) {
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			} else {
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			}
		}
	}

	protected void adjustToDropMove(Move move, boolean isRedrawing) {
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();
		if (getPreferences().getBoolean(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_MY_MOVES)) {
			board.getSquareHighlighter().addHighlight(
					new Highlight(move.getFrom(), move.getTo(),
							getPreferences().getColor(
									PreferenceKeys.HIGHLIGHT_MY_COLOR),
							getPreferences().getBoolean(
									PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
		}
		if (getPreferences().getBoolean(PreferenceKeys.ARROW_SHOW_ON_MY_MOVES)) {
			board.getArrowDecorator().addArrow(
					new Arrow(move.getFrom(), move.getTo(), getPreferences()
							.getColor(PreferenceKeys.ARROW_MY_COLOR),
							getPreferences().getBoolean(
									PreferenceKeys.ARROW_FADE_AWAY_MODE)));
		}
		board.getSquare(move.getTo()).setPiece(
				BoardUtils
						.getColoredPiece(move.getPiece(), move.isWhitesMove()));

		if (isRedrawing) {
			board.redrawSquares();
		}
	}

	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}
}
