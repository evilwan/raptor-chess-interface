package raptor.swt.chess.controller;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.GameCursor.Mode;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.Highlight;

public class BughouseSuggestController extends ObserveController {

	protected boolean isPartnerWhite;

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 */
	public BughouseSuggestController(Game game, Connector connector,
			boolean isPartnerWhite) {
		super(new GameCursor(game,
				GameCursor.Mode.MakeMovesOnMasterSetCursorToLast), connector);
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
			if (getGame().getPiece(squareId) == EMPTY) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = new ToolBar(parent, SWT.FLAT);
			ChessBoardUtils.addPromotionIconsToToolbar(this, toolbar,
					isPartnerWhite, false);
			new ToolItem(toolbar, SWT.SEPARATOR);
			ChessBoardUtils.addNavIconsToToolbar(this, toolbar, true, false);
			ToolItem forceUpdate = new ToolItem(toolbar, SWT.CHECK);
			addToolItem(ToolBarItemKey.FORCE_UPDATE, forceUpdate);
			forceUpdate.setText("UPDATE");
			forceUpdate
					.setToolTipText("When selected, as moves are made in the game the board will be refreshed.\n"
							+ "When unselected this will not occur, and you have to use the navigation\n"
							+ "buttons to traverse the game. This is useful when you are looking at a previous\n"
							+ "move and don't want the position to update as new moves are being made.");
			forceUpdate.setSelection(true);
			forceUpdate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (isToolItemSelected(ToolBarItemKey.FORCE_UPDATE)) {
						cursor.setMode(Mode.MakeMovesOnMasterSetCursorToLast);
						cursor.setCursorMasterLast();
						refresh();
					} else {
						cursor.setMode(Mode.MakeMovesOnMaster);
					}
					refresh();
				}
			});

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
		return toolbar;
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

	@Override
	public void userInitiatedMove(int square, boolean isDnd) {
		if (!isDisposed()) {
			LOG.debug("moveInitiated" + getGame().getId() + " " + square + " "
					+ isDnd);
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
				|| ChessBoardUtils.isPieceJailSquare(toSquare)
				|| board.getSquare(fromSquare).getPiece() == EMPTY) {
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
		boolean isColoredPieceWhite = GameUtils.isWhitePiece(fromColoredPiece);
		String san = GameUtils.getPseudoSan(getGame(), fromSquare, toSquare,
				false);

		if (isColoredPieceWhite && isPartnerWhite || !isColoredPieceWhite
				&& !isPartnerWhite) {
			connector.sendMessage(connector.getPartnerTellPrefix()
					+ " I suggest " + san);
		} else {
			connector.sendMessage(connector.getPartnerTellPrefix() + " Watch "
					+ san);
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

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}
}