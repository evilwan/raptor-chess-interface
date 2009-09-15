package raptor.gui.board;

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

import raptor.service.SWTService;

public class BoardSquare extends Composite {
	Image backgroundImage;

	Board board = null;
	ControlListener controlListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			forceLayout();
		}
	};
	DragSourceEffect dragSourceEffect = new DragSourceEffect(this) {
		@Override
		public void dragStart(DragSourceEvent event) {
			if (piece == Set.EMPTY) {

			} else {
				board.dragIcon = event.image = getSet().getIcon(piece);
			}
		}
	};
	DragSourceListener dragSourceListener = new DragSourceAdapter() {
		@Override
		public void dragFinished(DragSourceEvent event) {
			if (!event.doit) {
				board.fireMoveCancelled(BoardSquare.this.id, true);
			}
			board.lastDropTime = System.currentTimeMillis();
			board.setData(Board.CLICK_INITIATOR, null);
			board.setData(Board.DRAG_INITIATOR, null);
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			event.data = "" + piece;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			if (piece == Set.EMPTY) {
				event.doit = false;
			} else {
				event.doit = true;
				event.detail = DND.DROP_MOVE;
				board.setData(Board.DRAG_INITIATOR, BoardSquare.this);
				board.fireMoveInitiated(BoardSquare.this.id, true);
			}
		}
	};
	DropTargetListener dropTargetListener = new DropTargetAdapter() {
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
				BoardSquare start = (BoardSquare) board
						.getData(Board.DRAG_INITIATOR);
				board.fireMoveMade(start.id, BoardSquare.this.id);
			}
		}

	};
	int id;
	boolean isHighlighted;
	boolean isLight;
	boolean isSelected = false;
	Log LOG = LogFactory.getLog(BoardSquare.class);
	MouseListener mouseListener = new MouseListener() {
		public void mouseDoubleClick(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			if (e.button == 3) {
				board.fireOnRightClick(id);
			}
		}

		public void mouseUp(MouseEvent e) {
			if (e.button == 2) {
				board.fireOnMiddleClick(id);
			} else if (e.button == 1
					&& System.currentTimeMillis() - board.lastDropTime > 100) {
				BoardSquare initiator = (BoardSquare) board
						.getData(Board.CLICK_INITIATOR);

				if (initiator == null) {// Start of move
					board.setData(Board.CLICK_INITIATOR, BoardSquare.this);
					board.fireMoveInitiated(id, false);
				} else {
					if (BoardSquare.this == initiator) {// Clicked
						// on
						// same
						// square
						// twice.
						board.fireMoveCancelled(initiator.id, false);
						board.setData(Board.CLICK_INITIATOR, null);
					} else if (Set.arePiecesSameColor(piece, initiator.piece)) {// Clicked
						// on
						// same
						// piece
						// type twice.
						board.fireMoveCancelled(initiator.id, false);
						board.fireMoveInitiated(BoardSquare.this.id, false);
						board.setData(Board.CLICK_INITIATOR, BoardSquare.this);
					} else {// A valid move
						board.fireMoveMade(initiator.id, BoardSquare.this.id);
						board.setData(Board.CLICK_INITIATOR, null);
					}
				}
			}
		}
	};
	PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			Point size = getSize();
			if (backgroundImage == null
					|| backgroundImage.getBounds().width != getSize().x
					&& backgroundImage.getBounds().height != getSize().y) {
				backgroundImage = getSquareBackground().getScaledImage(isLight,
						size.x, size.y);
			}

			e.gc.drawImage(backgroundImage, 0, 0);

			int highlightBorderWidth = (int) (size.x * SWTService.getInstance()
					.getStore().getDouble(
							SWTService.BOARD_HIGHLIGHT_BORDER_WIDTH));
			if (isHighlighted) {
				for (int i = 0; i < highlightBorderWidth; i++) {
					e.gc.drawRectangle(i, i, size.x - 1 - i * 2, size.x - 1 - i
							* 2);
				}
			}

			double imageSquareSideAdjustment = SWTService.getInstance()
					.getStore().getDouble(
							SWTService.BOARD_PIECE_SIZE_ADJUSTMENT);
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
	int piece;

	Image pieceImage;

	DragSource source = new DragSource(this, DND.DROP_MOVE);

	DropTarget target = new DropTarget(this, DND.DROP_MOVE);

	public BoardSquare(Composite parent, Board board, int id, boolean isLight) {
		super(parent, SWT.NONE);
		this.board = board;
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

	public int getId() {
		return id;
	}

	public int getPiece() {
		return piece;
	}

	private Set getSet() {
		return SWTService.getInstance().getChessSet();
	}

	private Background getSquareBackground() {
		return SWTService.getInstance().getSquareBackground();
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
