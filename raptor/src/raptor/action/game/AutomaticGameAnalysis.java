package raptor.action.game;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.controller.InactiveController;

public class AutomaticGameAnalysis extends AbstractRaptorAction {
	public AutomaticGameAnalysis() {
		setName("Automatic Game Analysis");
		setDescription("Analyse the whole game automatically with an engine to spot mistakes.");
		setIcon("monitor");
		setCategory(Category.GameCommands);
	}
	

	@Override
	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof InactiveController) {
				InactiveController controller = (InactiveController) getChessBoardControllerSource();
				controller.gotoHalfMove(controller.getGame().getMoveList().getSize());

				wasHandled = true;
			}
		}

		if (!wasHandled) {
			Raptor
					.getInstance()
					.alert(
							"Automatic Game Analysis is not available for this chess board");
		}
	}

}
