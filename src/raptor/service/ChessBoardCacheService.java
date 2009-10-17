package raptor.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import raptor.Raptor;
import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.controller.InactiveController;

/**
 * A service used to cache chess board objects so they can be reused. This
 * service is currently experimental. If it works out well I will keep it
 * around.
 */
public class ChessBoardCacheService {
	static final Log LOG = LogFactory.getLog(ChessBoardCacheService.class);

	protected static final int CHESS_BOARD_CACHE_SIZE = 15;

	protected static final ChessBoardCacheService singletonInstance = new ChessBoardCacheService();

	/**
	 * An invisible composite Raptor uses to tie cached chess boards to.
	 */
	protected Composite composite;
	/**
	 * A cache of chess boards. Instead of disposing chess board a maximum of
	 * CHESS_BOARD_CACHE_SIZE are cached and reused. This is experimental.
	 */
	protected List<ChessBoard> chessBoardCache = new ArrayList<ChessBoard>(
			CHESS_BOARD_CACHE_SIZE);

	public static ChessBoardCacheService getInstance() {
		return singletonInstance;
	}

	private ChessBoardCacheService() {
		init();
	}

	public void dispose() {
		composite.dispose();
		chessBoardCache.clear();
	}

	/**
	 * Returns a chess board to use if one is one is available. Otherwise
	 * returns null. You will need to set the parent on returned chess board if
	 * it is not null.
	 */
	public ChessBoard getChessBoard() {
		if (chessBoardCache.isEmpty()) {
			return null;
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info("Returned a cached chess board.");
			}
			ChessBoard board = chessBoardCache
					.remove(chessBoardCache.size() - 1);
			board.getControl().setLayoutDeferred(false);
			return board;
		}
	}

	/**
	 * Recycles the chess board. Disposes of it if the cache is already full.
	 */
	public void recycle(final ChessBoard board) {
		long startTime = System.currentTimeMillis();
		if (chessBoardCache.size() < CHESS_BOARD_CACHE_SIZE) {
			if (board.getControl().getParent() != composite) {
				// Always dispose the controller so it can clean
				// up anything
				// being used on the board.
				// It will also possibly send messages to a
				// connector on
				// dispose.
				board.getController().dispose();
				ThreadService.getInstance().scheduleOneShot(3000,
						new Runnable() {
							public void run() {
								board.getControl().setLayoutDeferred(true);
								board.getControl().setParent(composite);
								board.hideMoveList();
								chessBoardCache.add(0, board);
							}
						});
			}
		} else {
			// It is'nt being cached so kill it off.
			board.getControl().dispose();
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Recycled a chess board in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected void init() {
		long startTime = System.currentTimeMillis();
		composite = new Shell(Raptor.getInstance().getDisplay());
		composite.setVisible(false);

		for (int i = 0; i < CHESS_BOARD_CACHE_SIZE; i++) {
			ChessBoard board = new ChessBoard();
			Game emptyGame = GameFactory
					.createStartingPosition(Variant.classic);
			ChessBoardController controller = new InactiveController(emptyGame);
			board.setController(controller);
			controller.setBoard(board);
			board.createControls(composite);
			controller.init();
			board.redrawSquares();
			chessBoardCache.add(board);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("Initialized ChessBoardCacheService in "
					+ (System.currentTimeMillis() - startTime));
		}
	}
}
