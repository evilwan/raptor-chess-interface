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
package raptor.pref.page;

import java.util.ArrayList;
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
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.script.RegularExpressionScript;
import raptor.script.ScriptConnectorType;
import raptor.service.ScriptService;
import raptor.swt.RaptorTable;
import raptor.swt.RegularExpressionEditorDialog;
import raptor.swt.ScriptEditorDialog;

public class RegularExpressionScripts extends PreferencePage {

	protected RaptorTable activeScriptsTable;
	protected RaptorTable inactiveScriptsTable;

	protected Composite composite;

	protected Text nameText;
	protected Text descriptionText;
	protected CLabel regularExpressionText;
	protected Button regularExpressionEditor;
	protected Button isActiveButton;
	protected CLabel script;
	protected Button scriptEditor;
	protected Combo typeCombo;
	protected Combo connectorTypeCombo;

	protected Button saveButton;
	protected Button deleteButton;

	public RegularExpressionScripts() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Regular Expression Scripts");
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
								"\tRegular Expression scripts are executed when a message is "
										+ "received from the server that matches a given "
										+ "regular expression. Events received are called ChatEvents."
										+ "context.getChatEvent() returns the chatEvent which occured.",
								70)
						+ "\n\t context.getChatEvent().getMessage() Returns the entire contents of the message.\n"
						+ "\t context.getChatEvent().getSource() Returns the user sending the message if applicable.\n"
						+ "\t context.getChatEvent().getChannel() Returns the channel of the message if applicable.\n"
						+ "\t context.getChatEvent().getGameId() Returns the gameId of the message if applicable.\n"
						+ "See Help->Raptor Help->Raptor Scripting for more details.");

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));
		tableComposite.setLayout(new GridLayout(3, false));

		activeScriptsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		activeScriptsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		activeScriptsTable.addColumn("Active Script Name", SWT.LEFT, 100, true,
				null);
		activeScriptsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = activeScriptsTable.getTable()
								.getSelectionIndex();
						String selection = activeScriptsTable.getTable()
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
				int selectedIndex = inactiveScriptsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex == -1) {
					return;
				}
				String selection = inactiveScriptsTable.getTable().getItem(
						selectedIndex).getText(0);

				RegularExpressionScript script = ScriptService.getInstance()
						.getRegularExpressionScript(selection);
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
				int selectedIndex = activeScriptsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex == -1) {
					return;
				}
				String selection = activeScriptsTable.getTable().getItem(
						selectedIndex).getText(0);

				RegularExpressionScript script = ScriptService.getInstance()
						.getRegularExpressionScript(selection);
				script.setActive(false);
				ScriptService.getInstance().save(script);
				refreshTables();
			}
		});

		inactiveScriptsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		inactiveScriptsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		inactiveScriptsTable.addColumn("Inactive Script Name", SWT.LEFT, 100,
				true, null);
		inactiveScriptsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = inactiveScriptsTable.getTable()
								.getSelectionIndex();
						String selection = inactiveScriptsTable.getTable()
								.getItem(selectedIndex).getText(0);
						loadControls(selection);
					}
				});

		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(new GridLayout(3, false));
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name:");
		nameLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameText = new Text(nameComposite, SWT.BORDER);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		isActiveButton = new Button(nameComposite, SWT.CHECK);
		isActiveButton.setText("Active");
		isActiveButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		Composite descriptionComposite = new Composite(composite, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Text(descriptionComposite, SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setLayout(new GridLayout(2, false));
		controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label connectorTypeLabel = new Label(controlsComposite, SWT.NONE);
		connectorTypeLabel.setText("Connector Type:");
		connectorTypeCombo = new Combo(controlsComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		for (ScriptConnectorType scriptConnectorType : ScriptConnectorType
				.values()) {
			connectorTypeCombo.add(scriptConnectorType.name());
		}
		connectorTypeCombo.select(0);

		Composite regularExpressionComposite = new Composite(composite,
				SWT.NONE);
		regularExpressionComposite.setLayout(new GridLayout(3, false));
		regularExpressionComposite.setLayoutData(new GridData(SWT.FILL,
				SWT.CENTER, true, false, 3, 1));
		Label regExLabel = new Label(regularExpressionComposite, SWT.NONE);
		regExLabel.setText("Regular Expression:");
		regExLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		regularExpressionText = new CLabel(regularExpressionComposite, SWT.LEFT);
		regularExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		regularExpressionText.setText(" \n \n");
		regularExpressionEditor = new Button(regularExpressionComposite,
				SWT.PUSH);

		regularExpressionEditor.setText("Edit");
		regularExpressionEditor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RegularExpressionEditorDialog dialog = new RegularExpressionEditorDialog(
						getShell(), "Regular Expression Editor",
						"Edit regular expression for script: "
								+ nameText.getText());
				dialog.setInput(regularExpressionText.getText());
				String result = dialog.open();
				if (StringUtils.isNotBlank(result)) {
					regularExpressionText.setText(result.trim());
				}
			}
		});

		Composite scriptComposite = new Composite(composite, SWT.NONE);
		scriptComposite.setLayout(new GridLayout(3, false));
		scriptComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label scriptLabel = new Label(scriptComposite, SWT.NONE);
		scriptLabel.setText("Script:");
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		script = new CLabel(scriptComposite, SWT.LEFT);
		script.setText(" \n \n \n \n \n \n");
		script.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));
		scriptEditor = new Button(scriptComposite, SWT.PUSH);
		scriptEditor.setText("Edit");
		scriptEditor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScriptEditorDialog dialog = new ScriptEditorDialog(getShell(),
						"Edit script: " + nameText.getText());
				dialog.setInput(script.getText());
				String result = dialog.open();
				if (StringUtils.isNotBlank(result)) {
					script.setText(result.trim());
				}
			}
		});

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 2, 1));
		buttonComposite.setLayout(new RowLayout());
		saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isBlank(nameText.getText())) {
					Raptor.getInstance().alert("Name is required.");
					return;
				}
				ScriptService.getInstance().deleteRegularExpressionScript(
						nameText.getText());
				refreshTables();
			}
		});
		activeScriptsTable.sort(0);
		inactiveScriptsTable.sort(0);
		refreshTables();
		tableComposite.setSize(tableComposite.computeSize(SWT.DEFAULT, 200));
		return composite;
	}

	protected void loadControls(String scriptName) {
		RegularExpressionScript currentScript = ScriptService.getInstance()
				.getRegularExpressionScript(scriptName);
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

		script.setText(currentScript.getScript());
		regularExpressionText.setText(currentScript.getRegularExpression());
	}

	protected void onSave() {
		if (StringUtils.isBlank(nameText.getText())) {
			Raptor.getInstance().alert("Name is required.");
			return;
		}
		if (StringUtils.isBlank(descriptionText.getText())) {
			Raptor.getInstance().alert("Description is required.");
			return;
		}
		if (StringUtils.isBlank(regularExpressionText.getText())) {
			Raptor.getInstance().alert("Regular Expression is required.");
			return;
		}
		if (StringUtils.isBlank(script.getText())) {
			Raptor.getInstance().alert("Script is required.");
			return;
		}

		RegularExpressionScript newScript = ScriptService.getInstance()
				.getRegularExpressionScript(nameText.getText());
		if (newScript == null) {
			newScript = new RegularExpressionScript();
		}

		newScript.setActive(isActiveButton.getSelection());
		newScript.setName(nameText.getText());
		newScript.setDescription(descriptionText.getText());
		newScript.setRegularExpression(regularExpressionText.getText());
		newScript.setScript(script.getText());
		newScript.setConnectorType(ScriptConnectorType
				.valueOf(connectorTypeCombo.getItem(connectorTypeCombo
						.getSelectionIndex())));
		ScriptService.getInstance().save(newScript);
		refreshTables();
	}

	@Override
	protected void performApply() {
		onSave();
		super.performApply();
	}

	protected void refreshTables() {

		RegularExpressionScript[] allScripts = ScriptService.getInstance()
				.getRegularExpressionScripts();

		List<String[]> activeScripts = new ArrayList<String[]>(10);
		List<String[]> inactiveScripts = new ArrayList<String[]>(10);
		for (RegularExpressionScript script : allScripts) {
			if (script.isActive()) {
				activeScripts.add(new String[] { script.getName() });
			} else {
				inactiveScripts.add(new String[] { script.getName() });
			}
		}

		String[][] activeData = new String[activeScripts.size()][];
		for (int i = 0; i < activeData.length; i++) {
			activeData[i] = activeScripts.get(i);
		}

		String[][] inactiveData = new String[inactiveScripts.size()][];
		for (int i = 0; i < inactiveData.length; i++) {
			inactiveData[i] = inactiveScripts.get(i);
		}
		activeScriptsTable.refreshTable(activeData);
		inactiveScriptsTable.refreshTable(inactiveData);
	}
}
