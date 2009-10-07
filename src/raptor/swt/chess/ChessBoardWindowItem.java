package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;
import raptor.swt.ItemChangedListener;

public class ChessBoardWindowItem implements RaptorWindowItem {
	static final Log LOG = LogFactory.getLog(ChessBoardWindowItem.class);

	ChessBoard board;

	// This is just added as a member variable so it can be stored form the time
	// its constructed until the time init is invoked.
	// It should never be referenced after that. Always use
	// board.getController()
	// so controller swapping can occur.
	ChessBoardController controller;

	public ChessBoardWindowItem(ChessBoardController controller) {
		this.controller = controller;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
		controller.addItemChangedListener(listener);
	}

	public boolean confirmClose() {
		return board.getController().confirmClose();
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		board.dispose();
	}

	public Composite getControl() {
		return board;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getCurrentLayoutQuadrant(
				PreferenceKeys.GAME_QUADRANT);
	}

	public String getTitle() {
		return board.getController().getTitle();
	}

	public Control getToolbar(Composite parent) {
		return board.getController().getToolbar(parent);
	}

	public void init(Composite parent) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Initing ChessBoardWindowItem");
		}
		long startTime = System.currentTimeMillis();
		board = new ChessBoard(parent);
		board.setLayoutDeferred(true);
		board.setController(controller);
		controller.setBoard(board);
		board.createControls();
		controller.init();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Inited window item in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	public void onActivate() {
		board.setLayoutDeferred(false);
		board.layout(true);
		board.getDisplay().asyncExec(new Runnable() {
			public void run() {
				board.getController().onActivate();
			}
		});
	}

	public void onPassivate() {
		board.setLayoutDeferred(true);
		board.getDisplay().asyncExec(new Runnable() {
			public void run() {
				board.getController().onPassivate();
			}
		});
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
		board.getController().removeItemChangedListener(listener);
	}
}
