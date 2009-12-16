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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.action.ActionUtils;
import raptor.action.RaptorAction;
import raptor.service.ActionScriptService;
import raptor.swt.RaptorTable;
import raptor.swt.SWTUtils;

public class ActionKeyBindingsPage extends PreferencePage {
	protected Composite composite;
	protected RaptorTable actionsTable;
	protected Label nameText;
	protected Label descriptionText;
	protected Text keyStrokeText;
	protected int modifierKeyCode = 0;
	protected boolean isModifierDown = false;

	public ActionKeyBindingsPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Action Key Bindings");
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			refreshActions();
		}
		super.setVisible(visible);
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(SWTUtils.createMarginlessGridLayout(3, false));

		Label textLabel = new Label(composite, SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		textLabel
				.setText(WordUtils
						.wrap(
								"\tOn this page you can bind actions provided by "
										+ "Raptor or created in the Action Scripts page to keystrokes.",
								70));

		actionsTable = new RaptorTable(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		actionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 3, 1));
		actionsTable.addColumn("Key Stroke", SWT.LEFT, 20, true, null);
		actionsTable.addColumn("Action Name", SWT.LEFT, 20, true, null);
		actionsTable.addColumn("Category", SWT.LEFT, 20, true, null);
		actionsTable.addColumn("Description", SWT.LEFT, 40, true, null);

		actionsTable.getTable().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = actionsTable.getTable().getSelectionIndex();
				String selection = actionsTable.getTable().getItem(
						selectedIndex).getText(1);
				loadControls(selection);
			}
		});

		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name:");
		nameLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameText = new Label(nameComposite, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Composite descriptionComposite = new Composite(composite, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description: ");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Label(descriptionComposite, SWT.BORDER
				| SWT.SINGLE);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite keyStrokeComposite = new Composite(composite, SWT.NONE);
		keyStrokeComposite.setLayout(new GridLayout(2, false));
		keyStrokeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label keystrokeLabel = new Label(descriptionComposite, SWT.NONE);
		keystrokeLabel.setText("Keystroke: ");
		keystrokeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		keyStrokeText = new Text(descriptionComposite, SWT.BORDER | SWT.SINGLE);
		keyStrokeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		keyStrokeText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				e.doit = false;
				if (ActionUtils.isValidModifier(e.keyCode)) {
					keyStrokeText.setText(ActionUtils
							.getStringFromModifier(e.keyCode)
							+ " ");
					modifierKeyCode = e.keyCode;
					isModifierDown = true;
				} else if (!isModifierDown) {
					if (ActionUtils.isValidKeyCodeWithoutModifier(e.keyCode)) {
						keyStrokeText.setText(ActionUtils
								.getNonModifierStringFromKeyCode(e.keyCode));
					} else {
						keyStrokeText.setText("");
					}
				} else if (ActionUtils.VALID_ACTION_KEY_CODES
						.indexOf((char) e.keyCode) != -1) {
					keyStrokeText.setText(ActionUtils
							.getStringFromModifier(modifierKeyCode)
							+ " " + (char) e.keyCode);
				} else {
					keyStrokeText.setText(ActionUtils
							.getStringFromModifier(modifierKeyCode)
							+ " ");
				}
			}

			public void keyReleased(KeyEvent e) {
				e.doit = false;
				isModifierDown = false;
				modifierKeyCode = 0;
			}
		});

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				true, false, 3, 1));
		buttonsComposite.setLayout(new RowLayout());
		Button saveButton = new Button(buttonsComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});

		// actionsTable.sort(1);
		refreshActions();
		actionsTable.getTable().setSelection(0);
		loadControls(actionsTable.getTable().getItem(
				actionsTable.getTable().getSelectionIndex()).getText(1));
		actionsTable.setSize(actionsTable.computeSize(SWT.DEFAULT, 200));
		return composite;
	}

	protected void loadControls(String actionName) {
		RaptorAction currentAction = ActionScriptService.getInstance()
				.getAction(actionName);
		if (currentAction != null) {
			nameText.setText(currentAction.getName());
			descriptionText.setText(currentAction.getDescription());
			keyStrokeText
					.setText(ActionUtils.keyBindingToString(currentAction));
		}
	}

	protected void onSave() {
		AbstractRaptorAction action = (AbstractRaptorAction) ActionScriptService
				.getInstance().getAction(nameText.getText());
		if (action != null) {
			if (StringUtils.isNotBlank(keyStrokeText.getText())) {
				action
						.setKeyCode(ActionUtils
								.keyBindingDescriptionToKeyCode(keyStrokeText
										.getText()));
				action.setModifierKey(ActionUtils
						.keyBindingDescriptionToKeyModifier(keyStrokeText
								.getText()));
			} else {
				action.setKeyCode(0);
				action.setModifierKey(0);
			}
		}
		ActionScriptService.getInstance().saveAction(action);
		refreshActions();
		for (int i = 0; i < actionsTable.getTable().getItemCount(); i++) {
			if (actionsTable.getTable().getItem(i).getText(1).equals(
					action.getName())) {
				actionsTable.getTable().select(i);
				break;
			}
		}
	}

	@Override
	protected void performApply() {
		onSave();
		super.performApply();
	}

	protected void refreshActions() {
		RaptorAction[] scripts = ActionScriptService.getInstance()
				.getAllActions();
		String[][] data = new String[scripts.length][];
		for (int i = 0; i < data.length; i++) {
			RaptorAction action = scripts[i];
			data[i] = new String[] { ActionUtils.keyBindingToString(action),
					action.getName(), action.getCategory().toString(),
					action.getDescription() };
		}
		actionsTable.refreshTable(data);
	}
}
