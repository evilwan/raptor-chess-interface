/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
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
import raptor.action.RaptorAction;
import raptor.action.ScriptedAction;
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.international.L10n;
import raptor.service.ActionScriptService;
import raptor.swt.RaptorTable;
import raptor.swt.SWTUtils;
import raptor.swt.ScriptEditorDialog;
import raptor.util.RaptorUtils;

public class ActionScriptsPage extends PreferencePage {
	protected RaptorActionContainer container;
	protected Combo icons;
	protected Combo categories;
	protected RaptorTable scriptedActionsTable;
	protected Composite iconComposite;
	protected Composite composite;
	protected Label icon;
	protected Text nameText;
	protected Text descriptionText;
	protected CLabel scriptText;
	protected boolean wasLastSortAscending;
	
	protected static L10n local = L10n.getInstance();

	public ActionScriptsPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle(local.getString("actScr"));
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
						.wrap(local.getString("actScrPDesc1"), 70)
						+ "\n"
						+ local.getString("actScrPDesc2")
						+ WordUtils
								.wrap(local.getString("actScrPDesc3"),
										70));

		scriptedActionsTable = new RaptorTable(composite, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		scriptedActionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 3, 1));
		scriptedActionsTable.addColumn(local.getString("scrName"), SWT.LEFT, 30, true, null);
		scriptedActionsTable.addColumn(local.getString("scrCat"), SWT.LEFT, 20, true,
				null);
		scriptedActionsTable.addColumn(local.getString("description"), SWT.LEFT, 50, true, null);

		scriptedActionsTable.getTable().addSelectionListener(
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						int selectedIndex = scriptedActionsTable.getTable()
								.getSelectionIndex();
						String selection = scriptedActionsTable.getTable()
								.getItem(selectedIndex).getText(0);
						loadControls(selection);
					}
				});

		Composite nameComposite = new Composite(composite, SWT.NONE);
		nameComposite.setLayout(new GridLayout(2, false));
		nameComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setText(local.getString("name"));
		nameLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		nameText = new Text(nameComposite, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		iconComposite = new Composite(composite, SWT.NONE);
		iconComposite.setLayout(new GridLayout(5, false));
		iconComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 3, 1));
		Label iconLabel = new Label(iconComposite, SWT.NONE);
		iconLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		iconLabel.setText("Icon: ");
		icons = new Combo(iconComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		icons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		icons.add("<None>");
		icons.setItems(RaptorUtils.getAllIconNames());
		icons.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedText = icons.getItem(icons.getSelectionIndex());
				if (selectedText.equals("<None>")) {
					icon.setImage(null);
				} else {
					icon.setImage(Raptor.getInstance().getIcon(selectedText));
				}
				icon.redraw();
				iconComposite.layout(true);
			}
		});
		icons.select(0);
		icon = new Label(iconComposite, SWT.NONE);
		icon.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		Label categoryLabel = new Label(iconComposite, SWT.NONE);
		categoryLabel.setText("  "+local.getString("category"));
		categoryLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		categories = new Combo(iconComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		categories.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));
		for (Category category : Category.values()) {
			categories.add(category.toString());
		}
		categories.select(0);

		Composite descriptionComposite = new Composite(composite, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout(2, false));
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 3, 1));
		Label descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText(local.getString("description"));
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Text(descriptionComposite, SWT.BORDER
				| SWT.SINGLE);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Label scriptLabel = new Label(composite, SWT.NONE);
		scriptLabel.setText(local.getString("script"));
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		scriptText = new CLabel(composite, SWT.LEFT);
		scriptText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		scriptText.setText(" \n \n \n \n \n");
		Button scriptEdit = new Button(composite, SWT.NONE);
		scriptEdit.setText(local.getString("edit"));
		scriptEdit.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		scriptEdit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScriptEditorDialog dialog = new ScriptEditorDialog(getShell(),
						local.getString("edScr") + nameText.getText());
				dialog.setInput(scriptText.getText());
				String result = dialog.open();
				if (StringUtils.isNotBlank(result)) {
					scriptText.setText(result.trim());
				}
			}
		});

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				true, false, 3, 1));
		buttonsComposite.setLayout(new RowLayout());
		Button saveButton = new Button(buttonsComposite, SWT.PUSH);
		saveButton.setText(local.getString("save"));
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onSave();
			}
		});

		Button deleteButton = new Button(buttonsComposite, SWT.PUSH);
		deleteButton.setText(local.getString("delete"));
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorAction action = ActionScriptService.getInstance()
						.getAction(nameText.getText());
				if (action != null && action.isSystemAction()) {
					Raptor.getInstance().alert(
							local.getString("actScrPAlert1"));
				} else if (action == null) {
					Raptor.getInstance().alert(
							local.getString("actScrPAlert2")
									+ nameText.getText());
				} else {
					ActionScriptService.getInstance().deleteAction(
							nameText.getText());
					refreshActions();
				}
			}
		});

		// scriptedActionsTable.sort(0);
		refreshActions();
		scriptedActionsTable.setSize(scriptedActionsTable.computeSize(
				SWT.DEFAULT, 200));
		scriptText.setSize(scriptedActionsTable.computeSize(SWT.DEFAULT, 100));
		return composite;
	}

	protected void loadControls(String actionName) {
		RaptorAction currentAction = ActionScriptService.getInstance()
				.getAction(actionName);

		nameText.setText(currentAction.getName());

		descriptionText.setText(currentAction.getDescription());

		if (currentAction instanceof ScriptedAction) {
			scriptText.setText(((ScriptedAction) currentAction).getScript());
		} else {
			scriptText.setText(local.getString("actScrPAlert3"));
		}

		if (currentAction.getIcon() == null) {
			icon.setImage(null);
			icons.select(0);
		} else {
			icon
					.setImage(Raptor.getInstance().getIcon(
							currentAction.getIcon()));
			int index = 0;
			String[] items = icons.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(currentAction.getIcon())) {
					index = i;
					break;
				}
			}
			icons.select(index);
		}
		iconComposite.layout(true);

		int categoryIndex = 0;
		String[] categoryItems = categories.getItems();
		for (int i = 0; i < categoryItems.length; i++) {
			if (categoryItems[i].equals(currentAction.getCategory().toString())) {
				categoryIndex = i;
				break;
			}
		}
		categories.select(categoryIndex);
	}

	protected void onSave() {
		ScriptedAction scriptedAction = new ScriptedAction();
		scriptedAction.setName(nameText.getText());
		scriptedAction.setDescription(descriptionText.getText());
		scriptedAction.setScript(scriptText.getText());
		scriptedAction.setCategory(Category.valueOf(categories
				.getItem(categories.getSelectionIndex())));

		if (icons.getSelectionIndex() == 0) {
			scriptedAction.setIcon(null);
		} else {
			scriptedAction.setIcon(icons.getItem(icons.getSelectionIndex()));
		}

		if (StringUtils.isBlank(scriptedAction.getName())) {
			Raptor.getInstance().alert(local.getString("actScrPAlert4"));
		} else if (StringUtils.isBlank(scriptedAction.getDescription())) {
			Raptor.getInstance().alert(local.getString("actScrPAlert5"));
		} else if (StringUtils.isBlank(scriptedAction.getScript())) {
			Raptor.getInstance().alert(local.getString("actScrPAlert6"));
		} else {
			ActionScriptService.getInstance().saveAction(scriptedAction);
			refreshActions();
			scriptedActionsTable.getTable().deselectAll();

			for (int i = 0; i < scriptedActionsTable.getTable().getItemCount(); i++) {
				if (scriptedActionsTable.getTable().getItem(i).getText(0)
						.equals(scriptedAction.getName())) {
					scriptedActionsTable.getTable().select(i);
					break;
				}
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
				.getAllScriptedActions();
		String[][] data = new String[scripts.length][];
		for (int i = 0; i < data.length; i++) {
			RaptorAction action = scripts[i];
			data[i] = new String[] { action.getName(),
					action.getCategory().toString(), action.getDescription() };
		}
		scriptedActionsTable.refreshTable(data);
	}
}
