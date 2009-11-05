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
import org.eclipse.swt.widgets.ToolBar;

import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.Arrow;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.Highlight;

/**
 * Examines a game on a backing connector. When examining a game the user is
 * allowed to traverse the position through the nav buttons and make moves.
 * However all of these actions are sent directly to the connector and it drives
 * the process
 */
public class ExamineController extends ChessBoardController {
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

							connector.getGameService()
									.removeGameServiceListener(listener);
							inactiveController
									.setItemChangedListeners(itemChangedListeners);
							// Clear the cool bar and init the inactive
							// controller.
							ChessBoardUtils.clearCoolbar(getBoard());
							inactiveController.init();

							setItemChangedListeners(null);
							ExamineController.this.dispose();

						} catch (Throwable t) {
							connector.onError("ExamineController.gameInactive",
									t);
						}
					}
				});
			}
		}

		@Override
		public void gameMovesAdded(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						try {
							refresh();
						} catch (Throwable t) {
							connector.onError(
									"ExamineController.gameMovesAdded", t);
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
							examinePositionUpdate();
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
							examineOnIllegalMove(move);
						} catch (Throwable t) {
							connector.onError("ExamineController.illegalMove",
									t);
						}
					}
				});
			}
		}

	};
	protected ToolBar toolbar;

	public ExamineController(Game game, Connector connector) {
		super(game, connector);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel().setText("Examining a game");
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isDisposed()) {
			if (ChessBoardUtils.isPieceJailSquare(squareId)) {
				return false;
			} else {
				int piece = GameUtils.getColoredPiece(squareId, getGame());
				return ChessBoardUtils.isWhitePiece(piece)
						&& getGame().isWhitesMove()
						|| ChessBoardUtils.isBlackPiece(piece)
						&& !getGame().isWhitesMove();
			}
		}
		return false;
	}

	@Override
	public void dispose() {
		try {
			connector.getGameService().removeGameServiceListener(listener);
			if (connector.isConnected()
					&& getGame().isInState(Game.ACTIVE_STATE)) {
				connector.onUnexamine(getGame());
			}
			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.setVisible(false);
				SWTUtils.clearToolbar(toolbar);
				toolbar = null;
			}
			super.dispose();
		} catch (Throwable t) {
		}// Eat it its prob a disposed exception
	}

	public void examineOnIllegalMove(String move) {
		if (LOG.isDebugEnabled()) {
			LOG.info("examineOnIllegalMove " + getGame().getId() + " " + move);
		}

		// board.unhighlightAllSquares();
		refresh();
		SoundService.getInstance().playSound("illegalMove");

		if (move != null) {
			board.getStatusLabel().setText("Illegal Move: " + move);
		}
	}

	public void examinePositionUpdate() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("examinePositionUpdate " + getGame().getId());
		}

		// board.unhighlightAllSquares();
		refresh();
		onPlayMoveSound();
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return connector.getShortName() + "(Exam " + getGame().getId() + ")";
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);
		if (toolbar == null) {
			toolbar = new ToolBar(isCoolbarMode ? getBoard().getCoolbar()
					: parent, SWT.FLAT);
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.ExaminingChessBoard, toolbar, true);

			if (game.getVariant() == Variant.suicide) {
				setToolItemSelected(ToolBarItemKey.AUTO_KING, true);
			} else {
				setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
			}
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
		board.setWhiteOnTop(false);
		board.setWhitePieceJailOnTop(true);
		refresh();
		onPlayGameStartSound();
		connector.getGameService().addGameServiceListener(listener);
		fireItemChanged();
	}

	@Override
	public void onBack() {
		connector.onExamineModeBack(getGame());
	}

	@Override
	public void onCommit() {
		connector.onExamineModeCommit(getGame());
	}

	@Override
	public void onFirst() {
		connector.onExamineModeFirst(getGame());
	}

	@Override
	public void onForward() {
		connector.onExamineModeForward(getGame());
	}

	@Override
	public void onLast() {
		connector.onExamineModeLast(getGame());
	}

	@Override
	public void onRevert() {
		connector.onExamineModeRevert(getGame());
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		board.unhidePieces();
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();
		refreshBoard();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();
		if (getPreferences().getBoolean(
				PreferenceKeys.HIGHLIGHT_SHOW_ON_MY_MOVES)) {
			board.getSquareHighlighter().addHighlight(
					new Highlight(square, getPreferences().getColor(
							PreferenceKeys.HIGHLIGHT_MY_COLOR), false));
		}

		if (isDnd) {
			board.getSquare(square).setHidingPiece(true);
		}
		board.getSquare(square).redraw();
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
		}

		board.unhidePieces();
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();

		if (ChessBoardUtils.isPieceJailSquare(toSquare)) {
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Game game = getGame();
		Move move = null;
		if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare,
					getAutoPromoteSelection());
		} else {
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare);
		}

		if (move != null) {
			connector.makeMove(game, move);
			if (getPreferences().getBoolean(
					PreferenceKeys.HIGHLIGHT_SHOW_ON_MY_MOVES)) {
				board
						.getSquareHighlighter()
						.addHighlight(
								new Highlight(
										move.getFrom(),
										move.getTo(),
										getPreferences()
												.getColor(
														PreferenceKeys.HIGHLIGHT_MY_COLOR),
										getPreferences()
												.getBoolean(
														PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
			}

			if (getPreferences().getBoolean(
					PreferenceKeys.ARROW_SHOW_ON_MY_MOVES)) {
				board.getArrowDecorator().addArrow(
						new Arrow(move.getFrom(), move.getTo(),
								getPreferences().getColor(
										PreferenceKeys.ARROW_MY_COLOR),
								getPreferences().getBoolean(
										PreferenceKeys.ARROW_FADE_AWAY_MODE)));
			}

			refreshForMove(move);
		} else {
			examineOnIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}
	}

	@Override
	public void userMiddleClicked(int square) {
	}

	@Override
	public void userRightClicked(int square) {
	}

	@Override
	protected void adjustClockColors() {
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
