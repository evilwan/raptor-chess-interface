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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.service.GameService.Offer;

public class PendingOffersAction extends AbstractRaptorAction {
	public PendingOffersAction() {
		setName("Show Pending Offers");
		setDescription("Lights up if there is a pending offer. When clicking on the icon you are presented with a popup menu to accept/decline/withdraw offers.");
		setCategory(Category.ConsoleCommands);
		setIcon("dimLightbulb");
	}

	public void run() {
		boolean wasHandled = false;
		if (getChatConsoleControllerSource() != null) {
			Offer[] offers = getChatConsoleControllerSource().getConnector()
					.getGameService().getOffers();

			List<String[]> items = new ArrayList<String[]>(10);

			int declineCounter = 0;
			String declineAllCommand = null;
			for (Offer offer : offers) {
				if (offer.isDeclinable()) {
					items.add(new String[] { offer.getDeclineDescription(),
							offer.getDeclineCommand() });
					declineCounter++;
					declineAllCommand = offer.getDeclineAllCommand();
				}
				items.add(new String[] { offer.getDescription(),
						offer.getCommand() });
			}

			if (declineCounter > 1) {
				items.add(new String[] { "Decline all", declineAllCommand });
			}

			if (items.size() == 0) {
				Raptor
						.getInstance()
						.alert(
								"There are no pending offers.");
			} else {
				Collections.sort(items, new Comparator<String[]>() {

					public int compare(String[] arg0, String[] arg1) {
						return arg0[0].compareTo(arg1[0]);
					}
				});

				Menu menu = new Menu(getChatConsoleControllerSource()
						.getChatConsole().getShell(), SWT.POP_UP);
				for (final String[] array : items) {
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(array[0]);
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							getChatConsoleControllerSource().getConnector()
									.sendMessage(array[1], true);
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