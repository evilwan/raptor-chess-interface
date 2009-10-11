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
package raptor.connector.fics.chat.john;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.connector.ics.IcsConnector;
import raptor.service.BughouseService;
import raptor.swt.ItemChangedListener;

public class BugArena implements RaptorWindowItem {

	private enum myTabs {
		TAB_AVAILABLETEAMS, TAB_GAMES, TAB_UNPARTNERED;
	}

	Composite composite;
	IcsConnector connector;

	String title;

	public BugArena(String title, IcsConnector connector) {
		this.title = title;
		this.connector = connector;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	public boolean confirmClose() {
		return !false;
	}

	public boolean confirmQuadrantMove() {
		return !false;
	}

	public void dispose() {
		composite.dispose();
	}

	public Composite getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
		// PreferenceKeys.APP_BROWSER_QUADRANT
				"app-bug-arena");
	}

	public String getTitle() {
		return title;
	}

	public Control getToolbar(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void init(Composite parent) {
		// TODO Auto-generated method stub
		BughouseService service = connector.getBughouseService();
		myTabs tab = myTabs.TAB_UNPARTNERED;
		if (tab == myTabs.TAB_UNPARTNERED) {
			Partnership[] array = service.getAvailablePartnerships();
			for (Partnership partnership : array) {
				Bugger[] buggers = partnership.getBuggers();
				for (Bugger myBugger : buggers) {
					Button b = new Button(composite, SWT.PUSH);
					b.setText(myBugger.getUsername() + "("
							+ myBugger.getRating() + ")");
				}
			}
		}
	}

	public void onActivate() {
		composite.layout(true);
	}

	public void onPassivate() {
	}

	public boolean onReparent(Composite newParent) {
		// TO DO: get cday to help with this.
		composite.dispose();
		init(newParent);

		return false;
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

}
