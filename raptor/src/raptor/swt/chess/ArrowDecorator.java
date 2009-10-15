package raptor.swt.chess;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

import raptor.Raptor;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;

/**
 * A class which manages GUI arrow decorations between squares on a chess board.
 * Currently drop or piece jail squares are not supported.
 */
public class ArrowDecorator {
	/**
	 * Contains the code to draw an arrow segment on a square.
	 */
	protected static enum ArrowSegment {

		UpwardTurnLeft, UpwardTurnRight, DownwardTurnLeft, DownwardTurnRight, DiagIncreasing, DiagDecreasing, Horizontal, Vertical, DiagNorthEastCorner, DiagNorthWestCorner, DiagSouthEastCorner, DiagSouthWestCorner, OriginVerticalUp, OriginVerticalDown, OriginHorizontalLeft, OriginHorizontalRight, OriginDiagIncreasingLeft, OriginDiagIncreasingRight, OriginDiagDecreasingLeft, OriginDiagDecreasingRight, DestinationVerticalUp, DestinationVerticalDown, DestinationHorizontalLeft, DestinationHorizontalRight, DestinationDiagIncreasingLeft, DestinationDiagIncreasingRight, DestinationDiagDecreasingLeft, DestinationDiagDecreasingRight;

		public void draw(ChessSquare square, Color color, int width, GC gc) {
			switch (this) {
			case UpwardTurnLeft:
				drawUpwardTurnLeft(square, color, width, gc);
				break;
			case UpwardTurnRight:
				drawUpwardTurnRight(square, color, width, gc);
				break;
			case DownwardTurnLeft:
				drawDownwardTurnLeft(square, color, width, gc);
				break;
			case DownwardTurnRight:
				drawDownwardTurnRight(square, color, width, gc);
				break;
			case DiagIncreasing:
				drawDiagIncreasing(square, color, width, gc);
				break;
			case DiagDecreasing:
				drawDiagDecreasing(square, color, width, gc);
				break;
			case Horizontal:
				drawHorizontal(square, color, width, gc);
				break;
			case Vertical:
				drawVertical(square, color, width, gc);
				break;
			case DiagNorthEastCorner:
				drawDiagNorthEastCorner(square, color, width, gc);
				break;
			case DiagNorthWestCorner:
				drawDiagNorthWestCorner(square, color, width, gc);
				break;
			case DiagSouthEastCorner:
				drawDiagSouthEastCorner(square, color, width, gc);
				break;
			case DiagSouthWestCorner:
				drawDiagSouthWestCorner(square, color, width, gc);
				break;
			case OriginVerticalUp:
				drawOriginVerticalUp(square, color, width, gc);
				break;
			case OriginVerticalDown:
				drawOriginVerticalDown(square, color, width, gc);
				break;
			case OriginHorizontalLeft:
				drawOriginHorizontalLeft(square, color, width, gc);
				break;
			case OriginHorizontalRight:
				drawOriginHorizontalRight(square, color, width, gc);
				break;
			case OriginDiagIncreasingLeft:
				drawOriginDiagIncreasingLeft(square, color, width, gc);
				break;
			case OriginDiagIncreasingRight:
				drawOriginDiagIncreasingRight(square, color, width, gc);
				break;
			case OriginDiagDecreasingLeft:
				drawOriginDiagDecreasingLeft(square, color, width, gc);
				break;
			case OriginDiagDecreasingRight:
				drawOriginDiagDecreasingRight(square, color, width, gc);
				break;
			case DestinationVerticalUp:
				drawDestinationVerticalUp(square, color, width, gc);
				break;
			case DestinationVerticalDown:
				drawDestinationVerticalDown(square, color, width, gc);
				break;
			case DestinationHorizontalLeft:
				drawDestinationHorizontalLeft(square, color, width, gc);
				break;
			case DestinationHorizontalRight:
				drawDestinationHorizontalRight(square, color, width, gc);
				break;
			case DestinationDiagIncreasingLeft:
				drawDestinationDiagIncreasingLeft(square, color, width, gc);
				break;
			case DestinationDiagIncreasingRight:
				drawDestinationDiagIncreasingRight(square, color, width, gc);
				break;
			case DestinationDiagDecreasingLeft:
				drawDestinationDiagDecreasingLeft(square, color, width, gc);
				break;
			case DestinationDiagDecreasingRight:
				drawDestinationDiagDecreasingRight(square, color, width, gc);
				break;
			}
		}

