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

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.service.SoundService;
import raptor.swt.SWTUtils;
import raptor.swt.chess.BoardConstants;
import raptor.swt.chess.BoardUtils;
import raptor.swt.chess.ChessBoardController;

/**
 * This controller is used when a game is no longer active. It allows the user
 * to play around with the position and traverser the move list. However it is
 * not backed by a connector, so the users actions do not do anything to a
 * connector.
 */
public class InactiveController extends ChessBoardController implements
		BoardConstants, GameConstants {
	static final Log LOG = LogFactory.getLog(ChessBoardController.class);
	protected GameCursor cursor;
	protected Random random = new SecureRandom();
	protected String title;
	protected ToolBar toolbar;
	protected boolean userMadeAdjustment = false;

	public InactiveController(Game game) {
		this(game, "Inactive");

	}

	public InactiveController(Game game, String title) {
		super(new GameCursor(game, GameCursor.Mode.MakeMovesOnCursor));
		cursor = (GameCursor) getGame();
		this.title = title;
	}

	/**
	 * If premove is enabled, and premove is not in queued mode then clear the
	 * premoves on an illegal move.
	 */
	public void adjustForIllegalMove(String move) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		board.unhighlightAllSquares();
		refresh();
		onPlayIllegalMoveSound();
		board.getStatusLabel().setText("Illegal Move: " + move);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isDisposed()) {
			board.getGameDescriptionLabel().setText(
					title == null ? "Inactive "
							+ getGame().getHeader(PgnHeader.Event) : title);
		}
	}

	/**
	 * Adjusts the game status label. If the game is not in an active state, the
	 * status label sets itself to getResultDescription in the game. If the game
	 * is in an active state, the status is set to the last move.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	@Override
	public void adjustGameStatusLabel() {
		if (userMadeAdjustment) {
			if (getGame().getMoveList().getSize() > 0) {
				Move lastMove = getGame().getMoveList().get(
						getGame().getMoveList().getSize() - 1);
				int moveNumber = getGame().getFullMoveCount();

				board.getStatusLabel().setText(
						"Last Move: "
								+ moveNumber
								+ ") "
								+ (lastMove.isWhitesMove() ? "" : "... ")
								+ GameUtils.convertSanToUseUnicode(lastMove
										.toString(), !game.isWhitesMove()));

			} else {
				board.getStatusLabel().setText("");
			}
		} else {
			String result = getGame().getHeader(PgnHeader.ResultDescription);
			if (result != null) {
				board.getStatusLabel().setText(result);
			}
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isDisposed()) {
			if (BoardUtils.isPieceJailSquare(squareId)) {
				if (getGame().isInState(Game.DROPPABLE_STATE)) {
					int pieceType = BoardUtils.pieceJailSquareToPiece(squareId);
					return getGame().isWhitesMove()
							&& BoardUtils.isWhitePiece(pieceType)
							|| !getGame().isWhitesMove()
							&& BoardUtils.isBlackPiece(pieceType);
				}
			} else if (getGame().getPiece(squareId) == EMPTY) {
				return false;
			} else {
				return getGame().isWhitesMove()
						&& BoardUtils.isWhitePiece(board.getSquare(squareId)
								.getPiece())
						|| !getGame().isWhitesMove()
						&& BoardUtils.isBlackPiece(board.getSquare(squareId)
								.getPiece());
			}
		}
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (toolbar != null) {
			toolbar.setVisible(false);
			SWTUtils.clearToolbar(toolbar);
			toolbar = null;
		}
	}

	public void enableDisableNavButtons() {
		setToolItemEnabled(ToolBarItemKey.NEXT_NAV, cursor.hasNext());
		setToolItemEnabled(ToolBarItemKey.BACK_NAV, cursor.hasPrevious());
		setToolItemEnabled(ToolBarItemKey.FIRST_NAV, cursor.hasFirst());
		setToolItemEnabled(ToolBarItemKey.LAST_NAV, cursor.hasLast());
		setToolItemEnabled(ToolBarItemKey.REVERT_NAV, cursor.canRevert());
		setToolItemEnabled(ToolBarItemKey.COMMIT_NAV, cursor.canCommit());
	}

	@Override
	public String getTitle() {
		return title == null ? "Inactive" : title;
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			BoardUtils.addPromotionIconsToToolbar(this, toolbar, true, game
					.getVariant() == Variant.suicide);
			new ToolItem(toolbar, SWT.SEPARATOR);
			ToolItem saveItem = new ToolItem(toolbar, SWT.PUSH);
			saveItem.setImage(Raptor.getInstance().getIcon("save"));
			saveItem.setToolTipText("Save to pgn.");
			saveItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onSave();
				}
			});

			BoardUtils.addNavIconsToToolbar(this, toolbar, true, true);
			ToolItem movesItem = new ToolItem(toolbar, SWT.CHECK);
			movesItem.setImage(Raptor.getInstance().getIcon("moveList"));
			movesItem.setToolTipText("Shows or hides the move list.");
			movesItem.setSelection(false);
			addToolItem(ToolBarItemKey.MOVE_LIST, movesItem);
			movesItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isToolItemSelected(ToolBarItemKey.MOVE_LIST)) {
						board.showMoveList();
					} else {
						board.hideMoveList();
					}
				}
			});
			new ToolItem(toolbar, SWT.SEPARATOR);
		} else if (toolbar.getParent() != parent) {
			toolbar.setParent(parent);
		}

		if (game.getVariant() == Variant.suicide) {
			setToolItemSelected(ToolBarItemKey.AUTO_KING, true);
		} else {
			setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
		}
		return toolbar;
	}

	@Override
	public void init() {
		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? true : false);
		} else {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? false : true);
		}
		board.getMoveList().updateToGame();
		cursor.setCursorMasterLast();
		refresh();
	}

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	public void onSave() {
		FileDialog fd = new FileDialog(board.getControl().getShell(), SWT.SAVE);
		fd.setText("Save To PGN");
		fd.setFilterPath("");
		String[] filterExt = { "*.pgn", "*.*" };
		fd.setFilterExtensions(filterExt);
		final String selected = fd.open();

		if (selected != null) {
			String pgn = cursor.toPgn();
			FileWriter writer = null;

			try {
				writer = new FileWriter(selected);
				writer.write(pgn);
				writer.flush();
			} catch (IOException ioe) {
				Raptor.getInstance().onError("Error saving pgn file.", ioe);
			} finally {
				try {
					writer.close();
				} catch (Throwable t) {
				}
			}
		}
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
		case REVERT_NAV:
			cursor.revert();
			refresh();
			break;
		case COMMIT_NAV:
			cursor.commit();
			refresh();
			break;
		case NEXT_NAV:
			cursor.setCursorNext();
			refresh();
			break;
		case BACK_NAV:
			cursor.setCursorPrevious();
			refresh();
			break;
		case FIRST_NAV:
			cursor.setCursorFirst();
			refresh();
			break;
		case LAST_NAV:
			cursor.setCursorLast();
			refresh();
			break;
		}
	}

	@Override
	public void refresh() {
		board.getMoveList().updateToGame();
		board.getMoveList().select(cursor.getCursorPosition());
		enableDisableNavButtons();
		super.refresh();
	}

	/**
	 * Sets the toolbar. Useful when controllers are swapping out.
	 * 
	 * @param toolbar
	 */
	public void setToolbar(ToolBar toolbar) {
		this.toolbar = toolbar;
	}

	@Override
	public void userCancelledMove(int fromSquare, boolean isDnd) {
		if (!isDisposed()) {
			LOG.debug("moveCancelled" + getGame().getId() + " " + fromSquare
					+ " " + isDnd);
			board.unhighlightAllSquares();
			refresh();
			onPlayIllegalMoveSound();
		}
	}

	/**
	 * Invoked when the move list is clicked on. THe halfMoveNumber is the move
	 * selected.
	 * 
	 * The default implementation does nothing. It can be overridden to provide
	 * functionality.
	 */
	@Override
	public void userClickedOnMove(int halfMoveNumber) {
		cursor.setCursor(halfMoveNumber);
		enableDisableNavButtons();
		refresh();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isDisposed()) {
			LOG.debug("moveInitiated" + getGame().getId() + " " + square + " "
					+ isDnd);
			userMadeAdjustment = true;
			board.getResultDecoration().setHiding(true);

			board.unhighlightAllSquares();
			board.getSquare(square).highlight();
			if (isDnd && !BoardUtils.isPieceJailSquare(square)) {
				board.getSquare(square).setPiece(GameConstants.EMPTY);
			}
			board.redrawSquares();
		}
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove " + getGame().getId() + " "
					+ GameUtils.getSan(fromSquare) + " "
					+ GameUtils.getSan(toSquare));
		}

		board.unhighlightAllSquares();

		if (fromSquare == toSquare || BoardUtils.isPieceJailSquare(toSquare)) {
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("User tried to make a move where from square == to square or toSquar was the piece jail.");
			}
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
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
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		} else {
			board.getSquare(fromSquare).highlight();
			board.getSquare(toSquare).highlight();

			if (game.move(move)) {
				refresh();
				onPlayMoveSound();
			} else {
				Raptor.getInstance().onError(
						"Game.move returned false for a move that should have been legal.Move: "
								+ move + ".",
						new Throwable(getGame().toString()));
				adjustForIllegalMove(move.toString());
			}
		}
	}

	@Override
	public void userMiddleClicked(int square) {
		LOG.debug("On middle click " + getGame().getId() + " " + square);
	}

	@Override
	public void userRightClicked(int square) {
		LOG.debug("On right click " + getGame().getId() + " " + square);
	}
}
