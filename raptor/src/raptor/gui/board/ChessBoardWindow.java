package raptor.gui.board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEffect;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import raptor.game.Game;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.gui.board.ChessBoardWindow.ChessBoard.ChessBoardSquare;
import raptor.pref.PreferencesDialog;
import raptor.service.GameService;
import raptor.service.SWTService;

public class ChessBoardWindow extends ApplicationWindow {
	class ChessBoard extends Canvas {
		class ChessBoardLayout extends Layout {

			@Override
			protected Point computeSize(Composite composite, int hint,
					int hint2, boolean flushCache) {
				return new Point(600, 400);
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				int width = composite.getSize().x;
				int height = composite.getSize().y;

				@SuppressWarnings("unused")
				int x, y, xInit, coordinatesHeight, squareSide;

				if (isShowingCoordinates) {
					squareSide = width > height ? height / 8 : width / 8;
					if (squareSide % 2 != 0) {
						squareSide -= 1;
					}

					GC gc = new GC(ChessBoard.this);
					gc.setFont(board.rankLabels[0].getFont());
					Point extent = gc.stringExtent("abcdefgh");

					int charWidth = gc.getFontMetrics().getAverageCharWidth() + 5;
					int charHeight = gc.getFontMetrics().getAscent() + gc.getFontMetrics().getDescent() + 6;
					gc.dispose();

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
					if (squareSide % 2 != 0) {
						squareSide -= 1;
					}

					x = 0;
					xInit = 0;
					y = 0;
					coordinatesHeight = 0;
				}

				if (!isWhiteOnTop) {
					for (int i = 7; i > -1; i--) {
						for (int j = 0; j < 8; j++) {
							board.squares[i][j].setBounds(x, y, squareSide,
									squareSide);

							x += squareSide;
						}
						x = xInit;
						y += squareSide;

					}
				} else {
					for (int i = 0; i < 8; i++) {
						for (int j = 7; j > -1; j--) {
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

		class ChessBoardSquare extends Composite {
			Image backgroundImage;
			Image pieceImage;
			int id;
			boolean isHighlighted;
			boolean isLight;
			int piece;
			boolean isSelected = false;
			DropTarget target = new DropTarget(this, DND.DROP_MOVE);
			DragSource source = new DragSource(this, DND.DROP_MOVE);
			DragSourceEffect dragSourceEffect = new DragSourceEffect(this) {
				public void dragStart(DragSourceEvent event) {
					if (piece == Set.EMPTY) {

					} else {
						dragIcon = event.image = getSet().getIcon(piece);
					}
				}
			};
			DragSourceListener dragSourceListener = new DragSourceAdapter() {
				public void dragFinished(DragSourceEvent event) {
					if (!event.doit) {
						fireMoveCancelled(ChessBoardSquare.this.id, true);
					}
					lastDropTime = System.currentTimeMillis();
					ChessBoard.this.setData(CLICK_INITIATOR, null);
					ChessBoard.this.setData(DRAG_INITIATOR, null);
				}

				public void dragSetData(DragSourceEvent event) {
					event.data = "" + piece;
				}

				public void dragStart(DragSourceEvent event) {
					if (piece == Set.EMPTY) {
						event.doit = false;
					} else {
						event.doit = true;
						event.detail = DND.DROP_MOVE;
						ChessBoard.this.setData(DRAG_INITIATOR,
								ChessBoardSquare.this);
						fireMoveInitiated(ChessBoardSquare.this.id, true);
					}
				}
			};
			DropTargetListener dropTargetListener = new DropTargetAdapter() {
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
						fireMoveMade(start.id, ChessBoardSquare.this.id);
					}
				}

			};

			MouseListener mouseListener = new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
				}

				public void mouseDown(MouseEvent e) {
					if (e.button == 3) {
						fireOnRightClick(id);
					}
				}

				public void mouseUp(MouseEvent e) {
					if (e.button == 2) {
						fireOnMiddleClick(id);
					} else if (e.button == 1
							&& System.currentTimeMillis() - lastDropTime > 100) {
						ChessBoardSquare initiator = (ChessBoardSquare) ChessBoard.this
								.getData(CLICK_INITIATOR);

						if (initiator == null) {// Start of move
							ChessBoard.this.setData(CLICK_INITIATOR,
									ChessBoardSquare.this);
							fireMoveInitiated(id, false);
						} else {
							if (ChessBoardSquare.this == initiator) {// Clicked
								// on
								// same
								// square
								// twice.
								fireMoveCancelled(initiator.id, false);
								ChessBoard.this.setData(CLICK_INITIATOR, null);
							} else if (Set.arePiecesSameColor(piece,
									initiator.piece)) {// Clicked on same piece
								// type twice.
								fireMoveCancelled(initiator.id, false);
								fireMoveInitiated(ChessBoardSquare.this.id,
										false);
								ChessBoard.this.setData(CLICK_INITIATOR,
										ChessBoardSquare.this);
							} else {// A valid move
								fireMoveMade(initiator.id,
										ChessBoardSquare.this.id);
								ChessBoard.this.setData(CLICK_INITIATOR, null);
							}
						}
					}
				}
			};

			ControlListener controlListener = new ControlListener() {

				public void controlMoved(ControlEvent e) {
				}

				public void controlResized(ControlEvent e) {
					forceLayout();
				}
			};

			PaintListener paintListener = new PaintListener() {
				public void paintControl(PaintEvent e) {
					Point size = getSize();
					if (backgroundImage == null
							|| backgroundImage.getBounds().width != getSize().x
							&& backgroundImage.getBounds().height != getSize().y) {
						backgroundImage = getSquareBackground().getScaledImage(
								isLight, size.x, size.y);
					}

					e.gc.drawImage(backgroundImage, 0, 0);

					int highlightBorderWidth = (int) (size.x * SWTService
							.getInstance()
							.getStore()
							.getDouble(
									SWTService.BOARD_HIGHLIGHT_BORDER_WIDTH_KEY));
					if (isHighlighted) {
						for (int i = 0; i < highlightBorderWidth; i++) {
							e.gc.drawRectangle(i, i, size.x - 1 - i * 2, size.x
									- 1 - i * 2);
						}
					}

					double imageSquareSideAdjustment = SWTService.getInstance()
							.getStore().getDouble(
									SWTService.BOARD_PIECE_SIZE_ADJUSTMENT_KEY);
					int imageSide = (int) ((size.x - highlightBorderWidth * 2) * (1.0 - imageSquareSideAdjustment));
					if (imageSide % 2 != 0) {
						imageSide = imageSide - 1;
					}

					if (pieceImage == null && piece != Set.EMPTY) {
						pieceImage = getSet().getScaledImage(piece, imageSide,
								imageSide);
					}

					if (pieceImage != null) {
						int pieceImageX = (size.x - imageSide) / 2;
						int pieceImageY = (size.y - imageSide) / 2;
						e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);
					}
				}
			};

