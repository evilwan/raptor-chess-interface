package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;

public class BrowserWindowItem implements RaptorWindowItem {

	protected Browser browser;
	protected String url;
	protected String title;

	public BrowserWindowItem(String title, String url) {
		this.url = url;
		this.title = title;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	public boolean confirmClose() {
		return true;
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		browser.dispose();
	}

	public Composite getControl() {
		return browser;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.ALL_BROWSER_QUADRANT);
	}

	public String getTitle() {
		return title;
	}

	public void init(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl(url);

	}

	public void onActivate() {
	}

	public void onPassivate() {
	}

	public void onReparent(Composite newParent) {
		url = browser.getUrl();
		browser.dispose();
		init(newParent);
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}
}
