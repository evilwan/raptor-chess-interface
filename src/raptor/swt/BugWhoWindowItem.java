package raptor.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorConnectorWindowItem;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.BughouseService;
import raptor.service.ThreadService;

public class BugWhoWindowItem implements RaptorConnectorWindowItem {
	public static final Quadrant[] MOVE_TO_QUADRANTS = { Quadrant.I,
			Quadrant.II, Quadrant.III, Quadrant.IV, Quadrant.V, Quadrant.VI,
			Quadrant.VII, Quadrant.VIII, Quadrant.IX };

	protected BughouseService service;
	protected TabFolder tabFolder;
	protected BugGames bugGames;
	protected BugPartners bugPartners;
	protected BugTeams bugTeams;
	protected Composite composite;
	protected boolean isActive = false;

	protected Runnable timer = new Runnable() {
		public void run() {
			if (isActive) {
				service.refreshGamesInProgress();
				ThreadService
						.getInstance()
						.scheduleOneShot(
								Raptor
										.getInstance()
										.getPreferences()
										.getInt(
												PreferenceKeys.APP_WINDOW_ITEM_POLL_INTERVAL) * 1000,
								this);
			}
		}
	};

	public BugWhoWindowItem(BughouseService service) {
		this.service = service;
	}

	public void addItemChangedListener(ItemChangedListener listener) {
	}

	/**
	 * Invoked after this control is moved to a new quadrant.
	 */
	public void afterQuadrantMove(Quadrant newQuadrant) {
		Raptor.getInstance().getPreferences().setValue(
				service.getConnector().getShortName() + "-"
						+ PreferenceKeys.BUG_WHO_QUADRANT, newQuadrant);
	}

	public boolean confirmClose() {
		return true;
	}

	public void dispose() {
		isActive = false;
		composite.dispose();
	}

	public Connector getConnector() {
		return service.getConnector();
	}

	public Control getControl() {
		return composite;
	}

	public Image getImage() {
		return null;
	}

	public Quadrant[] getMoveToQuadrants() {
		return MOVE_TO_QUADRANTS;
	}

	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				service.getConnector().getShortName() + "-"
						+ PreferenceKeys.BUG_WHO_QUADRANT);
	}

	public String getTitle() {
		return service.getConnector().getShortName() + "(Bug Who)";
	}

	public Control getToolbar(Composite parent) {
		return null;
	}

	public void init(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		tabFolder = new TabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TabItem bugPartnersTab = new TabItem(tabFolder, SWT.NONE);
		bugPartnersTab.setText("Partners");
		bugPartnersTab.setControl(bugPartners = new BugPartners(tabFolder,
				service));

		TabItem bugTeamsTab = new TabItem(tabFolder, SWT.NONE);
		bugTeamsTab.setText("Teams");
		bugTeamsTab.setControl(bugTeams = new BugTeams(tabFolder, service));

		TabItem bugGamesTab = new TabItem(tabFolder, SWT.NONE);
		bugGamesTab.setText("Games");
		bugGamesTab.setControl(bugGames = new BugGames(tabFolder, service));

		tabFolder.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BUG_ARENA_SELECTED_TAB,
						tabFolder.getSelectionIndex());

				activateSelectedControl();

			}
		});

		tabFolder.setSelection(Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.BUG_ARENA_SELECTED_TAB));
	}

	protected void activateSelectedControl() {
		Control selectedControl = tabFolder.getItem(
				tabFolder.getSelectionIndex()).getControl();

		if (selectedControl == bugGames) {
			bugTeams.onPassivate();
			bugPartners.onPassivate();
			bugGames.onActivate();
		} else if (selectedControl == bugTeams) {
			bugTeams.onActivate();
			bugPartners.onPassivate();
			bugGames.onPassivate();

		} else if (selectedControl == bugPartners) {
			bugTeams.onPassivate();
			bugPartners.onActivate();
			bugGames.onPassivate();
		}
	}

	public void onActivate() {
		if (!isActive) {
			isActive = true;
			activateSelectedControl();
		}
	}

	public void onPassivate() {
		if (isActive) {
			isActive = false;
			bugTeams.onPassivate();
			bugPartners.onPassivate();
			bugGames.onPassivate();
		}
	}

	public void removeItemChangedListener(ItemChangedListener listener) {
	}
}