			public ChessBoardSquare(int id, boolean isLight) {
				super(ChessBoard.this, 0);
				setSize(50, 50);
				this.id = id;
				this.isLight = isLight;
				addPaintListener(paintListener);
				addControlListener(controlListener);
				addMouseListener(mouseListener);

				source
						.setTransfer(new Transfer[] { org.eclipse.swt.dnd.TextTransfer
								.getInstance() });
				source.setDragSourceEffect(dragSourceEffect);
				source.addDragListener(dragSourceListener);

				target
						.setTransfer(new Transfer[] { TextTransfer
								.getInstance() });
				target.addDropListener(dropTargetListener);
			}

			public void dispose() {
				removePaintListener(paintListener);
				removeControlListener(controlListener);
				removeMouseListener(mouseListener);
				source.removeDragListener(dragSourceListener);
				target.removeDropListener(dropTargetListener);
				source.dispose();
				target.dispose();
				super.dispose();
			}

			public void forceLayout() {
				backgroundImage = null;
				pieceImage = null;
			}

			public int getId() {
				return id;
			}

			public int getPiece() {
				return piece;
			}

			public void highlight() {
				if (!isHighlighted) {
					isHighlighted = true;
					redraw();
				}
			}

			public boolean isLight() {
				return isHighlighted;
			}

			public void setLight(boolean isLight) {
				this.isHighlighted = isLight;
			}

			public void setPiece(int piece) {
				if (this.piece != piece) {
					LOG.debug("Setting piece in square " + id + " " + piece);
					this.piece = piece;
					pieceImage = null;
					redraw();
				}
			}

