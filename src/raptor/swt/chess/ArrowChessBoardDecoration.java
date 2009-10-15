package raptor.swt.chess;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;

import raptor.chess.util.GameUtils;

public class ArrowChessBoardDecoration {
	protected ChessBoard board;
	protected ArrowDecorator[] decorators = new ArrowDecorator[64];

	public ArrowChessBoardDecoration(ChessBoard board) {
		this.board = board;
		for (int i = 0; i < 64; i++) {
			decorators[i] = new ArrowDecorator(board.getSquare(i));
		}
	}

	protected class ArrowDecorator implements PaintListener {
		protected ChessSquare square;
		protected List<ArrowSpec> specs = new ArrayList<ArrowSpec>(10);

		public ArrowDecorator(ChessSquare square) {
			this.square = square;
			square.addPaintListener(this);
		}

		public void paintControl(PaintEvent e) {
			for (ArrowSpec spec : specs) {
				int width = (int) (16.0 / 100.0 * square.getSize().x);
				if (width % 2 != 0) {
					width++;
				}
				spec.segment.draw(square, spec.arrow.color, width, e.gc);
			}
		}

		public void addArrowSpec(ArrowSpec spec) {
			specs.add(spec);
		}

		public void clear() {
			specs.clear();
		}

		public void remove(Arrow arrow) {
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

	public static class Arrow {
		protected int startSquare;
		protected int endSquare;
		protected Color color;
		protected int frame;

		public Arrow(int startSquare, int endSquare, Color color) {
			this.startSquare = startSquare;
			this.endSquare = endSquare;
			this.color = color;
		}

		public int getStartSquare() {
			return startSquare;
		}

		public void setStartSquare(int startSquare) {
			this.startSquare = startSquare;
		}

		public int getEndSquare() {
			return endSquare;
		}

		public void setEndSquare(int endSquare) {
			this.endSquare = endSquare;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public int getFrame() {
			return frame;
		}

		public void setFrame(int frame) {
			this.frame = frame;
		}
	}

	protected static class ArrowSpec {
		public ArrowSpec(Arrow arrow, ArrowSegment segment) {
			this.arrow = arrow;
			this.segment = segment;
		}

		Arrow arrow;
		ArrowSegment segment;
	}

	public void removeAllArrows() {
		for (ArrowDecorator decorator : decorators) {
			decorator.clear();
		}
	}

	public void drawArrow(Arrow arrow) {
		if (arrow.startSquare == arrow.endSquare) {
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

	}

	public void removeArrow(Arrow arrow) {
		for (ArrowDecorator decorator : decorators) {
			decorator.remove(arrow);
		}
	}
}
