package raptor.gui.board;

public interface BoardListener {
	public void moveCancelled(String gameId, int fromSquare, boolean isDnd);

	public void moveInitiated(String gameId, int square, boolean isDnd);

	public void moveMade(String gameId, int fromSquare, int toSquare);

	public void onMiddleClick(String gameId, int square);

	public void onRightClick(String gameId, int square);
}
