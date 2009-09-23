package raptor.swt.chess;

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

import raptor.Raptor;
import raptor.connector.Connector;
import raptor.game.Game;
import raptor.swt.chess.layout.RightOrientedLayout;

public class ChessBoards extends Composite {
	CTabFolder folder;

	public void add(Game game, ChessBoardController controller,
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
		item.setText(game.getId());

		folder.setSelection(item);
	}

	public ChessBoards(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

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
				// TODO Auto-generated method stub
				super.mouseDoubleClick(e);
			}

		});
		pack();
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
