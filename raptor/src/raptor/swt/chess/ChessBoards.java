package raptor.swt.chess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.chess.ChessBoard.ChessBoardListener;
import raptor.swt.chess.layout.RightOrientedLayout;

public class ChessBoards extends Composite {
	private static final Log LOG = LogFactory.getLog(ChessBoards.class);
	protected CTabFolder folder;

	protected GameServiceListener gameServiceListener = new GameServiceAdapter() {
		@Override
		public void gameCreated(final Game game) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					ChessBoardController controller = Utils
							.buildController(game);

					if (controller == null) {
						Raptor
								.getInstance()
								.getFicsConnector()
								.onError(
										"Game type or action is not currenrly supported in ChessBoards. gameDescription"
												+ game.getGameDescription()
												+ " gameState"
												+ game.getState());
						return;
					}

					add(game, controller, Raptor.getInstance()
							.getFicsConnector());

				}
			});
		}
	};

	protected ChessBoardListener chessBoardListener = new ChessBoardListener() {
		public void onControllerStateChange() {
			for (int i = 0; i < folder.getItemCount(); i++) {
				CTabItem item = folder.getItem(i);
				ChessBoard board = (ChessBoard) item.getControl();
				item.setShowClose(board.getController().isCloseable());
				item.setText(board.getController().getTitle());
			}
		}
	};

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
			@Override
			public void close(CTabFolderEvent event) {
				ChessBoard currentBoard = getCurrentBoard();
				if (currentBoard.getController().onClose()) {
					currentBoard.dispose();
				} else {
					LOG.debug("Close vetoed by controller.");
					event.doit = false;
				}

				if (folder.getItemCount() == 1) {
					Raptor.getInstance().getAppWindow().maximizeChatConsoles();
				}
			}

			@Override
			public void maximize(CTabFolderEvent event) {
				Raptor.getInstance().getAppWindow().maximizeChessBoards();
			}

			@Override
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

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 3) {
					Menu menu = new Menu(folder.getShell(), SWT.POP_UP);
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("Comming soon.");
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							System.out.println("Item Selected");
						}
					});
					menu.setLocation(folder.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!folder.getDisplay().readAndDispatch())
							folder.getDisplay().sleep();
					}
					menu.dispose();
				}

			}

			@Override
			public void mouseUp(MouseEvent e) {
				System.err.println("Mouse up " + e.button);
				super.mouseUp(e);
			}
		});
		pack();
	}

	public ChessBoard add(Game game, ChessBoardController controller,
			Connector connector) {
		CTabItem item = new CTabItem(folder,
				controller.isCloseable() ? SWT.CLOSE : SWT.NONE);
		ChessBoard board = new ChessBoard(folder, SWT.NONE);
		board.setGame(game);
		board.setConnector(connector);
		board.setController(controller);
		board.setBoardLayout(new RightOrientedLayout(board));
		board.setPreferences(Raptor.getInstance().getPreferences());
		board.setResources(new ChessBoardResources(board));
		board.createControls();
		board.addChessBoardListener(chessBoardListener);
		controller.setBoard(board);
		board.getController().init();

		item.setControl(board);
		item.setText(controller.getTitle());

		folder.setSelection(item);

		if (Raptor.getInstance().getAppWindow().isChatConsolesMaximized()) {
			Raptor.getInstance().getAppWindow().restore();
		}
		return board;
	}

	@Override
	public void dispose() {
		Raptor.getInstance().getFicsConnector().getGameService()
				.removeGameServiceListener(gameServiceListener);
		super.dispose();
	}

	public ChessBoard getCurrentBoard() {
		CTabItem item = folder.getSelection();
		if (item != null) {
			return (ChessBoard) item.getControl();
		} else {
			return null;
		}
	}

	public boolean isMaximized() {
		return folder.getMaximized();
	}

	public void maximize() {
		folder.setMaximized(true);
	}

	public void restore() {
		folder.setMaximized(false);
	}

}
