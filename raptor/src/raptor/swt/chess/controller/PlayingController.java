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

public class PlayingController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(PlayingController.class);
	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(Game game) {
			if (!isBeingReparented() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {

							board.redrawSquares();

							InactiveController inactiveController = new InactiveController(
									getGame());
							getBoard().setController(inactiveController);
							inactiveController.setBoard(board);
							inactiveController
							.setItemChangedListeners(itemChangedListeners);
							connector.getGameService()
									.removeGameServiceListener(listener);
							board.clearCoolbar();
							inactiveController.init();
							inactiveController.fireItemChanged();
							// Set the listeners to null so they wont get
							// cleared and disposed
							setItemChangedListeners(null);
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

	protected static class PremoveInfo {
		int fromSquare;
		int toSquare;
		int promotionColorlessPiece;
	}

	protected Connector connector;
	protected boolean isUserWhite;
	protected Random random = new SecureRandom();
	protected boolean isPlayingMoveSound = true;
	protected List<PremoveInfo> premoves = Collections
			.synchronizedList(new ArrayList<PremoveInfo>(10));

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
	public void onClearPremoves() {
		premoves.clear();
		adjustPremoveLabel();
	}

	/**
	 * Returns true if a premove was made.
	 * 
	 * @return
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
					adjustToGameMove();
					return true;
				} catch (IllegalArgumentException iae) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Invalid premove trying next one in queue.",
								iae);
					}
				}
			}
		}
		premoves.clear();
		adjustPremoveLabel();
		return false;
	}

	public boolean isPremoveable() {
		return Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_PREMOVE_ENABLED);
	}

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
			board.addScripterCoolbar();
			board.packCoolbar();
		}
	}

	public void adjustForIllegalMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove " + GameUtils.getSan(fromSquare)
					+ " " + GameUtils.getSan(toSquare));
		}
		board.unhighlightAllSquares();
		adjustToGameMove();
		SoundService.getInstance().playSound("illegalMove");
	}

	public void adjustForIllegalMove(String move) {
		if (!isBeingReparented()) {
			if (LOG.isDebugEnabled()) {
				LOG.info("adjustForIllegalMove " + getGame().getId() + " ...");
			}

			long startTime = System.currentTimeMillis();
			SoundService.getInstance().playSound("illegalMove");
			board.getStatusLabel().setText("Illegal Move: " + move);
			board.unhighlightAllSquares();

			if (LOG.isDebugEnabled()) {
				LOG.info("adjustForIllegalMove in " + getGame().getId() + "  "
						+ (System.currentTimeMillis() - startTime));
			}
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
	public void dispose() {
		connector.getGameService().removeGameServiceListener(listener);
		super.dispose();
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return "Playing(" + getGame().getId() + ")";
	}

	@Override
	public void init() {
		board.setWhiteOnTop(!isUserWhite());
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
	public boolean isAbortable() {
		return true;
	}

	@Override
	public boolean isAdjournable() {
		return true;
	}

	@Override
	public boolean isAutoDrawable() {
		return true;
	}

	@Override
	public boolean isCloseable() {
		return false;
	}

	@Override
	public boolean isCommitable() {
		return false;
	}

	@Override
	public boolean isDrawable() {
		return true;
	}

	@Override
	public boolean isExaminable() {
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

	@Override
	public boolean isPausable() {
		return true;
	}

	@Override
	public boolean isRematchable() {
		return true;
	}

	@Override
	public boolean isResignable() {
		return true;
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

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (!isBeingReparented()) {
			board.unhighlightAllSquares();
			adjustToGameMove();
		}
	}

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
			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {

				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a move where from square == to square or toSquar was the piece jail.");
				}

				adjustForIllegalMove(fromSquare, toSquare);
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
				adjustForIllegalMove(fromSquare, toSquare);
			} else {
				board.getSquare(fromSquare).highlight();
				board.getSquare(toSquare).highlight();

				if (game.move(move)) {
					connector.makeMove(game, move);
					isPlayingMoveSound = false;
					adjustToGameMove();
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
			if (fromSquare == toSquare
					|| BoardUtils.isPieceJailSquare(toSquare)) {
				// No need to check other conditions they are checked in
				// canUserInitiateMoveFrom

				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a premove that failed immediate validation.");
				}

				adjustForIllegalMove(fromSquare, toSquare);
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
