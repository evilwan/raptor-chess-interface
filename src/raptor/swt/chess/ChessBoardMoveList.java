package raptor.swt.chess;

import org.eclipse.swt.widgets.Composite;

public interface ChessBoardMoveList {
	public Composite create(Composite parent);

	public void forceRedraw();

	public ChessBoardController getChessBoardController();

	public Composite getControl();

	public void select(int halfMoveIndex);

	public void setController(ChessBoardController controller);

	public void updateToGame();
}
