package raptor.action.game;

import raptor.action.AbstractRaptorAction;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ToolBarItemKey;

public class TryVariation extends AbstractRaptorAction {
	
	public TryVariation() {
		setName("TRY VARIATION");
		setDescription("If checked, this will update the position to the latest moves as"
				+ " they arrive. If unchecked this will not update the position to the latest "
				+ "moves as they arrive. This is useful when you are looking at a previous move.");
		setCategory(Category.GameCommands);
	}

	@Override
	public void run() {
		if (getChessBoardControllerSource() != null) {
			if (getChessBoardControllerSource().isToolItemSelected(
					ToolBarItemKey.TRY_VARIATION) 
					&& getChessBoardControllerSource() instanceof InactiveController) {
				((InactiveController)getChessBoardControllerSource()).setVariationMode(true);
			}
			else if (getChessBoardControllerSource() instanceof InactiveController) {
				((InactiveController)getChessBoardControllerSource()).setVariationMode(false);
			}
		}
	}

}
