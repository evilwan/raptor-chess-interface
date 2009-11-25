package raptor.swt.chess;

import org.eclipse.swt.widgets.Composite;

public interface EngineAnalysisWidget {
	/**
	 * Clears the move list.
	 */
	public void clear();

	/**
	 * Creates the move list tying it to the specified parent. Subsequent calls
	 * to getControl will return this Composite as well.
	 */
	public Composite create(Composite parent);

	/**
	 * Returns the ChessBoardController being used by the move list.
	 * 
	 * @return
	 */
	public ChessBoardController getChessBoardController();

	/**
	 * Returns the control representing the move list.
	 */
	public Composite getControl();

	/**
	 * Forces a redraw of the move list.
	 */
	public void onShow();

	public void quit();

	/**
	 * Sets the chess board controller the move list is using.
	 */
	public void setController(ChessBoardController controller);

	/**
	 * Starts analysis.
	 */
	public void start();

	/**
	 * Stops all analysis.
	 */
	public void stop();

	/**
	 * Updates the move list to the current game.
	 */
	public void updateToGame();
}
