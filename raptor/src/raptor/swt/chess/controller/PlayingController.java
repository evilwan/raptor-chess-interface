package raptor.swt.chess.controller;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.Game.Result;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;

/**
 * The controller used when a user is playing a game. Supports premove,queued
 * premove, and auto draw. game.getWhiteName() or game.getBlackName() must match
 * connector.getUserName().
 * 
 * When a game is no longer active, this controller swaps itself out with the
 * Inactive controller.
 * 
 */
public class PlayingController extends ChessBoardController {
	/**
	 * A class containing the details of a premove.
	 */
	protected static class PremoveInfo {
		int fromSquare;
		int toSquare;
		int promotionColorlessPiece;
	}

	static final Log LOG = LogFactory.getLog(PlayingController.class);

	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(Game game) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {

							board.redrawSquares();
							onPlayGameEndSound();

							// Now swap controllers to the inactive controller.
							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);
							inactiveController
									.setItemChangedListeners(itemChangedListeners);

							// Detatch from the GameService.
							connector.getGameService()
									.removeGameServiceListener(listener);

							// Clear the cool bar and init the inactive
							// controller.
							board.clearCoolbar();
							inactiveController.init();

							// Set the listeners to null so they wont get
							// cleared and we wont get notified.
							setItemChangedListeners(null);

							// Fire item changed from the inactive controller
							// so they tab information gets adjusted.
							inactiveController.fireItemChanged();

