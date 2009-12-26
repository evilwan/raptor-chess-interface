package raptor.action.chat;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.service.GameService.Challenge;

public class PendingChallangesAction extends AbstractRaptorAction {
	public PendingChallangesAction() {
		setName("Show Pending Challenges");
		setDescription("Lights up if there is a pending offer. When clicking on the icon you are presented with a popup menu to accept/remove a challenge.");
		setCategory(Category.ConsoleCommands);
		setIcon("dimLightbulb");
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			Challenge[] challenges = getChatConsoleControllerSource()
					.getConnector().getGameService().getChallenges();

			if (challenges.length == 0) {
				Raptor
						.getInstance()
						.alert(
								"There are no challenges pending or challanges issued.");
			} else {
				Arrays.sort(challenges, new Comparator<Challenge>() {

					public int compare(Challenge arg0, Challenge arg1) {
						if (arg0.isLoggedInUserChanneling()
								&& !arg1.isLoggedInUserChanneling()) {
							return 1;
						} else if (!arg0.isLoggedInUserChanneling()
								&& arg1.isLoggedInUserChanneling()) {
							return -1;
						} else {
							return arg0.getId().compareTo(arg1.getId());
						}
					}
				});

				Menu menu = new Menu(getChatConsoleControllerSource()
						.getChatConsole().getShell(), SWT.POP_UP);
				for (final Challenge challenge : challenges) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item
							.setText(challenge.isLoggedInUserChanneling() ? "remove challenge "
									+ challenge.getDescription()
									: "accept challenge "
											+ challenge.getDescription());
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							if (challenge.isLoggedInUserChanneling()) {
								getChatConsoleControllerSource().getConnector()
										.sendMessage(
												"withdraw " + challenge.getId(),
												true);
							} else {
								getChatConsoleControllerSource().getConnector()
										.sendMessage(
												"accept " + challenge.getId(),
												true);
							}
						}
					});
				}

				menu.setLocation(Raptor.getInstance().getDisplay()
						.getCursorLocation());
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!Raptor.getInstance().getDisplay().readAndDispatch()) {
						Raptor.getInstance().getDisplay().sleep();
					}
				}
				menu.dispose();
			}
			wasHandled = true;
		}

		if (!wasHandled) {
			Raptor.getInstance().alert(
					getName() + " is only avalible from ChatConsole sources.");
		}
	}
}