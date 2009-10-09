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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.game.GameConstants;
import raptor.game.Move;
import raptor.game.Result;
import raptor.game.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.BoardUtils;
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
		int fromPiece;
		int fromSquare;
		int promotionColorlessPiece;
		int toPiece;
		int toSquare;
	}

	static final Log LOG = LogFactory.getLog(PlayingController.class);

	protected Connector connector;
	protected boolean isUserWhite;
	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void droppablePiecesChanged(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						refreshBoard();
					}
				});
			}
		}

		@Override
		public void gameInactive(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
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
							// board.clearCoolbar();
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
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							if (isNewMove) {
								handleAutoDraw();
								if (!makePremove()) {
									board.unhighlightAllSquares();
									refresh();
								}
							} else {
								refresh();
							}
							onPlayMoveSound();
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
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							adjustForIllegalMove(move, true);
						} catch (Throwable t) {
							connector.onError("PlayingController.illegalMove",
									t);
						}
					}
				});
			}
		}
	};
	protected int movingPiece;
	protected List<PremoveInfo> premoves = Collections
			.synchronizedList(new ArrayList<PremoveInfo>(10));
	protected Random random = new SecureRandom();
	protected ToolBar toolbar;

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

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(int from, int to, boolean adjustClocks) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		if (!getPreferences().getBoolean(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
			onClearPremoves();
		}

		board.unhighlightAllSquares();
		if (adjustClocks) {
			refresh();
		} else {
			refreshBoard();
		}
		try {
			board.getStatusLabel().setText(
					"Illegal Move: "
							+ GameUtils.getPseudoSan(getGame(), from, to));
		} catch (IllegalArgumentException iae) {
			board.getStatusLabel().setText(
					"Illegal Move: " + GameUtils.getSan(from) + "-"
							+ GameUtils.getSan(to));
		}

		SoundService.getInstance().playSound("illegalMove");
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(String move, boolean adjustClocks) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		if (!getPreferences().getBoolean(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
			onClearPremoves();
		}

		board.unhighlightAllSquares();
		if (adjustClocks) {
			refresh();
		} else {
			refreshBoard();
		}

		board.getStatusLabel().setText("Illegal Move: " + move);
		SoundService.getInstance().playSound("illegalMove");
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText(
				"Playing " + getGame().getEvent());
	}

	/**
	 * Adds all premoves to the premoves label. Also updates the clear premove
	 * button if there are moves in the premove queue.
	 */
	@Override
	protected void adjustPremoveLabel() {
		String labelText = "Premoves: ";
		synchronized (premoves) {
			boolean hasAddedPremove = false;
			for (PremoveInfo info : premoves) {
				String premove = ""
						+ GameUtils.getPseudoSan(info.fromPiece, info.toPiece,
								info.fromSquare, info.toSquare);
				if (!hasAddedPremove) {
					labelText += premove;
				} else {
					labelText += " , " + premove;
				}
				hasAddedPremove = true;
			}
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
					return isUserWhite
							&& BoardUtils.isJailSquareWhitePiece(squareId)
							|| !isUserWhite
							&& BoardUtils.isJailSquareBlackPiece(squareId);
				} else {
					return isUserWhite
							&& GameUtils.isWhitePiece(getGame(), squareId)
							|| !isUserWhite
							&& GameUtils.isBlackPiece(game, squareId);
				}
			}
			return false;
		} else if (BoardUtils.isPieceJailSquare(squareId)
				&& !getGame().isInState(Game.DROPPABLE_STATE)) {
			return false;
		} else if (getGame().isInState(Game.DROPPABLE_STATE)
				&& BoardUtils.isPieceJailSquare(squareId)) {
			return isUserWhite && BoardUtils.isJailSquareWhitePiece(squareId)
					|| !isUserWhite
					&& BoardUtils.isJailSquareBlackPiece(squareId);
		} else {
			return isUserWhite && GameUtils.isWhitePiece(getGame(), squareId)
					|| !isUserWhite && GameUtils.isBlackPiece(game, squareId);
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
		if (toolbar != null) {
			toolbar.setVisible(false);
			SWTUtils.clearToolbar(toolbar);
			toolbar = null;
		}
	}

	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Play " + getGame().getId() + ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			BoardUtils.addPromotionIconsToToolbar(this, toolbar, isUserWhite);
			new ToolItem(toolbar, SWT.SEPARATOR);
			BoardUtils.addPremoveClearAndAutoDrawToolbar(this, toolbar);
			new ToolItem(toolbar, SWT.SEPARATOR);
			BoardUtils.addNavIconsToToolbar(this, toolbar, false, false);
			new ToolItem(toolbar, SWT.SEPARATOR);
		} else if (toolbar.getParent() != parent) {
			toolbar.setParent(parent);
		}
		setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
		return toolbar;
	}

	/**
	 * If auto draw is enabled, a draw request is sent. This method should be
	 * invoked when receiving a move and right after sending one.
	 * 
	 * In the future this will become smarter and only draw when the game shows
	 * a draw by three times in the same position or 50 move draw rule.
	 */
	protected void handleAutoDraw() {
		if (isToolItemSelected(ToolBarItemKey.AUTO_DRAW)) {
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
		refresh();
		onPlayGameStartSound();

		// Since the game object is updated while the game is being created,
		// there
		// is no risk of missing game events. We will pick them up when we get
		// the position
		// of the game since it will always be udpated.
		connector.getGameService().addGameServiceListener(listener);
	}

	/**
	 * Returns true if the premove preference is enabled.
	 */
	public boolean isPremoveable() {
		return Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_PREMOVE_ENABLED);
	}

	public boolean isUsersMove() {
		return isUserWhite() && game.isWhitesMove() || !isUserWhite()
				&& !game.isWhitesMove();
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
		boolean result = false;
		synchronized (premoves) {
			List<PremoveInfo> premovesToRemove = new ArrayList<PremoveInfo>(
					premoves.size());
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
					premovesToRemove.add(info);
					handleAutoDraw();
					result = true;
					break;
				} catch (IllegalArgumentException iae) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Invalid premove trying next one in queue.",
								iae);
					}
					premovesToRemove.add(info);
				}
			}
			if (!result) {
				premoves.clear();
			} else {
				premoves.removeAll(premovesToRemove);
			}
		}
		if (result) {
			board.unhighlightAllSquares();
			refresh();
		} else {
			adjustPremoveLabel();
		}
		return result;
	}

	public void onClearPremoves() {
		premoves.clear();
		adjustPremoveLabel();
	}

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

	protected void onPlayGameStartSound() {
		SoundService.getInstance().playSound("gameStart");
	}

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	@Override
	public void onToolbarButtonAction(ToolBarItemKey key, String... args) {
		switch (key) {
		case FEN:
			Raptor.getInstance().promptForText(
					"FEN for game " + game.getWhiteName() + " vs "
							+ game.getBlackName(), game.toFEN());
			break;
		case FLIP:
			onFlip();
			break;
		case CLEAR_PREMOVES:
			onClearPremoves();
		}
	}

	/**
	 * Invoked when the user cancels an initiated move. All squares are
	 * unhighlighted and the board is adjusted back to the game so any pieces
	 * removed in userInitiatedMove will be added back.
	 */
	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (isDisposed()) {
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("userCancelledMove " + GameUtils.getSan(fromSquare)
					+ " is drag and drop=" + isDnd);
		}
		board.unhighlightAllSquares();
		movingPiece = EMPTY;
		refresh();
		onPlayIllegalMoveSound();
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

		if (!isDisposed() && board.getSquare(square).getPiece() != EMPTY) {
			board.unhighlightAllSquares();
			board.getSquare(square).highlight();
			movingPiece = board.getSquare(square).getPiece();
			if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
				board.getSquare(square).setPiece(GameConstants.EMPTY);
			}
			board.redrawSquares();
		}
	}

	/**
	 * Invoked when a user makes a dnd move or a click click move on the
	 * chessboard.
	 */
	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (isDisposed()) {
			return;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove " + getGame().getId() + " "
					+ GameUtils.getSan(fromSquare) + " "
					+ GameUtils.getSan(toSquare));
		}

		if (movingPiece == 0) {
			LOG.error("movingPiece is 0 this should never happen.",
					new Exception());
			return;
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
				adjustForIllegalMove(fromSquare, toSquare, false);
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing user move..");
			}

			Move move = null;
			if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare,
						getAutoPromoteSelection());
			} else {
				move = BoardUtils.createMove(getGame(), fromSquare, toSquare);
			}

			if (move == null) {
				adjustForIllegalMove(fromSquare, toSquare, false);
			} else {
				board.getSquare(fromSquare).highlight();
				board.getSquare(toSquare).highlight();

				if (game.move(move)) {
					board.getSquare(fromSquare).setPiece(movingPiece);
					refreshForMove(move);
					connector.makeMove(game, move);
				} else {
					connector.onError(
							"Game.move returned false for a move that should have been legal.Move: "
									+ move + ".", new Throwable(getGame()
									.toString()));
					adjustForIllegalMove(move.toString(), false);
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

				adjustForIllegalMove(fromSquare, toSquare, false);
				return;
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing premove.");
			}

			PremoveInfo premoveInfo = new PremoveInfo();
			premoveInfo.fromSquare = fromSquare;
			premoveInfo.toSquare = toSquare;
			premoveInfo.fromPiece = movingPiece;
			premoveInfo.toPiece = board.getSquare(toSquare).getPiece();
			premoveInfo.promotionColorlessPiece = GameUtils.isPromotion(
					isUserWhite(), getGame(), fromSquare, toSquare) ? getAutoPromoteSelection()
					: EMPTY;

			board.getSquare(fromSquare).setPiece(movingPiece);

			/**
			 * In queued premove mode you can have multiple premoves so just add
			 * it to the queue. In non queued premove you can have only one, so
			 * always clear out the queue before adding new moves.
			 */
			if (!getPreferences().getBoolean(
					PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
				premoves.clear();
				premoves.add(premoveInfo);

				adjustPremoveLabel();
				board.unhighlightAllSquares();
				board.getSquare(premoveInfo.fromSquare).highlight();
				board.getSquare(premoveInfo.toSquare).highlight();
				refreshBoard();
			} else {
				premoves.add(premoveInfo);
				board.getSquare(premoveInfo.fromSquare).highlight();
				board.getSquare(premoveInfo.toSquare).highlight();
				adjustPremoveLabel();
				refreshBoard();
			}
		}

		// Clear the moving piece.
		movingPiece = EMPTY;

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
		if (isDisposed()) {
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
					refreshForMove(move);
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
		if (isDisposed()) {
			return;
		}

		if (!BoardUtils.isPieceJailSquare(square)
				&& getGame().isInState(Game.DROPPABLE_STATE)) {
		}
	}

}