							// And finally dispose.
							PlayingController.this.dispose();
						} catch (Throwable t) {
							connector.onError("PlayingController.gameInactive",
									t);
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
							if (isNewMove) {
								handleAutoDraw();
								if (!makePremove()) {
									adjustToGameMove();
								}
							} else {
								adjustToGameMove();
							}
						} catch (Throwable t) {
							connector.onError(
									"PlayingController.gameStateChanged", t);
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
							adjustForIllegalMove(move);
						} catch (Throwable t) {
							connector.onError("PlayingController.illegalMove",
									t);
						}
					}
				});
			}
		}
	};

	protected Connector connector;
	protected boolean isUserWhite;
	protected Random random = new SecureRandom();
	protected boolean isPlayingMoveSound = true;
	protected List<PremoveInfo> premoves = Collections
			.synchronizedList(new ArrayList<PremoveInfo>(10));

	/**
	 * Creates a playing controller. One of the players white or black playing
	 * the game must match the name of connector.getUserName.
	 * 
	 * @param game
	 *            The game to control.
	 * @param connector
	 *            The backing connector.
	 */
	public PlayingController(Game game, Connector connector) {
		super(game);
		this.connector = connector;

		if (StringUtils.equalsIgnoreCase(game.getWhiteName(), connector
				.getUserName())) {
			isUserWhite = true;
		} else if (StringUtils.equalsIgnoreCase(game.getBlackName(), connector
				.getUserName())) {
			isUserWhite = false;
		} else {
			throw new IllegalArgumentException(
					"Could not deterimne user color " + connector.getUserName()
							+ " " + game.getWhiteName() + " "
							+ game.getBlackName());
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("isUserWhite=" + isUserWhite);
		}

	}

	@Override
	protected void adjustCoolbarToInitial() {
		if (!isBeingReparented()) {
			board.addGameActionButtonsToCoolbar();
			board.addAutoPromoteRadioGroupToCoolbar();
			board.packCoolbar();
		}
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		if (!getPreferences().getBoolean(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
			onClearPremoves();
		}

		board.unhighlightAllSquares();
		isPlayingMoveSound = false;
		adjustToGameMove();
		isPlayingMoveSound = true;
		SoundService.getInstance().playSound("illegalMove");
	}

	public void adjustForIllegalMove(String move) {
		if (!isBeingReparented()) {
			board.getStatusLabel().setText("Illegal Move: " + move);
			adjustForIllegalMove();
		}
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isBeingReparented()) {
			board.getGameDescriptionLabel().setText(
					"Playing " + getGame().getEvent());
		}
	}

	@Override
	protected void adjustNavButtonEnabledState() {
		if (!isBeingReparented()) {
			board.setCoolBarButtonEnabled(true, ChessBoard.FIRST_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.LAST_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.NEXT_NAV);
			board.setCoolBarButtonEnabled(true, ChessBoard.BACK_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.COMMIT_NAV);
			board.setCoolBarButtonEnabled(false, ChessBoard.REVERT_NAV);
		}
	}

	/**
	 * Adds all premoves to the premoves label. Also updates the clear premove
	 * button if there are moves in the premove queue.
	 */
	@Override
	protected void adjustPremoveLabel() {
		if (isBeingReparented()) {
			return;
		}

		String labelText = "";
		synchronized (premoves) {
			for (PremoveInfo info : premoves) {
				String premove = ""
						+ BoardUtils.getPieceRepresentation(BoardUtils
								.getColoredPiece(info.fromSquare, game))
						+ (board.getSquare(info.toSquare).getPiece() != EMPTY ? "x"
								: "") + GameUtils.getSan(info.toSquare);
				if (labelText.equals("")) {
					labelText += premove;
				} else {
					labelText += " , " + premove;
				}
			}
		}
		if (labelText.equals("")) {
			labelText = "Premoves: EMPTY";
			board.setCoolBarButtonEnabled(false, ChessBoard.PREMOVE);
		} else {
			labelText = "Premoves: " + labelText;
			board.setCoolBarButtonEnabled(true, ChessBoard.PREMOVE);
		}
		board.getCurrentPremovesLabel().setText(labelText);
	}

	/**
	 * Invoked when the user tries to start a dnd or click click move operation
	 * on the board. This method returns false if its not allowed.
	 * 
	 * For non premoven you can only move one of your pieces when its your move.
	 * If the game is droppable you can drop from the piece jail, otherwise you
	 * can't.
	 * 
	 * If premove is enabled you are allowed to move your pieces when it is not
	 * your move.
	 */
	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("canUserInitiateMoveFrom " + GameUtils.getSan(squareId));
		}
		if (!isUsersMove()) {
			if (isPremoveable()) {
				if (getGame().isInState(Game.DROPPABLE_STATE)
						&& BoardUtils.isPieceJailSquare(squareId)) {
					return (isUserWhite && BoardUtils
							.isJailSquareWhitePiece(squareId))
							|| (!isUserWhite && BoardUtils
									.isJailSquareBlackPiece(squareId));
				} else {
					return (isUserWhite && GameUtils.isWhitePiece(getGame(),
							squareId))
							|| (!isUserWhite && GameUtils.isBlackPiece(game,
									squareId));
				}
			}
			return false;
		} else if (BoardUtils.isPieceJailSquare(squareId)
				&& !getGame().isInState(Game.DROPPABLE_STATE)) {
			return false;
		} else if (getGame().isInState(Game.DROPPABLE_STATE)
				&& BoardUtils.isPieceJailSquare(squareId)) {
			return (isUserWhite && BoardUtils.isJailSquareWhitePiece(squareId))
					|| (!isUserWhite && BoardUtils
							.isJailSquareBlackPiece(squareId));
		} else {
			return (isUserWhite && GameUtils.isWhitePiece(getGame(), squareId))
					|| (!isUserWhite && GameUtils.isBlackPiece(game, squareId));
		}
	}

	@Override
	public boolean confirmClose() {
		if (connector.isConnected()) {
			boolean result = Raptor.getInstance().confirm(
					"Closing a game you are playing will result in resignation of the game."
							+ ". Do you wish to proceed?");
			if (result) {
				connector.onResign(getGame());
			}
			return result;
		} else {
			return true;
		}
	}

	@Override
	public void dispose() {
		connector.getGameService().removeGameServiceListener(listener);
		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Play " + getGame().getId() + ")";
	}

	/**
	 * If auto draw is enabled, a draw request is sent. This method should be
	 * invoked when receiving a move and right after sending one.
	 * 
	 * In the future this will become smarter and only draw when the game shows
	 * a draw by three times in the same position or 50 move draw rule.
	 */
	protected void handleAutoDraw() {
		if (board.isCoolbarButtonSelectd(ChessBoard.AUTO_DRAW)) {
			getConnector().onDraw(getGame());
		}
	}

	@Override
	public void init() {

		board.setWhiteOnTop(!isUserWhite());

		/**
		 * In Droppable games (bughouse/crazyhouse) you own your own piece jail
		 * since you can drop pieces from it.
		 * 
		 * In other games its just a collection of pieces yu have captured so
		 * your opponent owns your piece jail.
		 */
		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? true : false);
		} else {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? false : true);
		}
		super.init();

		// Since the game object is updated while the game is being created,
		// there
		// is no risk of missing game events. We will pick them up when we get
		// the position
		// of the game since it will always be udpated.
		connector.getGameService().addGameServiceListener(listener);
	}

	@Override
	public boolean isAutoDrawable() {
		return true;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isMoveListTraversable() {
		return true;
	}

	@Override
	public boolean isNavigatable() {
		return true;
	}

	/**
	 * Returns true if the premove preference is enabled.
	 */
	@Override
	public boolean isPremoveable() {
		return Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_PREMOVE_ENABLED);
	}

	@Override
	public boolean isRevertable() {
		return false;
	}

	public boolean isUsersMove() {
		return (isUserWhite() && game.isWhitesMove())
				|| (!isUserWhite() && !game.isWhitesMove());
	}

	public boolean isUserWhite() {
		return isUserWhite;
	}

	/**
	 * Runs through the premove queue and tries to make each move. If a move
	 * succeeds it is made and the rest of the queue is left intact. If a move
	 * fails it is removed from the queue and the next move is tried.
	 * 
	 * If a move succeeded true is returned, otherwise false is returned.
	 */
	protected boolean makePremove() {
		synchronized (premoves) {
			for (PremoveInfo info : premoves) {
				Move move = null;
				try {
					if (info.promotionColorlessPiece == EMPTY) {
						move = game.makeMove(info.fromSquare, info.toSquare);
					} else {
						move = game.makeMove(info.fromSquare, info.toSquare,
								info.promotionColorlessPiece);
					}
					getConnector().makeMove(getGame(), move);
					premoves.remove(move);
					handleAutoDraw();
					adjustToGameMove();
					return true;
				} catch (IllegalArgumentException iae) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Invalid premove trying next one in queue.",
								iae);
					}
					premoves.remove(move);
				}
			}
		}
		premoves.clear();
		adjustPremoveLabel();
		return false;
	}

	/**
	 * Clears the premove queue and adjusts the premove label and clear premove
	 * button.
	 */
	@Override
	public void onClearPremoves() {
		premoves.clear();
		adjustPremoveLabel();
	}

	@Override
	protected void onPlayGameEndSound() {
		if (isUserWhite() && game.getResult() == Result.WHITE_WON) {
			SoundService.getInstance().playSound("win");
		} else if (!isUserWhite() && game.getResult() == Result.BLACK_WON) {
			SoundService.getInstance().playSound("win");
		} else if (isUserWhite() && game.getResult() == Result.BLACK_WON) {
			SoundService.getInstance().playSound("lose");
		} else if (!isUserWhite() && game.getResult() == Result.WHITE_WON) {
			SoundService.getInstance().playSound("lose");
		} else {
			SoundService.getInstance().playSound("obsGameEnd");
		}
	}

	@Override
	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	@Override
	protected void onPlayMoveSound() {
		if (isPlayingMoveSound) {
			SoundService.getInstance().playSound("move");
		}
	}

	/**
	 * Invoked when the user cancels an initiated move. All squares are
	 * unhighlighted and the board is adjusted back to the game so any pieces
	 * removed in userInitiatedMove will be added back.
	 */
	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (isBeingReparented()) {
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("userCancelledMove " + GameUtils.getSan(fromSquare)
					+ " is drag and drop=" + isDnd);
		}
		adjustForIllegalMove();
	}

	/**
	 * Invoked when the user initiates a move from a square. The square becomes
	 * highlighted and if its a DND move the piece is removed.
	 */
	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userInitiatedMove " + GameUtils.getSan(square)
					+ " is drag and drop=" + isDnd);
		}

		if (!isBeingReparented()) {
			board.unhighlightAllSquares();
			board.getSquare(square).highlight();
			if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
				board.getSquare(square).setPiece(GameConstants.EMPTY);
			}
		}
	}
	
