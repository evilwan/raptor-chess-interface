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

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
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
import raptor.script.ChatScript;
import raptor.script.ScriptConnectorType;
import raptor.script.ChatScript.ChatScriptType;
import raptor.service.ScriptService;
import raptor.swt.RaptorTable;

public class MessageEventScripts extends PreferencePage {

	protected RaptorTable activeScriptsTable;
	protected RaptorTable inactiveScriptsTable;

	protected Composite composite;

	protected Text nameText;
	protected Text descriptionText;
	protected Button isActiveButton;
	protected StyledText scriptText;
	protected Combo typeCombo;
	protected Combo connectorTypeCombo;

	protected Button saveButton;
	protected Button deleteButton;

	public MessageEventScripts() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Message Event Scripts");
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label textLabel = new Label(composite, SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\tMessage Event Scripts have access to the methods to the following methods in the context: "
										+ "getMessage() (returns the entire message), getMessageSource (returns the person sending "
										+ "the channel/personal tell), and getMessageChannel (returns the channel if the tell was a "
										+ "channel tell). To activate/inactiveate a script click on it then check the active checkbox "
										+ "and then save."
										+ "See the Scripting wiki on the raptor site "
										+ "http://code.google.com/p/raptor-chess-interface/wiki/Scripting for more details.",
								70));

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayout(new GridLayout(2, false));
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));

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
				true, false, 2, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Text(descriptionComposite, SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite controlsComposite = new Composite(composite, SWT.NONE);
		controlsComposite.setLayout(new GridLayout(7, false));
		controlsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 2, 1));

		Label typeLabel = new Label(controlsComposite, SWT.NONE);
		typeLabel.setText("Type:");
		typeCombo = new Combo(controlsComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		typeCombo.add(ChatScriptType.OnPersonTellMessages.name());
		typeCombo.add(ChatScriptType.OnPartnerTellMessages.name());
		typeCombo.add(ChatScriptType.onChannelTellMessages.name());
		typeCombo.select(0);

		Label connectorTypeLabel = new Label(controlsComposite, SWT.NONE);
		connectorTypeLabel.setText("Connector:");

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
				false, 2, 1));
		scriptText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		scriptText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		scriptText.setText("\n\n\n\n\n\n");
		scriptText.setWordWrap(false);

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false, 2, 1));
		buttonComposite.setLayout(new RowLayout());
		saveButton = new Button(buttonComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChatScript newScript = ScriptService.getInstance()
						.getChatScript(nameText.getText());
				if (newScript == null) {
					newScript = new ChatScript();
				}
				newScript.setActive(isActiveButton.getSelection());
				newScript.setName(nameText.getText());
				newScript.setDescription(descriptionText.getText());
				newScript.setScript(scriptText.getText().trim());
				newScript.setScriptConnectorType(ScriptConnectorType
						.valueOf(connectorTypeCombo.getItem(connectorTypeCombo
								.getSelectionIndex())));
				newScript.setChatScriptType(ChatScriptType.valueOf(typeCombo
						.getItem(typeCombo.getSelectionIndex())));
				ScriptService.getInstance().saveChatScript(newScript);
				refreshTables();
			}
		});

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScriptService.getInstance()
						.deleteChatScript(nameText.getText());
				refreshTables();
			}
		});
		activeScriptsTable.sort(0);
		inactiveScriptsTable.sort(0);
		refreshTables();
		tableComposite.setSize(tableComposite.computeSize(SWT.DEFAULT, 200));
		composite.pack();
		return composite;
	}

	protected void loadControls(String scriptName) {
		ChatScript currentScript = ScriptService.getInstance().getChatScript(
				scriptName);
		nameText.setText(currentScript.getName());
		descriptionText.setText(currentScript.getDescription());
		isActiveButton.setSelection(currentScript.isActive());

		int scriptType = 0;
		String[] typeItems = typeCombo.getItems();
		for (int i = 0; i < typeItems.length; i++) {
			if (typeItems[i].equals(currentScript.getChatScriptType().name())) {
				scriptType = i;
				break;
			}
		}
		typeCombo.select(scriptType);

		int connectorTypeSelection = 0;
		String[] connectorTypeItems = connectorTypeCombo.getItems();
		for (int i = 0; i < connectorTypeItems.length; i++) {
			if (connectorTypeItems[i].equals(currentScript
					.getScriptConnectorType().name())) {
				connectorTypeSelection = i;
				break;
			}
		}
		connectorTypeCombo.select(connectorTypeSelection);

		scriptText.setText(currentScript.getScript());
	}

	protected void refreshTables() {

		ChatScript[] allScripts = ScriptService.getInstance()
				.getAllChatScripts();

		List<String[]> activeScripts = new ArrayList<String[]>(10);
		List<String[]> inactiveScripts = new ArrayList<String[]>(10);
		for (ChatScript script : allScripts) {
			if (script.getChatScriptType() == ChatScriptType.onChannelTellMessages
					|| script.getChatScriptType() == ChatScriptType.OnPartnerTellMessages
					|| script.getChatScriptType() == ChatScriptType.OnPersonTellMessages) {
				if (script.isActive()) {
					activeScripts.add(new String[] { script.getName() });
				} else {
					inactiveScripts.add(new String[] { script.getName() });
				}
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
