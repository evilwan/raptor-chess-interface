package raptor.pref.page;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.engine.uci.UCIEngine;
import raptor.service.UCIEngineService;
import raptor.swt.UCIEnginePropertiesDialog;

public class EnginesPage extends PreferencePage {
	protected Combo enginesCombo;
	protected Text userNameText;
	protected Text processLocationText;
	protected Label engineName;
	protected Label engineAuthor;
	protected Button propertiesButton;
	protected Button pickFileButton;
	protected Button defaultButton;
	protected Button deleteButton;
	protected UCIEngine currentEngine;
	protected Composite parent;
	protected boolean isBuildingEnginesCombo = false;

	public EnginesPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Chess Engines");
	}

	@Override
	protected Control createContents(Composite parent) {
		this.parent = parent;
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2,
				1));
		label
				.setText(WordUtils
						.wrap(
								"\tOn this page you can configure UCI Chess Engines. Start out by selecting the engine "
										+ "process and giving it a user name. When you are finished click the apply button.",
								70));
		Label enginesLabel = new Label(parent, SWT.LEFT);
		enginesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		enginesLabel.setText("Engines:");

		enginesCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		enginesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		enginesCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isBuildingEnginesCombo) {
					if (currentEngine != null) {
						currentEngine.quit();
					}
					loadEngine(UCIEngineService.getInstance().getUCIEngine(
							enginesCombo.getText()));
				}
			}
		});

		Label processLabel = new Label(parent, SWT.LEFT);
		processLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		processLabel.setText("Engine Location:");

		Composite processComposite = new Composite(parent, SWT.NONE);
		processComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		processComposite.setLayout(new GridLayout(2, false));

		processLocationText = new Text(processComposite, SWT.SINGLE
				| SWT.BORDER);
		processLocationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));

		pickFileButton = new Button(processComposite, SWT.PUSH);
		pickFileButton.setText("Select Location");
		pickFileButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		pickFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText("Select the UCI chess engine location");
				fd.setFilterPath("");
				String[] filterExt = { "*.*" };
				fd.setFilterExtensions(filterExt);
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					try {
						processLocationText.setText(new File(selected)
								.getCanonicalPath());
					} catch (IOException ioe) {
						Raptor.getInstance().onError("Error getting filename",
								ioe);
					}

					if (currentEngine != null) {
						currentEngine.quit();
					}
					currentEngine = new UCIEngine();
					currentEngine.setProcessPath(processLocationText.getText());
					currentEngine.setUserName(userNameText.getText());
					loadEngine(currentEngine);
				}
			}
		});

		defaultButton = new Button(parent, SWT.CHECK);
		defaultButton.setText("Default Engine");
		defaultButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));

		Label userNameLabel = new Label(parent, SWT.LEFT);
		userNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		userNameLabel.setText("Nickname:");

		userNameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label engineNameHeaderLabel = new Label(parent, SWT.LEFT);
		engineNameHeaderLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));
		engineNameHeaderLabel.setText("Engine Name:");

		engineName = new Label(parent, SWT.LEFT);
		engineName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label engineAuthorHeaderLabel = new Label(parent, SWT.LEFT);
		engineAuthorHeaderLabel.setLayoutData(new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 1, 1));
		engineAuthorHeaderLabel.setText("Engine Author:");

		engineAuthor = new Label(parent, SWT.LEFT);
		engineAuthor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		propertiesButton = new Button(parent, SWT.LEFT);
		propertiesButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));
		propertiesButton.setText("Engine Properties");
		propertiesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onProperties();
			}
		});

		deleteButton = new Button(parent, SWT.LEFT);
		deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		deleteButton.setText("Delete Engine");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDelete();
			}
		});

		reload();
		return parent;
	}

	protected void loadEngine(UCIEngine engine) {
		if (!engine.isConnected()) {
			if (!engine.connect()) {
				Raptor
						.getInstance()
						.alert(
								"Could not connect to the engine. Make sure it is a UCI chess engine.");
				return;
			}
		}
		currentEngine = engine;
		engineName.setText(StringUtils.defaultString(currentEngine
				.getEngineName(), ""));
		engineAuthor.setText(StringUtils.defaultString(currentEngine
				.getEngineAuthor(), ""));
		defaultButton.setSelection(engine.isDefault());
		processLocationText.setText(engine.getProcessPath());
		userNameText.setText(engine.getUserName());
	}

	protected void onDelete() {
		if (StringUtils.isNotBlank(userNameText.getText())) {
			UCIEngineService.getInstance().deleteConfiguration(
					userNameText.getText());
			reload();
		}
	}

	protected void onProperties() {
		if (StringUtils.isBlank(processLocationText.getText())) {
			Raptor.getInstance().alert("Engine location can not be empty.");
		} else if (StringUtils.isBlank(userNameText.getText())) {
			Raptor.getInstance().alert("Nickname location can not be empty.");
		} else if (currentEngine == null) {
			Raptor.getInstance().alert(
					"You need to test the engine by clicking the "
							+ "connect button before applying.");
		} else {
			if (currentEngine != null) {
			} else {
				currentEngine = new UCIEngine();
			}
			currentEngine.setProcessPath(processLocationText.getText());
			currentEngine.setUserName(userNameText.getText());

			UCIEnginePropertiesDialog dialog = new UCIEnginePropertiesDialog(
					getShell(), currentEngine);
			dialog.open();
		}
	}

	protected void onSave() {
		if (StringUtils.isBlank(processLocationText.getText())) {
			Raptor.getInstance().alert("Engine location can not be empty.");
		} else if (StringUtils.isBlank(userNameText.getText())) {
			Raptor.getInstance().alert("Nickname location can not be empty.");
		} else if (currentEngine == null) {
			Raptor.getInstance().alert(
					"You need to test the engine by clicking the "
							+ "connect button before applying.");
		} else {
			if (currentEngine != null) {
			} else {
				currentEngine = new UCIEngine();
			}
			currentEngine.setProcessPath(processLocationText.getText());
			currentEngine.setUserName(userNameText.getText());
			currentEngine.setDefault(defaultButton.getSelection());

			UCIEngineService.getInstance().saveConfiguration(currentEngine);
			reload();
		}
	}

	@Override
	protected void performApply() {
		onSave();
		super.performApply();
	}

	protected void reload() {
		isBuildingEnginesCombo = true;
		UCIEngine[] currentEngines = UCIEngineService.getInstance()
				.getUCIEngines();
		enginesCombo.removeAll();
		for (UCIEngine engine : currentEngines) {
			enginesCombo.add(engine.getUserName());
		}

		UCIEngine defaultEngine = UCIEngineService.getInstance()
				.getDefaultEngine();
		if (defaultEngine != null) {
			for (int i = 0; i < enginesCombo.getItemCount(); i++) {
				if (enginesCombo.getItem(i).equals(defaultEngine.getUserName())) {
					enginesCombo.select(i);
					loadEngine(defaultEngine);
					break;
				}
			}
		}
		isBuildingEnginesCombo = false;
	}
}
