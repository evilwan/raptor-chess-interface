package raptor.chess.pgn;

import raptor.chess.Result;

public interface PgnParserListener {
	public void onAnnotation(PgnParser parser, String annotation);

	public void onGameEnd(PgnParser parser, Result result);

	public void onGameStart(PgnParser parser);

	public void onHeader(PgnParser parser, String headerName, String headerValue);

	public void onMoveNag(PgnParser parser, Nag nag);

	public void onMoveNumber(PgnParser parser, int moveNumber);

	public void onMoveSublineEnd(PgnParser parser);

	public void onMoveSublineStart(PgnParser parser);

	public void onMoveWord(PgnParser parser, String word);

	public void onUnknown(PgnParser parser, String unknown);
}
