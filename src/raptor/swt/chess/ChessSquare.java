package raptor.swt.chess;

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
import org.eclipse.swt.widgets.Composite;

public class ChessSquare extends Composite implements Constants {

	public static final String CLICK_INITIATOR = "CLICK_INITIATOR";
	public static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	public static final String LAST_DROP_TIME = "LAST_DROP_TIME";

	protected Image backgroundImage = null;

	protected ChessBoard board;
	protected ControlListener controlListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			forceLayout();
		}
	};
	protected DragSourceEffect dragSourceEffect = new DragSourceEffect(this) {
		@Override
		public void dragStart(DragSourceEvent event) {
			if (piece == EMPTY) {

			} else {
				event.image = board.resources.getChessPieceDragImage(piece);
			}
		}
	};
	protected DragSourceListener dragSourceListener = new DragSourceAdapter() {
		@Override
		public void dragFinished(DragSourceEvent event) {
			if (!event.doit) {
				board.controller.userCancelledMove(ChessSquare.this.id, true);
			}
			board.setData(LAST_DROP_TIME, System.currentTimeMillis());
			board.setData(CLICK_INITIATOR, null);
			board.setData(DRAG_INITIATOR, null);
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			event.data = "" + piece;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			if (board.controller.canUserInitiateMoveFrom(id)) {
				event.doit = true;
				event.detail = DND.DROP_MOVE;
				board.setData(DRAG_INITIATOR, ChessSquare.this);
				board.controller.userInitiatedMove(ChessSquare.this.id, true);
			} else {
				event.doit = false;
			}
		}
	};
	protected DropTargetListener dropTargetListener = new DropTargetAdapter() {
		@Override
		public void dragEnter(DropTargetEvent event) {
		}

		@Override
		public void dragOperationChanged(DropTargetEvent event) {
		}

		@Override
		public void dragOver(DropTargetEvent event) {
		}

		@Override
		public void drop(DropTargetEvent event) {
			if (event.detail != DND.DROP_NONE) {
				ChessSquare start = (ChessSquare) board.getData(DRAG_INITIATOR);
				board.controller.userMadeMove(start.id, ChessSquare.this.id);
			}
		}

	};
	protected boolean dontPaintBackground = false;
	protected int id;
	protected boolean isHighlighted;
	protected boolean isLight;
	protected boolean isSelected = false;
	static final Log LOG = LogFactory.getLog(ChessSquare.class);

	MouseListener mouseListener = new MouseListener() {
		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			if (e.button == 3) {
				board.controller.userRightClicked(id);
			}
		}

		public void mouseUp(MouseEvent e) {
			if (e.button == 2) {
				board.controller.userMiddleClicked(id);
			} else if (e.button == 1) {
				Long lastDropTime = (Long) board.getData(LAST_DROP_TIME);
				if (lastDropTime == null
						|| (System.currentTimeMillis() - lastDropTime > 100)) {
					ChessSquare initiator = (ChessSquare) board
							.getData(CLICK_INITIATOR);

					if (initiator == null) {// Start of move
						if (board.controller.canUserInitiateMoveFrom(id)) {
							board.setData(CLICK_INITIATOR, ChessSquare.this);
							board.controller.userInitiatedMove(id, false);
						}
					} else {
						if (ChessSquare.this == initiator) {// Clicked
							// on
							// same
							// square
							// twice.
							board.controller.userCancelledMove(initiator.id,
									false);
							board.setData(CLICK_INITIATOR, null);
						} else if (Utils.arePiecesSameColor(piece,
								initiator.piece)) {// Clicked
							// on
							// same
							// piece color
							// type.
							board.controller.userCancelledMove(initiator.id,
									false);
							board.controller.userInitiatedMove(
									ChessSquare.this.id, false);
							board.setData(CLICK_INITIATOR, ChessSquare.this);
						} else {// A valid move
							board.controller.userMadeMove(initiator.id,
									ChessSquare.this.id);
							board.setData(CLICK_INITIATOR, null);
						}
					}
				}
			}
		}
	};
	PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			if (!dontPaintBackground) {
				if (backgroundImage == null
						|| backgroundImage.getBounds().width != getSize().x
						&& backgroundImage.getBounds().height != getSize().y) {
					backgroundImage = board.resources.getSquareBackgroundImage(
							isLight, size.x, size.y);
				}
				e.gc.drawImage(backgroundImage, 0, 0);
			}
			else {
				e.gc.fillRectangle(0,0,size.x,size.y);
			}

			int highlightBorderWidth = (int) (size.x * board.preferences
					.getDouble(BOARD_HIGHLIGHT_BORDER_WIDTH));
			if (isHighlighted) {
				for (int i = 0; i < highlightBorderWidth; i++) {
					e.gc.drawRectangle(i, i, size.x - 1 - i * 2, size.x - 1 - i
							* 2);
				}
			}

			double imageSquareSideAdjustment = board.preferences
					.getDouble(BOARD_PIECE_SIZE_ADJUSTMENT);
			int imageSide = (int) ((size.x - highlightBorderWidth * 2) * (1.0 - imageSquareSideAdjustment));
			if (imageSide % 2 != 0) {
				imageSide = imageSide - 1;
			}

			if (pieceImage == null && piece != EMPTY) {
				pieceImage = board.resources.getChessPieceImage(piece,
						imageSide, imageSide);
			}

			if (pieceImage != null) {
				int pieceImageX = (size.x - imageSide) / 2;
				int pieceImageY = (size.y - imageSide) / 2;
				e.gc.drawImage(pieceImage, pieceImageX, pieceImageY);
			}
		}
	};

	int piece;

	Image pieceImage;

	DragSource source = new DragSource(this, DND.DROP_MOVE);
	DropTarget target = new DropTarget(this, DND.DROP_MOVE);

	public ChessSquare(ChessBoard chessTable, int id, boolean isLight) {
		this(chessTable, chessTable, id, isLight);
	}

	public ChessSquare(Composite parent, ChessBoard chessTable, int id,
			boolean isLight) {
		super(parent, SWT.NONE);
		this.board = chessTable;
		this.id = id;
		this.isLight = isLight;
		addPaintListener(paintListener);
		addControlListener(controlListener);
		addMouseListener(mouseListener);

		source.setTransfer(new Transfer[] { org.eclipse.swt.dnd.TextTransfer
				.getInstance() });
		source.setDragSourceEffect(dragSourceEffect);
		source.addDragListener(dragSourceListener);

		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		target.addDropListener(dropTargetListener);
	}

	@Override
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

	public Image getBackgroundImage() {
		return backgroundImage;
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

	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
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
