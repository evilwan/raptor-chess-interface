package raptor.swt.chess.movelist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

public class SimpleMoveList implements ChessBoardMoveList {
	private static final Log LOG = LogFactory.getLog(SimpleMoveList.class);

	protected ChessBoardController controller;
	protected TableCursor cursor;
	protected boolean ignoreSelection;
	protected Table movesTable;

	public Composite create(Composite parent) {
		if (movesTable == null) {
			createControls(parent);
		}
		return movesTable;
	}

	public void forceRedraw() {
		cursor.setVisible(true);
		cursor.redraw();
		movesTable.layout(true, true);

	}

	public ChessBoardController getChessBoardController() {
		return controller;
	}

	public Composite getControl() {
		return movesTable;
	}

	public void select(int halfMoveIndex) {
		ignoreSelection = true;
		if (movesTable.getItemCount() == 0) {
			return;
		} else if (halfMoveIndex < 0) {
			LOG.warn("Received invalid halfMoveIndex " + halfMoveIndex
					+ " Set halfMoveIndex to 0");
			halfMoveIndex = 0;
		} else if (halfMoveIndex / 2 > movesTable.getItemCount()) {
			LOG.warn("Received invalid halfMoveIndex adjust and set to "
					+ halfMoveIndex + " Set halfMoveIndex to 0");
			halfMoveIndex = 0;
		}
		if (halfMoveIndex > 0) {
			halfMoveIndex = halfMoveIndex - 1;
		}

		int row = halfMoveIndex / 2;
		int column = halfMoveIndex % 2 == 0 ? 0 : 1;
		cursor.setSelection(row, column);
		ignoreSelection = false;
	}

	public void setController(ChessBoardController controller) {
		this.controller = controller;
	}

	public void updateToGame() {
		long startTime = System.currentTimeMillis();

		Game game = controller.getGame();
		TableItem[] items = movesTable.getItems();
		for (TableItem item : items) {
			item.dispose();
		}
		for (int i = 0; i < game.getMoveList().getSize(); i += 2) {
			TableItem item = new TableItem(movesTable, SWT.NONE);
			int moveNumber = i / 2 + 1;
			String move1 = null;
			String move2 = null;

			if (i + 1 < game.getMoveList().getSize()) {
				move1 = ""
						+ moveNumber
						+ ") "
						+ GameUtils.convertSanToUseUnicode(game.getMoveList()
								.get(i).toString(), true);
				move2 = GameUtils.convertSanToUseUnicode(game.getMoveList()
						.get(i + 1).toString(), false);
			} else {
				move1 = ""
						+ moveNumber
						+ ") "
						+ GameUtils.convertSanToUseUnicode(game.getMoveList()
								.get(i).toString(), true);
				move2 = "";
				;
			}

			item.setText(new String[] { move1, move2 });
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Updated to game in : "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected void createControls(Composite parent) {
		movesTable = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
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
		cursor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!ignoreSelection) {
					int halfMoveIndex = movesTable.getSelectionIndex() * 2
							+ cursor.getColumn() + 1;
					controller.userClickedOnMove(halfMoveIndex);
				}
			}

		});
		cursor.setVisible(true);
	}
}
