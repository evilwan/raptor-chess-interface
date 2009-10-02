package raptor.connector.fics.chat.john;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.connector.ics.IcsConnector;
import raptor.service.BughouseService;
import raptor.swt.ItemChangedListener;

public class BugArena implements RaptorWindowItem {

	private enum myTabs {
		TAB_UNPARTNERED, TAB_AVAILABLETEAMS, TAB_GAMES;
	}

	Composite composite;
	String title;

	IcsConnector connector;

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
