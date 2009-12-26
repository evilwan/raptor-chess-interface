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

import static raptor.chess.util.GameUtils.getColoredPiece;
import static raptor.chess.util.GameUtils.getColoredPieceFromDropSquare;
import static raptor.chess.util.GameUtils.getDropSquareFromColoredPiece;
import static raptor.chess.util.GameUtils.getPieceRepresentation;
import static raptor.chess.util.GameUtils.getPseudoSan;
import static raptor.chess.util.GameUtils.getSan;
import static raptor.chess.util.GameUtils.getUncoloredPiece;
import static raptor.chess.util.GameUtils.isBlackPiece;
import static raptor.chess.util.GameUtils.isPromotion;
import static raptor.chess.util.GameUtils.isWhitePiece;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.Result;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.PlayingStatisticsService;
import raptor.service.SoundService;
import raptor.service.ThreadService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.Arrow;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.ClockLabelUpdater;
import raptor.swt.chess.Highlight;
import raptor.swt.chess.MouseButtonAction;
import raptor.util.RaptorRunnable;
import raptor.util.RaptorStringUtils;

/**
 * The controller used when a user is playing a game. Supports premove,queued
 * premove, and auto draw. game.getWhiteName() or game.getBlackName() must match
 * connector.getUserName().
 * 
 * When a game is no longer active, this controller swaps itself out with the
 * Inactive controller.
 * 
 * This controller supports premove,queued premove, and premove drop.
 * 
 * You can also right click to bring up a menu of available pieces to drop.
 */
public class PlayingController extends ChessBoardController {
	static final Log LOG = LogFactory.getLog(PlayingController.class);

	/**
	 * A class containing the details of a premove.
	 */
	protected static class PremoveInfo {
		int fromPiece;
		int fromSquare;
		int promotionColorlessPiece;
		int toPiece;
		int toSquare;
		boolean isPremoveDrop = false;
	}

	protected boolean isUserWhite;
	protected GameCursor cursor = null;
	protected MouseListener clearPremovesLabelListener = new MouseListener() {
		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			if (e.button == 1) {
				onClearPremoves();
			}
		}

