package raptor.pref.page;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.ScriptedAction;
import raptor.action.RaptorAction.Category;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.service.ActionService;
import raptor.swt.SWTUtils;
import raptor.util.RaptorUtils;

public class ActionScriptsPage extends PreferencePage {
	protected RaptorActionContainer container;
	protected Combo icons;
	protected Combo categories;
	protected Table scriptedActionsTable;
	protected Composite iconComposite;
	protected Composite composite;
	protected Label icon;
	protected Text nameText;
	protected Text descriptionText;
	protected StyledText scriptText;
	protected TableColumn lastStortedColumn;
	protected TableColumn nameColumn;
	protected TableColumn descriptionColumn;
	protected TableColumn categoryColumn;
	protected boolean wasLastSortAscending;

	public ActionScriptsPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Action Scripts");
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
								"\tAction Scripts have no parameters and can only access the "
										+ "methods in the context. You can also bind actions to keystrokes "
										+ "on the Action Key Bindings preference page."
										+ "See the Scripting wiki on the raptor site "
										+ "http://code.google.com/p/raptor-chess-interface/wiki/Scripting "
										+ "for more details.", 70)
						+ "\n"
						+ WordUtils
								.wrap(
										"\tIf an icon is set to a value other than <None>, only the icon is displayed "
												+ "for the action. If an icon is set to the value <None>, then the name is "
												+ "displayed for the action. You may add additional icons to the "
												+ "resources/icons directory.",
										70));

		Composite tableComposite = new Composite(composite, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 3, 1));
		scriptedActionsTable = new Table(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		scriptedActionsTable.setHeaderVisible(true);

		nameColumn = new TableColumn(scriptedActionsTable, SWT.LEFT);
		categoryColumn = new TableColumn(scriptedActionsTable, SWT.LEFT);
		descriptionColumn = new TableColumn(scriptedActionsTable, SWT.LEFT);
		nameColumn.setText("Script Name");
		nameColumn.setWidth(150);
		categoryColumn.setText("Script Category");
		categoryColumn.setWidth(100);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(200);

		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == nameColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = nameColumn;
				refreshActions();
			}
		});

		categoryColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == categoryColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = categoryColumn;
				refreshActions();
			}
		});

		descriptionColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == descriptionColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = descriptionColumn;
				refreshActions();
			}
		});

		scriptedActionsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = scriptedActionsTable.getSelectionIndex();
				String selection = scriptedActionsTable.getItem(selectedIndex)
						.getText(0);
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
		categoryLabel.setText("  Category:");
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
		descriptionLabel.setText("Description: ");
		descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		descriptionText = new Text(descriptionComposite, SWT.BORDER
				| SWT.SINGLE);
		descriptionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		Label scriptLabel = new Label(composite, SWT.NONE);
		scriptLabel.setText("Script:");
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 3, 1));
		scriptText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		scriptText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				3, 1));
		scriptText.setText("\n\n\n\n\n\n");
		scriptText.setEditable(true);
		scriptText.setWordWrap(false);

		Composite buttonsComposite = new Composite(composite, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER,
				true, false, 3, 1));
		buttonsComposite.setLayout(new RowLayout());
		Button saveButton = new Button(buttonsComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScriptedAction scriptedAction = new ScriptedAction();
				scriptedAction.setName(nameText.getText());
				scriptedAction.setDescription(descriptionText.getText());
				scriptedAction.setScript(scriptText.getText());
				scriptedAction.setCategory(Category.valueOf(categories
						.getItem(categories.getSelectionIndex())));

				if (icons.getSelectionIndex() == 0) {
					scriptedAction.setIcon(null);
				} else {
					scriptedAction.setIcon(icons.getItem(icons
							.getSelectionIndex()));
				}

				if (StringUtils.isBlank(scriptedAction.getName())) {
					Raptor.getInstance().alert("Name can not be empty.");
				} else if (StringUtils.isBlank(scriptedAction.getDescription())) {
					Raptor.getInstance().alert("Description can not be empty.");
				} else if (StringUtils.isBlank(scriptedAction.getScript())) {
					Raptor.getInstance().alert("Script can not be empty");
				} else {
					ActionService.getInstance().saveAction(scriptedAction);

					int indexBeforeSave = scriptedActionsTable
							.getSelectionIndex();
					refreshActions();
					if (indexBeforeSave != -1) {
						scriptedActionsTable.setSelection(indexBeforeSave);
						loadControls(scriptedActionsTable.getItem(
								scriptedActionsTable.getSelectionIndex())
								.getText(0));
					}
				}
			}
		});

		Button deleteButton = new Button(buttonsComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorAction action = ActionService.getInstance().getAction(
						nameText.getText());
				if (action != null && action.isSystemAction()) {
					Raptor.getInstance().alert(
							"You can not delete a System action.");
				} else if (action == null) {
					Raptor.getInstance().alert(
							"Could not find an action named: "
									+ nameText.getText());
				} else {
					ActionService.getInstance()
							.deleteAction(nameText.getText());
					refreshActions();
				}
			}
		});

		refreshActions();
		scriptedActionsTable.setSelection(0);
		loadControls(scriptedActionsTable.getItem(
				scriptedActionsTable.getSelectionIndex()).getText(0));
		scriptedActionsTable.setSize(scriptedActionsTable.computeSize(
				SWT.DEFAULT, 300, true));
		return composite;
	}

	protected Comparator<RaptorAction> getCompatator() {
		if (lastStortedColumn == null) {
			lastStortedColumn = nameColumn;
			wasLastSortAscending = true;
			return RaptorAction.NAME_COMPARATOR_ASCENDING;
		} else if (lastStortedColumn == nameColumn) {
			return wasLastSortAscending ? RaptorAction.NAME_COMPARATOR_ASCENDING
					: RaptorAction.NAME_COMPARATOR_DESCENDING;
		} else if (lastStortedColumn == descriptionColumn) {
			return wasLastSortAscending ? RaptorAction.DESCRIPTION_COMPARATOR_ASCENDING
					: RaptorAction.DESCRIPTION_COMPARATOR_DESCENDING;
		} else {
			return wasLastSortAscending ? RaptorAction.CATEGORY_COMPARATOR_ASCENDING
					: RaptorAction.CATEGORY_COMPARATOR_DESCENDING;
		}
	}

	protected void loadControls(String actionName) {
		RaptorAction currentAction = ActionService.getInstance().getAction(
				actionName);

		nameText.setText(currentAction.getName());

		descriptionText.setText(currentAction.getDescription());

		if (currentAction instanceof ScriptedAction) {
			scriptText.setText(((ScriptedAction) currentAction).getScript());
		} else {
			scriptText.setText("Not a script action");
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

	protected void refreshActions() {
		TableItem[] currentItems = scriptedActionsTable.getItems();
		for (TableItem item : currentItems) {
			item.dispose();
		}

		RaptorAction[] scripts = ActionService.getInstance()
				.getAllScriptedActions();
		Arrays.sort(scripts, getCompatator());

		for (RaptorAction action : scripts) {
			TableItem item = new TableItem(scriptedActionsTable, SWT.NONE);
			item.setText(new String[] { action.getName(),
					action.getCategory().toString(), action.getDescription() });
		}
	}
}
