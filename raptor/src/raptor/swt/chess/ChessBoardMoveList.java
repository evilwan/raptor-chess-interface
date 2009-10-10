package raptor.swt.chess;

import org.eclipse.swt.widgets.Composite;

public interface ChessBoardMoveList {
	public ChessBoardController getChessBoardController();

	public Composite getControl(Composite parent);

	public int getPreferredWeight();

	public void select(int halfMoveIndex);

	public void setController(ChessBoardController controller);

	public void updateToGame();
}