//	/**
//	 * This method doesnt update the clocks but updates the board and the piece jails.
//	 */
//	public void adjustToMoveBeingSentToConnector() {
//		adjustBoardToGame(getGame());
//		adjustPieceJailFromGame(getGame());
//	}

	/**
	 * Invoked when a user makes a dnd move or a click click move on the
	 * chessboard.
	 */
	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (isBeingReparented()) {
			return;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove " + getGame().getId() + " "
					+ GameUtils.getSan(fromSquare) + " "
					+ GameUtils.getSan(toSquare));
		}

		long startTime = System.currentTimeMillis();
		board.unhighlightAllSquares();

		if (isUsersMove()) {
			// Non premoves flow through here
			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a move where from square == to square or toSquar was the piece jail.");
				}

				adjustForIllegalMove();
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing user move..");
			}

			Move move = null;
			if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare,
						board.getAutoPromoteSelection());
			} else {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare);
			}

			if (move == null) {
				adjustForIllegalMove();
			} else {
				board.getSquare(fromSquare).highlight();
				board.getSquare(toSquare).highlight();

				if (game.move(move)) {
					connector.makeMove(game, move);
					isPlayingMoveSound = false;
					adjustToGameMove();
					handleAutoDraw();
					isPlayingMoveSound = true;
				} else {
					connector.onError(
							"Game.move returned false for a move that should have been legal.Move: "
									+ move + ".", new Throwable(getGame()
									.toString()));
					adjustForIllegalMove(move.toString());
				}
			}

		} else if (isPremoveable()) {
			// Premove logic flows through here

			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {
				// No need to check other conditions they are checked in
				// canUserInitiateMoveFrom

				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a premove that failed immediate validation.");
				}

				adjustForIllegalMove();
				return;
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing premove.");
			}

			PremoveInfo premoveInfo = new PremoveInfo();
			premoveInfo.fromSquare = fromSquare;
			premoveInfo.toSquare = toSquare;
			premoveInfo.promotionColorlessPiece = GameUtils.isPromotion(
					isUserWhite(), getGame(), fromSquare, toSquare) ? board
					.getAutoPromoteSelection() : EMPTY;

			/**
			 * In queued premove mode you can have multiple premoves so just add
			 * it to the queue. In non queued premove you can have only one, so
			 * always clear out the queue before adding new moves.
			 */
			if (!getPreferences().getBoolean(
					PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
				premoves.clear();
			}
			premoves.add(premoveInfo);

			adjustPremoveLabel();

			board.unhighlightAllSquares();
			adjustBoardToGame(getGame());

		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove completed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	/**
	 * Middle click is smart move. A move is randomly selected that is'nt a drop
	 * move and played if its enabled.
	 */
	@Override
	public void userMiddleClicked(int square) {
		if (isBeingReparented()) {
			return;
		}

		if (getPreferences().getBoolean(PreferenceKeys.BOARD_SMARTMOVE_ENABLED)) {
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("On middle click " + getGame().getId() + " "
								+ square);
			}
			if (isUsersMove()) {
				Move[] moves = getGame().getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.getTo() == square
							&& (move.getMoveCharacteristic() & Move.DROP_CHARACTERISTIC) != 0) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));
					if (game.move(move)) {
						connector.makeMove(game, move);
					} else {
						throw new IllegalStateException(
								"Game rejected move in smart move. This is a bug.");
					}
					board.unhighlightAllSquares();
					board.getSquare(move.getFrom()).highlight();
					board.getSquare(move.getTo()).highlight();

					// Turn off sound while the move is adjusted
					isPlayingMoveSound = false;
					adjustToGameMove();
					isPlayingMoveSound = true;
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Rejected smart move since its not users move.");
				}
			}
		}
	}

	/**
	 * In droppable games this shows a menu of the pieces available for
	 * dropping. In bughouse the menu includes the premove drop features which
	 * drops a move when the piece becomes available.
	 */
	@Override
	public void userRightClicked(final int square) {
		if (isBeingReparented()) {
			return;
		}

		if (!BoardUtils.isPieceJailSquare(square)
				&& getGame().isInState(Game.DROPPABLE_STATE)) {

			// TO DO:
			// Also add premove drop for bughouse.
			// Menu menu = new Menu(board.getShell(), SWT.POP_UP);
			//
			// if (getGame().getPiece(square) != EMPTY) {
			// MenuItem item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place white pawn on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, PAWN, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place white knight on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, KNIGHT, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place white bishop on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, BISHOP, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place white rook on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, ROOK, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item
			// .setText("Place white queen on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, QUEEN, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place white king on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, KING, WHITE);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place black pawn on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, PAWN, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place black knight on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, KNIGHT, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place black bishop on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, BISHOP, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place black rook on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, ROOK, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item
			// .setText("Place black queen on "
			// + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, QUEEN, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			//
			// item = new MenuItem(menu, SWT.PUSH);
			// item.setText("Place black king on " + GameUtils.getSan(square));
			// item.addListener(SWT.Selection, new Listener() {
			// public void handleEvent(Event e) {
			// Move move = new Move(square, KING, BLACK);
			// adjustToDropMove(move);
			// connector.makeMove(getGame(), move);
			// }
			// });
			// }
			// menu.setLocation(board.getSquare(square).toDisplay(10, 10));
			// menu.setVisible(true);
			// while (!menu.isDisposed() && menu.isVisible()) {
			// if (!board.getDisplay().readAndDispatch())
			// board.getDisplay().sleep();
			// }
			// menu.dispose();
		}
	}

}
