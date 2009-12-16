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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.RaptorActionFactory;
import raptor.action.ScriptedAction;
import raptor.action.SeparatorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.service.ActionScriptService;
import raptor.swt.RaptorTable;
import raptor.swt.ScriptEditorDialog;

public class ActionContainerPage extends PreferencePage {

	protected RaptorActionContainer container;
	protected Composite iconComposite;
	protected RaptorTable currentActionsTable;
	protected RaptorTable availableActionsTable;
	protected Composite composite;
	protected Label icon;
	protected Label nameText;
	protected Label descriptionText;
	protected Label categoryText;
	protected CLabel scriptText;
	protected String description;
	protected String title;

	public ActionContainerPage(String title, String description,
			RaptorActionContainer actionContainer) {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle(title);
		this.title = title;
		this.description = description;
		container = actionContainer;
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			refreshAvailableActions();
		}
		super.setVisible(visible);
	}

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		if (StringUtils.isNotBlank(description)) {
			Label textLabel = new Label(composite, SWT.WRAP);
			textLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false, 3, 1));
			textLabel.setText(WordUtils.wrap(description, 70));
		}

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 3, 1));
		tableComposite.setLayout(new GridLayout(3, false));

		currentActionsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		currentActionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, true));
		currentActionsTable.setFixedWidth(175);
		currentActionsTable.addColumn(title + " Actions", SWT.LEFT, 100, false,
				null);
		currentActionsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = currentActionsTable.getTable()
								.getSelectionIndex();
						String selection = currentActionsTable.getTable()
								.getItem(selectedIndex).getText();
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
				int selectedIndex = availableActionsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex != -1) {
					String actionName = availableActionsTable.getTable()
							.getItem(selectedIndex).getText(0);
					TableItem[] currentItems = currentActionsTable.getTable()
							.getItems();
					boolean isInCurrent = false;
					for (TableItem item : currentItems) {
						if (item.getText().equals(actionName)) {
							isInCurrent = true;
							break;
						}
					}

					if (!isInCurrent) {
						RaptorAction action = ActionScriptService.getInstance()
								.getAction(actionName);
						action.addContainer(container, currentActionsTable
								.getTable().getItemCount());
						ActionScriptService.getInstance().saveAction(action);
						refreshCurrentActions();
					}
				}
			}
		});

		Button removeButton = new Button(addRemoveComposite, SWT.PUSH);
		removeButton.setImage(Raptor.getInstance().getIcon("next"));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = currentActionsTable.getTable()
						.getSelectionIndex();
				if (selectedIndex != -1) {
					String actionName = currentActionsTable.getTable().getItem(
							selectedIndex).getText(0);
					RaptorAction action = ActionScriptService.getInstance()
							.getAction(actionName);
					action.removeContainer(container);
					ActionScriptService.getInstance().saveAction(action);
					refreshCurrentActions();
				}
			}
		});

		availableActionsTable = new RaptorTable(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		availableActionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		availableActionsTable.addColumn("All Actions Name", SWT.LEFT, 50, true,
				null);
		availableActionsTable.addColumn("All Actions Catagory", SWT.LEFT, 50,
				true, null);
		availableActionsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = availableActionsTable.getTable()
								.getSelectionIndex();
						String selection = availableActionsTable.getTable()
								.getItem(selectedIndex).getText(0);
						loadControls(selection);
					}
				});

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false, 2, 1));
		buttonsComposite.setLayout(new RowLayout());
		Button upButton = new Button(buttonsComposite, SWT.PUSH);
		upButton.setImage(Raptor.getInstance().getIcon("up"));
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedScript = currentActionsTable.getTable()
						.getSelectionIndex();
				if (selectedScript > 0) {
					TableItem[] items = currentActionsTable.getTable()
							.getItems();
					for (int i = 0; i < items.length; i++) {
						RaptorAction action = ActionScriptService.getInstance()
								.getAction(items[i].getText());
						if (i == selectedScript - 1) {
							action.setContainerOrder(container, i + 1);
						} else if (i == selectedScript) {
							action.setContainerOrder(container, i - 1);
						} else {
							action.setContainerOrder(container, i);
						}
						ActionScriptService.getInstance().saveAction(action);
					}
					String selectedText = items[selectedScript].getText();
					items[selectedScript].setText(items[selectedScript - 1]
							.getText());
					items[selectedScript - 1].setText(selectedText);
					currentActionsTable.getTable().setSelection(
							selectedScript - 1);
					currentActionsTable.redraw();
				}
			}
		});
		Button downButton = new Button(buttonsComposite, SWT.PUSH);
		downButton.setImage(Raptor.getInstance().getIcon("down"));
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedScript = currentActionsTable.getTable()
						.getSelectionIndex();
				if (selectedScript < currentActionsTable.getTable()
						.getItemCount() - 1) {
					TableItem[] items = currentActionsTable.getTable()
							.getItems();
					for (int i = 0; i < items.length; i++) {
						RaptorAction action = ActionScriptService.getInstance()
								.getAction(items[i].getText());
						if (i == selectedScript + 1) {
							action.setContainerOrder(container, i - 1);
						} else if (i == selectedScript) {
							action.setContainerOrder(container, i + 1);
						} else {
							action.setContainerOrder(container, i);
						}
						ActionScriptService.getInstance().saveAction(action);
					}
					String selectedText = items[selectedScript].getText();
					items[selectedScript].setText(items[selectedScript + 1]
							.getText());
					items[selectedScript + 1].setText(selectedText);
					currentActionsTable.getTable().setSelection(
							selectedScript + 1);
					currentActionsTable.redraw();
				}
			}
		});
		Button separatorButton = new Button(buttonsComposite, SWT.PUSH);
		separatorButton.setText("Add Separator");
		separatorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SeparatorAction separator = RaptorActionFactory
						.createSeparator();
				separator.addContainer(container, currentActionsTable
						.getTable().getItemCount());
				ActionScriptService.getInstance().saveAction(separator);
				refreshCurrentActions();
			}
		});

		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText("Name: ");
		nameLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameText = new Label(nameComposite, SWT.NONE);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		iconComposite = new Composite(composite, SWT.NONE);
		iconComposite.setLayout(new GridLayout(2, false));
		iconComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label iconLabel = new Label(iconComposite, SWT.NONE);
		iconLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		iconLabel.setText("Icon: ");
		icon = new Label(iconComposite, SWT.NONE);
		icon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Composite categoryComposite = new Composite(composite, SWT.NONE);
		categoryComposite.setLayout(new GridLayout(2, false));
		categoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label categoryLabel = new Label(categoryComposite, SWT.NONE);
		categoryLabel.setText("Category");
		categoryLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		categoryText = new Label(categoryComposite, SWT.NONE);
		categoryText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite descriptionComposite = new Composite(composite, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description: ");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Label(descriptionComposite, SWT.BORDER);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Composite scriptComposite = new Composite(composite, SWT.NONE);
		scriptComposite.setLayout(new GridLayout(3, false));
		scriptComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label scriptLabel = new Label(scriptComposite, SWT.NONE);
		scriptLabel.setText("Script:");
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		scriptText = new CLabel(scriptComposite, SWT.NONE);
		scriptText
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		scriptText.setText("\n \n \n \n \n");
		Button editButton = new Button(scriptComposite, SWT.NONE);
		editButton.setText("Edit");
		editButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		editButton.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(nameText.getText())) {
					Raptor.getInstance().alert(
							"You must first select a script in a table.");
				} else {
					RaptorAction action = ActionScriptService.getInstance()
							.getAction(nameText.getText());
					if (action instanceof ScriptedAction) {
						ScriptedAction scriptedAction = (ScriptedAction) action;
						ScriptEditorDialog dialog = new ScriptEditorDialog(
								getShell(), "Edit script: "
										+ nameText.getText());
						dialog.setInput(scriptedAction.getScript());
						String result = dialog.open();
						if (StringUtils.isNotBlank(result)) {
							scriptText.setText(result.trim());
							scriptedAction.setScript(result);
							ActionScriptService.getInstance().saveAction(
									scriptedAction);
						}
					} else {
						Raptor.getInstance().alert(
								"This action is not a scripted action.");
					}
				}

			}

		});

		// availableActionsTable.sort(0);
		refreshCurrentActions();
		refreshAvailableActions();
		return composite;
	}

	protected void loadControls(String actionName) {
		RaptorAction currentAction = ActionScriptService.getInstance()
				.getAction(actionName);
		nameText.setText(currentAction.getName());
		descriptionText.setText(currentAction.getDescription());
		if (currentAction instanceof ScriptedAction) {
			scriptText.setText(((ScriptedAction) currentAction).getScript()
					.trim());
		} else {
			scriptText.setText("Not a script action");
		}
		categoryText.setText(currentAction.getCategory().toString());

		if (currentAction.getIcon() == null) {
			icon.setImage(null);
		} else {
			icon
					.setImage(Raptor.getInstance().getIcon(
							currentAction.getIcon()));
		}
		iconComposite.layout(true);
	}

	protected void refreshAvailableActions() {
		RaptorAction[] availableActions = ActionScriptService.getInstance()
				.getAllActions();
		String[][] data = new String[availableActions.length][2];

		for (int i = 0; i < availableActions.length; i++) {
			RaptorAction action = availableActions[i];
			data[i][0] = action.getName();
			data[i][1] = action.getCategory().toString();
		}
		availableActionsTable.refreshTable(data);
	}

	protected void refreshCurrentActions() {
		RaptorAction[] availableActions = ActionScriptService.getInstance()
				.getActions(container);
		String[][] data = new String[availableActions.length][1];

		for (int i = 0; i < availableActions.length; i++) {
			RaptorAction action = availableActions[i];
			data[i][0] = action.getName();
		}
		currentActionsTable.refreshTable(data);
	}
}
