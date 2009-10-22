package raptor.pref.page;

import java.util.Arrays;
import java.util.Comparator;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.action.AbstractRaptorAction;
import raptor.action.ActionUtils;
import raptor.action.RaptorAction;
import raptor.service.ActionService;
import raptor.swt.SWTUtils;

public class ActionKeyBindingsPage extends PreferencePage {
	protected Composite composite;
	protected Table actionsTable;
	protected Label nameText;
	protected Label descriptionText;
	protected Text keyStrokeText;
	protected TableColumn lastStortedColumn;
	protected TableColumn keystrokesColumn;
	protected TableColumn nameColumn;
	protected TableColumn descriptionColumn;
	protected TableColumn categoryColumn;
	protected boolean wasLastSortAscending;
	protected int modifierKeyCode = 0;
	protected boolean isModifierDown = false;

	public ActionKeyBindingsPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle("Action Key Bindings");
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

		Composite actionsTableComposite = new Composite(composite, SWT.NONE);
		actionsTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				false, false, 3, 1));
		actionsTable = new Table(actionsTableComposite, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		actionsTable.setHeaderVisible(true);

		keystrokesColumn = new TableColumn(actionsTable, SWT.LEFT);
		nameColumn = new TableColumn(actionsTable, SWT.LEFT);
		categoryColumn = new TableColumn(actionsTable, SWT.LEFT);
		descriptionColumn = new TableColumn(actionsTable, SWT.LEFT);
		keystrokesColumn.setText("Keystroke");
		keystrokesColumn.setWidth(70);
		nameColumn.setText("Action Name");
		nameColumn.setWidth(120);
		categoryColumn.setText("Category");
		categoryColumn.setWidth(100);
		descriptionColumn.setText("Description");
		descriptionColumn.setWidth(200);

		keystrokesColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				wasLastSortAscending = lastStortedColumn == null ? true
						: lastStortedColumn == keystrokesColumn ? !wasLastSortAscending
								: true;
				lastStortedColumn = keystrokesColumn;
				refreshActions();
			}
		});

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

		actionsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectedIndex = actionsTable.getSelectionIndex();
				String selection = actionsTable.getItem(selectedIndex).getText(
						1);
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
				AbstractRaptorAction action = (AbstractRaptorAction) ActionService
						.getInstance().getAction(nameText.getText());
				if (action != null) {
					if (StringUtils.isNotBlank(keyStrokeText.getText())) {
						action.setKeyCode(ActionUtils
								.keyBindingDescriptionToKeyCode(keyStrokeText
										.getText()));
						action
								.setModifierKey(ActionUtils
										.keyBindingDescriptionToKeyModifier(keyStrokeText
												.getText()));
					} else {
						action.setKeyCode(0);
						action.setModifierKey(0);
					}
				}
				ActionService.getInstance().saveAction(action);
				int indexBeforeSave = actionsTable.getSelectionIndex();
				refreshActions();
				if (indexBeforeSave != -1) {
					actionsTable.setSelection(indexBeforeSave);
					loadControls(actionsTable.getItem(
							actionsTable.getSelectionIndex()).getText(1));
				}
			}
		});

		refreshActions();
		actionsTable.setSelection(0);
		loadControls(actionsTable.getItem(actionsTable.getSelectionIndex())
				.getText(1));
		actionsTable.setSize(actionsTable.computeSize(SWT.DEFAULT, 300));
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
		} else if (lastStortedColumn == keystrokesColumn) {
			return wasLastSortAscending ? RaptorAction.KEYBINDING_COMPARATOR_ASCENDING
					: RaptorAction.KEYBINDING_COMPARATOR_DESCENDING;
		} else {
			return wasLastSortAscending ? RaptorAction.CATEGORY_COMPARATOR_ASCENDING
					: RaptorAction.CATEGORY_COMPARATOR_DESCENDING;
		}
	}

	protected void loadControls(String actionName) {
		RaptorAction currentAction = ActionService.getInstance().getAction(
				actionName);
		if (currentAction != null) {
			nameText.setText(currentAction.getName());
			descriptionText.setText(currentAction.getDescription());
			keyStrokeText
					.setText(ActionUtils.keyBindingToString(currentAction));
		}
	}

	protected void refreshActions() {
		TableItem[] currentItems = actionsTable.getItems();
		for (TableItem item : currentItems) {
			item.dispose();
		}

		RaptorAction[] scripts = ActionService.getInstance().getAllActions();
		Arrays.sort(scripts, getCompatator());

		for (RaptorAction action : scripts) {
			TableItem item = new TableItem(actionsTable, SWT.NONE);
			item.setText(new String[] { ActionUtils.keyBindingToString(action),
					action.getName(), action.getCategory().toString(),
					action.getDescription() });
		}
	}
}
