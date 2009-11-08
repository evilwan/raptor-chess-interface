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
package raptor.swt.chess.movelist;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.chess.Game;
import raptor.chess.util.GameUtils;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardMoveList;

/**
 * A move list that just shows a simple 2 column table. The table is traversable
 * by mouse and by keystrokes.
 */
public class SimpleMoveList implements ChessBoardMoveList {
	private static final Log LOG = LogFactory.getLog(SimpleMoveList.class);

	protected ChessBoardController controller;
	protected TableCursor cursor;
	protected boolean ignoreSelection;
	protected Composite composite;
	protected Table movesTable;

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		TableItem[] items = movesTable.getItems();
		for (TableItem item : items) {
			item.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Composite create(Composite parent) {
		if (movesTable == null) {
			createControls(parent);
		}
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceRedraw() {
		cursor.setVisible(true);
		updateToGame();
		select(getChessBoardController().getGame().getMoveList().getSize());
	}

	/**
	 * {@inheritDoc}
	 */
	public ChessBoardController getChessBoardController() {
		return controller;
	}

	/**
	 * {@inheritDoc}
	 */
	public Composite getControl() {
		return composite;
	}

	/**
	 * {@inheritDoc}
	 */
	public void select(int halfMoveIndex) {
		if (composite.isVisible()) {
			if (movesTable.getItemCount() == 0) {
				return;
			}

			ignoreSelection = true;

			if (halfMoveIndex > 0) {
				halfMoveIndex = halfMoveIndex - 1;
			}

			if (movesTable.getItemCount() == 0) {
				return;
			} else if (halfMoveIndex < 0) {
				halfMoveIndex = 0;
			} else if (halfMoveIndex / 2 > movesTable.getItemCount()) {
				halfMoveIndex = 0;
			}

			int row = halfMoveIndex / 2;
			int column = halfMoveIndex % 2 == 0 ? 0 : 1;

			if (row > movesTable.getItemCount() - 1) {
				row = movesTable.getItemCount() - 1;
			}

			movesTable.select(row);
			movesTable.redraw();
			cursor.setSelection(row, column);

			try {
				cursor.redraw();
			} catch (NullPointerException npe) {
				// Hoping this fixes the linux issue along with the redraw
				// above.
			}

			ignoreSelection = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateToGame() {
		long startTime = System.currentTimeMillis();
		if (composite.isVisible()) {
			Game game = controller.getGame();
			TableItem[] items = movesTable.getItems();

			int moveListSize = game.getMoveList().getSize();
			int itemsInHalfMoves = items.length * 2;

			if (moveListSize == 0) {
				clear();
			} else {
				if (itemsInHalfMoves > 0
						&& StringUtils.isBlank(items[items.length - 1]
								.getText(1))) {
					itemsInHalfMoves--;
				}

				if (moveListSize == itemsInHalfMoves) {
					return;
				} else {
					if (moveListSize < itemsInHalfMoves) {
						// Full refresh.
						clear();
						itemsInHalfMoves = 0;
					}
					// Just append the new moves.
					while (moveListSize > itemsInHalfMoves) {
						appendMove(game, itemsInHalfMoves);
						itemsInHalfMoves++;
					}
				}
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updated to game in : "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * Appends the move at the specified half move number to the movesTable.
	 */
	protected void appendMove(Game game, int halfMoveNumber) {
		int currentRow = halfMoveNumber / 2;
		if (halfMoveNumber % 2 != 0) {
			TableItem item = movesTable.getItem(currentRow);
			item.setText(1, GameUtils.convertSanToUseUnicode(game.getMoveList()
					.get(halfMoveNumber).toString(), false));
		} else {
			TableItem item = new TableItem(movesTable, SWT.NONE);
			int moveNumber = currentRow + 1;
			item.setText(new String[] {
					""
							+ moveNumber
							+ ") "
							+ GameUtils.convertSanToUseUnicode(game
									.getMoveList().get(halfMoveNumber)
									.toString(), true), "" });
		}
	}

	/**
	 * Creates all of the controls.
	 */
	protected void createControls(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());
		movesTable = new Table(composite, SWT.SINGLE | SWT.V_SCROLL);
		final TableColumn whiteMove = new TableColumn(movesTable, SWT.LEFT);
		final TableColumn blackMove = new TableColumn(movesTable, SWT.LEFT);

		whiteMove.setWidth(90);
		blackMove.setWidth(70);

		whiteMove.setText("White");
		blackMove.setText("Black");

		movesTable.setHeaderVisible(true);

		movesTable.addListener(SWT.EraseItem, new Listener() {
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) != 0) {
					event.detail &= ~SWT.SELECTED;
				}
			}
		});

		cursor = new TableCursor(movesTable, SWT.NONE);

		// Adds a selection listener that fires userClickedOnMove on the
		// controller.
		cursor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!ignoreSelection) {
					controller
							.userSelectedMoveListMove(getCursorHalfMoveIndex());
				}
			}
		});

		// Adds up/down arrow key listener.
		cursor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP) {
					select(getCursorHalfMoveIndex() - 2);
					controller
							.userSelectedMoveListMove(getCursorHalfMoveIndex());

				} else if (e.keyCode == SWT.ARROW_DOWN) {
					select(getCursorHalfMoveIndex() + 2);
					controller
							.userSelectedMoveListMove(getCursorHalfMoveIndex());
				}
			}
		});
	}

	/**
	 * Returns the cursors current half move index.
	 * 
	 * @return
	 */
	protected int getCursorHalfMoveIndex() {
		return movesTable.getSelectionIndex() * 2 + cursor.getColumn() + 1;
	}
}
