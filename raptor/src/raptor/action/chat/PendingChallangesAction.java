/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
							.setText(challenge.isLoggedInUserChanneling() ? "remove "
									+ challenge.getDescription()
									: "accept " + challenge.getDescription());
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							if (challenge.isLoggedInUserChanneling()) {
								getChatConsoleControllerSource()
										.getConnector()
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