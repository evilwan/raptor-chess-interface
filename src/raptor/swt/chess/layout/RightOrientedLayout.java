package raptor.swt.chess.layout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

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
	public static final int[] BOARD_MARGIN = { 0, 0, 10, 10 };
	public static final int[] NAME_LABEL_MARGIN = { 0, 0, 10, 10 };
	public static final int[] CLOCK_MARGIN = { 5, 5, 10, 10 };
	public static final int[] PIECE_JAIL_MARGIN = { 0, 0, 10, 10 };

	protected Point boardTopLeft;
	protected Rectangle gameDescriptionLabelRect;
	protected Rectangle currentPremovesLabelRect;
	protected Rectangle openingDescriptionLabelRect;
	protected Rectangle statusLabelRect;
	protected Rectangle coolbarRect;
	protected Rectangle whiteNameLabelRect;
	protected Rectangle blackNameLabelRect;
	protected Rectangle whiteClockRect;
	protected Rectangle blackClockRect;
	protected Rectangle[] pieceJailRects;
	protected int boardSquareSize;
	protected int topBarHeight;
	protected int bottomBarHeight;
	protected int boardHeight;

	protected int pieceJailSquareSide;

	@Override
	public int getStyle(int controlConstant) {
		switch (controlConstant) {
		case GAME_DESCRIPTION_LABEL:
			return SWT.LEFT;
		case CURRENT_PREMOVE_LABEL:
			return SWT.RIGHT;
		case STATUS_LABEL:
			return SWT.LEFT;
		case OPENING_DESCRIPTION_LABEL:
			return SWT.RIGHT;
		case COOLBAR:
			return SWT.FLAT | SWT.BORDER;
		default:
			return SWT.NONE;
		}
	}

	protected void setLayoutData() {
		GC gc = new GC(board.getBoardPanel());

		int width = board.getBoardPanel().getSize().x;
		int height = board.getBoardPanel().getSize().y;

		// board.getCoolbar().layout();
		//		
		coolbarRect = new Rectangle(0, 0, 0, 0);

		topBarHeight = Math.max(getOneCharSizeInFont(board.getStatusLabel()
				.getFont(), gc).y, getOneCharSizeInFont(board
				.getCurrentPremovesLabel().getFont(), gc).y)
				+ TOP_BAR_MARGIN[NORTH] + TOP_BAR_MARGIN[SOUTH];
		bottomBarHeight = Math.max(getOneCharSizeInFont(board.getStatusLabel()
				.getFont(), gc).y, getOneCharSizeInFont(board
				.getOpeningDescriptionLabel().getFont(), gc).y)
				+ BOTTOM_BAR_MARGIN[NORTH] + BOTTOM_BAR_MARGIN[SOUTH];

		boardSquareSize = (height - coolbarRect.height - topBarHeight
				- bottomBarHeight - BOARD_MARGIN[NORTH] - BOARD_MARGIN[SOUTH]) / 8;
		boardHeight = boardSquareSize * 8;

		gameDescriptionLabelRect = new Rectangle(TOP_BAR_MARGIN[WEST],
				coolbarRect.height + TOP_BAR_MARGIN[NORTH], (width / 2)
						- TOP_BAR_MARGIN[WEST], topBarHeight
						- TOP_BAR_MARGIN[NORTH] - TOP_BAR_MARGIN[SOUTH]);
		currentPremovesLabelRect = new Rectangle(
				gameDescriptionLabelRect.width, coolbarRect.height
						+ TOP_BAR_MARGIN[NORTH], (width / 2)
						- TOP_BAR_MARGIN[EAST], topBarHeight
						- TOP_BAR_MARGIN[NORTH] - TOP_BAR_MARGIN[SOUTH]);

		int boardPlusTopBarHeight = coolbarRect.height + topBarHeight
				+ boardHeight + BOTTOM_BAR_MARGIN[NORTH] + BOARD_MARGIN[NORTH]
				+ BOARD_MARGIN[SOUTH];

		statusLabelRect = new Rectangle(BOTTOM_BAR_MARGIN[WEST],
				boardPlusTopBarHeight, (width / 2) - BOTTOM_BAR_MARGIN[WEST],
				bottomBarHeight - BOTTOM_BAR_MARGIN[NORTH]
						- BOTTOM_BAR_MARGIN[SOUTH]);
		openingDescriptionLabelRect = new Rectangle(statusLabelRect.width,
				boardPlusTopBarHeight, (width / 2) - BOTTOM_BAR_MARGIN[EAST],
				bottomBarHeight - BOTTOM_BAR_MARGIN[NORTH]
						- BOTTOM_BAR_MARGIN[SOUTH]);

		boardTopLeft = new Point(BOARD_MARGIN[WEST], topBarHeight
				+ coolbarRect.height + BOARD_MARGIN[NORTH]);

		int nameLabelHeight = getOneCharSizeInFont(board
				.getWhiteNameRatingLabel().getFont(), gc).y;
		int nameLabelStartX = BOARD_MARGIN[WEST] + boardHeight
				+ NAME_LABEL_MARGIN[WEST];

		int clockHeight = getOneCharSizeInFont(board.getWhiteClockLabel()
				.getFont(), gc).y * 2;
		int clockStartX = BOARD_MARGIN[WEST] + boardHeight + CLOCK_MARGIN[WEST];

		if (board.isWhiteOnTop()) {
			whiteNameLabelRect = new Rectangle(nameLabelStartX, boardTopLeft.y,
					width - nameLabelStartX - NAME_LABEL_MARGIN[EAST],
					nameLabelHeight);
			blackNameLabelRect = new Rectangle(nameLabelStartX, boardTopLeft.y
					+ boardHeight / 2, whiteNameLabelRect.width,
					whiteNameLabelRect.height);

			whiteClockRect = new Rectangle(clockStartX, whiteNameLabelRect.y
					+ whiteNameLabelRect.height + CLOCK_MARGIN[NORTH], width
					- clockStartX - CLOCK_MARGIN[EAST], clockHeight);
			blackClockRect = new Rectangle(clockStartX, whiteClockRect.y
					+ boardHeight / 2, whiteClockRect.width,
					whiteClockRect.height);
		} else {
			blackNameLabelRect = new Rectangle(nameLabelStartX, boardTopLeft.y,
					width - nameLabelStartX - NAME_LABEL_MARGIN[EAST],
					nameLabelHeight);
			whiteNameLabelRect = new Rectangle(nameLabelStartX, boardTopLeft.y
					+ boardHeight / 2, blackNameLabelRect.width,
					blackNameLabelRect.height);

			blackClockRect = new Rectangle(clockStartX, blackNameLabelRect.y
					+ whiteNameLabelRect.height + CLOCK_MARGIN[NORTH], width
					- clockStartX - CLOCK_MARGIN[EAST], clockHeight);
			whiteClockRect = new Rectangle(clockStartX, blackClockRect.y
					+ boardHeight / 2, blackClockRect.width,
					blackClockRect.height);
		}

		int pieceJailStartX = BOARD_MARGIN[WEST] + boardHeight
				+ PIECE_JAIL_MARGIN[WEST];
		pieceJailRects = new Rectangle[board.getPieceJailSquares().length];
		if (board.isWhitePieceJailOnTop()) {
			pieceJailRects[WP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 2, boardSquareSize, boardSquareSize);
			pieceJailRects[WN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 2,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 2,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WQ] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 3, boardSquareSize, boardSquareSize);
			pieceJailRects[WR] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WK] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);

			pieceJailRects[BP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 6, boardSquareSize, boardSquareSize);
			pieceJailRects[BN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 6,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 6,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BQ] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 7, boardSquareSize, boardSquareSize);
			pieceJailRects[BR] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BK] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
		} else {
			pieceJailRects[BP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 2, boardSquareSize, boardSquareSize);
			pieceJailRects[BN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 2,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 2,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BQ] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 3, boardSquareSize, boardSquareSize);
			pieceJailRects[BR] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);
			pieceJailRects[BK] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 3,
					boardSquareSize, boardSquareSize);

			pieceJailRects[WP] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 6, boardSquareSize, boardSquareSize);
			pieceJailRects[WN] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 6,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WB] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 6,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WQ] = new Rectangle(pieceJailStartX, boardTopLeft.y
					+ boardSquareSize * 7, boardSquareSize, boardSquareSize);
			pieceJailRects[WR] = new Rectangle(pieceJailStartX
					+ boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
			pieceJailRects[WK] = new Rectangle(pieceJailStartX + 2
					* boardSquareSize, boardTopLeft.y + boardSquareSize * 7,
					boardSquareSize, boardSquareSize);
		}

		gc.dispose();
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		LOG.debug("in layout ...");
		long startTime = System.currentTimeMillis();
		setLayoutData();

		// int width = board.getSize().x;
		// int height = board.getSize().y;
		// board.getCoolbar().setBounds(coolbarRect);

		board.getGameDescriptionLabel().setBounds(gameDescriptionLabelRect);

		board.getCurrentPremovesLabel().setBounds(currentPremovesLabelRect);

		layoutChessBoard(boardTopLeft, boardSquareSize);

		board.getStatusLabel().setBounds(statusLabelRect);
		board.getOpeningDescriptionLabel().setBounds(
				openingDescriptionLabelRect);

		board.getWhiteNameRatingLabel().setBounds(whiteNameLabelRect);
		board.getBlackNameRatingLabel().setBounds(blackNameLabelRect);

		board.getWhiteClockLabel().setBounds(whiteClockRect);
		board.getBlackClockLabel().setBounds(blackClockRect);

		for (int i = 0; i < pieceJailRects.length; i++) {
			if (pieceJailRects[i] != null) {
				board.getPieceJailSquares()[i].setBounds(pieceJailRects[i]);
			}
		}
		LOG.debug("Layout completed in "
				+ (System.currentTimeMillis() - startTime));
	}

	public RightOrientedLayout(ChessBoard board) {
		super(board);
	}

	@Override
	public String getName() {
		return "Right Oriented";
	}
}
