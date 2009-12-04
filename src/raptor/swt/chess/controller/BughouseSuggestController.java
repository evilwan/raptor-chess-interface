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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.ClockLabelUpdater;
import raptor.swt.chess.MouseButtonAction;

public class BughouseSuggestController extends ObserveController {

	protected boolean isPartnerWhite;

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 */
	public BughouseSuggestController(Game game, Connector connector,
			boolean isPartnerWhite) {
		super(game, connector);
		this.isPartnerWhite = isPartnerWhite;
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
	public boolean canUserInitiateMoveFrom(int squareId) {
		if (!isDisposed()) {
			return ChessBoardUtils.isPieceJailSquare(squareId) ? true
					: getGame().getPiece(squareId) != EMPTY;
		}
		return false;
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);
		if (toolbar == null) {
			toolbar = new ToolBar(isCoolbarMode ? getBoard().getCoolbar()
					: parent, SWT.FLAT);
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.BughouseSuggestChessBoard, toolbar,
					isPartnerWhite);

			setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
			setToolItemSelected(ToolBarItemKey.FORCE_UPDATE, true);
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

	/**
	 * In droppable games this shows a menu of the pieces available for
	 * dropping. In bughouse the menu includes the premove drop features which
	 * drops a move when the piece becomes available.
	 */
	public void onPopupMenu(final int square) {
		if (isDisposed()) {
			return;
		}

		if (!ChessBoardUtils.isPieceJailSquare(square)
				&& getGame().isInState(Game.DROPPABLE_STATE)) {
			final int color = getGame().getColorToMove();
			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch " + GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch " + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.SEPARATOR);

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Suggest "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(PAWN, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " I suggest P@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Suggest "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(KNIGHT, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " I suggest N@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Suggest "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(BISHOP, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " I suggest B@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Suggest "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(ROOK, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " I suggest R@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Suggest "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(QUEEN, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " I suggest Q@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.SEPARATOR);

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(PAWN, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch out for P@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(KNIGHT, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch out for N@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(BISHOP, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch out for B@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(ROOK, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch out for R@" + GameUtils.getSan(square));
				}
			});

			item = new MenuItem(menu, SWT.PUSH);
			item.setText("Watch "
					+ GameUtils.getPieceRepresentation(GameUtils
							.getColoredPiece(QUEEN, color)) + "@"
					+ GameUtils.getSan(square));
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					connector.sendMessage(connector.getPartnerTellPrefix()
							+ " Watch out for Q@" + GameUtils.getSan(square));
				}
			});

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

	@Override
	public void userCancelledMove(int fromSquare) {
		if (!isDisposed()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("moveCancelled" + getGame().getId() + " "
						+ fromSquare);
			}
			board.unhidePieces();
			refresh();
			onPlayIllegalMoveSound();
		}
	}

	@Override
	public void userInitiatedMove(int square) {
		if (!isDisposed()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("moveInitiated" + getGame().getId() + " " + square);
			}
			board.getSquare(square).setHidingPiece(true);
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
		removeAllMoveDecorations();

		if (fromSquare == toSquare
				|| board.getSquare(fromSquare).getPiece() == EMPTY
				|| ChessBoardUtils.isPieceJailSquare(toSquare)) {
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("User tried to make a move where from square == to square or toSquare was the piece jail.");
			}
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing user move..");
		}

		int fromColoredPiece = board.getSquare(fromSquare).getPiece();
		if (fromColoredPiece == EMPTY
				&& ChessBoardUtils.isPieceJailSquare(fromSquare)) {
			fromColoredPiece = GameUtils
					.getColoredPieceFromDropSquare(fromColoredPiece);
		}
		boolean isColoredPieceWhite = GameUtils.isWhitePiece(fromColoredPiece);
		String san = GameUtils.getPseudoSan(getGame(), fromSquare, toSquare,
				false);

		if (isColoredPieceWhite && isPartnerWhite || !isColoredPieceWhite
				&& !isPartnerWhite) {
			connector.sendMessage(connector.getPartnerTellPrefix()
					+ " I suggest " + san);
		} else {
			connector.sendMessage(connector.getPartnerTellPrefix()
					+ " Watch out for " + san);
		}

		refreshBoard();
	}

	@Override
	public void userPressedMouseButton(MouseButtonAction button, int square) {
		switch (button) {
		case Right:
			onPopupMenu(square);
			break;
		}
	}

	@Override
	protected void initClockUpdaters() {
		if (whiteClockUpdater == null) {
			boolean speakCountdown = getPreferences().getBoolean(
					BUGHOUSE_SPEAK_COUNTDOWN_ON_PARTNER_BOARD);
			whiteClockUpdater = new ClockLabelUpdater(true, this,
					speakCountdown && isPartnerWhite);
			blackClockUpdater = new ClockLabelUpdater(false, this,
					speakCountdown && !isPartnerWhite);
		}
	}

	@Override
	protected void onPlayGameEndSound() {
	}

	@Override
	protected void onPlayGameStartSound() {
	}

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}
}
