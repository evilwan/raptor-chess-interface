package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.layout.RightOrientedLayout;

public class ChessBoards extends Composite {
	private static final Log LOG = LogFactory.getLog(ChessBoards.class);
	protected CTabFolder folder;

	protected GameServiceListener gameServiceListener = new GameServiceListener() {
		public void gameInactive(Game game) {
		}

		public void gameStateChanged(Game game) {
		}

		public void gameCreated(final Game game) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					ChessBoardController controller = null;
					String title = null;

					if ((game.getState() & Game.OBSERVING_STATE) != 0
							|| (game.getState() & Game.OBSERVING_EXAMINED_STATE) != 0) {
						controller = new ObserveController();
						title = game.getEvent();
						ChessBoard result = add(game, controller, Raptor
								.getInstance().getFicsConnector(), title, true);

						result.addDisposeListener(new DisposeListener() {
							public void widgetDisposed(DisposeEvent e) {
								if (Raptor.getInstance().getFicsConnector()
										.isConnected()
										&& (game.getState() & Game.ACTIVE_STATE) != 0) {
									Raptor.getInstance().getFicsConnector()
											.onUnobserve(game);
								}

							}
						});

					} else {
						LOG
								.error("Could not find controller type for game state. "
										+ "Ignoring game. state= "
										+ game.getState());
					}
				}
			});

		}
	};

	public ChessBoard add(Game game, ChessBoardController controller,
			Connector connector, String title, boolean isCloseable) {
		CTabItem item = new CTabItem(folder, SWT.CLOSE);
		ChessBoard board = new ChessBoard(folder, SWT.NONE);
		board.setGame(game);
		board.setConnector(connector);
		board.setController(controller);
		board.setBoardLayout(new RightOrientedLayout(board));
		board.setPreferences(Raptor.getInstance().getPreferences());
		board.setResources(new ChessBoardResources(board));
		board.createControls();
		controller.setBoard(board);
		board.getController().init();
		board.getController().adjustToGameInitial();

		item.setControl(board);
		item.setText(title);

		folder.setSelection(item);
		return board;
	}

	public ChessBoards(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

		Raptor.getInstance().getFicsConnector().getGameService()
				.addGameServiceListener(gameServiceListener);

		folder = new CTabFolder(this, SWT.BORDER);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(false);
		folder.setUnselectedCloseVisible(false);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		folder.setMaximizeVisible(true);

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void maximize(CTabFolderEvent event) {
				Raptor.getInstance().getAppWindow().maximizeChessBoards();
			}

			public void restore(CTabFolderEvent event) {
				Raptor.getInstance().getAppWindow().restore();
			}
		});

		folder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				System.err.println("Mouse double click " + e.count);
				if (e.count == 2) {
					if (isMaximized()) {
						Raptor.getInstance().getAppWindow().restore();
					} else {
						Raptor.getInstance().getAppWindow()
								.maximizeChessBoards();
					}
				}
				super.mouseDoubleClick(e);
			}

		});
		pack();
	}

	public void dispose() {
		Raptor.getInstance().getFicsConnector().getGameService()
				.removeGameServiceListener(gameServiceListener);
		super.dispose();
	}

	public boolean isMaximized() {
		return folder.getMaximized();
	}

	public void restore() {
		folder.setMaximized(false);
	}

	public void maximize() {
		folder.setMaximized(true);
	}

}
