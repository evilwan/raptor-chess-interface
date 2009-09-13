package raptor.gui.board;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import raptor.game.Game;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.service.GameService;
import raptor.service.PreferenceService;
import raptor.service.SWTService;
import raptor.service.SoundService;

public class ChessBoardWindow {
	Log LOG = LogFactory.getLog(ChessBoardWindow.class);
	static final String DRAG_INITIATOR = "DRAG_INITIATOR";

	boolean isWhiteOnTop = false;
	boolean isShowingCoordinates = true;
	boolean dragSuccessful = false;
	boolean forceLayout = false;
	int storedPiece = Set.EMPTY;
	ChessBoard board = null;
	Image dragIcon = null;
	String gameId;
	Shell shell = null;

	class ChessBoard extends Composite {
		class ChessBoardSquare extends Composite {
			int id;
			boolean isLight;
			int piece;
			Label label;
			DropTarget target;
			DragSource source;
			FillLayout layout = new FillLayout();

			ControlListener controlListener = new ControlListener() {

				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					if (label.getBackgroundImage() != null) {
						label.getBackgroundImage().dispose();
					}
					label.setBackgroundImage(getSquareBackground()
							.getScaledImage(isLight, getSize().x, getSize().y));

					if (label.getImage() != null) {
						label.getImage().dispose();
					}
					label.setImage(getSet().getScaledImage(piece, getSize().x ,
							getSize().y ));
					label.setAlignment(SWT.CENTER);
				}
			};

			public ChessBoardSquare(int id, boolean isLight) {
				super(ChessBoard.this, 0);
				this.id = id;
				addControlListener(controlListener);
				label = new Label(this, SWT.CENTER | SWT.VERTICAL);
				//label.setAlignment();
				setLayout(layout);
				
				
				this.isLight = isLight;

				source = new DragSource(label, DND.DROP_MOVE);
				source.setDragSourceEffect(new DragSourceEffect(label) {
					public void dragStart(DragSourceEvent event) {
						if (piece == Set.EMPTY) {

						} else {
							dragIcon = event.image = getSet().getIcon(piece);
						}
					}
				});

				source
						.setTransfer(new Transfer[] { org.eclipse.swt.dnd.TextTransfer
								.getInstance() });
				source.addDragListener(new DragSourceAdapter() {
					public void dragStart(DragSourceEvent event) {
						if (piece == Set.EMPTY) {
							event.doit = false;
						} else {
							event.doit = true;
							event.detail = DND.DROP_MOVE;
							ChessBoard.this.setData(DRAG_INITIATOR,
									ChessBoardSquare.this);

							dragSuccessful = false;
							storedPiece = piece;
							setPiece(Set.EMPTY);
						}
					}

					public void dragSetData(DragSourceEvent event) {
						event.data = "" + piece;
					}

					public void dragFinished(DragSourceEvent event) {
						if (event.doit && dragSuccessful) {
							setPiece(Set.EMPTY);
						} else {
							setPiece(storedPiece);
						}
						dragIcon.dispose();
					}
				});

				target = new DropTarget(label, DND.DROP_MOVE);
				target
						.setTransfer(new Transfer[] { TextTransfer
								.getInstance() });
				target.addDropListener(new DropTargetAdapter() {
					public void dragEnter(DropTargetEvent event) {
					}

					public void dragOperationChanged(DropTargetEvent event) {
					}

					public void dragOver(DropTargetEvent event) {
					}

					public void drop(DropTargetEvent event) {
						if (event.detail != DND.DROP_NONE) {
							ChessBoardSquare start = (ChessBoardSquare) ChessBoard.this
									.getData(DRAG_INITIATOR);
							Move move = makeMove(start.id,
									ChessBoardSquare.this.id);
							if (move != null) {
								setPiece(storedPiece);
								ChessBoard.this.setData(DRAG_INITIATOR, null);
								dragSuccessful = true;
							} else {
								dragSuccessful = false;
							}
						}
					}
				});
			}

			public void dispose() {
				label.getImage().dispose();
				label.getBackground().dispose();
				removeControlListener(controlListener);
				source.dispose();
				target.dispose();
				super.dispose();
			}

			public int getId() {
				return id;
			}

			public boolean isLight() {
				return isLight;
			}

			public void setLight(boolean isLight) {
				this.isLight = isLight;
			}

			public int getPiece() {
				return piece;
			}

