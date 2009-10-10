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
	protected Table movesTable;
	protected TableCursor cursor;

	public ChessBoardController getChessBoardController() {
		return controller;
	}

	public Composite getControl(Composite parent) {
		if (movesTable == null) {
			createControls(parent);
		}
		return movesTable;
	}

	public int getPreferredWeight() {
		return 15;
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

			if (i + 1 < game.getMoveList().getSize()) {
				item
						.setText(new String[] {
								""
										+ moveNumber
										+ ") "
										+ GameUtils.convertSanToUseUnicode(game
												.getMoveList().get(i)
												.toString(), true),
								GameUtils.convertSanToUseUnicode(game
										.getMoveList().get(i + 1).toString(),
										false) });
			} else {
				item
						.setText(new String[] {
								""
										+ moveNumber
										+ ") "
										+ GameUtils.convertSanToUseUnicode(game
												.getMoveList().get(i)
												.toString(), true), "" });
			}
		}

		select(game.getMoveList().getSize() - 1);
		cursor.setVisible(true);

		LOG.info("Updated to game in : "
				+ (System.currentTimeMillis() - startTime));
	}

	public void select(int halfMoveIndex) {
		if (movesTable.getItemCount() == 0) {
			return;
		} else if (halfMoveIndex < 0) {
			halfMoveIndex = 0;
		} else if (halfMoveIndex > movesTable.getItemCount()) {
			halfMoveIndex = movesTable.getItemCount() - 1;
		}

		int row = halfMoveIndex / 2;
		int column = halfMoveIndex % 2 == 0 ? 0 : 1;
		cursor.setSelection(row, column);
	}

	protected void createControls(Composite parent) {
		movesTable = new Table(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.SINGLE);
		TableColumn whiteMove = new TableColumn(movesTable, SWT.LEFT);
		TableColumn blackMove = new TableColumn(movesTable, SWT.LEFT);

		whiteMove.setWidth(80);
		blackMove.setWidth(60);

		whiteMove.setText("White");
		blackMove.setText("Black");

		movesTable.setHeaderVisible(false);

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
				System.err.println("In widget selected");
				int halfMoveIndex = movesTable.getSelectionIndex() * 2
						+ cursor.getColumn() + 1;
				controller.userClickedOnMove(halfMoveIndex);
			}

		});
	}
}
