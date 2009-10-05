package raptor.swt.chess.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessBoardLayout;

public class RightOrientedLayout extends ChessBoardLayout {
	private static final Log LOG = LogFactory.getLog(RightOrientedLayout.class);

	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;

	public static final int[] TOP_BAR_MARGIN = { 0, 5, 5, 5 };
	public static final int[] BOTTOM_BAR_MARGIN = { 5, 0, 5, 10 };
	public static final int[] BOARD_MARGIN = { 0, 0, 10, 5 };
	public static final int[] NAME_LABEL_MARGIN = { 0, 0, 0, 10 };
	public static final int[] CLOCK_MARGIN = { 0, 0, 0, 0 };
	public static final int[] PIECE_JAIL_MARGIN = { 0, 0, 0, 0 };

	protected Point boardTopLeft;
	protected Rectangle gameDescriptionLabelRect;
	protected Rectangle currentPremovesLabelRect;
	protected Rectangle openingDescriptionLabelRect;
	protected Rectangle statusLabelRect;
	protected Rectangle whiteNameLabelRect;
	protected Rectangle blackNameLabelRect;
	protected Rectangle whiteClockRect;
	protected Rectangle blackClockRect;
	protected Rectangle[] pieceJailRects;
	protected Rectangle whiteLagRect;
	protected Rectangle blackLagRect;
	protected Rectangle whiteToMoveIndicatorRect;
	protected Rectangle blackToMoveIndicatorRect;
	protected int boardSquareSize;
	protected int topBarHeight;
	protected int bottomBarHeight;
	protected int boardHeight;

	protected int pieceJailSquareSide;

	protected Label worstCaseClockSizeLabel;
	protected boolean hasInitedPainter = false;

	public RightOrientedLayout(ChessBoard board) {
		super(board);
	}

	@Override
	public void dispose() {
		super.dispose();
		LOG.debug("Disposed RightOrientedLayout");
	}

	@Override
	public String getName() {
		return "Right Oriented";
	}

	@Override
	public int getStyle(Field field) {
		switch (field) {
		case GAME_DESCRIPTION_LABEL:
			return SWT.LEFT;
		case CURRENT_PREMOVE_LABEL:
			return SWT.RIGHT;
		case STATUS_LABEL:
			return SWT.LEFT;
		case OPENING_DESCRIPTION_LABEL:
			return SWT.RIGHT;
		default:
			return SWT.NONE;
		}
	}

	protected void initWorstCaseLabels() {
		if (worstCaseClockSizeLabel == null) {
			worstCaseClockSizeLabel = new Label(board.getBoardPanel(), SWT.NONE);
			worstCaseClockSizeLabel.setText("00:00:00.000");
		}
		worstCaseClockSizeLabel.setFont(board.getWhiteClockLabel().getFont());
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		LOG.debug("in layout ...");
		long startTime = System.currentTimeMillis();

		// if (!hasInitedPainter) {
		// initPainter();
		// }
		setLayoutData();

		board.getGameDescriptionLabel().setBounds(gameDescriptionLabelRect);

		board.getCurrentPremovesLabel().setBounds(currentPremovesLabelRect);

		layoutChessBoard(boardTopLeft, boardSquareSize);

		board.getStatusLabel().setBounds(statusLabelRect);
		board.getOpeningDescriptionLabel().setBounds(
				openingDescriptionLabelRect);

		board.getWhiteToMoveIndicatorLabel()
				.setBounds(whiteToMoveIndicatorRect);
		board.getBlackToMoveIndicatorLabel()
				.setBounds(blackToMoveIndicatorRect);
		board.getWhiteNameRatingLabel().setBounds(whiteNameLabelRect);
		board.getBlackNameRatingLabel().setBounds(blackNameLabelRect);

		board.getWhiteClockLabel().setBounds(whiteClockRect);
		board.getBlackClockLabel().setBounds(blackClockRect);

		board.getWhiteLagLabel().setBounds(whiteLagRect);
		board.getBlackLagLabel().setBounds(blackLagRect);

		for (int i = 0; i < pieceJailRects.length; i++) {
			if (pieceJailRects[i] != null) {
				board.getPieceJailSquares()[i].setBounds(pieceJailRects[i]);
			}
		}
		LOG.debug("Layout completed in "
				+ (System.currentTimeMillis() - startTime));
	}