		public void mouseUp(MouseEvent e) {
		}
	};

	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void droppablePiecesChanged(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {

								if (isDisposed()) {
									return;
								}

								if (!handlePremoveDrop()) {
									refreshBoard();
								}

							}
						});
			}
		}

		@Override
		public void gameInactive(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {

								if (isDisposed()) {
									return;
								}

								board.getResultDecorator()
										.setDecorationFromResult(
												getGame().getResult());
								board.redrawSquares();

								if (!handleSpeakResults(game)) {
									onPlayGameEndSound();
								}

								handleGameStatistics();
								ThreadService.getInstance().run(new Runnable() {
									public void run() {
										ChessBoardUtils
												.prependGameToPgnFile(getGame());
									}
								});

								// Now swap controllers to the inactive
								// controller.
								InactiveController inactiveController = new InactiveController(
										game, getConnector());
								getBoard().setController(inactiveController);
								inactiveController.setBoard(board);
								inactiveController
										.setItemChangedListeners(itemChangedListeners);

								// Detatch from the GameService.
								connector.getGameService()
										.removeGameServiceListener(listener);

								// Clear the cool bar and init the inactive
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								inactiveController.init();

								// Set the listeners to null so they wont get
								// cleared and we wont get notified.
								setItemChangedListeners(null);
								// And finally dispose.
								PlayingController.this.dispose();
							}
						});
			}
		}

		@Override
		public void gameStateChanged(final Game game, final boolean isNewMove) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								if (LOG.isDebugEnabled()) {
									LOG.debug("In gameStateChanged "
											+ getGame().getId() + " "
											+ isNewMove);
								}

								if (isNewMove) {
									handleAutoDraw();
									if (!wasLastMovePremove) {
										removeAllMoveDecorations();
									}
									handleAnnounceCheck();
									if (!handlePremove()) {
										if (LOG.isDebugEnabled()) {
											LOG
													.debug("In did not make premove block "
															+ getGame().getId()
															+ " " + isNewMove);
										}

										boolean wasUserMove = !isUsersMove();
										addDecorationsForMove(getGame()
												.getLastMove(), wasUserMove);

										if (LOG.isDebugEnabled()) {
											LOG.debug("Invoking refresh "
													+ wasUserMove);
										}

										refresh();
										if (!handleSpeakMove(game.getLastMove())) {
											onPlayMoveSound();
										}
									} else {
										if (LOG.isDebugEnabled()) {
											LOG
													.debug("Premove was made. Playing move sound. ");
										}
										if (!handleSpeakMove(game.getLastMove())) {
											onPlayMoveSound();
										}
									}
								} else {
									if (LOG.isDebugEnabled()) {
										LOG
												.debug("This isnt an update from a move, doing a full refresh.");
									}

									addDecorationsForLastMoveListMove();
									refresh();
								}
							}
						});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								adjustForIllegalMove(move, true);
							}
						});
			}
		}
	};

	protected List<PremoveInfo> premoves = Collections
			.synchronizedList(new ArrayList<PremoveInfo>(10));
	protected Random random = new SecureRandom();
	protected ToolBar toolbar;
	protected boolean wasLastMovePremove = false;

	/**
	 * Creates a playing controller. One of the players white or black playing
	 * the game must match the name of connector.getUserName.
	 * 
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * @param game
	 *            The game to control.
	 * @param connector
	 *            The backing connector.
	 */
	public PlayingController(Game game, Connector connector) {
		super(new GameCursor(game,
				GameCursor.Mode.MakeMovesOnMasterSetCursorToLast), connector);
		cursor = (GameCursor) getGame();

		if (StringUtils.equalsIgnoreCase(game.getHeader(PgnHeader.White),
				connector.getUserName())) {
			isUserWhite = true;
		} else if (StringUtils.equalsIgnoreCase(
				game.getHeader(PgnHeader.Black), connector.getUserName())) {
			isUserWhite = false;
		} else {
			throw new IllegalArgumentException(
					"Could not deterimne user color " + connector.getUserName()
							+ " " + game.getHeader(PgnHeader.White) + " "
							+ game.getHeader(PgnHeader.Black));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("isUserWhite=" + isUserWhite);
		}
	}

	protected boolean handleSpeakResults(Game game) {
		boolean result = false;
		if (SoundService.getInstance().isSpeechSetup()
				&& getPreferences().getBoolean(
						PreferenceKeys.BOARD_SPEAK_RESULTS)) {
			speakResults(game);
			result = true;
		}
		return result;
	}

	protected boolean isUserMove(Move move) {
		return (isUserWhite && move.isWhitesMove())
				|| (!isUserWhite && !move.isWhitesMove());
	}

	protected boolean handleSpeakMove(Move move) {
		boolean result = false;
		if (SoundService.getInstance().isSpeechSetup()
				&& ((isUserMove(move) && getPreferences().getBoolean(
						PreferenceKeys.BOARD_SPEAK_MOVES_I_MAKE)) || (!isUserMove(move) && getPreferences()
						.getBoolean(PreferenceKeys.BOARD_SPEAK_MOVES_OPP_MAKES)))) {
			speakMove(move);
			result = true;
		}
		return result;
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(boolean adjustClocks) {
		adjustForIllegalMove(null, adjustClocks);
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(int from, int to, boolean adjustClocks) {
		try {
			adjustForIllegalMove("Illegal Move: "
					+ getPseudoSan(getGame(), from, to), adjustClocks);
		} catch (IllegalArgumentException iae) {
			adjustForIllegalMove("Illegal Move: "
					+ getPseudoSan(getGame(), from, to), adjustClocks);
		}
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(String statusText, boolean adjustClocks) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		if (!getPreferences().getBoolean(
				PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
			onClearPremoves();
		}

		board.unhidePieces();
		if (adjustClocks) {
			refresh();
		} else {
			refreshBoard();
		}

		if (StringUtils.isNotBlank(statusText)) {
			board.getStatusLabel().setText(statusText);
		}

		SoundService.getInstance().playSound("illegalMove");
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText(
				"Playing " + getGame().getHeader(PgnHeader.Event));
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
			LOG.debug("canUserInitiateMoveFrom " + getSan(squareId));
		}
		if (!isUsersMove()) {
			if (isPremoveable()) {
				if (getGame().isInState(Game.DROPPABLE_STATE)
						&& ChessBoardUtils.isPieceJailSquare(squareId)) {
					return isUserWhite
							&& ChessBoardUtils.isJailSquareWhitePiece(squareId)
							|| !isUserWhite
							&& ChessBoardUtils.isJailSquareBlackPiece(squareId);
				} else {
					return isUserWhite && isWhitePiece(getGame(), squareId)
							|| !isUserWhite && isBlackPiece(game, squareId);
				}
			}
			return false;
		} else if (ChessBoardUtils.isPieceJailSquare(squareId)
				&& !getGame().isInState(Game.DROPPABLE_STATE)) {
			return false;
		} else if (getGame().isInState(Game.DROPPABLE_STATE)
				&& ChessBoardUtils.isPieceJailSquare(squareId)) {
			return isUserWhite
					&& ChessBoardUtils.isJailSquareWhitePiece(squareId)
					|| !isUserWhite
					&& ChessBoardUtils.isJailSquareBlackPiece(squareId);
		} else {
			return isUserWhite && isWhitePiece(getGame(), squareId)
					|| !isUserWhite && isBlackPiece(game, squareId);
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
		try {
			connector.getGameService().removeGameServiceListener(listener);
			board.getCurrentPremovesLabel().removeMouseListener(
					clearPremovesLabelListener);
			board.getCurrentPremovesLabel().setImage(null);
			board.getCurrentPremovesLabel().setToolTipText("");

			super.dispose();
			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.setVisible(false);
				SWTUtils.clearToolbar(toolbar);
				toolbar = null;
			}
		} catch (Throwable t) {
		} // Eat it its prob a disposed exception
	}

	public void enableDisableNavButtons() {
		setToolItemEnabled(ToolBarItemKey.NEXT_NAV, cursor.hasNext());
		setToolItemEnabled(ToolBarItemKey.BACK_NAV, cursor.hasPrevious());
		setToolItemEnabled(ToolBarItemKey.FIRST_NAV, cursor.hasFirst());
		setToolItemEnabled(ToolBarItemKey.LAST_NAV, cursor.hasLast());
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Play " + getGame().getId() + ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);

		if (toolbar == null) {
			toolbar = new ToolBar(isCoolbarMode ? getBoard().getCoolbar()
					: parent, SWT.FLAT);
			toolbar.setLayout(SWTUtils
					.createMarginlessRowLayout(SWT.HORIZONTAL));
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.PlayingChessBoard, toolbar,
					isUserWhite());

			if (game.getVariant() == Variant.suicide) {
				setToolItemSelected(ToolBarItemKey.AUTO_KING, true);
			} else {
				setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
			}
			enableDisableNavButtons();

			if (isCoolbarMode) {
				ChessBoardUtils.adjustCoolbar(getBoard(), toolbar);
			}
		} else {
			if (!isCoolbarMode) {
				toolbar.setParent(parent);
			}
		}

		if (isCoolbarMode) {
			return null;
		} else {
			return toolbar;
		}
	}

	@Override
	public void init() {
		board.getCurrentPremovesLabel().addMouseListener(
				clearPremovesLabelListener);
		board.getCurrentPremovesLabel().setToolTipText(
				"Lists your current premoves. Click to clear premoves");

		board.setWhiteOnTop(RaptorStringUtils.getBooleanValue(game
				.getHeader(PgnHeader.WhiteOnTop)));

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

		if (getPreferences().getBoolean(PreferenceKeys.BOARD_COOLBAR_MODE)) {
			getToolbar(null);
		}

		refresh();
		onPlayGameStartSound();

		// Since the game object is updated while the game is being created,
		// there
		// is no risk of missing game events. We will pick them up when we get
		// the position
		// of the game since it will always be udpated.
		connector.getGameService().addGameServiceListener(listener);
		fireItemChanged();
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

	@Override
	public void onAutoDraw() {
		ThreadService.getInstance().run(new Runnable() {
			public void run() {
				getConnector().onDraw(getGame());
			}
		});
	}

	@Override
	public void onBack() {
		cursor.setCursorPrevious();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	public void onClearPremoves() {
		premoves.clear();
		removeAllMoveDecorations();
		adjustPremoveLabelHighlightsAndArrows();
		board.redrawSquares();
	}

	@Override
	public void onFirst() {
		cursor.setCursorFirst();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onForward() {
		cursor.setCursorNext();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onLast() {
		cursor.setCursorMasterLast();
		refresh(false);
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void refresh() {
		if (isDisposed()) {
			return;
		}
		adjustTimeUpLabel();
		board.getMoveList().updateToGame();
		board.getMoveList().select(getGame().getMoveList().getSize());
		enableDisableNavButtons();
		super.refresh();
	}

	/**
	 * Invoked when the user cancels an initiated move. All squares are
	 * unhighlighted and the board is adjusted back to the game so any pieces
	 * removed in userInitiatedMove will be added back.
	 */
	@Override
	public void userCancelledMove(int fromSquare) {
		if (isDisposed()) {
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("userCancelledMove " + getSan(fromSquare));
		}
		adjustForIllegalMove(false);
	}

	/**
	 * Invoked when the user initiates a move from a square. The square becomes
	 * highlighted and if its a DND move the piece is removed.
	 */
	@Override
	public void userInitiatedMove(int square) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userInitiatedMove " + getSan(square));
		}

		if (!isDisposed() && board.getSquare(square).getPiece() != EMPTY) {
			board.getSquare(square).setHidingPiece(true);
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
					+ getSan(fromSquare) + " " + getSan(toSquare));
		}
		board.unhidePieces();

		long startTime = System.currentTimeMillis();

		if (isUsersMove()) {
			if (fromSquare == toSquare) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a move where from square == to square.");
				}
				refreshBoard();
				return;
			} else if (ChessBoardUtils.isPieceJailSquare(toSquare)) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a move where from square == to square or toSquar was the piece jail.");
				}
				adjustForIllegalMove(fromSquare, toSquare, false);
				return;
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing user move..");
			}

			Move move = null;
			if (isPromotion(getGame(), fromSquare, toSquare)) {
				move = ChessBoardUtils.createMove(getGame(), fromSquare,
						toSquare, getAutoPromoteSelection());
			} else {
				move = ChessBoardUtils.createMove(getGame(), fromSquare,
						toSquare);
			}

			if (move == null) {
				adjustForIllegalMove(fromSquare, toSquare, false);
			} else {
				if (game.move(move)) {
					game.rollback();
					final Move finalMove = move;
					ThreadService.getInstance().run(new Runnable() {
						public void run() {
							connector.makeMove(game, finalMove);
						}
					});
					board.unhidePieces();
					refreshForMove(move);
				} else {
					connector.onError(
							"Game.move returned false for a move that should have been legal.Move: "
									+ move + ".", new Throwable(getGame()
									.toString()));
					adjustForIllegalMove("Illegal Move: " + move.toString(),
							false);
				}
			}

		} else if (isPremoveable()) {
			// Premove logic flows through here
			if (fromSquare == toSquare) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("User tried to make a premove with fromSquare == toSquare.");
				}
				refreshBoard();
				return;
			} else if (ChessBoardUtils.isPieceJailSquare(toSquare)) {
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
			premoveInfo.fromPiece = board.getSquare(fromSquare).getPiece();
			premoveInfo.toPiece = board.getSquare(toSquare).getPiece();
			premoveInfo.promotionColorlessPiece = isPromotion(isUserWhite(),
					getGame(), fromSquare, toSquare) ? getAutoPromoteSelection()
					: EMPTY;

			/**
			 * In queued premove mode you can have multiple premoves so just add
			 * it to the queue. In non queued premove you can have only one, so
			 * always clear out the queue before adding new moves.
			 */
			if (!getPreferences().getBoolean(
					PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED)) {
				premoves.clear();
				premoves.add(premoveInfo);

				removeAllMoveDecorations();
				adjustPremoveLabelHighlightsAndArrows();

				board.unhidePieces();

				refreshBoard();
			} else {
				premoves.add(premoveInfo);

				board.unhidePieces();
				adjustPremoveLabelHighlightsAndArrows();
				refreshBoard();
			}
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove completed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
	}

	@Override
	public void userMouseWheeled(int count) {
		if (getPreferences().getBoolean(
				BOARD_ALLOW_MOUSE_WHEEL_NAVIGATION_WHEEL_PLAYING)) {
			if (count > 0) {
				onForward();
			} else {
				onBack();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void userPressedMouseButton(MouseButtonAction button, int square) {
		PlayingMouseAction action = PlayingMouseAction.valueOf(getPreferences()
				.getString(PLAYING_CONTROLLER + button.getPreferenceSuffix()));

		if (action == null) {
			Raptor.getInstance().onError(
					"PlayingMouseAction was null. This should never happn. "
							+ button.toString() + " " + square);
			return;
		}

		switch (action) {
		case None:
			break;
		case OfferDraw:
			onOfferDraw();
			break;
		case PopupMenu:
			onPopup(square);
			break;
		case RandomCapture:
			onRandomCapture();
			break;
		case SmartMove:
			onSmartMove(square);
			break;
		case RandomMove:
			onRandomMove();
			break;
		case RandomRecapture:
			onRandomRecapture();
			break;
		case ClearPremoves:
			onClearPremoves();
			break;
		}
	}

	/**
	 * Adds all premoves to the premoves label. Also updates the clear premove
	 * button if there are moves in the premove queue. Also adds arrows and
	 * highlights of premoves.
	 * 
	 * 
	 * Premove arrows are always permanent
	 */
	@Override
	protected void adjustPremoveLabelHighlightsAndArrows() {
		String labelText = "Premoves: ";
		synchronized (premoves) {
			boolean hasAddedPremove = false;
			for (PremoveInfo info : premoves) {
				String premove = (info.isPremoveDrop ? "{" : "")
						+ getPseudoSan(info.fromPiece, info.toPiece,
								info.fromSquare, info.toSquare)
						+ (info.isPremoveDrop ? "}" : "");
				if (!hasAddedPremove) {
					labelText += premove;
				} else {
					labelText += " , " + premove;
				}
				hasAddedPremove = true;

				if (getPreferences().getBoolean(HIGHLIGHT_SHOW_ON_MY_PREMOVES)) {
					Highlight highlight = new Highlight(info.fromSquare,
							info.toSquare, getPreferences().getColor(
									HIGHLIGHT_PREMOVE_COLOR), false);
					if (!board.getSquareHighlighter().containsHighlight(
							highlight)) {
						board.getSquareHighlighter().addHighlight(highlight);
					}
				}
				if (getPreferences().getBoolean(ARROW_SHOW_ON_MY_PREMOVES)) {
					Arrow arrow = new Arrow(info.fromSquare, info.toSquare,
							getPreferences().getColor(ARROW_PREMOVE_COLOR),
							false);
					if (!board.getArrowDecorator().containsArrow(arrow)) {
						board.getArrowDecorator().addArrow(arrow);
					}
				}
			}

			board.getCurrentPremovesLabel().setImage(
					hasAddedPremove ? Raptor.getInstance().getIcon("redx")
							: null);
			setToolItemEnabled(ToolBarItemKey.CLEAR_PREMOVES, hasAddedPremove);
			board.getCurrentPremovesLabel().setText(labelText);
		}
	}

	protected void handleAnnounceCheck() {
		if (game.getVariant() != Variant.suicide && game.isInCheck()
				&& !game.isCheckmate()) {
			if (isUserWhite && game.isWhitesMove() || !isUserWhite()
					&& !game.isWhitesMove()) {
				if (getPreferences().getBoolean(
						BOARD_ANNOUNCE_CHECK_WHEN_OPPONENT_CHECKS_ME)) {
					SoundService.getInstance().playSound("check");
				}
			} else if (isUserWhite && !game.isWhitesMove() || !isUserWhite()
					&& game.isWhitesMove()) {
				if (getPreferences().getBoolean(
						BOARD_ANNOUNCE_CHECK_WHEN_I_CHECK_OPPONENT)) {
					SoundService.getInstance().playSound("check");
				}
			}
		}
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
			onAutoDraw();
		}
	}

	protected void handleGameStatistics() {
		if (getPreferences().getBoolean(
				PreferenceKeys.BOARD_SHOW_PLAYING_GAME_STATS_ON_GAME_END)) {
			PlayingStatisticsService.getInstance().addStatisticsForGameEnd(
					connector, game, isUserWhite);
			String message = PlayingStatisticsService.getInstance()
					.getStatisticsString(connector, game, isUserWhite);
			if (StringUtils.isNotEmpty(message)) {
				getConnector().publishEvent(
						new ChatEvent(null, ChatType.PLAYING_STATISTICS,
								message));
			}
		}
	}

	/**
	 * Runs through the premove queue and tries to make each move. If a move
	 * succeeds it is made and the rest of the queue is left intact. If a move
	 * fails it is removed from the queue and the next move is tried.
	 * 
	 * If a move succeeded true is returned, otherwise false is returned.
	 */
	protected boolean handlePremove() {
		if (!isUsersMove()) {
			return false;
		}

		// Remove all arrows and highlights, they will be added back in this
		// method and after its invoked if it returns false.
		removeAllMoveDecorations();

		boolean result = false;
		Move moveMade = null;
		Move moveBeforePremove = getGame().getLastMove();
		synchronized (premoves) {
			List<PremoveInfo> premovesToRemove = new ArrayList<PremoveInfo>(
					premoves.size());
			for (PremoveInfo info : premoves) {
				Move move = null;
				try {
					if (info.isPremoveDrop) {
						premovesToRemove.add(info);
						continue;
					} else if (info.promotionColorlessPiece == EMPTY) {
						move = game.makeMove(info.fromSquare, info.toSquare);
					} else {
						move = game.makeMove(info.fromSquare, info.toSquare,
								info.promotionColorlessPiece);
					}
					game.rollback();
					final Move finalMove = move;
					ThreadService.getInstance().run(new Runnable() {
						public void run() {
							connector.makeMove(game, finalMove);
						}
					});

					// Grabs the last move.
					refresh();
					// Adjusts for the new move
					refreshForMove(move);
					premovesToRemove.add(info);
					// Handles auto draw if its pressed.
					handleAutoDraw();
					result = true;
					moveMade = move;

					// They will all be added back at the end.
					removeAllMoveDecorations();

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
			if (moveBeforePremove != null) {
				// add highlights/arrows for last move.
				addDecorationsForMove(moveBeforePremove, false);
			}
			if (moveMade != null) {
				// add highlights/arrows for this move.
				addDecorationsForMove(moveMade, true);

				// remove all premoves that have the same start square as this
				// move.
				for (int i = 0; i < premoves.size(); i++) {
					if (premoves.get(i).fromSquare == moveMade.getFrom()) {
						premoves.remove(i);
						i--;
					}
				}
			}
		}

		adjustPremoveLabelHighlightsAndArrows();

		wasLastMovePremove = result;
		return result;
	}

	/**
	 * Handles premove drop. THis should only be called on position updates when
	 * its the users move.
	 */
	protected boolean handlePremoveDrop() {
		if (!isUsersMove() || !isBughouse()) {
			return false;
		}

		boolean result = false;

		Move moveMade = null;
		Move moveBeforePremove = getGame().getLastMove();
		List<PremoveInfo> premovesToRemove = new ArrayList<PremoveInfo>(
				premoves.size());
		for (PremoveInfo info : premoves) {
			Move move = null;
			if (!info.isPremoveDrop) {
				continue;
			} else if (getGame()
					.getDropCount(
							isUserWhite() ? WHITE : BLACK,
							getUncoloredPiece(getColoredPieceFromDropSquare(info.fromSquare))) > 0) {
				try {
					move = game.makeMove(info.fromSquare, info.toSquare);
					game.rollback();
					final Move finalMove = move;
					ThreadService.getInstance().run(new Runnable() {
						public void run() {
							connector.makeMove(game, finalMove);
						}
					});

					// Clear everything a move was made.
					removeAllMoveDecorations();

					premovesToRemove.add(info);
					// Grabs the last move.
					refresh();
					// Adjusts for the new move
					refreshForMove(move);
					// Handles auto draw if its pressed.
					handleAutoDraw();
					moveMade = move;
					result = true;

					break;
				} catch (IllegalArgumentException iae) {
					getConnector()
							.onError(
									"Couldnt make a premove drop. This should never happen.",
									iae);
					premovesToRemove.add(info);
				}
			}
		}
		premoves.removeAll(premovesToRemove);
		if (result) {
			if (moveBeforePremove != null) {
				// add highlights/arrows for last move.
				addDecorationsForMove(moveBeforePremove, false);
			}
			if (moveMade != null) {
				// add highlights/arrows for this move.
				addDecorationsForMove(moveMade, true);
			}
		}
		adjustPremoveLabelHighlightsAndArrows();
		return result;
	}

	@Override
	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			boolean speakCountdown = getPreferences().getBoolean(
					BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS);
			whiteClockUpdater = new ClockLabelUpdater(true, this,
					speakCountdown && isUserWhite());
			blackClockUpdater = new ClockLabelUpdater(false, this,
					speakCountdown && !isUserWhite());
		}
	}

	protected boolean isBughouse() {
		return getGame().getVariant() == Variant.bughouse
				|| getGame().getVariant() == Variant.fischerRandomBughouse;
	}

	protected void onOfferDraw() {
		getConnector().onDraw(getGame());
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

	protected void onPopup(final int square) {
		if (isDisposed() || !getGame().isInState(Game.DROPPABLE_STATE)) {
			return;
		}

		if (!ChessBoardUtils.isPieceJailSquare(square)
				&& getGame().getPiece(square) == EMPTY) {
			final int color = isUserWhite() ? WHITE : BLACK;
			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			if (isBughouse() && isUsersMove()) {
				if (getGame().getDropCount(color, PAWN) == 0) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Premove drop "
							+ getPieceRepresentation(getColoredPiece(PAWN,
									color)) + "@" + getSan(square));
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							PremoveInfo premoveInfo = new PremoveInfo();
							premoveInfo.isPremoveDrop = true;
							premoveInfo.toSquare = square;
							premoveInfo.fromSquare = getDropSquareFromColoredPiece(getColoredPiece(
									PAWN, color));
							premoveInfo.fromPiece = isUserWhite() ? WP : BP;
							premoves.add(premoveInfo);
							adjustPremoveLabelHighlightsAndArrows();
						}
					});
				}
				if (getGame().getDropCount(color, KNIGHT) == 0) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Premove drop "
							+ getPieceRepresentation(getColoredPiece(KNIGHT,
									color)) + "@" + getSan(square));
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							PremoveInfo premoveInfo = new PremoveInfo();
							premoveInfo.isPremoveDrop = true;
							premoveInfo.toSquare = square;
							premoveInfo.fromSquare = getDropSquareFromColoredPiece(getColoredPiece(
									KNIGHT, color));
							premoveInfo.fromPiece = isUserWhite() ? WN : BN;
							premoves.add(premoveInfo);
							adjustPremoveLabelHighlightsAndArrows();
						}
					});
				}
				if (getGame().getDropCount(color, BISHOP) == 0) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Premove drop "
							+ getPieceRepresentation(getColoredPiece(BISHOP,
									color)) + "@" + getSan(square));
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							PremoveInfo premoveInfo = new PremoveInfo();
							premoveInfo.isPremoveDrop = true;
							premoveInfo.toSquare = square;
							premoveInfo.fromSquare = getDropSquareFromColoredPiece(getColoredPiece(
									BISHOP, color));
							premoveInfo.fromPiece = isUserWhite() ? WB : BB;
							premoves.add(premoveInfo);
							adjustPremoveLabelHighlightsAndArrows();
						}
					});
				}
				if (getGame().getDropCount(color, ROOK) == 0) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Premove drop "
							+ getPieceRepresentation(getColoredPiece(ROOK,
									color)) + "@" + getSan(square));
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							PremoveInfo premoveInfo = new PremoveInfo();
							premoveInfo.isPremoveDrop = true;
							premoveInfo.toSquare = square;
							premoveInfo.fromSquare = getDropSquareFromColoredPiece(getColoredPiece(
									ROOK, color));
							premoveInfo.fromPiece = isUserWhite() ? WR : BR;
							premoves.add(premoveInfo);
							adjustPremoveLabelHighlightsAndArrows();
						}
					});
				}
				if (getGame().getDropCount(color, QUEEN) == 0) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Premove drop "
							+ getPieceRepresentation(getColoredPiece(QUEEN,
									color)) + "@" + getSan(square));
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							PremoveInfo premoveInfo = new PremoveInfo();
							premoveInfo.isPremoveDrop = true;
							premoveInfo.toSquare = square;
							premoveInfo.fromSquare = getDropSquareFromColoredPiece(getColoredPiece(
									QUEEN, color));
							premoveInfo.fromPiece = isUserWhite() ? WQ : BQ;
							premoves.add(premoveInfo);
							adjustPremoveLabelHighlightsAndArrows();
						}
					});
				}
			}

			if (menu.getItemCount() > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}

			if (getGame().getDropCount(color, PAWN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item
						.setText(getPieceRepresentation(getColoredPiece(PAWN,
								color))
								+ "@" + getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(
								getDropSquareFromColoredPiece(getColoredPiece(
										PAWN, color)), square);

					}
				});
			}
			if (getGame().getDropCount(color, KNIGHT) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(getPieceRepresentation(getColoredPiece(KNIGHT,
						color))
						+ "@" + getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(
								getDropSquareFromColoredPiece(getColoredPiece(
										KNIGHT, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, BISHOP) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(getPieceRepresentation(getColoredPiece(BISHOP,
						color))
						+ "@" + getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(
								getDropSquareFromColoredPiece(getColoredPiece(
										BISHOP, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, ROOK) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item
						.setText(getPieceRepresentation(getColoredPiece(ROOK,
								color))
								+ "@" + getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(
								getDropSquareFromColoredPiece(getColoredPiece(
										ROOK, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, QUEEN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(getPieceRepresentation(getColoredPiece(QUEEN,
						color))
						+ "@" + getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(
								getDropSquareFromColoredPiece(getColoredPiece(
										QUEEN, color)), square);
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

	protected void onRandomCapture() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("On onRandomCapture " + getGame().getId());
		}
		try {
			if (isUsersMove()) {
				Move[] moves = getGame().getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.isCapture()) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));

					if (game.move(move)) {
						game.rollback();
						final Move finalMove = move;
						ThreadService.getInstance().run(new Runnable() {
							public void run() {
								connector.makeMove(game, finalMove);
							}
						});
					} else {
						throw new IllegalStateException(
								"Game rejected move in onRandomCapture. This is a bug.");
					}

					removeAllMoveDecorations();
					refreshForMove(move);
				} else {
					onPlayIllegalMoveSound();
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("Rejected onRandomCapture since its not users move.");
				}
				onPlayIllegalMoveSound();
			}
		} catch (Throwable t) {
			getConnector().onError("PlayingController.onRandomCapture", t);
		}
	}

	protected void onRandomMove() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("On onRandomMove " + getGame().getId());
		}
		try {
			if (isUsersMove()) {
				Move[] moves = getGame().getLegalMoves().asArray();
				if (moves.length > 0) {
					Move move = moves[random.nextInt(moves.length)];
					if (game.move(move)) {
						game.rollback();
						final Move finalMove = move;
						ThreadService.getInstance().run(new Runnable() {
							public void run() {
								connector.makeMove(game, finalMove);
							}
						});
					} else {
						throw new IllegalStateException(
								"Game rejected move in random move. This is a bug.");
					}

					removeAllMoveDecorations();
					refreshForMove(move);
				} else {
					onPlayIllegalMoveSound();
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Rejected onRandomMove its not users move.");
				}
				onPlayIllegalMoveSound();
			}
		} catch (Throwable t) {
			getConnector().onError("PlayingController.onRandomMove", t);
		}

	}

	protected void onRandomRecapture() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("On onRandomRecapture " + getGame().getId());
		}
		try {
			if (isUsersMove() && game.getLastMove() != null
					&& game.getLastMove().isCapture()) {
				Move lastMove = game.getLastMove();

				Move[] moves = getGame().getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.isCapture() && move.getTo() == lastMove.getTo()) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));

					if (game.move(move)) {
						game.rollback();
						final Move finalMove = move;
						ThreadService.getInstance().run(new Runnable() {
							public void run() {
								connector.makeMove(game, finalMove);
							}
						});
					} else {
						throw new IllegalStateException(
								"Game rejected move in onRandomRecapture. This is a bug.");
					}

					removeAllMoveDecorations();
					refreshForMove(move);
				} else {
					onPlayIllegalMoveSound();
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug("Rejected onRandomRecapture since its not users move.");
				}
				onPlayIllegalMoveSound();
			}
		} catch (Throwable t) {
			getConnector().onError("PlayingController.onRandomRecapture", t);
		}
	}

	protected void onSmartMove(final int square) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("On onSmartMove " + getGame().getId() + " " + square);
		}
		try {
			if (isUsersMove()) {
				Move[] moves = getGame().getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.getTo() == square
							&& (move.getMoveCharacteristic() & Move.DROP_CHARACTERISTIC) == 0) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));

					if (game.move(move)) {
						game.rollback();
						final Move finalMove = move;
						ThreadService.getInstance().run(new Runnable() {
							public void run() {
								connector.makeMove(game, finalMove);
							}
						});
					} else {
						throw new IllegalStateException(
								"Game rejected move in random move. This is a bug.");
					}

					removeAllMoveDecorations();
					refreshForMove(move);
				} else {
					onPlayIllegalMoveSound();
				}
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Rejected smart move since its not users move.");
				}
				onPlayIllegalMoveSound();
			}
		} catch (Throwable t) {
			getConnector().onError("PlayingController.onSmartMove", t);
		}
	}

}
