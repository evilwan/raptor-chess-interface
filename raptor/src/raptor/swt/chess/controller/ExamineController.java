/**
Ä * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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

import raptor.util.RaptorLogger;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;

import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.MouseButtonAction;
import raptor.util.RaptorRunnable;

/**
 * Examines a game on a backing connector. When examining a game the user is
 * allowed to traverse the position through the nav buttons and make moves.
 * However all of these actions are sent directly to the connector and it drives
 * the process
 */
public class ExamineController extends ChessBoardController {
	static final RaptorLogger LOG = RaptorLogger
			.getLog(ExamineController.class);
	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void examinedGameBecameSetup(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay()
						.asyncExec(new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								SetupController setupController = new SetupController(
										game, board.isWhiteOnTop(), connector);
								getBoard().setController(setupController);
								setupController
										.setItemChangedListeners(itemChangedListeners);
								setupController.setBoard(board);
								connector.getGameService()
										.removeGameServiceListener(listener);

								// Set the listeners to null so they wont
								// get
								// cleared and disposed
								setItemChangedListeners(null);

								// Clear the cool bar and init the
								// examineController
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								setupController.init();

								unexamineOnDispose = false;
								ExamineController.this.dispose();
							}
						});
			}
		}

		@Override
		public void gameInactive(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay()
						.asyncExec(new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								onPlayGameEndSound();
								InactiveController inactiveController = new InactiveController(
										game, getConnector());
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
							}
						});
			}
		}

		@Override
		public void gameMovesAdded(Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay()
						.asyncExec(new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								refresh();
							}
						});
			}
		}

		@Override
		public void gameStateChanged(Game game, final boolean isNewMove) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay()
						.asyncExec(new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								examinePositionUpdate();
							}
						});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay()
						.asyncExec(new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {

								if (isDisposed()) {
									return;
								}

								examineOnIllegalMove(move);
							}
						});
			}
		}

	};
	protected ToolBar toolbar;
	protected boolean initWhiteOnTop;
	protected boolean unexamineOnDispose = true;

	public ExamineController(Game game, boolean isWhiteOnTop,
			Connector connector) {
		super(game, connector);
		initWhiteOnTop = isWhiteOnTop;
	}

	public ExamineController(Game game, Connector connector) {
		this(game, false, connector);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		board.getGameDescriptionLabel()
				.setText(game.getHeader(PgnHeader.Event));
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isDisposed()) {
			if (ChessBoardUtils.isPieceJailSquare(squareId)
					&& !getGame().isInState(Game.DROPPABLE_STATE)) {
				return false;
			} else if (ChessBoardUtils.isPieceJailSquare(squareId)) {
				return getGame().isWhitesMove()
						&& ChessBoardUtils.isJailSquareWhitePiece(squareId)
						|| !getGame().isWhitesMove()
						&& ChessBoardUtils.isJailSquareBlackPiece(squareId);
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
			if (unexamineOnDispose && connector.isConnected()
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
			toolbar = SWTUtils.createToolbar(isCoolbarMode ? getBoard()
					.getCoolbar() : parent);
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
		board.getArrowDecorator().removeAllArrows();
		board.getSquareHighlighter().removeAllHighlights();

		board.setWhiteOnTop(initWhiteOnTop);

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
		board.getControl().layout(true, true);

		refresh();
		connector.getGameService().addGameServiceListener(listener);
		fireItemChanged();

		if (LOG.isDebugEnabled()) {
			LOG.debug("Inited Examine Controller: " + getGame().getId());
		}
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
	public void refresh(boolean isUpdatingClocks) {
		if (isDisposed()) {
			return;
		}

		removeAllMoveDecorations();
		Move lastMove = game.getMoveList().getSize() > 0 ? game.getMoveList()
				.getLast() : null;

		if (lastMove != null) {
			addDecorationsForMove(lastMove, false);
		}

		board.getMoveList().updateToGame();
		board.getMoveList().select(getGame().getMoveList().getSize());
		super.refresh(isUpdatingClocks);
		board.getEngineAnalysisWidget().updateToGame();
	}

	@Override
	public void userCancelledMove(int fromSquare) {
		SoundService.getInstance().playSound("illegalMove");
		board.unhidePieces();
		refreshBoard();
	}

	@Override
	public void userInitiatedMove(int square) {
		board.getSquare(square).setHidingPiece(true);
		board.getSquare(square).redraw();
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
		}

		board.unhidePieces();
		removeAllMoveDecorations();

		if (ChessBoardUtils.isPieceJailSquare(toSquare)) {
			SoundService.getInstance().playSound("illegalMove");
			refreshBoard();
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
			addDecorationsForMove(move, true);
			refreshForMove(move);
		} else {
			examineOnIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}
	}

	@Override
	public void userMouseWheeled(int count) {
		if (count < 0) {
			onForward();
		} else {
			onBack();
		}
	}

	@Override
	public void userPressedMouseButton(MouseButtonAction button, int square) {
	}

	protected void onPlayGameEndSound() {
		// SoundService.getInstance().playSound("obsGameEnd");
	}

	protected void onPlayGameStartSound() {
		// SoundService.getInstance().playSound("gameStart");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}
}
