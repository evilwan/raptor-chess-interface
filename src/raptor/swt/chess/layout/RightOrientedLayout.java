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

	public static final int[] TOP_BAR_MARGIN = { 5, 5, 5, 5 };
	public static final int[] BOTTOM_BAR_MARGIN = { 5, 5, 5, 10 };
	public static final int[] BOARD_MARGIN = { 0, 0, 10, 10 };

	protected Point boardTopLeft;
	protected Rectangle gameDescriptionLabelRect;
	protected Rectangle currentPremovesLabelRect;
	protected Rectangle openingDescriptionLabelRect;
	protected Rectangle statusLabelRect;
	protected Rectangle coolbarRect;
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
			return  SWT.FLAT | SWT.BORDER;
		default:
			return SWT.NONE;
		}
	}

	protected void setLayoutData() {
		GC gc = new GC(board.getBoardPanel());

		int width = board.getBoardPanel().getSize().x;
		int height = board.getBoardPanel().getSize().y;
		
//		board.getCoolbar().layout();
//		
		coolbarRect = new Rectangle(0,0,0,0);

		topBarHeight = Math.max(getOneCharSizeInFont(board.getStatusLabel()
				.getFont(), gc).y, getOneCharSizeInFont(board
				.getCurrentPremovesLabel().getFont(), gc).y)
				+ TOP_BAR_MARGIN[NORTH] + TOP_BAR_MARGIN[SOUTH];
		bottomBarHeight = Math.max(getOneCharSizeInFont(board.getStatusLabel()
				.getFont(), gc).y, getOneCharSizeInFont(board
				.getOpeningDescriptionLabel().getFont(), gc).y)
				+ BOTTOM_BAR_MARGIN[NORTH] + BOTTOM_BAR_MARGIN[SOUTH];

		boardSquareSize = (height - coolbarRect.height - topBarHeight - bottomBarHeight - BOARD_MARGIN[NORTH] - BOARD_MARGIN[SOUTH]) / 8;
		boardHeight = boardSquareSize*8;

		gameDescriptionLabelRect = new Rectangle(TOP_BAR_MARGIN[WEST],
				coolbarRect.height + TOP_BAR_MARGIN[NORTH], (width / 2) - TOP_BAR_MARGIN[WEST],
				topBarHeight - TOP_BAR_MARGIN[NORTH] - TOP_BAR_MARGIN[SOUTH]);
		currentPremovesLabelRect = new Rectangle(
				gameDescriptionLabelRect.width, coolbarRect.height + TOP_BAR_MARGIN[NORTH],
				(width / 2) - TOP_BAR_MARGIN[EAST], topBarHeight- TOP_BAR_MARGIN[NORTH] - TOP_BAR_MARGIN[SOUTH]);
		
		int boardPlusTopBarHeight = coolbarRect.height + topBarHeight + boardHeight + BOTTOM_BAR_MARGIN[NORTH] + BOARD_MARGIN[NORTH] + BOARD_MARGIN[SOUTH];

		statusLabelRect = new Rectangle(BOTTOM_BAR_MARGIN[WEST],
				boardPlusTopBarHeight,
				(width / 2) - BOTTOM_BAR_MARGIN[WEST],  bottomBarHeight - BOTTOM_BAR_MARGIN[NORTH] - BOTTOM_BAR_MARGIN[SOUTH]);
		openingDescriptionLabelRect = new Rectangle(statusLabelRect.width,
				boardPlusTopBarHeight,
				(width / 2) - BOTTOM_BAR_MARGIN[EAST], bottomBarHeight - BOTTOM_BAR_MARGIN[NORTH] - BOTTOM_BAR_MARGIN[SOUTH]);

		boardTopLeft = new Point(BOARD_MARGIN[WEST],topBarHeight + coolbarRect.height + BOARD_MARGIN[NORTH]);

		gc.dispose();
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
        LOG.debug("in layout ...");
		long startTime = System.currentTimeMillis();
		setLayoutData();

//		int width = board.getSize().x;
//		int height = board.getSize().y;
		//board.getCoolbar().setBounds(coolbarRect);

		board.getGameDescriptionLabel().setBounds(gameDescriptionLabelRect);

		board.getCurrentPremovesLabel().setBounds(currentPremovesLabelRect);

		layoutChessBoard(boardTopLeft, boardSquareSize);

		board.getStatusLabel().setBounds(statusLabelRect);
		board.getOpeningDescriptionLabel().setBounds(
				openingDescriptionLabelRect);
		
		LOG.debug("Layout completed in " + (System.currentTimeMillis() - startTime));

		// int width = composite.getSize().x;
		// int height = composite.getSize().y;
		//
		// if (board.getPreferences().getBoolean(BOARD_IS_SHOW_COORDINATES)) {
		//
		// } else {
		//
		// }
		//
		// int squareSide = width > height ? height / 8 : width / 8;
		// if (squareSide % 2 != 0) {
		// squareSide -= 1;
		// }
		//
		// layoutChessBoard(new Point(0, 0), squareSide);

		// @SuppressWarnings("unused")
		// int x, y, xInit, coordinatesHeight, squareSide;
		//
		// if (isShowingCoordinates) {
		// squareSide = width > height ? height / 8 : width / 8;
		// if (squareSide % 2 != 0) {
		// squareSide -= 1;
		// }
		//
		// GC gc = new GC(ChessBoard.this);
		// gc.setFont(rankLabels[0].getFont());
		// int charWidth = gc.getFontMetrics().getAverageCharWidth() + 5;
		// int charHeight = gc.getFontMetrics().getAscent()
		// + gc.getFontMetrics().getDescent() + 6;
		// gc.dispose();
		//
		// squareSide -= Math.round(charWidth / 8.0);
		//
		// x = charWidth;
		// xInit = charWidth;
		// y = 0;
		//
		// for (int i = 0; i < rankLabels.length; i++) {
		// int multiplier = (isWhiteOnTop ? 7 - i : i);
		// rankLabels[i].setLocation(0, (int) (squareSide * multiplier
		// + squareSide / 2 - .4 * charHeight));
		// rankLabels[i].setSize(charWidth, charHeight);
		// }
		//
		// for (int i = 0; i < ChessBoard.this.fileLabels.length; i++) {
		// int multiplier = (isWhiteOnTop ? 7 - i : i);
		// fileLabels[i].setLocation((int) (charHeight * .4 + squareSide
		// * multiplier + squareSide / 2), (squareSide * 8));
		// fileLabels[i].setSize(charWidth, charHeight);
		// }
		//
		// coordinatesHeight = charHeight;
		//
		// } else {
		// squareSide = width > height ? height / 8 : width / 8;
		// if (squareSide % 2 != 0) {
		// squareSide -= 1;
		// }
		//
		// x = 0;
		// xInit = 0;
		// y = 0;
		// coordinatesHeight = 0;
		// }
		//
		// if (!isWhiteOnTop) {
		// for (int i = 7; i > -1; i--) {
		// for (int j = 0; j < 8; j++) {
		// squares[i][j].setBounds(x, y, squareSide, squareSide);
		//
		// x += squareSide;
		// }
		// x = xInit;
		// y += squareSide;
		//
		// }
		// } else {
		// for (int i = 0; i < 8; i++) {
		// for (int j = 7; j > -1; j--) {
		// squares[i][j].setBounds(x, y, squareSide, squareSide);
		//
		// x += squareSide;
		// }
		// x = xInit;
		// y += squareSide;
		// }
		// }

	}

	public RightOrientedLayout(ChessBoard board) {
		super(board);
	}

	@Override
	public String getName() {
		return "Right Oriented";
	}
}
