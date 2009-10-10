package raptor.swt.chess;

import org.eclipse.swt.widgets.Composite;

public interface ChessBoardMoveList {
	public void setController(ChessBoardController controller);
	public ChessBoardController getChessBoardController();
	public void updateToGame();
	public int getPreferredWeight();
	public Composite getControl(Composite parent);
	public void select(int halfMoveIndex);
}