			public void setPiece(int piece) {
				if (forceLayout) {
					this.piece = piece;
					if (label.getImage() != null) {
						label.getImage().dispose();
					}
					
					label.setImage(getSet().getScaledImage(piece, getSize().x ,
							getSize().y ));
					label.setAlignment(SWT.CENTER);
					
					layout();
				} else if (this.piece != piece) {
					this.piece = piece;
					if (label.getImage() != null) {
						label.getImage().dispose();
					}
					if (getSize() != null) {
						label.setImage(getSet().getScaledImage(piece,
								getSize().x, getSize().y ));
						label.setAlignment(SWT.CENTER);
					}


					layout();
				}
			}

			public void highlight() {

			}

			public void unhighlight() {

			}

			public Label getLabel() {
				return label;
			}
		}

		class ChessBoardLayout extends Layout {

			@Override
			protected Point computeSize(Composite composite, int hint,
					int hint2, boolean flushCache) {
				return null;
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				int width = composite.getSize().x;
				int height = composite.getSize().y;

				@SuppressWarnings("unused")
				int x, y, xInit, coordinatesHeight, squareSide;

				if (isShowingCoordinates) {
					squareSide = width > height ? height / 8 : width / 8;

					int charWidth = 20;
					int charHeight = 20;

					squareSide -= Math.round(charWidth / 8.0);

					x = charWidth;
					xInit = charWidth;
					y = 0;

					for (int i = 0; i < board.rankLabels.length; i++) {
						int multiplier = (isWhiteOnTop ? 7 - i : i);
						board.rankLabels[i]
								.setLocation(0, (int) (squareSide * multiplier
										+ squareSide / 2 - .4 * charHeight));
						board.rankLabels[i].setSize(charWidth, charHeight);
					}

					for (int i = 0; i < board.fileLabels.length; i++) {
						int multiplier = (isWhiteOnTop ? 7 - i : i);
						board.fileLabels[i].setLocation((int) (charHeight * .4
								+ squareSide * multiplier + squareSide / 2),
								(int) (squareSide * 8));
						board.fileLabels[i].setSize(charWidth, charHeight);
					}

					coordinatesHeight = charHeight;

				} else {
					squareSide = width > height ? height / 8 : width / 8;
					x = 0;
					xInit = 0;
					y = 0;
					coordinatesHeight = 0;
				}

				if (!isWhiteOnTop) {
					for (int i = 7; i > -1; i--) {
						for (int j = 7; j > -1; j--) {
							board.squares[i][j].setBounds(x, y, squareSide,
									squareSide);

							x += squareSide;
						}
						x = xInit;
						y += squareSide;

					}
				} else {
					for (int i = 0; i < 8; i++) {
						for (int j = 0; j < board.squares[i].length; j++) {
							board.squares[i][j].setBounds(x, y, squareSide,
									squareSide);

							x += squareSide;
						}
						x = xInit;
						y += squareSide;
					}
				}

			}
		}

		ChessBoardSquare[][] squares = new ChessBoardSquare[8][8];
		Label[] rankLabels = new Label[8];
		Label[] fileLabels = new Label[8];

		public ChessBoard(Composite parent, int style) {
			super(parent, style);
			setLayout(new ChessBoardLayout());

			int labelStyle = SWT.CENTER | SWT.SHADOW_NONE;
			for (int i = 8; i > 0; i--) {
				rankLabels[i - 1] = new Label(this, labelStyle);
				rankLabels[i - 1].setText("" + i);
			}

			rankLabels[0] = new Label(this, labelStyle);
			rankLabels[0].setText("8");
			rankLabels[1] = new Label(this, labelStyle);
			rankLabels[1].setText("7");
			rankLabels[2] = new Label(this, labelStyle);
			rankLabels[2].setText("6");
			rankLabels[3] = new Label(this, labelStyle);
			rankLabels[3].setText("5");
			rankLabels[4] = new Label(this, labelStyle);
			rankLabels[4].setText("4");
			rankLabels[5] = new Label(this, labelStyle);
			rankLabels[5].setText("3");
			rankLabels[6] = new Label(this, labelStyle);
			rankLabels[6].setText("2");
			rankLabels[7] = new Label(this, labelStyle);
			rankLabels[7].setText("1");

			fileLabels[0] = new Label(this, labelStyle);
			fileLabels[0].setText("a");
			fileLabels[1] = new Label(this, labelStyle);
			fileLabels[1].setText("b");
			fileLabels[2] = new Label(this, labelStyle);
			fileLabels[2].setText("c");
			fileLabels[3] = new Label(this, labelStyle);
			fileLabels[3].setText("d");
			fileLabels[4] = new Label(this, labelStyle);
			fileLabels[4].setText("e");
			fileLabels[5] = new Label(this, labelStyle);
			fileLabels[5].setText("f");
			fileLabels[6] = new Label(this, labelStyle);
			fileLabels[6].setText("g");
			fileLabels[7] = new Label(this, labelStyle);
			fileLabels[7].setText("h");
		}