	protected void setLayoutData() {
		initWorstCaseLabels();

		int width = board.getBoardPanel().getSize().x;
		int height = board.getBoardPanel().getSize().y;

		Point gameDescriptionSize = board.getGameDescriptionLabel()
				.computeSize((width / 2) - TOP_BAR_MARGIN[WEST], SWT.DEFAULT,
						true);
		Point currentPremovesSize = board.getCurrentPremovesLabel()
				.computeSize((width / 2) - TOP_BAR_MARGIN[EAST], SWT.DEFAULT,
						true);

		topBarHeight = Math.max(gameDescriptionSize.y, currentPremovesSize.y)
				+ TOP_BAR_MARGIN[NORTH] + TOP_BAR_MARGIN[SOUTH];

		Point statusLabelSize = board.getStatusLabel().computeSize(
				(width / 2) - BOTTOM_BAR_MARGIN[WEST], SWT.DEFAULT, true);
		Point openingDescriptionSize = board.getOpeningDescriptionLabel()
				.computeSize((width / 2) - BOTTOM_BAR_MARGIN[EAST],
						SWT.DEFAULT, true);

		bottomBarHeight = Math.max(statusLabelSize.y, openingDescriptionSize.y)
				+ BOTTOM_BAR_MARGIN[NORTH] + BOTTOM_BAR_MARGIN[SOUTH];

		boardSquareSize = (height - topBarHeight - bottomBarHeight
				- BOARD_MARGIN[NORTH] - BOARD_MARGIN[SOUTH]) / 8;
		boardHeight = boardSquareSize * 8;

		gameDescriptionLabelRect = new Rectangle(TOP_BAR_MARGIN[WEST],
				TOP_BAR_MARGIN[NORTH], gameDescriptionSize.x,
				gameDescriptionSize.y);
		currentPremovesLabelRect = new Rectangle(
				gameDescriptionLabelRect.width, +TOP_BAR_MARGIN[NORTH],
				currentPremovesSize.x, currentPremovesSize.y);

		int boardPlusTopBarHeight = topBarHeight + boardHeight
				+ BOTTOM_BAR_MARGIN[NORTH] + BOARD_MARGIN[NORTH]
				+ BOARD_MARGIN[SOUTH];

		statusLabelRect = new Rectangle(BOTTOM_BAR_MARGIN[WEST],
				boardPlusTopBarHeight, statusLabelSize.x, statusLabelSize.y);
		openingDescriptionLabelRect = new Rectangle(statusLabelRect.width,
				boardPlusTopBarHeight, openingDescriptionSize.x,
				openingDescriptionSize.y);

		boardTopLeft = new Point(BOARD_MARGIN[WEST], topBarHeight
				+ BOARD_MARGIN[NORTH]);

		Point toMoveIndicatorSize = board.getWhiteToMoveIndicatorLabel()
				.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		int nameLabelStartX = BOARD_MARGIN[WEST] + boardHeight
				+ NAME_LABEL_MARGIN[WEST];
		Point whiteNameLabelSize = board.getWhiteNameRatingLabel().computeSize(
				width - nameLabelStartX - NAME_LABEL_MARGIN[EAST], SWT.DEFAULT,
				true);
		Point blackNameLabelSize = board.getBlackNameRatingLabel().computeSize(
				width - nameLabelStartX - NAME_LABEL_MARGIN[EAST], SWT.DEFAULT,
				true);

		int clockStartX = BOARD_MARGIN[WEST] + boardHeight + CLOCK_MARGIN[WEST];
		Point whiteClockLabelSize = board.getWhiteClockLabel().computeSize(
				width - clockStartX - CLOCK_MARGIN[EAST], SWT.DEFAULT, true);

		Point worseCaseClockSize = worstCaseClockSizeLabel.computeSize(
				SWT.DEFAULT, whiteClockLabelSize.y, false);

		int clockBarWidth = whiteClockLabelSize.x - worseCaseClockSize.x;

		Point lagRectSize = board.getWhiteLagLabel().computeSize(clockBarWidth,
				SWT.DEFAULT, true);

		int nameStartY = boardTopLeft.y;

		@SuppressWarnings("unused")
		int clockWidth = worseCaseClockSize.x;
		if (board.isWhiteOnTop()) {
			whiteToMoveIndicatorRect = new Rectangle(nameLabelStartX,
					boardTopLeft.y, toMoveIndicatorSize.x,
					toMoveIndicatorSize.y);
			whiteNameLabelRect = new Rectangle(whiteToMoveIndicatorRect.x + 2
					+ whiteToMoveIndicatorRect.width, nameStartY,
					whiteNameLabelSize.x, whiteNameLabelSize.y);

			blackToMoveIndicatorRect = new Rectangle(nameLabelStartX,
					boardTopLeft.y + boardHeight / 2, toMoveIndicatorSize.x,
					toMoveIndicatorSize.y);
			blackNameLabelRect = new Rectangle(blackToMoveIndicatorRect.x + 2
					+ whiteToMoveIndicatorRect.width, nameStartY + boardHeight
					/ 2, blackNameLabelSize.x, blackNameLabelSize.y);

			whiteLagRect = new Rectangle(whiteNameLabelRect.x,
					whiteNameLabelRect.y + whiteNameLabelRect.height,
					whiteNameLabelRect.width, lagRectSize.y);

			blackLagRect = new Rectangle(whiteNameLabelRect.x, whiteLagRect.y
					+ boardHeight / 2, blackNameLabelRect.width, lagRectSize.y);

		} else {
			blackToMoveIndicatorRect = new Rectangle(nameLabelStartX,
					boardTopLeft.y, toMoveIndicatorSize.x,
					toMoveIndicatorSize.y);
			blackNameLabelRect = new Rectangle(blackToMoveIndicatorRect.x + 2
					+ blackToMoveIndicatorRect.width, nameStartY,
					blackNameLabelSize.x, blackNameLabelSize.y);

			whiteToMoveIndicatorRect = new Rectangle(nameLabelStartX,
					boardTopLeft.y + boardHeight / 2, toMoveIndicatorSize.x,
					toMoveIndicatorSize.y);
			whiteNameLabelRect = new Rectangle(whiteToMoveIndicatorRect.x + 2
					+ whiteToMoveIndicatorRect.width, nameStartY + boardHeight
					/ 2, whiteNameLabelSize.x, whiteNameLabelSize.y);

			blackLagRect = new Rectangle(blackNameLabelRect.x,
					blackNameLabelRect.y + blackNameLabelRect.height,
					blackNameLabelRect.width, lagRectSize.y);
			whiteLagRect = new Rectangle(whiteNameLabelRect.x,
					whiteNameLabelRect.y + whiteNameLabelRect.height,
					whiteNameLabelRect.width, lagRectSize.y);

		}

		whiteClockRect = new Rectangle(whiteToMoveIndicatorRect.x,
				whiteLagRect.y + whiteLagRect.height, worseCaseClockSize.x,
				whiteClockLabelSize.y);
		blackClockRect = new Rectangle(blackToMoveIndicatorRect.x,
				blackLagRect.y + blackLagRect.height, worseCaseClockSize.x,
				whiteClockLabelSize.y);

		int controlHeight = whiteNameLabelRect.height + whiteLagRect.height;// +
		// whiteClockRect.height;

		if (controlHeight > whiteToMoveIndicatorRect.height) {
			whiteToMoveIndicatorRect.y = whiteNameLabelRect.y
					+ (controlHeight - whiteToMoveIndicatorRect.height) / 2;
			blackToMoveIndicatorRect.y = blackNameLabelRect.y
					+ (controlHeight - blackToMoveIndicatorRect.height) / 2;
		}

		int pieceJailStartX = whiteToMoveIndicatorRect.x;
		@SuppressWarnings("unused")
		int pieceJailStartY = whiteToMoveIndicatorRect.y
				+ whiteToMoveIndicatorRect.height;
		pieceJailRects = new Rectangle[board.getPieceJailSquares().length];
		if (board.isWhitePieceJailOnTop()) {
			pieceJailRects[WP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 3, boardSquareSize, boardSquareSize);
			pieceJailRects[WN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WQ] = new Rectangle(pieceJailStartX + 3
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WR] = new Rectangle(pieceJailStartX + 4
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WK] = new Rectangle(pieceJailStartX + 5
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);

			pieceJailRects[BP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 7, boardSquareSize, boardSquareSize);
			pieceJailRects[BN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BQ] = new Rectangle(pieceJailStartX + 3
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BR] = new Rectangle(pieceJailStartX + 4
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BK] = new Rectangle(pieceJailStartX + 5
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
		} else {
			pieceJailRects[BP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 3, boardSquareSize, boardSquareSize);
			pieceJailRects[BN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BQ] = new Rectangle(pieceJailStartX + 3
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BR] = new Rectangle(pieceJailStartX + 4
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BK] = new Rectangle(pieceJailStartX + 5
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);

			pieceJailRects[WP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 7, boardSquareSize, boardSquareSize);
			pieceJailRects[WN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WQ] = new Rectangle(pieceJailStartX + 3
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WR] = new Rectangle(pieceJailStartX + 4
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WK] = new Rectangle(pieceJailStartX + 5
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
		}
	}
}
