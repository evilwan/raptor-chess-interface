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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.swt.SWTUtils;
import raptor.swt.chess.Arrow;
import raptor.swt.chess.BoardConstants;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.Highlight;
import raptor.util.RaptorStringUtils;

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
	protected boolean canBeTakenOver;

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * isReusable will be set to the users preference
	 * BOARD_TAKEOVER_INACTIVE_GAMES
	 */
	public InactiveController(Game game) {
		this(game, "Inactive", Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES));
	}

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * @param canBeTakenOver
	 *            Means if another game needs to be displayed, it can take over
	 *            this game and remove it from being displayed. This may not
	 *            always be desirable for instance when viewing a PGN game.
	 */
	public InactiveController(Game game, String title, boolean canBeTakenOver) {
		super(new GameCursor(game, GameCursor.Mode.MakeMovesOnCursor));
		cursor = (GameCursor) getGame();
		this.title = title;
		this.canBeTakenOver = canBeTakenOver;
	}

	public void adjustForIllegalMove(String move) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

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

	/**
	 * Inactive games can be taken over by other games that need to be displayed
	 * for efficientcy. This is an optimization feature. See the constructors
	 * for more information on how this is set.
	 */
	public boolean canBeTakenOver() {
		return canBeTakenOver;
	}

	@Override
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isDisposed()) {
			if (ChessBoardUtils.isPieceJailSquare(squareId)) {
				if (getGame().isInState(Game.DROPPABLE_STATE)) {
					int pieceType = ChessBoardUtils
							.pieceJailSquareToPiece(squareId);
					return getGame().isWhitesMove()
							&& ChessBoardUtils.isWhitePiece(pieceType)
							|| !getGame().isWhitesMove()
							&& ChessBoardUtils.isBlackPiece(pieceType);
				}
			} else if (getGame().getPiece(squareId) == EMPTY) {
				return false;
			} else {
				return getGame().isWhitesMove()
						&& ChessBoardUtils.isWhitePiece(board.getSquare(
								squareId).getPiece())
						|| !getGame().isWhitesMove()
						&& ChessBoardUtils.isBlackPiece(board.getSquare(
								squareId).getPiece());
			}
		}
		return false;
	}

	public void decorateForLastMoveListMove() {
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();

		Move lastMove = getGame().getLastMove();

		if (lastMove != null) {
			if (getPreferences().getBoolean(
					PreferenceKeys.HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES)) {
				board
						.getSquareHighlighter()
						.addHighlight(
								new Highlight(
										lastMove.getFrom(),
										lastMove.getTo(),
										getPreferences()
												.getColor(
														PreferenceKeys.HIGHLIGHT_OBS_COLOR),
										getPreferences()
												.getBoolean(
														PreferenceKeys.HIGHLIGHT_FADE_AWAY_MODE)));
			}

			if (getPreferences().getBoolean(
					PreferenceKeys.ARROW_SHOW_ON_MOVE_LIST_MOVES)) {
				board.getArrowDecorator().addArrow(
						new Arrow(lastMove.getFrom(), lastMove.getTo(),
								getPreferences().getColor(
										PreferenceKeys.ARROW_OBS_COLOR),
								getPreferences().getBoolean(
										PreferenceKeys.ARROW_FADE_AWAY_MODE)));
			}
		}
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
			ChessBoardUtils.addPromotionIconsToToolbar(this, toolbar, true,
					game.getVariant() == Variant.suicide);
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

			ChessBoardUtils.addNavIconsToToolbar(this, toolbar, true, true);
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
		board.setWhiteOnTop(RaptorStringUtils.getBooleanValue(game
				.getHeader(PgnHeader.WhiteOnTop)));

		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? true : false);
		} else {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop() ? false : true);
		}
		board.getMoveList().updateToGame();
		cursor.setCursorMasterLast();
		refresh();
		fireItemChanged();
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
			decorateForLastMoveListMove();
			break;
		case COMMIT_NAV:
			cursor.commit();
			refresh();
			break;
		case NEXT_NAV:
			board.getResultDecorator().setDecoration(null);
			cursor.setCursorNext();
			refresh();
			decorateForLastMoveListMove();
			break;
		case BACK_NAV:
			board.getResultDecorator().setDecoration(null);
			cursor.setCursorPrevious();
			refresh();
			decorateForLastMoveListMove();
			break;
		case FIRST_NAV:
			board.getResultDecorator().setDecoration(null);
			cursor.setCursorFirst();
			refresh();
			decorateForLastMoveListMove();
			break;
		case LAST_NAV:
			board.getResultDecorator().setDecoration(null);
			cursor.setCursorLast();
			refresh();
			decorateForLastMoveListMove();
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
			board.unhidePieces();
			board.getSquareHighlighter().removeAllHighlights();
			board.getArrowDecorator().removeAllArrows();
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
		board.getResultDecorator().setDecoration(null);
		cursor.setCursor(halfMoveNumber);
		enableDisableNavButtons();
		refresh();
		decorateForLastMoveListMove();
	}

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isDisposed()) {
			LOG.debug("moveInitiated" + getGame().getId() + " " + square + " "
					+ isDnd);
			userMadeAdjustment = true;
			board.getResultDecorator().setDecoration(null);

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
	}

	@Override
	public void userMadeMove(int fromSquare, int toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove " + getGame().getId() + " "
					+ GameUtils.getSan(fromSquare) + " "
					+ GameUtils.getSan(toSquare));
		}
		board.unhidePieces();
		board.getSquareHighlighter().removeAllHighlights();
		board.getArrowDecorator().removeAllArrows();

		if (fromSquare == toSquare
				|| ChessBoardUtils.isPieceJailSquare(toSquare)) {
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
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare,
					getAutoPromoteSelection());
		} else {
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare);
		}

		if (move == null) {
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		} else {
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

		if (!ChessBoardUtils.isPieceJailSquare(square)
				&& getGame().isInState(Game.DROPPABLE_STATE)
				&& getGame().getPiece(square) == EMPTY) {
			final int color = getGame().getColorToMove();
			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			if (getGame().getDropCount(color, PAWN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PAWN, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PAWN, color)), square);

					}
				});
			}
			if (getGame().getDropCount(color, KNIGHT) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(KNIGHT, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(KNIGHT, color)),
								square);
					}
				});
			}
			if (getGame().getDropCount(color, BISHOP) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(BISHOP, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(BISHOP, color)),
								square);
					}
				});
			}
			if (getGame().getDropCount(color, ROOK) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(ROOK, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(ROOK, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, QUEEN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(QUEEN, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(QUEEN, color)), square);
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

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}
}
