package raptor.gui.board;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import raptor.game.Game;
import raptor.game.Move;
import raptor.game.util.GameUtils;
import raptor.gui.board.ChessBoardWindow.ChessBoard.ChessBoardSquare;
import raptor.service.GameService;
import raptor.service.PreferenceService;
import raptor.service.SWTService;

public class ChessBoardWindow {
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
	Shell shell = null;
	List<ChessBoardListener> chessBoardListeners = new ArrayList<ChessBoardListener>(
			3);

	public void addChessBoardListener(ChessBoardListener listener) {
		chessBoardListeners.add(listener);
	}

	public void removeChessBoardListener(ChessBoardListener listener) {
		chessBoardListeners.remove(listener);
	}

	public static interface ChessBoardListener {
		public void onRightClick(String gameId, int square);

		public void onMiddleClick(String gameId, int square);

		public void moveInitiated(String gameId, int square, boolean isDnd);

		public void moveMade(String gameId, int fromSquare, int toSquare);

		public void moveCancelled(String gameId, int fromSquare, boolean isDnd);
	};

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

	public void fireMoveCancelled(int fromSquare, boolean isDnd) {
		for (ChessBoardListener listener : chessBoardListeners) {
			listener.moveCancelled(gameId, fromSquare, isDnd);
		}
	}

	class ChessBoard extends Canvas {
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

				public void dragSetData(DragSourceEvent event) {
					event.data = "" + piece;
				}

				public void dragFinished(DragSourceEvent event) {
					if (!event.doit) {
						fireMoveCancelled(ChessBoardSquare.this.id, true);
					}
					dragIcon.dispose();
					lastDropTime = System.currentTimeMillis();
					ChessBoard.this.setData(CLICK_INITIATOR, null);
					ChessBoard.this.setData(DRAG_INITIATOR, null);
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
						if (backgroundImage != null) {
							backgroundImage.dispose();
						}
						backgroundImage = getSquareBackground().getScaledImage(
								isLight, size.x, size.y);
					}

					e.gc.drawImage(backgroundImage, 0, 0);

					int highlightBorderWidth = (int) (size.x * PreferenceService
							.getInstance()
							.getConfig()
							.getDouble(
									PreferenceService.BOARD_HIGHLIGHT_WIDTH_AKEY,
									.05));
					if (isHighlighted) {
						for (int i = 0; i < highlightBorderWidth; i++) {
							e.gc.drawRectangle(i, i, size.x - 1 - i * 2, size.x
									- 1 - i * 2);
						}
					}

					double imageSquareSideAdjustment = PreferenceService
							.getInstance()
							.getConfig()
							.getDouble(
									PreferenceService.PIECE_SIZE_ADJUSTMENT_KEY,
									.03);
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

						System.out.println("Drawing image in square " + id);
						e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);
					}
				}
			};

			public ChessBoardSquare(int id, boolean isLight) {
				super(ChessBoard.this, 0);
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
				if (backgroundImage != null) {
					backgroundImage.dispose();
				}
				if (pieceImage != null) {
					pieceImage.dispose();
				}
				removePaintListener(paintListener);
				removeControlListener(controlListener);
				removeMouseListener(mouseListener);
				source.removeDragListener(dragSourceListener);
				target.removeDropListener(dropTargetListener);
				source.dispose();
				target.dispose();
				super.dispose();
			}

			public int getId() {
				return id;
			}

			public boolean isLight() {
				return isHighlighted;
			}

			public void setLight(boolean isLight) {
				this.isHighlighted = isLight;
			}

			public int getPiece() {
				return piece;
			}

			public void setPiece(int piece) {
				if (this.piece != piece) {
					this.piece = piece;
					if (pieceImage != null) {
						pieceImage.dispose();
						pieceImage = null;
					}
					redraw();
				}
			}

			public void forceLayout() {
				if (backgroundImage != null) {
					backgroundImage.dispose();
					backgroundImage = null;
				}
				if (pieceImage != null) {
					pieceImage.dispose();
					pieceImage = null;
				}
			}

			public void highlight() {
				if (!isHighlighted) {
					System.out.println("Highlighting " + id);
					isHighlighted = true;
					redraw();
				}
			}

			public void unhighlight() {
				if (isHighlighted) {
					System.out.println("unhighlight " + id);
					isHighlighted = false;
					redraw();
				}
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
					if (squareSide % 2 != 0) {
						squareSide -= 1;
					}

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

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].forceLayout();
			}
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

	public void unhighlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].unhighlight();
			}
		}
	}

	public void highlightAllSquares() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				board.squares[i][j].highlight();
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

	public ChessBoardSquare getSquare(int square) {
		int rank = square / 8;
		int file = square % 8;
		return board.squares[rank][file];
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

	public ChessBoardWindow(String gameId) {
		this.gameId = gameId;
		shell = SWTService.getInstance().createShell();
		shell.setLayout(new FillLayout());

		board = new ChessBoard(shell, SWT.VIRTUAL | SWT.BORDER);
		board.init();
		updateFromGame();
		updateFromPrefs();

		shell.open();
	}

	public void dispose() {
		board.dispose();
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
				System.out.println("moveCancelled" + gameId + " " + fromSquare
						+ " " + isDnd);
				window.unhighlightAllSquares();
				window.updateFromGame();
			}

			public void moveInitiated(String gameId, int square, boolean isDnd) {
				System.out.println("moveInitiated" + gameId + " " + square
						+ " " + isDnd);

				window.unhighlightAllSquares();
				window.getSquare(square).highlight();
				if (isDnd) {
					window.getSquare(square).setPiece(Set.EMPTY);
				}
			}

			public void moveMade(String gameId, int fromSquare, int toSquare) {
				System.out.println("Move made " + gameId + " " + fromSquare
						+ " " + toSquare);
				window.unhighlightAllSquares();
				try {
					Move move = game.makeMove(fromSquare, toSquare);
				    window.getSquare(move.getFrom()).highlight();
				    window.getSquare(move.getTo()).highlight();
				} catch (IllegalArgumentException iae) {
					System.out.println("Move was illegal");
				}
				window.updateFromGame();
			}

			public void onMiddleClick(String gameId, int square) {
				System.out.println("On middle click " + gameId + " " + square);
				Move[] moves = game.getLegalMoves().asArray();
				List<Move> foundMoves = new ArrayList<Move>(5);
				for (Move move : moves) {
					if (move.getTo() == square) {
						foundMoves.add(move);
					}
				}
				
				if (foundMoves.size() > 0) {
					Move move = foundMoves.get(random.nextInt(foundMoves.size()));
					game.move(move);
				    window.unhighlightAllSquares();
				    window.getSquare(move.getFrom()).highlight();
				    window.getSquare(move.getTo()).highlight();
				    window.updateFromGame();
				}
			}

			public void onRightClick(String gameId, int square) {
				System.out.println("On right click " + gameId + " " + square);
			}
		});
		SWTService.getInstance().start();
	}
}
