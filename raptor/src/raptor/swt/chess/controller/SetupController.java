package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.LabeledChessSquare;

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
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							board.clearCoolbar();
							connector.getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();

							inactiveController
									.setItemChangedListeners(itemChangedListeners);
							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);
							unexamineOnDispose = false;
							SetupController.this.dispose();
							inactiveController.fireItemChanged();
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
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
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
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
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
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							ExamineController examineController = new ExamineController(
									getGame(), connector);
							getBoard().setController(examineController);
							examineController.setBoard(board);

							board.clearCoolbar();
							board.setWhitePieceJailOnTop(true);
							connector.getGameService()
									.removeGameServiceListener(listener);

							examineController.init();

							examineController
									.setItemChangedListeners(itemChangedListeners);
							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);
							unexamineOnDispose = false;
							SetupController.this.dispose();
							examineController.fireItemChanged();
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

	protected Connector connector;
	protected boolean unexamineOnDispose = true;

	public SetupController(Game game, Connector connector) {
		super(game);
		this.connector = connector;
	}

	@Override
	protected void adjustClockColors() {
		if (!isBeingReparented()) {
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

	@Override
	protected void adjustCoolbarToInitial() {
		if (!isBeingReparented()) {
			board.addGameActionButtonsToCoolbar();
			board.addSetupToCoolbar();
			board.packCoolbar();
		}
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isBeingReparented()) {
			board.getGameDescriptionLabel().setText(
					"Setting up a chess position");
		}
	}

	@Override
	protected void adjustNavButtonEnabledState() {
		if (!isBeingReparented()) {
			board.setCoolBarButtonEnabled(false, ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.BACK_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.COMMIT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.REVERT_NAV);
		}
	}

	@Override
	protected void adjustPieceJailFromGame(Game game) {
		if (!isBeingReparented()) {
			for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
				int color = DROPPABLE_PIECE_COLOR[i];
				int coloredPiece = DROPPABLE_PIECES[i];
				int uncoloredPiece = BoardUtils
						.pieceFromColoredPiece(coloredPiece);
				LabeledChessSquare square = getBoard().getPieceJailSquare(
						coloredPiece);
				int count = game.getPieceCount(color, uncoloredPiece);

				if (count > 0) {
					square.setPiece(coloredPiece);
				} else {
					square.setPiece(EMPTY);
				}

				square.setText(BoardUtils.pieceCountToString(count));
				square.redraw();
			}
		}
	}

	protected void adjustToDropMove(Move move) {
		if (!isBeingReparented()) {
			board.getSquare(move.getTo()).setPiece(
					BoardUtils.getColoredPiece(move.getPiece(), move
							.isWhitesMove()));
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return true;
	}

	@Override
	public void dispose() {
		connector.getGameService().removeGameServiceListener(listener);
		if (unexamineOnDispose && getGame().isInState(Game.ACTIVE_STATE)) {
			connector.onUnexamine(getGame());
		}
		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return "Setup(" + getGame().getId() + ")";
	}

	@Override
	public void init() {
		super.init();
		board.setWhitePieceJailOnTop(false);
		connector.getGameService().addGameServiceListener(listener);
	}

	@Override
	public boolean isAutoDrawable() {
		return false;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isMoveListTraversable() {
		return false;
	}

	@Override
	public boolean isNavigatable() {
		return false;
	}

	@Override
	public boolean isRevertable() {
		return false;
	}

	@Override
	protected void onPlayGameEndSound() {
		SoundService.getInstance().playSound("obsGameEnd");
	}

	@Override
	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	@Override
	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	@Override
	public void onSetupClear() {
		if (!isBeingReparented()) {
			connector.onSetupClear(getGame());
		}
	}

	@Override
	public void onSetupDone() {
		if (!isBeingReparented()) {
			connector.onSetupComplete(getGame());
		}
	}

	@Override
	public void onSetupFen(String fen) {
		if (!isBeingReparented()) {
			connector.onSetupFromFEN(getGame(), fen);
		}
	}

	@Override
	public void onSetupStart() {
		if (!isBeingReparented()) {
			connector.onSetupStartPosition(getGame());
		}
	}

	public void setupOnIllegalMove(String move) {
		if (!isBeingReparented()) {
			LOG.info("bsetupOnIllegalMove " + getGame().getId() + " ...");
			long startTime = System.currentTimeMillis();
			SoundService.getInstance().playSound("illegalMove");
			board.getStatusLabel().setText("Illegal Move: " + move);
			board.unhighlightAllSquares();
			LOG.info("examineOnIllegalMove in " + getGame().getId() + "  "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void setupPositionUpdated() {
		if (!isBeingReparented()) {
			LOG.info("besetupPositionUpdated " + getGame().getId() + " ...");
			long startTime = System.currentTimeMillis();

			stopClocks();

			adjustNameRatingLabels();
			adjustGameDescriptionLabel();
			adjustToGameChangeNotInvolvingMove();
			adjustBoardToGame(getGame());
			adjustPieceJailFromGame(getGame());
			adjustNavButtonEnabledState();
			adjustClockColors();

			board.forceUpdate();
			onPlayMoveSound();
			board.unhighlightAllSquares();
			LOG.info("examinePositionUpdate in " + getGame().getId() + "  "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (!isBeingReparented()) {
			board.unhighlightAllSquares();
			adjustToGameMove();
		}
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isBeingReparented()) {
			board.unhighlightAllSquares();
			board.getSquare(square).highlight();
			if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
				board.getSquare(square).setPiece(GameConstants.EMPTY);
			}
		}
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (!isBeingReparented()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
			board.unhighlightAllSquares();

			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {
				board.unhighlightAllSquares();
				adjustToGameMove();
				SoundService.getInstance().playSound("illegalMove");
				return;
			}

			Game game = getGame();
			Move move = null;

			if (BoardUtils.isPieceJailSquare(fromSquare)) {
				move = BoardUtils.createDropMove(fromSquare, toSquare);
				adjustToDropMove(move);
			} else {
				move = new Move(fromSquare, toSquare,
						game.getPiece(fromSquare), game.getColorToMove(), game
								.getPiece(toSquare));
				board.getSquare(toSquare).setPiece(
						BoardUtils.getColoredPiece(game.getPiece(fromSquare),
								game.isWhitesMove()));
			}

			board.getSquare(fromSquare).highlight();
			board.getSquare(toSquare).highlight();
			connector.makeMove(game, move);
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
		if (!isBeingReparented() && !BoardUtils.isPieceJailSquare(square)) {

			Menu menu = new Menu(board.getShell(), SWT.POP_UP);

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
				item.setText("Place white pawn on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white knight on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white bishop on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white rook on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item
						.setText("Place white queen on "
								+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, QUEEN, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white king on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, WHITE);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black pawn on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black knight on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black bishop on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black rook on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item
						.setText("Place black queen on "
								+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, QUEEN, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black king on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, BLACK);
						adjustToDropMove(move);
						connector.makeMove(getGame(), move);
					}
				});
			}
			menu.setLocation(board.getSquare(square).toDisplay(10, 10));
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!board.getDisplay().readAndDispatch())
					board.getDisplay().sleep();
			}
			menu.dispose();
		}
	}
}