		public Label[] getRankLabels() {
			return rankLabels;
		}

		public void setRankLabels(Label[] rankLabels) {
			this.rankLabels = rankLabels;
		}

		public Label[] getFileLabels() {
			return fileLabels;
		}

		public void setFileLabels(Label[] fileLabels) {
			this.fileLabels = fileLabels;
		}

		public void init() {
			boolean isWhiteSquare = false;
			for (int i = 0; i < 8; i++) {
				isWhiteSquare = !isWhiteSquare;
				for (int j = 0; j < squares[i].length; j++) {
					squares[i][j] = new ChessBoardSquare(GameUtils
							.rankFileToSquare(i, j), isWhiteSquare);
					isWhiteSquare = !isWhiteSquare;
				}
			}
		}

		public void dispose() {
			for (int i = 0; i < squares.length; i++) {
				for (int j = 0; j < squares[i].length; j++) {
					squares[i][j].dispose();
				}
			}

			for (int i = 0; i < rankLabels.length; i++) {
				rankLabels[i].dispose();
				fileLabels[i].dispose();
			}
		}
	}

	public void updateFromPrefs() {
		setShowingCoordinates(PreferenceService.getInstance().getConfig()
				.getBoolean(PreferenceService.SHOW_COORDINATES_KEY, true));

		forceLayout = true;
		updateFromGame();
		forceLayout = false;
		if (shell.isVisible()) {
			board.layout();
		}
	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public void setWhiteOnTop(boolean isWhiteOnTop) {
		if (this.isWhiteOnTop != isWhiteOnTop) {
			this.isWhiteOnTop = isWhiteOnTop;
			if (shell.isVisible()) {
				board.layout();
			}
		}
	}

	public boolean isShowingCoordinates() {
		return isShowingCoordinates;
	}

	public void setShowingCoordinates(boolean isShowingCoordinates) {
		if (this.isShowingCoordinates != isShowingCoordinates) {
			this.isShowingCoordinates = isShowingCoordinates;
			if (shell.isVisible()) {
				board.layout();
			}
		}
	}

	public void updateFromGame() {
		Game game = getGame();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].setPiece(Set.getSetPieceFromGamePiece(
						GameUtils.rankFileToSquare(i, j), game));
			}
		}
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	private Set getSet() {
		return SWTService.getInstance().getSet();
	}

	private Background getSquareBackground() {
		return SWTService.getInstance().getSquareBackground();
	}

	private Game getGame() {
		Game game = GameService.getInstance().getGame(gameId);
		if (game == null) {
			LOG.error("Could not find game with id " + gameId);
			throw new IllegalStateException("Game not found " + game.getId());
		}
		return game;
	}

	private Move makeMove(int startSquare, int endSquare) {
		Game game = getGame();
		try {
			Move move = game.makeMove(startSquare, endSquare);
			GameService.getInstance().notifyUserMove(game.getId(), move);
			LOG.info(game.getId() + " " + move.getLan());
			return move;

		} catch (IllegalArgumentException iae) {
			LOG.warn("Illegal move encountered " + startSquare + " "
					+ endSquare, iae);
			SoundService.getInstance().play(SoundService.GAME_PLAY_ILLEGAL);
			return null;
		}
	}

	public ChessBoardWindow(String gameId) {
		this.gameId = gameId;
		shell = SWTService.getInstance().createShell();
		shell.setLayout(new FillLayout());

		board = new ChessBoard(shell, SWT.VIRTUAL | SWT.BORDER);
		board.init();
		updateFromPrefs();

		shell.open();
	}

	public void dispose() {
		board.dispose();
	}

	public static void main(String[] args) {

		Game game = GameUtils.createStartingPosition();
		game.setId("1");
		GameService.getInstance().addGame(game);
		ChessBoardWindow window = new ChessBoardWindow("1");
		SWTService.getInstance().start();
	}
}
