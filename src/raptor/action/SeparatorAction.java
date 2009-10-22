package raptor.action;

import raptor.Raptor;

/**
 * An action used for separators in raptor toolbars.
 */
public class SeparatorAction extends AbstractRaptorAction {
	SeparatorAction() {

		setDescription("A seprator.");
	}

	public void run() {
		Raptor
				.getInstance()
				.alert(
						"You managed to "
								+ "execute a separator action. Why on earth would you do that?");
	}
}
