package raptor.action.game;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.chess.Variant;
import raptor.swt.chess.controller.ExamineController;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ObserveController;

public class ToggleEngineAnalysisAction extends AbstractRaptorAction {
	public ToggleEngineAnalysisAction() {
		setName("Toggle Engine Analysis");
		setDescription("Shows chess engine analysis.");
		setIcon("calculator");
		setCategory(Category.GameCommands);
	}

	public void run() {
		boolean wasHandled = false;
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource() instanceof InactiveController
					|| getChessBoardControllerSource() instanceof ExamineController
					|| getChessBoardControllerSource() instanceof ObserveController) {

				if (getChessBoardControllerSource().getGame().getVariant() == Variant.classic
						|| getChessBoardControllerSource().getGame()
								.getVariant() == Variant.wild) {
					getChessBoardControllerSource().onEngineAnalysis();
				}
				wasHandled = true;
			}
		}

		if (!wasHandled) {
			Raptor
					.getInstance()
					.alert(
							"Toggle Engine Analysis is not available for this chess board");
		}
	}

}