			public void unhighlight() {
				if (isHighlighted) {
					isHighlighted = false;
					redraw();
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

		public Label[] getFileLabels() {
			return fileLabels;
		}

		public Label[] getRankLabels() {
			return rankLabels;
		}

		public void init() {
			boolean isWhiteSquare = true;
			for (int i = 0; i < 8; i++) {
				isWhiteSquare = !isWhiteSquare;
				for (int j = 0; j < squares[i].length; j++) {
					squares[i][j] = new ChessBoardSquare(GameUtils
							.rankFileToSquare(i, j), isWhiteSquare);
					isWhiteSquare = !isWhiteSquare;
				}
			}
		}

		public void setFileLabels(Label[] fileLabels) {
			this.fileLabels = fileLabels;
		}

		public void setRankLabels(Label[] rankLabels) {
			this.rankLabels = rankLabels;
		}
	}

	public static interface ChessBoardListener {
		public void moveCancelled(String gameId, int fromSquare, boolean isDnd);

		public void moveInitiated(String gameId, int square, boolean isDnd);

		public void moveMade(String gameId, int fromSquare, int toSquare);

		public void onMiddleClick(String gameId, int square);

		public void onRightClick(String gameId, int square);
	}

	public static void main(String[] args) {
		final Game game = GameUtils.createStartingPosition();
		game.setId("1");
		GameService.getInstance().addGame(game);
		final ChessBoardWindow window = new ChessBoardWindow("1");
		window.addChessBoardListener(new ChessBoardListener() {

			private SecureRandom random = new SecureRandom();

			public void moveCancelled(String gameId, int fromSquare,
					boolean isDnd) {
				window.LOG.debug("moveCancelled" + gameId + " " + fromSquare
						+ " " + isDnd);
				window.unhighlightAllSquares();
				window.updateFromGame();
			}

			public void moveInitiated(String gameId, int square, boolean isDnd) {
				window.LOG.debug("moveInitiated" + gameId + " " + square + " "
						+ isDnd);

				window.unhighlightAllSquares();
				window.getSquare(square).highlight();
				if (isDnd) {
					window.getSquare(square).setPiece(Set.EMPTY);
				}
			}

			public void moveMade(String gameId, int fromSquare, int toSquare) {
				window.LOG.debug("Move made " + gameId + " " + fromSquare + " "
						+ toSquare);
				window.unhighlightAllSquares();
				try {
					Move move = game.makeMove(fromSquare, toSquare);
					window.getSquare(move.getFrom()).highlight();
					window.getSquare(move.getTo()).highlight();
				} catch (IllegalArgumentException iae) {
					window.LOG.info("Move was illegal " + fromSquare + " "
							+ toSquare);
				}
				window.updateFromGame();
			}

			public void onMiddleClick(String gameId, int square) {
				window.LOG.debug("On middle click " + gameId + " " + square);
				Move[] moves = game.getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.getTo() == square) {
						foundMoves.add(move);
					}
				}

				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random
							.nextInt(foundMoves.size()));
					game.move(move);
					window.unhighlightAllSquares();
					window.getSquare(move.getFrom()).highlight();
					window.getSquare(move.getTo()).highlight();
					window.updateFromGame();
				}
			}

			public void onRightClick(String gameId, int square) {
				window.LOG.debug("On right click " + gameId + " " + square);
			}
		});

		window.getToolBarManager2().add(new Action("Flip", SWT.BORDER) {
			@Override
			public void run() {
				super.run();
				window.setWhiteOnTop(!window.isWhiteOnTop);
			}
		});
		window.getToolBarManager2().add(new Action("Preferences", SWT.BORDER) {
			@Override
			public void run() {
				PreferencesDialog dialog = new PreferencesDialog();
				dialog.run();
			}
		});

