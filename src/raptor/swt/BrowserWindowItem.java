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
package raptor.swt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.pref.PreferenceKeys;

/**
 * Please use BrowserUtils.openUrl instead of this class unless you specifically
 * want a new browser window opened.
 */
public class BrowserWindowItem implements RaptorWindowItem {
	private static final Log LOG = LogFactory.getLog(BrowserWindowItem.class);

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII,Quadrant.IX };

	protected Composite addressBar;
	protected Browser browser;
	protected Composite composite;
	protected String url;
	private String storedHtml = null;

	public BrowserWindowItem(String url) {
		this.url = url;
	}

	public BrowserWindowItem(String html, boolean PLACEHOLDER) {
		this("");
		setHTML(html);
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				PreferenceKeys.APP_BROWSER_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		try {
			browser.stop();
			browser.setText("<html><body>I have to do this to get a browser "
					+ "to shut down correctly. Duno why.</body><html>");
			browser.close();
			browser.dispose();
		} catch (Throwable t) {
			LOG.error("Error stoping closing and disposing browser", t);
		}
		try {
			composite.dispose();
		} catch (Throwable t) {
			LOG.error("Error disposing composite", t);
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed BrowserWindowItem");
		}
	}

	public Composite getControl() {
		return composite;
	}

	public String getHTML() {
		return storedHtml;
	}

	public Image getImage() {
		return null;
	}

	/**
	 * Returns a list of the quadrants this window item can move to.
	 */
	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				PreferenceKeys.APP_BROWSER_QUADRANT);
	}

	public String getTitle() {
		return "Browser";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(SWTUtils.createMarginlessGridLayout(1, false));

		addressBar = new Composite(composite, SWT.NONE);
		addressBar
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addressBar.setLayout(SWTUtils.createMarginlessGridLayout(5, false));

		Button backButton = new Button(addressBar, SWT.FLAT);
		backButton.setImage(Raptor.getInstance().getIcon("back"));
		backButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		backButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.stop();
				browser.back();
			}

		});

		Button forwardButton = new Button(addressBar, SWT.FLAT);
		forwardButton.setImage(Raptor.getInstance().getIcon("next"));
		forwardButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));

		forwardButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.stop();
				browser.forward();
			}
		});

		Button refreshButton = new Button(addressBar, SWT.FLAT);
		refreshButton.setImage(Raptor.getInstance().getIcon("clockwise"));
		refreshButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));

		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browser.stop();
				browser.refresh();
			}
		});

		final Text urlText = new Text(addressBar, SWT.SINGLE | SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		urlText.setText(url);
		urlText.setEditable(true);
		urlText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					url = urlText.getText();
					setUrl(url);
				}
			}
		});

		Button submit = new Button(addressBar, SWT.FLAT);
		submit.setImage(Raptor.getInstance().getIcon("enter"));
		submit.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false));
		submit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				url = urlText.getText();
				setUrl(url);
			}
		});

		browser = new Browser(composite, SWT.NONE);

		browser.addOpenWindowListener(new OpenWindowListener() {
			public void open(WindowEvent event) {
				if (!event.required) {
					return;
				}
				BrowserWindowItem newWindowItem = new BrowserWindowItem("");
				Raptor.getInstance().getWindow().addRaptorWindowItem(
						newWindowItem, false);
				event.browser = newWindowItem.browser;
			}
		});
		browser.addCloseWindowListener(new CloseWindowListener() {
			public void close(WindowEvent event) {
				Raptor.getInstance().getWindow().disposeRaptorWindowItem(
						BrowserWindowItem.this);
			}
		});
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				urlText.setText(event.location);
			}
		});

		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (getHTML() != null) {
			browser.setText(getHTML());
		} else {
			setUrl(url);
		}

		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				urlText.setText(browser.getUrl());
			}
		});
	}

	public void onActivate() {
	}

	public void onPassivate() {
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

	public void setHTML(String html) {
		storedHtml = html;
		if (browser != null) {
			browser.setText(html);
		}
	}

	public void setUrl(String url) {
		if (StringUtils.isNotBlank(url)) {
			browser.stop();
			if (!browser.setUrl(url)) {
				browser
						.setText("<html><<body><h1>The page could not be loaded.</h1></body>/html>");
			}
		}
	}
}
