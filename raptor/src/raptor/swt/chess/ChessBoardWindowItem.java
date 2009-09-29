package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;

public class ChessBoardWindowItem implements RaptorWindowItem {
	static final Log LOG = LogFactory.getLog(ChessBoardWindowItem.class);

	ChessBoard board;
	ChessBoardController controller;

	public ChessBoardWindowItem(ChessBoardController controller) {
		this.controller = controller;
	}

	public boolean confirmReparenting() {
		return true;
	}

	public void dispose() {
		board.dispose();
	}

	public Composite getControl() {
		return board;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_GAME_QUADRANT);
	}

	public String getTitle() {
		return controller.getTitle();
	}

	public void init(Composite parent) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initing ChessBoardWindowItem");
		}
		long startTime = System.currentTimeMillis();
		board = new ChessBoard(parent, SWT.NONE);
		board.setController(controller);
		controller.setBoard(board);
		board.createControls();
		controller.init();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Inited window item in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public boolean isCloseable() {
		return controller.isCloseable();
	}

	public void onActivate() {
		board.getDisplay().asyncExec(new Runnable() {
			public void run() {
				controller.onActivate();
			}
		});
	}

	public void onPassivate() {
		board.getDisplay().asyncExec(new Runnable() {
			public void run() {
				controller.onPassivate();
			}
		});
	}

	public void onReparent(Composite newParent) {
		// Grab the controller from the board because
		// controllers can be changed during a game.
		controller = board.getController();
		board.setController(null);
		controller.onPreReparent();
		controller.setBoard(null);
		board.setVisible(false);
		board.dispose();
		board = new ChessBoard(newParent, SWT.NONE);
		board.setController(controller);
		controller.setBoard(board);
		board.createControls();
		controller.onPostReparent();
		board.forceFocus();
	}
}
