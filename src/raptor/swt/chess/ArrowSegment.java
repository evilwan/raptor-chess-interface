package raptor.swt.chess;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

/**
 * Contains the code to draw an arrow segment on a square.
 */
public enum ArrowSegment {

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

	private void drawDestinationHorizontalLeft(ChessSquare square, Color color,
			int width, GC gc) {
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

	private void drawDestinationVerticalDown(ChessSquare square, Color color,
			int width, GC gc) {
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

	private void drawDiagDecreasing(ChessSquare square, Color color, int width,
			GC gc) {
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

	private void drawDiagIncreasing(ChessSquare square, Color color, int width,
			GC gc) {
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
		gc.fillPolygon(new int[] { squareSide - halfWidth, squareSide,
				squareSide, squareSide - halfWidth, squareSide, squareSide, });
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

	private void drawOriginDiagDecreasingLeft(ChessSquare square, Color color,
			int width, GC gc) {
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

	private void drawOriginDiagDecreasingRight(ChessSquare square, Color color,
			int width, GC gc) {
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

	private void drawOriginDiagIncreasingLeft(ChessSquare square, Color color,
			int width, GC gc) {
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

	private void drawOriginDiagIncreasingRight(ChessSquare square, Color color,
			int width, GC gc) {
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
		gc.fillRectangle(halfSquareSide - width / 2, halfSquareSide + 1, width,
				halfSquareSide);
	}

	private void drawOriginVerticalUp(ChessSquare square, Color color,
			int width, GC gc) {
		gc.setBackground(color);
		int halfSquareSide = square.getSize().y / 2;
		gc.fillRectangle(halfSquareSide - width / 2, 0, width,
				halfSquareSide + 1);

	}

	private void drawUpwardTurnLeft(ChessSquare square, Color color, int width,
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

	private void drawVertical(ChessSquare square, Color color, int width, GC gc) {
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

	private int getArrowBaseHeight(int squareSide,int width) {
		int result = (int) (width*3);
		if (result % 2 != 0) {
			result++;
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
		int arrowBaseHeight = getArrowBaseHeight(squareSide,width);

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
		int arrowBaseHeight = getArrowBaseHeight(squareSide,width);

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

		int c2 = pythag(halfSquareSide + halfWidth, halfSquareSide + halfWidth);

		return new int[] { 0, -halfWidth, c2, -halfWidth, c2, halfWidth, 0,
				halfWidth };
	}

	private int pythag(int a, int b) {
		return (int) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
	}
}
