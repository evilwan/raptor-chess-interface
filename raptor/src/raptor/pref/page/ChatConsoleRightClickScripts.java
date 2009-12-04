package raptor.pref.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.script.ParameterScript;
import raptor.script.ScriptConnectorType;
import raptor.script.ParameterScript.Type;
import raptor.service.ScriptService;
import raptor.swt.RaptorTable;
import raptor.swt.SWTUtils;
import raptor.swt.ScriptEditorDialog;

public class ChatConsoleRightClickScripts extends PreferencePage {

	protected RaptorTable icsActiveScriptsTable;
	protected RaptorTable icsInactiveScriptsTable;

	protected Composite composite;

	protected Text nameText;
	protected Text descriptionText;
	protected Button isActiveButton;
	protected CLabel scriptText;
	protected Combo connectorTypeCombo;

	protected Button saveButton;
	protected Button deleteButton;

	public ChatConsoleRightClickScripts() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Right-Click Scripts");
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Label textLabel = new Label(composite, SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\tRight-Click Scripts scripts have one parameter 'selection', the text selected when "
										+ "the console was right clicked. "
										+ "See Help->Raptor Help->Scripting for more details.",
								70));

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));
		tableComposite.setLayout(new GridLayout(3, false));

		icsActiveScriptsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		icsActiveScriptsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		icsActiveScriptsTable.addColumn("Active Right-Click Scripts", SWT.LEFT,
				100, false, null);
		icsActiveScriptsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = icsActiveScriptsTable.getTable()
								.getSelectionIndex();
						String selection = icsActiveScriptsTable.getTable()
								.getItem(selectedIndex).getText(0);
						loadControls(selection);
					}
				});

		Composite addRemoveComposite = new Composite(tableComposite, SWT.NONE);
		addRemoveComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, true));
		RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		addRemoveComposite.setLayout(rowLayout);
		Label strut = new Label(addRemoveComposite, SWT.NONE);
		strut.setText(" ");
		Label strut2 = new Label(addRemoveComposite, SWT.NONE);
		strut2.setText(" ");
		Label strut3 = new Label(addRemoveComposite, SWT.NONE);
		strut3.setText(" ");
		Label strut4 = new Label(addRemoveComposite, SWT.NONE);
		strut4.setText(" ");
		Button addButton = new Button(addRemoveComposite, SWT.PUSH);
		addButton.setImage(Raptor.getInstance().getIcon("back"));
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = icsInactiveScriptsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex == -1) {
					return;
				}
				String selection = icsInactiveScriptsTable.getTable().getItem(
						selectedIndex).getText(0);

				ParameterScript script = ScriptService.getInstance()
						.getParameterScript(selection);
				script.setActive(true);
				ScriptService.getInstance().save(script);
				refreshTables();
			}
		});

		Button removeButton = new Button(addRemoveComposite, SWT.PUSH);
		removeButton.setImage(Raptor.getInstance().getIcon("next"));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = icsActiveScriptsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex == -1) {
					return;
				}
				String selection = icsActiveScriptsTable.getTable().getItem(
						selectedIndex).getText(0);

				ParameterScript script = ScriptService.getInstance()
						.getParameterScript(selection);
				script.setActive(false);
				ScriptService.getInstance().save(script);
				refreshTables();
			}
		});

		icsInactiveScriptsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		icsInactiveScriptsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		icsInactiveScriptsTable.addColumn("Inactive Right-Click Script Name",
				SWT.LEFT, 100, true, null);
		icsInactiveScriptsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = icsInactiveScriptsTable.getTable()
								.getSelectionIndex();
						String selection = icsInactiveScriptsTable.getTable()
								.getItem(selectedIndex).getText(0);
						loadControls(selection);
					}
				});

		isActiveButton = new Button(composite, SWT.CHECK);
		isActiveButton.setText("Active");
		isActiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));

		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(SWTUtils.createMarginlessGridLayout(2, false));
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name: ");
		nameLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Composite descriptionComposite = new Composite(composite, SWT.NONE);
		descriptionComposite.setLayout(SWTUtils.createMarginlessGridLayout(2,
				false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description: ");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Text(descriptionComposite, SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setLayout(SWTUtils.createMarginlessGridLayout(2,
				false));
		controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));

		Label connectorTypeLabel = new Label(controlsComposite, SWT.NONE);
		connectorTypeLabel.setText("Connector: ");

		connectorTypeCombo = new Combo(controlsComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		for (ScriptConnectorType scriptConnectorType : ScriptConnectorType
				.values()) {
			connectorTypeCombo.add(scriptConnectorType.name());
		}
		connectorTypeCombo.select(0);

		Label scriptLabel = new Label(composite, SWT.NONE);
		scriptLabel.setText("Script:");
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		scriptText = new CLabel(composite, SWT.LEFT);
		scriptText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		scriptText.setText("\n \n \n \n \n \n");
		Button scriptEdit = new Button(composite, SWT.PUSH);
		scriptEdit.setText("Edit");
		scriptEdit.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		scriptEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScriptEditorDialog dialog = new ScriptEditorDialog(getShell(),
						"Edit script: " + nameText.getText());
				dialog.setInput(scriptText.getText());
				String result = dialog.open();
				if (StringUtils.isNotBlank(result)) {
					scriptText.setText(result.trim());
				}
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 3, 1));
		buttonComposite.setLayout(new RowLayout());
		saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ParameterScript newScript = ScriptService.getInstance()
						.getParameterScript(nameText.getText());
				if (newScript == null) {
					newScript = new ParameterScript();
				}
				newScript.setActive(isActiveButton.getSelection());
				newScript.setName(nameText.getText());
				newScript.setDescription(descriptionText.getText());
				newScript.setScript(scriptText.getText().trim());
				newScript.setConnectorType(ScriptConnectorType
						.valueOf(connectorTypeCombo.getItem(connectorTypeCombo
								.getSelectionIndex())));
				newScript.setType(Type.ConsoleRightClickScripts);
				ScriptService.getInstance().save(newScript);
				refreshTables();
			}
		});

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ParameterScript script = ScriptService.getInstance()
						.getParameterScript(nameText.getText());
				if (script.isSystemScript()) {
					script.setActive(false);
					MessageBox messageBox = new MessageBox(getShell(),
							SWT.ICON_INFORMATION);
					messageBox.setText("Alert");
					messageBox
							.setMessage("You can't delete a system script. The script was however made inactive.");
					messageBox.open();
				} else {
					ScriptService.getInstance().deleteParameterScript(
							nameText.getText());
				}
				refreshTables();
			}
		});
		refreshTables();
		return composite;
	}

	protected void loadControls(String scriptName) {
		ParameterScript currentScript = ScriptService.getInstance()
				.getParameterScript(scriptName);
		nameText.setText(currentScript.getName());
		descriptionText.setText(currentScript.getDescription());
		isActiveButton.setSelection(currentScript.isActive());

		int connectorTypeSelection = 0;
		String[] connectorTypeItems = connectorTypeCombo.getItems();
		for (int i = 0; i < connectorTypeItems.length; i++) {
			if (connectorTypeItems[i].equals(currentScript.getConnectorType()
					.name())) {
				connectorTypeSelection = i;
				break;
			}
		}
		connectorTypeCombo.select(connectorTypeSelection);
		scriptText.setText(currentScript.getScript());
	}

	protected void refreshTables() {
		ParameterScript[] allScripts = ScriptService.getInstance()
				.getParameterScripts();

		List<ParameterScript> inactiveScripts = new ArrayList<ParameterScript>(
				10);
		List<ParameterScript> activeScripts = new ArrayList<ParameterScript>(10);

		for (ParameterScript script : allScripts) {
			if (script.getType() == Type.ConsoleRightClickScripts) {
				if (script.isActive()) {
					activeScripts.add(script);
				} else {
					inactiveScripts.add(script);
				}
			}
		}
		Collections.sort(activeScripts);
		Collections.sort(inactiveScripts);

		String[][] activeData = new String[activeScripts.size()][1];
		String[][] inactiveData = new String[inactiveScripts.size()][1];

		for (int i = 0; i < activeData.length; i++) {
			activeData[i][0] = activeScripts.get(i).getName();
		}
		for (int i = 0; i < inactiveData.length; i++) {
			inactiveData[i][0] = inactiveScripts.get(i).getName();
		}

		icsActiveScriptsTable.refreshTable(activeData);
		icsInactiveScriptsTable.refreshTable(inactiveData);
	}
}
