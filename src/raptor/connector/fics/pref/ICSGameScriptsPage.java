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
package raptor.connector.fics.pref;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
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
import raptor.connector.Connector;
import raptor.script.GameScript;

public class ICSGameScriptsPage extends PreferencePage {
	Button delete;
	Text description;
	Connector ficsConnector;
	Button isAvailableInExamineState;
	Button isAvailableInFreeFormState;
	Button isAvailableInObserveState;
	Button isAvailableInPlayingState;
	Button isAvailableInSetupState;
	Text name;
	Composite parent;
	Button save;
	Text script;
	Combo scriptName;
	Composite scriptNameComposite;
	Button test;

	public ICSGameScriptsPage(Connector connector) {
		// Use the "flat" layout
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Ics Script Editor (Comming Soon)");
		ficsConnector = connector;
	}

	@Override
	protected Control createContents(Composite arg0) {
		parent = new Composite(arg0, SWT.NONE);
		parent.setLayout(new GridLayout(1, false));

		scriptNameComposite = new Composite(parent, SWT.NONE);
		scriptNameComposite.setLayout(new RowLayout());
		scriptNameComposite
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label scriptNameLabel = new Label(scriptNameComposite, SWT.NONE);
		scriptNameLabel.setText("Script:");
		scriptName = new Combo(scriptNameComposite, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		scriptName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GameScript gameScript = ficsConnector.getGameScript(scriptName
						.getItem(scriptName.getSelectionIndex()));
				description.setText(gameScript.getDescription());
				script.setText(gameScript.getScript());
				name.setText(gameScript.getName());
				isAvailableInExamineState.setSelection(gameScript
						.isAvailableInExamineState());
				isAvailableInPlayingState.setSelection(gameScript
						.isAvailableInPlayingState());
				isAvailableInObserveState.setSelection(gameScript
						.isAvailableInObserveState());
				isAvailableInSetupState.setSelection(gameScript
						.isAvailableInSetupState());
				isAvailableInFreeFormState.setSelection(gameScript
						.isAvailableInFreeformState());
			}
		});

		Composite nameComposite = new Composite(parent, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name:");
		name = new Text(nameComposite, SWT.BORDER | SWT.SINGLE);
		name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite descriptionComposite = new Composite(parent, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:");
		description = new Text(descriptionComposite, SWT.BORDER);
		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		isAvailableInExamineState = new Button(parent, SWT.CHECK);
		isAvailableInExamineState.setText("Display in Examine Mode");
		isAvailableInPlayingState = new Button(parent, SWT.CHECK);
		isAvailableInPlayingState.setText("Display in Playing Mode");
		isAvailableInObserveState = new Button(parent, SWT.CHECK);
		isAvailableInObserveState.setText("Display in Observing Mode");
		isAvailableInSetupState = new Button(parent, SWT.CHECK);
		isAvailableInSetupState.setText("Display in Setup Mode");
		isAvailableInFreeFormState = new Button(parent, SWT.CHECK);
		isAvailableInFreeFormState.setText("Display in Free Form Mode");

		Label scriptLabel = new Label(parent, SWT.NONE | SWT.SINGLE);
		scriptLabel.setText("Script:");
		script = new Text(parent, SWT.BORDER | SWT.MULTI);
		script.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new RowLayout());
		save = new Button(buttonComposite, SWT.PUSH);
		save.setText("Save");
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String nameString = name.getText();
				GameScript gameScript = ficsConnector.getGameScript(nameString);
				if (gameScript != null) {
					gameScript.setDescription(description.getText());
					gameScript.setName(name.getText());
					gameScript.setScript(script.getText());
					gameScript
							.setAvailableInExamineState(isAvailableInExamineState
									.getSelection());
					gameScript
							.setAvailableInFreeformState(isAvailableInFreeFormState
									.getSelection());
					gameScript
							.setAvailableInObserveState(isAvailableInObserveState
									.getSelection());
					gameScript
							.setAvailableInPlayingState(isAvailableInPlayingState
									.getSelection());
					gameScript.setAvailableInSetupState(isAvailableInSetupState
							.getSelection());
					gameScript.save();
					ficsConnector.refreshGameScripts();
					populateScriptNames();

				} else {
					gameScript = new GameScript();
					gameScript.setConnector(ficsConnector);
					gameScript.setDescription(description.getText());
					gameScript.setName(name.getText());
					gameScript.setScript(script.getText());
					gameScript
							.setAvailableInExamineState(isAvailableInExamineState
									.getSelection());
					gameScript
							.setAvailableInFreeformState(isAvailableInFreeFormState
									.getSelection());
					gameScript
							.setAvailableInObserveState(isAvailableInObserveState
									.getSelection());
					gameScript
							.setAvailableInPlayingState(isAvailableInPlayingState
									.getSelection());
					gameScript.setAvailableInSetupState(isAvailableInSetupState
							.getSelection());
					gameScript.save();
					ficsConnector.refreshGameScripts();
					populateScriptNames();
				}
			}
		});
		delete = new Button(buttonComposite, SWT.PUSH);
		delete.setText("Delete");
		delete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String nameString = name.getText();
				GameScript gameScript = ficsConnector.getGameScript(nameString);
				if (gameScript != null) {
					ficsConnector.removeGameScript(gameScript);
					ficsConnector.refreshGameScripts();
					populateScriptNames();

				}
			}
		});

		populateScriptNames();
		return parent;
	}

	public void populateScriptNames() {
		scriptName.removeAll();
		GameScript[] scripts = ficsConnector.getGameScripts();
		for (GameScript script2 : scripts) {
			scriptName.add(script2.getName());
		}
		scriptNameComposite.pack();
		scriptNameComposite.layout();
	}

}
