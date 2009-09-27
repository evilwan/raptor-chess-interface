package raptor.swt.chess.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.LabeledChessSquare;
import raptor.swt.chess.Utils;

public class SetupController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(ExamineController.class);
	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(Game game) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							InactiveController inactiveController = new InactiveController();
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);

							board.clearCoolbar();
							board.getConnector().getGameService()
									.removeGameServiceListener(listener);

							inactiveController.init();

							getBoard().fireOnControllerStateChange();
							SetupController.this.dispose();
						} catch (Throwable t) {
							board.getConnector().onError(
									"SetupController.gameInactive", t);
						}
					}
				});

			}
		}

		@Override
		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							besetupPositionUpdated();
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.gameStateChanged", t);
						}
					}
				});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							bsetupOnIllegalMove(move);
						} catch (Throwable t) {
							board.getConnector().onError(
									"ExamineController.illegalMove", t);
						}
					}
				});
			}
		}

		@Override
		public void setupGameBecameExamined(Game game) {
			if (game.getId().equals(board.getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							ExamineController examineController = new ExamineController();
							getBoard().setController(examineController);
							examineController.setBoard(board);

							board.clearCoolbar();
							board.setWhitePieceJailOnTop(true);
							board.getConnector().getGameService()
									.removeGameServiceListener(listener);

							examineController.init();

							getBoard().fireOnControllerStateChange();
							SetupController.this.dispose();
						} catch (Throwable t) {
							board.getConnector().onError(
									"SetupController.setupGameBecameExamined",
									t);
						}
					}
				});
			}
		}
	};

	public SetupController() {
	}

	@Override
	protected void adjustClockColors() {
		if (getGame().getColorToMove() == WHITE) {
			board.getWhiteClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
			board.getBlackClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		} else {
			board.getBlackClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
			board.getWhiteClockLabel().setForeground(
					board.getPreferences().getColor(
							PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		}
	}

	@Override
	protected void adjustCoolbarToInitial() {
		board.addGameActionButtonsToCoolbar();
		board.addSetupToCoolbar();
		board.packCoolbar();
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText("Setting up a chess position");
	}

	@Override
	protected void adjustNavButtonEnabledState() {
		board.setCoolBarButtonEnabled(false, ChessBoard.FIRST_NAV);
		board.setCoolBarButtonEnabled(false, ChessBoard.LAST_NAV);
		board.setCoolBarButtonEnabled(false, ChessBoard.NEXT_NAV);
		board.setCoolBarButtonEnabled(false, ChessBoard.BACK_NAV);
		board.setCoolBarButtonEnabled(false, ChessBoard.COMMIT_NAV);
		board.setCoolBarButtonEnabled(false, ChessBoard.REVERT_NAV);
	}

	@Override
	protected void adjustPieceJailFromGame(Game game) {
		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			int color = DROPPABLE_PIECE_COLOR[i];
			int coloredPiece = DROPPABLE_PIECES[i];
			int uncoloredPiece = Utils.pieceFromColoredPiece(coloredPiece);
			LabeledChessSquare square = getBoard().getPieceJailSquare(
					coloredPiece);
			int count = game.getPieceCount(color, uncoloredPiece);

			if (count > 0) {
				square.setPiece(coloredPiece);
			} else {
				square.setPiece(EMPTY);
			}

			square.setText(pieceCountToString(count));
			square.redraw();
		}
	}

	protected void adjustToDropMove(Move move) {
		board.getSquare(move.getTo()).setPiece(
				Utils.getColoredPiece(move.getPiece(), move.isWhitesMove()));
	}

	public void besetupPositionUpdated() {
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

	public void bsetupOnIllegalMove(String move) {
		LOG.info("bsetupOnIllegalMove " + getGame().getId() + " ...");
		long startTime = System.currentTimeMillis();
		SoundService.getInstance().playSound("illegalMove");
		board.getStatusLabel().setText("Illegal Move: " + move);
		board.unhighlightAllSquares();
		LOG.info("examineOnIllegalMove in " + getGame().getId() + "  "
				+ (System.currentTimeMillis() - startTime));
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		return true;
	}

	@Override
	public void dispose() {
		board.getConnector().getGameService().removeGameServiceListener(
				listener);
		super.dispose();
	}

	@Override
	public String getTitle() {
		return "Setup(" + getGame().getId() + ")";
	}

	@Override
	public void init() {
		super.init();
		board.setWhitePieceJailOnTop(false);
		board.getConnector().getGameService().addGameServiceListener(listener);
	}

	@Override
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isAdjournable() {
		return false;
	}

	@Override
	public boolean isAutoDrawable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isDrawable() {
		return false;
	}

	@Override
	public boolean isExaminable() {
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
	public boolean isPausable() {
		return false;
	}

	@Override
	public boolean isRematchable() {
		return false;
	}

	@Override
	public boolean isResignable() {
		return false;
	}

	@Override
	public boolean isRevertable() {
		return false;
	}

	@Override
	public boolean onClose() {
		boolean result = true;
		if (board.getConnector().isConnected()
				&& board.getGame().isInState(Game.ACTIVE_STATE)) {
			board.getConnector().onUnexamine(board.getGame());
		}
		return result;
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
		board.getConnector().onSetupClear(getGame());
	}

	@Override
	public void onSetupDone() {
		board.getConnector().onSetupComplete(getGame());
	}

	@Override
	public void onSetupFen(String fen) {
		board.getConnector().onSetupFromFEN(getGame(), fen);
	}

	@Override
	public void onSetupStart() {
		board.getConnector().onSetupStartPosition(getGame());
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		board.unhighlightAllSquares();
		adjustToGameMove();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		board.unhighlightAllSquares();
		board.getSquare(square).highlight();
		if (isDnd && !Utils.isPieceJailSquare(square)) {
			board.getSquare(square).setPiece(GameConstants.EMPTY);
		}
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		LOG.debug("Move made " + board.getGame().getId() + " " + fromSquare
				+ " " + toSquare);
		board.unhighlightAllSquares();

		if (fromSquare == toSquare || Utils.isPieceJailSquare(toSquare)) {
			board.unhighlightAllSquares();
			adjustToGameMove();
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Game game = board.getGame();
		Move move = null;

		if (Utils.isPieceJailSquare(fromSquare)) {
			move = Utils.createDropMove(fromSquare, toSquare);
			adjustToDropMove(move);
		} else {
			move = new Move(fromSquare, toSquare, game.getPiece(fromSquare),
					game.getColorToMove(), game.getPiece(toSquare));
			board.getSquare(toSquare).setPiece(
					Utils.getColoredPiece(game.getPiece(fromSquare), game
							.isWhitesMove()));
		}

		board.getSquare(fromSquare).highlight();
		board.getSquare(toSquare).highlight();
		board.getConnector().makeMove(game, move);
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(final int square) {
		if (!Utils.isPieceJailSquare(square)) {

			Menu menu = new Menu(board.getShell(), SWT.POP_UP);

			if (getGame().getPiece(square) != EMPTY) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("Clear " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						board.getConnector().onSetupClearSquare(getGame(),
								square);
					}
				});
			} else {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white pawn on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, WHITE);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white knight on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, WHITE);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white bishop on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, WHITE);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white rook on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, WHITE);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
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
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place white king on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, WHITE);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black pawn on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, PAWN, BLACK);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black knight on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KNIGHT, BLACK);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black bishop on "
						+ GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, BISHOP, BLACK);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black rook on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, ROOK, BLACK);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
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
						board.getConnector().makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Place black king on " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, KING, BLACK);
						adjustToDropMove(move);
						board.getConnector().makeMove(getGame(), move);
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
