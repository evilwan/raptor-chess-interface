package raptor.action.game;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.AbstractRaptorAction;
import raptor.chess.Move;
import raptor.chess.PriorityMoveList;
import raptor.service.SoundService;
import raptor.swt.chess.ChessBoardController;
import raptor.swt.chess.ChessBoardWindowItem;

public class CastleShortAction extends AbstractRaptorAction {
	public CastleShortAction() {
		setName("O-O");
		setDescription("Castles Short (Only Visible In Fischer Random Games)");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			makeMove(getChessBoardControllerSource());
			wasHandled = true;
		}

		if (!wasHandled) {
			RaptorWindowItem[] items = Raptor.getInstance().getWindow()
					.getSelectedWindowItems(ChessBoardWindowItem.class);

			for (RaptorWindowItem item : items) {
				ChessBoardController controller = ((ChessBoardWindowItem) item)
						.getController();
				makeMove(controller);

			}
		}
	}

	protected void makeMove(ChessBoardController controller) {
		PriorityMoveList legals = controller.getGame().getLegalMoves();
		Move castlingMove = null;
		for (Move move : legals.asArray()) {
			if (move.isCastleShort()) {
				castlingMove = move;
				break;
			}
		}
		if (castlingMove == null) {
			SoundService.getInstance().playSound("illegalMove");
		} else {
			controller.userMadeMove(castlingMove.getFrom(), castlingMove
					.getTo());
		}
	}
}