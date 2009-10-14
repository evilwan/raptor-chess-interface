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
import raptor.RaptorWindowItem;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.script.ChatScript;
import raptor.script.ChatScript.ChatScriptType;
import raptor.service.ScriptService;

public class BugButtonsWindowItem implements RaptorWindowItem {
	static final Log LOG = LogFactory.getLog(BugButtonsWindowItem.class);

	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.VIII };

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

	protected void addButtons(ChatScript[] scripts) {

		for (final ChatScript script : scripts) {
			Button button = new Button(composite, SWT.FLAT);
			button.setText(script.getName());
			button.setToolTipText(script.getDescription());
			button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ScriptService.getInstance().getChatScript(script.getName())
							.execute(connector.getChatScriptContext());
				}
			});
		}
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		ChatScript[] scripts = ScriptService.getInstance().getChatScripts(
				connector, ChatScriptType.BugButtonsOneShot);
		if (newQuadrant == Quadrant.II) {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(2, true));
		} else {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(
					scripts.length / 2, true));
		}
		removeButtons();
		addButtons(scripts);
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
		if (LOG.isDebugEnabled()) {
			LOG.debug("Disposed BugButtonsWindowItem.");
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

		ChatScript[] scripts = ScriptService.getInstance().getChatScripts(
				connector, ChatScriptType.BugButtonsOneShot);

		composite = new Composite(parent, SWT.NONE);
		Quadrant quadrant = getPreferredQuadrant();
		if (quadrant == Quadrant.II) {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(2, true));
		} else {
			composite.setLayout(SWTUtils.createMarginlessGridLayout(
					scripts.length / 2, true));
		}

		addButtons(scripts);
		updateFromPrefs();
	}

	public void onActivate() {
	}

	public void onPassivate() {
	}

	protected void removeButtons() {
		Control[] children = composite.getTabList();
		for (Control control : children) {
			control.setVisible(false);
			control.dispose();
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
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
