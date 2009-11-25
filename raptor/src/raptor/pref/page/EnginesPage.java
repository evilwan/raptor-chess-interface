package raptor.pref.page;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.engine.uci.UCIEngine;
import raptor.engine.uci.UCIOption;
import raptor.engine.uci.options.UCIButton;
import raptor.engine.uci.options.UCICheck;
import raptor.engine.uci.options.UCICombo;
import raptor.engine.uci.options.UCISpinner;
import raptor.engine.uci.options.UCIString;
import raptor.pref.fields.ListFieldEditor;
import raptor.service.UCIEngineService;

public class EnginesPage extends PreferencePage {
	protected ListFieldEditor parameters;
	protected Combo enginesCombo;
	protected Text userNameText;
	protected Text processLocationText;
	protected Label engineName;
	protected Label engineAuthor;
	protected ScrolledComposite optionsComposite;
	protected Button connectButton;
	protected Button pickFileButton;
	protected Button defaultButton;
	protected Button saveButton;
	protected Button deleteButton;
	protected UCIEngine currentEngine;
	protected HashMap<String, Control> optionNameToControl = new HashMap<String, Control>();
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
										+ "process and giving it a user name then click the connect button and you can configure the "
										+ "engines settings. When you are finished click the apply button.",
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
					processLocationText.setText(selected);
					onConnect();
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

		connectButton = new Button(parent, SWT.LEFT);
		connectButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		connectButton.setText("Connect to Engine");
		connectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onConnect();
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

		optionsComposite = new ScrolledComposite(parent, SWT.BORDER
				| SWT.V_SCROLL);
		optionsComposite.setMinWidth(455);
		optionsComposite.setMinHeight(405);
		optionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 2, 1));

		reload();
		return parent;
	}

	protected void loadEngine(UCIEngine engine) {
		if (currentEngine != null && currentEngine != engine) {
			currentEngine.disconnect();
		}
		if (!engine.isConnected()) {
			if (!engine.connect()) {
				Raptor
						.getInstance()
						.alert(
								"Could not connect to the engine. Make sure it is a UCI chess engine.");
				return;
			}
			currentEngine = engine;
			engineName.setText(StringUtils.defaultString(currentEngine
					.getEngineName(), ""));
			engineAuthor.setText(StringUtils.defaultString(currentEngine
					.getEngineAuthor(), ""));
			processLocationText.setText(engine.getProcessPath());
			userNameText.setText(engine.getUserName());
			updateCustomControls();
		}
	}

	protected void onConnect() {
		if (currentEngine != null) {
			currentEngine.disconnect();
		}
		if (StringUtils.isNotBlank(processLocationText.getText())) {
			currentEngine = new UCIEngine();
			currentEngine.setProcessPath(processLocationText.getText());
			if (currentEngine.connect()) {
				engineName.setText(StringUtils.defaultString(currentEngine
						.getEngineName(), ""));
				engineAuthor.setText(StringUtils.defaultString(currentEngine
						.getEngineAuthor(), ""));
				currentEngine.disconnect();
				updateCustomControls();

			} else {
				Raptor
						.getInstance()
						.alert(
								"Could not connect to the engine. Make sure it is a UCI chess engine.");
			}
		} else {
			Raptor
					.getInstance()
					.alert(
							"You must first select a process location before connecting.");
		}
	}

	protected void onDelete() {
		if (StringUtils.isNotBlank(userNameText.getText())) {
			UCIEngineService.getInstance().deleteConfiguration(
					userNameText.getText());
			reload();
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
			UCIEngine engine = UCIEngineService.getInstance().getUCIEngine(
					userNameText.getText());
			if (engine == null) {
				engine = new UCIEngine();
				engine.setUserName(userNameText.getText());
			}
			engine.setProcessPath(processLocationText.getText());
			String[] customNames = engine.getOptionNames();
			for (String optionName : customNames) {
				UCIOption option = engine.getOption(optionName);
				if (!(option instanceof UCIButton)) {
					if (option instanceof UCICheck) {
						option.setValue(((Button) optionNameToControl
								.get(option.getName())).getSelection() ? "true"
								: "false");
					} else if (option instanceof UCIString) {
						option.setValue(((Text) optionNameToControl.get(option
								.getName())).getText());
					} else if (option instanceof UCISpinner) {
						option.setValue(""
								+ ((Spinner) optionNameToControl.get(option
										.getName())).getSelection());
					} else if (option instanceof UCICombo) {
						option.setValue(((Combo) optionNameToControl.get(option
								.getName())).getText());
					}
				}
			}
			UCIEngineService.getInstance().saveConfiguration(engine);
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

	protected void updateCustomControls() {
		if (optionsComposite.getContent() != null) {
			optionsComposite.getContent().setVisible(false);
			optionsComposite.getContent().dispose();
		}

		Composite customControls = new Composite(optionsComposite, SWT.NONE);
		customControls.setLayout(new GridLayout(2, false));
		optionNameToControl.clear();

		String[] optionNames = currentEngine.getOptionNames();
		for (String optionName : optionNames) {
			UCIOption uciOption = currentEngine.getOption(optionName);
			if (!(uciOption instanceof UCIButton)) {
				if (uciOption instanceof UCICheck) {
					Button button = new Button(customControls, SWT.CHECK);
					button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
							false, false, 2, 1));
					button.setText(uciOption.getName());
					button.setSelection(StringUtils.equals(
							uciOption.getValue(), "true"));
					optionNameToControl.put(uciOption.getName(), button);

				} else {
					Label label = new Label(customControls, SWT.LEFT);
					label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
							false, false, 1, 1));
					label.setText(uciOption.getName());

					if (uciOption instanceof UCIString) {
						if (uciOption.getName().equalsIgnoreCase(
								"UCI_EngineAbout")) {
							CLabel label2 = new CLabel(customControls, SWT.LEFT);
							label2.setText(WordUtils.wrap(uciOption.getValue(),
									70));

						} else {
							Text text = new Text(customControls, SWT.BORDER
									| SWT.SINGLE);
							text.setText(StringUtils.defaultString(uciOption
									.getValue()));
							text.setLayoutData(new GridData(SWT.FILL,
									SWT.CENTER, true, false, 1, 1));
							optionNameToControl.put(uciOption.getName(), text);
						}
					} else if (uciOption instanceof UCICombo) {
						Combo combo = new Combo(customControls, SWT.DROP_DOWN
								| SWT.READ_ONLY);

						for (String value : ((UCICombo) uciOption).getOptions()) {
							combo.add(value);
						}
						if (StringUtils.isNotBlank(uciOption.getDefaultValue())) {
							for (int i = 0; i < combo.getItemCount(); i++) {
								if (StringUtils.equals(combo.getItem(i),
										uciOption.getValue())) {
									combo.select(i);
									break;
								}
							}
						}
						combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
								true, false, 1, 1));
						optionNameToControl.put(uciOption.getName(), combo);
					} else if (uciOption instanceof UCISpinner) {
						Spinner spinner = new Spinner(customControls, SWT.NONE);
						spinner.setMinimum(((UCISpinner) uciOption)
								.getMinimum());
						spinner.setMaximum(((UCISpinner) uciOption)
								.getMaximum());
						spinner.setPageIncrement(1);
						spinner.setDigits(0);
						if (StringUtils.isNotBlank(uciOption.getValue())) {
							spinner.setSelection(Integer.parseInt(uciOption
									.getValue()));
						}
						optionNameToControl.put(uciOption.getName(), spinner);
					}

				}
			}
		}
		customControls.pack(true);
		optionsComposite.setContent(customControls);
	}
}