		private void drawDestinationDiagDecreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, squareSide);
			tr.rotate(-135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagDecreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.rotate(45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagIncreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, 0);
			gc.setTransform(tr);
			tr.rotate(135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagIncreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(0, squareSide);
			gc.setTransform(tr);
			tr.rotate(-45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationHorizontalLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(180);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationHorizontalRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(0, squareSide / 2.0F);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationVerticalDown(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, 0);
			gc.setTransform(tr);

			tr.rotate(90);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationVerticalUp(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, squareSide);
			gc.setTransform(tr);

			tr.rotate(-90);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDiagDecreasing(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, halfWidth, squareSide - halfWidth,
					squareSide, squareSide, squareSide, squareSide,
					squareSide - halfWidth, halfWidth, 0, 0, 0 });
		}

		private void drawDiagIncreasing(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, squareSide - halfWidth,
					squareSide - halfWidth, 0, squareSide, 0, squareSide,
					halfWidth, halfWidth, squareSide, 0, squareSide });
		}

		private void drawDiagNorthEastCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillPolygon(new int[] { squareSide - halfWidth, 0, squareSide,
					halfWidth, squareSide, 0 });
		}

		private void drawDiagNorthWestCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, halfWidth, halfWidth, 0, 0, 0 });
		}

		private void drawDiagSouthEastCorner(ChessSquare square, Color color,
				int width, GC gc) {

			int squareSide = square.getSize().x;

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc
					.fillPolygon(new int[] { squareSide - halfWidth,
							squareSide, squareSide, squareSide - halfWidth,
							squareSide, squareSide, });
		}

		private void drawDiagSouthWestCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, squareSide - halfWidth, halfWidth,
					squareSide, 0, squareSide, });
		}

		private void drawDownwardTurnLeft(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(-halfSquareSide, -halfSquareSide - halfWidth + i,
						squareSide, squareSide, 0, -95);
				gc.drawArc(-halfSquareSide - halfWidth + i, -halfSquareSide,
						squareSide, squareSide, 0, -95);
			}
		}

		private void drawDownwardTurnRight(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(halfSquareSide, -halfSquareSide - halfWidth + i,
						squareSide, squareSide, 270, -95);
				gc.drawArc(halfSquareSide + -halfWidth + i, -halfSquareSide,
						squareSide, squareSide, 270, -95);
			}
		}

		private void drawHorizontal(ChessSquare square, Color color, int width,
				GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillRectangle(0, halfSquareSide - halfWidth, squareSide, width);
		}

		private void drawOriginDiagDecreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(-135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagDecreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagIncreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagIncreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(-45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginHorizontalLeft(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int halfSquareSide = square.getSize().y / 2;
			gc.fillRectangle(0, halfSquareSide - width / 2, halfSquareSide + 1,
					width);

		}

		private void drawOriginHorizontalRight(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int halfSquareSide = square.getSize().y / 2;
			gc.fillRectangle(halfSquareSide, halfSquareSide - width / 2,
					halfSquareSide + 1, width);
		}

		private void drawOriginVerticalDown(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int halfSquareSide = square.getSize().y / 2;
			gc.fillRectangle(halfSquareSide - width / 2, halfSquareSide + 1,
					width, halfSquareSide);
		}

		private void drawOriginVerticalUp(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int halfSquareSide = square.getSize().y / 2;
			gc.fillRectangle(halfSquareSide - width / 2, 0, width,
					halfSquareSide + 1);

		}

		private void drawUpwardTurnLeft(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(-halfSquareSide, halfSquareSide - halfWidth + i,
						squareSide, squareSide, 90, -95);
				gc.drawArc(-halfSquareSide - halfWidth + i, halfSquareSide,
						squareSide, squareSide, 90, -95);
			}
		}

		private void drawUpwardTurnRight(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(halfSquareSide, halfSquareSide - halfWidth + i,
						squareSide, squareSide, 180, -95);
				gc.drawArc(halfSquareSide + -halfWidth + i, halfSquareSide,
						squareSide, squareSide, 180, -95);
			}
		}

		private void drawVertical(ChessSquare square, Color color, int width,
				GC gc) {
			int squareSide = square.getSize().y;
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			gc.setBackground(color);
			gc.fillRectangle(halfSquareSide - halfWidth, 0, width, squareSide);
		}

		private int getArrowBaseHeight(int squareSide, int width) {
			int result = width * 3;
			if (result % 2 != 0) {
				result++;
			}

			if (result < 10) {
				result = 10;
			}
			return result;
		}

		private int getArrowRectWidth(int squareSide) {
			int result = (int) (25.0 / 100.0 * squareSide);
			if (result % 2 != 0) {
				result++;
			}
			return result;
		}

		/**
		 * Creates the destination polygon with the origin at 0,0
		 */
		private int[] getDestinationPolygon(int squareSide, int width) {
			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int halfWidth = width / 2;
			if (halfWidth % 2 != 0) {
				halfWidth++;
			}

			int arrowRectWidth = getArrowRectWidth(squareSide);
			int arrowBaseHeight = getArrowBaseHeight(squareSide, width);

			int halfArrowBaseHeight = arrowBaseHeight / 2;

			return new int[] { 0, -halfWidth, arrowRectWidth, -halfWidth,
					arrowRectWidth, -halfArrowBaseHeight, halfSquareSide, -1,
					halfSquareSide, +1, arrowRectWidth, halfArrowBaseHeight,
					arrowRectWidth, halfWidth, 0, halfWidth };
		}

		/**
		 * Creates the diag destination polygon with the origin at 0,0
		 */
		private int[] getDiagDestinationPolygon(int squareSide, int width) {
			int halfWidth = width / 2;
			if (halfWidth % 2 == 0) {
				halfWidth--;
			}

			int c2 = pythag(squareSide, squareSide);
			int halfSquareSide = c2 / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int arrowRectWidth = getArrowRectWidth(c2);
			int arrowBaseHeight = getArrowBaseHeight(squareSide, width);

			int halfArrowBaseHeight = arrowBaseHeight / 2;

			return new int[] { 0, -halfWidth, arrowRectWidth, -halfWidth,
					arrowRectWidth, -halfArrowBaseHeight, halfSquareSide, -1,
					halfSquareSide, +1, arrowRectWidth, halfArrowBaseHeight,
					arrowRectWidth, halfWidth, 0, halfWidth };
		}

		private int[] getOriginDiagPolygon(int squareSide, int width) {
			int halfWidth = width / 2;
			if (halfWidth % 2 == 0) {
				halfWidth--;
			}

			int halfSquareSide = squareSide / 2;
			if (halfSquareSide % 2 != 0) {
				halfSquareSide++;
			}

			int c2 = pythag(halfSquareSide + halfWidth, halfSquareSide
					+ halfWidth);

			return new int[] { 0, -halfWidth, c2, -halfWidth, c2, halfWidth, 0,
					halfWidth };
		}

		private int pythag(int a, int b) {
			return (int) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		}
	}

	/**
	 * Ties an arrow to an arrow segment.
	 */
	protected static class ArrowSpec {
		Arrow arrow;

		ArrowSegment segment;

		public ArrowSpec(Arrow arrow, ArrowSegment segment) {
			this.arrow = arrow;
			this.segment = segment;
		}
	}

	protected class SquareArrowDecorator implements PaintListener {
		protected ChessSquare square;
		protected List<ArrowSpec> specs = new ArrayList<ArrowSpec>(10);

		public SquareArrowDecorator(ChessSquare square) {
			this.square = square;
			square.addPaintListener(this);
		}

		public void addArrowSpec(ArrowSpec spec) {
			specs.add(spec);
		}

		public void clear() {
			for (int i = 0; i < specs.size(); i++) {
				if (!specs.get(i).arrow.isFadeAway) {
					specs.remove(i);
					i--;
				}
			}
		}

		public void paintControl(PaintEvent e) {
			for (ArrowSpec spec : specs) {
				if (spec.arrow.frame == -1) {
					int width = (int) (Raptor.getInstance().getPreferences()
							.getInt(PreferenceKeys.ARROW_WIDTH_PERCENTAGE) / 100.0 * square
							.getSize().x);
					if (width % 2 != 0) {
						width++;
					}
					spec.segment.draw(square, spec.arrow.color, width, e.gc);
				} else if (spec.arrow.frame != 0) {
					int width = (int) (Raptor.getInstance().getPreferences()
							.getInt(PreferenceKeys.ARROW_WIDTH_PERCENTAGE) / 100.0 * square
							.getSize().x);
					if (width % 2 != 0) {
						width++;
					}

					e.gc.setAdvanced(true);
					e.gc.setAlpha(50 * spec.arrow.frame);
					spec.segment.draw(square, spec.arrow.color, width, e.gc);
					e.gc.setAlpha(255);
				}
			}
		}

		public void remove(Arrow arrow, boolean isForced) {
			if (!arrow.isFadeAway || isForced) {
				ArrowSpec specToRemove = null;

				for (ArrowSpec spec : specs) {
					if (arrow == spec.arrow) {
						specToRemove = spec;
					}
				}
				if (specToRemove != null) {
					specs.remove(specToRemove);
				}
			}
		}
	}

	protected ChessBoard board;

	protected SquareArrowDecorator[] decorators = new SquareArrowDecorator[64];

	public ArrowDecorator(ChessBoard board) {
		this.board = board;
		for (int i = 0; i < 64; i++) {
			decorators[i] = new SquareArrowDecorator(board.getSquare(i));
		}
	}

	/**
	 * Draws the specified arrow. Currently arrows are not supported to or from
	 * drop squares.
	 */
	public void drawArrow(final Arrow arrow) {
		if (arrow.startSquare == arrow.endSquare
				|| GameUtils.isDropSquare(arrow.startSquare)
				|| GameUtils.isDropSquare(arrow.endSquare)) {
			return;
		} else if (arrow.startSquare < arrow.endSquare) {
			if (GameUtils.getFile(arrow.startSquare) == GameUtils
					.getFile(arrow.endSquare)) {
				decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.OriginVerticalUp));

				int startRank = GameUtils.getRank(arrow.startSquare);
				int endRank = GameUtils.getRank(arrow.endSquare);
				int file = GameUtils.getFile(arrow.startSquare);

				for (int i = startRank + 1; i < endRank; i++) {
					decorators[GameUtils.getSquare(i, file)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.Vertical));
				}

				decorators[arrow.endSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.DestinationVerticalUp));
			} else if (GameUtils.getRank(arrow.startSquare) == GameUtils
					.getRank(arrow.endSquare)) {
				decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.OriginHorizontalRight));

				int startFile = GameUtils.getFile(arrow.startSquare);
				int endFile = GameUtils.getFile(arrow.endSquare);
				int rank = GameUtils.getRank(arrow.startSquare);

				for (int i = startFile + 1; i < endFile; i++) {
					decorators[GameUtils.getSquare(rank, i)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.Horizontal));
				}

				decorators[arrow.endSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.DestinationHorizontalRight));
			} else {
				int rankDelta = Math.abs(GameUtils.getRank(arrow.startSquare)
						- GameUtils.getRank(arrow.endSquare));
				int fileDelta = Math.abs(GameUtils.getFile(arrow.startSquare)
						- GameUtils.getFile(arrow.endSquare));

				if (rankDelta != fileDelta) {
					if (GameUtils.getFile(arrow.startSquare) < GameUtils
							.getFile(arrow.endSquare)) {
						decorators[arrow.startSquare]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.OriginVerticalUp));

						int startRank = GameUtils.getRank(arrow.startSquare) + 1;

						while (startRank < GameUtils.getRank(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank++,
									GameUtils.getFile(arrow.startSquare))]
									.addArrowSpec(new ArrowSpec(arrow,
											ArrowSegment.Vertical));
						}

						decorators[GameUtils.getSquare(startRank, GameUtils
								.getFile(arrow.startSquare))]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.UpwardTurnRight));

						int startFile = GameUtils.getFile(arrow.startSquare) + 1;
						while (startFile < GameUtils.getFile(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank,
									startFile++)].addArrowSpec(new ArrowSpec(
									arrow, ArrowSegment.Horizontal));
						}

						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DestinationHorizontalRight));

					} else {
						decorators[arrow.startSquare]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.OriginVerticalUp));

						int startRank = GameUtils.getRank(arrow.startSquare) + 1;

						while (startRank < GameUtils.getRank(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank++,
									GameUtils.getFile(arrow.startSquare))]
									.addArrowSpec(new ArrowSpec(arrow,
											ArrowSegment.Vertical));
						}

						decorators[GameUtils.getSquare(startRank, GameUtils
								.getFile(arrow.startSquare))]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.UpwardTurnLeft));

						int startFile = GameUtils.getFile(arrow.startSquare) - 1;
						while (startFile > GameUtils.getFile(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank,
									startFile--)].addArrowSpec(new ArrowSpec(
									arrow, ArrowSegment.Horizontal));
						}

						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DestinationHorizontalLeft));
					}
				} else if (GameUtils.getFile(arrow.startSquare) > GameUtils
						.getFile(arrow.endSquare)) {
					decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(
							arrow, ArrowSegment.OriginDiagDecreasingLeft));

					int startRank = GameUtils.getRank(arrow.startSquare) + 1;
					int startFile = GameUtils.getFile(arrow.startSquare) - 1;
					while (startRank < GameUtils.getRank(arrow.endSquare)
							&& startFile > GameUtils.getFile(arrow.endSquare)) {
						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagDecreasing));
						decorators[GameUtils
								.getSquare(startRank, startFile + 1)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagSouthWestCorner));
						decorators[GameUtils
								.getSquare(startRank - 1, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagNorthEastCorner));
						startRank++;
						startFile--;
					}

					decorators[GameUtils.getSquare(startRank, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DestinationDiagDecreasingLeft));
					decorators[GameUtils.getSquare(startRank, startFile + 1)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagSouthWestCorner));
					decorators[GameUtils.getSquare(startRank - 1, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagNorthEastCorner));

				} else { // getFile(startSquare) < getFile(endSquare)
					decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(
							arrow, ArrowSegment.OriginDiagIncreasingRight));

					int startRank = GameUtils.getRank(arrow.startSquare) + 1;
					int startFile = GameUtils.getFile(arrow.startSquare) + 1;
					while (startRank < GameUtils.getRank(arrow.endSquare)
							&& startFile < GameUtils.getFile(arrow.endSquare)) {
						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagIncreasing));
						decorators[GameUtils
								.getSquare(startRank, startFile - 1)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagSouthEastCorner));
						decorators[GameUtils
								.getSquare(startRank - 1, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagNorthWestCorner));
						startRank++;
						startFile++;
					}

					decorators[GameUtils.getSquare(startRank, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DestinationDiagIncreasingRight));
					decorators[GameUtils.getSquare(startRank, startFile - 1)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagSouthEastCorner));
					decorators[GameUtils.getSquare(startRank - 1, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagNorthWestCorner));
				}
			}
		} else {
			if (GameUtils.getFile(arrow.startSquare) == GameUtils
					.getFile(arrow.endSquare)) {
				decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.OriginVerticalDown));

				int startRank = GameUtils.getRank(arrow.endSquare);
				int endRank = GameUtils.getRank(arrow.startSquare);
				int file = GameUtils.getFile(arrow.startSquare);

				for (int i = startRank + 1; i < endRank; i++) {
					decorators[GameUtils.getSquare(i, file)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.Vertical));
				}

				decorators[arrow.endSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.DestinationVerticalDown));
			} else if (GameUtils.getRank(arrow.startSquare) == GameUtils
					.getRank(arrow.endSquare)) {
				decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.OriginHorizontalLeft));

				int startFile = GameUtils.getFile(arrow.endSquare);
				int endFile = GameUtils.getFile(arrow.startSquare);
				int rank = GameUtils.getRank(arrow.startSquare);

				for (int i = startFile + 1; i < endFile; i++) {
					decorators[GameUtils.getSquare(rank, i)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.Horizontal));
				}

				decorators[arrow.endSquare].addArrowSpec(new ArrowSpec(arrow,
						ArrowSegment.DestinationHorizontalLeft));
			} else {
				int rankDelta = Math.abs(GameUtils.getRank(arrow.startSquare)
						- GameUtils.getRank(arrow.endSquare));
				int fileDelta = Math.abs(GameUtils.getFile(arrow.startSquare)
						- GameUtils.getFile(arrow.endSquare));

				if (rankDelta != fileDelta) {
					if (GameUtils.getFile(arrow.startSquare) < GameUtils
							.getFile(arrow.endSquare)) {
						decorators[arrow.startSquare]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.OriginVerticalDown));

						int startRank = GameUtils.getRank(arrow.startSquare) - 1;

						while (startRank > GameUtils.getRank(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank--,
									GameUtils.getFile(arrow.startSquare))]
									.addArrowSpec(new ArrowSpec(arrow,
											ArrowSegment.Vertical));
						}

						decorators[GameUtils.getSquare(startRank, GameUtils
								.getFile(arrow.startSquare))]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DownwardTurnRight));

						int startFile = GameUtils.getFile(arrow.startSquare) + 1;
						while (startFile < GameUtils.getFile(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank,
									startFile++)].addArrowSpec(new ArrowSpec(
									arrow, ArrowSegment.Horizontal));
						}

						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DestinationHorizontalRight));

					} else {
						decorators[arrow.startSquare]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.OriginVerticalDown));

						int startRank = GameUtils.getRank(arrow.startSquare) - 1;

						while (startRank > GameUtils.getRank(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank--,
									GameUtils.getFile(arrow.startSquare))]
									.addArrowSpec(new ArrowSpec(arrow,
											ArrowSegment.Vertical));
						}

						decorators[GameUtils.getSquare(startRank, GameUtils
								.getFile(arrow.startSquare))]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DownwardTurnLeft));

						int startFile = GameUtils.getFile(arrow.startSquare) - 1;
						while (startFile > GameUtils.getFile(arrow.endSquare)) {
							decorators[GameUtils.getSquare(startRank,
									startFile--)].addArrowSpec(new ArrowSpec(
									arrow, ArrowSegment.Horizontal));
						}

						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DestinationHorizontalLeft));
					}
				} else if (GameUtils.getFile(arrow.startSquare) > GameUtils
						.getFile(arrow.endSquare)) {
					decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(
							arrow, ArrowSegment.OriginDiagIncreasingLeft));

					int startRank = GameUtils.getRank(arrow.startSquare) - 1;
					int startFile = GameUtils.getFile(arrow.startSquare) - 1;
					while (startRank > GameUtils.getRank(arrow.endSquare)
							&& startFile > GameUtils.getFile(arrow.endSquare)) {
						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagIncreasing));
						decorators[GameUtils
								.getSquare(startRank + 1, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagSouthEastCorner));
						decorators[GameUtils
								.getSquare(startRank, startFile + 1)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagNorthWestCorner));
						startRank--;
						startFile--;
					}

					decorators[GameUtils.getSquare(startRank, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DestinationDiagIncreasingLeft));
					decorators[GameUtils.getSquare(startRank + 1, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagSouthEastCorner));
					decorators[GameUtils.getSquare(startRank, startFile + 1)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagNorthWestCorner));

				} else { // getFile(startSquare) < getFile(endSquare)
					decorators[arrow.startSquare].addArrowSpec(new ArrowSpec(
							arrow, ArrowSegment.OriginDiagDecreasingRight));

					int startRank = GameUtils.getRank(arrow.startSquare) - 1;
					int startFile = GameUtils.getFile(arrow.startSquare) + 1;
					while (startRank > GameUtils.getRank(arrow.endSquare)
							&& startFile < GameUtils.getFile(arrow.endSquare)) {
						decorators[GameUtils.getSquare(startRank, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagDecreasing));
						decorators[GameUtils
								.getSquare(startRank + 1, startFile)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagSouthWestCorner));
						decorators[GameUtils
								.getSquare(startRank, startFile - 1)]
								.addArrowSpec(new ArrowSpec(arrow,
										ArrowSegment.DiagNorthEastCorner));
						startRank--;
						startFile++;
					}

					decorators[GameUtils.getSquare(startRank, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DestinationDiagDecreasingRight));
					decorators[GameUtils.getSquare(startRank + 1, startFile)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagSouthWestCorner));
					decorators[GameUtils.getSquare(startRank, startFile - 1)]
							.addArrowSpec(new ArrowSpec(arrow,
									ArrowSegment.DiagNorthEastCorner));
				}
			}
		}
		board.redrawSquares();
		if (arrow.isFadeAway) {
			Raptor.getInstance().getDisplay().timerExec(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.ARROW_ANIMATION_DELAY),
					new Runnable() {
						public void run() {
							arrow.frame--;
							board.redrawSquares();
							if (arrow.frame != 0) {
								Raptor
										.getInstance()
										.getDisplay()
										.timerExec(
												Raptor
														.getInstance()
														.getPreferences()
														.getInt(
																PreferenceKeys.ARROW_ANIMATION_DELAY),
												this);
							} else {
								removeArrow(arrow, true);
							}
						}
					});
		}
	}

	/**
	 * Removes all non fade away arrows on the chess board.
	 */
	public void removeAllArrows() {
		for (SquareArrowDecorator decorator : decorators) {
			decorator.clear();
		}
	}

	/**
	 * Removes an arrow.
	 */
	public void removeArrow(Arrow arrow) {
		removeArrow(arrow, false);
	}

	/**
	 * Removes an arrow.
	 */
	protected void removeArrow(Arrow arrow, boolean isForced) {
		for (SquareArrowDecorator decorator : decorators) {
			decorator.remove(arrow, isForced);
		}
		board.redrawSquares();
	}
}
