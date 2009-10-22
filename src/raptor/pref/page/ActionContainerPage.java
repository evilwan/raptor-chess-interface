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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.RaptorActionFactory;
import raptor.action.ScriptedAction;
import raptor.action.SeparatorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.service.ActionService;

public class ActionContainerPage extends PreferencePage {

	protected RaptorActionContainer container;
	protected Composite iconComposite;
	protected Table currentActionsTable;
	protected Table availableActionsTable;
	protected Composite composite;
	protected Label icon;
	protected Label nameText;
	protected Label descriptionText;
	protected Label categoryText;
	protected StyledText scriptText;
	protected String description;
	protected String title;
	protected TableColumn lastStortedColumn;
	protected TableColumn availableNameColumn;
	protected TableColumn availableCategoryColumn;
	protected boolean wasLastSortAscending;

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
		tableComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				false, false, 3, 1));

		currentActionsTable = new Table(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		currentActionsTable.setSize(currentActionsTable.computeSize(160, 250));
		currentActionsTable.setLocation(0, 0);

		TableColumn activeName = new TableColumn(currentActionsTable, SWT.LEFT);
		activeName.setText(title + " Actions");
		activeName.setWidth(150);
		currentActionsTable.setHeaderVisible(true);

		currentActionsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = currentActionsTable.getSelectionIndex();
				String selection = currentActionsTable.getItem(selectedIndex)
						.getText();
				loadControls(selection);
			}
		});

		Composite addRemoveComposite = new Composite(tableComposite, SWT.NONE);
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
				int selectedIndex = availableActionsTable.getSelectionIndex();
				if (selectedIndex != -1) {
					String actionName = availableActionsTable.getItem(
							selectedIndex).getText(0);
					TableItem[] currentItems = currentActionsTable.getItems();
					boolean isInCurrent = false;
					for (TableItem item : currentItems) {
						if (item.getText().equals(actionName)) {
							isInCurrent = true;
							break;
						}
					}

					if (!isInCurrent) {
						RaptorAction action = ActionService.getInstance()
								.getAction(actionName);
						action.addContainer(container, currentActionsTable
								.getItemCount());
						ActionService.getInstance().saveAction(action);
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
				int selectedIndex = currentActionsTable.getSelectionIndex();
				if (selectedIndex != -1) {
					String actionName = currentActionsTable.getItem(
							selectedIndex).getText(0);
					RaptorAction action = ActionService.getInstance()
							.getAction(actionName);
					action.removeContainer(container);
					ActionService.getInstance().saveAction(action);
					refreshCurrentActions();
				}
			}
		});
		addRemoveComposite.setSize(addRemoveComposite.computeSize(60, 260));
		addRemoveComposite.setLocation(currentActionsTable.getSize().x + 5, 0);

		availableActionsTable = new Table(tableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);

		availableActionsTable.setSize(availableActionsTable.computeSize(290,
				250));
		availableActionsTable.setLocation(currentActionsTable.getSize().x + 5
				+ addRemoveComposite.getSize().x + 15, 0);
		availableActionsTable.setHeaderVisible(true);

		availableNameColumn = new TableColumn(availableActionsTable, SWT.LEFT);
		availableCategoryColumn = new TableColumn(availableActionsTable,
				SWT.LEFT);
		availableNameColumn.setText("All Actions Name");
		availableNameColumn.setWidth(150);
		availableCategoryColumn.setText("Category");
		availableCategoryColumn.setWidth(120);

		availableNameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == availableNameColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = availableNameColumn;
				refreshAvailableActions();
			}
		});

		availableCategoryColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == availableCategoryColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = availableCategoryColumn;
				refreshAvailableActions();
			}
		});

		availableActionsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = availableActionsTable.getSelectionIndex();
				String selection = availableActionsTable.getItem(selectedIndex)
						.getText(0);
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
				int selectedScript = currentActionsTable.getSelectionIndex();
				if (selectedScript > 0) {
					TableItem[] items = currentActionsTable.getItems();
					for (int i = 0; i < items.length; i++) {
						RaptorAction action = ActionService.getInstance()
								.getAction(items[i].getText());
						if (i == selectedScript - 1) {
							action.setContainerOrder(container, i + 1);
						} else if (i == selectedScript) {
							action.setContainerOrder(container, i - 1);
						} else {
							action.setContainerOrder(container, i);
						}
						ActionService.getInstance().saveAction(action);
					}
					String selectedText = items[selectedScript].getText();
					items[selectedScript].setText(items[selectedScript - 1]
							.getText());
					items[selectedScript - 1].setText(selectedText);
					currentActionsTable.setSelection(selectedScript - 1);
					currentActionsTable.redraw();
				}
			}
		});
		Button downButton = new Button(buttonsComposite, SWT.PUSH);
		downButton.setImage(Raptor.getInstance().getIcon("down"));
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedScript = currentActionsTable.getSelectionIndex();
				if (selectedScript < currentActionsTable.getItemCount() - 1) {
					TableItem[] items = currentActionsTable.getItems();
					for (int i = 0; i < items.length; i++) {
						RaptorAction action = ActionService.getInstance()
								.getAction(items[i].getText());
						if (i == selectedScript + 1) {
							action.setContainerOrder(container, i - 1);
						} else if (i == selectedScript) {
							action.setContainerOrder(container, i + 1);
						} else {
							action.setContainerOrder(container, i);
						}
						ActionService.getInstance().saveAction(action);
					}
					String selectedText = items[selectedScript].getText();
					items[selectedScript].setText(items[selectedScript + 1]
							.getText());
					items[selectedScript + 1].setText(selectedText);
					currentActionsTable.setSelection(selectedScript + 1);
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
						.getItemCount());
				ActionService.getInstance().saveAction(separator);
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

		Label scriptLabel = new Label(composite, SWT.NONE);
		scriptLabel.setText("Script:");
		scriptLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));
		scriptText = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		scriptText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true,
				3, 1));
		scriptText.setText("\n\n\n\n\n\n");
		scriptText.setEditable(false);
		scriptText.setWordWrap(false);

		refreshCurrentActions();
		refreshAvailableActions();

		availableActionsTable.setSelection(0);
		loadControls(availableActionsTable.getItem(0).getText(0));

		return composite;
	}

	protected Comparator<RaptorAction> getCompatator() {
		if (lastStortedColumn == null) {
			lastStortedColumn = availableNameColumn;
			wasLastSortAscending = true;
			return RaptorAction.NAME_COMPARATOR_ASCENDING;
		} else if (lastStortedColumn == availableNameColumn) {
			return wasLastSortAscending ? RaptorAction.NAME_COMPARATOR_ASCENDING
					: RaptorAction.NAME_COMPARATOR_DESCENDING;
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
		TableItem[] inactiveItems = availableActionsTable.getItems();
		for (TableItem item : inactiveItems) {
			item.dispose();
		}

		RaptorAction[] availableActions = ActionService.getInstance()
				.getAllActions();
		Arrays.sort(availableActions, getCompatator());

		for (RaptorAction action : availableActions) {
			TableItem item = new TableItem(availableActionsTable, SWT.NONE);
			item.setText(new String[] { action.getName(),
					action.getCategory().toString() });
		}
	}

	protected void refreshCurrentActions() {
		TableItem[] currentItems = currentActionsTable.getItems();
		for (TableItem item : currentItems) {
			item.dispose();
		}

		for (RaptorAction action : ActionService.getInstance().getActions(
				container)) {
			TableItem item = new TableItem(currentActionsTable, SWT.NONE);
			item.setText(action.getName());
		}
	}
}
