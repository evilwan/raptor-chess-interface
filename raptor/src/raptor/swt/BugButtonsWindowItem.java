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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.action.RaptorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.ActionScriptService;

public class BugButtonsWindowItem implements RaptorConnectorWindowItem {
	static final Log LOG = LogFactory.getLog(BugButtonsWindowItem.class);

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII };

	protected Composite composite;
	protected String title;
	protected Connector connector;
	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().startsWith("bugbuttons")) {
				if (composite != null) {
					updateFromPrefs();
					composite.redraw();
				}
			}
		}
	};

	public BugButtonsWindowItem(Connector connector) {
		title = connector.getShortName() + "(Bughouse Buttons)";
		this.connector = connector;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		RaptorAction[] scripts = ActionScriptService.getInstance().getActions(
				RaptorActionContainer.BugButtons);
		if (!isHorizontalLayout(newQuadrant)) {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(2, true));
		} else {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(
					scripts.length / 2, true));
		}
		removeButtons();
		addButtons(scripts);
		Raptor.getInstance().getPreferences().setValue(
				connector.getShortName() + "-"
						+ PreferenceKeys.BUG_BUTTONS_QUADRANT, newQuadrant);

		updateFromPrefs();
		composite.layout(true, true);
	}

	public boolean confirmClose() {
		return true;
	}

	public boolean confirmQuadrantMove() {
		return true;
	}

	public void dispose() {
		composite.dispose();
		Raptor.getInstance().getPreferences().removePropertyChangeListener(
				propertyChangeListener);
		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed BugButtonsWindowItem.");
		}
	}

	public Connector getConnector() {
		return connector;
	}

	public Composite getControl() {
		return composite;
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
				connector.getShortName() + "-"
						+ PreferenceKeys.BUG_BUTTONS_QUADRANT);
	}

	public String getTitle() {
		return title;
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		Raptor.getInstance().getPreferences().addPropertyChangeListener(
				propertyChangeListener);

		RaptorAction[] actions = ActionScriptService.getInstance().getActions(
				RaptorActionContainer.BugButtons);

		composite = new Composite(parent, SWT.NONE);
		Quadrant quadrant = getPreferredQuadrant();
		if (!isHorizontalLayout(quadrant)) {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(2, true));
		} else {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(
					actions.length / 2, true));
		}

		addButtons(actions);
		updateFromPrefs();
	}

	public void onActivate() {
	}

	public void onPassivate() {
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}

	protected void addButtons(RaptorAction[] actions) {

		for (final RaptorAction action : actions) {
			Button button = new Button(composite, SWT.FLAT | SWT.NO_FOCUS);
			button.setText(action.getName());
			button.setToolTipText(action.getDescription());
			button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					RaptorAction loadedAction = ActionScriptService
							.getInstance().getAction(action.getName());
					loadedAction.setConnectorSource(connector);
					loadedAction.run();
				}
			});
		}
	}

	protected boolean isHorizontalLayout(Quadrant quadrant) {
		return quadrant == Quadrant.I || quadrant == Quadrant.VI
				|| quadrant == Quadrant.VII;
	}

	protected void removeButtons() {
		Control[] children = composite.getTabList();
		for (Control control : children) {
			control.setVisible(false);
			control.dispose();
		}
	}

	protected void updateFromPrefs() {
		Control[] children = composite.getTabList();
		for (Control control : children) {
			control.setFont(Raptor.getInstance().getPreferences().getFont(
					PreferenceKeys.BUG_BUTTONS_FONT));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("updateFromPrefs");
		}
	}
}