		window.setBlockOnOpen(true);
		window.open();
		Display.getCurrent().dispose();
	}

	Log LOG = LogFactory.getLog(ChessBoardWindow.class);
	static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	static final String CLICK_INITIATOR = "CLICK_INITIATOR";
	boolean isWhiteOnTop = false;
	boolean isShowingCoordinates = true;
	long lastDropTime;
	int storedPiece = Set.EMPTY;
	ChessBoard board = null;

	Image dragIcon = null;

	String gameId;

	List<ChessBoardListener> chessBoardListeners = new ArrayList<ChessBoardListener>(
			3);;

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg0) {
			updateFromPrefs();
			board.redraw();
		}
	};

	public ChessBoardWindow(String gameId) {
		super(null);
		this.gameId = gameId;
		addToolBar(SWT.HORIZONTAL);
	}

	@Override
	protected Control createContents(Composite parent) {
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 10;
		fillLayout.marginWidth = 10;
		// parent.setLayout(fillLayout);
		board = new ChessBoard(parent, 0);
		board.init();
		board.setSize(600, 400);
		updateFromGame();
		updateFromPrefs();

		SWTService.getInstance().getStore().addPropertyChangeListener(
				propertyChangeListener);
		parent.pack();

		return parent;
	}

	public void addChessBoardListener(ChessBoardListener listener) {
		chessBoardListeners.add(listener);
	}

	public void dispose() {
		board.dispose();
		SWTService.getInstance().getStore().removePropertyChangeListener(
				propertyChangeListener);
		chessBoardListeners.clear();
		LOG.info("Disposed " + gameId);
	}

	public void fireMoveCancelled(int fromSquare, boolean isDnd) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.moveCancelled(gameId, fromSquare, isDnd);
		}
	}

	public void fireMoveInitiated(int square, boolean isDnd) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.moveInitiated(gameId, square, isDnd);
		}
	}

	public void fireMoveMade(int fromSquare, int toSquare) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.moveMade(gameId, fromSquare, toSquare);
		}
	}

	public void fireOnMiddleClick(int square) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.onMiddleClick(gameId, square);
		}
	}

	public void fireOnRightClick(int square) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.onRightClick(gameId, square);
		}
	}

	private Game getGame() {
		Game game = GameService.getInstance().getGame(gameId);
		if (game == null) {
			LOG.error("Could not find game with id " + gameId);
			throw new IllegalStateException("Game not found " + game.getId());
		}
		return game;
	}

	public String getGameId() {
		return gameId;
	}

	private Set getSet() {
		return SWTService.getInstance().getChessSet();
	}

	public ChessBoardSquare getSquare(int square) {
		int rank = square / 8;
		int file = square % 8;
		return board.squares[rank][file];
	}

	private Background getSquareBackground() {
		return SWTService.getInstance().getSquareBackground();
	}

	public boolean isShowingCoordinates() {
		return isShowingCoordinates;
	}

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public void removeChessBoardListener(ChessBoardListener listener) {
		chessBoardListeners.remove(listener);
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public void setShowingCoordinates(boolean isShowingCoordinates) {
		if (this.isShowingCoordinates != isShowingCoordinates) {
			this.isShowingCoordinates = isShowingCoordinates;
			board.layout();
		}
	}

	public void setWhiteOnTop(boolean isWhiteOnTop) {
		if (this.isWhiteOnTop != isWhiteOnTop) {
			this.isWhiteOnTop = isWhiteOnTop;
			board.layout();
		}
	}
	
	protected void initializeBounds(){
		   getShell().setSize(800,600);
		   getShell().setLocation(0,0);
		}


	public void unhighlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].unhighlight();
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

	public void updateFromPrefs() {
		LOG.info("Updating prefs " + gameId);
		long startTime = System.currentTimeMillis();
		setShowingCoordinates(SWTService.getInstance().getStore().getBoolean(
				SWTService.BOARD_IS_SHOW_COORDINATES_KEY));

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].setForeground(SWTService.getInstance()
						.getColor(SWTService.BOARD_HIGHLIGHT_COLOR_KEY));
				board.squares[i][j].forceLayout();
			}
		}

		for (Label label : board.rankLabels) {

			if (isShowingCoordinates()) {

				label.setFont(SWTService.getInstance().getFont(
						SWTService.BOARD_COORDINATES_FONT));
				label.setForeground(SWTService.getInstance().getColor(
						SWTService.BOARD_COORDINATES_COLOR));
				label.setBackground(SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR_KEY));
				label.setVisible(true);
			} else {
				label.setVisible(false);
			}
		}

		for (Label label : board.fileLabels) {

			if (isShowingCoordinates()) {

				label.setFont(SWTService.getInstance().getFont(
						SWTService.BOARD_COORDINATES_FONT));
				label.setForeground(SWTService.getInstance().getColor(
						SWTService.BOARD_COORDINATES_COLOR));
				label.setBackground(SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR_KEY));
				label.setVisible(true);
			} else {
				label.setVisible(false);
			}
		}

		getShell().setBackground(
				SWTService.getInstance().getColor(
						SWTService.BOARD_BACKGROUND_COLOR_KEY));
		board.setBackground(SWTService.getInstance().getColor(
				SWTService.BOARD_BACKGROUND_COLOR_KEY));
		LOG
				.info("Updated prefs in "
						+ (System.currentTimeMillis() - startTime));
	}
}
