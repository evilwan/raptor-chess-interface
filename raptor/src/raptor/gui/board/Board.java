package raptor.gui.board;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

public class Board extends Composite {
	static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	Set set;
	Background squareBackground;

	Square[][] squares = new Square[8][8];
	Label[] rankLabels = new Label[8];
	Label[] fileLabels = new Label[8];
	boolean isWhiteOnTop = false;
	boolean isShowingCoordinates = true;

	class Square extends Composite {
		int[] coordinates;
		boolean isLight;
		int piece;
		Label label;
		DropTarget target;
		DragSource source;

		ControlListener controlListener = new ControlListener() {

			public void controlMoved(ControlEvent e) {
			}

			public void controlResized(ControlEvent e) {
				if (label.getBackgroundImage() != null) {
					label.getBackgroundImage().dispose();
				}
				label.setBackgroundImage(squareBackground.getScaledImage(
						isLight, getSize().x, getSize().y));

				if (label.getImage() != null) {
					label.getImage().dispose();
				}

				label.setImage(set.getScaledImage(piece, getSize().x,
						getSize().y));
			}
		};

		public Square(int[] coordinates, boolean isLight) {
			super(Board.this, 0);
			addControlListener(controlListener);
			label = new Label(this, 0);
			setLayout(new FillLayout());
			this.coordinates = coordinates;
			this.isLight = isLight;

			source = new DragSource(label, DND.DROP_COPY | DND.DROP_MOVE);
			source.setDragSourceEffect(new DragSourceEffect(label) {
				public void dragStart(DragSourceEvent event) {
					if (piece == Set.EMPTY) {

					} else {
						event.image = label.getImage();
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
						Board.this.setData(DRAG_INITIATOR, Square.this);
					}
				}

				public void dragSetData(DragSourceEvent event) {
					event.data = "" + piece;
				}

				public void dragFinished(DragSourceEvent event) {

				}
			});

			target = new DropTarget(label, DND.DROP_DEFAULT | DND.DROP_MOVE
					| DND.DROP_COPY | DND.DROP_LINK);
			target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
			target.addDropListener(new DropTargetAdapter() {
				public void dragEnter(DropTargetEvent event) {
				}

				public void dragOperationChanged(DropTargetEvent event) {
				}

				public void dragOver(DropTargetEvent event) {
				}

				public void drop(DropTargetEvent event) {
					if (event.detail != DND.DROP_NONE) {
						Square start = (Square) Board.this
								.getData(DRAG_INITIATOR);
						if (start != Square.this && start != null) {
							start.setPiece(Set.EMPTY);
							setPiece(Integer.parseInt((String) event.data));
							Board.this.setData(DRAG_INITIATOR, null);
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

		public int[] getCoordinates() {
			return coordinates;
		}

		public void setCoordinates(int[] coordinates) {
			this.coordinates = coordinates;
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
			if (this.piece != piece) {
				this.piece = piece;
				if (label.getImage() != null) {
					Image image = label.getImage();
					image.dispose();
				}
				if (getSize() != null) {
					label.setImage(set.getScaledImage(piece, getSize().x,
							getSize().y));
				}
				layout();
			}
		}

		public Label getLabel() {
			return label;
		}
	}

	class ChessBoardLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int hint, int hint2,
				boolean flushCache) {
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

				for (int i = 0; i < rankLabels.length; i++) {
					int multiplier = (isWhiteOnTop ? 7 - i : i);
					rankLabels[i].setLocation(0, (int) (squareSide * multiplier
							+ squareSide / 2 - .4 * charHeight));
					rankLabels[i].setSize(charWidth, charHeight);
				}

				for (int i = 0; i < fileLabels.length; i++) {
					int multiplier = (isWhiteOnTop ? 7 - i : i);
					fileLabels[i].setLocation((int) (charHeight * .4
							+ squareSide * multiplier + squareSide / 2),
							(int) (squareSide * 8));
					fileLabels[i].setSize(charWidth, charHeight);
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
						squares[i][j].setBounds(x, y, squareSide, squareSide);

						x += squareSide;
					}
					x = xInit;
					y += squareSide;

				}
			} else {
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < squares[i].length; j++) {
						squares[i][j].setBounds(x, y, squareSide, squareSide);

						x += squareSide;
					}
					x = xInit;
					y += squareSide;
				}
			}

		}
	}

	public Board(Composite parent, int style) {
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

	public void setupInitial() {
		squares[0][0].setPiece(Set.WR);
		squares[0][1].setPiece(Set.WN);
		squares[0][2].setPiece(Set.WB);
		squares[0][3].setPiece(Set.WQ);
		squares[0][4].setPiece(Set.WK);
		squares[0][5].setPiece(Set.WB);
		squares[0][6].setPiece(Set.WN);
		squares[0][7].setPiece(Set.WR);

		squares[1][0].setPiece(Set.WP);
		squares[1][1].setPiece(Set.WP);
		squares[1][2].setPiece(Set.WP);
		squares[1][3].setPiece(Set.WP);
		squares[1][4].setPiece(Set.WP);
		squares[1][5].setPiece(Set.WP);
		squares[1][6].setPiece(Set.WP);
		squares[1][7].setPiece(Set.WP);

		squares[7][0].setPiece(Set.BR);
		squares[7][1].setPiece(Set.BN);
		squares[7][2].setPiece(Set.BB);
		squares[7][3].setPiece(Set.BQ);
		squares[7][4].setPiece(Set.BK);
		squares[7][5].setPiece(Set.BB);
		squares[7][6].setPiece(Set.BN);
		squares[7][7].setPiece(Set.BR);

		squares[6][0].setPiece(Set.BP);
		squares[6][1].setPiece(Set.BP);
		squares[6][2].setPiece(Set.BP);
		squares[6][3].setPiece(Set.BP);
		squares[6][4].setPiece(Set.BP);
		squares[6][5].setPiece(Set.BP);
		squares[6][6].setPiece(Set.BP);
		squares[6][7].setPiece(Set.BP);
	}

	public void init() {
		boolean isWhiteSquare = false;
		for (int i = 0; i < 8; i++) {
			isWhiteSquare = !isWhiteSquare;
			for (int j = 0; j < squares[i].length; j++) {
				squares[i][j] = new Square(new int[] { i, j }, isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
		}
	}

	public Background getSquareBackground() {
		return squareBackground;
	}

	public void setSquareBackground(Background squareBackground) {
		if (this.squareBackground != squareBackground) {
			this.squareBackground = squareBackground;
			if (isVisible()) {
				layout();
			}
		}
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		if (this.set != set) {
			this.set = set;
			if (isVisible()) {
				layout();
			}
		}
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

	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	public void setWhiteOnTop(boolean isWhiteOnTop) {
		if (this.isWhiteOnTop != isWhiteOnTop) {
			this.isWhiteOnTop = isWhiteOnTop;
			if (isVisible()) {
				layout();
			}
		}
	}

	public boolean isShowingCoordinates() {
		return isShowingCoordinates;
	}

	public void setShowingCoordinates(boolean isShowingCoordinates) {
		if (this.isShowingCoordinates != isShowingCoordinates) {
			this.isShowingCoordinates = isShowingCoordinates;
			if (isVisible()) {
				layout();
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

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		Background background = new Background(display, "Paper");
		Set set = new Set(display, "WCN");

		Board board = new Board(shell, SWT.VIRTUAL | SWT.BORDER);
		board.setSquareBackground(background);
		board.setSet(set);
		board.setShowingCoordinates(true);
		board.init();
		board.setupInitial();

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
